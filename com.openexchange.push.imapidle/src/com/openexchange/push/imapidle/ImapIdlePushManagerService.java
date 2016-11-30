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

package com.openexchange.push.imapidle;

import static com.openexchange.java.Autoboxing.I;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.push.PushListener;
import com.openexchange.push.PushListenerService;
import com.openexchange.push.PushManagerExtendedService;
import com.openexchange.push.PushUser;
import com.openexchange.push.PushUserInfo;
import com.openexchange.push.PushUtility;
import com.openexchange.push.imapidle.ImapIdlePushListener.PushMode;
import com.openexchange.push.imapidle.control.ImapIdleControl;
import com.openexchange.push.imapidle.control.ImapIdleControlTask;
import com.openexchange.push.imapidle.locking.ImapIdleClusterLock;
import com.openexchange.push.imapidle.locking.SessionInfo;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.sessiond.SessiondServiceExtended;
import com.openexchange.timer.ScheduledTimerTask;
import com.openexchange.timer.TimerService;


/**
 * {@link ImapIdlePushManagerService} - The IMAP IDLE push manager.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since 7.6.1
 */
public final class ImapIdlePushManagerService implements PushManagerExtendedService {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(ImapIdlePushManagerService.class);

    private static enum StopResult {
        NONE, RECONNECTED, RECONNECTED_AS_PERMANENT, STOPPED;
    }

    private static volatile ImapIdlePushManagerService instance;

    /**
     * Gets the instance
     *
     * @return The instance or <code>null</code> if not initialized
     */
    public static ImapIdlePushManagerService getInstance() {
        return instance;
    }

    public static ImapIdlePushManagerService newInstance(ImapIdleConfiguration configuration, ServiceLookup services) throws OXException {
        ImapIdlePushManagerService tmp = new ImapIdlePushManagerService(configuration.getFullName(), configuration.getAccountId(), configuration.getPushMode(), configuration.getDelay(), configuration.getClusterLock(), services);
        instance = tmp;
        return tmp;
    }

    public static boolean isTransient(Session session, ServiceLookup services) {
        SessiondService service = services.getService(SessiondService.class);
        return (service instanceof SessiondServiceExtended) ? false == ((SessiondServiceExtended) service).isApplicableForSessionStorage(session) : false;
    }

    // ---------------------------------------------------------------------------------------------------- //

    private final String name;
    private final ServiceLookup services;
    private final ImapIdleControl control;
    private final ConcurrentMap<SimpleKey, ImapIdlePushListener> listeners;
    private final String fullName;
    private final int accountId;
    private final PushMode pushMode;
    private final ImapIdleClusterLock clusterLock;
    private final long delay;
    private final ScheduledTimerTask timerTask;

    /**
     * Initializes a new {@link ImapIdlePushManagerService}.
     */
    private ImapIdlePushManagerService(String fullName, int accountId, PushMode pushMode, long delay, ImapIdleClusterLock clusterLock, ServiceLookup services) throws OXException {
        super();
        name = "IMAP-IDLE Push Manager";
        this.pushMode = pushMode;
        this.delay = delay;
        this.fullName = fullName;
        this.accountId = accountId;
        this.clusterLock = clusterLock;
        this.services = services;
        this.control = new ImapIdleControl();
        listeners = new ConcurrentHashMap<SimpleKey, ImapIdlePushListener>(512, 0.9f, 1);

        // Initialize timer task to check for expired IMAP-IDLE push listeners for every 30 seconds
        TimerService timerService = services.getOptionalService(TimerService.class);
        if (null == timerService) {
            throw ServiceExceptionCode.absentService(TimerService.class);
        }
        timerTask = timerService.scheduleWithFixedDelay(new ImapIdleControlTask(control), 30, 30, TimeUnit.SECONDS);
    }

    /**
     * Shuts-down this IMAP-IDLE push manager.
     */
    public void shutDown() {
        timerTask.cancel();
    }

    @Override
    public String toString() {
        return name;
    }

    /**
     * Gets the account identifier
     *
     * @return The account identifier
     */
    public int getAccountId() {
        return accountId;
    }

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

    // ----------------------------------------------------------------------------------------------------------------------------------

    @Override
    public List<PushUserInfo> getAvailablePushUsers() throws OXException {
        List<PushUserInfo> l = new LinkedList<PushUserInfo>();
        for (Map.Entry<SimpleKey, ImapIdlePushListener> entry : listeners.entrySet()) {
            SimpleKey key = entry.getKey();
            l.add(new PushUserInfo(new PushUser(key.userId, key.contextId), entry.getValue().isPermanent()));
        }
        return l;
    }

    @Override
    public boolean supportsPermanentListeners() {
        boolean defaultValue = false;
        ConfigurationService service = services.getOptionalService(ConfigurationService.class);
        return null == service ? defaultValue : service.getBoolProperty("com.openexchange.push.imapidle.supportsPermanentListeners", defaultValue);
    }

    @Override
    public PushListener startPermanentListener(PushUser pushUser) throws OXException {
        if (null == pushUser) {
            return null;
        }

        Session session = generateSessionFor(pushUser);
        int contextId = session.getContextId();
        int userId = session.getUserId();

        SessionInfo sessionInfo = new SessionInfo(session, true, false);
        if (clusterLock.acquireLock(sessionInfo)) {
            synchronized (this) {
                // Locked...
                boolean unlock = true;
                try {
                    ImapIdlePushListener listener = new ImapIdlePushListener(fullName, accountId, pushMode, delay, session, true, supportsPermanentListeners(), control, services);
                    ImapIdlePushListener current = listeners.putIfAbsent(SimpleKey.valueOf(userId, contextId), listener);
                    if (null == current) {
                        listener.start();
                        unlock = false;
                        LOGGER.info("Started permanent IMAP-IDLE listener for user {} in context {}", I(userId), I(contextId));
                        return listener;
                    } else if (!current.isPermanent()) {
                        // Cancel current & replace
                        current.cancel(false);
                        listeners.put(SimpleKey.valueOf(userId, contextId), listener);
                        listener.start();
                        unlock = false;
                        LOGGER.info("Started permanent IMAP-IDLE listener for user {} in context {}", I(userId), I(contextId));
                        return listener;
                    }

                    // Already running for session user
                    LOGGER.info("Did not start permanent IMAP-IDLE listener for user {} in context {} with session {} as there is already an associated listener", I(userId), I(contextId), session.getSessionID());
                } finally {
                    if (unlock) {
                        releaseLock(sessionInfo);
                    }
                }
            }
        } else {
            LOGGER.info("Could not acquire lock to start IMAP-IDLE listener for user {} in context {} with session {} as there is already an associated listener", I(userId), I(contextId), session.getSessionID());
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
            LOGGER.info("Reconnected permanent IMAP-IDLE listener for user {} in context {}", I(pushUser.getUserId()), I(pushUser.getContextId()));
            return true;
        case STOPPED:
            LOGGER.info("Stopped permanent IMAP-IDLE listener for user {} in context {}", I(pushUser.getUserId()), I(pushUser.getContextId()));
            return true;
        default:
            break;
        }

        return false;
    }

    // ----------------------------------------------------------------------------------------------------------------------------------

    @Override
    public PushListener startListener(Session session) throws OXException {
        if (null == session) {
            return null;
        }
        int contextId = session.getContextId();
        int userId = session.getUserId();

        SessionInfo sessionInfo = new SessionInfo(session, false, isTransient(session, services));
        if (clusterLock.acquireLock(sessionInfo)) {
            synchronized (this) {
                // Locked...
                boolean unlock = true;
                try {
                    ImapIdlePushListener listener = new ImapIdlePushListener(fullName, accountId, pushMode, delay, session, false, supportsPermanentListeners(), control, services);
                    if (null == listeners.putIfAbsent(SimpleKey.valueOf(userId, contextId), listener)) {
                        listener.start();
                        unlock = false;
                        LOGGER.info("Started IMAP-IDLE listener for user {} in context {} with session {} ({})", I(userId), I(contextId), session.getSessionID(), session.getClient());
                        return listener;
                    }

                    // Already running for session user
                    LOGGER.info("Did not start IMAP-IDLE listener for user {} in context {} with session {} ({}) as there is already an associated listener", I(userId), I(contextId), session.getSessionID(), session.getClient());
                } finally {
                    if (unlock) {
                        releaseLock(sessionInfo);
                    }
                }
            }
        } else {
            LOGGER.info("Could not acquire lock to start IMAP-IDLE listener for user {} in context {} with session {} ({}) as there is already an associated listener", I(userId), I(contextId), session.getSessionID(), session.getClient());
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
            LOGGER.info("Reconnected IMAP-IDLE listener for user {} in context {} using another session", I(session.getUserId()), I(session.getContextId()));
            return true;
        case RECONNECTED_AS_PERMANENT:
            LOGGER.info("Reconnected as permanent IMAP-IDLE listener for user {} in context {}", I(session.getUserId()), I(session.getContextId()));
            return true;
        case STOPPED:
            LOGGER.info("Stopped IMAP-IDLE listener for user {} in context {} with session {}", I(session.getUserId()), I(session.getContextId()), session.getSessionID());
            return true;
        default:
            break;
        }

        return false;
    }

    /**
     * Stops the listener associated with given user.
     *
     * @param tryToReconnect <code>true</code> to signal that a reconnect using another sessions should be performed; otherwise <code>false</code>
     * @param stopIfPermanent <code>true</code> to signal that current listener is supposed to be stopped even though it might be associated with a permanent push registration; otherwise <code>false</code>
     * @param userId The user identifier
     * @param contextId The corresponding context identifier
     * @return The stop result
     */
    public StopResult stopListener(boolean tryToReconnect, boolean stopIfPermanent, int userId, int contextId) {
        synchronized (this) {
            SimpleKey key = SimpleKey.valueOf(userId, contextId);
            ImapIdlePushListener listener = listeners.get(key);
            if (null != listener) {
                if (!stopIfPermanent && listener.isPermanent()) {
                    return StopResult.NONE;
                }

                // Remove from map
                listeners.remove(key);

                boolean tryRecon = tryToReconnect || (!listener.isPermanent() && hasPermanentPush(userId, contextId));
                boolean reconnected = listener.cancel(tryRecon);
                if (!reconnected) {
                    return StopResult.STOPPED;
                }

                ImapIdlePushListener newListener = listeners.get(key);
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

    /**
     * Tries to look-up another valid session and injects a new listener for it (discarding the existing one bound to given <code>oldSession</code>)
     *
     * @param oldSession The expired/outdated session
     * @return The new listener or <code>null</code>
     * @throws OXException If operation fails
     */
    public ImapIdlePushListener injectAnotherListenerFor(Session oldSession) {
        int contextId = oldSession.getContextId();
        int userId = oldSession.getUserId();

        // Prefer permanent listener prior to performing look-up for another valid session
        if (hasPermanentPush(userId, contextId)) {
            try {
                Session session = generateSessionFor(userId, contextId);
                return injectAnotherListenerUsing(session, true).injectedPushListener;
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
                    return injectAnotherListenerUsing(session, false).injectedPushListener;
                }
            }

            // Look-up remote sessions, too, if possible
            if (sessiondService instanceof SessiondServiceExtended) {
                sessions = ((SessiondServiceExtended) sessiondService).getSessions(userId, contextId, true);
                for (Session session : sessions) {
                    if (!oldSessionId.equals(session.getSessionID()) && PushUtility.allowedClient(session.getClient(), session, true)) {
                        return injectAnotherListenerUsing(session, false).injectedPushListener;
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
    public InjectedImapIdlePushListener injectAnotherListenerUsing(Session newSession, boolean permanent) {
        ImapIdlePushListener listener = new ImapIdlePushListener(fullName, accountId, pushMode, delay, newSession, permanent, supportsPermanentListeners(), control, services);
        // Replace old/existing one
        ImapIdlePushListener prev = listeners.put(SimpleKey.valueOf(newSession), listener);
        return new InjectedImapIdlePushListener(listener, prev);
    }

    /**
     * Stops all listeners.
     */
    public void stopAllListeners() {
        for (Iterator<ImapIdlePushListener> it = listeners.values().iterator(); it.hasNext();) {
            ImapIdlePushListener listener = it.next();
            try {
                listener.cancel(false);
            } catch (Exception e) {
                // Ignore
            }
            it.remove();
        }
    }

    // -------------------------------------------------------------------------------------------------------------------------------

    private static class InjectedImapIdlePushListener {

        final ImapIdlePushListener injectedPushListener;
        final ImapIdlePushListener replacedPushListener;

        InjectedImapIdlePushListener(ImapIdlePushListener injectedPushListener, ImapIdlePushListener replacedPushListener) {
            super();
            this.injectedPushListener = injectedPushListener;
            this.replacedPushListener = replacedPushListener;
        }
    }

}
