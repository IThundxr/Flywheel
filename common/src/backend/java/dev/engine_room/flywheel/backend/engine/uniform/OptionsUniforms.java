package dev.engine_room.flywheel.backend.engine.uniform;

import java.nio.ByteBuffer;

import org.lwjgl.system.MemoryStack;

import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Options;

// TODO - Use GpuBuffer instead of MRB here
public final class OptionsUniforms implements FlwUniform {
	public static final int UBO_SIZE = new Std140SizeCalculator()
			.putFloat() // gamma
			.putInt()   // fov
			.putFloat() // screenEffectScale
			.putFloat() // glintSpeed
			.putFloat() // glintStrength
			.putInt()   // biomeBlendRadius
			.putInt()   // ambientOcclusion
			.putInt()   // bobView
			.putInt()   // highContrast
			.putFloat() // textBackgroundOpacity
			.putInt()   // backgroundForChatOnly
			.putFloat() // darknessEffectScale
			.putFloat() // damageTiltStrength
			.putInt()   // hideLightningFlash
			.get();

	public static final OptionsUniforms INSTANCE = new OptionsUniforms();

	private final GpuBuffer buffer = RenderSystem.getDevice().createBuffer(
			() -> "Flw Options UBO",
			GpuBuffer.USAGE_MAP_WRITE | GpuBuffer.USAGE_HINT_CLIENT_STORAGE | GpuBuffer.USAGE_COPY_DST | GpuBuffer.USAGE_UNIFORM,
			UBO_SIZE
	);

	private OptionsUniforms() {
	}

	@Override
	public String getUniformName() {
		return "_FlwOptionsUniforms";
	}

	@Override
	public GpuBuffer getBuffer() {
		return buffer;
	}

	@Override
	public void close() {
		buffer.close();
	}

	public void update(Options options) {
		try (MemoryStack stack = MemoryStack.stackPush()) {
			ByteBuffer byteBuffer = Std140Builder.onStack(stack, UBO_SIZE)
					.putFloat(options.gamma().get().floatValue())
					.putInt(options.fov().get())
					.putFloat(options.screenEffectScale().get().floatValue())
					.putFloat(options.glintSpeed().get().floatValue())
					.putFloat(options.glintStrength().get().floatValue())
					.putInt(options.biomeBlendRadius().get())
					.putInt(options.ambientOcclusion().get() ? 1 : 0)
					.putInt(options.bobView().get() ? 1 : 0)
					.putInt(options.highContrast().get() ? 1 : 0)
					.putFloat(options.textBackgroundOpacity().get().floatValue())
					.putInt(options.backgroundForChatOnly().get() ? 1 : 0)
					.putFloat(options.darknessEffectScale().get().floatValue())
					.putFloat(options.damageTiltStrength().get().floatValue())
					.putInt(options.hideLightningFlash().get() ? 1 : 0)
					.get();

			RenderSystem.getDevice().createCommandEncoder().writeToBuffer(buffer.slice(), byteBuffer);
		}
	}
}
