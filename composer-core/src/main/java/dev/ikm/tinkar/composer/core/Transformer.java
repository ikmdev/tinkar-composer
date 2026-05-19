package dev.ikm.tinkar.composer.core;

import java.util.concurrent.Future;

public interface Transformer {

	String getStandardName();

	Future<TransformRecord> transform(Composer composer);
}
