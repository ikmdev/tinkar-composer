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
import dev.ikm.tinkar.composer.ComposerSession;
import dev.ikm.tinkar.composer.template.Comment;
import dev.ikm.tinkar.composer.template.FullyQualifiedName;
import dev.ikm.tinkar.composer.template.Synonym;
import dev.ikm.tinkar.composer.template.USEnglishDialect;
import dev.ikm.tinkar.entity.EntityCountSummary;
import dev.ikm.tinkar.entity.EntityService;
import dev.ikm.tinkar.entity.load.LoadEntitiesFromProtobufFile;
import dev.ikm.tinkar.terms.EntityProxy;
import dev.ikm.tinkar.terms.EntityProxy.Concept;
import dev.ikm.tinkar.terms.State;
import dev.ikm.tinkar.terms.TinkarTerm;
import org.eclipse.collections.api.factory.Lists;
import org.junit.jupiter.api.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.UUID;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ComposerCreateIT {
    public static final Function<String,File> createFilePathInTarget = (pathName) -> new File("%s/target/%s".formatted(System.getProperty("user.dir"), pathName));
    public static final File PB_STARTER_DATA = createFilePathInTarget.apply("data/tinkar-starter-data-1.0.0-pb.zip");

    private final Path datastore = Path.of(System.getProperty("user.dir"))
            .resolve("target")
            .resolve(ComposerCreateIT.class.getSimpleName())
            .resolve("datastore");

    @BeforeAll
    public void beforeAll() throws IOException {
//        Files.createDirectories(datastore);
        CachingService.clearAll();
//        ServiceProperties.set(ServiceKeys.DATA_STORE_ROOT, datastore.toFile());
        PrimitiveData.selectControllerByName("Load Ephemeral Store");
        PrimitiveData.start();
        EntityCountSummary entityCountSummary = new LoadEntitiesFromProtobufFile(PB_STARTER_DATA).compute();
    }

    @AfterAll
    public void afterAll() {
        PrimitiveData.stop();
    }

    @Test
    @Order(1)
    public void createSessionTest() {
        State status = State.ACTIVE;
        long time = System.currentTimeMillis();
        Concept author = TinkarTerm.USER;
        Concept module = TinkarTerm.DEVELOPMENT_MODULE;
        Concept path = TinkarTerm.DEVELOPMENT_PATH;

        PublicId fqnId = PublicIds.of(UUID.nameUUIDFromBytes("fqnId".getBytes()));
        PublicId conceptId = PublicIds.of(UUID.nameUUIDFromBytes("conceptId".getBytes()));

        ComposerSession session = new ComposerSession(status, time, author, module, path);

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
    @Order(2)
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
    @Order(3)
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
    @Order(4)
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

}
