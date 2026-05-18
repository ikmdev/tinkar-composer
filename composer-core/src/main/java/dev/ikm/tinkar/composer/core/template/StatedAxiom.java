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
package dev.ikm.tinkar.composer.core.template;

import dev.ikm.tinkar.composer.core.ChronologyBuilder;
import dev.ikm.tinkar.composer.core.SingleSemanticTemplate;
import dev.ikm.tinkar.schema.DiTree;
import dev.ikm.tinkar.schema.Field;
import dev.ikm.tinkar.schema.IntToIntMap;
import dev.ikm.tinkar.schema.IntToMultipleIntMap;
import dev.ikm.tinkar.schema.PublicId;
import dev.ikm.tinkar.schema.Vertex;
import dev.ikm.tinkar.schema.VertexUUID;
import dev.ikm.tinkar.terms.EntityProxy.Concept;
import dev.ikm.tinkar.terms.EntityProxy.Pattern;
import dev.ikm.tinkar.terms.EntityProxy.Semantic;
import dev.ikm.tinkar.terms.TinkarTerm;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class StatedAxiom extends SingleSemanticTemplate {

	public StatedAxiom() {
	}

	private static final int IDX_DEFINITION_ROOT = 0;
	private final AtomicInteger vertexIdx = new AtomicInteger(1); //Index zero reserved for DefinitionRoot
	private final List<Vertex> originVertices = new ArrayList<>();

	/**
	 * Adds an isA relationship for each Concept provided, denoting it as a parent of the referenced Component for the StatedAxiom Semantic.
	 *
	 * @param originConcepts the parent Concept
	 * @return the StatedAxiom SemanticTemplate for further method chaining
	 */
	public StatedAxiom isA(Concept... originConcepts) {
		for (Concept originConcept : originConcepts) {
			// Create Vertex
			Vertex vertex = createEntityVertex(vertexIdx.getAndIncrement(),
					TinkarTerm.CONCEPT_REFERENCE,
					TinkarTerm.CONCEPT_REFERENCE,
					originConcept);
			// Add to Vertex List
			originVertices.add(vertex);
		}
		return this;
	}

	@Override
	public StatedAxiom semantic(Semantic semantic) {
		this.setSemantic(semantic);
		return this;
	}

	@Override
	protected Pattern assignPattern() {
		return TinkarTerm.EL_PLUS_PLUS_STATED_AXIOMS_PATTERN;
	}

	@Override
	protected List<Field> assignFieldValues() {
		if (!originVertices.isEmpty()) {
			Field diTreeField = Field.newBuilder().setDiTree(constructDiTree()).build();
			return List.of(diTreeField);
		}
		return List.of();
	}

	@Override
	protected void validate() {
		if (originVertices.isEmpty()) {
			throw new IllegalArgumentException("StatedAxiom requires at least one origin vertex");
		}
	}

	private Vertex createEntityVertex(int index, Concept meaningConcept, Concept propertyKey, Concept propertyValue) {
		PublicId meaningId = ChronologyBuilder.createPublicId(meaningConcept);
		PublicId propertyKeyId = ChronologyBuilder.createPublicId(propertyKey);
		PublicId propertyValueId = ChronologyBuilder.createPublicId(propertyValue);

		// Create Property on Vertex that contains the pointer to the Parent
		Field conceptPropertyField = Field.newBuilder().setPublicId(propertyValueId).build();
		Vertex.Property referenceProperty = Vertex.Property.newBuilder()
				.setPublicId(propertyKeyId)
				.setField(conceptPropertyField)
				.build();

		// Build Vertex
		VertexUUID vertexUUID = VertexUUID.newBuilder().setUuid(UUID.randomUUID().toString()).build();
		return Vertex.newBuilder()
				.setVertexUuid(vertexUUID)
				.setIndex(index)
				.setMeaningPublicId(meaningId)
				.addProperties(referenceProperty)
				.build();
	}

	private Vertex createEntityVertex(int index, Concept meaningConcept) {
		PublicId meaningId = ChronologyBuilder.createPublicId(meaningConcept);
		VertexUUID vertexUUID = VertexUUID.newBuilder().setUuid(UUID.randomUUID().toString()).build();
		return Vertex.newBuilder()
				.setVertexUuid(vertexUUID)
				.setIndex(index)
				.setMeaningPublicId(meaningId)
				.addAllProperties(new ArrayList<Vertex.Property>())
				.build();
	}

	private DiTree constructDiTree() {
		// Setup
		List<Vertex> vertexMap = new ArrayList<>();
		List<IntToMultipleIntMap> successorMap = new ArrayList<>();
		List<IntToIntMap> predecessorMap = new ArrayList<>();
		List<Integer> originVertexIdxList = new ArrayList<>();
		int andIndex = vertexIdx.getAndIncrement();
		int necessarySetIndex = vertexIdx.getAndIncrement();

		//Construct Vertex Map
		Vertex root = createEntityVertex(IDX_DEFINITION_ROOT, TinkarTerm.DEFINITION_ROOT);
		vertexMap.add(root);
		for (Vertex originVertex : originVertices) {
			vertexMap.add(originVertex.getIndex(), originVertex);
			originVertexIdxList.add(originVertex.getIndex());
		}
		vertexMap.add(createEntityVertex(andIndex, TinkarTerm.AND));
		vertexMap.add(createEntityVertex(necessarySetIndex, TinkarTerm.NECESSARY_SET));

		//Construct Successor Map
		IntToMultipleIntMap intToMultipleIntMap = IntToMultipleIntMap.newBuilder().setSource(IDX_DEFINITION_ROOT).addTargets(necessarySetIndex).build();
		successorMap.add(intToMultipleIntMap);
		IntToMultipleIntMap andToOriginVertexMap = IntToMultipleIntMap.newBuilder().setSource(andIndex).addAllTargets(originVertexIdxList).build();
		successorMap.add(andToOriginVertexMap);
		IntToMultipleIntMap necessarySetToAndMap = IntToMultipleIntMap.newBuilder().setSource(necessarySetIndex).addTargets(andIndex).build();
		successorMap.add(necessarySetToAndMap);

		//Construct Predecessor Map
		for (int originIdx : originVertexIdxList) {
			IntToIntMap intToIntMap = IntToIntMap.newBuilder().setSource(originIdx).setTarget(andIndex).build();
			predecessorMap.add(intToIntMap);
		}
		IntToIntMap andToNecessarySetMap = IntToIntMap.newBuilder().setSource(andIndex).setTarget(necessarySetIndex).build();
		predecessorMap.add(andToNecessarySetMap);
		IntToIntMap necessarySetToDefinitionRootMap = IntToIntMap.newBuilder().setSource(necessarySetIndex).setTarget(IDX_DEFINITION_ROOT).build();
		predecessorMap.add(necessarySetToDefinitionRootMap);

		return DiTree.newBuilder()
				.addAllVertices(vertexMap)
				.setRoot(root.getIndex())
				.addAllPredecessorMap(predecessorMap)
				.addAllSuccessorMap(successorMap)
				.build();
	}

}
