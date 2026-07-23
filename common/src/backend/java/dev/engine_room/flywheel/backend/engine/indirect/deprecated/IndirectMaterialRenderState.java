package dev.engine_room.flywheel.backend.engine.indirect.deprecated;

import org.jspecify.annotations.Nullable;

import com.mojang.blaze3d.opengl.GlConst;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuSampler;

import dev.engine_room.flywheel.api.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;

@Deprecated
public class IndirectMaterialRenderState {
	public static void setup(Material material) {
		setupTexture(material);
		setupBackfaceCulling(material.backfaceCulling());
		setupPolygonOffset(material.depthStencilState());
		setupDepthTest(material.depthStencilState());
		setupTransparency(material.colorTargetState());
		setupWriteMask(material.depthStencilState(), material.colorTargetState());
	}

	public static void setupOit(Material material) {
		setupTexture(material);
		setupBackfaceCulling(material.backfaceCulling());
		setupPolygonOffset(material.depthStencilState());
		setupDepthTest(material.depthStencilState());

		ColorTargetState colorState = material.colorTargetState();
		if (colorState != null) {
			GlStateManager._colorMask(colorState.writeMask());
		}
	}

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

	private static void setupBackfaceCulling(boolean backfaceCulling) {
		if (backfaceCulling) {
			GlStateManager._enableCull();
		} else {
			GlStateManager._disableCull();
		}
	}

	private static void setupPolygonOffset(@Nullable DepthStencilState state) {
		if (state != null) {
			if (state.depthBiasConstant() == 0.0F && state.depthBiasScaleFactor() == 0.0F) {
				GlStateManager._disablePolygonOffset();
			} else {
				GlStateManager._polygonOffset(state.depthBiasScaleFactor(), state.depthBiasConstant());
				GlStateManager._enablePolygonOffset();
			}
		} else {
			GlStateManager._disablePolygonOffset();
		}
	}

	private static void setupDepthTest(DepthStencilState state) {
		if (state != null) {
			GlStateManager._enableDepthTest();
			GlStateManager._depthFunc(GlConst.toGl(state.depthTest()));
		} else {
			GlStateManager._disableDepthTest();
		}
	}

	private static void setupTransparency(ColorTargetState colorTargetState) {
		if (colorTargetState != null) {
			colorTargetState.blendFunction().ifPresentOrElse(func -> {
				GlStateManager._enableBlend(0);
				GlStateManager._blendFuncSeparate(
						GlConst.toGl(func.color().sourceFactor()),
						GlConst.toGl(func.color().destFactor()),
						GlConst.toGl(func.alpha().sourceFactor()),
						GlConst.toGl(func.alpha().destFactor())
				);
				GlStateManager._blendEquationSeparate(
						GlConst.toGl(func.color().op()),
						GlConst.toGl(func.alpha().op())
				);
			}, () -> GlStateManager._disableBlend(0));
		}
	}

	private static void setupWriteMask(@Nullable DepthStencilState depthState, @Nullable ColorTargetState colorState) {
		if (depthState != null) {
			GlStateManager._depthMask(depthState.writeDepth());
		} else {
			GlStateManager._depthMask(false);
		}

		if (colorState != null) {
			GlStateManager._colorMask(colorState.writeMask());
		}
	}
}
