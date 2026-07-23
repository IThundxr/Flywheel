package dev.engine_room.flywheel.lib.material;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.platform.CompareOp;

import dev.engine_room.flywheel.api.material.CardinalLightingMode;
import dev.engine_room.flywheel.api.material.Material;
import net.minecraft.client.renderer.feature.ItemFeatureRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;

// FIXME 1.21.11: use default blur/mipmap from AbstractTexture for all materials here, and by default
public final class Materials {
	public static final Material SOLID_BLOCK = SimpleMaterial.builder()
			.build();
	public static final Material SOLID_UNSHADED_BLOCK = SimpleMaterial.builderOf(SOLID_BLOCK)
			.cardinalLightingMode(CardinalLightingMode.OFF)
			.build();

	public static final Material CUTOUT_BLOCK = SimpleMaterial.builder()
			.cutout(CutoutShaders.HALF)
			.build();
	public static final Material CUTOUT_UNSHADED_BLOCK = SimpleMaterial.builderOf(CUTOUT_BLOCK)
			.cardinalLightingMode(CardinalLightingMode.OFF)
			.build();

	public static final Material TRANSLUCENT_BLOCK = SimpleMaterial.builder()
			.cutout(CutoutShaders.EPSILON)
			.colorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
			.useOit(true)
			.build();
	public static final Material TRANSLUCENT_UNSHADED_BLOCK = SimpleMaterial.builderOf(TRANSLUCENT_BLOCK)
			.cardinalLightingMode(CardinalLightingMode.OFF)
			.build();

	public static final Material TRIPWIRE_BLOCK = SimpleMaterial.builder()
			.cutout(CutoutShaders.ONE_TENTH)
			.colorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
			.useOit(true)
			.build();
	public static final Material TRIPWIRE_UNSHADED_BLOCK = SimpleMaterial.builderOf(TRIPWIRE_BLOCK)
			.cardinalLightingMode(CardinalLightingMode.OFF)
			.build();

	public static final Material GLINT = SimpleMaterial.builder()
			.texture(ItemFeatureRenderer.ENCHANTED_GLINT_ITEM)
			.shaders(StandardMaterialShaders.GLINT)
			.colorTargetState(new ColorTargetState(BlendFunction.GLINT))
			.depthStencilState(new DepthStencilState(CompareOp.EQUAL, false))
			.backfaceCulling(false)
			.build();

	// FIXME 1.21.11: missing TextureTransform.ENTITY_GLINT_TEXTURING
	public static final Material GLINT_ENTITY = SimpleMaterial.builderOf(GLINT)
			.build();

	public static final Material TRANSLUCENT_ITEM_ENTITY_BLOCK = SimpleMaterial.builder()
			.colorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
			.build();

	public static final Material TRANSLUCENT_ITEM_ENTITY_ITEM = SimpleMaterial.builder()
			.texture(TextureAtlas.LOCATION_ITEMS)
			.colorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
			.build();

	private Materials() {
	}
}
