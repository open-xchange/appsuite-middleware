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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.caching.hazelcast;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import org.cliffc.high_scale_lib.NonBlockingHashMap;
import com.hazelcast.config.MapConfig;
import com.openexchange.caching.Cache;
import com.openexchange.caching.CacheElement;
import com.openexchange.caching.CacheExceptionCode;
import com.openexchange.caching.CacheStatistics;
import com.openexchange.caching.DefaultCacheKeyService;
import com.openexchange.caching.ElementAttributes;
import com.openexchange.caching.PutIfAbsent;
import com.openexchange.caching.SupportsLocalOperations;
import com.openexchange.caching.hazelcast.util.LocalCacheGenerator;
import com.openexchange.exception.OXException;

/**
 * {@link LocalCache}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class LocalCache extends DefaultCacheKeyService implements Cache, SupportsLocalOperations, PutIfAbsent {

    private final MapConfig mapConfig;

    private final com.google.common.cache.Cache<Serializable, Serializable> cache;

    private final ConcurrentMap<String, com.google.common.cache.Cache<Serializable, Serializable>> groups;

    /**
     * Initializes a new {@link LocalCache}.
     */
    public LocalCache(final com.google.common.cache.Cache<Serializable, Serializable> cache, final MapConfig mapConfig) {
        super();
        this.cache = cache;
        this.mapConfig = mapConfig;
        groups = new NonBlockingHashMap<String, com.google.common.cache.Cache<Serializable, Serializable>>(8);
    }

    @Override
    public Collection<Serializable> values() {
        return cache.asMap().values();
    }

    @Override
    public boolean isLocal() {
        return true;
    }

    @Override
    public boolean isDistributed() {
        return true;
    }

    @Override
    public boolean isReplicated() {
        return false;
    }

    @Override
    public void clear() throws OXException {
        for (final Entry<String, com.google.common.cache.Cache<Serializable, Serializable>> entry : groups.entrySet()) {
            entry.getValue().invalidateAll();
        }
        cache.invalidateAll();
    }

    @Override
    public void dispose() {
        for (final Entry<String, com.google.common.cache.Cache<Serializable, Serializable>> entry : groups.entrySet()) {
            final com.google.common.cache.Cache<Serializable, Serializable> groupCache = entry.getValue();
            groupCache.invalidateAll();
            groupCache.cleanUp();
        }
        groups.clear();
        cache.invalidateAll();
        cache.cleanUp();
    }

    @Override
    public Object get(final Serializable key) {
        return cache.getIfPresent(key);
    }

    @Override
    public CacheElement getCacheElement(final Serializable key) {
        return null;
    }

    @Override
    public ElementAttributes getDefaultElementAttributes() throws OXException {
        throw CacheExceptionCode.UNSUPPORTED_OPERATION.create("LocalCache.getDefaultElementAttributes()");
    }

    @Override
    public Object getFromGroup(final Serializable key, final String groupName) {
        final com.google.common.cache.Cache<Serializable, Serializable> group = groups.get(groupName);
        if (null != group) {
            return group.getIfPresent(key);
        }
        return null;
    }

    @Override
    public void invalidateGroup(final String groupName) {
        final com.google.common.cache.Cache<Serializable, Serializable> group = groups.get(groupName);
        if (null != group) {
            group.invalidateAll();
        }
    }

    @Override
    public void put(final Serializable key, final Serializable obj) throws OXException {
        cache.put(key, obj);
    }

    @Override
    public void put(final Serializable key, final Serializable val, final ElementAttributes attr) throws OXException {
        throw CacheExceptionCode.UNSUPPORTED_OPERATION.create("LocalCache.put()");
    }

    @Override
    public void putInGroup(final Serializable key, final String groupName, final Object value, final ElementAttributes attr) throws OXException {
        throw CacheExceptionCode.UNSUPPORTED_OPERATION.create("LocalCache.putInGroup()");
    }

    @Override
    public void putInGroup(final Serializable key, final String groupName, final Serializable value) throws OXException {
        com.google.common.cache.Cache<Serializable, Serializable> group = groups.get(groupName);
        if (null == group) {
            final com.google.common.cache.Cache<Serializable, Serializable> ngroup = LocalCacheGenerator.<Serializable, Serializable> createLocalCache(mapConfig);
            group = groups.putIfAbsent(groupName, ngroup);
            if (null == group) {
                group = ngroup;
            }
        }
        group.put(key, value);
    }

    @Override
    public void putSafe(final Serializable key, final Serializable value) throws OXException {
        try {
            final Serializable prev = cache.get(key, new Callable<Serializable>() {

                @Override
                public Serializable call() throws Exception {
                    return value;
                }
            });
            if (prev != value) {
                throw CacheExceptionCode.FAILED_SAFE_PUT.create();
            }
        } catch (final ExecutionException e) {
            final Throwable cause = e.getCause();
            throw CacheExceptionCode.FAILED_SAFE_PUT.create(cause, cause.getMessage());
        }
    }

    @Override
    public Serializable putIfAbsent(final Serializable key, final Serializable value) throws OXException {
        try {
            return cache.get(key, new Callable<Serializable>() {

                @Override
                public Serializable call() throws Exception {
                    return value;
                }
            });
        } catch (final ExecutionException e) {
            final Throwable cause = e.getCause();
            throw CacheExceptionCode.FAILED_SAFE_PUT.create(cause, cause.getMessage());
        } catch (final RuntimeException e) {
            throw CacheExceptionCode.CACHE_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public void remove(final Serializable key) throws OXException {
        cache.invalidate(key);
    }

    @Override
    public void localRemove(final Serializable key) throws OXException {
        cache.invalidate(key);
    }

    @Override
    public void localPut(final Serializable key, final Serializable value) throws OXException {
        cache.put(key, value);
    }

    @Override
    public void removeFromGroup(final Serializable key, final String groupName) {
        final com.google.common.cache.Cache<Serializable, Serializable> group = groups.get(groupName);
        if (null != group) {
            group.invalidate(key);
        }
    }

    @Override
    public void localRemoveFromGroup(final Serializable key, final String groupName) {
        final com.google.common.cache.Cache<Serializable, Serializable> group = groups.get(groupName);
        if (null != group) {
            group.invalidate(key);
        }
    }

    @Override
    public void setDefaultElementAttributes(final ElementAttributes attr) throws OXException {
        // TODO Auto-generated method stub

    }

    @Override
    public CacheStatistics getStatistics() {
        // TODO Auto-generated method stub
        return null;
    }

}
