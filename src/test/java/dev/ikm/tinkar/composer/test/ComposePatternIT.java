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
import dev.ikm.tinkar.composer.assembler.PatternAssembler;
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
import dev.ikm.tinkar.terms.EntityProxy.Pattern;
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

import static dev.ikm.tinkar.terms.TinkarTerm.DESCRIPTION_NOT_CASE_SENSITIVE;
import static dev.ikm.tinkar.terms.TinkarTerm.DEVELOPMENT_MODULE;
import static dev.ikm.tinkar.terms.TinkarTerm.DEVELOPMENT_PATH;
import static dev.ikm.tinkar.terms.TinkarTerm.EL_PLUS_PLUS_INFERRED_TERMINOLOGICAL_AXIOMS;
import static dev.ikm.tinkar.terms.TinkarTerm.EL_PLUS_PLUS_STATED_TERMINOLOGICAL_AXIOMS;
import static dev.ikm.tinkar.terms.TinkarTerm.ENGLISH_LANGUAGE;
import static dev.ikm.tinkar.terms.TinkarTerm.MEANING;
import static dev.ikm.tinkar.terms.TinkarTerm.PREFERRED;
import static dev.ikm.tinkar.terms.TinkarTerm.PURPOSE;
import static dev.ikm.tinkar.terms.TinkarTerm.ROOT_VERTEX;
import static dev.ikm.tinkar.terms.TinkarTerm.STRING;
import static dev.ikm.tinkar.terms.TinkarTerm.USER;
import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ComposePatternIT {
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

    // ### START: Creation Tests Basic
    @Test
    public void createPatternWithPublicIdTest() {
        Composer composer = new Composer("createPatternWithRandomPublicIdTest");
        Session session = composer.open(DEFAULT_STATUS, DEFAULT_TIME, DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);

        Pattern patternProxy = Pattern.make(PublicIds.newRandom());
        session.compose((PatternAssembler patternAssembler) -> patternAssembler
                .pattern(patternProxy)
                .meaning(MEANING)
                .purpose(PURPOSE)
                .fieldDefinition(MEANING, PURPOSE, STRING));

        composer.commitSession(session);
        int expectedComponentsUpdatedCount = 1;
        int actualComponentsUpdatedCount = session.componentsInSessionCount();
        assertEquals(expectedComponentsUpdatedCount, actualComponentsUpdatedCount,
                String.format("Expect %s updated components, but %s were updated instead.", expectedComponentsUpdatedCount, actualComponentsUpdatedCount));
    }

    @Test
    public void createPatternWithoutPublicIdTest() {
        Composer composer = new Composer("createPatternWithoutPublicIdTest");
        Session session = composer.open(DEFAULT_STATUS, DEFAULT_TIME, DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);

        session.compose((PatternAssembler patternAssembler) -> patternAssembler
                .meaning(MEANING)
                .purpose(PURPOSE)
                .fieldDefinition(MEANING, PURPOSE, STRING));

        composer.commitSession(session);
        int expectedComponentsUpdatedCount = 1;
        int actualComponentsUpdatedCount = session.componentsInSessionCount();
        assertEquals(expectedComponentsUpdatedCount, actualComponentsUpdatedCount,
                String.format("Expect %s updated components, but %s were updated instead.", expectedComponentsUpdatedCount, actualComponentsUpdatedCount));
    }
    // ### END: Creation Tests Basic

    // ### START: Creation Tests with Semantics
    @Test
    public void createPatternWithFqnTest() {
        Composer composer = new Composer("createPatternWithFqnTest");
        Session session = composer.open(DEFAULT_STATUS, DEFAULT_TIME, DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);

        session.compose((PatternAssembler patternAssembler) -> patternAssembler
                .meaning(MEANING)
                .purpose(PURPOSE)
                .fieldDefinition(MEANING, PURPOSE, STRING)
                .attach((FullyQualifiedName fqn) -> fqn
                        .language(ENGLISH_LANGUAGE)
                        .text("FQN for Pattern")
                        .caseSignificance(DESCRIPTION_NOT_CASE_SENSITIVE)));

        composer.commitSession(session);
        int expectedComponentsUpdatedCount = 2;
        int actualComponentsUpdatedCount = session.componentsInSessionCount();
        assertEquals(expectedComponentsUpdatedCount, actualComponentsUpdatedCount,
                String.format("Expect %s updated components, but %s were updated instead.", expectedComponentsUpdatedCount, actualComponentsUpdatedCount));
    }

    @Test
    public void createPatternWithSynonymTest() {
        Composer composer = new Composer("createPatternWithSynonymTest");
        Session session = composer.open(DEFAULT_STATUS, DEFAULT_TIME, DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);

        session.compose((PatternAssembler patternAssembler) -> patternAssembler
                .meaning(MEANING)
                .purpose(PURPOSE)
                .fieldDefinition(MEANING, PURPOSE, STRING)
                .attach((Synonym synonym) -> synonym
                        .language(ENGLISH_LANGUAGE)
                        .text("Synonym for Pattern")
                        .caseSignificance(DESCRIPTION_NOT_CASE_SENSITIVE)));

        composer.commitSession(session);
        int expectedComponentsUpdatedCount = 2;
        int actualComponentsUpdatedCount = session.componentsInSessionCount();
        assertEquals(expectedComponentsUpdatedCount, actualComponentsUpdatedCount,
                String.format("Expect %s updated components, but %s were updated instead.", expectedComponentsUpdatedCount, actualComponentsUpdatedCount));
    }

    @Test
    public void createPatternWithDefinitionTest() {
        Composer composer = new Composer("createPatternWithDefinitionTest");
        Session session = composer.open(DEFAULT_STATUS, DEFAULT_TIME, DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);

        session.compose((PatternAssembler patternAssembler) -> patternAssembler
                .meaning(MEANING)
                .purpose(PURPOSE)
                .fieldDefinition(MEANING, PURPOSE, STRING)
                .attach((Definition def) -> def
                        .language(ENGLISH_LANGUAGE)
                        .text("Definition for Pattern")
                        .caseSignificance(DESCRIPTION_NOT_CASE_SENSITIVE)));

        composer.commitSession(session);
        int expectedComponentsUpdatedCount = 2;
        int actualComponentsUpdatedCount = session.componentsInSessionCount();
        assertEquals(expectedComponentsUpdatedCount, actualComponentsUpdatedCount,
                String.format("Expect %s updated components, but %s were updated instead.", expectedComponentsUpdatedCount, actualComponentsUpdatedCount));
    }

    @Test
    public void createPatternWithAxiomSyntaxTest() {
        Composer composer = new Composer("createPatternWithAxiomSyntaxTest");
        Session session = composer.open(DEFAULT_STATUS, DEFAULT_TIME, DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);

        session.compose((PatternAssembler patternAssembler) -> patternAssembler
                .meaning(MEANING)
                .purpose(PURPOSE)
                .fieldDefinition(MEANING, PURPOSE, STRING)
                .attach((AxiomSyntax axiomSyntax) -> axiomSyntax
                        .text("AxiomSyntax for Pattern")));

        composer.commitSession(session);
        int expectedComponentsUpdatedCount = 2;
        int actualComponentsUpdatedCount = session.componentsInSessionCount();
        assertEquals(expectedComponentsUpdatedCount, actualComponentsUpdatedCount,
                String.format("Expect %s updated components, but %s were updated instead.", expectedComponentsUpdatedCount, actualComponentsUpdatedCount));
    }

    @Test
    public void createPatternWithCommentTest() {
        Composer composer = new Composer("createPatternWithCommentTest");
        Session session = composer.open(DEFAULT_STATUS, DEFAULT_TIME, DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);

        session.compose((PatternAssembler patternAssembler) -> patternAssembler
                .meaning(MEANING)
                .purpose(PURPOSE)
                .fieldDefinition(MEANING, PURPOSE, STRING)
                .attach((Comment comment) -> comment
                        .text("Comment for Pattern")));

        composer.commitSession(session);
        int expectedComponentsUpdatedCount = 2;
        int actualComponentsUpdatedCount = session.componentsInSessionCount();
        assertEquals(expectedComponentsUpdatedCount, actualComponentsUpdatedCount,
                String.format("Expect %s updated components, but %s were updated instead.", expectedComponentsUpdatedCount, actualComponentsUpdatedCount));
    }

    @Test
    public void createPatternWithGbDialectTest() {
        Composer composer = new Composer("createPatternWithGbDialectTest");
        Session session = composer.open(DEFAULT_STATUS, DEFAULT_TIME, DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);

        session.compose((PatternAssembler patternAssembler) -> patternAssembler
                .meaning(MEANING)
                .purpose(PURPOSE)
                .fieldDefinition(MEANING, PURPOSE, STRING)
                .attach((GBDialect gbDialect) -> gbDialect
                        .acceptability(PREFERRED)));

        composer.commitSession(session);
        int expectedComponentsUpdatedCount = 2;
        int actualComponentsUpdatedCount = session.componentsInSessionCount();
        assertEquals(expectedComponentsUpdatedCount, actualComponentsUpdatedCount,
                String.format("Expect %s updated components, but %s were updated instead.", expectedComponentsUpdatedCount, actualComponentsUpdatedCount));
    }

    @Test
    public void createPatternWithUsDialectTest() {
        Composer composer = new Composer("createPatternWithUsDialectTest");
        Session session = composer.open(DEFAULT_STATUS, DEFAULT_TIME, DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);

        session.compose((PatternAssembler patternAssembler) -> patternAssembler
                .meaning(MEANING)
                .purpose(PURPOSE)
                .fieldDefinition(MEANING, PURPOSE, STRING)
                .attach((USDialect usDialect) -> usDialect
                        .acceptability(PREFERRED)));

        composer.commitSession(session);
        int expectedComponentsUpdatedCount = 2;
        int actualComponentsUpdatedCount = session.componentsInSessionCount();
        assertEquals(expectedComponentsUpdatedCount, actualComponentsUpdatedCount,
                String.format("Expect %s updated components, but %s were updated instead.", expectedComponentsUpdatedCount, actualComponentsUpdatedCount));
    }

    @Test
    public void createPatternWithIdentifierTest() {
        Composer composer = new Composer("createPatternWithIdentifierTest");
        Session session = composer.open(DEFAULT_STATUS, DEFAULT_TIME, DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);

        Pattern patternProxy = Pattern.make(PublicIds.newRandom());
        session.compose((PatternAssembler patternAssembler) -> patternAssembler
                .meaning(MEANING)
                .purpose(PURPOSE)
                .fieldDefinition(MEANING, PURPOSE, STRING)
                .attach((Identifier identifier) -> identifier
                        .source(TinkarTerm.UNIVERSALLY_UNIQUE_IDENTIFIER)
                        .identifier(patternProxy.publicId().asUuidArray()[0].toString())));

        composer.commitSession(session);
        int expectedComponentsUpdatedCount = 2;
        int actualComponentsUpdatedCount = session.componentsInSessionCount();
        assertEquals(expectedComponentsUpdatedCount, actualComponentsUpdatedCount,
                String.format("Expect %s updated components, but %s were updated instead.", expectedComponentsUpdatedCount, actualComponentsUpdatedCount));
    }

    @Test
    public void createPatternWithKometBaseModelTest() {
        Composer composer = new Composer("createPatternWithKometBaseModelTest");
        Session session = composer.open(DEFAULT_STATUS, DEFAULT_TIME, DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);

        session.compose((PatternAssembler patternAssembler) -> patternAssembler
                .meaning(MEANING)
                .purpose(PURPOSE)
                .fieldDefinition(MEANING, PURPOSE, STRING)
                .attach((KometBaseModel kometBaseModelMembership) -> {}));

        composer.commitSession(session);
        int expectedComponentsUpdatedCount = 2;
        int actualComponentsUpdatedCount = session.componentsInSessionCount();
        assertEquals(expectedComponentsUpdatedCount, actualComponentsUpdatedCount,
                String.format("Expect %s updated components, but %s were updated instead.", expectedComponentsUpdatedCount, actualComponentsUpdatedCount));
    }

    @Test
    public void createPatternWithTinkarBaseModelTest() {
        Composer composer = new Composer("createPatternWithTinkarBaseModelTest");
        Session session = composer.open(DEFAULT_STATUS, DEFAULT_TIME, DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);

        session.compose((PatternAssembler patternAssembler) -> patternAssembler
                .meaning(MEANING)
                .purpose(PURPOSE)
                .fieldDefinition(MEANING, PURPOSE, STRING)
                .attach((TinkarBaseModel tinkarBaseModelMembership) -> {}));

        composer.commitSession(session);
        int expectedComponentsUpdatedCount = 2;
        int actualComponentsUpdatedCount = session.componentsInSessionCount();
        assertEquals(expectedComponentsUpdatedCount, actualComponentsUpdatedCount,
                String.format("Expect %s updated components, but %s were updated instead.", expectedComponentsUpdatedCount, actualComponentsUpdatedCount));
    }

    @Test
    public void createPatternWithStatedAxiomTest() {
        Composer composer = new Composer("createPatternWithStatedAxiomTest");
        Session session = composer.open(DEFAULT_STATUS, DEFAULT_TIME, DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);

        session.compose((PatternAssembler patternAssembler) -> patternAssembler
                .meaning(MEANING)
                .purpose(PURPOSE)
                .fieldDefinition(MEANING, PURPOSE, STRING)
                .attach((StatedAxiom statedAxiom) -> statedAxiom
                        .isA(EL_PLUS_PLUS_STATED_TERMINOLOGICAL_AXIOMS, EL_PLUS_PLUS_INFERRED_TERMINOLOGICAL_AXIOMS)));

        composer.commitSession(session);
        int expectedComponentsUpdatedCount = 2;
        int actualComponentsUpdatedCount = session.componentsInSessionCount();
        assertEquals(expectedComponentsUpdatedCount, actualComponentsUpdatedCount,
                String.format("Expect %s updated components, but %s were updated instead.", expectedComponentsUpdatedCount, actualComponentsUpdatedCount));
    }

    @Test
    public void createPatternWithStatedNavigationTest() {
        Composer composer = new Composer("createPatternWithStatedNavigationTest");
        Session session = composer.open(DEFAULT_STATUS, DEFAULT_TIME, DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);

        session.compose((PatternAssembler patternAssembler) -> patternAssembler
                .meaning(MEANING)
                .purpose(PURPOSE)
                .fieldDefinition(MEANING, PURPOSE, STRING)
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
    public void createPatternWithCustomSemanticTest() {
        Composer composer = new Composer("createPatternWithCustomSemanticTest");
        Session session = composer.open(DEFAULT_STATUS, DEFAULT_TIME, DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);

        session.compose((PatternAssembler patternAssembler) -> patternAssembler
                .meaning(MEANING)
                .purpose(PURPOSE)
                .fieldDefinition(MEANING, PURPOSE, STRING)
                .attach(new CustomSemantic()
                        .text("Custom Semantic for Pattern")));

        composer.commitSession(session);
        int expectedComponentsUpdatedCount = 2;
        int actualComponentsUpdatedCount = session.componentsInSessionCount();
        assertEquals(expectedComponentsUpdatedCount, actualComponentsUpdatedCount,
                String.format("Expect %s updated components, but %s were updated instead.", expectedComponentsUpdatedCount, actualComponentsUpdatedCount));
    }
    // ### END: Creation Tests with Semantics

    @Test
    public void complexPatternCreateTest() {
        PublicId fqnId = PublicIds.of(UUID.nameUUIDFromBytes("fqnId".getBytes()));
        PublicId patternId = PublicIds.of(UUID.nameUUIDFromBytes("patternId".getBytes()));
        Composer composer = new Composer("complexPatternCreateTest");

        Session session = composer.open(DEFAULT_STATUS, System.currentTimeMillis(), DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);

        session.compose((PatternAssembler patternAssembler) -> patternAssembler
                .meaning(MEANING)
                .purpose(PURPOSE)
                .fieldDefinition(MEANING, PURPOSE, STRING)
                .pattern(Pattern.make("Pattern", patternId)))
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
                        .text("Comment1 on Pattern"))
                .attach((FullyQualifiedName fqn) -> fqn
                        .semantic(Semantic.make("F2", PublicIds.newRandom()))
                        .language(ENGLISH_LANGUAGE)
                        .text("FQN2")
                        .caseSignificance(DESCRIPTION_NOT_CASE_SENSITIVE))
                .attach((Comment comment) -> comment
                        .semantic(Semantic.make("C2", PublicIds.newRandom()))
                        .text("Comment2 on Pattern"))
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
