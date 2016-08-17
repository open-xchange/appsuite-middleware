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

package com.openexchange.jslob.storage.db.cache;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import com.openexchange.caching.Cache;
import com.openexchange.caching.CacheService;
import com.openexchange.exception.OXException;
import com.openexchange.jslob.JSlob;
import com.openexchange.jslob.JSlobExceptionCodes;
import com.openexchange.jslob.JSlobId;
import com.openexchange.jslob.storage.JSlobStorage;
import com.openexchange.jslob.storage.db.DBJSlobStorage;
import com.openexchange.jslob.storage.db.osgi.DBJSlobStorageActivcator;
import com.openexchange.jslob.storage.db.util.DelayedStoreOp;
import com.openexchange.jslob.storage.db.util.DelayedStoreOpDelayQueue;
import com.openexchange.threadpool.ThreadPools;

/**
 * {@link CachingJSlobStorage}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class CachingJSlobStorage implements JSlobStorage, Runnable {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(CachingJSlobStorage.class);

    private static final String REGION_NAME = Constants.REGION_NAME;

    private static final AtomicReference<CacheService> SERVICE = new AtomicReference<CacheService>();

    /**
     * Sets the {@link CacheService}.
     *
     * @param service The service
     */
    public static void setCacheService(CacheService service) {
        SERVICE.set(service);
    }

    private static CachingJSlobStorage instance;

    /**
     * Initializes
     */
    public static synchronized CachingJSlobStorage initialize(DBJSlobStorage delegate) {
        CachingJSlobStorage tmp = instance;
        if (null == tmp) {
            tmp = new CachingJSlobStorage(delegate);
            ThreadPools.getThreadPool().submit(ThreadPools.task(tmp));
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
        CachingJSlobStorage tmp = instance;
        if (null != tmp) {
            tmp.release();
            instance = null;
        }
    }

    /** The poison element */
    private static final DelayedStoreOp POISON = DelayedStoreOp.POISON;

    /** Proxy attribute for the object implementing the persistent methods. */
    private final DBJSlobStorage delegate;

    /** The queue for delayed store operations */
    private final DelayedStoreOpDelayQueue delayedStoreOps;

    /** The keep-going flag */
    private final AtomicBoolean keepgoing;

    /**
     * Initializes a new {@link CachingJSlobStorage}.
     */
    private CachingJSlobStorage(DBJSlobStorage delegate) {
        super();
        this.delegate = delegate;
        delayedStoreOps = new DelayedStoreOpDelayQueue();
        keepgoing = new AtomicBoolean(true);
    }

    @Override
    public void run() {
        List<DelayedStoreOp> objects = new ArrayList<DelayedStoreOp>(16);
        while (keepgoing.get()) {
            try {
                objects.clear();
                // Blocking wait for at least 1 DelayedPushMsObject to expire.
                DelayedStoreOp object = delayedStoreOps.take();
                if (POISON == object) {
                    return;
                }
                objects.add(object);
                // Drain more if available
                delayedStoreOps.drainTo(objects);
                Cache cache = optCache();
                if (null != cache) {
                    try {
                        if (writeMultiple2DB(objects, cache)) {
                            // Reached poison element
                            return;
                        }
                    } catch (OXException e) {
                        // Multiple store failed
                        if (!JSlobExceptionCodes.UNEXPECTED_ERROR.equals(e) || !SQLException.class.isInstance(e.getCause())) {
                            throw e;
                        }
                        boolean leave = false;
                        for (DelayedStoreOp delayedStoreOp : objects) {
                            if (POISON == delayedStoreOp) {
                                // Reached poison element
                                leave = true;
                            } else if (delayedStoreOp != null) {
                                try {
                                    write2DB(delayedStoreOp, cache);
                                } catch (Exception x) {
                                    LOG.error("JSlobs could not be flushed to database", x);
                                }
                            }
                        }
                        if (leave) {
                            return;
                        }
                    }
                }
            } catch (InterruptedException e) {
                LOG.debug("Interrupted while checking for delayed JSlobs.", e);
                // Keep interrupted state
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                LOG.error("Checking for delayed JSlobs failed", e);
            }
        }
    }

    private void write2DB(DelayedStoreOp delayedStoreOp, Cache cache) throws OXException {
        Object obj = cache.getFromGroup(delayedStoreOp.id, delayedStoreOp.group);
        if (obj instanceof JSlob) {
            JSlob t = (JSlob) obj;

            // Write to store
            delegate.store(delayedStoreOp.jSlobId, t);

            // Propagate among remote caches
            cache.putInGroup(delayedStoreOp.id, delayedStoreOp.group, t.setId(delayedStoreOp.jSlobId), true);
        }
    }

    private boolean writeMultiple2DB(List<DelayedStoreOp> delayedStoreOps, Cache cache) throws OXException {
        boolean leave = false;

        // Collect valid delayed store operations
        int size = delayedStoreOps.size();
        Map<JSlobId, JSlob> jslobs = new HashMap<JSlobId, JSlob>(size);
        for (int i = 0; i < size; i++) {
            DelayedStoreOp delayedStoreOp = delayedStoreOps.get(i);
            if (POISON == delayedStoreOp) {
                leave = true;
            } else if (delayedStoreOp != null) {
                Object obj = cache.getFromGroup(delayedStoreOp.id, delayedStoreOp.group);
                if (obj instanceof JSlob) {
                    jslobs.put(delayedStoreOp.jSlobId, (JSlob) obj);
                }
            }
        }

        // Store them
        delegate.storeMultiple(jslobs);

        // Invalidate caches
        for (Entry<JSlobId, JSlob> entry : jslobs.entrySet()) {
            JSlobId id = entry.getKey();
            cache.putInGroup(id.getId(), groupName(id), entry.getValue().setId(id), true);
        }

        return leave;
    }

    /**
     * Invalidates all JSlob entries associated with specified user from cache.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     */
    public void dropAllUserJSlobs(int userId, int contextId) {
        Cache cache = optCache();
        if (null != cache) {
            flushDelayedOpsForUser(userId, contextId, cache);

            for (String serviceId : DBJSlobStorageActivcator.SERVICE_IDS) {
                cache.invalidateGroup(new StringBuilder(serviceId).append('@').append(userId).append('@').append(contextId).toString());
            }
        }
    }

    private void release() {
        keepgoing.set(false);
        delayedStoreOps.offer(POISON);
        CacheService cacheService = SERVICE.get();
        if (null != cacheService) {
            try {
                Cache cache = cacheService.getCache(REGION_NAME);
                flushDelayedOps2Storage(cache);
                cache.clear();
                cache.dispose();
            } catch (Exception e) {
                // Ignore
            }
        }
    }

    private void flushDelayedOps2Storage(Cache cache) {
        if (null != cache) {
            for (DelayedStoreOp delayedStoreOp : delayedStoreOps) {
                if (delayedStoreOp != null && POISON != delayedStoreOp) {
                    try {
                        write2DB(delayedStoreOp, cache);
                    } catch (Exception e) {
                        LOG.error("JSlobs could not be flushed to database", e);
                    }
                }
            }
        }
    }

    private Cache optCache() {
        try {
            CacheService cacheService = SERVICE.get();
            return null == cacheService ? null : cacheService.getCache(REGION_NAME);
        } catch (OXException e) {
            LOG.warn("Failed to get cache.", e);
        }
        return null;
    }

    private String groupName(JSlobId id) {
        return new StringBuilder(id.getServiceId()).append('@').append(id.getUser()).append('@').append(id.getContext()).toString();
    }

    /**
     * Flushes the delayed operations associated with given user to storage
     *
     * @param userId The suer identifier
     * @param contextId The cotnext identifier
     */
    public void flushDelayedOpsForUser(int userId, int contextId) {
        Cache cache = optCache();
        if (null != cache) {
            flushDelayedOpsForUser(userId, contextId, cache);
        }
    }

    private void flushDelayedOpsForUser(int userId, int contextId, Cache cache) {
        List<DelayedStoreOp> ops = new LinkedList<DelayedStoreOp>();
        int n = delayedStoreOps.drainForUser(userId, contextId, ops);

        if (n > 0) {
            for (DelayedStoreOp delayedStoreOp : ops) {
                if (delayedStoreOp != null && POISON != delayedStoreOp) {
                    try {
                        write2DB(delayedStoreOp, cache);
                    } catch (Exception e) {
                        LOG.error("JSlobs could not be flushed to database", e);
                    }
                }
            }
        }
    }

    @Override
    public String getIdentifier() {
        return delegate.getIdentifier();
    }

    @Override
    public boolean store(JSlobId id, JSlob t) throws OXException {
        Cache cache = optCache();
        if (null == cache) {
            return delegate.store(id, t);
        }

        // Delay store operation
        String groupName = groupName(id);
        delayedStoreOps.offerIfAbsent(new DelayedStoreOp(id.getId(), groupName, id));

        // Added to OR already contained in delay queue -- put current to cache
        cache.putInGroup(id.getId(), groupName, t.setId(id), false);
        return true;
    }

    @Override
    public void invalidate(JSlobId id) {
        Cache cache = optCache();
        if (null != cache) {
            cache.removeFromGroup(id.getId(), groupName(id));
        }
    }

    @Override
    public JSlob load(JSlobId id) throws OXException {
        Cache cache = optCache();
        if (null == cache) {
            return delegate.load(id);
        }
        Object object = cache.getFromGroup(id.getId(), groupName(id));
        if (object instanceof JSlob) {
            return (JSlob) object;
        }
        JSlob loaded = delegate.load(id);
        cache.putInGroup(id.getId(), groupName(id), loaded, false);
        return loaded.clone();
    }

    @Override
    public JSlob opt(JSlobId id) throws OXException {
        Cache cache = optCache();
        if (null == cache) {
            return delegate.opt(id);
        }
        String groupName = groupName(id);
        {
            Object fromCache = cache.getFromGroup(id.getId(), groupName);
            if (null != fromCache) {
                return ((JSlob) fromCache).clone();
            }
        }
        // Optional retrieval from DB storage
        JSlob opt = delegate.opt(id);
        if (null == opt) {
            // Null
            return null;
        }
        cache.putInGroup(id.getId(), groupName, opt, false);
        return opt.clone();
    }

    @Override
    public List<JSlob> list(List<JSlobId> ids) throws OXException {
        Cache cache = optCache();
        if (null == cache) {
            return delegate.list(ids);
        }

        int size = ids.size();
        Map<String, JSlob> map = new HashMap<String, JSlob>(size);
        List<JSlobId> toLoad = new ArrayList<JSlobId>(size);
        for (int i = 0; i < size; i++) {
            JSlobId id = ids.get(i);
            Object object = cache.getFromGroup(id.getId(), groupName(id));
            if (object instanceof JSlob) {
                map.put(id.getId(), (JSlob) object);
            } else {
                toLoad.add(id);
            }
        }

        if (!toLoad.isEmpty()) {
            List<JSlob> loaded = delegate.list(toLoad);
            for (JSlob jSlob : loaded) {
                if (null != jSlob) {
                    JSlobId id = jSlob.getId();
                    cache.putInGroup(id.getId(), groupName(id), jSlob, false);
                    map.put(id.getId(), jSlob.clone());
                }
            }
        }

        List<JSlob> ret = new ArrayList<JSlob>(size);
        for (JSlobId id : ids) {
            ret.add(null == id ? null : map.get(id.getId()));
        }
        return ret;
    }

    @Override
    public Collection<JSlob> list(JSlobId id) throws OXException {
        Cache cache = optCache();
        if (null == cache) {
            return delegate.list(id);
        }
        Collection<String> ids = delegate.getIDs(id);
        List<JSlob> ret = new ArrayList<JSlob>(ids.size());
        String serviceId = id.getServiceId();
        int user = id.getUser();
        int context = id.getContext();
        for (String sId : ids) {
            ret.add(load(new JSlobId(serviceId, sId, user, context)));
        }
        return ret;
    }

    @Override
    public JSlob remove(JSlobId id) throws OXException {
        Cache cache = optCache();
        if (null != cache) {
            cache.removeFromGroup(id.getId(), groupName(id));
        }
        return delegate.remove(id);
    }

    @Override
    public boolean lock(JSlobId jslobId) throws OXException {
        return delegate.lock(jslobId);
    }

    @Override
    public void unlock(JSlobId jslobId) throws OXException {
        delegate.unlock(jslobId);
    }

}
