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

import dev.ikm.tinkar.common.id.IntIdSet;
import dev.ikm.tinkar.common.id.IntIds;
import dev.ikm.tinkar.composer.SemanticTemplate;
import dev.ikm.tinkar.terms.EntityProxy;
import dev.ikm.tinkar.terms.EntityProxy.Pattern;
import dev.ikm.tinkar.terms.EntityProxy.Semantic;
import dev.ikm.tinkar.terms.TinkarTerm;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;

import java.util.Collections;

public class StatedNavigation extends SemanticTemplate {
    private final MutableList<EntityProxy> origins = Lists.mutable.empty();
    private final MutableList<EntityProxy> destinations = Lists.mutable.empty();

    /**
     * Adds parents to the referenced Component for the StatedNavigation Semantic.
     * @param originConcepts the list of parent Concepts
     * @return the StatedNavigation SemanticTemplate for further method chaining
     */
    public StatedNavigation parents(EntityProxy.Concept... originConcepts) {
        Collections.addAll(origins, originConcepts);
        return this;
    }

    /**
     * Adds children to the referenced Component for the StatedNavigation Semantic.
     * @param destinationConcepts the list of parent Concepts
     * @return the StatedNavigation SemanticTemplate for further method chaining
     */
    public StatedNavigation children(EntityProxy.Concept... destinationConcepts) {
        Collections.addAll(destinations, destinationConcepts);
        return this;
    }

    @Override
    public StatedNavigation semantic(Semantic semantic) {
        this.setSemantic(semantic);
        return this;
    }

    @Override
    protected Pattern assignPattern() {
        return TinkarTerm.STATED_NAVIGATION_PATTERN;
    }

    @Override
    protected ImmutableList<Object> assignFieldValues() {
        IntIdSet destinationNids = IntIds.set.of(destinations.stream().mapToInt(EntityProxy::nid).toArray());
        IntIdSet originNids = IntIds.set.of(origins.stream().mapToInt(EntityProxy::nid).toArray());
        return Lists.immutable.of(destinationNids, originNids);
    }

    @Override
    protected void validate() {
        if (origins.isEmpty() && destinations.isEmpty()) {
            throw new IllegalArgumentException("StatedNavigation requires at least one origin or destination concept");
        }
    }

}
