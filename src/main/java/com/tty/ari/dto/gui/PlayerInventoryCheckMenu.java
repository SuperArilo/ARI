package com.tty.ari.dto.gui;

import com.google.gson.annotations.Expose;
import com.tty.api.dto.gui.BaseMenu;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class PlayerInventoryCheckMenu extends BaseMenu {

    @Expose
    private List<Integer> shortcutBar;
    @Expose
    private List<Integer> playerInventory;

}
