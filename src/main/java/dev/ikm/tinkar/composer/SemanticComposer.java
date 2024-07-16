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
import dev.ikm.tinkar.composer.templates.SemanticTemplate;
import dev.ikm.tinkar.entity.transaction.Transaction;
import dev.ikm.tinkar.terms.EntityProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public class SemanticComposer {

    private static final Logger LOG = LoggerFactory.getLogger(SemanticComposer.class);
    private final Transaction transaction;
    private final PublicId stampId;
    private final EntityProxy referencedComponent;

    public SemanticComposer(Transaction transaction, PublicId stampId, EntityProxy referencedConcept) {
        this.transaction = transaction;
        this.stampId = stampId;
        this.referencedComponent = referencedConcept;
    }

    public SemanticComposer with(SemanticTemplate templates) {
        templates.setReferencedComponent(referencedComponent);
        templates.getSemanticTemplates().forEach(semanticTemplate -> {
            LOG.debug("ComposerSession {} - Saving {} Semantic: {}\n   Referencing: {}",
                    transaction.hashCode(),
                    semanticTemplate.getClass().getSimpleName(),
                    semanticTemplate.getSemantic(),
                    semanticTemplate.getReferencedComponent());
            semanticTemplate.save(stampId);
            transaction.addComponent(semanticTemplate.getSemantic());
        });
        return this;
    }

    public SemanticComposer with(SemanticTemplate... templates) {
        Arrays.stream(templates).forEach(this::with);
        return this;
    }

}
