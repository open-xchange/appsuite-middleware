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

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import com.google.common.cache.CacheBuilder;
import com.openexchange.config.cascade.BasicProperty;
import com.openexchange.threadpool.AbstractTask;
import com.openexchange.threadpool.ThreadPools;

/**
 * {@link PropertyMap} - An in-memory property map with LRU eviction policy.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class PropertyMap {

    private final com.google.common.cache.Cache<String, Wrapper> map;
    private final int maxLifeMillis;

    /**
     * Initializes a new {@link PropertyMap}.
     *
     * @param maxLifeUnits the max life units
     * @param unit the unit
     */
    public PropertyMap(int maxLifeUnits, TimeUnit unit) {
        super();
        map = CacheBuilder.newBuilder().expireAfterAccess(30, TimeUnit.MINUTES).build();
        this.maxLifeMillis = (int) unit.toMillis(maxLifeUnits);
    }

    /**
     * Initializes a new {@link PropertyMap}.
     *
     * @param maxLifeMillis the max life milliseconds
     */
    public PropertyMap(int maxLifeMillis) {
        this(maxLifeMillis, TimeUnit.MILLISECONDS);
    }

    /**
     * Removes elapsed entries from map.
     */
    public void shrink() {
        ConcurrentMap<String, Wrapper> map = this.map.asMap();
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
        ConcurrentMap<String, Wrapper> map = this.map.asMap();
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
        return (int) map.size();
    }

    /**
     * Checks if empty flag is set.
     *
     * @return <code>true</code> if empty flag is set; otherwise <code>false</code>
     */
    public boolean isEmpty() {
        return size() <= 0;
    }

    /**
     * Contains.
     *
     * @param propertyName the property name
     * @return <code>true</code> if successful; otherwise <code>false</code>
     */
    public boolean contains(final String propertyName) {
        return map.getIfPresent(propertyName) != null;
    }

    /**
     * Gets the property.
     *
     * @param propertyName the property name
     * @return The property or <code>null</code> if absent
     */
    public BasicProperty get(final String propertyName) {
        final Wrapper wrapper = map.getIfPresent(propertyName);
        if (null == wrapper) {
            return null;
        }
        if (wrapper.elapsed(maxLifeMillis)) {
            map.invalidate(propertyName);
            ThreadPools.submitElseExecute(new ShrinkerTask(this));
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
        ConcurrentMap<String, Wrapper> map = this.map.asMap();
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
        ConcurrentMap<String, Wrapper> map = this.map.asMap();
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
        map.invalidateAll();
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

        @SuppressWarnings("unused")
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
