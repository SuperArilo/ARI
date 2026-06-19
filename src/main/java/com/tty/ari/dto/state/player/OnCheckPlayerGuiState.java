package com.tty.ari.dto.state.player;

import com.tty.api.gui.BaseInventory;
import com.tty.ari.dto.state.GuiState;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;

public class OnCheckPlayerGuiState extends GuiState {

    @Getter
    private final OfflinePlayer monitoree;

    @Getter
    @Setter
    private volatile boolean updating;

    public OnCheckPlayerGuiState(Entity surveillant,  OfflinePlayer monitoree, BaseInventory menu) {
        super(surveillant, menu);
        this.monitoree = monitoree;
    }
}
