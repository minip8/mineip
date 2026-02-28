package io.github.minip8.mineip.util;

import net.minecraft.client.multiplayer.PlayerInfo;

import java.util.Collection;
import java.util.UUID;

public class UUIDUtil {
    public static UUID nameToUUID(String name, Collection<PlayerInfo> playerInfos) {
        return playerInfos
                .stream()
                .filter(playerInfo -> playerInfo.getProfile().name().equals(name))
                .map(playerInfo -> playerInfo.getProfile().id())
                .findFirst()
                .orElse(null);
    }
}
