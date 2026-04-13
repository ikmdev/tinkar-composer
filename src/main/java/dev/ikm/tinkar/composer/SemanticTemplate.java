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

import dev.ikm.tinkar.common.id.PublicIds;
import dev.ikm.tinkar.terms.EntityProxy;
import dev.ikm.tinkar.terms.EntityProxy.Pattern;
import dev.ikm.tinkar.terms.EntityProxy.Semantic;
import org.eclipse.collections.api.list.ImmutableList;

/**
 * Base class for semantic templates that define a pattern and field values for writing
 * semantic entities. Subclasses specify the pattern and populate the field values.
 */
public abstract class SemanticTemplate extends Attachable {

    /** Constructs a SemanticTemplate with no semantic identity assigned. */
    protected SemanticTemplate() {}

    private Semantic semantic;

    /**
     * Sets the Semantic Proxy containing the PublicId for the Semantic.
     * <br />
     * If not supplied, a random PublicId will be assigned.
     * @param semantic the Semantic Proxy containing the PublicId
     */
    protected void setSemantic(Semantic semantic) {
        this.semantic = semantic;
    }

    /**
     * Returns the semantic proxy for this template, creating a default one if none has been set.
     *
     * @return the semantic proxy
     */
    protected Semantic semantic() {
        if (semantic == null) {
            semantic = defaultSemantic();
        }
        return semantic;
    }

    /**
     * Creates a default semantic proxy with a randomly generated PublicId.
     * Subclasses may override to provide deterministic identity.
     *
     * @return a new semantic proxy with a random PublicId
     */
    protected Semantic defaultSemantic() {
        return Semantic.make(PublicIds.newRandom());
    }

    /**
     * Sets the Semantic for the SemanticTemplate
     * @param semantic the Semantic Proxy containing the PublicId to assign
     * @return this SemanticTemplate as the same SubType that called it
     */
    public abstract SemanticTemplate semantic(Semantic semantic);

    /**
     * Returns the pattern that defines the structure of this semantic template.
     *
     * @return the pattern proxy for this template
     */
    protected abstract Pattern assignPattern();

    /**
     * Returns the field values to write for this semantic version.
     *
     * @return an immutable list of field values matching the pattern's field definitions
     */
    protected abstract ImmutableList<Object> assignFieldValues();

    @Override
    protected EntityProxy asReferenceComponent() {
        return semantic();
    }

    @Override
    protected void validateAndWrite() {
        validate();
        if (super.getReference()==null) {
            throw new IllegalArgumentException("Semantic requires a reference");
        }
        super.getSessionTransaction().addComponent(semantic());
        Write.semantic(semantic(),
                super.getSessionStampEntity(),
                getReference(),
                assignPattern(),
                assignFieldValues());
    }

}
