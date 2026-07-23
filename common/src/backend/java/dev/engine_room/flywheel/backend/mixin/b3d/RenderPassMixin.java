package dev.engine_room.flywheel.backend.mixin.b3d;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.mojang.renderpearl.frontend.FrontendRenderPass;
import com.mojang.renderpearl.backend.api.RenderPassBackend;

import dev.engine_room.flywheel.backend.b3d.FlwUniformBinding;
import dev.engine_room.flywheel.backend.extension.b3d.FlwRenderPassExtension;

@Mixin(FrontendRenderPass.class)
public class RenderPassMixin implements FlwRenderPassExtension {
	@Shadow
	@Final
	private RenderPassBackend backend;

	@Override
	public void flywheel$setPlainUniform(FlwUniformBinding uniformBinding) {
		this.backend.flywheel$setPlainUniform(uniformBinding);
	}
}
