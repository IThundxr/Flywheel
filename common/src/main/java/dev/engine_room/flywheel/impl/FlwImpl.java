package dev.engine_room.flywheel.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.engine_room.flywheel.api.Flywheel;
import dev.engine_room.flywheel.backend.FlwBackend;
import dev.engine_room.flywheel.impl.registry.IdRegistryImpl;
import net.minecraft.SharedConstants;

public final class FlwImpl {
	public static final Logger LOGGER = LoggerFactory.getLogger(Flywheel.ID);
	public static final Logger CONFIG_LOGGER = LoggerFactory.getLogger(Flywheel.ID + "/config");

	private FlwImpl() {
	}

	public static void init() {
		// impl
		BackendManagerImpl.init();

		// backend
		FlwBackend.init(FlwConfig.INSTANCE.backendConfig());

		if (Boolean.getBoolean("flw.devEnv")) {
			SharedConstants.IS_RUNNING_IN_IDE = true;
		}
	}

	public static void freezeRegistries() {
		IdRegistryImpl.freezeAll();
	}
}
