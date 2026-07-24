package dev.engine_room.flywheel.backend.mixin;

import org.joml.Vector3fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.platform.Lighting.Entry;

import dev.engine_room.flywheel.backend.engine.uniform.LevelUniforms;

@Mixin(Lighting.class)
abstract class LightingMixin {
	@Inject(method = "updateBuffer", at = @At("HEAD"))
	private static void flywheel$onHeadUpdateBuffer(Entry entry, Vector3fc light0, Vector3fc light1, CallbackInfo ci) {
		if (entry == Entry.LEVEL) {
			LevelUniforms.INSTANCE.light0Direction.set(light0);
			LevelUniforms.INSTANCE.light1Direction.set(light1);
		}
	}
}
