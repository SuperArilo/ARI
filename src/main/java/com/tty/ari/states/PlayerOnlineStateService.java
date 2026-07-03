package com.tty.ari.states;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tty.ari.Ari;
import com.tty.api.repository.PartitionKey;
import com.tty.ari.dto.state.player.PlayerOnlineState;
import com.tty.ari.entity.ServerPlayer;
import com.tty.api.repository.EntityRepository;
import com.tty.api.state.StateService;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.concurrent.CompletableFuture;

public class PlayerOnlineStateService extends StateService<PlayerOnlineState> {
    
    public PlayerOnlineStateService(long rate, long c, boolean isAsync) {
        super(rate, c, isAsync, Ari.instance);
    }

    @Override
    protected boolean canAddState(PlayerOnlineState state) {
        return this.isNotHaveState(state.getOwner());
    }

    @Override
    protected void loopExecution(PlayerOnlineState state) {

    }

    @Override
    protected void abortAddState(PlayerOnlineState state) {

    }

    @Override
    protected void passAddState(PlayerOnlineState state) {
        Ari.instance.getLog().debug("added player {} state to save.", state.getOwner().getName());
    }

    @Override
    protected void onEarlyExit(PlayerOnlineState state) {
        Ari.instance.getLog().debug("stop save player {} data", state.getOwner().getName());
    }

    @Override
    protected void onFinished(PlayerOnlineState state) {
        Ari.instance.getLog().debug("start save player data {}.", state.getOwner().getName());
        this.savePlayerData(state);
    }

    @Override
    protected void onServiceAbort(PlayerOnlineState state) {
        Ari.instance.getLog().debug("player save service abort. saving {}.", state.getOwner().getName());
        this.savePlayerData(state);
    }

    @Override
    public void onReload() {
    }

    /**
     * 保存玩家在线时长数据数据
     * @param state 玩家的状态服务
     */
    public void savePlayerData(PlayerOnlineState state) {

        Player player = (Player) state.getOwner();
        boolean stopping = Bukkit.getServer().isStopping();
        EntityRepository<ServerPlayer> repository = Ari.REPOSITORY_MANAGER.get(ServerPlayer.class);

        repository.setExecutionMode(!stopping);
        String uuid = player.getUniqueId().toString();

        long nowTime = System.currentTimeMillis();
        long onlineDuration = nowTime - state.getLoginTime();

        LambdaQueryWrapper<ServerPlayer> wrapper = new LambdaQueryWrapper<>(ServerPlayer.class).eq(ServerPlayer::getPlayerUUID, uuid);

        repository.get(wrapper, PartitionKey.global()).thenCompose(serverPlayer -> {
            if (serverPlayer == null) {
                Ari.instance.getLog().error("Player data not found: {}", uuid);
                return CompletableFuture.completedFuture(false);
            }
            serverPlayer.setTotalOnlineTime(serverPlayer.getTotalOnlineTime() + onlineDuration);
            if (!player.isOnline() || stopping) {
                serverPlayer.setLastLoginOffTime(nowTime);
            }
            return repository.update(serverPlayer, wrapper, PartitionKey.global());
        }).thenAccept(success -> {
            if (success) {
                Ari.instance.getLog().debug("Saved player data: {}", player.getName());
            } else {
                Ari.instance.getLog().error("Failed to save player data: {}", player.getName());
            }
        }).whenComplete((result, ex) -> {
            if (ex != null) {
                Ari.instance.getLog().error(ex, "Error saving player data for {}", player.getName());
            }
            if (repository.isAsync() && player.isOnline()) {
                Ari.instance.getScheduler().runLater(Ari.instance, i -> this.addState(new PlayerOnlineState(player, System.currentTimeMillis())), 20L);
            } else {
                Ari.instance.getLog().debug("skip player {} save event.", player.getName());
            }
        });
    }

}
