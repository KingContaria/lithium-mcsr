package me.jellysquid.mods.lithium.mixin.cached_hashcode;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Block.NeighborGroup.class)
public abstract class BlockNeighborGroupMixin {
    @Shadow
    @Final
    private BlockState self;

    @Shadow
    @Final
    private BlockState other;

    @Shadow
    @Final
    private Direction facing;

    @Unique
    private int hash;

    /**
     * @reason Initialize the object's hashcode and cache it
     */
    @Inject(method = "<init>", at = @At("RETURN"))
    private void generateHash(CallbackInfo ci) {
        int hash = this.self.hashCode();
        hash = 31 * hash + this.other.hashCode();
        hash = 31 * hash + this.facing.hashCode();

        this.hash = hash;
    }

    /**
     * @reason Uses the cached hashcode
     * @author JellySquid
     */
    @Overwrite
    public int hashCode() {
        return this.hash;
    }
}
