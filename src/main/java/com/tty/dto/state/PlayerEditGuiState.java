package com.tty.dto.state;

import com.tty.api.enumType.FunctionType;
import com.tty.api.gui.BaseInventory;
import com.tty.api.state.State;
import lombok.Getter;
import org.bukkit.entity.Entity;

public class PlayerEditGuiState extends State {

    @Getter
    private final BaseInventory i;
    @Getter
    private final FunctionType functionType;

    public PlayerEditGuiState(Entity owner, int count, BaseInventory i, FunctionType functionType) {
        super(owner, count);
        this.i = i;
        this.functionType = functionType;
    }

}
