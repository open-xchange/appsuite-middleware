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

package com.openexchange.multifactor.storage.impl;

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
                LOG.debug("inner storage size: " + innerMap.size());
            }
        );

        storage.entrySet().removeIf(innerMap -> innerMap.getValue().isEmpty());
        LOG.debug("storage size: " + storage.mappingCount());
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
            if(token != null && !token.isExpired()) {
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
        if(tokensForSession == null) {
            tokensForSession = Collections.synchronizedMap(new HashMap<String,T>());
            Map<String, T> old = storage.putIfAbsent(multifactorRequest, tokensForSession);
            if(old != null) {
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
