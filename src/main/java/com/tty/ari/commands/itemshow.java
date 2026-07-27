package com.tty.ari.commands;

import com.tty.api.annotations.command.CommandMeta;
import com.tty.api.annotations.command.LiteralCommand;
import com.tty.api.command.SuperHandsomeCommand;
import com.tty.ari.Ari;
import com.tty.ari.command.LiteralArgumentCommand;
import com.tty.ari.enumType.lang.PlaceholderPlayer;
import com.tty.ari.enumType.lang.PlaceholderShowItem;
import com.tty.ari.tool.ConfigUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;

@CommandMeta(displayName = "itemshow", permission = "ari.command.itemshow", tokenLength = 1)
@LiteralCommand(directExecute = true)
public class itemshow extends LiteralArgumentCommand {

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) return;
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.isEmpty()) {
            ConfigUtils.t("function.itemshow.no-item-in-hand", player).thenAccept(t -> Ari.instance.getScheduler().run(i -> sender.sendMessage(t)));
        } else {
            Bukkit.getServer().broadcast(ConfigUtils.tAfter("function.itemshow.show-to-players",
                    Map.of(
                            PlaceholderShowItem.SHOW_ITEM_UNRESOLVED.getType(), Ari.instance.getEngine().setHoverItemText(player.getInventory().getItemInMainHand()),
                            PlaceholderPlayer.PLAYER_NAME.getType(), player.displayName()
                    )));
        }
    }

    @Override
    protected boolean isEnableInGame() {
        return true;
    }

}
