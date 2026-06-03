package com.tty.ari.dto.gui;

import com.tty.api.dto.gui.BaseMenu;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class PlayerInventoryCheckMenu extends BaseMenu {

    private List<Integer> shortcutBar;
    private List<Integer> playerInventory;

}
