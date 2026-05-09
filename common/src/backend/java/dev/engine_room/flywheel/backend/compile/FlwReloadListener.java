package dev.engine_room.flywheel.backend.compile;

import dev.engine_room.flywheel.backend.FlwRenderPipelines;
import dev.engine_room.flywheel.lib.util.IdentifierUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;

public final class FlwReloadListener implements ResourceManagerReloadListener {
	public static final Identifier ID = IdentifierUtil.id("reload_listener");

	public static final FlwReloadListener INSTANCE = new FlwReloadListener();

	private FlwReloadListener() {
	}

	@Override
	public void onResourceManagerReload(ResourceManager manager) {
		FlwPrograms.reload(manager);
		FlwRenderPipelines.clearCache();
	}
}
