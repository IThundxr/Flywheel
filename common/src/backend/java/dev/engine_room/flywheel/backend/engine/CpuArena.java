package dev.engine_room.flywheel.backend.engine;

import java.nio.ByteBuffer;

import dev.engine_room.flywheel.lib.memory.MemoryBlock;

public class CpuArena extends AbstractArena {
	private MemoryBlock memoryBlock;

	public CpuArena(long elementSizeBytes, int initialCapacity) {
		super(elementSizeBytes);

		memoryBlock = MemoryBlock.malloc(elementSizeBytes * initialCapacity);
	}

	public long indexToPointer(int i) {
		return memoryBlock.ptr() + i * elementSizeBytes;
	}

	public void delete() {
		memoryBlock.free();
	}

	public long byteCapacity() {
		return memoryBlock.size();
	}

	public ByteBuffer asBuffer() {
		return memoryBlock.asBuffer();
	}

	protected void grow() {
		memoryBlock = memoryBlock.realloc(memoryBlock.size() * 2);
	}
}
