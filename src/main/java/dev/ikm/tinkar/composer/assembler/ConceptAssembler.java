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
import dev.ikm.tinkar.composer.Write;
import dev.ikm.tinkar.terms.EntityProxy;
import dev.ikm.tinkar.terms.EntityProxy.Concept;

import java.util.UUID;

public class ConceptAssembler extends Attachable {

    private Concept concept;

    /**
     * Sets the Concept Proxy containing the PublicId for the Concept Entity being assembled.
     * <br />
     * If not supplied, a random PublicId will be assigned.
     * @param concept
     * @return the ConceptAssembler for further method chaining
     */
    public ConceptAssembler concept(Concept concept) {
        this.concept = concept;
        return this;
    }

    protected Concept concept() {
        if (concept == null) {
            concept = Concept.make(PublicIds.newRandom());
        }
        return concept;
    }

    /**
     * Sets the PublicId for the Concept Entity being assembled.
     * @param publicId
     * @return the ConceptAssembler for further method chaining
     */
    public ConceptAssembler publicId(PublicId publicId) {
        concept = Concept.make(publicId);
        return this;
    }

    /**
     * Adds a UUID to the current PublicId for the Concept Entity being assembled.
     * @param uuid
     * @return the ConceptAssembler for further method chaining
     */
    public ConceptAssembler addUuid(UUID uuid) {
        if (concept==null) {
            concept = Concept.make(PublicIds.of(uuid));
        } else {
            UUID[] oldUuids = concept.publicId().asUuidArray();
            UUID[] newUuids = new UUID[oldUuids.length + 1];
            System.arraycopy(oldUuids, 0, newUuids, 0, oldUuids.length);
            newUuids[newUuids.length-1] = uuid;
            PublicId newPublicId = PublicIds.of(newUuids);
            concept = Concept.make(concept.description(), newPublicId);
        }
        return this;
    }

    @Override
    protected EntityProxy asReferenceComponent() {
        return concept();
    }

    @Override
    protected void validateAndWrite() {
        validate();
        super.getSessionTransaction().addComponent(concept());
        Write.concept(concept(), super.getSessionStampEntity());
    }

    @Override
    protected void validate() throws IllegalArgumentException {
        // Nothing to validate
        // If a PublicId is not supplied, the default behavior is to generate one
    }

    @Override
    protected EntityProxy getReference() {
        throw new UnsupportedOperationException("ConceptAssembler does not have a reference");
    }

}
