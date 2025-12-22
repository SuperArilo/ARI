package com.tty.states;

import com.tty.Ari;
import com.tty.dto.event.OnZakoSavedEvent;
import com.tty.dto.state.player.PlayerSaveState;
import com.tty.function.PlayerManager;
import com.tty.lib.Lib;
import com.tty.lib.Log;
import com.tty.lib.services.StateService;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.CompletableFuture;

public class PlayerSaveStateService extends StateService<PlayerSaveState> {

    public final PlayerManager manager = new PlayerManager(true);

    public PlayerSaveStateService(long rate, long c, boolean isAsync, JavaPlugin javaPlugin) {
        super(rate, c, isAsync, javaPlugin);
    }

    @Override
    protected boolean canAddState(PlayerSaveState state) {
        return this.isNotHaveState(state.getOwner());
    }

    @Override
    protected void loopExecution(PlayerSaveState state) {
        state.setPending(false);
    }

    @Override
    protected void abortAddState(PlayerSaveState state) {

    }

    @Override
    protected void passAddState(PlayerSaveState state) {
        Log.debug("added player %s state to save.", state.getOwner().getName());
    }

    @Override
    protected void onEarlyExit(PlayerSaveState state) {
        Log.debug("stop save player %s data", state.getOwner().getName());
    }

    @Override
    protected void onFinished(PlayerSaveState state) {
        Log.debug("start save player data %s.", state.getOwner().getName());
        this.savePlayerData(state, true);
    }

    @Override
    protected void onServiceAbort(PlayerSaveState state) {
        Log.debug("player save service abort. saving %s.", state.getOwner().getName());
        this.savePlayerData(state, !Ari.PLUGIN_IS_DISABLED);
    }

    /**
     * 保存玩家在线时长数据数据
     * @param state 玩家的状态服务
     * @param asyncMode 保存模式。同步和异步
     */
    public void savePlayerData(PlayerSaveState state, boolean asyncMode) {

        Player player = (Player) state.getOwner();
        this.manager.setExecutionMode(asyncMode);
        String uuid = player.getUniqueId().toString();

        long onlineDuration = System.currentTimeMillis() -  state.getLoginTime();

        this.manager.getInstance(uuid)
            .thenCompose(serverPlayer -> {
                if (serverPlayer == null) {
                    Log.error("Player data not found: %s", uuid);
                    return CompletableFuture.completedFuture(false);
                }
                serverPlayer.setTotalOnlineTime(serverPlayer.getTotalOnlineTime() + onlineDuration);
                return this.manager.modify(serverPlayer);
            })
            .thenAccept(success -> {
                if (success) {
                    Log.debug("Saved player data: %s", player.getName());
                } else {
                    Log.error("Failed to save player data: %s", player.getName());
                }
            })
            .whenComplete((result, ex) -> {
                if (ex != null) {
                    Log.error(ex, "Error saving player data for %s", player.getName());
                }
                if (this.manager.isAsync && player.isOnline()) {
                    Lib.Scheduler.run(Ari.instance, i -> Bukkit.getPluginManager().callEvent(new OnZakoSavedEvent(player)));
                } else {
                    Log.debug("skip player %s save event.", player.getName());
                }
            });
    }

    public static void addPlayerState() {
        PlayerSaveStateService service = Ari.instance.stateMachineManager.get(PlayerSaveStateService.class);
        for (Player player : Bukkit.getOnlinePlayers()) {
            service.addState(new PlayerSaveState(player, System.currentTimeMillis()));
        }
    }

}
