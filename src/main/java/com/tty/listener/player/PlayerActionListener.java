package com.tty.listener.player;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import com.tty.Ari;
import com.tty.dto.state.action.PlayerRideActionState;
import com.tty.dto.state.action.PlayerSitActionState;
import com.tty.enumType.FilePath;
import com.tty.lib.Log;
import com.tty.states.action.PlayerRideActionStateService;
import com.tty.states.action.PlayerSitActionStateService;
import com.tty.tool.ConfigUtils;
import org.bukkit.Bukkit;
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
        if (event.getItem() != null || !player.getInventory().getItemInMainHand().getType().isAir()) return;
        // 点击的方块不存在
        Block clickedBlock = event.getClickedBlock();
        if (!this.canInteract(player, clickedBlock)) {
            player.sendMessage(ConfigUtils.t("function.sit.error-location"));
            return;
        }
        if (clickedBlock == null) return;
        // 玩家已经骑乘实体
        if (player.getVehicle() != null) return;
        Ari.instance.stateMachineManager
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

        Ari.instance.stateMachineManager
                .get(PlayerRideActionStateService.class)
                .addState(new PlayerRideActionState(player, clickedPlayer));
    }

    private boolean isNotEnableSit() {
        return Ari.C_INSTANCE.getValue("action.sit.enable", FilePath.FUNCTION_CONFIG, Boolean.class, false);
    }

    private boolean isNotEnablePlayerSitPlayer() {
        return Ari.C_INSTANCE.getValue("action.player-sit-player.enable", FilePath.FUNCTION_CONFIG, Boolean.class, false);
    }

    public boolean hasWorldGuard() {
        return Bukkit.getPluginManager().getPlugin("WorldGuard") != null;
    }

    public boolean canInteract(Player player, Block block) {
        if (!this.hasWorldGuard()) {
            Log.debug("not have WorldGuard. skip...");
            return true;
        }
        RegionQuery query = WorldGuard.getInstance()
                .getPlatform()
                .getRegionContainer()
                .createQuery();

        com.sk89q.worldguard.LocalPlayer localPlayer =
                WorldGuardPlugin.inst().wrapPlayer(player);

        return query.testState(
                BukkitAdapter.adapt(block.getLocation()),
                localPlayer,
                Flags.INTERACT
        );
    }

}
