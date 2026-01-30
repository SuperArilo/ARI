package com.tty.dto.state.action;

import lombok.Getter;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;

public class PlayerSitActionState extends PlayerActionState {

    @Getter
    public final Block sitBlock;

    public PlayerSitActionState(Entity owner, Block sitBlock) {
        super(owner);
        this.sitBlock = sitBlock;
    }

}
