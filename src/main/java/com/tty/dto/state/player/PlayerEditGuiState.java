package com.tty.dto.state.player;

import com.tty.Ari;
import com.tty.gui.BaseInventory;
import com.tty.lib.dto.State;
import com.tty.lib.enum_type.FunctionType;
import lombok.Getter;
import org.bukkit.entity.Entity;

public class PlayerEditGuiState extends State {

    @Getter
    private final BaseInventory i;
    @Getter
    private final FunctionType functionType;

    public PlayerEditGuiState(Entity owner, BaseInventory i, FunctionType functionType) {
        super(owner, Ari.instance.getConfig().getInt("server.gui-edit-timeout", 10));
        this.i = i;
        this.functionType = functionType;
    }
}
