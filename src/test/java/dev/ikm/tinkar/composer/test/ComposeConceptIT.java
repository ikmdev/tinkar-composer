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
import dev.ikm.tinkar.composer.template.AxiomSyntax;
import dev.ikm.tinkar.composer.template.Comment;
import dev.ikm.tinkar.composer.template.Definition;
import dev.ikm.tinkar.composer.template.FullyQualifiedName;
import dev.ikm.tinkar.composer.template.GBDialect;
import dev.ikm.tinkar.composer.template.Identifier;
import dev.ikm.tinkar.composer.template.KometBaseModel;
import dev.ikm.tinkar.composer.template.StatedAxiom;
import dev.ikm.tinkar.composer.template.StatedNavigation;
import dev.ikm.tinkar.composer.template.Synonym;
import dev.ikm.tinkar.composer.template.TinkarBaseModel;
import dev.ikm.tinkar.composer.template.USDialect;
import dev.ikm.tinkar.composer.test.template.CustomSemantic;
import dev.ikm.tinkar.entity.EntityCountSummary;
import dev.ikm.tinkar.entity.load.LoadEntitiesFromProtobufFile;
import dev.ikm.tinkar.terms.EntityProxy.Concept;
import dev.ikm.tinkar.terms.EntityProxy.Semantic;
import dev.ikm.tinkar.terms.State;
import dev.ikm.tinkar.terms.TinkarTerm;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.File;
import java.util.UUID;
import java.util.function.Function;

import static dev.ikm.tinkar.terms.TinkarTerm.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ComposeConceptIT {
    public static final Function<String, File> createFilePathInTarget = (pathName) -> new File("%s/target/%s".formatted(System.getProperty("user.dir"), pathName));
    public static final File PB_STARTER_DATA = createFilePathInTarget.apply("data/tinkar-starter-data-20250915-reasoned-pb.zip");
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

    // ### START: Creation Tests Basic
    @Test
    public void createConceptWithPublicIdTest() {
        Composer composer = new Composer("createConceptWithRandomPublicIdTest");
        Session session = composer.open(DEFAULT_STATUS, DEFAULT_TIME, DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);

        Concept conceptProxy = Concept.make(PublicIds.newRandom());
        session.compose((ConceptAssembler conceptAssembler) -> conceptAssembler
                .concept(conceptProxy));

        composer.commitSession(session);
        int expectedComponentsUpdatedCount = 1;
        int actualComponentsUpdatedCount = session.componentsInSessionCount();
        assertEquals(expectedComponentsUpdatedCount, actualComponentsUpdatedCount,
                String.format("Expect %s updated components, but %s were updated instead.", expectedComponentsUpdatedCount, actualComponentsUpdatedCount));
    }

    @Test
    public void createConceptWithoutPublicIdTest() {
        Composer composer = new Composer("createConceptWithoutPublicIdTest");
        Session session = composer.open(DEFAULT_STATUS, DEFAULT_TIME, DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);

        session.compose((ConceptAssembler conceptAssembler) -> {});

        composer.commitSession(session);
        int expectedComponentsUpdatedCount = 1;
        int actualComponentsUpdatedCount = session.componentsInSessionCount();
        assertEquals(expectedComponentsUpdatedCount, actualComponentsUpdatedCount,
                String.format("Expect %s updated components, but %s were updated instead.", expectedComponentsUpdatedCount, actualComponentsUpdatedCount));
    }
    // ### END: Creation Tests Basic

    // ### START: Creation Tests with Semantics
    @Test
    public void createConceptWithFqnTest() {
        Composer composer = new Composer("createConceptWithFqnTest");
        Session session = composer.open(DEFAULT_STATUS, DEFAULT_TIME, DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);

        session.compose((ConceptAssembler conceptAssembler) -> conceptAssembler
                .attach((FullyQualifiedName fqn) -> fqn
                        .language(ENGLISH_LANGUAGE)
                        .text("FQN for Concept")
                        .caseSignificance(DESCRIPTION_NOT_CASE_SENSITIVE)));

        composer.commitSession(session);
        int expectedComponentsUpdatedCount = 2;
        int actualComponentsUpdatedCount = session.componentsInSessionCount();
        assertEquals(expectedComponentsUpdatedCount, actualComponentsUpdatedCount,
                String.format("Expect %s updated components, but %s were updated instead.", expectedComponentsUpdatedCount, actualComponentsUpdatedCount));
    }

    @Test
    public void createConceptWithSynonymTest() {
        Composer composer = new Composer("createConceptWithSynonymTest");
        Session session = composer.open(DEFAULT_STATUS, DEFAULT_TIME, DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);

        session.compose((ConceptAssembler conceptAssembler) -> conceptAssembler
                .attach((Synonym synonym) -> synonym
                        .language(ENGLISH_LANGUAGE)
                        .text("Synonym for Concept")
                        .caseSignificance(DESCRIPTION_NOT_CASE_SENSITIVE)));

        composer.commitSession(session);
        int expectedComponentsUpdatedCount = 2;
        int actualComponentsUpdatedCount = session.componentsInSessionCount();
        assertEquals(expectedComponentsUpdatedCount, actualComponentsUpdatedCount,
                String.format("Expect %s updated components, but %s were updated instead.", expectedComponentsUpdatedCount, actualComponentsUpdatedCount));
    }

    @Test
    public void createConceptWithDefinitionTest() {
        Composer composer = new Composer("createConceptWithDefinitionTest");
        Session session = composer.open(DEFAULT_STATUS, DEFAULT_TIME, DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);

        session.compose((ConceptAssembler conceptAssembler) -> conceptAssembler
                .attach((Definition def) -> def
                        .language(ENGLISH_LANGUAGE)
                        .text("Definition for Concept")
                        .caseSignificance(DESCRIPTION_NOT_CASE_SENSITIVE)));

        composer.commitSession(session);
        int expectedComponentsUpdatedCount = 2;
        int actualComponentsUpdatedCount = session.componentsInSessionCount();
        assertEquals(expectedComponentsUpdatedCount, actualComponentsUpdatedCount,
                String.format("Expect %s updated components, but %s were updated instead.", expectedComponentsUpdatedCount, actualComponentsUpdatedCount));
    }

    @Test
    public void createConceptWithAxiomSyntaxTest() {
        Composer composer = new Composer("createConceptWithAxiomSyntaxTest");
        Session session = composer.open(DEFAULT_STATUS, DEFAULT_TIME, DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);

        session.compose((ConceptAssembler conceptAssembler) -> conceptAssembler
                .attach((AxiomSyntax axiomSyntax) -> axiomSyntax
                        .text("AxiomSyntax for Concept")));

        composer.commitSession(session);
        int expectedComponentsUpdatedCount = 2;
        int actualComponentsUpdatedCount = session.componentsInSessionCount();
        assertEquals(expectedComponentsUpdatedCount, actualComponentsUpdatedCount,
                String.format("Expect %s updated components, but %s were updated instead.", expectedComponentsUpdatedCount, actualComponentsUpdatedCount));
    }

    @Test
    public void createConceptWithCommentTest() {
        Composer composer = new Composer("createConceptWithCommentTest");
        Session session = composer.open(DEFAULT_STATUS, DEFAULT_TIME, DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);

        session.compose((ConceptAssembler conceptAssembler) -> conceptAssembler
                .attach((Comment comment) -> comment
                        .text("Comment for Concept")));

        composer.commitSession(session);
        int expectedComponentsUpdatedCount = 2;
        int actualComponentsUpdatedCount = session.componentsInSessionCount();
        assertEquals(expectedComponentsUpdatedCount, actualComponentsUpdatedCount,
                String.format("Expect %s updated components, but %s were updated instead.", expectedComponentsUpdatedCount, actualComponentsUpdatedCount));
    }

    @Test
    public void createConceptWithGbDialectTest() {
        Composer composer = new Composer("createConceptWithGbDialectTest");
        Session session = composer.open(DEFAULT_STATUS, DEFAULT_TIME, DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);

        session.compose((ConceptAssembler conceptAssembler) -> conceptAssembler
                .attach((GBDialect gbDialect) -> gbDialect
                        .acceptability(PREFERRED)));

        composer.commitSession(session);
        int expectedComponentsUpdatedCount = 2;
        int actualComponentsUpdatedCount = session.componentsInSessionCount();
        assertEquals(expectedComponentsUpdatedCount, actualComponentsUpdatedCount,
                String.format("Expect %s updated components, but %s were updated instead.", expectedComponentsUpdatedCount, actualComponentsUpdatedCount));
    }

    @Test
    public void createConceptWithUsDialectTest() {
        Composer composer = new Composer("createConceptWithUsDialectTest");
        Session session = composer.open(DEFAULT_STATUS, DEFAULT_TIME, DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);

        session.compose((ConceptAssembler conceptAssembler) -> conceptAssembler
                .attach((USDialect usDialect) -> usDialect
                        .acceptability(PREFERRED)));

        composer.commitSession(session);
        int expectedComponentsUpdatedCount = 2;
        int actualComponentsUpdatedCount = session.componentsInSessionCount();
        assertEquals(expectedComponentsUpdatedCount, actualComponentsUpdatedCount,
                String.format("Expect %s updated components, but %s were updated instead.", expectedComponentsUpdatedCount, actualComponentsUpdatedCount));
    }

    @Test
    public void createConceptWithIdentifierTest() {
        Composer composer = new Composer("createConceptWithIdentifierTest");
        Session session = composer.open(DEFAULT_STATUS, DEFAULT_TIME, DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);

        Concept conceptProxy = Concept.make(PublicIds.newRandom());
        session.compose((ConceptAssembler conceptAssembler) -> conceptAssembler
                .attach((Identifier identifier) -> identifier
                        .source(TinkarTerm.UNIVERSALLY_UNIQUE_IDENTIFIER)
                        .identifier(conceptProxy.publicId().asUuidArray()[0].toString())));

        composer.commitSession(session);
        int expectedComponentsUpdatedCount = 2;
        int actualComponentsUpdatedCount = session.componentsInSessionCount();
        assertEquals(expectedComponentsUpdatedCount, actualComponentsUpdatedCount,
                String.format("Expect %s updated components, but %s were updated instead.", expectedComponentsUpdatedCount, actualComponentsUpdatedCount));
    }

    @Test
    public void createConceptWithKometBaseModelTest() {
        Composer composer = new Composer("createConceptWithKometBaseModelTest");
        Session session = composer.open(DEFAULT_STATUS, DEFAULT_TIME, DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);

        session.compose((ConceptAssembler conceptAssembler) -> conceptAssembler
                .attach((KometBaseModel kometBaseModelMembership) -> {}));

        composer.commitSession(session);
        int expectedComponentsUpdatedCount = 2;
        int actualComponentsUpdatedCount = session.componentsInSessionCount();
        assertEquals(expectedComponentsUpdatedCount, actualComponentsUpdatedCount,
                String.format("Expect %s updated components, but %s were updated instead.", expectedComponentsUpdatedCount, actualComponentsUpdatedCount));
    }

    @Test
    public void createConceptWithTinkarBaseModelTest() {
        Composer composer = new Composer("createConceptWithTinkarBaseModelTest");
        Session session = composer.open(DEFAULT_STATUS, DEFAULT_TIME, DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);

        session.compose((ConceptAssembler conceptAssembler) -> conceptAssembler
                .attach((TinkarBaseModel tinkarBaseModelMembership) -> {}));

        composer.commitSession(session);
        int expectedComponentsUpdatedCount = 2;
        int actualComponentsUpdatedCount = session.componentsInSessionCount();
        assertEquals(expectedComponentsUpdatedCount, actualComponentsUpdatedCount,
                String.format("Expect %s updated components, but %s were updated instead.", expectedComponentsUpdatedCount, actualComponentsUpdatedCount));
    }

    @Test
    public void createConceptWithStatedAxiomTest() {
        Composer composer = new Composer("createConceptWithStatedAxiomTest");
        Session session = composer.open(DEFAULT_STATUS, DEFAULT_TIME, DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);

        session.compose((ConceptAssembler conceptAssembler) -> conceptAssembler
                .attach((StatedAxiom statedAxiom) -> statedAxiom
                        .isA(EL_PLUS_PLUS_STATED_TERMINOLOGICAL_AXIOMS, EL_PLUS_PLUS_INFERRED_TERMINOLOGICAL_AXIOMS)));

        composer.commitSession(session);
        int expectedComponentsUpdatedCount = 2;
        int actualComponentsUpdatedCount = session.componentsInSessionCount();
        assertEquals(expectedComponentsUpdatedCount, actualComponentsUpdatedCount,
                String.format("Expect %s updated components, but %s were updated instead.", expectedComponentsUpdatedCount, actualComponentsUpdatedCount));
    }

    @Test
    public void createConceptWithStatedNavigationTest() {
        Composer composer = new Composer("createConceptWithStatedNavigationTest");
        Session session = composer.open(DEFAULT_STATUS, DEFAULT_TIME, DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);

        session.compose((ConceptAssembler conceptAssembler) -> conceptAssembler
                .attach((StatedNavigation statedNav) -> statedNav
                        .parents(ROOT_VERTEX)
                        .children(MEANING)));

        composer.commitSession(session);
        int expectedComponentsUpdatedCount = 2;
        int actualComponentsUpdatedCount = session.componentsInSessionCount();
        assertEquals(expectedComponentsUpdatedCount, actualComponentsUpdatedCount,
                String.format("Expect %s updated components, but %s were updated instead.", expectedComponentsUpdatedCount, actualComponentsUpdatedCount));
    }

    @Test
    public void createConceptWithCustomSemanticTest() {
        Composer composer = new Composer("createConceptWithCustomSemanticTest");
        Session session = composer.open(DEFAULT_STATUS, DEFAULT_TIME, DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);

        session.compose((ConceptAssembler conceptAssembler) -> conceptAssembler
                .attach(new CustomSemantic()
                        .text("Custom Semantic for Concept")));

        composer.commitSession(session);
        int expectedComponentsUpdatedCount = 2;
        int actualComponentsUpdatedCount = session.componentsInSessionCount();
        assertEquals(expectedComponentsUpdatedCount, actualComponentsUpdatedCount,
                String.format("Expect %s updated components, but %s were updated instead.", expectedComponentsUpdatedCount, actualComponentsUpdatedCount));
    }
    // ### END: Creation Tests with Semantics

    @Test
    public void complexConceptCreateTest() {
        PublicId fqnId = PublicIds.of(UUID.nameUUIDFromBytes("fqnId".getBytes()));
        PublicId conceptId = PublicIds.of(UUID.nameUUIDFromBytes("conceptId".getBytes()));
        Composer composer = new Composer("complexConceptCreateTest");

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

        composer.commitSession(session);

        int expectedComponentsUpdatedCount = 11;
        int actualComponentsUpdatedCount = session.componentsInSessionCount();

        assertEquals(expectedComponentsUpdatedCount, actualComponentsUpdatedCount,
                String.format("Expect %s updated components, but %s were updated instead.", expectedComponentsUpdatedCount, actualComponentsUpdatedCount));
    }
}
