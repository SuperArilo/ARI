package com.tty.states;

import com.tty.Ari;
import com.tty.api.Log;
import com.tty.dto.state.CooldownState;
import com.tty.lib.services.StateService;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.java.JavaPlugin;


public class CoolDownStateService extends StateService<CooldownState> {

    public CoolDownStateService(long rate, long c, boolean isAsync, JavaPlugin javaPlugin) {
        super(rate, c, isAsync, javaPlugin);
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
        Log.debug("entity {} teleport cd time is cooling down. count {}, max_count {}", state.getOwner().getName(), state.getCount(), state.getMax_count());
    }

    @Override
    protected void abortAddState(CooldownState state) {

    }

    @Override
    protected void passAddState(CooldownState state) {

    }

    @Override
    protected void onEarlyExit(CooldownState state) {
        Log.debug("entity {} cd time has ended.", state.getOwner().getName());
    }

    @Override
    protected void onFinished(CooldownState state) {
        Log.debug("entity {} cd time has ended.", state.getOwner().getName());
    }

    @Override
    protected void onServiceAbort(CooldownState state) {

    }
}
