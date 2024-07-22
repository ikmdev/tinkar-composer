package dev.ikm.tinkar.composer.template;

import dev.ikm.tinkar.composer.SemanticTemplate;
import dev.ikm.tinkar.terms.TinkarTerm;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;

public class AxiomSyntax extends SemanticTemplate {

    private String text;

    public AxiomSyntax() {
        super(TinkarTerm.OWL_AXIOM_SYNTAX_PATTERN);
    }

    public AxiomSyntax text(String text) {
        this.text = text;
        return this;
    }

    @Override
    protected ImmutableList<Object> assignFields() {
        return Lists.immutable.of(text);
    }
}
