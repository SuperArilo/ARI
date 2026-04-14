package com.tty.tool;

import com.tty.Ari;
import com.tty.api.utils.PublicFunctionUtils;
import org.bukkit.*;
import org.bukkit.block.Block;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class SearchSafeLocation {
    
    private int searchCountInChunk = 3;

    public SearchSafeLocation() {

    }

    public SearchSafeLocation(int searchCountInChunk) {
        this.searchCountInChunk = searchCountInChunk;
        if (this.searchCountInChunk <= 0) {
            throw new IllegalArgumentException("searchCountInChunk can not set 0.");
        }
    }

    public CompletableFuture<Location> search(World world, int x, int z) {

        CompletableFuture<Location> result = new CompletableFuture<>();

        int count = this.searchCountInChunk;

        world.getChunkAtAsync(x >> 4, z >> 4)
                .orTimeout(5, TimeUnit.SECONDS)
                .thenAccept(chunk -> this.attemptSearch(world, chunk, count, result))
                .exceptionally(i -> {
                    Ari.LOG.debug("chunk load failed: {}", i.toString());
                    result.completeExceptionally(i);
                    return null;
                });

        return result;
    }

    //递归搜索
    private void attemptSearch(World world, Chunk chunk, int tryCount, CompletableFuture<Location> result) {
        if (result.isDone()) return;
        Ari.LOG.debug("search in chunk count {}. total {}. chunk info: x: {}, z: {}", tryCount, this.searchCountInChunk, chunk.getX(), chunk.getZ());

        if (tryCount <= 0) {
            Ari.LOG.debug("chunk x: {}, z: {}, search attempts exhausted, giving up.", chunk.getX(), chunk.getZ());
            result.complete(null);
            return;
        }

        tryCount--;

        int chunkLocalX = PublicFunctionUtils.randomGenerator(0, 15);
        int chunkLocalZ = PublicFunctionUtils.randomGenerator(0, 15);

        boolean isNether = world.getEnvironment().equals(World.Environment.NETHER);

        int chunkLocalY = isNether ? this.getSafeNetherY(world, chunk, chunkLocalX, chunkLocalZ):chunk.getChunkSnapshot().getHighestBlockYAt(chunkLocalX, chunkLocalZ);

        double newWorldX = (chunk.getX() << 4) + chunkLocalX + 0.5;
        double newWorldZ = (chunk.getZ() << 4) + chunkLocalZ + 0.5;

        Ari.LOG.debug("checking random location: x: {}, y: {}, z: {} is safe.", newWorldX, chunkLocalY, newWorldZ);

        int finalTryCount = tryCount;
        if (chunkLocalY == Integer.MAX_VALUE) {
            Ari.SCHEDULER.runAtRegion(Ari.instance, world, chunk.getX(), chunk.getZ(), i -> this.attemptSearch(world, chunk, finalTryCount, result));
            return;
        }

        Ari.SCHEDULER.runAtRegion(Ari.instance, world, chunk.getX(), chunk.getZ(), i -> {
            if (this.isLocationSafe(chunk, chunkLocalX, chunkLocalY, chunkLocalZ)) {
                Location location = new Location(world, newWorldX, chunkLocalY, newWorldZ);
                if (Ari.INTERACT_SERVICE.canTeleport(location)) {
                    Ari.LOG.debug("random location x: {}, y: {}, z: {} safe. return result.", newWorldX, chunkLocalY, newWorldZ);
                    result.complete(location);
                } else {
                    this.attemptSearch(world, chunk, finalTryCount, result);
                }
            } else {
                this.attemptSearch(world, chunk, finalTryCount, result);
            }
        });

    }

    //下界特殊处理
    public int getSafeNetherY(World world, Chunk chunk, int localX, int localZ) {
        ChunkSnapshot snapshot = chunk.getChunkSnapshot();

        final int minHeight = world.getMinHeight();
        final int maxHeight = world.getMaxHeight();

        for (int y = maxHeight - 1; y >= minHeight; y--) {
            Material footType = snapshot.getBlockType(localX, y, localZ);

            if (!footType.isSolid() || snapshot.getBlockType(localX, y, localZ).isAir()) continue;

            if (footType == Material.LAVA || footType == Material.WATER) continue;

            if (footType == Material.BEDROCK) continue;

            Material above1 = snapshot.getBlockType(localX, y + 1, localZ);
            Material above2 = snapshot.getBlockType(localX, y + 2, localZ);

            if ((above1.isAir() || !above1.isSolid()) && (above2.isAir() || !above2.isSolid())) return y;
        }

        return Integer.MAX_VALUE;
    }

    private boolean isLocationSafe(Chunk chunk, int chunkX, int chunkY, int chunkZ) {

        //判断Y轴高度合不合法
        if (chunkY < chunk.getWorld().getMinHeight()) {
            Ari.LOG.debug("illegal Y-axis height.");
            return false;
        }

        Block block = chunk.getBlock(chunkX, chunkY, chunkZ);

        //身体检查
        Material head = chunk.getBlock(chunkX, chunkY + 2, chunkZ).getType();
        Material body = chunk.getBlock(chunkX, chunkY + 1, chunkZ).getType();
        Material feet = block.getType();

        //周围检查
        Material left = block.getRelative(1, 0, 0).getType();
        Material right = block.getRelative(-1, 0, 0).getType();
        Material front = block.getRelative(0, 0, -1).getType();
        Material behind = block.getRelative(0, 0,  1).getType();

        if (isNotSafeStandingBlock(feet)) {
            Ari.LOG.debug("standing block illegal.");
            return false;
        }

        if (this.isSolid(body) || this.isSolid(head) || this.isDangerous(body) || this.isDangerous(head) || this.isDangerous(left) || this.isDangerous(right) || this.isDangerous(front) || this.isDangerous(behind)) {
            Ari.LOG.debug("the blocks around the player are illegal.");
            return false;
        }

        if (this.isDangerous(feet)) {
            Ari.LOG.debug("feet block is dangerous.");
            return false;
        }
        if (chunk.getBlock(chunkX, chunkY - 1, chunkZ).getType().isAir()) {
            Ari.LOG.debug("feet block illegal.");
            return false;
        }

        Block belowLeft = block.getRelative( 1, -1, 0);
        Block belowRight = block.getRelative(-1, -1, 0);
        Block belowFront = block.getRelative( 0, -1,-1);
        Block belowBehind = block.getRelative( 0, -1, 1);

        if (this.isNotSafeFloor(belowLeft) || this.isNotSafeFloor(belowRight) || this.isNotSafeFloor(belowFront) || this.isNotSafeFloor(belowBehind)) {
            Ari.LOG.debug("edge detected: missing safe floor around player.");
            return false;
        }

        return true;

    }

    private boolean isNotSafeFloor(Block block) {
        Material type = block.getType();
        if (type.isAir()) return true;
        if (this.isDangerous(type)) return true;
        return !type.isSolid() || this.isNotSafeStandingBlock(type);
    }

    private boolean isNotSafeStandingBlock(Material material) {
        return !material.isSolid() ||
                material.name().contains("LEAVES") ||
                material.name().contains("GLASS") ||
                material == Material.SLIME_BLOCK;
    }

    private boolean isSolid(Material material) {
        return switch (material) {
            case AIR, CAVE_AIR, VOID_AIR, WATER, LAVA -> false;
            default -> material.isSolid();
        };
    }

    private boolean isDangerous(Material material) {
        return switch (material) {
            case LAVA, FIRE, SOUL_FIRE, MAGMA_BLOCK, CACTUS, SWEET_BERRY_BUSH -> true;
            default -> false;
        };
    }

//    private boolean canUse(Location location) {
//        return this.worldGuardCheck(location) && this.dominionCheck(location) && this.cmiResidenceCheck(location);
//    }
//
//    /**
//     * 使用 WorldGuard 对随机到的区域进行检查是否可传送
//     * @param location 检查的位置
//     * @return true 可，false 不可
//     */
//    private boolean worldGuardCheck(Location location) {
//        if (!this.hasWorldGuard()) return true;
//        ApplicableRegionSet regions = WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery().getApplicableRegions(BukkitAdapter.adapt(location));
//        for (ProtectedRegion region : regions) {
//            if (region.getFlag(Flags.ENTRY) == StateFlag.State.DENY) {
//                Ari.LOG.debug("location: x: {}, y: {}, z: {} in WorldGuard not allow.", location.getX(), location.getY(), location.getZ());
//                return false;
//            }
//        }
//        return true;
//    }
//
//    /**
//     * 使用 cmi 的 residence 对随机到的区域进行检查是否可传送
//     * @param location 检查的位置
//     * @return true 可，false 不可
//     */
//    private boolean cmiResidenceCheck(Location location) {
//        if (!this.hasCmiResidence()) return true;
//        try {
//            ClaimedResidence byLoc = Residence.getInstance().getResidenceManager().getByLoc(location);
//            boolean status = byLoc == null;
//            if (!status) {
//                Ari.LOG.debug("location: x: {}, y: {}, z: {} in Residence not allow.", location.getX(), location.getY(), location.getZ());
//            }
//            return status;
//        } catch (Exception e) {
//            Ari.LOG.warn(e, "check cmi residence plugin error.");
//            return true;
//        }
//    }
//
//    /**
//     * 使用 Dominion 对随机到的区域进行检查是否可传送
//     * @param location 检查的位置
//     * @return true 可，false 不可
//     */
//    private boolean dominionCheck(Location location) {
//        if (!this.hasDominion()) return true;
//        try {
//            DominionDTO dominion = DominionAPI.getInstance().getDominion(location);
//            boolean status = dominion == null;
//            if (!status) {
//                Ari.LOG.debug("location: x: {}, y: {}, z: {} in Dominion not allow.", location.getX(), location.getY(), location.getZ());
//            }
//            return status;
//        } catch (Exception e) {
//            Ari.LOG.warn(e, "check dominion plugin error.");
//            return true;
//        }
//    }

    private boolean hasCmiResidence() {
        return Bukkit.getPluginManager().getPlugin("Residence") != null;
    }

    private boolean hasWorldGuard() {
        return Bukkit.getPluginManager().getPlugin("WorldGuard") != null;
    }

    private boolean hasDominion() {
        return Bukkit.getPluginManager().getPlugin("Dominion") != null;
    }

    public void debug(boolean status) {
        Ari.LOG.setDebug(status);
    }

}