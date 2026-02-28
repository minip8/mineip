package io.github.minip8.mineip.util;


import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class StalkUtil {
    // Store UUIDs as they are more reliable than names
    public static final Set<UUID> STALKED_PLAYERS = new HashSet<>();

    public static void toggleStalk(UUID playerUUID) {
        if (STALKED_PLAYERS.contains(playerUUID)) {
            STALKED_PLAYERS.remove(playerUUID);
        } else {
            STALKED_PLAYERS.add(playerUUID);
        }
    }

    public static boolean isStalked(UUID playerUUID) {
        return STALKED_PLAYERS.contains(playerUUID);
    }
}