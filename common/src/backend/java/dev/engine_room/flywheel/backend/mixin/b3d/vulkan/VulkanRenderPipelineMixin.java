package dev.engine_room.flywheel.backend.mixin.b3d.vulkan;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK12;
import org.lwjgl.vulkan.VkPipelineLayoutCreateInfo;
import org.lwjgl.vulkan.VkPushConstantRange;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vulkan.VulkanRenderPipeline;

import dev.engine_room.flywheel.api.Flywheel;

@Mixin(VulkanRenderPipeline.class)
public class VulkanRenderPipelineMixin {
	@ModifyExpressionValue(method = "compile", at = @At(value = "INVOKE", target = "Lorg/lwjgl/vulkan/VkPipelineLayoutCreateInfo;pSetLayouts(Ljava/nio/LongBuffer;)Lorg/lwjgl/vulkan/VkPipelineLayoutCreateInfo;"))
	private static VkPipelineLayoutCreateInfo flywheel$addPushConstantsSupport(VkPipelineLayoutCreateInfo original, @Local(argsOnly = true) RenderPipeline pipeline, @Local(name = "stack") MemoryStack stack) {
		if (pipeline.getLocation().getNamespace().equals(Flywheel.ID)) {
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
