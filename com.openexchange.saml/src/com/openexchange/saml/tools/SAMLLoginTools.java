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

package com.openexchange.saml.tools;

import static com.openexchange.ajax.AJAXServlet.PARAMETER_SESSION;
import java.util.Map;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import org.apache.http.client.utils.URIBuilder;
import com.openexchange.ajax.LoginServlet;
import com.openexchange.ajax.SessionUtility;
import com.openexchange.ajax.login.HashCalculator;
import com.openexchange.ajax.login.LoginConfiguration;
import com.openexchange.ajax.login.LoginTools;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.notify.hostname.HostnameService;
import com.openexchange.session.Session;
import com.openexchange.sessiond.ExpirationReason;
import com.openexchange.sessiond.SessionExceptionCodes;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.tools.servlet.http.Cookies;
import com.openexchange.tools.servlet.http.Tools;

/**
 * {@link SAMLLoginTools}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.1
 */
public class SAMLLoginTools {

    /**
     * The <code>token</code> parameter name.
     */
    public static final String PARAM_TOKEN = "token";

    /**
     * The <code>loginPath</code> parameter name.
     */
    public static final String PARAM_LOGIN_PATH = "loginPath";

    /**
     * The <code>shard</code> parameter name.
     */
    public static final String PARAM_SHARD = "shard";

    /**
     * The <code>uriFragment</code> parameter name.
     */
    public static final String PARAM_URI_FRAGMENT = "uriFragment";

    /**
     * The <code>samlLogin</code> login action.
     */
    public static final String ACTION_SAML_LOGIN = "samlLogin";

    /**
     * The <code>samlLogout</code> login action.
     */
    public static final String ACTION_SAML_LOGOUT = "samlLogout";

    /**
     * Generates the relative redirect location to enter the web front-end directly with a session.
     *
     * @param session The session
     * @param uiWebPath The path to use
     * @param deepLinkFragment The requested uri fragment to add after the session parameter or <code>null</code>
     */
    public static String buildFrontendRedirectLocation(Session session, String uiWebPath, String deepLinkFragment) {
        URIBuilder location = new URIBuilder();
        setRedirectLocation(location, session, uiWebPath, deepLinkFragment);
        return location.toString();
    }

    /**
     * Generates the absolute redirect location to the front-end.
     *
     * @param httpRequest The HTTP request
     * @param session The session
     * @param uiWebPath The path to use
     * @param hostnameService The {@link HostnameService} if available
     */
    public static String buildAbsoluteFrontendRedirectLocation(HttpServletRequest httpRequest, Session session, String uiWebPath, HostnameService hostnameService) {
        URIBuilder location = new URIBuilder().setScheme(Tools.considerSecure(httpRequest) ? "https" : "http");
        String hostname = null;
        if (hostnameService != null) {
            hostname = hostnameService.getHostname(session.getUserId(), session.getContextId());
        }
        if (hostname == null) {
            hostname = httpRequest.getServerName();
        }

        location.setHost(hostname);
        setRedirectLocation(location, session, uiWebPath, httpRequest.getParameter(SAMLLoginTools.PARAM_URI_FRAGMENT));
        return location.toString();
    }

    public static Cookie getSessionCookie(HttpServletRequest httpRequest, LoginConfiguration loginConfiguration) throws OXException {
        String hash = HashCalculator.getInstance().getHash(httpRequest, LoginTools.parseUserAgent(httpRequest), LoginTools.parseClient(httpRequest, false, loginConfiguration.getDefaultClient()));
        Map<String, Cookie> cookies = Cookies.cookieMapFor(httpRequest);
        return cookies.get(LoginServlet.SESSION_PREFIX + hash);
    }

    /**
     * Gets the (possibly cluster-wide distributed) session object that belongs to given {@code open-xchange-session-<hash>} cookie, if available.
     *
     * @param sessionCookie The session cookie or <code>null</code>
     * @param sessiondService The {@link SessiondService} instance to lookup the session
     * @return The session or <code>null</code> if it doesn't exist or cookie was <code>null</code>
     * @throws OXException  If an unexpected error occurs
     */
    public static Session getSessionForSessionCookie(Cookie sessionCookie, SessiondService sessiondService) throws IllegalArgumentException {
        if (sessionCookie == null) {
            return null;
        }

        return sessiondService.getSession(sessionCookie.getValue());
    }

    /**
     * Validates that the given session matches the given request with regards to the client IP and session secret.
     *
     * @param httpRequest The HTTP request
     * @param session The session
     * @param cookieHash The cookie hash to look up the secret cookie
     * @param loginConfiguration The login configuration
     * @throws {@link SessionExceptionCodes#SESSION_EXPIRED}
     */
    public static void validateSession(HttpServletRequest httpRequest, Session session, String cookieHash, @SuppressWarnings("unused") LoginConfiguration loginConfiguration) throws OXException {
        ExpirationReason optExpirationReason = getExpirationReasonOrNull(httpRequest, session, cookieHash);
        if (optExpirationReason != null) {
            OXException oxe = SessionExceptionCodes.SESSION_EXPIRED.create(session.getSessionID());
            oxe.setProperty(SessionExceptionCodes.OXEXCEPTION_PROPERTY_SESSION_EXPIRATION_REASON, optExpirationReason.getIdentifier());
            throw oxe;
        }
    }

    /**
     * Checks whether the given session is valid in terms of IP check and secret cookie.
     *
     * @param httpRequest The HTTP request
     * @param session The session
     * @param cookieHash The calculated cookie hash
     * @return {@code true} if the session valid, other wise {@code false}
     * @throws OXException
     */
    public static boolean isValidSession(HttpServletRequest httpRequest, Session session, String cookieHash) {
        return getExpirationReasonOrNull(httpRequest, session, cookieHash) == null;
    }

    private static ExpirationReason getExpirationReasonOrNull(HttpServletRequest httpRequest, Session session, String cookieHash) {
        // IP check
        try {
            SessionUtility.checkIP(session, httpRequest.getRemoteAddr());
        } catch (OXException e) {
            return ExpirationReason.IP_CHECK_FAILED;
        }

        // Check secret cookie
        Map<String, Cookie> cookies = Cookies.cookieMapFor(httpRequest);
        Cookie secretCookie = cookies.get(LoginServlet.SECRET_PREFIX + cookieHash);
        if (secretCookie == null) {
            return ExpirationReason.NO_EXPECTED_SECRET_COOKIE;
        }
        if (session.getSecret().equals(secretCookie.getValue()) == false) {
            return ExpirationReason.SECRET_MISMATCH;
        }
        return null;
    }

    /**
     * Determines the host name for redirect URIs.
     *
     * @param hostnameService The {@link HostnameService} or <code>null</code> if unavailable
     * @param httpRequest The HTTP request
     */
    public static String getHostName(HostnameService hostnameService, HttpServletRequest httpRequest) {
        if (hostnameService == null) {
            return httpRequest.getServerName();
        }

        String hostname = hostnameService.getHostname(-1, -1);
        if (hostname == null) {
            return httpRequest.getServerName();
        }

        return hostname;
    }

    /**
     * Sets path, query and fragment at the passed {@link URIBuilder} instance to create
     * an URI that can be used to bootstrap a frontend session with optional deep link.
     *
     * Path and deep link are sanitized before being appended. They are expected to be raw
     * (non-URI-encoded) Strings.
     *
     * @param uri The URI builder
     * @param session The session to jump into
     * @param path The URI path
     * @param deepLink The deep link argument which is appended to the URI fragment as <code>&[deepLink]</code>
     */
    private static void setRedirectLocation(URIBuilder uri, Session session, String path, String deepLink) {
        uri.setPath(path.replaceAll("[\n\r]", ""));

        StringBuilder fragment = new StringBuilder(PARAMETER_SESSION).append('=').append(session.getSessionID());
        fragment.append(sanitizeDeepLinkFragment(deepLink));
        uri.setFragment(fragment.toString());
    }

    private static final String sanitizeDeepLinkFragment(String uriFragment) {
        if (uriFragment == null) {
            return "";
        }

        uriFragment = uriFragment.replaceAll("[\n\r]", "");
        while (uriFragment.length() > 0 && (uriFragment.charAt(0) == '#' || uriFragment.charAt(0) == '&')) {
            uriFragment = uriFragment.substring(1);
        }

        if (uriFragment.length() > 0) {
            uriFragment = "&" + uriFragment;
        }

        return uriFragment;
    }

}
