package com.tty.states;

import com.google.common.reflect.TypeToken;
import com.tty.Ari;
import com.tty.api.utils.ComponentUtils;
import com.tty.api.annotations.gui.GuiMeta;
import com.tty.api.gui.BaseInventory;
import com.tty.api.state.EditGuiState;
import com.tty.api.state.StateService;
import org.bukkit.entity.Player;

public class GuiEditStateService extends StateService<EditGuiState> {

    public GuiEditStateService(long rate, long c, boolean isAsync) {
        super(rate, c, isAsync, Ari.instance, Ari.SCHEDULER);
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
        state.setPending(false);
        BaseInventory i = state.getI();
        GuiMeta annotation = i.getClass().getAnnotation(GuiMeta.class);
        Ari.LOG.debug("checking player {} edit gui {}, type {}. count = {} max_count = {}",
                owner.getName(),
                annotation.type(),
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
                ComponentUtils.setPlayerTitle(
                        Ari.DATA_SERVICE.getValue("base.on-edit.title"),
                        Ari.DATA_SERVICE.getValue("base.on-edit.sub-title"),
                        1000,
                        i * 1000L,
                        1000));
    }

    @Override
    protected void onEarlyExit(EditGuiState state) {
        Player owner = (Player) state.getOwner();
        Ari.LOG.debug("player {} edit status finish.", owner.getName());
    }

    @Override
    protected void onFinished(EditGuiState state) {
        Player owner = (Player) state.getOwner();
        owner.sendMessage(ComponentUtils.text(Ari.DATA_SERVICE.getValue("base.on-edit.timeout-cancel")));
        owner.clearTitle();
        Ari.LOG.debug("player {} edit status timeout.", owner.getName());
    }

    @Override
    protected void onServiceAbort(EditGuiState state) {

    }
}
