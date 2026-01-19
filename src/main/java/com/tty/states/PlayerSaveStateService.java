package com.tty.states;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tty.Ari;
import com.tty.dto.event.OnZakoSavedEvent;
import com.tty.dto.state.player.PlayerSaveState;
import com.tty.entity.ServerPlayer;
import com.tty.lib.Lib;
import com.tty.lib.Log;
import com.tty.lib.services.EntityRepository;
import com.tty.lib.services.StateService;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.CompletableFuture;

public class PlayerSaveStateService extends StateService<PlayerSaveState> {

    private final EntityRepository<ServerPlayer> repository;

    public PlayerSaveStateService(long rate, long c, boolean isAsync, JavaPlugin javaPlugin) {
        super(rate, c, isAsync, javaPlugin);
        this.repository = Ari.REPOSITORY_MANAGER.get(ServerPlayer.class);
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
        Log.debug("added player {} state to save.", state.getOwner().getName());
    }

    @Override
    protected void onEarlyExit(PlayerSaveState state) {
        Log.debug("stop save player {} data", state.getOwner().getName());
    }

    @Override
    protected void onFinished(PlayerSaveState state) {
        Log.debug("start save player data {}.", state.getOwner().getName());
        this.savePlayerData(state, true);
    }

    @Override
    protected void onServiceAbort(PlayerSaveState state) {
        Log.debug("player save service abort. saving {}.", state.getOwner().getName());
        this.savePlayerData(state, !Ari.PLUGIN_IS_DISABLED);
    }

    /**
     * 保存玩家在线时长数据数据
     * @param state 玩家的状态服务
     * @param asyncMode 保存模式。同步和异步
     */
    public void savePlayerData(PlayerSaveState state, boolean asyncMode) {

        Player player = (Player) state.getOwner();
        this.repository.setExecutionMode(asyncMode);
        String uuid = player.getUniqueId().toString();

        long onlineDuration = System.currentTimeMillis() -  state.getLoginTime();

        this.repository.get(new LambdaQueryWrapper<>(ServerPlayer.class).eq(ServerPlayer::getPlayerUUID, uuid))
            .thenCompose(serverPlayer -> {
                if (serverPlayer == null) {
                    Log.error("Player data not found: {}", uuid);
                    return CompletableFuture.completedFuture(false);
                }
                serverPlayer.setTotalOnlineTime(serverPlayer.getTotalOnlineTime() + onlineDuration);
                return this.repository.update(serverPlayer);
            })
            .thenAccept(success -> {
                if (success) {
                    Log.debug("Saved player data: {}", player.getName());
                } else {
                    Log.error("Failed to save player data: {}", player.getName());
                }
            })
            .whenComplete((result, ex) -> {
                if (ex != null) {
                    Log.error(ex, "Error saving player data for {}", player.getName());
                }
                if (this.repository.isAsync() && player.isOnline()) {
                    Lib.Scheduler.run(Ari.instance, i -> Bukkit.getPluginManager().callEvent(new OnZakoSavedEvent(player)));
                } else {
                    Log.debug("skip player {} save event.", player.getName());
                }
            });
    }

    public static void addPlayerState() {
        PlayerSaveStateService service = Ari.STATE_MACHINE_MANAGER.get(PlayerSaveStateService.class);
        for (Player player : Bukkit.getOnlinePlayers()) {
            service.addState(new PlayerSaveState(player, System.currentTimeMillis()));
        }
    }

}
