package dev.engine_room.flywheel.impl.mixin;

import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;

import dev.engine_room.flywheel.impl.event.RenderContextImpl;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
	@WrapOperation(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;render(Lcom/mojang/blaze3d/resource/GraphicsResourceAllocator;ZLnet/minecraft/client/renderer/state/level/CameraRenderState;Lcom/mojang/renderpearl/api/buffers/GpuBufferSlice;Lorg/joml/Vector4f;ZZ)V"))
	private void extractProjectionMatrixNoBob(LevelRenderer instance, GraphicsResourceAllocator resourceAllocator, boolean renderOutline, CameraRenderState cameraState, GpuBufferSlice terrainFog, Vector4f fogColor, boolean shouldRenderSky, boolean consistentDepthRequired, Operation<Void> original, @Local(name = "projectionMatrix") Matrix4f projectionMatrix) {
		ScopedValue.where(RenderContextImpl.PROJECTION_MATRIX_NO_BOB, projectionMatrix)
				.run(() -> original.call(instance, resourceAllocator, renderOutline, cameraState, terrainFog, fogColor, shouldRenderSky, consistentDepthRequired));
	}
}
