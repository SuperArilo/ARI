package com.tty.commands.sub.zako;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tty.Ari;
import com.tty.command.LiteralArgumentCommand;
import com.tty.commands.args.zako.ZakoListArgs;
import com.tty.entity.WhitelistInstance;
import com.tty.enumType.lang.LangZakoList;
import com.tty.api.Log;
import com.tty.api.annotations.CommandMeta;
import com.tty.api.annotations.LiteralCommand;
import com.tty.api.command.SuperHandsomeCommand;
import com.tty.api.dto.ComponentListPage;
import com.tty.enumType.FilePath;
import com.tty.api.repository.EntityRepository;
import com.tty.tool.ConfigUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
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
    public void execute(CommandSender sender, String[] args) {
        Build_Zako_List(sender, 1);
    }

    public static void Build_Zako_List(CommandSender sender, Integer pageNum) {
        String baseCommand = "/ari zako list ";
        String suggestCommand = "/ari zako info ";

        CompletableFuture<Component> future = (sender instanceof Player player) ? ConfigUtils.t("function.zako.list-requesting", player):ConfigUtils.t("function.zako.list-requesting");
        future.thenAccept(component -> {
            sender.sendMessage(component);

            EntityRepository<WhitelistInstance> repository = Ari.REPOSITORY_MANAGER.get(WhitelistInstance.class);
            repository.getList(pageNum, MAX_ZAKO_LIST_PAGE_SIZE, new LambdaQueryWrapper<>(WhitelistInstance.class)).thenAccept(result -> {
                        List<WhitelistInstance> records = result.records();
                        if (records.isEmpty()) {
                            sender.sendMessage(Ari.COMPONENT_SERVICE.text(Ari.DATA_SERVICE.getValue("base.page-change.none-next")));
                            return;
                        }
                        ComponentListPage dataPage = Ari.DATA_SERVICE
                                .createComponentDataPage(
                                        ConfigUtils.tAfter("function.zako.list-title"),
                                        baseCommand + (pageNum == 1 ? pageNum:pageNum - 1),
                                        baseCommand + (pageNum + 1),
                                        (int) result.currentPage(),
                                        (int) result.totalPages(),
                                        (int) result.total());

                        for (WhitelistInstance instance : records) {
                            String instancePlayerUUID = instance.getPlayerUUID();
                            OfflinePlayer offlinePlayer = Bukkit.getServer().getOfflinePlayer(UUID.fromString(instancePlayerUUID));
                            String name = offlinePlayer.getName();
                            if (name == null) {
                                Log.debug("uuid {} player is null. the possible reason is that the player has not logged into the server.", instancePlayerUUID);
                            }
                            TextComponent set = Ari.COMPONENT_SERVICE.setClickEventText(Ari.C_INSTANCE.getValue("server.player.zako." + (name == null ? "unable-record":"list-show"), FilePath.LANG),
                                    Map.of(LangZakoList.ZAKO_LIST_ITEM_NAME.getType(), Component.text(name == null ? instancePlayerUUID:name)),
                                    ClickEvent.Action.RUN_COMMAND,
                                    suggestCommand + instancePlayerUUID);
                            dataPage.addLine(set);
                        }

                        sender.sendMessage(dataPage.build());
                    })
                    .exceptionally(i -> {
                        Log.error(i);
                        if (sender instanceof Player player) {
                            ConfigUtils.t("function.zako.list-request-error", player).thenAccept(sender::sendMessage);
                        } else {
                            ConfigUtils.t("function.zako.list-request-error").thenAccept(sender::sendMessage);
                        }
                        return null;
                    });

        });
    }

}
