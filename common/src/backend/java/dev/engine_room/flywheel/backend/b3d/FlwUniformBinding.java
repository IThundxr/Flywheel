package dev.engine_room.flywheel.backend.b3d;

import org.joml.Matrix3fc;
import org.joml.Matrix4fc;
import org.lwjgl.opengl.GL33C;

import com.mojang.blaze3d.systems.RenderPass;

public sealed interface FlwUniformBinding {
	String name();

	void bindOpenGL(int location);

	default void bindVulkan(int location) {
		throw new UnsupportedOperationException();
	}

	default void set(RenderPass renderPass) {
		renderPass.flywheel$setPlainUniform(this);
	}

	record UIntUniform(String name, int value) implements FlwUniformBinding {
		@Override
		public void bindOpenGL(int location) {
			GL33C.glUniform1ui(location, value);
		}
	}

	record UVec2(String name, int x, int y) implements FlwUniformBinding {
		@Override
		public void bindOpenGL(int location) {
			GL33C.glUniform2ui(location, x, y);
		}
	}

	record Mat4(String name, Matrix4fc value) implements FlwUniformBinding {
		@Override
		public void bindOpenGL(int location) {
			GL33C.glUniformMatrix4fv(location, false, value.get(new float[16]));
		}
	}

	record Mat3(String name, Matrix3fc value) implements FlwUniformBinding {
		@Override
		public void bindOpenGL(int location) {
			GL33C.glUniformMatrix3fv(location, false, value.get(new float[9]));
		}
	}
}
