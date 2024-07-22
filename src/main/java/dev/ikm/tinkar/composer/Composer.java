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
import java.util.concurrent.atomic.AtomicReference;

public class Composer {
    private final Map<UUID, Session> composerSessionCache = new HashMap<>();
    private StampEntity stampEntity;
    private Transaction transaction;
    private final String name;

    public Composer(String name) {
        this.name = name;
    }

//    public Session open() {
//          TODO - contain defaults for free with no time
//    }

    public Session open(State status, long time, Concept author, Concept module, Concept path) {
        this.transaction = new Transaction(name);
        this.stampEntity = transaction.getStamp(status, time, author.publicId(), module.publicId(), path.publicId());
        UUID sessionKey = keyValue(status, time, author, module, path);
        composerSessionCache.computeIfAbsent(sessionKey, (key) -> new Session(transaction, stampEntity));
        return composerSessionCache.get(sessionKey);
    }

    public Session open(State status, Concept author, Concept module, Concept path) {
        this.transaction = new Transaction(name);
        this.stampEntity = transaction.getStamp(status, author, module, path);
        UUID sessionKey = keyValue(status, System.currentTimeMillis(), author, module, path);
        composerSessionCache.computeIfAbsent(sessionKey, (key) -> new Session(transaction, stampEntity));
        return composerSessionCache.get(sessionKey);
    }

    public boolean closeSession(Session session) {
        AtomicReference<UUID> closeKey = new AtomicReference<>();
        composerSessionCache.forEach((key, value) -> {
            if(value.equals(session)) {
                closeKey.set(key);
            }
        });
        return closeSession(closeKey.get());
    }

    public void closeAllSessions() {
        Set<UUID> keySet = new HashSet<>(composerSessionCache.keySet());
        for (UUID key : keySet) {
            closeSession(key);
        }
    }

    public void cancelAllSessions() {
        composerSessionCache.forEach((key, value) -> {
            value.cancel();
        });
    }

    private boolean closeSession(UUID closeKey) {
        AtomicBoolean isClosed = new AtomicBoolean(false);
        composerSessionCache.computeIfPresent(closeKey, (key, value) -> {
            value.close();
            isClosed.set(true);
            return null; // removes key
        });
        return isClosed.get();
    }

    private static UUID keyValue(State status, long time, Concept author, Concept module, Concept path) {
        UUID uuidKey = UuidT5Generator.fromPublicIds(UUID.nameUUIDFromBytes(String.valueOf(time).getBytes()),
                status.publicId(), author, module, path);
        return uuidKey;
    }
    
}
