package com.tty.tool;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class LastDamageTracker {

    /**
     * key: 受害者
     * value: 被其攻击的列表（按时间顺序，越后面的条目越新）
     */
    private final Map<Entity, List<DamageRecord>> records = new HashMap<>();

    public record DamageRecord(long timestamp, Entity damager, double damage, Location location, boolean isProjectile, ItemStack weapon) {}

    public void addRecord(Entity victim, Entity damager, double damage, boolean isProjectile, ItemStack weapon) {
        synchronized (this) {
            this.records.computeIfAbsent(victim, k -> new ArrayList<>())
                    .add(new DamageRecord(System.currentTimeMillis(), damager, damage, victim.getLocation(), isProjectile, weapon));
        }
    }

    public List<DamageRecord> getRecords(Entity victim) {
        synchronized (this) {
            List<DamageRecord> list = this.records.get(victim);
            return list == null ? Collections.emptyList() : list;
        }
    }

    public void clearRecords(Entity victim) {
        synchronized (this) {
            this.records.remove(victim);
        }
    }

    public void clearDamagerRecords(Entity damager) {
        synchronized (this) {
            Iterator<Map.Entry<Entity, List<DamageRecord>>> it = this.records.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<Entity, List<DamageRecord>> entry = it.next();
                List<DamageRecord> list = entry.getValue();
                list.removeIf(rec -> rec.damager() == damager);
                if (list.isEmpty()) {
                    it.remove();
                }
            }
        }
    }

    public void removeAll() {
        synchronized (this) {
            this.records.clear();
        }
    }

    /**
     * 返回当前 tracker 中所有被记录的受害者实体的快照。
     */
    public Set<Entity> getVictimsSnapshot() {
        synchronized (this) {
            return new HashSet<>(this.records.keySet());
        }
    }

    /**
     * 返回 victim 的最近一次记录时间戳（毫秒）。若没有记录，返回 0。
     */
    public long getLastTimestamp(Entity victim) {
        synchronized (this) {
            List<DamageRecord> list = this.records.get(victim);
            if (list == null || list.isEmpty()) return 0L;
            return list.getLast().timestamp();
        }
    }


}
