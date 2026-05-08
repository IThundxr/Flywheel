package dev.engine_room.flywheel.backend.engine.uniform;

import java.nio.ByteBuffer;

import org.joml.Vector4fc;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice.MappedView;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;

import net.minecraft.client.renderer.MappableRingBuffer;

// Can be relaced by vanilla fog uniform
// Mojang uses a MRB, but this is only updated once in renderLevel, do we really need a MRB here?
public final class FogUniforms implements FlwUniform {
	private static final int UBO_SIZE = new Std140SizeCalculator()
			.putVec4()  // color
			.putFloat() // environmentalStart
			.putFloat() // environmentalEnd
			.putFloat() // renderDistanceStart
			.putFloat() // renderDistanceEnd
			.putFloat() // skyEnd
			.putFloat() // cloudEnd
			.get();

	public static final FogUniforms INSTANCE = new FogUniforms();

	private final MappableRingBuffer buffer = new MappableRingBuffer(
			() -> "Flw Fog UBO",
			GpuBuffer.USAGE_MAP_WRITE | GpuBuffer.USAGE_HINT_CLIENT_STORAGE | GpuBuffer.USAGE_COPY_DST | GpuBuffer.USAGE_UNIFORM,
			UBO_SIZE
	);

	private FogUniforms() {
	}

	@Override
	public String getUniformName() {
		return "_FlwFogUniforms";
	}

	@Override
	public GpuBuffer getBuffer() {
		return buffer.currentBuffer();
	}

	@Override
	public void close() {
		buffer.close();
	}

	public void update(Vector4fc color, float environmentalStart, float environmentalEnd,
					   float renderDistanceStart, float renderDistanceEnd, float skyEnd, float cloudEnd) {
		buffer.rotate();
		try (MappedView view = buffer.currentBuffer().map(false, true)) {
			ByteBuffer byteBuffer = view.data();
			byteBuffer.position(0);
			Std140Builder.intoBuffer(byteBuffer)
					.putVec4(color)
					.putFloat(environmentalStart)
					.putFloat(environmentalEnd)
					.putFloat(renderDistanceStart)
					.putFloat(renderDistanceEnd)
					.putFloat(skyEnd)
					.putFloat(cloudEnd);
		}
	}
}
