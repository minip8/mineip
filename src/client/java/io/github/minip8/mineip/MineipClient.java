package io.github.minip8.mineip;

import io.github.minip8.mineip.registry.PotionEffectsRegistry;
import io.github.minip8.mineip.registry.StalkCommandRegistry;
import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MineipClient implements ClientModInitializer {
    public static final String MOD_ID = Mineip.MOD_ID;
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        // This entrypoint is suitable for setting up client-specific logic,
        // such as rendering.
        StalkCommandRegistry.register();
        PotionEffectsRegistry.register();
    }
}