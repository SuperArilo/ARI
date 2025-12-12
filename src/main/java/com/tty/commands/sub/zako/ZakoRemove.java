package com.tty.commands.sub.zako;

import com.tty.commands.args.zako.ZakoRemoveArgs;
import com.tty.lib.command.BaseLiteralArgumentLiteralCommand;
import com.tty.lib.command.SuperHandsomeCommand;
import org.bukkit.command.CommandSender;

import java.util.List;

public class ZakoRemove extends BaseLiteralArgumentLiteralCommand {

    public ZakoRemove() {
        super(true, 2);
    }

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of(new ZakoRemoveArgs());
    }

    @Override
    public void execute(CommandSender sender, String[] args) {

    }

    @Override
    public String name() {
        return "remove";
    }

    @Override
    public String permission() {
        return "ari.command.zako.remove";
    }
}
