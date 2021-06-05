/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.ajax.ipcheck;

import static com.openexchange.ajax.SessionUtility.isWhitelistedFromIPCheck;
import static com.openexchange.sessiond.ExpirationReason.IP_CHECK_FAILED;
import com.openexchange.ajax.SessionUtility;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.DefaultSessionAttributes;
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
     * @param whiteListedClient <code>true</code> if session-associated client has been excluded from IP check; otherwise <code>false</code> if IP check happened
     */
    public static boolean updateIPAddress(String current, Session session, boolean whiteListedClient) {
        if (whiteListedClient || false == isUsmEas(session.getClient())) { // Do not change session's IP address anymore in case of USM/EAS (Bug #29136)
            // Change IP in session so the IMAP NOOP command contains the correct client IP address (Bug #21842)
            return updateIPAddress(current, session);
        }
        return false;
    }

    /**
     * Updates the current IP address in specified session
     *
     * @param current The current IP address to set
     * @param session The session to apply to
     */
    private static boolean updateIPAddress(String current, Session session) {
        if (null == current) {
            return false;
        }

        SessiondService service = ServerServiceRegistry.getInstance().getService(SessiondService.class);
        if (null == service) {
            LOG.debug("The '{}' is absent. The session was not updated with the current IP '{}'", SessiondService.class.getSimpleName(), current);
            return false;
        }
        String oldIP = session.getLocalIp();
        if (current.equals(oldIP)) {
            LOG.debug("The session's IP '{}' is already up-to-date", current);
            return false;
        }
        try {
            service.setSessionAttributes(session.getSessionID(), DefaultSessionAttributes.builder().withLocalIp(current).build());
            LOG.debug("Successfully updated the session's IP address to '{}'", current);
            return true;
        } catch (OXException e) {
            LOG.info("Failed to update session's IP address. authID: {}, sessionID: {}, old IP address: {}, new IP address: {}", session.getAuthId(), session.getSessionID(), oldIP, current, e);
        }
        return false;
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
        OXException oxe = SessionExceptionCodes.SESSION_EXPIRED.create(session.getSessionID());
        oxe.setProperty(SessionExceptionCodes.OXEXCEPTION_PROPERTY_SESSION_EXPIRATION_REASON, IP_CHECK_FAILED.getIdentifier());
        throw oxe;
    }
}
