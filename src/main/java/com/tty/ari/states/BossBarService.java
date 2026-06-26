package com.tty.ari.states;

import com.tty.api.state.StateService;
import com.tty.ari.Ari;
import com.tty.ari.dto.state.player.ShowBossBarState;
import org.bukkit.entity.Player;

public class BossBarService extends StateService<ShowBossBarState> {

    public BossBarService(long rate, long c, boolean isAsync) {
        super(rate, c, isAsync, Ari.instance);
    }

    @Override
    protected boolean canAddState(ShowBossBarState state) {
        return true;
    }

    @Override
    protected void loopExecution(ShowBossBarState state) {
        if (!(state.getOwner() instanceof Player player)) {
            state.setPending(true);
            return;
        }
        player.showBossBar(state.getBossBar());
    }

    @Override
    protected void abortAddState(ShowBossBarState state) {

    }

    @Override
    protected void passAddState(ShowBossBarState state) {
        Ari.instance.getLog().debug("add boss bar to player {}.", state.getOwner().getName());
    }

    @Override
    protected void onEarlyExit(ShowBossBarState state) {
        if (!((state.getOwner()) instanceof Player player)) return;
        Ari.instance.getScheduler().runAtEntity(Ari.instance, player, i -> {
            if (player.isOnline()) {
                player.hideBossBar(state.getBossBar());
            }
        }, null);
    }

    @Override
    protected void onFinished(ShowBossBarState state) {}

    @Override
    protected void onServiceAbort(ShowBossBarState state) {
        if (!(state.getOwner() instanceof Player player)) return;
        player.hideBossBar(state.getBossBar());
    }

    @Override
    public void onReload() {
        for (ShowBossBarState state : this.getAllStates()) {
            state.setOver(true);
        }
    }

}
