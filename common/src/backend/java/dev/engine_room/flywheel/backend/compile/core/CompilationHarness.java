package dev.engine_room.flywheel.backend.compile.core;

import java.util.HashMap;
import java.util.Map;

import com.mojang.renderpearl.api.pipeline.RenderPipeline;

import dev.engine_room.flywheel.backend.engine.indirect.deprecated.gl.GlObject;
import dev.engine_room.flywheel.backend.engine.indirect.deprecated.gl.shader.GlProgram;
import dev.engine_room.flywheel.backend.glsl.ShaderSources;

public class CompilationHarness<K> {
	private final ShaderSources sources;
	private final KeyCompiler<K> compiler;
	private final ShaderCache shaderCache;
	private final ProgramLinker programLinker;

	@Deprecated(forRemoval = true)
	private final Map<K, GlProgram> programs = new HashMap<>();

	private final Map<PipelineKey<K>, RenderPipeline> pipelines = new HashMap<>();

	public CompilationHarness(String marker, ShaderSources sources, KeyCompiler<K> compiler) {
		this.sources = sources;
		this.compiler = compiler;
		shaderCache = new ShaderCache();
		programLinker = new ProgramLinker();
	}

	@Deprecated(forRemoval = true)
	public GlProgram get(K key) {
		return programs.computeIfAbsent(key, this::compile);
	}

	@Deprecated(forRemoval = true)
	private GlProgram compile(K key) {
		return compiler.compile(key, sources, shaderCache, programLinker);
	}

	public RenderPipeline getPipeline(RenderPipeline.Snippet pipelineSnippet, K key) {
		return pipelines.computeIfAbsent(new PipelineKey<>(pipelineSnippet, key), this::compileRenderPipeline);
	}

	private RenderPipeline compileRenderPipeline(PipelineKey<K> key) {
		return compiler.compileRenderPipeline(key.snippet, key.key, sources, shaderCache);
	}

	public void delete() {
		shaderCache.delete();

		programs.values()
				.forEach(GlObject::delete);

		programs.clear();
		pipelines.clear();
	}

	public interface KeyCompiler<K> {
		GlProgram compile(K key, ShaderSources loader, ShaderCache shaderCache, ProgramLinker programLinker);

		RenderPipeline compileRenderPipeline(RenderPipeline.Snippet snippet, K key, ShaderSources loader, ShaderCache shaderCache);
	}

	private record PipelineKey<K>(RenderPipeline.Snippet snippet, K key) {
	}
}
