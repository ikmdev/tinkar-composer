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
import dev.ikm.tinkar.composer.create.om.PatternDetail;
import dev.ikm.tinkar.composer.create.om.PatternFieldDetail;
import dev.ikm.tinkar.entity.transaction.Transaction;
import dev.ikm.tinkar.terms.EntityProxy.Concept;
import dev.ikm.tinkar.terms.EntityProxy.Pattern;
import dev.ikm.tinkar.terms.EntityProxy.Semantic;
import org.eclipse.collections.api.list.ImmutableList;

import java.util.List;

public class Composer {

    private final Transaction transaction;
    private final PublicId stampId;

    public Composer(Transaction transaction, PublicId stampId) {
        this.transaction = transaction;
        this.stampId = stampId;
    }

    public SemanticComposer concept(Concept concept) {
        Write.concept(concept.publicId(), stampId);
        return new SemanticComposer(transaction, stampId, concept);
    }

    public SemanticComposer pattern(Pattern pattern, PatternDetail patternDetail, List<PatternFieldDetail> patternFieldDetails) {
        Write.pattern(pattern.publicId(), stampId, patternDetail, patternFieldDetails);
        return new SemanticComposer(transaction, stampId, pattern);
    }

    public SemanticComposer semantic(Semantic semantic, Concept referencedComponent, Pattern pattern, ImmutableList fieldValues) {
        Write.semantic(semantic.publicId(), stampId, referencedComponent, pattern, fieldValues);
        return new SemanticComposer(transaction, stampId, semantic);
    }

}
