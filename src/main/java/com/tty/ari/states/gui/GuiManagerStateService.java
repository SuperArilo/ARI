package com.tty.ari.states.gui;

import com.tty.api.annotations.gui.GuiMeta;
import com.tty.api.state.StateService;
import com.tty.ari.Ari;
import com.tty.ari.dto.state.GuiState;
import org.bukkit.entity.HumanEntity;

import java.util.List;

public class GuiManagerStateService extends StateService<GuiState> {

    public GuiManagerStateService(long rate, long c, boolean isAsync) {
        super(rate, c, isAsync, Ari.instance);
    }

    @Override
    protected boolean canAddState(GuiState state) {
        GuiMeta checkMeta = state.getBaseInventory().getClass().getAnnotation(GuiMeta.class);
        List<GuiState> states = this.getStates(state.getOwner());
        if (states.isEmpty()) return true;

        GuiMeta nowMeta = states.getFirst().getBaseInventory().getClass().getAnnotation(GuiMeta.class);
        return !checkMeta.type().equals(nowMeta.type());
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
        Ari.instance.getLog().debug("add state to player {} open inventory. type: {}", entity.getName(), state.getBaseInventory().getType());
        Ari.instance.getScheduler().run(i -> entity.openInventory(state.getBaseInventory().getInventory()));
    }

    @Override
    protected void onEarlyExit(GuiState state) {
        state.getBaseInventory().close();
        Ari.instance.getLog().debug("remove state to player {} inventory. type {}.", state.getOwner().getName(), state.getBaseInventory().getType());
    }

    @Override
    protected void onFinished(GuiState state) {
        state.getBaseInventory().close();
        Ari.instance.getLog().debug("remove state to player {} inventory. type {}.", state.getOwner().getName(), state.getBaseInventory().getType());
    }

    @Override
    protected void onServiceAbort(GuiState state) {
        state.getBaseInventory().close();
    }

    @Override
    public void onReload() {
        for (GuiState state : this.getAllStates()) {
            state.setOver(true);
        }
    }

}
