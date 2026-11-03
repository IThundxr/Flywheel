package dev.engine_room.flywheel.backend.compile;

import java.util.Locale;
import java.util.function.Consumer;

import org.jspecify.annotations.Nullable;

import com.mojang.renderpearl.api.pipeline.RenderPipeline;

import dev.engine_room.flywheel.backend.FlwBindGroupLayouts;
import dev.engine_room.flywheel.backend.Samplers;
import dev.engine_room.flywheel.backend.compile.core.Compilation;
import dev.engine_room.flywheel.backend.gl.shader.GlProgram;

public enum ContextShader {
	DEFAULT,
	CRUMBLING(
			"_FLW_CRUMBLING",
			program -> program.setSamplerBinding("_flw_crumblingTex", Samplers.CRUMBLING),
			b -> b.withBindGroupLayout(FlwBindGroupLayouts.CRUMBLING_SAMPLER)
	),
	EMBEDDED("FLW_EMBEDDED", 128); // 2 Mat4's (64 + 64)

	@Nullable
	private final String define;
	private final Consumer<GlProgram> onLink;
	private final Consumer<RenderPipeline.Builder> onBuildPipeline;
	private final int pushConstantSize;

	ContextShader() {
		this(null, _ -> {}, _ -> {}, 0);
	}

	ContextShader(@Nullable String define) {
		this(define, _ -> {}, _ -> {}, 0);
	}

	ContextShader(@Nullable String define, int pushConstantSize) {
		this(define, _ -> {}, _ -> {}, pushConstantSize);
	}

	ContextShader(@Nullable String define, Consumer<GlProgram> onLink, Consumer<RenderPipeline.Builder> onBuildPipeline) {
		this(define, onLink, onBuildPipeline, 0);
	}

	ContextShader(@Nullable String define, Consumer<GlProgram> onLink, Consumer<RenderPipeline.Builder> onBuildPipeline, int pushConstantSize) {
		this.define = define;
		this.onLink = onLink;
		this.onBuildPipeline = onBuildPipeline;
		this.pushConstantSize = pushConstantSize;
	}

	public int pushConstantSize() {
		return pushConstantSize;
	}

	public void onLink(GlProgram program) {
		onLink.accept(program);
	}

	public void onBuildPipeline(RenderPipeline.Builder builder) {
		onBuildPipeline.accept(builder);
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
