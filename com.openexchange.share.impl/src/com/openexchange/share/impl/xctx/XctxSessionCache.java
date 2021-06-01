/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 * 
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.share.impl.xctx;

import static com.openexchange.java.Autoboxing.I;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.openexchange.ajax.login.LoginRequestImpl;
import com.openexchange.authentication.LoginExceptionCodes;
import com.openexchange.exception.OXException;
import com.openexchange.java.Reference;
import com.openexchange.java.util.UUIDs;
import com.openexchange.login.Interface;
import com.openexchange.login.LoginResult;
import com.openexchange.login.internal.LoginPerformer;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.sessiond.SessiondEventConstants;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.share.ShareExceptionCodes;
import com.openexchange.share.core.tools.ShareToken;
import com.openexchange.share.subscription.XctxSessionManager;

/**
 * {@link XctxSessionCache}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.org">Tobias Friedrich</a>
 * @since 7.10.5
 */
public class XctxSessionCache implements XctxSessionManager, EventHandler {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(XctxSessionCache.class);

    /** The parameter used to decorate regular sessions that spawned at least one cross-context session. */
    private static final String PARAM_XCTX_SESSIONS = "__session.xctx_sessions";

    private final ServiceLookup services;
    private final Cache<String, String> guestSessionCache;

    /**
     * Initializes a new {@link XctxSessionCache}.
     *
     * @param services A service lookup reference
     */
    public XctxSessionCache(ServiceLookup services) {
        super();
        this.services = services;
        this.guestSessionCache = CacheBuilder.newBuilder()
            .expireAfterAccess(60, TimeUnit.MINUTES)
            .removalListener(new RemovalListener<String, String>() {

                @Override
                public void onRemoval(RemovalNotification<String, String> notification) {
                    doLogout(notification.getValue());
                }
            })
        .build();
    }

    @Override
    public Session getGuestSession(Session session, String baseToken, String password) throws OXException {
        ShareToken shareToken = new ShareToken(baseToken);
        if (session.getContextId() == shareToken.getContextID()) {
            throw ShareExceptionCodes.INVALID_TOKEN.create(baseToken);
        }
        Reference<Session> newGuestSession = new Reference<Session>();
        String key = getKey(session, baseToken, password);
        String guestSessionId;
        try {
            guestSessionId = guestSessionCache.get(key, new Callable<String>() {

                @Override
                public String call() throws Exception {
                    Session guestSession = doLogin(session, baseToken, password);
                    newGuestSession.setValue(guestSession);
                    return guestSession.getSessionID();
                }
            });
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (null != cause && OXException.class.isInstance(e.getCause())) {
                throw (OXException) cause;
            }
            throw LoginExceptionCodes.UNKNOWN.create(e, e.getMessage());
        }
        if (newGuestSession.hasValue()) {
            /*
             * new guest login was performed in this request
             */
            return newGuestSession.getValue();
        }
        /*
         * session identifier looked up successfully in cache; get & implicitly validate session from service
         */
        Session guestSession = services.getServiceSafe(SessiondService.class).getSession(guestSessionId, false);
        if (null == guestSession) {
            /*
             * invalidate cache entry & login again if no session could be looked up
             */
            guestSessionCache.asMap().remove(key, guestSessionId);
            return getGuestSession(session, baseToken, password);
        }
        return guestSession;
    }


    @Override
    public void handleEvent(Event event) {
        if (null == event) {
            return;
        }
        try {
            if (SessiondEventConstants.TOPIC_REMOVE_CONTAINER.equals(event.getTopic())) {
                List<String> sessionIds = new ArrayList<String>();
                for (Session session : ((Map<String, Session>) event.getProperty(SessiondEventConstants.PROP_CONTAINER)).values()) {
                    if (session.containsParameter(PARAM_XCTX_SESSIONS)) {
                        sessionIds.add(session.getSessionID());
                    }
                }
                invalidateGuestSessions(sessionIds);
            } else if (SessiondEventConstants.TOPIC_REMOVE_SESSION.equals(event.getTopic())) {
                Session session = (Session) event.getProperty(SessiondEventConstants.PROP_SESSION);
                if (null != session && session.containsParameter(PARAM_XCTX_SESSIONS)) {
                    invalidateGuestSessions(Collections.singleton(session.getSessionID()));
                }
            }
        } catch (Exception e) {
            LOG.warn("Unexpected error invalidating cross-context sessions after removal of local session(s)", e);
        }
    }

    /**
     * Performs the login.
     *
     * @param session The user session spawning the guest session
     * @param baseToken The base token of the guest user
     * @param password The optional password of the guest account
     * @return The guest session
     */
    Session doLogin(Session session, String baseToken, String password) throws OXException {
        XctxLoginMethod loginMethod = new XctxLoginMethod(services, baseToken, password);
        LoginRequestImpl loginRequest = new LoginRequestImpl.Builder()
            .login(baseToken)
            .password(password)
            .authId(UUIDs.getUnformattedStringFromRandom())
            .tranzient(true)
            .client(session.getClient())
            .iface(Interface.HTTP_JSON)
            .clientIP(session.getLocalIp())
            .hash(session.getHash())
            .userAgent((String) session.getParameter(Session.PARAM_USER_AGENT))
        .build();
        LoginResult loginResult = LoginPerformer.getInstance().doLogin(loginRequest, new HashMap<String, Object>(), loginMethod);
        if (null == loginResult || null == loginResult.getSession()) {
            throw ShareExceptionCodes.UNEXPECTED_ERROR.create("no session from login result");
        }
        LOG.debug("Successful login for share {} with guest user {} in context {}, using session {}.",
            baseToken, I(loginResult.getUser().getId()), I(loginResult.getContext().getContextId()), loginResult.getSession().getSessionID());
        session.setParameter(PARAM_XCTX_SESSIONS, Boolean.TRUE);
        return loginResult.getSession();
    }

    /**
     * Performs the logout.
     *
     * @param session The session to log-out
     */
    void doLogout(String sessionId) {
        LOG.debug("Cross-context guest logout: {}...", sessionId);
        try {
            Session removedSession = LoginPerformer.getInstance().doLogout(sessionId);
            if (null != removedSession) {
                LOG.debug("Removed cross-context guest session {}", removedSession);
            } else {
                LOG.debug("Cross-context guest session {} not removed.", sessionId);
            }
        } catch (OXException e) {
            LOG.warn("Error removing cross-context guest session", e);
        }
    }

    private void invalidateGuestSessions(Collection<String> sessionIds) {
        if (null == sessionIds || sessionIds.isEmpty()) {
            return;
        }
        List<String> toInvalidate = new ArrayList<String>();
        for (String key : guestSessionCache.asMap().keySet()) {
            for (String sessionId : sessionIds) {
                if (key.startsWith(sessionId)) {
                    toInvalidate.add(key);
                }
            }
        }
        if (false == toInvalidate.isEmpty()) {
            guestSessionCache.invalidateAll(toInvalidate);
            LOG.debug("Successfully invalidated {} cross-context session(s) spawned by local sessions.", I(toInvalidate.size()));
        }
    }

    private static String getKey(Session session, String baseToken, String password) {
        return new StringBuilder().append(session.getSessionID()).append(baseToken).append((null == password ? "" : password).hashCode()).toString();
    }

}
