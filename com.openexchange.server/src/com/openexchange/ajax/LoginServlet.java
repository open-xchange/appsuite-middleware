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
import static com.openexchange.tools.servlet.http.Cookies.getDomainValue;
import static com.openexchange.tools.servlet.http.Tools.copyHeaders;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.fields.Header;
import com.openexchange.ajax.fields.LoginFields;
import com.openexchange.ajax.helper.Send;
import com.openexchange.ajax.login.AutoLogin;
import com.openexchange.ajax.login.Login;
import com.openexchange.ajax.login.FormLogin;
import com.openexchange.ajax.login.HashCalculator;
import com.openexchange.ajax.login.LoginConfiguration;
import com.openexchange.ajax.login.LoginRequestHandler;
import com.openexchange.ajax.login.LoginRequestImpl;
import com.openexchange.ajax.login.LoginTools;
import com.openexchange.ajax.login.OAuthLogin;
import com.openexchange.ajax.login.RedeemToken;
import com.openexchange.ajax.login.TokenLogin;
import com.openexchange.ajax.login.Tokens;
import com.openexchange.ajax.writer.LoginWriter;
import com.openexchange.ajax.writer.ResponseWriter;
import com.openexchange.authentication.LoginExceptionCodes;
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
import com.openexchange.java.Strings;
import com.openexchange.java.util.UUIDs;
import com.openexchange.log.LogFactory;
import com.openexchange.log.LogProperties;
import com.openexchange.login.ConfigurationProperty;
import com.openexchange.login.Interface;
import com.openexchange.login.LoginRequest;
import com.openexchange.login.LoginResult;
import com.openexchange.login.internal.LoginPerformer;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.sessiond.SessionExceptionCodes;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.sessiond.impl.IPRange;
import com.openexchange.tools.io.IOTools;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.servlet.http.Authorization;
import com.openexchange.tools.servlet.http.Authorization.Credentials;
import com.openexchange.tools.servlet.http.Cookies;
import com.openexchange.tools.servlet.http.Tools;

/**
 * Servlet doing the login and logout stuff.
 * 
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class LoginServlet extends AJAXServlet {

    private static final long serialVersionUID = 7680745138705836499L;

    protected static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(LoginServlet.class));

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

    public LoginServlet() {
        super();
        confReference = new AtomicReference<LoginConfiguration>();
        handlerMap = new ConcurrentHashMap<String, LoginRequestHandler>(16);
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
                        final String secret = SessionServlet.extractSecret(
                            conf.getHashSource(),
                            req,
                            session.getHash(),
                            session.getClient());

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
                final LoginConfiguration conf = confReference.get();
                // The magic spell to disable caching
                Tools.disableCaching(resp);
                resp.setContentType(CONTENTTYPE_JAVASCRIPT);
                String randomToken = null;
                if(conf.isRandomTokenEnabled()) {
                    randomToken = req.getParameter(LoginFields.RANDOM_PARAM);
                }
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
                    } else if (LoginServlet.LOG.isInfoEnabled()) {
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
                    session.getSessionID(),
                    conf.getUiWebPath()));
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
                        if (LoginServlet.LOG.isInfoEnabled()) {
                            final StringBuilder sb = new StringBuilder(32);
                            sb.append("Parameter \"").append(PARAMETER_SESSION).append("\" not found for action ").append(ACTION_CHANGEIP);
                            LOG.info(sb.toString());
                        }
                        throw AjaxExceptionCodes.MISSING_PARAMETER.create(PARAMETER_SESSION);
                    }
                    final String newIP = req.getParameter(LoginFields.CLIENT_IP_PARAM);
                    if (null == newIP) {
                        if (LoginServlet.LOG.isInfoEnabled()) {
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
                        final String secret = SessionServlet.extractSecret(
                            conf.getHashSource(),
                            req,
                            session.getHash(),
                            session.getClient());
                        if (secret == null || !session.getSecret().equals(secret)) {
                            if (LoginServlet.LOG.isInfoEnabled() && null != secret) {
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
                        if (LoginServlet.LOG.isInfoEnabled()) {
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
                final LoginConfiguration conf = confReference.get();
                // The magic spell to disable caching
                Tools.disableCaching(resp);
                resp.setContentType(CONTENTTYPE_JAVASCRIPT);
                String randomToken = null;
                if(conf.isRandomTokenEnabled()) {
                    randomToken = req.getParameter(LoginFields.RANDOM_PARAM);
                }
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
                    } else if (LoginServlet.LOG.isInfoEnabled()) {
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
        final boolean isRandomTokenEnabled = Boolean.parseBoolean(config.getInitParameter(ConfigurationProperty.RANDOM_TOKEN.getPropertyName()));
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
            formLoginWithoutAuthId,
            isRandomTokenEnabled);
        confReference.set(conf);
        handlerMap.put(ACTION_FORMLOGIN, new FormLogin(conf));
        handlerMap.put(ACTION_TOKENLOGIN, new TokenLogin(conf));
        handlerMap.put(ACTION_TOKENS, new Tokens(conf));
        handlerMap.put(ACTION_REDEEM_TOKEN, new RedeemToken(conf));
        handlerMap.put(ACTION_AUTOLOGIN, new AutoLogin(conf));
        handlerMap.put(ACTION_OAUTH, new OAuthLogin(conf));
        handlerMap.put(ACTION_LOGIN, new Login(conf));
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

    public static void addHeadersAndCookies(final LoginResult result, final HttpServletResponse resp) {
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
     * Parses the specified parameter to a <code>boolean</code> value.
     * 
     * @param parameter The parameter value
     * @return <code>true</code> if parameter is <b>not</b> <code>null</code> and is (ignore-case) one of the values <code>"true"</code>,
     *         <code>"1"</code>, <code>"yes"</code> or <code>"on"</code>; otherwise <code>false</code>
     */
    public static boolean parseBoolean(final String parameter) {
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
        Cookie cookie = new Cookie(LoginServlet.SECRET_PREFIX + hash, session.getSecret());
        configureCookie(cookie, secure, serverName, conf);
        resp.addCookie(cookie);

        final String altId = (String) session.getParameter(Session.PARAM_ALTERNATIVE_ID);
        if (null != altId) {
            cookie = new Cookie(LoginServlet.PUBLIC_SESSION_NAME, altId);
            configureCookie(cookie, secure, serverName, conf);
            resp.addCookie(cookie);
        }
    }

    public static void configureCookie(final Cookie cookie, final boolean secure, final String serverName, LoginConfiguration conf) {
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

    private static final String ERROR_PAGE_TEMPLATE = "<html>\n" + "<script type=\"text/javascript\">\n" + "// Display normal HTML for 5 seconds, then redirect via referrer.\n" + "setTimeout(redirect,5000);\n" + "function redirect(){\n" + " var referrer=document.referrer;\n" + " var redirect_url;\n" + " // If referrer already contains failed parameter, we don't add a 2nd one.\n" + " if(referrer.indexOf(\"login=failed\")>=0){\n" + "  redirect_url=referrer;\n" + " }else{\n" + "  // Check if referrer contains multiple parameter\n" + "  if(referrer.indexOf(\"?\")<0){\n" + "   redirect_url=referrer+\"?login=failed\";\n" + "  }else{\n" + "   redirect_url=referrer+\"&login=failed\";\n" + "  }\n" + " }\n" + " // Redirect to referrer\n" + " window.location.href=redirect_url;\n" + "}\n" + "</script>\n" + "<body>\n" + "<h1>ERROR_MESSAGE</h1>\n" + "</body>\n" + "</html>\n";
}
