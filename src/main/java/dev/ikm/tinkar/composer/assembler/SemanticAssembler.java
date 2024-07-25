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
import dev.ikm.tinkar.terms.EntityProxy;
import dev.ikm.tinkar.terms.EntityProxy.Pattern;
import dev.ikm.tinkar.terms.EntityProxy.Semantic;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;

import java.util.function.Consumer;

public class SemanticAssembler extends Attachable {

    private Semantic semantic;
    private EntityProxy reference;
    private Pattern pattern;
    private Consumer<MutableList<Object>> fieldsConsumer;

    public SemanticAssembler reference(EntityProxy reference) {
        this.reference = reference;
        return this;
    }

    public SemanticAssembler semantic(Semantic semantic) {
        this.semantic = semantic;
        return this;
    }

    public Semantic semantic() {
        if (semantic == null) {
            semantic = Semantic.make(PublicIds.newRandom());
        }
        return semantic;
    }

    public EntityProxy reference() {
        if (reference == null) {
            throw new IllegalStateException("Reference not set");
        }
        return reference;
    }

    public SemanticAssembler pattern(Pattern pattern) {
        this.pattern = pattern;
        return this;
    }

    public Pattern pattern() {
        return pattern;
    }

    public SemanticAssembler fields(Consumer<MutableList<Object>> fieldsConsumer) {
        this.fieldsConsumer = fieldsConsumer;
        return this;
    }

    public ImmutableList<Object> fields() {
        MutableList<Object> mutableList = Lists.mutable.empty();
        fieldsConsumer.accept(mutableList);
        return mutableList.toImmutable();
    }

    @Override
    protected EntityProxy asReference() {
        return semantic();
    }

    @Override
    protected void validate() throws IllegalArgumentException {
        if (reference==null || pattern==null || fieldsConsumer==null) {
            throw new IllegalArgumentException("Semantic requires reference, pattern, and field values");
        }
    }

}
