package dev.engine_room.flywheel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.MixinEnvironment;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public class FlywheelTestModClient implements ClientModInitializer {
	private static final Logger LOGGER = LoggerFactory.getLogger("Flywheel Test Mod");

	private int ticks = 0;

	@Override
	public void onInitializeClient() {
		LOGGER.info("Starting Test Mod");

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			LOGGER.info("Tick Count: {}", ticks);

			if (++ticks == 50) {
				LOGGER.info("Running mixin audit");
				MixinEnvironment.getCurrentEnvironment().audit();

				LOGGER.info("Ran mixin audit, stopping client.");
				client.stop();
			}
		});
	}
}
