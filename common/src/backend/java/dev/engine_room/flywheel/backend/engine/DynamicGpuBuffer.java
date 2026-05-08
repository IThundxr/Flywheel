package dev.engine_room.flywheel.backend.engine;

import java.nio.ByteBuffer;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.systems.RenderSystem;

import dev.engine_room.flywheel.backend.FlwBackend;
import net.minecraft.util.Mth;

// TODO b3d-ification: Document this a bit
public class DynamicGpuBuffer implements AutoCloseable {
	private GpuBuffer buffer;
	private final String label;
	@GpuBuffer.Usage
	private final int usage;
	private int capacity;

	public DynamicGpuBuffer(String label, int usage, int initialCapacity) {
		this.label = label;
		this.usage = usage;
		this.capacity = Mth.smallestEncompassingPowerOfTwo(initialCapacity);
		this.buffer = RenderSystem.getDevice().createBuffer(() -> this.label, this.usage, this.capacity);
	}

	private void resizeBuffers(int capacity) {
		this.capacity = capacity;
		RenderSystem.queueFencedTask(buffer::close);
		this.buffer = RenderSystem.getDevice().createBuffer(() -> this.label, this.usage, this.capacity);
	}

	public void write(ByteBuffer byteBuffer) {
		int neededSize = byteBuffer.position();
		if (neededSize > capacity) {
			int newCapacity = Mth.smallestEncompassingPowerOfTwo(neededSize);
			FlwBackend.LOGGER.info("Resizing {}, capacity limit of {} reached during a single frame. New capacity will be {}.", this.label, this.capacity, newCapacity);
			resizeBuffers(newCapacity);
		}

		RenderSystem.getDevice().createCommandEncoder().writeToBuffer(buffer.slice(), byteBuffer);
	}

	public void writeSpan(int offset, ByteBuffer byteBuffer) {
		GpuBufferSlice slice = buffer.slice(offset, byteBuffer.capacity());
		RenderSystem.getDevice().createCommandEncoder().writeToBuffer(slice, byteBuffer);
	}

	/// Return the current backing {@link GpuBuffer}
	///
	/// The returned buffer should not be written to manually,
	/// and {@link DynamicGpuBuffer#write(ByteBuffer)} should be used instead.
	public GpuBuffer getCurrentBuffer() {
		return buffer;
	}

	public int currentCapacity() {
		return capacity;
	}

	@Override
	public void close() {
		buffer.close();
	}
}
