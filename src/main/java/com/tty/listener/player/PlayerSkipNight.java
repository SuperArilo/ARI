package com.tty.listener.player;

import com.tty.Ari;
import com.tty.dto.SleepingWorld;
import com.tty.lib.Lib;
import com.tty.lib.Log;
import io.papermc.paper.event.player.PlayerDeepSleepEvent;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.event.server.ServerLoadEvent;
import org.jspecify.annotations.NonNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class PlayerSkipNight implements Listener {

    public static final Map<World, SleepingWorld> SLEEPING_WORLD = new ConcurrentHashMap<>();

    private void update(World world) {
        SleepingWorld sleepingWorld = SLEEPING_WORLD.get(world);
        if (sleepingWorld == null) {
            Log.error("cannot found world {} to sleep skipping.", world.getName());
            return;
        }
        Lib.Scheduler.run(Ari.instance, i -> sleepingWorld.update());
    }

    @EventHandler
    public void onServerLoad(ServerLoadEvent event) {
        SLEEPING_WORLD.clear();
        for (World world : Bukkit.getWorlds()) {
            if (!world.isBedWorks()) {
                if (!isBedWorksRe(world)) continue;
            }
            SLEEPING_WORLD.put(world, new SleepingWorld(world));
        }
    }

    @EventHandler
    public void deepSleep(PlayerDeepSleepEvent event) {
        if (this.isDisabled()) return;
        this.update(event.getPlayer().getWorld());
    }

    @EventHandler
    public void leave(PlayerBedLeaveEvent event) {
        if (this.isDisabled()) return;
        this.update(event.getPlayer().getWorld());
    }

    public static boolean isBedWorksRe(@NonNull World world) {
        World.Environment environment = world.getEnvironment();
        boolean a = !(environment.equals(World.Environment.NETHER) || environment.equals(World.Environment.THE_END));
        if (!a) {
            Log.info("world {} does not support bed usage; using fallback method.", world.getName());
            Log.info("this may be caused by the server running version 1.21.11.");
        }
        return a;
    }

    private boolean isDisabled() {
        return !Ari.instance.getConfig().getBoolean("server.skip-night.enable", false);
    }

}
