package dev.engine_room.flywheel.backend.engine.indirect;

import org.lwjgl.opengl.GL46;
import org.lwjgl.system.MemoryUtil;

import dev.engine_room.flywheel.backend.engine.embed.EnvironmentStorage;

public class MatrixBuffer {
	private final ResizableStorageArray matrices = new ResizableStorageArray(
			"Flywheel Indirect MatrixBuffer",
			0, // TODO b3d-ification: Handle usage type
			EnvironmentStorage.MATRIX_SIZE_BYTES
	);

	// TODO b3d-ification: This should use the vanilla staging buffer instead
	public void flush(StagingBuffer stagingBuffer, EnvironmentStorage environmentStorage) {
		var arena = environmentStorage.arena;
		var capacity = arena.capacity();

		if (capacity == 0) {
			return;
		}

		matrices.ensureCapacity(capacity);

		stagingBuffer.enqueueCopy(arena.byteCapacity(), matrices.handle(), 0, ptr -> {
			MemoryUtil.memCopy(arena.indexToPointer(0), ptr, arena.byteCapacity());
		});
	}

	public void flushB3D(com.mojang.blaze3d.vertex.StagingBuffer.Uploader uploader, com.mojang.blaze3d.vertex.StagingBuffer.BufferHandle bufferHandle, EnvironmentStorage environmentStorage) {
		var arena = environmentStorage.arena;
		var size = bufferHandle.size();

		if (size == 0) {
			return;
		}

		matrices.ensureCapacity(size);

		uploader.copyTo(bufferHandle, matrices.getBuffer(), 0);
	}

	// TODO b3d-ification: We need to pass the buffer slice to the draw call instead
	public void bind() {
		if (matrices.capacity() == 0) {
			return;
		}

		GL46.glBindBufferRange(GL46.GL_SHADER_STORAGE_BUFFER, BufferBindings.MATRICES, matrices.handle(), 0, matrices.byteCapacity());
	}

	public void delete() {
		matrices.close();
	}
}
