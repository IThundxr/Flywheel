package dev.engine_room.flywheel.backend.engine.uniform;

import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;

import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.systems.RenderSystem;

import dev.engine_room.flywheel.api.backend.RenderContext;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ARGB;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;

public final class LevelUniforms implements FlwUniform {
	private static final int UBO_SIZE = new Std140SizeCalculator()
			.putVec4()  // skyColor
			.putVec4()  // cloudColor
			.putVec3()  // light0Direction
			.putVec3()  // light1Direction
			.putInt()   // levelDay
			.putFloat() // timeOfDay
			.putInt()   // levelHasSkyLight
			.putFloat() // sunAngle
			.putFloat() // moonAngle
			.putFloat() // starAngle
			.putFloat() // moonBrightness
			.putInt()   // moonPhase
			.putFloat() // starBrightness
			.putInt()   // isRaining
			.putFloat() // rainLevel
			.putInt()   // isThundering
			.putFloat() // thunderLevel
			.putFloat() // skyDarken
			.putInt()   // cardinalLightType
			.putInt()   // dimension
			.get();

	public static final LevelUniforms INSTANCE = new LevelUniforms();

	private final GpuBuffer buffer = RenderSystem.getDevice().createBuffer(
			() -> "Flw Level UBO",
			GpuBuffer.USAGE_MAP_WRITE | GpuBuffer.USAGE_HINT_CLIENT_STORAGE | GpuBuffer.USAGE_COPY_DST | GpuBuffer.USAGE_UNIFORM,
			UBO_SIZE
	);

	public final Vector3f light0Direction = new Vector3f();
	public final Vector3f light1Direction = new Vector3f();

	private LevelUniforms() {
	}

	@Override
	public String getUniformName() {
		return "_FlwLevelUniforms";
	}

	@Override
	public GpuBuffer getBuffer() {
		return buffer;
	}

	@Override
	public void close() {
		buffer.close();
	}

	public void update(RenderContext context) {
		try (MemoryStack stack = MemoryStack.stackPush()) {
			Std140Builder builder = Std140Builder.onStack(stack, UBO_SIZE);

			ClientLevel level = context.level();
			float partialTick = context.partialTick();
			LevelRenderState levelRenderState = context.levelRenderState();

			int skyColor = levelRenderState.skyRenderState.skyColor;
			int cloudColor = levelRenderState.cloudColor;
			builder.putVec4(ARGB.redFloat(skyColor), ARGB.greenFloat(skyColor), ARGB.blueFloat(skyColor), 1f);
			builder.putVec4(ARGB.redFloat(cloudColor), ARGB.greenFloat(cloudColor), ARGB.blueFloat(cloudColor), 1f);

			builder.putVec3(light0Direction);
			builder.putVec3(light1Direction);

			long dayTime = level.getOverworldClockTime();
			long levelDay = dayTime / 24000L;
			float timeOfDay = (float) (dayTime - levelDay * 24000L) / 24000f;
			builder.putInt((int) (levelDay % 0x7FFFFFFFL));
			builder.putFloat(timeOfDay);

			builder.putInt(level.dimensionType().hasSkyLight() ? 1 : 0);

			float sunAngle = levelRenderState.skyRenderState.sunAngle;
			builder.putFloat(sunAngle);
			float moonAngle = levelRenderState.skyRenderState.moonAngle;
			builder.putFloat(moonAngle);
			float starAngle = levelRenderState.skyRenderState.starAngle;
			builder.putFloat(starAngle);

			int moonPhase = levelRenderState.skyRenderState.moonPhase.index();
			builder.putFloat(DimensionType.MOON_BRIGHTNESS_PER_PHASE[moonPhase]);
			builder.putInt(moonPhase);
			float starBrightness = levelRenderState.skyRenderState.starBrightness;
			builder.putFloat(starBrightness);

			builder.putInt(level.isRaining() ? 1 : 0);
			builder.putFloat(level.getRainLevel(partialTick));
			builder.putInt(level.isThundering() ? 1 : 0);
			builder.putFloat(level.getThunderLevel(partialTick));

			builder.putFloat(level.getSkyDarken());

			builder.putInt(level.dimensionType().cardinalLightType().ordinal());

			// TODO: use defines for custom dimension ids
			int dimensionId;
			ResourceKey<Level> dimension = level.dimension();
			if (Level.OVERWORLD.equals(dimension)) {
				dimensionId = 0;
			} else if (Level.NETHER.equals(dimension)) {
				dimensionId = 1;
			} else if (Level.END.equals(dimension)) {
				dimensionId = 2;
			} else {
				dimensionId = -1;
			}
			builder.putInt(dimensionId);

			RenderSystem.getDevice().createCommandEncoder().writeToBuffer(buffer.slice(), builder.get());
		}
    }
}
