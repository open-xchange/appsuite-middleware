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

package com.openexchange.ajax.login;

import static com.openexchange.ajax.AJAXServlet.PARAMETER_SESSION;
import static com.openexchange.ajax.fields.LoginFields.APPSECRET;
import static com.openexchange.ajax.fields.LoginFields.AUTHID_PARAM;
import static com.openexchange.ajax.fields.LoginFields.CLIENT_IP_PARAM;
import static com.openexchange.ajax.fields.LoginFields.CLIENT_PARAM;
import static com.openexchange.ajax.fields.LoginFields.PASSWORD_PARAM;
import static com.openexchange.ajax.fields.LoginFields.REDIRECT_URL;
import static com.openexchange.ajax.fields.LoginFields.SHARE_TOKEN;
import static com.openexchange.ajax.fields.LoginFields.TOKEN;
import static com.openexchange.ajax.fields.LoginFields.VERSION_PARAM;
import static com.openexchange.login.Interface.HTTP_JSON;
import static com.openexchange.tools.servlet.http.Tools.copyHeaders;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import com.openexchange.ajax.AJAXUtility;
import com.openexchange.ajax.fields.Header;
import com.openexchange.ajax.fields.LoginFields;
import com.openexchange.ajax.requesthandler.AJAXRequestDataTools;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.java.util.UUIDs;
import com.openexchange.log.LogProperties;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.share.GuestInfo;
import com.openexchange.share.ShareService;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.servlet.http.Tools;

/**
 * Shared methods for login operations.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public final class LoginTools {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(LoginTools.class);

    private LoginTools() {
        super();
    }

    /**
     * URL encodes given string.
     * <p>
     * Using <code>org.apache.commons.codec.net.URLCodec</code>.
     */
    public static String encodeUrl(final String s, final boolean forAnchor) {
        return AJAXUtility.encodeUrl(s, forAnchor);
    }

    private static final Pattern PATTERN_CRLF = Pattern.compile("\r?\n|(?:%0[aA])?%0[dD]");
    private static final Pattern PATTERN_DSLASH = Pattern.compile("(?:/|%2[fF]){2}");

    public static String generateRedirectURL(String uiWebPathParam, String shouldStore, String sessionId, String uiWebPath) {
        String retval = uiWebPathParam;
        if (null == retval) {
            retval = uiWebPath;
        }
        // Prevent HTTP response splitting.
        retval = PATTERN_CRLF.matcher(retval).replaceAll("");
        // All double slash strings ("//") should be replaced by a single slash ("/")
        // since it is interpreted by the Browser as "http://".
        retval = PATTERN_DSLASH.matcher(retval).replaceAll("/");
        retval = addFragmentParameter(retval, PARAMETER_SESSION, sessionId);
        if (shouldStore != null) {
            retval = addFragmentParameter(retval, "store", shouldStore);
        }
        return retval;
    }

    public static String addFragmentParameter(String usedUIWebPath, String param, String value) {
        String retval = usedUIWebPath;
        final int fragIndex = retval.indexOf('#');
        // First get rid off the query String, so we can re-append it later
        final int questionMarkIndex = retval.indexOf('?', fragIndex);
        String query = "";
        if (questionMarkIndex > 0) {
            query = retval.substring(questionMarkIndex);
            retval = retval.substring(0, questionMarkIndex);
        }
        // Now let's see, if this URL already contains a fragment
        if (retval.indexOf('#') < 0) {
            // Apparently it didn't, so we can append our own
            return retval + '#' + AJAXUtility.encodeUrl(param) + '=' + AJAXUtility.encodeUrl(value) + query;
        }
        // Ok, we already have a fragment, let's append a new parameter
        return retval + '&' + AJAXUtility.encodeUrl(param) + '=' + AJAXUtility.encodeUrl(value) + query;
    }

    public static String parseAuthId(HttpServletRequest req, boolean strict) throws OXException {
        return parseParameter(req, AUTHID_PARAM, strict, UUIDs.getUnformattedString(UUID.randomUUID()));
    }

    public static String parseClient(HttpServletRequest req, boolean strict, String defaultClient) throws OXException {
        return parseParameter(req, CLIENT_PARAM, strict, defaultClient);
    }

    public static String parseToken(HttpServletRequest req) throws OXException {
        return parseParameter(req, TOKEN);
    }

    public static String parseAppSecret(HttpServletRequest req) throws OXException {
        return parseParameter(req, APPSECRET);
    }

    public static String parseRedirectUrl(HttpServletRequest req) throws OXException {
        return parseParameter(req, REDIRECT_URL, "");
    }

    public static String parseAutoLogin(HttpServletRequest req, String defaultAutoLogin) {
        return parseParameter(req, LoginFields.AUTOLOGIN_PARAM, defaultAutoLogin);
    }

    public static String parseLanguage(HttpServletRequest req) {
        return parseParameter(req, LoginFields.LANGUAGE_PARAM, "");
    }

    public static boolean parseStoreLanguage(HttpServletRequest req) {
        String value = req.getParameter(LoginFields.STORE_LANGUAGE);
        return AJAXRequestDataTools.parseBoolParameter(value);
    }

    public static boolean parseTransient(HttpServletRequest req) {
        String value = req.getParameter(LoginFields.TRANSIENT);
        return AJAXRequestDataTools.parseBoolParameter(value);
    }

    public static String parseParameter(HttpServletRequest req, String paramName, boolean strict, String fallback) throws OXException {
        final String value = req.getParameter(paramName);
        if (null == value) {
            if (strict) {
                throw AjaxExceptionCodes.MISSING_PARAMETER.create(paramName);
            }
            return fallback;
        }
        return value;
    }

    public static String parseParameter(HttpServletRequest req, String paramName, String fallback) {
        final String value = req.getParameter(paramName);
        if (null == value) {
            return fallback;
        }
        return value;
    }

    public static String parseParameter(HttpServletRequest req, String paramName) throws OXException {
        final String value = req.getParameter(paramName);
        if (null == value) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create(paramName);
        }
        return value;
    }

    public static String parseClientIP(HttpServletRequest req) {
        return parseParameter(req, CLIENT_IP_PARAM, req.getRemoteAddr());
    }

    public static String parseUserAgent(HttpServletRequest req) {
        return parseParameter(req, LoginFields.USER_AGENT, req.getHeader(Header.USER_AGENT));
    }

    /**
     * Parses a login request based on the supplied servlet request and credentials.
     *
     * @param req The underlying servlet request
     * @param login The provided login name
     * @param password The provided password
     * @param strict <code>true</code> to fail on missing version- or client-parameter in the request, <code>false</code>, otherwise
     * @param defaultClient The client identifier to use as fallback if the request does provide contain the "client" parameter
     * @param forceHTTPS
     * @param requiredAuthId <code>true</code> to fail on missing authId-parameter in the request, <code>false</code>, otherwise
     * @return The parsed login request
     * @throws OXException
     */
    public static LoginRequestImpl parseLogin(HttpServletRequest req, String login, String password, boolean strict, String defaultClient, boolean forceHTTPS, boolean requiredAuthId) throws OXException {
        return parseLogin(req, login, password, strict, defaultClient, forceHTTPS, requiredAuthId, (String[])null);
    }

    /**
     * Parses a login request based on the underlying servlet request and provided user credentials.
     *
     * @param req The underlying servlet request
     * @param login The provided login name
     * @param password The provided password
     * @param strict <code>true</code> to fail on missing version- or client-parameter in the request, <code>false</code>, otherwise
     * @param defaultClient The client identifier to use as fallback if the request does provide contain the "client" parameter
     * @param forceHTTPS
     * @param requiredAuthId <code>true</code> to fail on missing authId-parameter in the request, <code>false</code>, otherwise
     * @param additionalsForHash Additional values to include when calculating the client-specific hash for the cookie names, or
     *                           <code>null</code> if not needed
     * @return The parsed login request
     * @throws OXException
     */
    public static LoginRequestImpl parseLogin(HttpServletRequest req, String login, String password, boolean strict, String defaultClient, boolean forceHTTPS, boolean requiredAuthId, String...additionalsForHash) throws OXException {
        final String authId = parseAuthId(req, requiredAuthId);
        final String client = parseClient(req, strict, defaultClient);
        final String version;
        if (null == req.getParameter(VERSION_PARAM)) {
            if (strict) {
                throw AjaxExceptionCodes.MISSING_PARAMETER.create(VERSION_PARAM);
            }
            version = null;
        } else {
            version = req.getParameter(VERSION_PARAM);
        }
        final String clientIP = parseClientIP(req);
        final String userAgent = parseUserAgent(req);
        final Map<String, List<String>> headers = copyHeaders(req);
        final com.openexchange.authentication.Cookie[] cookies = Tools.getCookieFromHeader(req);
        final String httpSessionId = req.getSession(true).getId();
        // Add properties
        {
            LogProperties.putProperty(LogProperties.Name.LOGIN_LOGIN, Strings.abbreviate(login, 256));
            LogProperties.putProperty(LogProperties.Name.LOGIN_CLIENT_IP, clientIP);
            LogProperties.putProperty(LogProperties.Name.LOGIN_USER_AGENT, userAgent);
            LogProperties.putProperty(LogProperties.Name.LOGIN_AUTH_ID, authId);
            LogProperties.putProperty(LogProperties.Name.LOGIN_CLIENT, client);
            LogProperties.putProperty(LogProperties.Name.LOGIN_VERSION, version);
        }
        // Return
        LoginRequestImpl.Builder b = new LoginRequestImpl.Builder().login(login).password(password).clientIP(clientIP);
        b.userAgent(userAgent).authId(authId).client(client).version(version);
        b.hash(HashCalculator.getInstance().getHash(req, userAgent, client, additionalsForHash));
        b.iface(HTTP_JSON).headers(headers).cookies(cookies).secure(Tools.considerSecure(req, forceHTTPS));
        b.serverName(req.getServerName()).serverPort(req.getServerPort()).httpSessionID(httpSessionId);
        b.language(parseLanguage(req)).storeLanguage(parseStoreLanguage(req)).tranzient(parseTransient(req));
        return b.build();
    }

    public static LoginRequestImpl parseLogin(HttpServletRequest req, String loginParamName, boolean strict, String defaultClient, boolean forceHTTPS, boolean disableTrimLogin, boolean requiredAuthId) throws OXException {
        String login = req.getParameter(loginParamName);
        if (null == login) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create(loginParamName);
        }
        if (!disableTrimLogin) {
            login = login.trim();
        }
        String password = req.getParameter(PASSWORD_PARAM);
        if (null == password) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create(PASSWORD_PARAM);
        }
        return parseLogin(req, login, password, strict, defaultClient, forceHTTPS, requiredAuthId);
    }

    /**
     * Updates session's IP address if different to specified IP address. This is only possible if the server is configured to be IP wise
     * insecure. @See configuration property com.openexchange.ajax.login.insecure.
     *
     * @param newIP The possibly new IP address
     * @param session The session to update if IP addresses differ
     */
    public static void updateIPAddress(LoginConfiguration conf, String newIP, Session session) {
        if (conf.isInsecure()) {
            String oldIP = session.getLocalIp();
            if (null != newIP && !newIP.equals(oldIP)) {
                LOG.info("Updating session's IP address. authID: {}, sessionID: {}, old IP address: {}, new IP address: {}", session.getAuthId(), session.getSessionID(), oldIP, newIP);
                SessiondService service = ServerServiceRegistry.getInstance().getService(SessiondService.class);
                if (null != service) {
                    try {
                        service.setLocalIp(session.getSessionID(), newIP);
                    } catch (OXException e) {
                        LOG.info("Failed to update session's IP address. authID: {}, sessionID: {}, old IP address: {}, new IP address: {}", session.getAuthId(), session.getSessionID(), oldIP, newIP, e);
                    }
                }
            }
        }
    }

    public static String[] parseShareInformation(HttpServletRequest req) throws OXException {
        String token = req.getParameter(SHARE_TOKEN);
        if (Strings.isNotEmpty(token)) {
            ShareService shareService = ServerServiceRegistry.getInstance().getService(ShareService.class);
            if (null == shareService) {
                return null;
            }
            GuestInfo guest = shareService.resolveGuest(token);
            int contextId = guest.getContextID();
            int guestId = guest.getGuestID();
            return new String[] { String.valueOf(contextId), String.valueOf(guestId) };
        }
        return null;
    }

}
