package com.tty.ari.states.gui;

import com.tty.ari.Ari;
import com.tty.ari.dto.state.CooldownState;
import com.tty.api.state.StateService;
import org.bukkit.entity.Entity;

public class CoolDownStateService extends StateService<CooldownState> {

    public CoolDownStateService(long rate, long c, boolean isAsync) {
        super(rate, c, isAsync, Ari.instance);
    }

    @Override
    protected boolean canAddState(CooldownState state) {
        return this.getStates(state.getOwner()).isEmpty();
    }

    @Override
    protected void loopExecution(CooldownState state) {

        Entity owner = state.getOwner();
        if (Ari.PERMISSION_SERVICE.hasPermission(owner, "ari.cooldown." + state.getType().getKey())) {
            state.setOver(true);
            return;
        }
        state.setPending(false);
        Ari.instance.getLog().debug("entity {} teleport cd time is cooling down. count {}, max_count {}", state.getOwner().getName(), state.getCount(), state.getMax_count());
    }

    @Override
    protected void abortAddState(CooldownState state) {

    }

    @Override
    protected void passAddState(CooldownState state) {

    }

    @Override
    protected void onEarlyExit(CooldownState state) {
        Ari.instance.getLog().debug("entity {} cd time has ended.", state.getOwner().getName());
    }

    @Override
    protected void onFinished(CooldownState state) {
        Ari.instance.getLog().debug("entity {} cd time has ended.", state.getOwner().getName());
    }

    @Override
    protected void onServiceAbort(CooldownState state) {

    }

    @Override
    public void onReload() {

    }
}
