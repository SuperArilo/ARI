package com.tty.ari.states;

import com.tty.api.StatusManager;
import com.tty.api.state.StateService;
import com.tty.ari.Ari;
import com.tty.api.event.PlayerEnterAFKEvent;
import com.tty.api.event.PlayerLeaveAFKEvent;
import com.tty.ari.dto.state.player.PlayerAFKState;
import com.tty.ari.states.gui.GuiEditFunctionStateService;
import com.tty.ari.states.gui.GuiManagerStateService;
import com.tty.ari.states.teleport.PreTeleportStateService;
import com.tty.ari.states.teleport.RandomTpStateService;
import com.tty.ari.states.teleport.TeleportStateService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PlayerAFKService extends StateService<PlayerAFKState> {

    public PlayerAFKService(long rate, long c, boolean isAsync) {
        super(rate, c, isAsync, Ari.instance);
    }

    @Override
    protected boolean canAddState(PlayerAFKState state) {
        boolean empty = this.getStates(state.getOwner()).isEmpty();
        if (!empty) {
            Ari.instance.getLog().warn("try add same player {}, break.", state.getOwner().getName());
        }
        return empty;
    }

    @Override
    protected void loopExecution(PlayerAFKState state) {
        if (!(state.getOwner() instanceof Player player) || !player.isOnline()) {
            Ari.instance.getLog().debug("player {} leave game, remove status.", state.getOwner().getName());
            state.setOver(true);
            return;
        }
        if (!Ari.instance.getConfig().getBoolean("server.afk.enable")) {
            state.setRunning(false);
            if (state.isSent()) {
                player.clearTitle();
            }
            return;
        }

        StatusManager manager = Ari.instance.getStatusManager();
        GuiEditFunctionStateService guiEditFunctionStateService = manager.get(GuiEditFunctionStateService.class);
        GuiManagerStateService guiManagerStateService = manager.get(GuiManagerStateService.class);
        RandomTpStateService randomTpStateService = manager.get(RandomTpStateService.class);
        PreTeleportStateService preTeleportStateService = manager.get(PreTeleportStateService.class);
        TeleportStateService teleportStateService = manager.get(TeleportStateService.class);

        boolean aboutOp = !player.isOp() && Ari.PERMISSION_SERVICE.hasPermission(player, "ari.pass-afk");

        if (!guiEditFunctionStateService.isNotHaveState(player) ||
                !guiManagerStateService.isNotHaveState(player) ||
                !randomTpStateService.isNotHaveState(player) ||
                !preTeleportStateService.isNotHaveState(player) ||
                !teleportStateService.isNotHaveState(player) ||
                player.isDead() ||
                aboutOp) {

            if (state.isSent()) {
                player.clearTitle();
            }
            state.resetStandCount();
            state.setRunning(false);
            return;

        }

        state.addStandCount();
        Ari.instance.getScheduler().run(i -> {
            if (state.isAFK()) {
                if (!state.isSent()) {
                    if (!new PlayerEnterAFKEvent(player).callEvent()) {
                        state.resetStandCount();
                    } else {
                        state.setSent(true);
                        Ari.instance.getLog().debug("player {} afk status: {}.", player.getName(), state.isAFK());
                    }
                }
            } else {
                if (state.isSent()) {
                    Bukkit.getServer().getPluginManager().callEvent(new PlayerLeaveAFKEvent(player));
                    state.setSent(false);
                }
            }
            state.setRunning(false);
        });
    }

    @Override
    protected void abortAddState(PlayerAFKState state) {}

    @Override
    protected void passAddState(PlayerAFKState state) {}

    @Override
    protected void onEarlyExit(PlayerAFKState state) {
        if (!(state.getOwner() instanceof Player player) || !player.isOnline()) return;
        Ari.instance.getScheduler().runLater(i -> this.addState(new PlayerAFKState(state.getOwner())), 20L);
    }

    @Override
    protected void onFinished(PlayerAFKState state) {
        if (!(state.getOwner() instanceof Player player) || !player.isOnline()) return;
        Ari.instance.getScheduler().runLater(i -> this.addState(new PlayerAFKState(state.getOwner())), 20L);
    }

    @Override
    protected void onServiceAbort(PlayerAFKState state) {
        state.setOver(true);
    }

    @Override
    public void onReload() {
        for (PlayerAFKState state : this.getAllStates()) {
            state.setOver(true);
            if (state.getOwner() instanceof Player player) {
                player.clearTitle();
                Ari.instance.getLog().debug("plugin reload, remove player {} afk status.", player.getName());
            }
        }
    }

}