package dev.engine_room.flywheel.backend.compile;

import java.util.Locale;
import java.util.function.Consumer;

import org.jspecify.annotations.Nullable;

import com.mojang.blaze3d.pipeline.RenderPipeline;

import dev.engine_room.flywheel.backend.FlwBindGroupLayouts;
import dev.engine_room.flywheel.backend.compile.core.Compilation;

public enum ContextShader {
	DEFAULT,
	CRUMBLING("_FLW_CRUMBLING", b -> b.withBindGroupLayout(FlwBindGroupLayouts.CRUMBLING_SAMPLER)),
	EMBEDDED("FLW_EMBEDDED");

	@Nullable
	private final String define;
	private final Consumer<RenderPipeline.Builder> onBuildPipeline;

	ContextShader() {
		this(null);
	}

	ContextShader(@Nullable String define) {
		this(define, _ -> {});
	}

	ContextShader(@Nullable String define, Consumer<RenderPipeline.Builder> onBuildPipeline) {
		this.define = define;
		this.onBuildPipeline = onBuildPipeline;
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
