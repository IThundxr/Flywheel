package dev.engine_room.flywheel.backend.engine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jspecify.annotations.Nullable;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.systems.RenderPass;

import dev.engine_room.flywheel.api.model.Mesh;
import dev.engine_room.flywheel.backend.InternalVertex;
import dev.engine_room.flywheel.backend.util.ReferenceCounted;
import dev.engine_room.flywheel.lib.memory.MemoryBlock;
import dev.engine_room.flywheel.lib.vertex.VertexView;

public class MeshPool implements AutoCloseable {
	private final VertexView vertexView;
	private final Map<Mesh, PooledMesh> meshes = new HashMap<>();
	private final List<PooledMesh> meshList = new ArrayList<>();
	private final List<PooledMesh> recentlyAllocated = new ArrayList<>();

	private final DynamicGpuBuffer vbo;
	private final IndexPool indexPool;

	private boolean dirty;
	private boolean anyToRemove;

	/**
	 * Create a new mesh pool.
	 */
	public MeshPool() {
		vertexView = InternalVertex.createVertexView();
		// TODO b3d-ification: Check if the default size needs to be bigger
		vbo = new DynamicGpuBuffer(
				"Flywheel MeshPool VBO",
				GpuBuffer.USAGE_MAP_WRITE | GpuBuffer.USAGE_HINT_CLIENT_STORAGE | GpuBuffer.USAGE_COPY_DST | GpuBuffer.USAGE_VERTEX,
				1024 * 16 // 16 MB
		);
		indexPool = new IndexPool();
	}

	/**
	 * Allocate a model in the arena.
	 *
	 * @param mesh The model to allocate.
	 * @return A handle to the allocated model.
	 */
	public PooledMesh alloc(Mesh mesh) {
		return meshes.computeIfAbsent(mesh, this::_alloc);
	}

	private PooledMesh _alloc(Mesh m) {
		PooledMesh bufferedModel = new PooledMesh(m);
		meshList.add(bufferedModel);
		recentlyAllocated.add(bufferedModel);

		dirty = true;
		return bufferedModel;
	}

	public MeshPool.@Nullable PooledMesh get(Mesh mesh) {
		return meshes.get(mesh);
	}

	public void flush() {
        if (!dirty) {
            return;
        }

		if (anyToRemove) {
			anyToRemove = false;
			processDeletions();
		}

		if (!recentlyAllocated.isEmpty()) {
			// Otherwise, just update the index with the new counts.
			for (PooledMesh mesh : recentlyAllocated) {
				indexPool.updateCount(mesh.mesh.indexSequence(), mesh.indexCount());
			}
			indexPool.flush();
			recentlyAllocated.clear();
		}

		uploadAll();
        dirty = false;
    }

	private void processDeletions() {
		// remove deleted meshes
		meshList.removeIf(pooledMesh -> {
			boolean deleted = pooledMesh.isDeleted();
			if (deleted) {
				meshes.remove(pooledMesh.mesh);
			}
			return deleted;
		});
	}

	private void uploadAll() {
		long neededSize = 0;
		for (PooledMesh mesh : meshList) {
			neededSize += mesh.byteSize();
		}

		final var vertexBlock = MemoryBlock.malloc(neededSize);
		final long vertexPtr = vertexBlock.ptr();

		int byteIndex = 0;
		int baseVertex = 0;
		for (PooledMesh mesh : meshList) {
			mesh.baseVertex = baseVertex;

			vertexView.ptr(vertexPtr + byteIndex);
			vertexView.vertexCount(mesh.vertexCount());
			mesh.mesh.write(vertexView);

			byteIndex += mesh.byteSize();
			baseVertex += mesh.vertexCount();
		}

		vbo.write(vertexBlock.asBuffer());
		vertexBlock.free();
	}

	public void bindToRenderPass(RenderPass renderPass) {
		renderPass.setVertexBuffer(0, vbo.getCurrentBuffer().slice());
		indexPool.bindToRenderPass(renderPass);
	}

	public List<PooledMesh> pooledMeshes() {
		return meshList;
	}

	@Override
	public void close() {
		vbo.close();
		indexPool.close();
		meshes.clear();
		meshList.clear();
	}

	public class PooledMesh extends ReferenceCounted {
		public static final int INVALID_BASE_VERTEX = -1;

		private final Mesh mesh;
		private int baseVertex = INVALID_BASE_VERTEX;

		private PooledMesh(Mesh mesh) {
			this.mesh = mesh;
		}

		public int vertexCount() {
			return mesh.vertexCount();
		}

		public int byteSize() {
			return mesh.vertexCount() * InternalVertex.FORMAT.getVertexSize();
		}

		public int indexCount() {
			return mesh.indexCount();
		}

		public int baseVertex() {
			return baseVertex;
		}

		public int firstIndex() {
			return MeshPool.this.indexPool.firstIndex(mesh.indexSequence());
		}

		public int firstIndexByteOffset() {
			return firstIndex() * Integer.BYTES;
		}

		public boolean isInvalid() {
			return mesh.vertexCount() == 0 || baseVertex == INVALID_BASE_VERTEX || isDeleted();
		}

		// TODO b3d-ification: We should probably submit RenderPass.Draw calls instead of doing these one by one
		public void submitDraw(RenderPass renderPass, int instanceCount) {
			renderPass.drawIndexed(baseVertex, firstIndexByteOffset(), mesh.indexCount(), instanceCount);
		}

		@Override
		protected void _delete() {
			MeshPool.this.dirty = true;
			MeshPool.this.anyToRemove = true;
		}
	}
}
