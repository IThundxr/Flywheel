package dev.engine_room.flywheel.backend.engine;

import java.util.Comparator;

import org.jspecify.annotations.Nullable;

import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuSampler;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.datafixers.util.Pair;

import dev.engine_room.flywheel.api.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;

public final class MaterialRenderState {
	public static final Comparator<Material> COMPARATOR = MaterialRenderState::compare;

	private MaterialRenderState() {
	}

	public static void setupTexture(RenderPass renderPass, Material material) {
		AbstractTexture texture = Minecraft.getInstance()
				.getTextureManager()
				.getTexture(material.texture());

		// TODO 1.21.11: give the Material more control, such as using default filter mode, address modes, AF, max LOD?
		FilterMode filterMode = material.blur() ? FilterMode.LINEAR : FilterMode.NEAREST;
		GpuSampler defaultSampler = texture.getSampler();
		GpuSampler sampler = RenderSystem.getSamplerCache()
				.getSampler(defaultSampler.getAddressModeU(), defaultSampler.getAddressModeV(), filterMode, filterMode, material.mipmap());

		renderPass.bindTexture("flw_diffuseTex", texture.getTextureView(), sampler);
	}

	// Using setupTexture is better, but currently B3D and VK don't agree as getTexture can create a memory barrier
	// in a renderpass, which isn't supported at the moment
	// See: https://discord.com/channels/1138536747932864532/1424838212702044190/1513844934283755641
	@Deprecated(forRemoval = true)
	public static Pair<GpuTextureView, GpuSampler> createTextureView(Material material) {
		AbstractTexture texture = Minecraft.getInstance()
				.getTextureManager()
				.getTexture(material.texture());

		// TODO 1.21.11: give the Material more control, such as using default filter mode, address modes, AF, max LOD?
		FilterMode filterMode = material.blur() ? FilterMode.LINEAR : FilterMode.NEAREST;
		GpuSampler defaultSampler = texture.getSampler();
		GpuSampler sampler = RenderSystem.getSamplerCache()
				.getSampler(defaultSampler.getAddressModeU(), defaultSampler.getAddressModeV(), filterMode, filterMode, material.mipmap());

		return Pair.of(texture.getTextureView(), sampler);
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
				&& lhs.depthStencilState() == rhs.depthStencilState()
				&& lhs.colorTargetState() == rhs.colorTargetState()
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
		cmp = Comparator.comparing(DepthStencilState::depthTest)
				.thenComparing(DepthStencilState::writeDepth)
				.thenComparing(DepthStencilState::depthBiasScaleFactor)
				.thenComparing(DepthStencilState::depthBiasConstant)
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
