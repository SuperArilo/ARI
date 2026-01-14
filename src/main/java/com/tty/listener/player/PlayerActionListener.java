package com.tty.listener.player;

import com.tty.Ari;
import com.tty.dto.state.action.PlayerRideActionState;
import com.tty.dto.state.action.PlayerSitActionState;
import com.tty.enumType.FilePath;
import com.tty.states.action.PlayerRideActionStateService;
import com.tty.states.action.PlayerSitActionStateService;
import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.*;
import org.bukkit.Material;
import org.bukkit.inventory.EquipmentSlot;


public class PlayerActionListener implements Listener {

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!this.isNotEnableSit()) return;

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
        Ari.STATE_MACHINE_MANAGER
                .get(PlayerSitActionStateService.class)
                .addState(new PlayerSitActionState(player, clickedBlock));
    }

    //玩家相互骑乘
    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        // 未开启
        if (!this.isNotEnablePlayerSitPlayer()) return;
        // 只处理主手
        if (event.getHand() != EquipmentSlot.HAND) return;
        Player player = event.getPlayer();
        // 玩家必须空手且不是旁观模式
        if (!player.getInventory().getItemInMainHand().getType().equals(Material.AIR)
                || player.getGameMode().equals(GameMode.SPECTATOR)) return;
        // 被点击的实体必须是玩家
        if (!(event.getRightClicked() instanceof Player clickedPlayer)) return;

        Ari.STATE_MACHINE_MANAGER
                .get(PlayerRideActionStateService.class)
                .addState(new PlayerRideActionState(player, clickedPlayer));
    }

    private boolean isNotEnableSit() {
        return Ari.C_INSTANCE.getValue("action.sit.enable", FilePath.FUNCTION_CONFIG, Boolean.class, false);
    }

    private boolean isNotEnablePlayerSitPlayer() {
        return Ari.C_INSTANCE.getValue("action.player-sit-player.enable", FilePath.FUNCTION_CONFIG, Boolean.class, false);
    }

}
