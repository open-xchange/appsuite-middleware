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

package com.openexchange.caching.internal;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.jcs.JCS;
import org.apache.jcs.access.CacheAccess;
import org.apache.jcs.access.exception.CacheException;
import org.apache.jcs.access.exception.ObjectExistsException;
import org.apache.jcs.auxiliary.AuxiliaryCache;
import org.apache.jcs.engine.behavior.ICache;
import org.apache.jcs.engine.behavior.ICacheElement;
import org.apache.jcs.engine.behavior.IElementAttributes;
import org.apache.jcs.engine.control.CompositeCache;
import org.apache.jcs.engine.control.event.ElementEvent;
import org.apache.jcs.engine.control.event.behavior.IElementEvent;
import org.apache.jcs.engine.control.event.behavior.IElementEventConstants;
import org.apache.jcs.engine.control.event.behavior.IElementEventHandler;
import org.apache.jcs.engine.control.group.GroupAttrName;
import org.apache.jcs.engine.control.group.GroupId;
import org.apache.jcs.engine.memory.MemoryCache;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.slf4j.Logger;
import com.openexchange.caching.Cache;
import com.openexchange.caching.CacheElement;
import com.openexchange.caching.CacheEventConstant;
import com.openexchange.caching.CacheExceptionCode;
import com.openexchange.caching.CacheKey;
import com.openexchange.caching.CacheStatistics;
import com.openexchange.caching.ElementAttributes;
import com.openexchange.caching.SupportsLocalOperations;
import com.openexchange.caching.internal.cache2jcs.CacheElement2JCS;
import com.openexchange.caching.internal.cache2jcs.CacheStatistics2JCS;
import com.openexchange.caching.internal.cache2jcs.ElementAttributes2JCS;
import com.openexchange.caching.internal.jcs2cache.JCSElementAttributesDelegator;
import com.openexchange.exception.OXException;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;

/**
 * {@link JCSCache} - A cache implementation that uses the <a href="http://jakarta.apache.org/jcs/">JCS</a> caching system.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class JCSCache extends AbstractCache implements Cache, SupportsLocalOperations {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(JCSCache.class);

    private static volatile Field cacheControlField;
    private static Field cacheControlField() {
        Field field = cacheControlField;
        if (null == field) {
            synchronized (JCSCache.class) {
                field = cacheControlField;
                if (null == field) {
                    try {
                        field = CacheAccess.class.getDeclaredField("cacheControl");
                        field.setAccessible(true);
                        cacheControlField = field;
                    } catch (@SuppressWarnings("unused") final Exception e) {
                        return null;
                    }
                }
            }
        }
        return field;
    }

    private static final boolean enableCacheEvents = false;

    // -------------------------------------------------------------------------------------------------------- //

    private final JCS cache;
    private final CompositeCache cacheControl;
    private volatile Boolean localOnly;
    private final MemoryCache memCache;
    private final String region;

    /**
     * Initializes a new {@link JCSCache}
     */
    public JCSCache(final JCS cache, final String region) throws CacheException {
        super();
        this.cache = cache;
        this.region = region;
        // Init CompositeCache reference
        CompositeCache tmp;
        try {
            tmp = (CompositeCache) cacheControlField().get(cache);
        } catch (@SuppressWarnings("unused") final Exception e) {
            tmp = null;
        }
        cacheControl = tmp;
        memCache = null == tmp ? null : tmp.getMemoryCache();

        if (enableCacheEvents) {
            final TIntSet removeEvents = new TIntHashSet(4);
            removeEvents.add(IElementEventConstants.ELEMENT_EVENT_EXCEEDED_IDLETIME_BACKGROUND);
            removeEvents.add(IElementEventConstants.ELEMENT_EVENT_EXCEEDED_IDLETIME_ONREQUEST);
            removeEvents.add(IElementEventConstants.ELEMENT_EVENT_EXCEEDED_MAXLIFE_BACKGROUND);
            removeEvents.add(IElementEventConstants.ELEMENT_EVENT_EXCEEDED_MAXLIFE_ONREQUEST);
            final IElementAttributes defaultElementAttributes = cache.getDefaultElementAttributes();
            defaultElementAttributes.addElementEventHandler(new IElementEventHandler() {

                @Override
                public void handleElementEvent(final IElementEvent event) {
                    final int type = event.getElementEvent();
                    if (removeEvents.contains(type)) {
                        final ElementEvent elementEvent = (ElementEvent) event;
                        final Object source = elementEvent.getSource();
                        if (source instanceof ICacheElement) {
                            final ICacheElement cacheElement = (ICacheElement) source;
                            Serializable key = cacheElement.getKey();
                            if (key instanceof GroupAttrName) {
                                final GroupAttrName groupAttrName = (GroupAttrName) key;
                                postRemove((Serializable) groupAttrName.attrName, groupAttrName.groupId.groupName, true);
                            } else {
                                postRemove(key, null, true);
                            }
                        }
                    }

                }
            });
            cache.setDefaultElementAttributes(defaultElementAttributes);
        }
    }

    @Override
    public boolean isLocal() {
        Boolean localOnly = this.localOnly;
        if (null == localOnly) {
            synchronized (this) {
                localOnly = this.localOnly;
                if (null == localOnly) {
                    /*
                     * check known auxiliaries first
                     */
                    if (JCSCacheServiceInit.getInstance().hasAuxiliary(cacheControl.getCacheName())) {
                        localOnly = Boolean.FALSE;
                    } else {
                        /*
                         * check aux caches field, too
                         */
                        AuxiliaryCache[] tmp;
                        try {
                            final Field auxCachesField = CompositeCache.class.getDeclaredField("auxCaches");
                            auxCachesField.setAccessible(true);
                            tmp = (AuxiliaryCache[]) auxCachesField.get(cacheControl);
                        } catch (@SuppressWarnings("unused") final Exception e) {
                            tmp = null;
                        }
                        localOnly = Boolean.TRUE;
                        if (null != tmp) {
                            for (AuxiliaryCache aux : tmp) {
                                if ((aux != null) && (ICache.LATERAL_CACHE == aux.getCacheType())) {
                                    localOnly = Boolean.FALSE;
                                    break;
                                }
                            }
                        }
                    }
                    this.localOnly = localOnly;
                    LOG.info("Cache ''{}'' is operating in {} mode", cache.getCacheAttributes().getCacheName(), (localOnly.booleanValue() ? "local-only" : "distributed"));
                }
            }
        }
        return localOnly.booleanValue();
    }

    @Override
    public boolean isReplicated() {
        return true;
    }

    @Override
    public boolean isDistributed() {
        return false;
    }

    @Override
    public void clear() throws OXException {
        try {
            cache.clear();
            postClear();
        } catch (org.apache.jcs.access.exception.CacheException e) {
            throw CacheExceptionCode.CACHE_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public void localClear() throws OXException {
        try {
            cacheControl.localRemoveAll();
            postClear();
        } catch (Exception e) {
            throw CacheExceptionCode.CACHE_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public Collection<Serializable> values() {
        final MemoryCache memCache = this.memCache;
        if (null == memCache) {
            return Collections.emptySet();
        }

        final Object[] keys = memCache.getKeyArray();
        if (null == keys || 0 >= keys.length) {
            return Collections.emptySet();
        }

        final int length = keys.length;
        final List<Serializable> list = new ArrayList<Serializable>(length);
        for (int i = 0; i < length; i++) {
            ICacheElement element = cacheControl.get((Serializable) keys[i]);
            if (element != null) {
                list.add(element.getVal());
            }
        }

        return list;
    }

    @Override
    public void dispose() {
        cache.dispose();
    }

    @Override
    public Object get(final Serializable key) {
        if (key == null) {
            throw new IllegalArgumentException("key must not be null");
        }
        return cache.get(key);
    }

    @Override
    public CacheElement getCacheElement(final Serializable key) {
        if (key == null) {
            throw new IllegalArgumentException("key must not be null");
        }
        final ICacheElement cacheElement = cache.getCacheElement(key);
        return cacheElement == null ? null : new CacheElement2JCS(cacheElement);
    }

    @Override
    public ElementAttributes getDefaultElementAttributes() throws OXException {
        try {
            return new ElementAttributes2JCS(cache.getDefaultElementAttributes());
        } catch (org.apache.jcs.access.exception.CacheException e) {
            throw CacheExceptionCode.FAILED_ATTRIBUTE_RETRIEVAL.create(e, e.getMessage());
        }
    }

    @Override
    public Object getFromGroup(final Serializable key, final String group) {
        if (key == null) {
            throw new IllegalArgumentException("key must not be null");
        }
        if (group == null) {
            throw new IllegalArgumentException("group must not be null");
        }
        return cache.getFromGroup(key, group);
    }

    @Override
    public void invalidateGroup(final String group) {
        if (group == null) {
            throw new IllegalArgumentException("group must not be null");
        }
        cache.invalidateGroup(group);
        postRemove(null, group, false);
    }

    @Override
    public void put(final Serializable key, final Serializable object) throws OXException {
        if (key == null) {
            throw new IllegalArgumentException("key must not be null");
        }
        if (object == null) {
            throw new IllegalArgumentException("object must not be null");
        }
        try {
            cache.put(key, object);
        } catch (org.apache.jcs.access.exception.CacheException e) {
            throw CacheExceptionCode.FAILED_PUT.create(e, e.getMessage());
        }
    }

    @Override
    public void put(Serializable key, Serializable object, boolean invalidate) throws OXException {
        if (key == null) {
            throw new IllegalArgumentException("key must not be null");
        }
        if (object == null) {
            throw new IllegalArgumentException("object must not be null");
        }
        if (invalidate) {
            remove(key);
        }
        put(key, object);
    }

    @Override
    public void put(final Serializable key, final Serializable value, final ElementAttributes attr) throws OXException {
        if (key == null) {
            throw new IllegalArgumentException("key must not be null");
        }
        if (value == null) {
            throw new IllegalArgumentException("value must not be null");
        }
        try {
            cache.put(key, value, new JCSElementAttributesDelegator(attr));
        } catch (org.apache.jcs.access.exception.CacheException e) {
            throw CacheExceptionCode.FAILED_PUT.create(e, e.getMessage());
        }
    }

    @Override
    public void put(Serializable key, Serializable value, ElementAttributes attr, boolean invalidate) throws OXException {
        if (key == null) {
            throw new IllegalArgumentException("key must not be null");
        }
        if (value == null) {
            throw new IllegalArgumentException("value must not be null");
        }
        if (invalidate) {
            remove(key);
        }
        put(key, value, attr);
    }

    @Override
    public void putInGroup(final Serializable key, final String groupName, final Serializable value) throws OXException {
        if (key == null) {
            throw new IllegalArgumentException("key must not be null");
        }
        if (groupName == null) {
            throw new IllegalArgumentException("groupName must not be null");
        }
        if (value == null) {
            throw new IllegalArgumentException("value must not be null");
        }
        try {
            cache.putInGroup(key, groupName, value);
        } catch (org.apache.jcs.access.exception.CacheException e) {
            throw CacheExceptionCode.FAILED_PUT.create(e, e.getMessage());
        }
    }

    @Override
    public void putInGroup(Serializable key, String groupName, Serializable value, boolean invalidate) throws OXException {
        if (key == null) {
            throw new IllegalArgumentException("key must not be null");
        }
        if (groupName == null) {
            throw new IllegalArgumentException("groupName must not be null");
        }
        if (value == null) {
            throw new IllegalArgumentException("value must not be null");
        }
        if (invalidate) {
            removeFromGroup(key, groupName);
        }
        putInGroup(key, groupName, value);
    }

    @Override
    public void putInGroup(final Serializable key, final String groupName, final Object value, final ElementAttributes attr) throws OXException {
        if (key == null) {
            throw new IllegalArgumentException("key must not be null");
        }
        if (groupName == null) {
            throw new IllegalArgumentException("groupName must not be null");
        }
        if (value == null) {
            throw new IllegalArgumentException("value must not be null");
        }
        try {
            cache.putInGroup(key, groupName, value, new JCSElementAttributesDelegator(attr));
        } catch (org.apache.jcs.access.exception.CacheException e) {
            throw CacheExceptionCode.FAILED_PUT.create(e, e.getMessage());
        }
    }

    @Override
    public void putInGroup(Serializable key, String groupName, Object value, ElementAttributes attr, boolean invalidate) throws OXException {
        if (key == null) {
            throw new IllegalArgumentException("key must not be null");
        }
        if (groupName == null) {
            throw new IllegalArgumentException("groupName must not be null");
        }
        if (value == null) {
            throw new IllegalArgumentException("value must not be null");
        }
        if (invalidate) {
            removeFromGroup(key, groupName);
        }
        putInGroup(key, groupName, value, attr);
    }

    @Override
    public void putSafe(final Serializable key, final Serializable value) throws OXException {
        if (key == null) {
            throw new IllegalArgumentException("key must not be null");
        }
        if (value == null) {
            throw new IllegalArgumentException("value must not be null");
        }
        try {
            cache.putSafe(key, value);
        } catch (ObjectExistsException e) {
            throw CacheExceptionCode.FAILED_SAFE_PUT.create(e, e.getMessage());
        } catch (org.apache.jcs.access.exception.CacheException e) {
            throw CacheExceptionCode.FAILED_PUT.create(e, e.getMessage());
        }
    }

    @Override
    public void remove(final Serializable key) throws OXException {
        if (key == null) {
            throw new IllegalArgumentException("key must not be null");
        }
        try {
            cache.remove(key);
            postRemove(key, null, false);
        } catch (org.apache.jcs.access.exception.CacheException e) {
            throw CacheExceptionCode.FAILED_REMOVE.create(e, e.getMessage());
        }
    }

    @Override
    public void remove(List<Serializable> keys) throws OXException {
        if (keys == null || keys.isEmpty()) {
            return;
        }
        for (Serializable key : keys) {
            if (key != null) {
                remove(key);
            }
        }
    }

    @Override
    public void localRemove(final Serializable key) throws OXException {
        if (key == null) {
            throw new IllegalArgumentException("key must not be null");
        }
        try {
            cacheControl.localRemove(key);
            postRemove(key, null, false);
        } catch (Exception e) {
            throw CacheExceptionCode.FAILED_REMOVE.create(e, e.getMessage());
        }
    }

    @Override
    public void localPut(final Serializable key, final Serializable value) throws OXException {
        if (key == null) {
            throw new IllegalArgumentException("key must not be null");
        }
        if (value == null) {
            throw new IllegalArgumentException("value must not be null");
        }
        try {
            final org.apache.jcs.engine.CacheElement ce = new org.apache.jcs.engine.CacheElement(cacheControl.getCacheName(), key, value);
            ce.setElementAttributes(cacheControl.getElementAttributes());
            cacheControl.localUpdate(ce);
        } catch (Exception e) {
            throw CacheExceptionCode.FAILED_PUT.create(e, e.getMessage());
        }
    }

    @Override
    public void removeFromGroup(final Serializable key, final String group) {
        if (key == null) {
            throw new IllegalArgumentException("key must not be null");
        }
        if (group == null) {
            throw new IllegalArgumentException("group must not be null");
        }
        cache.remove(key, group);
        postRemove(key, group, false);
    }

    @Override
    public void removeFromGroup(List<Serializable> keys, final String group) {
        if (keys == null || keys.isEmpty()) {
            return;
        }
        if (group == null) {
            throw new IllegalArgumentException("group must not be null");
        }
        for (Serializable key : keys) {
            if (key != null) {
                removeFromGroup(key, group);
            }
        }
    }

    @Override
    public void localRemoveFromGroup(final Serializable key, final String group) {
        if (key == null) {
            throw new IllegalArgumentException("key must not be null");
        }
        if (group == null) {
            throw new IllegalArgumentException("group must not be null");
        }
        final GroupAttrName groupAttrName = getGroupAttrName(group, key);
        this.cacheControl.localRemove(groupAttrName);
        postRemove(key, group, false);
    }

    private GroupAttrName getGroupAttrName(final String group, final Object name) {
        return new GroupAttrName(new GroupId(this.cacheControl.getCacheName(), group), name);
    }

    @Override
    public void setDefaultElementAttributes(final ElementAttributes attr) throws OXException {
        try {
            cache.setDefaultElementAttributes(new JCSElementAttributesDelegator(attr));
        } catch (org.apache.jcs.access.exception.CacheException e) {
            throw CacheExceptionCode.FAILED_ATTRIBUTE_ASSIGNMENT.create(e, e.getMessage());
        }
    }

    @Override
    public CacheKey newCacheKey(final int contextId, final int objectId) {
        return new CacheKeyImpl(contextId, objectId);
    }

    @Override
    public CacheKey newCacheKey(final int contextId, final String... objs) {
        return new CacheKeyImpl(contextId, objs);
    }

    @Override
    public CacheStatistics getStatistics() {
        return new CacheStatistics2JCS(cache.getStatistics());
    }

    @Override
    public Set<?> getGroupKeys(String group) {
        return cache.getGroupKeys(group);
    }

    @Override
    public Set<String> getGroupNames() {
        final MemoryCache memCache = this.memCache;
        if (null == memCache) {
            return Collections.emptySet();
        }
        final Object[] keyArray = memCache.getKeyArray();
        if (null == keyArray || 0 >= keyArray.length) {
            return Collections.emptySet();
        }
        final Set<String> ret = new HashSet<String>(keyArray.length);
        for (final Object key : keyArray) {
            if (key instanceof GroupAttrName) {
                ret.add(((GroupAttrName) key).groupId.groupName);
            }
        }
        return ret;
    }

    @Override
    public Set<?> getAllKeys() throws OXException {
        final MemoryCache memCache = this.memCache;
        if (null == memCache) {
            return Collections.emptySet();
        }

        Object[] keys = memCache.getKeyArray();
        if (null == keys || 0 >= keys.length) {
            return Collections.emptySet();
        }

        final int length = keys.length;
        Set<Object> set = new HashSet<Object>(length);
        for (int i = 0; i < length; i++) {
            set.add(keys[i]);
        }

        return set;
    }

    @Override
    public Set<?> getKeysInRange(int start, int end) throws OXException {
        if (start < 0) {
            throw new OXException(666, "start = " + start);
        }
        if (end < 0) {
            throw new OXException(666, "end = " + end);
        }
        if (start > end) {
            throw new OXException(666, "start(" + start + ") > end(" + end + ")");
        }

        final MemoryCache memCache = this.memCache;
        if (null == memCache) {
            return Collections.emptySet();
        }

        Object[] keys = memCache.getKeyArray();
        if (null == keys || 0 >= keys.length) {
            return Collections.emptySet();
        }

        final int length = Math.min(end, keys.length);
        Set<Object> set = new HashSet<Object>(length - start);
        for (int i = start; i < length; i++) {
            set.add(keys[i]);
        }

        return set;
    }

    private static final String TOPIC_REMOVE = CacheEventConstant.TOPIC_REMOVE;
    private static final String TOPIC_CLEAR = CacheEventConstant.TOPIC_CLEAR;

    private static final String PROP_REGION = CacheEventConstant.PROP_REGION;
    private static final String PROP_KEY = CacheEventConstant.PROP_KEY;
    private static final String PROP_GROUP = CacheEventConstant.PROP_GROUP;
    private static final String PROP_EXCEEDED = CacheEventConstant.PROP_EXCEEDED;

    void postRemove(final Serializable optKey, final String optGroup, final boolean exceeded) {
        if (!enableCacheEvents) {
            return;
        }
        final EventAdmin eventAdmin = EVENT_ADMIN_REF.get();
        if (null != eventAdmin) {
            final Map<String, Object> properties = new HashMap<String, Object>(6);
            properties.put(PROP_REGION, region);
            if (null != optGroup) {
                properties.put(PROP_GROUP, optGroup);
            }
            if (null != optKey) {
                properties.put(PROP_KEY, optKey);
            }
            properties.put(PROP_EXCEEDED, Boolean.valueOf(exceeded));
            eventAdmin.postEvent(new Event(TOPIC_REMOVE, properties));
        }
    }

    void postClear() {
        if (!enableCacheEvents) {
            return;
        }
        final EventAdmin eventAdmin = EVENT_ADMIN_REF.get();
        if (null != eventAdmin) {
            final Map<String, Object> properties = new HashMap<String, Object>(2);
            properties.put(PROP_REGION, region);
            eventAdmin.postEvent(new Event(TOPIC_CLEAR, properties));
        }
    }

    MemoryCache getMemCache() {
        return this.memCache;
    }

}
