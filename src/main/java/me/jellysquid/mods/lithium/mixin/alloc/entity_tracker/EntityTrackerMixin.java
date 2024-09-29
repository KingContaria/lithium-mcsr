package me.jellysquid.mods.lithium.mixin.alloc.entity_tracker;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

@Mixin(ThreadedAnvilChunkStorage.EntityTracker.class)
public abstract class EntityTrackerMixin {
    @Mutable
    @Shadow
    @Final
    private Set<ServerPlayerEntity> playersTracking;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void reinit(CallbackInfo ci) {
        // Uses less memory, and will cache the returned iterator
        this.playersTracking = new ObjectOpenHashSet<>(this.playersTracking);
    }
}
