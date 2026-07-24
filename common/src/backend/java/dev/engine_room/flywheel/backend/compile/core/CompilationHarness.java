package dev.engine_room.flywheel.backend.compile.core;

import java.util.HashMap;
import java.util.Map;

import com.mojang.renderpearl.api.pipeline.CompiledRenderPipeline;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;

import dev.engine_room.flywheel.backend.gl.GlObject;
import dev.engine_room.flywheel.backend.gl.shader.GlProgram;
import dev.engine_room.flywheel.backend.glsl.ShaderSources;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;

public class CompilationHarness<K> {
	private final ShaderSources sources;
	private final KeyCompiler<K> compiler;
	private final ShaderCache shaderCache;
	private final ProgramLinker programLinker;

	@Deprecated(forRemoval = true)
	private final Map<K, GlProgram> programs = new HashMap<>();

	private final Map<PipelineKey<K>, CompiledRenderPipeline> renderPipelines = new Reference2ReferenceOpenHashMap<>();

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

	public CompiledRenderPipeline getPipeline(RenderPipeline.Snippet pipelineSnippet, K key) {
		return renderPipelines.computeIfAbsent(new PipelineKey<>(pipelineSnippet, key), this::compileRenderPipeline);
	}

	private CompiledRenderPipeline compileRenderPipeline(PipelineKey<K> key) {
		return compiler.compileRenderPipeline(key.snippet, key.key, sources, shaderCache);
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

		CompiledRenderPipeline compileRenderPipeline(RenderPipeline.Snippet snippet, K key, ShaderSources loader, ShaderCache shaderCache);
	}

	private record PipelineKey<K>(RenderPipeline.Snippet snippet, K key) {
	}
}
