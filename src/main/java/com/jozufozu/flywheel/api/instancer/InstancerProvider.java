package com.jozufozu.flywheel.api.instancer;

import com.jozufozu.flywheel.api.event.RenderStage;
import com.jozufozu.flywheel.api.model.Model;
import com.jozufozu.flywheel.api.struct.StructType;

import net.minecraft.core.Vec3i;

public interface InstancerProvider {
	/**
	 * Get an instancer for the given struct type, model, and render stage. Calling this method twice with the same arguments will return the same instancer.
	 *
	 * @return An instancer for the given struct type, model, and render stage.
	 */
	<D extends InstancedPart> Instancer<D> getInstancer(StructType<D> type, Model model, RenderStage stage);

	// TODO: this method does not belong in this interface
	Vec3i getOriginCoordinate();
}