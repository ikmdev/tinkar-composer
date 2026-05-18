package dev.ikm.tinkar.composer.core.domain;

import dev.ikm.tinkar.terms.EntityProxy.Concept;

public record PatternDef(Concept meaning, Concept purpose, Concept datatype, int index) {
}
