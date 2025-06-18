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
package dev.ikm.tinkar.composer.template;

import dev.ikm.tinkar.composer.SingleSemanticTemplate;
import dev.ikm.tinkar.entity.graph.DiTreeEntity;
import dev.ikm.tinkar.entity.graph.EntityVertex;
import dev.ikm.tinkar.terms.ConceptFacade;
import dev.ikm.tinkar.terms.EntityProxy.Concept;
import dev.ikm.tinkar.terms.EntityProxy.Pattern;
import dev.ikm.tinkar.terms.EntityProxy.Semantic;
import dev.ikm.tinkar.terms.TinkarTerm;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.primitive.IntIntMaps;
import org.eclipse.collections.api.factory.primitive.IntLists;
import org.eclipse.collections.api.factory.primitive.IntObjectMaps;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.api.list.primitive.ImmutableIntList;
import org.eclipse.collections.api.list.primitive.MutableIntList;
import org.eclipse.collections.api.map.primitive.MutableIntIntMap;
import org.eclipse.collections.api.map.primitive.MutableIntObjectMap;

import java.util.concurrent.atomic.AtomicInteger;

import static dev.ikm.tinkar.entity.graph.EntityVertex.abstractObject;

public class StatedAxiom extends SingleSemanticTemplate {
    private static final int IDX_DEFINITION_ROOT = 0;
    private final AtomicInteger vertexIdx = new AtomicInteger(1); //Index zero reserved for DefinitionRoot
    private final MutableList<EntityVertex> originVertexList = Lists.mutable.empty();

    /**
     * Adds an isA relationship for each Concept provided, denoting it as a parent of the referenced Component for the StatedAxiom Semantic.
     * @param originConcepts the parent Concept
     * @return the StatedAxiom SemanticTemplate for further method chaining
     */
    public StatedAxiom isA(Concept... originConcepts) {
        for (Concept originConcept : originConcepts) {
            MutableIntObjectMap<Object> referenceProperty = IntObjectMaps.mutable.empty();
            referenceProperty.put(TinkarTerm.CONCEPT_REFERENCE.nid(), abstractObject(originConcept));

            EntityVertex originVertex = EntityVertex.make(originConcept);
            originVertex.setProperties(referenceProperty);
            originVertex.setVertexIndex(vertexIdx.getAndIncrement());
            originVertex.setMeaningNid(TinkarTerm.CONCEPT_REFERENCE.nid());
            originVertexList.with(originVertex);
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
    protected ImmutableList<Object> assignFieldValues() {
        MutableList<Object> result = Lists.mutable.empty();
        if (originVertexList.notEmpty()) {
            result.add(constructDiTree());
        }
        return result.toImmutable();
    }

    @Override
    protected void validate() {
        if (originVertexList.isEmpty()) {
            throw new IllegalArgumentException("StatedAxiom requires at least one origin vertex");
        }
    }

    private EntityVertex createEntityVertex(int vertexIndex, ConceptFacade conceptFacade){
        MutableIntObjectMap<Object> properties =  IntObjectMaps.mutable.empty();
        EntityVertex entityVertex = EntityVertex.make(conceptFacade);
        entityVertex.setVertexIndex(vertexIndex);
        entityVertex.setProperties(properties);
        return entityVertex;
    }

    private DiTreeEntity constructDiTree() {
        //Setup
        MutableList<EntityVertex> vertexMap = Lists.mutable.empty();
        MutableIntObjectMap<ImmutableIntList> successorMap = IntObjectMaps.mutable.empty();
        MutableIntIntMap predecessorMap = IntIntMaps.mutable.empty();
        MutableIntList originVertexIdxList = IntLists.mutable.empty();
        int andIndex = vertexIdx.getAndIncrement();
        int necessarySetIndex = vertexIdx.getAndIncrement();

        //Construct Vertex Map
        EntityVertex definitionRootVertex = createEntityVertex(IDX_DEFINITION_ROOT, TinkarTerm.DEFINITION_ROOT);
        vertexMap.add(definitionRootVertex);
        for (EntityVertex originVertex : originVertexList) {
            vertexMap.add(originVertex.vertexIndex(), originVertex);
            originVertexIdxList.add(originVertex.vertexIndex());
        }
        vertexMap.add(createEntityVertex(andIndex, TinkarTerm.AND));
        vertexMap.add(createEntityVertex(necessarySetIndex, TinkarTerm.NECESSARY_SET));

        //Construct Successor Map
        successorMap.put(IDX_DEFINITION_ROOT, IntLists.immutable.of(necessarySetIndex));
        successorMap.put(andIndex, originVertexIdxList.toImmutable());
        successorMap.put(necessarySetIndex, IntLists.immutable.of(andIndex));

        //Construct Predecessor Map
        for (int originIdx : originVertexIdxList.toArray()) {
            predecessorMap.put(originIdx, andIndex);
        }
        predecessorMap.put(andIndex, necessarySetIndex);
        predecessorMap.put(necessarySetIndex, IDX_DEFINITION_ROOT);

        return new DiTreeEntity(definitionRootVertex, vertexMap.toImmutable(), successorMap.toImmutable(), predecessorMap.toImmutable());
    }

}
