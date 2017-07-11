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

package com.openexchange.session.management;

import org.slf4j.Logger;
import com.openexchange.exception.OXException;
import com.openexchange.geolocation.GeoInformation;
import com.openexchange.geolocation.GeoLocationService;
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.session.Session;
import com.openexchange.session.management.osgi.Services;
import com.openexchange.user.UserService;

/**
 * {@link ManagedSession}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.10.0
 */
public interface ManagedSession {

    public String getSessionId();

    public String getIpAddress();

    public String getClient();

    public String getUserAgent();

    public long getLoginTime();

    public int getCtxId();

    public int getUserId();

    public String getLocation();

    public final static class ManagedSessionBuilder {

        private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(ManagedSessionBuilder.class);

        private String sessionId;
        private String ipAddress;
        private String client;
        private String userAgent;
        private long loginTime;
        private int ctxId;
        private int userId;

        public ManagedSessionBuilder() {
            super();
        }

        public ManagedSessionBuilder(Session session) {
            super();
            this.sessionId = session.getSessionID();
            this.ipAddress = session.getLocalIp();
            this.client = session.getClient();
            this.userAgent = (String) session.getParameter(Session.PARAM_USER_AGENT);
            this.loginTime = Long.parseLong((String) session.getParameter(Session.PARAM_LOGIN_TIME));
            this.ctxId = session.getContextId();
            this.userId = session.getUserId();
        }

        public ManagedSessionBuilder setSessionId(String sessionId) {
            this.sessionId = sessionId;
            return this;
        }

        public ManagedSessionBuilder setIpAddress(String ipAddress) {
            this.ipAddress = ipAddress;
            return this;
        }

        public ManagedSessionBuilder setClient(String client) {
            this.client = client;
            return this;
        }

        public ManagedSessionBuilder setUserAgent(String userAgent) {
            this.userAgent = userAgent;
            return this;
        }

        public ManagedSessionBuilder setLoginTime(long loginTime) {
            this.loginTime = loginTime;
            return this;
        }

        public ManagedSessionBuilder setCtxId(int ctxId) {
            this.ctxId = ctxId;
            return this;
        }

        public ManagedSessionBuilder setUserId(int userId) {
            this.userId = userId;
            return this;
        }

        public DefaultManagedSession build() {
            String location = SessionManagementStrings.UNKNOWN_LOCATION;
            GeoLocationService service = Services.getService(GeoLocationService.class);
            UserService userService = Services.getService(UserService.class);
            if (null != userService) {
                try {
                    location = StringHelper.valueOf(userService.getUser(userId, ctxId).getLocale()).getString(SessionManagementStrings.UNKNOWN_LOCATION);
                } catch (OXException e) {
                    LOG.info(e.getMessage());
                }
            }
            if (null != service && null != userService) {
                try {
                    GeoInformation geoInformation = service.getGeoInformation(ipAddress);
                    StringBuilder sb = new StringBuilder();
                    if (geoInformation.hasCity()) {
                        sb.append(geoInformation.getCity());
                    }
                    if (geoInformation.hasCountry()) {
                        sb.append(", ").append(geoInformation.getCountry());
                    }
                    location = sb.toString();
                } catch (OXException e) {
                    LOG.info(e.getMessage());
                }
            }
            return new DefaultManagedSession(sessionId, ipAddress, client, userAgent, loginTime, ctxId, userId, location);
        }
    }

    public final static class DefaultManagedSession implements ManagedSession {

        private final String sessionId;
        private final String ipAddress;
        private final String client;
        private final String userAgent;
        private final long loginTime;
        private final int ctxId;
        private final int userId;
        private final String location;

        public DefaultManagedSession(String sessionId, String ipAddress, String client, String userAgent, long loginTime, int ctxId, int userId, String location) {
            super();
            this.sessionId = sessionId;
            this.ipAddress = ipAddress;
            this.client = client;
            this.userAgent = userAgent;
            this.loginTime = loginTime;
            this.ctxId = ctxId;
            this.userId = userId;
            this.location = SessionManagementStrings.UNKNOWN_LOCATION;
        }

        public DefaultManagedSession(Session session) {
            super();
            this.sessionId = session.getSessionID();
            this.ipAddress = session.getLocalIp();
            this.client = session.getClient();
            this.userAgent = session.getParameter(Session.PARAM_USER_AGENT) != null ? (String) session.getParameter(Session.PARAM_USER_AGENT) : "unknown user-agent";
            this.loginTime = session.getParameter(Session.PARAM_LOGIN_TIME) != null ? Long.parseLong(String.valueOf(session.getParameter(Session.PARAM_LOGIN_TIME))) : -1;
            this.ctxId = session.getContextId();
            this.userId = session.getUserId();
            this.location = SessionManagementStrings.UNKNOWN_LOCATION;
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
        public int getCtxId() {
            return ctxId;
        }

        @Override
        public int getUserId() {
            return userId;
        }

        @Override
        public String getLocation() {
            return location;
        }
    }

}
