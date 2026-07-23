package dev.engine_room.flywheel.backend.engine.indirect;

import java.util.Optional;
import java.util.OptionalDouble;

import org.joml.Vector4f;
import org.joml.Vector4fc;
import org.jspecify.annotations.Nullable;

import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.renderpearl.api.commands.CommandEncoder;
import com.mojang.renderpearl.api.device.GpuDevice;
import com.mojang.renderpearl.api.commands.RenderPass;
import com.mojang.renderpearl.api.commands.RenderPass.RenderArea;
import com.mojang.renderpearl.api.commands.RenderPassDescriptor;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.textures.FilterMode;
import com.mojang.renderpearl.api.textures.GpuSampler;
import com.mojang.renderpearl.api.textures.GpuTexture;
import com.mojang.renderpearl.api.textures.GpuTextureView;

import dev.engine_room.flywheel.backend.NoiseTextures;
import dev.engine_room.flywheel.backend.compile.OitPrograms;
import dev.engine_room.flywheel.backend.engine.uniform.Uniforms;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;

public class OitFramebuffer implements AutoCloseable {
	private static final Optional<Vector4fc> CLEAR_TO_ZERO = Optional.of(new Vector4f(0, 0, 0, 0));
	private static final CommandEncoder COMMAND_ENCODER = RenderSystem.getDevice().createCommandEncoder();

	private final OitPrograms programs;

	@Nullable
	private GpuTextureView depthBounds = null;
	private final GpuTextureView[] coefficients = new GpuTextureView[4];
	@Nullable
	private GpuTextureView accumulate = null;

	private int lastWidth = -1;
	private int lastHeight = -1;

	public OitFramebuffer(OitPrograms programs) {
		this.programs = programs;
	}

	/**
	 * Render out the min and max depth per fragment.
	 */
	public RenderPass createDepthRangePass() {
		RenderTarget renderTarget = setupTexturesAndGetRenderTarget();
		float far = Minecraft.getInstance().gameRenderer.gameRenderState().levelRenderState.cameraRenderState.depthFar;
		return COMMAND_ENCODER.createRenderPass(
				() -> "Flw OIT Depth Range",
				depthBounds, Optional.of(new Vector4f(-far, -far, 0, 0)),
				renderTarget.getDepthTextureView(), OptionalDouble.empty()
		);
	}

	/**
	 * Generate the coefficients to the transmittance function.
	 */
	public RenderPass createTransmittancePass() {
		RenderTarget renderTarget = setupTexturesAndGetRenderTarget();
		RenderPassDescriptor.Builder descriptor = RenderPassDescriptor.builder(() -> "Flw OIT Transmittance")
				.withRenderArea(new RenderArea(0, 0, renderTarget.width, renderTarget.height))
				.withDepthAttachment(renderTarget.getDepthTextureView());

		for (GpuTextureView view : coefficients) {
			descriptor.withColorAttachment(view, CLEAR_TO_ZERO);
		}

		RenderPass renderPass = COMMAND_ENCODER.createRenderPass(descriptor.build());

		GpuSampler clampToEdgeNearest = RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST);
		renderPass.setUniform("_flw_depthRange", depthBounds, clampToEdgeNearest);
		renderPass.setUniform("_flw_blueNoise", NoiseTextures.BLUE_NOISE.getTextureView(), NoiseTextures.BLUE_NOISE.getSampler());

		return renderPass;
	}

	/**
	 * If any fragment has its transmittance fall off to zero, search the transmittance
	 * function to determine at what depth that occurs and write out to the depth buffer.
	 */
	public void renderDepthFromTransmittance() {
		RenderTarget renderTarget = setupTexturesAndGetRenderTarget();
		RenderPassDescriptor.Builder descriptor = RenderPassDescriptor.builder(() -> "Flw OIT Depth From Transmittance")
				.withRenderArea(new RenderArea(0, 0, renderTarget.width, renderTarget.height))
				.withUnusedColorAttachment()
				.withDepthAttachment(renderTarget.getDepthTextureView());

		try (RenderPass renderPass = COMMAND_ENCODER.createRenderPass(descriptor.build())) {
			Uniforms.bindToRenderPass(renderPass);

			GpuSampler clampToEdgeNearest = RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST);
			renderPass.setUniform("_flw_depthRange", depthBounds, clampToEdgeNearest);

			renderPass.setUniform("_flw_coefficients[0]", coefficients[0], clampToEdgeNearest);
			renderPass.setUniform("_flw_coefficients[1]", coefficients[1], clampToEdgeNearest);
			renderPass.setUniform("_flw_coefficients[2]", coefficients[2], clampToEdgeNearest);
			renderPass.setUniform("_flw_coefficients[3]", coefficients[3], clampToEdgeNearest);

			var flywheel$compiled = dev.engine_room.flywheel.backend.compile.core.CompiledPipelines.getOrNull(programs.getOitDepthPipeline());
			if (flywheel$compiled == null) {
				return;
			}
			renderPass.setPipeline(flywheel$compiled);

			drawFullscreenQuad(renderPass);
		}
	}

	/**
	 * Sample the transmittance function and accumulate.
	 */
	public RenderPass createAccumulatePass() {
		RenderTarget renderTarget = setupTexturesAndGetRenderTarget();
		return COMMAND_ENCODER.createRenderPass(
				() -> "Flw OIT Accumulate",
				accumulate, CLEAR_TO_ZERO,
				renderTarget.getDepthTextureView(), OptionalDouble.empty()
		);
	}

	/**
	 * Composite the accumulated luminance onto the main framebuffer.
	 */
	public void composite() {
		RenderTarget renderTarget;
		if (Minecraft.getInstance().gameRenderer.useImprovedTransparency()) {
			// TODO 26.3: vanilla removed the item-entity target; main target is the closest match
			renderTarget = Minecraft.getInstance().gameRenderer.mainRenderTarget();
		} else {
			renderTarget = Minecraft.getInstance().gameRenderer.mainRenderTarget();
		}

		try (RenderPass renderPass = COMMAND_ENCODER.createRenderPass(
				() -> "Flw OIT Composite",
				renderTarget.getColorTextureView(), Optional.empty(),
				renderTarget.getDepthTextureView(), OptionalDouble.empty()
		)) {
			Uniforms.bindToRenderPass(renderPass);

			GpuSampler clampToEdgeNearest = RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST);
			renderPass.setUniform("_flw_accumulate", accumulate, clampToEdgeNearest);
			renderPass.setUniform("_flw_depthRange", depthBounds, clampToEdgeNearest);

			renderPass.setUniform("_flw_coefficients[0]", coefficients[0], clampToEdgeNearest);
			renderPass.setUniform("_flw_coefficients[1]", coefficients[1], clampToEdgeNearest);
			renderPass.setUniform("_flw_coefficients[2]", coefficients[2], clampToEdgeNearest);
			renderPass.setUniform("_flw_coefficients[3]", coefficients[3], clampToEdgeNearest);

			var flywheel$compiled = dev.engine_room.flywheel.backend.compile.core.CompiledPipelines.getOrNull(programs.getOitCompositePipeline());
			if (flywheel$compiled == null) {
				return;
			}
			renderPass.setPipeline(flywheel$compiled);

			drawFullscreenQuad(renderPass);
		}
	}

	private RenderTarget setupTexturesAndGetRenderTarget() {
		Minecraft minecraft = Minecraft.getInstance();
		GameRenderer gameRenderer = minecraft.gameRenderer;

		RenderTarget renderTarget;
		if (gameRenderer.useImprovedTransparency()) {
			// TODO 26.3: vanilla removed the item-entity target; main target is the closest match
			renderTarget = gameRenderer.mainRenderTarget();

			renderTarget.copyDepthFrom(gameRenderer.mainRenderTarget());
		} else {
			renderTarget = gameRenderer.mainRenderTarget();
		}

		maybeResizeTextures(renderTarget.width, renderTarget.height);

		return renderTarget;
	}

	private void drawFullscreenQuad(RenderPass renderPass) {
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
				"Flw OIT FBO Depth Bounds",
				GpuTexture.USAGE_TEXTURE_BINDING | GpuTexture.USAGE_RENDER_ATTACHMENT,
				GpuFormat.RG32_FLOAT,
				width,
				height,
				1,
				1
		));

		for (int i = 0; i < coefficients.length; i++) {
			coefficients[i] = device.createTextureView(device.createTexture(
					"Flw OIT FBO Coefficients #" + i,
					GpuTexture.USAGE_TEXTURE_BINDING | GpuTexture.USAGE_RENDER_ATTACHMENT,
					GpuFormat.RGBA16_FLOAT,
					width,
					height,
					1,
					1
			));
		}

		accumulate = device.createTextureView(device.createTexture(
				"Flw OIT FBO Accumulate",
				GpuTexture.USAGE_TEXTURE_BINDING | GpuTexture.USAGE_RENDER_ATTACHMENT,
				GpuFormat.RGBA16_FLOAT,
				width,
				height,
				1,
				1
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
