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
package dev.ikm.tinkar.composer.template;

import dev.ikm.tinkar.composer.SemanticTemplate;
import dev.ikm.tinkar.terms.EntityProxy.Concept;
import dev.ikm.tinkar.terms.EntityProxy.Pattern;
import dev.ikm.tinkar.terms.EntityProxy.Semantic;
import dev.ikm.tinkar.terms.TinkarTerm;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;

public class GBDialect extends SemanticTemplate {

    private Concept acceptability;

    /**
     * Sets the acceptability value for the GBDialect Semantic.
     * @param acceptability the GBDialect acceptability value
     * @return the GBDialect SemanticTemplate for further method chaining
     */
    public GBDialect acceptability(Concept acceptability) {
        this.acceptability = acceptability;
        return this;
    }

    @Override
    public GBDialect semantic(Semantic semantic) {
        this.setSemantic(semantic);
        return this;
    }

    @Override
    protected Pattern assignPattern() {
        return TinkarTerm.GB_DIALECT_PATTERN;
    }

    @Override
    protected ImmutableList<Object> assignFieldValues() {
        return Lists.immutable.of(acceptability);
    }

    @Override
    protected void validate() throws IllegalArgumentException {
        if (acceptability==null) {
            throw new IllegalArgumentException("GBDialect requires acceptability");
        }
    }
}
