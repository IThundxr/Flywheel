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

// TODO b3d-ification: Maybe a helper method that does the following:
// renderPass.bindTexture(name, <NoiseTextures>.getTextureView(), <NoiseTextures>.getSampler());
public enum NoiseTextures {
	BLUE_NOISE("textures/flywheel/noise/blue.png");

	private final ReloadableTexture reloadableTexture;

	NoiseTextures(String texturePath) {
		Identifier textureId = IdentifierUtil.id(texturePath);
		this.reloadableTexture = new SimpleTexture(textureId);
	}

	public GpuTexture getTexture() {
		return reloadableTexture.getTexture();
	}

	public GpuTextureView getTextureView() {
		return reloadableTexture.getTextureView();
	}

	public GpuSampler getSampler() {
		return reloadableTexture.getSampler();
	}

	@Internal
	public void register(TextureManager textureManager) {
		textureManager.register(reloadableTexture.resourceId(), reloadableTexture);
	}
}
