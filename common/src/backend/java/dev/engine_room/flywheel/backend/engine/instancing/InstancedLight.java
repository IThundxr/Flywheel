package dev.engine_room.flywheel.backend.engine.instancing;

import org.lwjgl.system.MemoryUtil;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.systems.RenderPass;

import dev.engine_room.flywheel.backend.engine.DynamicGpuBuffer;
import dev.engine_room.flywheel.backend.engine.LightStorage;
import dev.engine_room.flywheel.lib.memory.MemoryBlock;

public class InstancedLight implements AutoCloseable {
	public static final String LUT_BINDING = "Flw_Lut";
	public static final String SECTIONS_BINDING = "Flw_Sections";

	private final DynamicGpuBuffer lut;
	private final DynamicGpuBuffer sections;

	// TODO b3d-ification: Check if the default sizes should be higher
	public InstancedLight() {
		lut = new DynamicGpuBuffer(
				"Flywheel Instanced Light LUT",
				GpuBuffer.USAGE_MAP_WRITE | GpuBuffer.USAGE_HINT_CLIENT_STORAGE | GpuBuffer.USAGE_COPY_DST,
				1024 * 4 // 4 MB
		);
		sections = new DynamicGpuBuffer(
				"Flywheel Instanced Light Sections",
				GpuBuffer.USAGE_MAP_WRITE | GpuBuffer.USAGE_HINT_CLIENT_STORAGE | GpuBuffer.USAGE_COPY_DST,
				1024 * 4 // 4 MB
		);
	}

	public void bindToRenderPass(RenderPass renderPass) {
		renderPass.setUniform(LUT_BINDING, lut.getCurrentBuffer());
		renderPass.setUniform(SECTIONS_BINDING, sections.getCurrentBuffer());
	}

	public void flush(LightStorage light) {
		if (light.capacity() == 0) {
			return;
		}

		light.upload(sections);

		if (light.checkNeedsLutRebuildAndClear()) {
			var lut = light.createLut();

			var memoryBlock = MemoryBlock.malloc((long) lut.size() * Integer.BYTES);
			long ptr = memoryBlock.ptr();

			for (int i = 0; i < lut.size(); i++) {
				MemoryUtil.memPutInt(ptr + (long) Integer.BYTES * i, lut.getInt(i));
			}

			this.lut.write(memoryBlock.asBuffer());
			memoryBlock.free();
		}
	}

	@Override
	public void close() {
		lut.close();
		sections.close();
	}
}
