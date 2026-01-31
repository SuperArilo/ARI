package com.tty.states.teleport;

import com.google.gson.reflect.TypeToken;
import com.tty.Ari;
import com.tty.dto.rtp.RtpConfig;
import com.tty.dto.state.teleport.EntityToLocationState;
import com.tty.dto.state.teleport.RandomTpState;
import com.tty.enumType.FilePath;
import com.tty.enumType.TeleportType;
import com.tty.api.state.StateService;
import com.tty.api.PublicFunctionUtils;
import com.tty.api.SearchSafeLocation;
import com.tty.states.CoolDownStateService;
import com.tty.tool.ConfigUtils;
import com.tty.tool.StateMachineManager;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class RandomTpStateService extends StateService<RandomTpState> {

    private final SearchSafeLocation searchSafeLocation = new SearchSafeLocation(Ari.instance, Ari.SCHEDULER);

    public RandomTpStateService(long rate, long c, boolean isAsync) {
        super(rate, c, isAsync, Ari.instance, Ari.SCHEDULER);
    }

    @Override
    protected boolean canAddState(RandomTpState state) {
        Player owner = (Player) state.getOwner();
        RtpConfig rtpConfig = this.rtpConfig(state.getWorld().getName());
        if (rtpConfig == null || !rtpConfig.isEnable()) {
            ConfigUtils.t("function.rtp.world-disable", owner).thenAccept(owner::sendMessage);
            return false;
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
            ConfigUtils.t("teleport.has-teleport", owner).thenAccept(owner::sendMessage);
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
            ConfigUtils.t("teleport.break", owner).thenAccept(owner::sendMessage);
            state.setOver(true);
            return;
        }

        this.sendCountTitle(owner);
        this.search(state);
    }

    private void search(RandomTpState state) {
        World world = state.getWorld();
        RtpConfig rtpConfig = this.rtpConfig(world.getName());

        int x = (int) Math.min(PublicFunctionUtils.randomGenerator((int) rtpConfig.getMin(), (int) rtpConfig.getMax()), world.getWorldBorder().getMaxSize());
        int z = (int) Math.min(PublicFunctionUtils.randomGenerator((int) rtpConfig.getMin(), (int) rtpConfig.getMax()), world.getWorldBorder().getMaxSize());
        this.getLog().debug("player {} search count {}. total {}.", state.getOwner().getName(), state.getCount(), state.getMax_count());
        synchronized (state) {
            if (state.getTrueLocation() != null || state.isRunning() || state.isOver()) return;
            state.setRunning(true);
        }
        this.searchSafeLocation.search(world, x, z)
            .thenAccept((location) ->
                Ari.SCHEDULER.run(Ari.instance, i -> {
                    state.setPending(false);
                    state.setRunning(false);
                    if (location == null) return;
                    this.getLog().debug("random location x: {}, y: {}, z: {}.", x, location.getY(), z);
                    state.setTrueLocation(location);
                    state.setOver(true);
                }))
            .exceptionally(e -> {
                this.getLog().error(e);
                return null;
            });
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
                    Ari.C_INSTANCE.getValue("main.teleport.delay", FilePath.RTP_CONFIG, Integer.class, 3),
                    state.getTrueLocation(),
                    TeleportType.RTP));

    }

    @Override
    protected void onFinished(RandomTpState state) {
        Player owner = (Player) state.getOwner();
        owner.clearTitle();
        ConfigUtils.t("function.rtp.search-failure", owner).thenAccept(owner::sendMessage);
    }

    @Override
    protected void onServiceAbort(RandomTpState state) {

    }

    private void sendCountTitle(Player player) {
        if (!player.isOnline()) return;
        Ari.PLACEHOLDER.render("function.rtp.title-search-count", player).thenAccept(result ->
                Ari.SCHEDULER.runAtEntity(Ari.instance, player, task -> player.showTitle(Ari.COMPONENT_SERVICE.setPlayerTitle(
                        Ari.C_INSTANCE.getValue("function.rtp.title-searching", FilePath.LANG, String.class, "null"),
                        result,
                        0,
                        1000L,
                        1000L
                )), null));
    }

    private RtpConfig rtpConfig(String worldName) {
        Map<String, RtpConfig> value = Ari.C_INSTANCE.getValue("main.worlds", FilePath.RTP_CONFIG, new TypeToken<Map<String, RtpConfig>>() {}.getType(), null);
        return value.get(worldName);
    }

    private static Map<String, Object> createWorldRtp() {
        Map<String, Object> map = new HashMap<>();
        map.put("enable", true);
        map.put("min", 300);
        map.put("max", 1500);
        return map;
    }

    public static void setRtpWorldConfig() {

        Map<String, Object> value = Ari.C_INSTANCE.getValue(
                "main.worlds",
                FilePath.RTP_CONFIG,
                new TypeToken<Map<String, Object>>(){}.getType(),
                null);

        if (value == null) {
            value = new HashMap<>();
            for (World world : Bukkit.getWorlds()) {
                value.put(world.getName(), createWorldRtp());
            }
        } else {
            for (World world : Bukkit.getWorlds()) {
                if (value.containsKey(world.getName())) continue;
                value.put(world.getName(), createWorldRtp());
            }
        }
        try {
            Ari.C_INSTANCE.setValue(Ari.instance,"main.worlds", FilePath.RTP_CONFIG, value);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
