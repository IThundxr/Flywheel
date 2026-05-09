package dev.engine_room.flywheel.backend.engine.uniform;

import com.mojang.blaze3d.systems.RenderPass;

import dev.engine_room.flywheel.api.backend.RenderContext;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;

public final class Uniforms {
	public static final FlwUniform[] UNIFORMS = {
			FrameUniforms.INSTANCE,
			FogUniforms.INSTANCE,
			OptionsUniforms.INSTANCE,
			PlayerUniforms.INSTANCE,
			LevelUniforms.INSTANCE
	};

	private Uniforms() {
	}

	public static void update(RenderContext context) {
		ProfilerFiller profiler = Profiler.get();
		profiler.push("flw_update_uniforms");
		FrameUniforms.INSTANCE.update(context);
		PlayerUniforms.INSTANCE.update(context);
		LevelUniforms.INSTANCE.update(context);
		profiler.pop();
	}

	public static void bindToRenderPass(RenderPass renderPass) {
		for (FlwUniform uniform : UNIFORMS) {
			renderPass.setUniform(uniform.getUniformName(), uniform.getBuffer());
		}
	}

	// TODO b3d-ification: This needs to be hooked up to close these buffers on shutdown
	private static void closeAll() {
		for (FlwUniform uniform : UNIFORMS) {
			uniform.close();
		}
	}
}
