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

package com.openexchange.saml.tools;

import static com.openexchange.ajax.AJAXServlet.PARAMETER_SESSION;
import java.util.Map;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import org.apache.http.client.utils.URIBuilder;
import com.openexchange.ajax.AJAXUtility;
import com.openexchange.ajax.LoginServlet;
import com.openexchange.ajax.SessionUtility;
import com.openexchange.ajax.login.LoginConfiguration;
import com.openexchange.ajax.login.LoginTools;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.notify.hostname.HostnameService;
import com.openexchange.session.Session;
import com.openexchange.sessiond.SessionExceptionCodes;
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
     * The <code>samlLogin</code> login action.
     */
    public static final String ACTION_SAML_LOGIN = "samlLogin";

    /**
     * The <code>samlLogout</code> login action.
     */
    public static final String ACTION_SAML_LOGOUT = "samlLogout";

    /**
     * The prefix of the auto-login cookie (<code>open-xchange-saml-</code>).
     */
    public static final String AUTO_LOGIN_COOKIE_PREFIX = "open-xchange-saml-";

    /**
     * Generates the relative redirect location to enter the web frontend directly with a session.
     *
     * @param session The session
     * @param uiWebPath The path to use
     */
    public static String buildFrontendRedirectLocation(Session session, String uiWebPath) {
        String retval = uiWebPath;

        // Prevent HTTP response splitting.
        retval = retval.replaceAll("[\n\r]", "");
        retval = LoginTools.addFragmentParameter(retval, PARAMETER_SESSION, session.getSessionID());
        return retval;
    }

    /**
     * Generates the absolute redirect location to the frontend.
     *
     * @param httpRequest The servlet request
     * @param session The session
     * @param uiWebPath The path to use
     * @param hostnameService The {@link HostnameService} if available
     */
    public static String buildAbsoluteFrontendRedirectLocation(HttpServletRequest httpRequest, Session session, String uiWebPath, HostnameService hostnameService) {
        URIBuilder location = new URIBuilder().setScheme(Tools.considerSecure(httpRequest) ? "https" : "http");
        String hostname = null;
        if (hostnameService != null) {
            hostname = hostnameService.getHostname(-1, -1);
        }
        if (hostname == null) {
            hostname = httpRequest.getServerName();
        }

        location.setHost(hostname).setPath(uiWebPath);

        StringBuilder fragment = new StringBuilder(AJAXUtility.encodeUrl(PARAMETER_SESSION)).append("=").append(AJAXUtility.encodeUrl(session.getSessionID()));
        location.setFragment(fragment.toString());
        return location.toString();
    }

    /**
     * Validates that the given session matches the given request with regards to the client IP
     * and session secret.
     *
     * @param httpRequest The HTTP request
     * @param session The session
     * @param cookieHash The cookie hash to look up the secret cookie
     * @param loginConfiguration The login configuration
     * @throws {@link SessionExceptionCodes#WRONG_CLIENT_IP} or {@link SessionExceptionCodes#WRONG_SESSION_SECRET}
     */
    public static void validateSession(HttpServletRequest httpRequest, Session session, String cookieHash, LoginConfiguration loginConfiguration) throws OXException {
        // IP check
        SessionUtility.checkIP(
            loginConfiguration.isIpCheck(),
            loginConfiguration.getRanges(),
            session,
            httpRequest.getRemoteAddr(),
            loginConfiguration.getIpCheckWhitelist());

        // Check secret cookie
        Map<String, Cookie> cookies = Cookies.cookieMapFor(httpRequest);
        Cookie secretCookie = cookies.get(LoginServlet.SECRET_PREFIX + cookieHash);
        if (secretCookie == null || !session.getSecret().equals(secretCookie.getValue())) {
            throw SessionExceptionCodes.WRONG_SESSION_SECRET.create();
        }
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

}
