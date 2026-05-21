package dev.engine_room.flywheel.backend.compile;

import dev.engine_room.flywheel.backend.FlwRenderPipelines;
import dev.engine_room.flywheel.backend.compile.component.BufferTextureInstanceComponent;
import dev.engine_room.flywheel.backend.compile.component.SsboInstanceComponent;
import dev.engine_room.flywheel.lib.util.IdentifierUtil;

public final class Pipelines {
	public static final Pipeline INSTANCING = Pipeline.builder()
			.compilerMarker("instancing")
			.vertexMain(IdentifierUtil.id("internal/instancing/main.vert"))
			.fragmentMain(IdentifierUtil.id("internal/instancing/main.frag"))
			.assembler(BufferTextureInstanceComponent::new)
			.snippet(FlwRenderPipelines.INSTANCING_SNIPPET)
			.build();

	public static final Pipeline INDIRECT = Pipeline.builder()
			.compilerMarker("indirect")
			.vertexMain(IdentifierUtil.id("internal/indirect/main.vert"))
			.fragmentMain(IdentifierUtil.id("internal/indirect/main.frag"))
			.assembler(SsboInstanceComponent::new)
			.build();

	private Pipelines() {
	}
}
