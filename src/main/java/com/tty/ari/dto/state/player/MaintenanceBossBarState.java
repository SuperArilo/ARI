package com.tty.ari.dto.state.player;

import com.tty.api.state.State;
import com.tty.ari.tool.ConfigUtils;
import lombok.Getter;
import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

public class MaintenanceBossBarState extends State {

    @Getter
    @NotNull
    private final BossBar bossBar;

    public MaintenanceBossBarState(Entity owner) {
        super(owner, Integer.MAX_VALUE);
        this.bossBar = BossBar.bossBar(ConfigUtils.tAfter("server.maintenance.on-enable"), 1.0f, BossBar.Color.BLUE, BossBar.Overlay.NOTCHED_10);
    }

}
