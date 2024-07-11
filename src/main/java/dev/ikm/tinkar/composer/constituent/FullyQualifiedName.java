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
package dev.ikm.tinkar.composer.constituent;

import dev.ikm.tinkar.terms.EntityProxy.Concept;
import dev.ikm.tinkar.terms.EntityProxy.Semantic;
import dev.ikm.tinkar.terms.TinkarTerm;
import org.eclipse.collections.api.list.MutableList;

public class FullyQualifiedName extends SemanticTemplate {

    private final Concept language;
    private final String fqn;
    private final Concept caseSensitivity;
    private final Concept type;

    public FullyQualifiedName(Semantic semantic, Concept language, String fqn, Concept caseSensitivity) {
        super(semantic, TinkarTerm.DESCRIPTION_PATTERN);
        this.language = language;
        this.fqn = fqn;
        this.caseSensitivity = caseSensitivity;
        this.type = TinkarTerm.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE;
    }

    @Override
    public void setFields(MutableList<Object> fields) {
        fields.add(language);
        fields.add(fqn);
        fields.add(caseSensitivity);
        fields.add(type);
    }
}
