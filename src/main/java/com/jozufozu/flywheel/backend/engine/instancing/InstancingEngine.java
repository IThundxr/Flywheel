package com.jozufozu.flywheel.backend.engine.instancing;

import java.util.List;

import org.lwjgl.opengl.GL32;

import com.jozufozu.flywheel.api.backend.Engine;
import com.jozufozu.flywheel.api.context.Context;
import com.jozufozu.flywheel.api.event.RenderContext;
import com.jozufozu.flywheel.api.event.RenderStage;
import com.jozufozu.flywheel.api.instancer.InstancedPart;
import com.jozufozu.flywheel.api.instancer.Instancer;
import com.jozufozu.flywheel.api.model.Model;
import com.jozufozu.flywheel.api.struct.StructType;
import com.jozufozu.flywheel.api.task.TaskExecutor;
import com.jozufozu.flywheel.backend.compile.FlwCompiler;
import com.jozufozu.flywheel.backend.engine.UniformBuffer;
import com.jozufozu.flywheel.gl.GlStateTracker;
import com.jozufozu.flywheel.gl.GlTextureUnit;
import com.jozufozu.flywheel.lib.material.MaterialIndices;
import com.jozufozu.flywheel.lib.pipeline.Pipelines;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.Vec3;

public class InstancingEngine implements Engine {
	private final Context context;
	private final int sqrMaxOriginDistance;

	private final InstancingDrawManager drawManager = new InstancingDrawManager();

	private BlockPos renderOrigin = BlockPos.ZERO;

	public InstancingEngine(Context context, int maxOriginDistance) {
		this.context = context;
		this.sqrMaxOriginDistance = maxOriginDistance * maxOriginDistance;
	}

	@Override
	public <D extends InstancedPart> Instancer<D> instancer(StructType<D> type, Model model, RenderStage stage) {
		return drawManager.getInstancer(type, model, stage);
	}

	@Override
	public void beginFrame(TaskExecutor executor, RenderContext context) {
		try (var restoreState = GlStateTracker.getRestoreState()) {
			drawManager.flush();
		}
	}

	@Override
	public void renderStage(TaskExecutor executor, RenderContext context, RenderStage stage) {
		var drawSet = drawManager.get(stage);

		if (drawSet.isEmpty()) {
			return;
		}

		try (var restoreState = GlStateTracker.getRestoreState()) {
			setup();

			render(drawSet);
		}
	}

	private void setup() {
		GlTextureUnit.T2.makeActive();
		Minecraft.getInstance().gameRenderer.lightTexture().turnOnLightLayer();

		RenderSystem.depthMask(true);
		RenderSystem.colorMask(true, true, true, true);
		RenderSystem.enableDepthTest();
		RenderSystem.depthFunc(GL32.GL_LEQUAL);
		RenderSystem.enableCull();
	}

	private void render(InstancingDrawManager.DrawSet drawSet) {
		for (var entry : drawSet) {
			var shader = entry.getKey();
			var drawCalls = entry.getValue();

			drawCalls.removeIf(DrawCall::shouldRemove);

			if (drawCalls.isEmpty()) {
				continue;
			}

			setup(shader);

			shader.material().setup();

			for (var drawCall : drawCalls) {
				drawCall.render();
			}

			shader.material().clear();
		}
	}

	private void setup(ShaderState desc) {
		var vertexType = desc.vertex();
		var structType = desc.instance();
		var material = desc.material();

		var program = FlwCompiler.INSTANCE.getPipelineProgram(vertexType, structType, context, Pipelines.INSTANCED_ARRAYS);
		UniformBuffer.syncAndBind(program);

		var uniformLocation = program.getUniformLocation("_flw_materialID_instancing");
		var vertexID = MaterialIndices.getVertexShaderIndex(material);
		var fragmentID = MaterialIndices.getFragmentShaderIndex(material);
		GL32.glUniform2ui(uniformLocation, vertexID, fragmentID);
	}

	@Override
	public boolean updateRenderOrigin(Camera camera) {
		Vec3 cameraPos = camera.getPosition();
		double dx = renderOrigin.getX() - cameraPos.x;
		double dy = renderOrigin.getY() - cameraPos.y;
		double dz = renderOrigin.getZ() - cameraPos.z;
		double distanceSqr = dx * dx + dy * dy + dz * dz;

		if (distanceSqr <= sqrMaxOriginDistance) {
			return false;
		}

		renderOrigin = new BlockPos(cameraPos);
		drawManager.clearInstancers();
		return true;
	}

	@Override
	public Vec3i renderOrigin() {
		return renderOrigin;
	}

	@Override
	public void delete() {
		drawManager.delete();
	}

	@Override
	public void addDebugInfo(List<String> info) {
		info.add("GL33 Instanced Arrays");
		info.add("Origin: " + renderOrigin.getX() + ", " + renderOrigin.getY() + ", " + renderOrigin.getZ());
	}
}
