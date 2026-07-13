package dev.engine_room.flywheel.backend;

import java.io.IOException;

import org.jetbrains.annotations.UnknownNullability;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.textures.GpuTexture;
import com.mojang.renderpearl.backend.opengl.GlConst;
import com.mojang.renderpearl.backend.opengl.GlStateManager;
import com.mojang.renderpearl.backend.opengl.GlTexture;

import dev.engine_room.flywheel.backend.gl.GlTextureUnit;
import dev.engine_room.flywheel.lib.util.IdentifierUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;

public class NoiseTextures {
	public static final Identifier NOISE_TEXTURE = IdentifierUtil.id("textures/flywheel/noise/blue.png");

	@UnknownNullability
	public static GpuTexture BLUE_NOISE;

	public static void reload(ResourceManager manager) {
		if (BLUE_NOISE != null) {
			BLUE_NOISE.close();
			BLUE_NOISE = null;
		}
		var optional = manager.getResource(NOISE_TEXTURE);

		if (optional.isEmpty()) {
			return;
		}

		try (var is = optional.get()
				.open()) {
			var image = NativeImage.read(NativeImage.Format.LUMINANCE, is);

			BLUE_NOISE = RenderSystem.getDevice().createTexture(
					() -> "Flywheel Blue Noise",
					GpuTexture.USAGE_TEXTURE_BINDING | GpuTexture.USAGE_COPY_DST,
					GpuFormat.R8_UNORM,
					image.getWidth(),
					image.getHeight(),
					1,
					1
			);
			RenderSystem.getDevice().createCommandEncoder().writeToTexture(BLUE_NOISE, image);

			GlTextureUnit.T0.makeActive();
			GlStateManager._bindTexture(((GlTexture) BLUE_NOISE).glId());

			GlStateManager._texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_MIN_FILTER, GlConst.GL_LINEAR);
			GlStateManager._texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_MAG_FILTER, GlConst.GL_LINEAR);
			GlStateManager._texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_WRAP_S, GlConst.GL_REPEAT);
			GlStateManager._texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_WRAP_T, GlConst.GL_REPEAT);

			GlStateManager._bindTexture(0);
		} catch (IOException e) {

		}
	}
}
