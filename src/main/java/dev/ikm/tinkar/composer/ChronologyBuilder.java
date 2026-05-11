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

import dev.ikm.tinkar.composer.domain.PatternDef;
import dev.ikm.tinkar.schema.*;
import dev.ikm.tinkar.terms.EntityProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;

public class ChronologyBuilder {

	private static final Logger LOG = LoggerFactory.getLogger(ChronologyBuilder.class);

	public static TinkarMsg buildConceptChronologyMsg(dev.ikm.tinkar.common.id.PublicId id, StampChronology stampChronology) {
		PublicId conceptPublicId = createPublicId(id);
		ConceptChronology.Builder conceptChronologyBuilder = ConceptChronology.newBuilder().setPublicId(conceptPublicId);
		ConceptVersion conceptVersion = ConceptVersion.newBuilder()
				.setStampChronologyPublicId(stampChronology.getPublicId())
				.build();
		ConceptChronology conceptChronology = conceptChronologyBuilder.addConceptVersions(conceptVersion).build();
		return TinkarMsg.newBuilder().setConceptChronology(conceptChronology).build();
	}

	public static TinkarMsg buildPatternChronologyMsg(dev.ikm.tinkar.common.id.PublicId ids, StampChronology stampChronology,
	                                                dev.ikm.tinkar.common.id.PublicId meaningIds, dev.ikm.tinkar.common.id.PublicId purposeIds,
	                                                List<PatternDef> patternDefs) {
		PublicId patternPublicId = createPublicId(ids);
		PublicId meaningPublicId = createPublicId(meaningIds);
		PublicId purposePublicId = createPublicId(purposeIds);
		PatternChronology.Builder patternChronologyBuilder = PatternChronology.newBuilder().setPublicId(patternPublicId);
		PatternVersion.Builder patternVersionBuilder = PatternVersion.newBuilder()
				.setStampChronologyPublicId(stampChronology.getPublicId())
				.setReferencedComponentMeaningPublicId(meaningPublicId)
				.setReferencedComponentPurposePublicId(purposePublicId);
		patternDefs.forEach(patternDef -> {
			FieldDefinition fieldDefinition = FieldDefinition.newBuilder()
					.setMeaningPublicId(createPublicId(patternDef.meaning()))
					.setDataTypePublicId(createPublicId(patternDef.datatype()))
					.setPurposePublicId(createPublicId(patternDef.purpose()))
					.setIndex(patternDef.index())
					.build();
			patternVersionBuilder.addFieldDefinitions(fieldDefinition);
		});
		PatternVersion patternVersion = patternVersionBuilder.build();
		PatternChronology patternChronology = patternChronologyBuilder.addPatternVersions(patternVersion).build();
		return TinkarMsg.newBuilder().setPatternChronology(patternChronology).build();
	}

	public static TinkarMsg buildSemanticChronologyMsg(dev.ikm.tinkar.common.id.PublicId ids, StampChronology stampChronology,
	                                                  dev.ikm.tinkar.common.id.PublicId referenceIds, dev.ikm.tinkar.common.id.PublicId patternIds,
	                                                  List<Field> fields) {
		PublicId semanticPublicId = createPublicId(ids);
		PublicId referencePublicId = createPublicId(referenceIds);
		PublicId patternPublicId = createPublicId(patternIds);
		SemanticChronology.Builder semanticChronologyBuilder = SemanticChronology.newBuilder()
				.setPublicId(semanticPublicId)
				.setReferencedComponentPublicId(referencePublicId)
				.setPatternForSemanticPublicId(patternPublicId);
		SemanticVersion.Builder semanticVersionBuilder = SemanticVersion.newBuilder()
				.setStampChronologyPublicId(stampChronology.getPublicId())
				.addAllFields(fields);
		SemanticVersion semanticVersion = semanticVersionBuilder.build();
		SemanticChronology semanticChronology = semanticChronologyBuilder.addSemanticVersions(semanticVersion).build();
		return TinkarMsg.newBuilder().setSemanticChronology(semanticChronology).build();
	}

	public static TinkarMsg buildStampChronologyMsg(dev.ikm.tinkar.common.id.PublicId id, dev.ikm.tinkar.common.id.PublicId stateIds, long time, dev.ikm.tinkar.common.id.PublicId authorIds,
	                                            dev.ikm.tinkar.common.id.PublicId moduleIds,
	                                            dev.ikm.tinkar.common.id.PublicId pathIds) {
		StampChronology.Builder stampBuilder = StampChronology.newBuilder().setPublicId(createPublicId(id));
		StampVersion stampVersion = StampVersion.newBuilder()
				.setStatusPublicId(createPublicId(stateIds))
				.setTime(time)
				.setAuthorPublicId(createPublicId(authorIds))
				.setModulePublicId(createPublicId(moduleIds))
				.setPathPublicId(createPublicId(pathIds))
				.build();
		StampChronology stampChronology = StampChronology.newBuilder().setFirstStampVersion(stampVersion).build();
		return TinkarMsg.newBuilder().setStampChronology(stampChronology).build();
	}

	public static PublicId createPublicId(EntityProxy entityProxy) {
		return createPublicId(entityProxy.publicId());
	}

	public static PublicId createPublicId(dev.ikm.tinkar.common.id.PublicId publicId) {
		return PublicId.newBuilder()
				.addAllUuids(publicId.asUuidList().stream()
						.map(UUID::toString)
						.toList())
				.build();
	}

}
