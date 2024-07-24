package dev.ikm.tinkar.composer.assembler;

@FunctionalInterface
public interface SemanticAssemblerConsumer {
    void accept(SemanticAssembler semanticAssembler);
}
