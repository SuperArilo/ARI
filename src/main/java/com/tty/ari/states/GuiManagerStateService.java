package com.tty.ari.states;

import com.tty.api.state.StateService;
import com.tty.ari.Ari;
import com.tty.ari.dto.state.player.OnCheckPlayerGuiState;
import org.bukkit.entity.HumanEntity;

public class GuiManagerStateService extends StateService<OnCheckPlayerGuiState> {

    public GuiManagerStateService(long rate, long c, boolean isAsync) {
        super(rate, c, isAsync, Ari.instance);
    }

    @Override
    protected boolean canAddState(OnCheckPlayerGuiState state) {
        return this.isNotHaveState(state.getOwner());
    }

    @Override
    protected void loopExecution(OnCheckPlayerGuiState state) {
        state.setPending(false);
    }

    @Override
    protected void abortAddState(OnCheckPlayerGuiState state) {

    }

    @Override
    protected void passAddState(OnCheckPlayerGuiState state) {
        if (!(state.getOwner() instanceof HumanEntity entity)) return;
        entity.openInventory(state.getMenu().getInventory());
    }

    @Override
    protected void onEarlyExit(OnCheckPlayerGuiState state) {

    }

    @Override
    protected void onFinished(OnCheckPlayerGuiState state) {

    }

    @Override
    protected void onServiceAbort(OnCheckPlayerGuiState state) {

    }

    @Override
    public void onReload() {

    }
}
