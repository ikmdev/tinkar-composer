package dev.ikm.tinkar.composer;

import dev.ikm.tinkar.composer.template.*;
import dev.ikm.tinkar.entity.StampEntity;
import dev.ikm.tinkar.entity.transaction.Transaction;
import dev.ikm.tinkar.terms.EntityProxy;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class SemanticComposer {

    private final EntityProxy reference;
    private final Transaction sessionTransaction;
    private final StampEntity sessionStamp;

    public SemanticComposer(EntityProxy reference, Transaction sessionTransaction, StampEntity sessionStamp) {
        this.reference = reference;
        this.sessionTransaction = sessionTransaction;
        this.sessionStamp = sessionStamp;
    }

    public SemanticComposer attach(AxiomSyntaxConsumer axiomSyntaxConsumer) {
        AxiomSyntax axiomSyntax = new AxiomSyntax();
        axiomSyntax.semanticComposer(this);
        axiomSyntaxConsumer.accept(axiomSyntax);
        attach(axiomSyntax);
        return this;
    }

    public SemanticComposer attach(AxiomSyntax axiomSyntax) {
        writeSemanticTemplate(axiomSyntax);
        return this;
    }

    public SemanticComposer attach(CommentConsumer commentConsumer) {
        Comment comment = new Comment();
        comment.semanticComposer(this);
        commentConsumer.accept(comment);
        attach(comment);
        return this;
    }

    public SemanticComposer attach(Comment comment) {
        writeSemanticTemplate(comment);
        return this;
    }

    public SemanticComposer attach(DefinitionConsumer definitionConsumer) {
        Definition definition = new Definition();
        definition.semanticComposer(this);
        definitionConsumer.accept(definition);
        attach(definition);
        return this;
    }

    public SemanticComposer attach(Definition definition) {
        writeSemanticTemplate(definition);
        return this;
    }

    public SemanticComposer attach(FullyQualifiedNameConsumer fullyQualifiedNameConsumer) {
        FullyQualifiedName fullyQualifiedName = new FullyQualifiedName();
        fullyQualifiedName.semanticComposer(this);
        fullyQualifiedNameConsumer.accept(fullyQualifiedName);
        attach(fullyQualifiedName);
        return this;
    }

    public SemanticComposer attach(FullyQualifiedName fullyQualifiedName) {
        writeSemanticTemplate(fullyQualifiedName);
        return this;
    }

    public SemanticComposer attach(GBDialectConsumer gbDialectConsumer) {
        GBDialect gbDialect = new GBDialect();
        gbDialect.semanticComposer(this);
        gbDialectConsumer.accept(gbDialect);
        attach(gbDialect);
        return this;
    }

    public SemanticComposer attach(GBDialect gbDialect) {
        writeSemanticTemplate(gbDialect);
        return this;
    }

    public SemanticComposer attach(IdentifierConsumer identifierConsumer) {
        Identifier identifier = new Identifier();
        identifier.semanticComposer(this);
        identifierConsumer.accept(identifier);
        attach(identifier);
        return this;
    }

    public SemanticComposer attach(Identifier identifier) {
        writeSemanticTemplate(identifier);
        return this;
    }

    public SemanticComposer attach(StatedAxiomConsumer statedAxiomConsumer) {
        StatedAxiom statedAxiom = new StatedAxiom();
        statedAxiom.semanticComposer(this);
        statedAxiomConsumer.accept(statedAxiom);
        attach(statedAxiom);
        return this;
    }

    public SemanticComposer attach(StatedAxiom statedAxiom) {
        writeSemanticTemplate(statedAxiom);
        return this;
    }

    public SemanticComposer attach(SynonymConsumer synonymConsumer) {
        Synonym synonym = new Synonym();
        synonym.semanticComposer(this);
        synonymConsumer.accept(synonym);
        attach(synonym);
        return this;
    }

    public SemanticComposer attach(Synonym synonym) {
        writeSemanticTemplate(synonym);
        return this;
    }

    public SemanticComposer attach(USDialectConsumer usDialectConsumer) {
        USDialect usDialect = new USDialect();
        usDialect.semanticComposer(this);
        usDialectConsumer.accept(usDialect);
        attach(usDialect);
        return this;
    }

    public SemanticComposer attach(USDialect usDialect) {
        writeSemanticTemplate(usDialect);
        return this;
    }

    public SemanticComposer attach(TinkarBaseModelConsumer tinkarBaseModelConsumer) {
        TinkarBaseModel tinkarBaseModel = new TinkarBaseModel();
        tinkarBaseModel.semanticComposer(this);
        tinkarBaseModelConsumer.accept(tinkarBaseModel);
        attach(tinkarBaseModel);
        return this;
    }

    public SemanticComposer attach(TinkarBaseModel tinkarBaseModel) {
        writeSemanticTemplate(tinkarBaseModel);
        return this;
    }

    public SemanticComposer attach(KometBaseModelConsumer kometBaseModelConsumer) {
        KometBaseModel kometBaseModel = new KometBaseModel();
        kometBaseModel.semanticComposer(this);
        kometBaseModelConsumer.accept(kometBaseModel);
        attach(kometBaseModel);
        return this;
    }

    public SemanticComposer attach(KometBaseModel kometBaseModel) {
        writeSemanticTemplate(kometBaseModel);
        return this;
    }

    public SemanticComposer attach(Supplier<SemanticTemplate> semanticTemplateSupplier) {
        attach(semanticTemplateSupplier.get());
        return this;
    }

    public SemanticComposer attach(SemanticTemplate semanticTemplate) {
        writeSemanticTemplate(semanticTemplate);
        return this;
    }

    private void writeSemanticTemplate(SemanticTemplate semanticTemplate) {
        sessionTransaction.addComponent(semanticTemplate.semantic());
        Write.semantic(semanticTemplate.semantic(),
                sessionStamp,
                reference,
                semanticTemplate.pattern(),
                semanticTemplate.assignFields());
    }
}
