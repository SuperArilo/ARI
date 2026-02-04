package com.tty.states.action;

import com.tty.Ari;
import com.tty.dto.state.action.PlayerRideActionState;
import com.tty.api.state.StateService;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class PlayerRideActionStateService extends StateService<PlayerRideActionState> {

    public PlayerRideActionStateService(long rate, long c, boolean isAsync) {
        super(rate, c, isAsync, Ari.instance, Ari.SCHEDULER);
    }

    @Override
    protected boolean canAddState(PlayerRideActionState state) {

        Player owner = (Player) state.getOwner();
        String playerName = owner.getName();
        //判断玩家是否已经 ride 了
        if (!this.getStates(owner).isEmpty()) {
            Ari.LOG.debug("player {} is sited. skip...", playerName);
            return false;
        }
        //被点击的玩家如果有乘客（隐藏实体
        return state.getBeRidePlayer().getPassengers().isEmpty();
    }

    @Override
    protected void loopExecution(PlayerRideActionState state) {

        Player beRidePlayer = state.getBeRidePlayer();
        Player owner = (Player) state.getOwner();
        Entity toolEntity = state.getTool_entity();
        Ari.SCHEDULER.runAtEntity(Ari.instance, toolEntity, i -> {
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
        Entity owner = state.getOwner();
        Location location = beRidePlayer.getEyeLocation();
        state.createToolEntity(
            beRidePlayer.getWorld(),
            location,
            i -> {
                beRidePlayer.addPassenger(i);
                i.addPassenger(owner);
                owner.setRotation(location.getYaw(), 0);
                Ari.LOG.debug("player {} riding player {}.", owner.getName(), beRidePlayer.getName());
            }
        );
    }

    @Override
    protected void onEarlyExit(PlayerRideActionState state) {
        state.removeToolEntity(Ari.instance);
    }

    @Override
    protected void onFinished(PlayerRideActionState state) {
        state.removeToolEntity(Ari.instance);
    }

    @Override
    protected void onServiceAbort(PlayerRideActionState state) {
        state.removeToolEntity(Ari.instance);
        Ari.LOG.info("ejected player {}", state.getOwner().getName());
    }
}
