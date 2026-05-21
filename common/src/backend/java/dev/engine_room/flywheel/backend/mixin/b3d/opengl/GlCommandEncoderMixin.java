package dev.engine_room.flywheel.backend.mixin.b3d.opengl;

import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.opengl.GlCommandEncoder;
import com.mojang.blaze3d.opengl.GlProgram;
import com.mojang.blaze3d.opengl.GlRenderPass;
import com.mojang.blaze3d.opengl.GlStateManager;

import dev.engine_room.flywheel.backend.b3d.FlwUniformBinding;
import dev.engine_room.flywheel.backend.extension.b3d.FlwGlVulkanRenderPassExtension;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

@Mixin(GlCommandEncoder.class)
public class GlCommandEncoderMixin {
	@Unique
	private final Map<GlProgram, Object2IntMap<String>> flywheel$uniformIdsCache = new IdentityHashMap<>();

	@Inject(method = "trySetup", at = @At(value = "INVOKE", target = "Ljava/util/Map;entrySet()Ljava/util/Set;"))
	private void flywheel$bindPlainUniforms(GlRenderPass renderPass, Collection<String> dynamicUniforms, CallbackInfoReturnable<Boolean> cir, @Local(name = "glProgram") GlProgram glProgram) {
		Collection<FlwUniformBinding> bindings = ((FlwGlVulkanRenderPassExtension) renderPass)
				.flywheel$getUniformBindings().values();

		if (bindings.isEmpty()) {
			return;
		}

		Object2IntMap<String> uniformIds = flywheel$uniformIdsCache
				.computeIfAbsent(glProgram, _ -> new Object2IntOpenHashMap<>());

		int glProgramId = glProgram.getProgramId();
		for (FlwUniformBinding binding : bindings) {
			String uniformName = binding.name();
			int location = uniformIds.computeIfAbsent(uniformName,
					(String name) -> GlStateManager._glGetUniformLocation(glProgramId, name));

			if (location > 0) {
				binding.bindOpenGL(location);
			}
		}
	}
}
