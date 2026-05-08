package dev.engine_room.flywheel.backend.extension.b3d;

import java.util.Map;

import dev.engine_room.flywheel.backend.b3d.FlwUniformBinding;

public interface FlwGlVulkanRenderPassExtension {
	Map<String, FlwUniformBinding> flywheel$getUniformBindings();
}
