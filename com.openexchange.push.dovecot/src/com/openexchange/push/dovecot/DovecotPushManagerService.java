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

package com.openexchange.push.dovecot;

import static com.openexchange.java.Autoboxing.I;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.push.PushExceptionCodes;
import com.openexchange.push.PushListener;
import com.openexchange.push.PushListenerService;
import com.openexchange.push.PushManagerExtendedService;
import com.openexchange.push.PushUser;
import com.openexchange.push.PushUserInfo;
import com.openexchange.push.PushUtility;
import com.openexchange.push.dovecot.locking.DovecotPushClusterLock;
import com.openexchange.push.dovecot.locking.SessionInfo;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.sessiond.SessiondServiceExtended;


/**
 * {@link DovecotPushManagerService}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.6.2
 */
public class DovecotPushManagerService implements PushManagerExtendedService {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(DovecotPushManagerService.class);

    private static enum StopResult {
        NONE, RECONNECTED, RECONNECTED_AS_PERMANENT, STOPPED;
    }

    private static volatile DovecotPushManagerService instance;

    /**
     * Initializes the push manager instance.
     *
     * @param endPoint The HTTP end-point
     * @param clusterLock The cluster lock
     * @param services The service look-up
     * @return The new instance
     * @throws OXException If initialization fails
     */
    public static DovecotPushManagerService newInstance(String endPoint, DovecotPushClusterLock clusterLock, ServiceLookup services) throws OXException {
        DovecotPushManagerService newi = new DovecotPushManagerService(endPoint, clusterLock, services);
        instance = newi;
        return newi;
    }

    /**
     * Drops the instance.
     */
    public static void dropInstance() {
        DovecotPushManagerService pushManager = instance;
        if (null != pushManager) {
            instance = null;
            pushManager.stopAll();
        }
    }

    /**
     * Gets the instance.
     *
     * @return The instance
     */
    public static DovecotPushManagerService getInstance() {
        return instance;
    }

    // --------------------------------------------------------------------------------------------------------------------------------

    private final String name;
    private final ServiceLookup services;
    private final Map<SimpleKey, DovecotPushListener> listeners;
    private final DovecotPushClusterLock clusterLock;
    private final String authLogin;
    private final String authPassword;
    private final URI uri;

    /**
     * Initializes a new {@link DovecotPushManagerService}.
     */
    private DovecotPushManagerService(String endPoint, DovecotPushClusterLock clusterLock, ServiceLookup services) throws OXException {
        super();
        name = "Dovecot Push Manager";
        this.services = services;
        this.clusterLock = clusterLock;
        listeners = new HashMap<SimpleKey, DovecotPushListener>(512, 0.9F);

        // Parse auth data
        {
            ConfigurationService service = services.getService(ConfigurationService.class);
            String authLogin = service.getProperty("com.openexchange.rest.services.basic-auth.login");
            String authPassword = service.getProperty("com.openexchange.rest.services.basic-auth.password");
            if (Strings.isEmpty(authLogin) || Strings.isEmpty(authPassword)) {
                LOGGER.error("Denied initialization due to unset Basic-Auth configuration. Please set properties 'com.openexchange.rest.services.basic-auth.login' and 'com.openexchange.rest.services.basic-auth.password' appropriately.");
                throw ServiceExceptionCode.absentService(ConfigurationService.class);
            }
            this.authLogin = authLogin.trim();
            this.authPassword = authPassword.trim();
        }

        // Parse URL
        if (Strings.isEmpty(endPoint)) {
            uri = null;
        } else {
            try {
                uri = new URI(endPoint);
            } catch (URISyntaxException e) {
                throw PushExceptionCodes.UNEXPECTED_ERROR.create(e, null == endPoint ? "<empty>" : endPoint);
            }
        }
    }

    @Override
    public boolean supportsPermanentListeners() {
        return true;
    }

    /**
     * Tries to look-up another valid session and injects a new listener for it (discarding the existing one bound to given <code>oldSession</code>)
     *
     * @param oldSession The expired/outdated session
     * @return The new listener or <code>null</code>
     * @throws OXException If operation fails
     */
    public DovecotPushListener injectAnotherListenerFor(Session oldSession) {
        int contextId = oldSession.getContextId();
        int userId = oldSession.getUserId();

        // Prefer permanent listener prior to performing look-up for another valid session
        if (hasPermanentPush(userId, contextId)) {
            try {
                Session session = generateSessionFor(userId, contextId);
                return injectAnotherListenerUsing(session, true);
            } catch (OXException e) {
                // Failed to inject a permanent listener
            }
        }

        // Look-up sessions
        SessiondService sessiondService = services.getService(SessiondService.class);
        if (null != sessiondService) {
            String oldSessionId = oldSession.getSessionID();

            // Query local ones first
            Collection<Session> sessions = sessiondService.getSessions(userId, contextId);
            for (Session session : sessions) {
                if (!oldSessionId.equals(session.getSessionID()) && PushUtility.allowedClient(session.getClient(), session, true)) {
                    return injectAnotherListenerUsing(session, false);
                }
            }

            // Look-up remote sessions, too, if possible
            if (sessiondService instanceof SessiondServiceExtended) {
                sessions = ((SessiondServiceExtended) sessiondService).getSessions(userId, contextId, true);
                for (Session session : sessions) {
                    if (!oldSessionId.equals(session.getSessionID()) && PushUtility.allowedClient(session.getClient(), session, true)) {
                        return injectAnotherListenerUsing(session, false);
                    }
                }
            }
        }

        return null;
    }

    /**
     * Tries to look-up another valid session and injects a new listener for it (discarding the existing one bound to given <code>oldSession</code>)
     *
     * @param newSession The new session to use
     * @param permanent <code>true</code> if permanent; otherwise <code>false</code>
     * @return The new listener or <code>null</code>
     * @throws OXException If operation fails
     */
    public DovecotPushListener injectAnotherListenerUsing(Session newSession, boolean permanent) {
        synchronized (this) {
            DovecotPushListener listener = new DovecotPushListener(uri, authLogin, authPassword, newSession, permanent, this, services);
            // Replace old/existing one
            listeners.put(SimpleKey.valueOf(newSession), listener);
            return listener;
        }
    }

    private void stopAll() {
        synchronized (this) {
            for (Map.Entry<SimpleKey, DovecotPushListener> entry : listeners.entrySet()) {
                try {
                    entry.getValue().unregister(false);
                } catch (Exception e) {
                    SimpleKey key = entry.getKey();
                    LOGGER.warn("Failed to stop listener for user {} in context {}", key.userId, key.contextId);
                }
            }
            listeners.clear();
        }
    }

    /**
     * Stops the listener associated with given user.
     *
     * @param tryToReconnect <code>true</code> to signal that a reconnect using another sessions should be performed; otherwise <code>false</code>
     * @param stopIfPermanent <code>true</code> to signal that current listener is supposed to be stopped even though it might be associated with a permanent push registration; otherwise <code>false</code>
     * @param userId The user identifier
     * @param contextId The corresponding context identifier
     * @return The stop result
     * @throws OXException If unregistration attempt fails
     */
    public StopResult stopListener(boolean tryToReconnect, boolean stopIfPermanent, int userId, int contextId) throws OXException {
        synchronized (this) {
            SimpleKey key = SimpleKey.valueOf(userId, contextId);
            DovecotPushListener listener = listeners.get(key);
            if (null != listener) {
                if (!stopIfPermanent && listener.isPermanent()) {
                    return StopResult.NONE;
                }

                // Remove from map
                listeners.remove(key);

                boolean tryRecon = tryToReconnect || (!listener.isPermanent() && hasPermanentPush(userId, contextId));
                boolean reconnected = listener.unregister(tryRecon);
                if (!reconnected) {
                    return StopResult.STOPPED;
                }

                DovecotPushListener newListener = listeners.get(key);
                return (null != newListener && newListener.isPermanent()) ? StopResult.RECONNECTED_AS_PERMANENT : StopResult.RECONNECTED;
            }
            return StopResult.NONE;
        }
    }

    /**
     * Releases the possibly held lock for given user.
     *
     * @param sessionInfo The associated session
     * @throws OXException If release operation fails
     */
    public void releaseLock(SessionInfo sessionInfo) throws OXException {
        clusterLock.releaseLock(sessionInfo);
    }

    /**
     * Refreshes the lock for given user.
     *
     * @param sessionInfo The associated session
     * @throws OXException If refresh operation fails
     */
    public void refreshLock(SessionInfo sessionInfo) throws OXException {
        clusterLock.refreshLock(sessionInfo);
    }

    // --------------------------------------------------------------------------------------------------------------------------

    @Override
    public PushListener startListener(Session session) throws OXException {
        if (null == session) {
            return null;
        }
        int contextId = session.getContextId();
        int userId = session.getUserId();
        SessionInfo sessionInfo = new SessionInfo(session, false);
        if (clusterLock.acquireLock(sessionInfo)) {
            synchronized (this) {
                // Locked...
                boolean unlock = true;
                boolean removeListener = false;
                SimpleKey key = SimpleKey.valueOf(userId, contextId);
                try {
                    if (false == listeners.containsKey(key)) {
                        DovecotPushListener listener = new DovecotPushListener(uri, authLogin, authPassword, session, false, this, services);
                        listeners.put(key, listener);
                        removeListener = true;
                        String reason = listener.initateRegistration();
                        if (null == reason) {
                            removeListener = false;
                            unlock = false;
                            LOGGER.info("Started Dovecot listener for user {} in context {} with session {} ({})", I(userId), I(contextId), session.getSessionID(), session.getClient());
                            return listener;
                        }

                        // Registration failed
                        LOGGER.info("Could not register Dovecot listener for user {} in context {} with session {} ({})", I(userId), I(contextId), session.getSessionID(), session.getClient());
                    } else {
                        // Already running for session user
                        LOGGER.info("Did not start Dovecot listener for user {} in context {} with session {} ({}) as there is already an associated listener", I(userId), I(contextId), session.getSessionID(), session.getClient());
                    }
                } finally {
                    if (removeListener) {
                        listeners.remove(key);
                    }
                    if (unlock) {
                        releaseLock(sessionInfo);
                    }
                }
            }
        } else {
            LOGGER.info("Could not acquire lock to start Dovecot listener for user {} in context {} with session {} ({}) as there is already an associated listener", I(userId), I(contextId), session.getSessionID(), session.getClient());
        }

        // No listener registered for given session
        return null;
    }

    @Override
    public boolean stopListener(Session session) throws OXException {
        if (null == session) {
            return false;
        }

        StopResult stopResult = stopListener(true, false, session.getUserId(), session.getContextId());
        switch (stopResult) {
        case RECONNECTED:
            LOGGER.info("Reconnected Dovecot listener for user {} in context {} using another session", I(session.getUserId()), I(session.getContextId()));
            return true;
        case RECONNECTED_AS_PERMANENT:
            LOGGER.info("Reconnected as permanent Dovecot listener for user {} in context {}", I(session.getUserId()), I(session.getContextId()));
            return true;
        case STOPPED:
            LOGGER.info("Stopped Dovecot listener for user {} in context {} with session {}", I(session.getUserId()), I(session.getContextId()), session.getSessionID());
            return true;
        default:
            break;
        }

        return false;
    }

    // --------------------------------------------------------------------------------------------------------------------------

    private boolean hasPermanentPush(int userId, int contextId) {
        try {
            PushListenerService pushListenerService = services.getService(PushListenerService.class);
            return pushListenerService.hasRegistration(new PushUser(userId, contextId));
        } catch (Exception e) {
            LOGGER.warn("Failed to check for push registration for user {} in context {}", I(userId), I(contextId), e);
            return false;
        }
    }

    private Session generateSessionFor(int userId, int contextId) throws OXException {
        PushListenerService pushListenerService = services.getService(PushListenerService.class);
        return pushListenerService.generateSessionFor(new PushUser(userId, contextId));
    }

    private Session generateSessionFor(PushUser pushUser) throws OXException {
        PushListenerService pushListenerService = services.getService(PushListenerService.class);
        return pushListenerService.generateSessionFor(pushUser);
    }

    @Override
    public PushListener startPermanentListener(PushUser pushUser) throws OXException {
        if (null == pushUser) {
            return null;
        }

        Session session = generateSessionFor(pushUser);
        int contextId = session.getContextId();
        int userId = session.getUserId();

        SessionInfo sessionInfo = new SessionInfo(session, true);
        if (clusterLock.acquireLock(sessionInfo)) {
            synchronized (this) {
                // Locked...
                boolean unlock = true;
                boolean removeListener = false;
                SimpleKey key = SimpleKey.valueOf(userId, contextId);
                try {
                    DovecotPushListener current = listeners.get(key);
                    if (null == current) {
                        DovecotPushListener listener = new DovecotPushListener(uri, authLogin, authPassword, session, true, this, services);
                        listeners.put(key, listener);
                        removeListener = true;
                        String reason = listener.initateRegistration();
                        if (null == reason) {
                            removeListener = false;
                            unlock = false;
                            LOGGER.info("Started permanent Dovecot listener for user {} in context {}", I(userId), I(contextId));
                            return listener;
                        }

                        // Registration failed
                        LOGGER.info("Could not register permanent Dovecot listener for user {} in context {} with session {} ({})", I(userId), I(contextId), session.getSessionID(), session.getClient());
                    } else if (!current.isPermanent()) {
                        // Cancel current & replace
                        current.unregister(false);
                        DovecotPushListener listener = new DovecotPushListener(uri, authLogin, authPassword, session, true, this, services);
                        listeners.put(key, listener);
                        removeListener = true;
                        String reason = listener.initateRegistration();
                        if (null == reason) {
                            removeListener = false;
                            unlock = false;
                            LOGGER.info("Started permanent Dovecot listener for user {} in context {}", I(userId), I(contextId));
                            return listener;
                        }

                        // Registration failed
                        LOGGER.info("Could not register permanent Dovecot listener for user {} in context {} with session {} ({})", I(userId), I(contextId), session.getSessionID(), session.getClient());
                    } else {
                        // Already running for session user
                        LOGGER.info("Did not start permanent Dovecot listener for user {} in context {} with session {} ({}) as there is already an associated listener", I(userId), I(contextId), session.getSessionID(), session.getClient());
                    }
                } finally {
                    if (removeListener) {
                        listeners.remove(key);
                    }
                    if (unlock) {
                        releaseLock(sessionInfo);
                    }
                }
            }
        } else {
            LOGGER.info("Could not acquire lock to start permanent Dovecot listener for user {} in context {} with session {} ({}) as there is already an associated listener", I(userId), I(contextId), session.getSessionID(), session.getClient());
        }

        // No listener registered for given session
        return null;
    }

    @Override
    public boolean stopPermanentListener(PushUser pushUser, boolean tryToReconnect) throws OXException {
        if (null == pushUser) {
            return false;
        }

        StopResult stopResult = stopListener(tryToReconnect, true, pushUser.getUserId(), pushUser.getContextId());
        switch (stopResult) {
        case RECONNECTED:
            LOGGER.info("Reconnected permanent Dovecot listener for user {} in context {}", I(pushUser.getUserId()), I(pushUser.getContextId()));
            return true;
        case STOPPED:
            LOGGER.info("Stopped permanent Dovecot listener for user {} in context {}", I(pushUser.getUserId()), I(pushUser.getContextId()));
            return true;
        default:
            break;
        }

        return false;
    }

    @Override
    public List<PushUserInfo> getAvailablePushUsers() throws OXException {
        List<PushUserInfo> l = new LinkedList<PushUserInfo>();
        for (Map.Entry<SimpleKey, DovecotPushListener> entry : listeners.entrySet()) {
            SimpleKey key = entry.getKey();
            l.add(new PushUserInfo(new PushUser(key.userId, key.contextId), entry.getValue().isPermanent()));
        }
        return l;
    }

}
