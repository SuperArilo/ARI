package com.tty.command;

import com.tty.Ari;
import com.tty.api.command.BaseRequiredArgumentCommand;
import com.tty.api.utils.ComponentUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;


public abstract class RequiredArgumentCommand<T> extends BaseRequiredArgumentCommand<T> {

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

}
