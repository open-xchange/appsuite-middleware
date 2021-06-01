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

package com.openexchange.config.cascade.user.cache;

import static com.openexchange.java.Autoboxing.I;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.cliffc.high_scale_lib.NonBlockingHashMap;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.openexchange.session.Session;

/**
 * {@link PropertyMapManagement}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class PropertyMapManagement {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(PropertyMapManagement.class);

    private static final PropertyMapManagement INSTANCE = new PropertyMapManagement();

    /**
     * Gets the {@link PropertyMapManagement management} instance.
     *
     * @return The management instance
     */
    public static PropertyMapManagement getInstance() {
        return INSTANCE;
    }

    private static final Callable<ConcurrentMap<Integer,PropertyMap>> LOADER = new Callable<ConcurrentMap<Integer,PropertyMap>>() {

        @Override
        public ConcurrentMap<Integer, PropertyMap> call() {
            return new NonBlockingHashMap<Integer, PropertyMap>();
        }
    };

    // -----------------------------------------------------------------------------------------------

    private final Cache<Integer, ConcurrentMap<Integer, PropertyMap>> map;

    /**
     * Initializes a new {@link PropertyMapManagement}.
     */
    private PropertyMapManagement() {
        super();
        map = CacheBuilder.newBuilder().expireAfterAccess(30, TimeUnit.MINUTES).build();
    }

    /**
     * Clears the property management.
     */
    public void clear() {
        map.invalidateAll();
    }

    /**
     * Drop caches for given context.
     *
     * @param contextId The context identifier
     */
    public void dropFor(final int contextId) {
        map.invalidate(Integer.valueOf(contextId));
        LOG.debug("Cleaned user-sensitive property cache for context {}", I(contextId));
    }

    /**
     * Drop caches for given session's user.
     *
     * @param session The session
     */
    public void dropFor(final Session session) {
        if (null != session) {
            dropFor(session.getUserId(), session.getContextId());
        }
    }

    /**
     * Drop caches for given session's user.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     */
    public void dropFor(final int userId, final int contextId) {
        final ConcurrentMap<Integer, PropertyMap> contextMap = map.getIfPresent(Integer.valueOf(contextId));
        if (null != contextMap) {
            contextMap.remove(Integer.valueOf(userId));
        }
        LOG.debug("Cleaned user-sensitive property cache for user {} in context {}", I(userId), I(contextId));
    }

    /**
     * Gets the property map for specified session.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The property map
     */
    public PropertyMap getFor(final int userId, final int contextId) {
        try {
            final Integer cid = Integer.valueOf(contextId);
            ConcurrentMap<Integer, PropertyMap> contextMap = map.get(cid, LOADER);

            final Integer us = Integer.valueOf(userId);
            PropertyMap propertyMap = contextMap.get(us);
            if (null == propertyMap) {
                final PropertyMap newPropertyMap = new PropertyMap(300, TimeUnit.SECONDS);
                propertyMap = contextMap.putIfAbsent(us, newPropertyMap);
                if (null == propertyMap) {
                    propertyMap = newPropertyMap;
                }
            }
            return propertyMap;
        } catch (ExecutionException e) {
            throw new IllegalStateException(e.getCause());
        }
    }

    /**
     * Optionally gets the property map for specified user in given context.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The property map or <code>null</code> if absent
     */
    public PropertyMap optFor(final int userId, final int contextId) {
        final ConcurrentMap<Integer, PropertyMap> contextMap = map.getIfPresent(Integer.valueOf(contextId));
        if (null == contextMap) {
            return null;
        }
        return contextMap.get(Integer.valueOf(userId));
    }

}
