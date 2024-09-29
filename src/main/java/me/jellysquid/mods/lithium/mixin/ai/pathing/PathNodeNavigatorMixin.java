package me.jellysquid.mods.lithium.mixin.ai.pathing;

import me.jellysquid.mods.lithium.common.ai.pathing.PathNodeCache;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.ai.pathing.PathNodeNavigator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PathNodeNavigator.class)
public class PathNodeNavigatorMixin {
    @Inject(method = "findPathToAny(Lnet/minecraft/entity/ai/pathing/PathNode;Ljava/util/Map;FIF)Lnet/minecraft/entity/ai/pathing/Path;", at = @At("HEAD"))
    private void preFindPathToAny(CallbackInfoReturnable<Path> cir) {
        PathNodeCache.enableChunkCache();
    }

    @Inject(method = "findPathToAny(Lnet/minecraft/entity/ai/pathing/PathNode;Ljava/util/Map;FIF)Lnet/minecraft/entity/ai/pathing/Path;", at = @At("RETURN"))
    private void postFindPathToAny(CallbackInfoReturnable<Path> cir) {
        PathNodeCache.disableChunkCache();
    }
}
