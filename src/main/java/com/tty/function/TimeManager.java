package com.tty.function;

import com.tty.Ari;
import com.tty.lib.Lib;
import com.tty.lib.task.CancellableTask;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.World;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class TimeManager {

    private final World world;
    @Setter
    @Getter
    private long delay;
    @Setter
    @Getter
    private long addTick;
    @Getter
    private final AtomicReference<CancellableTask> scheduledTask = new AtomicReference<>();

    private TimeManager(World world) {
        this.world = world;
        this.delay = 1L;
        this.addTick = 100L;
    }

    private TimeManager(long delay, long addTick, World world) {
        this.delay = delay;
        this.addTick = addTick;
        this.world = world;
    }

    public void timeSet(long tick, Consumer<Long> consumer) {
        CancellableTask cancellableTask = Lib.Scheduler.runAtFixedRate(
                Ari.instance,
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
        this.scheduledTask.set(cancellableTask);
    }

    public void timeSet(long targetTimeTick) {
        this.timeSet(targetTimeTick, null);
    }

    public void timeAutomaticallyPasses(Consumer<Long> consumer) {
        CancellableTask cancellableTask = Lib.Scheduler.runAtFixedRate(
                Ari.instance,
                t -> {
                    long newTime = this.world.getTime() + this.addTick;
                    this.world.setFullTime(newTime);
                    consumer.accept(newTime);
                },
                1L,
                1L
        );
        this.scheduledTask.set(cancellableTask);
    }

    public String tickToTime(long tick) {
        long adjustedTick = tick % 24000;
        int hours = (int) ((adjustedTick / 1000 + 6) % 24);
        int minutes = (int) ((adjustedTick % 1000) * 60 / 1000);
        return String.format("%02d:%02d %s", (hours == 0) ? 12 : hours % 12, minutes, (hours >= 12) ? "PM" : "AM");
    }

    public void cancelTask() {
        CancellableTask s = this.scheduledTask.get();
        if(s != null) {
            s.cancel();
            this.scheduledTask.set(null);
        }
    }

    public static TimeManager build(World world) {
        return new TimeManager(world);
    }
    public static TimeManager build(World world, long delay,long addTick) {
        return new TimeManager(delay, addTick, world);
    }
}
