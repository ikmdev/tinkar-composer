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
import dev.ikm.tinkar.common.service.ServiceKeys;
import dev.ikm.tinkar.common.service.ServiceProperties;
import dev.ikm.tinkar.composer.ComposerSession;
import dev.ikm.tinkar.composer.PatternFieldDetail;
import dev.ikm.tinkar.composer.template.Comment;
import dev.ikm.tinkar.composer.template.FullyQualifiedName;
import dev.ikm.tinkar.composer.template.Synonym;
import dev.ikm.tinkar.composer.template.USEnglishDialect;
import dev.ikm.tinkar.entity.EntityCountSummary;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.entity.load.LoadEntitiesFromProtobufFile;
import dev.ikm.tinkar.terms.EntityProxy;
import dev.ikm.tinkar.terms.EntityProxy.Concept;
import dev.ikm.tinkar.terms.EntityProxy.Pattern;
import dev.ikm.tinkar.terms.EntityProxy.Semantic;
import dev.ikm.tinkar.terms.State;
import dev.ikm.tinkar.terms.TinkarTerm;
import org.eclipse.collections.api.factory.Lists;
import org.junit.jupiter.api.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ComposerCreateIT {
    public static final Function<String,File> createFilePathInTarget = (pathName) -> new File("%s/target/%s".formatted(System.getProperty("user.dir"), pathName));
    public static final File PB_STARTER_DATA = createFilePathInTarget.apply("data/tinkar-starter-data-1.0.0-pb.zip");
    public static State DEFAULT_STATUS = State.ACTIVE;
    public static long DEFAULT_TIME = System.currentTimeMillis();
    public static Concept DEFAULT_AUTHOR = TinkarTerm.USER;
    public static Concept DEFAULT_MODULE = TinkarTerm.DEVELOPMENT_MODULE;
    public static Concept DEFAULT_PATH = TinkarTerm.DEVELOPMENT_PATH;
    public static final File DATASTORE = createFilePathInTarget.apply("generated-data/" + ComposerCreateIT.class.getSimpleName());


    private final Path datastore = Path.of(System.getProperty("user.dir"))
            .resolve("target")
            .resolve(ComposerCreateIT.class.getSimpleName())
            .resolve("datastore");

    @BeforeAll
    public void beforeAll() throws IOException {
//        Files.createDirectories(datastore);
        CachingService.clearAll();
        ServiceProperties.set(ServiceKeys.DATA_STORE_ROOT, datastore.toFile());
        PrimitiveData.selectControllerByName("Load Ephemeral Store");
//        ServiceProperties.set(ServiceKeys.DATA_STORE_ROOT, DATASTORE);
//        PrimitiveData.selectControllerByName("Open SpinedArrayStore");
        PrimitiveData.start();
        EntityCountSummary entityCountSummary = new LoadEntitiesFromProtobufFile(PB_STARTER_DATA).compute();
    }

    @AfterAll
    public void afterAll() {
        PrimitiveData.stop();
    }

    // ### START: Creation Tests Basic
    @Test
    public void createConceptTest() {
        ComposerSession session = new ComposerSession(DEFAULT_STATUS, DEFAULT_TIME, DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);

        session.composeConcept(Concept.make(PublicIds.newRandom()));

        session.close();
        int expectedComponentsUpdatedCount = 1;
        int actualComponentsUpdatedCount = session.componentsInSessionCount();
        assertEquals(expectedComponentsUpdatedCount, actualComponentsUpdatedCount,
                String.format("Expect %s updated components, but %s were updated instead.", expectedComponentsUpdatedCount, actualComponentsUpdatedCount));
    }

    @Test
    public void createPatternTest() {
        PatternFieldDetail testFieldDefinition = new PatternFieldDetail(TinkarTerm.ACTION_NAME, TinkarTerm.ACTION_PURPOSE, TinkarTerm.STRING, 0);
        ComposerSession session = new ComposerSession(DEFAULT_STATUS, DEFAULT_TIME, DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);

        session.composePattern(Pattern.make(PublicIds.newRandom()), TinkarTerm.MEANING, TinkarTerm.PURPOSE, List.of(testFieldDefinition));

        session.close();
        int expectedComponentsUpdatedCount = 1;
        int actualComponentsUpdatedCount = session.componentsInSessionCount();
        assertEquals(expectedComponentsUpdatedCount, actualComponentsUpdatedCount,
                String.format("Expect %s updated components, but %s were updated instead.", expectedComponentsUpdatedCount, actualComponentsUpdatedCount));
    }

    @Test
    public void createSemanticTest() {
        ComposerSession session = new ComposerSession(DEFAULT_STATUS, DEFAULT_TIME, DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);

        session.composeSemantic(EntityProxy.Semantic.make(PublicIds.newRandom()),
                Concept.make(PublicIds.newRandom()),
                TinkarTerm.DESCRIPTION_PATTERN,
                Lists.immutable.of(
                        TinkarTerm.ENGLISH_LANGUAGE,
                        "Synonym",
                        TinkarTerm.DESCRIPTION_NOT_CASE_SENSITIVE,
                        TinkarTerm.REGULAR_NAME_DESCRIPTION_TYPE));

        session.close();
        int expectedComponentsUpdatedCount = 1;
        int actualComponentsUpdatedCount = session.componentsInSessionCount();
        assertEquals(expectedComponentsUpdatedCount, actualComponentsUpdatedCount,
                String.format("Expect %s updated components, but %s were updated instead.", expectedComponentsUpdatedCount, actualComponentsUpdatedCount));
    }

    @Test
    public void createSemanticFromTemplateTest() {
        ComposerSession session = new ComposerSession(DEFAULT_STATUS, DEFAULT_TIME, DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);

        session.composeSemantic(new Synonym(EntityProxy.Semantic.make(PublicIds.newRandom()), TinkarTerm.ENGLISH_LANGUAGE,
                                "Synonym from Template", TinkarTerm.DESCRIPTION_NOT_CASE_SENSITIVE),
                        Concept.make(PublicIds.newRandom()));

        session.close();
        int expectedComponentsUpdatedCount = 1;
        int actualComponentsUpdatedCount = session.componentsInSessionCount();
        assertEquals(expectedComponentsUpdatedCount, actualComponentsUpdatedCount,
                String.format("Expect %s updated components, but %s were updated instead.", expectedComponentsUpdatedCount, actualComponentsUpdatedCount));
    }
    // ### END: Creation Tests Basic

    // ### START: Creation Tests with Semantics
    @Test
    public void createConceptWithSemanticTest() {
        ComposerSession session = new ComposerSession(DEFAULT_STATUS, DEFAULT_TIME, DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);

        session.composeConcept(Concept.make(PublicIds.newRandom()))
                .with(new FullyQualifiedName(EntityProxy.Semantic.make(PublicIds.newRandom()), TinkarTerm.ENGLISH_LANGUAGE, "FQN for Concept", TinkarTerm.DESCRIPTION_NOT_CASE_SENSITIVE));

        session.close();
        int expectedComponentsUpdatedCount = 2;
        int actualComponentsUpdatedCount = session.componentsInSessionCount();
        assertEquals(expectedComponentsUpdatedCount, actualComponentsUpdatedCount,
                String.format("Expect %s updated components, but %s were updated instead.", expectedComponentsUpdatedCount, actualComponentsUpdatedCount));
    }

    @Test
    public void createPatternWithSemanticTest() {
        PatternFieldDetail testFieldDefinition = new PatternFieldDetail(TinkarTerm.ACTION_NAME, TinkarTerm.ACTION_PURPOSE, TinkarTerm.STRING, 0);
        ComposerSession session = new ComposerSession(DEFAULT_STATUS, DEFAULT_TIME, DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);

        session.composePattern(Pattern.make(PublicIds.newRandom()), TinkarTerm.MEANING, TinkarTerm.PURPOSE, List.of(testFieldDefinition))
                .with(new FullyQualifiedName(EntityProxy.Semantic.make(PublicIds.newRandom()), TinkarTerm.ENGLISH_LANGUAGE, "FQN for Pattern", TinkarTerm.DESCRIPTION_NOT_CASE_SENSITIVE));

        session.close();
        int expectedComponentsUpdatedCount = 2;
        int actualComponentsUpdatedCount = session.componentsInSessionCount();
        assertEquals(expectedComponentsUpdatedCount, actualComponentsUpdatedCount,
                String.format("Expect %s updated components, but %s were updated instead.", expectedComponentsUpdatedCount, actualComponentsUpdatedCount));
    }

    @Test
    public void createSemanticWithSemanticTest() {
        ComposerSession session = new ComposerSession(DEFAULT_STATUS, DEFAULT_TIME, DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);

        session.composeSemantic(EntityProxy.Semantic.make(PublicIds.newRandom()),
                        Concept.make(PublicIds.newRandom()),
                        TinkarTerm.DESCRIPTION_PATTERN,
                        Lists.immutable.of(
                                TinkarTerm.ENGLISH_LANGUAGE,
                                "Synonym with Dialect Semantic",
                                TinkarTerm.DESCRIPTION_NOT_CASE_SENSITIVE,
                                TinkarTerm.REGULAR_NAME_DESCRIPTION_TYPE))
                .with(new USEnglishDialect(EntityProxy.Semantic.make("Dialect for Synonym", PublicIds.newRandom()), TinkarTerm.PREFERRED));

        session.close();
        int expectedComponentsUpdatedCount = 2;
        int actualComponentsUpdatedCount = session.componentsInSessionCount();
        assertEquals(expectedComponentsUpdatedCount, actualComponentsUpdatedCount,
                String.format("Expect %s updated components, but %s were updated instead.", expectedComponentsUpdatedCount, actualComponentsUpdatedCount));
    }

    @Test
    public void createSemanticFromTemplateWithSemanticTest() {
        ComposerSession session = new ComposerSession(DEFAULT_STATUS, DEFAULT_TIME, DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);

        session.composeSemantic(
                        new Synonym(EntityProxy.Semantic.make(PublicIds.newRandom()), TinkarTerm.ENGLISH_LANGUAGE,
                                "Synonym from Template with Dialect Semantic", TinkarTerm.DESCRIPTION_NOT_CASE_SENSITIVE),
                        Concept.make(PublicIds.newRandom()))
                .with(new USEnglishDialect(EntityProxy.Semantic.make("Dialect for Synonym from Template", PublicIds.newRandom()), TinkarTerm.ACCEPTABLE));

        session.close();
        int expectedComponentsUpdatedCount = 2;
        int actualComponentsUpdatedCount = session.componentsInSessionCount();
        assertEquals(expectedComponentsUpdatedCount, actualComponentsUpdatedCount,
                String.format("Expect %s updated components, but %s were updated instead.", expectedComponentsUpdatedCount, actualComponentsUpdatedCount));
    }
    // ### END: Creation Tests with Semantics

    // ### START: Append Version Tests Basic
    @Test
    public void appendVersionConceptTest() {
        PublicId conceptId = PublicIds.newRandom();
        // Create Initial Concept Version
        ComposerSession initSession = new ComposerSession(DEFAULT_STATUS, DEFAULT_TIME, DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);
        initSession.composeConcept(Concept.make(conceptId));
        initSession.close();

        // Append Concept Version
        ComposerSession appendSession = new ComposerSession(DEFAULT_STATUS, System.currentTimeMillis(), DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);
        appendSession.composeConcept(Concept.make(conceptId));
        appendSession.close();

        int expectedVersionCount = 2;
        int actualVersionCount = EntityService.get().getEntityFast(conceptId.asUuidArray()).versions().size();
        assertEquals(expectedVersionCount, actualVersionCount,
                String.format("Expected %s versions after append, but there were %s versions instead.", expectedVersionCount, actualVersionCount));
    }

    @Test
    public void appendVersionPatternTest() {
        PatternFieldDetail initFieldDefinition = new PatternFieldDetail(TinkarTerm.MEANING, TinkarTerm.PURPOSE, TinkarTerm.STRING, 0);
        PatternFieldDetail appendFieldDefinition = new PatternFieldDetail(TinkarTerm.ACTION_NAME, TinkarTerm.ACTION_PURPOSE, TinkarTerm.STRING, 0);
        PublicId patternId = PublicIds.newRandom();
        // Create Initial Pattern Version
        ComposerSession initSession = new ComposerSession(DEFAULT_STATUS, DEFAULT_TIME, DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);
        initSession.composePattern(Pattern.make(patternId), TinkarTerm.MEANING, TinkarTerm.PURPOSE, List.of(initFieldDefinition));
        initSession.close();

        // Append Pattern Version
        ComposerSession appendSession = new ComposerSession(DEFAULT_STATUS, System.currentTimeMillis(), DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);
        appendSession.composePattern(Pattern.make(patternId), TinkarTerm.ACTION_PROPERTIES, TinkarTerm.ACTION_PROPERTIES, List.of(appendFieldDefinition));
        appendSession.close();

        int expectedVersionCount = 2;
        int actualVersionCount = EntityService.get().getEntityFast(patternId.asUuidArray()).versions().size();
        assertEquals(expectedVersionCount, actualVersionCount,
                String.format("Expected %s versions after append, but there were %s versions instead.", expectedVersionCount, actualVersionCount));
    }

    @Test
    public void appendVersionSemanticTest() {
        PublicId semanticId = PublicIds.newRandom();
        PublicId conceptId = PublicIds.newRandom();
        Concept referencedConcept = Concept.make(conceptId);
        // Create Initial Semantic Version
        ComposerSession initSession = new ComposerSession(DEFAULT_STATUS, DEFAULT_TIME, DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);
        initSession.composeSemantic(Semantic.make(semanticId),
                referencedConcept,
                TinkarTerm.DESCRIPTION_PATTERN,
                Lists.immutable.of(
                        TinkarTerm.ENGLISH_LANGUAGE,
                        "Synonym V1",
                        TinkarTerm.DESCRIPTION_NOT_CASE_SENSITIVE,
                        TinkarTerm.REGULAR_NAME_DESCRIPTION_TYPE));
        initSession.close();

        // Append Semantic Version
        ComposerSession appendSession = new ComposerSession(DEFAULT_STATUS, System.currentTimeMillis(), DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);
        appendSession.composeSemantic(Semantic.make(semanticId),
                referencedConcept,
                TinkarTerm.DESCRIPTION_PATTERN,
                Lists.immutable.of(
                        TinkarTerm.ENGLISH_LANGUAGE,
                        "Synonym V2",
                        TinkarTerm.DESCRIPTION_NOT_CASE_SENSITIVE,
                        TinkarTerm.REGULAR_NAME_DESCRIPTION_TYPE));
        appendSession.close();

        int expectedVersionCount = 2;
        int actualVersionCount = EntityService.get().getEntityFast(semanticId.asUuidArray()).versions().size();
        assertEquals(expectedVersionCount, actualVersionCount,
                String.format("Expected %s versions after append, but there were %s versions instead.", expectedVersionCount, actualVersionCount));
    }

    @Test
    public void appendVersionSemanticFromTemplateTest() {
        PublicId semanticId = PublicIds.newRandom();
        PublicId conceptId = PublicIds.newRandom();
        Concept referencedConcept = Concept.make(conceptId);
        // Create Initial Semantic Version from Template
        ComposerSession initSession = new ComposerSession(DEFAULT_STATUS, DEFAULT_TIME, DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);
        initSession.composeSemantic(new Synonym(Semantic.make(semanticId), TinkarTerm.ENGLISH_LANGUAGE,
                        "Synonym from Template V1", TinkarTerm.DESCRIPTION_NOT_CASE_SENSITIVE),
                referencedConcept);
        initSession.close();

        // Append Semantic Version from Template
        ComposerSession appendSession = new ComposerSession(DEFAULT_STATUS, System.currentTimeMillis(), DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);
        appendSession.composeSemantic(new Synonym(Semantic.make(semanticId), TinkarTerm.ENGLISH_LANGUAGE,
                        "Synonym from Template V2", TinkarTerm.DESCRIPTION_NOT_CASE_SENSITIVE),
                referencedConcept);
        appendSession.close();

        int expectedVersionCount = 2;
        int actualVersionCount = EntityService.get().getEntityFast(semanticId.asUuidArray()).versions().size();
        assertEquals(expectedVersionCount, actualVersionCount,
                String.format("Expected %s versions after append, but there were %s versions instead.", expectedVersionCount, actualVersionCount));
    }

    @Test
    public void appendSemanticVersionFromWithTest() {
        PublicId semanticId = PublicIds.newRandom();
        PublicId conceptId = PublicIds.newRandom();
        Concept referencedConcept = Concept.make(conceptId);
        // Create Initial Semantic Version from Template
        ComposerSession initSession = new ComposerSession(DEFAULT_STATUS, DEFAULT_TIME, DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);
        initSession.composeSemantic(new Synonym(Semantic.make(semanticId), TinkarTerm.ENGLISH_LANGUAGE,
                        "Synonym from Template V1", TinkarTerm.DESCRIPTION_NOT_CASE_SENSITIVE),
                referencedConcept);
        initSession.close();

        // Append Concept Version
        ComposerSession appendSession = new ComposerSession(DEFAULT_STATUS, System.currentTimeMillis(), DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);
        appendSession.composeConcept(referencedConcept)
                .with(new Synonym(Semantic.make(semanticId), TinkarTerm.ENGLISH_LANGUAGE,
                        "Synonym from Template V2", TinkarTerm.DESCRIPTION_NOT_CASE_SENSITIVE));
        appendSession.close();

        int expectedVersionCount = 2;
        int actualVersionCount = EntityService.get().getEntityFast(semanticId.asUuidArray()).versions().size();
        assertEquals(expectedVersionCount, actualVersionCount,
                String.format("Expected %s versions after append, but there were %s versions instead.", expectedVersionCount, actualVersionCount));
    }
    // ### END: Append Version Tests Basic

    // ### START: Write / Commit Ordering Tests
    @Test
    public void writeOrderTest() {
        PublicId conceptId = PublicIds.newRandom();
        // Create Initial Concept Version
        ComposerSession firstSession = new ComposerSession(DEFAULT_STATUS, DEFAULT_TIME, DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);
        firstSession.composeConcept(Concept.make(conceptId));

        // Append Concept Version
        ComposerSession secondSession = new ComposerSession(DEFAULT_STATUS, System.currentTimeMillis(), DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);
        secondSession.composeConcept(Concept.make(conceptId));

        // Commit Transactions out of order
        secondSession.close();
        firstSession.close();

        int expectedVersionCount = 2;
        int actualVersionCount = EntityService.get().getEntityFast(conceptId.asUuidArray()).versions().size();
        assertEquals(expectedVersionCount, actualVersionCount,
                String.format("Expected %s versions after append, but there were %s versions instead.", expectedVersionCount, actualVersionCount));
    }

    @Test
    public void writeVersionsOutOfOrderTest() {
        PublicId conceptId = PublicIds.newRandom();
        // Create Version with later time first
        ComposerSession laterSession = new ComposerSession(DEFAULT_STATUS, System.currentTimeMillis(), DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);
        laterSession.composeConcept(Concept.make(conceptId));
        laterSession.close();

        // Create Version with previous time next
        ComposerSession previousSession = new ComposerSession(DEFAULT_STATUS, DEFAULT_TIME, DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);
        previousSession.composeConcept(Concept.make(conceptId));
        previousSession.close();

        int expectedVersionCount = 2;
        int actualVersionCount = EntityService.get().getEntityFast(conceptId.asUuidArray()).versions().size();
        assertEquals(expectedVersionCount, actualVersionCount,
                String.format("Expected %s versions after append, but there were %s versions instead.", expectedVersionCount, actualVersionCount));
    }
    // ### END: Write / Commit Ordering Tests

    // ### START: Miscellaneous Tests
    @Test
    @Disabled
    public void writeConceptWithMultipleUuids() {
        PublicId pubIdWithTwoUuids = PublicIds.of("785d9b31-571b-495e-8ca4-b584146e3bef", "7bf1f629-5585-4c23-903a-27be0d362b28");
        Concept conceptWithMultipleUuids = Concept.make(pubIdWithTwoUuids);
        Concept conceptWithSingleUuid = Concept.make(PublicIds.of(pubIdWithTwoUuids.asUuidArray()[0]));

        // Create Concept with multiple Uuids
        ComposerSession previousSession = new ComposerSession(DEFAULT_STATUS, DEFAULT_TIME, DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);
        previousSession.composeConcept(conceptWithSingleUuid); // Concept with [ UUID1 ]
        previousSession.close();

        // Create Concept version for one Uuid
        ComposerSession laterSession = new ComposerSession(DEFAULT_STATUS, System.currentTimeMillis(), DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);
        laterSession.composeConcept(conceptWithMultipleUuids); // Concept with [ UUID1, UUID2 ]
        laterSession.close();

        int expectedVersionCount = 2;
        int actualVersionCount = EntityService.get().getEntityFast(conceptWithMultipleUuids.asUuidArray()).versions().size();
        assertEquals(expectedVersionCount, actualVersionCount,
                String.format("Expected %s versions after append, but there were %s versions instead.", expectedVersionCount, actualVersionCount));
    }


    @Test
    @Disabled
    public void writeHealthConceptRecords() {
        // Create Concept with multiple UUIDs (i.e., Health Concept)
        ComposerSession healthConceptSession = new ComposerSession(DEFAULT_STATUS, DEFAULT_TIME, DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);
        healthConceptSession.composeConcept(TinkarTerm.HEALTH_CONCEPT);
        healthConceptSession.close();

        // Create Concept version for a single UUID in the PublicId from the Concept above
        ComposerSession snomedRootConceptSession = new ComposerSession(DEFAULT_STATUS, System.currentTimeMillis(), DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);
        snomedRootConceptSession.composeConcept(Concept.make(PublicIds.of(TinkarTerm.HEALTH_CONCEPT.asUuidArray()[0])));
        snomedRootConceptSession.close();

        int expectedVersionCount = 3; // First version is Premundane time from TinkarStarterData, next 2 versions are written above
        int actualVersionCount = EntityService.get().getEntityFast(TinkarTerm.HEALTH_CONCEPT).versions().size();
        assertEquals(expectedVersionCount, actualVersionCount,
                String.format("Expected %s versions after append, but there were %s versions instead.", expectedVersionCount, actualVersionCount));
    }

    @Test
    @Disabled
    public void writeFullyQualifiedNameConceptRecords() {
        // Create Concept with multiple UUIDs (i.e., FullyQualifiedName)
        ComposerSession healthConceptSession = new ComposerSession(DEFAULT_STATUS, DEFAULT_TIME, DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);
        healthConceptSession.composeConcept(Concept.make(PublicIds.of(TinkarTerm.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE.asUuidArray()[0])));
        healthConceptSession.close();

        // Create Concept version for a single UUID in the PublicId from the Concept above
        ComposerSession snomedRootConceptSession = new ComposerSession(DEFAULT_STATUS, System.currentTimeMillis(), DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);
        snomedRootConceptSession.composeConcept(TinkarTerm.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE);
        snomedRootConceptSession.close();

        int expectedVersionCount = 3; // First version is Premundane time from TinkarStarterData, next 2 versions are written above
        int actualVersionCount = EntityService.get().getEntityFast(TinkarTerm.HEALTH_CONCEPT).versions().size();
        assertEquals(expectedVersionCount, actualVersionCount,
                String.format("Expected %s versions after append, but there were %s versions instead.", expectedVersionCount, actualVersionCount));
    }

    @Test
    @Disabled
    public void writeSemanticWithMergableVersions() {
        PublicId conceptId = PublicIds.newRandom();
        PublicId initialSemanticId = PublicIds.newRandom();
        Concept referencedConcept = Concept.make(conceptId);
        // Create Initial Semantic Version from Template
        ComposerSession initSession = new ComposerSession(DEFAULT_STATUS, DEFAULT_TIME, DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);
        initSession.composeSemantic(new Synonym(Semantic.make(initialSemanticId), TinkarTerm.ENGLISH_LANGUAGE,
                        "Synonym Version with single UUID", TinkarTerm.DESCRIPTION_NOT_CASE_SENSITIVE),
                referencedConcept);
        initSession.close();

        PublicId semanticIdWithExtraUuid = PublicIds.of(initialSemanticId.asUuidArray()[0], PublicIds.newRandom().asUuidArray()[0]);
        // Append Semantic Version from Template
        ComposerSession appendSession = new ComposerSession(DEFAULT_STATUS, System.currentTimeMillis(), DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);
        appendSession.composeSemantic(new Synonym(Semantic.make(semanticIdWithExtraUuid), TinkarTerm.ENGLISH_LANGUAGE,
                        "Synonym Version with multiple UUIDs", TinkarTerm.DESCRIPTION_NOT_CASE_SENSITIVE),
                referencedConcept);
        appendSession.close();

        int expectedVersionCount = 2;
        int actualVersionCount = EntityService.get().getEntityFast(semanticIdWithExtraUuid.asUuidArray()).versions().size();
        assertEquals(expectedVersionCount, actualVersionCount,
                String.format("Expected %s versions after append, but there were %s versions instead.", expectedVersionCount, actualVersionCount));
    }

    @Test
    @Disabled
    public void createSessionTest() {
        long time = System.currentTimeMillis();

        PublicId fqnId = PublicIds.of(UUID.nameUUIDFromBytes("fqnId".getBytes()));
        PublicId conceptId = PublicIds.of(UUID.nameUUIDFromBytes("conceptId".getBytes()));

        ComposerSession session = new ComposerSession(DEFAULT_STATUS, time, DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);

        session.composeConcept(Concept.make("Concept", conceptId))
                .with(new FullyQualifiedName(EntityProxy.Semantic.make("F1", fqnId), TinkarTerm.ENGLISH_LANGUAGE, "FQN1", TinkarTerm.DESCRIPTION_NOT_CASE_SENSITIVE)
                        .with(new USEnglishDialect(EntityProxy.Semantic.make("D1F1", PublicIds.newRandom()), TinkarTerm.PREFERRED)
                                .with(new Comment(EntityProxy.Semantic.make("C1D1", PublicIds.newRandom()), "Comment on USEnglishDialect")))
                        .with(new Comment(EntityProxy.Semantic.make("C1F1", PublicIds.newRandom()), "Comment on FQN1")))
                .with(new Comment(EntityProxy.Semantic.make("C1", PublicIds.newRandom()), "Comment1 on Concept"))
                .with(
                        new FullyQualifiedName(EntityProxy.Semantic.make("F2", PublicIds.newRandom()), TinkarTerm.ENGLISH_LANGUAGE, "FQN2", TinkarTerm.DESCRIPTION_NOT_CASE_SENSITIVE),
                        new Comment(EntityProxy.Semantic.make("C2", PublicIds.newRandom()), "Comment2 on Concept"))
                .with(new Synonym(EntityProxy.Semantic.make("S1", PublicIds.newRandom()), TinkarTerm.ENGLISH_LANGUAGE, "Synonym", TinkarTerm.DESCRIPTION_NOT_CASE_SENSITIVE)
                        .with(
                                new Comment(EntityProxy.Semantic.make("C1S1", PublicIds.newRandom()), "Comment1 on Synonym"),
                                new Comment(EntityProxy.Semantic.make("C2S1", PublicIds.newRandom()), "Comment2 on Synonym")));

        session.close();

        int expectedComponentsUpdatedCount = 11;
        int actualComponentsUpdatedCount = session.componentsInSessionCount();

        assertEquals(expectedComponentsUpdatedCount, actualComponentsUpdatedCount,
                String.format("Expect %s updated components, but %s were updated instead.", expectedComponentsUpdatedCount, actualComponentsUpdatedCount));
    }

    @Test
    @Disabled
    public void appendVersionTest() {
        State status = State.ACTIVE;
        long updated_time = System.currentTimeMillis();
        Concept author = TinkarTerm.USER;
        Concept module = TinkarTerm.DEVELOPMENT_MODULE;
        Concept path = TinkarTerm.DEVELOPMENT_PATH;

        PublicId fqnId = PublicIds.of(UUID.nameUUIDFromBytes("fqnId".getBytes()));
        PublicId conceptId = PublicIds.of(UUID.nameUUIDFromBytes("conceptId".getBytes()));

        ComposerSession appendSession = new ComposerSession(status, System.currentTimeMillis(), author, module, path);

        appendSession.composeSemantic(EntityProxy.Semantic.make("FQN Version Test", fqnId),
                Concept.make("Concept Version Test", conceptId),
                TinkarTerm.DESCRIPTION_PATTERN,
                Lists.immutable.of(
                        TinkarTerm.ENGLISH_LANGUAGE,
                        "FQN1 Version Test",
                        TinkarTerm.DESCRIPTION_NOT_CASE_SENSITIVE,
                        TinkarTerm.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE));

        appendSession.close();

        int expectedVersionCount = 2;
        int actualVersionCount = EntityService.get().getEntityFast(fqnId.asUuidArray()).versions().size();

        assertEquals(expectedVersionCount, actualVersionCount,
                String.format("Expected %s versions after append, but there were %s versions instead.", expectedVersionCount, actualVersionCount));
    }

    @Test
    @Disabled
    public void appendVersionFromTemplateTest() {
        State status = State.ACTIVE;
        long updated_time = System.currentTimeMillis();
        Concept author = TinkarTerm.USER;
        Concept module = TinkarTerm.DEVELOPMENT_MODULE;
        Concept path = TinkarTerm.DEVELOPMENT_PATH;

        PublicId fqnId = PublicIds.of(UUID.nameUUIDFromBytes("fqnId".getBytes()));
        PublicId conceptId = PublicIds.of(UUID.nameUUIDFromBytes("conceptId".getBytes()));

        ComposerSession appendSession = new ComposerSession(status, System.currentTimeMillis(), author, module, path);

        appendSession.composeSemantic(
                new FullyQualifiedName(EntityProxy.Semantic.make("FQN Version from Template Test", fqnId), TinkarTerm.ENGLISH_LANGUAGE, "FQN1 Version from Template Test", TinkarTerm.DESCRIPTION_NOT_CASE_SENSITIVE),
                Concept.make("Concept Version from Template Test", conceptId))
                    .with(new USEnglishDialect(EntityProxy.Semantic.make("Dialect Version from Template Test", PublicIds.newRandom()), TinkarTerm.ACCEPTABLE));

        appendSession.close();

        int expectedVersionCount = 3;
        int actualVersionCount = EntityService.get().getEntityFast(fqnId.asUuidArray()).versions().size();

        assertEquals(expectedVersionCount, actualVersionCount,
                String.format("Expected %s versions after append, but there were %s versions instead.", expectedVersionCount, actualVersionCount));
    }


    @Test
    @Disabled
    public void retireSemanticTest() {
        State status = State.WITHDRAWN;
        long updated_time = System.currentTimeMillis();
        Concept author = TinkarTerm.USER;
        Concept module = TinkarTerm.DEVELOPMENT_MODULE;
        Concept path = TinkarTerm.DEVELOPMENT_PATH;

        PublicId fqnId = PublicIds.of(UUID.nameUUIDFromBytes("fqnId".getBytes()));
        PublicId conceptId = PublicIds.of(UUID.nameUUIDFromBytes("conceptId".getBytes()));

        ComposerSession appendSession = new ComposerSession(status, System.currentTimeMillis(), author, module, path);

        appendSession.composeSemantic(EntityProxy.Semantic.make("FQN Retire Test", fqnId),
                Concept.make("Concept", conceptId),
                TinkarTerm.DESCRIPTION_PATTERN,
                Lists.immutable.of(
                        TinkarTerm.ENGLISH_LANGUAGE,
                        "FQN1 Retire Test",
                        TinkarTerm.DESCRIPTION_NOT_CASE_SENSITIVE,
                        TinkarTerm.FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE));

        appendSession.close();

        int expectedVersionCount = 4;
        int actualVersionCount = EntityService.get().getEntityFast(fqnId.asUuidArray()).versions().size();

        assertEquals(expectedVersionCount, actualVersionCount,
                String.format("Expected %s versions after append, but there were %s versions instead.", expectedVersionCount, actualVersionCount));
    }
    // ### END: Miscellaneous Tests

}
