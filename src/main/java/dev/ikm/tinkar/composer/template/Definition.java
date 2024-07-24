package dev.ikm.tinkar.composer.template;

import dev.ikm.tinkar.composer.SemanticTemplate;
import dev.ikm.tinkar.terms.EntityProxy.Concept;
import dev.ikm.tinkar.terms.EntityProxy.Pattern;
import dev.ikm.tinkar.terms.TinkarTerm;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;

public class Definition extends SemanticTemplate {

    private Concept language;
    private String text;
    private Concept caseSignificance;

    public Definition language(Concept language) {
        this.language = language;
        return this;
    }

    public Definition text(String text) {
        this.text = text;
        return this;
    }

    public Definition caseSignificance(Concept caseSignificance) {
        this.caseSignificance = caseSignificance;
        return this;
    }

    @Override
    protected Pattern assignPattern() {
        return TinkarTerm.DESCRIPTION_PATTERN;
    }

    @Override
    protected ImmutableList<Object> assignFields() {
        return Lists.immutable.of(language, text, caseSignificance, TinkarTerm.DEFINITION_DESCRIPTION_TYPE);
    }

    @Override
    protected void validate() {

    }
}
