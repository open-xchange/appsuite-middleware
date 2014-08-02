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

package com.openexchange.push.imapidlev2;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.slf4j.Logger;
import com.openexchange.exception.OXException;
import com.openexchange.push.PushListener;
import com.openexchange.push.PushManagerService;
import com.openexchange.push.PushUtility;
import com.openexchange.push.imapidlev2.ImapIdlePushListener.PushMode;
import com.openexchange.push.imapidlev2.locking.ImapIdleClusterLock;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.sessiond.SessiondService;


/**
 * {@link ImapIdlePushManagerService} - The IMAP IDLE push manager.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since 7.6.1
 */
public final class ImapIdlePushManagerService implements PushManagerService {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(ImapIdlePushManagerService.class);

    private static enum StopResult {
        NONE, RECONNECTED, STOPPED;
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

    public static ImapIdlePushManagerService newInstance(ImapIdleConfiguration configuration, ServiceLookup services) {
        ImapIdlePushManagerService tmp = new ImapIdlePushManagerService(configuration.getFullName(), configuration.getAccountId(), configuration.getPushMode(), configuration.getDelay(), configuration.getClusterLock(), services);
        instance = tmp;
        return tmp;
    }

    // ---------------------------------------------------------------------------------------------------- //

    private final ServiceLookup services;
    private final ConcurrentMap<SimpleKey, ImapIdlePushListener> listeners;
    private final String fullName;
    private final int accountId;
    private final PushMode pushMode;
    private final ImapIdleClusterLock clusterLock;
    private final long delay;

    /**
     * Initializes a new {@link ImapIdlePushManagerService}.
     */
    private ImapIdlePushManagerService(String fullName, int accountId, PushMode pushMode, long delay, ImapIdleClusterLock clusterLock, ServiceLookup services) {
        super();
        this.pushMode = pushMode;
        this.delay = delay;
        this.fullName = fullName;
        this.accountId = accountId;
        this.clusterLock = clusterLock;
        this.services = services;
        listeners = new ConcurrentHashMap<SimpleKey, ImapIdlePushListener>(512);
    }

    /**
     * Gets the account identifier
     *
     * @return The account identifier
     */
    public int getAccountId() {
        return accountId;
    }

    @Override
    public PushListener startListener(Session session) throws OXException {
        if (null == session) {
            return null;
        }
        int contextId = session.getContextId();
        int userId = session.getUserId();
        if (clusterLock.acquireLock(session)) {
            // Locked...
            boolean unlock = true;
            try {
                ImapIdlePushListener listener = new ImapIdlePushListener(fullName, accountId, pushMode, delay, session, services);
                if (null == listeners.putIfAbsent(SimpleKey.valueOf(userId, contextId), listener)) {
                    listener.start();
                    unlock = false;
                    LOGGER.info("Started IMAP-IDLE listener for user {} in context {} with session {}", userId, contextId, session.getSessionID());
                    return listener;
                }

                // Already running for session user
                LOGGER.info("Did not start IMAP-IDLE listener for user {} in context {} with session {} as there is already another one using another session", userId, contextId, session.getSessionID());
            } finally {
                if (unlock) {
                    releaseLock(session);
                }
            }
        } else {
            LOGGER.info("Could not acquire lock to start IMAP-IDLE listener for user {} in context {} with session {}", userId, contextId, session.getSessionID());
        }

        // No listener registered for given session
        return null;
    }

    @Override
    public boolean stopListener(Session session) throws OXException {
        if (null == session) {
            return false;
        }

        StopResult stopResult = stopListener(true, session.getUserId(), session.getContextId());
        switch (stopResult) {
        case RECONNECTED:
            LOGGER.info("Reconnected IMAP-IDLE listener for user {} in context {} using another session", session.getUserId(), session.getContextId());
            return true;
        case STOPPED:
            LOGGER.info("Stopped IMAP-IDLE listener for user {} in context {} with session {}", session.getUserId(), session.getContextId(), session.getSessionID());
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
     * @param userId The user identifier
     * @param contextId The corresponding context identifier
     * @return The stop result
     */
    public StopResult stopListener(boolean tryToReconnect, int userId, int contextId) {
        ImapIdlePushListener listener = listeners.remove(SimpleKey.valueOf(userId, contextId));
        if (null != listener) {
            boolean reconnected = listener.cancel(tryToReconnect);
            return reconnected ? StopResult.RECONNECTED : StopResult.STOPPED;
        }

        return StopResult.NONE;
    }

    /**
     * Releases the possibly held lock for given user.
     *
     * @param session The associated session
     * @throws OXException If release operation fails
     */
    public void releaseLock(Session session) throws OXException {
        clusterLock.releaseLock(session);
    }

    /**
     * Refreshes the lock for given user.
     *
     * @param session The associated session
     * @throws OXException If refresh operation fails
     */
    public void refreshLock(Session session) throws OXException {
        clusterLock.refreshLock(session);
    }

    /**
     * Tries to look-up another valid session and injects a new listener for it (discarding the existing one bound to given <code>oldSession</code>)
     *
     * @param oldSession The expired/outdated session
     * @return The new listener or <code>null</code>
     * @throws OXException If operation fails
     */
    public ImapIdlePushListener injectAnotherListenerFor(Session oldSession) {
        SessiondService sessiondService = services.getService(SessiondService.class);
        if (null != sessiondService) {
            int contextId = oldSession.getContextId();
            int userId = oldSession.getUserId();
            Collection<Session> sessions = sessiondService.getSessions(userId, contextId);
            String oldSessionId = oldSession.getSessionID();
            for (Session session : sessions) {
                if (!oldSessionId.equals(session.getSessionID()) && PushUtility.allowedClient(session.getClient())) {
                    ImapIdlePushListener listener = new ImapIdlePushListener(fullName, accountId, pushMode, delay, session, services);
                    // Replace old/existing one
                    listeners.put(SimpleKey.valueOf(userId, contextId), listener);
                    return listener;
                }
            }
        }
        return null;
    }

}
