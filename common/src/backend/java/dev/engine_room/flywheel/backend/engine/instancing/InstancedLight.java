package dev.engine_room.flywheel.backend.engine.instancing;

import java.nio.ByteBuffer;

import org.lwjgl.opengl.GL32;
import org.lwjgl.system.MemoryStack;

import com.mojang.renderpearl.api.buffers.GpuBuffer;

import dev.engine_room.flywheel.backend.Samplers;
import dev.engine_room.flywheel.backend.engine.DynamicGpuBuffer;
import dev.engine_room.flywheel.backend.engine.LightStorage;
import dev.engine_room.flywheel.backend.gl.TextureBuffer;
import it.unimi.dsi.fastutil.ints.IntArrayList;

public class InstancedLight implements AutoCloseable {
	private final DynamicGpuBuffer lut;
	private final DynamicGpuBuffer sections;

	private final TextureBuffer lutTexture;
	private final TextureBuffer sectionsTexture;

	// TODO b3d-ification: Check if the default sizes should be higher
	public InstancedLight() {
		lut = new DynamicGpuBuffer(
				"Flw Instanced Lighting LUT UTB",
				GpuBuffer.USAGE_MAP_WRITE | GpuBuffer.USAGE_HINT_CLIENT_STORAGE | GpuBuffer.USAGE_COPY_DST | GpuBuffer.USAGE_UNIFORM_TEXEL_BUFFER,
				1024 * 4 // 4 KB
		);
		sections = new DynamicGpuBuffer(
				"Flw Instanced Lighting Sections UTB",
				GpuBuffer.USAGE_MAP_WRITE | GpuBuffer.USAGE_HINT_CLIENT_STORAGE | GpuBuffer.USAGE_COPY_DST | GpuBuffer.USAGE_UNIFORM_TEXEL_BUFFER,
				1024 * 4 // 4 KB
		);

		lutTexture = new TextureBuffer(GL32.GL_R32UI);
		sectionsTexture = new TextureBuffer(GL32.GL_R32UI);
	}

	public void bind() {
		Samplers.LIGHT_LUT.makeActive();
		lutTexture.bind(lut.getHandle());
		Samplers.LIGHT_SECTIONS.makeActive();
		sectionsTexture.bind(sections.getHandle());
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
		lutTexture.delete();
		sectionsTexture.delete();
	}
}
