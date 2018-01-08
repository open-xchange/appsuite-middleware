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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import com.google.common.collect.ImmutableSet;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.geolocation.GeoInformation;
import com.openexchange.geolocation.GeoLocationService;
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.session.Session;
import com.openexchange.session.management.ManagedSession;
import com.openexchange.session.management.SessionManagementService;
import com.openexchange.session.management.SessionManagementStrings;
import com.openexchange.session.management.exception.SessionManagementExceptionCodes;
import com.openexchange.session.management.osgi.Services;
import com.openexchange.sessiond.SessionFilter;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.user.UserService;

/**
 * {@link SessionManagementServiceImpl}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.10.0
 */
public class SessionManagementServiceImpl implements SessionManagementService {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(SessionManagementServiceImpl.class);

    private static final SessionManagementServiceImpl INSTANCE = new SessionManagementServiceImpl();

    /**
     * Gets the instance
     *
     * @return The instance
     */
    public static SessionManagementServiceImpl getInstance() {
        return INSTANCE;
    }

    // ----------------------------------------------------------------------------------------------------------------

    private final AtomicReference<Set<String>> blacklistedClients;

    private SessionManagementServiceImpl() {
        super();
        blacklistedClients = new AtomicReference<Set<String>>(doGetBlacklistedClients());
    }

    /**
     * Reinitializes clients black-list.
     */
    public void reinitBlacklistedClients() {
        blacklistedClients.set(doGetBlacklistedClients());
    }

    @Override
    public Collection<ManagedSession> getSessionsForUser(Session session) throws OXException {
        if (null == session) {
            return Collections.emptyList();
        }

        SessiondService sessiondService = Services.getService(SessiondService.class);
        if (null == sessiondService) {
            throw ServiceExceptionCode.absentService(SessiondService.class);
        }

        Set<String> blackListedClients = getBlacklistedClients();

        Collection<Session> localSessions = sessiondService.getSessions(session.getUserId(), session.getContextId());

        String location = getDefaultLocation(session);

        GeoLocationService geoLocationService = Services.optService(GeoLocationService.class);
        Map<String, String> ip2locationCache = new HashMap<>(localSessions.size());
        List<ManagedSession> result = new ArrayList<>(localSessions.size());
        for (Session s : localSessions) {
            if (false == blackListedClients.contains(s.getClient())) {
                ManagedSession managedSession = DefaultManagedSession.builder(s)
                    .setLocation(optLocationFor(s, location, ip2locationCache, geoLocationService))
                    .build();
                result.add(managedSession);
            }
        }
        return result;
    }

    private String optLocationFor(Session s, String def, Map<String, String> ip2locationCache, GeoLocationService geoLocationService) {
        String ipAddress = s.getLocalIp();

        // Check "cache" first
        String location = ip2locationCache.get(ipAddress);
        if (null != location) {
            return location;
        }

        if (null != geoLocationService) {
            try {
                GeoInformation geoInformation = geoLocationService.getGeoInformation(ipAddress);

                StringBuilder sb = null;
                if (geoInformation.hasCity()) {
                    sb = new StringBuilder(geoInformation.getCity());
                }
                if (geoInformation.hasCountry()) {
                    if (null == sb) {
                        sb = new StringBuilder();
                    } else {
                        sb.append(", ");
                    }
                    sb.append(geoInformation.getCountry());
                }
                if (null != sb) {
                    location = sb.toString();
                    ip2locationCache.put(ipAddress, location);
                    return location;
                }
            } catch (OXException e) {
                LOG.debug("Failed to determine location for session with IP address {}", ipAddress, e);
            }
        }

        ip2locationCache.put(ipAddress, def);
        return def;
    }

    @Override
    public void removeSession(Session session, String sessionIdToRemove) throws OXException {
        SessiondService sessiondService = Services.getService(SessiondService.class);
        StringBuilder sb = new StringBuilder("(&");
        sb.append("(").append(SessionFilter.SESSION_ID).append("=").append(sessionIdToRemove).append(")");
        sb.append("(").append(SessionFilter.CONTEXT_ID).append("=").append(session.getContextId()).append(")");
        sb.append("(").append(SessionFilter.USER_ID).append("=").append(session.getUserId()).append(")");
        sb.append(")");
        try {
            Collection<String> removedSessions = sessiondService.removeSessionsGlobally(SessionFilter.create(sb.toString()));
            if (removedSessions.isEmpty()) {
                throw SessionManagementExceptionCodes.SESSION_NOT_FOUND.create();
            }
        } catch (OXException e) {
            throw SessionManagementExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    private Set<String> getBlacklistedClients() {
        return blacklistedClients.get();
    }

    private Set<String> doGetBlacklistedClients() {
        LeanConfigurationService configService = Services.getService(LeanConfigurationService.class);
        if (null == configService) {
            return Collections.emptySet();
        }

        String value = configService.getProperty(SessionManagementProperty.clientBlacklist);
        if (Strings.isEmpty(value)) {
            return Collections.emptySet();
        }

        String[] clients = Strings.splitByComma(value);
        if (null == clients || clients.length == 0) {
            return Collections.emptySet();
        }

        return ImmutableSet.copyOf(clients);
    }

    private String getDefaultLocation(Session session) {
        // Initialize default value for location
        String location;
        {
            location = SessionManagementStrings.UNKNOWN_LOCATION;
            UserService userService = Services.getService(UserService.class);
            if (null != userService) {
                try {
                    location = StringHelper.valueOf(userService.getUser(session.getUserId(), session.getContextId()).getLocale()).getString(SessionManagementStrings.UNKNOWN_LOCATION);
                } catch (OXException e) {
                    LOG.warn("", e);
                }
            }
        }
        return location;
    }

}
