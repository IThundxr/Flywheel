package dev.engine_room.flywheel.backend.compile.core;

import java.util.List;
import java.util.function.Consumer;

import org.lwjgl.opengl.GL33C;

import com.mojang.renderpearl.backend.opengl.GlConst;

import dev.engine_room.flywheel.backend.gl.shader.GlProgram;
import dev.engine_room.flywheel.backend.gl.shader.GlShader;

public class ProgramLinker {

	public ProgramLinker() {
	}

	public GlProgram link(List<GlShader> shaders, Consumer<GlProgram> preLink) {
		// this probably doesn't need caching
		return linkInternal(shaders, preLink).unwrap();
	}

	private LinkResult linkInternal(List<GlShader> shaders, Consumer<GlProgram> preLink) {
		int handle = GL33C.glCreateProgram();
		var out = new GlProgram(handle);

		for (GlShader shader : shaders) {
			GL33C.glAttachShader(handle, shader.handle());
		}

		preLink.accept(out);

		GL33C.glLinkProgram(handle);
		String log = GL33C.glGetProgramInfoLog(handle, com.mojang.renderpearl.backend.opengl.GlProgram.MAX_LOG_LENGTH);

		if (linkSuccessful(handle)) {
			return LinkResult.success(out, log);
		} else {
			out.delete();
			return LinkResult.failure(log);
		}
	}

	private static boolean linkSuccessful(int handle) {
		return GL33C.glGetProgrami(handle, GlConst.GL_LINK_STATUS) == GlConst.GL_TRUE;
	}

}
