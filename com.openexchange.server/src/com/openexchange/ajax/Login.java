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

package com.openexchange.ajax;

import static com.openexchange.ajax.ConfigMenu.convert2JS;
import static com.openexchange.ajax.SessionServlet.removeJSESSIONID;
import static com.openexchange.ajax.SessionServlet.removeOXCookies;
import static com.openexchange.login.Interface.HTTP_JSON;
import static com.openexchange.tools.servlet.http.Cookies.getDomainValue;
import static com.openexchange.tools.servlet.http.Tools.copyHeaders;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.oauth.OAuthAccessor;
import net.oauth.OAuthException;
import net.oauth.OAuthMessage;
import net.oauth.OAuthProblemException;
import net.oauth.server.OAuthServlet;
import org.apache.commons.logging.Log;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.fields.Header;
import com.openexchange.ajax.fields.LoginFields;
import com.openexchange.ajax.helper.Send;
import com.openexchange.ajax.login.HashCalculator;
import com.openexchange.ajax.requesthandler.responseRenderers.APIResponseRenderer;
import com.openexchange.ajax.writer.LoginWriter;
import com.openexchange.ajax.writer.ResponseWriter;
import com.openexchange.authentication.LoginExceptionCodes;
import com.openexchange.config.ConfigTools;
import com.openexchange.config.ConfigurationService;
import com.openexchange.configuration.ClientWhitelist;
import com.openexchange.configuration.CookieHashSource;
import com.openexchange.configuration.ServerConfig;
import com.openexchange.configuration.ServerConfig.Property;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.settings.Setting;
import com.openexchange.groupware.settings.impl.ConfigTree;
import com.openexchange.groupware.settings.impl.SettingStorage;
import com.openexchange.java.Java7ConcurrentLinkedQueue;
import com.openexchange.java.util.UUIDs;
import com.openexchange.log.LogFactory;
import com.openexchange.log.LogProperties;
import com.openexchange.log.Props;
import com.openexchange.login.ConfigurationProperty;
import com.openexchange.login.Interface;
import com.openexchange.login.LoginRequest;
import com.openexchange.login.LoginResult;
import com.openexchange.login.internal.LoginPerformer;
import com.openexchange.oauth.provider.OAuthProviderConstants;
import com.openexchange.oauth.provider.OAuthProviderService;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.sessiond.SessionExceptionCodes;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.sessiond.impl.IPRange;
import com.openexchange.tools.io.IOTools;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.servlet.OXJSONExceptionCodes;
import com.openexchange.tools.servlet.http.Authorization;
import com.openexchange.tools.servlet.http.Authorization.Credentials;
import com.openexchange.tools.servlet.http.Tools;
import com.openexchange.tools.session.ServerSessionAdapter;

/**
 * Servlet doing the login and logout stuff.
 * 
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class Login extends AJAXServlet {

    private interface LoginClosure {

        LoginResult doLogin(final HttpServletRequest request) throws OXException;
    }

    private static final long serialVersionUID = 7680745138705836499L;

    protected static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(Login.class));

    protected static final boolean INFO = LOG.isInfoEnabled();

    private static interface JSONRequestHandler {

        void handleRequest(HttpServletRequest req, HttpServletResponse resp) throws IOException;
    }

    private static final class LoginConfiguration {

        protected final String uiWebPath;

        protected final boolean sessiondAutoLogin;

        protected final CookieHashSource hashSource;

        protected final String httpAuthAutoLogin;

        protected final String defaultClient;

        protected final String clientVersion;

        protected final String errorPageTemplate;

        protected final int cookieExpiry;

        protected final boolean insecure;

        protected final boolean cookieForceHTTPS;

        protected final boolean ipCheck;

        protected final ClientWhitelist ipCheckWhitelist;

        volatile boolean redirectIPChangeAllowed;

        protected final Queue<IPRange> ranges;

        protected LoginConfiguration(final String uiWebPath, final boolean sessiondAutoLogin, final CookieHashSource hashSource, final String httpAuthAutoLogin, final String defaultClient, final String clientVersion, final String errorPageTemplate, final int cookieExpiry, final boolean cookieForceHTTPS, final boolean insecure, final boolean ipCheck, final ClientWhitelist ipCheckWhitelist, final boolean redirectIPChangeAllowed, final Queue<IPRange> ranges) {
            super();
            this.uiWebPath = uiWebPath;
            this.sessiondAutoLogin = sessiondAutoLogin;
            this.hashSource = hashSource;
            this.httpAuthAutoLogin = httpAuthAutoLogin;
            this.defaultClient = defaultClient;
            this.clientVersion = clientVersion;
            this.errorPageTemplate = errorPageTemplate;
            this.cookieExpiry = cookieExpiry;
            this.cookieForceHTTPS = cookieForceHTTPS;
            this.insecure = insecure;
            this.ipCheck = ipCheck;
            this.ipCheckWhitelist = ipCheckWhitelist;
            this.redirectIPChangeAllowed = redirectIPChangeAllowed;
            this.ranges = ranges;
        }

        /**
         * Initializes a new {@link LoginConfiguration}.
         */
        protected LoginConfiguration() {
            super();
            this.uiWebPath = null;
            this.sessiondAutoLogin = false;
            this.hashSource = null;
            this.httpAuthAutoLogin = null;
            this.defaultClient = null;
            this.clientVersion = null;
            this.errorPageTemplate = null;
            this.cookieExpiry = -1;
            this.cookieForceHTTPS = false;
            this.insecure = false;
            this.ipCheck = false;
            this.ipCheckWhitelist = null;
            this.redirectIPChangeAllowed = true;
            this.ranges = null;
        }

    }

    /**
     * <code>"open-xchange-session-"</code>
     */
    public static final String SESSION_PREFIX = "open-xchange-session-".intern();

    /**
     * <code>"open-xchange-secret-"</code>
     */
    public static final String SECRET_PREFIX = "open-xchange-secret-".intern();

    /**
     * <code>"open-xchange-public-session"</code>
     */
    public static final String PUBLIC_SESSION_NAME = "open-xchange-public-session".intern();

    private static final String ACTION_FORMLOGIN = "formlogin";

    /**
     * <code>"changeip"</code>
     */
    public static final String ACTION_CHANGEIP = "changeip".intern();

    private static enum CookieType {
        SESSION, SECRET;
    }

    protected final AtomicReference<LoginConfiguration> confReference;

    private final Map<String, JSONRequestHandler> handlerMap;

    /**
     * Initializes the login servlet.
     */
    public Login() {
        super();
        confReference = new AtomicReference<LoginConfiguration>(new LoginConfiguration());
        final Map<String, JSONRequestHandler> map = new ConcurrentHashMap<String, Login.JSONRequestHandler>(8);
        map.put(ACTION_LOGIN, new JSONRequestHandler() {

            @Override
            public void handleRequest(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
                // Look-up necessary credentials
                try {
                    doLogin(req, resp);
                } catch (final OXException e) {
                    logAndSendException(resp, e);
                }
            }
        });
        map.put(ACTION_OAUTH, new JSONRequestHandler() {

            @Override
            public void handleRequest(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
                // Look-up necessary credentials
                try {
                    doOAuthLogin(req, resp);
                } catch (final OXException e) {
                    logAndSendException(resp, e);
                }
            }
        });
        map.put(ACTION_STORE, new JSONRequestHandler() {

            @Override
            public void handleRequest(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
                try {
                    doStore(req, resp);
                } catch (final OXException e) {
                    logAndSendException(resp, e);
                } catch (final JSONException e) {
                    log(RESPONSE_ERROR, e);
                    sendError(resp);
                }
            }
        });
        map.put(ACTION_REFRESH_SECRET, new JSONRequestHandler() {

            @Override
            public void handleRequest(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
                try {
                    doRefreshSecret(req, resp);
                } catch (final OXException e) {
                    logAndSendException(resp, e);
                } catch (final JSONException e) {
                    log(RESPONSE_ERROR, e);
                    sendError(resp);
                }
            }
        });
        map.put(ACTION_LOGOUT, new JSONRequestHandler() {

            @Override
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
                        final LoginConfiguration conf = confReference.get();
                        SessionServlet.checkIP(conf.ipCheck, conf.ranges, session, req.getRemoteAddr(), conf.ipCheckWhitelist);
                        final String secret = SessionServlet.extractSecret(conf.hashSource, req, session.getHash(), session.getClient());

                        if (secret == null || !session.getSecret().equals(secret)) {
                            LOG.info("Status code 403 (FORBIDDEN): Missing or non-matching secret.");
                            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
                            return;
                        }

                        LoginPerformer.getInstance().doLogout(sessionId);
                        // Drop relevant cookies
                        removeOXCookies(session.getHash(), req, resp);
                        removeJSESSIONID(req, resp);
                    }
                } catch (final OXException e) {
                    LOG.error("Logout failed", e);
                }
            }
        });
        map.put(ACTION_REDIRECT, new JSONRequestHandler() {

            @Override
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
                    final OXException se = ServiceExceptionCode.SERVICE_UNAVAILABLE.create(SessiondService.class.getName());
                    LOG.error(se.getMessage(), se);
                    resp.sendError(HttpServletResponse.SC_FORBIDDEN);
                    return;
                }
                final LoginConfiguration conf = confReference.get();
                final Session session;
                if (conf.insecure) {
                    if (conf.redirectIPChangeAllowed) {
                        session = sessiondService.getSessionByRandomToken(randomToken, req.getRemoteAddr());
                    } else {
                        session = sessiondService.getSessionByRandomToken(randomToken);
                        if (null != session) {
                            final String oldIP = session.getLocalIp();
                            if (null == oldIP || SessionServlet.isWhitelistedFromIPCheck(oldIP, conf.ranges)) {
                                final String newIP = req.getRemoteAddr();
                                if (!newIP.equals(oldIP)) {
                                    LOG.info("Changing IP of session " + session.getSessionID() + " with authID: " + session.getAuthId() + " from " + oldIP + " to " + newIP + '.');
                                    session.setLocalIp(newIP);
                                }
                            }
                        }
                    }
                } else {
                    // No IP change.
                    session = sessiondService.getSessionByRandomToken(randomToken);
                }
                if (session == null) {
                    // Unknown random token; throw error
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("No session could be found for random token: " + randomToken, new Throwable());
                    } else if (INFO) {
                        LOG.info("No session could be found for random token: " + randomToken);
                    }
                    resp.sendError(HttpServletResponse.SC_FORBIDDEN);
                    return;
                }
                // Remove old cookies to prevent usage of the old autologin cookie
                if (conf.insecure) {
                    SessionServlet.removeOXCookies(session.getHash(), req, resp);
                }
                try {
                    final Context context = ContextStorage.getInstance().getContext(session.getContextId());
                    final User user = UserStorage.getInstance().getUser(session.getUserId(), context);
                    if (!context.isEnabled() || !user.isMailEnabled()) {
                        LOG.info("Status code 403 (FORBIDDEN): Either context " + context.getContextId() + " or user " + user.getId() + " not enabled");
                        resp.sendError(HttpServletResponse.SC_FORBIDDEN);
                        return;
                    }
                } catch (final UndeclaredThrowableException e) {
                    LOG.info("Status code 403 (FORBIDDEN): Unexpected error occurred during login: " + e.getMessage());
                    resp.sendError(HttpServletResponse.SC_FORBIDDEN);
                    return;
                } catch (final OXException e) {
                    LOG.info("Status code 403 (FORBIDDEN): Couldn't resolve context/user by identifier: " + session.getContextId() + "/" + session.getUserId());
                    resp.sendError(HttpServletResponse.SC_FORBIDDEN);
                    return;
                }

                String client = req.getParameter(LoginFields.CLIENT_PARAM);
                final String hash;
                if (!conf.insecure) {
                    hash = session.getHash();
                } else {
                    if (null == client) {
                        client = session.getClient();
                    } else {
                        session.setClient(client);
                    }
                    hash = HashCalculator.getHash(req, client);
                    session.setHash(hash);
                }
                writeSecretCookie(resp, session, hash, req.isSecure(), req.getServerName());

                resp.sendRedirect(generateRedirectURL(
                    req.getParameter(LoginFields.UI_WEB_PATH_PARAM),
                    req.getParameter("store"),
                    session.getSessionID()));
            }
        });
        map.put(ACTION_CHANGEIP, new JSONRequestHandler() {

            @Override
            public void handleRequest(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
                final Response response = new Response();
                try {
                    final String sessionId = req.getParameter(PARAMETER_SESSION);
                    if (null == sessionId) {
                        if (INFO) {
                            final StringBuilder sb = new StringBuilder(32);
                            sb.append("Parameter \"").append(PARAMETER_SESSION).append("\" not found for action ").append(ACTION_CHANGEIP);
                            LOG.info(sb.toString());
                        }
                        throw AjaxExceptionCodes.MISSING_PARAMETER.create(PARAMETER_SESSION);
                    }
                    final String newIP = req.getParameter(LoginFields.CLIENT_IP_PARAM);
                    if (null == newIP) {
                        if (INFO) {
                            final StringBuilder sb = new StringBuilder(32);
                            sb.append("Parameter \"").append(LoginFields.CLIENT_IP_PARAM).append("\" not found for action ").append(
                                ACTION_CHANGEIP);
                            LOG.info(sb.toString());
                        }
                        throw AjaxExceptionCodes.MISSING_PARAMETER.create(LoginFields.CLIENT_IP_PARAM);
                    }
                    final SessiondService sessiondService = ServerServiceRegistry.getInstance().getService(SessiondService.class, true);
                    final Session session = sessiondService.getSession(sessionId);
                    final LoginConfiguration conf = confReference.get();
                    if (session != null) {
                        SessionServlet.checkIP(conf.ipCheck, conf.ranges, session, req.getRemoteAddr(), conf.ipCheckWhitelist);
                        final String secret = SessionServlet.extractSecret(conf.hashSource, req, session.getHash(), session.getClient());
                        if (secret == null || !session.getSecret().equals(secret)) {
                            if (INFO && null != secret) {
                                LOG.info("Session secret is different. Given secret \"" + secret + "\" differs from secret in session \"" + session.getSecret() + "\".");
                            }
                            throw SessionExceptionCodes.WRONG_SESSION_SECRET.create();
                        }
                        final String oldIP = session.getLocalIp();
                        if (!newIP.equals(oldIP)) {
                            LOG.info("Changing IP of session " + session.getSessionID() + " with authID: " + session.getAuthId() + " from " + oldIP + " to " + newIP + '.');
                            session.setLocalIp(newIP);
                        }
                        response.setData("1");
                    } else {
                        if (INFO) {
                            LOG.info("There is no session associated with session identifier: " + sessionId);
                        }
                        throw SessionExceptionCodes.SESSION_EXPIRED.create(sessionId);
                    }
                } catch (final OXException e) {
                    LOG.debug(e.getMessage(), e);
                    response.setException(e);
                }
                Tools.disableCaching(resp);
                resp.setContentType(CONTENTTYPE_JAVASCRIPT);
                resp.setStatus(HttpServletResponse.SC_OK);
                try {
                    ResponseWriter.write(response, resp.getWriter());
                } catch (final JSONException e) {
                    log(RESPONSE_ERROR, e);
                    sendError(resp);
                }
            }
        });
        map.put(ACTION_REDEEM, new JSONRequestHandler() {

            @Override
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
                    final OXException se = ServiceExceptionCode.SERVICE_UNAVAILABLE.create(SessiondService.class.getName());
                    LOG.error(se.getMessage(), se);
                    resp.sendError(HttpServletResponse.SC_FORBIDDEN);
                    return;
                }
                final LoginConfiguration conf = confReference.get();
                final Session session;
                if (conf.insecure) {
                    if (conf.redirectIPChangeAllowed) {
                        session = sessiondService.getSessionByRandomToken(randomToken, req.getRemoteAddr());
                    } else {
                        session = sessiondService.getSessionByRandomToken(randomToken);
                        if (null != session) {
                            final String oldIP = session.getLocalIp();
                            if (null == oldIP || SessionServlet.isWhitelistedFromIPCheck(oldIP, conf.ranges)) {
                                final String newIP = req.getRemoteAddr();
                                if (!newIP.equals(oldIP)) {
                                    LOG.info("Changing IP of session " + session.getSessionID() + " with authID: " + session.getAuthId() + " from " + oldIP + " to " + newIP + '.');
                                    session.setLocalIp(newIP);
                                }
                            }
                        }
                    }
                } else {
                    // No IP change.
                    session = sessiondService.getSessionByRandomToken(randomToken);
                }
                if (session == null) {
                    // Unknown random token; throw error
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("No session could be found for random token: " + randomToken, new Throwable());
                    } else if (INFO) {
                        LOG.info("No session could be found for random token: " + randomToken);
                    }
                    resp.sendError(HttpServletResponse.SC_FORBIDDEN);
                    return;
                }
                // Remove old cookies to prevent usage of the old autologin cookie
                if (conf.insecure) {
                    SessionServlet.removeOXCookies(session.getHash(), req, resp);
                }
                try {
                    final Context context = ContextStorage.getInstance().getContext(session.getContextId());
                    final User user = UserStorage.getInstance().getUser(session.getUserId(), context);
                    if (!context.isEnabled() || !user.isMailEnabled()) {
                        LOG.info("Status code 403 (FORBIDDEN): Either context " + context.getContextId() + " or user " + user.getId() + " not enabled");
                        resp.sendError(HttpServletResponse.SC_FORBIDDEN);
                        return;
                    }
                } catch (final UndeclaredThrowableException e) {
                    LOG.info("Status code 403 (FORBIDDEN): Unexpected error occurred during login: " + e.getMessage());
                    resp.sendError(HttpServletResponse.SC_FORBIDDEN);
                    return;
                } catch (final OXException e) {
                    LOG.info("Status code 403 (FORBIDDEN): Couldn't resolve context/user by identifier: " + session.getContextId() + "/" + session.getUserId());
                    resp.sendError(HttpServletResponse.SC_FORBIDDEN);
                    return;
                }

                String client = req.getParameter(LoginFields.CLIENT_PARAM);
                final String hash;
                if (!conf.insecure) {
                    hash = session.getHash();
                } else {
                    if (null == client) {
                        client = session.getClient();
                    } else {
                        session.setClient(client);
                    }
                    hash = HashCalculator.getHash(req, client);
                    session.setHash(hash);
                }
                writeSecretCookie(resp, session, hash, req.isSecure(), req.getServerName());

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

            @Override
            public void handleRequest(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
                Tools.disableCaching(resp);
                resp.setContentType(CONTENTTYPE_JAVASCRIPT);
                final Response response = new Response();
                Session session = null;
                try {
                    final LoginConfiguration conf = confReference.get();
                    if (!conf.sessiondAutoLogin) {
                        if (doAutoLogin(req, resp)) {
                            throw AjaxExceptionCodes.DISABLED_ACTION.create("autologin");
                        }
                        return;
                    }

                    final Cookie[] cookies = req.getCookies();
                    if (cookies == null) {
                        throw OXJSONExceptionCodes.INVALID_COOKIE.create();
                    }

                    final SessiondService sessiondService = ServerServiceRegistry.getInstance().getService(SessiondService.class);
                    if (null == sessiondService) {
                        final OXException se = ServiceExceptionCode.SERVICE_UNAVAILABLE.create(SessiondService.class.getName());
                        LOG.error(se.getMessage(), se);
                        resp.sendError(HttpServletResponse.SC_FORBIDDEN);
                        return;
                    }
                    String secret = null;
                    final String hash = HashCalculator.getHash(req);
                    final String sessionCookieName = SESSION_PREFIX + hash;
                    final String secretCookieName = SECRET_PREFIX + hash;

                    NextCookie: for (final Cookie cookie : cookies) {
                        final String cookieName = cookie.getName();
                        if (cookieName.startsWith(sessionCookieName)) {
                            final String sessionId = cookie.getValue();
                            session = sessiondService.getSession(sessionId);
                            if (null != session) {
                                // IP check if enabled; otherwise update session's IP address if different to request's IP address
                                // Insecure check is done in updateIPAddress method.
                                if (!conf.ipCheck) {
                                    // Update IP address if necessary
                                    updateIPAddress(req.getRemoteAddr(), session);
                                } else {
                                    final String newIP = req.getRemoteAddr();
                                    SessionServlet.checkIP(true, conf.ranges, session, newIP, conf.ipCheckWhitelist);
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
                                    throw LoginExceptionCodes.UNKNOWN.create(e, e.getMessage());
                                }
                                final JSONObject json = new JSONObject();
                                LoginWriter.write(session, json);
                                json.put(AJAXServlet.PARAMETER_USER, session.getLogin());
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
                        if (doAutoLogin(req, resp)) {
                            throw OXJSONExceptionCodes.INVALID_COOKIE.create();
                        }
                        return;
                    }
                } catch (final OXException e) {
                    e.log(LOG);
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
                } catch (final JSONException e) {
                    final OXException oje = OXJSONExceptionCodes.JSON_WRITE_ERROR.create(e);
                    LOG.error(oje.getMessage(), oje);
                    response.setException(oje);
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

            @Override
            public void handleRequest(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
                try {
                    doFormLogin(req, resp);
                } catch (final OXException e) {
                    final String errorPage = confReference.get().errorPageTemplate.replace("ERROR_MESSAGE", e.getMessage());
                    resp.setContentType(CONTENTTYPE_HTML);
                    resp.getWriter().write(errorPage);
                }
            }
        });
        handlerMap = Collections.unmodifiableMap(map);
    }

    @Override
    public void init(final ServletConfig config) throws ServletException {
        super.init(config);
        final String uiWebPath = config.getInitParameter(ServerConfig.Property.UI_WEB_PATH.getPropertyName());
        final boolean sessiondAutoLogin = Boolean.parseBoolean(config.getInitParameter(ConfigurationProperty.SESSIOND_AUTOLOGIN.getPropertyName()));
        final CookieHashSource hashSource = CookieHashSource.parse(config.getInitParameter(Property.COOKIE_HASH.getPropertyName()));
        final String httpAuthAutoLogin = config.getInitParameter(ConfigurationProperty.HTTP_AUTH_AUTOLOGIN.getPropertyName());
        final String defaultClient = config.getInitParameter(ConfigurationProperty.HTTP_AUTH_CLIENT.getPropertyName());
        final String clientVersion = config.getInitParameter(ConfigurationProperty.HTTP_AUTH_VERSION.getPropertyName());
        final String templateFileLocation = config.getInitParameter(ConfigurationProperty.ERROR_PAGE_TEMPLATE.getPropertyName());
        String errorPageTemplate;
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
        final int cookieExpiry = ConfigTools.parseTimespanSecs(config.getInitParameter(ServerConfig.Property.COOKIE_TTL.getPropertyName()));
        final boolean cookieForceHTTPS = Boolean.parseBoolean(config.getInitParameter(ServerConfig.Property.COOKIE_FORCE_HTTPS.getPropertyName())) || Boolean.parseBoolean(config.getInitParameter(ServerConfig.Property.FORCE_HTTPS.getPropertyName()));
        final boolean insecure = Boolean.parseBoolean(config.getInitParameter(ConfigurationProperty.INSECURE.getPropertyName()));
        final boolean ipCheck = Boolean.parseBoolean(config.getInitParameter(ServerConfig.Property.IP_CHECK.getPropertyName()));
        final ClientWhitelist ipCheckWhitelist = new ClientWhitelist().add(config.getInitParameter(Property.IP_CHECK_WHITELIST.getPropertyName()));
        final boolean redirectIPChangeAllowed = Boolean.parseBoolean(config.getInitParameter(ConfigurationProperty.REDIRECT_IP_CHANGE_ALLOWED.getPropertyName()));
        final Queue<IPRange> ranges = new Java7ConcurrentLinkedQueue<IPRange>();
        final String tmp = config.getInitParameter(ConfigurationProperty.NO_IP_CHECK_RANGE.getPropertyName());
        if (tmp != null) {
            final String[] lines = tmp.split("\n");
            for (String line : lines) {
                line = line.replaceAll("\\s", "");
                if (!line.equals("") && (line.length() == 0 || line.charAt(0) != '#')) {
                    ranges.add(IPRange.parseRange(line));
                }
            }
        }
        confReference.set(new LoginConfiguration(
            uiWebPath,
            sessiondAutoLogin,
            hashSource,
            httpAuthAutoLogin,
            defaultClient,
            clientVersion,
            errorPageTemplate,
            cookieExpiry,
            cookieForceHTTPS,
            insecure,
            ipCheck,
            ipCheckWhitelist,
            redirectIPChangeAllowed,
            ranges));
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
            logAndSendException(resp, AjaxExceptionCodes.MISSING_PARAMETER.create(PARAMETER_ACTION));
            return;
        }
    }

    private void doJSONAuth(final HttpServletRequest req, final HttpServletResponse resp, final String action) throws IOException {
        final JSONRequestHandler handler = handlerMap.get(action);
        if (null == handler) {
            logAndSendException(resp, AjaxExceptionCodes.UNKNOWN_ACTION.create(action));
            return;
        }
        handler.handleRequest(req, resp);
    }

    private void doHttpAuth(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        if (req.getHeader(Header.AUTH_HEADER) != null) {
            try {
                doAuthHeaderLogin(req, resp);

            } catch (final OXException e) {
                LOG.error(e.getMessage(), e);
                resp.addHeader("WWW-Authenticate", "NEGOTIATE");
                resp.addHeader("WWW-Authenticate", "Basic realm=\"Open-Xchange\"");
                resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, e.getMessage());
            }
        } else {
            resp.addHeader("WWW-Authenticate", "NEGOTIATE");
            resp.addHeader("WWW-Authenticate", "Basic realm=\"Open-Xchange\"");
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authorization Required!");
        }
    }

    /**
     * Updates session's IP address if different to specified IP address.
     * 
     * @param newIP The possibly new IP address
     * @param session The session to update if IP addresses differ
     */
    protected void updateIPAddress(final String newIP, final Session session) {
        if (confReference.get().insecure) {
            final String oldIP = session.getLocalIp();
            if (null != newIP && !newIP.equals(oldIP)) { // IPs differ
                LOG.info(new StringBuilder("Updating sessions IP address. authID: ").append(session.getAuthId()).append(", sessionID: ").append(
                    session.getSessionID()).append(", old ip: ").append(oldIP).append(", new ip: ").append(newIP).toString());
                session.setLocalIp(newIP);
            }
        }
    }

    protected static String addFragmentParameter(final String usedUIWebPath, final String param, final String value) {
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
    private void doCookieReWrite(final HttpServletRequest req, final HttpServletResponse resp, final CookieType type) throws OXException, JSONException, IOException {
        final LoginConfiguration conf = confReference.get();
        if (!conf.sessiondAutoLogin && CookieType.SESSION == type) {
            throw AjaxExceptionCodes.DISABLED_ACTION.create("store");
        }
        final SessiondService sessiond = ServerServiceRegistry.getInstance().getService(SessiondService.class);
        if (null == sessiond) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(SessiondService.class.getName());
        }

        final String sessionId = req.getParameter(PARAMETER_SESSION);
        if (null == sessionId) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create(PARAMETER_SESSION);
        }
        final Session session = SessionServlet.getSession(conf.hashSource, req, sessionId, sessiond);
        try {
            SessionServlet.checkIP(conf.ipCheck, conf.ranges, session, req.getRemoteAddr(), conf.ipCheckWhitelist);
            if (type == CookieType.SESSION) {
                writeSessionCookie(resp, session, session.getHash(), req.isSecure(), req.getServerName());
            } else {
                writeSecretCookie(resp, session, session.getHash(), req.isSecure(), req.getServerName());
            }
            // Refresh HTTP session, too
            req.getSession();
            final Response response = new Response();
            response.setData("1");
            ResponseWriter.write(response, resp.getWriter());
        } finally {
            if (LogProperties.isEnabled()) {
                final Props properties = LogProperties.optLogProperties();
                if (null != properties) {
                    properties.remove("com.openexchange.session.sessionId");
                    properties.remove("com.openexchange.session.userId");
                    properties.remove("com.openexchange.session.contextId");
                    properties.remove("com.openexchange.session.clientId");
                    properties.remove("com.openexchange.session.session");
                }
            }
        }
    }

    protected void doStore(final HttpServletRequest req, final HttpServletResponse resp) throws OXException, JSONException, IOException {
        Tools.disableCaching(resp);
        resp.setContentType(CONTENTTYPE_JAVASCRIPT);
        doCookieReWrite(req, resp, CookieType.SESSION);
    }

    protected void doRefreshSecret(final HttpServletRequest req, final HttpServletResponse resp) throws OXException, JSONException, IOException {
        Tools.disableCaching(resp);
        resp.setContentType(CONTENTTYPE_JAVASCRIPT);
        doCookieReWrite(req, resp, CookieType.SECRET);
    }

    protected static void logAndSendException(final HttpServletResponse resp, final OXException e) throws IOException {
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
     * Writes the (groupware's) secret cookie to specified HTTP servlet response whose name is composed by cookie prefix
     * <code>"open-xchange-secret-"</code> and a secret cookie identifier.
     *
     * @param resp The HTTP servlet response
     * @param session The session providing the secret cookie identifier
     * @param hash The hash string used for composing cookie name
     * @param secure <code>true</code> to set cookie's secure flag; otherwise <code>false</code>
     * @deprecated Use {@link #writeSecretCookie(HttpServletResponse, Session, String, boolean, String)}
     */
    @Deprecated
    protected void writeSecretCookie(final HttpServletResponse resp, final Session session, final String hash, final boolean secure) {
        writeSecretCookie(resp, session, hash, secure, null);
    }

    /**
     * Writes the (groupware's) secret cookie to specified HTTP servlet response whose name is composed by cookie prefix
     * <code>"open-xchange-secret-"</code> and a secret cookie identifier.
     * 
     * @param resp The HTTP servlet response
     * @param session The session providing the secret cookie identifier
     * @param hash The hash string used for composing cookie name
     * @param secure <code>true</code> to set cookie's secure flag; otherwise <code>false</code>
     * @param serverName The HTTP request's server name
     */
    protected void writeSecretCookie(final HttpServletResponse resp, final Session session, final String hash, final boolean secure, final String serverName) {
        Cookie cookie = new Cookie(SECRET_PREFIX + hash, session.getSecret());
        configureCookie(cookie, secure, serverName);
        resp.addCookie(cookie);

        final String altId = (String) session.getParameter(Session.PARAM_ALTERNATIVE_ID);
        if (null != altId) {
            cookie = new Cookie(PUBLIC_SESSION_NAME, altId);
            configureCookie(cookie, secure, serverName);
            resp.addCookie(cookie);
        }
    }

    /**
     * Writes the (groupware's) session cookie to specified HTTP servlet response whose name is composed by cookie prefix
     * <code>"open-xchange-session-"</code> and a secret cookie identifier.
     * 
     * @param resp The HTTP servlet response
     * @param session The session providing the secret cookie identifier
     * @param hash The hash string used for composing cookie name
     * @param secure <code>true</code> to set cookie's secure flag; otherwise <code>false</code>
     * @param serverName The HTTP request's server name
     * @deprecated Use {@link #writeSessionCookie(HttpServletResponse, Session, String, boolean, String)}
     */
    @Deprecated
    protected void writeSessionCookie(final HttpServletResponse resp, final Session session, final String hash, final boolean secure) {
        writeSessionCookie(resp, session, hash, secure, null);
    }

    /**
     * Writes the (groupware's) session cookie to specified HTTP servlet response whose name is composed by cookie prefix
     * <code>"open-xchange-session-"</code> and a secret cookie identifier.
     * 
     * @param resp The HTTP servlet response
     * @param session The session providing the secret cookie identifier
     * @param hash The hash string used for composing cookie name
     * @param secure <code>true</code> to set cookie's secure flag; otherwise <code>false</code>
     * @param serverName The HTTP request's server name
     */
    protected void writeSessionCookie(final HttpServletResponse resp, final Session session, final String hash, final boolean secure, final String serverName) {
        final Cookie cookie = new Cookie(SESSION_PREFIX + hash, session.getSessionID());
        configureCookie(cookie, secure, serverName);
        resp.addCookie(cookie);
    }

    private void configureCookie(final Cookie cookie, final boolean secure, final String serverName) {
        cookie.setPath("/");
        final LoginConfiguration conf = confReference.get();
        if (conf.cookieForceHTTPS || secure) {
            cookie.setSecure(true);
        }
        if (conf.sessiondAutoLogin || conf.cookieExpiry < 0) {
            /*
             * A negative value means that the cookie is not stored persistently and will be deleted when the Web browser exits. A zero
             * value causes the cookie to be deleted.
             */
            cookie.setMaxAge(conf.cookieExpiry);
        }
        final String domain = getDomainValue(null == serverName ? LogProperties.<String> getLogProperty("com.openexchange.ajp13.serverName") : serverName);
        if (null != domain) {
            cookie.setDomain(domain);
        }
    }

    protected boolean doAutoLogin(final HttpServletRequest req, final HttpServletResponse resp) throws IOException, OXException {
        return loginOperation(req, resp, new LoginClosure() {

            @Override
            public LoginResult doLogin(final HttpServletRequest req2) throws OXException {
                final LoginRequest request = parseAutoLoginRequest(req2);
                return LoginPerformer.getInstance().doAutoLogin(request);
            }
        });
    }

    protected void doLogin(final HttpServletRequest req, final HttpServletResponse resp) throws IOException, OXException {
        loginOperation(req, resp, new LoginClosure() {

            @Override
            public LoginResult doLogin(final HttpServletRequest req2) throws OXException {
                final LoginRequest request = parseLogin(req2, LoginFields.NAME_PARAM, false);
                return LoginPerformer.getInstance().doLogin(request);
            }
        });
    }

    protected void doOAuthLogin(final HttpServletRequest req, final HttpServletResponse resp) throws IOException, OXException {
        loginOperation(req, resp, new LoginClosure() {

            @Override
            public LoginResult doLogin(final HttpServletRequest req2) throws OXException {
                try {
                    final OAuthProviderService providerService = ServerServiceRegistry.getInstance().getService(OAuthProviderService.class);
                    final OAuthMessage requestMessage = OAuthServlet.getMessage(req2, null);
                    final OAuthAccessor accessor = providerService.getAccessor(requestMessage);
                    providerService.getValidator().validateMessage(requestMessage, accessor);
                    final String login = accessor.<String> getProperty(OAuthProviderConstants.PROP_LOGIN);
                    final String password = accessor.<String> getProperty(OAuthProviderConstants.PROP_PASSWORD);
                    final LoginRequest request = parseLogin(req2, login, password, false);
                    return LoginPerformer.getInstance().doLogin(request);
                } catch (final OAuthProblemException e) {
                    try {
                        handleException(e, req2, resp, false);
                        return null;
                    } catch (final IOException ioe) {
                        throw LoginExceptionCodes.UNKNOWN.create(ioe, ioe.getMessage());
                    } catch (final ServletException se) {
                        throw LoginExceptionCodes.UNKNOWN.create(se, se.getMessage());
                    }
                } catch (final IOException e) {
                    throw LoginExceptionCodes.UNKNOWN.create(e, e.getMessage());
                } catch (final OAuthException e) {
                    throw LoginExceptionCodes.UNKNOWN.create(e, e.getMessage());
                } catch (final URISyntaxException e) {
                    throw LoginExceptionCodes.UNKNOWN.create(e, e.getMessage());
                }
            }

            private void handleException(final Exception e, final HttpServletRequest request, final HttpServletResponse response, final boolean sendBody) throws IOException, ServletException {
                final StringBuilder realm = new StringBuilder(32).append((request.isSecure()) ? "https://" : "http://");
                realm.append(request.getLocalName());
                OAuthServlet.handleException(response, e, realm.toString(), sendBody);
            }
        });
    }

    /**
     * @return a boolean value indicated if an auto login should proceed afterwards
     */
    private boolean loginOperation(final HttpServletRequest req, final HttpServletResponse resp, final LoginClosure login) throws IOException, OXException {
        Tools.disableCaching(resp);
        resp.setContentType(CONTENTTYPE_JAVASCRIPT);
        // Perform the login
        final Response response = new Response();
        LoginResult result = null;
        try {
            final Map<String, Object> properties = new HashMap<String, Object>(1);
            properties.put("http.request", req);
            {
                final String capabilities = req.getParameter("capabilities");
                if (null != capabilities) {
                    properties.put("client.capabilities", capabilities);
                }
            }
            result = login.doLogin(req);
            if (null == result) {
                return true;
            }
            addHeadersAndCookies(result, resp);
            if (null != result.getCode()) {
                switch (result.getCode()) {
                case FAILED:
                    return true;
                case REDIRECT:
                    throw LoginExceptionCodes.REDIRECT.create(result.getRedirect());
                default:
                    break;
                }
            }
            final Session session = result.getSession();
            session.setParameter("user-agent", req.getHeader("user-agent"));
            // Write response
            final JSONObject json = new JSONObject();
            LoginWriter.write(result, json);
            // Append "config/modules"
            appendModules(session, json, req);
            response.setData(json);
        } catch (final OXException e) {
            if (AjaxExceptionCodes.PREFIX.equals(e.getPrefix())) {
                throw e;
            }
            if (LoginExceptionCodes.REDIRECT.equals(e)) {
                LOG.debug(e.getMessage(), e);
            } else {
                LOG.error(e.getMessage(), e);
            }
            response.setException(e);
        } catch (final JSONException e) {
            final OXException oje = OXJSONExceptionCodes.JSON_WRITE_ERROR.create(e);
            LOG.error(oje.getMessage(), oje);
            response.setException(oje);
        }
        try {
            if (response.hasError() || null == result) {
                ResponseWriter.write(response, resp.getWriter());
                return false;
            }
            final Session session = result.getSession();
            // Store associated session
            SessionServlet.rememberSession(req, new ServerSessionAdapter(session, result.getContext(), result.getUser()));
            writeSecretCookie(resp, session, session.getHash(), req.isSecure(), req.getServerName());
            // Login response is unfortunately not conform to default responses.
            if (req.getParameter("callback") != null && req.getParameter("action").equals(ACTION_LOGIN)) {
                APIResponseRenderer.writeResponse(response, ACTION_LOGIN, req, resp);
            } else {
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
            return false;
        }
        return false;
    }

    private static void addHeadersAndCookies(final LoginResult result, final HttpServletResponse resp) {
        final com.openexchange.authentication.Cookie[] cookies = result.getCookies();
        if (null != cookies) {
            for (final com.openexchange.authentication.Cookie cookie : cookies) {
                resp.addCookie(wrapCookie(cookie));
            }
        }
        final com.openexchange.authentication.Header[] headers = result.getHeaders();
        if (null != headers) {
            for (final com.openexchange.authentication.Header header : headers) {
                resp.addHeader(header.getName(), header.getValue());
            }
        }
    }

    private static Cookie wrapCookie(final com.openexchange.authentication.Cookie cookie) {
        return new Cookie(cookie.getName(), cookie.getValue());
    }

    protected LoginRequest parseLogin(final HttpServletRequest req, final String loginParamName, final boolean strict) throws OXException {
        final String login;
        {
            String tmp = req.getParameter(loginParamName);
            if (null == tmp) {
                throw AjaxExceptionCodes.MISSING_PARAMETER.create(loginParamName);
            }
            final ConfigurationService service = ServerServiceRegistry.getInstance().getService(ConfigurationService.class);
            final Object prop = null == service ? null : service.getProperty("com.openexchange.login.disableTrimLogin");
            if (null == prop || !(Boolean.parseBoolean(prop.toString()))) {
                tmp = tmp.trim();
            }
            login = tmp;
        }
        final String password = req.getParameter(LoginFields.PASSWORD_PARAM);
        if (null == password) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create(LoginFields.PASSWORD_PARAM);
        }
        return parseLogin(req, login, password, strict);
    }

    protected LoginRequest parseLogin(final HttpServletRequest req, final String login, final String password, final boolean strict) throws OXException {
        final String authId = parseAuthId(req, strict);
        final String client = parseClient(req, strict);
        final String version;
        if (null == req.getParameter(LoginFields.VERSION_PARAM)) {
            if (strict) {
                throw AjaxExceptionCodes.MISSING_PARAMETER.create(LoginFields.VERSION_PARAM);
            }
            version = null;
        } else {
            version = req.getParameter(LoginFields.VERSION_PARAM);
        }
        final String clientIP = parseClientIP(req);
        final String userAgent = parseUserAgent(req);
        final Map<String, List<String>> headers = copyHeaders(req);
        final com.openexchange.authentication.Cookie[] cookies = Tools.getCookieFromHeader(req);
        final LoginRequest loginRequest = new LoginRequest() {

            private final String hash = HashCalculator.getHash(req, userAgent, client);

            @Override
            public String getLogin() {
                return login;
            }

            @Override
            public String getPassword() {
                return password;
            }

            @Override
            public String getClientIP() {
                return clientIP;
            }

            @Override
            public String getUserAgent() {
                return userAgent;
            }

            @Override
            public String getAuthId() {
                return authId;
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
            public Interface getInterface() {
                return HTTP_JSON;
            }

            @Override
            public String getHash() {
                return hash;
            }

            @Override
            public Map<String, List<String>> getHeaders() {
                return headers;
            }

            @Override
            public com.openexchange.authentication.Cookie[] getCookies() {
                return cookies;
            }
        };
        return loginRequest;
    }

    protected LoginRequest parseAutoLoginRequest(final HttpServletRequest req) throws OXException {
        final String authId = parseAuthId(req, false);
        final String client = parseClient(req, false);
        final String clientIP = parseClientIP(req);
        final String userAgent = parseUserAgent(req);
        final Map<String, List<String>> headers = copyHeaders(req);
        final com.openexchange.authentication.Cookie[] cookies = Tools.getCookieFromHeader(req);
        return new LoginRequest() {

            private final String hash = HashCalculator.getHash(req, client);

            @Override
            public String getVersion() {
                return null;
            }

            @Override
            public String getUserAgent() {
                return userAgent;
            }

            @Override
            public String getPassword() {
                return null;
            }

            @Override
            public String getLogin() {
                return null;
            }

            @Override
            public Interface getInterface() {
                return HTTP_JSON;
            }

            @Override
            public Map<String, List<String>> getHeaders() {
                return headers;
            }

            @Override
            public String getHash() {
                return hash;
            }

            @Override
            public String getClientIP() {
                return clientIP;
            }

            @Override
            public String getClient() {
                return client;
            }

            @Override
            public String getAuthId() {
                return authId;
            }

            @Override
            public com.openexchange.authentication.Cookie[] getCookies() {
                return cookies;
            }
        };
    }

    private static String parseUserAgent(final HttpServletRequest req) {
        final String parameter = req.getParameter(LoginFields.USER_AGENT);
        return null == parameter ? req.getHeader(Header.USER_AGENT) : parameter;
    }

    private static String parseClientIP(final HttpServletRequest req) {
        final String parameter = req.getParameter(LoginFields.CLIENT_IP_PARAM);
        return null == parameter ? req.getRemoteAddr() : parameter;
    }

    private String parseClient(final HttpServletRequest req, final boolean strict) throws OXException {
        final String parameter = req.getParameter(LoginFields.CLIENT_PARAM);
        if (null == parameter) {
            if (strict) {
                throw AjaxExceptionCodes.MISSING_PARAMETER.create(LoginFields.CLIENT_PARAM);
            }
            return confReference.get().defaultClient;
        }
        return parameter;
    }

    private String parseClient(final HttpServletRequest req) {
        try {
            return parseClient(req, false);
        } catch (final OXException e) {
            return confReference.get().defaultClient;
        }
    }

    private static String parseAuthId(final HttpServletRequest req, final boolean strict) throws OXException {
        final String authIdParam = req.getParameter(LoginFields.AUTHID_PARAM);
        if (null == authIdParam) {
            if (strict) {
                throw AjaxExceptionCodes.MISSING_PARAMETER.create(LoginFields.AUTHID_PARAM);
            }
            return UUIDs.getUnformattedString(UUID.randomUUID());
        }
        return authIdParam;
    }

    protected static void appendModules(final Session session, final JSONObject json, final HttpServletRequest req) {
        final String modules = "modules";
        if (parseBoolean(req.getParameter(modules))) {
            try {
                final Setting setting = ConfigTree.getInstance().getSettingByPath(modules);
                SettingStorage.getInstance(session).readValues(setting);
                json.put(modules, convert2JS(setting));
            } catch (final OXException e) {
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

    protected void doFormLogin(final HttpServletRequest req, final HttpServletResponse resp) throws OXException, OXException, IOException {
        final LoginRequest request = parseLogin(req, LoginFields.LOGIN_PARAM, true);
        final Map<String, Object> properties = new HashMap<String, Object>(1);
        properties.put("http.request", req);
        {
            final String capabilities = req.getParameter("capabilities");
            if (null != capabilities) {
                properties.put("client.capabilities", capabilities);
            }
        }
        final LoginResult result = LoginPerformer.getInstance().doLogin(request, properties);
        final Session session = result.getSession();

        Tools.disableCaching(resp);
        writeSecretCookie(resp, session, session.getHash(), req.isSecure(), req.getServerName());
        resp.sendRedirect(generateRedirectURL(
            req.getParameter(LoginFields.UI_WEB_PATH_PARAM),
            req.getParameter(LoginFields.AUTOLOGIN_PARAM),
            session.getSessionID()));
    }

    private void doAuthHeaderLogin(final HttpServletRequest req, final HttpServletResponse resp) throws OXException, IOException {
        final String auth = req.getHeader(Header.AUTH_HEADER);
        final String version;
        final Credentials creds;
        if (!Authorization.checkForAuthorizationHeader(auth)) {
            throw LoginExceptionCodes.UNKNOWN_HTTP_AUTHORIZATION.create();
        }
        final LoginConfiguration conf = confReference.get();
        if (Authorization.checkForBasicAuthorization(auth)) {
            creds = Authorization.decode(auth);
            version = conf.clientVersion;
        } else if (Authorization.checkForKerberosAuthorization(auth)) {
            creds = new Credentials("kerberos", "");
            version = "Kerberos";
        } else {
            throw LoginExceptionCodes.UNKNOWN_HTTP_AUTHORIZATION.create("");
        }
        final String client = parseClient(req);
        final String clientIP = parseClientIP(req);
        final String userAgent = parseUserAgent(req);
        final Map<String, List<String>> headers = copyHeaders(req);
        final com.openexchange.authentication.Cookie[] cookies = Tools.getCookieFromHeader(req);
        final LoginRequest request = new LoginRequest() {

            private final String hash = HashCalculator.getHash(req, userAgent, client);

            @Override
            public String getVersion() {
                return version;
            }

            @Override
            public String getUserAgent() {
                return userAgent;
            }

            @Override
            public String getPassword() {
                return creds.getPassword();
            }

            @Override
            public String getLogin() {
                return creds.getLogin();
            }

            @Override
            public Interface getInterface() {
                return Interface.HTTP_JSON;
            }

            @Override
            public String getHash() {
                return hash;
            }

            @Override
            public String getClientIP() {
                return clientIP;
            }

            @Override
            public String getClient() {
                return client;
            }

            @Override
            public String getAuthId() {
                return UUIDs.getUnformattedString(UUID.randomUUID());
            }

            @Override
            public Map<String, List<String>> getHeaders() {
                return headers;
            }

            @Override
            public com.openexchange.authentication.Cookie[] getCookies() {
                return cookies;
            }
        };
        final Map<String, Object> properties = new HashMap<String, Object>(1);
        properties.put("http.request", req);
        {
            final String capabilities = req.getParameter("capabilities");
            if (null != capabilities) {
                properties.put("client.capabilities", capabilities);
            }
        }
        final LoginResult result = LoginPerformer.getInstance().doLogin(request, properties);
        final Session session = result.getSession();
        Tools.disableCaching(resp);
        writeSecretCookie(resp, session, session.getHash(), req.isSecure(), req.getServerName());
        addHeadersAndCookies(result, resp);
        resp.sendRedirect(generateRedirectURL(null, conf.httpAuthAutoLogin, session.getSessionID()));
    }

    protected String generateRedirectURL(final String uiWebPathParam, final String shouldStore, final String sessionId) {
        String retval = uiWebPathParam;
        if (null == retval) {
            retval = confReference.get().uiWebPath;
        }
        // Prevent HTTP response splitting.
        retval = retval.replaceAll("[\n\r]", "");
        retval = addFragmentParameter(retval, PARAMETER_SESSION, sessionId);
        if (shouldStore != null) {
            retval = addFragmentParameter(retval, "store", shouldStore);
        }
        return retval;
    }

    private static final String ERROR_PAGE_TEMPLATE = "<html>\n" + "<script type=\"text/javascript\">\n" + "// Display normal HTML for 5 seconds, then redirect via referrer.\n" + "setTimeout(redirect,5000);\n" + "function redirect(){\n" + " var referrer=document.referrer;\n" + " var redirect_url;\n" + " // If referrer already contains failed parameter, we don't add a 2nd one.\n" + " if(referrer.indexOf(\"login=failed\")>=0){\n" + "  redirect_url=referrer;\n" + " }else{\n" + "  // Check if referrer contains multiple parameter\n" + "  if(referrer.indexOf(\"?\")<0){\n" + "   redirect_url=referrer+\"?login=failed\";\n" + "  }else{\n" + "   redirect_url=referrer+\"&login=failed\";\n" + "  }\n" + " }\n" + " // Redirect to referrer\n" + " window.location.href=redirect_url;\n" + "}\n" + "</script>\n" + "<body>\n" + "<h1>ERROR_MESSAGE</h1>\n" + "</body>\n" + "</html>\n";
}
