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

package com.openexchange.oauth.access;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import com.openexchange.java.Strings;

/**
 * {@link OAuthAccessRegistry}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class OAuthAccessRegistry {

    private final ConcurrentMap<OAuthAccessKey, OAuthAccess> map;
    private final String serviceId;

    /**
     * Initialises a new {@link OAuthAccessRegistry}.
     * 
     * @param serviceId the service identifier
     */
    public OAuthAccessRegistry(String serviceId) {
        super();
        if (Strings.isEmpty(serviceId)) {
            throw new IllegalArgumentException("The service identifier can be neither 'null' nor empty");
        }
        map = new ConcurrentHashMap<>();
        this.serviceId = serviceId;
    }

    /**
     * Adds the specified {@link OAuthAccess} to the registry
     * 
     * @param contextId The context identifier
     * @param userId The user identifier
     * @param accountId The account identifier
     * @param oauthAccess The {@link OAuthAccess}
     * @return The previous associated {@link OAuthAccess}, or <code>null</code> if there was none
     */
    public OAuthAccess add(int contextId, int userId, OAuthAccess oauthAccess) {
        OAuthAccessKey key = new OAuthAccessKey(contextId, userId);
        return map.putIfAbsent(key, oauthAccess);
    }

    /**
     * Checks the presence of the {@link OAuthAccess} associated with the givent user/context/account tuple
     * 
     * @param contextId The context identifier
     * @param userId The user identifier
     * @param accountId The account identifier
     * @return <code>true if such an {@link OAuthAccess} is present; <code>false</code> otherwise
     */
    public boolean contains(int contextId, int userId) {
        OAuthAccess access = map.get(new OAuthAccessKey(contextId, userId));
        return access != null;
    }

    /**
     * Retrieves the {@link OAuthAccess} associated with the given user/context/account tuple
     * 
     * @param contextId The context identifier
     * @param userId The user identifier
     * @param accountId The account identifier
     * @return The {@link OAuthAccess} that is associated with the tuple, or <code>null</code> if none exists
     */
    public OAuthAccess get(int contextId, int userId) {
        return map.get(new OAuthAccessKey(contextId, userId));
    }

    /**
     * Removes the {@link OAuthAccess} associated with the specified user/context tuple, if no more accesses for that tuple are present
     * 
     * @param contextId The context identifier
     * @param userId The user identifier
     * @return <code>true</code> if an {@link OAuthAccess} for the specified tuple was found and removed; <code>false</code> otherwise
     */
    public boolean removeIfLast(int contextId, int userId) {
        OAuthAccess access = map.remove(new OAuthAccessKey(contextId, userId));
        if (null == access) {
            return false;
        }
        access.dispose();
        return true;
    }

    /**
     * Purges the {@link OAuthAccess} associated with the specified user/context/account tuple.
     * 
     * @param contextId The context identifier
     * @param userId The user identifier
     * @param accountId The account identifier
     * @return <code>true</code> if an {@link OAuthAccess} for the specified tuple was found and purged; <code>false</code> otherwise
     */
    public boolean purgeUserAccess(int contextId, int userId) {
        OAuthAccessKey key = new OAuthAccessKey(contextId, userId);
        return map.remove(key) != null;
    }

    /**
     * Returns the service identifier of this registry
     * 
     * @return the service identifier of this registry
     */
    public String getServiceId() {
        return serviceId;
    }
}
