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
package dev.ikm.tinkar.composer;

import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.composer.assembler.ConceptAssembler;
import dev.ikm.tinkar.composer.assembler.ConceptAssemblerConsumer;
import dev.ikm.tinkar.composer.assembler.PatternAssembler;
import dev.ikm.tinkar.composer.assembler.PatternAssemblerConsumer;
import dev.ikm.tinkar.composer.assembler.SemanticAssembler;
import dev.ikm.tinkar.composer.assembler.SemanticAssemblerConsumer;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityVersion;
import dev.ikm.tinkar.entity.StampEntity;
import dev.ikm.tinkar.terms.EntityProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.List;

public final class Session {

	private static final Logger LOG = LoggerFactory.getLogger(Session.class);
	private final StampEntity<?> stampEntity;
	private final ChangeSetWriter changeSetWriter;

	/**
	 * Provides a Session for creating Components using the Transaction and STAMP provided.
	 */
	protected Session(StampEntity<?> stampEntity, Path changeSetFile) {
		this.stampEntity = stampEntity;
		this.changeSetWriter = new ChangeSetWriter(changeSetFile);
		LOG.info("Session {} - Initializing with stamp: {}", stampEntity.publicId());
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
		conceptAssembler.setSessionStampEntity(stampEntity);

		conceptAssemblerConsumer.accept(conceptAssembler);
		Entity entity = ((Attachable) conceptAssembler).validateAndWrite();
		ChangeSetManager.getInstance().add(stampEntity.publicId(), entity);
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
		patternAssembler.setSessionStampEntity(stampEntity);

		patternAssemblerConsumer.accept(patternAssembler);
		Entity entity = ((Attachable) patternAssembler).validateAndWrite();
		ChangeSetManager.getInstance().add(stampEntity.publicId(), entity);
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
		semanticAssembler.setSessionStampEntity(stampEntity);

		semanticAssemblerConsumer.accept(semanticAssembler);
		Entity entity = ((Attachable) semanticAssembler).validateAndWrite();
		ChangeSetManager.getInstance().add(stampEntity.publicId(), entity);
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
		semanticTemplate.setSessionStampEntity(stampEntity);
		semanticTemplate.setReference(reference);

		Entity entity = semanticTemplate.validateAndWrite();
		ChangeSetManager.getInstance().add(stampEntity.publicId(), entity);
		return semanticTemplate;
	}

	public PublicId getId() {
		return stampEntity.publicId();
	}

	public void commit() {
		LOG.info("Session {} - Committing {} changes", stampEntity.publicId(), ChangeSetManager.getInstance().size(stampEntity.publicId()));
		List<Entity<EntityVersion>> messages = ChangeSetManager.getInstance().get(stampEntity.publicId());
		if (messages != null) {
			changeSetWriter.writeChangeSet(messages);
			ChangeSetManager.getInstance().clear(stampEntity.publicId());
		} else {
			LOG.warn("Session {} - No changes to commit", stampEntity.publicId());
		}
	}

}
