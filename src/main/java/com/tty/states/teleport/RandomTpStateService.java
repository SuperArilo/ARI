package com.tty.states.teleport;

import com.google.gson.reflect.TypeToken;
import com.tty.Ari;
import com.tty.dto.rtp.RtpConfig;
import com.tty.lib.Lib;
import com.tty.lib.Log;
import com.tty.dto.state.teleport.EntityToLocationState;
import com.tty.dto.state.teleport.RandomTpState;
import com.tty.enumType.FilePath;
import com.tty.lib.enum_type.TeleportType;
import com.tty.lib.enum_type.LangType;
import com.tty.lib.services.StateService;
import com.tty.lib.tool.ComponentUtils;
import com.tty.lib.tool.PublicFunctionUtils;
import com.tty.lib.tool.SearchSafeLocation;
import com.tty.states.CoolDownStateService;
import com.tty.tool.ConfigUtils;
import com.tty.tool.StateMachineManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

public class RandomTpStateService extends StateService<RandomTpState> {

    private final SearchSafeLocation searchSafeLocation = new SearchSafeLocation(Ari.instance);

    public RandomTpStateService(long rate, long c, boolean isAsync, JavaPlugin javaPlugin) {
        super(rate, c, isAsync, javaPlugin);
    }

    @Override
    protected boolean canAddState(RandomTpState state) {
        Entity owner = state.getOwner();
        RtpConfig rtpConfig = this.rtpConfig(state.getWorld().getName());
        if (rtpConfig == null || !rtpConfig.isEnable()) {
            state.getOwner().sendMessage(ConfigUtils.t("function.rtp.world-disable"));
            return false;
        }
        StateMachineManager manager = Ari.instance.stateMachineManager;

        //判断当前实体是否在传送冷却中
        if (!manager.get(CoolDownStateService.class).getStates(owner).isEmpty()) {
            owner.sendMessage(ConfigUtils.t("teleport.cooling"));
            return false;
        }

        //判断当前发起玩家是否在传送状态中
        if (!manager.get(TeleportStateService.class).getStates(owner).isEmpty() ||
                !this.getStates(owner).isEmpty() ||
                !manager.get(PreTeleportStateService.class).getStates(owner).isEmpty()) {
            owner.sendMessage(ConfigUtils.t("teleport.has-teleport"));
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
            owner.sendMessage(ConfigUtils.t("teleport.break"));
            state.setOver(true);
            return;
        }

        this.sendCountTitle(owner, state);
        this.search(state);
    }

    private void search(RandomTpState state) {
        World world = state.getWorld();
        RtpConfig rtpConfig = this.rtpConfig(world.getName());

        int x = (int) Math.min(PublicFunctionUtils.randomGenerator((int) rtpConfig.getMin(), (int) rtpConfig.getMax()), world.getWorldBorder().getMaxSize());
        int z = (int) Math.min(PublicFunctionUtils.randomGenerator((int) rtpConfig.getMin(), (int) rtpConfig.getMax()), world.getWorldBorder().getMaxSize());
        Log.debug("player %s search count %s. total %s.", state.getOwner().getName(), state.getCount(), state.getMax_count());
        synchronized (state) {
            if (state.getTrueLocation() != null || state.isRunning() || state.isOver()) return;
            state.setRunning(true);
        }
        this.searchSafeLocation.search(world, x, z)
            .thenAccept((location) ->
                Lib.Scheduler.run(Ari.instance, i -> {
                    state.setPending(false);
                    state.setRunning(false);
                    if (location == null) return;
                    state.setTrueLocation(location);
                    state.setOver(true);
                }))
            .exceptionally(e -> {
                Log.error(e);
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
        Entity owner = state.getOwner();
        owner.clearTitle();
        owner.sendMessage(ConfigUtils.t("function.rtp.location-found"));

        Ari.instance.stateMachineManager
            .get(TeleportStateService.class)
                .addState(new EntityToLocationState(
                    owner,
                    Ari.C_INSTANCE.getValue("main.teleport.delay", FilePath.RTP_CONFIG, Integer.class, 3),
                    state.getTrueLocation(),
                    TeleportType.RTP));

    }

    @Override
    protected void onFinished(RandomTpState state) {
        Entity owner = state.getOwner();
        owner.clearTitle();
        owner.sendMessage(ConfigUtils.t("function.rtp.search-failure"));
    }

    @Override
    protected void onServiceAbort(RandomTpState state) {

    }

    private void sendCountTitle(Player player, RandomTpState state) {
        String sub = Ari.C_INSTANCE.getValue(
                "function.rtp.title-search-count",
                FilePath.LANG,
                String.class,
                "null");
        Title title = ComponentUtils.setPlayerTitle(
                Ari.C_INSTANCE.getValue("function.rtp.title-searching", FilePath.LANG, String.class, "null"),
                sub,
                Map.of(LangType.RTP_SEARCH_COUNT.getType(), Component.text(state.getMax_count() - state.getCount())),
                0,
                1000L,
                1000L);
        if (player.isOnline()) player.showTitle(title);
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
        Ari.C_INSTANCE.setValue(Ari.instance,"main.worlds", FilePath.RTP_CONFIG, value);
    }
}
