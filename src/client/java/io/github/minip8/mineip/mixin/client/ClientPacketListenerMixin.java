package io.github.minip8.mineip.mixin.client;

import io.github.minip8.mineip.util.PotionEffectsUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {

    @Inject(method = "handleUpdateMobEffect", at = @At("HEAD"), cancellable =
            true)
    private void onUpdateMobEffect(ClientboundUpdateMobEffectPacket packet,
                                   CallbackInfo ci) {
        if (Minecraft.getInstance().player != null
                &&
                packet.getEntityId() == Minecraft.getInstance().player.getId()
                && PotionEffectsUtil.isDisabledEffect(
                packet.getEffect().value())) {
            ci.cancel();
        }
    }
}