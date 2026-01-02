package com.tty.listener.player;

import com.tty.Ari;
import com.tty.lib.Log;
import com.tty.lib.enum_type.LangType;
import com.tty.lib.tool.ComponentUtils;
import com.tty.tool.PlayerDeathInfoCollector;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.Map;

public class CustomPlayerDeathListener implements Listener {

    private final PlayerDeathInfoCollector collector = new PlayerDeathInfoCollector();

    @EventHandler(priority = EventPriority.LOW)
    public void onDeath(PlayerDeathEvent event){
        if (!Ari.instance.getConfig().getBoolean("server.custom-death", false)) return;
        PlayerDeathInfoCollector.DeathInfo info = this.collector.collect(event);
        Log.debug(info.toString());

        StringBuilder sb = new StringBuilder();
        String baseKey = "custom-death.";
        switch (info.deathCause) {
            case ENTITY_EXPLOSION, BLOCK_EXPLOSION -> {
                if (info.killer instanceof Explosive) {
                    sb.append(info.getRandomOfList(baseKey + "player.explosion"));
                } else {
                    sb.append(info.getRandomOfList(baseKey + "mob.explosion"));
                }
            }
            case ENTITY_ATTACK, ENTITY_SWEEP_ATTACK, PROJECTILE, POISON -> {
                String lastKey = info.weapon == null || info.weapon.getType().isAir() ? "air" : info.isProjectile ? "projectile" : "item";
                if(info.killer instanceof Player) {
                    sb.append(info.getRandomOfList(baseKey + "player." + lastKey));
                } else {
                    sb.append(info.getRandomOfList(baseKey + "mob." + lastKey));
                }
                if(info.isEscapeAttempt) {
                    sb.append(info.getRandomOfList(baseKey + "running-away"));
                }
            }
            case CONTACT, LAVA, HOT_FLOOR -> sb.append(info.getRandomOfList(baseKey + "block." + event.getDamageSource().getDamageType().getTranslationKey()));
            case FALLING_BLOCK -> {
                Material material = null;
                String key = "";
                if(event.getEntity().getLastDamageCause() instanceof EntityDamageByEntityEvent damageByEntityEvent) {
                    if(damageByEntityEvent.getDamager() instanceof FallingBlock fallingBlock) {
                        material = fallingBlock.getBlockData().getMaterial();
                        if (material == Material.ANVIL || material == Material.CHIPPED_ANVIL || material == Material.DAMAGED_ANVIL) {
                            key = "anvil";
                        } else {
                            key = material.name().toLowerCase();
                        }
                    } else {
                        Log.error("can not find falling block");
                    }
                }
                if(material == null) {
                    Log.error("custom-death: can not find material data"); return;
                }
                sb.append(info.getRandomOfList(baseKey + "falling-blocks." + key));
            }
            case FALL -> sb.append(info.getRandomOfList(baseKey + "player.fall"));
            case FIRE, FIRE_TICK, CAMPFIRE -> sb.append(info.getRandomOfList(baseKey + "player.fire"));
            case LIGHTNING -> sb.append(info.getRandomOfList(baseKey + "player.lightning"));
            case SUFFOCATION -> sb.append(info.getRandomOfList(baseKey + "player.suffocation"));
            case DROWNING -> sb.append(info.getRandomOfList(baseKey + "player.drowning"));
            case FREEZE -> sb.append(info.getRandomOfList(baseKey + "player.freeze"));
            case SUICIDE -> sb.append(info.getRandomOfList(baseKey + "player.suicide"));
            case VOID -> sb.append(info.getRandomOfList(baseKey + "player.void"));
            case WITHER -> sb.append(info.getRandomOfList(baseKey + "player.wither"));
            case FLY_INTO_WALL -> sb.append(info.getRandomOfList(baseKey + "player.fly_into_wall"));
            case KILL -> sb.append(info.getRandomOfList(baseKey + "player.kill"));
            case MAGIC -> sb.append(info.getRandomOfList(baseKey + "player.magic"));
            case STARVATION -> sb.append(info.getRandomOfList(baseKey + "player.starvation"));
            case SONIC_BOOM -> sb.append(info.getRandomOfList(baseKey + "player.sonic_boom"));
            case THORNS -> sb.append(info.getRandomOfList(baseKey + "player.thorns"));
        }
        event.deathMessage(ComponentUtils.text(
                sb.toString(),
                Map.of(
                    LangType.VICTIM.getType(), ComponentUtils.setEntityHoverText(info.victim),
                    LangType.KILLER.getType(), info.killer != null ? ComponentUtils.setEntityHoverText(info.killer) : Component.empty(),
                    LangType.KILLER_ITEM.getType(), ComponentUtils.setHoverItemText(info.weapon)
                )
            )
        );
    }

}
