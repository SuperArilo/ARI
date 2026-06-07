package com.tty.ari.dto.state;

import com.tty.api.gui.BaseInventory;
import com.tty.api.state.State;
import lombok.Getter;
import org.bukkit.entity.Entity;

public class GuiState<T extends BaseInventory> extends State {

    @Getter
    private final T menu;

    public GuiState(Entity owner, T menu) {
        super(owner, Integer.MAX_VALUE);
        this.menu = menu;
    }

}
