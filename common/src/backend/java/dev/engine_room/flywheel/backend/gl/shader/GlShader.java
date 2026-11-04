package dev.engine_room.flywheel.backend.gl.shader;

import org.lwjgl.opengl.GL33C;

import dev.engine_room.flywheel.backend.gl.GlObject;

@Deprecated(forRemoval = true)
public class GlShader extends GlObject {

	public final ShaderType type;
	private final String name;

	public GlShader(int handle, ShaderType type, String name) {
		this.type = type;
		this.name = name;

		handle(handle);
	}

	@Override
	protected void deleteInternal(int handle) {
		GL33C.glDeleteShader(handle);
	}

	@Override
	public String toString() {
		return "GlShader{" + type.name + handle() + " " + name + "}";
	}

}
