package com.tty.ari.states;

import com.tty.api.persistence.CachedRepository;
import com.tty.api.persistence.query.Patch;
import com.tty.api.persistence.query.Query;
import com.tty.api.state.StateService;
import com.tty.ari.Ari;
import com.tty.ari.dto.state.player.PlayerSaveState;
import com.tty.ari.entity.ServerPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class PlayerSaveDataStateService extends StateService<PlayerSaveState> {

    public PlayerSaveDataStateService(long rate, long c, boolean isAsync) {
        super(rate, c, isAsync, Ari.instance);
    }

    @Override
    protected boolean canAddState(PlayerSaveState state) {
        return this.isNotHaveState(state.getOwner());
    }

    @Override
    protected void loopExecution(PlayerSaveState state) {}

    @Override
    protected void abortAddState(PlayerSaveState state) {

    }

    @Override
    protected void passAddState(PlayerSaveState state) {
        Ari.instance.getLog().debug("added player {} state to save.", state.getOwner().getName());
    }

    @Override
    protected void onEarlyExit(PlayerSaveState state) {
        Ari.instance.getLog().debug("stop save player {} data", state.getOwner().getName());
        this.savePlayerData(state);
    }

    @Override
    protected void onFinished(PlayerSaveState state) {
        Ari.instance.getLog().debug("start save player data {}.", state.getOwner().getName());
        this.savePlayerData(state);
    }

    @Override
    protected void onServiceAbort(PlayerSaveState state) {
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
    public void savePlayerData(PlayerSaveState state) {

        Player player = (Player) state.getOwner();
        boolean stopping = Bukkit.getServer().isStopping();
        String uuid = player.getUniqueId().toString();

        long nowTime = System.currentTimeMillis();
        long onlineDuration = nowTime - state.getLoginTime();

        CachedRepository<ServerPlayer, Integer> players = Ari.REPOSITORY_MANAGER.players();
        Query<ServerPlayer> query = Query.of(ServerPlayer.class).eq(ServerPlayer::getPlayerUUID, uuid);

        CompletableFuture<Void> save = players.findOne(query).thenCompose(existing -> {
            if (existing.isEmpty()) {
                Ari.instance.getLog().error("Player data not found: {}", uuid);
                return CompletableFuture.<Void>completedFuture(null);
            }
            ServerPlayer serverPlayer = existing.get();
            Patch<ServerPlayer> patch = Patch.of(ServerPlayer.class)
                    .set(ServerPlayer::getTotalOnlineTime, serverPlayer.getTotalOnlineTime() + onlineDuration);
            if (!player.isOnline() || stopping) {
                patch = patch.set(ServerPlayer::getLastLoginOffTime, nowTime);
            }
            return players.update(query, patch).thenApply(count -> (Void) null);
        }).thenAccept(v -> Ari.instance.getLog().debug("Saved player data: {}", player.getName()))
          .exceptionally(ex -> {
              Ari.instance.getLog().error(ex, "Error saving player data for {}", player.getName());
              return null;
          });

        // 关服时同步等待，确保在连接池关闭前落库
        if (stopping) {
            try {
                save.get(10, TimeUnit.SECONDS);
            } catch (Exception e) {
                Ari.instance.getLog().error(e, "waiting for player data save failed");
            }
        }

        save.whenComplete((result, ex) -> {
            if (!stopping && player.isOnline()) {
                Ari.instance.getScheduler().runLater(i -> this.addState(new PlayerSaveState(player, System.currentTimeMillis())), 20L);
            } else {
                Ari.instance.getLog().debug("skip player {} save event.", player.getName());
            }
        });
    }

}
