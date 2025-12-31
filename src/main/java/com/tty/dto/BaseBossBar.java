package com.tty.dto;

import lombok.Getter;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;


public class BaseBossBar {

    protected final BossBar bossBar;
    @Getter
    protected boolean removed = false;

    protected BaseBossBar() {
        this.bossBar = BossBar.bossBar(Component.text(), 1.0f, BossBar.Color.YELLOW, BossBar.Overlay.NOTCHED_10);
    }

    protected BaseBossBar(Component title) {
        this.bossBar = BossBar.bossBar(title, 1.0f, BossBar.Color.YELLOW, BossBar.Overlay.NOTCHED_10);
    }

    protected BaseBossBar(Component title, float progress) {
        this.bossBar = BossBar.bossBar(title, progress, BossBar.Color.YELLOW, BossBar.Overlay.NOTCHED_10);
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

    /**
     * 获取进度条值
     * @return 值 0.0 ~ 1.0
     */
    public float getProgress() {
        return this.bossBar.progress();
    }

    public void show(@NotNull Audience audience) {
        audience.showBossBar(this.bossBar);
    }

    public void remove(@NotNull Audience audience) {
        if (this.removed) return;
        this.removed = true;
        audience.hideBossBar(this.bossBar);
    }



}
