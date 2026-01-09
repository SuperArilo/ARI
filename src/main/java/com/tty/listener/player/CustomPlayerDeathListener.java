package com.tty.listener.player;

import com.tty.Ari;
import com.tty.lib.Log;
import com.tty.lib.enum_type.LangType;
import com.tty.lib.tool.ComponentUtils;
import com.tty.tool.PlayerDeathInfoCollector;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.Map;

public class CustomPlayerDeathListener implements Listener {

    private final PlayerDeathInfoCollector collector = new PlayerDeathInfoCollector();

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        if (!Ari.instance.getConfig().getBoolean("server.custom-death", false)) return;

        PlayerDeathInfoCollector.DeathInfo info = collector.collect(event);
        Log.debug(info.toString());

        String message = getDeathMessage(info, event);

        if (message != null) {
            event.deathMessage(ComponentUtils.text(
                    message,
                    Map.of(
                            LangType.VICTIM.getType(), ComponentUtils.setEntityHoverText(info.victim),
                            LangType.KILLER.getType(), info.killer != null ? ComponentUtils.setEntityHoverText(info.killer) : Component.empty(),
                            LangType.KILLER_ITEM.getType(), ComponentUtils.setHoverItemText(info.weapon)
                    )
            ));
        }
    }

    private String getDeathMessage(PlayerDeathInfoCollector.DeathInfo info, PlayerDeathEvent event) {

        String baseKey = "custom-death.";
        switch (info.deathCause) {
            case ENTITY_EXPLOSION, BLOCK_EXPLOSION -> {
                return info.getRandomOfList(baseKey + (info.killer instanceof Explosive ? "player.explosion" : "mob.explosion"), info.isDestine);
            }
            case ENTITY_ATTACK, ENTITY_SWEEP_ATTACK, PROJECTILE, POISON -> {
                String weaponKey = getWeaponKey(info);
                String targetKey = info.killer instanceof Player ? "player" : "mob";
                String message = info.getRandomOfList(baseKey + targetKey + "." + weaponKey, info.isDestine);

                if (info.isEscapeAttempt) {
                    message += info.getRandomOfList(baseKey + "running-away");
                }
                return message;
            }
            case CONTACT, LAVA, HOT_FLOOR -> {
                if (event.getEntity().getLastDamageCause() instanceof EntityDamageByBlockEvent blockEvent) {
                    Block block = blockEvent.getDamager();
                    if (block == null) {
                        Log.error("can not find contact block");
                        return null;
                    }
                    return info.getRandomOfList(baseKey + "block." + block.getType().name().toLowerCase(), info.isDestine);
                }
                return null;
            }
            case FALLING_BLOCK -> {
                Material material = null;
                String key = "";
                if (event.getEntity().getLastDamageCause() instanceof EntityDamageByEntityEvent entityEvent) {
                    if (entityEvent.getDamager() instanceof FallingBlock fallingBlock) {
                        material = fallingBlock.getBlockData().getMaterial();
                        key = switch (material) {
                            case ANVIL, CHIPPED_ANVIL, DAMAGED_ANVIL -> "anvil";
                            default -> material.name().toLowerCase();
                        };
                    } else {
                        Log.error("can not find falling block");
                        return null;
                    }
                }

                if (material == null) {
                    Log.error("custom-death: can not find material data");
                    return null;
                }
                return info.getRandomOfList(baseKey + "falling-blocks." + key, info.isDestine);
            }
            case FALL -> { return info.getRandomOfList(baseKey + "player.fall", info.isDestine); }
            case FIRE, FIRE_TICK, CAMPFIRE -> { return info.getRandomOfList(baseKey + "player.fire", info.isDestine); }
            case LIGHTNING -> { return info.getRandomOfList(baseKey + "player.lightning", info.isDestine); }
            case SUFFOCATION -> { return info.getRandomOfList(baseKey + "player.suffocation", info.isDestine); }
            case DROWNING -> { return info.getRandomOfList(baseKey + "player.drowning", info.isDestine); }
            case FREEZE -> { return info.getRandomOfList(baseKey + "player.freeze", info.isDestine); }
            case SUICIDE -> { return info.getRandomOfList(baseKey + "player.suicide", info.isDestine); }
            case VOID -> { return info.getRandomOfList(baseKey + "player.void", info.isDestine); }
            case WITHER -> { return info.getRandomOfList(baseKey + "player.wither", info.isDestine); }
            case FLY_INTO_WALL -> { return info.getRandomOfList(baseKey + "player.fly_into_wall", info.isDestine); }
            case KILL -> { return info.getRandomOfList(baseKey + "player.kill", info.isDestine); }
            case MAGIC -> { return info.getRandomOfList(baseKey + "player.magic", info.isDestine); }
            case STARVATION -> { return info.getRandomOfList(baseKey + "player.starvation", info.isDestine); }
            case SONIC_BOOM -> { return info.getRandomOfList(baseKey + "player.sonic_boom", info.isDestine); }
            case THORNS -> { return info.getRandomOfList(baseKey + "player.thorns", info.isDestine); }

            default -> { return null; }
        }
    }

    private String getWeaponKey(PlayerDeathInfoCollector.DeathInfo info) {
        if (info.weapon == null || info.weapon.getType().isAir()) {
            return "air";
        } else if (info.isProjectile) {
            return "projectile";
        } else {
            return "item";
        }
    }
}
