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
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.openexchange.authentication.LoginExceptionCodes;
import com.openexchange.exception.OXException;
import com.openexchange.login.LoginRequest;
import com.openexchange.login.internal.LoginPerformer;
import com.openexchange.session.Session;
import com.openexchange.tools.encoding.Base64;

/**
 * Cache for short-living WebDAV sessions that can't be looked up by cookie for consecutive requests of the same client.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.org">Tobias Friedrich</a>
 */
public class WebDAVSessionStore {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(WebDAVSessionStore.class);

    private static final WebDAVSessionStore instance = new WebDAVSessionStore();

    /**
     * Gets the session store instance.
     *
     * @return The instance
     */
    public static WebDAVSessionStore getInstance() {
        return instance;
    }

    private final Cache<String, Session> webdavSessions;

    /**
     * Initializes a new {@link WebDAVSessionStore}.
     */
    private WebDAVSessionStore() {
        super();
        this.webdavSessions = CacheBuilder.newBuilder()
            .expireAfterAccess(5, TimeUnit.MINUTES)
            .expireAfterWrite(1, TimeUnit.HOURS)
            .removalListener(new RemovalListener<String, Session>() {

                @Override
                public void onRemoval(RemovalNotification<String, Session> notification) {
                    logout(notification.getValue());
                }
            })
        .build();
    }

    /**
     * Gets the session for the supplied login request, either from the cached sessions or by implicitly performing the login operation.
     *
     * @param loginRequest The login request
     * @return The session
     * @throws OXException
     */
    public Session getSession(final LoginRequest loginRequest) throws OXException {
        String key = getKey(loginRequest);
        try {
            return webdavSessions.get(key, new Callable<Session>() {

                @Override
                public Session call() throws Exception {
                    return login(loginRequest);
                }
            });
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (null != cause && OXException.class.isInstance(e.getCause())) {
                throw (OXException)cause;
            }
            throw LoginExceptionCodes.UNKNOWN.create(e, e.getMessage());
        }
    }

    /**
     * Performs the login.
     *
     * @param loginRequest The login request to use
     * @return The associated session
     * @throws OXException If login attempt fails
     */
    static Session login(LoginRequest loginRequest) throws OXException {
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
    static void logout(Session session) {
        LOG.debug("WebDAV Logout: {}...", session);
        try {
            Session removedSession = LoginPerformer.getInstance().doLogout(session.getSessionID());
            if (null != removedSession) {
                LOG.debug("Removed WebDAV session {}", removedSession);
            } else {
                LOG.debug("WebDAV session {} not removed.", session);
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
