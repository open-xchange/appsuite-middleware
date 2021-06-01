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

package com.openexchange.multifactor.storage.hazelcast.impl;

import static com.openexchange.java.Autoboxing.L;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.openexchange.multifactor.MultifactorRequest;
import com.openexchange.multifactor.MultifactorToken;
import com.openexchange.multifactor.storage.MultifactorTokenStorage;

/**
 * {@link HazelcastMultifactorTokenStorage}
 *
 * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
 * @since v7.10.2
 */
public class HazelcastMultifactorTokenStorage<T extends MultifactorToken<?>> implements MultifactorTokenStorage<T> {

    /**
     * Factory for mapping {@link PortableMultifactorToken} to {@link MultifactorToken}
     *
     * {@link TokenFactory}
     *
     * @author <a href="mailto:benjamin.gruedelbach@open-xchange.com">Benjamin Gruedelbach</a>
     * @since v7.10.2
     * @param <T>
     */
    @FunctionalInterface
    public interface TokenFactory<T>{

        /**
         * Creates the {@link MultifactorToken} Token from the given{@link PortableMultifactorToken}
         *
         * @param portableToken The portable to create the token from
         * @return The {@link MultifactorToken} created from the given portable
         */
        T create(PortableMultifactorToken portableToken);
    }

    private final String            mapName;
    private final HazelcastInstance hz;
    private final TokenFactory<T>         tokenFactory;

    /**
     * Initializes a new {@link HazelcastMultifactorTokenStorage}.
     *
     * @param HazelcastInstance The {@link HazelcastInstance}
     * @param mapName The name of the hazelcast map to use
     * @param tokenFactory A factory for creating the MultifactorToken from a given portable
     */
    public HazelcastMultifactorTokenStorage(HazelcastInstance hazelcastInstance, String mapName, TokenFactory<T> tokenFactory) {
        this.hz = Objects.requireNonNull(hazelcastInstance, "hazelcastInstance must not be null");
        this.mapName = Objects.requireNonNull(mapName, "mapName must not be null");
        this.tokenFactory = Objects.requireNonNull(tokenFactory, "tokenFactory must not be null");
    }

    private IMap<String, PortableMultifactorToken> getMap() {
        return hz.getMap(mapName);
    }

    private String toPartialKey(MultifactorRequest multifactorRequests) {
        return multifactorRequests.getContextId() + "_" + multifactorRequests.getUserId();
    }

    private String toKey(MultifactorRequest  session, String key) {
       return toPartialKey(session) + "_" + key.toString();
    }

    @Override
    public Optional<T> getAndRemove(MultifactorRequest multifactorRequest, String key) {
        String hzKey = toKey(multifactorRequest, key);
        Optional<PortableMultifactorToken> portableToken = Optional.ofNullable(getMap().remove(hzKey));
        return portableToken.map( p -> tokenFactory.create(p));
    }

    @Override
    public void add(MultifactorRequest multifactorRequest, String key, T token) {
        getMap().putIfAbsent(
            toKey(multifactorRequest,key),
            new PortableMultifactorToken(token.getLifeTime().get(), token.getValue()),
            token.getLifeTime().map(d -> L(d.getSeconds())).orElse(L(0L)).longValue(),
            TimeUnit.SECONDS);
    }

    @Override
    public int getTokenCount(MultifactorRequest multifactorRequest) {
        return (int) getMap().keySet().stream().filter(
            k -> k.startsWith(toPartialKey(multifactorRequest))
        ).count();
    }
}
