package com.tty.ari.states.gui;

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
        return getStates(state.getOwner()).stream().noneMatch(s -> s.getMenu().equals(state.getMenu()));
    }

    @Override
    protected void loopExecution(GuiState state) {
    }

    @Override
    protected void abortAddState(GuiState state) {

    }

    @Override
    protected void passAddState(GuiState state) {
        if (!(state.getOwner() instanceof HumanEntity entity)) return;
        Ari.instance.getLog().debug("add state to player {} open inventory. type: {}", entity.getName(), state.getMenu().getType());
        Ari.instance.getScheduler().run(Ari.instance, i -> entity.openInventory(state.getMenu().getInventory()));
    }

    @Override
    protected void onEarlyExit(GuiState state) {
        state.getMenu().close();
        Ari.instance.getLog().debug("remove state to player {} inventory. type {}.", state.getOwner().getName(), state.getMenu().getType());
    }

    @Override
    protected void onFinished(GuiState state) {
        state.getMenu().close();
        Ari.instance.getLog().debug("remove state to player {} inventory. type {}.", state.getOwner().getName(), state.getMenu().getType());
    }

    @Override
    protected void onServiceAbort(GuiState state) {
        state.getMenu().close();
    }

    @Override
    public void onReload() {
        for (GuiState state : this.getAllStates()) {
            state.setOver(true);
        }
    }

}
