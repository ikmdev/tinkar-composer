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

import dev.ikm.tinkar.composer.assembler.*;
import dev.ikm.tinkar.entity.StampEntity;
import dev.ikm.tinkar.entity.transaction.Transaction;
import dev.ikm.tinkar.terms.EntityProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class Session {

    private static final Logger LOG = LoggerFactory.getLogger(Session.class);
    private final StampEntity stampEntity;
    private final Transaction transaction;
    private final UUID id;

    /**
     * Provides a Session for creating Components using the Transaction and STAMP provided.
     */
    protected Session(Transaction transaction, StampEntity stampEntity, UUID id){
        this.transaction = transaction;
        this.stampEntity = stampEntity;
        this.id = id;
        LOG.info("Session {} - Initializing with stamp: {}", transaction.hashCode(), stampEntity);
    }

    /**
     * Creates a Concept and provides a ConceptAssembler as an Attachable for attaching Semantics.
     * @param conceptAssemblerConsumer Consumer that defines the Concept
     * @return {@link ConceptAssembler} which can be referred to by {@link Attachable} implementations.
     * @see ConceptAssemblerConsumer
     */
    public Attachable compose(ConceptAssemblerConsumer conceptAssemblerConsumer) {
        ConceptAssembler conceptAssembler = new ConceptAssembler();
        conceptAssembler.setSessionTransaction(transaction);
        conceptAssembler.setSessionStampEntity(stampEntity);

        conceptAssemblerConsumer.accept(conceptAssembler);
        ((Attachable) conceptAssembler).validateAndWrite();
        return conceptAssembler;
    }

    /**
     * Creates a Pattern and provides a PatternAssembler as an Attachable for attaching Semantics.
     * @param patternAssemblerConsumer Consumer that defines the Pattern
     * @return {@link PatternAssembler} which can be referred to by {@link Attachable} implementations.
     * @see PatternAssemblerConsumer
     */
    public Attachable compose(PatternAssemblerConsumer patternAssemblerConsumer) {
        PatternAssembler patternAssembler = new PatternAssembler();
        patternAssembler.setSessionTransaction(transaction);
        patternAssembler.setSessionStampEntity(stampEntity);

        patternAssemblerConsumer.accept(patternAssembler);
        ((Attachable) patternAssembler).validateAndWrite();
        return patternAssembler;
    }

    /**
     * Creates a Semantic and provides a SemanticAssembler as an Attachable for attaching Semantics.
     * @param semanticAssemblerConsumer Consumer that defines the Semantic
     * @return {@link SemanticAssembler} which can be referred to by {@link Attachable} implementations.
     * @see SemanticAssemblerConsumer
     */
    public Attachable compose(SemanticAssemblerConsumer semanticAssemblerConsumer) {
        SemanticAssembler semanticAssembler = new SemanticAssembler();
        semanticAssembler.setSessionTransaction(transaction);
        semanticAssembler.setSessionStampEntity(stampEntity);

        semanticAssemblerConsumer.accept(semanticAssembler);
        ((Attachable) semanticAssembler).validateAndWrite();
        return semanticAssembler;
    }

    /**
     * Creates a Semantic from a SemanticTemplate and provides a SemanticTemplate as an Attachable for attaching Semantics.
     * @param semanticTemplate SemanticTemplate that defines the Semantic
     * @param reference referencedComponent for the Semantic
     * @return {@link SemanticTemplate} which can be referred to by {@link Attachable} implementations.
     * @see SemanticTemplate
     */
    public Attachable compose(SemanticTemplate semanticTemplate, EntityProxy reference) {
        semanticTemplate.setSessionTransaction(transaction);
        semanticTemplate.setSessionStampEntity(stampEntity);
        semanticTemplate.setReference(reference);

        semanticTemplate.validateAndWrite();
        return semanticTemplate;
    }

    /**
     * Provides the number of Components written by the Session. This count does not include the STAMP associated with the Session.
     */
    public int componentsInSessionCount() {
        return transaction.componentsInTransactionCount();
    }

    /**
     * Cancels the Transaction and STAMP associated with this Session so that they will not be committed.
     */
    public void cancel() {
        LOG.info("Session {} - Cancelling updates to {} Entities with stamp: {}",
                transaction.hashCode(),
                transaction.componentsInTransactionCount(),
                stampEntity);
        transaction.cancel();
    }

    /**
     * Commits the Transaction and STAMP associated with this Session. If the Session
     * was not Constructed with a timestamp, then the timestamp will be set to the time of commit.
     */
    protected void commit() {
        LOG.info("Session {} - Commiting updates to {} Entities with stamp: {}",
                transaction.hashCode(),
                transaction.componentsInTransactionCount(),
                stampEntity);
        transaction.commit();
    }

    protected UUID getId() {
        return this.id;
    }

}
