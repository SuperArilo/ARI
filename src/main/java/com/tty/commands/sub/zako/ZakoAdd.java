package com.tty.commands.sub.zako;

import com.tty.commands.args.zako.ZakoAddArgs;
import com.tty.lib.command.BaseLiteralArgumentLiteralCommand;
import com.tty.lib.command.SuperHandsomeCommand;
import org.bukkit.command.CommandSender;

import java.util.List;

public class ZakoAdd extends BaseLiteralArgumentLiteralCommand {

    public ZakoAdd() {
        super(true, 2);
    }

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of(new ZakoAddArgs());
    }

    @Override
    public void execute(CommandSender sender, String[] args) {

    }

    @Override
    public String name() {
        return "add";
    }

    @Override
    public String permission() {
        return "ari.command.zako.add";
    }
}
