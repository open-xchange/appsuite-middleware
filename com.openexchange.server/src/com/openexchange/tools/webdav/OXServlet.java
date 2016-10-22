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

package com.openexchange.tools.webdav;

import static com.openexchange.tools.servlet.http.Tools.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.jdom2.Document;
import org.jdom2.JDOMException;
import com.google.common.net.HttpHeaders;
import com.openexchange.ajax.AJAXUtility;
import com.openexchange.ajax.fields.Header;
import com.openexchange.ajax.fields.LoginFields;
import com.openexchange.ajax.login.LoginTools;
import com.openexchange.ajax.requesthandler.oauth.OAuthConstants;
import com.openexchange.authentication.LoginExceptionCodes;
import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.framework.request.RequestContextHolder;
import com.openexchange.java.util.UUIDs;
import com.openexchange.log.LogProperties;
import com.openexchange.login.Interface;
import com.openexchange.login.LoginRequest;
import com.openexchange.login.internal.LoginPerformer;
import com.openexchange.oauth.provider.exceptions.OAuthInvalidTokenException;
import com.openexchange.oauth.provider.resourceserver.OAuthAccess;
import com.openexchange.oauth.provider.resourceserver.OAuthResourceService;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.tools.servlet.http.Authorization.Credentials;
import com.openexchange.tools.servlet.http.Cookies;
import com.openexchange.tools.servlet.http.Tools;
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

    private static final transient org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(OXServlet.class);

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
            this.client = AJAXUtility.sanitizeParam(req.getParameter(LoginFields.CLIENT_PARAM));
            version = AJAXUtility.sanitizeParam(req.getParameter(LoginFields.VERSION_PARAM));
            userAgent = AJAXUtility.sanitizeParam(req.getParameter("agent"));
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
            HttpSession session = req.getSession(false);
            return null != session ? session.getId() : null;
        }

        @Override
        public String getClientToken() {
            return null;
        }

        @Override
        public boolean isTransient() {
            return OXServlet.isTransient(interfaze);
        }

        @Override
        public String getLanguage() {
            return LoginTools.parseLanguage(req);
        }

        @Override
        public boolean isStoreLanguage() {
            return LoginTools.parseStoreLanguage(req);
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

    protected static final String COOKIE_SESSIONID = "sessionid";

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

    /**
     * Defines if this servlet supports OAuth access, i.e. the bearer authentication scheme may be used to provide an OAuth 2.0 access
     * token. OAuth is only taken into account, if {@link #useHttpAuth()} returns <code>true</code>. If OAuth is allowed you are responsible
     * on your own to check the granted scope on every request! After successful OAuth authentication you'll find the according {@link OAuthAccess}
     * instances as attribute on the servlet request under the {@link OAuthConstants#PARAM_OAUTH_ACCESS} key.
     */
    protected boolean allowOAuthAccess() {
        return false;
    }

    private OAuthResourceService getOAuthResourceService() {
        return ServerServiceRegistry.getInstance().getService(OAuthResourceService.class);
    }

    /**
     * Gets a value indicating whether this servlet uses HTTP cookies to associate consecutive requests to the same server session.
     * Otherwise, a short-living server session is used for requests from the same client that expires a short while after the last
     * request of that client.
     * <p/>
     * Override if applicable, e.g. if the client does not use cookies anyway, it truly makes sense to return <code>false</code> here.
     * <p/>
     * When using the default value <code>true</code>, it's up to the subclass to care about removing the client cookies and triggering
     * the session removal / logout.
     *
     * @return <code>true</code> if HTTP cookies are used (default), <code>false</code>, otherwise.
     */
    protected boolean useCookies() {
        return true;
    }

    protected abstract Interface getInterface();

    @Override
    protected void service(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        if (useCookies()) {
            // create a new HttpSession it it's missing
            req.getSession(true);
        }
        if (!"TRACE".equals(req.getMethod()) && useHttpAuth() && !authenticate(req, resp)) {
            return;
        }
        Session session = getSession(req);
        RequestContextHolder.set(new WebDAVRequestContext(req, session));
        try {
            if (session != null) {
                LogProperties.putSessionProperties(session);
                LOG.trace("Entering HTTP sub method. Session: {}", session);
            }
            super.service(req, resp);
        } catch (final ServletException e) {
            throw e;
        } catch (final IOException e) {
            throw e;
        } catch (final Exception e) {
            LOG.error("", e);
            final ServletException se = new ServletException(e.getMessage(), e);
            throw se;
        } finally {
            RequestContextHolder.reset();
        }
    }

    protected LoginCustomizer getLoginCustomizer() {
        return null;
    }

    /**
     * Tries to authenticate the user via any of the supported HTTP auth schemes.
     *
     * @param req The HTTP servlet request.
     * @param resp The HTTP servlet response.
     * @return <code>true</code> if the authentication was successful; otherwise <code>false</code>. In that case the response is already committed.
     * @throws IOException If an I/O error occurs
     */
    protected boolean authenticate(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        Session session = null;
        if (useCookies()) {
            /*
             * try by cookie
             */
            try {
                session = findSessionByCookie(req, resp);
            } catch (OXException e) {
                LOG.error("", e);
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
                return false;
            }
        }
        if (null == session) {
            AuthorizationHeader authHeader = AuthorizationHeader.parseSafe(req.getHeader(Header.AUTH_HEADER));
            if (authHeader == null) {
                addUnauthorizedHeader(req, resp);
                resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authorization Required!");
                return false;
            }

            switch (authHeader.getScheme().toLowerCase()) {
                case "basic":
                    session = doBasicAuth(authHeader, req, resp);
                    break;

                case "bearer":
                    session = doOAuth(authHeader, req, resp);
                    break;
                default:
                    resp.sendError(HttpServletResponse.SC_FORBIDDEN);
                    break;

            }

            if (session == null) {
                return false;
            }
        } else {
            /*
             * Session found by cookie
             */
            final String address = req.getRemoteAddr();
            if (null == address || !address.equals(session.getLocalIp())) {
                LOG.info("Request to server denied for session: {}. in WebDAV XML interface. Client login IP changed from {} to {}{}", session.getSessionID(), session.getLocalIp(), address, '.');
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

    /**
     * Handles OXExceptions thrown during login requests by responding with an HTTP error code.
     *
     * @param e the exception
     * @param req The servlet request
     * @param resp The servlet response
     * @throws IOException
     */
    private void handleFailedWebDAVLogin(OXException e, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (e.getCategory() == Category.CATEGORY_USER_INPUT) {
            LOG.debug("WebDAV login failed", e);
            addUnauthorizedHeader(req, resp);
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authorization Required!");
        } else if (LoginExceptionCodes.AUTHENTICATION_DISABLED.equals(e)) {
            LOG.debug("WebDAV login failed", e);
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
        } else {
            LOG.error("WebDAV login failed", e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private Session doBasicAuth(AuthorizationHeader authHeader, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            Credentials creds = com.openexchange.tools.servlet.http.Authorization.decode(authHeader.getRawValue());
            if (!com.openexchange.tools.servlet.http.Authorization.checkLogin(creds.getPassword())) {
                resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authorization Required!");
                return null;
            }

            return performWebDAVLogin(new LoginRequestImpl(creds.getLogin(), creds.getPassword(), getInterface(), req), req, resp);
        } catch (OXException e) {
            handleFailedWebDAVLogin(e, req, resp);
            return null;
        }
    }

    private Session performWebDAVLogin(LoginRequest loginRequest, HttpServletRequest req, HttpServletResponse resp) throws OXException {
        LoginCustomizer customizer = getLoginCustomizer();
        if (customizer != null) {
            loginRequest = customizer.modifyLogin(loginRequest);
        }

        if (false == useCookies()) {
            /*
             * try to get session indirectly from store
             */
            return WebDAVSessionStore.getInstance().getSession(loginRequest);
        } else {
            /*
             * login as usual
             */
            final Map<String, Object> properties = new HashMap<String, Object>(1);
            Session session = addSession(loginRequest, properties);
            resp.addCookie(new Cookie(COOKIE_SESSIONID, session.getSessionID()));
            return session;
        }
    }

    private Session doOAuth(AuthorizationHeader authHeader, HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws IOException {
        OAuthResourceService oAuthResourceService = getOAuthResourceService();
        if (oAuthResourceService == null) {
            httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN);
            return null;
        }

        try {
            OAuthAccess oAuthAccess = oAuthResourceService.checkAccessToken(authHeader.getAuthString(), httpRequest);
            httpRequest.setAttribute(OAuthConstants.PARAM_OAUTH_ACCESS, oAuthAccess);
            return oAuthAccess.getSession();
        } catch (OXException e) {
            if (e instanceof OAuthInvalidTokenException) {
                OAuthInvalidTokenException ex = (OAuthInvalidTokenException) e;
                String errorDescription = ex.getErrorDescription();
                StringBuilder sb = new StringBuilder(OAuthConstants.BEARER_SCHEME);
                sb.append(",error=\"invalid_token\"");
                if (errorDescription != null) {
                    sb.append(",error_description=\"").append(errorDescription).append("\"");
                }

                sendEmptyErrorResponse(httpResponse, HttpServletResponse.SC_UNAUTHORIZED, Collections.singletonMap(HttpHeaders.WWW_AUTHENTICATE, sb.toString()));
            } else {
                handleFailedWebDAVLogin(e, httpRequest, httpResponse);
            }
        }

        return null;
    }

    /**
     * Tries to authenticate the user via HTTP Basic Auth.
     *
     * @param req The HTTP servlet request.
     * @param resp The HTTP servlet response.
     * @param face the used interface.
     * @return <code>true</code> if the authentication was successful; otherwise <code>false</code>. In that case the response is already committed.
     * @throws IOException If an I/O error occurs
     */
    public static boolean doAuth(final HttpServletRequest req, final HttpServletResponse resp, final Interface face) throws IOException {
        return doAuth(req, resp, face, null);
    }

    /**
     * Tries to authenticate the user via HTTP Basic Auth.
     *
     * @param req The HTTP servlet request.
     * @param resp The HTTP servlet response.
     * @param face the used interface.
     * @param customizer The login customizer, or <code>null</code> if not used
     * @return <code>true</code> if the authentication was successful; otherwise <code>false</code>. In that case the response is already committed.
     * @throws IOException If an I/O error occurs
     */
    public static boolean doAuth(final HttpServletRequest req, final HttpServletResponse resp, final Interface face, final LoginCustomizer customizer) throws IOException {
        return doAuth(req, resp, face, customizer, true);
    }

    /**
     * Tries to authenticate the user via HTTP Basic Auth.
     *
     * @param req The HTTP servlet request.
     * @param resp The HTTP servlet response.
     * @param face the used interface.
     * @param customizer The login customizer, or <code>null</code> if not used
     * @param useCookies <code>true</code> if cookies should be used, <code>false</code>, otherwise
     * @return <code>true</code> if the authentication was successful; otherwise <code>false</code>. In that case the response is already committed.
     * @throws IOException If an I/O error occurs
     */
    public static boolean doAuth(final HttpServletRequest req, final HttpServletResponse resp, final Interface face, final LoginCustomizer customizer, boolean useCookies) throws IOException {
        Session session = null;
        if (useCookies) {
            /*
             * try by cookie
             */
            try {
                session = findSessionByCookie(req, resp);
            } catch (OXException e) {
                LOG.error("", e);
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
                return false;
            }
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
                LOG.debug("", e);
                addBasicAuthenticateHeader(resp);
                resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authorization Required!");
                return false;
            }
            try {
                if (false == useCookies) {
                    /*
                     * try to get session indirectly from store
                     */
                    session = WebDAVSessionStore.getInstance().getSession(loginRequest);
                } else {
                    /*
                     * login as usual
                     */
                    final Map<String, Object> properties = new HashMap<String, Object>(1);
                    session = addSession(loginRequest, properties);
                    resp.addCookie(new Cookie(COOKIE_SESSIONID, session.getSessionID()));
                }
            } catch (final OXException e) {
                if (e.getCategory() == Category.CATEGORY_USER_INPUT) {
                    addBasicAuthenticateHeader(resp);
                    resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authorization Required!");
                } else if (LoginExceptionCodes.AUTHENTICATION_DISABLED.equals(e)) {
                    resp.sendError(HttpServletResponse.SC_FORBIDDEN);
                } else {
                    LOG.error("", e);
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
                }
                return false;
            }
        } else {
            /*
             * Session found by cookie
             */
            final String address = req.getRemoteAddr();
            if (null == address || !address.equals(session.getLocalIp())) {
                LOG.info("Request to server denied for session: {}. in WebDAV XML interface. Client login IP changed from {} to {}{}", session.getSessionID(), session.getLocalIp(), address, '.');
                addBasicAuthenticateHeader(resp);
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
     * Adds the <code>WWW-Authenticate</code> header for the enabled schemes to the given HTTP response.
     *
     * @param resp the response to that the header should be added.
     */
    private void addUnauthorizedHeader(final HttpServletRequest req, final HttpServletResponse resp) {
        if (useHttpAuth()) {
            resp.addHeader("WWW-Authenticate", "Basic realm=\"" + basicRealm + "\", encoding=\"UTF-8\"");
            if (allowOAuthAccess()) {
                if (getOAuthResourceService() != null) {
                    resp.addHeader("WWW-Authenticate", "Bearer");
                }
            }
        }
    }

    /**
     * Adds the <code>WWW-Authenticate</code> header for the <code>Basic</code> scheme to the given HTTP response.
     *
     * @param resp the response to that the header should be added.
     */
    private static void addBasicAuthenticateHeader(HttpServletResponse resp) {
        resp.addHeader("WWW-Authenticate", "Basic realm=\"" + basicRealm + "\", encoding=\"UTF-8\"");
    }

    /**
     * Parses the HTTP requests <code>Authorization</code> header and creates a login request based on the provided
     * Basic Auth credentials.
     *
     * @param req
     * @param face
     * @return the login request
     * @throws OXException If the request contains no valid <code>Authorization</code> header with Basic Auth scheme
     */
    private static LoginRequest parseLogin(final HttpServletRequest req, final Interface face) throws OXException {
        final String auth = req.getHeader(Header.AUTH_HEADER);
        if (null == auth) {
            LOG.debug("Authorization header missing.");
            throw WebdavExceptionCode.MISSING_HEADER_FIELD.create("Authorization");
        }
        if (com.openexchange.tools.servlet.http.Authorization.checkForBasicAuthorization(auth)) {
            final Credentials creds = com.openexchange.tools.servlet.http.Authorization.decode(auth);
            if (!com.openexchange.tools.servlet.http.Authorization.checkLogin(creds.getPassword())) {
                throw WebdavExceptionCode.EMPTY_PASSWORD.create();
            }
            return new LoginRequestImpl(creds.getLogin(), creds.getPassword(), face, req);
        }
        /*
         * No known auth mechanism
         */
        final int pos = auth.indexOf(' ');
        final String mech = pos > 0 ? auth.substring(0, pos) : auth;
        LOG.debug("Unsupported Authentication header.");
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

    /**
     * Tries to find an already existing session on the server based on the cookies found in the supplied HTTP request.
     *
     * @param req The request
     * @param resp The response
     * @return The session, or <code>null</code> if no matching session could be looked up
     * @throws OXException
     */
    public static Session findSessionByCookie(final HttpServletRequest req, final HttpServletResponse resp) throws OXException {
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
            case DRIVE_UPDATER:
                return true;
            default:
                return false;
        }
    }

}
