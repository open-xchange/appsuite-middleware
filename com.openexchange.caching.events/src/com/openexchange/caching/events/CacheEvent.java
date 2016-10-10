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

package com.openexchange.caching.events;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

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

    private final CacheOperation operation;
    private final List<Serializable> keys;
    private final String groupName;
    private final String region;

    /**
     * Initializes a new {@link CacheEvent}.
     *
     * @param operation The cache operation
     * @param region The cache region
     * @param keys The keys of the affected cache entries
     * @param groupName The cache group name
     */
    public CacheEvent(CacheOperation operation, String region, List<Serializable> keys, String groupName) {
        super();
        this.operation = operation;
        this.region = region;
        this.keys = getPreparedKeys(keys);
        this.groupName = groupName;
    }

    private static List<Serializable> getPreparedKeys(List<Serializable> keys) {
        if (null == keys) {
            return null;
        }
        List<Serializable> retval = new ArrayList<Serializable>(keys.size());
        for (Serializable keyToAdd : keys) {
            if (!retval.contains(keyToAdd)) {
                retval.add(keyToAdd);
            }
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
            if (!thisKeys.contains(keyToAdd)) {
                thisKeys.add(keyToAdd);
            }
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
        return "CacheEvent [operation=" + operation + ", region=" + region + ", keys=" + keys + ", groupName=" + groupName + "]";
    }

}
