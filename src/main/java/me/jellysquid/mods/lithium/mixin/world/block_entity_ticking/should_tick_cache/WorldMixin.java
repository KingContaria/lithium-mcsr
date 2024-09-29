package me.jellysquid.mods.lithium.mixin.world.block_entity_ticking.should_tick_cache;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(World.class)
public abstract class WorldMixin {
    @Unique
    private long lastBlockEntityChunkPos = Long.MIN_VALUE;
    @Unique
    private boolean lastShouldTick;

    /**
     * As block entities are loaded in chunk batches, it is likely that the same chunk is queried multiple times in a row.
     * By caching the result we can reduce the amount of chunk manager lookups to one per chunk, assuming no new block
     * entities are created (pistons for example don't meet this criteria).
     */
    @WrapOperation(
            method = "tickBlockEntities",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/chunk/ChunkManager;shouldTickBlock(Lnet/minecraft/util/math/BlockPos;)Z"
            )
    )
    private boolean shouldTick(ChunkManager chunkManager, BlockPos pos, Operation<Boolean> original) {
        long l = ChunkPos.toLong(pos.getX() >> 4, pos.getZ() >> 4);
        if (this.lastBlockEntityChunkPos == l) {
            return this.lastShouldTick;
        } else {
            this.lastBlockEntityChunkPos = l;
            return this.lastShouldTick = original.call(chunkManager, pos);
        }
    }

    @Inject(
            method = "tickBlockEntities",
            at = @At("RETURN")
    )
    private void clearCache(CallbackInfo ci) {
        this.lastBlockEntityChunkPos = Long.MIN_VALUE;
    }
}
