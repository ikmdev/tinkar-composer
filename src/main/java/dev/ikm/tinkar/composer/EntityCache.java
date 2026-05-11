package dev.ikm.tinkar.composer;

import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityVersion;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class EntityCache {

	private final ConcurrentSkipListMap<UUID, List<Entity<EntityVersion>>> changeSetMap;

	public EntityCache() {
		changeSetMap = new ConcurrentSkipListMap<>();
	}

	public void add(UUID id, Entity<EntityVersion> entity) {
		changeSetMap.computeIfAbsent(id, k -> new CopyOnWriteArrayList<>())
				.add(entity);
	}

	public void clear(UUID id) {
		changeSetMap.remove(id);
	}

	public void clearAll() {
		changeSetMap.clear();
	}

	public int size(UUID id) {
		List<Entity<EntityVersion>> messages = changeSetMap.get(id);
		return messages == null ? 0 : messages.size();
	}

	public boolean isEmpty() {
		return changeSetMap.isEmpty();
	}

	public List<Entity<EntityVersion>> get(UUID id) {
		List<Entity<EntityVersion>> entities = changeSetMap.get(id);
		return entities == null ? List.of() : List.copyOf(entities);
	}

}
