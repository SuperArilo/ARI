package com.tty.commands;

import com.tty.command.LiteralArgumentCommand;
import com.tty.commands.sub.enchant.EnchantShowList;
import com.tty.api.annotations.CommandMeta;
import com.tty.api.annotations.LiteralCommand;
import com.tty.api.command.SuperHandsomeCommand;
import org.bukkit.command.CommandSender;

import java.util.List;

@CommandMeta(displayName = "enchant", permission = "ari.command.enchant", tokenLength = 2)
@LiteralCommand
public class enchant extends LiteralArgumentCommand {

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of(new EnchantShowList());
    }

    @Override
    public void execute(CommandSender sender, String[] args) {

    }

}
