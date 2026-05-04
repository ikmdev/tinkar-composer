package dev.ikm.tinkar.composer;

import dev.ikm.tinkar.common.id.PublicId;
import dev.ikm.tinkar.entity.ConceptEntity;
import dev.ikm.tinkar.entity.Entity;
import dev.ikm.tinkar.entity.EntityVersion;
import dev.ikm.tinkar.entity.PatternEntity;
import dev.ikm.tinkar.entity.SemanticEntity;
import dev.ikm.tinkar.entity.StampEntity;
import dev.ikm.tinkar.entity.export.ExportEntitiesToProtobufFile;
import dev.ikm.tinkar.entity.transform.EntityToTinkarSchemaTransformer;
import dev.ikm.tinkar.schema.TinkarMsg;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.LongAdder;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ChangeSetWriter {

	private final EntityToTinkarSchemaTransformer transformer;
	private final Path changeSetFile;
	private final LongAdder entityCount;
	private final LongAdder conceptsCount;
	private final LongAdder semanticsCount;
	private final LongAdder patternsCount;
	private final LongAdder stampsCount;
	private final Set<PublicId> moduleSet;
	private final Set<PublicId> authorSet;

	public ChangeSetWriter(Path changeSetFile) {
		this.changeSetFile = changeSetFile;
		this.entityCount = new LongAdder();
		this.conceptsCount = new LongAdder();
		this.semanticsCount = new LongAdder();
		this.patternsCount = new LongAdder();
		this.stampsCount = new LongAdder();
		this.moduleSet = new HashSet<>();
		this.authorSet = new HashSet<>();
		this.transformer = EntityToTinkarSchemaTransformer.getInstance();
	}

	public void writeChangeSet(List<Entity<EntityVersion>> entities) {
		try (FileOutputStream fos = new FileOutputStream(changeSetFile.toFile());
			 BufferedOutputStream bos = new BufferedOutputStream(fos);
			 ZipOutputStream zos = new ZipOutputStream(bos)) {
			// Create a single entry for all changes in this zip file
			final ZipEntry zipEntry = new ZipEntry("Entities");
			zos.putNextEntry(zipEntry);
			try {
				for (Entity<EntityVersion> entity : entities) {
					collectEntityCounts(entity);
					writeEntity(entity, zos);
					writeManifest(zos);
					zos.flush();
					zos.finish();
				}
			} finally {
				zos.closeEntry();
				// zos will be closed automatically by the try-with-resources
			}
		} catch (IOException e) {
			e.printStackTrace();
			// Handle exception appropriately
			// For example, you might want to log the error or rethrow it as a runtime exception
		}
	}

	private void collectEntityCounts(Entity<EntityVersion> entity) {
		entityCount.increment();
		switch (entity) {
			case ConceptEntity _ -> conceptsCount.increment();
			case SemanticEntity _ -> semanticsCount.increment();
			case PatternEntity _ -> patternsCount.increment();
			case StampEntity stampEntity -> {
				stampsCount.increment();
				// Store Module & Author Dependencies for Manifest
				moduleSet.add(stampEntity.module().publicId());
				authorSet.add(stampEntity.author().publicId());
			}
			default -> throw new IllegalStateException("Unexpected value: " + entity);
		}
	}

	private void writeEntity(Entity<EntityVersion> entity, ZipOutputStream zos) throws IOException {
		TinkarMsg msgEntity = transformer.transform(entity);
		msgEntity.writeDelimitedTo(zos);
	}

	private void writeManifest(ZipOutputStream zos) throws IOException {
		String manifestString = ExportEntitiesToProtobufFile.generateManifestContent(entityCount.sum(),
				conceptsCount.sum(),
				semanticsCount.sum(),
				patternsCount.sum(),
				stampsCount.sum(),
				moduleSet,
				authorSet);
		// Write Manifest File
		final ZipEntry manifestEntry = new ZipEntry("META-INF/MANIFEST.MF");
		zos.putNextEntry(manifestEntry);
		zos.write(manifestString.getBytes(StandardCharsets.UTF_8));
		zos.closeEntry();
	}

}
