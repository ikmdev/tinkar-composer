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
import dev.ikm.tinkar.entity.EntityCountSummary;
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

import static dev.ikm.tinkar.terms.TinkarTerm.DESCRIPTION_NOT_CASE_SENSITIVE;
import static dev.ikm.tinkar.terms.TinkarTerm.DEVELOPMENT_MODULE;
import static dev.ikm.tinkar.terms.TinkarTerm.DEVELOPMENT_PATH;
import static dev.ikm.tinkar.terms.TinkarTerm.EL_PLUS_PLUS_INFERRED_TERMINOLOGICAL_AXIOMS;
import static dev.ikm.tinkar.terms.TinkarTerm.EL_PLUS_PLUS_STATED_AXIOMS_PATTERN;
import static dev.ikm.tinkar.terms.TinkarTerm.EL_PLUS_PLUS_STATED_TERMINOLOGICAL_AXIOMS;
import static dev.ikm.tinkar.terms.TinkarTerm.ENGLISH_LANGUAGE;
import static dev.ikm.tinkar.terms.TinkarTerm.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE;
import static dev.ikm.tinkar.terms.TinkarTerm.HEALTH_CONCEPT;
import static dev.ikm.tinkar.terms.TinkarTerm.USER;
import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ComposeVersionsToMergeIT {
    public static final Function<String, File> createFilePathInTarget = (pathName) -> new File("%s/target/%s".formatted(System.getProperty("user.dir"), pathName));
    public static final File PB_STARTER_DATA = createFilePathInTarget.apply("data/tinkar-starter-data-1.0.0-pb.zip");
    public static State DEFAULT_STATUS = State.ACTIVE;
    public static long DEFAULT_TIME = System.currentTimeMillis();
    public static Concept DEFAULT_AUTHOR = USER;
    public static Concept DEFAULT_MODULE = DEVELOPMENT_MODULE;
    public static Concept DEFAULT_PATH = DEVELOPMENT_PATH;

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
        int actualVersionCount = EntityService.get().getEntityFast(conceptId.asUuidArray()).versions().size();
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
        int actualVersionCount = EntityService.get().getEntityFast(conceptId.asUuidArray()).versions().size();
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
        int actualVersionCount = EntityService.get().getEntityFast(conceptId.asUuidArray()).versions().size();
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
        PublicId pubIdWithTwoUuids = PublicIds.of(UUID.randomUUID(), UUID.randomUUID());
        Concept conceptWithSingleUuid = Concept.make(PublicIds.of(pubIdWithTwoUuids.asUuidArray()[0]));
        Concept conceptWithMultipleUuids = Concept.make(pubIdWithTwoUuids);
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
        int actualVersionCount = EntityService.get().getEntityFast(conceptWithMultipleUuids.asUuidArray()).versions().size();
        assertEquals(expectedVersionCount, actualVersionCount,
                String.format("Expected %s versions after append, but there were %s versions instead.", expectedVersionCount, actualVersionCount));
    }

    @Test
    public void writeConceptWithMultipleThenOneUuids() {
        PublicId pubIdWithTwoUuids = PublicIds.of(UUID.randomUUID(), UUID.randomUUID());
        Concept conceptWithSingleUuid = Concept.make(PublicIds.of(pubIdWithTwoUuids.asUuidArray()[0]));
        Concept conceptWithMultipleUuids = Concept.make(pubIdWithTwoUuids);
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
        int actualVersionCount = EntityService.get().getEntityFast(conceptWithMultipleUuids.asUuidArray()).versions().size();
        assertEquals(expectedVersionCount, actualVersionCount,
                String.format("Expected %s versions after append, but there were %s versions instead.", expectedVersionCount, actualVersionCount));
    }

    @Test
    public void writeConceptsWithMultipleUuidsWithOverlap() {
        PublicId pubIdWithThreeUuids = PublicIds.of(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
        Concept conceptWithFirstUuids = Concept.make(PublicIds.of(pubIdWithThreeUuids.asUuidArray()[0], pubIdWithThreeUuids.asUuidArray()[1]));
        Concept conceptWithLastUuids = Concept.make(PublicIds.of(pubIdWithThreeUuids.asUuidArray()[1], pubIdWithThreeUuids.asUuidArray()[2]));
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
        int actualVersionCount = EntityService.get().getEntityFast(conceptWithLastUuids.asUuidArray()).versions().size();
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
                .concept(Concept.make(PublicIds.of(HEALTH_CONCEPT.asUuidArray()[0]))));
        composer.commitSession(snomedRootConceptSession);

        int expectedVersionCount = 3; // First version is Premundane time from TinkarStarterData, next 2 versions are written above
        int actualVersionCount = EntityService.get().getEntityFast(HEALTH_CONCEPT).versions().size();
        assertEquals(expectedVersionCount, actualVersionCount,
                String.format("Expected %s versions after append, but there were %s versions instead.", expectedVersionCount, actualVersionCount));
    }

    @Test
    public void writeFullyQualifiedNameConceptRecords() {
        Composer composer = new Composer("writeFullyQualifiedNameConceptRecords");

        // Create Concept with multiple UUIDs (i.e., FullyQualifiedName)
        Session healthConceptSession = composer.open(DEFAULT_STATUS, DEFAULT_TIME, DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);
        healthConceptSession.compose((ConceptAssembler conceptAssembler) -> conceptAssembler
                .concept(Concept.make(PublicIds.of(FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE.asUuidArray()[0]))));
        composer.commitSession(healthConceptSession);

        // Create Concept version for a single UUID in the PublicId from the Concept above
        Session snomedRootConceptSession = composer.open(DEFAULT_STATUS, System.currentTimeMillis(), DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);
        snomedRootConceptSession.compose((ConceptAssembler conceptAssembler) -> conceptAssembler
                .concept(FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE));
        composer.commitSession(snomedRootConceptSession);

        int expectedVersionCount = 3; // First version is Premundane time from TinkarStarterData, next 2 versions are written above
        int actualVersionCount = EntityService.get().getEntityFast(HEALTH_CONCEPT).versions().size();
        assertEquals(expectedVersionCount, actualVersionCount,
                String.format("Expected %s versions after append, but there were %s versions instead.", expectedVersionCount, actualVersionCount));
    }

//    @Test
//    @Disabled
//    public void writeSemanticWithMergableVersions() {
//        PublicId conceptId = PublicIds.newRandom();
//        PublicId initialSemanticId = PublicIds.newRandom();
//        PublicId semanticIdWithExtraUuid = PublicIds.of(initialSemanticId.asUuidArray()[0], PublicIds.newRandom().asUuidArray()[0]);
//        Concept referencedConcept = Concept.make(conceptId);
//        Composer composer = new Composer("writeSemanticWithMergableVersions");
//
//        // Create Initial Semantic Version from Template
//        Session initSession = composer.open(DEFAULT_STATUS, DEFAULT_TIME, DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);
//        initSession.compose(new Synonym(Semantic.make(initialSemanticId), ENGLISH_LANGUAGE,
//                        "Synonym Version with single UUID", DESCRIPTION_NOT_CASE_SENSITIVE),
//                referencedConcept);
//        composer.commitSession(initSession);
//
//        // Append Semantic Version from Template
//        Session appendSession = composer.open(DEFAULT_STATUS, System.currentTimeMillis(), DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);
//        appendSession.compose(new Synonym(Semantic.make(semanticIdWithExtraUuid), ENGLISH_LANGUAGE,
//                        "Synonym Version with multiple UUIDs", DESCRIPTION_NOT_CASE_SENSITIVE),
//                referencedConcept);
//        composer.commitSession(appendSession);
//
//        int expectedVersionCount = 2;
//        int actualVersionCount = EntityService.get().getEntityFast(semanticIdWithExtraUuid.asUuidArray()).versions().size();
//        assertEquals(expectedVersionCount, actualVersionCount,
//                String.format("Expected %s versions after append, but there were %s versions instead.", expectedVersionCount, actualVersionCount));
//    }



    @Test
    public void test() throws ParseException {
        Composer composer = new Composer("test");
        Session session = composer.open(DEFAULT_STATUS, System.currentTimeMillis(), DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);

        Concept conceptId = Concept.make(PublicIds.newRandom());
        session.compose((ConceptAssembler concept) -> concept
                .concept(conceptId)
                .attach((StatedAxiom statedAxiom) -> statedAxiom
                        .isA(EL_PLUS_PLUS_STATED_TERMINOLOGICAL_AXIOMS, EL_PLUS_PLUS_INFERRED_TERMINOLOGICAL_AXIOMS)));

        composer.commitAllSessions();

        EntityService.get().forEachSemanticForComponentOfPattern(conceptId.nid(), EL_PLUS_PLUS_STATED_AXIOMS_PATTERN.nid(),
                semanticEntity -> {
                    SemanticEntityVersion semanticEntityVersion = semanticEntity.versions().get(0);
                    semanticEntityVersion.active();
                });


//        Concept conceptId = EntityProxy.Concept.make(PublicIds.of(UuidUtil.fromSNOMED("2309482309")));
//        long epochTime2002 = new SimpleDateFormat("yyyyMMdd").parse("20020131").getTime();
//        Session session2002 = composer.open(DEFAULT_STATUS, epochTime2002, DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);
//        session2002.compose((ConceptAssembler concept) -> concept
//                .concept(conceptId));
//
//        long epochTime2017 = new SimpleDateFormat("yyyyMMdd").parse("20170731").getTime();
//        Session session2017 = composer.open(DEFAULT_STATUS, epochTime2017, DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);
//        session2017.compose((ConceptAssembler concept) -> concept
//                .concept(conceptId));
//
//        composer.cancelAllSessions();
////        composer.commitSession(session2002);
////        composer.commitAllSessions();
//
//        assertEquals(2, EntityService.get().getEntityFast(conceptId).versions().size(), "Versions should be 2");
//
//        Session session = composer.open(DEFAULT_STATUS, System.currentTimeMillis(), DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);
//        session.compose((ConceptAssembler concept) -> concept
//                .concept(ENGLISH_DIALECT_ASSEMBLAGE))
//                        .attach((FullyQualifiedName fqn) -> fqn
//                                .language(ENGLISH_LANGUAGE)
//                                .text("English Dialect")
//                                .caseSignificance(DESCRIPTION_NOT_CASE_SENSITIVE)
//                                .attach((USDialect usDialect) -> usDialect
//                                        .acceptability(PREFERRED)))
//                        .attach((Synonym synonym) -> synonym
//                                .language(ENGLISH_LANGUAGE)
//                                .text("English Dialect")
//                                .caseSignificance(DESCRIPTION_NOT_CASE_SENSITIVE)
//                                .attach((USDialect usDialect) -> usDialect
//                                        .acceptability(PREFERRED))))
//
//
//
//        starterData.concept(TinkarTerm.ENGLISH_DIALECT_ASSEMBLAGE)
//                .fullyQualifiedName("English Dialect", TinkarTerm.PREFERRED)
//                .synonym("English dialect", TinkarTerm.PREFERRED)
//                .definition("Specifies the dialect of the English language", TinkarTerm.PREFERRED)
//                .identifier(TinkarTerm.UNIVERSALLY_UNIQUE_IDENTIFIER, TinkarTerm.ENGLISH_DIALECT_ASSEMBLAGE.asUuidArray()[0].toString())
//                .statedNavigation(List.of(TinkarTerm.GB_ENGLISH_DIALECT, TinkarTerm.US_ENGLISH_DIALECT), List.of(TinkarTerm.DIALECT_ASSEMBLAGE))
//                .statedDefinition(List.of(TinkarTerm.DIALECT_ASSEMBLAGE))
//                .tinkarBaseModelMembership()
//                .build();

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
