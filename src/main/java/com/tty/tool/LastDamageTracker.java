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

    /**
     * 实体之间的伤害记录（玩家 - 玩家，玩家 - 实体， 实体 - 玩家， 实体 x 实体）
     * @param hash 用于是否确认添加重复伤害
     * @param timestamp 造成伤害的时间戳
     * @param damager 伤害着
     * @param damage 造成的伤害
     * @param location 位置
     * @param weapon 武器
     */
    public record DamageRecord(int hash, long timestamp, Entity damager, double damage, Location location, ItemStack weapon) {}

    public void addRecord(int hash, Entity victim, Entity damager, double damage, ItemStack weapon) {
        synchronized (this) {
            this.records.computeIfAbsent(victim, k -> new ArrayList<>())
                    .add(new DamageRecord(hash, System.currentTimeMillis(), damager, damage, victim.getLocation(), weapon));
        }
    }

    public List<DamageRecord> getRecords(Entity victim) {
        synchronized (this) {
            List<DamageRecord> list = this.records.get(victim);
            return list == null ? Collections.emptyList() : list;
        }
    }

    /**
     * 清除与被害者所有有关的记录
     * @param victim 被害者
     */
    public void clearRecords(Entity victim) {
        synchronized (this) {
            this.records.remove(victim);
            Iterator<Map.Entry<Entity, List<DamageRecord>>> it = this.records.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<Entity, List<DamageRecord>> entry = it.next();
                List<DamageRecord> list = entry.getValue();
                list.removeIf(r -> r.damager().equals(victim));
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
