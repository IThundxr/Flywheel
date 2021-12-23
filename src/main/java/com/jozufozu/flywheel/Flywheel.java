package com.jozufozu.flywheel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.jozufozu.flywheel.config.EngineArgument;
import com.jozufozu.flywheel.config.FlwCommands;
import com.jozufozu.flywheel.config.FlwConfig;
import com.jozufozu.flywheel.config.FlwPackets;

import net.minecraft.commands.synchronization.ArgumentTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod("flywheel")
public class Flywheel {

	public static final String ID = "flywheel";
	public static final Logger log = LogManager.getLogger(Flywheel.class);

	public Flywheel() {
		FMLJavaModLoadingContext.get()
				.getModEventBus()
				.addListener(this::setup);

		MinecraftForge.EVENT_BUS.addListener(FlwCommands::onServerStarting);

		FlwConfig.init();

		DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> FlywheelClient::clientInit);
	}

	public static ResourceLocation rl(String path) {
		return new ResourceLocation(ID, path);
	}

    private void setup(final FMLCommonSetupEvent event) {
		FlwPackets.registerPackets();
		ArgumentTypes.register("flywheel:engine", EngineArgument.class, EngineArgument.SERIALIZER);
	}
}
