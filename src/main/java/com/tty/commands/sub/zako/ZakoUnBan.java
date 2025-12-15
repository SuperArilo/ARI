package com.tty.commands.sub.zako;

import com.tty.commands.args.zako.ZakoUnBanPlayerArgs;
import com.tty.lib.command.BaseLiteralArgumentLiteralCommand;
import com.tty.lib.command.SuperHandsomeCommand;
import org.bukkit.command.CommandSender;

import java.util.List;

public class ZakoUnBan extends BaseLiteralArgumentLiteralCommand {

    public ZakoUnBan() {
        super(true, 2);
    }

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of(new ZakoUnBanPlayerArgs());
    }

    @Override
    public String name() {
        return "unban";
    }

    @Override
    public String permission() {
        return "ari.command.zako.unban";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {

    }
}
