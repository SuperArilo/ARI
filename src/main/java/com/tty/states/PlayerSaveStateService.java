package com.tty.states;

import com.tty.Ari;
import com.tty.dto.state.player.PlayerSaveState;
import com.tty.function.PlayerManager;
import com.tty.lib.Lib;
import com.tty.lib.Log;
import com.tty.lib.services.StateService;
import com.tty.lib.task.CancellableTask;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

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
        if (state.getOwner() instanceof Player p && !p.isOnline()) {
            Log.debug("player %s offline, stop save.", p.getName());
            state.setOver(true);
        }
        this.savePlayerData(state, true);
    }

    @Override
    protected void abortAddState(PlayerSaveState state) {

    }

    @Override
    protected void passAddState(PlayerSaveState state) {
        Log.debug("added player %s state to save data", state.getOwner().getName());
    }

    @Override
    protected void onEarlyExit(PlayerSaveState state) {
        Log.debug("stop save player %s data", state.getOwner().getName());
    }

    @Override
    protected void onFinished(PlayerSaveState state) {
        Log.debug("player %s exit server. save.", state.getOwner().getName());
        this.savePlayerData(state, true);
    }

    @Override
    protected void onServiceAbort(PlayerSaveState state) {
        CancellableTask task = state.getTask();
        if (task == null) return;
        task.cancel();
        state.setTask(null);
    }

    /**
     * 保存玩家在线时长数据数据
     * @param state 玩家的状态服务
     * @param asyncMode 保存模式。同步和异步
     */
    public void savePlayerData(PlayerSaveState state, boolean asyncMode) {
        synchronized (state) {
            if (state.isRunning() || state.isOver()) return;
            state.setRunning(true);
        }

        Player player = (Player) state.getOwner();
        this.manager.setExecutionMode(asyncMode);
        String uuid = player.getUniqueId().toString();

        long now = System.currentTimeMillis();
        long loginTime = state.getLoginTime();

        if (loginTime <= 0 || loginTime > now) {
            Log.warn("Invalid loginTime for %s: %d", player.getName(), loginTime);
            state.setLoginTime(now);
            state.setRunning(false);
            return;
        }

        long onlineDuration = now - loginTime;

        if (onlineDuration > TimeUnit.DAYS.toMillis(1)) {
            Log.warn("Abnormal onlineDuration %d ms for %s", onlineDuration, player.getName());
            onlineDuration = TimeUnit.DAYS.toMillis(1);
        }

        state.setLoginTime(now);
        long finalOnlineDuration = onlineDuration;

        this.manager.getInstance(uuid)
                .thenCompose(serverPlayer -> {
                    if (serverPlayer == null) {
                        Log.error("Player data not found: %s", uuid);
                        return CompletableFuture.completedFuture(false);
                    }
                    serverPlayer.setTotalOnlineTime(serverPlayer.getTotalOnlineTime() + finalOnlineDuration);
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
                    state.setRunning(false);
                    state.setTask(Lib.Scheduler.runAsyncDelayed(
                        Ari.instance,
                        i -> {
                            state.setPending(false);
                            state.setTask(null);
                        },
                        Ari.instance.getConfig().getInt("server.save-interval", 300) * 20L));
                });
    }

    public static void addPlayerState() {
        PlayerSaveStateService service = Ari.instance.stateMachineManager.get(PlayerSaveStateService.class);
        for (Player player : Bukkit.getOnlinePlayers()) {
            service.addState(new PlayerSaveState(player, System.currentTimeMillis()));
        }
    }

}
