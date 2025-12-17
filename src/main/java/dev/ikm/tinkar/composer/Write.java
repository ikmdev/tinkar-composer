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
package dev.ikm.tinkar.composer;

import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.entity.*;
import dev.ikm.tinkar.terms.EntityProxy;
import dev.ikm.tinkar.terms.EntityProxy.Concept;
import dev.ikm.tinkar.terms.EntityProxy.Pattern;
import dev.ikm.tinkar.terms.EntityProxy.Semantic;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.primitive.LongLists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.list.primitive.ImmutableLongList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;

public class Write {

    private static final Logger LOG = LoggerFactory.getLogger(Write.class);

    public record PatternDefinition(Concept meaning, Concept purpose, Concept datatype, int index) {}

    private static long[] createAdditionalLongs(PublicId publicId) {
        long[] additionalLongs = new long[(publicId.uuidCount() * 2) - 2];
        int index = 0;
        for (int i = 1; i < publicId.uuidCount(); i++) {
            UUID uuid = publicId.asUuidArray()[i];
            additionalLongs[index++] = uuid.getMostSignificantBits();
            additionalLongs[index++] = uuid.getLeastSignificantBits();
        }
        return additionalLongs.length == 0 ? null : additionalLongs;
    }

    public static void concept(Concept concept, PublicId stampId) {
        //Pull out primordial UUID from PublicId
        UUID primordialUUID = concept.asUuidArray()[0];

        //Process additional UUID longs from PublicId
        ImmutableLongList additionalLongs = LongLists.immutable.of(createAdditionalLongs(concept));

        //Create empty version list
        RecordListBuilder<ConceptVersionRecord> versions = RecordListBuilder.make();

        //Assign nids for PublicIds
        int stampNid = EntityService.get().nidForPublicId(stampId);

        //Create Concept Chronology
        ConceptRecord conceptRecord = ConceptRecordBuilder.builder()
                .nid(concept.nid())
                .leastSignificantBits(primordialUUID.getLeastSignificantBits())
                .mostSignificantBits(primordialUUID.getMostSignificantBits())
                .additionalUuidLongs(additionalLongs)
                .versions(versions)
                .build();

        //Append Concept Version
        versions.add(ConceptVersionRecordBuilder.builder()
                .chronology(conceptRecord)
                .stampNid(stampNid)
                .build());

        //Rebuild the ConceptRecord with the now populated version data
        ConceptEntity<? extends ConceptEntityVersion> conceptEntity = ConceptRecordBuilder.builder(conceptRecord).versions(versions.toImmutable()).build();
        EntityService.get().putEntity(conceptEntity);
    }

    public static void pattern(Pattern pattern, PublicId stampId,
                               Concept meaning, Concept purpose,
                               List<PatternDefinition> patternDefinitions){
        //Pull out primordial UUID from PublicId
        UUID primordialUUID = pattern.asUuidArray()[0];

        //Process additional UUID longs from PublicId
        ImmutableLongList additionalLongs = LongLists.immutable.of(createAdditionalLongs(pattern));

        //Create empty version list
        RecordListBuilder<PatternVersionRecord> versions = RecordListBuilder.make();

        //Assign nids for PublicIds
        int stampNid = EntityService.get().nidForPublicId(stampId);

        //Create Pattern Chronology
        PatternRecord patternRecord = PatternRecordBuilder.builder()
                .nid(pattern.nid())
                .leastSignificantBits(primordialUUID.getLeastSignificantBits())
                .mostSignificantBits(primordialUUID.getMostSignificantBits())
                .additionalUuidLongs(additionalLongs)
                .versions(versions.toImmutable())
                .build();

        //Create individual pattern definitions
        MutableList<FieldDefinitionRecord> fieldDefinitions = Lists.mutable.empty();
        patternDefinitions.forEach(patternDefinition -> {
            int meaningNid = EntityService.get().nidForPublicId(patternDefinition.meaning());
            int purposeNid = EntityService.get().nidForPublicId(patternDefinition.purpose());
            int dataTypeNid = EntityService.get().nidForPublicId(patternDefinition.datatype());

            FieldDefinitionRecord fieldDefinitionRecord = FieldDefinitionRecordBuilder.builder()
                    .patternNid(pattern.nid())
                    .meaningNid(meaningNid)
                    .purposeNid(purposeNid)
                    .dataTypeNid(dataTypeNid)
                    .indexInPattern(patternDefinition.index())
                    .patternVersionStampNid(stampNid)
                    .build();
            fieldDefinitions.add(fieldDefinitionRecord);
        });

        //Append new Pattern Version
        versions.add(PatternVersionRecordBuilder.builder()
                .chronology(patternRecord)
                .stampNid(stampNid)
                .semanticMeaningNid(meaning.nid())
                .semanticPurposeNid(purpose.nid())
                .fieldDefinitions(fieldDefinitions.toImmutable())
                .build());

        //Rebuild the Pattern with the now populated version data
        PatternEntity<? extends PatternEntityVersion> patternEntity = PatternRecordBuilder.builder(patternRecord).versions(versions.toImmutable()).build();
        EntityService.get().putEntity(patternEntity);
    }

    public static void semantic(Semantic semantic, PublicId stampId, EntityProxy referencedComponent, Pattern pattern, ImmutableList fieldValues) {
        //Assign primordial UUID from PublicId
        UUID primordialUUID = semantic.asUuidArray()[0];

        //Process additional UUID longs from PublicId
        ImmutableLongList additionalLongs = LongLists.immutable.of(createAdditionalLongs(semantic));

        //Create empty version list
        RecordListBuilder<SemanticVersionRecord> versions = RecordListBuilder.make();

        //Assign nids for PublicIds
        int stampNid = EntityService.get().nidForPublicId(stampId);

        //Create Semantic Chronology
        SemanticRecord semanticRecord = SemanticRecordBuilder.builder()
                .nid(semantic.nid())
                .leastSignificantBits(primordialUUID.getLeastSignificantBits())
                .mostSignificantBits(primordialUUID.getMostSignificantBits())
                .additionalUuidLongs(additionalLongs)
                .patternNid(pattern.nid())
                .referencedComponentNid(referencedComponent.nid())
                .versions(versions.toImmutable())
                .build();

        //Append new Semantic Version
        versions.add(SemanticVersionRecordBuilder.builder()
                .chronology(semanticRecord)
                .stampNid(stampNid)
                .fieldValues(fieldValues)
                .build());

        //Rebuild the Semantic with the now populated version data
        SemanticEntity<? extends SemanticEntityVersion> semanticEntity = SemanticRecordBuilder
                .builder(semanticRecord)
                .versions(versions.toImmutable()).build();
        EntityService.get().putEntity(semanticEntity);
    }

}
