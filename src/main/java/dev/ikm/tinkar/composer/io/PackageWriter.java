package dev.ikm.tinkar.composer.io;

import dev.ikm.tinkar.schema.TinkarMsg;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class PackageWriter implements AutoCloseable {

	private final Manifest manifest;
	private final ZipOutputStream zos;
	private boolean isFirstWrite = true;

	public PackageWriter(Path packageFile) throws FileNotFoundException {
		manifest = new Manifest();
		FileOutputStream fos = new FileOutputStream(packageFile.toFile());
		BufferedOutputStream bos = new BufferedOutputStream(fos);
		this.zos = new ZipOutputStream(bos);
	}

	public void writeToPackage(TinkarMsg tinkarMsg) {
		try {
			// Need to create initial ZipEntry for Entities
			if (isFirstWrite) {
				isFirstWrite = false;
				ZipEntry zipEntry = new ZipEntry("Entities");
				zos.putNextEntry(zipEntry);
			}
			// Start collecting Manifest Information
			collectManifestInformation(tinkarMsg);
			// Write TinkarMsg to Package Entities Zip Entry
			tinkarMsg.writeDelimitedTo(zos);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void collectManifestInformation(TinkarMsg tinkarMsg) {
		manifest.addEntityCount(1);
		if (tinkarMsg.hasConceptChronology()) {
			manifest.addConceptCount(1);
		} else if (tinkarMsg.hasSemanticChronology()) {
			manifest.addSemanticCount(1);
		} else if (tinkarMsg.hasPatternChronology()) {
			manifest.addPatternCount(1);
		} else if (tinkarMsg.hasStampChronology()) {
			manifest.addStampCount(1);
			// Store Module & Author Dependencies for Manifest
			manifest.addModule(tinkarMsg.getStampChronology().getFirstStampVersion().getModulePublicId());
			manifest.addAuthor(tinkarMsg.getStampChronology().getFirstStampVersion().getAuthorPublicId());
		}
	}

	private void writePackageManifest() throws IOException {
		// Close the Entities Zip Entry
		zos.closeEntry();
		// Write the Manifest Zip Entry
		final ZipEntry manifestEntry = new ZipEntry("META-INF/MANIFEST.MF");
		String manifestString = manifest.manifestString();
		zos.putNextEntry(manifestEntry);
		zos.write(manifestString.getBytes(StandardCharsets.UTF_8));
		zos.closeEntry();
	}

	@Override
	public void close() throws Exception {
		writePackageManifest();
		zos.close();
	}
}
