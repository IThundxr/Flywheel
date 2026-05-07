package dev.engine_room.flywheel.lib.model.baked;

import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.client.resources.model.geometry.BakedQuad;

class FabricMeshEmitterManager extends MeshEmitterManager<FabricMeshEmitter> {
	private boolean useAo;
	private boolean defaultAo;

	FabricMeshEmitterManager() {
		super(FabricMeshEmitter::new);
	}

	public void prepareForModel(boolean useAo, boolean defaultAo) {
		this.useAo = useAo;
		this.defaultAo = defaultAo;
	}

	public void prepareForGeometry(BakedQuad quad) {
		boolean shade = quad.materialInfo().shade();
		TriState aoMode = TriState.DEFAULT;
		boolean ao = useAo && aoMode.orElse(defaultAo);

		for (FabricMeshEmitter emitter : emitterMap.values()) {
			emitter.prepareForGeometry(shade, ao);
		}
	}
}
