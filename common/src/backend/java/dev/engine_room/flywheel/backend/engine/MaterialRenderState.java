package dev.engine_room.flywheel.backend.engine;

import java.util.Comparator;

import org.jspecify.annotations.Nullable;
import org.lwjgl.opengl.GL11;

import com.google.common.collect.Comparators;
import com.mojang.blaze3d.opengl.GlConst;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuSampler;

import dev.engine_room.flywheel.api.material.DepthTest;
import dev.engine_room.flywheel.api.material.Material;
import dev.engine_room.flywheel.api.material.Transparency;
import dev.engine_room.flywheel.api.material.WriteMask;
import dev.engine_room.flywheel.backend.Samplers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;

public final class MaterialRenderState {
	public static final Comparator<Material> COMPARATOR = MaterialRenderState::compare;

	private MaterialRenderState() {
	}

	// TODO b3d-ification: This is a pretty slim method, texture binding should be handled in the pass
	// I guess we could move setupTextureForRenderPass into TextureBinder or some other util class
	// Or we could have a builder that takes in an identifier and other params and binds the texture with a created sampler
	public static void setupForRenderPass(RenderPass renderPass, Material material) {
		setupTextureForRenderPass(renderPass, material);
		// 		setupBackfaceCulling(material.backfaceCulling()); // Should be handled by RenderPipeline
		//		setupPolygonOffset(material.polygonOffset()); // Should be handled by RenderPipeline
		//		setupDepthTest(material.depthTest()); // Should be handled by RenderPipeline
		//		setupTransparency(material.transparency()); // Should be handled by RenderPipeline
		//		setupWriteMask(material.writeMask()); // Should be handled by RenderPipeline
	}

//	@Deprecated(forRemoval = true)
//	public static void setup(Material material) {
//		setupTexture(material);
//		setupBackfaceCulling(material.backfaceCulling());
//		setupPolygonOffset(material.polygonOffset());
//		setupDepthTest(material.depthTest());
//		setupTransparency(material.transparency());
//		setupWriteMask(material.writeMask());
//	}
//
//	@Deprecated(forRemoval = true)
//	public static void setupOit(Material material) {
//		setupTexture(material);
//		setupBackfaceCulling(material.backfaceCulling());
//		setupPolygonOffset(material.polygonOffset());
//		setupDepthTest(material.depthTest());
//		setupWriteMask(material.writeMask());
//	}

	private static void setupTextureForRenderPass(RenderPass renderPass, Material material) {
		AbstractTexture texture = Minecraft.getInstance()
				.getTextureManager()
				.getTexture(material.texture());

		// TODO 1.21.11: give the Material more control, such as using default filter mode, address modes, AF, max LOD?
		FilterMode filterMode = material.blur() ? FilterMode.LINEAR : FilterMode.NEAREST;
		GpuSampler defaultSampler = texture.getSampler();
		GpuSampler sampler = RenderSystem.getSamplerCache()
				.getSampler(defaultSampler.getAddressModeU(), defaultSampler.getAddressModeV(), filterMode, filterMode, material.mipmap());

		renderPass.bindTexture("Sampler0", texture.getTextureView(), sampler);
	}

	@Deprecated(forRemoval = true)
	private static void setupTexture(Material material) {
		AbstractTexture texture = Minecraft.getInstance()
				.getTextureManager()
				.getTexture(material.texture());

		// TODO 1.21.11: give the Material more control, such as using default filter mode, address modes, AF, max LOD?
		FilterMode filterMode = material.blur() ? FilterMode.LINEAR : FilterMode.NEAREST;
		GpuSampler defaultSampler = texture.getSampler();
		GpuSampler sampler = RenderSystem.getSamplerCache()
				.getSampler(defaultSampler.getAddressModeU(), defaultSampler.getAddressModeV(), filterMode, filterMode, material.mipmap());

		/// TODO 1.21.11: should cubemap textures be allowed?
		TextureBinder.bind(Samplers.DIFFUSE.number, texture.getTextureView(), sampler);
	}

	@Deprecated(forRemoval = true)
	private static void setupBackfaceCulling(boolean backfaceCulling) {
		if (backfaceCulling) {
			GlStateManager._enableCull();
		} else {
			GlStateManager._disableCull();
		}
	}

	@Deprecated(forRemoval = true)
	private static void setupPolygonOffset(boolean polygonOffset) {
		if (polygonOffset) {
			GlStateManager._polygonOffset(0.1F, 1.0F);
			GlStateManager._enablePolygonOffset();
		} else {
			GlStateManager._polygonOffset(0.0F, 0.0F);
			GlStateManager._disablePolygonOffset();
		}
	}

	@Deprecated(forRemoval = true)
	private static void setupDepthTest(DepthTest depthTest) {
		switch (depthTest) {
		case OFF -> {
			GlStateManager._disableDepthTest();
		}
		case NEVER -> {
			GlStateManager._enableDepthTest();
			GlStateManager._depthFunc(GL11.GL_NEVER);
		}
		case LESS -> {
			GlStateManager._enableDepthTest();
			GlStateManager._depthFunc(GlConst.GL_LESS);
		}
		case EQUAL -> {
			GlStateManager._enableDepthTest();
			GlStateManager._depthFunc(GlConst.GL_EQUAL);
		}
		case LEQUAL -> {
			GlStateManager._enableDepthTest();
			GlStateManager._depthFunc(GlConst.GL_LEQUAL);
		}
		case GREATER -> {
			GlStateManager._enableDepthTest();
			GlStateManager._depthFunc(GlConst.GL_GREATER);
		}
		case NOTEQUAL -> {
			GlStateManager._enableDepthTest();
			GlStateManager._depthFunc(GL11.GL_NOTEQUAL);
		}
		case GEQUAL -> {
			GlStateManager._enableDepthTest();
			GlStateManager._depthFunc(GlConst.GL_GEQUAL);
		}
		case ALWAYS -> {
			GlStateManager._enableDepthTest();
			GlStateManager._depthFunc(GlConst.GL_ALWAYS);
		}
		}
	}

	@Deprecated(forRemoval = true)
	private static void setupTransparency(Transparency transparency) {
		switch (transparency) {
		case OPAQUE -> {
			GlStateManager._disableBlend(0);
		}
		case ADDITIVE -> {
			GlStateManager._enableBlend(0);
			GlStateManager._blendFuncSeparate(GlConst.GL_ONE, GlConst.GL_ONE, GlConst.GL_ONE, GlConst.GL_ONE);
		}
		case LIGHTNING -> {
			GlStateManager._enableBlend(0);
			GlStateManager._blendFuncSeparate(GlConst.GL_SRC_ALPHA, GlConst.GL_ONE, GlConst.GL_SRC_ALPHA, GlConst.GL_ONE);
		}
		case GLINT -> {
			GlStateManager._enableBlend(0);
			GlStateManager._blendFuncSeparate(GlConst.GL_SRC_COLOR, GlConst.GL_ONE, GlConst.GL_ZERO, GlConst.GL_ONE);
		}
		case CRUMBLING -> {
			GlStateManager._enableBlend(0);
			GlStateManager._blendFuncSeparate(GlConst.GL_DST_COLOR, GlConst.GL_SRC_COLOR, GlConst.GL_ONE, GlConst.GL_ZERO);
		}
		case TRANSLUCENT -> {
			GlStateManager._enableBlend(0);
			GlStateManager._blendFuncSeparate(GlConst.GL_SRC_ALPHA, GlConst.GL_ONE_MINUS_SRC_ALPHA, GlConst.GL_ONE, GlConst.GL_ONE_MINUS_SRC_ALPHA);
		}
		}
	}

	@Deprecated(forRemoval = true)
	private static void setupWriteMask(WriteMask mask) {
		GlStateManager._depthMask(mask.depth());
		boolean writeColor = mask.color();
		GlStateManager._colorMask(writeColor ? ColorTargetState.WRITE_ALL : ColorTargetState.WRITE_NONE);
	}

	public static boolean materialEquals(Material lhs, Material rhs) {
		if (lhs == rhs) {
			return true;
		}

		// Not here because ubershader: useLight, useOverlay, diffuse, fog shader, ambient occlusion
		// Everything in the comparator should be here.
		// @formatter:off
		return lhs.blur() == rhs.blur()
				&& lhs.mipmap() == rhs.mipmap()
				&& lhs.backfaceCulling() == rhs.backfaceCulling()
				&& lhs.depthStencilState().orElse(null) == rhs.depthStencilState().orElse(null)
				&& lhs.useOit() == rhs.useOit()
				&& lhs.light().source().equals(rhs.light().source())
				&& lhs.texture().equals(rhs.texture())
				&& lhs.cutout().source().equals(rhs.cutout().source())
				&& lhs.shaders().fragmentSource().equals(rhs.shaders().fragmentSource())
				&& lhs.shaders().vertexSource().equals(rhs.shaders().vertexSource());
		// @formatter:on
	}

	public static boolean materialIsAllNonNull(@Nullable Material material) {
		// We do not trust people to give us valid NotNull objects.
		// @formatter:off
		return material != null &&
				material.shaders() != null &&
				material.shaders().fragmentSource() != null &&
				material.shaders().vertexSource() != null &&
				material.fog() != null &&
				material.fog().source() != null &&
				material.cutout() != null &&
				material.cutout().source() != null &&
				material.light() != null &&
				material.light().source() != null &&
				material.texture() != null &&
				material.cardinalLightingMode() != null;
		// @formatter:on
	}

	public static int compare(Material lhs, Material rhs) {
		if (lhs == rhs) {
			return 0;
		}

		int cmp;
		cmp = lhs.light()
				.source()
				.compareTo(rhs.light()
						.source());
		if (cmp != 0) {
			return cmp;
		}
		cmp = lhs.cutout()
				.source()
				.compareTo(rhs.cutout()
						.source());
		if (cmp != 0) {
			return cmp;
		}
		cmp = lhs.shaders()
				.fragmentSource()
				.compareTo(rhs.shaders()
						.fragmentSource());
		if (cmp != 0) {
			return cmp;
		}
		cmp = lhs.shaders()
				.vertexSource()
				.compareTo(rhs.shaders()
						.vertexSource());
		if (cmp != 0) {
			return cmp;
		}
		cmp = lhs.texture()
				.compareTo(rhs.texture());
		if (cmp != 0) {
			return cmp;
		}
		cmp = Boolean.compare(lhs.blur(), rhs.blur());
		if (cmp != 0) {
			return cmp;
		}
		cmp = Boolean.compare(lhs.mipmap(), rhs.mipmap());
		if (cmp != 0) {
			return cmp;
		}
		cmp = Boolean.compare(lhs.backfaceCulling(), rhs.backfaceCulling());
		if (cmp != 0) {
			return cmp;
		}
		cmp = Comparators.emptiesFirst(Comparator.comparing(DepthStencilState::depthTest)
					.thenComparing(DepthStencilState::writeDepth)
					.thenComparing(DepthStencilState::depthBiasScaleFactor)
					.thenComparing(DepthStencilState::depthBiasConstant))
				.compare(lhs.depthStencilState(), rhs.depthStencilState());
		if (cmp != 0) {
			return cmp;
		}
		cmp = Boolean.compare(lhs.useOit(), rhs.useOit());
		if (cmp != 0) {
			return cmp;
		}
		return 0;
	}
}
