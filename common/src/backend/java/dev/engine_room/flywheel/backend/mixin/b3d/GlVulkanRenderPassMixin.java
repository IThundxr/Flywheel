package dev.engine_room.flywheel.backend.mixin.b3d;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import com.mojang.blaze3d.opengl.GlRenderPass;
import com.mojang.blaze3d.vulkan.VulkanRenderPass;

import dev.engine_room.flywheel.backend.b3d.FlwUniformBinding;
import dev.engine_room.flywheel.backend.extension.b3d.FlwGlVulkanRenderPassExtension;
import dev.engine_room.flywheel.backend.extension.b3d.FlwRenderPassExtension;

@Mixin({ GlRenderPass.class, VulkanRenderPass.class })
public class GlVulkanRenderPassMixin implements FlwRenderPassExtension, FlwGlVulkanRenderPassExtension {
	@Unique
	private final Map<String, FlwUniformBinding> flywheel$plainUniforms = new HashMap<>();

	@Override
	public void flywheel$setPlainUniform(FlwUniformBinding uniformBinding) {
		flywheel$plainUniforms.put(uniformBinding.name(), uniformBinding);
	}

	@Override
	public Map<String, FlwUniformBinding> flywheel$getUniformBindings() {
		return Collections.unmodifiableMap(flywheel$plainUniforms);
	}
}
