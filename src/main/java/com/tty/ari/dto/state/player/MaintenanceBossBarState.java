package com.tty.ari.dto.state.player;

import com.tty.api.state.State;
import lombok.Getter;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

public class MaintenanceBossBarState extends State {

    @Getter
    private final Component title;
    @Getter
    private final float progress;
    @Getter
    private final BossBar.Color color;
    @Getter
    @NotNull
    private final BossBar bossBar;

    public MaintenanceBossBarState(Entity owner, @NotNull Component title, float progress, @NotNull BossBar.Color color, int max_count) {
        super(owner, max_count);
        this.title = title;
        this.progress = progress;
        this.color = color;
        this.bossBar = BossBar.bossBar(title, progress, color, BossBar.Overlay.NOTCHED_10);
    }

}
