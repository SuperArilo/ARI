package com.tty.commands;

import com.tty.command.LiteralArgumentCommand;
import com.tty.commands.sub.zako.*;
import com.tty.api.annotations.command.CommandMeta;
import com.tty.api.annotations.command.LiteralCommand;
import com.tty.api.command.SuperHandsomeCommand;
import org.bukkit.command.CommandSender;

import java.util.List;

@CommandMeta(displayName = "zako", permission = "ari.command.zako", tokenLength = 2)
@LiteralCommand
public class zako extends LiteralArgumentCommand {

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of(
                new ZakoAdd(),
                new ZakoList(),
                new ZakoInfo(),
                new ZakoBan(),
                new ZakoUnBan(),
                new ZakoRemove());
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
    }

}
