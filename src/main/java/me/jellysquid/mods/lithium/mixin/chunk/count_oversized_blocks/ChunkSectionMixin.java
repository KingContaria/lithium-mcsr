package me.jellysquid.mods.lithium.mixin.chunk.count_oversized_blocks;

import com.llamalad7.mixinextras.injector.ModifyReceiver;
import me.jellysquid.mods.lithium.common.entity.movement.ChunkAwareBlockCollisionSweeper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.FluidState;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.PalettedContainer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Keep track of how many oversized blocks are in this chunk section. If none are there, collision code can skip a few blocks.
 * Oversized blocks are fences, walls, extended piston heads and blocks with dynamic bounds (scaffolding, shulker box, moving blocks)
 *
 * @author 2No2Name
 */
@Mixin(ChunkSection.class)
public abstract class ChunkSectionMixin implements ChunkAwareBlockCollisionSweeper.OversizedBlocksCounter {
    @Shadow
    @Final
    private PalettedContainer<BlockState> container;
    @Unique
    private short oversizedBlockCount;

    @ModifyArg(
            method = "calculateCounts",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/chunk/PalettedContainer;count(Lnet/minecraft/world/chunk/PalettedContainer$CountConsumer;)V"
            )
    )
    private PalettedContainer.CountConsumer<BlockState> addToOversizedBlockCount(PalettedContainer.CountConsumer<BlockState> consumer) {
        return (state, count) -> {
            consumer.accept(state, count);
            if (state.exceedsCube()) {
                this.oversizedBlockCount += (short) count;
            }
        };
    }

    @Inject(method = "calculateCounts", at = @At("HEAD"))
    private void resetOversizedBlockCount(CallbackInfo ci) {
        this.oversizedBlockCount = 0;
    }

    @ModifyReceiver(
            method = "setBlockState(IIILnet/minecraft/block/BlockState;Z)Lnet/minecraft/block/BlockState;",
            at = @At(
                    ordinal = 0,
                    value = "INVOKE",
                    target = "Lnet/minecraft/block/BlockState;hasRandomTicks()Z"
            )
    )
    private BlockState decrOversizedBlockCount(BlockState state) {
        if (state.exceedsCube()) {
            --this.oversizedBlockCount;
        }
        return state;
    }

    @ModifyReceiver(
            method = "setBlockState(IIILnet/minecraft/block/BlockState;Z)Lnet/minecraft/block/BlockState;",
            at = @At(
                    ordinal = 1,
                    value = "INVOKE",
                    target = "Lnet/minecraft/block/BlockState;hasRandomTicks()Z"
            )
    )
    private BlockState incrOversizedBlockCount(BlockState state) {
        if (state.exceedsCube()) {
            ++this.oversizedBlockCount;
        }
        return state;
    }

    @Override
    public boolean lithium$hasOversizedBlocks() {
        return this.oversizedBlockCount > 0;
    }

    /**
     * Initialize oversized block count in the client worlds.
     */
    @Environment(EnvType.CLIENT)
    @Inject(method = "fromPacket", at = @At("RETURN"))
    private void initOversizedBlockCounts(CallbackInfo ci) {
        this.oversizedBlockCount = 0;
        this.container.count((state, count) -> {
            if (state.exceedsCube()) {
                this.oversizedBlockCount += (short) count;
            }
        });
    }
}
