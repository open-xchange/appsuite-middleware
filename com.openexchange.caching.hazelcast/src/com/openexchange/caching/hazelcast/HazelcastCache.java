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
import java.util.concurrent.ConcurrentHashMap;
import com.hazelcast.config.MapConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.MapEntry;
import com.openexchange.caching.Cache;
import com.openexchange.caching.CacheElement;
import com.openexchange.caching.CacheExceptionCode;
import com.openexchange.caching.CacheKey;
import com.openexchange.caching.CacheKeyImpl;
import com.openexchange.caching.CacheStatistics;
import com.openexchange.caching.ElementAttributes;
import com.openexchange.exception.OXException;

/**
 * {@link HazelcastCache}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class HazelcastCache implements Cache {

    private final HazelcastInstance hazelcastInstance;

    private final IMap<Serializable, Serializable> map;

    private volatile MapConfig mapConfig;

    /**
     * Initializes a new {@link HazelcastCache}.
     */
    public HazelcastCache(final IMap<Serializable, Serializable> map, final HazelcastInstance hazelcastInstance) {
        super();
        this.map = map;
        this.hazelcastInstance = hazelcastInstance;
    }

    private MapConfig getMapConfig() {
        MapConfig tmp = mapConfig;
        if (null == tmp) {
            synchronized (this) {
                tmp = mapConfig;
                if (null == tmp) {
                    tmp = hazelcastInstance.getConfig().getMapConfig(map.getName());
                    mapConfig = tmp;
                }
            }
        }
        return tmp;
    }

    @Override
    public boolean isReplicated() {
        return false;
    }

    @Override
    public boolean isDistributed() {
        return true;
    }

    @Override
    public void clear() throws OXException {
        map.clear();
    }

    @Override
    public void dispose() {
        map.destroy();
    }

    @Override
    public Object get(final Serializable key) {
        return map.get(key);
    }

    @Override
    public CacheElement getCacheElement(final Serializable key) {
        final MapEntry<Serializable, Serializable> mapEntry = map.getMapEntry(key);
        if (null == mapEntry) {
            return null;
        }
        final HazelcastCacheElement cacheElement = new HazelcastCacheElement();
        cacheElement.setCacheName(map.getName());
        cacheElement.setKey(key);
        cacheElement.setVal(mapEntry.getValue());
        cacheElement.setElementAttributes(new HazelcastElementAttributes(mapEntry, getMapConfig(), map));
        return cacheElement;
    }

    @Override
    public ElementAttributes getDefaultElementAttributes() throws OXException {
        return new HazelcastElementAttributes(null, getMapConfig(), map);
    }

    @Override
    public Object getFromGroup(final Serializable key, final String group) {
        try {
            @SuppressWarnings("unchecked") final ConcurrentHashMap<Serializable, Serializable> groupMap = (ConcurrentHashMap<Serializable, Serializable>) map.get(group);
            if (null == groupMap) {
                return null;
            }
            return groupMap.get(key);
        } catch (final ClassCastException e) {
            return null;
        }
    }

    @Override
    public void invalidateGroup(final String group) {
        map.remove(group);
    }

    @Override
    public void put(final Serializable key, final Serializable obj) throws OXException {
        put(key, obj, null);
    }

    @Override
    public void put(final Serializable key, final Serializable val, final ElementAttributes attr) throws OXException {
        map.put(key, val);
    }

    @Override
    public void putInGroup(final Serializable key, final String groupName, final Object value, final ElementAttributes attr) throws OXException {
        try {
            @SuppressWarnings("unchecked") ConcurrentHashMap<Serializable, Serializable> groupMap = (ConcurrentHashMap<Serializable, Serializable>) map.get(groupName);
            if (null == groupMap) {
                ConcurrentHashMap<Serializable, Serializable> ngroupMap = new ConcurrentHashMap<Serializable, Serializable>();
                groupMap = (ConcurrentHashMap<Serializable, Serializable>) map.putIfAbsent(groupName, ngroupMap);
                if (null == groupMap) {
                    groupMap = ngroupMap;
                }
            }
            groupMap.put(key, (Serializable) value);
        } catch (final ClassCastException e) {
            return;
        }
    }

    @Override
    public void putInGroup(final Serializable key, final String groupName, final Serializable value) throws OXException {
        putInGroup(key, groupName, value, null);
    }

    @Override
    public void putSafe(final Serializable key, final Serializable value) throws OXException {
        if (null != map.putIfAbsent(key, value)) {
            throw CacheExceptionCode.FAILED_SAFE_PUT.create();
        }
    }

    @Override
    public void remove(final Serializable key) throws OXException {
        map.remove(key);
    }

    @Override
    public void localRemove(final Serializable key) throws OXException {
        throw new UnsupportedOperationException("HazelcastCache.localRemove()");
    }

    @Override
    public void localPut(final Serializable key, final Serializable value) throws OXException {
        throw new UnsupportedOperationException("HazelcastCache.localPut()");
    }

    @Override
    public void removeFromGroup(final Serializable key, final String group) {
        try {
            @SuppressWarnings("unchecked") final ConcurrentHashMap<Serializable, Serializable> groupMap = (ConcurrentHashMap<Serializable, Serializable>) map.get(group);
            if (null == groupMap) {
                return;
            }
            groupMap.remove(key);
        } catch (final ClassCastException e) {
            // Ignore
        }
    }

    @Override
    public void localRemoveFromGroup(final Serializable key, final String group) {
        throw new UnsupportedOperationException("HazelcastCache.localRemoveFromGroup()");
    }

    @Override
    public void setDefaultElementAttributes(final ElementAttributes attr) throws OXException {
        final MapConfig mapConfig = getMapConfig();
        {
            final long l = attr.getIdleTime();
            if (l > 0) {
                mapConfig.setMaxIdleSeconds((int) l);
            }
        }
        {
            long l = attr.getMaxLifeSeconds();
            if (l > 0) {
                mapConfig.setTimeToLiveSeconds((int) l);
            } else {
                l = attr.getTimeToLiveSeconds();
                if (l > 0) {
                    mapConfig.setTimeToLiveSeconds((int) l);
                }
            }
        }
        {
            final int i = attr.getSize();
            if (i > 0) {
                mapConfig.getMaxSizeConfig().setSize(i);
            }
        }
    }

    @Override
    public CacheStatistics getStatistics() {
        // TODO
        return null;
    }

    @Override
    public CacheKey newCacheKey(final int contextId, final int objectId) {
        return new CacheKeyImpl(contextId, objectId);
    }

    @Override
    public CacheKey newCacheKey(final int contextId, final Serializable... objs) {
        return new CacheKeyImpl(contextId, objs);
    }

}
