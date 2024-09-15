package dev.engine_room.flywheel.lib.model.part;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.ObjIntConsumer;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.joml.Quaternionf;
import org.joml.Vector3fc;

import com.mojang.blaze3d.vertex.PoseStack;

import dev.engine_room.flywheel.api.instance.InstancerProvider;
import dev.engine_room.flywheel.api.material.Material;
import dev.engine_room.flywheel.api.model.Mesh;
import dev.engine_room.flywheel.api.model.Model;
import dev.engine_room.flywheel.lib.instance.InstanceTypes;
import dev.engine_room.flywheel.lib.instance.TransformedInstance;
import dev.engine_room.flywheel.lib.model.ModelCache;
import dev.engine_room.flywheel.lib.model.SingleMeshModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;

public final class InstanceTree {
	private static final ModelCache<Model.ConfiguredMesh> MODEL_CACHE = new ModelCache<>(entry -> new SingleMeshModel(entry.mesh(), entry.material()));

	@Nullable
	private final TransformedInstance instance;
	private final PartPose initialPose;
	@Unmodifiable
	private final Map<String, InstanceTree> children;

	private final Quaternionf rotation = new Quaternionf();

	public float x;
	public float y;
	public float z;
	public float xRot;
	public float yRot;
	public float zRot;
	public float xScale;
	public float yScale;
	public float zScale;
	public boolean visible = true;
	public boolean skipDraw;

	private InstanceTree(@Nullable TransformedInstance instance, PartPose initialPose, @Unmodifiable Map<String, InstanceTree> children) {
		this.instance = instance;
		this.initialPose = initialPose;
		this.children = children;
		resetPose();
	}

	private static InstanceTree create(InstancerProvider provider, MeshTree meshTree, BiFunction<String, Mesh, Model.ConfiguredMesh> meshFinalizerFunc, String path) {
		Map<String, InstanceTree> children = new HashMap<>();
		String pathSlash = path + "/";

		meshTree.children().forEach((name, meshTreeChild) -> {
			children.put(name, InstanceTree.create(provider, meshTreeChild, meshFinalizerFunc, pathSlash + name));
		});

		Mesh mesh = meshTree.mesh();
		TransformedInstance instance;
		if (mesh != null) {
			Model.ConfiguredMesh configuredMesh = meshFinalizerFunc.apply(path, mesh);
			instance = provider.instancer(InstanceTypes.TRANSFORMED, MODEL_CACHE.get(configuredMesh))
					.createInstance();
		} else {
			instance = null;
		}

		return new InstanceTree(instance, meshTree.initialPose(), Collections.unmodifiableMap(children));
	}

	public static InstanceTree create(InstancerProvider provider, MeshTree meshTree, BiFunction<String, Mesh, Model.ConfiguredMesh> meshFinalizerFunc) {
		return create(provider, meshTree, meshFinalizerFunc, "");
	}

	public static InstanceTree create(InstancerProvider provider, ModelLayerLocation layer, BiFunction<String, Mesh, Model.ConfiguredMesh> meshFinalizerFunc) {
		return create(provider, MeshTree.of(layer), meshFinalizerFunc);
	}

	public static InstanceTree create(InstancerProvider provider, MeshTree meshTree, Material material) {
		return create(provider, meshTree, (path, mesh) -> new Model.ConfiguredMesh(material, mesh));
	}

	public static InstanceTree create(InstancerProvider provider, ModelLayerLocation layer, Material material) {
		return create(provider, MeshTree.of(layer), material);
	}

	@Nullable
	public TransformedInstance instance() {
		return instance;
	}

	public PartPose initialPose() {
		return initialPose;
	}

	@Unmodifiable
	public Map<String, InstanceTree> children() {
		return children;
	}

	public boolean hasChild(String name) {
		return children.containsKey(name);
	}

	@Nullable
	public InstanceTree child(String name) {
		return children.get(name);
	}

	public InstanceTree childOrThrow(String name) {
		InstanceTree child = child(name);

		if (child == null) {
			throw new NoSuchElementException("Can't find part " + name);
		}

		return child;
	}

	public void traverse(Consumer<? super TransformedInstance> consumer) {
		if (instance != null) {
			consumer.accept(instance);
		}
		for (InstanceTree child : children.values()) {
			child.traverse(consumer);
		}
	}

	@ApiStatus.Experimental
	public void traverse(int i, ObjIntConsumer<? super TransformedInstance> consumer) {
		if (instance != null) {
			consumer.accept(instance, i);
		}
		for (InstanceTree child : children.values()) {
			child.traverse(i, consumer);
		}
	}

	@ApiStatus.Experimental
	public void traverse(int i, int j, ObjIntIntConsumer<? super TransformedInstance> consumer) {
		if (instance != null) {
			consumer.accept(instance, i, j);
		}
		for (InstanceTree child : children.values()) {
			child.traverse(i, j, consumer);
		}
	}

	public void translateAndRotate(PoseStack poseStack) {
		poseStack.translate(x / 16.0F, y / 16.0F, z / 16.0F);

		if (xRot != 0.0F || yRot != 0.0F || zRot != 0.0F) {
			poseStack.mulPose(rotation.rotationZYX(zRot, yRot, xRot));
		}

		if (xScale != 1.0F || yScale != 1.0F || zScale != 1.0F) {
			poseStack.scale(xScale, yScale, zScale);
		}
	}

	public void updateInstances(PoseStack poseStack) {
		// Need to use an anonymous class so it can reference this.
		updateInstancesInner(poseStack, new Walker() {
			@Override
			public void accept(InstanceTree child) {
				child.updateInstancesInner(poseStack, this);
			}
		});
	}

	private void updateInstancesInner(PoseStack poseStack, Walker walker) {
		if (visible) {
			poseStack.pushPose();
			translateAndRotate(poseStack);

			if (instance != null && !skipDraw) {
				instance.setTransform(poseStack.last())
						.setChanged();
			}

			// Use the bare HashMap.forEach because .values() always allocates a new collection.
			// We also don't want to store an array of children because that would statically use a lot more memory.
			children.forEach(walker);

			poseStack.popPose();
		}
	}

	public void pos(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public void rotation(float xRot, float yRot, float zRot) {
		this.xRot = xRot;
		this.yRot = yRot;
		this.zRot = zRot;
	}

	public void scale(float xScale, float yScale, float zScale) {
		this.xScale = xScale;
		this.yScale = yScale;
		this.zScale = zScale;
	}

	public void offsetPos(float xOffset, float yOffset, float zOffset) {
		x += xOffset;
		y += yOffset;
		z += zOffset;
	}

	public void offsetRotation(float xOffset, float yOffset, float zOffset) {
		xRot += xOffset;
		yRot += yOffset;
		zRot += zOffset;
	}

	public void offsetScale(float xOffset, float yOffset, float zOffset) {
		xScale += xOffset;
		yScale += yOffset;
		zScale += zOffset;
	}

	public void offsetPos(Vector3fc offset) {
		offsetPos(offset.x(), offset.y(), offset.z());
	}

	public void offsetRotation(Vector3fc offset) {
		offsetRotation(offset.x(), offset.y(), offset.z());
	}

	public void offsetScale(Vector3fc offset) {
		offsetScale(offset.x(), offset.y(), offset.z());
	}

	public PartPose storePose() {
		return PartPose.offsetAndRotation(x, y, z, xRot, yRot, zRot);
	}

	public void loadPose(PartPose pose) {
		x = pose.x;
		y = pose.y;
		z = pose.z;
		xRot = pose.xRot;
		yRot = pose.yRot;
		zRot = pose.zRot;
		xScale = ModelPart.DEFAULT_SCALE;
		yScale = ModelPart.DEFAULT_SCALE;
		zScale = ModelPart.DEFAULT_SCALE;
	}

	public void resetPose() {
		loadPose(initialPose);
	}

	public void copyTransform(InstanceTree tree) {
		x = tree.x;
		y = tree.y;
		z = tree.z;
		xRot = tree.xRot;
		yRot = tree.yRot;
		zRot = tree.zRot;
		xScale = tree.xScale;
		yScale = tree.yScale;
		zScale = tree.zScale;
	}

	public void copyTransform(ModelPart modelPart) {
		x = modelPart.x;
		y = modelPart.y;
		z = modelPart.z;
		xRot = modelPart.xRot;
		yRot = modelPart.yRot;
		zRot = modelPart.zRot;
		xScale = modelPart.xScale;
		yScale = modelPart.yScale;
		zScale = modelPart.zScale;
	}

	public void delete() {
		if (instance != null) {
			instance.delete();
		}
		children.values()
				.forEach(InstanceTree::delete);
	}

	@ApiStatus.Experimental
	@FunctionalInterface
	public interface ObjIntIntConsumer<T> {
		void accept(T t, int i, int j);
	}

	// Helper interface for writing walking classes.
	private interface Walker extends BiConsumer<String, InstanceTree> {
		void accept(InstanceTree child);

		@Override
		default void accept(String name, InstanceTree child) {
			accept(child);
		}
	}
}