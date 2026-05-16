package dev.ikm.tinkar.composer.io;

import dev.ikm.tinkar.common.id.PublicIds;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityHandle;
import dev.ikm.tinkar.entity.EntityVersion;
import dev.ikm.tinkar.schema.PublicId;
import dev.ikm.tinkar.terms.TinkarTerm;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;

public class Manifest {

	private final Set<PublicId> modules;
	private final Set<PublicId> authors;
	private final LongAdder entityCount;
	private final LongAdder conceptsCount;
	private final LongAdder semanticsCount;
	private final LongAdder patternsCount;
	private final LongAdder stampsCount;

	public Manifest() {
		modules = new HashSet<>();
		authors = new HashSet<>();
		this.entityCount = new LongAdder();
		this.conceptsCount = new LongAdder();
		this.semanticsCount = new LongAdder();
		this.patternsCount = new LongAdder();
		this.stampsCount = new LongAdder();
	}

	public void addEntityCount(long count) {
		this.entityCount.add(count);
	}

	public void addConceptCount(long count) {
		this.conceptsCount.add(count);
	}

	public void addSemanticCount(long count) {
		this.semanticsCount.add(count);
	}

	public void addPatternCount(long count) {
		this.patternsCount.add(count);
	}

	public void addStampCount(long count) {
		this.stampsCount.add(count);
	}

	public void addModule(PublicId module) {
		this.modules.add(module);
	}

	public void addAuthor(PublicId author) {
		this.authors.add(author);
	}

	public String constructManifestContent() {
		return "Packager-Name: " + TinkarTerm.KOMET_USER.description() + "\n" +
				"Package-Date: " + LocalDateTime.now(Clock.systemUTC()) + "\n" +
				"Total-Count: " + entityCount + "\n" +
				"Concept-Count: " + conceptsCount + "\n" +
				"Semantic-Count: " + semanticsCount + "\n" +
				"Pattern-Count: " + patternsCount + "\n" +
				"Stamp-Count: " + stampsCount + "\n" +
				idsToManifestEntry(modules) +
				idsToManifestEntry(authors) +
				"\n";
	}


	public static String idsToManifestEntry(Collection<PublicId> publicIds) {
		StringBuilder manifestEntry = new StringBuilder();
		publicIds.forEach((publicId) -> {
			// Convert PublicId to Manifest Entry Name
			String idString = publicId.getUuidsList().stream()
					.map(UUID::fromString)
					.map(UUID::toString)
					.collect(Collectors.joining(","));
			// Get Description
			dev.ikm.tinkar.common.id.PublicId tinkarId = PublicIds.of(publicId.getUuidsList().getFirst());
			EntityHandle entityHandle = EntityHandle.get(tinkarId);
			Optional<Entity<? extends EntityVersion>> entity = entityHandle.entity();
			String manifestDescription = "Description Undefined";
			if (entity.isPresent()) {
				manifestDescription = entity.get().description();
			}
			// Create Manifest Entry
			manifestEntry.append("\n")
					.append("Name: ").append(idString).append("\n")
					.append("Description: ").append(manifestDescription).append("\n");
		});
		return manifestEntry.toString();
	}

}
