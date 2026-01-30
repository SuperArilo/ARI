package com.tty.commands;

import com.tty.commands.sub.Reload;
import com.tty.api.annotations.CommandMeta;
import com.tty.api.annotations.LiteralCommand;
import com.tty.api.command.BaseLiteralArgumentLiteralCommand;
import com.tty.api.command.SuperHandsomeCommand;
import org.bukkit.command.CommandSender;

import java.util.List;

@CommandMeta(displayName = "ari", allowConsole = true, tokenLength = 1)
@LiteralCommand
public class ari extends BaseLiteralArgumentLiteralCommand {

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
            new Reload()
        );
    }

    @Override
    public void execute(CommandSender sender, String[] args) {}

}
