package com.tty.dto.state.action;

import com.tty.Ari;
import com.tty.api.FormatUtils;
import com.tty.api.state.State;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.*;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class PlayerActionState extends State {

    @Getter
    @Setter
    private Entity tool_entity;

    public PlayerActionState(Entity owner) {
        super(owner, Integer.MAX_VALUE);
    }

    public void createToolEntity(@NotNull World world, @NotNull Location location, Consumer<AreaEffectCloud> i) {
        if (this.tool_entity != null) {
            Location l = tool_entity.getLocation();
            Ari.LOG.error("tool_entity already exists. location {}", FormatUtils.XYZText(l.getX(), l.getY(), l.getZ()));
            Ari.LOG.warn("removing...");
            this.tool_entity.remove();
            this.tool_entity = null;
        }
        this.tool_entity = world.spawnEntity(
            location,
            EntityType.AREA_EFFECT_CLOUD,
            CreatureSpawnEvent.SpawnReason.CUSTOM,
            entity -> {
                if (entity instanceof AreaEffectCloud cloud) {
                    cloud.setPersistent(false);
                    cloud.setRadius(0);
                    cloud.setInvulnerable(true);
                    cloud.setGravity(false);
                    cloud.setInvisible(true);
                    cloud.setParticle(Particle.DUST, new Particle.DustOptions(Color.fromRGB(0, 0, 0), 0f));
                    i.accept(cloud);
                }
            }
        );
    }

    public void removeToolEntity(JavaPlugin plugin) {
        if (this.tool_entity == null) return;
        if (!Bukkit.getServer().isStopping()) {
            Ari.SCHEDULER.runAtEntity(plugin,
                    this.tool_entity,
                    i-> this.cancelTaskEntity(),
                    () -> Ari.LOG.error("remove tool_entity error."));
        } else {
            this.cancelTaskEntity();
        }

    }

    private void cancelTaskEntity() {
        this.getOwner().eject();
        if (!Bukkit.getServer().isStopping()) {
            Ari.LOG.debug("remove entity.");
            this.tool_entity.remove();
        }
        this.tool_entity = null;
        this.setOver(true);
        Ari.LOG.debug("player {} ejected, remove tool entity", this.getOwner().getName());
    }
}
