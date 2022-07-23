package com.jozufozu.flywheel.backend.instancing;

import com.jozufozu.flywheel.api.RenderStage;
import com.jozufozu.flywheel.core.RenderContext;

import net.minecraft.client.Camera;

public interface RenderDispatcher {

	void renderStage(TaskEngine taskEngine, RenderContext context, RenderStage stage);

	/**
	 * Maintain the integer origin coordinate to be within a certain distance from the camera in all directions,
	 * preventing floating point precision issues at high coordinates.
	 * @return {@code true} if the origin coordinate was changed, {@code false} otherwise.
	 */
	boolean maintainOriginCoordinate(Camera camera);

	void beginFrame(TaskEngine taskEngine, RenderContext context);

	void delete();
}
