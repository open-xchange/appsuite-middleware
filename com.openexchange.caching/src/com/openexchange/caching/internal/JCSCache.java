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

package com.openexchange.caching.internal;

import java.io.Serializable;
import java.lang.reflect.Field;
import org.apache.jcs.JCS;
import org.apache.jcs.access.CacheAccess;
import org.apache.jcs.access.exception.ObjectExistsException;
import org.apache.jcs.auxiliary.AuxiliaryCache;
import org.apache.jcs.engine.behavior.ICache;
import org.apache.jcs.engine.behavior.ICacheElement;
import org.apache.jcs.engine.control.CompositeCache;
import org.apache.jcs.engine.control.group.GroupAttrName;
import org.apache.jcs.engine.control.group.GroupId;
import com.openexchange.caching.Cache;
import com.openexchange.caching.CacheElement;
import com.openexchange.caching.CacheExceptionCode;
import com.openexchange.caching.CacheKey;
import com.openexchange.caching.CacheKeyImpl;
import com.openexchange.caching.CacheStatistics;
import com.openexchange.caching.ElementAttributes;
import com.openexchange.caching.SupportsLocalOperations;
import com.openexchange.caching.internal.cache2jcs.CacheElement2JCS;
import com.openexchange.caching.internal.cache2jcs.CacheStatistics2JCS;
import com.openexchange.caching.internal.cache2jcs.ElementAttributes2JCS;
import com.openexchange.caching.internal.jcs2cache.JCSElementAttributesDelegator;
import com.openexchange.exception.OXException;

/**
 * {@link JCSCache} - A cache implementation that uses the <a href="http://jakarta.apache.org/jcs/">JCS</a> caching system.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class JCSCache implements Cache, SupportsLocalOperations {

    private final JCS cache;

    private final CompositeCache cacheControl;

    private volatile Boolean localOnly;

    /**
     * Initializes a new {@link JCSCache}
     */
    public JCSCache(final JCS cache) {
        super();
        this.cache = cache;
        // Init CompositeCache reference
        CompositeCache tmp;
        try {
            final Field cacheControlField = CacheAccess.class.getDeclaredField("cacheControl");
            cacheControlField.setAccessible(true);
            tmp = (CompositeCache) cacheControlField.get(cache);
        } catch (final Exception e) {
            tmp = null;
        }
        cacheControl = tmp;
    }

    @Override
    public boolean isLocal() {
        Boolean localOnly = this.localOnly;
        if (null == localOnly) {
            synchronized (this) {
                localOnly = this.localOnly;
                if (null == localOnly) {
                    AuxiliaryCache[] tmp;
                    try {
                        final Field auxCachesField = CompositeCache.class.getDeclaredField("auxCaches");
                        auxCachesField.setAccessible(true);
                        tmp = (AuxiliaryCache[]) auxCachesField.get(cacheControl);
                    } catch (final Exception e) {
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
                    this.localOnly = localOnly;
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
        } catch (final org.apache.jcs.access.exception.CacheException e) {
            throw CacheExceptionCode.CACHE_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public void dispose() {
        cache.dispose();
    }

    @Override
    public Object get(final Serializable key) {
        return cache.get(key);
    }

    @Override
    public CacheElement getCacheElement(final Serializable key) {
        final ICacheElement cacheElement = cache.getCacheElement(key);
        if (cacheElement == null) {
            return null;
        }
        return new CacheElement2JCS(cacheElement);
    }

    @Override
    public ElementAttributes getDefaultElementAttributes() throws OXException {
        try {
            return new ElementAttributes2JCS(cache.getDefaultElementAttributes());
        } catch (final org.apache.jcs.access.exception.CacheException e) {
            throw CacheExceptionCode.FAILED_ATTRIBUTE_RETRIEVAL.create(e, e.getMessage());
        }
    }

    @Override
    public Object getFromGroup(final Serializable key, final String group) {
        return cache.getFromGroup(key, group);
    }

    @Override
    public void invalidateGroup(final String group) {
        cache.invalidateGroup(group);
    }

    @Override
    public void put(final Serializable key, final Serializable obj) throws OXException {
        try {
            cache.put(key, obj);
        } catch (final org.apache.jcs.access.exception.CacheException e) {
            throw CacheExceptionCode.FAILED_PUT.create(e, e.getMessage());
        }
    }

    @Override
    public void put(final Serializable key, final Serializable val, final ElementAttributes attr) throws OXException {
        try {
            cache.put(key, val, new JCSElementAttributesDelegator(attr));
        } catch (final org.apache.jcs.access.exception.CacheException e) {
            throw CacheExceptionCode.FAILED_PUT.create(e, e.getMessage());
        }
    }

    @Override
    public void putInGroup(final Serializable key, final String groupName, final Serializable value) throws OXException {
        try {
            cache.putInGroup(key, groupName, value);
        } catch (final org.apache.jcs.access.exception.CacheException e) {
            throw CacheExceptionCode.FAILED_PUT.create(e, e.getMessage());
        }
    }

    @Override
    public void putInGroup(final Serializable key, final String groupName, final Object value, final ElementAttributes attr) throws OXException {
        try {
            cache.putInGroup(key, groupName, value, new JCSElementAttributesDelegator(attr));
        } catch (final org.apache.jcs.access.exception.CacheException e) {
            throw CacheExceptionCode.FAILED_PUT.create(e, e.getMessage());
        }
    }

    @Override
    public void putSafe(final Serializable key, final Serializable value) throws OXException {
        try {
            cache.putSafe(key, value);
        } catch (final ObjectExistsException e) {
            throw CacheExceptionCode.FAILED_SAFE_PUT.create(e, e.getMessage());
        } catch (final org.apache.jcs.access.exception.CacheException e) {
            throw CacheExceptionCode.FAILED_PUT.create(e, e.getMessage());
        }
    }

    @Override
    public void remove(final Serializable key) throws OXException {
        try {
            cache.remove(key);
        } catch (final org.apache.jcs.access.exception.CacheException e) {
            throw CacheExceptionCode.FAILED_REMOVE.create(e, e.getMessage());
        }
    }

    @Override
    public void localRemove(final Serializable key) throws OXException {
        try {
            cacheControl.localRemove(key);
        } catch (final Exception e) {
            throw CacheExceptionCode.FAILED_REMOVE.create(e, e.getMessage());
        }
    }

    @Override
    public void localPut(final Serializable key, final Serializable value) throws OXException {
        try {
            final org.apache.jcs.engine.CacheElement ce = new org.apache.jcs.engine.CacheElement(cacheControl.getCacheName(), key, value);
            ce.setElementAttributes(cacheControl.getElementAttributes());
            cacheControl.localUpdate(ce);
        } catch (final Exception e) {
            throw CacheExceptionCode.FAILED_PUT.create(e, e.getMessage());
        }
    }

    @Override
    public void removeFromGroup(final Serializable key, final String group) {
        cache.remove(key, group);
    }

    @Override
    public void localRemoveFromGroup(final Serializable key, final String group) {
        final GroupAttrName groupAttrName = getGroupAttrName(group, key);
        this.cacheControl.localRemove(groupAttrName);
    }

    private GroupAttrName getGroupAttrName(final String group, final Object name) {
        return new GroupAttrName(new GroupId(this.cacheControl.getCacheName(), group), name);
    }

    @Override
    public void setDefaultElementAttributes(final ElementAttributes attr) throws OXException {
        try {
            cache.setDefaultElementAttributes(new JCSElementAttributesDelegator(attr));
        } catch (final org.apache.jcs.access.exception.CacheException e) {
            throw CacheExceptionCode.FAILED_ATTRIBUTE_ASSIGNMENT.create(e, e.getMessage());
        }
    }

    @Override
    public CacheKey newCacheKey(final int contextId, final int objectId) {
        return new CacheKeyImpl(contextId, objectId);
    }

    @Override
    public CacheKey newCacheKey(final int contextId, final Serializable... objs) {
        return new CacheKeyImpl(contextId, objs);
    }

    @Override
    public CacheStatistics getStatistics() {
        return new CacheStatistics2JCS(cache.getStatistics());
    }
}
