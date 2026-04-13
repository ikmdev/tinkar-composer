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
import dev.ikm.tinkar.terms.EntityProxy.Concept;
import dev.ikm.tinkar.terms.EntityProxy.Pattern;

import java.util.ArrayList;
import java.util.List;

/**
 * Assembler for constructing Pattern entities within a Tinkar composition session.
 * Provides a fluent API for setting pattern identity, meaning, purpose, and field definitions.
 * Supports attaching semantics through the {@link Attachable} base class.
 * If no identity is explicitly provided, a random {@link dev.ikm.tinkar.common.id.PublicId}
 * is generated on first access.
 */
public class PatternAssembler extends Attachable {

    private Pattern pattern;
    private Concept meaning;
    private Concept purpose;
    private final List<Write.PatternDefinition> patternDefinitions;

    /**
     * Creates a new PatternAssembler with an empty list of field definitions.
     */
    public PatternAssembler() {
        this.patternDefinitions = new ArrayList<>();
    }

    /**
     * Sets the Pattern Proxy containing the PublicId for the Pattern Entity being assembled.
     * <br />
     * If not supplied, a random PublicId will be assigned.
     * @param pattern the Pattern proxy identifying the entity to assemble
     * @return the PatternAssembler for further method chaining
     */
    public PatternAssembler pattern(Pattern pattern) {
        this.pattern = pattern;
        return this;
    }

    /**
     * Returns the Pattern proxy for this assembler, generating a random identity if none was set.
     *
     * @return the Pattern proxy for the entity being assembled
     */
    protected Pattern pattern() {
        if (pattern == null) {
            pattern = Pattern.make(PublicIds.newRandom());
        }
        return pattern;
    }

    /**
     * Sets the meaning of the Pattern.
     * @param meaning the Concept representing the meaning of this pattern
     * @return the PatternAssembler for further method chaining
     */
    public PatternAssembler meaning(Concept meaning) {
        this.meaning = meaning;
        return this;
    }

    /**
     * Returns the meaning Concept for this pattern.
     *
     * @return the meaning Concept, or {@code null} if not yet set
     */
    protected Concept meaning() {
        return meaning;
    }

    /**
     * Sets the purpose of the Pattern.
     * @param purpose the Concept representing the purpose of this pattern
     * @return the PatternAssembler for further method chaining
     */
    public PatternAssembler purpose(Concept purpose) {
        this.purpose = purpose;
        return this;
    }

    /**
     * Returns the purpose Concept for this pattern.
     *
     * @return the purpose Concept, or {@code null} if not yet set
     */
    protected Concept purpose() {
        return purpose;
    }

    /**
     * Adds a field definition with a predefined index to the Pattern.
     * @param meaning the meaning of the field definition
     * @param purpose the purpose of the field definition
     * @param datatype the datatype of the field definition
     * @param index the index of the field definition
     * @return the PatternAssembler for further method chaining
     */
    public PatternAssembler fieldDefinition(Concept meaning, Concept purpose, Concept datatype, int index) {
        patternDefinitions.add(new Write.PatternDefinition(meaning, purpose, datatype, index));
        return this;
    }

    /**
     * Appends a field definition with incremented index to the Pattern.
     * @param meaning the meaning of the field definition
     * @param purpose the purpose of the field definition
     * @param datatype the datatype of the field definition
     * @return the PatternAssembler for further method chaining
     */
    public PatternAssembler fieldDefinition(Concept meaning, Concept purpose, Concept datatype) {
        int index = patternDefinitions.size();
        patternDefinitions.add(new Write.PatternDefinition(meaning, purpose, datatype, index));
        return this;
    }

    /**
     * Returns the list of field definitions added to this pattern.
     *
     * @return the list of {@link Write.PatternDefinition} entries
     */
    protected List<Write.PatternDefinition> fieldDefinitions() {
        return patternDefinitions;
    }

    /**
     * {@inheritDoc}
     *
     * @return the Pattern proxy as the reference component for attached semantics
     */
    @Override
    protected EntityProxy asReferenceComponent() {
        return pattern();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateAndWrite() {
        validate();
        super.getSessionTransaction().addComponent(pattern());
        Write.pattern(pattern(), super.getSessionStampEntity(), meaning(), purpose(), fieldDefinitions());
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException if meaning or purpose is not set, if any field definition
     *         is missing meaning/purpose/datatype, if duplicate indexes exist, or if indexes
     *         are not a contiguous zero-based sequence
     */
    @Override
    protected void validate() throws IllegalArgumentException {
        if (meaning==null || purpose==null) {
            throw new IllegalArgumentException("Pattern requires meaning and purpose");
        }
        List<Integer> indexes = new ArrayList<>();
        patternDefinitions.forEach(patternDefinition -> {
            if (patternDefinition.meaning()==null || patternDefinition.purpose()==null || patternDefinition.datatype()==null) {
                throw new IllegalArgumentException("Pattern Definition requires meaning, purpose, and datatype");
            }
            if (indexes.contains(patternDefinition.index())) {
                throw new IllegalArgumentException("Pattern Definitions cannot have the same index");
            }
            indexes.add(patternDefinition.index());
        });
        int actualIndexSum = indexes.stream().mapToInt(Integer::intValue).sum();
        int sequentialIndexSum = (indexes.size() * (indexes.size() -1))/2;
        if (actualIndexSum != sequentialIndexSum) {
            throw new IllegalArgumentException("Pattern Definition index value out of bounds for field definition array of size " + indexes.size());
        }
    }

    /**
     * {@inheritDoc}
     *
     * @return never returns normally
     * @throws UnsupportedOperationException always; PatternAssembler does not have a reference
     */
    @Override
    protected EntityProxy getReference() {
        throw new UnsupportedOperationException("PatternAssembler does not have a reference");
    }

}
