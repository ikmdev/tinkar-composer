package dev.ikm.tinkar.composer.template;

import dev.ikm.tinkar.composer.SemanticTemplate;
import dev.ikm.tinkar.terms.EntityProxy.Pattern;
import dev.ikm.tinkar.terms.TinkarTerm;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;

public class AxiomSyntax extends SemanticTemplate {

    private String text;

    public AxiomSyntax text(String text) {
        this.text = text;
        return this;
    }

    @Override
    protected Pattern assignPattern() {
        return TinkarTerm.OWL_AXIOM_SYNTAX_PATTERN;
    }

    @Override
    protected ImmutableList<Object> assignFields() {
        return Lists.immutable.of(text);
    }

    @Override
    protected void validate() {
        if (text == null || text.isEmpty()) {
            throw new IllegalArgumentException("Axiom syntax requires a text");
        }
    }
}
