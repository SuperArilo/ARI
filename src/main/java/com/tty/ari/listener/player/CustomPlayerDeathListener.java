package com.tty.ari.listener.player;

import com.tty.api.ComponentTool;
import com.tty.ari.Ari;
import com.tty.ari.enumType.lang.PlaceholderPlayerDeath;
import com.tty.ari.tool.PlayerDeathInfoCollector;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
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
import java.util.concurrent.CompletableFuture;

public class CustomPlayerDeathListener implements Listener {

    private final PlayerDeathInfoCollector collector = new PlayerDeathInfoCollector();

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        if (!Ari.instance.getConfig().getBoolean("server.custom-death", false)) return;
        event.deathMessage(null);
        PlayerDeathInfoCollector.DeathInfo info = collector.collect(event);
        Ari.instance.getLog().debug(info.toString());

        ComponentTool tool = Ari.instance.getComponentTool();
        boolean isSuicide = (info.victim.equals(info.killer) && info.killer instanceof Player);

        CompletableFuture<Component> victimFuture = CompletableFuture.supplyAsync(() -> info.victim).thenComposeAsync(victim -> {
            if (victim == null) return CompletableFuture.completedFuture(Component.empty());
            return tool.setEntityHoverText(victim);
        }, Ari.instance.getExecutorAsync()).exceptionally(e -> {
            Ari.instance.getLog().error(e);
            return null;
        });

        CompletableFuture<Component> killerFuture;
        if (isSuicide) {
            killerFuture = CompletableFuture.completedFuture(tool.text(Ari.DATA_SERVICE.getValue("base.on-player.self")));
        } else {
            killerFuture = CompletableFuture.supplyAsync(() -> info.killer).thenComposeAsync(killer -> {
                if (killer == null) return CompletableFuture.completedFuture(Component.empty());
                return tool.setEntityHoverText(killer);
            }, Ari.instance.getExecutorAsync()).exceptionally(e -> {
                Ari.instance.getLog().error(e);
                return null;
            });
        }

        CompletableFuture<Component> weaponFuture = tool.setHoverItemText(info.weapon);
        CompletableFuture<String> messageFuture = this.getDeathMessage(info, event);

        CompletableFuture.allOf(killerFuture, victimFuture, weaponFuture, messageFuture).thenRunAsync(() -> {
            Component killComp = killerFuture.join();
            Component victimComp = victimFuture.join();
            Component weaponComp = weaponFuture.join();
            String message = messageFuture.join();

            Component deathMsg = tool.text(message, Map.of(
                    PlaceholderPlayerDeath.KILLER_UNRESOLVED.getType(), killComp,
                    PlaceholderPlayerDeath.VICTIM_UNRESOLVED.getType(), victimComp,
                    PlaceholderPlayerDeath.KILLER_ITEM_UNRESOLVED.getType(), weaponComp
            ));

            Bukkit.getServer().broadcast(deathMsg);
        }, Ari.instance.getExecutorAsync()).exceptionally(e -> {
            Ari.instance.getLog().error(e);
            return null;
        });
    }

    private CompletableFuture<String> getDeathMessage(PlayerDeathInfoCollector.DeathInfo info, PlayerDeathEvent event) {

        CompletableFuture<String> future = new CompletableFuture<>();
        Player entity = event.getEntity();

        String baseKey = "custom-death.";
        switch (info.deathCause) {
            case ENTITY_EXPLOSION, BLOCK_EXPLOSION -> future.complete(info.getRandomOfList(baseKey + (info.killer instanceof Explosive ? "player.explosion" : "mob.explosion"), info.isDestine));
            case ENTITY_ATTACK, ENTITY_SWEEP_ATTACK, PROJECTILE, POISON ->
                    Ari.instance.getScheduler().runAtEntity(entity, i -> {
                        String weaponKey = this.getWeaponKey(info);
                        String targetKey = info.killer instanceof Player ? "player" : "mob";
                        String message = info.getRandomOfList(baseKey + targetKey + "." + weaponKey, info.isDestine);

                        if (info.isEscapeAttempt) {
                            message += info.getRandomOfList(baseKey + "running-away");
                        }
                        future.complete(message);
                    }, null);
            case CONTACT, LAVA, HOT_FLOOR ->
                    Ari.instance.getScheduler().runAtEntity(entity, i -> {
                        if (event.getEntity().getLastDamageCause() instanceof EntityDamageByBlockEvent blockEvent) {
                            Block block = blockEvent.getDamager();
                            if (block == null) {
                                Ari.instance.getLog().error("can not find contact block");
                                future.complete(null);
                            } else {
                                future.complete(info.getRandomOfList(baseKey + "block." + block.getType().name().toLowerCase(), info.isDestine));
                            }
                        }
                    }, null);
            case FALLING_BLOCK ->
                    Ari.instance.getScheduler().runAtEntity(entity, i -> {
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
                                future.complete(null);
                                return;
                            }
                        }

                        if (material == null) {
                            Ari.instance.getLog().error("custom-death: can not find material data");
                            future.complete(null);
                            return;
                        }
                        future.complete(info.getRandomOfList(baseKey + "falling-blocks." + key, info.isDestine));
                    }, null);
            case FALL -> future.complete(info.getRandomOfList(baseKey + "player.fall", info.isDestine));
            case FIRE, FIRE_TICK, CAMPFIRE -> future.complete(info.getRandomOfList(baseKey + "player.fire", info.isDestine));
            case LIGHTNING -> future.complete(info.getRandomOfList(baseKey + "player.lightning", info.isDestine));
            case SUFFOCATION -> future.complete(info.getRandomOfList(baseKey + "player.suffocation", info.isDestine));
            case DROWNING -> future.complete(info.getRandomOfList(baseKey + "player.drowning", info.isDestine));
            case FREEZE -> future.complete(info.getRandomOfList(baseKey + "player.freeze", info.isDestine));
            case SUICIDE -> future.complete(info.getRandomOfList(baseKey + "player.suicide", info.isDestine));
            case VOID -> future.complete(info.getRandomOfList(baseKey + "player.void", info.isDestine));
            case WITHER -> future.complete(info.getRandomOfList(baseKey + "player.wither", info.isDestine));
            case FLY_INTO_WALL -> future.complete(info.getRandomOfList(baseKey + "player.fly_into_wall", info.isDestine));
            case KILL -> future.complete(info.getRandomOfList(baseKey + "player.kill", info.isDestine));
            case MAGIC -> future.complete(info.getRandomOfList(baseKey + "player.magic", info.isDestine));
            case STARVATION -> future.complete(info.getRandomOfList(baseKey + "player.starvation", info.isDestine));
            case SONIC_BOOM -> future.complete(info.getRandomOfList(baseKey + "player.sonic_boom", info.isDestine));
            case THORNS -> future.complete(info.getRandomOfList(baseKey + "player.thorns", info.isDestine));
            default -> future.complete(null);
        }
        return future;
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
