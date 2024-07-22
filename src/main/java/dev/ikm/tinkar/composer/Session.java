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

import dev.ikm.tinkar.entity.StampEntity;
import dev.ikm.tinkar.entity.transaction.Transaction;
import dev.ikm.tinkar.terms.EntityProxy.Concept;
import dev.ikm.tinkar.terms.EntityProxy.Pattern;
import dev.ikm.tinkar.terms.EntityProxy.Semantic;
import dev.ikm.tinkar.terms.State;
import org.eclipse.collections.api.list.MutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.util.function.Consumer;

public class Session implements Closeable {

    private static final Logger LOG = LoggerFactory.getLogger(Session.class);
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
     * @see State
     */
    public Session(Transaction transaction, StampEntity stampEntity){
        this.transaction = transaction;
        this.stampEntity = stampEntity;
        LOG.info("ComposerSession {} - Initializing Session with stamp: {}", transaction.hashCode(), stampEntity);
    }

    public SemanticComposer compose(Concept concept) {
        return new SemanticComposer(concept, transaction, stampEntity);
    }

    public SemanticBuilder compose(Semantic semantic) {
        return new SemanticBuilder(semantic, transaction, stampEntity);
    }

    public SemanticComposer compose(Semantic semantic, Concept reference, Pattern pattern, Consumer<MutableList<Object>> fieldsConsumer) {
        SemanticBuilder semanticBuilder = new SemanticBuilder(semantic, transaction, stampEntity);
        semanticBuilder.reference(reference);
        semanticBuilder.pattern(pattern);
        semanticBuilder.fields(fieldsConsumer);
        semanticBuilder.build();
        return new SemanticComposer(semantic, transaction, stampEntity);
    }

    public PatternBuilder compose(Pattern pattern) {
        return new PatternBuilder(pattern, transaction, stampEntity);
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
