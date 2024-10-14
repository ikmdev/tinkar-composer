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

import dev.ikm.tinkar.common.util.uuid.UuidT5Generator;
import dev.ikm.tinkar.entity.StampEntity;
import dev.ikm.tinkar.entity.transaction.Transaction;
import dev.ikm.tinkar.terms.EntityProxy.Concept;
import dev.ikm.tinkar.terms.State;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class Composer {
    private final Map<UUID, Session> composerSessionCache = new HashMap<>();
    private StampEntity stampEntity;
    private Transaction transaction;
    private final String name;

    public Composer(String name) {
        this.name = name;
    }

    /**
     * Provides a Session for creating Components with a <strong>predefined timestamp</strong>.
     * <br /><br />
     * Example use case: ingesting a file with previously defined data definitions.
     * <pre>{@code
     *
     *    Composer composer = new Composer("name");
     *    Session session = composer.open(status, time, author, module, path);
     *    session.compose((ConceptAssembler conceptAssembler) -> conceptAssembler
     *              .concept(Concept.make("Example Concept with predefined time", PublicIds.newRandom())));
     *    composer.commitSession(session);
     * }</pre>
     * @param status the status set for Components composed in the Session
     * @param time the timestamp (in epoch milliseconds) set for Components composed in the Session
     * @param author the author set for Components composed in the Session
     * @param module the module set for Components composed in the Session
     * @param path the path set for Components composed in the Session
     * @see State
     */
    public Session open(State status, long time, Concept author, Concept module, Concept path) {
        UUID sessionKey = keyValue(status, time, author, module, path);
        composerSessionCache.computeIfAbsent(sessionKey, (key) -> {
            this.transaction = new Transaction(name);
            this.stampEntity = transaction.getStamp(status, time, author.publicId(), module.publicId(), path.publicId());
            return new Session(transaction, stampEntity, sessionKey);
        });
        return composerSessionCache.get(sessionKey);
    }

    /**
     * Provides a Session for creating Components with a <strong>current timestamp</strong>.
     * Timestamp will be defined as the time of commit.
     * <br /><br />
     * Example use case: creating or editing Components resulting in net new Components / Versions.
     * <pre>{@code
     *    Composer composer = new Composer("name");
     *    Session session = composer.open(status, author, module, path);
     *    session.compose((ConceptAssembler conceptAssembler) -> conceptAssembler
     *              .concept((Concept.make("Example Concept with commit time", PublicIds.newRandom()));
     *    composer.commitSession(session);
     * }</pre>
     * @param status the status set for Components composed in the Session
     * @param author the author set for Components composed in the Session
     * @param module the module set for Components composed in the Session
     * @param path the path set for Components composed in the Session
     * @see State
     */
    public Session open(State status, Concept author, Concept module, Concept path) {
        UUID sessionKey = keyValue(status, Long.MAX_VALUE, author, module, path);
        composerSessionCache.computeIfAbsent(sessionKey, (key) -> {
            this.transaction = new Transaction(name);
            this.stampEntity = transaction.getStamp(status, author, module, path);
            return new Session(transaction, stampEntity, sessionKey);
        });
        return composerSessionCache.get(sessionKey);
    }

    /**
     * Commits a Session opened by this Composer.
     * <br />
     * {@link Session#commit()} <strong>commits</strong> the Components and STAMPs in the session transaction.
     * @param session
     * @return boolean representing whether the Session was committed. A Composer can only close a Session it opened.
     */
    public boolean commitSession(Session session) {
        return commitSession(session.getId());
    }

    /**
     * Commits all Sessions opened by this Composer.
     * <br />
     * {@link Session#commit()} <strong>commits</strong> the Components and STAMPs in the session transaction.
     */
    public void commitAllSessions() {
        Set<UUID> keySet = new HashSet<>(composerSessionCache.keySet());
        for (UUID key : keySet) {
            commitSession(key);
        }
    }

    /**
     * Cancels a Session opened by this Composer.
     * <br />
     * {@link Session#cancel()} <strong>cancels</strong> the Components and STAMPs in the session transaction.
     * @param session
     * @return boolean representing whether the Session was cancelled. A Composer can only close a Session it opened.
     */
    public boolean cancelSession(Session session) {
        return cancelSession(session.getId());
    }

    /**
     * Cancels all Sessions opened by this Composer.
     * <br />
     * {@link Session#cancel()} <strong>cancels</strong> the Components and STAMPs in the session transaction.
     */
    public void cancelAllSessions() {
        composerSessionCache.forEach((key, value) -> {
            value.cancel();
        });
    }

    private boolean commitSession(UUID sessionKey) {
        AtomicBoolean isClosed = new AtomicBoolean(false);
        composerSessionCache.computeIfPresent(sessionKey, (key, value) -> {
            value.commit();
            isClosed.set(true);
            return null; // removes key
        });
        return isClosed.get();
    }

    private boolean cancelSession(UUID sessionKey) {
        AtomicBoolean isCancelled = new AtomicBoolean(false);
        composerSessionCache.computeIfPresent(sessionKey, (key, value) -> {
            value.cancel();
            isCancelled.set(true);
            return null; // removes key
        });
        return isCancelled.get();
    }

    private static UUID keyValue(State status, long time, Concept author, Concept module, Concept path) {
        UUID uuidKey = UuidT5Generator.fromPublicIds(UUID.nameUUIDFromBytes(String.valueOf(time).getBytes()),
                status.publicId(), author, module, path);
        return uuidKey;
    }
    
}
