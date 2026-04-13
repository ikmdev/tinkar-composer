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
 * Functional interface for consuming a {@link SemanticAssembler} during semantic composition.
 * Implementations configure the assembler (setting reference, pattern, field values, and
 * attaching child semantics) before the framework validates and writes the resulting entity.
 */
@FunctionalInterface
public interface SemanticAssemblerConsumer {

    /**
     * Accepts and configures a {@link SemanticAssembler} to define a Semantic entity.
     *
     * @param semanticAssembler the assembler to configure
     */
    void accept(SemanticAssembler semanticAssembler);
}
