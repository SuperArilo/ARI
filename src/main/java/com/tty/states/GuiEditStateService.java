package com.tty.states;

import com.tty.Ari;
import com.tty.lib.Lib;
import com.tty.dto.state.PlayerEditGuiState;
import com.tty.api.Log;
import com.tty.lib.enum_type.FilePath;
import com.tty.lib.services.StateService;
import com.tty.lib.tool.LibConfigUtils;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class GuiEditStateService extends StateService<PlayerEditGuiState> {

    public GuiEditStateService(long rate, long c, boolean isAsync, JavaPlugin javaPlugin) {
        super(rate, c, isAsync, javaPlugin);
    }

    @Override
    protected boolean canAddState(PlayerEditGuiState state) {
        return this.isNotHaveState(state.getOwner());
    }

    @Override
    protected void loopExecution(PlayerEditGuiState state) {

        Player owner = (Player) state.getOwner();
        if (!owner.isOnline()) {
            state.setOver(true);
            return;
        }
        state.setPending(false);
        Log.debug("checking player {} edit gui {}. type {}", owner.getName(), state.getI().type, state.getFunctionType());
    }

    @Override
    protected void abortAddState(PlayerEditGuiState state) {

    }

    @Override
    protected void passAddState(PlayerEditGuiState state) {
        Player owner = (Player) state.getOwner();
        int i = Lib.instance.getConfig().getInt("server.gui-edit-timeout", 10);
        owner.showTitle(
                Ari.COMPONENT_SERVICE.setPlayerTitle(
                        Lib.C_INSTANCE.getValue("base.on-edit.title", FilePath.Lang),
                        Lib.C_INSTANCE.getValue("base.on-edit.sub-title", FilePath.Lang),
                        1000,
                        i * 1000L,
                        1000));
    }

    @Override
    protected void onEarlyExit(PlayerEditGuiState state) {
        Player owner = (Player) state.getOwner();
        Log.debug("player {} edit status finish.", owner.getName());
    }

    @Override
    protected void onFinished(PlayerEditGuiState state) {
        Player owner = (Player) state.getOwner();
        owner.sendMessage(LibConfigUtils.t("base.on-edit.timeout-cancel"));
        owner.clearTitle();
        Log.debug("player {} edit status timeout.", owner.getName());
    }

    @Override
    protected void onServiceAbort(PlayerEditGuiState state) {

    }
}
