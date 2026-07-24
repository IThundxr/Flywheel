package dev.engine_room.flywheel.backend.extension.b3d;

import dev.engine_room.flywheel.backend.b3d.FlwUniformBinding;

public interface FlwRenderPassExtension {
	default void flywheel$setPlainUniform(FlwUniformBinding uniformBinding) {
		throw new UnsupportedOperationException();
	}
}
