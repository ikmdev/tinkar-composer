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
import dev.ikm.tinkar.composer.Composer;
import dev.ikm.tinkar.composer.Session;
import dev.ikm.tinkar.composer.assembler.ConceptAssembler;
import dev.ikm.tinkar.composer.assembler.PatternAssembler;
import dev.ikm.tinkar.composer.assembler.SemanticAssembler;
import dev.ikm.tinkar.composer.template.*;
import dev.ikm.tinkar.composer.test.template.CustomSemantic;
import dev.ikm.tinkar.entity.EntityCountSummary;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.entity.load.LoadEntitiesFromProtobufFile;
import dev.ikm.tinkar.terms.EntityProxy.Concept;
import dev.ikm.tinkar.terms.EntityProxy.Pattern;
import dev.ikm.tinkar.terms.EntityProxy.Semantic;
import dev.ikm.tinkar.terms.State;
import org.eclipse.collections.api.factory.Lists;
import org.junit.jupiter.api.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

import static dev.ikm.tinkar.terms.TinkarTerm.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ComposerCreateIT {
    public static final Function<String, File> createFilePathInTarget = (pathName) -> new File("%s/target/%s".formatted(System.getProperty("user.dir"), pathName));
    public static final File PB_STARTER_DATA = createFilePathInTarget.apply("data/tinkar-starter-data-1.0.0-pb.zip");
    public static State DEFAULT_STATUS = State.ACTIVE;
    public static long DEFAULT_TIME = System.currentTimeMillis();
    public static Concept DEFAULT_AUTHOR = USER;
    public static Concept DEFAULT_MODULE = DEVELOPMENT_MODULE;
    public static Concept DEFAULT_PATH = DEVELOPMENT_PATH;
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
        Composer composer = new Composer("createConceptTest");
        Session session = composer.open(DEFAULT_STATUS, DEFAULT_TIME, DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);

        session.compose((ConceptAssembler conceptAssembler) -> {});

        session.close();
        int expectedComponentsUpdatedCount = 1;
        int actualComponentsUpdatedCount = session.componentsInSessionCount();
        assertEquals(expectedComponentsUpdatedCount, actualComponentsUpdatedCount,
                String.format("Expect %s updated components, but %s were updated instead.", expectedComponentsUpdatedCount, actualComponentsUpdatedCount));
    }

    @Test
    public void createPatternTest() {
        Composer composer = new Composer("createPatternTest");
        Session session = composer.open(DEFAULT_STATUS, DEFAULT_TIME, DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);

        session.compose((PatternAssembler patternAssembler) -> patternAssembler
                        .meaning(MEANING)
                        .purpose(PURPOSE)
                        .fieldDefinition(ACTION_NAME, ACTION_PURPOSE, STRING));

        session.close();
        int expectedComponentsUpdatedCount = 1;
        int actualComponentsUpdatedCount = session.componentsInSessionCount();
        assertEquals(expectedComponentsUpdatedCount, actualComponentsUpdatedCount,
                String.format("Expect %s updated components, but %s were updated instead.", expectedComponentsUpdatedCount, actualComponentsUpdatedCount));
    }

    @Test
    public void createSemanticTest() {
        Concept referenceConcept = Concept.make(PublicIds.newRandom());
        Composer composer = new Composer("createSemanticTest");
        Session session = composer.open(DEFAULT_STATUS, DEFAULT_TIME, DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);

        session.compose((SemanticAssembler semanticAssembler) -> semanticAssembler
                        .reference(referenceConcept)
                        .pattern(DESCRIPTION_PATTERN)
                        .fieldValues(fieldValues -> fieldValues
                                .with(ENGLISH_LANGUAGE)
                                .with("Synonym")
                                .with(DESCRIPTION_NOT_CASE_SENSITIVE)
                                .with(REGULAR_NAME_DESCRIPTION_TYPE)));

        session.close();
        int expectedComponentsUpdatedCount = 1;
        int actualComponentsUpdatedCount = session.componentsInSessionCount();
        assertEquals(expectedComponentsUpdatedCount, actualComponentsUpdatedCount,
                String.format("Expect %s updated components, but %s were updated instead.", expectedComponentsUpdatedCount, actualComponentsUpdatedCount));
    }

    @Test
    public void createSemanticFromTemplateTest() {
        Concept referenceConcept = Concept.make(PublicIds.newRandom());
        Composer composer = new Composer("createSemanticFromTemplateTest");
        Session session = composer.open(DEFAULT_STATUS, DEFAULT_TIME, DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);

        session.compose(new Synonym()
                .language(ENGLISH_LANGUAGE)
                .text("Synonym from Template")
                .caseSignificance(DESCRIPTION_NOT_CASE_SENSITIVE), referenceConcept);

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
        Composer composer = new Composer("createConceptWithSemanticTest");
        Session session = composer.open(DEFAULT_STATUS, DEFAULT_TIME, DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);

        session.compose((ConceptAssembler conceptAssembler) -> conceptAssembler
                .attach((FullyQualifiedName fqn) -> fqn.semantic(Semantic.make(PublicIds.newRandom()))
                        .language(ENGLISH_LANGUAGE)
                        .text("FQN for Concept")
                        .caseSignificance(DESCRIPTION_NOT_CASE_SENSITIVE)));

        session.close();
        int expectedComponentsUpdatedCount = 2;
        int actualComponentsUpdatedCount = session.componentsInSessionCount();
        assertEquals(expectedComponentsUpdatedCount, actualComponentsUpdatedCount,
                String.format("Expect %s updated components, but %s were updated instead.", expectedComponentsUpdatedCount, actualComponentsUpdatedCount));
    }

    @Test
    public void createPatternWithSemanticTest() {
        Composer composer = new Composer("createPatternWithSemanticTest");
        Session session = composer.open(DEFAULT_STATUS, DEFAULT_TIME, DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);

        session.compose((PatternAssembler patternAssembler) -> patternAssembler
                .meaning(MEANING)
                .purpose(PURPOSE)
                .fieldDefinition(ACTION_NAME, ACTION_PURPOSE, STRING)
                .attach((FullyQualifiedName fqn) -> fqn.language(ENGLISH_LANGUAGE)
                        .text("FQN for Pattern")
                        .caseSignificance(DESCRIPTION_NOT_CASE_SENSITIVE)));

        session.close();
        int expectedComponentsUpdatedCount = 2;
        int actualComponentsUpdatedCount = session.componentsInSessionCount();
        assertEquals(expectedComponentsUpdatedCount, actualComponentsUpdatedCount,
                String.format("Expect %s updated components, but %s were updated instead.", expectedComponentsUpdatedCount, actualComponentsUpdatedCount));
    }

    @Test
    public void createSemanticWithSemanticTest() {
        Concept referenceConcept = Concept.make(PublicIds.newRandom());
        Composer composer = new Composer("createSemanticWithSemanticTest");
        Session session = composer.open(DEFAULT_STATUS, DEFAULT_TIME, DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);

        session.compose((SemanticAssembler semanticAssembler) -> semanticAssembler
                .reference(referenceConcept)
                .pattern(DESCRIPTION_PATTERN)
                .fieldValues(fieldValues -> fieldValues
                        .with(ENGLISH_LANGUAGE)
                        .with("Synonym with Dialect Semantic")
                        .with(DESCRIPTION_NOT_CASE_SENSITIVE)
                        .with(REGULAR_NAME_DESCRIPTION_TYPE))
                .attach((USDialect dialect) -> dialect
                        .semantic(Semantic.make("Dialect for Synonym", PublicIds.newRandom()))
                        .acceptability(PREFERRED)));

        session.close();
        int expectedComponentsUpdatedCount = 2;
        int actualComponentsUpdatedCount = session.componentsInSessionCount();
        assertEquals(expectedComponentsUpdatedCount, actualComponentsUpdatedCount,
                String.format("Expect %s updated components, but %s were updated instead.", expectedComponentsUpdatedCount, actualComponentsUpdatedCount));
    }

    @Test
    public void createThreeLayerTest() {
        Concept referenceConcept = Concept.make(PublicIds.newRandom());
        Composer composer = new Composer("createThreeLayerTest");
        Session session = composer.open(DEFAULT_STATUS, DEFAULT_TIME, DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);

        session.compose((SemanticAssembler semanticAssembler) -> semanticAssembler
                .reference(referenceConcept)
                .pattern(DESCRIPTION_PATTERN)
                .fieldValues(fieldValues -> fieldValues
                        .with(ENGLISH_LANGUAGE)
                        .with("Synonym with Dialect Semantic")
                        .with(DESCRIPTION_NOT_CASE_SENSITIVE)
                        .with(REGULAR_NAME_DESCRIPTION_TYPE))
                .attach((USDialect dialect) -> dialect
                        .semantic(Semantic.make("Dialect for Synonym", PublicIds.newRandom()))
                        .acceptability(PREFERRED)
                        .attach((Comment comment) -> comment
                                .semantic(Semantic.make("Comment for Synonym", PublicIds.newRandom()))
                                .text("Comment for Synonym"))));

        session.close();
        int expectedComponentsUpdatedCount = 3;
        int actualComponentsUpdatedCount = session.componentsInSessionCount();
        assertEquals(expectedComponentsUpdatedCount, actualComponentsUpdatedCount,
                String.format("Expect %s updated components, but %s were updated instead.", expectedComponentsUpdatedCount, actualComponentsUpdatedCount));
    }

    @Test
    public void createThreeLayerWithCustomTemplatesTest() {
        Concept referenceConcept = Concept.make(PublicIds.newRandom());
        Composer composer = new Composer("createThreeLayerWithCustomTemplatesTest");
        Session session = composer.open(DEFAULT_STATUS, DEFAULT_TIME, DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);

        session.compose((SemanticAssembler semanticAssembler) -> semanticAssembler
                .reference(referenceConcept)
                .pattern(DESCRIPTION_PATTERN)
                .fieldValues(fieldValues -> fieldValues
                        .with(ENGLISH_LANGUAGE)
                        .with("Synonym with Dialect Semantic")
                        .with(DESCRIPTION_NOT_CASE_SENSITIVE)
                        .with(REGULAR_NAME_DESCRIPTION_TYPE))
                .attach(new CustomSemantic()
                        .semantic(Semantic.make("Custom1 for Synonym", PublicIds.newRandom()))
                        .text("Custom1"))
                        .attach((Comment comment) -> comment
                                .semantic(Semantic.make("Comment for Custom1", PublicIds.newRandom()))
                                .text("Comment for Custom1")));

        session.close();
        int expectedComponentsUpdatedCount = 3;
        int actualComponentsUpdatedCount = session.componentsInSessionCount();
        assertEquals(expectedComponentsUpdatedCount, actualComponentsUpdatedCount,
                String.format("Expect %s updated components, but %s were updated instead.", expectedComponentsUpdatedCount, actualComponentsUpdatedCount));
    }

    @Test
    public void createSemanticFromTemplateWithSemanticTest() {
        Composer composer = new Composer("createSemanticFromTemplateWithSemanticTest");
        Session session = composer.open(DEFAULT_STATUS, DEFAULT_TIME, DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);

        session.compose(new Synonym()
                .language(ENGLISH_LANGUAGE)
                .text("Synonym from Template with Dialect Semantic")
                .caseSignificance(DESCRIPTION_NOT_CASE_SENSITIVE), Concept.make(PublicIds.newRandom()))
                .attach(new USDialect()
                        .semantic(Semantic.make("Dialect for Synonym from Template", PublicIds.newRandom()))
                        .acceptability(ACCEPTABLE));

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
        Composer Composer = new Composer("appendVersionConceptTest");

        // Create Initial Concept Version
        Session initSession = Composer.open(DEFAULT_STATUS, DEFAULT_TIME, DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);
        initSession.compose((ConceptAssembler conceptAssembler) -> conceptAssembler.concept(Concept.make(conceptId)));
        initSession.close();

        // Append Concept Version
        Session appendSession = Composer.open(DEFAULT_STATUS, System.currentTimeMillis(), DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);
        appendSession.compose((ConceptAssembler conceptAssembler) -> conceptAssembler.concept(Concept.make(conceptId)));
        appendSession.close();

        int expectedVersionCount = 2;
        int actualVersionCount = EntityService.get().getEntityFast(conceptId.asUuidArray()).versions().size();
        assertEquals(expectedVersionCount, actualVersionCount,
                String.format("Expected %s versions after append, but there were %s versions instead.", expectedVersionCount, actualVersionCount));
    }

    @Test
    public void appendVersionPatternTest() {
        PublicId patternId = PublicIds.newRandom();
        Composer Composer = new Composer("appendVersionPatternTest");

        // Create Initial Pattern Version
        Session initSession = Composer.open(DEFAULT_STATUS, DEFAULT_TIME, DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);
        initSession.compose((PatternAssembler patternAssembler) -> patternAssembler
                .pattern(Pattern.make(patternId))
                .meaning(MEANING)
                .purpose(PURPOSE)
                .fieldDefinition(MEANING, PURPOSE, STRING));
        initSession.close();

        // Append Pattern Version
        Session appendSession = Composer.open(DEFAULT_STATUS, System.currentTimeMillis(), DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);
        appendSession.compose((PatternAssembler patternAssembler) -> patternAssembler
                .pattern(Pattern.make(patternId))
                .meaning(ACTION_PROPERTIES)
                .purpose(ACTION_PROPERTIES)
                .fieldDefinition(ACTION_NAME, ACTION_PURPOSE, STRING));
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
        Composer Composer = new Composer("appendVersionSemanticTest");

        // Create Initial Semantic Version
        Session initSession = Composer.open(DEFAULT_STATUS, DEFAULT_TIME, DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);
        initSession.compose((SemanticAssembler semanticAssembler) -> semanticAssembler
                .semantic(Semantic.make(semanticId))
                .reference(referencedConcept)
                .pattern(DESCRIPTION_PATTERN)
                .fieldValues((fieldValues) -> fieldValues
                        .with(ENGLISH_LANGUAGE)
                        .with("Synonym V1")
                        .with(DESCRIPTION_NOT_CASE_SENSITIVE)
                        .with(REGULAR_NAME_DESCRIPTION_TYPE)));
        initSession.close();

        // Append Semantic Version
        Session appendSession = Composer.open(DEFAULT_STATUS, System.currentTimeMillis(), DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);
        appendSession.compose((SemanticAssembler semanticAssembler) -> semanticAssembler
                .semantic(Semantic.make(semanticId))
                .reference(referencedConcept)
                .pattern(DESCRIPTION_PATTERN)
                .fieldValues((fieldValues) -> fieldValues
                        .with(ENGLISH_LANGUAGE)
                        .with("Synonym V2")
                        .with(DESCRIPTION_NOT_CASE_SENSITIVE)
                        .with(REGULAR_NAME_DESCRIPTION_TYPE)));
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
        Composer Composer = new Composer("appendVersionSemanticFromTemplateTest");
        Session initSession = Composer.open(DEFAULT_STATUS, DEFAULT_TIME, DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);
        initSession.compose(new Synonym()
                .semantic(Semantic.make(semanticId))
                        .language(ENGLISH_LANGUAGE)
                .text("Synonym from Template V1")
                .caseSignificance(DESCRIPTION_NOT_CASE_SENSITIVE), referencedConcept);
        initSession.close();

        // Append Semantic Version from Template
        Session appendSession = Composer.open(DEFAULT_STATUS, System.currentTimeMillis(), DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);
        appendSession.compose(new Synonym()
                .semantic(Semantic.make(semanticId))
                .language(ENGLISH_LANGUAGE)
                .text("Synonym from Template V2")
                .caseSignificance(DESCRIPTION_NOT_CASE_SENSITIVE), referencedConcept);
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
        // Create Initial Semantic Version from Template
        Composer initComposer = new Composer("appendSemanticVersionFromWithTest");
        Session initSession = initComposer.open(DEFAULT_STATUS, DEFAULT_TIME, DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);
        initSession.compose((ConceptAssembler conceptAssembler) -> conceptAssembler
                .concept(Concept.make(conceptId))
                .attach(new Synonym().semantic(Semantic.make(semanticId))
                        .language(ENGLISH_LANGUAGE)
                        .text("Synonym from Template V1")
                        .caseSignificance(DESCRIPTION_NOT_CASE_SENSITIVE)));
        initSession.close();

        // Append Concept Version
        Composer appendComposer = new Composer("appendSemanticVersionFromWithTest");
        Session appendSession = appendComposer.open(DEFAULT_STATUS, System.currentTimeMillis(), DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);
        appendSession.compose((ConceptAssembler conceptAssembler) -> conceptAssembler
                .concept(Concept.make(conceptId))
                .attach(new Synonym().semantic(Semantic.make(semanticId))
                        .language(ENGLISH_LANGUAGE)
                        .text("Synonym from Template V2")
                        .caseSignificance(DESCRIPTION_NOT_CASE_SENSITIVE)));
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
        Composer Composer = new Composer("writeOrderTest");

        // Create Initial Concept Version
        Session firstSession = Composer.open(DEFAULT_STATUS, DEFAULT_TIME, DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);
        firstSession.compose((ConceptAssembler conceptAssembler) -> conceptAssembler.concept(Concept.make(conceptId)));

        // Append Concept Version
        Session secondSession = Composer.open(DEFAULT_STATUS, System.currentTimeMillis(), DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);
        secondSession.compose((ConceptAssembler conceptAssembler) -> conceptAssembler.concept(Concept.make(conceptId)));

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
        Composer Composer = new Composer("writeVersionsOutOfOrderTest");

        // Create Version with later time first
        Session laterSession = Composer.open(DEFAULT_STATUS, System.currentTimeMillis(), DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);
        laterSession.compose((ConceptAssembler conceptAssembler) -> conceptAssembler.concept(Concept.make(conceptId)));
        laterSession.close();

        // Create Version with previous time next
        Session previousSession = Composer.open(DEFAULT_STATUS, DEFAULT_TIME, DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);
        previousSession.compose((ConceptAssembler conceptAssembler) -> conceptAssembler.concept(Concept.make(conceptId)));
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
    public void writeConceptWithOneThenMultipleUuids() {
        PublicId pubIdWithTwoUuids = PublicIds.of("785d9b31-571b-495e-8ca4-b584146e3bef", "7bf1f629-5585-4c23-903a-27be0d362b28");
        Concept conceptWithSingleUuid = Concept.make(PublicIds.of(pubIdWithTwoUuids.asUuidArray()[0]));
        Concept conceptWithMultipleUuids = Concept.make(pubIdWithTwoUuids);
        Composer Composer = new Composer("writeConceptWithOneThenMultipleUuids");

        // Create Concept with one Uuids
        Session previousSession = Composer.open(DEFAULT_STATUS, DEFAULT_TIME, DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);
        previousSession.compose((ConceptAssembler conceptAssembler) -> conceptAssembler.concept(conceptWithSingleUuid)); // Concept with [ UUID1 ]
        previousSession.close();

        // Create Concept version for multiple Uuid
        Session laterSession = Composer.open(DEFAULT_STATUS, System.currentTimeMillis(), DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);
        laterSession.compose((ConceptAssembler conceptAssembler) -> conceptAssembler.concept(conceptWithMultipleUuids)); // Concept with [ UUID1, UUID2 ]
        laterSession.close();

        int expectedVersionCount = 2;
        int actualVersionCount = EntityService.get().getEntityFast(conceptWithMultipleUuids.asUuidArray()).versions().size();
        assertEquals(expectedVersionCount, actualVersionCount,
                String.format("Expected %s versions after append, but there were %s versions instead.", expectedVersionCount, actualVersionCount));
    }

    @Test
    public void writeConceptWithMultipleThenOneUuids() {
        PublicId pubIdWithTwoUuids = PublicIds.of("785d9b31-571b-495e-8ca4-b584146e3bef", "7bf1f629-5585-4c23-903a-27be0d362b28");
        Concept conceptWithSingleUuid = Concept.make(PublicIds.of(pubIdWithTwoUuids.asUuidArray()[0]));
        Concept conceptWithMultipleUuids = Concept.make(pubIdWithTwoUuids);
        Composer Composer = new Composer("writeConceptWithMultipleThenOneUuids");

        // Create Concept with multiple Uuids
        Session previousSession = Composer.open(DEFAULT_STATUS, DEFAULT_TIME, DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);
        previousSession.compose((ConceptAssembler conceptAssembler) -> conceptAssembler.concept(conceptWithMultipleUuids)); // Concept with [ UUID1 ]
        previousSession.close();

        // Create Concept version for one Uuid
        Session laterSession = Composer.open(DEFAULT_STATUS, System.currentTimeMillis(), DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);
        laterSession.compose((ConceptAssembler conceptAssembler) -> conceptAssembler.concept(conceptWithSingleUuid)); // Concept with [ UUID1, UUID2 ]
        laterSession.close();

        int expectedVersionCount = 2;
        int actualVersionCount = EntityService.get().getEntityFast(conceptWithMultipleUuids.asUuidArray()).versions().size();
        assertEquals(expectedVersionCount, actualVersionCount,
                String.format("Expected %s versions after append, but there were %s versions instead.", expectedVersionCount, actualVersionCount));
    }


    @Test
    @Disabled
    public void writeHealthConceptRecords() {
        Composer Composer = new Composer("writeHealthConceptRecords");

        // Create Concept with multiple UUIDs (i.e., Health Concept)
        Session healthConceptSession = Composer.open(DEFAULT_STATUS, DEFAULT_TIME, DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);
        healthConceptSession.compose((ConceptAssembler conceptAssembler) -> conceptAssembler
                .concept(HEALTH_CONCEPT));
        healthConceptSession.close();

        // Create Concept version for a single UUID in the PublicId from the Concept above
        Session snomedRootConceptSession = Composer.open(DEFAULT_STATUS, System.currentTimeMillis(), DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);
        snomedRootConceptSession.compose((ConceptAssembler conceptAssembler) -> conceptAssembler
                .concept(Concept.make(PublicIds.of(HEALTH_CONCEPT.asUuidArray()[0]))));
        snomedRootConceptSession.close();

        int expectedVersionCount = 3; // First version is Premundane time from TinkarStarterData, next 2 versions are written above
        int actualVersionCount = EntityService.get().getEntityFast(HEALTH_CONCEPT).versions().size();
        assertEquals(expectedVersionCount, actualVersionCount,
                String.format("Expected %s versions after append, but there were %s versions instead.", expectedVersionCount, actualVersionCount));
    }

    @Test
    @Disabled
    public void writeFullyQualifiedNameConceptRecords() {
        Composer Composer = new Composer("writeFullyQualifiedNameConceptRecords");

        // Create Concept with multiple UUIDs (i.e., FullyQualifiedName)
        Session healthConceptSession = Composer.open(DEFAULT_STATUS, DEFAULT_TIME, DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);
        healthConceptSession.compose((ConceptAssembler conceptAssembler) -> conceptAssembler
                .concept(Concept.make(PublicIds.of(FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE.asUuidArray()[0]))));
        healthConceptSession.close();

        // Create Concept version for a single UUID in the PublicId from the Concept above
        Session snomedRootConceptSession = Composer.open(DEFAULT_STATUS, System.currentTimeMillis(), DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);
        snomedRootConceptSession.compose((ConceptAssembler conceptAssembler) -> conceptAssembler
                .concept(FULLY_QUALIFIED_NAME_DESCRIPTION_TYPE));
        snomedRootConceptSession.close();

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
//        Composer Composer = new Composer("writeSemanticWithMergableVersions");
//
//        // Create Initial Semantic Version from Template
//        Session initSession = Composer.open(DEFAULT_STATUS, DEFAULT_TIME, DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);
//        initSession.compose(new Synonym(Semantic.make(initialSemanticId), ENGLISH_LANGUAGE,
//                        "Synonym Version with single UUID", DESCRIPTION_NOT_CASE_SENSITIVE),
//                referencedConcept);
//        initSession.close();
//
//        // Append Semantic Version from Template
//        Session appendSession = Composer.open(DEFAULT_STATUS, System.currentTimeMillis(), DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);
//        appendSession.compose(new Synonym(Semantic.make(semanticIdWithExtraUuid), ENGLISH_LANGUAGE,
//                        "Synonym Version with multiple UUIDs", DESCRIPTION_NOT_CASE_SENSITIVE),
//                referencedConcept);
//        appendSession.close();
//
//        int expectedVersionCount = 2;
//        int actualVersionCount = EntityService.get().getEntityFast(semanticIdWithExtraUuid.asUuidArray()).versions().size();
//        assertEquals(expectedVersionCount, actualVersionCount,
//                String.format("Expected %s versions after append, but there were %s versions instead.", expectedVersionCount, actualVersionCount));
//    }

    @Test
    public void complexCreateTest() {
        PublicId fqnId = PublicIds.of(UUID.nameUUIDFromBytes("fqnId".getBytes()));
        PublicId conceptId = PublicIds.of(UUID.nameUUIDFromBytes("conceptId".getBytes()));
        Composer composer = new Composer("complexCreateTest");

        Session session = composer.open(DEFAULT_STATUS, System.currentTimeMillis(), DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);

        session.compose((ConceptAssembler conceptAssembler) -> conceptAssembler
                .concept(Concept.make("Concept", conceptId)))
                .attach((FullyQualifiedName fqn) -> fqn
                        .semantic(Semantic.make("F1", PublicIds.newRandom()))
                        .language(ENGLISH_LANGUAGE)
                        .text("FQN1")
                        .caseSignificance(DESCRIPTION_NOT_CASE_SENSITIVE)
                        .attach((USDialect dialect) -> dialect
                                .semantic(Semantic.make("D1F1", PublicIds.newRandom()))
                                .acceptability(PREFERRED)
                                .attach((Comment comment) -> comment
                                        .semantic(Semantic.make("C1D1", PublicIds.newRandom()))
                                        .text("Comment on USEnglishDialect")))
                        .attach((Comment comment) -> comment
                                .semantic(Semantic.make("C1F1", PublicIds.newRandom()))
                                .text("Comment on FQN1")))
                .attach((Comment comment) -> comment
                        .semantic(Semantic.make("C1", PublicIds.newRandom()))
                        .text("Comment1 on Concept"))
                .attach((FullyQualifiedName fqn) -> fqn
                        .semantic(Semantic.make("F2", PublicIds.newRandom()))
                        .language(ENGLISH_LANGUAGE)
                        .text("FQN2")
                        .caseSignificance(DESCRIPTION_NOT_CASE_SENSITIVE))
                .attach((Comment comment) -> comment
                        .semantic(Semantic.make("C2", PublicIds.newRandom()))
                        .text("Comment2 on Concept"))
                .attach((Synonym synonym) -> synonym
                        .semantic(Semantic.make("S1", PublicIds.newRandom()))
                        .language(ENGLISH_LANGUAGE)
                        .text("Synonym")
                        .caseSignificance(DESCRIPTION_NOT_CASE_SENSITIVE)
                        .attach((Comment comment) -> comment
                                .semantic(Semantic.make("C1S1", PublicIds.newRandom()))
                                .text("Comment2 on Synonym"))
                        .attach((Comment comment) -> comment
                                .semantic(Semantic.make("C2S1", PublicIds.newRandom()))
                                .text("Comment2 on Synonym")));

        session.close();

        int expectedComponentsUpdatedCount = 11;
        int actualComponentsUpdatedCount = session.componentsInSessionCount();

        assertEquals(expectedComponentsUpdatedCount, actualComponentsUpdatedCount,
                String.format("Expect %s updated components, but %s were updated instead.", expectedComponentsUpdatedCount, actualComponentsUpdatedCount));
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
//        appendSession.close();
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
//        appendSession.close();
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
//        appendSession.close();
//
//        int expectedVersionCount = 4;
//        int actualVersionCount = EntityService.get().getEntityFast(fqnId.asUuidArray()).versions().size();
//
//        assertEquals(expectedVersionCount, actualVersionCount,
//                String.format("Expected %s versions after append, but there were %s versions instead.", expectedVersionCount, actualVersionCount));
//    }
    // ### END: Miscellaneous Tests

}
