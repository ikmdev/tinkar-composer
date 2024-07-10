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

import dev.ikm.tinkar.entity.StampEntity;
import dev.ikm.tinkar.entity.transaction.Transaction;
import dev.ikm.tinkar.terms.EntityProxy.Concept;
import dev.ikm.tinkar.terms.State;

import java.io.Closeable;

public class Session implements Closeable {

    private final StampEntity stampEntity;
    private final Transaction transaction;

    public Session(State status, long time, Concept author, Concept module, Concept path){
        this.transaction = new Transaction();
        this.stampEntity = transaction.getStamp(status, time, author.publicId(), module.publicId(), path.publicId());
    }

    public Session(State status, Concept author, Concept module, Concept path) {
        this.transaction = new Transaction();
        this.stampEntity = transaction.getStamp(status, author, module, path);
    }

    public Composer makeCompose() {
        return new Composer(transaction, stampEntity.publicId());
    }

    @Override
    public void close() {
        transaction.commit();
    }
}
