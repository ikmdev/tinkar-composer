package dev.ikm.tinkar.composer;

import dev.ikm.tinkar.entity.StampEntity;
import dev.ikm.tinkar.entity.transaction.Transaction;
import dev.ikm.tinkar.terms.EntityProxy;
import dev.ikm.tinkar.terms.EntityProxy.Pattern;
import dev.ikm.tinkar.terms.EntityProxy.Semantic;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;

import java.util.function.Consumer;

public class SemanticBuilder {

    private final Semantic semantic;
    private final Transaction sessionTransaction;
    private final StampEntity sessoinStamp;

    private EntityProxy reference;
    private Pattern pattern;
    private Consumer<MutableList<Object>> fieldsConsumer;

    public SemanticBuilder(Semantic semantic, Transaction sessionTransaction, StampEntity sessoinStamp) {
        this.semantic = semantic;
        this.sessionTransaction = sessionTransaction;
        this.sessoinStamp = sessoinStamp;
    }

    public SemanticBuilder reference(EntityProxy reference) {
        this.reference = reference;
        return this;
    }

    public SemanticBuilder pattern(Pattern pattern) {
        this.pattern = pattern;
        return this;
    }

    public SemanticBuilder fields(Consumer<MutableList<Object>> fieldsConsumer) {
        this.fieldsConsumer = fieldsConsumer;
        return this;
    }

    public SemanticComposer build() {
        MutableList<Object> fields = Lists.mutable.empty();
        fieldsConsumer.accept(fields);
        sessionTransaction.addComponent(semantic);
        Write.semantic(semantic, sessoinStamp, reference, pattern, fields.toImmutable());
        return new SemanticComposer(semantic, sessionTransaction, sessoinStamp);
    }
}
