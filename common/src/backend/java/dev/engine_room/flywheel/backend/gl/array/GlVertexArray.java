package dev.engine_room.flywheel.backend.gl.array;

import org.lwjgl.opengl.GL32;
import org.lwjgl.opengl.GL33C;

import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.opengl.VertexArrayCache;
import com.mojang.blaze3d.vertex.VertexFormat;

import dev.engine_room.flywheel.backend.gl.GlObject;
import dev.engine_room.flywheel.backend.gl.GlStateTracker;

/// This entirely needs to go and be replaced with {@link VertexArrayCache} and render passes
@Deprecated(forRemoval = true)
public abstract class GlVertexArray extends GlObject {
	protected static final int MAX_ATTRIBS = GlStateManager._getInteger(GL32.GL_MAX_VERTEX_ATTRIBS);
	protected static final int MAX_ATTRIB_BINDINGS = 16;

	public static GlVertexArray create() {
		if (GlVertexArrayDSA.SUPPORTED) {
			return new GlVertexArrayDSA();
		} else if (GlVertexArraySeparateAttributes.SUPPORTED) {
			return new GlVertexArraySeparateAttributes();
		} else if (GlVertexArrayGL3.Core33.SUPPORTED) {
			return new GlVertexArrayGL3.Core33();
		} else if (GlVertexArrayGL3.ARB.SUPPORTED) {
			return new GlVertexArrayGL3.ARB();
		} else {
			return new GlVertexArrayGL3.Core();
		}
	}

	public void bindForDraw() {
		GlStateTracker.bindVao(handle());
	}

	public abstract void bindVertexBuffer(int bindingIndex, int vbo, long offset, int stride);

	public abstract void setBindingDivisor(int bindingIndex, int divisor);

	public abstract void bindAttributes(int bindingIndex, int startAttribIndex, VertexFormat vertexFormat);

	public abstract void setElementBuffer(int ebo);

	@Override
	protected void deleteInternal(int handle) {
		GL33C.glDeleteVertexArrays(handle);
	}
}
