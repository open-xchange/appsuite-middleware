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

package com.openexchange.oauth.provider.impl;

import static com.openexchange.osgi.Tools.requireService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.google.common.net.HttpHeaders;
import com.openexchange.ajax.login.HashCalculator;
import com.openexchange.ajax.login.LoginRequestImpl;
import com.openexchange.authentication.Authenticated;
import com.openexchange.authentication.Cookie;
import com.openexchange.authentication.LoginExceptionCodes;
import com.openexchange.authentication.SessionEnhancement;
import com.openexchange.config.ConfigurationService;
import com.openexchange.configuration.ServerConfig.Property;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.java.util.UUIDs;
import com.openexchange.login.Interface;
import com.openexchange.login.LoginResult;
import com.openexchange.login.internal.LoginMethodClosure;
import com.openexchange.login.internal.LoginPerformer;
import com.openexchange.login.internal.LoginResultImpl;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.tools.servlet.http.AuthCookie;
import com.openexchange.user.User;
import com.openexchange.user.UserService;

/**
 * {@link SessionProvider}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class SessionProvider {

    static final Logger LOG = LoggerFactory.getLogger(SessionProvider.class);

    private static final String OAUTH_SESSION_KEY = "oauthSession";

    private final ServiceLookup services;
    private final Cache<String, String> sessionCache;

    /**
     * Initializes a new {@link SessionProvider}.
     *
     * @param services The service lookup
     */
    public SessionProvider(ServiceLookup services) {
        super();
        this.services = services;
        sessionCache = CacheBuilder.newBuilder()
            .expireAfterAccess(5, TimeUnit.MINUTES)
            .expireAfterWrite(1, TimeUnit.HOURS)
            .removalListener(new RemovalListener<String, String>() {

                @Override
                public void onRemoval(RemovalNotification<String, String> notification) {
                    String sessionId = notification.getValue();
                    try {
                        Session session = LoginPerformer.getInstance().doLogout(sessionId);
                        if (session == null) {
                            LOG.debug("Removed session ID {} from OAuth 2.0 cache. The according session was already removed from the session container.", sessionId);
                        } else {
                            LOG.debug("Removed session ID {} from OAuth 2.0 cache. A logout was performed.", sessionId);
                        }
                    } catch (OXException e) {
                        LOG.warn("Error while removing OAuth 2.0 session", e);
                    }
                }
            }).build();
    }


    /**
     * Performs a logout
     *
     * @param sessionId The session id to log out
     */
    void logout(String sessionId) {
        try {
            Session session = LoginPerformer.getInstance().doLogout(sessionId);
            if (session == null) {
                LOG.debug("Removed session ID {} from OAuth 2.0 cache. The according session was already removed from the session container.", sessionId);
            } else {
                LOG.debug("Removed session ID {} from OAuth 2.0 cache. A logout was performed.", sessionId);
            }
        } catch (OXException e) {
            LOG.warn("Error while removing OAuth 2.0 session", e);
        }
    }

    /**
     * Get a session for the given accessToken
     *
     * @param accessToken The access token
     * @param contextId The context id
     * @param userId The user id
     * @param clientName The client name
     * @param httpRequest The {@link HttpServletRequest}
     * @return The session
     * @throws OXException
     */
    public Session getSession(final String accessToken, final int contextId, final int userId, final String clientName, final HttpServletRequest httpRequest) throws OXException {
        SessiondService sessiondService = requireService(SessiondService.class, services);
        Session session = null;
        try {
            do {
                String sessionId = sessionCache.get(accessToken, new Callable<String>() {

                    @Override
                    public String call() throws Exception {
                        try {
                            return login(contextId, userId, clientName, httpRequest, accessToken).getSessionID();
                        } catch (Exception e) {
                            LOG.error("Exception occurred while trying to get session.", e);
                            throw e;
                        }
                    }
                });

                Object attribute = httpRequest.getAttribute(OAUTH_SESSION_KEY);
                if (attribute == null) {
                    // Session identifier fetched from cache; validate it through fetching the session from SessionD
                    session = sessiondService.getSession(sessionId, false);
                    if (session == null) {
                        LOG.debug("OAuth 2.0 session with ID {} was invalidated since last request", sessionId);
                        sessionCache.asMap().remove(accessToken, sessionId);
                    }
                } else {
                    // This thread performed the login using given HTTP request
                    session = (Session) attribute;
                }
            } while (session == null);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (OXException.class.isInstance(cause)) {
                throw (OXException) cause;
            }
            throw LoginExceptionCodes.UNKNOWN.create(cause, cause.getMessage());
        }
        return session;
    }

    /**
     * Performs the actual login for the OAuth client.
     *
     * @param contextId The context identifier
     * @param userId The user identifier
     * @param clientName The name of the OAuth client
     * @param httpRequest The HTTP request performing the login
     * @param accessToken The access token
     * @return The established session
     * @throws OXException If login fails
     */
    Session login(int contextId, int userId, String clientName, HttpServletRequest httpRequest, String accessToken) throws OXException {
        ContextService contextService = requireService(ContextService.class, services);
        UserService userService = requireService(UserService.class, services);

        final Context context = contextService.getContext(contextId);
        final User user = userService.getUser(userId, context);
        LoginResult loginResult = LoginPerformer.getInstance().doLogin(getLoginRequest(httpRequest, user, clientName), new HashMap<String, Object>(1), new LoginMethodClosure() {

            @Override
            public Authenticated doAuthentication(LoginResultImpl retval) {
                return new OAuthProviderAuthenticated(user.getLoginInfo(), context.getLoginInfo()[0], accessToken);
            }
        });

        Session session = loginResult.getSession();
        httpRequest.setAttribute(OAUTH_SESSION_KEY, session);
        LOG.debug("Created new OAuth 2.0 session: {}", session.getSessionID());
        return session;
    }

    /**
     * Creates a new login request for the user and client
     *
     * @param httpRequest The original http request
     * @param user The user
     * @param client The client id
     * @return A {@link LoginRequestImpl}
     * @throws OXException
     */
    private LoginRequestImpl getLoginRequest(HttpServletRequest httpRequest, User user, String client) throws OXException {
        String userAgent = httpRequest.getHeader(HttpHeaders.USER_AGENT);
        String hash = HashCalculator.getInstance().getHash(httpRequest, userAgent, client);
        boolean forceHTTPS = com.openexchange.tools.servlet.http.Tools.considerSecure(httpRequest, forceHTTPS());
        Cookie[] cookies = getCookies(httpRequest);
        Map<String, List<String>> headers = getHeaders(httpRequest);
        HttpSession session = httpRequest.getSession(false);
        LoginRequestImpl req = new LoginRequestImpl(
            user.getLoginInfo(),
            null,                                   /* password */
            httpRequest.getRemoteAddr(),
            userAgent,
            UUIDs.getUnformattedStringFromRandom(), /* auth id */
            client,
            "1.0",
            hash,
            Interface.HTTP_JSON,
            headers,
            httpRequest.getParameterMap(),
            cookies,
            forceHTTPS,
            httpRequest.getServerName(),
            httpRequest.getServerPort(),
            session);
        req.setTransient(true);

        return req;
    }

    /**
     * Gets the cookies from the given requests.
     *
     * @param req The {@link HttpServletRequest}
     * @return An array of {@link Cookie}s
     */
    private static Cookie[] getCookies(HttpServletRequest req) {
        final List<Cookie> cookies;
        if (null == req) {
            cookies = Collections.emptyList();
        } else {
            cookies = new ArrayList<>();
            for (final javax.servlet.http.Cookie c : req.getCookies()) {
                cookies.add(new AuthCookie(c));
            }
        }
        return cookies.toArray(new Cookie[cookies.size()]);
    }

    /**
     * Gets the headers from the given request
     *
     * @param req The request
     * @return A map of headers
     */
    private static Map<String, List<String>> getHeaders(HttpServletRequest req) {
        final Map<String, List<String>> headers;
        if (null == req) {
            headers = Collections.emptyMap();
        } else {
            headers = new HashMap<>();
            Enumeration<String> headerNames = req.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String name = headerNames.nextElement();
                List<String> header = new ArrayList<>();
                if (headers.containsKey(name)) {
                    header = headers.get(name);
                }
                header.add(req.getHeader(name));
                headers.put(name, header);
            }
        }
        return headers;
    }

    /**
     * Whether https should be enforced or not
     *
     * @return <code>true</code> if https should be enforced
     * @throws OXException
     */
    private boolean forceHTTPS() throws OXException {
        ConfigurationService configService = requireService(ConfigurationService.class, services);
        return Boolean.parseBoolean(configService.getProperty(Property.FORCE_HTTPS.getPropertyName(), Property.FORCE_HTTPS.getDefaultValue()));
    }

    /**
     * {@link OAuthProviderAuthenticated} enhances the session with the {@link Session#PARAM_IS_OAUTH} nad {@link Session#PARAM_OAUTH_ACCESS_TOKEN} parametern.
     *
     * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
     */
    private static final class OAuthProviderAuthenticated implements Authenticated, SessionEnhancement {

        private final String userInfo;
        private final String contextInfo;
        private final String accessToken;

        /**
         * Initializes a new {@link OAuthProviderAuthenticated}.
         *
         * @param userInfo The user info
         * @param contextInfo The context info
         * @param accessToken The oauth access token
         */
        OAuthProviderAuthenticated(String userInfo, String contextInfo, String accessToken) {
            super();
            this.userInfo = userInfo;
            this.contextInfo = contextInfo;
            this.accessToken = accessToken;
        }

        @Override
        public void enhanceSession(Session session) {
            session.setParameter(Session.PARAM_IS_OAUTH, Boolean.TRUE);
            session.setParameter(Session.PARAM_OAUTH_ACCESS_TOKEN, accessToken);
        }

        @Override
        public String getContextInfo() {
            return contextInfo;
        }

        @Override
        public String getUserInfo() {
            return userInfo;
        }

    }

}
