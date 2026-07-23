package dev.engine_room.flywheel.backend.compile;

import java.util.Objects;

import org.jspecify.annotations.Nullable;

import com.mojang.renderpearl.api.pipeline.RenderPipeline;

import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.instance.InstanceType;
import dev.engine_room.flywheel.backend.glsl.SourceComponent;
import net.minecraft.resources.Identifier;

public record Pipeline(Identifier vertexMain, Identifier fragmentMain,
                       InstanceAssembler assembler, RenderPipeline.@Nullable Snippet snippet, String compilerMarker) {

	@FunctionalInterface
	public interface InstanceAssembler {
		/**
		 * Generate the source component necessary to convert a packed {@link Instance} into its shader representation.
		 *
		 * @return A source component defining functions that unpack a representation of the given instance type.
		 */
		SourceComponent assemble(InstanceType<?> instanceType);
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		@Nullable
		private Identifier vertexMain;
		@Nullable
		private Identifier fragmentMain;
		@Nullable
		private InstanceAssembler assembler;
		private RenderPipeline.@Nullable Snippet snippet;
		@Nullable
		private String compilerMarker;

		public Builder vertexMain(Identifier shader) {
			this.vertexMain = shader;
			return this;
		}

		public Builder fragmentMain(Identifier shader) {
			this.fragmentMain = shader;
			return this;
		}

		public Builder assembler(InstanceAssembler assembler) {
			this.assembler = assembler;
			return this;
		}

		public Builder snippet(RenderPipeline.Snippet snippet) {
			this.snippet = snippet;
			return this;
		}

		public Builder compilerMarker(String compilerMarker) {
			this.compilerMarker = compilerMarker;
			return this;
		}

		public Pipeline build() {
			Objects.requireNonNull(vertexMain);
			Objects.requireNonNull(fragmentMain);
			Objects.requireNonNull(assembler);
			Objects.requireNonNull(compilerMarker);
			return new Pipeline(vertexMain, fragmentMain, assembler, snippet, compilerMarker);
		}
	}
}
