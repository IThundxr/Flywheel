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
import com.mojang.renderpearl.backend.opengl.GlCommandEncoder;
import com.mojang.renderpearl.backend.opengl.GlProgram;
import com.mojang.renderpearl.backend.opengl.GlRenderPass;
import com.mojang.renderpearl.backend.opengl.GlStateManager;

import dev.engine_room.flywheel.backend.b3d.FlwUniformBinding;
import dev.engine_room.flywheel.backend.extension.b3d.FlwGlVulkanRenderPassExtension;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

@Mixin(GlCommandEncoder.class)
public class GlCommandEncoderMixin {
	@Unique
	private final Map<GlProgram, Object2IntMap<String>> flywheel$uniformIdsCache = new IdentityHashMap<>();

	// TODO 26.3: trySetup became setupDraw and the dynamic-uniform loop was reworked;
	// GL plain-uniform binding needs a proper port. Optional so the game can boot meanwhile.
	@Inject(method = "trySetup", at = @At(value = "INVOKE", target = "Ljava/util/Map;entrySet()Ljava/util/Set;"), require = 0, expect = 0)
	private void flywheel$bindPlainUniforms(GlRenderPass renderPass, Collection<String> dynamicUniforms, CallbackInfoReturnable<Boolean> cir, @Local GlProgram glProgram) {
		FlwGlVulkanRenderPassExtension ext = (FlwGlVulkanRenderPassExtension) renderPass;

		if (ext.flywheel$uniformsDirty()) {
			Collection<FlwUniformBinding> bindings = ext.flywheel$getUniformBindings().values();
			if (bindings.isEmpty()) {
				return;
			}

			Object2IntMap<String> uniformIds = flywheel$uniformIdsCache
					.computeIfAbsent(glProgram, _ -> new Object2IntOpenHashMap<>());

			for (FlwUniformBinding binding : bindings) {
				int location = uniformIds.computeIfAbsent(binding.name(),
						(String name) -> GlStateManager._glGetUniformLocation(glProgram.getProgramId(), name));

				if (location >= 0) {
					binding.bindOpenGL(location);
				}
			}

			ext.flywheel$setUniformsDirty(false);
		}
	}
}
