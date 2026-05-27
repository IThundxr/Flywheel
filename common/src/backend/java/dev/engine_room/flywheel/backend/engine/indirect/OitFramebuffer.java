package dev.engine_room.flywheel.backend.engine.indirect;

import java.util.Optional;
import java.util.OptionalDouble;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.joml.Vector4f;
import org.joml.Vector4fc;
import org.jspecify.annotations.Nullable;

import com.mojang.blaze3d.GpuFormat;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderPassDescriptor;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuSampler;
import com.mojang.blaze3d.textures.GpuTextureView;

import dev.engine_room.flywheel.backend.NoiseTextures;
import dev.engine_room.flywheel.backend.compile.OitPrograms;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;

public class OitFramebuffer implements AutoCloseable {
	public static final Optional<Vector4fc> CLEAR_TO_ZERO = Optional.of(new Vector4f(0, 0, 0, 0));

	private final OitPrograms programs;

	@Nullable
	public GpuTextureView depthBounds = null;
	public final GpuTextureView[] coefficients = new GpuTextureView[4];
	@Nullable
	public GpuTextureView accumulate = null;

	private int lastWidth = -1;
	private int lastHeight = -1;

	public OitFramebuffer(OitPrograms programs) {
		this.programs = programs;
	}

	/**
	 * Render out the min and max depth per fragment.
	 */
	public RenderPass createDepthRangePass() {
		return createRenderPass(() -> "Flw OIT Depth Range", descriptor -> {
			float far = Minecraft.getInstance().gameRenderer.gameRenderState().levelRenderState.cameraRenderState.depthFar;
			descriptor.withColorAttachment(depthBounds, Optional.of(new Vector4f(-far, -far, 0, 0)));
		});
	}

	/**
	 * Generate the coefficients to the transmittance function.
	 */
	public RenderPass createTransmittancePass() {
		RenderPass renderPass = createRenderPass(() -> "Flw OIT Transmittance", descriptor -> {
			for (GpuTextureView view : coefficients) {
				descriptor.withColorAttachment(view, CLEAR_TO_ZERO);
			}
		});

		GpuSampler clampToEdgeNearest = RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST);
		renderPass.bindTexture("_flw_depthRange", depthBounds, clampToEdgeNearest);
		renderPass.bindTexture("_flw_blueNoise", NoiseTextures.BLUE_NOISE.getTextureView(), NoiseTextures.BLUE_NOISE.getSampler());

		return renderPass;
	}

	/**
	 * If any fragment has its transmittance fall off to zero, search the transmittance
	 * function to determine at what depth that occurs and write out to the depth buffer.
	 */
	public void renderDepthFromTransmittance() {
		try (RenderPass renderPass = createRenderPass(() -> "Flw OIT Depth From Transmittance", _ -> {})) {
			GpuSampler clampToEdgeNearest = RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST);
			renderPass.bindTexture("_flw_coefficients0", coefficients[0], clampToEdgeNearest);
			renderPass.bindTexture("_flw_coefficients1", coefficients[1], clampToEdgeNearest);
			renderPass.bindTexture("_flw_coefficients2", coefficients[2], clampToEdgeNearest);
			renderPass.bindTexture("_flw_coefficients3", coefficients[3], clampToEdgeNearest);

			renderPass.setPipeline(programs.getOitDepthPipeline());

			drawFullscreenQuad(renderPass);
		}
	}

	/**
	 * Sample the transmittance function and accumulate.
	 */
	public RenderPass createAccumulatePass() {
		return createRenderPass(() -> "Flw OIT Accumulate", descriptor -> {
			descriptor.withColorAttachment(accumulate, CLEAR_TO_ZERO);
		});
	}

	/**
	 * Composite the accumulated luminance onto the main framebuffer.
	 */
	public void composite() {
		CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();

		RenderTarget renderTarget;
		if (Minecraft.getInstance().gameRenderer.gameRenderState().useShaderTransparency()) {
			renderTarget = Minecraft.getInstance().levelRenderer.itemEntityTarget();
		} else {
			renderTarget = Minecraft.getInstance().gameRenderer.mainRenderTarget();
		}

		GpuTextureView colorTextureView = renderTarget.getColorTextureView();
		GpuTextureView depthTextureView = renderTarget.getDepthTextureView();

		try (RenderPass renderPass = encoder.createRenderPass(() -> "Flw OIT Composite", colorTextureView, Optional.empty(), depthTextureView, OptionalDouble.empty())) {
			GpuSampler clampToEdgeNearest = RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST);
			renderPass.bindTexture("_flw_accumulate", accumulate, clampToEdgeNearest);

			renderPass.setPipeline(programs.getOitCompositePipeline());

			drawFullscreenQuad(renderPass);
		}
	}

	private RenderPass createRenderPass(Supplier<String> label, Consumer<RenderPassDescriptor> descriptorFunc) {
		RenderTarget renderTarget = setupTexturesAndGetRenderTarget();

		RenderPassDescriptor descriptor = RenderPassDescriptor.create(label)
				.withRenderArea(new RenderPass.RenderArea(0, 0, renderTarget.width, renderTarget.height))
				.withDepthAttachment(renderTarget.getDepthTextureView());

		descriptorFunc.accept(descriptor);

		return RenderSystem.getDevice().createCommandEncoder().createRenderPass(descriptor);
	}

	private RenderTarget setupTexturesAndGetRenderTarget() {
		Minecraft minecraft = Minecraft.getInstance();
		GameRenderer gameRenderer = minecraft.gameRenderer;

		RenderTarget renderTarget;
		if (gameRenderer.gameRenderState().useShaderTransparency()) {
			renderTarget = minecraft.levelRenderer.itemEntityTarget();

			renderTarget.copyDepthFrom(gameRenderer.mainRenderTarget());
		} else {
			renderTarget = gameRenderer.mainRenderTarget();
		}

		maybeResizeTextures(renderTarget.width, renderTarget.height);

		return renderTarget;
	}

	private void drawFullscreenQuad(RenderPass renderPass) {
		// TODO b3d-ification: We need to pass a empty buffer to renderpass so it uses a clean vertexarray
		// Empty VAO, the actual full screen triangle is generated in the vertex shader
		//GlStateManager._glBindVertexArray(vao);

		renderPass.draw(3, 1, 0, 0);
	}

	private void maybeResizeTextures(int width, int height) {
		if (lastWidth == width && lastHeight == height) {
			return;
		}

		lastWidth = width;
		lastHeight = height;

		deleteTextures();

		GpuDevice device = RenderSystem.getDevice();

		depthBounds = device.createTextureView(device.createTexture(
				"Flw OIT Depth Bounds",
				0,
				GpuFormat.RG32_FLOAT,
				width,
				height,
				0,
				0
		));

		for (int i = 0; i < coefficients.length; i++) {
			coefficients[i] = device.createTextureView(device.createTexture(
					"Flw OIT Coefficients #" + i,
					0,
					GpuFormat.RGBA16_FLOAT,
					width,
					height,
					0,
					0
			));
		}

		accumulate = device.createTextureView(device.createTexture(
				"Flw OIT Accumulate",
				0,
				GpuFormat.RGBA16_FLOAT,
				width,
				height,
				0,
				0
		));

	}

	@Override
	public void close() {
		deleteTextures();
	}

	private void deleteTextures() {
		if (depthBounds != null) {
			depthBounds.close();
		}
		for (int i = 0; i < coefficients.length; i++) {
			GpuTextureView coefficient = coefficients[i];
			if (coefficient != null) {
				coefficient.close();
				coefficients[i] = null;
			}
		}
		if (accumulate != null) {
			accumulate.close();
		}
	}
}
