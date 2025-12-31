package com.tty.listener.player;

import com.tty.Ari;
import com.tty.dto.SleepingWorld;
import com.tty.lib.Lib;
import io.papermc.paper.event.player.PlayerDeepSleepEvent;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.event.server.ServerLoadEvent;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class PlayerSkipNight implements Listener {

    private final Map<World, SleepingWorld> worlds = new ConcurrentHashMap<>();

    private void update(World world) {
        Lib.Scheduler.run(Ari.instance, i -> this.worlds.get(world).update());
    }

    @EventHandler
    public void onServerLoad(ServerLoadEvent event) {
        this.worlds.clear();
        for (World world : Bukkit.getWorlds()) {
            if (!world.isBedWorks()) continue;
            this.worlds.put(world, new SleepingWorld(world));
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

    private boolean isDisabled() {
        return !Ari.instance.getConfig().getBoolean("server.skip-night.enable", false);
    }

}
