package com.tty.ari.dto.state;

import com.tty.api.gui.BaseInventory;
import com.tty.api.state.State;
import lombok.Getter;
import org.bukkit.entity.Entity;

public class GuiState extends State {

    @Getter
    private final BaseInventory menu;

    public GuiState(Entity owner, BaseInventory menu) {
        super(owner, Integer.MAX_VALUE);
        this.menu = menu;
    }

}
