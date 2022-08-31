package me.jellysquid.mods.lithium.common.block;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.CollisionView;

public interface ExtendedBlockShapeCache {
    /**
     * Cached version of {@link net.minecraft.block.Block#isSolidSmallSquare(CollisionView, BlockPos, Direction)}
     */
    boolean sideCoversSmallSquare(Direction facing);

    /**
     * Cached and directional version of {@link net.minecraft.block.Block#topCoversMediumSquare(BlockView, BlockPos)}
     */
    boolean sideCoversMediumSquare(Direction facing);

    /**
     * Cached version of {@link net.minecraft.block.Block#isFaceFullSquare(VoxelShape, Direction)}
     */
    boolean isFaceFullSquare(Direction facing);
}
