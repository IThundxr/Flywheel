package dev.engine_room.flywheel;

import net.minecraftforge.common.MinecraftForge;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.MixinEnvironment;

import net.minecraft.client.Minecraft;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod("flywheel_testmod")
public class FlywheelTestModClient {
	private static final Logger LOGGER = LoggerFactory.getLogger("Flywheel Test Mod");

	private int ticks = 0;

	public FlywheelTestModClient() {
		MinecraftForge.EVENT_BUS.addListener((TickEvent.ClientTickEvent e) -> {
			if (e.phase == TickEvent.Phase.END) {
				LOGGER.info("Tick Count: {}", ticks);

				if (++ticks == 50) {
					LOGGER.info("Running mixin audit");
					MixinEnvironment.getCurrentEnvironment().audit();

					LOGGER.info("Ran mixin audit, stopping client.");
					Minecraft.getInstance().stop();
				}
			}
		});
	}
}
