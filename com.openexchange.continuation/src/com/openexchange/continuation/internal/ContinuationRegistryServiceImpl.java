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

package com.openexchange.continuation.internal;

import java.io.Serializable;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.openexchange.caching.Cache;
import com.openexchange.caching.CacheExceptionCode;
import com.openexchange.caching.CacheService;
import com.openexchange.caching.ElementAttributes;
import com.openexchange.continuation.Continuation;
import com.openexchange.continuation.ContinuationRegistryService;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;

/**
 * {@link ContinuationRegistryServiceImpl}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since 7.6.0
 */
public class ContinuationRegistryServiceImpl implements ContinuationRegistryService {

    /** The logger constant */
    static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(ContinuationRegistryServiceImpl.class);

    // ------------------------------------------------------------------------------------------------------ //

    private final ServiceLookup services;
    private final String region;
    private final RemovalListener<UUID, Continuation<?>> removalListener;

    /**
     * Initializes a new {@link ContinuationRegistryServiceImpl}.
     *
     * @throws OXException If cache service is not available
     */
    public ContinuationRegistryServiceImpl(final String region, final ServiceLookup services) throws OXException {
        super();
        this.services = services;
        this.region = region;

        this.removalListener = new RemovalListener<UUID, Continuation<?>>() {

            @Override
            public void onRemoval(final RemovalNotification<UUID, Continuation<?>> notification) {
                final Continuation<?> continuation = notification.getValue();
                if (null != continuation) {
                    try {
                        continuation.cancel(true);
                    } catch (final Exception x) {
                        LOGGER.warn("Failed to cancel continuation {}", continuation.getUuid());
                    }
                }
            }
        };

        // Register cache event handler
        final CacheService cacheService = services.getOptionalService(CacheService.class);
        if (null == cacheService) {
            throw ServiceExceptionCode.absentService(CacheService.class);
        }
        final Cache cache = cacheService.getCache(region);
        final ElementAttributes attributes = cache.getDefaultElementAttributes();
        attributes.addElementEventHandler(new ContinuationCacheElementEventHandler());
        cache.setDefaultElementAttributes(attributes);
    }

    private Cache getCache() throws OXException {
        final CacheService cacheService = services.getOptionalService(CacheService.class);
        if (null == cacheService) {
            throw ServiceExceptionCode.absentService(CacheService.class);
        }
        return cacheService.getCache(region);
    }

    private com.google.common.cache.Cache<UUID, Continuation<?>> newUserCache() {
        // Not more than 100 concurrent continuations per user, each with 5 minutes expiry
        return CacheBuilder.newBuilder().initialCapacity(16).expireAfterAccess(5, TimeUnit.MINUTES).maximumSize(100).removalListener(removalListener).build();
    }

    private String cacheKey(final Session session) {
        return new StringBuilder(16).append(session.getUserId()).append('@').append(session.getContextId()).toString();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <V> Continuation<V> getContinuation(final UUID uuid, final Session session) throws OXException {
        if (null != uuid && null != session) {
            final Cache cache = getCache();
            final Object object = cache.get(cacheKey(session));
            if (object instanceof com.google.common.cache.Cache) {
                final com.google.common.cache.Cache<UUID, Continuation<?>> userCache = (com.google.common.cache.Cache<UUID, Continuation<?>>) object;
                return (Continuation<V>) userCache.getIfPresent(uuid);
            }
        }
        return null;
    }

    @Override
    public <V> void putContinuation(final Continuation<V> continuation, final Session session) throws OXException {
        if (null != continuation && null != session) {
            Object object;

            final Cache cache = getCache();
            synchronized (cache) {
                final String cacheKey = cacheKey(session);
                object = cache.get(cacheKey);
                while (!(object instanceof com.google.common.cache.Cache)) {
                    try {
                        final com.google.common.cache.Cache<UUID, Continuation<?>> newUserCache = newUserCache();
                        cache.putSafe(cacheKey, (Serializable) newUserCache);
                        object = newUserCache;
                    } catch (final OXException e) {
                        if (!CacheExceptionCode.FAILED_SAFE_PUT.equals(e)) {
                            throw e;
                        }
                        // An object bound to given key already exists
                        object = cache.get(cacheKey);
                    }
                }
            }

            @SuppressWarnings("unchecked")
            final com.google.common.cache.Cache<UUID, Continuation<?>> userCache = (com.google.common.cache.Cache<UUID, Continuation<?>>) object;
            if (null != userCache.asMap().putIfAbsent(continuation.getUuid(), continuation)) {
                // Already present

            }
        }
    }

    @Override
    public void removeContinuation(final UUID uuid, final Session session) throws OXException {
        if (null != uuid && null != session) {
            final Cache cache = getCache();
            synchronized (cache) {
                final String key = cacheKey(session);
                final Object object = cache.get(key);
                if (object instanceof com.google.common.cache.Cache) {
                    @SuppressWarnings("unchecked") final com.google.common.cache.Cache<UUID, Continuation<?>> userCache = (com.google.common.cache.Cache<UUID, Continuation<?>>) object;
                    userCache.invalidate(uuid);

                    if (userCache.asMap().isEmpty()) {
                        cache.remove(key);
                    }
                }
            }
        }
    }

}
