package dev.engine_room.flywheel.backend.engine.indirect;

import java.util.Collections;
import java.util.function.Supplier;

import org.jspecify.annotations.Nullable;
import org.lwjgl.opengl.GL32;
import org.lwjgl.opengl.GL32C;
import org.lwjgl.opengl.GL33C;
import org.lwjgl.opengl.GL45C;
import org.lwjgl.opengl.GL46;

import com.mojang.blaze3d.GpuFormat;
import com.mojang.blaze3d.opengl.GlConst;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.opengl.GlTexture;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderPassDescriptor;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuSampler;
import com.mojang.blaze3d.textures.GpuTextureView;

import dev.engine_room.flywheel.backend.NoiseTextures;
import dev.engine_room.flywheel.backend.Samplers;
import dev.engine_room.flywheel.backend.compile.OitPrograms;
import dev.engine_room.flywheel.backend.engine.TextureBinder;
import dev.engine_room.flywheel.backend.gl.GlCompat;
import dev.engine_room.flywheel.backend.gl.GlTextureUnit;
import dev.engine_room.flywheel.backend.gl.GlUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;

public class OitFramebuffer implements AutoCloseable {
	public static final float[] CLEAR_TO_ZERO = {0, 0, 0, 0};
	public static final int[] DEPTH_RANGE_DRAW_BUFFERS = {GlConst.GL_COLOR_ATTACHMENT0};
	public static final int[] RENDER_TRANSMITTANCE_DRAW_BUFFERS = {GL46.GL_COLOR_ATTACHMENT1, GL46.GL_COLOR_ATTACHMENT2, GL46.GL_COLOR_ATTACHMENT3, GL46.GL_COLOR_ATTACHMENT4};
	public static final int[] ACCUMULATE_DRAW_BUFFERS = {GL46.GL_COLOR_ATTACHMENT5};
	public static final int[] DEPTH_ONLY_DRAW_BUFFERS = {};

	private final OitPrograms programs;
	@Deprecated(forRemoval = true)
	private final int vao;

	@Deprecated(forRemoval = true)
	public int fbo = -1;
	@Deprecated(forRemoval = true)
	public int depthBounds = -1;
	@Deprecated(forRemoval = true)
	public int coefficients = -1;
	@Deprecated(forRemoval = true)
	public int accumulate = -1;

	@Nullable
	public GpuTextureView depthBoundsB3D = null;
	@Nullable
	public GpuTextureView[] coefficientsB3D = null;
	@Nullable
	public GpuTextureView accumulateB3D = null;

	private int lastWidth = -1;
	private int lastHeight = -1;

	@Deprecated(forRemoval = true)
	public OitFramebuffer(OitPrograms programs) {
		this.programs = programs;
		if (GlCompat.SUPPORTS_DSA) {
			vao = GL45C.glCreateVertexArrays();
		} else {
			vao = GlStateManager._glGenVertexArrays();
		}
	}

	// TODO b3d-ification: Temp
	public OitFramebuffer(OitPrograms programs, boolean isNew) {
		this.programs = programs;
		vao = -1;
		throw new UnsupportedOperationException();
	}

	/**
	 * Set up the framebuffer.
	 */
	@Deprecated(forRemoval = true)
	public void prepare() {
		RenderTarget renderTarget;

		if (Minecraft.getInstance().gameRenderer.gameRenderState().useShaderTransparency()) {
			renderTarget = Minecraft.getInstance().levelRenderer.itemEntityTarget();

			renderTarget.copyDepthFrom(Minecraft.getInstance().gameRenderer.mainRenderTarget());
		} else {
			renderTarget = Minecraft.getInstance().gameRenderer.mainRenderTarget();
		}

		maybeResizeFBO(renderTarget.width, renderTarget.height);

		Samplers.COEFFICIENTS.makeActive();
		// Bind zero to state manager to make sure we clear its internal state
		GlStateManager._bindTexture(0);
		GL33C.glBindTexture(GL33C.GL_TEXTURE_2D_ARRAY, coefficients);

		Samplers.DEPTH_RANGE.makeActive();
		GlStateManager._bindTexture(depthBounds);

		TextureBinder.bind(Samplers.NOISE.number, NoiseTextures.BLUE_NOISE.getTextureView(), null);

		GlStateManager._glBindFramebuffer(GlConst.GL_FRAMEBUFFER, fbo);
		GlTexture depthTexture = (GlTexture) renderTarget.getDepthTexture();
		GL33C.glFramebufferTexture(GlConst.GL_FRAMEBUFFER, GlConst.GL_DEPTH_ATTACHMENT, depthTexture != null ? depthTexture.glId() : 0, 0);
	}

	/**
	 * Set up the framebuffer.
	 */
	public RenderPass prepareB3D(Supplier<String> label) {
		Minecraft minecraft = Minecraft.getInstance();
		GameRenderer gameRenderer = minecraft.gameRenderer;

		RenderTarget renderTarget;
		if (gameRenderer.gameRenderState().useShaderTransparency()) {
			renderTarget = minecraft.levelRenderer.itemEntityTarget();

			renderTarget.copyDepthFrom(gameRenderer.mainRenderTarget());
		} else {
			renderTarget = gameRenderer.mainRenderTarget();
		}

		int width = renderTarget.width;
		int height = renderTarget.height;

		maybeResizeFBOB3D(width, height);

		RenderPassDescriptor descriptor = RenderPassDescriptor.create(label)
				.withRenderArea(new RenderPass.RenderArea(0, 0, width, height))
				.withColorAttachment(depthBoundsB3D) // GL_COLOR_ATTACHMENT0, depthBounds
				.withColorAttachment(null) // GL_COLOR_ATTACHMENT1, coefficients, layer 0
				.withColorAttachment(null) // GL_COLOR_ATTACHMENT2, coefficients, layer 1
				.withColorAttachment(null) // GL_COLOR_ATTACHMENT3, coefficients, layer 2
				.withColorAttachment(null) // GL_COLOR_ATTACHMENT4, coefficients, layer 3
				.withColorAttachment(accumulateB3D) // GL_COLOR_ATTACHMENT5, accumulate
				.withDepthAttachment(renderTarget.getDepthTextureView());

		RenderPass renderPass = RenderSystem.getDevice()
				.createCommandEncoder()
				.createRenderPass(descriptor);

		GpuSampler clampToEdgeNearest = RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST);

		// TODO b3d-ification: Not sure if the bindTexture(0) is still needed
//		Samplers.COEFFICIENTS.makeActive();
//		// Bind zero to state manager to make sure we clear its internal state
//		GlStateManager._bindTexture(0);
//		GL32.glBindTexture(GL32.GL_TEXTURE_2D_ARRAY, coefficients);

		// TODO b3d-ification: This is a TEXTURE_2D_ARRAY so we need support for binding those
		renderPass.bindTexture("_flw_coefficients", coefficientsB3D, clampToEdgeNearest);

		renderPass.bindTexture("_flw_depthRange", depthBoundsB3D, clampToEdgeNearest);

		renderPass.bindTexture("_flw_blueNoise", NoiseTextures.BLUE_NOISE.getTextureView(), NoiseTextures.BLUE_NOISE.getSampler());

		return renderPass;
	}

	/**
	 * Render out the min and max depth per fragment.
	 */
	public void depthRange() {
		// No depth writes, but we'll still use the depth test.
		GlStateManager._depthMask(false);
		GlStateManager._colorMask(ColorTargetState.WRITE_ALL);
		GlStateManager._enableBlend(0);
		GlStateManager._blendFuncSeparate(GlConst.GL_ONE, GlConst.GL_ONE, GlConst.GL_ONE, GlConst.GL_ONE);
		GlStateManager._blendEquationSeparate(GlConst.GL_MAX, GlConst.GL_MAX);

		float far = Minecraft.getInstance().gameRenderer.gameRenderState().levelRenderState.cameraRenderState.depthFar;

		if (GlCompat.SUPPORTS_DSA) {
			GL45C.glNamedFramebufferDrawBuffers(fbo, DEPTH_RANGE_DRAW_BUFFERS);
			GL45C.glClearNamedFramebufferfv(fbo, GL33C.GL_COLOR, 0, new float[]{-far, -far, 0, 0});
		} else {
			GL33C.glDrawBuffers(DEPTH_RANGE_DRAW_BUFFERS);
			GL33C.glClearColor(-far, -far, 0, 0);
			GlStateManager._clear(GlConst.GL_COLOR_BUFFER_BIT);
		}
	}

	/**
	 * Generate the coefficients to the transmittance function.
	 */
	public void renderTransmittance() {
		// No depth writes, but we'll still use the depth test
		GlStateManager._depthMask(false);
		GlStateManager._colorMask(ColorTargetState.WRITE_ALL);
		GlStateManager._enableBlend(0);
		GlStateManager._blendFuncSeparate(GlConst.GL_ONE, GlConst.GL_ONE, GlConst.GL_ONE, GlConst.GL_ONE);
		GlStateManager._blendEquationSeparate(GlConst.GL_FUNC_ADD, GlConst.GL_FUNC_ADD);

		if (GlCompat.SUPPORTS_DSA) {
			GL45C.glNamedFramebufferDrawBuffers(fbo, RENDER_TRANSMITTANCE_DRAW_BUFFERS);

			GL45C.glClearNamedFramebufferfv(fbo, GL33C.GL_COLOR, 0, CLEAR_TO_ZERO);
			GL45C.glClearNamedFramebufferfv(fbo, GL33C.GL_COLOR, 1, CLEAR_TO_ZERO);
			GL45C.glClearNamedFramebufferfv(fbo, GL33C.GL_COLOR, 2, CLEAR_TO_ZERO);
			GL45C.glClearNamedFramebufferfv(fbo, GL33C.GL_COLOR, 3, CLEAR_TO_ZERO);
		} else {
			GL33C.glDrawBuffers(RENDER_TRANSMITTANCE_DRAW_BUFFERS);
			GL33C.glClearColor(0, 0, 0, 0);
			GlStateManager._clear(GlConst.GL_COLOR_BUFFER_BIT);
		}
	}

	/**
	 * If any fragment has its transmittance fall off to zero, search the transmittance
	 * function to determine at what depth that occurs and write out to the depth buffer.
	 */
	public void renderDepthFromTransmittance() {
		// Only write to depth, not color.
		GlStateManager._depthMask(true);
		GlStateManager._colorMask(ColorTargetState.WRITE_NONE);
		GlStateManager._disableBlend(0);
		GlStateManager._depthFunc(GlConst.GL_ALWAYS);

		if (GlCompat.SUPPORTS_DSA) {
			GL45C.glNamedFramebufferDrawBuffers(fbo, DEPTH_ONLY_DRAW_BUFFERS);
		} else {
			GL33C.glDrawBuffers(DEPTH_ONLY_DRAW_BUFFERS);
		}

		programs.getOitDepthProgram().bind();

		drawFullscreenQuad();
	}

	/**
	 * Sample the transmittance function and accumulate.
	 */
	public void accumulate() {
		// No depth writes, but we'll still use the depth test
		GlStateManager._depthMask(false);
		GlStateManager._colorMask(ColorTargetState.WRITE_ALL);
		GlStateManager._enableBlend(0);
		GlStateManager._blendFuncSeparate(GlConst.GL_ONE, GlConst.GL_ONE, GlConst.GL_ONE, GlConst.GL_ONE);
		GlStateManager._blendEquationSeparate(GlConst.GL_FUNC_ADD, GlConst.GL_FUNC_ADD);

		if (GlCompat.SUPPORTS_DSA) {
			GL45C.glNamedFramebufferDrawBuffers(fbo, ACCUMULATE_DRAW_BUFFERS);

			GL45C.glClearNamedFramebufferfv(fbo, GL33C.GL_COLOR, 0, CLEAR_TO_ZERO);
		} else {
			GL32C.glDrawBuffers(ACCUMULATE_DRAW_BUFFERS);
			GL32C.glClearColor(0, 0, 0, 0);
			GlStateManager._clear(GlConst.GL_COLOR_BUFFER_BIT);
		}
	}

	/**
	 * Composite the accumulated luminance onto the main framebuffer.
	 */
	public void composite() {
		if (Minecraft.getInstance().gameRenderer.gameRenderState().useShaderTransparency()) {
			bindRenderTarget(Minecraft.getInstance().levelRenderer.itemEntityTarget());
		} else {
			bindRenderTarget(Minecraft.getInstance().gameRenderer.mainRenderTarget());
		}

		// The composite shader writes out the closest depth to gl_FragDepth.
		// depthMask = true: OIT stuff renders on top of other transparent stuff.
		// depthMask = false: other transparent stuff renders on top of OIT stuff.
		// If Neo gets wavelet OIT we can use their hooks to be correct with everything.
		GlStateManager._depthMask(true);
		GlStateManager._colorMask(ColorTargetState.WRITE_ALL);
		GlStateManager._enableBlend(0);

		// We rely on the blend func to achieve:
		// final color = (1 - transmittance_total) * sum(color_f * alpha_f * transmittance_f) / sum(alpha_f * transmittance_f)
		//			+ color_dst * transmittance_total
		//
		// Though note that the alpha value we emit in the fragment shader is actually (1. - transmittance_total).
		// The extra inversion step is so we can have a sane alpha value written out for the fabulous blit shader to consume.
		GlStateManager._blendFuncSeparate(GlConst.GL_SRC_ALPHA, GlConst.GL_ONE_MINUS_SRC_ALPHA, GlConst.GL_ONE, GlConst.GL_ONE_MINUS_SRC_ALPHA);
		GlStateManager._blendEquationSeparate(GlConst.GL_FUNC_ADD, GlConst.GL_FUNC_ADD);
		GlStateManager._depthFunc(GlConst.GL_ALWAYS);

		GlTextureUnit.T0.makeActive();
		GlStateManager._bindTexture(accumulate);

		programs.getOitCompositeProgram().bind();

		drawFullscreenQuad();

		bindRenderTarget(Minecraft.getInstance().gameRenderer.mainRenderTarget());
	}

	@Deprecated(forRemoval = true)
	private static void bindRenderTarget(RenderTarget target) {
		GlTexture colorTexture = (GlTexture) target.getColorTexture();
		GlTexture depthTexture = (GlTexture) target.getDepthTexture();
		int i = GlUtil.getGlDevice().frameBufferCache().getFbo(
				GlUtil.getGlDevice().directStateAccess(),
				Collections.singletonList(colorTexture),
				depthTexture
		);
		GlStateManager._glBindFramebuffer(GlConst.GL_FRAMEBUFFER, i);
		GlStateManager._viewport(0, 0, colorTexture.getWidth(0), colorTexture.getHeight(0));
	}

	@Deprecated(forRemoval = true)
	private void drawFullscreenQuad() {
		// Empty VAO, the actual full screen triangle is generated in the vertex shader
		GlStateManager._glBindVertexArray(vao);

		GlStateManager._drawArrays(GlConst.GL_TRIANGLES, 0, 3);
	}

	// TODO b3d-ification: Would be nice if RenderPass had a normal draw method, need to ask for that to be added
	private void drawFullScreenQuadB3D(RenderPass renderPass) {
		// TODO b3d-ification: We need to pass a empty buffer to renderpass so it uses a clean vertexarray
		// Empty VAO, the actual full screen triangle is generated in the vertex shader
		//GlStateManager._glBindVertexArray(vao);

		renderPass.draw(3, 1, 0, 0);
	}

	@Deprecated(forRemoval = true)
	private void maybeResizeFBO(int width, int height) {
		if (lastWidth == width && lastHeight == height) {
			return;
		}

		lastWidth = width;
		lastHeight = height;

		deleteTextures();

		if (GlCompat.SUPPORTS_DSA) {
			fbo = GL46.glCreateFramebuffers();

			depthBounds = GL46.glCreateTextures(GL46.GL_TEXTURE_2D);
			coefficients = GL46.glCreateTextures(GL46.GL_TEXTURE_2D_ARRAY);
			accumulate = GL46.glCreateTextures(GL46.GL_TEXTURE_2D);

			GL46.glTextureStorage2D(depthBounds, 1, GL32.GL_RG32F, width, height);
			GL46.glTextureStorage3D(coefficients, 1, GL32.GL_RGBA16F, width, height, 4);
			GL46.glTextureStorage2D(accumulate, 1, GL32.GL_RGBA16F, width, height);

			GL46.glNamedFramebufferTexture(fbo, GlConst.GL_COLOR_ATTACHMENT0, depthBounds, 0);
			GL46.glNamedFramebufferTextureLayer(fbo, GL32.GL_COLOR_ATTACHMENT1, coefficients, 0, 0);
			GL46.glNamedFramebufferTextureLayer(fbo, GL32.GL_COLOR_ATTACHMENT2, coefficients, 0, 1);
			GL46.glNamedFramebufferTextureLayer(fbo, GL32.GL_COLOR_ATTACHMENT3, coefficients, 0, 2);
			GL46.glNamedFramebufferTextureLayer(fbo, GL32.GL_COLOR_ATTACHMENT4, coefficients, 0, 3);
			GL46.glNamedFramebufferTexture(fbo, GL32.GL_COLOR_ATTACHMENT5, accumulate, 0);
		} else {
			fbo = GL46.glGenFramebuffers();

			depthBounds = GlStateManager._genTexture();
			coefficients = GlStateManager._genTexture();
			accumulate = GlStateManager._genTexture();

			GlTextureUnit.T0.makeActive();
			GlStateManager._bindTexture(0);

			GlStateManager._bindTexture(depthBounds);
			GL32.glTexImage2D(GlConst.GL_TEXTURE_2D, 0, GL32.GL_RG32F, width, height, 0, GL46.GL_RGBA, GL46.GL_BYTE, 0);

			GlStateManager._texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_MIN_FILTER, GlConst.GL_NEAREST);
			GlStateManager._texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_MAG_FILTER, GlConst.GL_NEAREST);
			GlStateManager._texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_WRAP_S, GlConst.GL_CLAMP_TO_EDGE);
			GlStateManager._texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_WRAP_T, GlConst.GL_CLAMP_TO_EDGE);

			GL32.glBindTexture(GL32.GL_TEXTURE_2D_ARRAY, coefficients);
			GL32.glTexImage3D(GL32.GL_TEXTURE_2D_ARRAY, 0, GL32.GL_RGBA16F, width, height, 4, 0, GL46.GL_RGBA, GL46.GL_BYTE, 0);

			GlStateManager._texParameter(GL32.GL_TEXTURE_2D_ARRAY, GlConst.GL_TEXTURE_MIN_FILTER, GlConst.GL_NEAREST);
			GlStateManager._texParameter(GL32.GL_TEXTURE_2D_ARRAY, GlConst.GL_TEXTURE_MAG_FILTER, GlConst.GL_NEAREST);
			GlStateManager._texParameter(GL32.GL_TEXTURE_2D_ARRAY, GlConst.GL_TEXTURE_WRAP_S, GlConst.GL_CLAMP_TO_EDGE);
			GlStateManager._texParameter(GL32.GL_TEXTURE_2D_ARRAY, GlConst.GL_TEXTURE_WRAP_T, GlConst.GL_CLAMP_TO_EDGE);

			GlStateManager._bindTexture(accumulate);
			GL32.glTexImage2D(GlConst.GL_TEXTURE_2D, 0, GL32.GL_RGBA16F, width, height, 0, GL46.GL_RGBA, GL46.GL_BYTE, 0);

			GlStateManager._texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_MIN_FILTER, GlConst.GL_NEAREST);
			GlStateManager._texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_MAG_FILTER, GlConst.GL_NEAREST);
			GlStateManager._texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_WRAP_S, GlConst.GL_CLAMP_TO_EDGE);
			GlStateManager._texParameter(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_WRAP_T, GlConst.GL_CLAMP_TO_EDGE);

			GlStateManager._glBindFramebuffer(GlConst.GL_FRAMEBUFFER, fbo);

			GL46.glFramebufferTexture(GlConst.GL_FRAMEBUFFER, GlConst.GL_COLOR_ATTACHMENT0, depthBounds, 0);
			GL46.glFramebufferTextureLayer(GlConst.GL_FRAMEBUFFER, GL32.GL_COLOR_ATTACHMENT1, coefficients, 0, 0);
			GL46.glFramebufferTextureLayer(GlConst.GL_FRAMEBUFFER, GL32.GL_COLOR_ATTACHMENT2, coefficients, 0, 1);
			GL46.glFramebufferTextureLayer(GlConst.GL_FRAMEBUFFER, GL32.GL_COLOR_ATTACHMENT3, coefficients, 0, 2);
			GL46.glFramebufferTextureLayer(GlConst.GL_FRAMEBUFFER, GL32.GL_COLOR_ATTACHMENT4, coefficients, 0, 3);
			GL46.glFramebufferTexture(GlConst.GL_FRAMEBUFFER, GL32.GL_COLOR_ATTACHMENT5, accumulate, 0);
		}
	}

	private void maybeResizeFBOB3D(int width, int height) {
		if (lastWidth == width && lastHeight == height) {
			return;
		}

		lastWidth = width;
		lastHeight = height;

		deleteTexturesB3D();

		if (GlCompat.SUPPORTS_DSA) {
			fbo = GL45C.glCreateFramebuffers();

			depthBounds = GL45C.glCreateTextures(GL45C.GL_TEXTURE_2D);
			coefficients = GL45C.glCreateTextures(GL45C.GL_TEXTURE_2D_ARRAY);
			accumulate = GL45C.glCreateTextures(GL45C.GL_TEXTURE_2D);

			GL45C.glTextureStorage2D(depthBounds, 1, GL32.GL_RG32F, width, height);
			GL45C.glTextureStorage3D(coefficients, 1, GL32.GL_RGBA16F, width, height, 4);
			GL45C.glTextureStorage2D(accumulate, 1, GL32.GL_RGBA16F, width, height);

			GL45C.glNamedFramebufferTexture(fbo, GlConst.GL_COLOR_ATTACHMENT0, depthBounds, 0);
			GL45C.glNamedFramebufferTextureLayer(fbo, GL32.GL_COLOR_ATTACHMENT1, coefficients, 0, 0);
			GL45C.glNamedFramebufferTextureLayer(fbo, GL32.GL_COLOR_ATTACHMENT2, coefficients, 0, 1);
			GL45C.glNamedFramebufferTextureLayer(fbo, GL32.GL_COLOR_ATTACHMENT3, coefficients, 0, 2);
			GL45C.glNamedFramebufferTextureLayer(fbo, GL32.GL_COLOR_ATTACHMENT4, coefficients, 0, 3);
			GL45C.glNamedFramebufferTexture(fbo, GL32.GL_COLOR_ATTACHMENT5, accumulate, 0);
		} else {
			GpuDevice device = RenderSystem.getDevice();

			depthBoundsB3D = device.createTextureView(device.createTexture(
					"Flw OIT Depth Bounds",
					0,
					GpuFormat.RG32_FLOAT,
					width,
					height,
					0,
					0
			));

			for (int i = 0; i < 4; i++) {
				coefficientsB3D[i] = device.createTextureView(device.createTexture(
						"Flw OIT Coefficients #" + i,
						0,
						GpuFormat.RGBA16_FLOAT,
						width,
						height,
						0,
						0
				));
			}

			accumulateB3D = device.createTextureView(device.createTexture(
					"Flw OIT Accumulate",
					0,
					GpuFormat.RGBA16_FLOAT,
					width,
					height,
					0,
					0
			));
		}
	}

	@Override
	public void close() {
		deleteTextures();
		deleteTexturesB3D();
		GL32.glDeleteVertexArrays(vao);
	}

	@Deprecated(forRemoval = true)
	private void deleteTextures() {
		if (depthBounds != -1) {
			GlStateManager._deleteTexture(depthBounds);
		}
		if (coefficients != -1) {
			GlStateManager._deleteTexture(coefficients);
		}
		if (accumulate != -1) {
			GlStateManager._deleteTexture(accumulate);
		}
		if (fbo != -1) {
			GlStateManager._glDeleteFramebuffers(fbo);
		}

		// We sometimes get the same texture ID back when creating new textures,
		// so bind zero to clear the GlStateManager
		Samplers.COEFFICIENTS.makeActive();
		GlStateManager._bindTexture(0);
		Samplers.DEPTH_RANGE.makeActive();
		GlStateManager._bindTexture(0);
	}

	private void deleteTexturesB3D() {
		if (depthBoundsB3D != null) {
			depthBoundsB3D.close();
		}
		for (GpuTextureView coefficient : coefficientsB3D) {
			if (coefficient != null) {
				coefficient.close();
			}
		}
		if (accumulateB3D != null) {
			accumulateB3D.close();
		}
	}
}
