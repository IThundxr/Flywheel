package dev.engine_room.flywheel.backend.engine.embed;

import com.mojang.renderpearl.api.commands.RenderPass;

import dev.engine_room.flywheel.backend.compile.ContextShader;

public interface Environment {
	ContextShader contextShader();

	void setupDraw(RenderPass renderPass);

	int matrixIndex();
}
