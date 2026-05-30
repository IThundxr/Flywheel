package dev.engine_room.flywheel.backend.engine.instancing;

import java.nio.ByteBuffer;

import org.lwjgl.system.MemoryStack;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.systems.RenderPass;

import dev.engine_room.flywheel.backend.engine.DynamicGpuBuffer;
import dev.engine_room.flywheel.backend.engine.LightStorage;
import it.unimi.dsi.fastutil.ints.IntArrayList;

public class InstancedLight implements AutoCloseable {
	public static final String LUT_BINDING = "_flw_lightLut";
	public static final String SECTIONS_BINDING = "_flw_lightSections";

	private final DynamicGpuBuffer lut;
	private final DynamicGpuBuffer sections;

	// TODO b3d-ification: Check if the default sizes should be higher
	public InstancedLight() {
		lut = new DynamicGpuBuffer(
				"Flywheel Instanced Light LUT UTB",
				GpuBuffer.USAGE_MAP_WRITE | GpuBuffer.USAGE_HINT_CLIENT_STORAGE | GpuBuffer.USAGE_COPY_DST | GpuBuffer.USAGE_UNIFORM_TEXEL_BUFFER,
				1024 * 4 // 4 KB
		);
		sections = new DynamicGpuBuffer(
				"Flywheel Instanced Light Sections UTB",
				GpuBuffer.USAGE_MAP_WRITE | GpuBuffer.USAGE_HINT_CLIENT_STORAGE | GpuBuffer.USAGE_COPY_DST | GpuBuffer.USAGE_UNIFORM_TEXEL_BUFFER,
				1024 * 4 // 4 KB
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
			IntArrayList lut = light.createLut();
			int[] lutData = lut.elements();

			try (MemoryStack stack = MemoryStack.stackPush()) {
				ByteBuffer buffer = stack.malloc(lut.size() * Integer.BYTES);
				buffer.asIntBuffer().put(lutData);
				this.lut.write(buffer);
			}
		}
	}

	@Override
	public void close() {
		lut.close();
		sections.close();
	}
}
