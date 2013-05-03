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

package com.openexchange.caching.events;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

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
        return new CacheEvent(CacheOperation.INVALIDATE, region, key, groupName);
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
     * Creates a new cache event from POJO map.
     * 
     * @param map The POJO map
     * @return The resulting cache event
     */
    public static CacheEvent fromPojo(Map<String, Object> map) {
        if (null == map) {
            return null;
        }
        final CacheEvent cacheEvent = new CacheEvent();
        cacheEvent.readPojo(map);
        return cacheEvent;
    }

    private static final long serialVersionUID = 7172029773641345572L;

    private CacheOperation operation;
    private Serializable key;
    private String groupName;
    private String region;

    /**
     * Initializes a new {@link CacheEvent}.
     * 
     * @param operation The cache operation
     * @param region The cache region
     * @param groupName The cache group name
     * @param key The key of the affected cache entry
     */
    public CacheEvent(CacheOperation operation, String region, Serializable key, String groupName) {
        super();
        this.operation = operation;
        this.region = region;
        this.key = key;
        this.groupName = groupName;
    }

    /**
     * Initializes a new {@link CacheEvent}.
     * 
     * @param map The POJO map
     */
    private CacheEvent() {
        super();
    }

    /**
     * Reads the POJO view for this instance.
     *
     * @param pojo The POJO view
     */
    public void readPojo(final Map<String, Object> pojo) {
        if (null == pojo) {
            return;
        }
        {
            final String operationId = (String) pojo.get("__operation");
            if (null != operationId) {
                operation = CacheOperation.cacheOperationFor(operationId);
            }
        }
        {
            final Serializable key = (Serializable) pojo.get("__key");
            if (null != key) {
                this.key = key;
            }
        }
        {
            final String groupName = (String) pojo.get("__groupName");
            if (null != groupName) {
                this.groupName = groupName;
            }
        }
        {
            final String region = (String) pojo.get("__region");
            if (null != region) {
                this.region = region;
            }
        }
    }
    /**
     * Generates the POJO view for this instance.
     *
     * @return The POJO view
     */
    public Map<String, Object> writePojo() {
        final Map<String, Object> m = new LinkedHashMap<String, Object>(4);
        if (null != operation) {
            m.put("__operation", operation.getId());
        }
        if (null != key) {
            m.put("__key", key);
        }
        if (null != groupName) {
            m.put("__groupName", groupName);
        }
        if (null != region) {
            m.put("__region", region);
        }
        return m;
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
     * Sets the operation
     * 
     * @param operation The operation to set
     */
    public void setOperation(CacheOperation operation) {
        this.operation = operation;
    }

    /**
     * Gets the key
     * 
     * @return The key
     */
    public Serializable getKey() {
        return key;
    }

    /**
     * Sets the key
     * 
     * @param key The key to set
     */
    public void setKey(Serializable key) {
        this.key = key;
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
     * Sets the groupName
     * 
     * @param groupName The groupName to set
     */
    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    /**
     * Gets the region
     * 
     * @return The region
     */
    public String getRegion() {
        return region;
    }

    /**
     * Sets the region
     * 
     * @param region The region to set
     */
    public void setRegion(String region) {
        this.region = region;
    }

    @Override
    public String toString() {
        return "CacheEvent [operation=" + operation + ", region=" + region + ", key=" + key + ", groupName=" + groupName + "]";
    }

}
