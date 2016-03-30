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

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;
import com.googlecode.concurrentlinkedhashmap.Weighers;
import com.openexchange.config.cascade.BasicProperty;
import com.openexchange.threadpool.AbstractTask;
import com.openexchange.threadpool.ThreadPools;

/**
 * {@link PropertyMap} - An in-memory property map with LRU eviction policy.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class PropertyMap {

    private final ConcurrentMap<String, Wrapper> map;
    private final int maxLifeMillis;

    /**
     * Initializes a new {@link PropertyMap}.
     *
     * @param maxCapacity the max capacity
     * @param maxLifeUnits the max life units
     * @param unit the unit
     */
    public PropertyMap(final int maxCapacity, final int maxLifeUnits, final TimeUnit unit) {
        super();
        map = new ConcurrentLinkedHashMap.Builder<String, Wrapper>().maximumWeightedCapacity(maxCapacity).weigher(Weighers.entrySingleton()).build();
        this.maxLifeMillis = (int) unit.toMillis(maxLifeUnits);
    }

    /**
     * Initializes a new {@link PropertyMap}.
     *
     * @param maxCapacity the max capacity
     * @param maxLifeMillis the max life milliseconds
     */
    public PropertyMap(final int maxCapacity, final int maxLifeMillis) {
        this(maxCapacity, maxLifeMillis, TimeUnit.MILLISECONDS);
    }

    /**
     * Removes elapsed entries from map.
     */
    public void shrink() {
        final List<String> removeKeys = new ArrayList<String>(16);
        final long minStamp = System.currentTimeMillis() - maxLifeMillis;
        for (final Entry<String, Wrapper> entry : map.entrySet()) {
            final Wrapper wrapper = entry.getValue();
            if (wrapper.getStamp() < minStamp) {
                removeKeys.add(entry.getKey());
            }
        }
        map.keySet().removeAll(removeKeys);
    }

    /**
     * Put if absent.
     *
     * @param propertyName the property name
     * @param property the property
     * @return The property
     */
    public BasicProperty putIfAbsent(final String propertyName, final BasicProperty property) {
        final Wrapper wrapper = wrapperOf(property);
        Wrapper prev = map.putIfAbsent(propertyName, wrapper);
        if (null == prev) {
            // Successfully put into map
            return null;
        }
        if (prev.elapsed(maxLifeMillis)) {
            if (map.replace(propertyName, prev, wrapper)) {
                // Successfully replaced with elapsed one
                return null;
            }
            prev = map.get(propertyName);
            if (null == prev) {
                prev = map.putIfAbsent(propertyName, wrapper);
                return null == prev ? null : prev.getValue();
            }
            return prev.getValue();
        }
        return prev.getValue();
    }

    /**
     * Gets the size.
     *
     * @return The size
     */
    public int size() {
        return map.size();
    }

    /**
     * Checks if empty flag is set.
     *
     * @return <code>true</code> if empty flag is set; otherwise <code>false</code>
     */
    public boolean isEmpty() {
        return map.isEmpty();
    }

    /**
     * Contains.
     *
     * @param propertyName the property name
     * @return <code>true</code> if successful; otherwise <code>false</code>
     */
    public boolean contains(final String propertyName) {
        return map.containsKey(propertyName);
    }

    /**
     * Gets the property.
     *
     * @param propertyName the property name
     * @return The property or <code>null</code> if absent
     */
    public BasicProperty get(final String propertyName) {
        final Wrapper wrapper = map.get(propertyName);
        if (null == wrapper) {
            return null;
        }
        if (wrapper.elapsed(maxLifeMillis)) {
            map.remove(propertyName);
            ThreadPools.getThreadPool().submit(new ShrinkerTask(this));
            return null;
        }
        return wrapper.getValue();
    }

    /**
     * Puts specified property.
     *
     * @param propertyName the property name
     * @param property the property
     * @return The previous property or <code>null</code>
     */
    public BasicProperty put(final String propertyName, final BasicProperty property) {
        final Wrapper wrapper = map.put(propertyName, wrapperOf(property));
        if (null == wrapper) {
            return null;
        }
        if (wrapper.elapsed(maxLifeMillis)) {
            map.remove(propertyName);
            ThreadPools.getThreadPool().submit(new ShrinkerTask(this));
            return null;
        }
        return wrapper.getValue();
    }

    /**
     * Removes the property.
     *
     * @param propertyName the property name
     * @return The removed property or <code>null</code>
     */
    public BasicProperty remove(final String propertyName) {
        final Wrapper wrapper = map.remove(propertyName);
        if (null == wrapper) {
            return null;
        }
        if (wrapper.elapsed(maxLifeMillis)) {
            map.remove(propertyName);
            ThreadPools.getThreadPool().submit(new ShrinkerTask(this));
            return null;
        }
        return wrapper.getValue();
    }

    /**
     * Clears this map.
     */
    public void clear() {
        map.clear();
    }

    @Override
    public String toString() {
        return map.toString();
    }

    private Wrapper wrapperOf(final BasicProperty value) {
        return new Wrapper(value);
    }

    private static final class Wrapper {

        final BasicProperty value;
        private final long stamp;

        public Wrapper(final BasicProperty value) {
            super();
            this.value = value;
            this.stamp = System.currentTimeMillis();
        }

        public long getStamp() {
            return stamp;
        }

        public boolean elapsed(final int maxLifeMillis) {
            return (System.currentTimeMillis() - stamp) > maxLifeMillis;
        }

        public BasicProperty getIfNotElapsed(final int maxLifeMillis) {
            return elapsed(maxLifeMillis) ? null : value;
        }

        public BasicProperty getValue() {
            return value;
        }

        @Override
        public String toString() {
            return value.toString();
        }

    } // End of class Wrapper

    private static final class ShrinkerTask extends AbstractTask<Object> {

        private final PropertyMap propertyMap;

        ShrinkerTask(PropertyMap propertyMap) {
            super();
            this.propertyMap = propertyMap;
        }

        @Override
        public Object call() throws Exception {
            propertyMap.shrink();
            return null;
        }
    } // End of class Shrinker

}
