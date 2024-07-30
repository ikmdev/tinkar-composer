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

public abstract class Attachable {

    private Transaction sessionTransaction;
    private StampEntity sessionStampEntity;
    private EntityProxy reference;

    protected void setSessionTransaction(Transaction sessionTransaction) {
        this.sessionTransaction = sessionTransaction;
    }

    protected Transaction getSessionTransaction() {
        return sessionTransaction;
    }

    protected void setSessionStampEntity(StampEntity sessionStampEntity) {
        this.sessionStampEntity = sessionStampEntity;
    }

    protected StampEntity getSessionStampEntity() {
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

    //### START: Attach Options
    /**
     * Creates a Semantic which references this Component.
     * @param semanticTemplate
     * @return this Component as an Attachable
     */
    public Attachable attach(SemanticTemplate semanticTemplate) {
        initializeAttachable(semanticTemplate);
        semanticTemplate.validateAndWrite();
        return this;
    }

    /**
     * Creates an AxiomSyntax Semantic which references this Component.
     * @param axiomSyntaxConsumer
     * @return this Component as an Attachable
     */
    public Attachable attach(AxiomSyntaxConsumer axiomSyntaxConsumer) {
        AxiomSyntax axiomSyntax = new AxiomSyntax();
        initializeAttachable(axiomSyntax);
        axiomSyntaxConsumer.accept(axiomSyntax);
        axiomSyntax.validateAndWrite();
        return this;
    }

    /**
     * Creates an Comment Semantic which references this Component.
     * @param commentConsumer
     * @return this Component as an Attachable
     */
    public Attachable attach(CommentConsumer commentConsumer) {
        Comment comment = new Comment();
        initializeAttachable(comment);
        commentConsumer.accept(comment);
        comment.validateAndWrite();
        return this;
    }

    /**
     * Creates an Definition Semantic which references this Component.
     * @param definitionConsumer
     * @return this Component as an Attachable
     */
    public Attachable attach(DefinitionConsumer definitionConsumer) {
        Definition definition = new Definition();
        initializeAttachable(definition);
        definitionConsumer.accept(definition);
        definition.validateAndWrite();
        return this;
    }

    /**
     * Creates an FullyQualifiedName Semantic which references this Component.
     * @param fullyQualifiedNameConsumer
     * @return this Component as an Attachable
     */
    public Attachable attach(FullyQualifiedNameConsumer fullyQualifiedNameConsumer) {
        FullyQualifiedName fullyQualifiedName = new FullyQualifiedName();
        initializeAttachable(fullyQualifiedName);
        fullyQualifiedNameConsumer.accept(fullyQualifiedName);
		fullyQualifiedName.validateAndWrite();
        return this;
    }

    /**
      * Creates an GBDialect Semantic which references this Component.
      * @param gbDialectConsumer
      * @return this Component as an Attachable
      */
    public Attachable attach(GBDialectConsumer gbDialectConsumer) {
        GBDialect gbDialect = new GBDialect();
        initializeAttachable(gbDialect);
        gbDialectConsumer.accept(gbDialect);
		gbDialect.validateAndWrite();
        return this;
    }

    /**
      * Creates an Identifier Semantic which references this Component.
      * @param identifierConsumer
      * @return this Component as an Attachable
      */
    public Attachable attach(IdentifierConsumer identifierConsumer) {
        Identifier identifier = new Identifier();
        initializeAttachable(identifier);
        identifierConsumer.accept(identifier);
		identifier.validateAndWrite();
        return this;
    }

    /**
      * Creates an StatedAxiom Semantic which references this Component.
      * @param statedAxiomConsumer
      * @return this Component as an Attachable
      */
    public Attachable attach(StatedAxiomConsumer statedAxiomConsumer) {
        StatedAxiom statedAxiom = new StatedAxiom();
        initializeAttachable(statedAxiom);
        statedAxiomConsumer.accept(statedAxiom);
		statedAxiom.validateAndWrite();
        return this;
    }

    /**
      * Creates an Synonym Semantic which references this Component.
      * @param synonymConsumer
      * @return this Component as an Attachable
      */
    public Attachable attach(SynonymConsumer synonymConsumer) {
        Synonym synonym = new Synonym();
        initializeAttachable(synonym);
        synonymConsumer.accept(synonym);
		synonym.validateAndWrite();
        return this;
    }

    /**
      * Creates an USDialect Semantic which references this Component.
      * @param usDialectConsumer
      * @return this Component as an Attachable
      */
    public Attachable attach(USDialectConsumer usDialectConsumer) {
        USDialect usDialect = new USDialect();
        initializeAttachable(usDialect);
        usDialectConsumer.accept(usDialect);
		usDialect.validateAndWrite();
        return this;
    }

    /**
      * Creates an TinkarBaseModel Semantic which references this Component.
      * @param tinkarBaseModelConsumer
      * @return this Component as an Attachable
      */
    public Attachable attach(TinkarBaseModelConsumer tinkarBaseModelConsumer) {
        TinkarBaseModel tinkarBaseModel = new TinkarBaseModel();
        initializeAttachable(tinkarBaseModel);
        tinkarBaseModelConsumer.accept(tinkarBaseModel);
		tinkarBaseModel.validateAndWrite();
        return this;
    }

    /**
      * Creates an KometBaseModel Semantic which references this Component.
      * @param kometBaseModelConsumer
      * @return this Component as an Attachable
      */
    public Attachable attach(KometBaseModelConsumer kometBaseModelConsumer) {
        KometBaseModel kometBaseModel = new KometBaseModel();
        initializeAttachable(kometBaseModel);
        kometBaseModelConsumer.accept(kometBaseModel);
		kometBaseModel.validateAndWrite();
        return this;
    }

}
