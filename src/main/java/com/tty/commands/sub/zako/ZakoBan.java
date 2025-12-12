package com.tty.commands.sub.zako;

import com.tty.commands.args.zako.ban.ZakoBanPlayer;
import com.tty.lib.command.BaseLiteralArgumentLiteralCommand;
import com.tty.lib.command.SuperHandsomeCommand;
import org.bukkit.command.CommandSender;

import java.util.List;

public class ZakoBan extends BaseLiteralArgumentLiteralCommand {

    public ZakoBan() {
        super(true, 2);
    }

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of(new ZakoBanPlayer());
    }

    @Override
    public void execute(CommandSender sender, String[] args) {

    }

    @Override
    public String name() {
        return "ban";
    }

    @Override
    public String permission() {
        return "ari.command.zako.ban";
    }
}
