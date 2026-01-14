package com.tty.commands.sub.zako;

import com.tty.Ari;
import com.tty.commands.args.zako.ZakoListArgs;
import com.tty.entity.WhitelistInstance;
import com.tty.function.WhitelistManager;
import com.tty.lib.Log;
import com.tty.lib.command.BaseLiteralArgumentLiteralCommand;
import com.tty.lib.command.SuperHandsomeCommand;
import com.tty.lib.dto.ComponentListPage;
import com.tty.enumType.FilePath;
import com.tty.lib.enum_type.LangType;
import com.tty.lib.services.EntityRepository;
import com.tty.lib.tool.ComponentUtils;
import com.tty.tool.ConfigUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ZakoList extends BaseLiteralArgumentLiteralCommand {

    public static Integer MAX_ZAKO_LIST_PAGE_SIZE = 10;

    public ZakoList() {
        super(true, 2, true);
    }

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of(new ZakoListArgs());
    }

    @Override
    public String name() {
        return "list";
    }

    @Override
    public String permission() {
        return "ari.command.zako.list";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Build_Zako_List(sender, 1);
    }

    public static void Build_Zako_List(CommandSender sender, Integer pageNum) {
        String baseCommand = "/ari zako list ";
        String suggestCommand = "/ari zako info ";

        sender.sendMessage(ConfigUtils.t("function.zako.list-requesting"));
        EntityRepository<Object, WhitelistInstance> repository = Ari.REPOSITORY_MANAGER.get(WhitelistInstance.class);
        repository.getList(pageNum, 10, null).thenAccept(result -> {
            List<WhitelistInstance> records = result.getRecords();
            if (records.isEmpty()) {
                sender.sendMessage(ComponentUtils.text(Ari.instance.dataService.getValue("base.page-change.none-next")));
                return;
            }
            ComponentListPage dataPage = Ari.instance.dataService
                    .createComponentDataPage(
                            ConfigUtils.t("function.zako.list-title"),
                            baseCommand + (pageNum == 1 ? pageNum:pageNum - 1),
                            baseCommand + (pageNum + 1),
                            (int) result.getCurrentPage(),
                            (int) result.getTotalPages(),
                            (int) result.getTotal());

            for (WhitelistInstance instance : records) {
                String instancePlayerUUID = instance.getPlayerUUID();
                OfflinePlayer offlinePlayer = Bukkit.getServer().getOfflinePlayer(UUID.fromString(instancePlayerUUID));
                String name = offlinePlayer.getName();
                if (name == null) {
                    Log.debug("uuid %s player is null. the possible reason is that the player has not logged into the server.", instancePlayerUUID);
                }
                TextComponent set = ComponentUtils.setClickEventText(Ari.C_INSTANCE.getValue("server.player.zako." + (name == null ? "unable-record":"list-show"), FilePath.LANG),
                        Map.of(LangType.PLAYER_NAME.getType(), Component.text(name == null ? instancePlayerUUID:name)),
                        ClickEvent.Action.RUN_COMMAND,
                        suggestCommand + instancePlayerUUID);
                dataPage.addLine(set);
            }

            sender.sendMessage(dataPage.build());
        })
        .exceptionally(i -> {
            Log.error("zako list error ", i);
            sender.sendMessage(ConfigUtils.t("function.zako.list-request-error"));
            return null;
        });
    }

}
