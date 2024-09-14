package dev.engine_room.flywheel.impl.compat;

import java.util.function.Supplier;

import dev.engine_room.flywheel.impl.FlwImplXplat;

public enum CompatMods {
	SODIUM,
	EMBEDDIUM,
	IRIS;

	private final Supplier<Boolean> isLoaded;

	CompatMods() {
		isLoaded = FlwImplXplat.INSTANCE.getModLoaded(name());
	}

	public boolean isLoaded() {
		return isLoaded.get();
	}
}
