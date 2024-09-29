package me.jellysquid.mods.lithium.mixin.world.block_entity_ticking.sleeping;

import me.jellysquid.mods.lithium.common.util.collections.MaskedTickingBlockEntityList;
import me.jellysquid.mods.lithium.common.world.blockentity.BlockEntitySleepTracker;
import me.jellysquid.mods.lithium.common.world.blockentity.SleepingBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Iterator;
import java.util.List;

/**
 * Allows block entities to sleep.
 * Inspired by PallaPalla's lazy blockentities
 * @author 2No2Name
 */
@Mixin(World.class)
public abstract class WorldMixin implements BlockEntitySleepTracker {

    @Mutable
    @Shadow
    @Final
    public List<BlockEntity> tickingBlockEntities;

    @Unique
    private MaskedTickingBlockEntityList<BlockEntity> tickingBlockEntities$lithium;

    @Shadow
    public abstract boolean isClient();

    @Inject(method = "<init>", at = @At("RETURN"))
    private void reinit(CallbackInfo ci) {
        this.tickingBlockEntities$lithium = new MaskedTickingBlockEntityList<>(this.tickingBlockEntities, blockEntity -> ((SleepingBlockEntity) blockEntity).lithium$canTickOnSide(this.isClient()));
        this.tickingBlockEntities = tickingBlockEntities$lithium;
    }

    @Redirect(method = "tickBlockEntities", at = @At(value = "INVOKE", target = "Ljava/util/List;iterator()Ljava/util/Iterator;"))
    private Iterator<BlockEntity> getAwakeBlockEntities(List<BlockEntity> list) {
        if (list == this.tickingBlockEntities && list instanceof MaskedTickingBlockEntityList) {
            return ((MaskedTickingBlockEntityList<BlockEntity>) list).filteredIterator();
        }
        return list.iterator();
    }

    @Override
    public void lithium$setAwake(BlockEntity blockEntity, boolean needsTicking) {
        this.tickingBlockEntities$lithium.setEntryVisible(blockEntity, needsTicking);
    }
}
