package com.tty.ari.listener.player;

import com.tty.api.ComponentTool;
import com.tty.ari.Ari;
import com.tty.ari.enumType.lang.PlaceholderPlayerDeath;
import com.tty.ari.tool.PlayerDeathInfoCollector;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Explosive;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.Map;

public class CustomPlayerDeathListener implements Listener {

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        if (!Ari.instance.getConfig().getBoolean("server.custom-death", false)) return;

        PlayerDeathInfoCollector.DeathInfo info = PlayerDeathInfoCollector.collect(event);
        Ari.instance.getLog().debug(info.toString());

        boolean isSuicide = false;
        if (info.victim != null) {
            isSuicide = (info.victim.equals(info.killer) && info.killer instanceof Player);
        }
        Component victim = info.victim == null ? Component.empty():ComponentTool.setEntityHoverText(info.victim);
        Component killer;
        if (isSuicide) {
            killer = ComponentTool.text(Ari.DATA_SERVICE.getValue("base.on-player.self"));
        } else {
            killer = info.killer == null ? Component.empty():ComponentTool.setEntityHoverText(info.killer);
        }
        Component weapon = ComponentTool.setHoverItemText(info.weapon);
        String messageFuture = this.getDeathMessage(info, event);
        Component deathMsg = ComponentTool.text(messageFuture, Map.of(
                PlaceholderPlayerDeath.KILLER_UNRESOLVED.getType(), killer,
                PlaceholderPlayerDeath.VICTIM_UNRESOLVED.getType(), victim,
                PlaceholderPlayerDeath.KILLER_ITEM_UNRESOLVED.getType(), weapon
        ));
        event.deathMessage(deathMsg);
    }

    private String getDeathMessage(PlayerDeathInfoCollector.DeathInfo info, PlayerDeathEvent event) {

        String baseKey = "custom-death.";
        return switch (info.deathCause) {
            case ENTITY_EXPLOSION, BLOCK_EXPLOSION -> info.getRandomOfList(baseKey + (info.killer instanceof Explosive ? "player.explosion" : "mob.explosion"), info.isDestine);
            case ENTITY_ATTACK, ENTITY_SWEEP_ATTACK, PROJECTILE, POISON -> {
                String weaponKey = this.getWeaponKey(info);
                String targetKey = info.killer instanceof Player ? "player" : "mob";
                String message = info.getRandomOfList(baseKey + targetKey + "." + weaponKey, info.isDestine);

                if (info.isEscapeAttempt) {
                    message += info.getRandomOfList(baseKey + "running-away");
                }
                yield message;
            }
            case CONTACT, LAVA, HOT_FLOOR -> {
                if (event.getEntity().getLastDamageCause() instanceof EntityDamageByBlockEvent blockEvent) {
                    Block block = blockEvent.getDamager();
                    if (block == null) {
                        Ari.instance.getLog().error("can not find contact block");
                        yield "";
                    } else {
                        yield info.getRandomOfList(baseKey + "block." + block.getType().name().toLowerCase(), info.isDestine);
                    }
                }
                yield "";
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
                        Ari.instance.getLog().error("can not find falling block");
                        yield "";
                    }
                }

                if (material == null) {
                    Ari.instance.getLog().error("custom-death: can not find material data");
                    yield "";
                }
                yield info.getRandomOfList(baseKey + "falling-blocks." + key, info.isDestine);
            }
            case FALL -> info.getRandomOfList(baseKey + "player.fall", info.isDestine);
            case FIRE, FIRE_TICK, CAMPFIRE -> info.getRandomOfList(baseKey + "player.fire", info.isDestine);
            case LIGHTNING -> info.getRandomOfList(baseKey + "player.lightning", info.isDestine);
            case SUFFOCATION -> info.getRandomOfList(baseKey + "player.suffocation", info.isDestine);
            case DROWNING -> info.getRandomOfList(baseKey + "player.drowning", info.isDestine);
            case FREEZE -> info.getRandomOfList(baseKey + "player.freeze", info.isDestine);
            case SUICIDE -> info.getRandomOfList(baseKey + "player.suicide", info.isDestine);
            case VOID -> info.getRandomOfList(baseKey + "player.void", info.isDestine);
            case WITHER -> info.getRandomOfList(baseKey + "player.wither", info.isDestine);
            case FLY_INTO_WALL -> info.getRandomOfList(baseKey + "player.fly_into_wall", info.isDestine);
            case KILL -> info.getRandomOfList(baseKey + "player.kill", info.isDestine);
            case MAGIC -> info.getRandomOfList(baseKey + "player.magic", info.isDestine);
            case STARVATION -> info.getRandomOfList(baseKey + "player.starvation", info.isDestine);
            case SONIC_BOOM -> info.getRandomOfList(baseKey + "player.sonic_boom", info.isDestine);
            case THORNS -> info.getRandomOfList(baseKey + "player.thorns", info.isDestine);
            default -> "";
        };
    }

    private String getWeaponKey(PlayerDeathInfoCollector.DeathInfo info) {
        if (info.weapon == null || info.weapon.getType().isAir()) {
            return "air";
        } else if (info.isProjectile) {
            return "projectile";
        } else if (info.weapon.getType().equals(Material.POTION)){
            return "magic";
        } else {
            return "item";
        }
    }
}
