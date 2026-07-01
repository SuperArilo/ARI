package com.tty.ari.states;

import com.tty.api.state.StateService;
import com.tty.ari.Ari;
import com.tty.ari.dto.state.player.PlayerChatState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class PlayerChatService extends StateService<PlayerChatState> {

    public PlayerChatService(long rate, long c, boolean isAsync) {
        super(rate, c, isAsync, Ari.instance);
    }

    @Override
    protected boolean canAddState(PlayerChatState state) {
        for (PlayerChatState existing : this.getAllStates()) {
            if (existing.getOwner().equals(state.getOwner())) {
                Ari.instance.getLog().debug("player {} chat is cooling-down now.", state.getOwner().getName());
                return false;
            }
        }
        return true;
    }

    @Override
    protected void loopExecution(PlayerChatState state) {
        Entity owner = state.getOwner();
        if (owner instanceof Player player && !player.isOnline()) {
            state.setOver(true);
            Ari.instance.getLog().debug("player {} is offline，over.", owner.getName());
        } else {
            Ari.instance.getLog().debug("player {} cooldown count: {}.", owner.getName(), state.getRemain());
        }

    }

    @Override
    protected void abortAddState(PlayerChatState state) {

    }

    @Override
    protected void passAddState(PlayerChatState state) {
        Ari.instance.getLog().debug("player {} add chat cooldown state.", state.getOwner().getName());
    }

    @Override
    protected void onEarlyExit(PlayerChatState state) {
        Ari.instance.getLog().debug("player {} chat cooldown early stop.", state.getOwner().getName());
    }

    @Override
    protected void onFinished(PlayerChatState state) {
        Ari.instance.getLog().debug("player {} chat cooldown finished.", state.getOwner().getName());
    }

    @Override
    protected void onServiceAbort(PlayerChatState state) {

    }

    @Override
    public void onReload() {

    }
}
