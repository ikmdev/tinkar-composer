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
import dev.ikm.tinkar.terms.EntityProxy;

import java.util.function.Consumer;

public abstract class Attachable {

    private Transaction sessionTransaction;
    private StampEntity<?> sessionStampEntity;
    private EntityProxy reference;

    protected void setSessionTransaction(Transaction sessionTransaction) {
        this.sessionTransaction = sessionTransaction;
    }

    protected Transaction getSessionTransaction() {
        return sessionTransaction;
    }

    protected void setSessionStampEntity(StampEntity<?> sessionStampEntity) {
        this.sessionStampEntity = sessionStampEntity;
    }

    protected StampEntity<?> getSessionStampEntity() {
        return sessionStampEntity;
    }

    protected void setReference(EntityProxy reference) {
        this.reference = reference;
    }

    protected EntityProxy getReference() {
        if (reference == null) {
            throw new IllegalStateException("Reference not set");
        }
        return reference;
    }

    protected abstract EntityProxy asReferenceComponent();

    protected abstract void validateAndWrite();

    protected abstract void validate() throws IllegalArgumentException;

    private void initializeAttachable(Attachable childAttachable) {
        childAttachable.setReference(this.asReferenceComponent());
        childAttachable.setSessionTransaction(sessionTransaction);
        childAttachable.setSessionStampEntity(sessionStampEntity);
    }

    /**
     * Creates a Semantic which references this Component from a pre-built template.
     * @param semanticTemplate the pre-built template
     * @return this Component as an Attachable
     */
    public Attachable attach(SemanticTemplate semanticTemplate) {
        initializeAttachable(semanticTemplate);
        semanticTemplate.validateAndWrite();
        return this;
    }

    /**
     * Creates a Semantic which references this Component using a consumer to configure the template.
     * @param type the SemanticTemplate subclass to instantiate
     * @param consumer configures the template before it is written
     * @return this Component as an Attachable
     * @param <T> the SemanticTemplate type
     */
    public <T extends SemanticTemplate> Attachable attach(Class<T> type, Consumer<T> consumer) {
        try {
            T template = type.getDeclaredConstructor().newInstance();
            initializeAttachable(template);
            consumer.accept(template);
            template.validateAndWrite();
            return this;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to instantiate " + type.getSimpleName(), e);
        }
    }
}
