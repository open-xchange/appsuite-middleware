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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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
import static com.openexchange.login.Interface.HTTP_JSON;
import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.UUID;
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
import com.openexchange.config.ConfigurationService;
import com.openexchange.configuration.ServerConfig;
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
import com.openexchange.login.Interface;
import com.openexchange.login.LoginRequest;
import com.openexchange.login.LoginResult;
import com.openexchange.login.internal.LoginPerformer;
import com.openexchange.server.ServiceException;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.tools.servlet.AjaxException;
import com.openexchange.tools.servlet.OXJSONException;
import com.openexchange.tools.servlet.http.Tools;
import com.openexchange.tools.session.ServerSessionAdapter;

/**
 * Servlet doing the login and logout stuff.
 * 
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */

public class Login extends AJAXServlet {
	private enum CookieType {
		SESSION,
		SECRET;
	}
	
    private static final long serialVersionUID = 7680745138705836499L;

    private static final String PARAM_NAME = "name";

    private static final String PARAM_PASSWORD = "password";

    private static final String PARAM_UI_WEB_PATH = "uiWebPath";

    public static final String SESSION_PREFIX = "open-xchange-session-";

    public static final String SECRET_PREFIX = "open-xchange-secret-";

    private static final Log LOG = LogFactory.getLog(Login.class);

    private String uiWebPath;

    public Login() {
        super();
    }

    @Override
    public void init(final ServletConfig config) throws ServletException {
        super.init(config);
        uiWebPath = config.getInitParameter(ServerConfig.Property.UI_WEB_PATH.getPropertyName());
    }

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        final String action = req.getParameter(PARAMETER_ACTION);
        if (action == null) {
            logAndSendException(resp, new AjaxException(AjaxException.Code.MISSING_PARAMETER, PARAMETER_ACTION));
            return;
        }
        if (ACTION_LOGIN.equals(action)) {
            // Look-up necessary credentials
            try {
                doLogin(req, resp);
            } catch (final AjaxException e) {
                logAndSendException(resp, e);
                return;
            }
        } else if (ACTION_STORE.equals(action)) {
            try {
                doStore(req, resp);
            } catch (final AbstractOXException e) {
                logAndSendException(resp, e);
                return;
            } catch (final JSONException e) {
                log(RESPONSE_ERROR, e);
                sendError(resp);
            }
        } else if (ACTION_REFRESH_SECRET.equals(action)) {
            try {
                doRefreshSecret(req, resp);
            } catch (final AbstractOXException e) {
                logAndSendException(resp, e);
                return;
            } catch (final JSONException e) {
                log(RESPONSE_ERROR, e);
                sendError(resp);
            }
        } else if (ACTION_LOGOUT.equals(action)) {
            // The magic spell to disable caching
            Tools.disableCaching(resp);
            final String sessionId = req.getParameter(PARAMETER_SESSION);
            if (sessionId == null) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            try {
                final Session session = LoginPerformer.getInstance().doLogout(sessionId);
                if(session != null) {
                    // Drop relevant cookies
                    SessionServlet.removeOXCookies(session.getHash(), req, resp);
                }
            } catch (final LoginException e) {
                LOG.error("Logout failed", e);
            }
        } else if (ACTION_REDIRECT.equals(action) || ACTION_REDEEM.equals(action)) {
            // The magic spell to disable caching
            Tools.disableCaching(resp);
            final String randomToken = req.getParameter(LoginFields.PARAM_RANDOM);
            if (randomToken == null) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            final SessiondService sessiondService = ServerServiceRegistry.getInstance().getService(SessiondService.class);
            if (sessiondService == null) {
                final ServiceException se = new ServiceException(ServiceException.Code.SERVICE_UNAVAILABLE, SessiondService.class.getName());
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

            session.setHash(HashCalculator.getHash(req));
            
            final String oldIP = session.getLocalIp();
            final String newIP = req.getRemoteAddr();
            if (!newIP.equals(oldIP)) {
                LOG.info("Updating sessions IP address. authID: " + session.getAuthId() + ", sessionID" + session.getSessionID() + " old ip: " + oldIP + " new ip: " + newIP);
                session.setLocalIp(newIP);
            }
            
            writeSecretCookie(resp, session, req.isSecure());

            if(ACTION_REDIRECT.equals(action)) {
                String usedUIWebPath = req.getParameter(PARAM_UI_WEB_PATH);
                if (null == usedUIWebPath) {
                    usedUIWebPath = uiWebPath;
                }
                // Prevent HTTP response splitting.
                usedUIWebPath = usedUIWebPath.replaceAll("[\n\r]", "");
                usedUIWebPath = addFragmentParameter(usedUIWebPath, PARAMETER_SESSION, session.getSessionID());
                final String shouldStore = req.getParameter("store");
                if(shouldStore != null) {
                    usedUIWebPath = addFragmentParameter(usedUIWebPath, "store", shouldStore);
                }
                resp.sendRedirect(usedUIWebPath);
            } else {
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
        } else if (ACTION_AUTOLOGIN.equals(action)) {
            final Response response = new Response();
            try {
                if (!isAutologinEnabled()) {
                    throw new AjaxException(AjaxException.Code.DisabledAction, "autologin");
                }

                final Cookie[] cookies = req.getCookies();
                final String hash = HashCalculator.getHash(req);

                if (cookies == null) {
                    throw new OXJSONException(OXJSONException.Code.INVALID_COOKIE);
                }
                final SessiondService sessiondService = ServerServiceRegistry.getInstance().getService(SessiondService.class);

                Session session = null;
                String secret = null;

                final String sessionCookieName = SESSION_PREFIX + hash;
                final String secretCookieName = SECRET_PREFIX + hash;

                NextCookie: for (final Cookie cookie : cookies) {
                    final String cookieName = cookie.getName();
                    if (cookieName.startsWith(sessionCookieName)) {
                        final String sessionId = cookie.getValue();
                        if (sessiondService.refreshSession(sessionId)) {
                            session = sessiondService.getSession(sessionId);
                            final String oldIP = session.getLocalIp();
                            final String newIP = req.getRemoteAddr();
                            if (!newIP.equals(oldIP)) {
                                LOG.info("Updating sessions IP address. authID: " + session.getAuthId() + ", sessionID" + session.getSessionID() + " old ip: " + oldIP + " new ip: " + newIP);
                                session.setLocalIp(newIP);
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
                    throw new OXJSONException(OXJSONException.Code.INVALID_COOKIE);
                }
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
        } else {
            logAndSendException(resp, new AjaxException(AjaxException.Code.UnknownAction, action));
        }
    }

    protected String addFragmentParameter(String usedUIWebPath, final String param, final String value) {
        final int fragIndex = usedUIWebPath.indexOf('#');
        
        // First get rid of the query String, so we can reappend it later
        final int questionMarkIndex = usedUIWebPath.indexOf('?', fragIndex);
        String query = "";
        if(questionMarkIndex > 0) {
            query = usedUIWebPath.substring(questionMarkIndex);
            usedUIWebPath = usedUIWebPath.substring(0, questionMarkIndex);
        }
        // Now let's see, if this url already contains a fragment
        if(!usedUIWebPath.contains("#")) {
            // Apparently it didn't, so we can append our own
            return usedUIWebPath+"#"+param+"="+value+query;
        }
        // Alright, we already have a fragment, let's append a new parameer
        
        return usedUIWebPath+"&"+param+"="+value+query;
    }

    /**
     * Writes or rewrites a cookie
     */
    private void doCookieReWrite(final HttpServletRequest req, final HttpServletResponse resp, CookieType type) throws AbstractOXException, JSONException, IOException {
        if (!isAutologinEnabled() && CookieType.SESSION == type) {
            throw new AjaxException(AjaxException.Code.DisabledAction, "store");
        }
        final SessiondService sessiond = ServerServiceRegistry.getInstance().getService(SessiondService.class);
        if (null == sessiond) {
            throw new ServiceException(ServiceException.Code.SERVICE_UNAVAILABLE, SessiondService.class.getName());
        }

        final String sessionId = req.getParameter(PARAMETER_SESSION);
        if (null == sessionId) {
            throw new AjaxException(AjaxException.Code.MISSING_PARAMETER, PARAMETER_SESSION);
        }

        final Session session = SessionServlet.getSession(req, sessionId, sessiond);

        if(type == CookieType.SESSION) {
            writeSessionCookie(resp, session, req.isSecure());
        } else {
            writeSecretCookie(resp, session, req.isSecure());
        }

        final Response response = new Response();
        response.setData("1");

        ResponseWriter.write(response, resp.getWriter());
    }
    
    private void doStore(final HttpServletRequest req, final HttpServletResponse resp) throws AbstractOXException, JSONException, IOException {
    	doCookieReWrite(req, resp, CookieType.SESSION);
    }

    private void doRefreshSecret(final HttpServletRequest req, final HttpServletResponse resp) throws AbstractOXException, JSONException, IOException {
    	doCookieReWrite(req, resp, CookieType.SECRET);
    }

    private boolean isAutologinEnabled() {
        final ConfigurationService configurationService = ServerServiceRegistry.getInstance().getService(ConfigurationService.class);
        return configurationService.getBoolProperty("com.openexchange.sessiond.autologin", false);
    }

    private void logAndSendException(final HttpServletResponse resp, final AbstractOXException e) throws IOException {
        LOG.debug(e.getMessage(), e);
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
    protected static void writeSecretCookie(final HttpServletResponse resp, final Session session, final boolean secure) {
        final Cookie cookie = new Cookie(SECRET_PREFIX + session.getHash(), session.getSecret());
        configureCookie(cookie, secure);
        resp.addCookie(cookie);
    }

    protected static void writeSessionCookie(final HttpServletResponse resp, final Session session, final boolean secure) {
        final Cookie cookie = new Cookie(SESSION_PREFIX + session.getHash(), session.getSessionID());
        configureCookie(cookie, secure);
        resp.addCookie(cookie);
    }

    private static void configureCookie(final Cookie cookie, final boolean secure) {
        cookie.setPath("/");
        final ConfigurationService configurationService = ServerServiceRegistry.getInstance().getService(ConfigurationService.class);
        final boolean autologin = configurationService.getBoolProperty("com.openexchange.sessiond.autologin", false);
        if (!autologin) {
            return;
        }
        final String spanDef = configurationService.getProperty("com.openexchange.sessiond.cookie.ttl", "1W");
        cookie.setMaxAge((int) (ConfigTools.parseTimespan(spanDef) / 1000));
        final boolean forceHTTPS = configurationService.getBoolProperty("com.openexchange.sessiond.cookie.forceHTTPS", false);
        if (forceHTTPS || secure) {
            cookie.setSecure(true);
        }
    }

    private void doLogin(final HttpServletRequest req, final HttpServletResponse resp) throws AjaxException, IOException {
        final LoginRequest request = parseLogin(req);
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
        // The magic spell to disable caching
        Tools.disableCaching(resp);
        resp.setContentType(CONTENTTYPE_JAVASCRIPT);
        try {
            if (response.hasError() || null == result) {
                ResponseWriter.write(response, resp.getWriter());
            } else {
                final Session session = result.getSession();
                // Store associated session
                SessionServlet.rememberSession(req, new ServerSessionAdapter(session, result.getContext(), result.getUser()));
                writeSecretCookie(resp, session, req.isSecure());
                
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

    private LoginRequest parseLogin(final HttpServletRequest req) throws AjaxException {
        final String login = req.getParameter(PARAM_NAME);
        if (null == login) {
            throw new AjaxException(AjaxException.Code.MISSING_PARAMETER, PARAM_NAME);
        }
        final String password = req.getParameter(PARAM_PASSWORD);
        if (null == password) {
            throw new AjaxException(AjaxException.Code.MISSING_PARAMETER, PARAM_PASSWORD);
        }
        final String authId = null == req.getParameter(LoginFields.AUTHID_PARAM) ? UUIDs.getUnformattedString(UUID.randomUUID()) : req.getParameter(LoginFields.AUTHID_PARAM);
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
                if (req.getParameter(LoginFields.CLIENT_PARAM) == null) {
                    return "default";
                }
                return req.getParameter(LoginFields.CLIENT_PARAM);
            }

            public String getVersion() {
                return req.getParameter(LoginFields.VERSION_PARAM);
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

}
