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

package com.openexchange.tools.webdav;

import static com.openexchange.tools.servlet.http.Tools.copyHeaders;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.jdom2.Document;
import org.jdom2.JDOMException;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.fields.Header;
import com.openexchange.ajax.fields.LoginFields;
import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.java.util.UUIDs;
import com.openexchange.log.LogFactory;
import com.openexchange.login.Interface;
import com.openexchange.login.LoginRequest;
import com.openexchange.login.internal.LoginPerformer;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.tools.servlet.http.Authorization.Credentials;
import com.openexchange.tools.servlet.http.Cookies;
import com.openexchange.tools.servlet.http.Tools;
import com.openexchange.tools.webdav.digest.Authorization;
import com.openexchange.tools.webdav.digest.DigestUtility;
import com.openexchange.webdav.WebdavExceptionCode;
import com.openexchange.xml.jdom.JDOMParser;

/**
 * This servlet can be used as super class for all OX webdav servlets.
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein </a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class OXServlet extends WebDavServlet {

    private static final long serialVersionUID = 301910346402779362L;

    private static final transient Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(OXServlet.class));

    /**
     * Simple {@link LoginRequest} implementation.
     */
    private static final class LoginRequestImpl implements LoginRequest {

        private final String login;
        private final HttpServletRequest req;
        private final String userAgent;

        private final String pass;
        private final String client;
        private final Interface interfaze;
        private final String version;

        public LoginRequestImpl(final String login, final String pass, final Interface interfaze, final HttpServletRequest req) {
            super();
            this.client = AJAXServlet.sanitizeParam(req.getParameter(LoginFields.CLIENT_PARAM));
            version = AJAXServlet.sanitizeParam(req.getParameter(LoginFields.VERSION_PARAM));
            userAgent = AJAXServlet.sanitizeParam(req.getParameter("agent"));
            this.login = login;
            this.req = req;
            this.pass = pass;
            this.interfaze = interfaze;
        }

        @Override
        public String getUserAgent() {
            return null == userAgent ? req.getHeader("user-agent") : userAgent;
        }

        @Override
        public String getPassword() {
            return pass;
        }

        @Override
        public String getLogin() {
            return login;
        }

        @Override
        public Interface getInterface() {
            return interfaze;
        }

        @Override
        public String getClientIP() {
            return req.getRemoteAddr();
        }

        @Override
        public String getAuthId() {
            return UUIDs.getUnformattedString(UUID.randomUUID());
        }

        @Override
        public String getClient() {
            return client;
        }

        @Override
        public String getVersion() {
            return version;
        }

        @Override
        public String getHash() {
            return null;
        }

        @Override
        public Map<String, List<String>> getHeaders() {
            return copyHeaders(req);
        }

        @Override
        public com.openexchange.authentication.Cookie[] getCookies() {
            return Tools.getCookieFromHeader(req);
        }

        @Override
        public boolean isSecure() {
            return Tools.considerSecure(req);
        }

        @Override
        public String getServerName() {
            return req.getServerName();
        }

        @Override
        public int getServerPort() {
            return req.getServerPort();
        }

        @Override
        public String getHttpSessionID() {
            return req.getSession(true).getId();
        }

        @Override
        public String getClientToken() {
            return null;
        }

        @Override
        public boolean isTransient() {
            return OXServlet.isTransient(interfaze);
        }
    }

    /**
     * Store the session object under this name in the request.
     */
    private static final String SESSION = OXServlet.class.getName() + "SESSION";

    /**
     * Authentication identifier.
     */
    private static final String basicRealm = "OX WebDAV";

    private static final String digestRealm = "Open-Xchange";

    protected static final String COOKIE_SESSIONID = "sessionid";

    /**
     * Digest type for authorization.
     */
    private static final String DIGEST_AUTH = "digest";

    private static final LoginPerformer loginPerformer = LoginPerformer.getInstance();

    protected OXServlet() {
        super();
    }

    /**
     * Defines if this servlet uses the HTTP Authorization header to identify the user. Return false to deactivate the use of the HTTP
     * Authorization header. Do the authorization with the extending class through the method
     * {@link #doAuth(HttpServletRequest, HttpServletResponse)}.
     */
    protected boolean useHttpAuth() {
        return true;
    }

    protected abstract Interface getInterface();

    @Override
    protected void service(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        // create a new HttpSession it it's missing
        req.getSession(true);
        if (!"TRACE".equals(req.getMethod()) && useHttpAuth() && !doAuth(req, resp, getInterface(), getLoginCustomizer())) {
            return;
        }
        try {
            if (LOG.isTraceEnabled()) {
                LOG.trace("Entering HTTP sub method. Session: " + getSession(req));
            }
            super.service(req, resp);
        } catch (final ServletException e) {
            throw e;
        } catch (final IOException e) {
            throw e;
        } catch (final Exception e) {
            LOG.error(e.getMessage(), e);
            final ServletException se = new ServletException(e.getMessage(), e);
            throw se;
        }
    }

    protected LoginCustomizer getLoginCustomizer() {
        return null;
    }

    public static boolean doAuth(final HttpServletRequest req, final HttpServletResponse resp, final Interface face) throws IOException {
        return doAuth(req, resp, face, null);
    }

    /**
     * Performs authentication.
     *
     * @param req The HTTP servlet request.
     * @param resp The HTTP servlet response.
     * @param face the used interface.
     * @return <code>true</code> if the authentication was successful; otherwise <code>false</code>.
     * @throws IOException If an I/O error occurs
     */
    public static boolean doAuth(final HttpServletRequest req, final HttpServletResponse resp, final Interface face, final LoginCustomizer customizer) throws IOException {
        Session session;
        try {
            session = findSessionByCookie(req, resp);
        } catch (final OXException e) {
            LOG.error(e.getMessage(), e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
            return false;
        }
        if (null == session) {
            /*
             * No session found by cookie
             */
            LoginRequest loginRequest;
            try {
                loginRequest = parseLogin(req, face);
                if (customizer != null) {
                    loginRequest = customizer.modifyLogin(loginRequest);
                }
            } catch (final OXException e) {
                LOG.debug(e.getMessage(), e);
                addUnauthorizedHeader(req, resp);
                resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authorization Required!");
                return false;
            }
            try {
                final Map<String, Object> properties = new HashMap<String, Object>(1);
                session = addSession(loginRequest, properties);
            } catch (final OXException e) {
                if (e.getCategory() == Category.CATEGORY_USER_INPUT) {
                    addUnauthorizedHeader(req, resp);
                    resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authorization Required!");
                } else {
                    LOG.error(e.getMessage(), e);
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
                }
                return false;
            }
            resp.addCookie(new Cookie(COOKIE_SESSIONID, session.getSessionID()));
        } else {
            /*
             * Session found by cookie
             */
            final String address = req.getRemoteAddr();
            if (null == address || !address.equals(session.getLocalIp())) {
                if (LOG.isInfoEnabled()) {
                    LOG.info("Request to server denied for session: " + session.getSessionID() + ". in WebDAV XML interface. Client login IP changed from " + session.getLocalIp() + " to " + address + '.');
                }
                addUnauthorizedHeader(req, resp);
                removeSession(session.getSessionID());
                removeCookie(req, resp, COOKIE_SESSIONID);
                resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authorization Required!");
                return false;
            }
        }
        req.setAttribute(SESSION, session);
        return true;
    }

    private static void removeCookie(final HttpServletRequest req, final HttpServletResponse resp, final String...cookiesToRemove) {
        final Map<String, Cookie> cookies = Cookies.cookieMapFor(req);
        if (cookies == null) {
            return;
        }
        final List<String> cookieNames = Arrays.asList(cookiesToRemove);
        for (final String name : cookieNames) {
            final Cookie cookie = cookies.get(name);
            if (null != cookie) {
                final Cookie respCookie = new Cookie(name, cookie.getValue());
                respCookie.setPath("/");
                respCookie.setMaxAge(0); // delete
                resp.addCookie(respCookie);
            }
        }
    }

    /**
     * @param sessionID
     */
    private static void removeSession(final String sessionID) {
        try {
            ServerServiceRegistry.getInstance().getService(SessiondService.class, true).removeSession(sessionID);
        } catch (final OXException e) {
            // Ignore. Probably we're just about to shut down.
        }
    }

    /**
     * Checks if the client sends a correct digest authorization header.
     *
     * @param auth Authorization header.
     * @return <code>true</code> if the client sent a correct authorization header.
     */
    private static boolean checkForDigestAuthorization(final String auth) {
        if (null == auth) {
            return false;
        }
        if (auth.length() <= DIGEST_AUTH.length()) {
            return false;
        }
        if (!auth.substring(0, DIGEST_AUTH.length()).equalsIgnoreCase(DIGEST_AUTH)) {
            return false;
        }
        return true;
    }

    /**
     * Adds the header to the response message for authorization. Only add this header if the authorization of the user failed.
     *
     * @param resp the response to that the header should be added.
     */
    protected static void addUnauthorizedHeader(final HttpServletRequest req, final HttpServletResponse resp) {
        final StringBuilder builder = new StringBuilder(64);
        builder.append("Basic realm=\"").append(basicRealm).append("\", encoding=\"UTF-8\"");
        resp.setHeader("WWW-Authenticate", builder.toString());
        /*-
         * Digest realm="testrealm@host.com",
         * qop="auth,auth-int",
         * nonce="dcd98b7102dd2f0e8b11d0f600bfb0c093",
         * opaque="5ccc069c403ebaf9f0171e9517f40e41"
         */
        builder.setLength(0);
        builder.append("Digest realm=\"").append(digestRealm).append('"').append(", ");
        builder.append("qop=\"auth,auth-int\"").append(", ");
        builder.append("nonce=\"").append(DigestUtility.getInstance().generateNOnce(req)).append('"').append(", ");
        final String opaque = UUIDs.getUnformattedString(UUID.randomUUID());
        builder.append("opaque=\"").append(opaque).append('"').append(", ");
        builder.append("stale=\"false\"").append(", ");
        builder.append("algorithm=\"MD5\"");
//        resp.addHeader("WWW-Authenticate", builder.toString());
    }

    private static LoginRequest parseLogin(final HttpServletRequest req, final Interface face) throws OXException {
        final String auth = req.getHeader(Header.AUTH_HEADER);
        if (null == auth) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Authorization header missing.");
            }
            throw WebdavExceptionCode.MISSING_HEADER_FIELD.create("Authorization");
        }
        if (com.openexchange.tools.servlet.http.Authorization.checkForBasicAuthorization(auth)) {
            final Credentials creds = com.openexchange.tools.servlet.http.Authorization.decode(auth);
            if (!com.openexchange.tools.servlet.http.Authorization.checkLogin(creds.getPassword())) {
                throw WebdavExceptionCode.EMPTY_PASSWORD.create();
            }
            return new LoginRequestImpl(creds.getLogin(), creds.getPassword(), face, req);
        }
        if (checkForDigestAuthorization(auth)) {
            /*
             * Digest auth
             */
            final DigestUtility digestUtility = DigestUtility.getInstance();
            final Authorization authorization = digestUtility.parseDigestAuthorization(auth);
            /*
             * Determine user by "username"
             */
            final String userName = authorization.getUser();
            final String password = digestUtility.getPasswordByUserName(userName);
            if (!com.openexchange.tools.servlet.http.Authorization.checkLogin(password)) {
                throw WebdavExceptionCode.UNSUPPORTED_AUTH_MECH.create("Digest");
            }
            /*
             * Calculate MD5
             */
            final String serverDigest = digestUtility.generateServerDigest(req, password);
            /*
             * Compare to client "response"
             */
            if (!serverDigest.equals(authorization.getResponse())) {
                throw WebdavExceptionCode.AUTH_FAILED.create(userName);
            }
            /*
             * Return appropriate login request to generate a session
             */
            return new LoginRequestImpl(userName, password, face, req);
        }
        /*
         * No known auth mechanism
         */
        final int pos = auth.indexOf(' ');
        final String mech = pos > 0 ? auth.substring(0, pos) : auth;
        if (LOG.isDebugEnabled()) {
            LOG.debug("Unsupported Authentication header.");
        }
        throw WebdavExceptionCode.UNSUPPORTED_AUTH_MECH.create(mech);
    }

    /**
     * This method tries to create a session for the given user.
     *
     * @param request The login request
     * @param properties The login request properties
     * @return the initialized session or <code>null</code>.
     * @throws OXException if an error occurs while creating the session.
     */
    private static Session addSession(final LoginRequest request, final Map<String, Object> properties) throws OXException {
        return loginPerformer.doLogin(request, properties).getSession();
    }

    private static Session findSessionByCookie(final HttpServletRequest req, final HttpServletResponse resp) throws OXException {
        final Map<String, Cookie> cookies = Cookies.cookieMapFor(req);
        String sessionId = null;
        if (null != cookies) {
            final Cookie cookie = cookies.get(COOKIE_SESSIONID);
            if (null != cookie) {
                sessionId = cookie.getValue();
            }
        }
        if (null == sessionId) {
            return null;
        }
        final Session session = ServerServiceRegistry.getInstance().getService(SessiondService.class, true).getSession(sessionId);
        if (null == session && resp != null) {
            final Cookie cookie = new Cookie(COOKIE_SESSIONID, sessionId);
            cookie.setMaxAge(0);
            resp.addCookie(cookie);
        }
        return session;
    }

    public static Session getSession(final HttpServletRequest req) {
        final Session session = (Session) req.getAttribute(SESSION);
        if (null == session) {
            LOG.error("Somebody gets a null session.");
        }
        return session;
    }

    /**
     * Parses the xml request body and returns a JDOM document.
     *
     * @param req the HttpServletRequest that body should be parsed.
     * @return a JDOM document of the parsed body.
     * @throws JDOMException if JDOM gets an exception
     * @throws IOException if an exception occurs while reading the body.
     */
    protected Document getJDOMDocument(final HttpServletRequest req) throws JDOMException, IOException {
        org.jdom2.Document doc = null;
        if (req.getContentLength() > 0) {
            doc = ServerServiceRegistry.getInstance().getService(JDOMParser.class).parse(req.getInputStream());
        }
        return doc;
    }

    /**
     * Gets a value indicating whether to create a transient session or not, based on the supplied interface.
     *
     * @param iface The interface
     * @return <code>true</code> if the interface can use a transient session, <code>false</code>, otherwise
     */
    protected static boolean isTransient(Interface iface) {
        switch (iface) {
        case CALDAV:
        case CARDDAV:
        case WEBDAV_INFOSTORE:
        case WEBDAV_ICAL:
        case WEBDAV_VCARD:
        case OUTLOOK_UPDATER:
            return true;
        default:
            return false;
        }
    }

}
