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

import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SnomedTransformIT {
//    public static final Function<String,File> createFilePathInTarget = (pathName) -> new File("%s/target/%s".formatted(System.getProperty("user.dir"), pathName));
//    public static final File PB_STARTER_DATA = createFilePathInTarget.apply("data/tinkar-starter-data-20250915-reasoned-pb.zip");
//    public static final File DATASTORE = createFilePathInTarget.apply("generated-data/" + SnomedTransformIT.class.getSimpleName());
//    public final Composer COMPOSER_SESSION_MANAGER = new Composer();
//
//    private final Path datastore = Path.of(System.getProperty("user.dir"))
//            .resolve("target")
//            .resolve(SnomedTransformIT.class.getSimpleName())
//            .resolve("datastore");
//
//    @BeforeAll
//    public void beforeAll() throws IOException {
////        Files.createDirectories(datastore);
//        CachingService.clearAll();
////        ServiceProperties.set(ServiceKeys.DATA_STORE_ROOT, datastore.toFile());
//        ServiceProperties.set(ServiceKeys.DATA_STORE_ROOT, DATASTORE);
////        PrimitiveData.selectControllerByName("Load Ephemeral Store");
//        PrimitiveData.selectControllerByName("Open SpinedArrayStore");
//        PrimitiveData.start();
//        EntityCountSummary entityCountSummary = new LoadEntitiesFromProtobufFile(PB_STARTER_DATA).compute();
//        loadSnomedStarterData();
//    }
//
//    @AfterAll
//    public void afterAll() {
//        PrimitiveData.stop();
//    }
//
//    @Test
//    @Disabled
//    public void snomedTransformationTest() {
//        String snomedFilePathString = System.getenv("SNOMED_DATA_FILE");
//        if (snomedFilePathString == null) {
//            fail("Error: No Snomed data. Please configure SNOMED_DATA_FILE environment variable with the path to a valid Snomed zip file to run this test.");
//        }
//        Map<File, SnomedFileType> snomedFileTypeMap = processSnomedFileTypes(new File(snomedFilePathString));
//
//        snomedFileTypeMap.forEach((inputFile, fileType) -> {
//            switch(fileType) {
//                case CONCEPT -> processConceptFile(inputFile);
//                case DESCRIPTION -> processDescriptionFile(inputFile);
//            }
//        });
//        COMPOSER_SESSION_MANAGER.closeAllSessions();
//    }
//
//    private void loadSnomedStarterData() {
//        State status = State.ACTIVE;
//        long time = PrimitiveData.PREMUNDANE_TIME;
//        Concept author = TinkarTerm.USER;
//        Concept module = TinkarTerm.PRIMORDIAL_MODULE;
//        Concept path = TinkarTerm.PRIMORDIAL_PATH;
//
//        ComposerSession session = COMPOSER_SESSION_MANAGER.makeSession(status, time, author, module, path);
//
//        Concept snomedAuthor = Concept.make("IHTSDO SNOMED CT Author", UUID.nameUUIDFromBytes("IHTSDO SNOMED CT Author".getBytes()));
//        session.composeConcept(snomedAuthor)
//                .with(new FullyQualifiedName(Semantic.make(PublicIds.newRandom()), TinkarTerm.ENGLISH_LANGUAGE, "IHTSDO SNOMED CT Author", TinkarTerm.DESCRIPTION_CASE_SENSITIVE)
//                        .with(new USEnglishDialect(Semantic.make(PublicIds.newRandom()),TinkarTerm.PREFERRED)))
//                .with(new Synonym(Semantic.make(PublicIds.newRandom()), TinkarTerm.ENGLISH_LANGUAGE, "SNOMED CT Author", TinkarTerm.DESCRIPTION_NOT_CASE_SENSITIVE)
//                        .with(new USEnglishDialect(Semantic.make(PublicIds.newRandom()),TinkarTerm.PREFERRED)))
//                .with(new Definition(Semantic.make(PublicIds.newRandom()), TinkarTerm.ENGLISH_LANGUAGE, "International Health Terminology Standards Development Organisation (IHTSDO) SNOMED CT Author", TinkarTerm.DESCRIPTION_NOT_CASE_SENSITIVE)
//                        .with(new USEnglishDialect(Semantic.make(PublicIds.newRandom()),TinkarTerm.PREFERRED)));
//
//        Concept healthConcept = Concept.make("", TinkarTerm.HEALTH_CONCEPT.publicId());
//        session.composeConcept(healthConcept);
//
////        Concept snomedModule = Concept.make("IHTSDO SNOMED CT Module", UuidUtil.fromSNOMED().nameUUIDFromBytes("IHTSDO SNOMED CT Module".getBytes());
////        session.composeConcept(snomedModule)
////                .with(new FullyQualifiedName(Semantic.make(PublicIds.newRandom()), TinkarTerm.ENGLISH_LANGUAGE, "IHTSDO SNOMED CT Module", TinkarTerm.DESCRIPTION_CASE_SENSITIVE)
////                        .with(new USEnglishDialect(Semantic.make(PublicIds.newRandom()),TinkarTerm.PREFERRED)))
////                .with(new Synonym(Semantic.make(PublicIds.newRandom()), TinkarTerm.ENGLISH_LANGUAGE, "SNOMED CT Module", TinkarTerm.DESCRIPTION_NOT_CASE_SENSITIVE)
////                        .with(new USEnglishDialect(Semantic.make(PublicIds.newRandom()),TinkarTerm.PREFERRED)))
////                .with(new Definition(Semantic.make(PublicIds.newRandom()), TinkarTerm.ENGLISH_LANGUAGE, "International Health Terminology Standards Development Organisation (IHTSDO) SNOMED CT Module", TinkarTerm.DESCRIPTION_NOT_CASE_SENSITIVE)
////                        .with(new USEnglishDialect(Semantic.make(PublicIds.newRandom()),TinkarTerm.PREFERRED)));
//
//        COMPOSER_SESSION_MANAGER.closeSession(session);
//    }
//
//    private enum SnomedFileType {
//        CONCEPT,
//        DEFINITION,
//        DESCRIPTION,
//        LANGUAGE,
//        AXIOM,
//        NO_OP;
//
//        static SnomedFileType fromFile(File file) {
//            String fileName = file.getName();
//            if(fileName.contains("Concept")) {
//                return CONCEPT;
//            } else if(fileName.contains("Definition")) {
//                return DEFINITION;
//            } else if(fileName.contains("Description")) {
//                return DESCRIPTION;
//            } else if(fileName.contains("Language")) {
//                return LANGUAGE;
//            } else if(fileName.contains("OWLExpression")) {
//                return AXIOM;
//            } else {
//                return NO_OP;
//            }
//        }
//    }
//
//    private static Map<File, SnomedFileType> processSnomedFileTypes(File inputFileOrDirectory) {
//        if(!inputFileOrDirectory.exists()){
//            throw new RuntimeException("Invalid input directory or file. Directory or file does not exist");
//        }
//
//        Map<File, SnomedFileType> snomedFileTypeMap = new HashMap<>();
//
//        //Process all the text files inside the directory.
//        if(inputFileOrDirectory.isDirectory()){
//            Arrays.stream(inputFileOrDirectory.listFiles()).filter(p -> p.getName().endsWith(".txt")).forEach(file -> {
//                SnomedFileType snomedFileType = SnomedFileType.fromFile(file);
//                if (snomedFileType != SnomedFileType.NO_OP) {
//                    snomedFileTypeMap.put(file, snomedFileType);
//                }
//            });
//        }else if(inputFileOrDirectory.isFile()
//                && inputFileOrDirectory.getName().endsWith(".txt")){
//            SnomedFileType snomedFileType = SnomedFileType.fromFile(inputFileOrDirectory);
//            if (snomedFileType != SnomedFileType.NO_OP) {
//                snomedFileTypeMap.put(inputFileOrDirectory, snomedFileType);
//            }
//        }
//
//        return snomedFileTypeMap;
//    }
//
//    private void processConceptFile(File inputFile) {
//        int ID = 0;
//        int EFFECTIVE_TIME = 1;
//        int ACTIVE = 2;
//        int MODULE_ID = 3;
//        int DEFINITION_STATUS_ID = 4;
//
//        Concept author = SnomedTestUtility.getUserConcept();
//        Concept path = SnomedTestUtility.getPathConcept();
//
//        try (Stream<String> lines = Files.lines(inputFile.toPath())) {
//            lines.skip(1) //skip first line, i.e. header line
//                    .map(row -> row.split("\t"))
//                    .forEach(data -> {
//                        State status = Integer.parseInt(data[ACTIVE]) == 1 ? State.ACTIVE : State.INACTIVE;
//                        long time = SnomedTestUtility.snomedTimestampToEpochSeconds(data[EFFECTIVE_TIME]);
//                        Concept module = Concept.make(PublicIds.of(UuidUtil.fromSNOMED(data[MODULE_ID])));
//
//                        ComposerSession session = COMPOSER_SESSION_MANAGER.makeSession(status, time, author, module, path);
//                        session.composeConcept(Concept.make(PublicIds.of(UuidUtil.fromSNOMED(data[ID]))));
//                    });
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    private void processDescriptionFile(File inputFile) {
//        int ID = 0;
//        int EFFECTIVE_TIME = 1;
//        int ACTIVE = 2;
//        int MODULE_ID = 3;
//        int CONCEPT_ID = 4;
//        int LANGUAGE_CODE = 5;
//        int TYPE_ID = 6;
//        int TERM = 7;
//        int CASE_SIGNIFICANCE = 8;
//
//        Concept author = SnomedTestUtility.getUserConcept();
//        Concept path = SnomedTestUtility.getPathConcept();
//
//        try (Stream<String> lines = Files.lines(inputFile.toPath())) {
//            lines.skip(1) //skip first line, i.e. header line
//                    .map(row -> row.split("\t"))
//                    .forEach(data -> {
//                        State status = Integer.parseInt(data[ACTIVE]) == 1 ? State.ACTIVE : State.INACTIVE;
//                        long time = SnomedTestUtility.snomedTimestampToEpochSeconds(data[EFFECTIVE_TIME]);
//                        Concept module = Concept.make(PublicIds.of(UuidUtil.fromSNOMED(data[MODULE_ID])));
//
//                        Concept referencedComponent = Concept.make(PublicIds.of(UuidUtil.fromSNOMED(data[CONCEPT_ID])));
//                        Concept descriptionType = SnomedTestUtility.getDescriptionType(data[TYPE_ID]);
//                        Concept languageType = SnomedTestUtility.getLanguageConcept(data[LANGUAGE_CODE]);
//                        Concept caseSensitivityConcept = SnomedTestUtility.getDescriptionCaseSignificanceConcept(data[CASE_SIGNIFICANCE]);
//
//                        ComposerSession session = COMPOSER_SESSION_MANAGER.makeSession(status, time, author, module, path);
//                        session.composeSemantic(Semantic.make(PublicIds.of(UuidUtil.fromSNOMED(data[ID]))),
//                                referencedComponent,
//                                TinkarTerm.DESCRIPTION_PATTERN,
//                                Lists.immutable.of(
//                                        languageType,
//                                        data[TERM],
//                                        caseSensitivityConcept,
//                                        descriptionType
//                                ));
//                    });
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }

}
