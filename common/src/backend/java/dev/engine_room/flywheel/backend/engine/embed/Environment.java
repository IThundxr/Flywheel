package dev.engine_room.flywheel.backend.engine.embed;

import com.mojang.blaze3d.buffers.Std140Builder;

import dev.engine_room.flywheel.backend.compile.ContextShader;

public interface Environment {
	ContextShader contextShader();

	void setupDraw(Std140Builder pushConstantsBuilder);

	int matrixIndex();
}
