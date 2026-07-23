package dev.engine_room.flywheel.backend.b3d;

import java.nio.ByteBuffer;

import org.joml.Matrix3fc;
import org.joml.Matrix4fc;
import org.lwjgl.opengl.GL33C;

import com.mojang.blaze3d.systems.RenderPass;

public sealed interface FlwUniformBinding {
	String name();

	void bindOpenGL(int location);

	void writeVulkan(ByteBuffer buffer);

	default void set(RenderPass renderPass) {
		renderPass.flywheel$setPlainUniform(this);
	}

	record IntUniform(String name, int value) implements FlwUniformBinding {
		@Override
		public void bindOpenGL(int location) {
			GL33C.glUniform1i(location, value);
		}

		@Override
		public void writeVulkan(ByteBuffer buffer) {
			buffer.putInt(value);
		}
	}

	record UIntUniform(String name, int value) implements FlwUniformBinding {
		@Override
		public void bindOpenGL(int location) {
			GL33C.glUniform1ui(location, value);
		}

		@Override
		public void writeVulkan(ByteBuffer buffer) {
			buffer.putInt(value);
		}
	}

	record UVec2(String name, int x, int y) implements FlwUniformBinding {
		@Override
		public void bindOpenGL(int location) {
			GL33C.glUniform2ui(location, x, y);
		}

		@Override
		public void writeVulkan(ByteBuffer buffer) {
			buffer.putInt(x);
			buffer.putInt(y);
		}
	}

	record Mat4(String name, Matrix4fc value) implements FlwUniformBinding {
		@Override
		public void bindOpenGL(int location) {
			GL33C.glUniformMatrix4fv(location, false, value.get(new float[16]));
		}

		@Override
		public void writeVulkan(ByteBuffer buffer) {
			value.get(buffer);
			buffer.position(buffer.position() + (16 * Float.BYTES));
		}
	}

	record Mat3(String name, Matrix3fc value) implements FlwUniformBinding {
		@Override
		public void bindOpenGL(int location) {
			GL33C.glUniformMatrix3fv(location, false, value.get(new float[9]));
		}

		// TODO - Mat3 is weird, this might be wrong
		@Override
		public void writeVulkan(ByteBuffer buffer) {
			value.get(buffer);
			buffer.position(buffer.position() + (9 * Float.BYTES));
		}
	}
}
