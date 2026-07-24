package dev.engine_room.flywheel.backend.engine.embed;

import com.mojang.renderpearl.api.commands.RenderPass;

import dev.engine_room.flywheel.backend.compile.ContextShader;

public class GlobalEnvironment implements Environment {
	public static final GlobalEnvironment INSTANCE = new GlobalEnvironment();

	private GlobalEnvironment() {
	}

	@Override
	public ContextShader contextShader() {
		return ContextShader.DEFAULT;
	}

	@Override
	public void setupDraw(RenderPass renderPass) {
	}

	@Override
	public int matrixIndex() {
		return 0;
	}
}
