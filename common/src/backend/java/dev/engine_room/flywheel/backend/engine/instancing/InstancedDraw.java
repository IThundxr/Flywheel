package dev.engine_room.flywheel.backend.engine.instancing;

import com.mojang.renderpearl.api.commands.RenderPass;

import dev.engine_room.flywheel.api.material.Material;
import dev.engine_room.flywheel.backend.engine.GroupKey;
import dev.engine_room.flywheel.backend.engine.MaterialRenderState;
import dev.engine_room.flywheel.backend.engine.MeshPool;
import net.minecraft.client.renderer.texture.TextureHandle;

public class InstancedDraw {
	public final GroupKey<?> groupKey;
	private final InstancedInstancer<?> instancer;
	private final MeshPool.PooledMesh mesh;
	private final Material material;
	private final int bias;
	private final int indexOfMeshInModel;

	// TODO - Check if your allowed to create/upload textures inside of a pass, was previously bugged hence why the
	//  texture is stored in the InstancedDraw object
	private final TextureHandle textureHandle;

	private boolean deleted;

	public InstancedDraw(InstancedInstancer<?> instancer, MeshPool.PooledMesh mesh, GroupKey<?> groupKey, Material material, int bias, int indexOfMeshInModel) {
		this.instancer = instancer;
		this.mesh = mesh;
		this.groupKey = groupKey;
		this.material = material;
		this.bias = bias;
		this.indexOfMeshInModel = indexOfMeshInModel;

		this.textureHandle = MaterialRenderState.createTextureHandle(material);

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

	public TextureHandle getTextureHandle() {
		return textureHandle;
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
