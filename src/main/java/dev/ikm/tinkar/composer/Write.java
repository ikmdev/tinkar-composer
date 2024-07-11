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
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.composer.create.om.PatternDetail;
import dev.ikm.tinkar.composer.create.om.PatternFieldDetail;
import dev.ikm.tinkar.entity.*;
import dev.ikm.tinkar.terms.EntityProxy;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class Write {

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

    public static void concept(PublicId concept, PublicId stampId){
        //Pull out primordial UUID from PublicId
        UUID primordialUUID = concept.asUuidArray()[0];

        //Process additional UUID longs from PublicId
        long[] additionalLongs = createAdditionalLongs(concept);

        //Create empty version list
        RecordListBuilder<ConceptVersionRecord> versions = RecordListBuilder.make();

        //Populate version list with existing versions if present
        if(PrimitiveData.get().hasPublicId(concept)) { //Must be evaluated before invoking: EntityService.get().nidForPublicId(concept)
            Entity entity = EntityService.get().getEntityFast(concept.asUuidArray());
            if (entity instanceof ConceptEntity conceptEntity) {
                conceptEntity.versions().forEach((version) -> {
                    versions.add(ConceptVersionRecordBuilder.builder((ConceptVersionRecord) version).build());
                });
            } else {
                throw new RuntimeException("Expecting PublicId of a ConceptEntity, but was:\n" + entity);
            }
        }

        //Assign nids for PublicIds
        int conceptNid = EntityService.get().nidForPublicId(concept);
        int stampNid = EntityService.get().nidForPublicId(stampId);

        //Create Concept Chronology
        ConceptRecord conceptRecord = ConceptRecordBuilder.builder()
                .nid(conceptNid)
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

    public static void pattern(PublicId pattern,
                             PublicId stampId,
                             PatternDetail patternDetail,
                             List<PatternFieldDetail> patternFieldDetails){
        //Pull out primordial UUID from PublicId
        UUID primordialUUID = pattern.asUuidArray()[0];

        //Process additional UUID longs from PublicId
        long[] additionalLongs = createAdditionalLongs(pattern);

        //Create empty version list
        RecordListBuilder<PatternVersionRecord> versions = RecordListBuilder.make();

        //Populate version list with existing versions if present
        if(PrimitiveData.get().hasPublicId(pattern)) { //Must be evaluated before invoking: EntityService.get().nidForPublicId(pattern)
            Entity entity = EntityService.get().getEntityFast(pattern.asUuidArray());
            if (entity instanceof PatternEntity patternEntity) {
                patternEntity.versions().forEach((version) -> {
                    versions.add(PatternVersionRecordBuilder.builder((PatternVersionRecord) version).build());
                });
            } else {
                throw new RuntimeException("Expecting PublicId of a PatternEntity, but was:\n" + entity);
            }
        }

        //Assign nids for PublicIds
        int patternNid = EntityService.get().nidForPublicId(pattern);
        int meaningConceptNid = EntityService.get().nidForPublicId(patternDetail.meaning());
        int purposeConceptNid = EntityService.get().nidForPublicId(patternDetail.purpose());
        int stampNid = EntityService.get().nidForPublicId(stampId);

        //Create Pattern Chronology
        PatternRecord patternRecord = PatternRecordBuilder.builder()
                .nid(patternNid)
                .leastSignificantBits(primordialUUID.getLeastSignificantBits())
                .mostSignificantBits(primordialUUID.getMostSignificantBits())
                .additionalUuidLongs(additionalLongs)
                .versions(versions.toImmutable())
                .build();

        //Create individual pattern definitions
        final AtomicInteger patternIndex = new AtomicInteger(0);
        MutableList<FieldDefinitionRecord> fieldDefinitions = Lists.mutable.empty();
        patternFieldDetails.forEach(patternFieldDetail -> {
            int meaningNid = EntityService.get().nidForPublicId(patternFieldDetail.meaning());
            int purposeNid = EntityService.get().nidForPublicId(patternFieldDetail.purpose());
            int dataTypeNid = EntityService.get().nidForPublicId(patternFieldDetail.dataType());

            FieldDefinitionRecord fieldDefinitionRecord = FieldDefinitionRecordBuilder.builder()
                    .patternNid(patternNid)
                    .meaningNid( meaningNid)
                    .purposeNid(purposeNid)
                    .dataTypeNid(dataTypeNid)
                    .indexInPattern(patternIndex.getAndIncrement())
                    .patternVersionStampNid(stampNid)
                    .build();
            fieldDefinitions.add(fieldDefinitionRecord);
        });

        //Append new Pattern Version
        versions.add(PatternVersionRecordBuilder.builder()
                .chronology(patternRecord)
                .stampNid(stampNid)
                .semanticMeaningNid(meaningConceptNid)
                .semanticPurposeNid(purposeConceptNid)
                .fieldDefinitions(fieldDefinitions.toImmutable())
                .build());

        //Rebuild the Pattern with the now populated version data
        PatternEntity<? extends PatternEntityVersion> patternEntity = PatternRecordBuilder.builder(patternRecord).versions(versions.toImmutable()).build();
        EntityService.get().putEntity(patternEntity);
    }

    public static void semantic(PublicId semantic, PublicId stampId, EntityProxy referencedComponent, EntityProxy.Pattern pattern, ImmutableList fieldValues) {
        //Assign primordial UUID from PublicId
        UUID primordialUUID = semantic.asUuidArray()[0];

        //Process additional UUID longs from PublicId
        long[] additionalLongs = createAdditionalLongs(semantic);

        //Create empty version list
        RecordListBuilder<SemanticVersionRecord> versions = RecordListBuilder.make();

        //Populate version list with existing versions if present
        if(PrimitiveData.get().hasPublicId(semantic)) { //Must be evaluated before invoking: EntityService.get().nidForPublicId(semantic)
            Entity entity = EntityService.get().getEntityFast(semantic.asUuidArray());
            if (entity instanceof SemanticEntity semanticEntity) {
                semanticEntity.versions().forEach((version) -> {
                    versions.add(SemanticVersionRecordBuilder.builder((SemanticVersionRecord) version).build());
                });
            } else {
                throw new RuntimeException("Expecting PublicId of a SemanticEntity, but was:\n" + entity);
            }
        }

        //Assign nids for PublicIds
        int semanticNid = EntityService.get().nidForPublicId(semantic);
        int patternNid = EntityService.get().nidForPublicId(pattern);
        int referencedComponentNid = EntityService.get().nidForPublicId(referencedComponent);
        int stampNid = EntityService.get().nidForPublicId(stampId);

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
