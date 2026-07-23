package dev.engine_room.flywheel.backend.compile;

import java.util.Locale;
import java.util.function.Consumer;

import org.jspecify.annotations.Nullable;

import com.mojang.renderpearl.api.pipeline.RenderPipeline;

import dev.engine_room.flywheel.backend.FlwBindGroupLayouts;
import dev.engine_room.flywheel.backend.engine.indirect.deprecated.Samplers;
import dev.engine_room.flywheel.backend.compile.core.Compilation;
import dev.engine_room.flywheel.backend.engine.indirect.deprecated.gl.shader.GlProgram;

public enum ContextShader {
	DEFAULT,
	CRUMBLING("_FLW_CRUMBLING", b -> b.withBindGroupLayout(FlwBindGroupLayouts.CRUMBLING_SAMPLER), program -> program.setSamplerBinding("_flw_crumblingTex", Samplers.CRUMBLING)),
	EMBEDDED("FLW_EMBEDDED");

	@Nullable
	private final String define;
	private final Consumer<RenderPipeline.Builder> onBuildPipeline;
	@Deprecated(forRemoval = true)
	private final Consumer<GlProgram> onLink;

	ContextShader() {
		this(null);
	}

	ContextShader(@Nullable String define) {
		this(define, _ -> {}, _ -> {});
	}

	ContextShader(@Nullable String define, Consumer<RenderPipeline.Builder> onBuildPipeline, @Deprecated(forRemoval = true) Consumer<GlProgram> onLink) {
		this.define = define;
		this.onBuildPipeline = onBuildPipeline;
		this.onLink = onLink;
	}

	public void onBuildPipeline(RenderPipeline.Builder builder) {
		onBuildPipeline.accept(builder);
	}

	@Deprecated(forRemoval = true)
	public void onLink(GlProgram program) {
		onLink.accept(program);
	}

	public void onCompile(Compilation comp) {
		if (define != null) {
			comp.define(define);
		}
	}

	public String nameLowerCase() {
		return name().toLowerCase(Locale.ROOT);
	}
}
