package me.jellysquid.mods.lithium.mixin.world.block_entity_ticking.sleeping;

import me.jellysquid.mods.lithium.common.world.blockentity.BlockEntitySleepTracker;
import me.jellysquid.mods.lithium.common.world.blockentity.SleepingBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.BrewingStandBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BrewingStandBlockEntity.class)
public abstract class BrewingStandBlockEntityMixin extends BlockEntity implements SleepingBlockEntity {
    @Shadow
    private int brewTime;

    @Unique
    private boolean isTicking = true;

    public BrewingStandBlockEntityMixin(BlockEntityType<?> type) {
        super(type);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void checkSleep(CallbackInfo ci) {
        if (this.brewTime == 0 && this.world != null) {
            this.isTicking = false;
            ((BlockEntitySleepTracker) this.world).lithium$setAwake(this, false);
        }
    }

    @Inject(method = "fromTag", at = @At("RETURN"))
    private void wakeUpAfterFromTag(CallbackInfo ci) {
        if (!this.isTicking && this.world != null && !this.world.isClient()) {
            this.isTicking = true;
            ((BlockEntitySleepTracker) this.world).lithium$setAwake(this, true);
        }
    }

    @Override
    public void markDirty() {
        super.markDirty();
        if (!this.isTicking && this.world != null && !this.world.isClient()) {
            this.isTicking = true;
            ((BlockEntitySleepTracker) this.world).lithium$setAwake(this, true);
        }
    }

    @Override
    public boolean lithium$canTickOnSide(boolean isClient) {
        return !isClient;
    }
}
