package com.tty.listener.player;

import com.tty.Ari;
import com.tty.lib.Log;
import com.tty.lib.enum_type.LangType;
import com.tty.lib.tool.*;
import com.tty.tool.PlayerDeathInfoCollector;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.HashMap;
import java.util.Map;

public class CustomPlayerDeathListener implements Listener {

    @EventHandler
    public void onDeath(PlayerDeathEvent event){
        if (!Ari.instance.getConfig().getBoolean("server.custom-death", false)) return;
        PlayerDeathInfoCollector.DeathInfo collect = PlayerDeathInfoCollector.collect(event);
        Log.debug(collect.toString());
        String BASE_PREFIX = "server.custom-death.";
        Map<String, Component> placeholders = new HashMap<>();
        placeholders.put(LangType.VICTIM.getType(), ComponentUtils.setEntityHoverText(collect.victim));
        placeholders.put(LangType.KILLER.getType(), ComponentUtils.setEntityHoverText(collect.killer));
        placeholders.put(LangType.KILLER_ITEM.getType(), ComponentUtils.setHoverItemText(collect.weapon));

        StringBuilder sb = new StringBuilder();

        switch (collect.deathCause) {
            case ENTITY_ATTACK, ENTITY_EXPLOSION, ENTITY_SWEEP_ATTACK, PROJECTILE, POISON -> {
                if (collect.killer instanceof Player) {
                    sb.append(collect.getRandomOfList(BASE_PREFIX + "player." + (collect.weapon == null || collect.weapon.isEmpty() ? "air":collect.isProjectile ? "projectile" : "item")));
                } else {
                    sb.append(collect.getRandomOfList(BASE_PREFIX + "mob." + (collect.weapon == null || collect.weapon.isEmpty() ? "air":collect.isProjectile ? "projectile" : "item")));
                }
                //增加 running away
                if(collect.killer instanceof LivingEntity livingEntity && PublicFunctionUtils.randomGenerator(0, 1) == 0) {
                    AttributeInstance max_health = livingEntity.getAttribute(Attribute.MAX_HEALTH);
                    if(max_health != null && livingEntity.getHealth() < max_health.getValue()) {
                        sb.append(collect.getRandomOfList(BASE_PREFIX + "running-away"));
                    }
                }

            }
            case CONTACT, LAVA, HOT_FLOOR -> {
                if (collect.event instanceof EntityDamageByBlockEvent damageByBlockEvent) {
                    Block block = damageByBlockEvent.getDamager();
                    if (block == null) {
                        Log.error("can not find contact block");
                    } else {
                        sb.append(collect.getRandomOfList(BASE_PREFIX + "block." + block.getType().name().toLowerCase()));
                    }
                }

            }
            case FALLING_BLOCK -> {
                Material material = null;
                String key = "";
                if(collect.event instanceof EntityDamageByEntityEvent damageByEntityEvent) {
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
                    Log.error("custom-death: can not find material data");
                    return;
                }
                sb.append(collect.getRandomOfList(BASE_PREFIX + "falling-blocks." + key));
            }
            case FALL -> sb.append(collect.getRandomOfList(BASE_PREFIX + "player.fall"));
            case FIRE, FIRE_TICK, CAMPFIRE -> sb.append(collect.getRandomOfList(BASE_PREFIX + "player.fire"));
            case LIGHTNING -> sb.append(collect.getRandomOfList(BASE_PREFIX + "player.lightning"));
            case SUFFOCATION -> sb.append(collect.getRandomOfList(BASE_PREFIX + "player.suffocation"));
            case DROWNING -> sb.append(collect.getRandomOfList(BASE_PREFIX + "player.drowning"));
            case FREEZE -> sb.append(collect.getRandomOfList(BASE_PREFIX + "player.freeze"));
            case SUICIDE -> sb.append(collect.getRandomOfList(BASE_PREFIX + "player.suicide"));
            case VOID -> sb.append(collect.getRandomOfList(BASE_PREFIX + "player.void"));
            case WITHER -> sb.append(collect.getRandomOfList(BASE_PREFIX + "player.wither"));
            case FLY_INTO_WALL -> sb.append(collect.getRandomOfList(BASE_PREFIX + "player.fly_into_wall"));
            case KILL -> sb.append(collect.getRandomOfList(BASE_PREFIX + "player.kill"));
            case MAGIC -> sb.append(collect.getRandomOfList(BASE_PREFIX + "player.magic"));
            case STARVATION -> sb.append(collect.getRandomOfList(BASE_PREFIX + "player.starvation"));
            case SONIC_BOOM -> sb.append(collect.getRandomOfList(BASE_PREFIX + "player.sonic_boom"));
            case BLOCK_EXPLOSION -> sb.append(collect.getRandomOfList(BASE_PREFIX + "player.explosion"));
        }

        event.deathMessage(ComponentUtils.text(sb.toString(), placeholders));

    }

}
