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
package dev.ikm.tinkar.composer.template;

import dev.ikm.tinkar.composer.SemanticTemplate;
import dev.ikm.tinkar.schema.Field;
import dev.ikm.tinkar.schema.PublicId;
import dev.ikm.tinkar.terms.EntityProxy.Concept;
import dev.ikm.tinkar.terms.EntityProxy.Pattern;
import dev.ikm.tinkar.terms.EntityProxy.Semantic;
import dev.ikm.tinkar.terms.TinkarTerm;

import java.util.List;

import static dev.ikm.tinkar.composer.ChronologyBuilder.createPublicId;

public class Definition extends SemanticTemplate {

    public Definition() {}

    private Concept language;
    private String text;
    private Concept caseSignificance;

    /**
     * Sets the language for the Definition Semantic.
     * @param language the Definition language
     * @return the Definition SemanticTemplate for further method chaining
     */
    public Definition language(Concept language) {
        this.language = language;
        return this;
    }

    /**
     * Sets the text for the Definition Semantic.
     * @param text the Definition text
     * @return the Definition SemanticTemplate for further method chaining
     */
    public Definition text(String text) {
        this.text = text;
        return this;
    }

    /**
     * Sets the case significance value for the Definition Semantic.
     * @param caseSignificance the Definition case significance value
     * @return the Definition SemanticTemplate for further method chaining
     */
    public Definition caseSignificance(Concept caseSignificance) {
        this.caseSignificance = caseSignificance;
        return this;
    }

    @Override
    public Definition semantic(Semantic semantic) {
        this.setSemantic(semantic);
        return this;
    }

    @Override
    protected Pattern assignPattern() {
        return TinkarTerm.DESCRIPTION_PATTERN;
    }

    @Override
    protected List<Field> assignFieldValues() {
        // Create PublicIds for each Concept
        PublicId languageId = createPublicId(language);
        PublicId caseSignificanceId = createPublicId(caseSignificance);
        PublicId descriptionType = createPublicId(TinkarTerm.DEFINITION_DESCRIPTION_TYPE);

        // Create Fields for Semantic
        Field languageField = Field.newBuilder().setPublicId(languageId).build();
        Field textField = Field.newBuilder().setStringValue(text).build();
        Field caseSignificanceField = Field.newBuilder().setPublicId(caseSignificanceId).build();
        Field descriptionTypeField = Field.newBuilder().setPublicId(descriptionType).build();

        return List.of(languageField, textField, caseSignificanceField, descriptionTypeField);
    }

    @Override
    protected void validate() throws IllegalArgumentException {
        if (language==null || text == null || text.isEmpty() || caseSignificance==null) {
            throw new IllegalArgumentException("Definition requires language, text, and case significance");
        }
    }
}
