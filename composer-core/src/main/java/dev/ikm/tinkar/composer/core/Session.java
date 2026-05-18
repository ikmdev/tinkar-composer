/*
 * Copyright © 2024 Integrated Knowledge Management (support@ikm.dev)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.ikm.tinkar.composer.core;

import dev.ikm.tinkar.composer.core.assembler.ConceptAssembler;
import dev.ikm.tinkar.composer.core.assembler.ConceptAssemblerConsumer;
import dev.ikm.tinkar.composer.core.assembler.PatternAssembler;
import dev.ikm.tinkar.composer.core.assembler.PatternAssemblerConsumer;
import dev.ikm.tinkar.composer.core.assembler.SemanticAssembler;
import dev.ikm.tinkar.composer.core.assembler.SemanticAssemblerConsumer;
import dev.ikm.tinkar.composer.core.io.PackageWriter;
import dev.ikm.tinkar.schema.StampChronology;
import dev.ikm.tinkar.schema.TinkarMsg;
import dev.ikm.tinkar.terms.EntityProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public final class Session {

	private static final Logger LOG = LoggerFactory.getLogger(Session.class);
	private final UUID id;
	private final StampChronology stampChronology;
	private final PackageWriter packageWriter;

	/**
	 * Provides a Session for creating Components using the Transaction and STAMP provided.
	 */
	protected Session(PackageWriter packageWriter, StampChronology stampChronology) {
		this.id = UUID.randomUUID();
		this.stampChronology = stampChronology;
		this.packageWriter = packageWriter;
		LOG.info("Session {} - Initializing with stamp: {}", stampChronology);
	}

	/**
	 * Creates a Concept and provides a ConceptAssembler as an Attachable for attaching Semantics.
	 *
	 * @param conceptAssemblerConsumer Consumer that defines the Concept
	 * @return {@link ConceptAssembler} which can be referred to by {@link Attachable} implementations.
	 * @see ConceptAssemblerConsumer
	 */
	public Attachable compose(ConceptAssemblerConsumer conceptAssemblerConsumer) {
		ConceptAssembler conceptAssembler = new ConceptAssembler();
		conceptAssembler.setSessionStampChronology(stampChronology);
		conceptAssembler.setSessionId(id);
		conceptAssembler.setPackageWriter(packageWriter);

		conceptAssemblerConsumer.accept(conceptAssembler);
		TinkarMsg tinkarMsg = ((Attachable) conceptAssembler).validateAndWrite();
		packageWriter.writeToPackage(tinkarMsg);
		return conceptAssembler;
	}

	/**
	 * Creates a Pattern and provides a PatternAssembler as an Attachable for attaching Semantics.
	 *
	 * @param patternAssemblerConsumer Consumer that defines the Pattern
	 * @return {@link PatternAssembler} which can be referred to by {@link Attachable} implementations.
	 * @see PatternAssemblerConsumer
	 */
	public Attachable compose(PatternAssemblerConsumer patternAssemblerConsumer) {
		PatternAssembler patternAssembler = new PatternAssembler();
		patternAssembler.setSessionStampChronology(stampChronology);
		patternAssembler.setSessionId(id);
		patternAssembler.setPackageWriter(packageWriter);

		patternAssemblerConsumer.accept(patternAssembler);
		TinkarMsg tinkarMsg = ((Attachable) patternAssembler).validateAndWrite();
		packageWriter.writeToPackage(tinkarMsg);
		return patternAssembler;
	}

	/**
	 * Creates a Semantic and provides a SemanticAssembler as an Attachable for attaching Semantics.
	 *
	 * @param semanticAssemblerConsumer Consumer that defines the Semantic
	 * @return {@link SemanticAssembler} which can be referred to by {@link Attachable} implementations.
	 * @see SemanticAssemblerConsumer
	 */
	public Attachable compose(SemanticAssemblerConsumer semanticAssemblerConsumer) {
		SemanticAssembler semanticAssembler = new SemanticAssembler();
		semanticAssembler.setSessionStampChronology(stampChronology);
		semanticAssembler.setSessionId(id);
		semanticAssembler.setPackageWriter(packageWriter);

		semanticAssemblerConsumer.accept(semanticAssembler);
		TinkarMsg tinkarMsg = ((Attachable) semanticAssembler).validateAndWrite();
		packageWriter.writeToPackage(tinkarMsg);
		return semanticAssembler;
	}

	/**
	 * Creates a Semantic from a SemanticTemplate and provides a SemanticTemplate as an Attachable for attaching Semantics.
	 *
	 * @param semanticTemplate SemanticTemplate that defines the Semantic
	 * @param reference        referencedComponent for the Semantic
	 * @return {@link SemanticTemplate} which can be referred to by {@link Attachable} implementations.
	 * @see SemanticTemplate
	 */
	public Attachable compose(SemanticTemplate semanticTemplate, EntityProxy reference) {
		semanticTemplate.setSessionStampChronology(stampChronology);
		semanticTemplate.setReference(reference);
		semanticTemplate.setSessionId(id);
		semanticTemplate.setPackageWriter(packageWriter);

		TinkarMsg tinkarMsg = semanticTemplate.validateAndWrite();
		packageWriter.writeToPackage(tinkarMsg);
		return semanticTemplate;
	}

	public UUID getId() {
		return id;
	}

}
