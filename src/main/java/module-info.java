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
module dev.ikm.tinkar.composer {
    requires dev.ikm.tinkar.common;
    requires dev.ikm.tinkar.terms;
    requires dev.ikm.tinkar.entity;
    requires org.eclipse.collections.api;
    requires org.slf4j;
    exports dev.ikm.tinkar.composer;
    exports dev.ikm.tinkar.composer.create.om;
    exports dev.ikm.tinkar.composer.templates;
}