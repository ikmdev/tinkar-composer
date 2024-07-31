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

public class SemanticAssembler extends Attachable {

    private Semantic semantic;
    private Pattern pattern;
    private Consumer<MutableList<Object>> fieldValuesConsumer;

    /**
     * Sets the Semantic Proxy containing the PublicId for the Semantic Entity being assembled.
     * <br />
     * If not supplied, a random PublicId will be assigned.
     * @param semantic
     * @return the SemanticAssembler for further method chaining
     */
    public SemanticAssembler semantic(Semantic semantic) {
        this.semantic = semantic;
        return this;
    }

    /**
     * Sets the reference - the Component to which the Semantic information applies.
     * @param reference
     * @return the SemanticAssembler for further method chaining
     */
    public SemanticAssembler reference(EntityProxy reference) {
        super.setReference(reference);
        return this;
    }

    protected Semantic semantic() {
        if (semantic == null) {
            semantic = Semantic.make(PublicIds.newRandom());
        }
        return semantic;
    }

    /**
     * Sets the pattern which defines the fields for the Semantic.
     * @param pattern
     * @return the SemanticAssembler for further method chaining
     */
    public SemanticAssembler pattern(Pattern pattern) {
        this.pattern = pattern;
        return this;
    }

    protected Pattern pattern() {
        return pattern;
    }

    /**
     * Sets a consumer that defines the field values for the Semantic.
     * @param fieldValuesConsumer
     * @return the SemanticAssembler for further method chaining
     */
    public SemanticAssembler fieldValues(Consumer<MutableList<Object>> fieldValuesConsumer) {
        this.fieldValuesConsumer = fieldValuesConsumer;
        return this;
    }

    protected ImmutableList<Object> fieldValues() {
        MutableList<Object> mutableList = Lists.mutable.empty();
        fieldValuesConsumer.accept(mutableList);
        return mutableList.toImmutable();
    }

    @Override
    protected EntityProxy asReferenceComponent() {
        return semantic();
    }

    @Override
    protected void validateAndWrite() {
        validate();
        super.getSessionTransaction().addComponent(semantic());
        Write.semantic(semantic(), super.getSessionStampEntity(), getReference(), pattern(), fieldValues());
    }

    @Override
    protected void validate() throws IllegalArgumentException {
        if (super.getReference()==null || pattern==null || fieldValuesConsumer==null) {
            throw new IllegalArgumentException("Semantic requires reference, pattern, and field values");
        }
    }

}
