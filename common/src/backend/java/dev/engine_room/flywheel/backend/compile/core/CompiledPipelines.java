package dev.engine_room.flywheel.backend.compile.core;

import java.util.IdentityHashMap;
import java.util.Map;

import com.mojang.renderpearl.api.pipeline.CompiledRenderPipeline;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;

// 26.3-snapshot-5: RenderPass.setPipeline now requires a CompiledRenderPipeline.
// Flywheel compiles pipelines up front in Compile; this cache lets draw-time code
// look the compiled form back up without re-plumbing pipeline types everywhere.
public final class CompiledPipelines {
	private static final Map<RenderPipeline, CompiledRenderPipeline> CACHE = new IdentityHashMap<>();

	private CompiledPipelines() {
	}

	public static synchronized void put(RenderPipeline pipeline, CompiledRenderPipeline compiled) {
		CACHE.put(pipeline, compiled);
	}

	public static synchronized CompiledRenderPipeline get(RenderPipeline pipeline) {
		CompiledRenderPipeline compiled = CACHE.get(pipeline);
		if (compiled == null) {
			throw new IllegalStateException("Pipeline was not compiled through Flywheel's Compile: " + pipeline.getLocation());
		}
		return compiled;
	}
}
