package dev.ikm.tinkar.composer.template;

import dev.ikm.tinkar.composer.SemanticTemplate;
import dev.ikm.tinkar.terms.EntityProxy;
import dev.ikm.tinkar.terms.TinkarTerm;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;

public class Synonym extends SemanticTemplate {

    private EntityProxy.Concept language;
    private String text;
    private EntityProxy.Concept caseSignificance;

    public Synonym() {
        super(TinkarTerm.DESCRIPTION_PATTERN);
    }

    public Synonym language(EntityProxy.Concept language) {
        this.language = language;
        return this;
    }

    public Synonym text(String text) {
        this.text = text;
        return this;
    }

    public Synonym caseSignificance(EntityProxy.Concept caseSignificance) {
        this.caseSignificance = caseSignificance;
        return this;
    }

    @Override
    protected ImmutableList<Object> assignFields() {
        return Lists.immutable.of(language, text, caseSignificance, TinkarTerm.REGULAR_NAME_DESCRIPTION_TYPE);
    }
}
