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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

package com.openexchange.ajax;

import static com.openexchange.ajax.ConfigMenu.convert2JS;
import static com.openexchange.ajax.SessionServlet.removeJSESSIONID;
import static com.openexchange.ajax.SessionServlet.removeOXCookies;
import static com.openexchange.login.Interface.HTTP_JSON;
import static com.openexchange.tools.servlet.http.Tools.copyHeaders;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.fields.Header;
import com.openexchange.ajax.fields.LoginFields;
import com.openexchange.ajax.helper.Send;
import com.openexchange.ajax.login.HashCalculator;
import com.openexchange.ajax.writer.LoginWriter;
import com.openexchange.ajax.writer.ResponseWriter;
import com.openexchange.authentication.LoginException;
import com.openexchange.authentication.LoginExceptionCodes;
import com.openexchange.config.ConfigTools;
import com.openexchange.configuration.CookieHashSource;
import com.openexchange.configuration.ServerConfig;
import com.openexchange.configuration.ServerConfig.Property;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextException;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.ldap.LdapException;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.settings.Setting;
import com.openexchange.groupware.settings.SettingException;
import com.openexchange.groupware.settings.impl.ConfigTree;
import com.openexchange.groupware.settings.impl.SettingStorage;
import com.openexchange.java.util.UUIDs;
import com.openexchange.login.ConfigurationProperty;
import com.openexchange.login.Interface;
import com.openexchange.login.LoginRequest;
import com.openexchange.login.LoginResult;
import com.openexchange.login.internal.LoginPerformer;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceException;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.sessiond.SessiondException;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.sessiond.impl.IPRange;
import com.openexchange.tools.io.IOTools;
import com.openexchange.tools.servlet.AjaxException;
import com.openexchange.tools.servlet.OXJSONException;
import com.openexchange.tools.servlet.http.Authorization;
import com.openexchange.tools.servlet.http.Tools;
import com.openexchange.tools.servlet.http.Authorization.Credentials;
import com.openexchange.tools.session.ServerSessionAdapter;

/**
 * Servlet doing the login and logout stuff.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */

public class Login extends AJAXServlet {

    private static final long serialVersionUID = 7680745138705836499L;

    static final Log LOG = com.openexchange.exception.Log.valueOf(LogFactory.getLog(Login.class));

    private static interface JSONRequestHandler {
        
        void handleRequest(HttpServletRequest req, HttpServletResponse resp) throws IOException;
    }

    public static final String SESSION_PREFIX = "open-xchange-session-";

    public static final String SECRET_PREFIX = "open-xchange-secret-";

    private static final String ACTION_FORMLOGIN = "formlogin";

    private static enum CookieType {
        SESSION,
        SECRET;
    }
    
    private String uiWebPath;
    private boolean sessiondAutoLogin;
    private CookieHashSource hashSource;
    private String httpAuthAutoLogin;
    private String defaultClient;
    private String clientVersion;
    private String errorPageTemplate;
    private int cookieExpiry;
    private boolean cookieForceHTTPS;
    private boolean ipCheck;
    private Queue<IPRange> ranges;
    private final Map<String, JSONRequestHandler> handlerMap;

    /**
     * Initializes the login servlet.
     */
    public Login() {
        super();
        final Map<String, JSONRequestHandler> map = new ConcurrentHashMap<String, Login.JSONRequestHandler>(8);
        map.put(ACTION_LOGIN, new JSONRequestHandler() {

            public void handleRequest(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
                // Look-up necessary credentials
                try {
                    doLogin(req, resp);
                } catch (final AjaxException e) {
                    logAndSendException(resp, e);
                }
            }
        });
        map.put(ACTION_STORE, new JSONRequestHandler() {

            public void handleRequest(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
                try {
                    doStore(req, resp);
                } catch (final AbstractOXException e) {
                    logAndSendException(resp, e);
                } catch (final JSONException e) {
                    log(RESPONSE_ERROR, e);
                    sendError(resp);
                }
            }
        });
        map.put(ACTION_REFRESH_SECRET, new JSONRequestHandler() {

            public void handleRequest(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
                try {
                    doRefreshSecret(req, resp);
                } catch (final AbstractOXException e) {
                    logAndSendException(resp, e);
                } catch (final JSONException e) {
                    log(RESPONSE_ERROR, e);
                    sendError(resp);
                }
            }
        });
        map.put(ACTION_LOGOUT, new JSONRequestHandler() {

            public void handleRequest(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
                // The magic spell to disable caching
                Tools.disableCaching(resp);
                resp.setContentType(CONTENTTYPE_JAVASCRIPT);
                final String sessionId = req.getParameter(PARAMETER_SESSION);
                if (sessionId == null) {
                    resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
                    return;
                }
                try {
                    final Session session = LoginPerformer.getInstance().lookupSession(sessionId);
                    if (session != null) {
                        final String secret = SessionServlet.extractSecret(hashSource, req, session.getHash(), session.getClient());

                        if (secret == null || !session.getSecret().equals(secret)) {
                            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
                            return;
                        }

                        LoginPerformer.getInstance().doLogout(sessionId);
                        // Drop relevant cookies
                        removeOXCookies(session.getHash(), req, resp);
                        removeJSESSIONID(req, resp);
                    }
                } catch (final LoginException e) {
                    LOG.error("Logout failed", e);
                }
            }
        });
        map.put(ACTION_REDIRECT, new JSONRequestHandler() {

            public void handleRequest(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
                // The magic spell to disable caching
                Tools.disableCaching(resp);
                resp.setContentType(CONTENTTYPE_JAVASCRIPT);
                final String randomToken = req.getParameter(LoginFields.RANDOM_PARAM);
                if (randomToken == null) {
                    resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
                    return;
                }
                final SessiondService sessiondService = ServerServiceRegistry.getInstance().getService(SessiondService.class);
                if (sessiondService == null) {
                    final ServiceException se = ServiceExceptionCode.SERVICE_UNAVAILABLE.create( SessiondService.class.getName());
                    LOG.error(se.getMessage(), se);
                    resp.sendError(HttpServletResponse.SC_FORBIDDEN);
                    return;
                }
                final Session session = sessiondService.getSessionByRandomToken(randomToken, req.getRemoteAddr());
                if (session == null) {
                    // Unknown random token; throw error
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("No session could be found for random token: " + randomToken, new Throwable());
                    } else if (LOG.isInfoEnabled()) {
                        LOG.info("No session could be found for random token: " + randomToken);
                    }
                    resp.sendError(HttpServletResponse.SC_FORBIDDEN);
                    return;
                }
                // Remove old cookies to prevent usage of the old autologin cookie
                SessionServlet.removeOXCookies(session.getHash(), req, resp);
                try {
                    final Context context = ContextStorage.getInstance().getContext(session.getContextId());
                    final User user = UserStorage.getInstance().getUser(session.getUserId(), context);
                    if (!context.isEnabled() || !user.isMailEnabled()) {
                        resp.sendError(HttpServletResponse.SC_FORBIDDEN);
                        return;
                    }
                } catch (final UndeclaredThrowableException e) {
                    resp.sendError(HttpServletResponse.SC_FORBIDDEN);
                    return;
                } catch (final ContextException e) {
                    resp.sendError(HttpServletResponse.SC_FORBIDDEN);
                    return;
                } catch (final LdapException e) {
                    resp.sendError(HttpServletResponse.SC_FORBIDDEN);
                    return;
                }

                String client = req.getParameter(LoginFields.CLIENT_PARAM);
                if (null == client) {
                    client = session.getClient();
                } else {
                    session.setClient(client);
                }
                final String hash = HashCalculator.getHash(req, client);
                session.setHash(hash);
                writeSecretCookie(resp, session, hash, req.isSecure());

                resp.sendRedirect(generateRedirectURL(
                    req.getParameter(LoginFields.UI_WEB_PATH_PARAM),
                    req.getParameter("store"),
                    session.getSessionID()));
            }
        });
        map.put(ACTION_REDEEM, new JSONRequestHandler() {

            public void handleRequest(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
             // The magic spell to disable caching
                Tools.disableCaching(resp);
                resp.setContentType(CONTENTTYPE_JAVASCRIPT);
                final String randomToken = req.getParameter(LoginFields.RANDOM_PARAM);
                if (randomToken == null) {
                    resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
                    return;
                }
                final SessiondService sessiondService = ServerServiceRegistry.getInstance().getService(SessiondService.class);
                if (sessiondService == null) {
                    final ServiceException se = ServiceExceptionCode.SERVICE_UNAVAILABLE.create( SessiondService.class.getName());
                    LOG.error(se.getMessage(), se);
                    resp.sendError(HttpServletResponse.SC_FORBIDDEN);
                    return;
                }
                final Session session = sessiondService.getSessionByRandomToken(randomToken, req.getRemoteAddr());
                if (session == null) {
                    // Unknown random token; throw error
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("No session could be found for random token: " + randomToken, new Throwable());
                    } else if (LOG.isInfoEnabled()) {
                        LOG.info("No session could be found for random token: " + randomToken);
                    }
                    resp.sendError(HttpServletResponse.SC_FORBIDDEN);
                    return;
                }
                // Remove old cookies to prevent usage of the old autologin cookie
                SessionServlet.removeOXCookies(session.getHash(), req, resp);
                try {
                    final Context context = ContextStorage.getInstance().getContext(session.getContextId());
                    final User user = UserStorage.getInstance().getUser(session.getUserId(), context);
                    if (!context.isEnabled() || !user.isMailEnabled()) {
                        resp.sendError(HttpServletResponse.SC_FORBIDDEN);
                        return;
                    }
                } catch (final UndeclaredThrowableException e) {
                    resp.sendError(HttpServletResponse.SC_FORBIDDEN);
                    return;
                } catch (final ContextException e) {
                    resp.sendError(HttpServletResponse.SC_FORBIDDEN);
                    return;
                } catch (final LdapException e) {
                    resp.sendError(HttpServletResponse.SC_FORBIDDEN);
                    return;
                }

                String client = req.getParameter(LoginFields.CLIENT_PARAM);
                if (null == client) {
                    client = session.getClient();
                } else {
                    session.setClient(client);
                }
                final String hash = HashCalculator.getHash(req, client);
                session.setHash(hash);
                writeSecretCookie(resp, session, hash, req.isSecure());

                try {
                    final JSONObject json = new JSONObject();
                    LoginWriter.write(session, json);
                    // Append "config/modules"
                    appendModules(session, json, req);
                    json.write(resp.getWriter());
                } catch (final JSONException e) {
                    log(RESPONSE_ERROR, e);
                    sendError(resp);
                }
            }
        });
        map.put(ACTION_AUTOLOGIN, new JSONRequestHandler() {

            public void handleRequest(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
                Tools.disableCaching(resp);
                resp.setContentType(CONTENTTYPE_JAVASCRIPT);
                final Response response = new Response();
                Session session = null;
                try {
                    if (!sessiondAutoLogin) {
                        throw new AjaxException(AjaxException.Code.DisabledAction, "autologin");
                    }

                    final Cookie[] cookies = req.getCookies();
                    if (cookies == null) {
                        throw new OXJSONException(OXJSONException.Code.INVALID_COOKIE);
                    }

                    final SessiondService sessiondService = ServerServiceRegistry.getInstance().getService(SessiondService.class);
                    String secret = null;
                    final String hash = HashCalculator.getHash(req);
                    final String sessionCookieName = SESSION_PREFIX + hash;
                    final String secretCookieName = SECRET_PREFIX + hash;

                    NextCookie: for (final Cookie cookie : cookies) {
                        final String cookieName = cookie.getName();
                        if (cookieName.startsWith(sessionCookieName)) {
                            final String sessionId = cookie.getValue();
                            if (sessiondService.refreshSession(sessionId)) {
                                session = sessiondService.getSession(sessionId);
                                // IP check if enabled; otherwise update session's IP address if different to request's IP address
                                if (!ipCheck) {
                                    // Update IP address if necessary
                                    updateIPAddress(req.getRemoteAddr(), session);
                                } else {
                                    final String newIP = req.getRemoteAddr();
                                    SessionServlet.checkIP(true, ranges, session, newIP);
                                    // IP check passed: update IP address if necessary
                                    updateIPAddress(newIP, session);
                                }
                                try {
                                    final Context ctx = ContextStorage.getInstance().getContext(session.getContextId());
                                    final User user = UserStorage.getInstance().getUser(session.getUserId(), ctx);
                                    if (!ctx.isEnabled() || !user.isMailEnabled()) {
                                        throw LoginExceptionCodes.INVALID_CREDENTIALS.create();
                                    }
                                } catch (final UndeclaredThrowableException e) {
                                    throw LoginExceptionCodes.UNKNOWN.create(e);
                                }
                                final JSONObject json = new JSONObject();
                                LoginWriter.write(session, json);
                                // Append "config/modules"
                                appendModules(session, json, req);
                                response.setData(json);
                                /*
                                 * Secret already found?
                                 */
                                if (null != secret) {
                                    break NextCookie;
                                }
                            }
                        } else if (cookieName.startsWith(secretCookieName)) {
                            secret = cookie.getValue();
                            /*
                             * Session already found?
                             */
                            if (null != session) {
                                break NextCookie;
                            }
                        }
                    }
                    if (null == response.getData() || session == null || secret == null || !(session.getSecret().equals(secret))) {
                        SessionServlet.removeOXCookies(hash, req, resp);
                        SessionServlet.removeJSESSIONID(req, resp);
                        throw new OXJSONException(OXJSONException.Code.INVALID_COOKIE);
                    }
                } catch (final SessiondException e) {
                    LOG.debug(e.getMessage(), e);
                    if (SessionServlet.isIpCheckError(e) && null != session) {
                        try {
                            // Drop Open-Xchange cookies
                            final SessiondService sessiondService = ServerServiceRegistry.getInstance().getService(SessiondService.class);
                            SessionServlet.removeOXCookies(session.getHash(), req, resp);
                            SessionServlet.removeJSESSIONID(req, resp);
                            sessiondService.removeSession(session.getSessionID());
                        } catch (final Exception e2) {
                            LOG.error("Cookies could not be removed.", e2);
                        }
                    }
                    response.setException(e);
                } catch (final AjaxException e) {
                    LOG.debug(e.getMessage(), e);
                    response.setException(e);
                } catch (final OXJSONException e) {
                    LOG.debug(e.getMessage(), e);
                    response.setException(e);
                } catch (final JSONException e) {
                    final OXJSONException oje = new OXJSONException(OXJSONException.Code.JSON_WRITE_ERROR, e);
                    LOG.error(oje.getMessage(), oje);
                    response.setException(oje);
                } catch (final ContextException e) {
                    LOG.debug(e.getMessage(), e);
                    response.setException(e);
                } catch (final LdapException e) {
                    LOG.debug(e.getMessage(), e);
                    response.setException(e);
                } catch (final LoginException e) {
                    if (AbstractOXException.Category.USER_INPUT == e.getCategory()) {
                        LOG.debug(e.getMessage(), e);
                    } else {
                        LOG.error(e.getMessage(), e);
                    }
                    response.setException(e);
                }
                // The magic spell to disable caching
                Tools.disableCaching(resp);
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.setContentType(CONTENTTYPE_JAVASCRIPT);
                try {
                    if (response.hasError()) {
                        ResponseWriter.write(response, resp.getWriter());
                    } else {
                        ((JSONObject) response.getData()).write(resp.getWriter());
                    }
                } catch (final JSONException e) {
                    log(RESPONSE_ERROR, e);
                    sendError(resp);
                }
            }
        });
        map.put(ACTION_FORMLOGIN, new JSONRequestHandler() {

            public void handleRequest(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
                try {
                    doFormLogin(req, resp);
                } catch (final AjaxException e) {
                    final String errorPage = errorPageTemplate.replace("ERROR_MESSAGE", e.getMessage());
                    resp.setContentType(CONTENTTYPE_HTML);
                    resp.getWriter().write(errorPage);
                } catch (final LoginException e) {
                    final String errorPage = errorPageTemplate.replace("ERROR_MESSAGE", e.getMessage());
                    resp.setContentType(CONTENTTYPE_HTML);
                    resp.getWriter().write(errorPage);
                }
            }
        });
        handlerMap = map;
    }

    @Override
    public void init(final ServletConfig config) throws ServletException {
        super.init(config);
        uiWebPath = config.getInitParameter(ServerConfig.Property.UI_WEB_PATH.getPropertyName());
        sessiondAutoLogin = Boolean.parseBoolean(config.getInitParameter(ConfigurationProperty.SESSIOND_AUTOLOGIN.getPropertyName()));
        hashSource = CookieHashSource.parse(config.getInitParameter(Property.COOKIE_HASH.getPropertyName()));
        httpAuthAutoLogin = config.getInitParameter(ConfigurationProperty.HTTP_AUTH_AUTOLOGIN.getPropertyName());
        defaultClient = config.getInitParameter(ConfigurationProperty.HTTP_AUTH_CLIENT.getPropertyName());
        clientVersion = config.getInitParameter(ConfigurationProperty.HTTP_AUTH_VERSION.getPropertyName());
        final String templateFileLocation = config.getInitParameter(ConfigurationProperty.ERROR_PAGE_TEMPLATE.getPropertyName());
        if (null == templateFileLocation) {
            errorPageTemplate = ERROR_PAGE_TEMPLATE;
        } else {
            final File templateFile = new File(templateFileLocation);
            try {
                errorPageTemplate = IOTools.getFileContents(templateFile);
                LOG.info("Found an error page template at " + templateFileLocation);
            } catch (final FileNotFoundException e) {
                LOG.error("Could not find an error page template at " + templateFileLocation + ", using default.");
                errorPageTemplate = ERROR_PAGE_TEMPLATE;
            }
        }
        cookieExpiry = (int) (ConfigTools.parseTimespan(config.getInitParameter(ServerConfig.Property.COOKIE_TTL.getPropertyName())) / 1000);
        cookieForceHTTPS = Boolean.parseBoolean(config.getInitParameter(ServerConfig.Property.COOKIE_FORCE_HTTPS.getPropertyName())) || Boolean.parseBoolean(config.getInitParameter(ServerConfig.Property.FORCE_HTTPS.getPropertyName()));
        ipCheck = Boolean.parseBoolean(config.getInitParameter(ServerConfig.Property.IP_CHECK.getPropertyName()));
        ranges = new LinkedList<IPRange>();
        final String tmp = config.getInitParameter(ConfigurationProperty.NO_IP_CHECK_RANGE.getPropertyName());
        if (tmp != null) {
            final String[] lines = tmp.split("\n");
            for (String line : lines) {
                line = line.replaceAll("\\s", "");
                if (!line.equals("") && !line.startsWith("#")) {
                    ranges.add(IPRange.parseRange(line));
                }
            }
        }
    }

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        final String action = req.getParameter(PARAMETER_ACTION);
        final String subPath = getServletSpecificURI(req);
        if (null != subPath && subPath.length() > 0 && subPath.startsWith("/httpAuth")) {
            doHttpAuth(req, resp);
        } else if (null != action) {
            doJSONAuth(req, resp, action);
        } else {
            logAndSendException(resp, new AjaxException(AjaxException.Code.MISSING_PARAMETER, PARAMETER_ACTION));
            return;
        }
    }

    private void doJSONAuth(final HttpServletRequest req, final HttpServletResponse resp, final String action) throws IOException {
        final JSONRequestHandler handler = handlerMap.get(action);
        if (null == handler) {
            logAndSendException(resp, new AjaxException(AjaxException.Code.UnknownAction, action));
            return;
        }
        handler.handleRequest(req, resp);
    }

    private void doHttpAuth(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        if (req.getHeader(Header.AUTH_HEADER) != null) {
            try {
                doAuthHeaderLogin(req, resp);
            } catch (final LoginException e) {
                resp.setHeader("WWW-Authenticate", "Basic realm=\"Open-Xchange\"");
                resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, e.getMessage());
            }
        } else {
            resp.setHeader("WWW-Authenticate", "Basic realm=\"Open-Xchange\"");
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authorization Required!");
        }
    }

    /**
     * Updates session's IP address if different to specified IP address.
     *
     * @param newIP The possibly new IP address
     * @param session The session to update if IP addresses differ
     */
    private static void updateIPAddress(final String newIP, final Session session) {
        final String oldIP = session.getLocalIp();
        if (null != newIP && !newIP.equals(oldIP)) { // IPs differ
            LOG.info(new StringBuilder("Updating sessions IP address. authID: ").append(session.getAuthId()).append(", sessionID: ").append(
                session.getSessionID()).append(", old ip: ").append(oldIP).append(", new ip: ").append(newIP).toString());
            session.setLocalIp(newIP);
        }
    }

    protected String addFragmentParameter(final String usedUIWebPath, final String param, final String value) {
        String retval = usedUIWebPath;
        final int fragIndex = retval.indexOf('#');

        // First get rid of the query String, so we can reappend it later
        final int questionMarkIndex = retval.indexOf('?', fragIndex);
        String query = "";
        if (questionMarkIndex > 0) {
            query = retval.substring(questionMarkIndex);
            retval = retval.substring(0, questionMarkIndex);
        }
        // Now let's see, if this url already contains a fragment
        if (!retval.contains("#")) {
            // Apparently it didn't, so we can append our own
            return retval + "#" + param + "=" + value + query;
        }
        // Alright, we already have a fragment, let's append a new parameter

        return retval + "&" + param + "=" + value + query;
    }

    /**
     * Writes or rewrites a cookie
     */
    private void doCookieReWrite(final HttpServletRequest req, final HttpServletResponse resp, final CookieType type) throws AbstractOXException, JSONException, IOException {
        if (!sessiondAutoLogin && CookieType.SESSION == type) {
            throw new AjaxException(AjaxException.Code.DisabledAction, "store");
        }
        final SessiondService sessiond = ServerServiceRegistry.getInstance().getService(SessiondService.class);
        if (null == sessiond) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create( SessiondService.class.getName());
        }

        final String sessionId = req.getParameter(PARAMETER_SESSION);
        if (null == sessionId) {
            throw new AjaxException(AjaxException.Code.MISSING_PARAMETER, PARAMETER_SESSION);
        }

        final Session session = SessionServlet.getSession(hashSource, req, sessionId, sessiond);

        if (type == CookieType.SESSION) {
            writeSessionCookie(resp, session, session.getHash(), req.isSecure());
        } else {
            writeSecretCookie(resp, session, session.getHash(), req.isSecure());
        }
        // Refresh HTTP session, too
        req.getSession();

        final Response response = new Response();
        response.setData("1");

        ResponseWriter.write(response, resp.getWriter());
    }

    private void doStore(final HttpServletRequest req, final HttpServletResponse resp) throws AbstractOXException, JSONException, IOException {
        Tools.disableCaching(resp);
        resp.setContentType(CONTENTTYPE_JAVASCRIPT);
    	doCookieReWrite(req, resp, CookieType.SESSION);
    }

    private void doRefreshSecret(final HttpServletRequest req, final HttpServletResponse resp) throws AbstractOXException, JSONException, IOException {
        Tools.disableCaching(resp);
        resp.setContentType(CONTENTTYPE_JAVASCRIPT);
    	doCookieReWrite(req, resp, CookieType.SECRET);
    }

    private void logAndSendException(final HttpServletResponse resp, final AbstractOXException e) throws IOException {
        LOG.debug(e.getMessage(), e);
        Tools.disableCaching(resp);
        resp.setContentType(CONTENTTYPE_JAVASCRIPT);
        final Response response = new Response();
        response.setException(e);
        Send.sendResponse(response, resp);
    }

    @Override
    protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        doGet(req, resp);
    }

    /**
     * Writes the (groupware's) session cookie to specified HTTP servlet response whose name is composed by cookie prefix
     * <code>"open-xchange-session-"</code> and a secret cookie identifier.
     *
     * @param resp The HTTP servlet response
     * @param session The session providing the secret cookie identifier
     */
    protected void writeSecretCookie(final HttpServletResponse resp, final Session session, final String hash, final boolean secure) {
        final Cookie cookie = new Cookie(SECRET_PREFIX + hash, session.getSecret());
        configureCookie(cookie, secure);
        resp.addCookie(cookie);
    }

    protected void writeSessionCookie(final HttpServletResponse resp, final Session session, final String hash, final boolean secure) {
        final Cookie cookie = new Cookie(SESSION_PREFIX + hash, session.getSessionID());
        configureCookie(cookie, secure);
        resp.addCookie(cookie);
    }

    private void configureCookie(final Cookie cookie, final boolean secure) {
        cookie.setPath("/");
        if (!sessiondAutoLogin) {
            return;
        }
        cookie.setMaxAge(cookieExpiry);
        if (cookieForceHTTPS || secure) {
            cookie.setSecure(true);
        }
    }

    private void doLogin(final HttpServletRequest req, final HttpServletResponse resp) throws AjaxException, IOException {
        Tools.disableCaching(resp);
        resp.setContentType(CONTENTTYPE_JAVASCRIPT);

        final LoginRequest request = parseLogin(req, LoginFields.NAME_PARAM, false);
        // Perform the login
        final Response response = new Response();
        LoginResult result = null;
        try {
            result = LoginPerformer.getInstance().doLogin(request);
            // Write response
            final JSONObject json = new JSONObject();
            final Session session = result.getSession();
            LoginWriter.write(session, json);
            // Append "config/modules"
            appendModules(session, json, req);
            response.setData(json);
        } catch (final LoginException e) {
            if (AbstractOXException.Category.USER_INPUT == e.getCategory()) {
                LOG.debug(e.getMessage(), e);
            } else {
                LOG.error(e.getMessage(), e);
            }
            response.setException(e);
        } catch (final JSONException e) {
            final OXJSONException oje = new OXJSONException(OXJSONException.Code.JSON_WRITE_ERROR, e);
            LOG.error(oje.getMessage(), oje);
            response.setException(oje);
        }
        try {
            if (response.hasError() || null == result) {
                ResponseWriter.write(response, resp.getWriter());
            } else {
                final Session session = result.getSession();
                // Store associated session
                SessionServlet.rememberSession(req, new ServerSessionAdapter(session, result.getContext(), result.getUser()));
                writeSecretCookie(resp, session, session.getHash(), req.isSecure());

                // Login response is unfortunately not conform to default responses.
                ((JSONObject) response.getData()).write(resp.getWriter());
            }
        } catch (final JSONException e) {
            if (e.getCause() instanceof IOException) {
                // Throw proper I/O error since a serious socket error could been occurred which prevents further communication. Just
                // throwing a JSON error possibly hides this fact by trying to write to/read from a broken socket connection.
                throw (IOException) e.getCause();
            }
            LOG.error(RESPONSE_ERROR, e);
            sendError(resp);
        }
    }

    private LoginRequest parseLogin(final HttpServletRequest req, final String loginParamName, final boolean strict) throws AjaxException {
        final String login = req.getParameter(loginParamName);
        if (null == login) {
            throw new AjaxException(AjaxException.Code.MISSING_PARAMETER, loginParamName);
        }
        final String password = req.getParameter(LoginFields.PASSWORD_PARAM);
        if (null == password) {
            throw new AjaxException(AjaxException.Code.MISSING_PARAMETER, LoginFields.PASSWORD_PARAM);
        }
        final String authId;
        if (null == req.getParameter(LoginFields.AUTHID_PARAM)) {
            if (strict) {
                throw new AjaxException(AjaxException.Code.MISSING_PARAMETER, LoginFields.AUTHID_PARAM);
            }
            authId = UUIDs.getUnformattedString(UUID.randomUUID());
        } else {
            authId = req.getParameter(LoginFields.AUTHID_PARAM);
        }
        final String client;
        if (null == req.getParameter(LoginFields.CLIENT_PARAM)) {
            if (strict) {
                throw new AjaxException(AjaxException.Code.MISSING_PARAMETER, LoginFields.CLIENT_PARAM);
            }
            client = "default";
        } else {
            client = req.getParameter(LoginFields.CLIENT_PARAM);
        }
        final String version;
        if (null == req.getParameter(LoginFields.VERSION_PARAM)) {
            if (strict) {
                throw new AjaxException(AjaxException.Code.MISSING_PARAMETER, LoginFields.VERSION_PARAM);
            }
            version = null;
        } else {
            version = req.getParameter(LoginFields.VERSION_PARAM);
        }
        final Map<String, List<String>> headers = copyHeaders(req);
        final LoginRequest loginRequest = new LoginRequest() {

            private String hash = null;

            public String getLogin() {
                return login;
            }

            public String getPassword() {
                return password;
            }

            public String getClientIP() {
                return req.getRemoteAddr();
            }

            public String getUserAgent() {
                return req.getHeader(Header.USER_AGENT);
            }

            public String getAuthId() {
                return authId;
            }

            public String getClient() {
                return client;
            }

            public String getVersion() {
                return version;
            }

            public Interface getInterface() {
                return HTTP_JSON;
            }

            public String getHash() {
                if (hash != null) {
                    return hash;
                }
                return hash = HashCalculator.getHash(req);
            }

            public Map<String, List<String>> getHeaders() {
                return headers;
            }
        };
        return loginRequest;
    }

    private static void appendModules(final Session session, final JSONObject json, final HttpServletRequest req) {
        final String modules = "modules";
        if (parseBoolean(req.getParameter(modules))) {
            try {
                final Setting setting = ConfigTree.getSettingByPath(modules);
                SettingStorage.getInstance(session).readValues(setting);
                json.put(modules, convert2JS(setting));
            } catch (final SettingException e) {
                LOG.warn("Modules could not be added to login JSON response: " + e.getMessage(), e);
            } catch (final JSONException e) {
                LOG.warn("Modules could not be added to login JSON response: " + e.getMessage(), e);
            }
        }
    }

    /**
     * Parses the specified parameter to a <code>boolean</code> value.
     *
     * @param parameter The parameter value
     * @return <code>true</code> if parameter is <b>not</b> <code>null</code> and is (ignore-case) one of the values <code>"true"</code>,
     *         <code>"1"</code>, <code>"yes"</code> or <code>"on"</code>; otherwise <code>false</code>
     */
    private static boolean parseBoolean(final String parameter) {
        return "true".equalsIgnoreCase(parameter) || "1".equals(parameter) || "yes".equalsIgnoreCase(parameter) || "on".equalsIgnoreCase(parameter);
    }

    private void doFormLogin(final HttpServletRequest req, final HttpServletResponse resp) throws AjaxException, LoginException, IOException {
        final LoginRequest request = parseLogin(req, LoginFields.LOGIN_PARAM ,true);
        final LoginResult result = LoginPerformer.getInstance().doLogin(request);
        final Session session = result.getSession();

        Tools.disableCaching(resp);
        writeSecretCookie(resp, session, session.getHash(), req.isSecure());
        resp.sendRedirect(generateRedirectURL(
            req.getParameter(LoginFields.UI_WEB_PATH_PARAM),
            req.getParameter(LoginFields.AUTOLOGIN_PARAM),
            session.getSessionID()));
    }

    private void doAuthHeaderLogin(final HttpServletRequest req, final HttpServletResponse resp) throws LoginException, IOException {
        final String auth = req.getHeader(Header.AUTH_HEADER);
        final Credentials creds;
        if (Authorization.checkForBasicAuthorization(auth)) {
            creds = Authorization.decode(auth);
        } else {
            throw LoginExceptionCodes.UNKNOWN_HTTP_AUTHORIZATION.create();
        }
        final String client = defaultClient;
        final String version = clientVersion;
        final Map<String, List<String>> headers = copyHeaders(req);
        final LoginRequest request = new LoginRequest() {
            private String hash;
            public String getVersion() {
                return version;
            }
            public String getUserAgent() {
                return req.getHeader(Header.USER_AGENT);
            }
            public String getPassword() {
                return creds.getPassword();
            }
            public String getLogin() {
                return creds.getLogin();
            }
            public Interface getInterface() {
                return Interface.HTTP_JSON;
            }
            public String getHash() {
                if (hash != null) {
                    return hash;
                }
                return hash = HashCalculator.getHash(req, client);
            }
            public String getClientIP() {
                return req.getRemoteAddr();
            }
            public String getClient() {
                return client;
            }
            public String getAuthId() {
                return UUIDs.getUnformattedString(UUID.randomUUID());
            }
            public Map<String, List<String>> getHeaders() {
                return headers;
            }
        };
        final LoginResult result = LoginPerformer.getInstance().doLogin(request);
        final Session session = result.getSession();
        Tools.disableCaching(resp);
        writeSecretCookie(resp, session, session.getHash(), req.isSecure());
        resp.sendRedirect(generateRedirectURL(null, httpAuthAutoLogin, session.getSessionID()));
    }

    private String generateRedirectURL(final String uiWebPathParam, final String shouldStore, final String sessionId) {
        String retval = uiWebPathParam;
        if (null == retval) {
            retval = uiWebPath;
        }
        // Prevent HTTP response splitting.
        retval = retval.replaceAll("[\n\r]", "");
        retval = addFragmentParameter(retval, PARAMETER_SESSION, sessionId);
        if (shouldStore != null) {
            retval = addFragmentParameter(retval, "store", shouldStore);
        }
        return retval;
    }

    private static final String ERROR_PAGE_TEMPLATE = 
        "<html>\n" +
        "<script type=\"text/javascript\">\n" + 
        "// Display normal HTML for 5 seconds, then redirect via referrer.\n" + 
        "setTimeout(redirect,5000);\n" + 
        "function redirect(){\n" + 
        " var referrer=document.referrer;\n" + 
        " var redirect_url;\n" + 
        " // If referrer already contains failed parameter, we don't add a 2nd one.\n" + 
        " if(referrer.indexOf(\"login=failed\")>=0){\n" + 
        "  redirect_url=referrer;\n" + 
        " }else{\n" + 
        "  // Check if referrer contains multiple parameter\n" + 
        "  if(referrer.indexOf(\"?\")<0){\n" + 
        "   redirect_url=referrer+\"?login=failed\";\n" + 
        "  }else{\n" + 
        "   redirect_url=referrer+\"&login=failed\";\n" + 
        "  }\n" + 
        " }\n" + 
        " // Redirect to referrer\n" +
        " window.location.href=redirect_url;\n" + 
        "}\n" + 
        "</script>\n" + 
        "<body>\n" + 
        "<h1>ERROR_MESSAGE</h1>\n" + 
        "</body>\n" + 
        "</html>\n";
}
