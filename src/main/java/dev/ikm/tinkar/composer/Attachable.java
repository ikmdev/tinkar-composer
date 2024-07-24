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

import dev.ikm.tinkar.composer.template.*;
import dev.ikm.tinkar.entity.StampEntity;
import dev.ikm.tinkar.entity.transaction.Transaction;
import dev.ikm.tinkar.terms.EntityProxy;

import java.util.function.Supplier;

public abstract class Attachable {

    private Transaction sessionTransaction;
    private StampEntity sessionStampEntity;
    private EntityProxy reference;

    protected void setSessionTransaction(Transaction sessionTransaction) {
        this.sessionTransaction = sessionTransaction;
    }

    protected void setSessionStampEntity(StampEntity sessionStampEntity) {
        this.sessionStampEntity = sessionStampEntity;
    }

    protected Transaction getSessionTransaction() {
        return sessionTransaction;
    }

    protected StampEntity getSessionStampEntity() {
        return sessionStampEntity;
    }

    public EntityProxy getReference() {
        return reference;
    }

    public void setReference(EntityProxy reference) {
        this.reference = reference;
    }

    abstract EntityProxy asReference();

    private void initializeAttachable(Attachable childAttachable) {
        childAttachable.setReference(childAttachable.asReference());
        childAttachable.setSessionTransaction(sessionTransaction);
        childAttachable.setSessionStampEntity(sessionStampEntity);
    }

    public Attachable attach(AxiomSyntaxConsumer axiomSyntaxConsumer) {
        AxiomSyntax axiomSyntax = new AxiomSyntax();
        initializeAttachable(axiomSyntax);
        axiomSyntaxConsumer.accept(axiomSyntax);
        writeSemanticTemplate(axiomSyntax);
        return this;
    }

    public Attachable attach(AxiomSyntax axiomSyntax) {
        initializeAttachable(axiomSyntax);
		writeSemanticTemplate(axiomSyntax);
        return this;
    }

    public Attachable attach(CommentConsumer commentConsumer) {
        Comment comment = new Comment();
        initializeAttachable(comment);
        commentConsumer.accept(comment);
        writeSemanticTemplate(comment);
        return this;
    }

    public Attachable attach(Comment comment) {
        initializeAttachable(comment);
        writeSemanticTemplate(comment);
        return this;
    }

    public Attachable attach(DefinitionConsumer definitionConsumer) {
        Definition definition = new Definition();
        initializeAttachable(definition);
        definitionConsumer.accept(definition);
        writeSemanticTemplate(definition);
        return this;
    }

    public Attachable attach(Definition definition) {
        initializeAttachable(definition);
		writeSemanticTemplate(definition);
        return this;
    }

    public Attachable attach(FullyQualifiedNameConsumer fullyQualifiedNameConsumer) {
        FullyQualifiedName fullyQualifiedName = new FullyQualifiedName();
        initializeAttachable(fullyQualifiedName);
        fullyQualifiedNameConsumer.accept(fullyQualifiedName);
		writeSemanticTemplate(fullyQualifiedName);
        return this;
    }

    public Attachable attach(FullyQualifiedName fullyQualifiedName) {
        initializeAttachable(fullyQualifiedName);
		writeSemanticTemplate(fullyQualifiedName);
        return this;
    }

    public Attachable attach(GBDialectConsumer gbDialectConsumer) {
        GBDialect gbDialect = new GBDialect();
        initializeAttachable(gbDialect);
        gbDialectConsumer.accept(gbDialect);
		writeSemanticTemplate(gbDialect);
        return this;
    }

    public Attachable attach(GBDialect gbDialect) {
        initializeAttachable(gbDialect);
		writeSemanticTemplate(gbDialect);
        return this;
    }

    public Attachable attach(IdentifierConsumer identifierConsumer) {
        Identifier identifier = new Identifier();
        initializeAttachable(identifier);
        identifierConsumer.accept(identifier);
		writeSemanticTemplate(identifier);
        return this;
    }

    public Attachable attach(Identifier identifier) {
        initializeAttachable(identifier);
		writeSemanticTemplate(identifier);
        return this;
    }

    public Attachable attach(StatedAxiomConsumer statedAxiomConsumer) {
        StatedAxiom statedAxiom = new StatedAxiom();
        initializeAttachable(statedAxiom);
        statedAxiomConsumer.accept(statedAxiom);
		writeSemanticTemplate(statedAxiom);
        return this;
    }

    public Attachable attach(StatedAxiom statedAxiom) {
        initializeAttachable(statedAxiom);
		writeSemanticTemplate(statedAxiom);
        return this;
    }

    public Attachable attach(SynonymConsumer synonymConsumer) {
        Synonym synonym = new Synonym();
        initializeAttachable(synonym);
        synonymConsumer.accept(synonym);
		writeSemanticTemplate(synonym);
        return this;
    }

    public Attachable attach(Synonym synonym) {
        initializeAttachable(synonym);
		writeSemanticTemplate(synonym);
        return this;
    }

    public Attachable attach(USDialectConsumer usDialectConsumer) {
        USDialect usDialect = new USDialect();
        initializeAttachable(usDialect);
        usDialectConsumer.accept(usDialect);
		writeSemanticTemplate(usDialect);
        return this;
    }

    public Attachable attach(USDialect usDialect) {
        initializeAttachable(usDialect);
		writeSemanticTemplate(usDialect);
        return this;
    }

    public Attachable attach(TinkarBaseModelConsumer tinkarBaseModelConsumer) {
        TinkarBaseModel tinkarBaseModel = new TinkarBaseModel();
        initializeAttachable(tinkarBaseModel);
        tinkarBaseModelConsumer.accept(tinkarBaseModel);
		writeSemanticTemplate(tinkarBaseModel);
        return this;
    }

    public Attachable attach(TinkarBaseModel tinkarBaseModel) {
        initializeAttachable(tinkarBaseModel);
		writeSemanticTemplate(tinkarBaseModel);
        return this;
    }

    public Attachable attach(KometBaseModelConsumer kometBaseModelConsumer) {
        KometBaseModel kometBaseModel = new KometBaseModel();
        initializeAttachable(kometBaseModel);
        kometBaseModelConsumer.accept(kometBaseModel);
		writeSemanticTemplate(kometBaseModel);
        return this;
    }

    public Attachable attach(KometBaseModel kometBaseModel) {
        initializeAttachable(kometBaseModel);
		writeSemanticTemplate(kometBaseModel);
        return this;
    }

    // TODO: Is this appropriate now that we have to propogate parameters?
    public Attachable attach(Supplier<SemanticTemplate> semanticTemplateSupplier) {
        attach(semanticTemplateSupplier.get());
        return this;
    }

    public Attachable attach(SemanticTemplate semanticTemplate) {
        initializeAttachable(semanticTemplate);
		writeSemanticTemplate(semanticTemplate);
        return this;
    }

    protected void writeSemanticTemplate(SemanticTemplate semanticTemplate) {
        System.out.println(semanticTemplate.semantic());
        sessionTransaction.addComponent(semanticTemplate.semantic());
        Write.semantic(semanticTemplate.semantic(),
                sessionStampEntity,
                this.asReference(),
                semanticTemplate.assignPattern(),
                semanticTemplate.assignFields());
    }

}



