package dev.engine_room.flywheel.lib.material;

import java.util.Optional;

import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;

import dev.engine_room.flywheel.api.material.CardinalLightingMode;
import dev.engine_room.flywheel.api.material.CutoutShader;
import dev.engine_room.flywheel.api.material.FogShader;
import dev.engine_room.flywheel.api.material.LightShader;
import dev.engine_room.flywheel.api.material.Material;
import dev.engine_room.flywheel.api.material.MaterialShaders;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.Identifier;

public class SimpleMaterial implements Material {
	protected final MaterialShaders shaders;
	protected final FogShader fog;
	protected final CutoutShader cutout;
	protected final LightShader light;

	protected final Identifier texture;
	protected final boolean blur;
	protected final boolean mipmap;

	protected final boolean backfaceCulling;
	protected final Optional<DepthStencilState> depthStencilState;
	protected final Optional<ColorTargetState> colorTargetState;

	protected final boolean useOverlay;
	protected final boolean useOit;
	protected final boolean useLight;
	protected final CardinalLightingMode cardinalLightingMode;

	protected final boolean ambientOcclusion;

	protected SimpleMaterial(Builder builder) {
		shaders = builder.shaders();
		fog = builder.fog();
		cutout = builder.cutout();
		light = builder.light();
		texture = builder.texture();
		blur = builder.blur();
		mipmap = builder.mipmap();
		backfaceCulling = builder.backfaceCulling();
		depthStencilState = builder.depthStencilState();
		colorTargetState = builder.colorTargetState();
		useOverlay = builder.useOverlay();
		useOit = builder.useOit();
		useLight = builder.useLight();
		cardinalLightingMode = builder.cardinalLightingMode();
		ambientOcclusion = builder.ambientOcclusion();
	}

	public static Builder builder() {
		return new Builder();
	}

	public static Builder builderOf(Material material) {
		return new Builder(material);
	}

	@Override
	public MaterialShaders shaders() {
		return shaders;
	}

	@Override
	public FogShader fog() {
		return fog;
	}

	@Override
	public CutoutShader cutout() {
		return cutout;
	}

	@Override
	public LightShader light() {
		return light;
	}

	@Override
	public Identifier texture() {
		return texture;
	}

	@Override
	public boolean blur() {
		return blur;
	}

	@Override
	public boolean mipmap() {
		return mipmap;
	}

	@Override
	public boolean backfaceCulling() {
		return backfaceCulling;
	}

	@Override
	public Optional<DepthStencilState> depthStencilState() {
		return depthStencilState;
	}

	@Override
	public Optional<ColorTargetState> colorTargetState() {
		return colorTargetState;
	}

	@Override
	public boolean useOverlay() {
		return useOverlay;
	}

	@Override
	public boolean useOit() {
		return useOit;
	}

	@Override
	public boolean useLight() {
		return useLight;
	}

	@Override
	public CardinalLightingMode cardinalLightingMode() {
		return cardinalLightingMode;
	}

	@Override
	public boolean ambientOcclusion() {
		return ambientOcclusion;
	}

	public static class Builder implements Material {
		protected MaterialShaders shaders;
		protected FogShader fog;
		protected CutoutShader cutout;
		protected LightShader light;

		protected Identifier texture;
		protected boolean blur;
		protected boolean mipmap;

		protected boolean backfaceCulling;
		protected Optional<DepthStencilState> depthStencilState;
		protected Optional<ColorTargetState> colorTargetState;

		protected boolean useOverlay;
		protected boolean useOit;
		protected boolean useLight;
		protected CardinalLightingMode cardinalLightingMode;

		protected boolean ambientOcclusion;

		public Builder() {
			shaders = StandardMaterialShaders.DEFAULT;
			fog = FogShaders.LINEAR;
			cutout = CutoutShaders.OFF;
			light = LightShaders.SMOOTH_WHEN_EMBEDDED;
			texture = TextureAtlas.LOCATION_BLOCKS;
			blur = false;
			mipmap = true;
			backfaceCulling = true;
			depthStencilState = Optional.empty();
			colorTargetState = Optional.empty();
			useOverlay = true;
			useOit = false;
			useLight = true;
			cardinalLightingMode = CardinalLightingMode.ENTITY;
			ambientOcclusion = true;
		}

		public Builder(Material material) {
			copyFrom(material);
		}

		public Builder copyFrom(Material material) {
			shaders = material.shaders();
			fog = material.fog();
			cutout = material.cutout();
			light = material.light();
			texture = material.texture();
			blur = material.blur();
			mipmap = material.mipmap();
			backfaceCulling = material.backfaceCulling();
			depthStencilState = material.depthStencilState();
			colorTargetState = material.colorTargetState();
			useOverlay = material.useOverlay();
			useOit = material.useOit();
			useLight = material.useLight();
			cardinalLightingMode = material.cardinalLightingMode();
			ambientOcclusion = material.ambientOcclusion();
			return this;
		}

		public Builder shaders(MaterialShaders value) {
			this.shaders = value;
			return this;
		}

		public Builder fog(FogShader value) {
			this.fog = value;
			return this;
		}

		public Builder cutout(CutoutShader value) {
			this.cutout = value;
			return this;
		}

		public Builder light(LightShader value) {
			this.light = value;
			return this;
		}

		public Builder texture(Identifier value) {
			this.texture = value;
			return this;
		}

		public Builder blur(boolean value) {
			this.blur = value;
			return this;
		}

		public Builder mipmap(boolean value) {
			this.mipmap = value;
			return this;
		}

		public Builder backfaceCulling(boolean value) {
			this.backfaceCulling = value;
			return this;
		}

		public Builder depthStencilState(DepthStencilState value) {
			this.depthStencilState = Optional.of(value);
			return this;
		}

		public Builder colorTargetState(ColorTargetState value) {
			this.colorTargetState = Optional.of(value);
			return this;
		}

		public Builder useOverlay(boolean value) {
			this.useOverlay = value;
			return this;
		}

		public Builder useOit(boolean value) {
			this.useOit = value;
			return this;
		}

		public Builder useLight(boolean value) {
			this.useLight = value;
			return this;
		}

		/**
		 * @deprecated Use {@link #cardinalLightingMode(CardinalLightingMode)} instead.
		 */
		@Deprecated(forRemoval = true)
		public Builder diffuse(boolean value) {
			return cardinalLightingMode(value ? CardinalLightingMode.ENTITY : CardinalLightingMode.OFF);
		}

		public Builder cardinalLightingMode(CardinalLightingMode value) {
			this.cardinalLightingMode = value;
			return this;
		}

		public Builder ambientOcclusion(boolean ambientOcclusion) {
			this.ambientOcclusion = ambientOcclusion;
			return this;
		}

		@Override
		public MaterialShaders shaders() {
			return shaders;
		}

		@Override
		public FogShader fog() {
			return fog;
		}

		@Override
		public CutoutShader cutout() {
			return cutout;
		}

		@Override
		public LightShader light() {
			return light;
		}

		@Override
		public Identifier texture() {
			return texture;
		}

		@Override
		public boolean blur() {
			return blur;
		}

		@Override
		public boolean mipmap() {
			return mipmap;
		}

		@Override
		public boolean backfaceCulling() {
			return backfaceCulling;
		}

		@Override
		public Optional<DepthStencilState> depthStencilState() {
			return depthStencilState;
		}

		@Override
		public Optional<ColorTargetState> colorTargetState() {
			return colorTargetState;
		}

		@Override
		public boolean useOverlay() {
			return useOverlay;
		}

		@Override
		public boolean useOit() {
			return useOit;
		}

		@Override
		public boolean useLight() {
			return useLight;
		}

		@Override
		public CardinalLightingMode cardinalLightingMode() {
			return cardinalLightingMode;
		}

		@Override
		public boolean ambientOcclusion() {
			return ambientOcclusion;
		}

		public SimpleMaterial build() {
			return new SimpleMaterial(this);
		}
	}
}
