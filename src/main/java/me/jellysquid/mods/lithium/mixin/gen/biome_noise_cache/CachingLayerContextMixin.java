package me.jellysquid.mods.lithium.mixin.gen.biome_noise_cache;

import it.unimi.dsi.fastutil.longs.Long2IntLinkedOpenHashMap;
import me.jellysquid.mods.lithium.common.world.layer.FastCachingLayerSampler;
import net.minecraft.world.biome.layer.util.CachingLayerContext;
import net.minecraft.world.biome.layer.util.CachingLayerSampler;
import net.minecraft.world.biome.layer.util.LayerOperator;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CachingLayerContext.class)
public abstract class CachingLayerContextMixin {

    @Shadow
    @Final
    @Mutable
    private Long2IntLinkedOpenHashMap cache;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void init(CallbackInfo ci) {
        // We don't use this cache
        this.cache = null;
    }

    /**
     * @reason Replace with optimized cache implementation
     * @author gegy1000
     */
    @Overwrite
    public CachingLayerSampler createSampler(LayerOperator operator) {
        return new FastCachingLayerSampler(256, operator);
    }

    /**
     * @reason Replace with optimized cache implementation
     * @author gegy1000
     */
    @Overwrite
    public CachingLayerSampler createSampler(LayerOperator operator, CachingLayerSampler sampler) {
        return new FastCachingLayerSampler(1024, operator);
    }

    /**
     * @reason Replace with optimized cache implementation
     * @author gegy1000
     */
    @Overwrite
    public CachingLayerSampler createSampler(LayerOperator operator, CachingLayerSampler left, CachingLayerSampler right) {
        return new FastCachingLayerSampler(1024, operator);
    }
}
