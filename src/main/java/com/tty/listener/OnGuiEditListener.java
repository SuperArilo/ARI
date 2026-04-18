package com.tty.listener;

import com.tty.Ari;
import com.tty.api.BaseJavaPlugin;
import com.tty.api.gui.BaseInventory;
import com.tty.api.listener.BaseEditFunctionGuiListener;
import com.tty.api.state.EditGuiState;
import com.tty.enumType.GuiType;
import com.tty.states.GuiEditStateService;
import org.bukkit.entity.Player;

import java.util.List;

public abstract class OnGuiEditListener<T extends BaseInventory> extends BaseEditFunctionGuiListener<T> {

    protected OnGuiEditListener(BaseJavaPlugin plugin, GuiType guiType) {
        super(plugin, guiType);
    }

    @Override
    public EditGuiState isHaveState(Player player) {
        GuiEditStateService stateService = Ari.STATE_MACHINE_MANAGER.get(GuiEditStateService.class);
        if (stateService.stateIsEmpty()) return null;
        if (stateService.isNotHaveState(player)) return null;
        List<EditGuiState> states = stateService.getStates(player);
        if (states.isEmpty()) {
            Ari.instance.getLog().error("player {} on edit status error, states is empty", player.getName());
            return null;
        }
        return states.getFirst();
    }
}
