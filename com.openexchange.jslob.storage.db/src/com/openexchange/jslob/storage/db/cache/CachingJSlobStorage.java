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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.jslob.storage.db.cache;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.commons.logging.Log;
import com.openexchange.caching.Cache;
import com.openexchange.caching.CacheService;
import com.openexchange.caching.dynamic.ModifyingOXObjectFactory;
import com.openexchange.caching.dynamic.OXObjectFactory;
import com.openexchange.exception.OXException;
import com.openexchange.java.StringAllocator;
import com.openexchange.jslob.JSlob;
import com.openexchange.jslob.JSlobId;
import com.openexchange.jslob.storage.JSlobStorage;
import com.openexchange.jslob.storage.db.DBJSlobStorage;
import com.openexchange.jslob.storage.db.osgi.DBJSlobStorageActivcator;

/**
 * {@link CachingJSlobStorage}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class CachingJSlobStorage implements JSlobStorage {

    private static final Log LOG = com.openexchange.log.Log.loggerFor(CachingJSlobStorage.class);

    private static final String REGION_NAME = Constants.REGION_NAME;

    private static final AtomicReference<CacheService> SERVICE = new AtomicReference<CacheService>();

    /**
     * Sets the {@link CacheService}.
     * 
     * @param service The service
     */
    public static void setCacheService(final CacheService service) {
        SERVICE.set(service);
    }

    private static CachingJSlobStorage instance;

    /**
     * Initializes
     */
    public static synchronized CachingJSlobStorage initialize(final DBJSlobStorage delegate) {
        CachingJSlobStorage tmp = instance;
        if (null == tmp) {
            tmp = new CachingJSlobStorage(delegate);
            instance = tmp;
        }
        return tmp;
    }

    /**
     * Gets the instance
     * 
     * @return The instance
     */
    public static synchronized CachingJSlobStorage getInstance() {
        return instance;
    }

    /**
     * Shuts-down
     */
    public static synchronized void shutdown() {
        final CachingJSlobStorage tmp = instance;
        if (null != tmp) {
            tmp.release();
            instance = null;
        }
    }

    /**
     * Proxy attribute for the object implementing the persistent methods.
     */
    private final DBJSlobStorage delegate;

    /**
     * Lock for the cache.
     */
    private final Lock cacheLock;

    /**
     * Initializes a new {@link CachingJSlobStorage}.
     */
    private CachingJSlobStorage(final DBJSlobStorage delegate) {
        super();
        this.delegate = delegate;
        cacheLock = new ReentrantLock();
    }

    /**
     * Drops all JSlob entries associated with specified user.
     * 
     * @param userId The user identifier
     * @param contextId The context identifier
     */
    public void dropAllUserJSlobs(final int userId, final int contextId) {
        final Cache cache = optCache();
        if (null != cache) {
            for (final String serviceId : DBJSlobStorageActivcator.SERVICE_IDS) {
                cache.invalidateGroup(new StringAllocator(serviceId).append('@').append(userId).append('@').append(contextId).toString());
            }
        }
    }

    private void release() {
        final CacheService cacheService = SERVICE.get();
        if (null != cacheService) {
            try {
                final Cache cache = cacheService.getCache(REGION_NAME);
                cache.clear();
                cache.dispose();
            } catch (final Exception e) {
                // Ignore
            }
        }
    }

    private Cache optCache() {
        try {
            final CacheService cacheService = SERVICE.get();
            return null == cacheService ? null : cacheService.getCache(REGION_NAME);
        } catch (final OXException e) {
            LOG.warn("Failed to get cache.", e);
        }
        return null;
    }

    String groupName(final JSlobId id) {
        return new StringAllocator(id.getServiceId()).append('@').append(id.getUser()).append('@').append(id.getContext()).toString();
    }

    @Override
    public String getIdentifier() {
        return delegate.getIdentifier();
    }

    @Override
    public boolean store(final JSlobId id, final JSlob t) throws OXException {
        final Cache cache = optCache();
        if (null == cache) {
            return delegate.store(id, t);
        }
        final boolean storeResult = delegate.store(id, t);
        cache.putInGroup(id.getId(), groupName(id), t, !storeResult);
        return storeResult;
    }

    @Override
    public void invalidate(final JSlobId id) {
        final Cache cache = optCache();
        if (null != cache) {
            cache.removeFromGroup(id.getId(), groupName(id));
        }
    }

    @Override
    public JSlob load(final JSlobId id) throws OXException {
        final Cache cache = optCache();
        if (null == cache) {
            return delegate.load(id);
        }
        final DBJSlobStorage d = delegate;
        final Lock l = cacheLock;
        final OXObjectFactory<JSlob> factory = new ModifyingOXObjectFactory<JSlob>() {

            @Override
            public Serializable getKey() {
                return id.getId();
            }

            @Override
            public String getGroupName() {
                return groupName(id);
            }

            @Override
            public JSlob load() throws OXException, OXException {
                return d.load(id);
            }

            @Override
            public Lock getCacheLock() {
                return l;
            }

            @Override
            public JSlob modify(JSlob element) throws OXException {
                return (JSlob) (null == element ? null : element.clone());
            }
        };
        return new JSlobReloader(factory, REGION_NAME);
    }

    @Override
    public JSlob opt(final JSlobId id) throws OXException {
        final Cache cache = optCache();
        if (null == cache) {
            return delegate.opt(id);
        }
        final String groupName = groupName(id);
        {
            final Object fromCache = cache.getFromGroup(id.getId(), groupName);
            if (null != fromCache) {
                final DBJSlobStorage d = delegate;
                final Lock l = cacheLock;
                final OXObjectFactory<JSlob> factory = new ModifyingOXObjectFactory<JSlob>() {

                    @Override
                    public Serializable getKey() {
                        return id.getId();
                    }

                    @Override
                    public String getGroupName() {
                        return groupName;
                    }

                    @Override
                    public JSlob load() throws OXException, OXException {
                        return d.load(id);
                    }

                    @Override
                    public Lock getCacheLock() {
                        return l;
                    }

                    @Override
                    public JSlob modify(JSlob element) throws OXException {
                        return (JSlob) (null == element ? null : element.clone());
                    }
                };
                return new JSlobReloader(factory, REGION_NAME);
            }
        }
        // Optional retrieval from DB storage
        final JSlob opt = delegate.opt(id);
        if (null != opt) {
            final Lock l = cacheLock;
            final OXObjectFactory<JSlob> factory = new ModifyingOXObjectFactory<JSlob>() {

                @Override
                public Serializable getKey() {
                    return id.getId();
                }

                @Override
                public String getGroupName() {
                    return groupName;
                }

                @Override
                public JSlob load() throws OXException, OXException {
                    return opt;
                }

                @Override
                public Lock getCacheLock() {
                    return l;
                }

                @Override
                public JSlob modify(JSlob element) throws OXException {
                    return (JSlob) (null == element ? null : element.clone());
                }
            };
            return new JSlobReloader(factory, REGION_NAME);
        }
        // Null
        return null;
    }

    @Override
    public Collection<JSlob> list(final JSlobId id) throws OXException {
        final Cache cache = optCache();
        if (null == cache) {
            return delegate.list(id);
        }
        final Collection<String> ids = delegate.getIDs(id);
        final List<JSlob> ret = new ArrayList<JSlob>(ids.size());
        String serviceId = id.getServiceId();
        int user = id.getUser();
        int context = id.getContext();
        for (final String sId : ids) {
            ret.add(load(new JSlobId(serviceId, sId, user, context)));
        }
        return ret;
    }

    @Override
    public JSlob remove(final JSlobId id) throws OXException {
        final Cache cache = optCache();
        if (null != cache) {
            cache.removeFromGroup(id.getId(), groupName(id));
        }
        return delegate.remove(id);
    }

    @Override
    public boolean lock(final JSlobId jslobId) throws OXException {
        return delegate.lock(jslobId);
    }

    @Override
    public void unlock(final JSlobId jslobId) throws OXException {
        delegate.unlock(jslobId);
    }

}
