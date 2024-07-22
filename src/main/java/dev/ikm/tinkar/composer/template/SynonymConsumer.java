package dev.ikm.tinkar.composer.template;

@FunctionalInterface
public interface SynonymConsumer {
    void accept(Synonym synonym);
}
