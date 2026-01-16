package com.tty.dto;

import com.tty.Ari;
import com.tty.function.TimeManager;
import com.tty.lib.Lib;
import com.tty.lib.Log;
import com.tty.lib.enum_type.TimePeriod;
import com.tty.lib.tool.ComponentUtils;
import lombok.Getter;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;


public class SleepingWorld {

    private final World world;
    @Getter
    private final TimeManager timeManager;
    private boolean skipNightOver = false;

    public SleepingWorld(World world) {
        this.world = world;
        this.timeManager = TimeManager.build(world, 20L, this.getTickIncrement());
    }

    public void update() {
        boolean condition = this.playerCondition(this.world);
        if (!condition) {
            this.timeManager.setAddTick(Math.min(this.getSleepPlayers() * this.getTickIncrement(), this.getMaxTickIncrement()));
            this.sendTipsActionBar();
        }
        if (this.timeManager.getTask() != null && condition) {
            this.timeManager.cancelTask();
        } else if(this.timeManager.getTask() == null && !condition) {
            this.skipNightOver = false;
            this.timeManager.timeAutomaticallyPasses(10, i -> {
                for (Player player : this.world.getPlayers()) {
                    if (this.playerCondition(player.getWorld()) || !player.isSleeping() || !player.isDeeplySleeping()) continue;

                    Ari.PLACEHOLDER.renderAsync("server.time.skip-to-night", player).thenAccept(c -> Lib.Scheduler.runAtEntity(
                            Ari.instance,
                            player,
                            b ->
                                    player.showTitle(
                                            ComponentUtils.setPlayerTitle(
                                                    timeManager.tickToTime(i),
                                                    c,
                                                    0L,
                                                    1000L,
                                                    500L)
                                    ),
                            null));
                }
            });
        }
    }

    private void sendTipsActionBar() {
        for (Player player : this.world.getPlayers()) {
            if (!player.isSleeping()) {
                Ari.PLACEHOLDER.renderAsync("server.time.report-status", player).thenAccept(player::sendActionBar);

            }
        }
    }

    /**
     * 判断对应世界里玩家数量是否满足skip夜晚的条件
     * @param world 指定世界
     * @return true 代表满足原版跳过夜晚的条件，false 则是满足自定义skip夜晚的条件
     */

    private boolean playerCondition(@NotNull World world) {
        Integer gameRuleValue = world.getGameRuleValue(GameRule.PLAYERS_SLEEPING_PERCENTAGE);
        if (gameRuleValue != null) {
            var a = gameRuleValue * world.getPlayers().size();
            //这个世界需要睡下的人
            int numSleepersNeeded = Math.max(a / 100, 1);
            //已经睡下的人
            long sleepers = this.getSleepPlayers();
            long worldTime = world.getTime();
            Log.debug("world time: {}, abs: {}, need_to_sleep: {}, sleep_now: {}", world.getTime(), TimePeriod.WAKE_UP.getEnd(), numSleepersNeeded, sleepers);
            if ((worldTime >= TimePeriod.WAKE_UP.getEnd() ||
                    (worldTime > 0 && worldTime < TimePeriod.SUNRISE.getEnd())) ||
                    (world.isThundering() || world.hasStorm()) &&
                            !this.skipNightOver) {
                Lib.Scheduler.run(Ari.instance, i-> {
                    world.setStorm(false);
                    world.setThundering(false);
                });
                this.skipNightOver = true;
                return true;
            }
            if (sleepers == 0) return true;
            if (numSleepersNeeded == sleepers) {
                this.timeManager.cancelTask();
            }
            return numSleepersNeeded == sleepers;
        }
        return true;
    }

    private long getSleepPlayers() {
        return this.world.getPlayers().stream().filter(HumanEntity::isSleeping).count();
    }

    private long getTickIncrement() {
        return Ari.instance.getConfig().getLong("server.skip-night.tick-increment", 5L);
    }

    private long getMaxTickIncrement() {
        return Ari.instance.getConfig().getLong("server.skip-night.max-tick-increment", 100L);
    }
}
