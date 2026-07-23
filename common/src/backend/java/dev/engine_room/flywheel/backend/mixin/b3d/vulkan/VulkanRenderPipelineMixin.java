package dev.engine_room.flywheel.backend.mixin.b3d.vulkan;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK12;
import org.lwjgl.vulkan.VkPipelineLayoutCreateInfo;
import org.lwjgl.vulkan.VkPushConstantRange;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.renderpearl.backend.api.BackendRenderPipeline;
import com.mojang.renderpearl.backend.vulkan.VulkanRenderPipeline;

import dev.engine_room.flywheel.api.Flywheel;

@Mixin(VulkanRenderPipeline.class)
public class VulkanRenderPipelineMixin {
	@ModifyExpressionValue(method = "compile", at = @At(value = "INVOKE", target = "Lorg/lwjgl/vulkan/VkPipelineLayoutCreateInfo;pSetLayouts(Ljava/nio/LongBuffer;)Lorg/lwjgl/vulkan/VkPipelineLayoutCreateInfo;"))
	private static VkPipelineLayoutCreateInfo flywheel$addPushConstantsSupport(VkPipelineLayoutCreateInfo original, @Local(argsOnly = true) BackendRenderPipeline.CreateInfo createInfo) {
		// 26.3-snapshot-5 strips LVTs, and compile() now receives CreateInfo instead of RenderPipeline
		MemoryStack stack = MemoryStack.stackGet();
		if (createInfo.name().startsWith(Flywheel.ID + ":")) {
			VkPushConstantRange.Buffer pushConstants = VkPushConstantRange.calloc(1, stack);

			pushConstants.get(0)
					.stageFlags(VK12.VK_SHADER_STAGE_VERTEX_BIT | VK12.VK_SHADER_STAGE_FRAGMENT_BIT)
					.offset(0)
					.size(128);

			original.pPushConstantRanges(pushConstants);
		}

		return original;
	}
}
