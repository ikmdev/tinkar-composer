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

/**
 * Base class for components that can have Semantics attached to them.
 * Subclasses hold a reference to the session's transaction and stamp, and provide
 * methods to attach {@link SemanticTemplate} instances that write Semantics
 * referencing this component.
 */
public abstract class Attachable {

    /** Constructs an Attachable with no session context. Context is set before use. */
    protected Attachable() {}

    private Transaction sessionTransaction;
    private StampEntity<?> sessionStampEntity;
    private EntityProxy reference;

    /**
     * Sets the transaction for this attachable's session context.
     *
     * @param sessionTransaction the transaction to associate with this attachable
     */
    protected void setSessionTransaction(Transaction sessionTransaction) {
        this.sessionTransaction = sessionTransaction;
    }

    /**
     * Returns the transaction associated with this attachable's session.
     *
     * @return the session transaction
     */
    protected Transaction getSessionTransaction() {
        return sessionTransaction;
    }

    /**
     * Sets the stamp entity for this attachable's session context.
     *
     * @param sessionStampEntity the stamp entity to associate with this attachable
     */
    protected void setSessionStampEntity(StampEntity<?> sessionStampEntity) {
        this.sessionStampEntity = sessionStampEntity;
    }

    /**
     * Returns the stamp entity associated with this attachable's session.
     *
     * @return the session stamp entity
     */
    protected StampEntity<?> getSessionStampEntity() {
        return sessionStampEntity;
    }

    /**
     * Sets the referenced component for this attachable.
     *
     * @param reference the entity proxy representing the referenced component
     */
    protected void setReference(EntityProxy reference) {
        this.reference = reference;
    }

    /**
     * Returns the referenced component for this attachable.
     *
     * @return the entity proxy representing the referenced component
     * @throws IllegalStateException if the reference has not been set
     */
    protected EntityProxy getReference() {
        if (reference == null) {
            throw new IllegalStateException("Reference not set");
        }
        return reference;
    }

    /**
     * Returns an entity proxy that represents this attachable as a reference component
     * for child semantics.
     *
     * @return the entity proxy for this component
     */
    protected abstract EntityProxy asReferenceComponent();

    /**
     * Validates this attachable's state and writes the corresponding entity
     * to the entity service.
     */
    protected abstract void validateAndWrite();

    /**
     * Validates that this attachable's required fields have been set.
     *
     * @throws IllegalArgumentException if required fields are missing or invalid
     */
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
