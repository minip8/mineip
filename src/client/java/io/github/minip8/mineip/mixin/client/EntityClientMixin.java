package io.github.minip8.mineip.mixin.client;

import io.github.minip8.mineip.util.StalkManagerUtil;
import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class EntityClientMixin {
    @Inject(at = @At("HEAD"), method = "isCurrentlyGlowing", cancellable = true)
    private void handleIsGlowing(CallbackInfoReturnable<Boolean> cir) {
        // Cast 'this' to Entity since we are in a Mixin of Entity
        Entity entity = (Entity) (Object) this;

        // Check if the entity is a player
        if (entity instanceof Player player && StalkManagerUtil.isStalked(player.getGameProfile().id())) {
            // Force the glowing outline to be true
            cir.setReturnValue(true);
        }
    }
}