package dev.ikm.tinkar.composer.test;

import dev.ikm.tinkar.common.id.PublicIds;
import dev.ikm.tinkar.common.service.CachingService;
import dev.ikm.tinkar.common.service.PrimitiveData;
import dev.ikm.tinkar.composer.Composer;
import dev.ikm.tinkar.composer.Session;
import dev.ikm.tinkar.composer.assembler.ConceptAssembler;
import dev.ikm.tinkar.composer.template.Definition;
import dev.ikm.tinkar.composer.template.FullyQualifiedName;
import dev.ikm.tinkar.composer.template.Identifier;
import dev.ikm.tinkar.composer.template.StatedAxiom;
import dev.ikm.tinkar.composer.template.Synonym;
import dev.ikm.tinkar.composer.template.USDialect;
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

import static dev.ikm.tinkar.terms.TinkarTerm.DESCRIPTION_NOT_CASE_SENSITIVE;
import static dev.ikm.tinkar.terms.TinkarTerm.DEVELOPMENT_MODULE;
import static dev.ikm.tinkar.terms.TinkarTerm.DEVELOPMENT_PATH;
import static dev.ikm.tinkar.terms.TinkarTerm.ENGLISH_LANGUAGE;
import static dev.ikm.tinkar.terms.TinkarTerm.PREFERRED;
import static dev.ikm.tinkar.terms.TinkarTerm.USER;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ComposeConceptIT {

	public static final Function<String, File> createFilePathInTarget = (pathName) -> new File("%s/target/%s".formatted(System.getProperty("user.dir"), pathName));
	public static final File packageOutput = createFilePathInTarget.apply("composer-package.zip");

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
	}

	@AfterAll
	public void afterAll() {
		PrimitiveData.stop();
	}

	// ### START: Creation Tests Basic
	@Test
	public void createConceptWithPublicIdTest() throws Exception {
		Path conceptPackagePath = createFilePathInTarget.apply("concept-with-public-id.zip").toPath();
		try (Composer composer = new Composer(conceptPackagePath)) {
			Session session = composer.open(DEFAULT_STATUS, DEFAULT_TIME, DEFAULT_AUTHOR, DEFAULT_MODULE, DEFAULT_PATH);

			Concept concept = Concept.make(PublicIds.newRandom());
			session.compose((ConceptAssembler conceptAssembler) -> conceptAssembler.concept(concept));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Test
	public void createConceptWithSemanticsTest() throws Exception {
		Path conceptWithSemanticsPath = createFilePathInTarget.apply("concept-with-semantics.zip").toPath();
		try (Composer composer = new Composer(conceptWithSemanticsPath)) {
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
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
