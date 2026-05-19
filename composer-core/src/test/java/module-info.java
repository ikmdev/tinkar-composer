module dev.ikm.tinkar.composer.core.test {
	requires dev.ikm.tinkar.composer;
	requires dev.ikm.tinkar.common;
	requires dev.ikm.tinkar.terms;
	requires dev.ikm.tinkar.entity;
	requires org.eclipse.collections.api;
	requires org.eclipse.collections.impl;
	requires dev.ikm.tinkar.schema;
	requires org.slf4j;
	requires dev.ikm.jpms.protobuf;
	requires org.junit.jupiter.api;

	exports dev.ikm.tinkar.composer.core.test;
}