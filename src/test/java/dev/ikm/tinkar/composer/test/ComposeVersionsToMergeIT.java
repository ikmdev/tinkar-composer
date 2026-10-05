package dev.ikm.tinkar.composer.test;

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

import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.common.id.PublicIds;
import dev.ikm.tinkar.common.service.CachingService;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.composer.Composer;
import dev.ikm.tinkar.composer.Session;
import dev.ikm.tinkar.composer.assembler.ConceptAssembler;
import dev.ikm.tinkar.composer.template.FullyQualifiedName;
import dev.ikm.tinkar.composer.template.StatedAxiom;
import dev.ikm.tinkar.common.service.EntityCountSummary;
import dev.ikm.tinkar.entity.EntityHandle;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.entity.SemanticEntityVersion;
import dev.ikm.tinkar.entity.load.LoadEntitiesFromProtobufFile;
import dev.ikm.tinkar.terms.EntityProxy.Concept;
import dev.ikm.tinkar.terms.State;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.File;
import java.text.ParseException;
import java.util.UUID;
import java.util.function.Function;

import static dev.ikm.tinkar.terms.KernelTerm.*;
import static dev.ikm.tinkar.terms.TinkarTerm.ACCEPTABLE;
import static dev.ikm.tinkar.terms.TinkarTerm.DEVELOPMENT_MODULE;
import static dev.ikm.tinkar.terms.TinkarTerm.HEALTH_CONCEPT;
import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ComposeVersionsToMergeIT {
    public static final Function<String, File> createFilePathInTarget = (pathName) -> new File("%s/target/%s".formatted(System.getProperty("user.dir"), pathName));
    public static final File PB_STARTER_DATA = createFilePathInTarget.apply("data/tinkar-starter-data-20250915-reasoned-pb.zip");
    public static State DEFAULT_STATUS = State.ACTIVE;
    public static long DEFAULT_TIME = System.currentTimeMillis();
    public static Concept DEFAULT_AUTHOR = USER;
    public static Concept DEFAULT_MODULE = DEVELOPMENT_MODULE;
    public static Concept DEFAULT_PATH = DEVELOPMENT_PATH;
    /** One of {@code HEALTH_CONCEPT}'s UUIDs: the SNOMED CT root concept's. */
    private static final UUID SNOMED_ROOT_UUID = UUID.fromString("ee9ac5d2-a07c-3981-a57a-f7f26baf38d8");
    /** One of {@code FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE}'s UUIDs. */
    private static final UUID FULLY_QUALIFIED_NAME_UUID = UUID.fromString("00791270-77c9-32b6-b34f-d932569bd2bf");

    @BeforeAll
    public void beforeAll() {
        CachingService.clearAll();
        PrimitiveData.selectControllerByName("Load Ephemeral Store");
        PrimitiveData.start();
        EntityCountSummary entityCountSummary = new LoadEntitiesFromProtobufFile(PB_STARTER_DATA).compute();
    }

    @AfterAll
    public void afterAll() {
        PrimitiveData.stop();
    }

    // ### START: Append Version Tests Basic
    @Test
    public void appendVersionConceptTest() {
        PublicId conceptId = PublicIds.newRandom();
        Composer composer = new Composer("appendVersionConceptTest");

        // Create Initial Concept Version
        Session initSession = composer.open(DEFAULT_STATUS, DEFAULT_TIME, DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);
        initSession.compose((ConceptAssembler conceptAssembler) -> conceptAssembler.concept(Concept.make(conceptId)));
        composer.commitSession(initSession);

        // Append Concept Version
        Session appendSession = composer.open(DEFAULT_STATUS, System.currentTimeMillis(), DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);
        appendSession.compose((ConceptAssembler conceptAssembler) -> conceptAssembler.concept(Concept.make(conceptId)));
        composer.commitSession(appendSession);

        int expectedVersionCount = 2;
        int actualVersionCount = EntityHandle.get(conceptId.asUuidArray()).expectConcept().versions().size();
        assertEquals(expectedVersionCount, actualVersionCount,
                String.format("Expected %s versions after append, but there were %s versions instead.", expectedVersionCount, actualVersionCount));
    }

    // ### END: Append Version Tests Basic

    // ### START: Write / Commit Ordering Tests
    @Test
    public void writeOrderTest() {
        PublicId conceptId = PublicIds.newRandom();
        Composer composer = new Composer("writeOrderTest");

        // Create Initial Concept Version
        Session firstSession = composer.open(DEFAULT_STATUS, DEFAULT_TIME, DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);
        firstSession.compose((ConceptAssembler conceptAssembler) -> conceptAssembler.concept(Concept.make(conceptId)));

        // Append Concept Version
        Session secondSession = composer.open(DEFAULT_STATUS, System.currentTimeMillis(), DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);
        secondSession.compose((ConceptAssembler conceptAssembler) -> conceptAssembler.concept(Concept.make(conceptId)));

        // Commit Transactions out of order
        composer.commitSession(secondSession);
        composer.commitSession(firstSession);

        int expectedVersionCount = 2;
        int actualVersionCount = EntityHandle.get(conceptId.asUuidArray()).expectConcept().versions().size();
        assertEquals(expectedVersionCount, actualVersionCount,
                String.format("Expected %s versions after append, but there were %s versions instead.", expectedVersionCount, actualVersionCount));
    }

    @Test
    public void writeVersionsOutOfOrderTest() {
        PublicId conceptId = PublicIds.newRandom();
        Composer composer = new Composer("writeVersionsOutOfOrderTest");

        // Create Version with later time first
        Session laterSession = composer.open(DEFAULT_STATUS, System.currentTimeMillis(), DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);
        laterSession.compose((ConceptAssembler conceptAssembler) -> conceptAssembler.concept(Concept.make(conceptId)));
        composer.commitSession(laterSession);

        // Create Version with previous time next
        Session previousSession = composer.open(DEFAULT_STATUS, DEFAULT_TIME, DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);
        previousSession.compose((ConceptAssembler conceptAssembler) -> conceptAssembler.concept(Concept.make(conceptId)));
        composer.commitSession(previousSession);

        int expectedVersionCount = 2;
        int actualVersionCount = EntityHandle.get(conceptId.asUuidArray()).expectConcept().versions().size();
        assertEquals(expectedVersionCount, actualVersionCount,
                String.format("Expected %s versions after append, but there were %s versions instead.", expectedVersionCount, actualVersionCount));
    }

    @Test
    public void sessionDoubleCloseTest() {
        Concept referenceConcept = Concept.make(PublicIds.newRandom());
        Composer composer = new Composer("createFullyQualifiedNameTemplateTest");
        Session session = composer.open(DEFAULT_STATUS, DEFAULT_TIME, DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);

        session.compose(new FullyQualifiedName()
                .language(ENGLISH_LANGUAGE)
                .text("FQN from Template")
                .caseSignificance(DESCRIPTION_NOT_CASE_SENSITIVE), referenceConcept);

        composer.commitSession(session);
        composer.commitSession(session);
        int expectedComponentsUpdatedCount = 1;
        int actualComponentsUpdatedCount = session.componentsInSessionCount();
        assertEquals(expectedComponentsUpdatedCount, actualComponentsUpdatedCount,
                String.format("Expect %s updated components, but %s were updated instead.", expectedComponentsUpdatedCount, actualComponentsUpdatedCount));
    }
    // ### END: Write / Commit Ordering Tests

    // ### START: Miscellaneous Tests
    @Test
    public void writeConceptWithOneThenMultipleUuids() {
        UUID uuid1 = UUID.randomUUID();
        UUID uuid2 = UUID.randomUUID();
        Concept conceptWithSingleUuid = Concept.make(PublicIds.of(uuid1));
        Concept conceptWithMultipleUuids = Concept.make(PublicIds.of(uuid1, uuid2));
        Composer composer = new Composer("writeConceptWithOneThenMultipleUuids");

        // Create Concept with one Uuids
        Session previousSession = composer.open(DEFAULT_STATUS, DEFAULT_TIME, DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);
        previousSession.compose((ConceptAssembler conceptAssembler) -> conceptAssembler.concept(conceptWithSingleUuid)); // Concept with [ UUID1 ]
        composer.commitSession(previousSession);

        // Create Concept version for multiple Uuid
        Session laterSession = composer.open(DEFAULT_STATUS, System.currentTimeMillis(), DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);
        laterSession.compose((ConceptAssembler conceptAssembler) -> conceptAssembler.concept(conceptWithMultipleUuids)); // Concept with [ UUID1, UUID2 ]
        composer.commitSession(laterSession);

        int expectedVersionCount = 2;
        int actualVersionCount = EntityHandle.get(conceptWithMultipleUuids.asUuidArray()).expectConcept().versions().size();
        assertEquals(expectedVersionCount, actualVersionCount,
                String.format("Expected %s versions after append, but there were %s versions instead.", expectedVersionCount, actualVersionCount));
    }

    @Test
    public void writeConceptWithMultipleThenOneUuids() {
        UUID uuid1 = UUID.randomUUID();
        UUID uuid2 = UUID.randomUUID();
        Concept conceptWithSingleUuid = Concept.make(PublicIds.of(uuid1));
        Concept conceptWithMultipleUuids = Concept.make(PublicIds.of(uuid1, uuid2));
        Composer composer = new Composer("writeConceptWithMultipleThenOneUuids");

        // Create Concept with multiple Uuids
        Session previousSession = composer.open(DEFAULT_STATUS, DEFAULT_TIME, DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);
        previousSession.compose((ConceptAssembler conceptAssembler) -> conceptAssembler.concept(conceptWithMultipleUuids)); // Concept with [ UUID1 ]
        composer.commitSession(previousSession);

        // Create Concept version for one Uuid
        Session laterSession = composer.open(DEFAULT_STATUS, System.currentTimeMillis(), DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);
        laterSession.compose((ConceptAssembler conceptAssembler) -> conceptAssembler.concept(conceptWithSingleUuid)); // Concept with [ UUID1, UUID2 ]
        composer.commitSession(laterSession);

        int expectedVersionCount = 2;
        int actualVersionCount = EntityHandle.get(conceptWithMultipleUuids.asUuidArray()).expectConcept().versions().size();
        assertEquals(expectedVersionCount, actualVersionCount,
                String.format("Expected %s versions after append, but there were %s versions instead.", expectedVersionCount, actualVersionCount));
    }

    @Test
    public void writeConceptsWithMultipleUuidsWithOverlap() {
        UUID uuid1 = UUID.randomUUID();
        UUID uuid2 = UUID.randomUUID();
        UUID uuid3 = UUID.randomUUID();
        Concept conceptWithFirstUuids = Concept.make(PublicIds.of(uuid1, uuid2));
        Concept conceptWithLastUuids = Concept.make(PublicIds.of(uuid2, uuid3));
        Composer composer = new Composer("writeConceptWithMultipleThenOneUuids");

        // Create Concept with multiple Uuids
        Session previousSession = composer.open(DEFAULT_STATUS, DEFAULT_TIME, DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);
        previousSession.compose((ConceptAssembler conceptAssembler) -> conceptAssembler.concept(conceptWithFirstUuids)); // Concept with [ UUID1 ]
        composer.commitSession(previousSession);

        // Create Concept version for one Uuid
        Session laterSession = composer.open(DEFAULT_STATUS, System.currentTimeMillis(), DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);
        laterSession.compose((ConceptAssembler conceptAssembler) -> conceptAssembler.concept(conceptWithLastUuids)); // Concept with [ UUID1, UUID2 ]
        composer.commitSession(laterSession);

        int expectedVersionCount = 2;
        int actualVersionCount = EntityHandle.get(conceptWithLastUuids.asUuidArray()).expectConcept().versions().size();
        assertEquals(expectedVersionCount, actualVersionCount,
                String.format("Expected %s versions after append, but there were %s versions instead.", expectedVersionCount, actualVersionCount));
    }

    @Test
    public void writeHealthConceptRecords() {
        Composer composer = new Composer("writeHealthConceptRecords");

        // Create Concept with multiple UUIDs (i.e., Health Concept)
        Session healthConceptSession = composer.open(DEFAULT_STATUS, DEFAULT_TIME, DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);
        healthConceptSession.compose((ConceptAssembler conceptAssembler) -> conceptAssembler
                .concept(HEALTH_CONCEPT));
        composer.commitSession(healthConceptSession);

        // Create Concept version for a single UUID in the PublicId from the Concept above
        Session snomedRootConceptSession = composer.open(DEFAULT_STATUS, System.currentTimeMillis(), DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);
        snomedRootConceptSession.compose((ConceptAssembler conceptAssembler) -> conceptAssembler
                .concept(Concept.make(PublicIds.of(SNOMED_ROOT_UUID))));
        composer.commitSession(snomedRootConceptSession);

        int expectedVersionCount = 3; // First version is Pre-inception time from TinkarStarterData, next 2 versions are written above
        int actualVersionCount = EntityHandle.get(HEALTH_CONCEPT.nid()).expectConcept().versions().size();
        assertEquals(expectedVersionCount, actualVersionCount,
                String.format("Expected %s versions after append, but there were %s versions instead.", expectedVersionCount, actualVersionCount));
    }

    @Test
    public void writeFullyQualifiedNameConceptRecords() {
        Composer composer = new Composer("writeFullyQualifiedNameConceptRecords");

        // Create Concept with multiple UUIDs (i.e., FullyQualifiedName)
        Session healthConceptSession = composer.open(DEFAULT_STATUS, DEFAULT_TIME, DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);
        healthConceptSession.compose((ConceptAssembler conceptAssembler) -> conceptAssembler
                .concept(Concept.make(PublicIds.of(FULLY_QUALIFIED_NAME_UUID))));
        composer.commitSession(healthConceptSession);

        // Create Concept version for a single UUID in the PublicId from the Concept above
        Session snomedRootConceptSession = composer.open(DEFAULT_STATUS, System.currentTimeMillis(), DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);
        snomedRootConceptSession.compose((ConceptAssembler conceptAssembler) -> conceptAssembler
                .concept(FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE));
        composer.commitSession(snomedRootConceptSession);

        int expectedVersionCount = 3; // First version is Pre-inception time from TinkarStarterData, next 2 versions are written above
        int actualVersionCount = EntityHandle.get(HEALTH_CONCEPT.nid()).expectConcept().versions().size();
        assertEquals(expectedVersionCount, actualVersionCount,
                String.format("Expected %s versions after append, but there were %s versions instead.", expectedVersionCount, actualVersionCount));
    }

    @Test
    public void test() throws ParseException {
        Composer composer = new Composer("test");
        Session session = composer.open(DEFAULT_STATUS, System.currentTimeMillis(), DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);

        Concept conceptId = Concept.make(PublicIds.newRandom());
        session.compose((ConceptAssembler concept) -> concept
                .concept(conceptId)
                .attach(StatedAxiom.class, statedAxiom -> statedAxiom
                        .isA(EL_PLUS_PLUS_STATED_TERMINOLOGICAL_AXIOMS, EL_PLUS_PLUS_INFERRED_TERMINOLOGICAL_AXIOMS)));

        composer.commitAllSessions();

        EntityService.get().forEachSemanticForComponentOfPattern(conceptId.nid(), EL_PLUS_PLUS_STATED_AXIOMS_PATTERN.nid(),
                semanticEntity -> {
                    SemanticEntityVersion semanticEntityVersion = semanticEntity.versions().get(0);
                    semanticEntityVersion.active();
                });
    }

//    @Test
//    @Disabled
//    public void appendVersionTest() {
//        State status = State.ACTIVE;
//        long updated_time = System.currentTimeMillis();
//        Concept author = USER;
//        Concept module = DEVELOPMENT_MODULE;
//        Concept path = DEVELOPMENT_PATH;
//
//        PublicId fqnId = PublicIds.of(UUID.nameUUIDFromBytes("fqnId".getBytes()));
//        PublicId conceptId = PublicIds.of(UUID.nameUUIDFromBytes("conceptId".getBytes()));
//
//        Composer appendComposer = new Composer("testname");
//        Session appendSession = appendComposer.open(status, System.currentTimeMillis(), author, module, path);
//
//        appendSession.compose(Semantic.make("FQN Version Test", fqnId),
//                Concept.make("Concept Version Test", conceptId),
//                DESCRIPTION_PATTERN,
//                Lists.immutable.of(
//                        ENGLISH_LANGUAGE,
//                        "FQN1 Version Test",
//                        DESCRIPTION_NOT_CASE_SENSITIVE,
//                        FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE));
//
//        composer.commitSession(appendSession);
//
//        int expectedVersionCount = 2;
//        int actualVersionCount = EntityService.get().getEntityFast(fqnId.asUuidArray()).versions().size();
//
//        assertEquals(expectedVersionCount, actualVersionCount,
//                String.format("Expected %s versions after append, but there were %s versions instead.", expectedVersionCount, actualVersionCount));
//    }
//
//    @Test
//    @Disabled
//    public void appendVersionFromTemplateTest() {
//        State status = State.ACTIVE;
//        long updated_time = System.currentTimeMillis();
//        Concept author = USER;
//        Concept module = DEVELOPMENT_MODULE;
//        Concept path = DEVELOPMENT_PATH;
//
//        PublicId fqnId = PublicIds.of(UUID.nameUUIDFromBytes("fqnId".getBytes()));
//        PublicId conceptId = PublicIds.of(UUID.nameUUIDFromBytes("conceptId".getBytes()));
//
//        Composer appendComposer = new Composer("testname");
//        Session appendSession = appendComposer.open(status, System.currentTimeMillis(), author, module, path);
//
//        appendSession.compose(
//                new FullyQualifiedName(Semantic.make("FQN Version from Template Test", fqnId), ENGLISH_LANGUAGE, "FQN1 Version from Template Test", DESCRIPTION_NOT_CASE_SENSITIVE),
//                Concept.make("Concept Version from Template Test", conceptId))
//                    .with(new USEnglishDialect(Semantic.make("Dialect Version from Template Test", PublicIds.newRandom()), ACCEPTABLE));
//
//        composer.commitSession(appendSession);
//
//        int expectedVersionCount = 3;
//        int actualVersionCount = EntityService.get().getEntityFast(fqnId.asUuidArray()).versions().size();
//
//        assertEquals(expectedVersionCount, actualVersionCount,
//                String.format("Expected %s versions after append, but there were %s versions instead.", expectedVersionCount, actualVersionCount));
//    }
//
//
//    @Test
//    @Disabled
//    public void retireSemanticTest() {
//        State status = State.WITHDRAWN;
//        long updated_time = System.currentTimeMillis();
//        Concept author = USER;
//        Concept module = DEVELOPMENT_MODULE;
//        Concept path = DEVELOPMENT_PATH;
//
//        PublicId fqnId = PublicIds.of(UUID.nameUUIDFromBytes("fqnId".getBytes()));
//        PublicId conceptId = PublicIds.of(UUID.nameUUIDFromBytes("conceptId".getBytes()));
//
//        Composer appendComposer = new Composer("testname");
//        Session appendSession = appendComposer.open(status, System.currentTimeMillis(), author, module, path);
//
//        appendSession.compose(Semantic.make("FQN Retire Test", fqnId),
//                Concept.make("Concept", conceptId),
//                DESCRIPTION_PATTERN,
//                Lists.immutable.of(
//                        ENGLISH_LANGUAGE,
//                        "FQN1 Retire Test",
//                        DESCRIPTION_NOT_CASE_SENSITIVE,
//                        FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE));
//
//        composer.commitSession(appendSession);
//
//        int expectedVersionCount = 4;
//        int actualVersionCount = EntityService.get().getEntityFast(fqnId.asUuidArray()).versions().size();
//
//        assertEquals(expectedVersionCount, actualVersionCount,
//                String.format("Expected %s versions after append, but there were %s versions instead.", expectedVersionCount, actualVersionCount));
//    }
    // ### END: Miscellaneous Tests

}
