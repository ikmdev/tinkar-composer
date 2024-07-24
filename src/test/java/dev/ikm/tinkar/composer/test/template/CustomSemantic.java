package dev.ikm.tinkar.composer.test.template;

import dev.ikm.tinkar.composer.SemanticTemplate;
import dev.ikm.tinkar.terms.EntityProxy;
import dev.ikm.tinkar.terms.TinkarTerm;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;

public class CustomSemantic extends SemanticTemplate {

    private String text;

    public CustomSemantic text(String text) {
        this.text = text;
        return this;
    }

    @Override
    protected EntityProxy.Pattern assignPattern() {
        return TinkarTerm.COMMENT_PATTERN;
    }

    @Override
    protected ImmutableList<Object> assignFields() {
        return Lists.immutable.of(text);
    }

    @Override
    protected void validate() {

    }

}
