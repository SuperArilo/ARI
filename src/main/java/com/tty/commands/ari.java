package com.tty.commands;

import com.tty.command.LiteralArgumentCommand;
import com.tty.commands.sub.Reload;
import com.tty.api.annotations.command.CommandMeta;
import com.tty.api.annotations.command.LiteralCommand;
import com.tty.api.command.SuperHandsomeCommand;
import org.bukkit.command.CommandSender;

import java.util.List;

@CommandMeta(displayName = "ari", allowConsole = true, tokenLength = 1)
@LiteralCommand
public class ari extends LiteralArgumentCommand {

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
            new infinitytotem(),
            new enderchest(),
            new Reload()
        );
    }

    @Override
    public void execute(CommandSender sender, String[] args) {}

}
