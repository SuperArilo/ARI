package com.tty.ari.states.teleport;

import com.tty.api.ComponentTool;
import com.tty.api.ConfigurationManager;
import com.tty.api.state.State;
import com.tty.api.state.StateService;
import com.tty.ari.Ari;
import com.tty.ari.configuration.FunctionConfig;
import com.tty.ari.configuration.home.HomeConfig;
import com.tty.ari.configuration.lang.LangConfig;
import com.tty.ari.configuration.warp.WarpConfig;
import com.tty.ari.dto.TeleportState;
import com.tty.ari.dto.state.CooldownState;
import com.tty.ari.dto.state.teleport.EntityToLocationCallbackState;
import com.tty.ari.dto.state.teleport.EntityToLocationState;
import com.tty.ari.dto.state.teleport.PlayerToPlayerState;
import com.tty.ari.enumType.TeleportType;
import com.tty.ari.states.CoolDownStateService;
import com.tty.ari.tool.ConfigUtils;
import org.bukkit.Location;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TeleportStateService extends StateService<State> {

    private final Map<UUID, Double> initHealthMap = new HashMap<>();
    private final Map<UUID, Location> initLocationMap = new HashMap<>();

    public TeleportStateService(long rate, long c, boolean isAsync) {
        super(rate, c, isAsync, Ari.instance);
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
                owner.sendMessage(ComponentTool.text(Ari.DATA_SERVICE.getValue("function.teleport.break"), player));
                state.setOver(true);
                return;
            }
        }

        if (this.hasMoved(player) || this.hasLostHealth(player) || player.isInsideVehicle() || !player.getPassengers().isEmpty()) {
            owner.sendMessage(ComponentTool.text(Ari.DATA_SERVICE.getValue("function.teleport.break"), player));
            state.setOver(true);
            return;
        }

        if (state.isOver() || state.isDone()) return;
        ConfigUtils.t("teleport.title.sub-title", player).thenAccept(result ->
                Ari.instance.getScheduler().runAtEntity(player, task -> {
                    player.showTitle(ComponentTool.setPlayerTitle(
                            Ari.instance.getConfigurationManager().get(LangConfig.class).getString("teleport.title.main"),
                            result,
                            Duration.ofMillis(200),
                            Duration.ofMillis(1000),
                            Duration.ofMillis(200)
                    ));
                    Ari.instance.getLog().debug("checking entity {} teleporting. count {}, max_count {}", owner.getName(), state.getCount(), state.getMax_count());
                }, null));
    }


    @Override
    protected boolean canAddState(State state) {
        Entity owner = state.getOwner();
        if (!(owner instanceof Player player && player.isOnline())) return false;

        if (!(state instanceof TeleportState ts) || !Ari.INTERACT_SERVICE.canTeleport(ts.getLocation(), player)) {
            player.sendMessage(ComponentTool.text(Ari.DATA_SERVICE.getValue("function.teleport.no-permission")));
            return false;
        }

        //判断当前实体是否在传送冷却中
        if (!Ari.instance.getStatusManager().get(CoolDownStateService.class).getStates(owner).isEmpty()) {
            ConfigUtils.t("teleport.cooling", player).thenAccept(owner::sendMessage);
            return false;
        }

        if(!Ari.instance.getStatusManager().get(TeleportStateService.class).getStates(owner).isEmpty()) {
            Ari.instance.getScheduler().runAtEntity(owner, i -> player.sendMessage(ComponentTool.text(Ari.DATA_SERVICE.getValue("function.teleport.has-teleport"), player)), null);
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
        Ari.instance.getLog().debug("entity {} teleport state break.", owner.getName());
        this.removeEntityInitData(owner);
    }

    @Override
    protected void onFinished(State state) {
        Entity owner = state.getOwner();
        owner.clearTitle();
        CoolDownStateService machine = Ari.instance.getStatusManager().get(CoolDownStateService.class);

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
        Ari.TELEPORTING_SERVICE.teleport(owner, owner.getLocation(), targetLocation).after(afterAction);
    }

    @Override
    protected void onServiceAbort(State state) {

    }

    @Override
    public void onReload() {
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
        ConfigurationManager manager = Ari.instance.getConfigurationManager();
        FunctionConfig functionConfig = manager.get(FunctionConfig.class);
        return switch (type) {
            case RTP, TPA, TPAHERE, BACK, SPAWN -> functionConfig.getTeleportCooldown(type);
            case WARP -> manager.get(WarpConfig.class).getCooldown();
            case HOME -> manager.get(HomeConfig.class).getCooldown();
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
        Ari.instance.getLog().debug("entity {} teleport to x: {}, y: {}, z: {} success.", owner.getName(), location.getX(), location.getY(), location.getZ());
    }

}
