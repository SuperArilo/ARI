package com.tty.listener;

import com.tty.Ari;
import com.tty.api.FormatUtils;
import com.tty.api.enumType.FunctionType;
import com.tty.api.enumType.GuiType;
import com.tty.api.listener.BaseGuiListener;
import com.tty.dto.state.PlayerEditGuiState;
import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;

public abstract class BaseEditFunctionGuiListener extends BaseGuiListener {

    protected BaseEditFunctionGuiListener(GuiType guiType) {
        super(guiType);
    }

    @Override
    public void passClick(InventoryClickEvent event) {}

    @EventHandler
    public void onPlayerChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        PlayerEditGuiState state = this.isHaveState(player);
        if (state == null) return;
        if (!state.getI().type.equals(this.guiType)) return;
        event.setCancelled(true);
        String message = FormatUtils.componentToString(event.message());

        //玩家手动输入 cancel 取消操作
        if (FunctionType.CANCEL.name().equals(message.toUpperCase())) {
            state.setOver(true);
            player.clearTitle();
            player.sendMessage(Ari.COMPONENT_SERVICE.text(Ari.DATA_SERVICE.getValue("base.on-edit.cancel")));
            return;
        }

        //玩家输入内容检查通过
        if (this.onTitleEditStatus(message, state)) {
            player.clearTitle();
            state.setOver(true);
        }
    }

    public abstract PlayerEditGuiState isHaveState(Player player);

    /**
     * 检查玩家的输入内容
     * @param message 玩家的输入内容
     * @param state 玩家的输入状态类
     * @return true 检查通过，反之
     */
    public abstract boolean onTitleEditStatus(String message, PlayerEditGuiState state);

}
