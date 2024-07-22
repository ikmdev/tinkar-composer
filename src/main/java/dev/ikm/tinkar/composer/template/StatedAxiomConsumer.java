package dev.ikm.tinkar.composer.template;

@FunctionalInterface
public interface StatedAxiomConsumer {
    void accept(StatedAxiom axiom);
}
