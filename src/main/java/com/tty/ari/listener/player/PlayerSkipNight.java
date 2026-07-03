package com.tty.ari.listener.player;

import com.tty.ari.Ari;
import com.tty.ari.dto.SleepingWorld;
import io.papermc.paper.event.player.PlayerDeepSleepEvent;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.event.server.ServerLoadEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class PlayerSkipNight implements Listener {

    public static final Map<World, SleepingWorld> SLEEPING_WORLD = new ConcurrentHashMap<>();

    private void update(World world) {
        SleepingWorld sleepingWorld = SLEEPING_WORLD.get(world);
        if (sleepingWorld == null) {
            Ari.instance.getLog().error("cannot found world {} to sleep skipping.", world.getName());
            return;
        }
        Ari.instance.getScheduler().run(i -> sleepingWorld.update());
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

    public static boolean isBedWorksRe(@NotNull World world) {
        World.Environment environment = world.getEnvironment();
        boolean a = !(environment.equals(World.Environment.NETHER) || environment.equals(World.Environment.THE_END));
        if (!a) {
            Ari.instance.getLog().info("world {} does not support bed usage; using fallback method.", world.getName());
            Ari.instance.getLog().info("this may be caused by the server running version 1.21.11.");
        }
        return a;
    }

    private boolean isDisabled() {
        return !Ari.instance.getConfig().getBoolean("server.skip-night.enable", false);
    }

}
