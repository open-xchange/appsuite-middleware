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

package com.openexchange.config.cascade.user.cache;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import org.cliffc.high_scale_lib.NonBlockingHashMap;
import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;
import com.googlecode.concurrentlinkedhashmap.Weighers;
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

    private final ConcurrentMap<Integer, ConcurrentMap<Integer, PropertyMap>> map;

    /**
     * Initializes a new {@link PropertyMapManagement}.
     */
    private PropertyMapManagement() {
        super();
        map = new ConcurrentLinkedHashMap.Builder<Integer, ConcurrentMap<Integer, PropertyMap>>().maximumWeightedCapacity(5000).weigher(Weighers.entrySingleton()).build();
    }

    /**
     * Clears the property management.
     */
    public void clear() {
        map.clear();
    }

    /**
     * Drop caches for given context.
     *
     * @param contextId The context identifier
     */
    public void dropFor(final int contextId) {
        map.remove(Integer.valueOf(contextId));
        LOG.debug("Cleaned user-sensitive property cache for context {}", contextId);
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
        final ConcurrentMap<Integer, PropertyMap> contextMap = map.get(Integer.valueOf(contextId));
        if (null != contextMap) {
            contextMap.remove(Integer.valueOf(userId));
        }
        LOG.debug("Cleaned user-sensitive property cache for user {} in context {}", userId, contextId);
    }

    /**
     * Gets the property map for specified session.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The property map
     */
    public PropertyMap getFor(final int userId, final int contextId) {
        final Integer cid = Integer.valueOf(contextId);
        ConcurrentMap<Integer, PropertyMap> contextMap = map.get(cid);
        if (null == contextMap) {
            final ConcurrentMap<Integer, PropertyMap> newMap = new NonBlockingHashMap<Integer, PropertyMap>(256);
            contextMap = map.putIfAbsent(cid, newMap);
            if (null == contextMap) {
                contextMap = newMap;
            }
        }
        final Integer us = Integer.valueOf(userId);
        PropertyMap propertyMap = contextMap.get(us);
        if (null == propertyMap) {
            final PropertyMap newPropertyMap = new PropertyMap(1024, 60, TimeUnit.SECONDS);
            propertyMap = contextMap.putIfAbsent(us, newPropertyMap);
            if (null == propertyMap) {
                propertyMap = newPropertyMap;
            }
        }
        return propertyMap;
    }

    /**
     * Optionally gets the property map for specified user in given context.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The property map or <code>null</code> if absent
     */
    public PropertyMap optFor(final int userId, final int contextId) {
        final ConcurrentMap<Integer, PropertyMap> contextMap = map.get(Integer.valueOf(contextId));
        if (null == contextMap) {
            return null;
        }
        return contextMap.get(Integer.valueOf(userId));
    }

}
