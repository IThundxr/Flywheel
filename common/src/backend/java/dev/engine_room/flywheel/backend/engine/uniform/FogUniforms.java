package dev.engine_room.flywheel.backend.engine.uniform;

import java.nio.ByteBuffer;

import org.joml.Vector4fc;
import org.lwjgl.system.MemoryStack;

import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.buffers.GpuBuffer;

public final class FogUniforms implements FlwUniform {
	private static final int UBO_SIZE = new Std140SizeCalculator()
			.putVec4() // color
			.putFloat() // environmentalStart
			.putFloat() // environmentalEnd
			.putFloat() // renderDistanceStart
			.putFloat() // renderDistanceEnd
			.putFloat() // skyEnd
			.putFloat() // cloudEnd
			.get();

	public static final FogUniforms INSTANCE = new FogUniforms();

	private final GpuBuffer buffer = RenderSystem.getDevice().createBuffer(
			() -> "Flw Fog UBO",
			GpuBuffer.USAGE_MAP_WRITE | GpuBuffer.USAGE_HINT_CLIENT_STORAGE | GpuBuffer.USAGE_COPY_DST | GpuBuffer.USAGE_UNIFORM,
			UBO_SIZE
	);

	private FogUniforms() {
	}

	public void update(Vector4fc color, float environmentalStart, float environmentalEnd,
							  float renderDistanceStart, float renderDistanceEnd, float skyEnd, float cloudEnd) {
		try (MemoryStack stack = MemoryStack.stackPush()) {
			ByteBuffer byteBuffer = Std140Builder.onStack(stack, UBO_SIZE)
					.putVec4(color)
					.putFloat(environmentalStart)
					.putFloat(environmentalEnd)
					.putFloat(renderDistanceStart)
					.putFloat(renderDistanceEnd)
					.putFloat(skyEnd)
					.putFloat(cloudEnd)
					.get();

			RenderSystem.getDevice().createCommandEncoder().writeToBuffer(buffer.slice(), byteBuffer);
		}
	}

	@Override
	public String getUniformName() {
		return "_FlwFogUniforms";
	}

	@Override
	public GpuBuffer getBuffer() {
		return buffer;
	}

	@Override
	public void close() {
		buffer.close();
	}
}
