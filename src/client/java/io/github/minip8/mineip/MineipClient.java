package io.github.minip8.mineip;

import io.github.minip8.mineip.registry.StalkCommandRegistry;
import net.fabricmc.api.ClientModInitializer;

public class MineipClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
		StalkCommandRegistry.register();

	}
}