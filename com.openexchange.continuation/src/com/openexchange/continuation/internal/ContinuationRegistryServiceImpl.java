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
                    } catch (Exception x) {
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
                    } catch (OXException e) {
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
