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
 *     Copyright (C) 2004-2016 Open-Xchange, Inc.
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

package com.openexchange.oauth.access.impl;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import com.openexchange.oauth.access.OAuthAccess;
import com.openexchange.oauth.access.OAuthAccessRegistry;

/**
 * {@link OAuthAccessRegistryImpl}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class OAuthAccessRegistryImpl implements OAuthAccessRegistry {

    private final ConcurrentMap<OAuthAccessKey, ConcurrentMap<String, OAuthAccess>> map;

    /**
     * Initialises a new {@link OAuthAccessRegistryImpl}.
     */
    public OAuthAccessRegistryImpl() {
        super();
        map = new ConcurrentHashMap<>();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.oauth.access.OAuthAccessRegistry#add(int, int, java.lang.String, com.openexchange.oauth.access.OAuthAccess)
     */
    @Override
    public OAuthAccess add(int contextId, int userId, String accountId, OAuthAccess oauthAccess) {
        OAuthAccessKey key = new OAuthAccessKey(contextId, userId);
        ConcurrentMap<String, OAuthAccess> accessMap = map.get(key);
        if (accessMap == null) {
            ConcurrentMap<String, OAuthAccess> innerMap = new ConcurrentHashMap<>();
            accessMap = map.putIfAbsent(key, innerMap);
            if (accessMap == null) {
                accessMap = innerMap;
            }
        }
        return accessMap.putIfAbsent(accountId, oauthAccess);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.oauth.access.OAuthAccessRegistry#contains(int, int, java.lang.String)
     */
    @Override
    public boolean contains(int contextId, int userId, String accountId) {
        final ConcurrentMap<String, OAuthAccess> access = map.get(new OAuthAccessKey(contextId, userId));
        return access != null && access.containsKey(accountId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.oauth.access.OAuthAccessRegistry#get(int, int, java.lang.String)
     */
    @Override
    public OAuthAccess get(int contextId, int userId, String accountId) {
        final ConcurrentMap<String, OAuthAccess> access = map.get(new OAuthAccessKey(contextId, userId));
        return access == null ? null : access.get(accountId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.oauth.access.OAuthAccessRegistry#removeIfLast(int, int)
     */
    @Override
    public boolean removeIfLast(int contextId, int userId) {
        final ConcurrentMap<String, OAuthAccess> accesses = map.remove(new OAuthAccessKey(contextId, userId));
        if (null == accesses || accesses.isEmpty()) {
            return false;
        }
        for (final OAuthAccess access : accesses.values()) {
            access.dispose();
        }
        return !accesses.isEmpty();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.oauth.access.OAuthAccessRegistry#purgeUserAccess(int, int, java.lang.String)
     */
    @Override
    public boolean purgeUserAccess(int contextId, int userId, String accountId) {
        OAuthAccessKey key = new OAuthAccessKey(contextId, userId);
        final ConcurrentMap<String, OAuthAccess> access = map.get(key);
        if (null == access) {
            return false;
        }
        final OAuthAccess oAuthInfo = access.remove(accountId);
        if (null == oAuthInfo) {
            return false;
        }
        if (access.isEmpty()) {
            map.remove(key);
        }
        return true;
    }

}
