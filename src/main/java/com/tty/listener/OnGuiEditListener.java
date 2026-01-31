package com.tty.listener;

import com.tty.Ari;
import com.tty.Log;
import com.tty.api.listener.BaseEditFunctionGuiListener;
import com.tty.api.state.PlayerEditGuiState;
import com.tty.enumType.GuiType;
import com.tty.states.GuiEditStateService;
import org.bukkit.entity.Player;

import java.util.List;

public abstract class OnGuiEditListener extends BaseEditFunctionGuiListener {

    protected OnGuiEditListener(GuiType guiType) {
        super(guiType);
    }

    @Override
    public PlayerEditGuiState isHaveState(Player player) {
        GuiEditStateService stateService = Ari.STATE_MACHINE_MANAGER.get(GuiEditStateService.class);
        if (stateService.stateIsEmpty()) return null;
        if (stateService.isNotHaveState(player)) return null;
        List<PlayerEditGuiState> states = stateService.getStates(player);
        if (states.isEmpty()) {
            Log.error("player {} on edit status error, states is empty", player.getName());
            return null;
        }
        return states.getFirst();
    }

}
