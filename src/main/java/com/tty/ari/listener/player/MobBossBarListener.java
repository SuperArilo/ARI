package com.tty.ari.listener.player;

import com.tty.api.event.WhenPluginConfigReloadCompleteEvent;
import com.tty.ari.Ari;
import com.tty.ari.dto.state.player.AttackBossBarState;
import com.tty.ari.enumType.FilePath;
import com.tty.ari.states.AttackBossBarService;
import com.tty.ari.tool.LastDamageTracker;
import org.bukkit.entity.Boss;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;

import java.util.List;

import static com.tty.ari.listener.DamageTrackerListener.DAMAGE_TRACKER;

public class MobBossBarListener implements Listener {

    private boolean isEnable = false;

    private boolean isBoss(Damageable damageable) {
        return (damageable instanceof Boss);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!this.isEnable) return;
        Entity entity = event.getEntity();
        if (!(entity instanceof Damageable victim)) return;

        // 如果是boss，不显示bar
        if (this.isBoss(victim)) return;

        // 获取伤害记录
        List<LastDamageTracker.DamageRecord> records = DAMAGE_TRACKER.getRecords(victim);
        if (records.isEmpty()) return;

        // 获取最近一次伤害记录
        LastDamageTracker.DamageRecord last = records.getLast();

        // 攻击者必须是玩家
        if (!(last.damager() instanceof Player player)) return;

        // 如果是自己造成的伤害不显示
        if (victim.equals(player)) return;

        AttackBossBarService service = Ari.STATE_MACHINE_MANAGER.get(AttackBossBarService.class);

        AttackBossBarState barState = null;
        for (AttackBossBarState state : service.getStates(player)) {
            if (state.getTarget().getUniqueId().equals(victim.getUniqueId())) {
                barState = state;
                break;
            }
        }
        if (barState != null) {
            barState.setDamage((float) event.getFinalDamage());
        } else {
            service.addState(new AttackBossBarState(player, victim, Integer.MAX_VALUE));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void regainHealth(EntityRegainHealthEvent event) {
        if (!this.isEnable) return;
        Entity entity = event.getEntity();
        if (!(entity instanceof Damageable victim) || entity instanceof Player) return;
        List<LastDamageTracker.DamageRecord> records = DAMAGE_TRACKER.getRecords(victim);
        if (records.isEmpty()) return;
        LastDamageTracker.DamageRecord last = records.getLast();
        if (!(last.damager() instanceof Player player)) return;

        AttackBossBarService service = Ari.STATE_MACHINE_MANAGER.get(AttackBossBarService.class);
        AttackBossBarState barState = null;
        for (AttackBossBarState state : service.getStates(player)) {
            if (state.getTarget().getUniqueId().equals(victim.getUniqueId())) {
                barState = state;
                break;
            }
        }
        if (barState != null) {
            barState.setDamage((float) -event.getAmount());
        } else {
            service.addState(new AttackBossBarState(player, victim, Integer.MAX_VALUE));
        }
    }

    @EventHandler
    public void onReload(WhenPluginConfigReloadCompleteEvent event) {
        this.isEnable = this.isEnable();
    }

    private boolean isEnable() {
        return Ari.instance.getConfigInstance().getValue("attack-bar.enable", FilePath.ATTACK_BAR_CONFIG, Boolean.class, false);
    }

}
