package dev.engine_room.flywheel;

import org.spongepowered.asm.mixin.MixinEnvironment;

import net.minecraft.client.Minecraft;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod("flywheel_testmod")
public class FlywheelTestModClient {
	private int ticks = 0;

	public FlywheelTestModClient() {
		if (Boolean.parseBoolean(System.getProperty("FLYWHEEL_AUTO_TEST"))) {

			IEventBus modEventBus = FMLJavaModLoadingContext.get()
					.getModEventBus();

			modEventBus.addListener((TickEvent.ClientTickEvent e) -> {
				if (e.phase == TickEvent.Phase.END) {
					if (++ticks == 50) {
						MixinEnvironment.getCurrentEnvironment().audit();
						Minecraft.getInstance().stop();
					}
				}
			});
		}
	}
}
