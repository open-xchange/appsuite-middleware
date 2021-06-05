/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.multifactor.storage.impl;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.L;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.multifactor.MultifactorRequest;
import com.openexchange.multifactor.MultifactorToken;
import com.openexchange.multifactor.storage.MultifactorTokenStorage;

/**
 * {@link MemoryMultifactorTokenStorage}
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.2
 */
public class MemoryMultifactorTokenStorage<T extends MultifactorToken<?>> implements MultifactorTokenStorage<T>{

    private static final Logger LOG = LoggerFactory.getLogger(MemoryMultifactorTokenStorage.class);
    private final ConcurrentHashMap<MultifactorRequest, Map<String,T> > storage;

    /**
     * Initializes a new {@link MemoryMultifactorTokenStorage}.
     *
     */
    public MemoryMultifactorTokenStorage() {
        this.storage = new ConcurrentHashMap<MultifactorRequest, Map<String,T>>();
    }

    private void cleanup() {

        storage.entrySet().forEach(
            (entry) -> {
                Map<String,T> innerMap = entry.getValue();
                innerMap.entrySet().removeIf(e -> e.getValue().isExpired());
                LOG.debug("inner storage size: {}", I(innerMap.size()));
            }
        );

        storage.entrySet().removeIf(innerMap -> innerMap.getValue().isEmpty());
        LOG.debug("storage size: {}", L(storage.mappingCount()));
    }

    private Map<String, T> getTokensFor(MultifactorRequest multifactorRequest) {
        return storage.getOrDefault(multifactorRequest, Collections.emptyMap());
    }

    @Override
    public Optional<T> getAndRemove(MultifactorRequest multifactorRequest, String key) {
        cleanup();
        Map<String, T> tokensForSession = getTokensFor(multifactorRequest);
        synchronized(tokensForSession){
            T token = tokensForSession.get(key);
            if (token != null && !token.isExpired()) {
               tokensForSession.remove(key);
               return Optional.of(token);
            }
            return Optional.empty();
        }
    }

    @Override
    public void add(MultifactorRequest multifactorRequest, String key, T token) {
        cleanup();
        Map<String, T> tokensForSession = storage.get(multifactorRequest);
        if (tokensForSession == null) {
            tokensForSession = Collections.synchronizedMap(new HashMap<String,T>());
            Map<String, T> old = storage.putIfAbsent(multifactorRequest, tokensForSession);
            if (old != null) {
                tokensForSession = old;
            }
        }
        tokensForSession.put(key, token);
    }

    @Override
    public int getTokenCount(MultifactorRequest multifactorRequest) {
        cleanup();
        return getTokensFor(multifactorRequest).size();
    }
}
