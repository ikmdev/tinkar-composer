package dev.ikm.tinkar.composer.domain;

import dev.ikm.tinkar.terms.EntityProxy.Concept;

public record PatternDef(Concept meaning, Concept purpose, Concept datatype, int index) {
}
