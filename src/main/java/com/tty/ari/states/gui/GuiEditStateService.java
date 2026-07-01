package com.tty.ari.states.gui;

import com.google.common.reflect.TypeToken;
import com.tty.ari.Ari;
import com.tty.api.state.EditGuiState;
import com.tty.api.state.StateService;
import org.bukkit.entity.Player;

public class GuiEditStateService extends StateService<EditGuiState> {

    public GuiEditStateService(long rate, long c, boolean isAsync) {
        super(rate, c, isAsync, Ari.instance);
    }

    @Override
    protected boolean canAddState(EditGuiState state) {
        return this.isNotHaveState(state.getOwner());
    }

    @Override
    protected void loopExecution(EditGuiState state) {

        Player owner = (Player) state.getOwner();
        if (!owner.isOnline() || owner.isDead()) {
            state.setOver(true);
            return;
        }
        Ari.instance.getLog().debug("checking player {} edit gui {}. count = {} max_count = {}",
                owner.getName(),
                state.getFunctionType(),
                state.getCount(),
                state.getMax_count()
        );
    }

    @Override
    protected void abortAddState(EditGuiState state) {

    }

    @Override
    protected void passAddState(EditGuiState state) {
        Player owner = (Player) state.getOwner();
        int i = Ari.DATA_SERVICE.getValue("server.gui-edit-timeout", new TypeToken<Integer>(){}.getType());
        owner.showTitle(
                Ari.instance.getComponentTool().setPlayerTitle(
                        Ari.DATA_SERVICE.getValue("base.on-edit.title"),
                        Ari.DATA_SERVICE.getValue("base.on-edit.sub-title"),
                        1000,
                        i * 1000L,
                        1000));
    }

    @Override
    protected void onEarlyExit(EditGuiState state) {
        Player owner = (Player) state.getOwner();
        Ari.instance.getLog().debug("player {} edit status finish.", owner.getName());
    }

    @Override
    protected void onFinished(EditGuiState state) {
        Player owner = (Player) state.getOwner();
        owner.sendMessage(Ari.instance.getComponentTool().text(Ari.DATA_SERVICE.getValue("base.on-edit.timeout-cancel")));
        owner.clearTitle();
        Ari.instance.getLog().debug("player {} edit status timeout.", owner.getName());
    }

    @Override
    protected void onServiceAbort(EditGuiState state) {

    }

    @Override
    public void onReload() {
        for (EditGuiState state : this.getAllStates()) {
            state.setOver(true);
        }
    }
}
