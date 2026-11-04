package dev.engine_room.flywheel.backend;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import org.jetbrains.annotations.UnknownNullability;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.textures.GpuTexture;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import com.mojang.renderpearl.backend.opengl.GlConst;
import com.mojang.renderpearl.backend.opengl.GlStateManager;
import com.mojang.renderpearl.backend.opengl.GlTexture;

import dev.engine_room.flywheel.backend.b3d.DeviceFeatureCompat;
import dev.engine_room.flywheel.backend.gl.GlTextureUnit;
import dev.engine_room.flywheel.lib.util.IdentifierUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.PngInfo;

public class NoiseTextures {
	public static final Identifier NOISE_TEXTURE = IdentifierUtil.id("textures/flywheel/noise/blue.png");

	@UnknownNullability
	public static GpuTexture BLUE_NOISE;
	@UnknownNullability
	public static GpuTextureView BLUE_NOISE_VIEW;

	public static void reload(ResourceManager manager) {
		if (BLUE_NOISE != null) {
			BLUE_NOISE.close();
			BLUE_NOISE = null;
		}
		if (BLUE_NOISE_VIEW != null) {
			BLUE_NOISE_VIEW.close();
			BLUE_NOISE_VIEW = null;
		}
		var optional = manager.getResource(NOISE_TEXTURE);

		if (optional.isEmpty()) {
			return;
		}

		try (InputStream is = optional.get().open()) {
			ByteBuffer file = null;
			try {
				file = TextureUtil.readResource(is);

				if (MemoryUtil.memAddress(file) == 0L) {
					throw new IllegalArgumentException("Invalid buffer");
				}

				PngInfo.validateHeader(file);

				try (MemoryStack stack = MemoryStack.stackPush()) {
					IntBuffer w = stack.mallocInt(1);
					IntBuffer h = stack.mallocInt(1);
					IntBuffer comp = stack.mallocInt(1);
					ByteBuffer pixels = STBImage.stbi_load_from_memory(file, w, h, comp, NativeImage.Format.LUMINANCE.components());
					if (pixels == null) {
						throw new IOException("Could not load image: " + STBImage.stbi_failure_reason());
					}

					long address = MemoryUtil.memAddress(pixels);
					try (NativeImage image = new NativeImage(NativeImage.Format.LUMINANCE, w.get(0), h.get(0), true, address)) {
						BLUE_NOISE = RenderSystem.getDevice().createTexture(
								() -> "Flywheel Blue Noise",
								GpuTexture.USAGE_TEXTURE_BINDING | GpuTexture.USAGE_COPY_DST,
								GpuFormat.R8_UNORM,
								image.getWidth(),
								image.getHeight(),
								1,
								1
						);
						BLUE_NOISE_VIEW = RenderSystem.getDevice().createTextureView(BLUE_NOISE);
						RenderSystem.getDevice().createCommandEncoder().writeToTexture(BLUE_NOISE, image);
					}
				}
			} finally {
				MemoryUtil.memFree(file);
			}

			// TODO - Remove, this is only for GL Compat
			if (DeviceFeatureCompat.BACKEND_NAME.equals("OpenGL")) {
				GlTextureUnit.T0.makeActive();
				GlStateManager._bindTexture(((GlTexture) BLUE_NOISE).glId());

				GlStateManager._texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_MIN_FILTER, GlConst.GL_LINEAR);
				GlStateManager._texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_MAG_FILTER, GlConst.GL_LINEAR);
				GlStateManager._texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_WRAP_S, GlConst.GL_REPEAT);
				GlStateManager._texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_WRAP_T, GlConst.GL_REPEAT);

				GlStateManager._bindTexture(0);
			}
		} catch (IOException _) {

		}
	}
}
