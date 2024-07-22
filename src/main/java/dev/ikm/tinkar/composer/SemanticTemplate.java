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

import dev.ikm.tinkar.entity.StampEntity;
import dev.ikm.tinkar.entity.transaction.Transaction;
import dev.ikm.tinkar.terms.EntityProxy.Pattern;
import dev.ikm.tinkar.terms.EntityProxy.Semantic;
import org.eclipse.collections.api.list.ImmutableList;

public abstract class SemanticTemplate {

    private Transaction sessionTransaction;
    private StampEntity sessionStampEntity;
    private Semantic semantic;
    private final Pattern pattern;
    private SemanticComposer semanticComposer;

    public SemanticTemplate(Pattern pattern) {
        this.pattern = pattern;
    }

    public void semantic(Semantic semantic) {
        this.semantic = semantic;
    }

    public Semantic getSemantic() {
        return semantic;
    }

    public Pattern getPattern() {
        return pattern;
    }

    protected void setSemanticComposer(SemanticComposer semanticComposer) {
        this.semanticComposer = semanticComposer;
    }

    public void assemble(){
        Write.semantic(semantic, null, null, pattern, assignFields());
    }

    public SemanticComposer compose() {
        assemble();
        return new SemanticComposer(semantic, sessionTransaction, sessionStampEntity);
    }

    protected abstract ImmutableList<Object> assignFields();

}
