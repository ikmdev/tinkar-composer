package dev.ikm.tinkar.composer.assembler;

@FunctionalInterface
public interface PatternAssemblerConsumer {
    void accept(PatternAssembler patternAssembler);
}
