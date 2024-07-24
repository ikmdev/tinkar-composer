package dev.ikm.tinkar.composer.assembler;

import dev.ikm.tinkar.common.id.PublicIds;
import dev.ikm.tinkar.composer.Attachable;
import dev.ikm.tinkar.composer.Write;
import dev.ikm.tinkar.terms.EntityProxy;
import dev.ikm.tinkar.terms.EntityProxy.Concept;
import dev.ikm.tinkar.terms.EntityProxy.Pattern;

import java.util.ArrayList;
import java.util.List;

public class PatternAssembler extends Attachable {

    private Pattern pattern;
    private Concept meaning;
    private Concept purpose;
    private final List<Write.PatternDefinition> patternDefinitions;

    public PatternAssembler() {
        this.patternDefinitions = new ArrayList<>();
    }

    public PatternAssembler pattern(Pattern pattern) {
        this.pattern = pattern;
    }

    public Pattern pattern() {
        if (pattern == null) {
            pattern = Pattern.make(PublicIds.newRandom());
        }
        return pattern;
    }

    public PatternAssembler meaning(Concept meaning) {
        this.meaning = meaning;
        return this;
    }

    public Concept meaning() {
        return meaning;
    }

    public PatternAssembler purpose(Concept purpose) {
        this.purpose = purpose;
        return this;
    }

    public Concept purpose() {
        return purpose;
    }

    public PatternAssembler field(Concept meaning, Concept purpose, Concept datatype, int index) {
        patternDefinitions.add(new Write.PatternDefinition(meaning, purpose, datatype, index));
        return this;
    }

    public List<Write.PatternDefinition> fields() {
        return patternDefinitions;
    }

    @Override
    EntityProxy asReference() {
        return pattern();
    }
}
