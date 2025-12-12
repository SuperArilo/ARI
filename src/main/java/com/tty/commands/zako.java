package com.tty.commands;

import com.tty.commands.sub.zako.ZakoAdd;
import com.tty.commands.sub.zako.ZakoBan;
import com.tty.commands.sub.zako.ZakoInfo;
import com.tty.commands.sub.zako.ZakoRemove;
import com.tty.lib.command.BaseLiteralArgumentLiteralCommand;
import com.tty.lib.command.SuperHandsomeCommand;
import org.bukkit.command.CommandSender;

import java.util.List;

public class zako extends BaseLiteralArgumentLiteralCommand {

    public zako() {
        super(true, 2);
    }

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of(new ZakoAdd(true),
                new ZakoInfo(true),
                new ZakoBan(true),
                new ZakoRemove(true));
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
    }

    @Override
    public String name() {
        return "zako";
    }

    @Override
    public String permission() {
        return "ari.command.zako";
    }
}
