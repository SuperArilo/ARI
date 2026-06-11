package com.tty.ari.command;

import com.tty.api.utils.PublicFunctionUtils;
import com.tty.ari.Ari;
import com.tty.api.command.BaseRequiredArgumentCommand;
import com.tty.api.utils.ComponentUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;


public abstract class RequiredArgumentCommand<T> extends BaseRequiredArgumentCommand<T> {

    protected RequiredArgumentCommand() {
        super(Ari.instance);
    }

    protected boolean getDisableStatus(YamlConfiguration configuration) {
        return !configuration.getBoolean("main.enable", false);
    }

    @Override
    protected @NotNull Component tokenNotAllow() {
        return ComponentUtils.text(Ari.DATA_SERVICE.getValue("function.public.fail"));
    }

    @Override
    protected @NotNull Component onlyUseInGame() {
        return ComponentUtils.text(Ari.DATA_SERVICE.getValue("function.public.not-player"));
    }

    @Override
    protected boolean havePermission(CommandSender sender, String permission) {
        return Ari.PERMISSION_SERVICE.hasPermission(sender, permission);
    }

    @Override
    protected @NotNull Component disableInGame() {
        return ComponentUtils.text(Ari.DATA_SERVICE.getValue("base.command.disabled"));
    }

    protected Set<String> getExcludeMePlayerList(CommandSender sender, String[] args) {
        Collection<? extends Player> onlinePlayers = new ArrayList<>(Bukkit.getOnlinePlayers());
        if (onlinePlayers.isEmpty()) return Collections.emptySet();
        Set<String> otherPlayers = onlinePlayers.stream()
                .map(Player::getName)
                .filter(name -> !(sender instanceof Player) || !name.equalsIgnoreCase(sender.getName()))
                .collect(Collectors.toSet());
        if (args.length == 1) return otherPlayers;
        return PublicFunctionUtils.tabList(args[1], otherPlayers);
    }

}
