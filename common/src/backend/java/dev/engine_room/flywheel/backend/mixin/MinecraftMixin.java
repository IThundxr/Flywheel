package dev.engine_room.flywheel.backend.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.engine_room.flywheel.backend.NoiseTextures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.main.GameConfig;
import net.minecraft.client.renderer.texture.TextureManager;

@Mixin(Minecraft.class)
public class MinecraftMixin {
	@Shadow
	@Final
	private TextureManager textureManager;

	@Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/TitleScreen;registerTextures(Lnet/minecraft/client/renderer/texture/TextureManager;)V"))
	private void flywheel$registerNoiseTextures(GameConfig gameConfig, CallbackInfo ci) {
		for (NoiseTextures noiseTexture : NoiseTextures.values()) {
			noiseTexture.register(textureManager);
		}
	}
}
