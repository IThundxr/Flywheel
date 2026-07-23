package dev.engine_room.flywheel.backend.engine;

import java.nio.ByteBuffer;
import java.util.function.Function;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.systems.RenderSystem;

import dev.engine_room.flywheel.backend.FlwBackend;

// TODO b3d-ification: Document this a bit
public class DynamicGpuBuffer implements AutoCloseable {
	private final String label;
	@GpuBuffer.Usage
	private final int usage;
	private final Function<Long, Long> sizeIncreaseFunc;

	private GpuBuffer buffer;
	private long capacity;

	public DynamicGpuBuffer(String label, int usage, long initialCapacity) {
		this(label, usage, initialCapacity, DynamicGpuBuffer::smallestEncompassingPowerOfTwo);
	}

	public DynamicGpuBuffer(String label, int usage, long initialCapacity, Function<Long, Long> sizeIncreaseFunc) {
		this.label = label;
		this.usage = usage;
		this.sizeIncreaseFunc = sizeIncreaseFunc;

		this.capacity = this.sizeIncreaseFunc.apply(initialCapacity);
		this.buffer = RenderSystem.getDevice().createBuffer(() -> this.label, this.usage, this.capacity);
	}

	public void ensureCapacity(long neededSize) {
		if (neededSize > capacity) {
			long newCapacity = sizeIncreaseFunc.apply(neededSize);
			FlwBackend.LOGGER.info("Resizing {}, capacity limit of {} reached. New capacity will be {}.", this.label, this.capacity, newCapacity);

			this.capacity = newCapacity;
			RenderSystem.queueFencedTask(buffer::close);
			this.buffer = RenderSystem.getDevice().createBuffer(() -> this.label, this.usage, this.capacity);
		}
	}

	public void write(ByteBuffer byteBuffer) {
		ensureCapacity(byteBuffer.capacity());
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

	public long currentCapacity() {
		return capacity;
	}

	@Override
	public void close() {
		buffer.close();
	}

	private static long smallestEncompassingPowerOfTwo(final long input) {
		long result = input - 1;
		result |= result >> 1;
		result |= result >> 2;
		result |= result >> 4;
		result |= result >> 8;
		result |= result >> 16;
		result |= result >> 32;
		return result + 1;
	}
}
