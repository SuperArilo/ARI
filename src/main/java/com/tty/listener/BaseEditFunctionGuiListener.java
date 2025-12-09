package com.tty.listener;

import com.tty.Ari;
import com.tty.dto.state.player.PlayerEditGuiState;
import com.tty.enumType.GuiType;
import com.tty.lib.Log;
import com.tty.lib.enum_type.FunctionType;
import com.tty.lib.tool.ComponentUtils;
import com.tty.lib.tool.FormatUtils;
import com.tty.states.GuiEditStateService;
import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.List;

public abstract class BaseEditFunctionGuiListener extends BaseGuiListener {

    protected BaseEditFunctionGuiListener(GuiType guiType) {
        super(guiType);
    }

    @Override
    public void passClick(InventoryClickEvent event) {}

    @EventHandler
    public void onPlayerChat(AsyncChatEvent event) {
        GuiEditStateService stateService = Ari.instance.stateMachineManager.get(GuiEditStateService.class);
        Player player = event.getPlayer();
        if (stateService.getSTATE_LIST().isEmpty()) return;
        if (stateService.isNotHaveState(player)) return;
        List<PlayerEditGuiState> states = stateService.getStates(player);
        if (states.isEmpty()) {
            Log.error("player %s on edit status error, states is empty", player.getName());
            return;
        }
        PlayerEditGuiState first = states.getFirst();
        if (!first.getHolder().type().equals(this.guiType)) return;
        event.setCancelled(true);
        String message = FormatUtils.componentToString(event.message());

        //玩家手动输入 cancel 取消操作
        if (FunctionType.CANCEL.name().equals(message.toUpperCase())) {
            first.setOver(true);
            player.clearTitle();
            player.sendMessage(ComponentUtils.text(Ari.instance.dataService.getValue("base.on-edit.cancel")));
            return;
        }

        //玩家输入内容检查通过
        if (this.onTitleEditStatus(message, first)) {
            player.clearTitle();
            first.setOver(true);
        }
    }

    /**
     * 检查玩家的输入内容
     * @param message 玩家的输入内容
     * @param state 玩家的输入状态类
     * @return true 检查通过，反之
     */
    public abstract boolean onTitleEditStatus(String message, PlayerEditGuiState state);

}
