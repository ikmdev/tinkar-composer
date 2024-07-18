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

    /**
     * Provides a ComposerSession for creating Components with a <strong>predefined timestamp</strong>.
     * <br /><br />
     * Example use case: ingesting a file with previously defined data definitions.
     * <pre>{@code
     *    ComposerSession session = new ComposerSession(status, time, author, module, path);
     *    session.composeConcept(Concept.make("Example Concept with predefined time", PublicIds.newRandom()));
     *    session.close();
     * }</pre>
     * @param status the status applied to Components composed in this ComposerSession
     * @param time the timestamp (in epoch milliseconds) set for Components composed in this ComposerSession
     * @param author the author set for Components composed in this ComposerSession
     * @param module the module set for Components composed in this ComposerSession
     * @param path the path set for Components composed in this ComposerSession
     * @see State
     */
    public ComposerSession(State status, long time, Concept author, Concept module, Concept path){
        this.transaction = new Transaction();
        this.stampEntity = transaction.getStamp(status, time, author.publicId(), module.publicId(), path.publicId());
        LOG.info("ComposerSession {} - Initializing Session with stamp: {}", transaction.hashCode(), stampEntity);
    }

    /**
     * Provides a ComposerSession for creating Components with a <strong>current timestamp</strong>.
     * Timestamp will be defined as the time of close / commit.
     * <br /><br />
     * Example use case: create or edit Components resulting in net new Components / Versions.
     * <pre>{@code
     *    ComposerSession session = new ComposerSession(status, author, module, path);
     *    session.composeConcept(Concept.make("Example Concept with commit time", PublicIds.newRandom()));
     *    session.close();
     * }</pre>
     * @param status the status applied to Components composed in this ComposerSession
     * @param author the author set for Components composed in this ComposerSession
     * @param module the module set for Components composed in this ComposerSession
     * @param path the path set for Components composed in this ComposerSession
     * @see State
     */
    public ComposerSession(State status, Concept author, Concept module, Concept path) {
        this.transaction = new Transaction();
        this.stampEntity = transaction.getStamp(status, author, module, path);
        LOG.info("ComposerSession {} - Initializing Session with stamp: {}", transaction.hashCode(), stampEntity);
    }

    /**
     * Writes an uncommitted <strong>Concept</strong> to the database and returns a SemanticComposer
     * for composing Semantics related to this Concept.
     * @param concept the concept to write
     * @return {@link SemanticComposer}
     * @see SemanticComposer
     */
    public SemanticComposer composeConcept(Concept concept) {
        LOG.debug("ComposerSession {} - Composing Concept: {}",
                transaction.hashCode(),
                concept);
        Write.concept(concept, stampEntity);
        transaction.addComponent(concept);
        return new SemanticComposer(transaction, stampEntity, concept);
    }

    /**
     * Writes an uncommitted <strong>Pattern</strong> to the database and returns a SemanticComposer
     * for composing Semantics related to this Pattern.
     * @param pattern the pattern to write
     * @param meaning the meaning of the pattern
     * @param purpose the purpose of the pattern
     * @param patternFieldDetails the field definitions of the pattern
     * @return {@link SemanticComposer}
     * @see SemanticComposer
     * @see PatternFieldDetail
     */
    public SemanticComposer composePattern(EntityProxy.Pattern pattern, Concept meaning, Concept purpose, List<PatternFieldDetail> patternFieldDetails) {
        LOG.debug("ComposerSession {} - Composing Pattern: {}",
                transaction.hashCode(),
                pattern);
        Write.pattern(pattern, stampEntity, meaning, purpose, patternFieldDetails);
        transaction.addComponent(pattern);
        return new SemanticComposer(transaction, stampEntity, pattern);
    }

    /**
     * Writes an uncommitted <strong>Semantic</strong> to the database and returns a SemanticComposer
     * for composing Semantics related to this Semantic.
     * @param semantic the semantic to write
     * @param referencedComponent the component to which the semantic information applies
     * @param pattern the pattern whose field definitions define the meaning, purpose, and data type of the semantic field values
     * @param fieldValues the field values which provide information about the referenced component
     * @return {@link SemanticComposer}
     * @see SemanticComposer
     */
    public SemanticComposer composeSemantic(EntityProxy.Semantic semantic, EntityProxy referencedComponent, EntityProxy.Pattern pattern, ImmutableList fieldValues) {
        LOG.debug("ComposerSession {} - Composing Semantic: {}\n   Referencing: {}",
                transaction.hashCode(),
                semantic,
                referencedComponent);
        Write.semantic(semantic, stampEntity, referencedComponent, pattern, fieldValues);
        transaction.addComponent(semantic);
        return new SemanticComposer(transaction, stampEntity, semantic);
    }

    /**
     * Writes an uncommitted <strong>Semantic</strong> from a {@link SemanticTemplate} to the database
     * and returns a SemanticComposer for composing Semantics related to this Semantic.
     * @param template the semantic to write - defined as a SemanticTemplate
     * @param referencedComponent the component to which the semantic information applies
     * @return {@link SemanticComposer}
     * @see SemanticComposer
     * @see SemanticTemplate
     */
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

    /**
     * Provides the number of Components written by the ComposerSession.
     * This count does not include the STAMP associated with the ComposerSession.
     */
    public int componentsInSessionCount() {
        return transaction.componentsInTransactionCount();
    }

    /**
     * Cancels the Transaction and STAMP associated with this ComposerSession so that they will not be committed.
     */
    public void cancel() {
        LOG.info("ComposerSession {} - Cancelling updates to {} Entities with stamp: {}",
                transaction.hashCode(),
                transaction.componentsInTransactionCount(),
                stampEntity);
        transaction.cancel();
    }

    /**
     * Commits the Transaction and STAMP associated with this ComposerSession. If the ComposerSession
     * was not Constructed with a timestamp, then the timestamp will be set to the time of commit.
     */
    @Override
    public void close() {
        LOG.info("ComposerSession {} - Commiting updates to {} Entities with stamp: {}",
                transaction.hashCode(),
                transaction.componentsInTransactionCount(),
                stampEntity);
        transaction.commit();
    }

}
