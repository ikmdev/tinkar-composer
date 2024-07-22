package dev.ikm.tinkar.composer.template;

import dev.ikm.tinkar.composer.SemanticTemplate;
import dev.ikm.tinkar.terms.EntityProxy;
import dev.ikm.tinkar.terms.EntityProxy.Concept;
import dev.ikm.tinkar.terms.TinkarTerm;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;

public class Identifier extends SemanticTemplate {

    private Concept source;
    private String identifier;

    public Identifier() {
        super(TinkarTerm.IDENTIFIER_PATTERN);
    }

    public Identifier source(Concept source) {
        this.source = source;
        return this;
    }

    public Identifier identifier(String identifier) {
        this.identifier = identifier;
        return this;
    }

    @Override
    protected ImmutableList<Object> assignFields() {
        return Lists.immutable.of(source, identifier);
    }
}
