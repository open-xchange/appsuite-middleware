/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.multifactor.storage.hazelcast.impl;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
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
            token.getLifeTime().map(d -> d.getSeconds()).orElse(0L),
            TimeUnit.SECONDS);
    }

    @Override
    public int getTokenCount(MultifactorRequest multifactorRequest) {
        return (int) getMap().keySet().stream().filter(
            k -> k.startsWith(toPartialKey(multifactorRequest))
        ).count();
    }
}
