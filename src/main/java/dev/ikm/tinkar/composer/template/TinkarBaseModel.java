package dev.ikm.tinkar.composer.template;

import dev.ikm.tinkar.composer.SemanticTemplate;
import dev.ikm.tinkar.terms.TinkarTerm;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;

public class TinkarBaseModel extends SemanticTemplate {


    public TinkarBaseModel() {
        super(TinkarTerm.TINKAR_BASE_MODEL_COMPONENT_PATTERN);
    }

    @Override
    protected ImmutableList<Object> assignFields() {
        return Lists.immutable.empty();
    }
}
