package dev.ikm.tinkar.composer.template;

import dev.ikm.tinkar.composer.SemanticTemplate;
import dev.ikm.tinkar.terms.EntityProxy.Concept;
import dev.ikm.tinkar.terms.TinkarTerm;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;

public class GBDialect extends SemanticTemplate {

    private Concept acceptability;

    public GBDialect() {
        super(TinkarTerm.GB_DIALECT_PATTERN);
    }

    public GBDialect acceptability(Concept acceptability) {
        this.acceptability = acceptability;
        return this;
    }

    @Override
    protected ImmutableList<Object> assignFields() {
        return Lists.immutable.of(acceptability);
    }
}
