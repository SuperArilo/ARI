package com.tty.listener;

import com.tty.Ari;
import com.tty.lib.dto.state.PlayerEditGuiState;
import com.tty.lib.Log;
import com.tty.lib.enum_type.GuiType;
import com.tty.lib.listener.BaseEditFunctionGuiListener;
import com.tty.states.GuiEditStateService;
import org.bukkit.entity.Player;

import java.util.List;

public abstract class OnGuiEditListener extends BaseEditFunctionGuiListener {

    protected OnGuiEditListener(GuiType guiType) {
        super(guiType);
    }

    @Override
    public PlayerEditGuiState isHaveState(Player player) {
        GuiEditStateService stateService = Ari.instance.stateMachineManager.get(GuiEditStateService.class);
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
