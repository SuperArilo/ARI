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

import java.util.concurrent.TimeUnit;

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
            Component join = Ari.PLACEHOLDER.rawRender(PlaceholderTypeEnum.testBuild(params), player).orTimeout(20, TimeUnit.MILLISECONDS).join();
            return MiniMessage.miniMessage().serializeOr(PAPIComponents.setPlaceholders(player, join), "");
        }

        return null;
    }

}
