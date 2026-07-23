package dev.engine_room.flywheel.backend.compile.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import org.jspecify.annotations.Nullable;

import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.pipeline.RenderPipeline.Snippet;
import com.mojang.renderpearl.api.pipeline.ShaderSource;
import com.mojang.blaze3d.systems.RenderSystem;

import dev.engine_room.flywheel.backend.compile.FlwPrograms;
import dev.engine_room.flywheel.backend.compile.core.CompilationHarness.KeyCompiler;
import dev.engine_room.flywheel.backend.engine.indirect.deprecated.gl.shader.GlProgram;
import dev.engine_room.flywheel.backend.engine.indirect.deprecated.gl.shader.GlShader;
import dev.engine_room.flywheel.backend.gl.shader.ShaderType;
import dev.engine_room.flywheel.backend.glsl.GlslVersion;
import dev.engine_room.flywheel.backend.glsl.ShaderSources;
import dev.engine_room.flywheel.backend.glsl.SourceComponent;
import dev.engine_room.flywheel.lib.util.IdentifierUtil;
import dev.engine_room.flywheel.lib.util.StringUtil;
import net.minecraft.resources.Identifier;

/**
 * A typed provider for shader compiler builders.
 * <br>
 * This could just be a static utility class, but creating an instance of Compile
 * and calling the functors on it prevents you from having to specify the key type everywhere.
 * <br>
 * Consider {@code Compile.<PipelineKey>shader(...)} vs {@code PIPELINE.shader(...)}
 *
 * @param <K> The type of the key used to compile shaders.
 */
public class Compile<K> {
	public ShaderCompiler<K> shader(GlslVersion glslVersion, ShaderType shaderType) {
		return new ShaderCompiler<>(glslVersion, shaderType);
	}

	public ProgramStitcher<K> program() {
		return new ProgramStitcher<>();
	}

	public static class ShaderCompiler<K> {
		private final GlslVersion glslVersion;
		private final ShaderType shaderType;
		private final List<BiFunction<K, ShaderSources, SourceComponent>> fetchers = new ArrayList<>();
		private BiConsumer<K, Compilation> compilationCallbacks = ($, $$) -> {
		};
		private Function<K, String> nameMapper = Object::toString;

		public ShaderCompiler(GlslVersion glslVersion, ShaderType shaderType) {
			this.glslVersion = glslVersion;
			this.shaderType = shaderType;
		}

		public ShaderCompiler<K> nameMapper(Function<K, String> nameMapper) {
			this.nameMapper = nameMapper;
			return this;
		}

		public ShaderCompiler<K> with(BiFunction<K, ShaderSources, SourceComponent> fetch) {
			fetchers.add(fetch);
			return this;
		}

		public ShaderCompiler<K> withComponents(Collection<SourceComponent> components) {
			components.forEach(this::withComponent);
			return this;
		}

		public ShaderCompiler<K> withComponent(SourceComponent component) {
			return withComponent($ -> component);
		}

		public ShaderCompiler<K> withComponent(Function<K, SourceComponent> sourceFetcher) {
			return with((key, $) -> sourceFetcher.apply(key));
		}

		public ShaderCompiler<K> withResource(Function<K, Identifier> sourceFetcher) {
			return with((key, loader) -> loader.get(sourceFetcher.apply(key)));
		}

		public ShaderCompiler<K> withResource(Identifier id) {
			return withResource($ -> id);
		}

		public ShaderCompiler<K> onCompile(BiConsumer<K, Compilation> cb) {
			compilationCallbacks = compilationCallbacks.andThen(cb);
			return this;
		}

		@Deprecated(forRemoval = true)
		public ShaderCompiler<K> define(String def, int value) {
			return onCompile(($, ctx) -> ctx.define(def, String.valueOf(value)));
		}

		public ShaderCompiler<K> enableExtension(String extension) {
			return onCompile(($, ctx) -> ctx.enableExtension(extension));
		}

		public ShaderCompiler<K> enableExtensions(String... extensions) {
			return onCompile(($, ctx) -> {
				for (String extension : extensions) {
					ctx.enableExtension(extension);
				}
			});
		}

		public ShaderCompiler<K> enableExtensions(Collection<String> extensions) {
			return onCompile(($, ctx) -> {
				for (String extension : extensions) {
					ctx.enableExtension(extension);
				}
			});
		}

		public ShaderCompiler<K> requireExtensions(Collection<String> extensions) {
			return onCompile(($, ctx) -> {
				for (String extension : extensions) {
					ctx.requireExtension(extension);
				}
			});
		}

		@Deprecated(forRemoval = true)
		private GlShader compile(K key, ShaderCache compiler, ShaderSources loader) {
			long start = System.nanoTime();

			var components = new ArrayList<SourceComponent>();
			for (var fetcher : fetchers) {
				components.add(fetcher.apply(key, loader));
			}

			Consumer<Compilation> cb = ctx -> compilationCallbacks.accept(key, ctx);
			var name = getShaderName(key);
			var out = compiler.compile(glslVersion, shaderType, name, cb, components);

			long end = System.nanoTime();

			FlwPrograms.LOGGER.debug("Compiled {} in {}", name, StringUtil.formatTime(end - start));

			return out;
		}

		private String getSource(K key, ShaderCache compiler, ShaderSources loader) {
			var components = new ArrayList<SourceComponent>();
			for (var fetcher : fetchers) {
				components.add(fetcher.apply(key, loader));
			}

			Consumer<Compilation> cb = ctx -> compilationCallbacks.accept(key, ctx);
			return compiler.getSource(glslVersion, shaderType, getShaderName(key), cb, components);
		}

		private String getShaderName(K key) {
			return nameMapper.apply(key);
		}
	}

	public static class ProgramStitcher<K> implements KeyCompiler<K> {
		private final Map<ShaderType, ShaderCompiler<K>> compilers = new EnumMap<>(ShaderType.class);
		private RenderPipeline.@Nullable Snippet programSnippet;
		@Deprecated(forRemoval = true)
		private BiConsumer<K, GlProgram> postLink = (k, p) -> {
		};
		@Deprecated(forRemoval = true)
		private BiConsumer<K, GlProgram> preLink = (k, p) -> {
		};

		public CompilationHarness<K> harness(String marker, ShaderSources sources) {
			return new CompilationHarness<>(marker, sources, this);
		}

		public ProgramStitcher<K> link(ShaderCompiler<K> compilerBuilder) {
			if (compilers.containsKey(compilerBuilder.shaderType)) {
				throw new IllegalArgumentException("Duplicate shader type: " + compilerBuilder.shaderType);
			}
			compilers.put(compilerBuilder.shaderType, compilerBuilder);
			return this;
		}

		@Deprecated(forRemoval = true)
		public ProgramStitcher<K> postLink(BiConsumer<K, GlProgram> postLink) {
			this.postLink = postLink;
			return this;
		}

		@Deprecated(forRemoval = true)
		public ProgramStitcher<K> preLink(BiConsumer<K, GlProgram> preLink) {
			this.preLink = preLink;
			return this;
		}

		public ProgramStitcher<K> snippet(RenderPipeline.Snippet snippet) {
			this.programSnippet = snippet;
			return this;
		}

		@Deprecated(forRemoval = true)
		@Override
		public GlProgram compile(K key, ShaderSources loader, ShaderCache shaderCache, ProgramLinker programLinker) {
			if (compilers.isEmpty()) {
				throw new IllegalStateException("No shader compilers were added!");
			}

			long start = System.nanoTime();

			List<GlShader> shaders = new ArrayList<>();

			for (ShaderCompiler<K> compiler : compilers.values()) {
				shaders.add(compiler.compile(key, shaderCache, loader));
			}

			var out = programLinker.link(shaders, p -> preLink.accept(key, p));

			postLink.accept(key, out);

			long end = System.nanoTime();

			FlwPrograms.LOGGER.debug("Linked {} in {}", key, StringUtil.formatTime(end - start));

			return out;
		}

		@Override
		public RenderPipeline compileRenderPipeline(Snippet snippet, K key, ShaderSources loader, ShaderCache shaderCache) {
			if (compilers.isEmpty()) {
				throw new IllegalStateException("No shader compilers were added!");
			}

			Snippet[] snippets = programSnippet == null
					? new Snippet[] { snippet }
					: new Snippet[] { snippet, programSnippet };

			Identifier vertexShaderId = getIdFor(ShaderType.VERTEX, key);
			Identifier fragmentShaderId = getIdFor(ShaderType.FRAGMENT, key);
			RenderPipeline pipeline = RenderPipeline.builder(snippets)
					.withLocation(vertexShaderId)
					.withVertexShader(vertexShaderId)
					.withFragmentShader(fragmentShaderId)
					.build();

			// SPIR-V (Vulkan backend) requires explicit locations on all user in/out;
			// process both stages together so varying locations agree.
			String flwVertexSrc = compilers.get(ShaderType.VERTEX).getSource(key, shaderCache, loader);
			ShaderCompiler<K> flwFragCompiler = compilers.get(ShaderType.FRAGMENT);
			String flwFragmentSrc = flwFragCompiler == null ? null : flwFragCompiler.getSource(key, shaderCache, loader);
			FlwSpirvLocations.Processed flwProcessed = dev.engine_room.flywheel.backend.b3d.DeviceFeatureCompat.BACKEND_NAME.equals("OpenGL")
					? new FlwSpirvLocations.Processed(flwVertexSrc, flwFragmentSrc)
					: FlwSpirvLocations.process(flwVertexSrc, flwFragmentSrc);

			ShaderSource shaderSource = (_, type) -> switch (type) {
				case VERTEX -> flwProcessed.vertex();
				case FRAGMENT -> flwProcessed.fragment();
			};

			if (Compilation.DUMP_SHADER_SOURCE) {
				for (ShaderType shaderType : ShaderType.values()) {
					ShaderCompiler<K> compiler = compilers.get(shaderType);
					if (compiler != null) {
						String source = compiler.getSource(key, shaderCache, loader);

						String shaderName = compiler.getShaderName(key) + "." + shaderType.extension;
						Compilation.dumpSource(source, shaderName);
					}
				}
			}

			CompiledPipelines.put(pipeline, RenderSystem.getDevice().compilePipeline(pipeline, shaderSource));
			return pipeline;
		}

		private Identifier getIdFor(ShaderType type, K key) {
			String name = compilers.get(type).nameMapper.apply(key);
			return IdentifierUtil.id("generated/" + name);
		}
	}
}
