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
import static com.openexchange.ajax.login.LoginTools.updateIPAddress;
import static com.openexchange.login.Interface.HTTP_JSON;
import static com.openexchange.tools.servlet.http.Cookies.getDomainValue;
import static com.openexchange.tools.servlet.http.Tools.copyHeaders;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.fields.Header;
import com.openexchange.ajax.fields.LoginFields;
import com.openexchange.ajax.helper.Send;
import com.openexchange.ajax.login.FormLogin;
import com.openexchange.ajax.login.HashCalculator;
import com.openexchange.ajax.login.LoginConfiguration;
import com.openexchange.ajax.login.LoginRequestHandler;
import com.openexchange.ajax.login.LoginRequestImpl;
import com.openexchange.ajax.login.LoginTools;
import com.openexchange.ajax.login.RedeemToken;
import com.openexchange.ajax.login.TokenLogin;
import com.openexchange.ajax.login.Tokens;
import com.openexchange.ajax.requesthandler.responseRenderers.APIResponseRenderer;
import com.openexchange.ajax.writer.LoginWriter;
import com.openexchange.ajax.writer.ResponseWriter;
import com.openexchange.authentication.LoginExceptionCodes;
import com.openexchange.authentication.ResultCode;
import com.openexchange.config.ConfigTools;
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
import com.openexchange.i18n.LocaleTools;
import com.openexchange.java.Strings;
import com.openexchange.java.util.UUIDs;
import com.openexchange.log.LogFactory;
import com.openexchange.log.LogProperties;
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
import com.openexchange.threadpool.AbstractTask;
import com.openexchange.threadpool.ThreadPools;
import com.openexchange.tools.io.IOTools;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.servlet.OXJSONExceptionCodes;
import com.openexchange.tools.servlet.http.Authorization;
import com.openexchange.tools.servlet.http.Authorization.Credentials;
import com.openexchange.tools.servlet.http.Cookies;
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

    /** The log properties for login-related information. */
    protected static final Set<LogProperties.Name> LOG_PROPERTIES;

    static {
        final Set<LogProperties.Name> set = EnumSet.noneOf(LogProperties.Name.class);
        set.add(LogProperties.Name.LOGIN_AUTH_ID);
        set.add(LogProperties.Name.LOGIN_CLIENT);
        set.add(LogProperties.Name.LOGIN_CLIENT_IP);
        set.add(LogProperties.Name.LOGIN_LOGIN);
        set.add(LogProperties.Name.LOGIN_USER_AGENT);
        set.add(LogProperties.Name.LOGIN_VERSION);
        set.add(LogProperties.Name.SESSION_SESSION_ID);
        set.add(LogProperties.Name.SESSION_USER_ID);
        set.add(LogProperties.Name.SESSION_CONTEXT_ID);
        set.add(LogProperties.Name.SESSION_CLIENT_ID);
        set.add(LogProperties.Name.SESSION_SESSION);
        LOG_PROPERTIES = Collections.unmodifiableSet(set);
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

    public static final String ACTION_FORMLOGIN = "formlogin";
    public static final String ACTION_TOKENLOGIN = "tokenLogin";
    public static final String ACTION_TOKENS = "tokens";
    public static final String ACTION_REDEEM_TOKEN = "redeemToken";

    /**
     * <code>"changeip"</code>
     */
    public static final String ACTION_CHANGEIP = "changeip".intern();

    private static enum CookieType {
        SESSION, SECRET;
    }

    final AtomicReference<LoginConfiguration> confReference;
    private final Map<String, LoginRequestHandler> handlerMap;

    public Login() {
        super();
        confReference = new AtomicReference<LoginConfiguration>();
        handlerMap = new ConcurrentHashMap<String, LoginRequestHandler>(16);
        handlerMap.put(ACTION_LOGIN, new LoginRequestHandler() {

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
        handlerMap.put(ACTION_OAUTH, new LoginRequestHandler() {

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
        handlerMap.put(ACTION_STORE, new LoginRequestHandler() {

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
        handlerMap.put(ACTION_REFRESH_SECRET, new LoginRequestHandler() {

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
        handlerMap.put(ACTION_LOGOUT, new LoginRequestHandler() {

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
                        SessionServlet.checkIP(conf.isIpCheck(), conf.getRanges(), session, req.getRemoteAddr(), conf.getIpCheckWhitelist());
                        final String secret = SessionServlet.extractSecret(conf.getHashSource(), req, session.getHash(), session.getClient());

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
        handlerMap.put(ACTION_REDIRECT, new LoginRequestHandler() {

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
                if (conf.isInsecure()) {
                    if (conf.isRedirectIPChangeAllowed()) {
                        session = sessiondService.getSessionByRandomToken(randomToken, req.getRemoteAddr());
                    } else {
                        session = sessiondService.getSessionByRandomToken(randomToken);
                        if (null != session) {
                            final String oldIP = session.getLocalIp();
                            if (null == oldIP || SessionServlet.isWhitelistedFromIPCheck(oldIP, conf.getRanges())) {
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
                    } else if (Login.LOG.isInfoEnabled()) {
                        LOG.info("No session could be found for random token: " + randomToken);
                    }
                    resp.sendError(HttpServletResponse.SC_FORBIDDEN);
                    return;
                }
                // Remove old cookies to prevent usage of the old autologin cookie
                if (conf.isInsecure()) {
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
                    LOG.info("Status code 403 (FORBIDDEN): Couldn't resolve context/user by identifier: " + session.getContextId() + '/' + session.getUserId());
                    resp.sendError(HttpServletResponse.SC_FORBIDDEN);
                    return;
                }

                String client = req.getParameter(LoginFields.CLIENT_PARAM);
                final String hash;
                if (!conf.isInsecure()) {
                    hash = session.getHash();
                } else {
                    if (null == client) {
                        client = session.getClient();
                    } else {
                        session.setClient(client);
                    }
                    hash = HashCalculator.getInstance().getHash(req, client);
                    session.setHash(hash);
                }
                writeSecretCookie(resp, session, hash, req.isSecure(), req.getServerName(), conf);
                resp.sendRedirect(LoginTools.generateRedirectURL(
                    req.getParameter(LoginFields.UI_WEB_PATH_PARAM),
                    req.getParameter("store"),
                    session.getSessionID(), conf.getUiWebPath()));
            }
        });
        handlerMap.put(ACTION_CHANGEIP, new LoginRequestHandler() {

            @Override
            public void handleRequest(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
                final Response response = new Response();
                Session session = null;
                try {
                    final String sessionId = req.getParameter(PARAMETER_SESSION);
                    if (null == sessionId) {
                        if (Login.LOG.isInfoEnabled()) {
                            final StringBuilder sb = new StringBuilder(32);
                            sb.append("Parameter \"").append(PARAMETER_SESSION).append("\" not found for action ").append(ACTION_CHANGEIP);
                            LOG.info(sb.toString());
                        }
                        throw AjaxExceptionCodes.MISSING_PARAMETER.create(PARAMETER_SESSION);
                    }
                    final String newIP = req.getParameter(LoginFields.CLIENT_IP_PARAM);
                    if (null == newIP) {
                        if (Login.LOG.isInfoEnabled()) {
                            final StringBuilder sb = new StringBuilder(32);
                            sb.append("Parameter \"").append(LoginFields.CLIENT_IP_PARAM).append("\" not found for action ").append(
                                ACTION_CHANGEIP);
                            LOG.info(sb.toString());
                        }
                        throw AjaxExceptionCodes.MISSING_PARAMETER.create(LoginFields.CLIENT_IP_PARAM);
                    }
                    final SessiondService sessiondService = ServerServiceRegistry.getInstance().getService(SessiondService.class, true);
                    session = sessiondService.getSession(sessionId);
                    final LoginConfiguration conf = confReference.get();
                    if (session != null) {
                        SessionServlet.checkIP(conf.isIpCheck(), conf.getRanges(), session, req.getRemoteAddr(), conf.getIpCheckWhitelist());
                        final String secret = SessionServlet.extractSecret(conf.getHashSource(), req, session.getHash(), session.getClient());
                        if (secret == null || !session.getSecret().equals(secret)) {
                            if (Login.LOG.isInfoEnabled() && null != secret) {
                                LOG.info("Session secret is different. Given secret \"" + secret + "\" differs from secret in session \"" + session.getSecret() + "\".");
                            }
                            throw SessionExceptionCodes.WRONG_SESSION_SECRET.create();
                        }
                        final String oldIP = session.getLocalIp();
                        if (!newIP.equals(oldIP)) {
                            // In case changing IP is intentionally requested by client, log it only if DEBUG aka FINE log level is enabled
                            if (LOG.isDebugEnabled()) {
                                LOG.info("Changing IP of session " + session.getSessionID() + " with authID: " + session.getAuthId() + " from " + oldIP + " to " + newIP + '.');
                            }
                            session.setLocalIp(newIP);
                        }
                        response.setData("1");
                    } else {
                        if (Login.LOG.isInfoEnabled()) {
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
                    ResponseWriter.write(response, resp.getWriter(), localeFrom(session));
                } catch (final JSONException e) {
                    log(RESPONSE_ERROR, e);
                    sendError(resp);
                }
            }
        });
        handlerMap.put(ACTION_REDEEM, new LoginRequestHandler() {

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
                if (conf.isInsecure()) {
                    if (conf.isRedirectIPChangeAllowed()) {
                        session = sessiondService.getSessionByRandomToken(randomToken, req.getRemoteAddr());
                    } else {
                        session = sessiondService.getSessionByRandomToken(randomToken);
                        if (null != session) {
                            final String oldIP = session.getLocalIp();
                            if (null == oldIP || SessionServlet.isWhitelistedFromIPCheck(oldIP, conf.getRanges())) {
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
                    } else if (Login.LOG.isInfoEnabled()) {
                        LOG.info("No session could be found for random token: " + randomToken);
                    }
                    resp.sendError(HttpServletResponse.SC_FORBIDDEN);
                    return;
                }
                // Remove old cookies to prevent usage of the old autologin cookie
                if (conf.isInsecure()) {
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
                    LOG.info("Status code 403 (FORBIDDEN): Couldn't resolve context/user by identifier: " + session.getContextId() + '/' + session.getUserId());
                    resp.sendError(HttpServletResponse.SC_FORBIDDEN);
                    return;
                }

                String client = req.getParameter(LoginFields.CLIENT_PARAM);
                final String hash;
                if (!conf.isInsecure()) {
                    hash = session.getHash();
                } else {
                    if (null == client) {
                        client = session.getClient();
                    } else {
                        session.setClient(client);
                    }
                    hash = HashCalculator.getInstance().getHash(req, client);
                    session.setHash(hash);
                }
                writeSecretCookie(resp, session, hash, req.isSecure(), req.getServerName(), conf);

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
        handlerMap.put(ACTION_AUTOLOGIN, new LoginRequestHandler() {

            @Override
            public void handleRequest(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
                Tools.disableCaching(resp);
                resp.setContentType(CONTENTTYPE_JAVASCRIPT);
                final Response response = new Response();
                Session session = null;
                try {
                    final LoginConfiguration conf = confReference.get();
                    if (!conf.isSessiondAutoLogin()) {
                        if (doAutoLogin(req, resp)) {
                            throw AjaxExceptionCodes.DISABLED_ACTION.create("autologin");
                        }
                        return;
                    }

                    Cookie[] cookies = req.getCookies();
                    if (cookies == null) {
                        cookies = new Cookie[0];
                    }

                    final SessiondService sessiondService = ServerServiceRegistry.getInstance().getService(SessiondService.class);
                    if (null == sessiondService) {
                        final OXException se = ServiceExceptionCode.SERVICE_UNAVAILABLE.create(SessiondService.class.getName());
                        LOG.error(se.getMessage(), se);
                        resp.sendError(HttpServletResponse.SC_FORBIDDEN);
                        return;
                    }
                    String secret = null;
                    final String hash = HashCalculator.getInstance().getHash(req);
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
                                if (!conf.isIpCheck()) {
                                    // Update IP address if necessary
                                    updateIPAddress(conf, req.getRemoteAddr(), session);
                                } else {
                                    final String newIP = req.getRemoteAddr();
                                    SessionServlet.checkIP(true, conf.getRanges(), session, newIP, conf.getIpCheckWhitelist());
                                    // IP check passed: update IP address if necessary
                                    updateIPAddress(conf, newIP, session);
                                }
                                try {
                                    final Context ctx = ContextStorage.getInstance().getContext(session.getContextId());
                                    if (!ctx.isEnabled()) {
                                        throw LoginExceptionCodes.INVALID_CREDENTIALS.create();
                                    }
                                    final User user = UserStorage.getInstance().getUser(session.getUserId(), ctx);
                                    if (!user.isMailEnabled()) {
                                        throw LoginExceptionCodes.INVALID_CREDENTIALS.create();
                                    }
                                } catch (final UndeclaredThrowableException e) {
                                    throw LoginExceptionCodes.UNKNOWN.create(e, e.getMessage());
                                }

                                // Request modules
                                final Future<Object> optModules = getModulesAsync(session, req);

                                // Create JSON object
                                final JSONObject json = new JSONObject(8);
                                LoginWriter.write(session, json);

                                if (null != optModules) {
                                    // Append "config/modules"
                                    try {
                                        final Object oModules = optModules.get();
                                        if (null != oModules) {
                                            json.put("modules", oModules);
                                        }
                                    } catch (final InterruptedException e) {
                                        // Keep interrupted state
                                        Thread.currentThread().interrupt();
                                        throw LoginExceptionCodes.UNKNOWN.create(e, "Thread interrupted.");
                                    } catch (final ExecutionException e) {
                                        // Cannot occur
                                        final Throwable cause = e.getCause();
                                        LOG.warn("Modules could not be added to login JSON response: " + cause.getMessage(), cause);
                                    }
                                }

                                // Set data
                                response.setData(json);

                                // Secret already found?
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

                    /*-
                     * Ensure appropriate public-session-cookie is set
                     */
                    writePublicSessionCookie(resp, session, req.isSecure(), req.getServerName(), conf);

                } catch (final OXException e) {
                    if (AjaxExceptionCodes.DISABLED_ACTION.equals(e)) {
                        LOG.debug(e.getMessage(), e);
                    } else {
                        e.log(LOG);
                    }
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
                        ResponseWriter.write(response, resp.getWriter(), localeFrom(session));
                    } else {
                        ((JSONObject) response.getData()).write(resp.getWriter());
                    }
                } catch (final JSONException e) {
                    log(RESPONSE_ERROR, e);
                    sendError(resp);
                }
            }
        });
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
        final List<IPRange> ranges = new LinkedList<IPRange>();
        final String tmp = config.getInitParameter(ConfigurationProperty.NO_IP_CHECK_RANGE.getPropertyName());
        if (tmp != null) {
            final String[] lines = Strings.splitByCRLF(tmp);
            for (String line : lines) {
                line = line.replaceAll("\\s", "");
                if (!line.equals("") && (line.length() == 0 || line.charAt(0) != '#')) {
                    ranges.add(IPRange.parseRange(line));
                }
            }
        }
        final boolean disableTrimLogin = Boolean.parseBoolean(config.getInitParameter(ConfigurationProperty.DISABLE_TRIM_LOGIN.getPropertyName()));
        final boolean formLoginWithoutAuthId = Boolean.parseBoolean(config.getInitParameter(ConfigurationProperty.FORM_LOGIN_WITHOUT_AUTHID.getPropertyName()));
        LoginConfiguration conf = new LoginConfiguration(
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
            ranges,
            disableTrimLogin,
            formLoginWithoutAuthId);
        confReference.set(conf);
        handlerMap.put(ACTION_FORMLOGIN, new FormLogin(conf));
        handlerMap.put(ACTION_TOKENLOGIN, new TokenLogin(conf));
        handlerMap.put(ACTION_TOKENS,  new Tokens(conf));
        handlerMap.put(ACTION_REDEEM_TOKEN, new RedeemToken(conf));

    }

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        try {
            final String action = req.getParameter(PARAMETER_ACTION);
            final String subPath = getServletSpecificURI(req);
            if (null != subPath && subPath.startsWith("/httpAuth")) {
                doHttpAuth(req, resp);
            } else if (null != action) {
                doJSONAuth(req, resp, action);
            } else {
                logAndSendException(resp, AjaxExceptionCodes.MISSING_PARAMETER.create(PARAMETER_ACTION));
                return;
            }
        } finally {
            LogProperties.removeLogProperties(LOG_PROPERTIES);
        }
    }

    private void doJSONAuth(final HttpServletRequest req, final HttpServletResponse resp, final String action) throws IOException {
        final LoginRequestHandler handler = handlerMap.get(action);
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
     * Writes or rewrites a cookie
     */
    private void doCookieReWrite(final HttpServletRequest req, final HttpServletResponse resp, final CookieType type) throws OXException, JSONException, IOException {
        final LoginConfiguration conf = confReference.get();
        if (!conf.isSessiondAutoLogin() && CookieType.SESSION == type) {
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
        final Session session = SessionServlet.getSession(conf.getHashSource(), req, sessionId, sessiond);
        try {
            SessionServlet.checkIP(conf.isIpCheck(), conf.getRanges(), session, req.getRemoteAddr(), conf.getIpCheckWhitelist());
            if (type == CookieType.SESSION) {
                writeSessionCookie(resp, session, session.getHash(), req.isSecure(), req.getServerName());
            } else {
                writeSecretCookie(resp, session, session.getHash(), req.isSecure(), req.getServerName(), conf);
            }
            // Refresh HTTP session, too
            req.getSession();
            final Response response = new Response();
            response.setData("1");
            ResponseWriter.write(response, resp.getWriter(), localeFrom(session));
        } finally {
            LogProperties.removeSessionProperties();
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

    public static void logAndSendException(final HttpServletResponse resp, final OXException e) throws IOException {
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
        writeSecretCookie(resp, session, hash, secure, null, confReference.get());
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
    public static void writeSecretCookie(HttpServletResponse resp, Session session, String hash, boolean secure, String serverName, LoginConfiguration conf) {
        Cookie cookie = new Cookie(SECRET_PREFIX + hash, session.getSecret());
        configureCookie(cookie, secure, serverName, conf);
        resp.addCookie(cookie);

        final String altId = (String) session.getParameter(Session.PARAM_ALTERNATIVE_ID);
        if (null != altId) {
            cookie = new Cookie(PUBLIC_SESSION_NAME, altId);
            configureCookie(cookie, secure, serverName, conf);
            resp.addCookie(cookie);
        }
    }

    /**
     * Writes the (groupware's) public session cookie <code>"open-xchange-public-session"</code> to specified HTTP servlet response.
     *
     * @param resp The HTTP servlet response
     * @param session The session providing the public session cookie identifier
     * @param secure <code>true</code> to set cookie's secure flag; otherwise <code>false</code>
     * @param serverName The HTTP request's server name
     */
    public static void writePublicSessionCookie(final HttpServletResponse resp, final Session session, final boolean secure, final String serverName, final LoginConfiguration conf) {
        final String altId = (String) session.getParameter(Session.PARAM_ALTERNATIVE_ID);
        if (null != altId) {
            final Cookie cookie = new Cookie(PUBLIC_SESSION_NAME, altId);
            configureCookie(cookie, secure, serverName, conf);
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
        configureCookie(cookie, secure, serverName, confReference.get());
        resp.addCookie(cookie);
    }

    private static void configureCookie(final Cookie cookie, final boolean secure, final String serverName, LoginConfiguration conf) {
        cookie.setPath("/");
        if (secure || (conf.isCookieForceHTTPS() && !Cookies.isLocalLan(serverName))) {
            cookie.setSecure(true);
        }
        if (conf.isSessiondAutoLogin() || conf.getCookieExpiry() < 0) {
            /*
             * A negative value means that the cookie is not stored persistently and will be deleted when the Web browser exits. A zero
             * value causes the cookie to be deleted.
             */
            cookie.setMaxAge(conf.getCookieExpiry());
        }
        final String domain = getDomainValue(null == serverName ? LogProperties.<String> getLogProperty(LogProperties.Name.AJP_SERVER_NAME) : serverName);
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
                LoginConfiguration conf = confReference.get();
                final LoginRequest request = LoginTools.parseLogin(
                    req2,
                    LoginFields.NAME_PARAM,
                    false,
                    conf.getDefaultClient(),
                    conf.isCookieForceHTTPS(),
                    conf.isDisableTrimLogin(),
                    false);
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
                    LoginConfiguration conf = confReference.get();
                    final LoginRequest request = LoginTools.parseLogin(req2, login, password, false, conf.getDefaultClient(), conf.isCookieForceHTTPS(), false);
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
                final com.openexchange.java.StringAllocator realm = new com.openexchange.java.StringAllocator(32).append((request.isSecure()) ? "https://" : "http://");
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
            // Do the login...
            result = login.doLogin(req);
            if (null == result) {
                return true;
            }

            // The associated session
            final Session session = result.getSession();

            // Add session log properties
            LogProperties.putSessionProperties(session);

            // Request modules
            final Future<Object> optModules = getModulesAsync(session, req);

            // Add headers and cookies from login result
            addHeadersAndCookies(result, resp);

            // Check result code
            {
                final ResultCode code = result.getCode();
                if (null != code) {
                    switch (code) {
                    case FAILED:
                        return true;
                    case REDIRECT:
                        throw LoginExceptionCodes.REDIRECT.create(result.getRedirect());
                    default:
                        break;
                    }
                }
            }

            // Remember User-Agent
            session.setParameter("user-agent", req.getHeader("user-agent"));

            // Write response
            final JSONObject json = new JSONObject(8);
            LoginWriter.write(result, json);

            // Handle initial multiple
            final String multipleRequest = req.getParameter("multiple");
            if (multipleRequest != null) {
            	final JSONArray dataArray = new JSONArray(multipleRequest);
            	if (dataArray.length() > 0) {
            	    JSONArray responses = Multiple.perform(dataArray, req, ServerSessionAdapter.valueOf(session));
                    json.put("multiple", responses);
                } else {
                    json.put("multiple", new JSONArray(0));
                }
            }

            if (null != optModules) {
                // Append "config/modules"
                try {
                    final Object oModules = optModules.get();
                    if (null != oModules) {
                        json.put("modules", oModules);
                    }
                } catch (final InterruptedException e) {
                    // Keep interrupted state
                    Thread.currentThread().interrupt();
                    throw LoginExceptionCodes.UNKNOWN.create(e, "Thread interrupted.");
                } catch (final ExecutionException e) {
                    // Cannot occur
                    final Throwable cause = e.getCause();
                    LOG.warn("Modules could not be added to login JSON response: " + cause.getMessage(), cause);
                }
            }

            // Set response
            response.setData(json);
        } catch (final OXException e) {
            if (AjaxExceptionCodes.PREFIX.equals(e.getPrefix())) {
                throw e;
            }
            if (LoginExceptionCodes.NOT_SUPPORTED.equals(e)) {
                // Rethrow according to previous behavior
                LOG.debug(e.getMessage(), e);
                throw AjaxExceptionCodes.DISABLED_ACTION.create("autologin");
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
                final Locale locale;
                {
                    final String sLocale = req.getParameter("language");
                    if (null == sLocale) {
                        locale = bestGuessLocale(result, req);
                    } else {
                        final Locale loc = LocaleTools.getLocale(sLocale);
                        locale = null == loc ? bestGuessLocale(result, req) : loc;
                    }
                }
                ResponseWriter.write(response, resp.getWriter(), locale);
                return false;
            }
            final Session session = result.getSession();
            // Store associated session
            SessionServlet.rememberSession(req, new ServerSessionAdapter(session, result.getContext(), result.getUser()));
            writeSecretCookie(resp, session, session.getHash(), req.isSecure(), req.getServerName(), confReference.get());
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

    private static Locale bestGuessLocale(LoginResult result, final HttpServletRequest req) {
        final Locale locale;
        if (null == result) {
            locale = Tools.getLocaleByAcceptLanguage(req, null);
        } else {
            final User user = result.getUser();
            if (null == user) {
                locale = Tools.getLocaleByAcceptLanguage(req, null);
            } else {
                locale = user.getLocale();
            }
        }
        return locale;
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

    protected LoginRequest parseAutoLoginRequest(final HttpServletRequest req) throws OXException {
        final LoginConfiguration conf = confReference.get();
        final String authId = LoginTools.parseAuthId(req, false);
        final String client = LoginTools.parseClient(req, false, conf.getDefaultClient());
        final String clientIP = LoginTools.parseClientIP(req);
        final String userAgent = LoginTools.parseUserAgent(req);
        final Map<String, List<String>> headers = copyHeaders(req);
        final com.openexchange.authentication.Cookie[] cookies = Tools.getCookieFromHeader(req);
        final String httpSessionId = req.getSession(true).getId();
        return new LoginRequestImpl(
            null,
            null,
            clientIP,
            userAgent,
            authId,
            client,
            null,
            HashCalculator.getInstance().getHash(req, client),
            HTTP_JSON,
            headers,
            cookies,
            Tools.considerSecure(req, conf.isCookieForceHTTPS()),
            req.getServerName(),
            req.getServerPort(),
            httpSessionId);
    }

    private String parseClient(final HttpServletRequest req) {
        try {
            return LoginTools.parseClient(req, false, confReference.get().getDefaultClient());
        } catch (final OXException e) {
            return confReference.get().getDefaultClient();
        }
    }

    /**
     * Appends the modules to given JSON object.
     *
     * @param session The associated session
     * @param json The JSON object to append to
     * @param req The request
     */
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
            } catch (final Exception e) {
                LOG.warn("Modules could not be added to login JSON response: " + e.getMessage(), e);
            }
        }
    }

    /**
     * Asynchronously retrieves modules.
     *
     * @param session The associated session
     * @param req The request
     * @return The resulting object or <code>null</code>
     */
    protected static Future<Object> getModulesAsync(final Session session, final HttpServletRequest req) {
        final String modules = "modules";
        if (!parseBoolean(req.getParameter(modules))) {
            return null;
        }
        // Submit task
        return ThreadPools.getThreadPool().submit(new AbstractTask<Object>() {

            @Override
            public Object call() throws Exception {
                try {
                    final Setting setting = ConfigTree.getInstance().getSettingByPath(modules);
                    SettingStorage.getInstance(session).readValues(setting);
                    return convert2JS(setting);
                } catch (final OXException e) {
                    LOG.warn("Modules could not be added to login JSON response: " + e.getMessage(), e);
                } catch (final JSONException e) {
                    LOG.warn("Modules could not be added to login JSON response: " + e.getMessage(), e);
                } catch (final Exception e) {
                    LOG.warn("Modules could not be added to login JSON response: " + e.getMessage(), e);
                }
                return null;
            }
        });
    }

    /**
     * Parses the specified parameter to a <code>boolean</code> value.
     *
     * @param parameter The parameter value
     * @return <code>true</code> if parameter is <b>not</b> <code>null</code> and is (ignore-case) one of the values <code>"true"</code>,
     *         <code>"1"</code>, <code>"yes"</code> or <code>"on"</code>; otherwise <code>false</code>
     */
    private static boolean parseBoolean(final String parameter) {
        return "true".equalsIgnoreCase(parameter) || "1".equals(parameter) || "yes".equalsIgnoreCase(parameter) || "y".equalsIgnoreCase(parameter) || "on".equalsIgnoreCase(parameter);
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
            version = conf.getClientVersion();
        } else if (Authorization.checkForKerberosAuthorization(auth)) {
            creds = new Credentials("kerberos", "");
            version = "Kerberos";
        } else {
            throw LoginExceptionCodes.UNKNOWN_HTTP_AUTHORIZATION.create("");
        }
        final String client = parseClient(req);
        final String clientIP = LoginTools.parseClientIP(req);
        final String userAgent = LoginTools.parseUserAgent(req);
        final Map<String, List<String>> headers = copyHeaders(req);
        final com.openexchange.authentication.Cookie[] cookies = Tools.getCookieFromHeader(req);
        final String httpSessionId = req.getSession(true).getId();
        final LoginRequest request = new LoginRequestImpl(
            creds.getLogin(),
            creds.getPassword(),
            clientIP,
            userAgent,
            UUIDs.getUnformattedString(UUID.randomUUID()),
            client,
            version,
            HashCalculator.getInstance().getHash(req, userAgent, client),
            Interface.HTTP_JSON,
            headers,
            cookies,
            Tools.considerSecure(req, conf.isCookieForceHTTPS()),
            req.getServerName(),
            req.getServerPort(),
            httpSessionId);
        final Map<String, Object> properties = new HashMap<String, Object>(1);
        {
            final String capabilities = req.getParameter("capabilities");
            if (null != capabilities) {
                properties.put("client.capabilities", capabilities);
            }
        }
        final LoginResult result = LoginPerformer.getInstance().doLogin(request, properties);
        final Session session = result.getSession();
        Tools.disableCaching(resp);
        writeSecretCookie(resp, session, session.getHash(), req.isSecure(), req.getServerName(), conf);
        addHeadersAndCookies(result, resp);
        resp.sendRedirect(LoginTools.generateRedirectURL(null, conf.getHttpAuthAutoLogin(), session.getSessionID(), conf.getUiWebPath()));
    }

    private static final String ERROR_PAGE_TEMPLATE = "<html>\n" + "<script type=\"text/javascript\">\n" + "// Display normal HTML for 5 seconds, then redirect via referrer.\n" + "setTimeout(redirect,5000);\n" + "function redirect(){\n" + " var referrer=document.referrer;\n" + " var redirect_url;\n" + " // If referrer already contains failed parameter, we don't add a 2nd one.\n" + " if(referrer.indexOf(\"login=failed\")>=0){\n" + "  redirect_url=referrer;\n" + " }else{\n" + "  // Check if referrer contains multiple parameter\n" + "  if(referrer.indexOf(\"?\")<0){\n" + "   redirect_url=referrer+\"?login=failed\";\n" + "  }else{\n" + "   redirect_url=referrer+\"&login=failed\";\n" + "  }\n" + " }\n" + " // Redirect to referrer\n" + " window.location.href=redirect_url;\n" + "}\n" + "</script>\n" + "<body>\n" + "<h1>ERROR_MESSAGE</h1>\n" + "</body>\n" + "</html>\n";
}
