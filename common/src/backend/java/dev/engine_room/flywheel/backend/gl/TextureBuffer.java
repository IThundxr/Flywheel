package dev.engine_room.flywheel.backend.gl;

import org.lwjgl.opengl.GL32;

public class TextureBuffer extends GlObject {
	private final int format;

	public TextureBuffer() {
		this(GL32.GL_RGBA32UI);
	}

	public TextureBuffer(int format) {
		handle(GL32.glGenTextures());
		this.format = format;
	}

	public void bind(int buffer) {
		GL32.glBindTexture(GL32.GL_TEXTURE_BUFFER, handle());
		GL32.glTexBuffer(GL32.GL_TEXTURE_BUFFER, format, buffer);
	}

	@Override
	protected void deleteInternal(int handle) {
		GL32.glDeleteTextures(handle);
	}
}
