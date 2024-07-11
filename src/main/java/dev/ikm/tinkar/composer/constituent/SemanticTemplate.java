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
package dev.ikm.tinkar.composer.constituent;

import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.entity.*;
import dev.ikm.tinkar.terms.EntityProxy;
import dev.ikm.tinkar.terms.EntityProxy.Pattern;
import dev.ikm.tinkar.terms.EntityProxy.Semantic;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static dev.ikm.tinkar.composer.Utility.createAdditionalLongs;

public abstract class SemanticTemplate {

    private Semantic semantic;
    private EntityProxy referencedComponent;
    private final Pattern pattern;
    private final List<SemanticTemplate> descendantSemanticTemplates = Lists.mutable.empty();

    public SemanticTemplate(Semantic semantic, Pattern pattern) {
        this.semantic = semantic;
        this.pattern = pattern;
    }

    public Semantic getSemantic() {
        return semantic;
    }

    public void setReferencedComponent(EntityProxy referencedComponent) {
        this.referencedComponent = referencedComponent;
    }

    public EntityProxy getReferencedComponent() {
         return this.referencedComponent;
    }

    public List<SemanticTemplate> getSemanticTemplates() {
        MutableList<SemanticTemplate> semanticTemplates = Lists.mutable.of(this);
        semanticTemplates.addAll(descendantSemanticTemplates);
        return semanticTemplates;
    }

    protected abstract void setFields(MutableList<Object> fields);

    public void save(PublicId stampId){
        //Assign primordial UUID from PublicId
        UUID primordialUUID = semantic.asUuidArray()[0];

        //Assign nids for PublicIds
        int semanticNid = EntityService.get().nidForPublicId(semantic);
        int patternNid = EntityService.get().nidForPublicId(pattern);
        int referencedComponentNid = EntityService.get().nidForPublicId(referencedComponent);
        int stampNid = EntityService.get().nidForPublicId(stampId);

        //Process additional UUID longs from PublicId
        long[] additionalLongs = createAdditionalLongs(semantic);

        //Create empty version list
        RecordListBuilder<SemanticVersionRecord> versions = RecordListBuilder.make();

        //Create Semantic Chronology
        SemanticRecord semanticRecord = SemanticRecordBuilder.builder()
                .nid(semanticNid)
                .leastSignificantBits(primordialUUID.getLeastSignificantBits())
                .mostSignificantBits(primordialUUID.getMostSignificantBits())
                .additionalUuidLongs(additionalLongs)
                .patternNid(patternNid)
                .referencedComponentNid(referencedComponentNid)
                .versions(versions.toImmutable())
                .build();

        //Create Semantic Version
        MutableList<Object> fields = Lists.mutable.empty();
        setFields(fields);
        versions.add(SemanticVersionRecordBuilder.builder()
                .chronology(semanticRecord)
                .stampNid(stampNid)
                .fieldValues(fields.toImmutable())
                .build());

        //Rebuild the Semantic with the now populated version data
        SemanticEntity<? extends SemanticEntityVersion> semanticEntity = SemanticRecordBuilder
                .builder(semanticRecord)
                .versions(versions.toImmutable()).build();
        EntityService.get().putEntity(semanticEntity);
    }

    public SemanticTemplate with(SemanticTemplate semanticTemplate) {
        semanticTemplate.setReferencedComponent(this.semantic);
        descendantSemanticTemplates.addAll(semanticTemplate.getSemanticTemplates());
        return this;
    }

    public SemanticTemplate with(SemanticTemplate... templates) {
        Arrays.stream(templates).forEach(this::with);
        return this;
    }

}
