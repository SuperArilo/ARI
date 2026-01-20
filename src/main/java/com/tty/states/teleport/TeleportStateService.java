package com.tty.states.teleport;

import com.tty.Ari;
import com.tty.dto.state.teleport.PlayerToPlayerState;
import com.tty.enumType.TeleportType;
import com.tty.lib.Lib;
import com.tty.lib.Log;
import com.tty.lib.dto.state.State;
import com.tty.dto.state.CooldownState;
import com.tty.dto.state.teleport.EntityToLocationCallbackState;
import com.tty.dto.state.teleport.EntityToLocationState;
import com.tty.enumType.FilePath;
import com.tty.lib.tool.Teleporting;
import com.tty.lib.services.StateService;
import com.tty.lib.tool.ComponentUtils;
import com.tty.states.CoolDownStateService;
import com.tty.tool.ConfigUtils;
import org.bukkit.Location;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class TeleportStateService extends StateService<State> {

    private final Map<UUID, Double> initHealthMap = new HashMap<>();
    private final Map<UUID, Location> initLocationMap = new HashMap<>();

    public TeleportStateService(long rate, long c, boolean isAsync, JavaPlugin javaPlugin) {
        super(rate, c, isAsync, javaPlugin);
    }

    @Override
    protected void loopExecution(State state) {
        Entity owner = state.getOwner();
        if (!(owner instanceof Player player && player.isOnline())) {
            state.setOver(true);
            return;
        }

        if (state instanceof PlayerToPlayerState playerToPlayerState) {
            Entity target = playerToPlayerState.getTarget();
            if (target instanceof Player targetPlayer && !targetPlayer.isOnline()) {
                ConfigUtils.t("teleport.break", player).thenAccept(owner::sendMessage);
                state.setOver(true);
                return;
            }
        }

        if (this.hasMoved(owner) || this.hasLostHealth(player)) {
            ConfigUtils.t("teleport.break", player).thenAccept(owner::sendMessage);
            state.setOver(true);
            return;
        }

        if (state.isOver() || state.isDone()) return;
        Ari.PLACEHOLDER.render("teleport.title.sub-title", player).thenAccept(result ->
                Lib.Scheduler.runAtEntity(Ari.instance, player, task -> {
                    player.showTitle(ComponentUtils.setPlayerTitle(
                            Ari.C_INSTANCE.getValue("teleport.title.main", FilePath.LANG),
                            result,
                            200,
                            1000,
                            200
                    ));
                    state.setPending(false);
                    Log.debug("checking entity {} teleporting. count {}, max_count {}", owner.getName(), state.getCount(), state.getMax_count());
                }, null));
    }


    @Override
    protected boolean canAddState(State state) {
        Entity owner = state.getOwner();
        if (!(owner instanceof Player player && player.isOnline())) {
            return false;
        }
        //判断当前实体是否在传送冷却中
        if (!Ari.STATE_MACHINE_MANAGER.get(CoolDownStateService.class).getStates(owner).isEmpty()) {
            ConfigUtils.t("teleport.cooling", player).thenAccept(owner::sendMessage);
            return false;
        }

        if(!Ari.STATE_MACHINE_MANAGER.get(TeleportStateService.class).getStates(owner).isEmpty()) {
            ConfigUtils.t("teleport.has-teleport", player).thenAccept(owner::sendMessage);
            return false;
        }

        if (state instanceof EntityToLocationCallbackState callbackState) {
            return callbackState.checkCondition();
        }

        return true;
    }

    @Override
    protected void abortAddState(State state) {

    }

    @Override
    protected void passAddState(State state) {
        Entity owner = state.getOwner();
        this.addEntityInitData(owner);
    }

    @Override
    protected void onEarlyExit(State state) {
        Entity owner = state.getOwner();
        owner.clearTitle();
        Log.debug("entity {} teleport state break.", owner.getName());
        this.removeEntityInitData(owner);
    }

    @Override
    protected void onFinished(State state) {
        Entity owner = state.getOwner();
        owner.clearTitle();
        CoolDownStateService machine = Ari.STATE_MACHINE_MANAGER.get(CoolDownStateService.class);

        Location targetLocation;
        Runnable afterAction;

        switch (state) {
            case PlayerToPlayerState toPlayerState -> {
                targetLocation = toPlayerState.getTarget().getLocation();
                afterAction = () -> handleTeleportAfter(owner, targetLocation,
                        () -> this.removeEntityInitData(owner),
                        () -> machine.addState(new CooldownState(owner, this.getCooldownTime(toPlayerState.getType()), toPlayerState.getType()))
                );
            }
            case EntityToLocationState toLocationState -> {
                targetLocation = toLocationState.getLocation();
                if (targetLocation == null) return;
                afterAction = () -> handleTeleportAfter(owner, targetLocation,
                        () -> this.removeEntityInitData(owner),
                        () -> machine.addState(new CooldownState(owner, this.getCooldownTime(toLocationState.getType()), toLocationState.getType()))
                );
            }
            case EntityToLocationCallbackState callbackState -> {
                targetLocation = callbackState.getLocation();
                afterAction = () -> {
                    callbackState.executeCallback();
                    handleTeleportAfter(owner, targetLocation,
                            () -> this.removeEntityInitData(owner),
                            () -> machine.addState(new CooldownState(owner, this.getCooldownTime(callbackState.getType()), callbackState.getType()))
                    );
                };
            }
            default -> {
                return;
            }
        }
        Teleporting.create(Ari.instance, owner, targetLocation).teleport().after(afterAction);
    }

    @Override
    protected void onServiceAbort(State state) {

    }

    /**
     * 检查是否受伤
     */
    private boolean hasLostHealth(Damageable entity) {
        Double initHealth = this.initHealthMap.get(entity.getUniqueId());
        if (initHealth == null) return false;
        return entity.getHealth() < initHealth;
    }

    /**
     * 检查是否移动
     */
    protected boolean hasMoved(Entity entity) {
        Location initLocation = this.initLocationMap.get(entity.getUniqueId());
        if (initLocation == null) return false;
        return entity.getLocation().distanceSquared(initLocation) > 0.1;
    }

    private int getCooldownTime(TeleportType type) {
        return switch (type) {
            case RTP -> Ari.C_INSTANCE.getValue("main.teleport.cooldown", FilePath.RTP_CONFIG, Integer.class, 10);
            case TPA, TPAHERE -> Ari.C_INSTANCE.getValue("main.teleport.cooldown", FilePath.TPA_CONFIG, Integer.class, 10);
            case BACK -> Ari.C_INSTANCE.getValue("main.teleport.cooldown", FilePath.BACK_CONFIG, Integer.class, 10);
            case WARP -> Ari.C_INSTANCE.getValue("main.teleport.cooldown", FilePath.WARP_CONFIG, Integer.class, 10);
            case HOME -> Ari.C_INSTANCE.getValue("main.teleport.cooldown", FilePath.HOME_CONFIG, Integer.class, 10);
            case SPAWN -> Ari.C_INSTANCE.getValue("main.teleport.cooldown", FilePath.SPAWN_CONFIG, Integer.class, 10);
        };
    }

    private void addEntityInitData(Entity entity) {
        if (!this.initLocationMap.containsKey(entity.getUniqueId()))  {
            this.initLocationMap.put(entity.getUniqueId(), entity.getLocation().clone());
        }
        if (entity instanceof Damageable damageable && !this.initHealthMap.containsKey(entity.getUniqueId())) {
            this.initHealthMap.put(entity.getUniqueId(), damageable.getHealth());
        }
    }

    private void removeEntityInitData(Entity entity) {
        this.initHealthMap.remove(entity.getUniqueId());
        this.initLocationMap.remove(entity.getUniqueId());
    }

    private void handleTeleportAfter(Entity owner, Location location, Runnable removeInit, Runnable addState) {
        removeInit.run();
        addState.run();
        Log.debug("entity {} teleport to x: {}, y: {}, z: {} success.", owner.getName(), location.getX(), location.getY(), location.getZ());
    }

}
