package dev.engine_room.flywheel.backend.compile.core;

import java.util.HashMap;
import java.util.Map;

import org.jspecify.annotations.Nullable;

import com.mojang.blaze3d.pipeline.ShaderSource;
import com.mojang.renderpearl.api.pipeline.ShaderType;

import dev.engine_room.flywheel.backend.glsl.ShaderSources;
import dev.engine_room.flywheel.backend.glsl.SourceFile;
import net.minecraft.resources.Identifier;

public final class FlwShaderSource implements ShaderSource {
	private final FlwSourceGetter sourceGetter;
	private final ShaderSources loader;
	private final Map<Identifier, CachedIncludeSource> includeSources = new HashMap<>();

	public FlwShaderSource(FlwSourceGetter sourceGetter, ShaderSources loader) {
		this.sourceGetter = sourceGetter;
		this.loader = loader;
	}

	@Override
	public @Nullable String getShader(Identifier id, ShaderType type) {
		return sourceGetter.getShader(id, type);
	}

	@Override
	public @Nullable CachedIncludeSource getInclude(Identifier id) {
		CachedIncludeSource cached = includeSources.get(id);
		if (cached != null) {
			return cached;
		}

		SourceFile sourceFile = loader.peek(id);
		if (sourceFile != null) {
			CachedIncludeSource includeSource = CachedIncludeSource.create(id, sourceFile.source());
			includeSources.put(id, includeSource);
			return includeSource;
		}

		// TODO - Fallback to vanilla shaders, would be nice to support #include-ing those
		return null;
	}

	@Override
	public void close() {
    	for (CachedIncludeSource source : includeSources.values()) {
			source.close();
		}
	}

	@FunctionalInterface
	public interface FlwSourceGetter {
		@Nullable
		String getShader(Identifier id, ShaderType type);
	}
}
