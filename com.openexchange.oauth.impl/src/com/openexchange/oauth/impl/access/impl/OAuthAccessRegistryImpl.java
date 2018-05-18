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

package com.openexchange.oauth.impl.access.impl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.oauth.OAuthExceptionCodes;
import com.openexchange.oauth.access.OAuthAccess;
import com.openexchange.oauth.access.OAuthAccessRegistry;

/**
 * {@link OAuthAccessRegistryImpl}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class OAuthAccessRegistryImpl implements OAuthAccessRegistry, Iterable<Map<Integer, OAuthAccess>> {

    private final ConcurrentMap<OAuthAccessKey, Map<Integer, OAuthAccess>> map;
    private final String serviceId;

    /**
     * Initializes a new {@link OAuthAccessRegistryImpl}.
     *
     * @param serviceId The service identifier
     */
    public OAuthAccessRegistryImpl(String serviceId) {
        super();
        if (Strings.isEmpty(serviceId)) {
            throw new IllegalArgumentException("The service identifier can be neither 'null' nor empty");
        }
        map = new ConcurrentHashMap<>();
        this.serviceId = serviceId;
    }

    @Override
    public Iterator<Map<Integer, OAuthAccess>> iterator() {
        return map.values().iterator();
    }

    @Override
    public OAuthAccess addIfAbsent(int contextId, int userId, int oauthAccountId, OAuthAccess oauthAccess) {
        OAuthAccessKey key = new OAuthAccessKey(contextId, userId);

        Map<Integer, OAuthAccess> accesses = map.getOrDefault(key, new HashMap<>());
        OAuthAccess existingAccess = accesses.get(oauthAccountId);
        if (existingAccess != null) {
            return existingAccess;
        }
        accesses.put(oauthAccountId, oauthAccess);
        Map<Integer, OAuthAccess> raced = map.putIfAbsent(key, accesses);
        if (raced != null) {
            return raced.get(oauthAccountId);
        }
        return oauthAccess;
    }

    @Override
    public <V> OAuthAccess addIfAbsent(int contextId, int userId, int oauthAccountId, OAuthAccess oauthAccess, Callable<V> executeIfAdded) throws OXException {
        OAuthAccessKey key = new OAuthAccessKey(contextId, userId);
        Map<Integer, OAuthAccess> accesses = map.getOrDefault(key, new HashMap<>());
        OAuthAccess existingAccess = accesses.get(oauthAccountId);
        if (existingAccess != null) {
            return existingAccess;
        }

        accesses.put(oauthAccountId, oauthAccess);
        Map<Integer, OAuthAccess> raced = map.putIfAbsent(key, accesses);
        if (raced != null) {
            return raced.get(oauthAccountId);
        }

        // Execute task (if any) since given OAuthAccess instance was added
        if (null != executeIfAdded) {
            try {
                executeIfAdded.call();
            } catch (OXException e) {
                throw e;
            } catch (Exception e) {
                throw OAuthExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
            }
        }

        return null;
    }

    @Override
    public boolean contains(int contextId, int userId, int oauthAccountId) {
        Map<Integer, OAuthAccess> accesses = map.get(new OAuthAccessKey(contextId, userId));
        if (accesses == null || accesses.isEmpty()) {
            return false;
        }
        return accesses.containsKey(oauthAccountId);
    }

    @Override
    public OAuthAccess get(int contextId, int userId, int oauthAccountId) {
        Map<Integer, OAuthAccess> accesses = map.get(new OAuthAccessKey(contextId, userId));
        if (accesses == null || accesses.isEmpty()) {
            return null;
        }
        return accesses.get(oauthAccountId);
    }

    @Override
    public boolean removeIfLast(int contextId, int userId) {
        Map<Integer, OAuthAccess> accesses = map.remove(new OAuthAccessKey(contextId, userId));
        if (null == accesses) {
            return false;
        }
        for (OAuthAccess access : accesses.values()) {
            access.dispose();
        }
        return true;
    }

    @Override
    public boolean purgeUserAccess(int contextId, int userId, int oauthAccountId) {
        OAuthAccessKey key = new OAuthAccessKey(contextId, userId);
        Map<Integer, OAuthAccess> accesses = map.get(key);
        if (accesses == null || accesses.isEmpty()) {
            return false;
        }
        boolean purged = accesses.remove(oauthAccountId) != null;
        if (accesses.isEmpty()) {
            map.remove(key);
        }
        return purged;
    }

    @Override
    public String getServiceId() {
        return serviceId;
    }
}
