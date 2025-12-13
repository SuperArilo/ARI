package com.tty.states.action;

import com.tty.Ari;
import com.tty.lib.Log;
import com.tty.dto.state.action.PlayerRideActionState;
import com.tty.lib.Lib;
import com.tty.lib.services.StateService;
import lombok.SneakyThrows;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.plugin.java.JavaPlugin;



public class PlayerRideActionStateService extends StateService<PlayerRideActionState> {

    public PlayerRideActionStateService(long rate, long c, boolean isAsync, JavaPlugin javaPlugin) {
        super(rate, c, isAsync, javaPlugin);
    }

    @Override
    protected boolean canAddState(PlayerRideActionState state) {

        Player owner = (Player) state.getOwner();
        String playerName = owner.getName();
        //判断玩家是否已经 ride 了
        if (!this.getStates(owner).isEmpty()) {
            Log.debug("player %s is sited. skip...", playerName);
            return false;
        }
        //被点击的玩家如果有乘客（隐藏实体
        return state.getBeRidePlayer().getPassengers().isEmpty();
    }

    @SneakyThrows
    @Override
    protected void loopExecution(PlayerRideActionState state) {

        Player beRidePlayer = state.getBeRidePlayer();
        Player owner = (Player) state.getOwner();
        Entity toolEntity = state.getTool_entity();
        Lib.Scheduler.runAtEntity(Ari.instance, toolEntity, i -> {
            boolean b = toolEntity.getPassengers().isEmpty() ||
                    !toolEntity.isInsideVehicle() ||
                    beRidePlayer.isDead() ||
                    owner.isSleeping() ||
                    owner.isSneaking() ||
                    owner.isDeeplySleeping() ||
                    !beRidePlayer.isOnline() ||
                    beRidePlayer.isSneaking() ||
                    beRidePlayer.isSwimming();
            if (b) {
                state.setOver(true);
            } else {
                state.setPending(false);
            }
        }, null);
    }

    @Override
    protected void abortAddState(PlayerRideActionState state) {
    }

    @Override
    protected void passAddState(PlayerRideActionState state) {
        Player beRidePlayer = state.getBeRidePlayer();
        Entity entity = beRidePlayer.getWorld().spawnEntity(
                beRidePlayer.getEyeLocation(),
                EntityType.AREA_EFFECT_CLOUD,
                CreatureSpawnEvent.SpawnReason.CUSTOM,
                e -> {
                    if (e instanceof AreaEffectCloud cloud) {
                        cloud.setRadius(0);
                        cloud.setInvulnerable(true);
                        cloud.setGravity(false);
                        cloud.setParticle(Particle.DUST, new Particle.DustOptions(Color.fromRGB(0, 0, 0), 0f));
                        cloud.setInvisible(true);
                        beRidePlayer.addPassenger(cloud);
                    }
                });
        state.setTool_entity(entity);
        entity.addPassenger(state.getOwner());
    }

    @Override
    protected void onEarlyExit(PlayerRideActionState state) {
        state.removeToolEntity();
    }

    @Override
    protected void onFinished(PlayerRideActionState state) {
        state.removeToolEntity();
    }

    @Override
    protected void onServiceAbort(PlayerRideActionState state) {
        state.removeToolEntity();
        Log.info("ejected player %s", state.getOwner().getName());
    }
}
