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
package dev.ikm.tinkar.composer.templates;

import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.composer.Write;
import dev.ikm.tinkar.terms.EntityProxy;
import dev.ikm.tinkar.terms.EntityProxy.Pattern;
import dev.ikm.tinkar.terms.EntityProxy.Semantic;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;

import java.util.Arrays;
import java.util.List;

public abstract class SemanticTemplate {

    private Semantic semantic;
    private EntityProxy referencedComponent;
    private final Pattern pattern;
    private final List<SemanticTemplate> descendantSemanticTemplates = Lists.mutable.empty();

    public SemanticTemplate(Semantic semantic, Pattern pattern) {
        this.semantic = semantic;
        this.pattern = pattern;
    }

    public Semantic getSemantic() {
        return semantic;
    }

    public void setReferencedComponent(EntityProxy referencedComponent) {
        this.referencedComponent = referencedComponent;
    }

    public EntityProxy getReferencedComponent() {
         return this.referencedComponent;
    }

    public List<SemanticTemplate> getSemanticTemplates() {
        MutableList<SemanticTemplate> semanticTemplates = Lists.mutable.of(this);
        semanticTemplates.addAll(descendantSemanticTemplates);
        return semanticTemplates;
    }

    protected abstract void setFields(MutableList<Object> fields);

    public SemanticTemplate with(SemanticTemplate semanticTemplate) {
        semanticTemplate.setReferencedComponent(this.semantic);
        descendantSemanticTemplates.addAll(semanticTemplate.getSemanticTemplates());
        return this;
    }

    public SemanticTemplate with(SemanticTemplate... templates) {
        Arrays.stream(templates).forEach(this::with);
        return this;
    }

    public void save(PublicId stampId){
        MutableList<Object> fieldValues = Lists.mutable.empty();
        setFields(fieldValues);
        Write.semantic(semantic.publicId(), stampId, referencedComponent, pattern, fieldValues.toImmutable());
    }

}
