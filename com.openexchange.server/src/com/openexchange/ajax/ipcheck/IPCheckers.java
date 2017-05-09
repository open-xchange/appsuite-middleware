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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.ajax.ipcheck;

import static com.openexchange.ajax.SessionUtility.isWhitelistedFromIPCheck;
import com.openexchange.ajax.SessionUtility;
import com.openexchange.ajax.ipcheck.internal.NoneIPChecker;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.sessiond.SessionExceptionCodes;
import com.openexchange.sessiond.SessiondService;

/**
 * {@link IPCheckers} - A utility class for IP checkers.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.4
 */
public class IPCheckers {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(IPCheckers.class);

    /**
     * Initializes a new {@link IPCheckers}.
     */
    private IPCheckers() {
        super();
    }

    /**
     * Checks if IP check is not supposed to be applied since session-associated client is white-listed as per configuration.
     *
     * @param session The associated session
     * @param configuration The IP check configuration
     * @return <code>true</code> if white-listed; otherwise <code>false</code>
     */
    public static boolean isWhitelistedClient(Session session, IPCheckConfiguration configuration) {
        return SessionUtility.isWhitelistedClient(session, configuration.getClientWhitelist());
    }

    /**
     * Checks if IP check is not supposed to be applied since IP addresses are white-listed as per configuration.
     * <p>
     * Either:
     * <ul>
     * <li>White-listed by IP range; see file <code>noipcheck.cnf</code></li>
     * <li>White-listed as new and old IP address reside in the same configured sub-net</li>
     * </ul>
     *
     * @param current The current/changed IP address
     * @param previous The previous IP address
     * @param session The associated session
     * @param configuration The IP check configuration
     * @return <code>true</code> if white-listed; otherwise <code>false</code>
     */
    public static boolean isWhiteListedAddress(String current, String previous, IPCheckConfiguration configuration) {
        return isWhitelistedFromIPCheck(current, configuration.getRanges()) || configuration.getAllowedSubnet().areInSameSubnet(current, previous);
    }

    /**
     * Checks if IP check is not supposed to be applied since IP and/or session characteristics are white-listed.
     * <p>
     * Either:
     * <ul>
     * <li>White-listed by IP range; see file <code>noipcheck.cnf</code></li>
     * <li>White-listed by session-associated client identifier</li>
     * <li>White-listed as new and old IP address reside in the same configured sub-net</li>
     * </ul>
     *
     * @param current The current/changed IP address
     * @param previous The previous IP address
     * @param session The associated session
     * @param configuration The IP check configuration
     * @return <code>true</code> if white-listed; otherwise <code>false</code>
     */
    public static boolean isWhiteListed(String current, String previous, Session session, IPCheckConfiguration configuration) {
        return isWhiteListedAddress(current, previous, configuration) || isWhitelistedClient(session, configuration);
    }

    /**
     * Applies changed IP address to given session
     *
     * @param checkIp <code>true</code> if IP has been checked; otherwise <code>false</code>
     * @param current The changed IP address to apply
     * @param session The session to apply to
     * @param configuration The IP check configuration
     */
    public static void apply(boolean checkIp, String current, Session session, IPCheckConfiguration configuration) {
        if (null != current) {
            if (SessionUtility.isWhitelistedClient(session, configuration.getClientWhitelist())) {
                // Change IP in session so the IMAP NOOP command contains the correct client IP address (Bug #21842)
                updateIPAddress(current, session);
            } else if (!checkIp) {
                // Do not change session's IP address anymore in case of USM/EAS (Bug #29136)
                if (!isUsmEas(session.getClient())) {
                    updateIPAddress(current, session);
                }
            }
        }
    }

    /**
     * Updates the current IP address in specified session
     *
     * @param current The current IP address to set
     * @param session The session to apply to
     * @param effectivelyChecked Whether changed IP has been effectively checked (<code>false</code> for {@link NoneIPChecker})
     * @param whiteListedClient <code>true</code> if session-associated client has been excluded from IP check; otherwise <code>false</code> if IP check happened
     */
    public static void updateIPAddress(String current, Session session, boolean effectivelyChecked, boolean whiteListedClient) {
        if (whiteListedClient) {
            // Change IP in session so the IMAP NOOP command contains the correct client IP address (Bug #21842)
            updateIPAddress(current, session);
        } else if (!effectivelyChecked) {
            // Do not change session's IP address anymore in case of USM/EAS (Bug #29136)
            if (!isUsmEas(session.getClient())) {
                updateIPAddress(current, session);
            }
        }
    }

    /**
     * Updates the current IP address in specified session
     *
     * @param current The current IP address to set
     * @param session The session to apply to
     */
    private static void updateIPAddress(String current, Session session) {
        if (null == current) {
            return;
        }

        SessiondService service = ServerServiceRegistry.getInstance().getService(SessiondService.class);
        if (null != service) {
            String oldIP = session.getLocalIp();
            if (!current.equals(oldIP)) {
                try {
                    service.setLocalIp(session.getSessionID(), current);
                } catch (OXException e) {
                    LOG.info("Failed to update session's IP address. authID: {}, sessionID: {}, old IP address: {}, new IP address: {}", session.getAuthId(), session.getSessionID(), oldIP, current, e);
                }
            }
        }
    }

    private static boolean isUsmEas(String clientId) {
        if (Strings.isEmpty(clientId)) {
            return false;
        }
        final String uc = Strings.toUpperCase(clientId);
        return uc.startsWith("USM-EAS") || uc.startsWith("USM-JSON");
    }

    /**
     * Kicks the specified session.
     *
     * @param current The changed IP address
     * @param session The associated session
     * @throws OXException To actually kick the session
     */
    public static void kick(String current, Session session) throws OXException {
        // kick client with changed IP address
        LOG.info("Request to server denied (IP check activated) for session: {}. Client login IP changed from {} to {} and is not covered by IP white-list or netmask.", session.getSessionID(), session.getLocalIp(), (null == current ? "<missing>" : current));
        throw SessionExceptionCodes.WRONG_CLIENT_IP.create(session.getLocalIp(), null == current ? "<unknown>" : current);
    }

}
