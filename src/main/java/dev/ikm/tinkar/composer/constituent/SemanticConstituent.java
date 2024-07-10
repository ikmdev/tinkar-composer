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
import dev.ikm.tinkar.composer.SemanticComposer;
import dev.ikm.tinkar.entity.*;
import dev.ikm.tinkar.terms.EntityProxy.Pattern;
import dev.ikm.tinkar.terms.EntityProxy.Semantic;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;

import java.util.UUID;
import java.util.function.Consumer;

import static dev.ikm.tinkar.composer.Utility.createAdditionalLongs;

public abstract class SemanticConstituent {

    private Semantic semantic;
    private final Pattern referencedPattern;
    private final PublicId stampId;

    public SemanticConstituent(Semantic semantic,
                               Pattern referencedPattern,
                               PublicId stampId) {
        this.semantic = semantic;
        this.referencedPattern = referencedPattern;
        this.stampId = stampId;
    }

    public Semantic getSemantic() {
        return semantic;
    }

    public abstract void create(PublicId referencedComponent);

    protected void save(PublicId referencedComponent, Consumer<MutableList<Object>> fieldConsumer){
        //Assign primordial UUID from PublicId
        UUID primordialUUID = semantic.asUuidArray()[0];

        //Assign nids for PublicIds
        int semanticNid = EntityService.get().nidForPublicId(semantic);
        int patternNid = EntityService.get().nidForPublicId(referencedPattern);
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
        fieldConsumer.accept(fields);
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

    public SemanticConstituent with(SemanticConstituent constituent) {
        constituent.create(this.semantic.publicId());
        System.out.println(constituent.semantic.description() + " references " + this.semantic.description());
        return this;
    }

}
