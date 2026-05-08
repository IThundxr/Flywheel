package dev.engine_room.flywheel.backend.mixin.b3d.vulkan;

import org.spongepowered.asm.mixin.Mixin;

import com.mojang.blaze3d.vulkan.VulkanCommandEncoder;

// TODO b3d-ification
@Mixin(VulkanCommandEncoder.class)
public class VulkanCommandEncoderMixin {
	static {
		((Runnable) () -> {
			throw new UnsupportedOperationException("Support for push constants still needs to be added by flw");
		}).run();
	}
}
