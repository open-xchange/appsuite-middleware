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
 *     Copyright (C) 2004-2015 Open-Xchange, Inc.
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

package com.openexchange.ajax.requesthandler.oauth;

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
import com.openexchange.config.ConfigurationService;
import com.openexchange.configuration.ServerConfig.Property;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.java.util.UUIDs;
import com.openexchange.login.Interface;
import com.openexchange.login.LoginResult;
import com.openexchange.login.internal.LoginMethodClosure;
import com.openexchange.login.internal.LoginPerformer;
import com.openexchange.login.internal.LoginResultImpl;
import com.openexchange.oauth.provider.OAuthProviderService;
import com.openexchange.oauth.provider.OAuthToken;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.sessiond.SessiondServiceExtended;
import com.openexchange.tools.servlet.http.AuthCookie;
import com.openexchange.user.UserService;

/**
 * {@link DefaultSessionProvider}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class DefaultSessionProvider implements OAuthSessionProvider {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultSessionProvider.class);

    private final ServiceLookup services;

    private final Cache<String, String> sessionCache;

    public DefaultSessionProvider(ServiceLookup services) {
        super();
        this.services = services;
        sessionCache = CacheBuilder.newBuilder()
            .expireAfterAccess(5, TimeUnit.MINUTES)
            .expireAfterWrite(1, TimeUnit.HOURS)
            .removalListener(new RemovalListener<String, String>() {

                @Override
                public void onRemoval(RemovalNotification<String, String> notification) {
                    logout(notification.getValue());
                }
            })
            .build();
    }

    private void logout(String sessionId) {
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

    @Override
    public Session getSession(final OAuthToken token, final HttpServletRequest httpRequest) throws OXException {
        SessiondService sessiondService = requireService(SessiondService.class, services);
        String accessToken = token.getAccessToken();
        Session session = null;
        try {
            do {
                String sessionId = sessionCache.get(accessToken, new Callable<String>() {
                    @Override
                    public String call() throws Exception {
                        return login(token, httpRequest).getSessionID();
                    }
                });

                if (sessiondService instanceof SessiondServiceExtended) {
                    session = ((SessiondServiceExtended)sessiondService).getSession(sessionId, false);
                } else {
                    session = sessiondService.getSession(sessionId);
                }
                if (session == null) {
                    LOG.debug("OAuth 2.0 session with ID {} was invalidated since last request", sessionId);
                    sessionCache.asMap().remove(accessToken, sessionId);
                }
            } while (session == null);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (null != cause && OXException.class.isInstance(e.getCause())) {
                throw (OXException) cause;
            }
            throw LoginExceptionCodes.UNKNOWN.create(e, e.getMessage());
        }

        return session;
    }

    private Session login(OAuthToken token, HttpServletRequest httpRequest) throws OXException {
        ContextService contextService = requireService(ContextService.class, services);
        UserService userService = requireService(UserService.class, services);
        OAuthProviderService oAuthProvider = requireService(OAuthProviderService.class, services);

        final Context context = contextService.getContext(token.getContextID());
        final User user = userService.getUser(token.getUserID(), context);
        final String client = oAuthProvider.getClient(token).getName();
        LoginResult loginResult = LoginPerformer.getInstance().doLogin(getLoginRequest(httpRequest, user, client), new HashMap<String, Object>(1), new LoginMethodClosure() {
            @Override
            public Authenticated doAuthentication(LoginResultImpl retval) throws OXException {
                return new Authenticated() {
                    @Override
                    public String getUserInfo() {
                        return user.getLoginInfo();
                    }

                    @Override
                    public String getContextInfo() {
                        return context.getLoginInfo()[0];
                    }
                };
            }
        });

        Session session = loginResult.getSession();
        LOG.debug("Created new OAuth 2.0 session: {}", session);
        return session;
    }

    private LoginRequestImpl getLoginRequest(HttpServletRequest httpRequest, User user, String client) throws OXException {
        String userAgent = httpRequest.getHeader(HttpHeaders.USER_AGENT);
        String hash = HashCalculator.getInstance().getHash(httpRequest, userAgent, client);
        boolean forceHTTPS = com.openexchange.tools.servlet.http.Tools.considerSecure(httpRequest, forceHTTPS());
        Cookie[] cookies = getCookies(httpRequest);
        Map<String, List<String>> headers = getHeaders(httpRequest);
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
            cookies,
            forceHTTPS,
            httpRequest.getServerName(),
            httpRequest.getServerPort(),
            com.openexchange.tools.servlet.http.Tools.getRoute(httpRequest.getSession(true).getId()));
        req.setTransient(true);

        return req;
    }

    private static Cookie[] getCookies(HttpServletRequest req) {
        final List<Cookie> cookies;
        if (null == req) {
            cookies = Collections.emptyList();
        } else {
            cookies = new ArrayList<Cookie>();
            for (final javax.servlet.http.Cookie c : req.getCookies()) {
                cookies.add(new AuthCookie(c));
            }
        }
        return cookies.toArray(new Cookie[cookies.size()]);
    }

    private static Map<String, List<String>> getHeaders(HttpServletRequest req) {
        final Map<String, List<String>> headers;
        if (null == req) {
            headers = Collections.emptyMap();
        } else {
            headers = new HashMap<String, List<String>>();
            @SuppressWarnings("unchecked") Enumeration<String> headerNames = req.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String name = headerNames.nextElement();
                List<String> header = new ArrayList<String>();
                if (headers.containsKey(name)) {
                    header = headers.get(name);
                }
                header.add(req.getHeader(name));
                headers.put(name, header);
            }
        }
        return headers;
    }

    private boolean forceHTTPS() throws OXException {
        ConfigurationService configService = requireService(ConfigurationService.class, services);
        return Boolean.parseBoolean(configService.getProperty(Property.FORCE_HTTPS.getPropertyName(), Property.FORCE_HTTPS.getDefaultValue()));
    }

}
