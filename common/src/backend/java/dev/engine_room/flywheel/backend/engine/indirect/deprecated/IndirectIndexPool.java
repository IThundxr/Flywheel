package dev.engine_room.flywheel.backend.engine.indirect.deprecated;

import java.nio.ByteBuffer;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import dev.engine_room.flywheel.api.model.IndexSequence;
import dev.engine_room.flywheel.backend.engine.indirect.deprecated.gl.array.GlVertexArray;
import dev.engine_room.flywheel.backend.engine.indirect.deprecated.gl.buffer.GlBuffer;
import dev.engine_room.flywheel.backend.engine.indirect.deprecated.gl.buffer.GlBufferUsage;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;

public class IndirectIndexPool {
	private final GlBuffer ebo;

	private final Reference2IntMap<IndexSequence> indexCounts;
	private final Reference2IntMap<IndexSequence> firstIndices;

	private boolean dirty;

    public IndirectIndexPool() {
		ebo = new GlBuffer(GlBufferUsage.DYNAMIC_DRAW);

		indexCounts = new Reference2IntOpenHashMap<>();
		firstIndices = new Reference2IntOpenHashMap<>();

		indexCounts.defaultReturnValue(0);
	}

	public int firstIndex(IndexSequence sequence) {
		return firstIndices.getInt(sequence);
	}

	public void reset() {
		indexCounts.clear();
		firstIndices.clear();
		dirty = true;
	}

	public void updateCount(IndexSequence sequence, int indexCount) {
		int oldCount = indexCounts.getInt(sequence);
		int newCount = Math.max(oldCount, indexCount);

		if (newCount > oldCount) {
			indexCounts.put(sequence, newCount);
			dirty = true;
		}
	}

	public void flush() {
		if (!dirty) {
			return;
		}

		firstIndices.clear();
		dirty = false;

		int totalIndexCount = 0;

		for (int count : indexCounts.values()) {
			totalIndexCount += count;
		}

		try (MemoryStack stack = MemoryStack.stackPush()) {
			int totalSize = totalIndexCount * Integer.BYTES;
			ByteBuffer buffer = stack.malloc(totalSize);
			long ptr = MemoryUtil.memAddress(buffer);

			int firstIndex = 0;
			for (Reference2IntMap.Entry<IndexSequence> entries : indexCounts.reference2IntEntrySet()) {
				var indexSequence = entries.getKey();
				var indexCount = entries.getIntValue();

				firstIndices.put(indexSequence, firstIndex);

				indexSequence.fill(buffer, indexCount);

				firstIndex += indexCount;
			}

			ebo.upload(ptr, totalSize);
		}
	}

	public void bind(GlVertexArray vertexArray) {
		vertexArray.setElementBuffer(ebo.handle());
	}

	public void delete() {
		ebo.delete();
	}
}
