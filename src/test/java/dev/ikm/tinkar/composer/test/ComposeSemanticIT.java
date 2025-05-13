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
import dev.ikm.tinkar.composer.assembler.SemanticAssembler;
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

import static dev.ikm.tinkar.terms.TinkarTerm.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ComposeSemanticIT {
    public static final Function<String, File> createFilePathInTarget = (pathName) -> new File("%s/target/%s".formatted(System.getProperty("user.dir"), pathName));
    public static final File PB_STARTER_DATA = createFilePathInTarget.apply("data/tinkar-starter-data-1.0.0-pb.zip");
    public static State DEFAULT_STATUS = State.ACTIVE;
    public static long DEFAULT_TIME = System.currentTimeMillis();
    public static Concept DEFAULT_AUTHOR = USER;
    public static Concept DEFAULT_MODULE = DEVELOPMENT_MODULE;
    public static Concept DEFAULT_PATH = DEVELOPMENT_PATH;

    private static final Pattern COOL_NEW_PATTERN = Pattern.make(PublicIds.newRandom());
    private static final Concept DEFAULT_REF_PROXY = Concept.make(PublicIds.newRandom());

    @BeforeAll
    public void beforeAll() {
        CachingService.clearAll();
        PrimitiveData.selectControllerByName("Load Ephemeral Store");
        PrimitiveData.start();
        EntityCountSummary entityCountSummary = new LoadEntitiesFromProtobufFile(PB_STARTER_DATA).compute();

        Composer composer = new Composer("newPattern");
        Session session = composer.open(DEFAULT_STATUS, DEFAULT_TIME, DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);
        session.compose((PatternAssembler patternAssembler) -> patternAssembler
                .pattern(COOL_NEW_PATTERN)
                .meaning(MEANING)
                .purpose(PURPOSE)
                .fieldDefinition(MEANING, PURPOSE, STRING));
        composer.commitSession(session);
    }

    @AfterAll
    public void afterAll() {
        PrimitiveData.stop();
    }

    // ### START: SemanticAssembler Creation Tests Basic
    @Test
    public void createSemanticWithPublicIdTest() {
        Composer composer = new Composer("createSemanticWithRandomPublicIdTest");
        Session session = composer.open(DEFAULT_STATUS, DEFAULT_TIME, DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);

        Semantic semanticProxy = Semantic.make(PublicIds.newRandom());
        session.compose((SemanticAssembler semanticAssembler) -> semanticAssembler
                .semantic(semanticProxy)
                .reference(ROOT_VERTEX)
                .pattern(COOL_NEW_PATTERN)
                .fieldValues(vals -> vals
                        .add("String Field Value for Cool New Pattern")));

        composer.commitSession(session);
        int expectedComponentsUpdatedCount = 1;
        int actualComponentsUpdatedCount = session.componentsInSessionCount();
        assertEquals(expectedComponentsUpdatedCount, actualComponentsUpdatedCount,
                String.format("Expect %s updated components, but %s were updated instead.", expectedComponentsUpdatedCount, actualComponentsUpdatedCount));
    }

    @Test
    public void createSemanticWithoutPublicIdTest() {
        Composer composer = new Composer("createSemanticWithoutPublicIdTest");
        Session session = composer.open(DEFAULT_STATUS, DEFAULT_TIME, DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);

        session.compose((SemanticAssembler semanticAssembler) -> semanticAssembler
                .reference(ROOT_VERTEX)
                .pattern(COOL_NEW_PATTERN)
                .fieldValues(vals -> vals
                        .add("String Field Value for Cool New Pattern")));

        composer.commitSession(session);
        int expectedComponentsUpdatedCount = 1;
        int actualComponentsUpdatedCount = session.componentsInSessionCount();
        assertEquals(expectedComponentsUpdatedCount, actualComponentsUpdatedCount,
                String.format("Expect %s updated components, but %s were updated instead.", expectedComponentsUpdatedCount, actualComponentsUpdatedCount));
    }
    // ### END: SemanticAssembler Creation Tests Basic

    // ### START: SemanticAssembler Creation Tests with Semantics
    @Test
    public void createSemanticWithFqnTest() {
        Composer composer = new Composer("createSemanticWithFqnTest");
        Session session = composer.open(DEFAULT_STATUS, DEFAULT_TIME, DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);

        session.compose((SemanticAssembler semanticAssembler) -> semanticAssembler
                .reference(ROOT_VERTEX)
                .pattern(COOL_NEW_PATTERN)
                .fieldValues(vals -> vals
                        .add("String Field Value for Cool New Pattern"))
                .attach((FullyQualifiedName fqn) -> fqn
                        .language(ENGLISH_LANGUAGE)
                        .text("FQN for Semantic")
                        .caseSignificance(DESCRIPTION_NOT_CASE_SENSITIVE)));

        composer.commitSession(session);
        int expectedComponentsUpdatedCount = 2;
        int actualComponentsUpdatedCount = session.componentsInSessionCount();
        assertEquals(expectedComponentsUpdatedCount, actualComponentsUpdatedCount,
                String.format("Expect %s updated components, but %s were updated instead.", expectedComponentsUpdatedCount, actualComponentsUpdatedCount));
    }

    @Test
    public void createSemanticWithSynonymTest() {
        Composer composer = new Composer("createSemanticWithSynonymTest");
        Session session = composer.open(DEFAULT_STATUS, DEFAULT_TIME, DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);

        session.compose((SemanticAssembler semanticAssembler) -> semanticAssembler
                .reference(ROOT_VERTEX)
                .pattern(COOL_NEW_PATTERN)
                .fieldValues(vals -> vals
                        .add("String Field Value for Cool New Pattern"))
                .attach((Synonym synonym) -> synonym
                        .language(ENGLISH_LANGUAGE)
                        .text("Synonym for Semantic")
                        .caseSignificance(DESCRIPTION_NOT_CASE_SENSITIVE)));

        composer.commitSession(session);
        int expectedComponentsUpdatedCount = 2;
        int actualComponentsUpdatedCount = session.componentsInSessionCount();
        assertEquals(expectedComponentsUpdatedCount, actualComponentsUpdatedCount,
                String.format("Expect %s updated components, but %s were updated instead.", expectedComponentsUpdatedCount, actualComponentsUpdatedCount));
    }

    @Test
    public void createSemanticWithDefinitionTest() {
        Composer composer = new Composer("createSemanticWithDefinitionTest");
        Session session = composer.open(DEFAULT_STATUS, DEFAULT_TIME, DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);

        session.compose((SemanticAssembler semanticAssembler) -> semanticAssembler
                .reference(ROOT_VERTEX)
                .pattern(COOL_NEW_PATTERN)
                .fieldValues(vals -> vals
                        .add("String Field Value for Cool New Pattern"))
                .attach((Definition def) -> def
                        .language(ENGLISH_LANGUAGE)
                        .text("Definition for Semantic")
                        .caseSignificance(DESCRIPTION_NOT_CASE_SENSITIVE)));

        composer.commitSession(session);
        int expectedComponentsUpdatedCount = 2;
        int actualComponentsUpdatedCount = session.componentsInSessionCount();
        assertEquals(expectedComponentsUpdatedCount, actualComponentsUpdatedCount,
                String.format("Expect %s updated components, but %s were updated instead.", expectedComponentsUpdatedCount, actualComponentsUpdatedCount));
    }

    @Test
    public void createSemanticWithAxiomSyntaxTest() {
        Composer composer = new Composer("createSemanticWithAxiomSyntaxTest");
        Session session = composer.open(DEFAULT_STATUS, DEFAULT_TIME, DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);

        session.compose((SemanticAssembler semanticAssembler) -> semanticAssembler
                .reference(ROOT_VERTEX)
                .pattern(COOL_NEW_PATTERN)
                .fieldValues(vals -> vals
                        .add("String Field Value for Cool New Pattern"))
                .attach((AxiomSyntax axiomSyntax) -> axiomSyntax
                        .text("AxiomSyntax for Semantic")));

        composer.commitSession(session);
        int expectedComponentsUpdatedCount = 2;
        int actualComponentsUpdatedCount = session.componentsInSessionCount();
        assertEquals(expectedComponentsUpdatedCount, actualComponentsUpdatedCount,
                String.format("Expect %s updated components, but %s were updated instead.", expectedComponentsUpdatedCount, actualComponentsUpdatedCount));
    }

    @Test
    public void createSemanticWithCommentTest() {
        Composer composer = new Composer("createSemanticWithCommentTest");
        Session session = composer.open(DEFAULT_STATUS, DEFAULT_TIME, DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);

        session.compose((SemanticAssembler semanticAssembler) -> semanticAssembler
                .reference(ROOT_VERTEX)
                .pattern(COOL_NEW_PATTERN)
                .fieldValues(vals -> vals
                        .add("String Field Value for Cool New Pattern"))
                .attach((Comment comment) -> comment
                        .text("Comment for Semantic")));

        composer.commitSession(session);
        int expectedComponentsUpdatedCount = 2;
        int actualComponentsUpdatedCount = session.componentsInSessionCount();
        assertEquals(expectedComponentsUpdatedCount, actualComponentsUpdatedCount,
                String.format("Expect %s updated components, but %s were updated instead.", expectedComponentsUpdatedCount, actualComponentsUpdatedCount));
    }

    @Test
    public void createSemanticWithGbDialectTest() {
        Composer composer = new Composer("createSemanticWithGbDialectTest");
        Session session = composer.open(DEFAULT_STATUS, DEFAULT_TIME, DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);

        session.compose((SemanticAssembler semanticAssembler) -> semanticAssembler
                .reference(ROOT_VERTEX)
                .pattern(COOL_NEW_PATTERN)
                .fieldValues(vals -> vals
                        .add("String Field Value for Cool New Pattern"))
                .attach((GBDialect gbDialect) -> gbDialect
                        .acceptability(PREFERRED)));

        composer.commitSession(session);
        int expectedComponentsUpdatedCount = 2;
        int actualComponentsUpdatedCount = session.componentsInSessionCount();
        assertEquals(expectedComponentsUpdatedCount, actualComponentsUpdatedCount,
                String.format("Expect %s updated components, but %s were updated instead.", expectedComponentsUpdatedCount, actualComponentsUpdatedCount));
    }

    @Test
    public void createSemanticWithUsDialectTest() {
        Composer composer = new Composer("createSemanticWithUsDialectTest");
        Session session = composer.open(DEFAULT_STATUS, DEFAULT_TIME, DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);

        session.compose((SemanticAssembler semanticAssembler) -> semanticAssembler
                .reference(ROOT_VERTEX)
                .pattern(COOL_NEW_PATTERN)
                .fieldValues(vals -> vals
                        .add("String Field Value for Cool New Pattern"))
                .attach((USDialect usDialect) -> usDialect
                        .acceptability(PREFERRED)));

        composer.commitSession(session);
        int expectedComponentsUpdatedCount = 2;
        int actualComponentsUpdatedCount = session.componentsInSessionCount();
        assertEquals(expectedComponentsUpdatedCount, actualComponentsUpdatedCount,
                String.format("Expect %s updated components, but %s were updated instead.", expectedComponentsUpdatedCount, actualComponentsUpdatedCount));
    }

    @Test
    public void createSemanticWithIdentifierTest() {
        Composer composer = new Composer("createSemanticWithIdentifierTest");
        Session session = composer.open(DEFAULT_STATUS, DEFAULT_TIME, DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);

        Semantic semanticProxy = Semantic.make(PublicIds.newRandom());
        session.compose((SemanticAssembler semanticAssembler) -> semanticAssembler
                .reference(ROOT_VERTEX)
                .pattern(COOL_NEW_PATTERN)
                .fieldValues(vals -> vals
                        .add("String Field Value for Cool New Pattern"))
                .attach((Identifier identifier) -> identifier
                        .source(TinkarTerm.UNIVERSALLY_UNIQUE_IDENTIFIER)
                        .identifier(semanticProxy.publicId().asUuidArray()[0].toString())));

        composer.commitSession(session);
        int expectedComponentsUpdatedCount = 2;
        int actualComponentsUpdatedCount = session.componentsInSessionCount();
        assertEquals(expectedComponentsUpdatedCount, actualComponentsUpdatedCount,
                String.format("Expect %s updated components, but %s were updated instead.", expectedComponentsUpdatedCount, actualComponentsUpdatedCount));
    }

    @Test
    public void createSemanticWithKometBaseModelTest() {
        Composer composer = new Composer("createSemanticWithKometBaseModelTest");
        Session session = composer.open(DEFAULT_STATUS, DEFAULT_TIME, DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);

        session.compose((SemanticAssembler semanticAssembler) -> semanticAssembler
                .reference(ROOT_VERTEX)
                .pattern(COOL_NEW_PATTERN)
                .fieldValues(vals -> vals
                        .add("String Field Value for Cool New Pattern"))
                .attach((KometBaseModel kometBaseModelMembership) -> {}));

        composer.commitSession(session);
        int expectedComponentsUpdatedCount = 2;
        int actualComponentsUpdatedCount = session.componentsInSessionCount();
        assertEquals(expectedComponentsUpdatedCount, actualComponentsUpdatedCount,
                String.format("Expect %s updated components, but %s were updated instead.", expectedComponentsUpdatedCount, actualComponentsUpdatedCount));
    }

    @Test
    public void createSemanticWithTinkarBaseModelTest() {
        Composer composer = new Composer("createSemanticWithTinkarBaseModelTest");
        Session session = composer.open(DEFAULT_STATUS, DEFAULT_TIME, DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);

        session.compose((SemanticAssembler semanticAssembler) -> semanticAssembler
                .reference(ROOT_VERTEX)
                .pattern(COOL_NEW_PATTERN)
                .fieldValues(vals -> vals
                        .add("String Field Value for Cool New Pattern"))
                .attach((TinkarBaseModel tinkarBaseModelMembership) -> {}));

        composer.commitSession(session);
        int expectedComponentsUpdatedCount = 2;
        int actualComponentsUpdatedCount = session.componentsInSessionCount();
        assertEquals(expectedComponentsUpdatedCount, actualComponentsUpdatedCount,
                String.format("Expect %s updated components, but %s were updated instead.", expectedComponentsUpdatedCount, actualComponentsUpdatedCount));
    }

    @Test
    public void createSemanticWithStatedAxiomTest() {
        Composer composer = new Composer("createSemanticWithStatedAxiomTest");
        Session session = composer.open(DEFAULT_STATUS, DEFAULT_TIME, DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);

        session.compose((SemanticAssembler semanticAssembler) -> semanticAssembler
                .reference(ROOT_VERTEX)
                .pattern(COOL_NEW_PATTERN)
                .fieldValues(vals -> vals
                        .add("String Field Value for Cool New Pattern"))
                .attach((StatedAxiom statedAxiom) -> statedAxiom
                        .isA(EL_PLUS_PLUS_STATED_TERMINOLOGICAL_AXIOMS, EL_PLUS_PLUS_INFERRED_TERMINOLOGICAL_AXIOMS)));

        composer.commitSession(session);
        int expectedComponentsUpdatedCount = 2;
        int actualComponentsUpdatedCount = session.componentsInSessionCount();
        assertEquals(expectedComponentsUpdatedCount, actualComponentsUpdatedCount,
                String.format("Expect %s updated components, but %s were updated instead.", expectedComponentsUpdatedCount, actualComponentsUpdatedCount));
    }

    @Test
    public void createSemanticWithStatedNavigationTest() {
        Composer composer = new Composer("createSemanticWithStatedNavigationTest");
        Session session = composer.open(DEFAULT_STATUS, DEFAULT_TIME, DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);

        session.compose((SemanticAssembler semanticAssembler) -> semanticAssembler
                .reference(ROOT_VERTEX)
                .pattern(COOL_NEW_PATTERN)
                .fieldValues(vals -> vals
                        .add("String Field Value for Cool New Pattern"))
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
    public void createSemanticWithCustomSemanticTest() {
        Composer composer = new Composer("createSemanticWithCustomSemanticTest");
        Session session = composer.open(DEFAULT_STATUS, DEFAULT_TIME, DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);

        session.compose((SemanticAssembler semanticAssembler) -> semanticAssembler
                .reference(ROOT_VERTEX)
                .pattern(COOL_NEW_PATTERN)
                .fieldValues(vals -> vals
                        .add("String Field Value for Cool New Pattern"))
                .attach(new CustomSemantic()
                        .text("Custom Semantic for Semantic")));

        composer.commitSession(session);
        int expectedComponentsUpdatedCount = 2;
        int actualComponentsUpdatedCount = session.componentsInSessionCount();
        assertEquals(expectedComponentsUpdatedCount, actualComponentsUpdatedCount,
                String.format("Expect %s updated components, but %s were updated instead.", expectedComponentsUpdatedCount, actualComponentsUpdatedCount));
    }
    // ### END: SemanticAssembler Creation Tests with Semantics

    // ### START: Semantic Supplier Creation Tests Basic
    @Test
    public void createSemanticFromSupplierWithPublicIdTest() {
        Composer composer = new Composer("createSemanticFromSupplierWithPublicIdTest");
        Session session = composer.open(DEFAULT_STATUS, DEFAULT_TIME, DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);

        Semantic semanticProxy = Semantic.make(PublicIds.newRandom());
        session.compose(new CustomSemantic()
                        .semantic(semanticProxy)
                        .text("Custom Semantic from Supplier"),
                DEFAULT_REF_PROXY);

        composer.commitSession(session);
        int expectedComponentsUpdatedCount = 1;
        int actualComponentsUpdatedCount = session.componentsInSessionCount();
        assertEquals(expectedComponentsUpdatedCount, actualComponentsUpdatedCount,
                String.format("Expect %s updated components, but %s were updated instead.", expectedComponentsUpdatedCount, actualComponentsUpdatedCount));
    }

    @Test
    public void createSemanticFromSupplierWithoutPublicIdTest() {
        Composer composer = new Composer("createSemanticFromSupplierWithoutPublicIdTest");
        Session session = composer.open(DEFAULT_STATUS, DEFAULT_TIME, DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);

        session.compose(new CustomSemantic().text("Custom Semantic from Supplier"), DEFAULT_REF_PROXY);

        composer.commitSession(session);
        int expectedComponentsUpdatedCount = 1;
        int actualComponentsUpdatedCount = session.componentsInSessionCount();
        assertEquals(expectedComponentsUpdatedCount, actualComponentsUpdatedCount,
                String.format("Expect %s updated components, but %s were updated instead.", expectedComponentsUpdatedCount, actualComponentsUpdatedCount));
    }
    // ### END: Semantic Supplier Creation Tests Basic

    // ### START: Semantic Supplier Creation Tests with Semantics
    @Test
    public void createSemanticFromSupplierWithFqnTest() {
        Composer composer = new Composer("createSemanticFromSupplierWithFqnTest");
        Session session = composer.open(DEFAULT_STATUS, DEFAULT_TIME, DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);

        session.compose(new CustomSemantic().text("Custom Semantic from Supplier"), DEFAULT_REF_PROXY)
                .attach((FullyQualifiedName fqn) -> fqn
                        .language(ENGLISH_LANGUAGE)
                        .text("FQN for Semantic")
                        .caseSignificance(DESCRIPTION_NOT_CASE_SENSITIVE));

        composer.commitSession(session);
        int expectedComponentsUpdatedCount = 2;
        int actualComponentsUpdatedCount = session.componentsInSessionCount();
        assertEquals(expectedComponentsUpdatedCount, actualComponentsUpdatedCount,
                String.format("Expect %s updated components, but %s were updated instead.", expectedComponentsUpdatedCount, actualComponentsUpdatedCount));
    }

    @Test
    public void createSemanticFromSupplierWithSynonymTest() {
        Composer composer = new Composer("createSemanticFromSupplierWithSynonymTest");
        Session session = composer.open(DEFAULT_STATUS, DEFAULT_TIME, DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);

        session.compose(new CustomSemantic().text("Custom Semantic from Supplier"), DEFAULT_REF_PROXY)
                .attach((Synonym synonym) -> synonym
                        .language(ENGLISH_LANGUAGE)
                        .text("Synonym for Semantic")
                        .caseSignificance(DESCRIPTION_NOT_CASE_SENSITIVE));

        composer.commitSession(session);
        int expectedComponentsUpdatedCount = 2;
        int actualComponentsUpdatedCount = session.componentsInSessionCount();
        assertEquals(expectedComponentsUpdatedCount, actualComponentsUpdatedCount,
                String.format("Expect %s updated components, but %s were updated instead.", expectedComponentsUpdatedCount, actualComponentsUpdatedCount));
    }

    @Test
    public void createSemanticFromSupplierWithDefinitionTest() {
        Composer composer = new Composer("createSemanticFromSupplierWithDefinitionTest");
        Session session = composer.open(DEFAULT_STATUS, DEFAULT_TIME, DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);

        session.compose(new CustomSemantic().text("Custom Semantic from Supplier"), DEFAULT_REF_PROXY)
                .attach((Definition def) -> def
                        .language(ENGLISH_LANGUAGE)
                        .text("Definition for Semantic")
                        .caseSignificance(DESCRIPTION_NOT_CASE_SENSITIVE));

        composer.commitSession(session);
        int expectedComponentsUpdatedCount = 2;
        int actualComponentsUpdatedCount = session.componentsInSessionCount();
        assertEquals(expectedComponentsUpdatedCount, actualComponentsUpdatedCount,
                String.format("Expect %s updated components, but %s were updated instead.", expectedComponentsUpdatedCount, actualComponentsUpdatedCount));
    }

    @Test
    public void createSemanticFromSupplierWithAxiomSyntaxTest() {
        Composer composer = new Composer("createSemanticFromSupplierWithAxiomSyntaxTest");
        Session session = composer.open(DEFAULT_STATUS, DEFAULT_TIME, DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);

        session.compose(new CustomSemantic().text("Custom Semantic from Supplier"), DEFAULT_REF_PROXY)
                .attach((AxiomSyntax axiomSyntax) -> axiomSyntax
                        .text("AxiomSyntax for Semantic"));

        composer.commitSession(session);
        int expectedComponentsUpdatedCount = 2;
        int actualComponentsUpdatedCount = session.componentsInSessionCount();
        assertEquals(expectedComponentsUpdatedCount, actualComponentsUpdatedCount,
                String.format("Expect %s updated components, but %s were updated instead.", expectedComponentsUpdatedCount, actualComponentsUpdatedCount));
    }

    @Test
    public void createSemanticFromSupplierWithCommentTest() {
        Composer composer = new Composer("createSemanticFromSupplierWithCommentTest");
        Session session = composer.open(DEFAULT_STATUS, DEFAULT_TIME, DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);

        session.compose(new CustomSemantic().text("Custom Semantic from Supplier"), DEFAULT_REF_PROXY)
                .attach((Comment comment) -> comment
                        .text("Comment for Semantic"));

        composer.commitSession(session);
        int expectedComponentsUpdatedCount = 2;
        int actualComponentsUpdatedCount = session.componentsInSessionCount();
        assertEquals(expectedComponentsUpdatedCount, actualComponentsUpdatedCount,
                String.format("Expect %s updated components, but %s were updated instead.", expectedComponentsUpdatedCount, actualComponentsUpdatedCount));
    }

    @Test
    public void createSemanticFromSupplierWithGbDialectTest() {
        Composer composer = new Composer("createSemanticFromSupplierWithGbDialectTest");
        Session session = composer.open(DEFAULT_STATUS, DEFAULT_TIME, DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);

        session.compose(new CustomSemantic().text("Custom Semantic from Supplier"), DEFAULT_REF_PROXY)
                .attach((GBDialect gbDialect) -> gbDialect
                        .acceptability(PREFERRED));

        composer.commitSession(session);
        int expectedComponentsUpdatedCount = 2;
        int actualComponentsUpdatedCount = session.componentsInSessionCount();
        assertEquals(expectedComponentsUpdatedCount, actualComponentsUpdatedCount,
                String.format("Expect %s updated components, but %s were updated instead.", expectedComponentsUpdatedCount, actualComponentsUpdatedCount));
    }

    @Test
    public void createSemanticFromSupplierWithUsDialectTest() {
        Composer composer = new Composer("createSemanticFromSupplierWithUsDialectTest");
        Session session = composer.open(DEFAULT_STATUS, DEFAULT_TIME, DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);

        session.compose(new CustomSemantic().text("Custom Semantic from Supplier"), DEFAULT_REF_PROXY)
                .attach((USDialect usDialect) -> usDialect
                        .acceptability(PREFERRED));

        composer.commitSession(session);
        int expectedComponentsUpdatedCount = 2;
        int actualComponentsUpdatedCount = session.componentsInSessionCount();
        assertEquals(expectedComponentsUpdatedCount, actualComponentsUpdatedCount,
                String.format("Expect %s updated components, but %s were updated instead.", expectedComponentsUpdatedCount, actualComponentsUpdatedCount));
    }

    @Test
    public void createSemanticFromSupplierWithIdentifierTest() {
        Composer composer = new Composer("createSemanticFromSupplierWithIdentifierTest");
        Session session = composer.open(DEFAULT_STATUS, DEFAULT_TIME, DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);

        Semantic semanticProxy = Semantic.make(PublicIds.newRandom());
        session.compose(new CustomSemantic().text("Custom Semantic from Supplier"), DEFAULT_REF_PROXY)
                .attach((Identifier identifier) -> identifier
                        .source(TinkarTerm.UNIVERSALLY_UNIQUE_IDENTIFIER)
                        .identifier(semanticProxy.publicId().asUuidArray()[0].toString()));

        composer.commitSession(session);
        int expectedComponentsUpdatedCount = 2;
        int actualComponentsUpdatedCount = session.componentsInSessionCount();
        assertEquals(expectedComponentsUpdatedCount, actualComponentsUpdatedCount,
                String.format("Expect %s updated components, but %s were updated instead.", expectedComponentsUpdatedCount, actualComponentsUpdatedCount));
    }

    @Test
    public void createSemanticFromSupplierWithKometBaseModelTest() {
        Composer composer = new Composer("createSemanticFromSupplierWithKometBaseModelTest");
        Session session = composer.open(DEFAULT_STATUS, DEFAULT_TIME, DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);

        session.compose(new CustomSemantic().text("Custom Semantic from Supplier"), DEFAULT_REF_PROXY)
                .attach((KometBaseModel kometBaseModelMembership) -> {});

        composer.commitSession(session);
        int expectedComponentsUpdatedCount = 2;
        int actualComponentsUpdatedCount = session.componentsInSessionCount();
        assertEquals(expectedComponentsUpdatedCount, actualComponentsUpdatedCount,
                String.format("Expect %s updated components, but %s were updated instead.", expectedComponentsUpdatedCount, actualComponentsUpdatedCount));
    }

    @Test
    public void createSemanticFromSupplierWithTinkarBaseModelTest() {
        Composer composer = new Composer("createSemanticFromSupplierWithTinkarBaseModelTest");
        Session session = composer.open(DEFAULT_STATUS, DEFAULT_TIME, DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);

        session.compose(new CustomSemantic().text("Custom Semantic from Supplier"), DEFAULT_REF_PROXY)
                .attach((TinkarBaseModel tinkarBaseModelMembership) -> {});

        composer.commitSession(session);
        int expectedComponentsUpdatedCount = 2;
        int actualComponentsUpdatedCount = session.componentsInSessionCount();
        assertEquals(expectedComponentsUpdatedCount, actualComponentsUpdatedCount,
                String.format("Expect %s updated components, but %s were updated instead.", expectedComponentsUpdatedCount, actualComponentsUpdatedCount));
    }

    @Test
    public void createSemanticFromSupplierWithStatedAxiomTest() {
        Composer composer = new Composer("createSemanticFromSupplierWithStatedAxiomTest");
        Session session = composer.open(DEFAULT_STATUS, DEFAULT_TIME, DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);

        session.compose(new CustomSemantic().text("Custom Semantic from Supplier"), DEFAULT_REF_PROXY)
                .attach((StatedAxiom statedAxiom) -> statedAxiom
                        .isA(EL_PLUS_PLUS_STATED_TERMINOLOGICAL_AXIOMS, EL_PLUS_PLUS_INFERRED_TERMINOLOGICAL_AXIOMS));

        composer.commitSession(session);
        int expectedComponentsUpdatedCount = 2;
        int actualComponentsUpdatedCount = session.componentsInSessionCount();
        assertEquals(expectedComponentsUpdatedCount, actualComponentsUpdatedCount,
                String.format("Expect %s updated components, but %s were updated instead.", expectedComponentsUpdatedCount, actualComponentsUpdatedCount));
    }

    @Test
    public void createSemanticFromSupplierWithStatedNavigationTest() {
        Composer composer = new Composer("createSemanticFromSupplierWithStatedNavigationTest");
        Session session = composer.open(DEFAULT_STATUS, DEFAULT_TIME, DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);

        session.compose(new CustomSemantic().text("Custom Semantic from Supplier"), DEFAULT_REF_PROXY)
                .attach((StatedNavigation statedNav) -> statedNav
                        .parents(ROOT_VERTEX)
                        .children(MEANING));

        composer.commitSession(session);
        int expectedComponentsUpdatedCount = 2;
        int actualComponentsUpdatedCount = session.componentsInSessionCount();
        assertEquals(expectedComponentsUpdatedCount, actualComponentsUpdatedCount,
                String.format("Expect %s updated components, but %s were updated instead.", expectedComponentsUpdatedCount, actualComponentsUpdatedCount));
    }

    @Test
    public void createSemanticFromSupplierWithCustomSemanticTest() {
        Composer composer = new Composer("createSemanticFromSupplierWithCustomSemanticTest");
        Session session = composer.open(DEFAULT_STATUS, DEFAULT_TIME, DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);

        session.compose(new CustomSemantic().text("Custom Semantic from Supplier"), DEFAULT_REF_PROXY)
                .attach(new CustomSemantic()
                        .text("Custom Semantic for Semantic"));

        composer.commitSession(session);
        int expectedComponentsUpdatedCount = 2;
        int actualComponentsUpdatedCount = session.componentsInSessionCount();
        assertEquals(expectedComponentsUpdatedCount, actualComponentsUpdatedCount,
                String.format("Expect %s updated components, but %s were updated instead.", expectedComponentsUpdatedCount, actualComponentsUpdatedCount));
    }
    // ### END: Semantic Supplier Creation Tests with Semantics

    @Test
    public void complexSemanticCreateTest() {
        PublicId fqnId = PublicIds.of(UUID.nameUUIDFromBytes("fqnId".getBytes()));
        PublicId semanticId = PublicIds.of(UUID.nameUUIDFromBytes("semanticId".getBytes()));
        Composer composer = new Composer("complexSemanticCreateTest");

        Session session = composer.open(DEFAULT_STATUS, System.currentTimeMillis(), DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);

        session.compose((SemanticAssembler semanticAssembler) -> semanticAssembler
                .semantic(Semantic.make("Semantic", semanticId))
                .reference(ROOT_VERTEX)
                .pattern(COOL_NEW_PATTERN)
                .fieldValues(vals -> vals
                        .add("String Field Value for Cool New Pattern"))
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
                        .text("Comment1 on Semantic"))
                .attach((FullyQualifiedName fqn) -> fqn
                        .semantic(Semantic.make("F2", PublicIds.newRandom()))
                        .language(ENGLISH_LANGUAGE)
                        .text("FQN2")
                        .caseSignificance(DESCRIPTION_NOT_CASE_SENSITIVE))
                .attach((Comment comment) -> comment
                        .semantic(Semantic.make("C2", PublicIds.newRandom()))
                        .text("Comment2 on Semantic"))
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
                                .text("Comment2 on Synonym"))));

        composer.commitSession(session);

        int expectedComponentsUpdatedCount = 11;
        int actualComponentsUpdatedCount = session.componentsInSessionCount();

        assertEquals(expectedComponentsUpdatedCount, actualComponentsUpdatedCount,
                String.format("Expect %s updated components, but %s were updated instead.", expectedComponentsUpdatedCount, actualComponentsUpdatedCount));
    }
}
