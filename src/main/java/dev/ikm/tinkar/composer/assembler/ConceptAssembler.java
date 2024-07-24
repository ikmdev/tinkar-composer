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
package dev.ikm.tinkar.composer.assembler;

import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.common.id.PublicIds;
import dev.ikm.tinkar.composer.Attachable;
import dev.ikm.tinkar.terms.EntityProxy;
import dev.ikm.tinkar.terms.EntityProxy.Concept;

import java.util.UUID;

public class ConceptAssembler extends Attachable {

    private Concept concept;
    private PublicId publicId;

    public ConceptAssembler concept(Concept concept) {
        this.concept = concept;
        return this;
    }

    public Concept concept() {
        if (concept == null) {
            concept = Concept.make(publicId());
        }
        return concept;
    }

    public ConceptAssembler publicId(PublicId publicId) {
        this.publicId = publicId;
        return this;
    }

    public PublicId publicId() {
        if (publicId == null) {
            publicId = PublicIds.newRandom();
        }
        return publicId;
    }

    public ConceptAssembler uuid(UUID uuid) {
        UUID[] uuids = new UUID[publicId.uuidCount() + 1];
        uuids[publicId.uuidCount()] = uuid;
        publicId = PublicIds.of(uuids);
        return this;
    }

    @Override
    protected EntityProxy asReference() {
        return concept();
    }

    @Override
    protected void validate() throws IllegalArgumentException {
        // Nothing to validate
        // If a PublicId is not supplied, the default behavior is to generate one
    }

}
