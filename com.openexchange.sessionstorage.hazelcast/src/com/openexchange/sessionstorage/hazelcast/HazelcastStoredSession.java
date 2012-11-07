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

package com.openexchange.sessionstorage.hazelcast;

import java.io.Serializable;
import java.util.Map;
import com.openexchange.session.Session;
import com.openexchange.sessionstorage.StoredSession;

/**
 * {@link HazelcastStoredSession}
 * 
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public class HazelcastStoredSession extends StoredSession implements Serializable {

    private static final long serialVersionUID = -2346327568417617677L;

    private long lastAccess;

    /**
     * Initializes a new {@link HazelcastStoredSession}.
     */
    public HazelcastStoredSession(final String sessionId, final String loginName, final String password, final int contextId, final int userId, final String secret, final String login, final String randomToken, final String localIP, final String authId, final String hash, final String client, final Map<String, Object> parameters) {
        super(sessionId, loginName, password, contextId, userId, secret, login, randomToken, localIP, authId, hash, client, parameters);
        this.lastAccess = System.currentTimeMillis();
    }

    /**
     * Initializes a new {@link HazelcastStoredSession}.
     *
     */
    public HazelcastStoredSession(final String sessionId, final String loginName, final String password, final int contextId, final int userId, final String secret, final String login, final String randomToken, final String localIP, final String authId, final String hash, final String client, final Map<String, Object> parameters, final long lastAccess) {
        super(sessionId, loginName, password, contextId, userId, secret, login, randomToken, localIP, authId, hash, client, parameters);
        this.lastAccess = lastAccess;
    }

    /**
     * Initializes a new {@link HazelcastStoredSession}.
     * 
     * @param session
     */
    public HazelcastStoredSession(final Session session) {
        super(session);
        this.lastAccess = System.currentTimeMillis();
    }

    /**
     * Gets the last-accessed time stamp.
     * 
     * @return The last-accessed time stamp
     */
    public long getLastAccess() {
        return lastAccess;
    }

    /**
     * Sets the last-accessed time stamp.
     * 
     * @param lastAccess The last-accessed time stamp
     */
    public void setLastAccess(final long lastAccess) {
        this.lastAccess = lastAccess;
    }

}
