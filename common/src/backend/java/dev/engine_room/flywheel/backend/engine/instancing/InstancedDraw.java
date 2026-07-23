package dev.engine_room.flywheel.backend.engine.instancing;

import com.mojang.renderpearl.api.commands.RenderPass;

import com.mojang.renderpearl.api.textures.GpuSampler;
import com.mojang.renderpearl.api.textures.GpuTextureView;

import com.mojang.datafixers.util.Pair;

import dev.engine_room.flywheel.api.material.Material;
import dev.engine_room.flywheel.backend.engine.GroupKey;
import dev.engine_room.flywheel.backend.engine.MaterialRenderState;
import dev.engine_room.flywheel.backend.engine.MeshPool;

public class InstancedDraw {
	public final GroupKey<?> groupKey;
	private final InstancedInstancer<?> instancer;
	private final MeshPool.PooledMesh mesh;
	private final Material material;
	private final int bias;
	private final int indexOfMeshInModel;

	// TODO - See comment on MaterialRenderState#createTextureView
	@Deprecated(forRemoval = true)
	private final GpuTextureView textureView;
	// TODO - See comment on MaterialRenderState#createTextureView
	@Deprecated(forRemoval = true)
	private final GpuSampler textureSampler;

	private boolean deleted;

	public InstancedDraw(InstancedInstancer<?> instancer, MeshPool.PooledMesh mesh, GroupKey<?> groupKey, Material material, int bias, int indexOfMeshInModel) {
		this.instancer = instancer;
		this.mesh = mesh;
		this.groupKey = groupKey;
		this.material = material;
		this.bias = bias;
		this.indexOfMeshInModel = indexOfMeshInModel;

		// TODO - See comment on MaterialRenderState#createTextureView
		Pair<GpuTextureView, GpuSampler> texturePair = MaterialRenderState.createTextureView(material);
		this.textureView = texturePair.getFirst();
		this.textureSampler = texturePair.getSecond();

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

	// TODO - See comment on MaterialRenderState#createTextureView
	@Deprecated(forRemoval = true)
	public GpuTextureView getTextureView() {
		return textureView;
	}

	// TODO - See comment on MaterialRenderState#createTextureView
	@Deprecated(forRemoval = true)
	public GpuSampler getTextureSampler() {
		return textureSampler;
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

		mesh.submitDraw(renderPass, instancer.instanceCount(), 0);
	}

	public void renderOne(RenderPass renderPass) {
		renderOne(renderPass, 0);
	}

	public void renderOne(RenderPass renderPass, int baseInstance) {
		if (mesh.isInvalid()) {
			return;
		}

		instancer.bindToRenderPass(renderPass);

		mesh.submitDraw(renderPass, 1, baseInstance);
	}

	public void delete() {
		if (deleted) {
			return;
		}

		mesh.release();

		deleted = true;
	}
}
