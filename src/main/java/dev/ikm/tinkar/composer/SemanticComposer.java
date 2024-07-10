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
import dev.ikm.tinkar.composer.constituent.SemanticConstituent;
import dev.ikm.tinkar.terms.EntityProxy;
import dev.ikm.tinkar.terms.EntityProxy.Concept;

import java.util.*;

public class SemanticComposer {

    private final PublicId stampId;
    private final Concept referencedConcept;

    public SemanticComposer(PublicId stampId, Concept referencedConcept) {
        this.stampId = stampId;
        this.referencedConcept = referencedConcept;
    }

    public SemanticComposer with(SemanticConstituent constituent) {
        constituent.create(referencedConcept);
        System.out.println(constituent.getSemantic().description() + " references " + referencedConcept.description());
        return this;
    }

    public SemanticComposer with(SemanticConstituent... constituents) {
        Arrays.stream(constituents).forEach(constituent -> System.out.println(constituent.getSemantic().description() + " references " + referencedConcept.description()));
        return this;
    }

}
