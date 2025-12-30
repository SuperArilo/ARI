package com.tty.tool;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class LastDamageTracker {

    private final Map<UUID, List<DamageRecord>> records = new HashMap<>();

    public record DamageRecord(long timestamp, Entity damager, double damage, Location location, boolean isProjectile, ItemStack weapon) {}

    public void addRecord(Player victim, Entity damager, double damage, boolean isProjectile, ItemStack weapon) {
        this.records.computeIfAbsent(victim.getUniqueId(), k -> new ArrayList<>())
                .add(new DamageRecord(System.currentTimeMillis(), damager, damage, victim.getLocation(), isProjectile, weapon));
    }

    public List<DamageRecord> getRecords(UUID uuid) {
        return this.records.getOrDefault(uuid, Collections.emptyList());
    }

    public void clearRecords(UUID uuid) {
        this.records.remove(uuid);
    }
}
