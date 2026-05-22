package dev.engine_room.flywheel.backend.engine.indirect;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBuffer.Usage;
import com.mojang.blaze3d.systems.RenderSystem;

/**
 * A buffer for storing data on the GPU that can be resized.
 * <br>
 * The only way to get data in and out is to use GPU copies.
 */
public class ResizableStorageBuffer implements AutoCloseable {
	@Usage
	private static final int BASE_USAGE_FLAGS = GpuBuffer.USAGE_COPY_DST | GpuBuffer.USAGE_COPY_SRC;

	private final String label;
	@Usage
	private final int usage;

	private GpuBuffer buffer;
	private long capacity = 0;

	public ResizableStorageBuffer(String label, @Usage int usage) {
		this.label = label;
		this.usage = usage | BASE_USAGE_FLAGS;
	}

	public long capacity() {
		return capacity;
	}

	public void ensureCapacity(long capacity) {
		if (this.capacity <= 0) {
			buffer = RenderSystem.getDevice().createBuffer(() -> this.label, this.usage, capacity);
		} else {
			GpuBuffer oldBuffer = buffer;
			GpuBuffer newBuffer = RenderSystem.getDevice().createBuffer(() -> this.label, this.usage, capacity);

			RenderSystem.getDevice()
					.createCommandEncoder()
					.copyToBuffer(oldBuffer.slice(), newBuffer.slice());

			oldBuffer.close();

			buffer = newBuffer;
		}
		this.capacity = capacity;
	}

	public GpuBuffer getBuffer() {
		return buffer;
	}

	@Override
	public void close() {
		buffer.close();
	}
}
