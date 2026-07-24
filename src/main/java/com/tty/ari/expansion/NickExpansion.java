package com.tty.ari.expansion;

import com.tty.api.enumType.PlaceholderTypeEnum;
import com.tty.ari.Ari;
import com.tty.ari.enumType.lang.PlaceholderPlayer;
import me.clip.placeholderapi.PAPIComponents;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class NickExpansion extends PlaceholderExpansion {

    @Override
    public @NotNull String getIdentifier() {
        return Ari.instance.getName();
    }

    @Override
    public @NotNull String getAuthor() {
        return "Arilo007";
    }

    @Override
    public @NotNull String getVersion() {
        return "v1.0";
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) return null;

        if (params.equals(PlaceholderPlayer.PLAYER_NAME_PREFIX.getType())
                || params.equals(PlaceholderPlayer.PLAYER_NAME_SUFFIX.getType())) {
            Component join = Component.empty();
            try {
                join = Ari.PLACEHOLDER.rawRender(PlaceholderTypeEnum.testBuild(params), player).get(20, TimeUnit.MILLISECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                if (e instanceof TimeoutException) {
                    Ari.instance.getLog().debug(e);
                }
            }
            return MiniMessage.miniMessage().serializeOr(PAPIComponents.setPlaceholders(player, join), "");
        }

        return null;
    }

}
