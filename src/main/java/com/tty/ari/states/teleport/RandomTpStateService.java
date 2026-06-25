package com.tty.ari.states.teleport;

import com.google.gson.reflect.TypeToken;
import com.tty.ari.Ari;
import com.tty.ari.dto.rtp.RtpConfig;
import com.tty.ari.dto.state.teleport.EntityToLocationState;
import com.tty.ari.dto.state.teleport.RandomTpState;
import com.tty.ari.enumType.FilePath;
import com.tty.ari.enumType.LangFile;
import com.tty.ari.enumType.TeleportType;
import com.tty.api.state.StateService;
import com.tty.api.utils.PublicFunctionUtils;
import com.tty.api.utils.SearchSafeLocation;
import com.tty.ari.states.CoolDownStateService;
import com.tty.ari.tool.ConfigUtils;
import com.tty.ari.tool.StateMachineManager;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.io.IOException;
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

        StateMachineManager manager = Ari.STATE_MACHINE_MANAGER;

        //判断当前实体是否在传送冷却中
        if (!manager.get(CoolDownStateService.class).getStates(owner).isEmpty()) {
            ConfigUtils.t("teleport.cooling", owner).thenAccept(owner::sendMessage);
            return false;
        }

        //判断当前发起玩家是否在传送状态中
        if (!manager.get(TeleportStateService.class).getStates(owner).isEmpty() ||
                !this.getStates(owner).isEmpty() ||
                !manager.get(PreTeleportStateService.class).getStates(owner).isEmpty()) {
            Ari.instance.getScheduler().runAtEntity(Ari.instance, owner, i -> owner.sendMessage(Ari.instance.getComponentTool().text(Ari.DATA_SERVICE.getValue("function.teleport.has-teleport"), owner)), null);
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
                    Ari.instance,
                    owner,
                    i -> owner.sendMessage(Ari.instance.getComponentTool().text(Ari.DATA_SERVICE.getValue("function.teleport.break"),owner)),
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

        Ari.STATE_MACHINE_MANAGER
            .get(TeleportStateService.class)
                .addState(new EntityToLocationState(
                    owner,
                    Ari.instance.getConfigInstance().getValue("rtp.teleport.delay", FilePath.FUNCTION_CONFIG, Integer.class, 3),
                    state.getTrueLocation(),
                    TeleportType.RTP));

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
                state.setPending(false);
                state.setRunning(false);
                if (location == null) return;
                state.setTrueLocation(location);
                state.setOver(true);
            }, Ari.instance.getExecutorSync())
            .exceptionallyAsync(e -> {
                if (e.getCause() instanceof TimeoutException && owner instanceof Player player) {
                    state.setRunning(false);
                    state.setPending(false);
                    Ari.PLACEHOLDER.render("function.rtp.abort-search", player).thenAccept(player::sendMessage);
                } else {
                    state.setOver(true);
                    if (owner instanceof Player player) {
                        player.sendMessage(Ari.instance.getComponentTool().text(Ari.DATA_SERVICE.getValue("base.on-error"), player));
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
        Ari.PLACEHOLDER.render("function.rtp.title-search-count", player).thenAccept(result ->
                Ari.instance.getScheduler().runAtEntity(Ari.instance, player, task -> player.showTitle(Ari.instance.getComponentTool().setPlayerTitle(
                        Ari.instance.getConfigInstance().getValue("function.rtp.title-searching", LangFile.LANG, String.class, "null"),
                        result,
                        0,
                        1000L,
                        1000L
                )), null));
    }

    private Map<String, RtpConfig> getRtpConfigWorlds() {

        Map<String, RtpConfig> value = Ari.instance.getConfigInstance().getValue(
                "rtp.worlds",
                FilePath.FUNCTION_CONFIG,
                new TypeToken<Map<String, RtpConfig>>(){}.getType(),
                null);

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
        try {
            Ari.instance.getConfigInstance().setValue("rtp.worlds", FilePath.FUNCTION_CONFIG, value);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return value;
    }

}
