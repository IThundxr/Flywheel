package dev.engine_room.flywheel.backend.engine.instancing;

import com.mojang.datafixers.util.Pair;
import com.mojang.renderpearl.api.commands.RenderPass;
import com.mojang.renderpearl.api.textures.GpuSampler;

import dev.engine_room.flywheel.api.material.Material;
import dev.engine_room.flywheel.backend.engine.GroupKey;
import dev.engine_room.flywheel.backend.engine.MaterialRenderState;
import dev.engine_room.flywheel.backend.engine.MeshPool;
import net.minecraft.client.renderer.texture.AbstractTexture;

public class InstancedDraw {
	public final GroupKey<?> groupKey;
	private final InstancedInstancer<?> instancer;
	private final MeshPool.PooledMesh mesh;
	private final Material material;
	private final int bias;
	private final int indexOfMeshInModel;

	private final AbstractTexture texture;
	private final GpuSampler textureSampler;

	private boolean deleted;

	public InstancedDraw(InstancedInstancer<?> instancer, MeshPool.PooledMesh mesh, GroupKey<?> groupKey, Material material, int bias, int indexOfMeshInModel) {
		this.instancer = instancer;
		this.mesh = mesh;
		this.groupKey = groupKey;
		this.material = material;
		this.bias = bias;
		this.indexOfMeshInModel = indexOfMeshInModel;

		Pair<AbstractTexture, GpuSampler> texturePair = MaterialRenderState.createTextureView(material);
		this.texture = texturePair.getFirst();
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

	public boolean deleted() {
		return deleted;
	}

	public MeshPool.PooledMesh mesh() {
		return mesh;
	}

	public AbstractTexture getTexture() {
		return texture;
	}

	public GpuSampler getTextureSampler() {
		return textureSampler;
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
