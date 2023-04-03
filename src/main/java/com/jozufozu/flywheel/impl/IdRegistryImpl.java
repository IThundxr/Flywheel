package com.jozufozu.flywheel.impl;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import com.jozufozu.flywheel.api.registry.IdRegistry;

import it.unimi.dsi.fastutil.objects.Object2ReferenceMap;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import it.unimi.dsi.fastutil.objects.ObjectSets;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceCollection;
import it.unimi.dsi.fastutil.objects.ReferenceCollections;
import net.minecraft.resources.ResourceLocation;

public class IdRegistryImpl<T> implements IdRegistry<T> {
	private static final ObjectList<IdRegistryImpl<?>> ALL = new ObjectArrayList<>();

	private final Object2ReferenceMap<ResourceLocation, T> map = new Object2ReferenceOpenHashMap<>();
	private final Reference2ObjectMap<T, ResourceLocation> reverseMap = new Reference2ObjectOpenHashMap<>();
	private final ObjectSet<ResourceLocation> keysView = ObjectSets.unmodifiable(map.keySet());
	private final ReferenceCollection<T> valuesView = ReferenceCollections.unmodifiable(map.values());
	private final ObjectList<Runnable> freezeCallbacks = new ObjectArrayList<>();
	private boolean frozen;

	private IdRegistryImpl() {
		ALL.add(this);
	}

	public static <T> IdRegistry<T> create() {
		return new IdRegistryImpl<>();
	}

	@Override
	public void register(ResourceLocation id, T object) {
		if (frozen) {
			throw new IllegalStateException("Cannot register to frozen registry!");
		}
		T oldValue = map.put(id, object);
		if (oldValue != null) {
			throw new IllegalArgumentException("Cannot override registration for ID '" + id + "'!");
		}
		ResourceLocation oldId = reverseMap.put(object, id);
		if (oldId != null) {
			throw new IllegalArgumentException("Cannot override ID '" + id + "' with registration for ID '" + oldId + "'!");
		}
	}

	@Override
	public <S extends T> S registerAndGet(ResourceLocation id, S object) {
		register(id, object);
		return object;
	}

	@Override
	@Nullable
	public T get(ResourceLocation id) {
		return map.get(id);
	}

	@Override
	@Nullable
	public ResourceLocation getId(T object) {
		return reverseMap.get(object);
	}

	@Override
	@Unmodifiable
	public Set<ResourceLocation> getAllIds() {
		return keysView;
	}

	@Override
	@Unmodifiable
	public Collection<T> getAll() {
		return valuesView;
	}

	@Override
	public void addFreezeCallback(Runnable callback) {
		if (frozen) {
			throw new IllegalStateException("Cannot add freeze callback to frozen registry!");
		}
		freezeCallbacks.add(callback);
	}

	@Override
	public boolean isFrozen() {
		return frozen;
	}

	@Override
	public Iterator<T> iterator() {
		return getAll().iterator();
	}

	public void freeze() {
		frozen = true;
		for (Runnable runnable : freezeCallbacks) {
			runnable.run();
		}
	}

	public static void freezeAll() {
		for (IdRegistryImpl<?> registry : ALL) {
			registry.freeze();
		}
	}
}
