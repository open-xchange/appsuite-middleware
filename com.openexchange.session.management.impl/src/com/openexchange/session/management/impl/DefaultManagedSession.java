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

package com.openexchange.session.management.impl;

import com.openexchange.session.Session;
import com.openexchange.session.management.ManagedSession;
import com.openexchange.session.management.SessionManagementStrings;

/**
 * {@link DefaultManagedSession}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public class DefaultManagedSession implements ManagedSession {

    /**
     * Creates a new empty builder instance.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Creates a new builder instance pre-filled with values from given session.
     */
    public static Builder builder(Session session) {
        return new Builder(session);
    }

    /**
     * Builds an instance of {@link DefaultManagedSession}.
     */
    public final static class Builder {

        private String sessionId;
        private String ipAddress;
        private String client;
        private String userAgent;
        private long loginTime;
        private long lastActive;
        private String location;
        private Session session;

        Builder() {
            super();
        }

        Builder(Session session) {
            super();
            this.sessionId = session.getSessionID();
            this.ipAddress = session.getLocalIp();
            this.client = session.getClient();
            this.userAgent = (String) session.getParameter(Session.PARAM_USER_AGENT);
            this.loginTime = parseLoginTime(session);
            this.lastActive = parseLocalLastActive(session);
            location = SessionManagementStrings.UNKNOWN_LOCATION;
            this.session = session;
        }

        private static long parseLoginTime(Session session) {
            return parseLongNumber(Session.PARAM_LOGIN_TIME, session);
        }

        private static long parseLocalLastActive(Session session) {
            return parseLongNumber(Session.PARAM_LOCAL_LAST_ACTIVE, session);
        }

        private static long parseLongNumber(String paramName, Session session) {
            Object oNumber = session.getParameter(paramName);
            if (null == oNumber) {
                return 0L;
            }

            if (oNumber instanceof Number) {
                return ((Number) oNumber).longValue();
            }

            try {
                return Long.parseLong(oNumber.toString());
            } catch (NumberFormatException e) {
                // Cannot be parsed to a long value
                return 0L;
            }
        }

        public Builder setSessionId(String sessionId) {
            this.sessionId = sessionId;
            return this;
        }

        public Builder setIpAddress(String ipAddress) {
            this.ipAddress = ipAddress;
            return this;
        }

        public Builder setClient(String client) {
            this.client = client;
            return this;
        }

        public Builder setUserAgent(String userAgent) {
            this.userAgent = userAgent;
            return this;
        }

        public Builder setLoginTime(long loginTime) {
            this.loginTime = loginTime;
            return this;
        }

        public Builder setLastActive(long lastActive) {
            this.lastActive = lastActive;
            return this;
        }

        public Builder setLocation(String location) {
            this.location = location;
            return this;
        }

        public Builder setSession(Session session) {
            this.session = session;
            return this;
        }

        public DefaultManagedSession build() {
            return new DefaultManagedSession(sessionId, ipAddress, client, userAgent, loginTime, lastActive, location, session);
        }
    }

    // -----------------------------------------------------------------------------------------------------------------------------------

    private final String sessionId;
    private final String ipAddress;
    private final String client;
    private final String userAgent;
    private final long loginTime;
    private final long lastActive;
    private final String location;
    private final Session session;

    DefaultManagedSession(String sessionId, String ipAddress, String client, String userAgent, long loginTime, long lastActive, String location, Session session) {
        super();
        this.sessionId = sessionId;
        this.ipAddress = ipAddress;
        this.client = client;
        this.userAgent = userAgent;
        this.loginTime = loginTime;
        this.lastActive = lastActive;
        this.location = location;
        this.session = session;
    }

    @Override
    public String getSessionId() {
        return sessionId;
    }

    @Override
    public String getIpAddress() {
        return ipAddress;
    }

    @Override
    public String getClient() {
        return client;
    }

    @Override
    public String getUserAgent() {
        return userAgent;
    }

    @Override
    public long getLoginTime() {
        return loginTime;
    }

    @Override
    public long getLastActive() {
        return lastActive;
    }

    @Override
    public String getLocation() {
        return location;
    }

    @Override
    public Session getSession() {
        return session;
    }

}
