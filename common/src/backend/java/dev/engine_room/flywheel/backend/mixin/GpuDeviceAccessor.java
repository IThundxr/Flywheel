package dev.engine_room.flywheel.backend.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import com.mojang.renderpearl.api.device.GpuDevice;
import com.mojang.renderpearl.backend.api.GpuDeviceBackend;

@Mixin(GpuDevice.class)
public interface GpuDeviceAccessor {
	@Accessor("backend")
	GpuDeviceBackend flywheel$getBackend();
}
