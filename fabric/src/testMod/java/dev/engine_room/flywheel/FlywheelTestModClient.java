package dev.engine_room.flywheel;

import org.spongepowered.asm.mixin.MixinEnvironment;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public class FlywheelTestModClient implements ClientModInitializer {
	private int ticks = 0;

	@Override
	public void onInitializeClient() {
		if (Boolean.parseBoolean(System.getProperty("FLYWHEEL_AUTO_TEST"))) {
			ClientTickEvents.END_CLIENT_TICK.register(client -> {
				if (++ticks == 50) {
					MixinEnvironment.getCurrentEnvironment().audit();
					client.stop();
				}
			});
		}
	}
}
