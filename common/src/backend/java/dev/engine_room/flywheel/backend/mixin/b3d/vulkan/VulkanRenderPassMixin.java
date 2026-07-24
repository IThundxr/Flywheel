package dev.engine_room.flywheel.backend.mixin.b3d.vulkan;

import java.nio.ByteBuffer;
import java.util.Collection;

import org.jspecify.annotations.Nullable;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK12;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.renderpearl.backend.vulkan.VulkanRenderPass;
import com.mojang.renderpearl.backend.vulkan.VulkanRenderPipeline;

import dev.engine_room.flywheel.backend.b3d.FlwUniformBinding;
import dev.engine_room.flywheel.backend.extension.b3d.FlwGlVulkanRenderPassExtension;

@Mixin(VulkanRenderPass.class)
public abstract class VulkanRenderPassMixin {
	@Shadow
	protected abstract VkCommandBuffer commandBuffer();

	@Shadow
	@Nullable
	protected VulkanRenderPipeline pipeline;

	@Inject(method = "pushDescriptors", at = @At("HEAD"))
	private void flywheel$addPushConstantsSupport(CallbackInfo ci) {
		FlwGlVulkanRenderPassExtension ext = (FlwGlVulkanRenderPassExtension) this;

		if (ext.flywheel$uniformsDirty()) {
			Collection<FlwUniformBinding> bindings = ext.flywheel$getUniformBindings().values();
			if (bindings.isEmpty()) {
				return;
			}

			try (MemoryStack stack = MemoryStack.stackPush()) {
				ByteBuffer data = stack.malloc(128);

				for (FlwUniformBinding binding : bindings) {
					binding.writeVulkan(data);
				}

				VK12.vkCmdPushConstants(
						this.commandBuffer(),
						this.pipeline.pipelineLayout(),
						VK12.VK_SHADER_STAGE_VERTEX_BIT | VK12.VK_SHADER_STAGE_FRAGMENT_BIT,
						0,
						data.flip()
				);
			}

			ext.flywheel$setUniformsDirty(false);
		}
	}
}
