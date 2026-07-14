package com.tty.ari.commands.sub.zako;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tty.api.annotations.command.CommandMeta;
import com.tty.api.annotations.command.LiteralCommand;
import com.tty.api.command.SuperHandsomeCommand;
import com.tty.api.dto.ComponentListPage;
import com.tty.api.repository.PartitionKey;
import com.tty.ari.Ari;
import com.tty.ari.command.LiteralArgumentCommand;
import com.tty.ari.commands.args.zako.ZakoListArgs;
import com.tty.ari.entity.WhitelistInstance;
import com.tty.ari.tool.ConfigUtils;
import com.tty.ari.tool.PlayerCache;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
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

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of(new ZakoListArgs());
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Build_Zako_List(sender, 1);
    }

    public static void Build_Zako_List(CommandSender sender, Integer pageNum) {

        String baseCommand = "/" + Ari.instance.getName() + " zako list ";
        String suggestCommand = "/" + Ari.instance.getName() +" zako info ";

        ConfigUtils.t("function.zako.list-requesting").thenAccept(component -> Ari.instance.getScheduler().run(i -> sender.sendMessage(component)))
        .thenCompose(v -> Ari.REPOSITORY_MANAGER.get(WhitelistInstance.class).getList(pageNum, 10, new LambdaQueryWrapper<>(WhitelistInstance.class).orderByDesc(WhitelistInstance::getAddTime), PartitionKey.global())).thenCompose(result -> {
            List<WhitelistInstance> records = result.records();
            if (records.isEmpty()) {
                Ari.instance.getScheduler().run(i -> sender.sendMessage(Ari.instance.getComponentTool().text(Ari.DATA_SERVICE.getValue("base.page-change.none-next"))));
                return CompletableFuture.completedFuture(null);
            }

            ComponentListPage dataPage = Ari.DATA_SERVICE.createComponentDataPage(
                    ConfigUtils.tAfter("function.zako.page-title.whitelist"),
                    baseCommand + (pageNum == 1 ? pageNum : pageNum - 1),
                    baseCommand + (pageNum + 1),
                    (int) result.currentPage(),
                    (int) result.totalPages(),
                    (int) result.total());

            List<CompletableFuture<Component>> lineFutures = new ArrayList<>();

            for (WhitelistInstance instance : records) {
                String uuid = instance.getPlayerUUID();
                OfflinePlayer offlinePlayer = PlayerCache.getPlayer(UUID.fromString(uuid));
                lineFutures.add(ConfigUtils.tList("server.player.zako.list-show.whitelist", offlinePlayer).thenCombine(ConfigUtils.t("server.player.zako.unable-record", offlinePlayer), (e, i) -> {
                    Component t = e;
                    if (offlinePlayer.getName() == null) {
                        t = t.appendNewline().append(i);
                    }
                    return t.clickEvent(ClickEvent.runCommand(suggestCommand + uuid));
                }));
            }
            return CompletableFuture.allOf(lineFutures.toArray(new CompletableFuture[0])).thenApply(v -> {
                        lineFutures.stream().map(CompletableFuture::join).forEach(dataPage::addLine);
                        return dataPage;
            });
        }).thenAccept(dataPage -> {
            if (dataPage != null) {
                Ari.instance.getScheduler().run(i -> sender.sendMessage(dataPage.build()));
            }
        })
        .exceptionally(ex -> {
            Ari.instance.getLog().error(ex, "query zako list error.");
            CompletableFuture<Component> errorMsg = (sender instanceof Player player)
                    ? ConfigUtils.t("function.zako.list-request-error", player)
                    : ConfigUtils.t("function.zako.list-request-error");
            errorMsg.thenAccept(msg -> Ari.instance.getScheduler().run(i -> sender.sendMessage(msg)));
            return null;
        });
    }

    @Override
    protected boolean isEnableInGame() {
        return true;
    }

}
