package dev.engine_room.flywheel.backend.engine.uniform;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.lwjgl.opengl.GL32;
import org.lwjgl.opengl.GL33C;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.opengl.GlBuffer;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;

import dev.engine_room.flywheel.api.backend.RenderContext;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;

public final class Uniforms {
	public static final FlwUniform[] UNIFORMS = {
			FrameUniforms.INSTANCE,
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

	@Deprecated
	public static void bindAll(int programId) {
		for (FlwUniform uniform : UNIFORMS) {
			int index = GL33C.glGetUniformBlockIndex(programId, uniform.getUniformName());
			GlBuffer glBuffer = (GlBuffer) uniform.getBuffer();
			GL32.glBindBufferRange(GL32.GL_UNIFORM_BUFFER, index, glBuffer.handle(), 0, glBuffer.size());
		}
	}

	public static void bindToRenderPass(RenderPass renderPass) {
		GpuBufferSlice fogUBO = RenderSystem.getShaderFog();
		if (fogUBO != null) {
			renderPass.setUniform("_FlwFogUniforms", fogUBO);
		}

		for (FlwUniform uniform : UNIFORMS) {
			renderPass.setUniform(uniform.getUniformName(), uniform.getBuffer());
		}
	}

	@Internal
	public static void closeAll() {
		for (FlwUniform uniform : UNIFORMS) {
			uniform.close();
		}
	}
}
