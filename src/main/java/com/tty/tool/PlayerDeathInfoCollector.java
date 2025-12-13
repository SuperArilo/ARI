package com.tty.tool;

import com.google.common.reflect.TypeToken;
import com.tty.Ari;
import com.tty.enumType.FilePath;
import com.tty.lib.tool.PublicFunctionUtils;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PlayerDeathInfoCollector {

    public static class DeathInfo {
        //受害者
        public Player victim;
        //死亡时间
        public long deathTime;
        //死亡原因
        public EntityDamageEvent.DamageCause deathCause;
        //
        public EntityDamageEvent event;
        //是否是实体造成的
        public boolean isEntityCause;
        //是否是远程舞曲
        public boolean isProjectile;
        //实施者
        public Entity killer;
        //实施者使用的武器，如果有
        public ItemStack weapon;   // 武器或手
        public boolean isEscapeAttempt; // 可自定义逻辑

        @Override
        public String toString() {
            return "DeathInfo{" +
                    "victim=" + victim.getName() +
                    ", deathTime=" + deathTime +
                    ", event=" + event +
                    ", deathCause=" + deathCause +
                    ", isEntityCause=" + isEntityCause +
                    ", isProjectile=" + isProjectile +
                    ", killer=" + (killer != null ? killer.getName() : "null") +
                    ", weapon=" + (weapon != null ? weapon.getType().name() : "null") +
                    '}';
        }

        public String getRandomOfList(String keyPath) {
            String killerName = this.killer == null ? "null" : this.killer.getType().name();
            Type type = new TypeToken<List<String>>() {}.getType();

            List<String> publicList = Ari.C_INSTANCE.getValue(keyPath + ".public", FilePath.LANG, type, List.of());
            List<String> pool = new ArrayList<>();

            if (!publicList.isEmpty()) {
                pool.addAll(publicList);

                List<String> killerList = Ari.C_INSTANCE.getValue(keyPath + "." + killerName.toLowerCase(), FilePath.LANG, type, List.of());
                if (!killerList.isEmpty()) {
                    pool.addAll(killerList);
                }
            } else {
                List<String> killerList = Ari.C_INSTANCE.getValue(keyPath + "." + killerName.toLowerCase(), FilePath.LANG, type, List.of());
                if (!killerList.isEmpty()) {
                    pool.addAll(killerList);
                } else {
                    List<String> fallbackList = Ari.C_INSTANCE.getValue(keyPath, FilePath.LANG, type, List.of());
                    if (!fallbackList.isEmpty()) {
                        pool.addAll(fallbackList);
                    }
                }
            }
            if (pool.isEmpty()) {
                return "";
            }
            return pool.get(PublicFunctionUtils.randomGenerator(0, pool.size()));
        }
    }

    public static DeathInfo collect(PlayerDeathEvent event) {
        DeathInfo info = new DeathInfo();
        info.victim = event.getEntity();
        info.deathTime = System.currentTimeMillis();
        info.event = event.getEntity().getLastDamageCause();
        info.deathCause = event.getEntity().getLastDamageCause() != null
                ? event.getEntity().getLastDamageCause().getCause()
                : EntityDamageEvent.DamageCause.CUSTOM;
        info.isEscapeAttempt = false;

        info.weapon = null;
        info.isEntityCause = false;
        info.isProjectile = false;
        info.killer = null;

        // 检查是否由实体造成伤害
        if (event.getEntity().getLastDamageCause() instanceof EntityDamageByEntityEvent damageEvent) {
            Entity damager = damageEvent.getDamager();
            info.isEntityCause = true;

            // 处理远程攻击（箭、雪球等）
            if (damager instanceof Projectile projectile) {
                info.isProjectile = true;
                if (projectile.getShooter() instanceof Entity shooter) {
                    damager = shooter;
                }
            }

            info.killer = damager;

            // 玩家攻击
            if (damager instanceof Player playerDamager) {
                info.weapon = Optional.of(playerDamager.getEquipment())
                        .map(EntityEquipment::getItemInMainHand)
                        .orElse(null);
            }
            // 怪物或其他生物攻击
            else if (damager instanceof LivingEntity entityDamager) {
                Optional.ofNullable(entityDamager.getEquipment())
                        .map(EntityEquipment::getItemInMainHand).ifPresent(weaponItem -> info.weapon = weaponItem);
            }
        }

        return info;
    }

}