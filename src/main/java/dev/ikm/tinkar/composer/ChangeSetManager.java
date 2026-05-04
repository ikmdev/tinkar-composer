package dev.ikm.tinkar.composer;

import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityVersion;

import java.util.List;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class ChangeSetManager {

	private final ConcurrentSkipListMap<PublicId, List<Entity<EntityVersion>>> changeSetMap;

	private ChangeSetManager() {
		changeSetMap = new ConcurrentSkipListMap<>();
	}

	private static class LazyHolder {
		static final ChangeSetManager INSTANCE = new ChangeSetManager();
	}

	public static ChangeSetManager getInstance() {
		return LazyHolder.INSTANCE;
	}

	public void add(PublicId id, Entity<EntityVersion> entity) {
		changeSetMap.computeIfAbsent(id, k -> new CopyOnWriteArrayList<>())
				.add(entity);
	}

	public void clear(PublicId id) {
		changeSetMap.remove(id);
	}

	public void clear() {
		changeSetMap.clear();
	}

	public int size(PublicId id) {
		List<Entity<EntityVersion>> messages = changeSetMap.get(id);
		return messages == null ? 0 : messages.size();
	}

	public boolean isEmpty() {
		return changeSetMap.isEmpty();
	}

	public List<Entity<EntityVersion>> get(PublicId id) {
		List<Entity<EntityVersion>> entities = changeSetMap.get(id);
		return entities == null ? List.of() : List.copyOf(entities);
	}

}
