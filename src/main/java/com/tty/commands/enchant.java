package com.tty.commands;

import com.tty.commands.sub.enchant.EnchantShowList;
import com.tty.lib.annotations.CommandMeta;
import com.tty.lib.annotations.LiteralCommand;
import com.tty.lib.command.BaseLiteralArgumentLiteralCommand;
import com.tty.lib.command.SuperHandsomeCommand;
import org.bukkit.command.CommandSender;

import java.util.List;

@CommandMeta(displayName = "enchant", permission = "ari.command.enchant", tokenLength = 2)
@LiteralCommand
public class enchant extends BaseLiteralArgumentLiteralCommand {

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of(new EnchantShowList());
    }

    @Override
    public void execute(CommandSender sender, String[] args) {

    }
}
