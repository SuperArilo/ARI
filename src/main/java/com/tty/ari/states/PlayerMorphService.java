package com.tty.ari.states;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.*;
import com.google.common.reflect.TypeToken;
import com.tty.api.state.StateService;
import com.tty.ari.Ari;
import com.tty.ari.dto.event.CustomPlayerRespawnEvent;
import com.tty.ari.dto.state.player.PlayerMorphState;
import com.tty.ari.listener.DamageTrackerListener;
import com.tty.ari.tool.ConfigUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerMorphService extends StateService<PlayerMorphState> implements Listener {

    private final Map<Integer, PlayerMorphState> Entity_ID_TO_State = new ConcurrentHashMap<>();

    public PlayerMorphService(long rate, long c, boolean isAsync) {
        super(rate, c, isAsync, Ari.instance);
        if (Ari.PROTOCOL_MANAGER == null) return;
        Bukkit.getPluginManager().registerEvents(this, Ari.instance);
        Ari.PROTOCOL_MANAGER.addPacketListener(new PacketAdapter(
                Ari.instance,
                ListenerPriority.NORMAL,
                PacketType.Play.Server.ENTITY_METADATA,
                PacketType.Play.Server.ENTITY_EQUIPMENT,
                PacketType.Play.Server.SPAWN_ENTITY) {
            @Override
            public void onPacketSending(PacketEvent event) {
                Player viewer = event.getPlayer();
                PacketContainer packet = event.getPacket();
                PacketType type = packet.getType();

                if (type == PacketType.Play.Server.ENTITY_METADATA) {
                    int entityId = packet.getIntegers().read(0);
                    PlayerMorphState state = Entity_ID_TO_State.get(entityId);
                    if (state != null && state.getOwner() instanceof Player target && !target.equals(viewer)) {
                        event.setCancelled(true);
                    }
                }
                else if (type == PacketType.Play.Server.ENTITY_EQUIPMENT) {
                    int entityId = packet.getIntegers().read(0);
                    PlayerMorphState state = Entity_ID_TO_State.get(entityId);
                    if (state != null && state.getOwner() instanceof Player target && !target.equals(viewer)) {
                        if (isEquipment(state.getType())) {
                            event.setCancelled(true);
                        }
                    }
                }
                else if (type == PacketType.Play.Server.SPAWN_ENTITY) {
                    int entityId = packet.getIntegers().read(0);
                    PlayerMorphState state = Entity_ID_TO_State.get(entityId);
                    if (state != null && state.getOwner() instanceof Player target && !target.equals(viewer)) {
                        event.setCancelled(true);
                        sendMorphToViewer(target, state.getType(), viewer);
                        sendInitialEquipment(target, viewer);
                    }
                }
            }
        });
    }

    private boolean isEquipment(EntityType type) {
        Class<? extends Entity> entityClass = type.getEntityClass();
        return (entityClass == null || !LivingEntity.class.isAssignableFrom(entityClass)) && type.isAlive();
    }

    @Override
    protected boolean canAddState(PlayerMorphState state) {
        if (!(state.getOwner() instanceof Player player)) return false;
        if (Ari.PROTOCOL_MANAGER == null) {
            Ari.instance.getScheduler().runAtEntity(player, i -> player.sendMessage(Ari.instance.getComponentTool().text(Ari.DATA_SERVICE.getValue("base.require-pre-plugin"), player)), null);
            return false;
        }
        return this.isNotHaveState(player);
    }

    @Override
    protected void loopExecution(PlayerMorphState state) {
        if (!(state.getOwner() instanceof Player player)) {
            state.setOver(true);
            return;
        }
        state.setRunning(true);
        Ari.instance.getScheduler().runAtEntity(state.getOwner(), i -> {
            if (!player.isOnline()) {
                state.setOver(true);
            }
            state.setRunning(false);
        }, null);
    }

    @Override
    protected void abortAddState(PlayerMorphState state) {}

    @Override
    protected void passAddState(PlayerMorphState state) {
        if (!(state.getOwner() instanceof Player player)) return;
        this.Entity_ID_TO_State.put(player.getEntityId(), state);
        this.morphPlayer(player, state.getType());
        ConfigUtils.t("function.morph.changed", player).thenAccept(player::sendMessage);
        Ari.instance.getLog().debug("player {} morph entity {}.", player.getName(), state.getType().name().toLowerCase());
    }

    @Override
    protected void onEarlyExit(PlayerMorphState state) {
        if (!(state.getOwner() instanceof Player player)) return;
        this.Entity_ID_TO_State.remove(player.getEntityId());
        this.restorePlayer(player);
        Ari.instance.getLog().debug("player {} restore morph", player.getName());
    }

    @Override
    protected void onFinished(PlayerMorphState state) {
        if (!(state.getOwner() instanceof Player player)) return;
        this.Entity_ID_TO_State.remove(player.getEntityId());
        this.restorePlayer(player);
        Ari.instance.getLog().debug("player {} restore morph", player.getName());
    }

    @Override
    protected void onServiceAbort(PlayerMorphState state) {
        if (!(state.getOwner() instanceof Player player)) return;
        state.setOver(true);
        this.Entity_ID_TO_State.remove(player.getEntityId());
        Ari.instance.getLog().debug("player {} restore morph by abort.", player.getName());
    }

    @Override
    public void onReload() {
    }

    @EventHandler
    public void onPlayerRespawnPaper(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        List<PlayerMorphState> states = this.getStates(player);
        if (states.isEmpty()) return;
        Ari.instance.getScheduler().runAtEntity(player, i -> this.morphPlayer(player, states.getFirst().getType()), null);
    }

    @EventHandler
    public void onPlayerRespawnFolia(CustomPlayerRespawnEvent event) {
        Player player = event.getPlayer();
        List<PlayerMorphState> states = this.getStates(player);
        if (states.isEmpty()) return;
        Ari.instance.getScheduler().runAtEntity(player, i -> this.morphPlayer(player, states.getFirst().getType()), null);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        this.stopStateByOwner(event.getPlayer());
        this.Entity_ID_TO_State.remove(event.getPlayer().getEntityId());
    }

    @EventHandler
    public void onEntityChange(PlayerMorphState.PlayerMorphTypeChangeEvent event) {
        if (!(event.getMorpher() instanceof Player player) || !player.isOnline()) return;
        this.morphPlayer(event.getMorpher(), event.getNewType());
        ConfigUtils.t("function.morph.changed", player).thenAccept(player::sendMessage);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity victim = event.getEntity();
        if (victim instanceof Player) return;
        if (!(event.getDamageSource().getCausingEntity() instanceof Player killer)) return;
        EntityType type = victim.getType();
        if (!type.isAlive() || !type.isSpawnable()) return;
        if (killer.getStatistic(Statistic.KILL_ENTITY, type) != 0) return;
        Entity entity = DamageTrackerListener.DAMAGE_TRACKER.getLastBeKillEntity(killer);
        if (entity == null) return;
        ConfigUtils.t("function.morph.kill-entity", killer).thenAccept(i -> Ari.instance.getScheduler().runAtRegion(victim.getLocation(), t -> {
            Ari.FIREWORK_SERVICE.spawnFireworks(victim.getLocation(), 1);
            killer.sendMessage(i);
        }));
    }

    private void morphPlayer(Player player, EntityType type) {
        for(Player viewer : Bukkit.getOnlinePlayers()) {
            if(viewer.equals(player)) continue;
            this.sendMorphToViewer(player,type,viewer);
        }
    }

    private void sendMorphToViewer(Player target, EntityType type, Player viewer) {
        if (target.equals(viewer)) return;
        if (!this.canSeeMorph(target, viewer)) return;
        Location loc = target.getLocation();

        PacketContainer destroy = Ari.PROTOCOL_MANAGER.createPacket(PacketType.Play.Server.ENTITY_DESTROY);
        destroy.getIntLists().write(0, Collections.singletonList(target.getEntityId()));
        Ari.PROTOCOL_MANAGER.sendServerPacket(viewer, destroy, false);

        PacketContainer spawn = Ari.PROTOCOL_MANAGER.createPacket(PacketType.Play.Server.SPAWN_ENTITY);
        spawn.getIntegers().write(0, target.getEntityId());
        spawn.getUUIDs().write(0, target.getUniqueId());
        spawn.getEntityTypeModifier().write(0, type);
        spawn.getDoubles().write(0, loc.getX());
        spawn.getDoubles().write(1, loc.getY());
        spawn.getDoubles().write(2, loc.getZ());
        byte yawByte = (byte) ((int) (loc.getYaw() * 256.0F / 360.0F) & 0xFF);
        spawn.getBytes().write(0, yawByte);
        spawn.getBytes().write(1, (byte) 0);
        Ari.PROTOCOL_MANAGER.sendServerPacket(viewer, spawn, false);

        PacketContainer look = Ari.PROTOCOL_MANAGER.createPacket(PacketType.Play.Server.ENTITY_LOOK);
        look.getIntegers().write(0, target.getEntityId());
        look.getBytes().write(0, yawByte);
        look.getBytes().write(1, (byte) 0);
        Ari.PROTOCOL_MANAGER.sendServerPacket(viewer, look, false);

        PacketContainer headRotation = Ari.PROTOCOL_MANAGER.createPacket(PacketType.Play.Server.ENTITY_HEAD_ROTATION);
        headRotation.getIntegers().write(0, target.getEntityId());
        headRotation.getBytes().write(0, yawByte);
        Ari.PROTOCOL_MANAGER.sendServerPacket(viewer, headRotation, false);

        PacketContainer meta = Ari.PROTOCOL_MANAGER.createPacket(PacketType.Play.Server.ENTITY_METADATA);
        meta.getIntegers().write(0, target.getEntityId());

        List<WrappedDataValue> values = new ArrayList<>();
        values.add(new WrappedDataValue(2, WrappedDataWatcher.Registry.getChatComponentSerializer(true), Optional.of(AdventureComponentConverter.fromComponent(target.displayName()).getHandle())));
        values.add(new WrappedDataValue(3, WrappedDataWatcher.Registry.get(new TypeToken<Boolean>(){}.getType()), true));
        meta.getDataValueCollectionModifier().write(0, values);
        Ari.PROTOCOL_MANAGER.sendServerPacket(viewer, meta, false);
    }

    private void sendInitialEquipment(Player target, Player viewer) {
        PlayerMorphState state = Entity_ID_TO_State.get(target.getEntityId());
        if (state == null) return;
        if (isEquipment(state.getType())) return;

        int entityId = target.getEntityId();

        this.sendSingleEquipment(entityId, EnumWrappers.ItemSlot.MAINHAND, target.getInventory().getItemInMainHand(), viewer);
        this.sendSingleEquipment(entityId, EnumWrappers.ItemSlot.OFFHAND, target.getInventory().getItemInOffHand(), viewer);
        this.sendSingleEquipment(entityId, EnumWrappers.ItemSlot.FEET, target.getInventory().getBoots(), viewer);
        this.sendSingleEquipment(entityId, EnumWrappers.ItemSlot.LEGS, target.getInventory().getLeggings(), viewer);
        this.sendSingleEquipment(entityId, EnumWrappers.ItemSlot.CHEST, target.getInventory().getChestplate(), viewer);
        this.sendSingleEquipment(entityId, EnumWrappers.ItemSlot.HEAD, target.getInventory().getHelmet(), viewer);
    }

    private void sendSingleEquipment(int entityId, EnumWrappers.ItemSlot slot, ItemStack item, Player viewer) {
        PacketContainer packet = Ari.PROTOCOL_MANAGER.createPacket(PacketType.Play.Server.ENTITY_EQUIPMENT);
        packet.getIntegers().write(0, entityId);
        if (item == null) item = new ItemStack(Material.AIR);
        List<Pair<EnumWrappers.ItemSlot, ItemStack>> list = Collections.singletonList(new Pair<>(slot, item));
        packet.getSlotStackPairLists().write(0, list);
        Ari.PROTOCOL_MANAGER.sendServerPacket(viewer, packet, false);
    }

    private boolean canSeeMorph(Player target, Player viewer) {
        int distance = Bukkit.getViewDistance() * 16;
        return target.getWorld().equals(viewer.getWorld()) && target.getLocation().distanceSquared(viewer.getLocation()) <= distance * distance;
    }

    private void restorePlayer(Player player) {
        if (!player.isOnline()) return;
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            if (!viewer.equals(player)) {
                PacketContainer destroy = Ari.PROTOCOL_MANAGER.createPacket(PacketType.Play.Server.ENTITY_DESTROY);
                destroy.getIntLists().write(0, Collections.singletonList(player.getEntityId()));
                Ari.PROTOCOL_MANAGER.sendServerPacket(viewer, destroy, false);
                Ari.instance.getScheduler().runAtEntity(viewer, i -> {
                    viewer.hidePlayer(Ari.instance, player);
                    viewer.showPlayer(Ari.instance, player);
                }, null);
            }
        }
    }

}