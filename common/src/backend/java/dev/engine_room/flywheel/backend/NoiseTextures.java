package dev.engine_room.flywheel.backend;

import org.jetbrains.annotations.ApiStatus.Internal;

import com.mojang.blaze3d.textures.GpuSampler;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;

import dev.engine_room.flywheel.lib.util.IdentifierUtil;
import net.minecraft.client.renderer.texture.ReloadableTexture;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.Identifier;

public enum NoiseTextures {
	BLUE_NOISE("textures/flywheel/noise/blue.png");

	private final ReloadableTexture reloadableTexture;

	NoiseTextures(String texturePath) {
		Identifier textureId = IdentifierUtil.id(texturePath);
		this.reloadableTexture = new SimpleTexture(textureId);
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

	@Internal
	public void register(TextureManager textureManager) {
		textureManager.register(reloadableTexture.resourceId(), reloadableTexture);
	}
}
