package com.tty.ari.states.action;

import com.tty.api.state.StateService;
import com.tty.ari.Ari;
import com.tty.ari.configuration.GameActionConfig;
import com.tty.ari.dto.state.action.PlayerSitActionState;
import com.tty.ari.tool.ConfigUtils;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.block.data.type.Slab;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.bukkit.util.VoxelShape;

import java.util.List;

public class PlayerSitActionStateService extends StateService<PlayerSitActionState> {

    private List<String> disableBlockList;

    private final Object disableBlockListLock = new Object();

    public PlayerSitActionStateService(long rate, long c, boolean isAsync) {
        super(rate, c, isAsync, Ari.instance);
        this.disableBlockList = this.getDisableList();
    }

    @Override
    protected boolean canAddState(PlayerSitActionState state) {
        Player owner = (Player) state.getOwner();
        String playerName = owner.getName();
        //判断玩家是否已经 sit 了
        if (!this.getStates(owner).isEmpty()) {
            Ari.instance.getLog().debug("player {} is sited. skip...", playerName);
            return false;
        }
        //获取列表判断是否满足的方块
        Block sitBlock = state.getSitBlock();
        String sitBlockName = sitBlock.getType().name();

        synchronized (this.disableBlockListLock) {
            if (this.disableBlockList.contains(sitBlockName)) {
                Ari.instance.getLog().debug("player {} interact the block {} is disabled", playerName, sitBlockName);
                return false;
            }
        }

        if (!Ari.INTERACT_SERVICE.canInteract(sitBlock.getLocation(), owner)) {
            ConfigUtils.t("function.sit.error-location", owner).thenAccept(owner::sendActionBar);
            return false;
        }

        boolean status = false;
        BlockData blockData = sitBlock.getBlockData();
        //如果为楼梯
        if (blockData instanceof Stairs stairs) {
            //如果为倒放楼梯，不允许
            if (this.checkBlockTopIsNotAllow(owner, sitBlock) || stairs.getHalf().equals(Bisected.Half.TOP) || this.isBlockFullyInWater(sitBlock)) {
                ConfigUtils.t("function.sit.error-location", owner).thenAccept(owner::sendActionBar);
            } else {
                status = true;
            }
        }
        //如果为半砖
        if (blockData instanceof Slab) {
            if (this.checkBlockTopIsNotAllow(owner, sitBlock) || this.isBlockFullyInWater(sitBlock)) {
                ConfigUtils.t("function.sit.error-location", owner).thenAccept(owner::sendActionBar);
            } else {
                status = true;
            }
        }
        return status;
    }

    @Override
    protected void loopExecution(PlayerSitActionState state) {

        Player owner = (Player) state.getOwner();
        if (state.getTool_entity() == null) {
            Ari.instance.getLog().error("player {} tool entity is null", owner.getName());
            state.setOver(true);
            return;
        }

        state.setRunning(true);
        Ari.instance.getScheduler().runAtEntity(Ari.instance, owner, i -> {
            boolean b = !owner.isDead() &&
                    !owner.isFlying() &&
                    !owner.isSleeping() &&
                    !owner.isDeeplySleeping() &&
                    owner.isOnline() &&
                    owner.isInsideVehicle();
            if (!b) {
                state.setOver(true);
            }
            state.setRunning(false);
        }, null);
    }

    @Override
    protected void abortAddState(PlayerSitActionState state) {

    }

    @Override
    protected void passAddState(PlayerSitActionState state) {
        Player player = (Player) state.getOwner();
        Block sitBlock = state.getSitBlock();
        Location location = this.locationRecalculate(player, sitBlock);
        state.createToolEntity(
            player.getWorld(),
            location,
            i -> {
                i.addPassenger(player);
                player.setRotation(location.getYaw(), 0);
                ConfigUtils.t("function.sit.tips", player).thenAccept(t ->
                    Ari.instance.getScheduler().runAtEntity(Ari.instance, player, p -> player.sendActionBar(t), null));
            }
        );

        Ari.instance.getLog().debug("player {} sit block {}.", state.getOwner().getName(), sitBlock.getType().name());
    }

    @Override
    protected void onEarlyExit(PlayerSitActionState state) {
        String playerName = state.getOwner().getName();
        Ari.instance.getLog().debug("player {} sit check status fail, remove tool entity", playerName);
        state.removeToolEntity(Ari.instance);
    }

    @Override
    protected void onFinished(PlayerSitActionState state) {
        state.removeToolEntity(Ari.instance);
    }

    @Override
    protected void onServiceAbort(PlayerSitActionState state) {
        state.removeToolEntity(Ari.instance);
    }

    @Override
    public void onReload() {
        synchronized (this.disableBlockListLock) {
            this.disableBlockList = this.getDisableList();
        }
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
        List<String> value = Ari.instance.getConfigurationManager().get(GameActionConfig.class).getSitDisableBlock();
        return value.stream().map(String::toUpperCase).toList();
    }

    private float getYawFromBlockFace(BlockFace face) {
        return switch (face) {
            case EAST -> 90.0F;
            case SOUTH -> 180.0F;
            case WEST -> -90.0F;
            default -> 0.0F;
        };
    }

    public boolean isBlockFullyInWater(Block block) {
        return block.getType() == Material.WATER || (block.getBlockData() instanceof Waterlogged wl && wl.isWaterlogged());
    }

}
