package com.tty.ari.states;

import com.destroystokyo.paper.event.player.PlayerPickupExperienceEvent;
import com.tty.api.NbtManager;
import com.tty.api.state.State;
import com.tty.api.state.StateService;
import com.tty.ari.Ari;
import com.tty.ari.configuration.FunctionConfig;
import com.tty.ari.enumType.PlayerNbt;
import com.tty.ari.tool.ConfigUtils;
import com.tty.ari.tool.PlayerCache;
import fr.skytasul.glowingentities.GlowingEntities;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.player.*;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Collection;
import java.util.List;

public class PlayerVanishService extends StateService<State> implements Listener {

    private final GlowingEntities glowing;

    public PlayerVanishService(long rate, long c, boolean isAsync) {
        super(rate, c, isAsync, Ari.instance);
        Ari.instance.getServer().getPluginManager().registerEvents(this, Ari.instance);
        this.glowing = new GlowingEntities(Ari.instance);
    }

    @Override
    protected boolean canAddState(State state) {
        return this.isNotHaveState(state.getOwner());
    }

    @Override
    protected void loopExecution(State state) {
        if (!(state.getOwner() instanceof Player player) || !player.isOnline()) {
            state.setOver(true);
            return;
        }
        GameMode gameMode = player.getGameMode();
        if (gameMode.equals(GameMode.ADVENTURE) || gameMode.equals(GameMode.SURVIVAL)) {
            player.setAllowFlight(true);
        }
    }

    @Override
    protected void abortAddState(State state) {

    }

    @Override
    protected void passAddState(State state) {
        if (!(state.getOwner() instanceof Player player)) return;
        this.hide(player);
        this.giveEffect(player);
        ConfigUtils.t("function.vanish.enable").thenAccept(player::sendMessage);
        Ari.instance.getLog().debug("player {} is vanish.", player.getName());
    }

    @Override
    protected void onEarlyExit(State state) {
        if (!(state.getOwner() instanceof Player player)) return;
        this.show(player);
        this.removeEffect(player);
        if (player.isOnline()) {
            ConfigUtils.t("function.vanish.disable").thenAccept(player::sendMessage);
        }
        Ari.instance.getLog().debug("player {} is show up.", player.getName());
    }

    @Override
    protected void onFinished(State state) {
        if (!(state.getOwner() instanceof Player player)) return;
        this.show(player);
        this.removeEffect(player);
        if (player.isOnline()) {
            ConfigUtils.t("function.vanish.disable").thenAccept(player::sendMessage);
        }
        Ari.instance.getLog().debug("player {} is show up.", player.getName());
    }

    @Override
    protected void onServiceAbort(State state) {
        this.glowing.disable();
        if (!(state.getOwner() instanceof Player player)) return;
        this.removeEffect(player);
    }

    @Override
    public void onReload() {

    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.isCancelled() || !(event.getEntity() instanceof Player player)) return;
        if (this.isNotHaveState(player)) return;
        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onJoin(PlayerJoinEvent event) {
        Player joinPlayer = event.getPlayer();
        NbtManager nbtManager = Ari.instance.getNbtManager();
        if (nbtManager.hasNbt(PlayerNbt.VANISH, joinPlayer)) {
            nbtManager.removeNbt(PlayerNbt.VANISH, joinPlayer);
            this.removeEffect(joinPlayer);
        }
        if (this.getAllStates().isEmpty()) return;
        for (State state : this.getAllStates()) {
            if (!(state.getOwner() instanceof Player player)) continue;
            this.hideForPlayer(player, joinPlayer);
        }
    }
    
    @EventHandler
    public void onPotionEffectRemove(EntityPotionEffectEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (this.isNotHaveState(player)) return;
        if (!event.getModifiedType().equals(PotionEffectType.NIGHT_VISION)) return;
        EntityPotionEffectEvent.Action action = event.getAction();
        if (action != EntityPotionEffectEvent.Action.REMOVED && action != EntityPotionEffectEvent.Action.CLEARED) return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onTarget(EntityTargetLivingEntityEvent event) {
        if (event.getTarget() instanceof Player player && !this.isNotHaveState(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;
        if (this.isNotHaveState(player)) return;
        if (event.getEntity() instanceof Mob mob) {
            Ari.instance.getScheduler().runAtEntityLater(mob, i -> {
                if (mob.isValid() && mob.getTarget() == player) {
                    mob.setTarget(null);
                }
            }, null, 20L);
        } else if (event.getEntity() instanceof Player p) {
            Ari.ATTACK_SERVICE.cancelPvpTag(p);
            Ari.ATTACK_SERVICE.cancelPvpTag(player);
        }
    }

    @EventHandler
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        Player player = event.getPlayer();
        if (!this.isNotHaveState(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBucketFill(PlayerBucketFillEvent event) {
        Player player = event.getPlayer();
        if (!this.isNotHaveState(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBedEnter(PlayerBedEnterEvent event) {
        Player player = event.getPlayer();
        if (!this.isNotHaveState(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        if (!this.isNotHaveState(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if (!this.isNotHaveState(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPickItem(PlayerAttemptPickupItemEvent event) {
        Player player = event.getPlayer();
        if (!this.isNotHaveState(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPickupExperience(PlayerPickupExperienceEvent event) {
        Player player = event.getPlayer();
        if (!this.isNotHaveState(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInputCommand(PlayerCommandPreprocessEvent event) {
        String message = event.getMessage();
        if (message.startsWith("/kick") || message.startsWith("/minecraft:kick")) {
            String[] args = message.split(" ");
            if (args.length < 2) return;
            OfflinePlayer offlinePlayer = PlayerCache.getPlayer(args[1]);
            if (!(offlinePlayer instanceof Player player) || Ari.instance.getStatusManager().get(PlayerVanishService.class).getStates(player).isEmpty()) return;
            event.setMessage("/kick null-" + Ari.instance.getName());
        } else if (message.startsWith("/ban") || message.startsWith("/minecraft:ban") || message.startsWith("/tp") || message.startsWith("/minecraft:tp")) {
            String[] args = message.split(" ");
            if (args.length < 2) return;
            OfflinePlayer offlinePlayer = PlayerCache.getPlayer(args[1]);
            if (!(offlinePlayer instanceof Player player) || Ari.instance.getStatusManager().get(PlayerVanishService.class).getStates(player).isEmpty()) return;
            event.getPlayer().sendMessage(Ari.instance.getEngine().directRender(Ari.DATA_SERVICE.getValue("base.on-player.not-exist"), player));
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerKick(PlayerKickEvent event) {
        Player player = event.getPlayer();
        if (this.getStates(player).isEmpty()) return;
        PlayerKickEvent.Cause cause = event.getCause();
        if (cause.equals(PlayerKickEvent.Cause.BANNED) ||
                cause.equals(PlayerKickEvent.Cause.WHITELIST) ||
                cause.equals(PlayerKickEvent.Cause.KICK_COMMAND) ||
                cause.equals(PlayerKickEvent.Cause.IP_BANNED)) {
            event.setCancelled(true);
        }
    }

    private void hide(Player player) {
        for (Player onlinePlayer : Bukkit.getServer().getOnlinePlayers()) {
            if (player.equals(onlinePlayer)) continue;
            this.hideForPlayer(player, onlinePlayer);
        }
    }

    private void show(Player player) {
        for (Player onlinePlayer : Bukkit.getServer().getOnlinePlayers()) {
            if (onlinePlayer.equals(player)) continue;
            this.showForPlayer(player, onlinePlayer);
        }
    }

    private void hideForPlayer(Player vanishPlayer, Player player) {
        player.hidePlayer(Ari.instance, vanishPlayer);
        try {
            this.glowing.unsetGlowing(player, vanishPlayer);
            this.glowing.setGlowing(player, vanishPlayer, ChatColor.WHITE);
        } catch (ReflectiveOperationException e) {
            Ari.instance.getLog().error(e);
        }
    }

    private void showForPlayer(Player vanishPlayer, Player player) {
        player.showPlayer(Ari.instance, vanishPlayer);
        try {
            this.glowing.unsetGlowing(player, vanishPlayer);
        } catch (ReflectiveOperationException e) {
            Ari.instance.getLog().error(e);
        }
    }

    private void giveEffect(Player player) {
        PotionEffect nightVersion = new PotionEffect(
                PotionEffectType.NIGHT_VISION,
                PotionEffect.INFINITE_DURATION,
                0,
                false,
                false
        );
        PotionEffect invisibility = new PotionEffect(
                PotionEffectType.INVISIBILITY,
                PotionEffect.INFINITE_DURATION,
                0,
                false,
                false
        );
        player.addPotionEffect(nightVersion);
        player.addPotionEffect(invisibility);
        player.setAllowFlight(true);
        player.setFlying(true);
        Ari.instance.getNbtManager().setNbt(PlayerNbt.VANISH, player, PersistentDataType.BOOLEAN, true);
        Collection<Entity> nearby = player.getNearbyEntities(24, 24, 24);
        for (Entity entity : nearby) {
            if (entity instanceof Mob mob && mob.getTarget() == player) {
                mob.setTarget(null);
            }
        }
    }

    private void removeEffect(Player player) {
        player.removePotionEffect(PotionEffectType.NIGHT_VISION);
        player.removePotionEffect(PotionEffectType.INVISIBILITY);
        List<String> nodes = Ari.instance.getConfigurationManager().get(FunctionConfig.class).getVanishFlyPermissionNodes();
        boolean hasPerm = false;
        for (String node : nodes) {
            if (Ari.PERMISSION_SERVICE.hasPermission(player, node) && !player.isOp()) {
                hasPerm = true;
                break;
            }
        }

        boolean keepFlight = player.getGameMode() == GameMode.CREATIVE
                || player.getGameMode() == GameMode.SPECTATOR
                || hasPerm;

        if (keepFlight) {
            player.setAllowFlight(true);
        } else {
            player.setFlying(false);
            player.setAllowFlight(false);
        }
    }

}
