package me.jellysquid.mods.lithium.mixin.ai.nearby_entity_tracking;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import me.jellysquid.mods.lithium.common.entity.tracker.EntityTrackerEngine;
import me.jellysquid.mods.lithium.common.entity.tracker.EntityTrackerEngineProvider;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Installs event listeners to the world class which will be used to notify the {@link EntityTrackerEngine} of changes.
 */
@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin {
    /**
     * Notify the entity tracker when an entity is removed from the world.
     */
    @ModifyExpressionValue(
            method = "unloadEntities",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/Iterator;next()Ljava/lang/Object;"
            )
    )
    private Object onEntityRemoved(Object next) {
        Entity entity = (Entity) next;
        if (!(entity instanceof LivingEntity)) {
            return entity;
        }

        int chunkX = MathHelper.floor(entity.getX()) >> 4;
        int chunkY = MathHelper.clamp(MathHelper.floor(entity.getY()) >> 4, 0, 15);
        int chunkZ = MathHelper.floor(entity.getZ()) >> 4;

        EntityTrackerEngine tracker = EntityTrackerEngineProvider.lithium$getEntityTracker(this);
        tracker.onEntityRemoved(chunkX, chunkY, chunkZ, (LivingEntity) entity);
        return entity;
    }
}
