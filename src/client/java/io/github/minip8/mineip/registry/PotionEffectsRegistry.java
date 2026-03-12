package io.github.minip8.mineip.registry;

import io.github.minip8.mineip.util.PotionEffectsUtil;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;

import java.util.List;

public class PotionEffectsRegistry {

    public static void register() {
        register_enable_command();
        register_disable_command();
//        register_tick_event();
    }

    // this results in a flicker because the potion effects are rendered for
    // one tick - use a mixin instead
    private static void register_tick_event() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) {
                return;
            }
            for (MobEffectInstance mobEffectInstance : List.copyOf(
                    client.player.getActiveEffects())) {

                MobEffect mobEffect = mobEffectInstance.getEffect().value();
                if (PotionEffectsUtil.isDisabledEffect(mobEffect)) {
                    client.player.removeEffect(mobEffectInstance.getEffect());
                }
            }
        });
    }

    private static void register_disable_command() {
        ClientCommandRegistrationCallback.EVENT.register(
                (dispatcher, registryAccess) -> dispatcher.register(
                        ClientCommandManager.literal("effect_disable")
                                .then(ClientCommandManager.argument("effect_id",
                                                ResourceArgument.resource(
                                                        registryAccess,
                                                        Registries.MOB_EFFECT))
                                        .executes(context -> {

                                            Holder<MobEffect> effect =
                                                    context.getArgument(
                                                            "effect_id",
                                                            Holder.class);

                                            MobEffect mobEffect =
                                                    effect.value();


                                            boolean effectDisableSuccess =
                                                    PotionEffectsUtil.addDisabledEffect(
                                                            mobEffect);

                                            context.getSource().sendFeedback(
                                                    Component.literal(
                                                            effectDisableSuccess ? String.format(
                                                                    "%s is now disabled!",
                                                                    mobEffect) : "No effects changed!"));
                                            return 1;
                                        }))));
    }

    private static void register_enable_command() {
        ClientCommandRegistrationCallback.EVENT.register(
                (dispatcher, registryAccess) -> dispatcher.register(
                        ClientCommandManager.literal("effect_enable")
                                .then(ClientCommandManager.argument("effect_id",
                                                ResourceArgument.resource(
                                                        registryAccess,
                                                        Registries.MOB_EFFECT))
                                        .executes(context -> {

                                            Holder<MobEffect> effect =
                                                    context.getArgument(
                                                            "effect_id",
                                                            Holder.class);

                                            MobEffect mobEffect =
                                                    effect.value();

                                            boolean effectEnableSuccess =
                                                    PotionEffectsUtil.removeDisabledEffect(
                                                            mobEffect);

                                            context.getSource().sendFeedback(
                                                    Component.literal(
                                                            effectEnableSuccess ? String.format(
                                                                    "%s is now enabled!",
                                                                    mobEffect) : "No effects changed!"));
                                            return 1;
                                        }))));
    }
}
