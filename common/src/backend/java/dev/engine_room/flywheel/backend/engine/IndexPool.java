package dev.engine_room.flywheel.backend.engine;

import com.mojang.blaze3d.IndexType;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.systems.RenderPass;

import dev.engine_room.flywheel.api.model.IndexSequence;
import dev.engine_room.flywheel.lib.memory.MemoryBlock;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;

public class IndexPool implements AutoCloseable {
	private final DynamicGpuBuffer ebo;

	private final Reference2IntMap<IndexSequence> indexCounts;
	private final Reference2IntMap<IndexSequence> firstIndices;

	private boolean dirty;

    public IndexPool() {
		// TODO b3d-ification: check if we need a bigger buffer
		ebo = new DynamicGpuBuffer(
				"Flywheel IndexPool EBO",
				GpuBuffer.USAGE_MAP_WRITE | GpuBuffer.USAGE_HINT_CLIENT_STORAGE | GpuBuffer.USAGE_COPY_DST | GpuBuffer.USAGE_INDEX,
				1024 * 4 // 4 KB
		);

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

		long totalIndexCount = 0;

		for (int count : indexCounts.values()) {
			totalIndexCount += count;
		}

		final var indexBlock = MemoryBlock.malloc(totalIndexCount * Integer.BYTES);
		final long indexPtr = indexBlock.ptr();

		int firstIndex = 0;
		for (Reference2IntMap.Entry<IndexSequence> entries : indexCounts.reference2IntEntrySet()) {
			var indexSequence = entries.getKey();
			var indexCount = entries.getIntValue();

			firstIndices.put(indexSequence, firstIndex);

			indexSequence.fill(indexPtr + (long) firstIndex * Integer.BYTES, indexCount);

			firstIndex += indexCount;
		}

		ebo.write(indexBlock.asBuffer());
		indexBlock.free();
	}

	public void bindToRenderPass(RenderPass renderPass) {
		renderPass.setIndexBuffer(ebo.getCurrentBuffer(), IndexType.INT);
	}

	@Override
	public void close() {
		ebo.close();
	}
}
