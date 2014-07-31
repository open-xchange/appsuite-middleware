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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;
import javax.security.auth.login.LoginException;
import com.openexchange.ajax.fields.LoginFields;
import com.openexchange.authentication.Authenticated;
import com.openexchange.authentication.Cookie;
import com.openexchange.authentication.GuestAuthenticated;
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
import com.openexchange.login.Blocking;
import com.openexchange.login.LoginHandlerService;
import com.openexchange.login.LoginRequest;
import com.openexchange.login.LoginResult;
import com.openexchange.login.NonTransient;
import com.openexchange.login.internal.format.DefaultLoginFormatter;
import com.openexchange.login.internal.format.LoginFormatter;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.threadpool.ThreadPoolCompletionService;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.threadpool.ThreadPools;
import com.openexchange.threadpool.behavior.CallerRunsBehavior;

/**
 * {@link LoginPerformer} - Performs a login for specified credentials.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class LoginPerformer {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(LoginPerformer.class);

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
        final Map<String, Object> properties = new HashMap<String, Object>();
        return doLogin(request, properties, new AutoLoginMethod(request, properties));
    }

    /**
     * Performs the login for specified login request.
     *
     * @param request The login request
     * @return The login providing login information
     * @throws OXException If login fails
     */
    public LoginResult doLogin(LoginRequest request, Map<String, Object> properties, LoginMethodClosure loginMethod) throws OXException {
        sanityChecks(request);
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

            /*
             * get user & context
             */
            final Context ctx;
            final User user;
            if (GuestAuthenticated.class.isInstance(authed)) {
                /*
                 * use already resolved user / context
                 */
                GuestAuthenticated guestAuthenticated = (GuestAuthenticated) authed;
                ctx = getContext(guestAuthenticated.getContextID());
                user = getUser(ctx, guestAuthenticated.getUserID());
            } else {
                /*
                 * perform user / context lookup
                 */
                ctx = findContext(authed.getContextInfo());
                user = findUser(ctx, authed.getUserInfo());
                // Checks if something is deactivated.
                final AuthorizationService authService = Authorization.getService();
                if (null == authService) {
                    final OXException e = ServiceExceptionCode.SERVICE_UNAVAILABLE.create(AuthorizationService.class.getName());
                    LOG.error("unable to find AuthorizationService", e);
                    throw e;
                }
                /*
                 * authorize
                 */
                authService.authorizeUser(ctx, user);
            }
            retval.setContext(ctx);
            retval.setUser(user);

            // Check if indicated client is allowed to perform a login
            checkClient(request, user, ctx);
            // Create session
            SessiondService sessiondService = SessiondService.SERVICE_REFERENCE.get();
            if (null == sessiondService) {
                sessiondService = ServerServiceRegistry.getInstance().getService(SessiondService.class);
                if (null == sessiondService) {
                    // Giving up...
                    throw ServiceExceptionCode.absentService(SessiondService.class);
                }
            }
            final Session session = sessiondService.addSession(new AddSessionParameterImpl(authed.getUserInfo(), request, user, ctx));
            if (null == session) {
                // Session could not be created
                throw LoginExceptionCodes.UNKNOWN.create("Session could not be created.");
            }
            retval.setServerToken((String) session.getParameter(LoginFields.SERVER_TOKEN));
            if (SessionEnhancement.class.isInstance(authed)) {
                ((SessionEnhancement) authed).enhanceSession(session);
            }
            retval.setSession(session);

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

    private static void sanityChecks(LoginRequest request) throws OXException {
        // Check if somebody is using the User-Agent as client parameter
        String client = request.getClient();
        if (null != client && client.equals(request.getUserAgent())) {
            throw LoginExceptionCodes.DONT_USER_AGENT.create();
        }
    }

    private static void checkClient(final LoginRequest request, final User user, final Context ctx) throws OXException {
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
    }

    private static Context findContext(final String contextInfo) throws OXException {
        final ContextStorage contextStor = ContextStorage.getInstance();
        final int contextId = contextStor.getContextId(contextInfo);
        if (ContextStorage.NOT_FOUND == contextId) {
            throw ContextExceptionCodes.NO_MAPPING.create(contextInfo);
        }
        return getContext(contextId);
    }

    private static User findUser(final Context ctx, final String userInfo) throws OXException {
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
     * Gets a context by it's identifier from the context storage.
     *
     * @param contextID The context ID
     * @return The context
     * @throws OXException
     */
    private static Context getContext(int contextID) throws OXException {
        final Context context = ContextStorage.getInstance().getContext(contextID);
        if (null == context) {
            throw ContextExceptionCodes.NOT_FOUND.create(I(contextID));
        }
        return context;
    }

    /**
     * Gets a user by it's identifier from the user storage.
     *
     * @param ctx The context
     * @param userID The user ID
     * @return The user
     * @throws OXException
     */
    private static User getUser(Context ctx, int userID) throws OXException {
        return UserStorage.getInstance().getUser(userID, ctx);
    }

    /**
     * Performs the logout for specified session ID.
     *
     * @param sessionId The session ID
     * @throws OXException If logout fails
     */
    public Session doLogout(final String sessionId) throws OXException {
        // Drop the session
        SessiondService sessiondService = SessiondService.SERVICE_REFERENCE.get();
        if (null == sessiondService) {
            sessiondService = ServerServiceRegistry.getInstance().getService(SessiondService.class);
            if (null == sessiondService) {
                // Giving up...
                throw ServiceExceptionCode.absentService(SessiondService.class);
            }
        }
        final Session session = sessiondService.getSession(sessionId);
        if (null == session) {
            LOG.debug("No session found for ID: {}", sessionId);
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
            final boolean tranzient = login.getSession().isTransient();
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
                    final Callable<Void> callable = new Callable<Void>() {

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
                    final Callable<Void> callable = new Callable<Void>() {

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
            LOG.error("", e);
        }
    }

    private static final AtomicReference<LoginFormatter> FORMATTER_REF = new AtomicReference<LoginFormatter>();

    /**
     * Sets the applicable formatter.
     *
     * @param formatter The formatter or <code>null</code> to remove
     */
    public static void setLoginFormatter(final LoginFormatter formatter) {
        FORMATTER_REF.set(formatter);
    }

    private static void logLoginRequest(final LoginRequest request, final LoginResult result) {
        final LoginFormatter formatter = FORMATTER_REF.get();
        final StringBuilder sb = new StringBuilder(1024);
        if (null == formatter) {
            DefaultLoginFormatter.getInstance().formatLogin(request, result, sb);
        } else {
            formatter.formatLogin(request, result, sb);
        }
        LOG.info(sb.toString());
    }

    private static void logLogout(final LoginResult result) {
        final LoginFormatter formatter = FORMATTER_REF.get();
        final StringBuilder sb = new StringBuilder(512);
        if (null == formatter) {
            DefaultLoginFormatter.getInstance().formatLogout(result, sb);
        } else {
            formatter.formatLogout(result, sb);
        }
        LOG.info(sb.toString());
    }

    public Session lookupSession(final String sessionId) throws OXException {
        return ServerServiceRegistry.getInstance().getService(SessiondService.class, true).getSession(sessionId);
    }

    public Session lookupSessionWithTokens(String clientToken, String serverToken) throws OXException {
        return ServerServiceRegistry.getInstance().getService(SessiondService.class, true).getSessionWithTokens(clientToken, serverToken);
    }
}
