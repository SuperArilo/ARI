package com.tty.ari.states;

import com.tty.api.state.StateService;
import com.tty.ari.Ari;
import com.tty.ari.dto.state.player.PlayerPreCommandState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class PlayerCommandPreprocessService extends StateService<PlayerPreCommandState> {

    public PlayerCommandPreprocessService(long rate, long c, boolean isAsync) {
        super(rate, c, isAsync, Ari.instance);
    }

    @Override
    protected boolean canAddState(PlayerPreCommandState state) {
        Entity owner = state.getOwner();
        String command = state.getMainCommand();
        for (PlayerPreCommandState s : this.getAllStates()) {
            if (s.getMainCommand().equals(command) && s.getOwner().equals(owner)) {
                Ari.instance.getLog().debug("find same PlayerPreCommandState, player: {}, command: {}", owner.getName(), command);
                return false;
            }
        }
        return true;
    }

    @Override
    protected void loopExecution(PlayerPreCommandState state) {
        Entity owner = state.getOwner();
        if ((owner instanceof Player player && !player.isOnline())) {
            state.setOver(true);
            Ari.instance.getLog().debug("player {} is offline, over now.", owner.getName());
        } else  {
            state.setPending(false);
            Ari.instance.getLog().debug("player {} input command {} cooldown count {}.", owner, state.getMainCommand(), state.getCount().get());
        }
    }

    @Override
    protected void abortAddState(PlayerPreCommandState state) {

    }

    @Override
    protected void passAddState(PlayerPreCommandState state) {
        Ari.instance.getLog().debug("player {} input command {} cooldown now.", state.getOwner().getName(), state.getMainCommand());
    }

    @Override
    protected void onEarlyExit(PlayerPreCommandState state) {
        Ari.instance.getLog().debug("player {} input command {} cooldown end.", state.getOwner().getName(), state.getMainCommand());
    }

    @Override
    protected void onFinished(PlayerPreCommandState state) {
        Ari.instance.getLog().debug("player {} input command {} cooldown end.", state.getOwner().getName(), state.getMainCommand());
    }

    @Override
    protected void onServiceAbort(PlayerPreCommandState state) {

    }

    @Override
    public void onReload() {

    }

}
