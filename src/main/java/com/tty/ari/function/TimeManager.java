package com.tty.ari.function;

import com.tty.ari.Ari;
import com.tty.api.scheduler.RunTask;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.World;

import java.util.function.Consumer;

public class TimeManager {

    private final World world;
    @Setter
    @Getter
    private long delay;
    @Setter
    @Getter
    private volatile long addTick;
    @Getter
    private RunTask runTask;

    private final long[] counter = {0};

    protected TimeManager(World world) {
        this.world = world;
        this.delay = 1L;
        this.addTick = 100L;
    }

    protected TimeManager(long delay, long addTick, World world) {
        this.delay = delay;
        this.addTick = addTick;
        this.world = world;
    }

    public void timeSet(long tick, Consumer<Long> consumer) {
        this.runTask = Ari.instance.getScheduler().runAtFixedRate(
                i -> {
                long currentTime = this.world.getTime();
                long delta = (tick - currentTime + 24000) % 24000;
                if (delta == 0) {
                    this.cancelTask();
                }
                long add = Math.min(delta, this.addTick);
                if (add == delta) {
                    this.cancelTask();
                }
                long nowTime = currentTime + add;
                this.world.setFullTime(nowTime);
                if (consumer != null) {
                    consumer.accept(nowTime);
                }
            },
            this.delay,
            1L
        );
    }

    public void timeSet(long targetTimeTick) {
        this.timeSet(targetTimeTick, null);
    }

    public void timeAutomaticallyPasses(Consumer<Long> consumer) {
        this.timeAutomaticallyPasses(0, consumer);
    }

    public void timeAutomaticallyPasses(long delayRate, Consumer<Long> consumer) {
        this.runTask = Ari.instance.getScheduler().runAtFixedRate(
                t -> {
                long newTime = this.world.getTime() + this.addTick;
                this.world.setFullTime(newTime);
                if (consumer != null && counter[0]++ % delayRate == 0) {
                    consumer.accept(newTime);
                }
            },
            1L,
            1L
        );
    }

    public String tickToTime(long tick) {
        long adjustedTick = tick % 24000;
        int hours = (int) ((adjustedTick / 1000 + 6) % 24);
        int minutes = (int) ((adjustedTick % 1000) * 60 / 1000);
        return String.format("%02d:%02d %s", (hours == 0) ? 12 : hours % 12, minutes, (hours >= 12) ? "PM" : "AM");
    }

    public void cancelTask() {
        if(this.runTask != null) {
            this.runTask.cancel();
            this.runTask = null;
        }
    }

    public static TimeManager build(World world) {
        return new TimeManager(world);
    }

    public static TimeManager build(World world, long delay,long addTick) {
        return new TimeManager(delay, addTick, world);
    }
}
