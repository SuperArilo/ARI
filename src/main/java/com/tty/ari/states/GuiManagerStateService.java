package com.tty.ari.states;

import com.tty.api.state.StateService;
import com.tty.ari.Ari;
import com.tty.ari.dto.state.GuiState;
import com.tty.ari.gui.PlayerInventoryEdit;
import org.bukkit.entity.HumanEntity;

public class GuiManagerStateService extends StateService<GuiState<PlayerInventoryEdit>> {

    public GuiManagerStateService(long rate, long c, boolean isAsync) {
        super(rate, c, isAsync, Ari.instance);
    }

    @Override
    protected boolean canAddState(GuiState<PlayerInventoryEdit> state) {
        return this.isNotHaveState(state.getOwner());
    }

    @Override
    protected void loopExecution(GuiState<PlayerInventoryEdit> state) {
        state.setPending(false);
    }

    @Override
    protected void abortAddState(GuiState<PlayerInventoryEdit> state) {

    }

    @Override
    protected void passAddState(GuiState<PlayerInventoryEdit> state) {
        if (!(state.getOwner() instanceof HumanEntity entity)) return;
        entity.openInventory(state.getMenu().getInventory());
    }

    @Override
    protected void onEarlyExit(GuiState<PlayerInventoryEdit> state) {

    }

    @Override
    protected void onFinished(GuiState<PlayerInventoryEdit> state) {

    }

    @Override
    protected void onServiceAbort(GuiState<PlayerInventoryEdit> state) {

    }

    @Override
    public void onReload() {

    }
}
