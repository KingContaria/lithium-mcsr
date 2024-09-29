package me.jellysquid.mods.lithium.mixin.entity.replace_entitytype_predicates;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;
import java.util.function.Predicate;

@Mixin(ArmorStandEntity.class)
public abstract class ArmorStandEntityMixin {
    @Shadow
    @Final
    private static Predicate<Entity> RIDEABLE_MINECART_PREDICATE;

    @WrapOperation(
            method = "tickCramming",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/World;getEntities(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/Box;Ljava/util/function/Predicate;)Ljava/util/List;"
            )
    )
    private List<Entity> getMinecartsDirectly(World world, Entity excluded, Box box, Predicate<? super Entity> predicate, Operation<List<Entity>> original) {
        if (predicate == RIDEABLE_MINECART_PREDICATE) {
            // Not using MinecartEntity.class and no predicate, because mods may add another minecart that is type ridable without being MinecartEntity
            return world.getEntities(AbstractMinecartEntity.class, box, (Entity e) -> e != excluded && ((AbstractMinecartEntity) e).getMinecartType() == AbstractMinecartEntity.Type.RIDEABLE);
        }
        return original.call(world, excluded, box, predicate);
    }
}
