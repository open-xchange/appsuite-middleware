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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.caching.events.ms.internal;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import com.openexchange.caching.CacheKey;
import com.openexchange.caching.CacheKeyService;
import com.openexchange.caching.events.CacheEvent;
import com.openexchange.caching.events.CacheOperation;

/**
 * {@link CacheEventWrapper}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class CacheEventWrapper {

    private static final AtomicReference<CacheKeyService> CKS_REFERENCE = new AtomicReference<CacheKeyService>();

    /**
     * Sets the specified {@link CacheKeyService}.
     *
     * @param service The {@link CacheKeyService}
     */
    public static void setCacheKeyService(CacheKeyService service) {
        CKS_REFERENCE.set(service);
    }

    /**
     * Wraps the supplied cache event into a pojo map.
     *
     * @param cacheEvent The cache event to wrap
     * @return The wrapped cache event
     */
    public static Map<String, Serializable> wrap(CacheEvent cacheEvent) {
        if (null == cacheEvent) {
            return null;
        }
        final Map<String, Serializable> m = new LinkedHashMap<String, Serializable>(4);
        if (null != cacheEvent.getOperation()) {
            m.put("__operation", cacheEvent.getOperation().getId());
        }
        if (null != cacheEvent.getKey()) {
            m.put("__key", wrapKey(cacheEvent.getKey()));
        }
        if (null != cacheEvent.getGroupName()) {
            m.put("__groupName", cacheEvent.getGroupName());
        }
        if (null != cacheEvent.getRegion()) {
            m.put("__region", cacheEvent.getRegion());
        }
        return m;
    }

    /**
     * Unwraps a cache event from the supplied pojo map.
     *
     * @param map The wrapped cache event
     * @return The cache event
     */
    public static CacheEvent unwrap(Map<String, Serializable> map) {
        if (null == map) {
            return null;
        }
        CacheOperation operation = null;
        String operationId = (String) map.get("__operation");
        if (null != operationId) {
            operation = CacheOperation.cacheOperationFor(operationId);
        }
        Serializable key = unwrapKey(map.get("__key"));
        String groupName = (String) map.get("__groupName");
        String region = (String) map.get("__region");
        return new CacheEvent(operation, region, key, groupName);
    }

    private static Serializable wrapKey(Serializable serializable) {
        if (null != serializable && CacheKey.class.isInstance(serializable)) {
            CacheKey cacheKey = (CacheKey)serializable;
            final ArrayList<Serializable> ret = new ArrayList<Serializable>(Arrays.asList(cacheKey.getKeys()));
            ret.add(0, Integer.valueOf(cacheKey.getContextId()));
            return ret;
        }
        return serializable;
    }

    private static Serializable unwrapKey(Serializable serializable) {
        if (null != serializable && ArrayList.class.isInstance(serializable)) {
            ArrayList<Serializable> cacheKeyArray = (ArrayList<Serializable>)serializable;
            if (1 < cacheKeyArray.size()) {
                CacheKeyService cacheKeyService = CKS_REFERENCE.get();
                if (null != cacheKeyService) {
                    int contextID = ((Integer)cacheKeyArray.get(0)).intValue();
                    Serializable[] objs = new Serializable[cacheKeyArray.size() - 1];
                    for (int i = 0; i < objs.length; i++) {
                        objs[i] = cacheKeyArray.get(i+1);
                    }
                    return cacheKeyService.newCacheKey(contextID, objs);
                }
            }
        }
        return serializable;
    }

    private CacheEventWrapper() {
        super();
    }

}
