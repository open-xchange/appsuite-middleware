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

package com.openexchange.tokenlogin.impl;

import java.util.UUID;
import java.util.concurrent.ConcurrentMap;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.javacodegeeks.concurrent.ConcurrentLinkedHashMap;
import com.openexchange.exception.OXException;
import com.openexchange.java.util.UUIDs;
import com.openexchange.session.Session;
import com.openexchange.tokenlogin.TokenLoginExceptionCodes;
import com.openexchange.tokenlogin.TokenLoginService;


/**
 * {@link TokenLoginServiceImpl} - Implementation of {@code TokenLoginService}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class TokenLoginServiceImpl implements TokenLoginService {

    private volatile String hzMapName;
    private final ConcurrentMap<String, String> token2sessionId;
    private final ConcurrentMap<String, String> sessionId2token;

    /**
     * Initializes a new {@link TokenLoginServiceImpl}.
     */
    public TokenLoginServiceImpl(final int maxIdleTime) {
        super();
        final IdleExpirationPolicy evictionPolicy = new IdleExpirationPolicy(maxIdleTime);
        token2sessionId = new ConcurrentLinkedHashMap<String, String>(1024, 0.75f, 16, Integer.MAX_VALUE, evictionPolicy);
        sessionId2token = new ConcurrentLinkedHashMap<String, String>(1024, 0.75f, 16, Integer.MAX_VALUE, evictionPolicy);
    }

    /**
     * Sets the name of the Hazelcast map.
     *
     * @param hzMapName The map name
     */
    public void setHzMapName(final String hzMapName) {
        this.hzMapName = hzMapName;
    }

    /**
     * Gets the Hazelcast 'token2sessionId' map or <code>null</code> if unavailable.
     */
    private IMap<String, String> hzMap() {
        final String hzMapName = this.hzMapName;
        if (null == hzMapName) {
            return null;
        }
        HazelcastInstance hazelcastInstance = Services.getService(HazelcastInstance.class);
        if (hazelcastInstance == null || !hazelcastInstance.getLifecycleService().isRunning()) {
            return null;
        }
        return hazelcastInstance.getMap(hzMapName);
    }

    private void removeFromHzMap(final String token) {
        final IMap<String, String> hzMap = hzMap();
        if (null != hzMap) {
            hzMap.removeAsync(token);
        }
    }

    private void putToHzMap(String token, final String sessionId) {
        final IMap<String, String> hzMap = hzMap();
        if (null != hzMap) {
            hzMap.putAsync(token, sessionId);
        }
    }

    @Override
    public String acquireToken(final Session session) throws OXException {
        // Only one token per session
        final String sessionId = session.getSessionID();
        String token = sessionId2token.get(sessionId);
        if (null == token) {
            final String newToken = UUIDs.getUnformattedString(UUID.randomUUID());
            token = sessionId2token.putIfAbsent(sessionId, newToken);
            if (null == token) {
                token = newToken;
                token2sessionId.put(token, sessionId);
                putToHzMap(token, sessionId);
            }
        }
        return token;
    }

    @Override
    public Session redeemToken(final String token) throws OXException {
        final String sessionId = token2sessionId.remove(token);
        if (null == sessionId) {
            throw TokenLoginExceptionCodes.NO_SUCH_TOKEN.create(token);
        }
        sessionId2token.remove(sessionId);
        removeFromHzMap(token);
        // TODO: Create duplicate session

        return null;
    }

    /**
     * Removes the token for specified session.
     *
     * @param session The session
     */
    public void removeTokenFor(final Session session) {
        final String token = sessionId2token.remove(session.getSessionID());
        if (null != token) {
            token2sessionId.remove(token);
            removeFromHzMap(token);
        }
    }

}
