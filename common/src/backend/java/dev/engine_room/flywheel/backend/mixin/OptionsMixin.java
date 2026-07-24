package dev.engine_room.flywheel.backend.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.systems.RenderSystem;

import dev.engine_room.flywheel.backend.engine.uniform.OptionsUniforms;
import net.minecraft.client.Options;

@Mixin(Options.class)
abstract class OptionsMixin {
	@Inject(method = "load()V", at = @At("RETURN"))
	private void flywheel$onLoad(CallbackInfo ci) {
		flywheel$update();
	}

	@Inject(method = "save", at = @At("HEAD"))
	private void flywheel$onSave(CallbackInfo ci) {
		flywheel$update();
	}

	@Unique
	private void flywheel$update() {
		if (RenderSystem.tryGetDevice() != null) {
			OptionsUniforms.INSTANCE.update((Options) (Object) this);
		}
	}
}
