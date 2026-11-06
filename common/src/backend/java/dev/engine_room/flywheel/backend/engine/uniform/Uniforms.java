package dev.engine_room.flywheel.backend.engine.uniform;

import java.util.List;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Unmodifiable;
import org.lwjgl.opengl.GL33C;

import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.commands.RenderPass;

import dev.engine_room.flywheel.api.backend.RenderContext;
import dev.engine_room.flywheel.backend.gl.GlUtil;
import dev.engine_room.flywheel.backend.gl.shader.GlProgram;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;

public final class Uniforms {
	@Unmodifiable
	public static final List<FlwUniform> UNIFORMS = List.of(
			FrameUniforms.INSTANCE,
			FogUniforms.INSTANCE,
			OptionsUniforms.INSTANCE,
			PlayerUniforms.INSTANCE,
			LevelUniforms.INSTANCE
	);

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

	@Deprecated(forRemoval = true)
	public static void bindAll() {
		for (int i = 0; i < UNIFORMS.size(); i++) {
			FlwUniform uniform = UNIFORMS.get(i);
			GpuBuffer buffer = uniform.getBuffer();

			GL33C.glBindBufferRange(GL33C.GL_UNIFORM_BUFFER, i, GlUtil.getHandle(buffer), 0, buffer.size());
		}
	}

	@Deprecated(forRemoval = true)
	public static void setUniformBlockBindings(GlProgram program) {
		for (int i = 0; i < UNIFORMS.size(); i++) {
			FlwUniform uniform = UNIFORMS.get(i);

			program.setUniformBlockBinding(uniform.getUniformName(), i);
		}
	}

	@Internal
	public static void closeAll() {
		for (FlwUniform uniform : UNIFORMS) {
			uniform.close();
		}
	}
}
