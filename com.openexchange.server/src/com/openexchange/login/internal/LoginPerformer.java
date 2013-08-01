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

package com.openexchange.login.internal;

import static com.openexchange.java.Autoboxing.I;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;
import javax.security.auth.login.LoginException;
import org.apache.commons.logging.Log;
import com.openexchange.ajax.fields.LoginFields;
import com.openexchange.authentication.Authenticated;
import com.openexchange.authentication.Cookie;
import com.openexchange.authentication.LoginExceptionCodes;
import com.openexchange.authentication.ResponseEnhancement;
import com.openexchange.authentication.ResultCode;
import com.openexchange.authentication.SessionEnhancement;
import com.openexchange.authorization.Authorization;
import com.openexchange.authorization.AuthorizationService;
import com.openexchange.database.DBPoolingExceptionCodes;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextExceptionCodes;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.groupware.userconfiguration.UserConfigurationStorage;
import com.openexchange.java.Strings;
import com.openexchange.log.LogFactory;
import com.openexchange.login.Blocking;
import com.openexchange.login.LoginHandlerService;
import com.openexchange.login.LoginRequest;
import com.openexchange.login.LoginResult;
import com.openexchange.login.NonTransient;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.threadpool.ThreadPoolCompletionService;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.threadpool.ThreadPools;
import com.openexchange.threadpool.behavior.CallerRunsBehavior;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;

/**
 * {@link LoginPerformer} - Performs a login for specified credentials.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class LoginPerformer {

    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(LoginPerformer.class));

    private static final LoginPerformer SINGLETON = new LoginPerformer();

    /**
     * Initializes a new {@link LoginPerformer}.
     */
    private LoginPerformer() {
        super();
    }

    /**
     * Gets the {@link LoginPerformer} instance.
     *
     * @return The instance
     */
    public static LoginPerformer getInstance() {
        return SINGLETON;
    }

    /**
     * Performs the login for specified login request.
     *
     * @param request The login request
     * @return The login providing login information
     * @throws LoginException If login fails
     */
    public LoginResult doLogin(final LoginRequest request) throws OXException {
        return doLogin(request, new HashMap<String, Object>(1));
    }

    public LoginResult doLogin(final LoginRequest request, final Map<String, Object> properties) throws OXException {
        return doLogin(request, properties, new NormalLoginMethod(request, properties));
    }

    /**
     * Performs the login for specified login request.
     *
     * @param request The login request
     * @return The login providing login information
     * @throws OXException If login fails
     */
    public LoginResult doAutoLogin(final LoginRequest request) throws OXException {
        return doLogin(request, new HashMap<String, Object>(1), new AutoLoginMethod(request, new HashMap<String, Object>(1)));
    }

    private static final Pattern SPLIT = Pattern.compile(" *, *");

    /**
     * Performs the login for specified login request.
     *
     * @param request The login request
     * @return The login providing login information
     * @throws OXException If login fails
     */
    private LoginResult doLogin(final LoginRequest request, final Map<String, Object> properties, final LoginMethodClosure loginMethod) throws OXException {
        final LoginResultImpl retval = new LoginResultImpl();
        retval.setRequest(request);
        try {
            final Map<String, List<String>> headers = request.getHeaders();
            if (headers != null) {
                properties.put("headers", headers);
            }
            final Cookie[] cookies = request.getCookies();
            if (null != cookies) {
                properties.put("cookies", cookies);
            }
            final Authenticated authed = loginMethod.doAuthentication(retval);
            if (null == authed) {
                return null;
            }
            if (authed instanceof ResponseEnhancement) {
                final ResponseEnhancement responseEnhancement = (ResponseEnhancement) authed;
                retval.setHeaders(responseEnhancement.getHeaders());
                retval.setCookies(responseEnhancement.getCookies());
                retval.setRedirect(responseEnhancement.getRedirect());
                final ResultCode code = responseEnhancement.getCode();
                retval.setCode(code);
                if (ResultCode.REDIRECT.equals(code) || ResultCode.FAILED.equals(code)) {
                    return retval;
                }
            }
            final Context ctx = findContext(authed.getContextInfo());
            retval.setContext(ctx);
            final String username = authed.getUserInfo();
            final User user = findUser(ctx, username);
            retval.setUser(user);
            // Checks if something is deactivated.
            final AuthorizationService authService = Authorization.getService();
            if (null == authService) {
                final OXException e = ServiceExceptionCode.SERVICE_UNAVAILABLE.create(AuthorizationService.class.getName());
                LOG.error("unable to find AuthorizationService", e);
                throw e;
            }
            authService.authorizeUser(ctx, user);
            // Check if indicated client is allowed to perform a login
            checkClient(request, user, ctx);
            // Create session
            final SessiondService sessiondService = SessiondService.SERVICE_REFERENCE.get();
            final Session session = sessiondService.addSession(new AddSessionParameterImpl(username, request, user, ctx));
            if (null == session) {
                // Session could not be created
                throw LoginExceptionCodes.UNKNOWN.create("Session could not be created.");
            }
            // Initial parameters
            {
                final String capabilities = (String) properties.get("client.capabilities");
                if (null == capabilities) {
                    session.setParameter(Session.PARAM_CAPABILITIES, Collections.<String> emptyList());
                    // retval.addWarning(LoginExceptionCodes.MISSING_CAPABILITIES.create());
                } else {
                    final String[] sa = SPLIT.split(capabilities, 0);
                    final int length = sa.length;
                    if (0 == length) {
                        session.setParameter(Session.PARAM_CAPABILITIES, Collections.<String> emptyList());
                    } else {
                        session.setParameter(Session.PARAM_CAPABILITIES, Collections.<String> unmodifiableList(Arrays.asList(sa)));
                    }
                }
            }
            retval.setServerToken((String) session.getParameter(LoginFields.SERVER_TOKEN));
            if (SessionEnhancement.class.isInstance(authed)) {
                ((SessionEnhancement) authed).enhanceSession(session);
            }
            retval.setSession(session);

            // init session
            ServerSession serverSession = ServerSessionAdapter.valueOf(session);
            serverSession.getUser();
            serverSession.getUserSettingMail();
            serverSession.getUserConfiguration();

            // Trigger registered login handlers
            triggerLoginHandlers(retval);
            return retval;
        } catch (final OXException e) {
            if (DBPoolingExceptionCodes.PREFIX.equals(e.getPrefix())) {
                LOG.error(e.getLogMessage(), e);
            }
            throw e;
        } catch (final RuntimeException e) {
            throw LoginExceptionCodes.UNKNOWN.create(e, e.getMessage());
        } finally {
            logLoginRequest(request, retval);
        }
    }

    private void checkClient(final LoginRequest request, final User user, final Context ctx) throws OXException {
        try {
            final String client = request.getClient();
            // Check for OLOX v2.0
            if ("USM-JSON".equalsIgnoreCase(client)) {
                final UserConfigurationStorage ucs = UserConfigurationStorage.getInstance();
                final UserConfiguration userConfiguration = ucs.getUserConfiguration(user.getId(), user.getGroups(), ctx);
                if (!userConfiguration.hasOLOX20()) {
                    // Deny login for OLOX v2.0 client since disabled as per user configuration
                    throw LoginExceptionCodes.CLIENT_DENIED.create(client);
                }
            }
        } catch (final OXException e) {
            throw e;
        }
    }

    private Context findContext(final String contextInfo) throws OXException {
        final ContextStorage contextStor = ContextStorage.getInstance();
        final int contextId = contextStor.getContextId(contextInfo);
        if (ContextStorage.NOT_FOUND == contextId) {
            throw ContextExceptionCodes.NO_MAPPING.create(contextInfo);
        }
        final Context context = contextStor.getContext(contextId);
        if (null == context) {
            throw ContextExceptionCodes.NOT_FOUND.create(I(contextId));
        }
        return context;
    }

    private User findUser(final Context ctx, final String userInfo) throws OXException {
        final String proxyDelimiter = MailProperties.getInstance().getAuthProxyDelimiter();
        final UserStorage us = UserStorage.getInstance();
        int userId = 0;
        if (null != proxyDelimiter && userInfo.contains(proxyDelimiter)) {
            userId = us.getUserId(userInfo.substring(userInfo.indexOf(proxyDelimiter) + proxyDelimiter.length(), userInfo.length()), ctx);
        } else {
            userId = us.getUserId(userInfo, ctx);
        }
        return us.getUser(userId, ctx);
    }

    /**
     * Performs the logout for specified session ID.
     *
     * @param sessionId The session ID
     * @throws OXException If logout fails
     */
    public Session doLogout(final String sessionId) throws OXException {
        // Drop the session
        final SessiondService sessiondService = SessiondService.SERVICE_REFERENCE.get();
        final Session session = sessiondService.getSession(sessionId);
        if (null == session) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("No session found for ID: " + sessionId);
            }
            return null;
        }
        // Get context
        final ContextStorage contextStor = ContextStorage.getInstance();
        final Context context;
        context = contextStor.getContext(session.getContextId());
        if (null == context) {
            throw ContextExceptionCodes.NOT_FOUND.create(Integer.valueOf(session.getContextId()));
        }
        // Get user
        final User u;
        {
            final UserStorage us = UserStorage.getInstance();
            u = us.getUser(session.getUserId(), context);
        }
        final LoginResultImpl logout = new LoginResultImpl(session, context, u);
        // Remove session
        sessiondService.removeSession(sessionId);
        logLogout(logout);
        // Trigger registered logout handlers
        triggerLogoutHandlers(logout);
        return session;
    }

    private static void triggerLoginHandlers(final LoginResult login) {
        final ThreadPoolService executor = ThreadPools.getThreadPool();
        if (null == executor) {
            for (final Iterator<LoginHandlerService> it = LoginHandlerRegistry.getInstance().getLoginHandlers(); it.hasNext();) {
                final LoginHandlerService handler = it.next();
                handleSafely(login, handler, true);
            }
        } else {
            ThreadPoolCompletionService<Void> completionService = null;
            int blocking = 0;
            boolean tranzient = login.getSession().isTransient();
            for (final Iterator<LoginHandlerService> it = LoginHandlerRegistry.getInstance().getLoginHandlers(); it.hasNext();) {
                final LoginHandlerService handler = it.next();
                if (tranzient && NonTransient.class.isInstance(handler)) {
                    // skip
                    continue;
                }
                if (handler instanceof Blocking) {
                    // Current LoginHandlerService must not be invoked concurrently
                    if (null == completionService) {
                        completionService = new ThreadPoolCompletionService<Void>(executor);
                    }
                    Callable<Void> callable = new Callable<Void>() {
                        @Override
                        public Void call() {
                            handleSafely(login, handler, true);
                            return null;
                        }
                    };
                    completionService.submit(callable);
                    blocking++;
                } else {
                    executor.submit(new LoginPerformerTask() {
                        @Override
                        public Object call() {
                            handleSafely(login, handler, true);
                            return null;
                        }
                    }, CallerRunsBehavior.getInstance());
                }
            }
            // Await completion of blocking LoginHandlerServices
            if (blocking > 0 && null != completionService) {
                for (int i = 0; i < blocking; i++) {
                    try {
                        completionService.take();
                    } catch (final InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
    }

    private static void triggerLogoutHandlers(final LoginResult logout) {
        final ThreadPoolService executor = ThreadPools.getThreadPool();
        if (null == executor) {
            for (final Iterator<LoginHandlerService> it = LoginHandlerRegistry.getInstance().getLoginHandlers(); it.hasNext();) {
                handleSafely(logout, it.next(), false);
            }
        } else {
            ThreadPoolCompletionService<Void> completionService = null;
            int blocking = 0;
            for (final Iterator<LoginHandlerService> it = LoginHandlerRegistry.getInstance().getLoginHandlers(); it.hasNext();) {
                final LoginHandlerService handler = it.next();
                if (handler instanceof Blocking) {
                    // Current LoginHandlerService must not be invoked concurrently
                    if (null == completionService) {
                        completionService = new ThreadPoolCompletionService<Void>(executor);
                    }
                    Callable<Void> callable = new Callable<Void>() {
                        @Override
                        public Void call() {
                            handleSafely(logout, handler, false);
                            return null;
                        }
                    };
                    completionService.submit(callable);
                    blocking++;
                } else {
                    executor.submit(new LoginPerformerTask() {
                        @Override
                        public Object call() {
                            handleSafely(logout, handler, false);
                            return null;
                        }
                    }, CallerRunsBehavior.getInstance());
                }
            }
            // Await completion of blocking LoginHandlerServices
            if (blocking > 0 && null != completionService) {
                for (int i = 0; i < blocking; i++) {
                    try {
                        completionService.take();
                    } catch (final InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
    }

    /**
     * Handles given {@code LoginResult} safely.
     *
     * @param login The login result to handle
     * @param handler The handler
     * @param isLogin <code>true</code> to signal specified {@code LoginResult} refers to a login operation; otherwise it refers to a logout operation
     */
    protected static void handleSafely(final LoginResult login, final LoginHandlerService handler, final boolean isLogin) {
        if ((null == login) || (null == handler)) {
            return;
        }
        try {
            if (isLogin) {
                handler.handleLogin(login);
            } else {
                handler.handleLogout(login);
            }
        } catch (final OXException e) {
            e.log(LOG);
        } catch (final RuntimeException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private static void logLoginRequest(final LoginRequest request, final LoginResult result) {
        final StringBuilder sb = new StringBuilder();
        sb.append("Login:");
        sb.append(Strings.abbreviate(request.getLogin(), 256));
        sb.append(" IP:");
        sb.append(request.getClientIP());
        sb.append(" AuthID:");
        sb.append(request.getAuthId());
        sb.append(" Agent:");
        sb.append(request.getUserAgent());
        sb.append(" Client:");
        sb.append(request.getClient());
        sb.append('(');
        sb.append(request.getVersion());
        sb.append(") Interface:");
        sb.append(request.getInterface().toString());
        final Context ctx = result.getContext();
        if (null != ctx) {
            sb.append(" Context:");
            sb.append(ctx.getContextId());
            sb.append('(');
            sb.append(Strings.join(ctx.getLoginInfo(), ","));
            sb.append(')');
        }
        final User user = result.getUser();
        if (null != user) {
            sb.append(" User:");
            sb.append(user.getId());
            sb.append('(');
            sb.append(user.getLoginInfo());
            sb.append(')');
        }
        final Session session = result.getSession();
        if (null != session) {
            sb.append(" Session:");
            sb.append(session.getSessionID());
            sb.append(" Random:");
            sb.append(session.getRandomToken());
        } else {
            sb.append(" Failed.");
        }
        LOG.info(sb.toString());
    }

    private static void logLogout(final LoginResult result) {
        final StringBuilder sb = new StringBuilder();
        sb.append("Logout ");
        final Context ctx = result.getContext();
        sb.append(" Context:");
        sb.append(ctx.getContextId());
        sb.append('(');
        sb.append(Strings.join(ctx.getLoginInfo(), ","));
        sb.append(')');
        final User user = result.getUser();
        sb.append(" User:");
        sb.append(user.getId());
        sb.append('(');
        sb.append(user.getLoginInfo());
        sb.append(')');
        final Session session = result.getSession();
        sb.append(" Session:");
        sb.append(session.getSessionID());
        LOG.info(sb.toString());
    }

    public Session lookupSession(final String sessionId) throws OXException {
        return ServerServiceRegistry.getInstance().getService(SessiondService.class, true).getSession(sessionId);
    }

    public Session lookupSessionWithTokens(String clientToken, String serverToken) throws OXException {
        return ServerServiceRegistry.getInstance().getService(SessiondService.class, true).getSessionWithTokens(clientToken, serverToken);
    }
}
