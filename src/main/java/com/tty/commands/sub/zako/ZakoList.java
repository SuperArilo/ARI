package com.tty.commands.sub.zako;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mojang.brigadier.Command;
import com.tty.Ari;
import com.tty.api.repository.PartitionKey;
import com.tty.api.utils.ComponentUtils;
import com.tty.command.LiteralArgumentCommand;
import com.tty.commands.args.zako.ZakoListArgs;
import com.tty.entity.WhitelistInstance;
import com.tty.api.annotations.command.CommandMeta;
import com.tty.api.annotations.command.LiteralCommand;
import com.tty.api.command.SuperHandsomeCommand;
import com.tty.api.dto.ComponentListPage;
import com.tty.api.repository.EntityRepository;
import com.tty.tool.ConfigUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@CommandMeta(displayName = "list", permission = "ari.command.zako.list", tokenLength = 2, allowConsole = true)
@LiteralCommand(directExecute = true)
public class ZakoList extends LiteralArgumentCommand {

    public static final Integer MAX_ZAKO_LIST_PAGE_SIZE = 10;

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of(new ZakoListArgs());
    }

    @Override
    public int execute(CommandSender sender, String[] args) {
        return Build_Zako_List(sender, 1);
    }

    public static int Build_Zako_List(CommandSender sender, Integer pageNum) {

        String baseCommand = "/ari zako list ";
        String suggestCommand = "/ari zako info ";

        CompletableFuture<Component> requesting = (sender instanceof Player player)
                ? ConfigUtils.t("function.zako.list-requesting", player)
                : ConfigUtils.t("function.zako.list-requesting");

        requesting.thenAcceptAsync(component -> Ari.instance.getScheduler().run(Ari.instance, i -> sender.sendMessage(component))).thenCompose(v -> {

            EntityRepository<WhitelistInstance> repository = Ari.REPOSITORY_MANAGER.get(WhitelistInstance.class);
            return repository.getList(pageNum, MAX_ZAKO_LIST_PAGE_SIZE, new LambdaQueryWrapper<>(WhitelistInstance.class), PartitionKey.global());

        }).thenCompose(result -> {

            List<WhitelistInstance> records = result.records();
            if (records.isEmpty()) {
                Ari.instance.getScheduler().run(Ari.instance, i -> sender.sendMessage(ComponentUtils.text(Ari.DATA_SERVICE.getValue("base.page-change.none-next"))));
                return CompletableFuture.completedFuture(null);
            }

            ComponentListPage dataPage = Ari.DATA_SERVICE.createComponentDataPage(
                    ConfigUtils.tAfter("function.zako.list-title"),
                    baseCommand + (pageNum == 1 ? pageNum : pageNum - 1),
                    baseCommand + (pageNum + 1),
                    (int) result.currentPage(),
                    (int) result.totalPages(),
                    (int) result.total());

            List<CompletableFuture<Void>> renderFutures = new ArrayList<>();
            for (WhitelistInstance instance : records) {

                String uuid = instance.getPlayerUUID();
                OfflinePlayer offlinePlayer = Bukkit.getServer().getOfflinePlayer(UUID.fromString(uuid));

                CompletableFuture<Component> renderList = Ari.PLACEHOLDER.renderList("server.player.zako.list-show", offlinePlayer);
                CompletableFuture<Component> unableFuture = Ari.PLACEHOLDER.render("server.player.zako.unable-record", offlinePlayer);

                CompletableFuture<Void> lineFuture = renderList.thenCombine(unableFuture, (e, i) -> {
                    Component t = e;
                    if (offlinePlayer.getName() == null) {
                        t = t.appendNewline().append(i);
                    }
                    return t.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, suggestCommand + uuid));
                }).thenAccept(dataPage::addLine);

                renderFutures.add(lineFuture);
            }

            return CompletableFuture.allOf(renderFutures.toArray(new CompletableFuture[0])).thenApply(v -> dataPage);
        }).thenAccept(dataPage -> {
            if (dataPage != null) {
                Ari.instance.getScheduler().run(Ari.instance, i -> sender.sendMessage(dataPage.build()));
            }
        }).exceptionally(ex -> {
            Ari.instance.getLog().error(ex, "query zako list error.");
            CompletableFuture<Component> errorMsg = (sender instanceof Player player)
                    ? ConfigUtils.t("function.zako.list-request-error", player)
                    : ConfigUtils.t("function.zako.list-request-error");
            errorMsg.thenAccept(msg -> Ari.instance.getScheduler().run(Ari.instance, i -> sender.sendMessage(msg)));
            return null;
        });
        return Command.SINGLE_SUCCESS;
    }

    @Override
    protected boolean isDisabledInGame() {
        return false;
    }

}
