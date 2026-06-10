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
        return getStates(state.getOwner()).stream().noneMatch(s -> s.getMenu().equals(state.getMenu()));
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
        Ari.instance.getScheduler().runAtEntity(Ari.instance, entity, i -> entity.openInventory(state.getMenu().getInventory()), null);
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

    }

    @Override
    public void onReload() {

    }
}
