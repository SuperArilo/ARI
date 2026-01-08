package com.tty.commands;

import com.tty.commands.sub.enchant.EnchantShowList;
import com.tty.lib.command.BaseLiteralArgumentLiteralCommand;
import com.tty.lib.command.SuperHandsomeCommand;
import org.bukkit.command.CommandSender;

import java.util.List;

public class enchant extends BaseLiteralArgumentLiteralCommand {

    public enchant() {
        super(2);
    }

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of(new EnchantShowList());
    }

    @Override
    public String name() {
        return "enchant";
    }

    @Override
    public String permission() {
        return "ari.command.enchant";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {

    }
}
