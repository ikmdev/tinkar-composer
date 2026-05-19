package dev.ikm.tinkar.composer.core;

import java.time.Duration;

public record TransformRecord(
		String standardName,
		long conceptsCreated,
		long semanticsCreated,
		long patternsCreated,
		long stampsCreated,
		Duration duration,
		long errorCount
) {
	// The Builder is a static nested class within the record.
	public static class Builder {
		private final String standardName;
		private long conceptsCreated = 0;
		private long semanticsCreated = 0;
		private long patternsCreated = 0;
		private long stampsCreated = 0;
		private long errorCount = 0;
		private final long startTime = System.nanoTime();

		public Builder(String standardName) {
			this.standardName = standardName;
		}

		// Synchronized methods to allow for concurrent updates if needed
		public synchronized void incrementConcepts() { this.conceptsCreated++; }
		public synchronized void incrementSemantics() { this.semanticsCreated++; }
		public synchronized void incrementPatterns() { this.patternsCreated++; }
		public synchronized void incrementStamps() { this.stampsCreated++; }
		public synchronized void incrementErrors() { this.errorCount++; }

		public TransformRecord build() {
			Duration duration = Duration.ofNanos(System.nanoTime() - startTime);
			return new TransformRecord(
					standardName,
					conceptsCreated,
					semanticsCreated,
					patternsCreated,
					stampsCreated,
					duration,
					errorCount
			);
		}
	}
}
