package me.jellysquid.mods.lithium.mixin.world.block_entity_ticking.sleeping;

import me.jellysquid.mods.lithium.common.world.blockentity.BlockEntitySleepTracker;
import net.minecraft.block.entity.BellBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BellBlockEntity.class)
public abstract class BellBlockEntityMixin extends BlockEntity {

    @Shadow
    private boolean resonating;

    @Shadow
    public boolean ringing;

    public BellBlockEntityMixin(BlockEntityType<?> type) {
        super(type);
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void checkSleep(CallbackInfo ci) {
        if (!this.ringing && !this.resonating && this.world != null) {
            ((BlockEntitySleepTracker)this.world).lithium$setAwake(this, false);
        }
    }

    @Inject(method = "activate", at = @At("HEAD"))
    private void checkWakeUp(CallbackInfo ci) {
        if (!this.ringing && this.world != null) {
            ((BlockEntitySleepTracker)this.world).lithium$setAwake(this, true);
        }
    }

    @Inject(method = "onSyncedBlockEvent", at = @At("HEAD"))
    private void checkWakeUp(int type, int data, CallbackInfoReturnable<Boolean> cir) {
        if (!this.ringing && type == 1 && this.world != null) {
            ((BlockEntitySleepTracker)this.world).lithium$setAwake(this, true);
        }
    }
}
