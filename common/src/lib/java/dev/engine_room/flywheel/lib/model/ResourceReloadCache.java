package dev.engine_room.flywheel.lib.model;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import org.jetbrains.annotations.ApiStatus;

import dev.engine_room.flywheel.lib.util.FlwUtil;

public class ResourceReloadCache<T, U> implements Function<T, U> {
	private static final Set<ResourceReloadCache<?, ?>> ALL = FlwUtil.createWeakHashSet();
	private final Function<T, U> factory;
	private final Map<T, U> map = new ConcurrentHashMap<>();

	public ResourceReloadCache(Function<T, U> factory) {
		this.factory = factory;

		synchronized (ALL) {
			ALL.add(this);
		}
	}

	public final U get(T key) {
		return map.computeIfAbsent(key, factory);
	}

	@Override
	public final U apply(T t) {
		return get(t);
	}

	public final void clear() {
		map.clear();
	}

	@ApiStatus.Internal
	public static void onEndClientResourceReload() {
		for (ResourceReloadCache<?, ?> cache : ALL) {
			cache.clear();
		}
	}
}