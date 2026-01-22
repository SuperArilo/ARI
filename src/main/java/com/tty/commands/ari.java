package com.tty.commands;

import com.tty.commands.sub.Reload;
import com.tty.lib.command.BaseLiteralArgumentLiteralCommand;
import com.tty.lib.command.SuperHandsomeCommand;
import org.bukkit.command.CommandSender;

import java.util.List;

public class ari extends BaseLiteralArgumentLiteralCommand {

    public ari() {
        super(true, 1);
    }

    @Override
    public List<SuperHandsomeCommand> thenCommands() {
        return List.of(
            new back(),
            new home(),
            new itemlore(),
            new itemname(),
            new itemshow(),
            new rtp(),
            new sethome(),
            new setspawn(),
            new setwarp(),
            new spawn(),
            new time(),
            new tpa(),
            new tpaaccept(),
            new tpahere(),
            new tparefuse(),
            new warp(),
            new zako(),
            new Reload()
        );
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
    }

    @Override
    public String name() {
        return "ari";
    }

    @Override
    public String permission() {
        return "";
    }
}
