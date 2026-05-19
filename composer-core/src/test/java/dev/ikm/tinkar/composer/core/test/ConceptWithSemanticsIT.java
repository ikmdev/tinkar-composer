package dev.ikm.tinkar.composer.core.test;

import dev.ikm.tinkar.common.id.PublicIds;
import dev.ikm.tinkar.common.service.CachingService;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.composer.core.Composer;
import dev.ikm.tinkar.composer.core.Session;
import dev.ikm.tinkar.composer.core.TransformRecord;
import dev.ikm.tinkar.composer.core.assembler.ConceptAssembler;
import dev.ikm.tinkar.composer.core.template.Definition;
import dev.ikm.tinkar.composer.core.template.FullyQualifiedName;
import dev.ikm.tinkar.composer.core.template.Identifier;
import dev.ikm.tinkar.composer.core.template.StatedAxiom;
import dev.ikm.tinkar.composer.core.template.Synonym;
import dev.ikm.tinkar.composer.core.template.USDialect;
import dev.ikm.tinkar.terms.EntityProxy.Concept;
import dev.ikm.tinkar.terms.State;
import dev.ikm.tinkar.terms.TinkarTermV2;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.File;
import java.nio.file.Path;
import java.util.function.Function;

import static dev.ikm.tinkar.terms.TinkarTermV2.AUTHOR;
import static dev.ikm.tinkar.terms.TinkarTermV2.DESCRIPTION_NOT_CASE_SENSITIVE;
import static dev.ikm.tinkar.terms.TinkarTermV2.DEVELOPMENT_MODULE;
import static dev.ikm.tinkar.terms.TinkarTermV2.DEVELOPMENT_PATH;
import static dev.ikm.tinkar.terms.TinkarTermV2.ENGLISH_LANGUAGE;
import static dev.ikm.tinkar.terms.TinkarTermV2.PREFERRED;
import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ConceptWithSemanticsIT {
	public static final Function<String, File> createFilePathInTarget = (pathName) -> new File("%s/target/%s".formatted(System.getProperty("user.dir"), pathName));
	public static final File packageOutput = createFilePathInTarget.apply("composer-package.zip");

	public static State DEFAULT_STATUS = State.ACTIVE;
	public static long DEFAULT_TIME = System.currentTimeMillis();
	public static Concept DEFAULT_AUTHOR = AUTHOR;
	public static Concept DEFAULT_MODULE = DEVELOPMENT_MODULE;
	public static Concept DEFAULT_PATH = DEVELOPMENT_PATH;


	@BeforeAll
	public void beforeAll() {
		CachingService.clearAll();
		PrimitiveData.selectControllerByName("Load Ephemeral Store");
		PrimitiveData.start();
	}

	@AfterAll
	public void afterAll() {
		PrimitiveData.stop();
	}

	// ### START: Creation Tests Basic
	@Test
	public void createConceptWithPublicIdTest() throws Exception {
		Path conceptPackagePath = createFilePathInTarget.apply("concept-with-public-id.zip").toPath();
		try (Composer composer = new Composer(conceptPackagePath, "ConceptWithPublicId")) {
			Session session = composer.open(DEFAULT_STATUS, DEFAULT_TIME, DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);

			Concept concept = Concept.make(PublicIds.newRandom());
			session.compose((ConceptAssembler conceptAssembler) -> conceptAssembler.concept(concept));

			TransformRecord record = composer.getTransformRecord();
			assertEquals(1, record.conceptsCreated(), "Concepts created should be 1");
			assertEquals(0, record.semanticsCreated(), "Semantics created should be 1");
			assertEquals(0, record.patternsCreated(), "Patterns created should be 1");
			assertEquals(1, record.stampsCreated(), "Stamps created should be 1");
			assertEquals(0, record.errorCount(), "Errors should be 0");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Test
	public void createConceptWithSemanticsTest() throws Exception {
		Path conceptWithSemanticsPath = createFilePathInTarget.apply("concept-with-semantics.zip").toPath();
		try (Composer composer = new Composer(conceptWithSemanticsPath, "ConceptWithSemantics")) {
			Session session = composer.open(DEFAULT_STATUS, DEFAULT_TIME, DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);
			Concept concept = Concept.make(PublicIds.newRandom());
			session.compose((ConceptAssembler conceptAssembler) -> conceptAssembler.concept(concept))
					.attach(FullyQualifiedName.class, fqn -> fqn
							.language(ENGLISH_LANGUAGE)
							.text("Test Composer Concept")
							.caseSignificance(DESCRIPTION_NOT_CASE_SENSITIVE)
							.attach(USDialect.class, usDialect -> usDialect.acceptability(PREFERRED)))
					.attach(Synonym.class, syn -> syn
							.language(ENGLISH_LANGUAGE)
							.text("Composer Concept")
							.caseSignificance(DESCRIPTION_NOT_CASE_SENSITIVE)
							.attach(USDialect.class, usDialect -> usDialect.acceptability(PREFERRED)))
					.attach(Definition.class, def -> def
							.language(ENGLISH_LANGUAGE)
							.text("This is a test concept created by the Tinkar Composer")
							.caseSignificance(DESCRIPTION_NOT_CASE_SENSITIVE)
							.attach(USDialect.class, usDialect -> usDialect.acceptability(PREFERRED)))
					.attach(Identifier.class, identifier -> identifier
							.source(TinkarTermV2.IDENTIFIER_SOURCE)
							.identifier("1337"))
					.attach(StatedAxiom.class, axiom -> axiom
							.isA(TinkarTermV2.PHENOMENON));
			TransformRecord record = composer.getTransformRecord();

			assertEquals(1, record.conceptsCreated(), "Concepts created should be 1");
			assertEquals(8, record.semanticsCreated(), "Semantics created should be 1");
			assertEquals(0, record.patternsCreated(), "Patterns created should be 1");
			assertEquals(1, record.stampsCreated(), "Stamps created should be 1");
			assertEquals(0, record.errorCount(), "Errors should be 0");

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
