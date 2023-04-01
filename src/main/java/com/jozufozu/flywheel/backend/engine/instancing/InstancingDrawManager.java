package com.jozufozu.flywheel.backend.engine.instancing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ListMultimap;
import com.jozufozu.flywheel.api.event.RenderStage;
import com.jozufozu.flywheel.api.instancer.InstancedPart;
import com.jozufozu.flywheel.api.instancer.Instancer;
import com.jozufozu.flywheel.api.model.Mesh;
import com.jozufozu.flywheel.api.model.Model;
import com.jozufozu.flywheel.api.struct.StructType;
import com.jozufozu.flywheel.api.vertex.VertexType;
import com.jozufozu.flywheel.backend.instancing.InstancerKey;

public class InstancingDrawManager {

	private final Map<InstancerKey<?>, GPUInstancer<?>> instancers = new HashMap<>();
	private final List<UninitializedInstancer> uninitializedInstancers = new ArrayList<>();
	private final List<GPUInstancer<?>> initializedInstancers = new ArrayList<>();
	private final Map<RenderStage, DrawSet> drawSets = new EnumMap<>(RenderStage.class);
	private final Map<VertexType, InstancedMeshPool> meshPools = new HashMap<>();

	public DrawSet get(RenderStage stage) {
		return drawSets.getOrDefault(stage, DrawSet.EMPTY);
	}

	@SuppressWarnings("unchecked")
	public <D extends InstancedPart> Instancer<D> getInstancer(StructType<D> type, Model model, RenderStage stage) {
		InstancerKey<D> key = new InstancerKey<>(type, model, stage);
		GPUInstancer<D> instancer = (GPUInstancer<D>) instancers.get(key);
		if (instancer == null) {
			instancer = new GPUInstancer<>(type);
			instancers.put(key, instancer);
			uninitializedInstancers.add(new UninitializedInstancer(instancer, model, stage));
		}
		return instancer;
	}

	public void flush() {
		for (var instancer : uninitializedInstancers) {
			instancer.instancer()
					.init();

			add(instancer.instancer(), instancer.model(), instancer.stage());
		}
		uninitializedInstancers.clear();

		for (var pool : meshPools.values()) {
			pool.flush();
		}
	}

	public void delete() {
		instancers.clear();

		meshPools.values()
				.forEach(InstancedMeshPool::delete);
		meshPools.clear();

		drawSets.values()
				.forEach(DrawSet::delete);
		drawSets.clear();

		initializedInstancers.forEach(GPUInstancer::delete);
		initializedInstancers.clear();
	}

	public void clearInstancers() {
		initializedInstancers.forEach(GPUInstancer::clear);
	}

	private void add(GPUInstancer<?> instancer, Model model, RenderStage stage) {
		DrawSet drawSet = drawSets.computeIfAbsent(stage, DrawSet::new);
		var meshes = model.getMeshes();
		for (var entry : meshes.entrySet()) {
			DrawCall drawCall = new DrawCall(instancer, entry.getKey(), alloc(entry.getValue()));
			var shaderState = new ShaderState(drawCall.getMaterial(), drawCall.getVertexType(), drawCall.instancer.type);
			drawSet.put(shaderState, drawCall);
		}
		initializedInstancers.add(instancer);
	}

	private InstancedMeshPool.BufferedMesh alloc(Mesh mesh) {
		return meshPools.computeIfAbsent(mesh.getVertexType(), InstancedMeshPool::new)
				.alloc(mesh);
	}

	public static class DrawSet implements Iterable<Map.Entry<ShaderState, Collection<DrawCall>>> {

		public static final DrawSet EMPTY = new DrawSet(ImmutableListMultimap.of());

		private final ListMultimap<ShaderState, DrawCall> drawCalls;

		public DrawSet(RenderStage renderStage) {
			drawCalls = ArrayListMultimap.create();
		}

		public DrawSet(ListMultimap<ShaderState, DrawCall> drawCalls) {
			this.drawCalls = drawCalls;
		}

		private void delete() {
			drawCalls.values()
					.forEach(DrawCall::delete);
			drawCalls.clear();
		}

		public void put(ShaderState shaderState, DrawCall drawCall) {
			drawCalls.put(shaderState, drawCall);
		}

		public boolean isEmpty() {
			return drawCalls.isEmpty();
		}

		@NotNull
		@Override
		public Iterator<Map.Entry<ShaderState, Collection<DrawCall>>> iterator() {
			return drawCalls.asMap()
					.entrySet()
					.iterator();
		}
	}

	private record UninitializedInstancer(GPUInstancer<?> instancer, Model model, RenderStage stage) {
	}
}