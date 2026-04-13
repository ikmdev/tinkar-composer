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

import dev.ikm.tinkar.common.id.PublicIds;
import dev.ikm.tinkar.composer.Attachable;
import dev.ikm.tinkar.composer.Write;
import dev.ikm.tinkar.terms.EntityProxy;
import dev.ikm.tinkar.terms.EntityProxy.Pattern;
import dev.ikm.tinkar.terms.EntityProxy.Semantic;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;

import java.util.function.Consumer;

/**
 * Assembler for constructing Semantic entities within a Tinkar composition session.
 * Provides a fluent API for setting the semantic identity, reference component, pattern,
 * and field values. Supports attaching child semantics through the {@link Attachable} base class.
 * If no identity is explicitly provided, a random {@link dev.ikm.tinkar.common.id.PublicId}
 * is generated on first access.
 */
public class SemanticAssembler extends Attachable {

    /**
     * Creates a new SemanticAssembler with no preset identity, reference, or pattern.
     */
    public SemanticAssembler() {}

    private Semantic semantic;
    private Pattern pattern;
    private Consumer<MutableList<Object>> fieldValuesConsumer;

    /**
     * Sets the Semantic Proxy containing the PublicId for the Semantic Entity being assembled.
     * <br />
     * If not supplied, a random PublicId will be assigned.
     * @param semantic the Semantic proxy identifying the entity to assemble
     * @return the SemanticAssembler for further method chaining
     */
    public SemanticAssembler semantic(Semantic semantic) {
        this.semantic = semantic;
        return this;
    }

    /**
     * Sets the reference - the Component to which the Semantic information applies.
     * @param reference the component to which this semantic information applies
     * @return the SemanticAssembler for further method chaining
     */
    public SemanticAssembler reference(EntityProxy reference) {
        super.setReference(reference);
        return this;
    }

    /**
     * Returns the Semantic proxy for this assembler, generating a random identity if none was set.
     *
     * @return the Semantic proxy for the entity being assembled
     */
    protected Semantic semantic() {
        if (semantic == null) {
            semantic = Semantic.make(PublicIds.newRandom());
        }
        return semantic;
    }

    /**
     * Sets the pattern which defines the fields for the Semantic.
     * @param pattern the Pattern that defines the field structure for this semantic
     * @return the SemanticAssembler for further method chaining
     */
    public SemanticAssembler pattern(Pattern pattern) {
        this.pattern = pattern;
        return this;
    }

    /**
     * Returns the pattern for this semantic.
     *
     * @return the Pattern proxy, or {@code null} if not yet set
     */
    protected Pattern pattern() {
        return pattern;
    }

    /**
     * Sets a consumer that defines the field values for the Semantic.
     * @param fieldValuesConsumer a consumer that populates a mutable list with the field values
     * @return the SemanticAssembler for further method chaining
     */
    public SemanticAssembler fieldValues(Consumer<MutableList<Object>> fieldValuesConsumer) {
        this.fieldValuesConsumer = fieldValuesConsumer;
        return this;
    }

    /**
     * Evaluates the field values consumer and returns the resulting immutable list of field values.
     *
     * @return an immutable list of field values for this semantic
     */
    protected ImmutableList<Object> fieldValues() {
        MutableList<Object> mutableList = Lists.mutable.empty();
        fieldValuesConsumer.accept(mutableList);
        return mutableList.toImmutable();
    }

    /**
     * {@inheritDoc}
     *
     * @return the Semantic proxy as the reference component for attached child semantics
     */
    @Override
    protected EntityProxy asReferenceComponent() {
        return semantic();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateAndWrite() {
        validate();
        super.getSessionTransaction().addComponent(semantic());
        Write.semantic(semantic(), super.getSessionStampEntity(), getReference(), pattern(), fieldValues());
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException if reference, pattern, or field values consumer is not set
     */
    @Override
    protected void validate() throws IllegalArgumentException {
        if (super.getReference()==null || pattern==null || fieldValuesConsumer==null) {
            throw new IllegalArgumentException("Semantic requires reference, pattern, and field values");
        }
    }

}
