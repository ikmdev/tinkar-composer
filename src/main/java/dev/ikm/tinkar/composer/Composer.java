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
import dev.ikm.tinkar.composer.create.om.PatternFieldDetail;
import dev.ikm.tinkar.entity.transaction.Transaction;
import dev.ikm.tinkar.terms.EntityProxy;
import dev.ikm.tinkar.terms.EntityProxy.Concept;
import dev.ikm.tinkar.terms.EntityProxy.Pattern;
import dev.ikm.tinkar.terms.EntityProxy.Semantic;
import org.eclipse.collections.api.list.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class Composer {

    private static final Logger LOG = LoggerFactory.getLogger(Composer.class);
    private final Transaction transaction;
    private final PublicId stampId;

    public Composer(Transaction transaction, PublicId stampId) {
        this.transaction = transaction;
        this.stampId = stampId;
    }

    public SemanticComposer concept(Concept concept) {
        LOG.debug("Session {} - Composing Concept: {}",
                transaction.hashCode(),
                concept);
        Write.concept(concept, stampId);
        transaction.addComponent(concept);
        return new SemanticComposer(transaction, stampId, concept);
    }

    public SemanticComposer pattern(Pattern pattern, Concept meaning, Concept purpose, List<PatternFieldDetail> patternFieldDetails) {
        LOG.debug("Session {} - Composing Pattern: {}",
                transaction.hashCode(),
                pattern);
        Write.pattern(pattern, stampId, meaning, purpose, patternFieldDetails);
        transaction.addComponent(pattern);
        return new SemanticComposer(transaction, stampId, pattern);
    }

    public SemanticComposer semantic(Semantic semantic, EntityProxy referencedComponent, Pattern pattern, ImmutableList fieldValues) {
        LOG.debug("Session {} - Composing Semantic: {}",
                transaction.hashCode(),
                semantic);
        Write.semantic(semantic, stampId, referencedComponent, pattern, fieldValues);
        transaction.addComponent(semantic);
        return new SemanticComposer(transaction, stampId, semantic);
    }

}
