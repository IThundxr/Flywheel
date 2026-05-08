package dev.engine_room.flywheel.backend.engine.instancing;

import com.mojang.blaze3d.systems.RenderPass;

import dev.engine_room.flywheel.api.material.Material;
import dev.engine_room.flywheel.backend.engine.GroupKey;
import dev.engine_room.flywheel.backend.engine.MeshPool;

public class InstancedDraw {
	public final GroupKey<?> groupKey;
	private final InstancedInstancer<?> instancer;
	private final MeshPool.PooledMesh mesh;
	private final Material material;
	private final int bias;
	private final int indexOfMeshInModel;

	private boolean deleted;

	public InstancedDraw(InstancedInstancer<?> instancer, MeshPool.PooledMesh mesh, GroupKey<?> groupKey, Material material, int bias, int indexOfMeshInModel) {
		this.instancer = instancer;
		this.mesh = mesh;
		this.groupKey = groupKey;
		this.material = material;
		this.bias = bias;
		this.indexOfMeshInModel = indexOfMeshInModel;

		mesh.acquire();
	}

	public int bias() {
		return bias;
	}

	public int indexOfMeshInModel() {
		return indexOfMeshInModel;
	}

	public Material material() {
		return material;
	}

	public boolean deleted() {
		return deleted;
	}

	public MeshPool.PooledMesh mesh() {
		return mesh;
	}

	public void render(RenderPass renderPass) {
		if (mesh.isInvalid()) {
			return;
		}

		instancer.bindToRenderPass(renderPass);

		mesh.submitDraw(renderPass, instancer.instanceCount());
	}

	public void renderOne(RenderPass renderPass) {
		if (mesh.isInvalid()) {
			return;
		}

		instancer.bindToRenderPass(renderPass);

		mesh.submitDraw(renderPass, 1);
	}

	public void delete() {
		if (deleted) {
			return;
		}

		mesh.release();

		deleted = true;
	}
}
