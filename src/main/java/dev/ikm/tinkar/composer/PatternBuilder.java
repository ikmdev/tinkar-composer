package dev.ikm.tinkar.composer;

import dev.ikm.tinkar.composer.Write.PatternDefinition;
import dev.ikm.tinkar.entity.StampEntity;
import dev.ikm.tinkar.entity.transaction.Transaction;
import dev.ikm.tinkar.terms.EntityProxy.Concept;
import dev.ikm.tinkar.terms.EntityProxy.Pattern;

import java.util.ArrayList;
import java.util.List;

public class PatternBuilder {

    private final Pattern pattern;
    private final Transaction sessionTransaction;
    private final StampEntity sessoinStamp;

    private Concept meaning;
    private Concept purpose;
    private final List<PatternDefinition> patternDefinitions;

    public PatternBuilder(Pattern pattern, Transaction sessionTransaction, StampEntity sessoinStamp) {
        this.pattern = pattern;
        this.sessionTransaction = sessionTransaction;
        this.sessoinStamp = sessoinStamp;
        this.patternDefinitions = new ArrayList<>();
    }

    public PatternBuilder meaning(Concept meaning) {
        this.meaning = meaning;
        return this;
    }

    public PatternBuilder purpose(Concept purpose) {
        this.purpose = purpose;
        return this;
    }

    public PatternBuilder field(Concept meaning, Concept purpose, Concept datatype, int index) {
        patternDefinitions.add(new PatternDefinition(meaning, purpose, datatype, index));
        return this;
    }

    public SemanticComposer build() {
        sessionTransaction.addComponent(pattern);
        Write.pattern(pattern, sessoinStamp, meaning, purpose, patternDefinitions);
        return new SemanticComposer(pattern, sessionTransaction, sessoinStamp);
    }
}
