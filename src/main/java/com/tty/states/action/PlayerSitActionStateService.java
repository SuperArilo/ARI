package com.tty.states.action;

import com.google.gson.reflect.TypeToken;
import com.tty.Ari;
import com.tty.lib.Log;
import com.tty.dto.state.action.PlayerSitActionState;
import com.tty.enumType.FilePath;
import com.tty.lib.services.StateService;
import com.tty.tool.ConfigUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Slab;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.bukkit.util.VoxelShape;

import java.util.List;

public class PlayerSitActionStateService extends StateService<PlayerSitActionState> {

    public PlayerSitActionStateService(long rate, long c, boolean isAsync, JavaPlugin javaPlugin) {
        super(rate, c, isAsync, javaPlugin);
    }

    @Override
    protected boolean canAddState(PlayerSitActionState state) {
        Player owner = (Player) state.getOwner();
        String playerName = owner.getName();
        //判断玩家是否已经 sit 了
        if (!this.getStates(owner).isEmpty()) {
            Log.debug("player %s is sited. skip...", playerName);
            return false;
        }
        //获取列表判断是否满足的方块
        Block sitBlock = state.getSitBlock();
        String sitBlockName = sitBlock.getType().name();
        if (this.getDisableList().contains(sitBlockName)) {
            Log.debug("player %s interact the block %s is disabled", playerName, sitBlockName);
            return false;
        }
        BlockData blockData = sitBlock.getBlockData();
        //如果为楼梯
        if (blockData instanceof Stairs stairs) {
            //如果为倒放楼梯，不允许
            if (this.checkBlockTopIsNotAllow(owner, sitBlock) || stairs.getHalf().equals(Bisected.Half.TOP)) {
                owner.sendActionBar(ConfigUtils.t("function.sit.error-location"));
                return false;
            }
            return sitBlockName.endsWith("_STAIRS");
            //如果为半砖
        } else if (blockData instanceof Slab) {
            if (this.checkBlockTopIsNotAllow(owner, sitBlock)) {
                owner.sendActionBar(ConfigUtils.t("function.sit.error-location"));
                return false;
            }
            return sitBlockName.endsWith("_SLAB");
        } else {
            return false;
        }
    }

    @Override
    protected void loopExecution(PlayerSitActionState state) {

        Player owner = (Player) state.getOwner();
        if (state.getTool_entity() == null) {
            Log.error("player %s tool entity is null", owner.getName());
            state.setOver(true);
            return;
        }

        boolean b = !owner.isDead() &&
                !owner.isFlying() &&
                !owner.isSleeping() &&
                !owner.isDeeplySleeping() &&
                owner.isOnline() &&
                owner.isInsideVehicle();
        if (b) {
            state.setPending(false);
            Log.debug("player %s is sitting now.", owner.getName());
        } else {
            state.setOver(true);
        }
        state.setPending(false);
    }

    @Override
    protected void abortAddState(PlayerSitActionState state) {
        Log.debug("player %s try sit block %s fail", state.getOwner().getName(), state.getSitBlock().getType().name());
    }

    @Override
    protected void passAddState(PlayerSitActionState state) {
        Player player = (Player) state.getOwner();
        Block sitBlock = state.getSitBlock();
        Log.debug("create sit entity to player %s", player.getName());
        Location location = this.locationRecalculate(player, sitBlock);
        Entity entity = player.getWorld()
                .spawnEntity(
                        location,
                        EntityType.AREA_EFFECT_CLOUD,
                        CreatureSpawnEvent.SpawnReason.CUSTOM,
                        e -> {
                            if (e instanceof AreaEffectCloud cloud) {
                                cloud.setRadius(0);
                                cloud.setInvulnerable(true);
                                cloud.setGravity(false);
                                cloud.setInvisible(true);
                                cloud.setParticle(Particle.DUST, new Particle.DustOptions(Color.fromRGB(0, 0, 0), 0f));
                                cloud.addPassenger(player);
                            }
                        }
                );
        player.setRotation(location.getYaw(), 0);
        player.sendActionBar(ConfigUtils.t("function.sit.tips"));
        state.setTool_entity(entity);
    }

    @Override
    protected void onEarlyExit(PlayerSitActionState state) {
        String playerName = state.getOwner().getName();
        Log.debug("player %s sit check status fail, remove tool entity", playerName);
        state.removeToolEntity();
    }

    @Override
    protected void onFinished(PlayerSitActionState state) {
        state.removeToolEntity();
    }

    @Override
    protected void onServiceAbort(PlayerSitActionState state) {
        state.removeToolEntity();
    }

    private Location locationRecalculate(Player player, Block sitBlock) {
        Location location = sitBlock.getLocation();
        BlockData blockData = sitBlock.getBlockData();
        double centerX = 0.5;
        double centerZ = 0.5;

        if (blockData instanceof Stairs stairs) {
            location.add(centerX, 0, centerZ);
            location.setYaw(this.getYawFromBlockFace(stairs.getFacing()));
        } else if (blockData instanceof Slab slab){
            switch (slab.getType()) {
                case BOTTOM ->  location.add(centerX, 0, centerZ);
                case TOP, DOUBLE -> location.add(centerX, 0.5, centerZ);
            }
            location.setYaw(player.getYaw());
        }
        return location;
    }

    private boolean checkBlockTopIsNotAllow(Player p, Block actionBlock) {
        if (actionBlock == null) return true;
        Location location = actionBlock.getLocation();
        World world = location.getWorld();
        if (world == null) return true;

        double x = location.getX();
        double y = location.getY();
        double z = location.getZ();

        BoundingBox seatBox = BoundingBox.of(
                new Vector(x + 0.1, y + 1.0, z + 0.1),
                new Vector(x + 0.9, y + 1.8, z + 0.9)
        );

        int minX = (int) Math.floor(x + 0.1);
        int maxX = (int) Math.floor(x + 0.9);
        int minZ = (int) Math.floor(z + 0.1);
        int maxZ = (int) Math.floor(z + 0.9);
        int minY = (int) Math.floor(y + 1.0);
        int maxY = (int) Math.floor(y + 2.0);

        for (int bx = minX; bx <= maxX; bx++) {
            for (int by = minY; by <= maxY; by++) {
                for (int bz = minZ; bz <= maxZ; bz++) {
                    Block b = world.getBlockAt(bx, by, bz);
                    Material type = b.getType();
                    VoxelShape shape = b.getCollisionShape();
                    if (shape.overlaps(seatBox)) {
                        return true;
                    }
                    String name = type.name();
                    if (name.endsWith("_CARPET")
                            || name.endsWith("_BUTTON")
                            || name.endsWith("_PRESSURE_PLATE")
                            || name.endsWith("_LILY_PAD")
                            || name.endsWith("_RAIL")
                            || name.contains("TORCH")
                            || name.contains("SIGN")
                            || type == Material.DRAGON_EGG
                            || type == Material.FIRE
                            || type == Material.LAVA) {
                        return true;
                    }

                    if (!b.isEmpty() && !b.isPassable()) {
                        return true;
                    }
                }
            }
        }

        for (Entity entity : world.getNearbyEntities(seatBox)) {
            if (entity instanceof Player other &&
                    !other.equals(p) &&
                    other.getGameMode() != GameMode.SPECTATOR) {
                return true;
            }
        }

        return false;
    }

    private List<String> getDisableList() {
        return Ari.C_INSTANCE.getValue("action.sit.disable-block", FilePath.FUNCTION_CONFIG, new TypeToken<List<String>>(){}.getType(), List.of());
    }

    private float getYawFromBlockFace(BlockFace face) {
        return switch (face) {
            case EAST -> 90.0F;
            case SOUTH -> 180.0F;
            case WEST -> -90.0F;
            default -> 0.0F;
        };
    }

}
