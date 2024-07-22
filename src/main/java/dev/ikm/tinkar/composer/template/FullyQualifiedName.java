package dev.ikm.tinkar.composer.template;

import dev.ikm.tinkar.composer.SemanticTemplate;
import dev.ikm.tinkar.terms.EntityProxy.Concept;
import dev.ikm.tinkar.terms.TinkarTerm;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;

public class FullyQualifiedName extends SemanticTemplate {

    private Concept language;
    private String text;
    private Concept caseSignificance;

    public FullyQualifiedName() {
        super(TinkarTerm.DESCRIPTION_PATTERN);
    }

    public FullyQualifiedName language(Concept language) {
        this.language = language;
        return this;
    }

    public FullyQualifiedName text(String text) {
        this.text = text;
        return this;
    }

    public FullyQualifiedName caseSignificance(Concept caseSignificance) {
        this.caseSignificance = caseSignificance;
        return this;
    }

    @Override
    protected ImmutableList<Object> assignFields() {
        return Lists.immutable.of(language, text, caseSignificance, TinkarTerm.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE);
    }
}
