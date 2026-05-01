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
package dev.ikm.tinkar.composer;

import java.util.UUID;

import dev.ikm.tinkar.entity.StampEntity;
import dev.ikm.tinkar.terms.EntityProxy.Concept;
import dev.ikm.tinkar.terms.State;

public class Composer {

    /**
     * Provides a Session for creating Components with a <strong>predefined
     * timestamp</strong>.
     * <br />
     * <br />
     * Example use case: ingesting a file with previously defined data definitions.
     * 
     * <pre>{@code
     *
     * Composer composer = new Composer("name");
     * Session session = composer.open(status, time, author, module, path);
     * session.compose((ConceptAssembler conceptAssembler) -> conceptAssembler
     *         .concept(Concept.make("Example Concept with predefined time", PublicIds.newRandom())));
     * composer.commitSession(session);
     * }</pre>
     * 
     * @param status the status set for Components composed in the Session
     * @param time   the timestamp (in epoch milliseconds) set for Components
     *               composed in the Session
     * @param author the author set for Components composed in the Session
     * @param module the module set for Components composed in the Session
     * @param path   the path set for Components composed in the Session
     * @see State
     */
    public Session open(String name, State status, long time, Concept author, Concept module, Concept path) {
        UUID stampId = UUID.randomUUID();
        StampEntity stampEntity = Write.stamp(stampId, status, time, author, module, path);
        return new Session(name, stampEntity);
    }

    /**
     * Provides a Session for creating Components with a <strong>current
     * timestamp</strong>.
     * Timestamp will be defined as the time of commit.
     * <br />
     * <br />
     * Example use case: creating or editing Components resulting in net new
     * Components / Versions.
     * 
     * <pre>{@code
     *    Composer composer = new Composer("name");
     *    Session session = composer.open(status, author, module, path);
     *    session.compose((ConceptAssembler conceptAssembler) -> conceptAssembler
     *              .concept((Concept.make("Example Concept with commit time", PublicIds.newRandom()));
     *    composer.commitSession(session);
     * }</pre>
     * 
     * @param status the status set for Components composed in the Session
     * @param author the author set for Components composed in the Session
     * @param module the module set for Components composed in the Session
     * @param path   the path set for Components composed in the Session
     * @see State
     */
    public Session open(String name, State status, Concept author, Concept module, Concept path) {
        UUID stampId = UUID.randomUUID();
        long time = System.currentTimeMillis();
        StampEntity stampEntity = Write.stamp(stampId, status, time, author, module, path);
        return new Session(name, stampEntity);
    }

}
