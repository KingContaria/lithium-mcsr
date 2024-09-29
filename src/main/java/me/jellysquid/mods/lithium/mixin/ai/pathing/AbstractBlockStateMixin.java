package me.jellysquid.mods.lithium.mixin.ai.pathing;

import me.jellysquid.mods.lithium.api.pathing.BlockPathingBehavior;
import me.jellysquid.mods.lithium.common.ai.pathing.BlockStatePathingCache;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.ai.pathing.PathNodeType;
import org.apache.commons.lang3.Validate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractBlock.AbstractBlockState.class)
public abstract class AbstractBlockStateMixin implements BlockStatePathingCache {
    @Unique
    private PathNodeType pathNodeType = PathNodeType.OPEN;
    @Unique
    private PathNodeType pathNodeTypeNeighbor = PathNodeType.OPEN;

    @Shadow
    protected abstract BlockState asBlockState();

    @Shadow
    public abstract Block getBlock();

    @Inject(method = "initShapeCache", at = @At("RETURN"))
    private void init(CallbackInfo ci) {
        BlockState state = this.asBlockState();
        BlockPathingBehavior behavior = (BlockPathingBehavior) this.getBlock();

        this.pathNodeType = Validate.notNull(behavior.lithium$getPathNodeType(state));
        this.pathNodeTypeNeighbor = Validate.notNull(behavior.lithium$getPathNodeTypeAsNeighbor(state));
    }

    @Override
    public PathNodeType lithium$getPathNodeType() {
        return this.pathNodeType;
    }

    @Override
    public PathNodeType lithium$getNeighborPathNodeType() {
        return this.pathNodeTypeNeighbor;
    }
}
