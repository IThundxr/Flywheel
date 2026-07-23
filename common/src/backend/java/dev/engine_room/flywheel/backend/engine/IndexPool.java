package dev.engine_room.flywheel.backend.engine;

import java.nio.ByteBuffer;

import org.lwjgl.system.MemoryStack;

import com.mojang.renderpearl.api.pipeline.IndexType;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.backend.opengl.GlBuffer;
import com.mojang.renderpearl.api.commands.RenderPass;

import dev.engine_room.flywheel.api.model.IndexSequence;
import dev.engine_room.flywheel.backend.engine.indirect.deprecated.gl.array.GlVertexArray;
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
				"Flw IndexPool EBO",
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

		int totalIndexCount = 0;

		for (int count : indexCounts.values()) {
			totalIndexCount += count;
		}

		try (MemoryStack stack = MemoryStack.stackPush()) {
			ByteBuffer buffer = stack.malloc(totalIndexCount * Integer.BYTES);

			int firstIndex = 0;
			for (Reference2IntMap.Entry<IndexSequence> entries : indexCounts.reference2IntEntrySet()) {
				var indexSequence = entries.getKey();
				var indexCount = entries.getIntValue();

				firstIndices.put(indexSequence, firstIndex);

				indexSequence.fill(buffer, indexCount);

				firstIndex += indexCount;
			}

			ebo.write(buffer.flip());
		}
	}

	public void bindToRenderPass(RenderPass renderPass) {
		renderPass.setIndexBuffer(ebo.getCurrentBuffer(), IndexType.INT);
	}

	@Deprecated
	public void bind(GlVertexArray vertexArray) {
		GlBuffer glBuffer = (GlBuffer) ebo.getCurrentBuffer();
		vertexArray.setElementBuffer(glBuffer.handle());
	}

	@Override
	public void close() {
		ebo.close();
	}
}
