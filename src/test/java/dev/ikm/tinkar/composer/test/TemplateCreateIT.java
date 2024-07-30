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
import org.junit.jupiter.api.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.UUID;
import java.util.function.Function;

import static dev.ikm.tinkar.terms.TinkarTerm.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TemplateCreateIT {
    public static final Function<String, File> createFilePathInTarget = (pathName) -> new File("%s/target/%s".formatted(System.getProperty("user.dir"), pathName));
    public static final File PB_STARTER_DATA = createFilePathInTarget.apply("data/tinkar-starter-data-1.0.0-pb.zip");
    public static State DEFAULT_STATUS = State.ACTIVE;
    public static long DEFAULT_TIME = System.currentTimeMillis();
    public static Concept DEFAULT_AUTHOR = USER;
    public static Concept DEFAULT_MODULE = DEVELOPMENT_MODULE;
    public static Concept DEFAULT_PATH = DEVELOPMENT_PATH;
    public static final File DATASTORE = createFilePathInTarget.apply("generated-data/" + TemplateCreateIT.class.getSimpleName());


    private final Path datastore = Path.of(System.getProperty("user.dir"))
            .resolve("target")
            .resolve(TemplateCreateIT.class.getSimpleName())
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

    @Test
    public void createFullyQualifiedNameTemplateTest() {
        Concept referenceConcept = Concept.make(PublicIds.newRandom());
        Composer composer = new Composer("createFullyQualifiedNameTemplateTest");
        Session session = composer.open(DEFAULT_STATUS, DEFAULT_TIME, DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);

        session.compose(new FullyQualifiedName()
                .language(ENGLISH_LANGUAGE)
                .text("FQN from Template")
                .caseSignificance(DESCRIPTION_NOT_CASE_SENSITIVE), referenceConcept);

        session.close();
        int expectedComponentsUpdatedCount = 1;
        int actualComponentsUpdatedCount = session.componentsInSessionCount();
        assertEquals(expectedComponentsUpdatedCount, actualComponentsUpdatedCount,
                String.format("Expect %s updated components, but %s were updated instead.", expectedComponentsUpdatedCount, actualComponentsUpdatedCount));
    }

    @Test
    public void createSynonymTemplateTest() {
        Concept referenceConcept = Concept.make(PublicIds.newRandom());
        Composer composer = new Composer("createSynonymTemplateTest");
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

    @Test
    public void createDefinitionTemplateTest() {
        Concept referenceConcept = Concept.make(PublicIds.newRandom());
        Composer composer = new Composer("createDefinitionTemplateTest");
        Session session = composer.open(DEFAULT_STATUS, DEFAULT_TIME, DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);

        session.compose(new Definition()
                .language(ENGLISH_LANGUAGE)
                .text("Definition from Template")
                .caseSignificance(DESCRIPTION_NOT_CASE_SENSITIVE), referenceConcept);

        session.close();
        int expectedComponentsUpdatedCount = 1;
        int actualComponentsUpdatedCount = session.componentsInSessionCount();
        assertEquals(expectedComponentsUpdatedCount, actualComponentsUpdatedCount,
                String.format("Expect %s updated components, but %s were updated instead.", expectedComponentsUpdatedCount, actualComponentsUpdatedCount));
    }

    @Test
    public void createAxiomSyntaxTemplateTest() {
        Concept referenceConcept = Concept.make(PublicIds.newRandom());
        Composer composer = new Composer("createAxiomSyntaxTemplateTest");
        Session session = composer.open(DEFAULT_STATUS, DEFAULT_TIME, DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);

        session.compose(new AxiomSyntax()
                .text("Insert OWL String"), referenceConcept);

        session.close();
        int expectedComponentsUpdatedCount = 1;
        int actualComponentsUpdatedCount = session.componentsInSessionCount();
        assertEquals(expectedComponentsUpdatedCount, actualComponentsUpdatedCount,
                String.format("Expect %s updated components, but %s were updated instead.", expectedComponentsUpdatedCount, actualComponentsUpdatedCount));
    }

    @Test
    public void createCommentTemplateTest() {
        Concept referenceConcept = Concept.make(PublicIds.newRandom());
        Composer composer = new Composer("createCommentTemplateTest");
        Session session = composer.open(DEFAULT_STATUS, DEFAULT_TIME, DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);

        session.compose(new Comment()
                .text("Comment from Template"), referenceConcept);

        session.close();
        int expectedComponentsUpdatedCount = 1;
        int actualComponentsUpdatedCount = session.componentsInSessionCount();
        assertEquals(expectedComponentsUpdatedCount, actualComponentsUpdatedCount,
                String.format("Expect %s updated components, but %s were updated instead.", expectedComponentsUpdatedCount, actualComponentsUpdatedCount));
    }

    @Test
    public void createUSDialectTemplateTest() {
        Concept referenceConcept = Concept.make(PublicIds.newRandom());
        Composer composer = new Composer("createUSDialectTemplateTest");
        Session session = composer.open(DEFAULT_STATUS, DEFAULT_TIME, DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);

        session.compose(new USDialect()
                .acceptability(PREFERRED), referenceConcept);

        session.close();
        int expectedComponentsUpdatedCount = 1;
        int actualComponentsUpdatedCount = session.componentsInSessionCount();
        assertEquals(expectedComponentsUpdatedCount, actualComponentsUpdatedCount,
                String.format("Expect %s updated components, but %s were updated instead.", expectedComponentsUpdatedCount, actualComponentsUpdatedCount));
    }

    @Test
    public void createGBDialectTemplateTest() {
        Concept referenceConcept = Concept.make(PublicIds.newRandom());
        Composer composer = new Composer("createGBDialectTemplateTest");
        Session session = composer.open(DEFAULT_STATUS, DEFAULT_TIME, DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);

        session.compose(new GBDialect()
                .acceptability(PREFERRED), referenceConcept);

        session.close();
        int expectedComponentsUpdatedCount = 1;
        int actualComponentsUpdatedCount = session.componentsInSessionCount();
        assertEquals(expectedComponentsUpdatedCount, actualComponentsUpdatedCount,
                String.format("Expect %s updated components, but %s were updated instead.", expectedComponentsUpdatedCount, actualComponentsUpdatedCount));
    }


    @Test
    public void createIdentifierTemplateTest() {
        Concept referenceConcept = Concept.make(PublicIds.newRandom());
        Composer composer = new Composer("createIdentifierTemplateTest");
        Session session = composer.open(DEFAULT_STATUS, DEFAULT_TIME, DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);

        session.compose(new Identifier()
                        .source(UNIVERSALLY_UNIQUE_IDENTIFIER)
                        .identifier(UUID.randomUUID().toString()), referenceConcept);

        session.close();
        int expectedComponentsUpdatedCount = 1;
        int actualComponentsUpdatedCount = session.componentsInSessionCount();
        assertEquals(expectedComponentsUpdatedCount, actualComponentsUpdatedCount,
                String.format("Expect %s updated components, but %s were updated instead.", expectedComponentsUpdatedCount, actualComponentsUpdatedCount));
    }

    @Test
    public void createStatedAxiomTemplateTest() {
        Concept referenceConcept = Concept.make(PublicIds.newRandom());
        Composer composer = new Composer("createStatedAxiomTemplateTest");
        Session session = composer.open(DEFAULT_STATUS, DEFAULT_TIME, DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);

        session.compose(new StatedAxiom()
                        .isA(ROOT_VERTEX), referenceConcept);

        session.close();
        int expectedComponentsUpdatedCount = 1;
        int actualComponentsUpdatedCount = session.componentsInSessionCount();
        assertEquals(expectedComponentsUpdatedCount, actualComponentsUpdatedCount,
                String.format("Expect %s updated components, but %s were updated instead.", expectedComponentsUpdatedCount, actualComponentsUpdatedCount));
    }

    @Test
    public void createKometBaseModelTemplateTest() {
        Concept referenceConcept = Concept.make(PublicIds.newRandom());
        Composer composer = new Composer("createKometBaseModelTemplateTest");
        Session session = composer.open(DEFAULT_STATUS, DEFAULT_TIME, DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);

        session.compose(new KometBaseModel(), referenceConcept);

        session.close();
        int expectedComponentsUpdatedCount = 1;
        int actualComponentsUpdatedCount = session.componentsInSessionCount();
        assertEquals(expectedComponentsUpdatedCount, actualComponentsUpdatedCount,
                String.format("Expect %s updated components, but %s were updated instead.", expectedComponentsUpdatedCount, actualComponentsUpdatedCount));
    }

    @Test
    public void createTinkarBaseModelTemplateTest() {
        Concept referenceConcept = Concept.make(PublicIds.newRandom());
        Composer composer = new Composer("createTinkarBaseModelTemplateTest");
        Session session = composer.open(DEFAULT_STATUS, DEFAULT_TIME, DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);

        session.compose(new TinkarBaseModel(), referenceConcept);

        session.close();
        int expectedComponentsUpdatedCount = 1;
        int actualComponentsUpdatedCount = session.componentsInSessionCount();
        assertEquals(expectedComponentsUpdatedCount, actualComponentsUpdatedCount,
                String.format("Expect %s updated components, but %s were updated instead.", expectedComponentsUpdatedCount, actualComponentsUpdatedCount));
    }

    @Test
    public void createCustomSemanticTemplateTest() {
        Concept referenceConcept = Concept.make(PublicIds.newRandom());
        Composer composer = new Composer("createCustomSemanticTemplateTest");
        Session session = composer.open(DEFAULT_STATUS, DEFAULT_TIME, DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);

        session.compose(new CustomSemantic()
                .text("Custom Semantic from Template"), referenceConcept);

        session.close();
        int expectedComponentsUpdatedCount = 1;
        int actualComponentsUpdatedCount = session.componentsInSessionCount();
        assertEquals(expectedComponentsUpdatedCount, actualComponentsUpdatedCount,
                String.format("Expect %s updated components, but %s were updated instead.", expectedComponentsUpdatedCount, actualComponentsUpdatedCount));
    }

}
