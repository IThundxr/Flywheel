package dev.engine_room.flywheel.backend.compile.core;

import java.util.HashMap;
import java.util.Map;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.pipeline.CompiledRenderPipeline;
import com.mojang.renderpearl.api.pipeline.RenderPassLayout;

import dev.engine_room.flywheel.backend.gl.GlObject;
import dev.engine_room.flywheel.backend.gl.shader.GlProgram;
import dev.engine_room.flywheel.backend.glsl.ShaderSources;
import it.unimi.dsi.fastutil.objects.Object2ReferenceMap;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;

public class CompilationHarness<K> {
	private final ShaderSources sources;
	private final KeyCompiler<K> compiler;
	private final ShaderCache shaderCache;
	private final ProgramLinker programLinker;

	@Deprecated(forRemoval = true)
	private final Map<K, GlProgram> programs = new HashMap<>();

	private final Object2ReferenceMap<PipelineKey<K>, CompiledRenderPipeline> renderPipelines = new Object2ReferenceOpenHashMap<>();

	public CompilationHarness(String marker, ShaderSources sources, KeyCompiler<K> compiler) {
		this.sources = sources;
		this.compiler = compiler;
		shaderCache = new ShaderCache();
		programLinker = new ProgramLinker();
	}

	public GlProgram get(K key) {
		return programs.computeIfAbsent(key, this::compile);
	}

	private GlProgram compile(K key) {
		return compiler.compile(key, sources, shaderCache, programLinker);
	}

	public CompiledRenderPipeline getPipeline(RenderPipeline.Snippet pipelineSnippet, RenderPassLayout.Subpass subpass, K key) {
		return renderPipelines.computeIfAbsent(new PipelineKey<>(pipelineSnippet, subpass, key), this::compileRenderPipeline);
	}

	private CompiledRenderPipeline compileRenderPipeline(PipelineKey<K> key) {
		return compiler.compileRenderPipeline(key.snippet, key.subpass, key.key, sources, shaderCache);
	}

	public void delete() {
		shaderCache.delete();

		programs.values()
				.forEach(GlObject::delete);
		programs.clear();

		renderPipelines.values()
				.forEach(CompiledRenderPipeline::close);
		renderPipelines.clear();
	}

	public interface KeyCompiler<K> {
		GlProgram compile(K key, ShaderSources loader, ShaderCache shaderCache, ProgramLinker programLinker);

		CompiledRenderPipeline compileRenderPipeline(RenderPipeline.Snippet snippet, RenderPassLayout.Subpass subpass, K key, ShaderSources loader, ShaderCache shaderCache);
	}

	private record PipelineKey<K>(RenderPipeline.Snippet snippet, RenderPassLayout.Subpass subpass, K key) {
	}
}
