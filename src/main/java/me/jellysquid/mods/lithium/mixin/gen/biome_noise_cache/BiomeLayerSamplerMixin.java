package me.jellysquid.mods.lithium.mixin.gen.biome_noise_cache;

import com.llamalad7.mixinextras.injector.ModifyReceiver;
import net.minecraft.world.biome.layer.util.CachingLayerSampler;
import net.minecraft.world.biome.layer.util.LayerFactory;
import net.minecraft.world.biome.source.BiomeLayerSampler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BiomeLayerSampler.class)
public abstract class BiomeLayerSamplerMixin {
    @Unique
    private ThreadLocal<CachingLayerSampler> tlSampler;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void init(LayerFactory<CachingLayerSampler> factory, CallbackInfo ci) {
        this.tlSampler = ThreadLocal.withInitial(factory::make);
    }

    /**
     * @reason Replace with implementation that accesses the thread-local sampler
     * @author gegy1000
     * <p>
     * Original implementation by gegy1000, 2No2Name replaced @Overwrite with @Redirect, contaria replaced @Redirect with @ModifyReceiver
     */
    @ModifyReceiver(
            method = "sample",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/biome/layer/util/CachingLayerSampler;sample(II)I"
            )
    )
    private CachingLayerSampler sampleThreadLocal(CachingLayerSampler cachingLayerSampler, int i, int j) {
        return this.tlSampler.get();
    }
}
