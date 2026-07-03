package com.tty.ari.listener.player;

import com.tty.ari.Ari;
import com.tty.ari.configuration.GameActionConfig;
import com.tty.ari.dto.state.action.PlayerRideActionState;
import com.tty.ari.dto.state.action.PlayerSitActionState;
import com.tty.ari.states.action.PlayerRideActionStateService;
import com.tty.ari.states.action.PlayerSitActionStateService;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;


public class PlayerActionListener implements Listener {

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!Ari.instance.getConfigurationManager().get(GameActionConfig.class).isSitEnable()) return;

        Player player = event.getPlayer();
        // 只处理主手
        if (event.getHand() != EquipmentSlot.HAND) return;
        // 动作不匹配或旁观模式
        if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK) || player.getGameMode().equals(GameMode.SPECTATOR)) return;
        // 手持空
        if (event.getItem() != null ||
                !player.getInventory().getItemInMainHand().isEmpty() ||
                !player.getInventory().getItemInOffHand().isEmpty()) return;
        // 点击的方块不存在
        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null) return;
        // 玩家已经骑乘实体
        if (player.getVehicle() != null) return;
        Ari.instance.getStatusManager().get(PlayerSitActionStateService.class).addState(new PlayerSitActionState(player, clickedBlock));
    }

    //玩家相互骑乘
    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (!Ari.instance.getConfigurationManager().get(GameActionConfig.class).isPlayerSitPlayerEnable()) return;
        // 只处理主手
        if (event.getHand() != EquipmentSlot.HAND) return;
        Player player = event.getPlayer();
        // 玩家必须空手且不是旁观模式
        if (!player.getInventory().getItemInMainHand().getType().equals(Material.AIR)
                || player.getGameMode().equals(GameMode.SPECTATOR)) return;
        // 被点击的实体必须是玩家
        if (!(event.getRightClicked() instanceof Player clickedPlayer)) return;

        Ari.instance.getStatusManager().get(PlayerRideActionStateService.class).addState(new PlayerRideActionState(player, clickedPlayer));
    }

}
