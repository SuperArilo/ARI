package com.tty.ari.states;

import com.tty.api.state.StateService;
import com.tty.ari.Ari;
import com.tty.ari.dto.state.GuiState;
import org.bukkit.entity.HumanEntity;

public class GuiManagerStateService extends StateService<GuiState> {

    public GuiManagerStateService(long rate, long c, boolean isAsync) {
        super(rate, c, isAsync, Ari.instance);
    }

    @Override
    protected boolean canAddState(GuiState state) {
        return this.isNotHaveState(state.getOwner());
    }

    @Override
    protected void loopExecution(GuiState state) {
        state.setPending(false);
    }

    @Override
    protected void abortAddState(GuiState state) {

    }

    @Override
    protected void passAddState(GuiState state) {
        if (!(state.getOwner() instanceof HumanEntity entity)) return;
        Ari.instance.getLog().debug("add state to player {} open inventory. type: {}", entity.getName(), state.getMenu().getType());
        entity.openInventory(state.getMenu().getInventory());
    }

    @Override
    protected void onEarlyExit(GuiState state) {
        Ari.instance.getLog().debug("remove state to player {} inventory. type {}.", state.getOwner().getName(), state.getMenu().getType());
    }

    @Override
    protected void onFinished(GuiState state) {

    }

    @Override
    protected void onServiceAbort(GuiState state) {

    }

    @Override
    public void onReload() {

    }
}
