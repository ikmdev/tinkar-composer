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
package dev.ikm.tinkar.composer.core;

import java.util.UUID;
import java.util.function.Consumer;

import dev.ikm.tinkar.composer.core.SemanticTemplate;
import dev.ikm.tinkar.composer.core.io.PackageWriter;
import dev.ikm.tinkar.schema.StampChronology;
import dev.ikm.tinkar.schema.TinkarMsg;
import dev.ikm.tinkar.terms.EntityProxy;

public abstract class Attachable {

    protected Attachable() {}

    private StampChronology sessionStampChronology;
    private UUID sessionId;
    private EntityProxy reference;
    private PackageWriter packageWriter;
    private TransformRecord.Builder recordBuilder;

    protected void setSessionStampChronology(StampChronology stampChronology) {
        this.sessionStampChronology = stampChronology;
    }

    protected StampChronology getSessionStampChronology() {
        return sessionStampChronology;
    }

    protected void setSessionId(UUID sessionId) {
        this.sessionId = sessionId;
    }

    protected UUID getSessionId() {
        if (sessionId == null) {
            throw new IllegalStateException("Session id not set");
        }
        return sessionId;
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

    protected void setPackageWriter(PackageWriter packageWriter) {
        this.packageWriter = packageWriter;
    }

    protected PackageWriter getPackageWriter() {
        return packageWriter;
    }

    protected void setRecordBuilder(TransformRecord.Builder recordBuilder) {
        this.recordBuilder = recordBuilder;
    }

    protected TransformRecord.Builder getRecordBuilder() {
        return recordBuilder;
    }

    protected abstract EntityProxy asReferenceComponent();

    protected abstract TinkarMsg validateAndWrite();

    protected abstract void validate() throws IllegalArgumentException;

    private void initializeAttachable(Attachable childAttachable) {
        childAttachable.setReference(this.asReferenceComponent());
        childAttachable.setSessionStampChronology(sessionStampChronology);
        childAttachable.setSessionId(getSessionId());
        childAttachable.setPackageWriter(packageWriter);
        childAttachable.setRecordBuilder(recordBuilder);
    }

    /**
     * Creates a Semantic which references this Component from a pre-built template.
     * @param semanticTemplate the pre-built template
     * @return this Component as an Attachable
     */
    public Attachable attach(SemanticTemplate semanticTemplate) {
        initializeAttachable(semanticTemplate);
        TinkarMsg tinkarMsg = semanticTemplate.validateAndWrite();
        packageWriter.writeToPackage(tinkarMsg);
        recordBuilder.incrementSemantics();
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
            TinkarMsg tinkarMsg = template.validateAndWrite();
            packageWriter.writeToPackage(tinkarMsg);
            recordBuilder.incrementSemantics();
            return this;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to instantiate " + type.getSimpleName(), e);
        }
    }
}
