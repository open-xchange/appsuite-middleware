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

import java.util.Collection;
import java.util.HashMap;
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
public class OAuthAccessRegistryImpl implements OAuthAccessRegistry {

    private final ConcurrentMap<OAuthAccessKey, OAuthAccessMap> map;
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

    /**
     * Disposes all <code>OAuthAccess</code> instances and clears this registry.
     */
    public void disposeAll() {
        for (OAuthAccessMap accesses : map.values()) {
            synchronized (accesses) {
                accesses.invalidate();
            }
        }
        map.clear();
    }

    @Override
    public OAuthAccess addIfAbsent(int contextId, int userId, int oauthAccountId, OAuthAccess oauthAccess) {
        try {
            return addIfAbsent(contextId, userId, oauthAccountId, oauthAccess, null);
        } catch (OXException e) {
            // Cannot occur
            throw new IllegalStateException(e);
        }
    }

    @Override
    public <V> OAuthAccess addIfAbsent(int contextId, int userId, int oauthAccountId, OAuthAccess oauthAccess, Callable<V> executeIfAdded) throws OXException {
        OAuthAccessKey key = new OAuthAccessKey(contextId, userId);

        OAuthAccessMap accesses = map.get(key);
        if (null == accesses) {
            OAuthAccessMap newMap = new OAuthAccessMap();
            accesses = map.putIfAbsent(key, newMap);
            if (null == accesses) {
                accesses = newMap;
            }
        }

        synchronized (accesses) {
            if (accesses.isInvalid()) {
                // Re-execute
                return addIfAbsent(contextId, userId, oauthAccountId, oauthAccess, executeIfAdded);
            }

            OAuthAccess existingAccess = accesses.optAccess(oauthAccountId);
            if (existingAccess != null) {
                return existingAccess;
            }

            accesses.addAccess(oauthAccountId, oauthAccess);

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
    }

    @Override
    public boolean contains(int contextId, int userId, int oauthAccountId) {
        OAuthAccessMap accesses = map.get(new OAuthAccessKey(contextId, userId));
        if (accesses == null) {
            return false;
        }

        synchronized (accesses) {
            return accesses.isInvalid() ? false : accesses.containsAccess(oauthAccountId);
        }
    }

    @Override
    public OAuthAccess get(int contextId, int userId, int oauthAccountId) {
        OAuthAccessMap accesses = map.get(new OAuthAccessKey(contextId, userId));
        if (accesses == null) {
            return null;
        }

        synchronized (accesses) {
            return accesses.isInvalid() ? null : accesses.optAccess(oauthAccountId);
        }
    }

    @Override
    public boolean removeIfLast(int contextId, int userId) {
        OAuthAccessMap accesses = map.remove(new OAuthAccessKey(contextId, userId));
        if (null == accesses) {
            return false;
        }

        synchronized (accesses) {
            if (accesses.isInvalid()) {
                return removeIfLast(contextId, userId);
            }

            for (OAuthAccess access : accesses.allAccesses()) {
                access.dispose();
            }
            return true;
        }
    }

    @Override
    public boolean purgeUserAccess(int contextId, int userId, int oauthAccountId) {
        OAuthAccessKey key = new OAuthAccessKey(contextId, userId);
        OAuthAccessMap accesses = map.get(key);
        if (accesses == null) {
            return false;
        }

        synchronized (accesses) {
            if (accesses.isInvalid()) {
                return purgeUserAccess(contextId, userId, oauthAccountId);
            }

            boolean purged = accesses.removeAccess(oauthAccountId);
            if (accesses.invalidateIfEmpty()) {
                map.remove(key);
            }
            return purged;
        }
    }

    @Override
    public String getServiceId() {
        return serviceId;
    }

    // -------------------------------------------------------------------------------------------------------

    private static class OAuthAccessMap {

        private final Map<Integer, OAuthAccess> map;
        private boolean invalid;

        OAuthAccessMap() {
            super();
            map = new HashMap<>();
            invalid = false;
        }

        boolean isInvalid() {
            return invalid;
        }

        boolean containsAccess(int oauthAccountId) {
            return map.containsKey(oauthAccountId);
        }

        OAuthAccess optAccess(int oauthAccountId) {
            return map.get(oauthAccountId);
        }

        void addAccess(int oauthAccountId, OAuthAccess access) {
            map.put(oauthAccountId, access);
        }

        Collection<OAuthAccess> allAccesses() {
            return map.values();
        }

        boolean removeAccess(int oauthAccountId) {
            return map.remove(oauthAccountId) != null;
        }

        boolean invalidateIfEmpty() {
            if (map.isEmpty()) {
                invalid = true;
                return true;
            }
            return false;
        }

        void invalidate() {
            for (OAuthAccess oAuthAccess : map.values()) {
                if (null != oAuthAccess) {
                    oAuthAccess.dispose();
                }
            }
            map.clear();
            invalid = true;
        }
    }
}
