package com.tty.ari.states.teleport;

import com.tty.api.ComponentTool;
import com.tty.api.StatusManager;
import com.tty.api.state.StateService;
import com.tty.api.utils.PublicFunctionUtils;
import com.tty.api.utils.SearchSafeLocation;
import com.tty.ari.Ari;
import com.tty.ari.configuration.FunctionConfig;
import com.tty.ari.configuration.lang.LangConfig;
import com.tty.ari.dto.rtp.RtpConfig;
import com.tty.ari.dto.state.teleport.EntityToLocationState;
import com.tty.ari.dto.state.teleport.RandomTpState;
import com.tty.ari.enumType.TeleportType;
import com.tty.ari.states.CoolDownStateService;
import com.tty.ari.tool.ConfigUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

public class RandomTpStateService extends StateService<RandomTpState> {

    private final SearchSafeLocation searchSafeLocation;
    private Map<String, RtpConfig> rtpConfigMap;

    private final Object rtpConfigMapLock = new Object();

    public RandomTpStateService(long rate, long c, boolean isAsync) {
        super(rate, c, isAsync, Ari.instance);
        this.searchSafeLocation = new SearchSafeLocation(Ari.instance, Ari.INTERACT_SERVICE, 5);
        this.rtpConfigMap = this.getRtpConfigWorlds();
    }

    @Override
    protected boolean canAddState(RandomTpState state) {
        Player owner = (Player) state.getOwner();

        synchronized (this.rtpConfigMapLock) {
            RtpConfig rtpConfig = this.rtpConfigMap.get(state.getWorld().getName());
            if (rtpConfig == null || !rtpConfig.isEnable()) {
                ConfigUtils.t("function.rtp.world-disable", owner).thenAccept(owner::sendMessage);
                return false;
            }
        }

        StatusManager manager = Ari.instance.getStatusManager();

        //判断当前实体是否在传送冷却中
        if (!manager.get(CoolDownStateService.class).getStates(owner).isEmpty()) {
            ConfigUtils.t("teleport.cooling", owner).thenAccept(owner::sendMessage);
            return false;
        }

        //判断当前发起玩家是否在传送状态中
        if (!manager.get(TeleportStateService.class).getStates(owner).isEmpty() ||
                !this.getStates(owner).isEmpty() ||
                !manager.get(PreTeleportStateService.class).getStates(owner).isEmpty()) {
            Ari.instance.getScheduler().runAtEntity(owner, i -> owner.sendMessage(ComponentTool.text(Ari.DATA_SERVICE.getValue("function.teleport.has-teleport"), owner)), null);
            return false;
        }

        return true;
    }

    @Override
    protected void loopExecution(RandomTpState state) {
        Player owner = (Player) state.getOwner();
        if (!owner.isOnline()
                || owner.isSleeping()
                || owner.isDeeplySleeping()
                || owner.isFlying()
                || owner.isGliding()
                || owner.isInsideVehicle()) {
            Ari.instance.getScheduler().runAtEntity(
                    owner,
                    i -> owner.sendMessage(ComponentTool.text(Ari.DATA_SERVICE.getValue("function.teleport.break"),owner)),
                    null);
            state.setOver(true);
            return;
        }
        this.sendCountTitle(owner);
        this.search(state);
    }

    @Override
    protected void abortAddState(RandomTpState state) {

    }

    @Override
    protected void passAddState(RandomTpState state) {

    }

    @Override
    protected void onEarlyExit(RandomTpState state) {
        if (state.getTrueLocation() == null) return;
        Player owner = (Player) state.getOwner();
        owner.clearTitle();
        ConfigUtils.t("function.rtp.location-found", owner).thenAccept(owner::sendMessage);

        Ari.instance.getStatusManager().get(TeleportStateService.class).addState(
                new EntityToLocationState(
                    owner,
                    Ari.instance.getConfigurationManager().get(FunctionConfig.class).getTeleportDelay(TeleportType.RTP),
                    state.getTrueLocation(),
                    TeleportType.RTP)
        );

    }

    @Override
    protected void onFinished(RandomTpState state) {
        Ari.instance.getLog().debug("onFinished");
        Player owner = (Player) state.getOwner();
        ConfigUtils.t("function.rtp.search-failure", owner).thenAccept(owner::sendMessage);
    }

    @Override
    protected void onServiceAbort(RandomTpState state) {

    }

    @Override
    public void onReload() {
        synchronized (this.rtpConfigMapLock) {
            this.rtpConfigMap = this.getRtpConfigWorlds();
        }
    }

    private void search(RandomTpState state) {
        Entity owner = state.getOwner();

        Ari.instance.getLog().debug("player {} search count {}. total {}.", owner.getName(), state.getCount(), state.getMax_count());

        synchronized (state) {
            if (state.getTrueLocation() != null || state.isRunning() || state.isOver()) return;
            state.setRunning(true);
        }

        World world = state.getWorld();

        RtpConfig rtpConfig;
        synchronized (this.rtpConfigMapLock) {
            rtpConfig = this.rtpConfigMap.get(world.getName());
        }

        int[] randomXZ = this.calculateRandomXZ(world, rtpConfig.getMin(), rtpConfig.getMax());
        if (randomXZ == null) {
            state.setOver(true);
            Ari.instance.getLog().warn("world {} not have border.", world.getName());
            return;
        }

        this.searchSafeLocation.search(world, randomXZ[0], randomXZ[1])
            .thenAcceptAsync(location -> {
                state.setRunning(false);
                if (location == null) return;
                state.setTrueLocation(location);
                state.setOver(true);
            }, Ari.instance.getExecutorSync())
            .exceptionallyAsync(e -> {
                if (e.getCause() instanceof TimeoutException && owner instanceof Player player) {
                    state.setRunning(false);
                    ConfigUtils.t("function.rtp.abort-search", player).thenAccept(player::sendMessage);
                } else {
                    state.setOver(true);
                    if (owner instanceof Player player) {
                        player.sendMessage(ComponentTool.text(Ari.DATA_SERVICE.getValue("base.on-error"), player));
                    }
                    Ari.instance.getLog().error(e, "running rtp error on entity {}.", owner.getName());
                }
                return null;
            }, Ari.instance.getExecutorSync());
    }

    private int[] calculateRandomXZ(World world, int minRadius, int maxRadius) {

        WorldBorder border = world.getWorldBorder();
        double centerX = border.getCenter().getX();
        double centerZ = border.getCenter().getZ();
        double size = border.getSize();

        double borderMinX = centerX - size / 2;
        double borderMaxX = centerX + size / 2;
        double borderMinZ = centerZ - size / 2;
        double borderMaxZ = centerZ + size / 2;

        int absX = PublicFunctionUtils.randomGenerator(minRadius, maxRadius);
        int absZ = PublicFunctionUtils.randomGenerator(minRadius, maxRadius);
        int x = PublicFunctionUtils.randomGenerator(0, 1) == 0 ? absX : -absX;
        int z = PublicFunctionUtils.randomGenerator(0, 1) == 0 ? absZ : -absZ;

        if (x >= borderMinX && x <= borderMaxX && z >= borderMinZ && z <= borderMaxZ) {
            return new int[]{x, z};
        }

        return null;

    }

    private void sendCountTitle(Player player) {
        if (!player.isOnline()) return;

        ConfigUtils.t("function.rtp.title-search-count", player).thenAccept(result ->
                Ari.instance.getScheduler().runAtEntity(player, task -> player.showTitle(ComponentTool.setPlayerTitle(
                        Ari.instance.getConfigurationManager().get(LangConfig.class).getString("function.rtp.title-searching"),
                        result,
                        Duration.ZERO,
                        Duration.ofMillis(1000),
                        Duration.ofMillis(1000)
                )), null));
    }

    private Map<String, RtpConfig> getRtpConfigWorlds() {
        FunctionConfig config = Ari.instance.getConfigurationManager().get(FunctionConfig.class);

        Map<String, RtpConfig> value = config.getRtpWorlds();

        if (value == null) {
            value = new HashMap<>();
            for (World world : Bukkit.getWorlds()) {
                value.put(world.getName(), new RtpConfig());
            }
        } else {
            for (World world : Bukkit.getWorlds()) {
                if (value.containsKey(world.getName())) continue;
                value.put(world.getName(), new RtpConfig());
            }
        }
        config.setValue("rtp.worlds", value);
        return value;
    }

}
