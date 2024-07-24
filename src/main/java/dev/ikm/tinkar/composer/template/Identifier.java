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
import dev.ikm.tinkar.terms.TinkarTerm;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;

public class Identifier extends SemanticTemplate {

    private Concept source;
    private String identifier;

    public Identifier source(Concept source) {
        this.source = source;
        return this;
    }

    public Identifier identifier(String identifier) {
        this.identifier = identifier;
        return this;
    }

    @Override
    protected Pattern assignPattern() {
        return TinkarTerm.IDENTIFIER_PATTERN;
    }

    @Override
    protected ImmutableList<Object> assignFields() {
        return Lists.immutable.of(source, identifier);
    }

    @Override
    protected void validate() throws IllegalArgumentException {
        if (source==null || identifier==null) {
            throw new IllegalArgumentException("Identifier requires a source and identifier");
        }
    }
}
