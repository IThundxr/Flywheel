package dev.engine_room.flywheel.backend.engine;

import java.util.Collections;

import org.jspecify.annotations.Nullable;
import org.lwjgl.opengl.GL33C;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.textures.FilterMode;
import com.mojang.renderpearl.api.textures.GpuSampler;
import com.mojang.renderpearl.api.textures.GpuTexture;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import com.mojang.renderpearl.backend.opengl.GlConst;
import com.mojang.renderpearl.backend.opengl.GlStateManager;
import com.mojang.renderpearl.backend.opengl.GlTextureView;

import dev.engine_room.flywheel.backend.Samplers;
import dev.engine_room.flywheel.backend.gl.GlUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

public class TextureBinder {
	public static void bind(int unit, Identifier textureId, @Nullable GpuSampler sampler) {
		GpuTextureView texture = Minecraft.getInstance()
				.getTextureManager()
				.getTexture(textureId)
				.textureView();

		bind(unit, texture, sampler);
	}

	// Taken from GlCommandEncoder.trySetup
	public static void bind(int unit, GpuTextureView textureView, @Nullable GpuSampler sampler) {
		GlStateManager stateManager = GlUtil.getStateManager();

		stateManager._activeTexture(GlConst.GL_TEXTURE0 + unit);
		GpuTexture texture = textureView.texture();
		int textureId = GlUtil.getHandle(texture);
		int i;
		if ((texture.usage() & GpuTexture.USAGE_CUBEMAP_COMPATIBLE) != 0) {
			i = GL33C.GL_TEXTURE_CUBE_MAP;
			GL33C.glBindTexture(i, textureId);
		} else {
			i = GlConst.GL_TEXTURE_2D;
			stateManager._bindTexture(textureId);
		}

		if (sampler != null) {
			GL33C.glBindSampler(unit, GlUtil.getHandle(sampler));
			GL33C.glTexParameteri(i, GL33C.GL_TEXTURE_BASE_LEVEL, textureView.baseMipLevel());
			GL33C.glTexParameteri(i, GL33C.GL_TEXTURE_MAX_LEVEL, textureView.baseMipLevel() + textureView.mipLevels() - 1);
		}
	}

	public static void bindLightAndOverlay() {
		var gameRenderer = Minecraft.getInstance().gameRenderer;
		GpuSampler sampler = RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR);
		bind(Samplers.OVERLAY.number, gameRenderer.overlayTexture().getTextureView(), sampler);
		bind(Samplers.LIGHT.number, gameRenderer.lightmap(), sampler);
	}

	public static void bindRenderTarget(RenderTarget target) {
		GpuTexture colorTexture = target.getColorTexture();
		GpuTexture depthTexture = target.getDepthTexture();
		int i = GlUtil.getFramebufferCache().getFbo(
				GlUtil.getDirectStateAccess(),
				Collections.singletonList((GlTextureView) colorTexture),
				(GlTextureView) depthTexture
		);
		GlUtil.getStateManager()._glBindFramebuffer(GlConst.GL_FRAMEBUFFER, i);
	}
}
