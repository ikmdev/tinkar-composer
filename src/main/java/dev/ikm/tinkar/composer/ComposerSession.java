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

import dev.ikm.tinkar.composer.template.SemanticTemplate;
import dev.ikm.tinkar.entity.StampEntity;
import dev.ikm.tinkar.entity.transaction.Transaction;
import dev.ikm.tinkar.terms.EntityProxy;
import dev.ikm.tinkar.terms.EntityProxy.Concept;
import dev.ikm.tinkar.terms.EntityProxy.Semantic;
import dev.ikm.tinkar.terms.State;
import org.eclipse.collections.api.list.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.util.List;

public class ComposerSession implements Closeable {

    private static final Logger LOG = LoggerFactory.getLogger(ComposerSession.class);
    private final StampEntity stampEntity;
    private final Transaction transaction;

    public ComposerSession(State status, long time, Concept author, Concept module, Concept path){
        this.transaction = new Transaction();
        this.stampEntity = transaction.getStamp(status, time, author.publicId(), module.publicId(), path.publicId());
        LOG.info("ComposerSession {} - Initializing Session with stamp: {}", transaction.hashCode(), stampEntity);
    }

    public ComposerSession(State status, Concept author, Concept module, Concept path) {
        this.transaction = new Transaction();
        this.stampEntity = transaction.getStamp(status, author, module, path);
        LOG.info("ComposerSession {} - Initializing Session with stamp: {}", transaction.hashCode(), stampEntity);
    }

    public SemanticComposer composeConcept(Concept concept) {
        LOG.debug("ComposerSession {} - Composing Concept: {}",
                transaction.hashCode(),
                concept);
        Write.concept(concept, stampEntity);
        transaction.addComponent(concept);
        return new SemanticComposer(transaction, stampEntity, concept);
    }

    public SemanticComposer composerPattern(EntityProxy.Pattern pattern, Concept meaning, Concept purpose, List<PatternFieldDetail> patternFieldDetails) {
        LOG.debug("ComposerSession {} - Composing Pattern: {}",
                transaction.hashCode(),
                pattern);
        Write.pattern(pattern, stampEntity, meaning, purpose, patternFieldDetails);
        transaction.addComponent(pattern);
        return new SemanticComposer(transaction, stampEntity, pattern);
    }

    public SemanticComposer composeSemantic(EntityProxy.Semantic semantic, EntityProxy referencedComponent, EntityProxy.Pattern pattern, ImmutableList fieldValues) {
        LOG.debug("ComposerSession {} - Composing Semantic: {}\n   Referencing: {}",
                transaction.hashCode(),
                semantic,
                referencedComponent);
        Write.semantic(semantic, stampEntity, referencedComponent, pattern, fieldValues);
        transaction.addComponent(semantic);
        return new SemanticComposer(transaction, stampEntity, semantic);
    }

    public SemanticComposer composeSemantic(SemanticTemplate template, EntityProxy referencedComponent) {
        template.setReferencedComponent(referencedComponent);
        Semantic semantic = template.getSemantic();
        LOG.debug("ComposerSession {} - Composing {} Semantic: {}\n   Referencing: {}",
                transaction.hashCode(),
                template.getClass().getSimpleName(),
                semantic,
                referencedComponent);
        template.save(stampEntity);
        transaction.addComponent(semantic);
        return new SemanticComposer(transaction, stampEntity, semantic);
    }

    public int componentsInSessionCount() {
        return transaction.componentsInTransactionCount();
    }

    public void cancel() {
        LOG.info("ComposerSession {} - Cancelling updates to {} Entities with stamp: {}",
                transaction.hashCode(),
                transaction.componentsInTransactionCount(),
                stampEntity);
        transaction.cancel();
    }

    @Override
    public void close() {
        LOG.info("ComposerSession {} - Commiting updates to {} Entities with stamp: {}",
                transaction.hashCode(),
                transaction.componentsInTransactionCount(),
                stampEntity);
        transaction.commit();
    }

}
