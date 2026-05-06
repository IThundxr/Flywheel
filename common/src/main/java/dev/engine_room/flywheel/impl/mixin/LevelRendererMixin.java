package dev.engine_room.flywheel.impl.mixin;

import java.util.List;

import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector4f;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.framegraph.FrameGraphBuilder;
import com.mojang.blaze3d.framegraph.FrameGraphBuilder.Inspector;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;

import dev.engine_room.flywheel.api.visualization.VisualizationManager;
import dev.engine_room.flywheel.impl.event.RenderContextImpl;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.state.level.BlockBreakingRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;

@Mixin(value = LevelRenderer.class, priority = 1001) // Higher priority to go after Sodium
abstract class LevelRendererMixin {
	@Shadow
	@Final
	private RenderBuffers renderBuffers;

	@Shadow
	@Final
	private LevelRenderState levelRenderState;

	@Unique
	@Nullable
	private RenderContextImpl flywheel$renderContext;

	@Unique
	private static final ScopedValue<List<BlockBreakingRenderState>> FLYWHEEL$BLOCK_BREAKING_RENDER_STATES = ScopedValue.newInstance();

	@Inject(method = "render", at = @At("HEAD"))
	private void flywheel$beginRender(GraphicsResourceAllocator resourceAllocator, DeltaTracker deltaTracker, boolean renderOutline, CameraRenderState cameraState, Matrix4fc modelViewMatrix, GpuBufferSlice terrainFog, Vector4f fogColor, boolean shouldRenderSky, CallbackInfo ci) {
		ClientLevel level = Minecraft.getInstance().level;

		Matrix4f projectionMatrix = RenderContextImpl.PROJECTION_MATRIX_NO_BOB.get();
		flywheel$renderContext = RenderContextImpl.create((LevelRenderer) (Object) this, level, renderBuffers, modelViewMatrix, projectionMatrix, cameraState, levelRenderState, deltaTracker.getGameTimeDeltaPartialTick(false));

		VisualizationManager manager = VisualizationManager.get(level);
		if (manager != null) {
			manager.renderDispatcher().onStartLevelRender(flywheel$renderContext);
		}
	}

	@Inject(method = "render", at = @At("RETURN"))
	private void flywheel$endRender(CallbackInfo ci) {
		flywheel$renderContext = null;
	}

	@Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;submitFeatures(Lnet/minecraft/client/renderer/state/level/LevelRenderState;Lnet/minecraft/client/renderer/SubmitNodeCollector;Z)V"))
	private void flywheel$copyCrumblingState(GraphicsResourceAllocator resourceAllocator, DeltaTracker deltaTracker, boolean renderOutline, CameraRenderState cameraState, Matrix4fc modelViewMatrix, GpuBufferSlice terrainFog, Vector4f fogColor, boolean shouldRenderSky, CallbackInfo ci, @Share("blockBreakingRenderStates") LocalRef<List<BlockBreakingRenderState>> blockBreakingRenderStates) {
		blockBreakingRenderStates.set(List.copyOf(levelRenderState.blockBreakingRenderStates));
	}

	@WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/framegraph/FrameGraphBuilder;execute(Lcom/mojang/blaze3d/resource/GraphicsResourceAllocator;Lcom/mojang/blaze3d/framegraph/FrameGraphBuilder$Inspector;)V"))
	private void flywheel$passCrumblingState(FrameGraphBuilder instance, GraphicsResourceAllocator resourceAllocator, Inspector inspector, Operation<Void> original, @Share("blockBreakingRenderStates") LocalRef<List<BlockBreakingRenderState>> blockBreakingRenderStates) {
		ScopedValue.where(FLYWHEEL$BLOCK_BREAKING_RENDER_STATES, blockBreakingRenderStates.get())
				.run(() -> original.call(instance, resourceAllocator, inspector));
	}

	@Inject(method = "lambda$addMainPass$0", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/feature/FeatureRenderDispatcher$PreparedFrame;executeSolid()V"))
	private void flywheel$beforeSolids(CallbackInfo ci) {
		if (flywheel$renderContext != null) {
			VisualizationManager manager = VisualizationManager.get(Minecraft.getInstance().level);
			if (manager != null) {
				manager.renderDispatcher().afterEntities(flywheel$renderContext);
				manager.renderDispatcher().beforeCrumbling(flywheel$renderContext, FLYWHEEL$BLOCK_BREAKING_RENDER_STATES.get());
			}
		}
	}

	@Inject(method = "lambda$addMainPass$0", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/feature/FeatureRenderDispatcher$PreparedFrame;executeTranslucent()V", shift = Shift.AFTER))
	private void flywheel$beforeRenderCrumbling(CallbackInfo ci) {
		if (flywheel$renderContext != null) {
			VisualizationManager manager = VisualizationManager.get(Minecraft.getInstance().level);
			if (manager != null) {
				//manager.renderDispatcher().beforeCrumbling(flywheel$renderContext, FLYWHEEL$BLOCK_BREAKING_RENDER_STATES.get());
			}
		}
	}
}
