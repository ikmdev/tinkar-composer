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
package dev.ikm.tinkar.composer.assembler;

/**
 * Functional interface for consuming a {@link ConceptAssembler} during concept composition.
 * Implementations configure the assembler (setting identity, attaching semantics) before
 * the framework validates and writes the resulting entity.
 */
@FunctionalInterface
public interface ConceptAssemblerConsumer {

    /**
     * Accepts and configures a {@link ConceptAssembler} to define a Concept entity.
     *
     * @param conceptAssembler the assembler to configure
     */
    void accept(ConceptAssembler conceptAssembler);
}
