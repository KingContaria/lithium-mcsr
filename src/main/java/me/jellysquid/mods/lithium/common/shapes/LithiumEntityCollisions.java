package me.jellysquid.mods.lithium.common.shapes;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityContext;
import net.minecraft.util.BooleanBiFunction;
import net.minecraft.util.CuboidBlockIterator;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.CollisionView;
import net.minecraft.world.EntityView;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.Chunk;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class LithiumEntityCollisions {
    /**
     * [VanillaCopy] CollisionView#getBlockCollisions(Entity, Box)
     * This is a much, much faster implementation which uses simple collision testing against full-cube block shapes.
     * Checks against the world border are replaced with our own optimized functions which do not go through the
     * VoxelShape system.
     */
    public static Stream<VoxelShape> getBlockCollisions(CollisionView world, final Entity entity, Box entityBox) {
        int minX = MathHelper.floor(entityBox.x1 - 1.0E-7D) - 1;
        int maxX = MathHelper.floor(entityBox.x2 + 1.0E-7D) + 1;
        int minY = MathHelper.floor(entityBox.y1 - 1.0E-7D) - 1;
        int maxY = MathHelper.floor(entityBox.y2 + 1.0E-7D) + 1;
        int minZ = MathHelper.floor(entityBox.z1 - 1.0E-7D) - 1;
        int maxZ = MathHelper.floor(entityBox.z2 + 1.0E-7D) + 1;

        final EntityContext context = entity == null ? EntityContext.absent() : EntityContext.of(entity);
        final CuboidBlockIterator cuboidIt = new CuboidBlockIterator(minX, minY, minZ, maxX, maxY, maxZ);
        final BlockPos.Mutable pos = new BlockPos.Mutable();
        final VoxelShape entityShape = VoxelShapes.cuboid(entityBox);

        return StreamSupport.stream(new Spliterators.AbstractSpliterator<VoxelShape>(Long.MAX_VALUE, Spliterator.NONNULL | Spliterator.IMMUTABLE) {
            boolean skipWorldBorderCheck = entity == null;

            public boolean tryAdvance(Consumer<? super VoxelShape> consumer) {
                if (!this.skipWorldBorderCheck) {
                    this.skipWorldBorderCheck = true;

                    VoxelShape border = world.getWorldBorder().asVoxelShape();

                    boolean isInsideBorder = VoxelShapes.matchesAnywhere(border, VoxelShapes.cuboid(entity.getBoundingBox().contract(1.0E-7D)), BooleanBiFunction.AND);
                    boolean isCrossingBorder = VoxelShapes.matchesAnywhere(border, VoxelShapes.cuboid(entity.getBoundingBox().expand(1.0E-7D)), BooleanBiFunction.AND);

                    if (!isInsideBorder && isCrossingBorder) {
                        consumer.accept(border);

                        return true;
                    }
                }

                VoxelShape shape = null;
                boolean flag;

                do {
                    int axisHit;
                    BlockState state;
                    int x, y, z;

                    do {
                        do {
                            Chunk chunk;
                            do {
                                do {
                                    if (!cuboidIt.step()) {
                                        return false;
                                    }

                                    x = cuboidIt.getX();
                                    y = cuboidIt.getY();
                                    z = cuboidIt.getZ();
                                    axisHit = cuboidIt.method_20789();
                                } while (axisHit == 3);

                                int chunkX = x >> 4;
                                int chunkZ = z >> 4;

                                chunk = world.getChunk(chunkX, chunkZ, world.getLeastChunkStatusForCollisionCalculation(), false);
                            } while (chunk == null);

                            pos.set(x, y, z);

                            state = chunk.getBlockState(pos);
                        } while (axisHit == 1 && !state.method_17900());
                    } while (axisHit == 2 && state.getBlock() != Blocks.MOVING_PISTON);

                    VoxelShape blockShape = state.getCollisionShape(world, pos, context);
                    flag = doesEntityCollideWithShape(blockShape, entityShape, entityBox, x, y, z);

                    if (flag) {
                        shape = blockShape.offset(x, y, z);
                    }
                } while (!flag);

                if (shape == null) {
                    throw new IllegalStateException();
                }

                consumer.accept(shape);

                return true;
            }
        }, false);
    }

    public static boolean doesEntityCollideWithShape(VoxelShape block, VoxelShape entityShape, Box entityBox, int x, int y, int z) {
        if (block == VoxelShapes.fullCube()) {
            return entityBox.intersects(x, y, z, x + 1.0D, y + 1.0D, z + 1.0D);
        } else if (block == VoxelShapes.empty()) {
            return false;
        }

        return VoxelShapes.matchesAnywhere(block.offset(x, y, z), entityShape, BooleanBiFunction.AND);
    }

    /**
     * This provides a faster check for seeing if an entity is within the world border as it avoids going through
     * the slower shape system.
     * @return True if the {@param box} is fully within the {@param border}, otherwise false.
     */
    public static boolean isBoxFullyWithinWorldBorder(WorldBorder border, Box box) {
        double wboxMinX = Math.floor(border.getBoundWest());
        double wboxMinZ = Math.floor(border.getBoundNorth());

        double wboxMaxX = Math.ceil(border.getBoundEast());
        double wboxMaxZ = Math.ceil(border.getBoundSouth());

        return box.x1 >= wboxMinX && box.x1 < wboxMaxX && box.z1 >= wboxMinZ && box.z1 < wboxMaxZ &&
                box.x2 >= wboxMinX && box.x2 < wboxMaxX && box.z2 >= wboxMinZ && box.z2 < wboxMaxZ;
    }

    /**
     * [VanillaCopy] EntityView#getEntityCollisions
     * Re-implements the function named above without stream code or unnecessary allocations. This can provide a small
     * boost in some situations (such as heavy entity crowding) and reduces the allocation rate significantly.
     */
    public static Stream<VoxelShape> getEntityCollisions(EntityView view, Entity entity, Box box, Set<Entity> excluded) {
        if (box.getAverageSideLength() < 1.0E-7D) {
            return Stream.empty();
        }

        Box selection = box.expand(1.0E-7D);

        List<Entity> entities = view.getEntities(entity, selection);
        List<VoxelShape> shapes = new ArrayList<>();

        for (Entity otherEntity : entities) {
            if (!excluded.isEmpty() && excluded.contains(otherEntity)) {
                continue;
            }

            if (entity != null && entity.isConnectedThroughVehicle(otherEntity)) {
                continue;
            }

            Box otherEntityBox = otherEntity.getCollisionBox();

            if (otherEntityBox != null && selection.intersects(otherEntityBox)) {
                shapes.add(VoxelShapes.cuboid(otherEntityBox));
            }

            if (entity != null) {
                Box otherEntityHardBox = entity.getHardCollisionBox(otherEntity);

                if (otherEntityHardBox != null && selection.intersects(otherEntityHardBox)) {
                    shapes.add(VoxelShapes.cuboid(otherEntityHardBox));
                }
            }
        }

        return shapes.stream();
    }
}