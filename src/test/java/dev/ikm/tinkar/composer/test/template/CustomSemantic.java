package dev.ikm.tinkar.composer.test.template;

import dev.ikm.tinkar.composer.SemanticTemplate;
import dev.ikm.tinkar.schema.Field;
import dev.ikm.tinkar.terms.EntityProxy;
import dev.ikm.tinkar.terms.TinkarTerm;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;

import java.util.List;

public class CustomSemantic extends SemanticTemplate {
	private String text;

	public CustomSemantic text(String text) {
		this.text = text;
		return this;
	}

	@Override
	public CustomSemantic semantic(EntityProxy.Semantic semantic) {
		this.setSemantic(semantic);
		return this;
	}

	@Override
	protected EntityProxy.Pattern assignPattern() {
		return TinkarTerm.COMMENT_PATTERN;
	}

	@Override
	protected List<Field> assignFieldValues() {
		return List.of(Field.newBuilder().setStringValue(text).build());
	}

	@Override
	protected void validate() {

	}
}
