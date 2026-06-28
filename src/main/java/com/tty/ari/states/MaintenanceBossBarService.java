package com.tty.ari.states;

import com.tty.api.state.StateService;
import com.tty.ari.Ari;
import com.tty.ari.dto.state.player.MaintenanceBossBarState;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;

public class MaintenanceBossBarService extends StateService<MaintenanceBossBarState> {

    @Getter
    @Setter
    private boolean maintenance;

    public MaintenanceBossBarService(long rate, long c, boolean isAsync) {
        super(rate, c, isAsync, Ari.instance);
    }

    @Override
    protected boolean canAddState(MaintenanceBossBarState state) {
        return true;
    }

    @Override
    protected void loopExecution(MaintenanceBossBarState state) {
        if (!(state.getOwner() instanceof Player player)) {
            state.setOver(true);
            return;
        }
        if (!player.isOnline()) {
            state.setOver(true);
            return;
        }
        player.showBossBar(state.getBossBar());
    }

    @Override
    protected void abortAddState(MaintenanceBossBarState state) {

    }

    @Override
    protected void passAddState(MaintenanceBossBarState state) {
        Ari.instance.getLog().debug("add boss bar to player {}.", state.getOwner().getName());
    }

    @Override
    protected void onEarlyExit(MaintenanceBossBarState state) {
        if (!((state.getOwner()) instanceof Player player)) return;
        Ari.instance.getScheduler().runAtEntity(Ari.instance, player, i -> {
            if (player.isOnline()) {
                player.hideBossBar(state.getBossBar());
            }
        }, null);
    }

    @Override
    protected void onFinished(MaintenanceBossBarState state) {}

    @Override
    protected void onServiceAbort(MaintenanceBossBarState state) {
        if (!(state.getOwner() instanceof Player player)) return;
        player.hideBossBar(state.getBossBar());
    }

    @Override
    public void onReload() {
        for (MaintenanceBossBarState state : this.getAllStates()) {
            state.setOver(true);
        }
    }

}
