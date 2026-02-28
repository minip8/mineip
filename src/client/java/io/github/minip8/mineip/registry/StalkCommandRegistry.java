package io.github.minip8.mineip.registry;


import com.mojang.brigadier.arguments.StringArgumentType;
import io.github.minip8.mineip.util.StalkManagerUtil;
import io.github.minip8.mineip.util.UUIDUtil;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;

import java.util.UUID;

public class StalkCommandRegistry {

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register(
                (dispatcher, registryAccess) -> dispatcher.register(ClientCommandManager.literal("stalk")
                        .then(ClientCommandManager.argument("player", StringArgumentType.string())
                                // Adds auto-completion for players currently in the Tab list
                                .suggests((context, builder) -> {
                                    Minecraft client = Minecraft.getInstance();
                                    if (client.getConnection() != null) {
                                        for (PlayerInfo info : client.getConnection().getOnlinePlayers()) {
                                            builder.suggest(info.getProfile().name());
                                        }
                                    }
                                    return builder.buildFuture();
                                }).executes(context -> {
                                    String targetName = StringArgumentType.getString(context, "player");
                                    Minecraft client = Minecraft.getInstance();

                                    if (client.getConnection() == null) return 0;

                                    UUID targetUUID = UUIDUtil.nameToUUID(targetName,
                                            client.getConnection().getOnlinePlayers());

                                    if (targetUUID == null) {
                                        context.getSource().sendFeedback(Component.literal(
                                                "§c" + targetName + " is not a" + " valid player on this server!"));
                                        return 0;
                                    }

                                    // Execute toggle in your specific StalkManagerUtil
                                    StalkManagerUtil.toggleStalk(targetUUID);

                                    boolean isStalking = StalkManagerUtil.isStalked(targetUUID);

                                    // Success Feedback
                                    context.getSource().sendFeedback(Component.literal(
                                            (isStalking ? "§aStarted stalking: §f" : "§cStopped stalking: §f") + targetName));

                                    return 1;
                                }))));
    }
}