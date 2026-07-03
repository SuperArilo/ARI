package com.tty.ari.states;

import com.tty.api.state.StateService;
import com.tty.ari.Ari;
import com.tty.ari.dto.state.player.PlayerOnlineState;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class PlayerOnlineService extends StateService<PlayerOnlineState> {

    public PlayerOnlineService(long rate, long c, boolean isAsync) {
        super(rate, c, isAsync, Ari.instance);
    }

    @Override
    protected boolean canAddState(PlayerOnlineState state) {
        return this.getStates(state.getOwner()).isEmpty();
    }

    @Override
    protected void loopExecution(PlayerOnlineState state) {
        if (!((state.getOwner()) instanceof Player player) || !player.isOnline()) {
            state.setOver(true);
            return;
        }
        state.setRunning(true);
        Location location = player.getLocation();
        Ari.instance.getScheduler().runAtEntity(player, i -> {
            Location nowLocation = player.getLocation();
            if (nowLocation.distance(location) == 0) {
                state.addStandCount();
            } else {
                state.resetStandCount();
            }
            player.setCollidable(!state.isAFK());
            Ari.instance.getLog().debug("player {} afk status: {}.", player.getName(), state.isAFK());
            state.setRunning(false);
        }, null);
    }

    @Override
    protected void abortAddState(PlayerOnlineState state) {

    }

    @Override
    protected void passAddState(PlayerOnlineState state) {

    }

    @Override
    protected void onEarlyExit(PlayerOnlineState state) {

    }

    @Override
    protected void onFinished(PlayerOnlineState state) {

    }

    @Override
    protected void onServiceAbort(PlayerOnlineState state) {
        state.setOver(true);
    }

    @Override
    public void onReload() {
        for (PlayerOnlineState state : this.getAllStates()) {
            Entity owner = state.getOwner();
            state.setOver(true);
            if (owner instanceof Player player) {
                player.setCollidable(true);
                Ari.instance.getLog().debug("plugin reload, remove player {} online status.", player.getName());
                this.addState(new PlayerOnlineState(player));
            }
        }
    }

}
