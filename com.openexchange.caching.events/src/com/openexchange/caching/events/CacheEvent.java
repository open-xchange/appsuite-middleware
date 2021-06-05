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

package com.openexchange.caching.events;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import com.openexchange.java.ListSet;

/**
 * {@link CacheEvent}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class CacheEvent implements Serializable {

    /**
     * Creates a new {@link CacheOperation#INVALIDATE} event.
     *
     * @param region The cache region
     * @param groupName The cache group name
     * @param key The key of the affected cache entry
     * @return The cache event
     */
    public static CacheEvent INVALIDATE(String region, String groupName, Serializable key) {
        List<Serializable> keys = new LinkedList<Serializable>();
        keys.add(key);
        return INVALIDATE(region, groupName, keys);
    }

    /**
     * Creates a new {@link CacheOperation#INVALIDATE} event.
     *
     * @param region The cache region
     * @param groupName The cache group name
     * @param keys The keys of the affected cache entries
     * @return The cache event
     */
    public static CacheEvent INVALIDATE(String region, String groupName, List<Serializable> keys) {
        List<Serializable> myKeys = ((keys instanceof LinkedList) || (keys instanceof ArrayList) ? keys : new LinkedList<Serializable>(keys));
        return new CacheEvent(CacheOperation.INVALIDATE, region, myKeys, groupName);
    }

    /**
     * Creates a new {@link CacheOperation#INVALIDATE_GROUP} event.
     *
     * @param region The cache region
     * @param groupName The cache group name
     * @return The cache event
     */
    public static CacheEvent INVALIDATE_GROUP(String region, String groupName) {
        return new CacheEvent(CacheOperation.INVALIDATE_GROUP, region, null, groupName);
    }

    /**
     * Creates a new {@link CacheOperation#CLEAR} event.
     *
     * @param region The cache region
     * @return The cache event
     */
    public static CacheEvent CLEAR(String region) {
        return new CacheEvent(CacheOperation.CLEAR, region, null, null);
    }

    // ----------------------------------------------------------------------------------------------------------- //

    private static final long serialVersionUID = 7172029773641345572L;

    protected final CacheOperation operation;
    protected final ListSet<Serializable> keys;
    protected final String groupName;
    protected final String region;
    protected final transient Condition condition;

    /**
     * Initializes a new {@link CacheEvent}.
     *
     * @param operation The cache operation
     * @param region The cache region
     * @param keys The keys of the affected cache entries
     * @param groupName The cache group name
     */
    public CacheEvent(CacheOperation operation, String region, List<Serializable> keys, String groupName) {
        this(operation, region, keys, groupName, null);
    }

    /**
     * Initializes a new {@link CacheEvent}.
     *
     * @param operation The cache operation
     * @param region The cache region
     * @param keys The keys of the affected cache entries
     * @param groupName The cache group name
     * @param condition The condition to wait for; must not be <code>null</code>
     */
    protected CacheEvent(CacheOperation operation, String region, List<Serializable> keys, String groupName, Condition condition) {
        super();
        this.operation = operation;
        this.region = region;
        this.keys = getPreparedKeys(keys);
        this.groupName = groupName;
        this.condition = condition;
    }

    private static ListSet<Serializable> getPreparedKeys(List<Serializable> keys) {
        if (null == keys) {
            return null;
        }
        ListSet<Serializable> retval = new ListSet<Serializable>(keys.size());
        for (Serializable keyToAdd : keys) {
            retval.add(keyToAdd);
        }
        return retval;
    }

    /**
     * Checks if given cache event could be aggregated to this cache event.
     *
     * @param event The cache vent to aggregate
     * @return <code>true</code> if aggregated; otherwise <code>false</code>
     */
    public boolean aggregate(CacheEvent event) {
        // Check operation
        if (this.operation != event.operation) {
            return false;
        }

        // Check condition
        {
            Condition thisCondition = this.condition;
            if (null == thisCondition) {
                if (null != event.condition) {
                    return false;
                }
            } else if (thisCondition != event.condition) { // Yepp, "==" operator
                return false;
            }
        }

        // Check region name
        {
            String thisRegion = this.region;
            if (null == thisRegion) {
                if (null != event.region) {
                    return false;
                }
            } else if (!thisRegion.equals(event.region)) {
                return false;
            }
        }

        // Check group name
        {
            String thisGroupName = this.groupName;
            if (null == thisGroupName) {
                if (null != event.groupName) {
                    return false;
                }
            } else if (!thisGroupName.equals(event.groupName)) {
                return false;
            }
        }

        // Check keys
        List<Serializable> thisKeys = this.keys;
        if (null == thisKeys) {
            return null == event.keys;
        }
        if (null == event.keys) {
            return false;
        }

        // Add keys if absent
        for (Serializable keyToAdd : event.keys) {
            thisKeys.add(keyToAdd);
        }

        return true;
    }

    /**
     * Gets the operation
     *
     * @return The operation
     */
    public CacheOperation getOperation() {
        return operation;
    }

    /**
     * Gets the keys
     *
     * @return The keys
     */
    public List<Serializable> getKeys() {
        return keys;
    }

    /**
     * Gets the groupName
     *
     * @return The groupName
     */
    public String getGroupName() {
        return groupName;
    }

    /**
     * Gets the region
     *
     * @return The region
     */
    public String getRegion() {
        return region;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(128).append("CacheEvent [");
        sb.append("operation=").append(operation);
        sb.append(", region=").append(region);
        sb.append(", groupName=").append(groupName);
        sb.append(", keys=").append(keys);
        sb.append(']');
        return sb.toString();
    }

}
