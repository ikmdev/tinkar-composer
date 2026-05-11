package dev.ikm.tinkar.composer.io;

import dev.ikm.tinkar.schema.PublicId;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.LongAdder;

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

	public String manifestString() {
		return "Entity Count: " + entityCount + "\n" +
				"Concept Count: " + conceptsCount + "\n" +
				"Semantic Count: " + semanticsCount + "\n" +
				"Pattern Count: " + patternsCount + "\n" +
				"Stamp Count: " + stampsCount + "\n" +
				"Modules: " + modules + "\n" +
				"Authors: " + authors;
	}

}
