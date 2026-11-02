package dev.engine_room.flywheel.lib.model.baked;

import org.jetbrains.annotations.ApiStatus;

import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadView;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.core.Direction;

@ApiStatus.Internal
public class FabricMeshEmitterManager extends MeshEmitterManager<FabricMeshEmitter> {
	private boolean useAo;
	private boolean defaultAo;

	FabricMeshEmitterManager() {
		super(FabricMeshEmitter::new);
	}

	public void prepareForModel(boolean useAo, boolean defaultAo) {
		this.useAo = useAo;
		this.defaultAo = defaultAo;
	}

	public void prepareForGeometry(QuadView quad) {
		// TODO - Look over this/port this
		boolean shade = quad.shadeDirectionOverride() != Direction.UP;
		TriState aoMode = quad.ambientOcclusion();
		boolean ao = useAo && aoMode.orElse(defaultAo);

		for (FabricMeshEmitter emitter : emitterMap.values()) {
			emitter.prepareForGeometry(shade, ao);
		}
	}
}
