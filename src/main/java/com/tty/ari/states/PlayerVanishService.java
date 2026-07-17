package com.tty.ari.states;

import com.tty.api.NbtManager;
import com.tty.api.state.State;
import com.tty.api.state.StateService;
import com.tty.ari.Ari;
import com.tty.ari.enumType.PlayerNbt;
import com.tty.ari.tool.ConfigUtils;
import fr.skytasul.glowingentities.GlowingEntities;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

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

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent event) {
        Player joinPlayer = event.getPlayer();
        NbtManager nbtManager = Ari.instance.getNbtManager();
        if (nbtManager.hasNbt(PlayerNbt.VANISH, joinPlayer)) {
            nbtManager.removeNbt(PlayerNbt.VANISH, joinPlayer);
            this.removeEffect(joinPlayer);
        }
        if (!this.getStates(joinPlayer).isEmpty()) return;
        for (State state : this.getAllStates()) {
            if (!(state.getOwner() instanceof Player player)) continue;
            this.hideForPlayer(player, joinPlayer);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onLeave(PlayerQuitEvent event) {
        Player quitPlayer = event.getPlayer();
        if (!this.getStates(quitPlayer).isEmpty()) return;
        for (State state : this.getAllStates()) {
            if (!(state.getOwner() instanceof Player player)) continue;
            this.showForPlayer(player, quitPlayer);
        }
    }

    private void hide(Player player) {
        Bukkit.getServer().getOnlinePlayers().stream().filter(i -> !i.equals(player)).forEach(p -> this.hideForPlayer(player, p));
    }

    private void show(Player player) {
        Bukkit.getServer().getOnlinePlayers().stream().filter(i -> !i.equals(player)).forEach(p -> this.showForPlayer(player, p));
    }

    private void hideForPlayer(Player vanishPlayer, Player player) {
        player.hidePlayer(Ari.instance, vanishPlayer);
        try {
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
        PotionEffect effect = new PotionEffect(
                PotionEffectType.NIGHT_VISION,
                PotionEffect.INFINITE_DURATION,
                0,
                false,
                false
        );
        player.addPotionEffect(effect);
        player.setAllowFlight(true);
        player.setFlying(true);
        Ari.instance.getNbtManager().setNbt(PlayerNbt.VANISH, player, PersistentDataType.BOOLEAN, true);
    }

    private void removeEffect(Player player) {
        player.removePotionEffect(PotionEffectType.NIGHT_VISION);
        player.setFlying(false);
        player.setAllowFlight(false);
    }

}
