package dev.engine_room.flywheel.backend.engine.uniform;

import org.joml.Math;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;

import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.systems.RenderSystem;

import dev.engine_room.flywheel.api.backend.RenderContext;
import dev.engine_room.flywheel.api.visualization.VisualizationManager;
import dev.engine_room.flywheel.backend.engine.indirect.DepthPyramid;
import dev.engine_room.flywheel.backend.extension.CameraRenderStateExtension;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Util;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class FrameUniforms implements FlwUniform {
	private static final int UBO_SIZE = new Std140SizeCalculator()
			.putVec4()  // <nx.x, px.x, ny.x, py.x>
			.putVec4()  // <nx.y, px.y, ny.y, py.y>
			.putVec4()  // <nx.z, px.z, ny.z, py.z>
			.putVec4()  // <nx.w, px.w, ny.w, py.w>
			.putVec2()  // <nz.x, pz.x>
			.putVec2()  // <nz.y, pz.y>
			.putVec2()  // <nz.z, pz.z>
			.putVec2()  // <nz.w, pz.w>
			.putFloat() // zNear
			.putFloat() // zFar
			.putFloat() // P00
			.putFloat() // P11
			.putFloat() // pyramidWidth
			.putFloat() // pyramidHeight
			.putInt()   // pyramidLevels
			.putInt()   // useMin
			.putMat4f() // view
			.putMat4f() // viewInverse
			.putMat4f() // viewPrev
			.putMat4f() // projection
			.putMat4f() // projectionInverse
			.putMat4f() // projectionPrev
			.putMat4f() // viewProjection
			.putMat4f() // viewProjectionInverse
			.putMat4f() // viewProjectionPrev
			.putIVec3() // renderOrigin
			.putVec4()  // cameraPos
			.putVec4()  // cameraPosPrev
			.putVec4()  // cameraLook
			.putVec4()  // cameraLookPrev
			.putVec4()  // cameraRot
			.putVec4()  // cameraRotPrev
			.putVec2()  // viewportSize
			.putFloat() // aspectRatio
			.putFloat() // defaultLineWidth
			.putFloat() // viewDistance
			.putInt()   // ticks
			.putFloat() // partialTick
			.putFloat() // renderTicks
			.putFloat() // renderSeconds
			.putFloat() // systemSeconds
			.putInt()   // systemMillis
			.putInt()   // cameraInFluid
			.putInt()   // cameraInBlock
			.putInt()   // debugMode
			.putFloat() // oitNoise
			.get();

	public static final FrameUniforms INSTANCE = new FrameUniforms();

	private final GpuBuffer buffer = RenderSystem.getDevice().createBuffer(
			() -> "Flw Frame UBO",
			GpuBuffer.USAGE_MAP_WRITE | GpuBuffer.USAGE_HINT_CLIENT_STORAGE | GpuBuffer.USAGE_COPY_DST | GpuBuffer.USAGE_UNIFORM,
			UBO_SIZE
	);

	private final Matrix4f view = new Matrix4f();
	private final Matrix4f viewInverse = new Matrix4f();
	private final Matrix4f viewPrev = new Matrix4f();
	private final Matrix4f projection = new Matrix4f();
	private final Matrix4f projectionInverse = new Matrix4f();
	private final Matrix4f projectionPrev = new Matrix4f();
	private final Matrix4f viewProjection = new Matrix4f();
	private final Matrix4f viewProjectionInverse = new Matrix4f();
	private final Matrix4f viewProjectionPrev = new Matrix4f();

	private final Vector3f cameraPos = new Vector3f();
	private final Vector3f cameraPosPrev = new Vector3f();
	private final Vector3f cameraLook = new Vector3f();
	private final Vector3f cameraLookPrev = new Vector3f();
	private final Vector2f cameraRot = new Vector2f();
	private final Vector2f cameraRotPrev = new Vector2f();

	private final float[] cachedFrustumPlanes = new float[24];

	private boolean firstWrite = true;

	private int debugMode = DebugMode.OFF.ordinal();
	private boolean frustumPaused = false;
	private boolean frustumCapture = false;

	private FrameUniforms() {
	}

	@Override
	public String getUniformName() {
		return "_FlwFrameUniforms";
	}

	@Override
	public GpuBuffer getBuffer() {
		return buffer;
	}

	@Override
	public void close() {
		buffer.close();
	}

	public boolean debugOn() {
		return debugMode != DebugMode.OFF.ordinal();
	}

	public void debugMode(DebugMode mode) {
		debugMode = mode.ordinal();
	}

	public void captureFrustum() {
		frustumPaused = true;
		frustumCapture = true;
	}

	public void unpauseFrustum() {
		frustumPaused = false;
	}

	private void setPrev() {
		viewPrev.set(view);
		projectionPrev.set(projection);
		viewProjectionPrev.set(viewProjection);
		cameraPosPrev.set(cameraPos);
		cameraLookPrev.set(cameraLook);
		cameraRotPrev.set(cameraRot);
	}

	public void update(RenderContext context) {
		try (MemoryStack stack = MemoryStack.stackPush()) {
			Std140Builder builder = Std140Builder.onStack(stack, UBO_SIZE);
			setPrev();

			Vec3i renderOrigin = VisualizationManager.getOrThrow(context.level())
					.renderOrigin();
			CameraRenderState cameraRenderState = context.cameraRenderState();
			Vec3 cameraPos = cameraRenderState.pos;
			var camX = (float) (cameraPos.x - renderOrigin.getX());
			var camY = (float) (cameraPos.y - renderOrigin.getY());
			var camZ = (float) (cameraPos.z - renderOrigin.getZ());

			view.set(context.modelView());
			view.translate(-camX, -camY, -camZ);
			projection.set(context.projection());
			viewProjection.set(context.viewProjection());
			viewProjection.translate(-camX, -camY, -camZ);

			this.cameraPos.set(camX, camY, camZ);
			cameraLook.set(((CameraRenderStateExtension) cameraRenderState).flywheel$getForwardVector());
			cameraRot.set(cameraRenderState.xRot, cameraRenderState.yRot);

			if (firstWrite) {
				setPrev();
			}

			if (firstWrite || !frustumPaused || frustumCapture) {
				writePackedFrustumPlanes(viewProjection, cachedFrustumPlanes);
				frustumCapture = false;
			}

			putPackedFrustumPlanes(builder, cachedFrustumPlanes);

			putCullData(builder, cameraRenderState);

			putMatrices(builder);

			putRenderOrigin(builder, renderOrigin);

			putCamera(builder);

			var window = Minecraft.getInstance()
					.getWindow();
			builder.putVec2(window.getWidth(), window.getHeight());
			builder.putFloat((float) window.getWidth() / (float) window.getHeight());
			// default line width: net.minecraft.client.renderer.RenderStateShard.LineStateShard
			builder.putFloat(Math.max(2.5F, (float) window.getWidth() / 1920.0F * 2.5F));
			builder.putFloat(cameraRenderState.depthFar);

			putTime(builder, context);

			putCameraIn(builder, cameraRenderState);

			builder.putInt(debugMode);

			// OIT noise factor
			builder.putFloat(0.07f);

			firstWrite = false;
			RenderSystem.getDevice().createCommandEncoder().writeToBuffer(buffer.slice(), builder.get());
		}
	}

	private static void putPackedFrustumPlanes(Std140Builder builder, float[] input) {
		builder.putVec4(input[0],  input[1],  input[2],  input[3]);
		builder.putVec4(input[4],  input[5],  input[6],  input[7]);
		builder.putVec4(input[8],  input[9],  input[10], input[11]);
		builder.putVec4(input[12], input[13], input[14], input[15]);
		builder.putVec2(input[16], input[17]);
		builder.putVec2(input[18], input[19]);
		builder.putVec2(input[20], input[21]);
		builder.putVec2(input[22], input[23]);
	}

	private static void putRenderOrigin(Std140Builder builder, Vec3i renderOrigin) {
		builder.putIVec3(renderOrigin.getX(), renderOrigin.getY(), renderOrigin.getZ());
	}

	private void putMatrices(Std140Builder builder) {
		builder.putMat4f(view);
		builder.putMat4f(view.invert(viewInverse));
		builder.putMat4f(viewPrev);
		builder.putMat4f(projection);
		builder.putMat4f(projection.invert(projectionInverse));
		builder.putMat4f(projectionPrev);
		builder.putMat4f(viewProjection);
		builder.putMat4f(viewProjection.invert(viewProjectionInverse));
		builder.putMat4f(viewProjectionPrev);
	}

	private void putCamera(Std140Builder builder) {
		builder.putVec3(cameraPos.x, cameraPos.y, cameraPos.z);
		builder.putVec3(cameraPosPrev.x, cameraPosPrev.y, cameraPosPrev.z);
		builder.putVec3(cameraLook.x, cameraLook.y, cameraLook.z);
		builder.putVec3(cameraLookPrev.x, cameraLookPrev.y, cameraLookPrev.z);
		builder.putVec2(cameraRot.x, cameraRot.y);
		builder.putVec2(cameraRotPrev.x, cameraRotPrev.y);
	}

	private static void putTime(Std140Builder builder, RenderContext context) {
		// TODO - We probably shouldn't cast longs to ints like this
		int ticks = (int) context.levelRenderState().gameTime;
		float partialTick = context.partialTick();
		float renderTicks = ticks + partialTick;
		float renderSeconds = renderTicks / 20f;
		float systemSeconds = Util.getMillis() / 1000f;
		int systemMillis = (int) (Util.getMillis() % Integer.MAX_VALUE);

		builder.putInt(ticks);
		builder.putFloat(partialTick);
		builder.putFloat(renderTicks);
		builder.putFloat(renderSeconds);
		builder.putFloat(systemSeconds);
		builder.putInt(systemMillis);
	}

	private static void putCameraIn(Std140Builder builder, CameraRenderState cameraRenderState) {
		if (!cameraRenderState.initialized) {
			builder.putInt(0);
			builder.putInt(0);
			return;
		}

		Level level = Minecraft.getInstance().level;
		BlockPos blockPos = cameraRenderState.blockPos;
		Vec3 cameraPos = cameraRenderState.pos;
		FlwUniform.putInFluidAndBlock(builder, level, blockPos, cameraPos);
	}

	private void putCullData(Std140Builder builder, CameraRenderState cameraRenderState) {
		var mc = Minecraft.getInstance();
		var mainRenderTarget = mc.gameRenderer.mainRenderTarget();

		int pyramidWidth = DepthPyramid.mip0Size(mainRenderTarget.width);
		int pyramidHeight = DepthPyramid.mip0Size(mainRenderTarget.height);
		int pyramidDepth = DepthPyramid.getImageMipLevels(pyramidWidth, pyramidHeight);

		builder.putFloat(Camera.PROJECTION_Z_NEAR); // zNear
		builder.putFloat(cameraRenderState.depthFar); // zFar
		builder.putFloat(projection.m00()); // P00
		builder.putFloat(projection.m11()); // P11
		builder.putFloat(pyramidWidth); // pyramidWidth
		builder.putFloat(pyramidHeight); // pyramidHeight
		builder.putInt(pyramidDepth - 1); // pyramidLevels
		builder.putInt(0); // useMin
	}

	/**
	 * Writes the frustum planes of the given projection matrix to the given array.<p>
	 * Uses a different format that is friendly towards an optimized instruction-parallel
	 * implementation of sphere-frustum intersection.<p>
	 * The format is as follows:<p>
	 * {@code vec4(nxX, pxX, nyX, pyX)}<br>
	 * {@code vec4(nxY, pxY, nyY, pyY)}<br>
	 * {@code vec4(nxZ, pxZ, nyZ, pyZ)}<br>
	 * {@code vec4(nxW, pxW, nyW, pyW)}<br>
	 * {@code vec2(nzX, pzX)}<br>
	 * {@code vec2(nzY, pzY)}<br>
	 * {@code vec2(nzZ, pzZ)}<br>
	 * {@code vec2(nzW, pzW)}<br>
	 * <p>
	 *
	 * @param m      The projection matrix to compute the frustum planes for.
	 * @param output The array to write the planes to.
	 */
	private static void writePackedFrustumPlanes(Matrix4f m, float[] output) {
		float nxX, nxY, nxZ, nxW;
		float pxX, pxY, pxZ, pxW;
		float nyX, nyY, nyZ, nyW;
		float pyX, pyY, pyZ, pyW;
		float nzX, nzY, nzZ, nzW;
		float pzX, pzY, pzZ, pzW;

		float invl;
		nxX = m.m03() + m.m00();
		nxY = m.m13() + m.m10();
		nxZ = m.m23() + m.m20();
		nxW = m.m33() + m.m30();
		invl = Math.invsqrt(nxX * nxX + nxY * nxY + nxZ * nxZ);
		nxX *= invl;
		nxY *= invl;
		nxZ *= invl;
		nxW *= invl;

		pxX = m.m03() - m.m00();
		pxY = m.m13() - m.m10();
		pxZ = m.m23() - m.m20();
		pxW = m.m33() - m.m30();
		invl = Math.invsqrt(pxX * pxX + pxY * pxY + pxZ * pxZ);
		pxX *= invl;
		pxY *= invl;
		pxZ *= invl;
		pxW *= invl;

		nyX = m.m03() + m.m01();
		nyY = m.m13() + m.m11();
		nyZ = m.m23() + m.m21();
		nyW = m.m33() + m.m31();
		invl = Math.invsqrt(nyX * nyX + nyY * nyY + nyZ * nyZ);
		nyX *= invl;
		nyY *= invl;
		nyZ *= invl;
		nyW *= invl;

		pyX = m.m03() - m.m01();
		pyY = m.m13() - m.m11();
		pyZ = m.m23() - m.m21();
		pyW = m.m33() - m.m31();
		invl = Math.invsqrt(pyX * pyX + pyY * pyY + pyZ * pyZ);
		pyX *= invl;
		pyY *= invl;
		pyZ *= invl;
		pyW *= invl;

		nzX = m.m03() + m.m02();
		nzY = m.m13() + m.m12();
		nzZ = m.m23() + m.m22();
		nzW = m.m33() + m.m32();
		invl = Math.invsqrt(nzX * nzX + nzY * nzY + nzZ * nzZ);
		nzX *= invl;
		nzY *= invl;
		nzZ *= invl;
		nzW *= invl;

		pzX = m.m03() - m.m02();
		pzY = m.m13() - m.m12();
		pzZ = m.m23() - m.m22();
		pzW = m.m33() - m.m32();
		invl = Math.invsqrt(pzX * pzX + pzY * pzY + pzZ * pzZ);
		pzX *= invl;
		pzY *= invl;
		pzZ *= invl;
		pzW *= invl;

		output[0] = nxX;
		output[1] = pxX;
		output[2] = nyX;
		output[3] = pyX;
		output[4] = nxY;
		output[5] = pxY;
		output[6] = nyY;
		output[7] = pyY;
		output[8] = nxZ;
		output[9] = pxZ;
		output[10] = nyZ;
		output[11] = pyZ;
		output[12] = nxW;
		output[13] = pxW;
		output[14] = nyW;
		output[15] = pyW;
		output[16] = nzX;
		output[17] = pzX;
		output[18] = nzY;
		output[19] = pzY;
		output[20] = nzZ;
		output[21] = pzZ;
		output[22] = nzW;
		output[23] = pzW;
	}
}
