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
package dev.ikm.tinkar.composer.test;

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
import dev.ikm.tinkar.composer.template.Comment;
import dev.ikm.tinkar.composer.template.FullyQualifiedName;
import dev.ikm.tinkar.composer.template.GBDialect;
import dev.ikm.tinkar.composer.template.USDialect;
import dev.ikm.tinkar.composer.test.template.CustomSemantic;
import dev.ikm.tinkar.terms.EntityProxy.Concept;
import dev.ikm.tinkar.terms.EntityProxy.Pattern;
import dev.ikm.tinkar.terms.EntityProxy.Semantic;
import dev.ikm.tinkar.terms.State;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Function;

import static dev.ikm.tinkar.terms.TinkarTerm.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ConceptComposerIT {

    public static final Function<String,File> createFilePathInTarget = (pathName) -> new File("%s/target/%s".formatted(System.getProperty("user.dir"), pathName));
    public static final File PB_STARTER_DATA = createFilePathInTarget.apply("data/tinkar-starter-data-1.0.0-pb.zip");
    public static State DEFAULT_STATUS = State.ACTIVE;
    public static long DEFAULT_TIME = System.currentTimeMillis();
    public static Concept DEFAULT_AUTHOR = USER;
    public static Concept DEFAULT_MODULE = DEVELOPMENT_MODULE;
    public static Concept DEFAULT_PATH = DEVELOPMENT_PATH;

    private final Path datastore = Path.of(System.getProperty("user.dir"))
            .resolve("target")
            .resolve(ConceptComposerIT.class.getSimpleName())
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
//        EntityCountSummary entityCountSummary = new LoadEntitiesFromProtobufFile(PB_STARTER_DATA).compute();
    }

    @AfterAll
    public void afterAll() {
        PrimitiveData.stop();
    }

    @Test
    public void ConceptComposerTest() {
        Composer composer = new Composer("ConceptComposerTest");
        Session session = composer.open(DEFAULT_STATUS, DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);

        Concept myConcept = Concept.make(PublicIds.newRandom());

        session.compose((ConceptAssembler conceptAssembler) -> conceptAssembler.concept(myConcept))
                .attach((Comment c1) -> c1.text("comment 1")
                        .attach((Comment c2) -> c2.text("comment 2")
                                .attach((Comment c3) -> c3.text("comment 3")
                                        .attach((Comment c4) -> c4.text("Comment 4")
                                                .attach((Comment c5) -> c5.text("Comment 5"))
                                                .attach(new Comment().text("Comment 6")))
                                        .attach((Comment c7) -> c7.text("comment 7")))));


        session.compose((ConceptAssembler conceptAssembler) -> conceptAssembler.publicId(PublicIds.newRandom()))
                .attach((FullyQualifiedName fqn) -> fqn.language(ENGLISH_LANGUAGE)
                        .text("Color")
                        .caseSignificance(DESCRIPTION_NOT_CASE_SENSITIVE)
                        .attach((USDialect usDialect) -> usDialect.acceptability(PREFERRED))
                        .attach((GBDialect gbDialect) -> gbDialect
                                .acceptability(ACCEPTABLE)
                                .attach((Comment comment) -> comment.text("Comment 1")
                                        .attach((Comment comment2) -> comment2.text("Comment 2"))
                                                .attach(new CustomSemantic().text("Custom Semantic"))))
                        .attach(new Comment().text("Comment 3")))
                .attach((Comment comment) -> comment.text("They spell things weird"))
                .attach(new CustomSemantic().text("Custom Comments"));

        Semantic semantic = Semantic.make(PublicIds.newRandom());
        session.compose((SemanticAssembler semanticAssembler) -> semanticAssembler.semantic(semantic)
                    .pattern(DESCRIPTION_PATTERN)
                    .reference(myConcept)
                    .fieldValues(fieldValue -> System.out.println(fieldValue))
                    .attach((USDialect usDialect) -> usDialect.acceptability(PREFERRED)
                        .attach((Comment comment) -> comment.text("Comment 1"))));

        session.compose(new FullyQualifiedName()
                .language(ENGLISH_LANGUAGE)
                .text("Color")
                .caseSignificance(DESCRIPTION_NOT_CASE_SENSITIVE), myConcept)
                .attach((USDialect usDialect) -> usDialect.acceptability(PREFERRED)
                        .attach((Comment comment) -> comment.text("Comment 1")));

        Pattern pattern = Pattern.make(PublicIds.newRandom());
        session.compose((PatternAssembler patternAssembler) -> patternAssembler.pattern(pattern)
                .meaning(MEANING)
                .purpose(PURPOSE)
                .fieldDefinition(MEANING, PURPOSE, STRING)
                .attach((FullyQualifiedName fqn) -> fqn.language(ENGLISH_LANGUAGE)
                        .text("FQN")
                        .caseSignificance(DESCRIPTION_NOT_CASE_SENSITIVE)));

        session.close();
    }
}
