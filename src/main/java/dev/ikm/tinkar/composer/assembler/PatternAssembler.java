/*
 * Copyright © 2024 Integrated Knowledge Management (support@ikm.dev)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
        return this;
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

    public PatternAssembler field(Concept meaning, Concept purpose, Concept datatype) {
        int index = patternDefinitions.size();
        patternDefinitions.add(new Write.PatternDefinition(meaning, purpose, datatype, index));
        return this;
    }

    public List<Write.PatternDefinition> fields() {
        return patternDefinitions;
    }

    @Override
    protected EntityProxy asReference() {
        return pattern();
    }

    @Override
    protected void validate() throws IllegalArgumentException {
        if (meaning==null || purpose==null) {
            throw new IllegalArgumentException("Pattern requires meaning and purpose");
        }
        List<Integer> indexes = new ArrayList<>();
        patternDefinitions.forEach(patternDefinition -> {
            if (patternDefinition.meaning()==null || patternDefinition.purpose()==null || patternDefinition.datatype()==null) {
                throw new IllegalArgumentException("Pattern Definition requires meaning, purpose, and datatype");
            }
            if (indexes.contains(patternDefinition.index())) {
                throw new IllegalArgumentException("Pattern Definitions cannot have the same index");
            }
            indexes.add(patternDefinition.index());
        });
        int actualIndexSum = indexes.stream().mapToInt(Integer::intValue).sum();
        int sequentialIndexSum = (indexes.size() * (indexes.size() -1))/2;
        if (actualIndexSum != sequentialIndexSum) {
            throw new IllegalArgumentException("Pattern Definition indexes must be ordered sequentially starting from zero");
        }
    }
}
