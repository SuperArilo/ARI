package com.tty.dto;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

public class BaseBossBar {

    protected final Player player;
    protected final BossBar bossBar;
    public boolean removed = false;

    protected BaseBossBar(Player player) {
        this.player = player;
        this.bossBar = BossBar.bossBar(Component.text(), 1.0f, BossBar.Color.YELLOW, BossBar.Overlay.NOTCHED_10);
        this.player.showBossBar(this.bossBar);

    }

    /**
     * 设置当前 boos bar 的进度条颜色
     * @param color 颜色
     */
    public void setColor(BossBar.Color color) {
        this.bossBar.color(color);
    }

    /**
     * 设置 boos bar 的名称
     * @param component 名称
     */
    public void setName(Component component) {
        this.bossBar.name(component);
    }

    /**
     * 设置进度条进度值
     * @param value 0.0 ~ 1.0
     */
    public void setProgress(float value) {
        this.bossBar.progress(Math.max(0.0f, Math.min(1.0f, value)));

    }

    public void remove() {
        if (removed) return;
        removed = true;
        this.player.hideBossBar(this.bossBar);

    }



}
