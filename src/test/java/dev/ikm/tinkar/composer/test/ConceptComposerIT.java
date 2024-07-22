package dev.ikm.tinkar.composer.test;

import dev.ikm.tinkar.common.id.PublicIds;
import dev.ikm.tinkar.common.service.CachingService;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.common.service.ServiceKeys;
import dev.ikm.tinkar.common.service.ServiceProperties;
import dev.ikm.tinkar.composer.Composer;
import dev.ikm.tinkar.composer.Session;
import dev.ikm.tinkar.composer.template.*;
import dev.ikm.tinkar.terms.EntityProxy.Concept;
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
//        EntityCountSummary entityCountSummary = new LoadEntitiesFromProtobufFile(PB_STARTER_DATA).compute();
    }

    @AfterAll
    public void afterAll() {
        PrimitiveData.stop();
    }

    @Test
    public void ConceptComposerTest() {
        Composer composer = new Composer("ConceptComposer Test");
        Session session = composer.open(DEFAULT_STATUS, DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);

        Concept myConcept = Concept.make(PublicIds.newRandom());

        session.compose(myConcept)
                .attach((FullyQualifiedName fqn) -> fqn.language(ENGLISH_LANGUAGE)
                        .text("Color")
                        .caseSignificance(DESCRIPTION_NOT_CASE_SENSITIVE)
                        .compose()
                        .attach((USDialect usDialect) -> usDialect.acceptability(PREFERRED))
                        .attach((GBDialect gbDialect) -> gbDialect.acceptability(ACCEPTABLE)
                                .compose()
                                .attach((Comment comment) -> comment.text("They spell things weird")
                                        .compose()
                                        .attach((Comment comment2) -> comment2.text("really really true!")))))
                .attach((Comment comment) -> comment.text("They spell things weird"));

        session.close();
    }
}
