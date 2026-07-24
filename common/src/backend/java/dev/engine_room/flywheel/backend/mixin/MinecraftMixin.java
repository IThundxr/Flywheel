package dev.engine_room.flywheel.backend.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.engine_room.flywheel.backend.engine.uniform.OptionsUniforms;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.main.GameConfig;

@Mixin(Minecraft.class)
public class MinecraftMixin {
	@Shadow
	@Final
	public Options options;

	@Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;initRenderer(Lcom/mojang/renderpearl/api/device/GpuDevice;)V", shift = Shift.AFTER))
	private void flywheel$updateOptionsUniforms(GameConfig gameConfig, CallbackInfo ci) {
		OptionsUniforms.INSTANCE.update(options);
	}
}
