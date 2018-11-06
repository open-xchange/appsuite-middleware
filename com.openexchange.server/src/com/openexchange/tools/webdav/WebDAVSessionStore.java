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

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import javax.servlet.http.HttpServletRequest;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.openexchange.authentication.LoginExceptionCodes;
import com.openexchange.exception.OXException;
import com.openexchange.login.LoginRequest;
import com.openexchange.login.internal.LoginPerformer;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.sessiond.SessiondServiceExtended;
import com.openexchange.tools.encoding.Base64;

/**
 * Cache for short-living WebDAV sessions that can't be looked up by cookie for consecutive requests of the same client.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.org">Tobias Friedrich</a>
 */
public class WebDAVSessionStore {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(WebDAVSessionStore.class);
    private static final String PARAMETER_WEBDAV_SESSION = "com.openexchange.webdav.session";
    private static final WebDAVSessionStore instance = new WebDAVSessionStore();

    /**
     * Gets the session store instance.
     *
     * @return The instance
     */
    public static WebDAVSessionStore getInstance() {
        return instance;
    }

    private final Cache<String, String> sessionIdsPerClient;

    /**
     * Initializes a new {@link WebDAVSessionStore}.
     */
    private WebDAVSessionStore() {
        super();
        this.sessionIdsPerClient = CacheBuilder.newBuilder()
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

    /**
     * Gets the session for the supplied login request, either from the cached sessions or by implicitly performing the login operation.
     *
     * @param loginRequest The login request
     * @param httpRequest The underlying HTTP request, or <code>null</code> if not available
     * @return The session
     */
    public Session getSession(final LoginRequest loginRequest, HttpServletRequest httpRequest) throws OXException {
        String key = getKey(loginRequest);
        String sessionId;
        try {
            sessionId = sessionIdsPerClient.get(key, new Callable<String>() {

                @Override
                public String call() throws Exception {
                    Session session = login(loginRequest);
                    if (null != httpRequest) {
                        httpRequest.setAttribute(PARAMETER_WEBDAV_SESSION, session);
                    }
                    return session.getSessionID();
                }
            });
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (null != cause && OXException.class.isInstance(e.getCause())) {
                throw (OXException)cause;
            }
            throw LoginExceptionCodes.UNKNOWN.create(e, e.getMessage());
        }
        Object sessionAttribute = httpRequest.getAttribute(PARAMETER_WEBDAV_SESSION);
        if (null == sessionAttribute || false == Session.class.isInstance(sessionAttribute)) {
            /*
             * session identifier looked up successfully in cache; get & implicitly validate session from service
             */
            Session session = optSession(sessionId);
            if (null == session) {
                /*
                 * invalidate & login again if no session could be looked up
                 */
                sessionIdsPerClient.asMap().remove(key, sessionId);
                return getSession(loginRequest, httpRequest);
            }
            return session;
        }
        /*
         * login was performed in this request, use session from parameter
         */
        httpRequest.removeAttribute(PARAMETER_WEBDAV_SESSION);
        return (Session) sessionAttribute;
    }

    /**
     * Optionally looks up a session by its identifier from the session service.
     * 
     * @param sessionId The identifier of the session to get
     * @return The session, or <code>null</code> if not found
     */
    private static Session optSession(String sessionId) {
        SessiondService sessiondService = ServerServiceRegistry.getServize(SessiondService.class);
        if (null != sessiondService) {
            if (SessiondServiceExtended.class.isInstance(sessiondService)) {
                return ((SessiondServiceExtended) sessiondService).getSession(sessionId, false);
            }
            return sessiondService.getSession(sessionId);
        }
        return null;
    }

    /**
     * Performs the login.
     *
     * @param loginRequest The login request to use
     * @return The associated session
     * @throws OXException If login attempt fails
     */
    private static Session login(LoginRequest loginRequest) throws OXException {
        LOG.debug("WebDAV Login: {}...", loginRequest.getLogin());
        Session session = LoginPerformer.getInstance().doLogin(loginRequest).getSession();
        LOG.debug("Added WebDAV session {}", session);
        return session;
    }

    /**
     * Performs the logout.
     *
     * @param session The session to log-out
     */
    private static void logout(String sessionId) {
        LOG.debug("WebDAV Logout: {}...", sessionId);
        try {
            Session removedSession = LoginPerformer.getInstance().doLogout(sessionId);
            if (null != removedSession) {
                LOG.debug("Removed WebDAV session {}", removedSession);
            } else {
                LOG.debug("WebDAV session {} not removed.", sessionId);
            }
        } catch (OXException e) {
            LOG.warn("Error removing WebDAV session", e);
        }
    }

    private static String getKey(LoginRequest loginRequest) {
        int clientHash = (loginRequest.getInterface() + loginRequest.getUserAgent() + loginRequest.getClientIP()).hashCode();
        return Base64.encode(new StringBuilder().append(loginRequest.getLogin()).append(loginRequest.getPassword()).append(clientHash).toString());
    }

}
