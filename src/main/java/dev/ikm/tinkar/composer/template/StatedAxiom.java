package dev.ikm.tinkar.composer.template;

import dev.ikm.tinkar.composer.SemanticTemplate;
import dev.ikm.tinkar.terms.EntityProxy.Pattern;
import dev.ikm.tinkar.terms.TinkarTerm;
import org.eclipse.collections.api.list.ImmutableList;

public class StatedAxiom extends SemanticTemplate {

    //TODO - finish this with new EL++ reasoner updates


    @Override
    protected Pattern assignPattern() {
        return TinkarTerm.EL_PLUS_PLUS_STATED_AXIOMS_PATTERN;
    }

    @Override
    protected ImmutableList<Object> assignFields() {
        return null;
    }

    @Override
    protected void validate() {

    }
}
