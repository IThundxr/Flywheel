package dev.engine_room.flywheel.backend;

import com.mojang.blaze3d.textures.GpuSampler;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;

import dev.engine_room.flywheel.lib.util.IdentifierUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.ReloadableTexture;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.resources.Identifier;

public enum NoiseTextures {
	BLUE_NOISE("textures/flywheel/noise/blue.png");

	private final ReloadableTexture reloadableTexture;

	NoiseTextures(String texturePath) {
		Identifier textureId = IdentifierUtil.id(texturePath);
		this.reloadableTexture = new SimpleTexture(textureId);
		Minecraft.getInstance().getTextureManager().registerAndLoad(textureId, this.reloadableTexture);
	}

	public GpuTexture getGpuTexture() {
		return reloadableTexture.getTexture();
	}

	public GpuTextureView getGpuTextureView() {
		return reloadableTexture.getTextureView();
	}

	public GpuSampler getGpuSampler() {
		return reloadableTexture.getSampler();
	}

	// Just so the class gets loaded
	static void init() {}
}
