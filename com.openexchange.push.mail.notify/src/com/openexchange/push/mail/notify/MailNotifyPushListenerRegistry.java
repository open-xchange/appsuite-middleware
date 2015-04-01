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

package com.openexchange.push.mail.notify;

import static com.openexchange.java.Autoboxing.I;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.java.Strings;
import com.openexchange.push.PushListener;
import com.openexchange.push.PushListenerService;
import com.openexchange.push.PushUser;
import com.openexchange.push.PushUtility;
import com.openexchange.push.mail.notify.osgi.Services;
import com.openexchange.push.mail.notify.util.DelayedNotification;
import com.openexchange.push.mail.notify.util.MailNotifyDelayQueue;
import com.openexchange.session.Session;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.sessiond.SessiondServiceExtended;
import com.openexchange.timer.ScheduledTimerTask;
import com.openexchange.timer.TimerService;

/**
 * {@link MailNotifyPushListenerRegistry} - The registry for {@code MailNotifyPushListener}s.
 *
 */
public final class MailNotifyPushListenerRegistry {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(MailNotifyPushListenerRegistry.class);

    private static enum StopResult {
        NONE, RECONNECTED, STOPPED;
    }

    // ---------------------------------------------------------------------------------------------------------------------------------

    private final ConcurrentMap<String, MailNotifyPushListener> mboxId2Listener;
    private final boolean useOXLogin;
    private final boolean useEmailAddress;
    private final MailNotifyDelayQueue notificationsQueue;
    private final ScheduledTimerTask timerTask;

    /**
     * Initializes a new {@link MailNotifyPushListenerRegistry}.
     */
    public MailNotifyPushListenerRegistry(boolean useOXLogin, boolean useEmailAddress) {
        super();
        mboxId2Listener = new ConcurrentHashMap<String, MailNotifyPushListener>();
        this.useOXLogin = useOXLogin;
        this.useEmailAddress = useEmailAddress;
        notificationsQueue = new MailNotifyDelayQueue();

        // Timer task
        TimerService timerService = Services.optService(TimerService.class);
        final org.slf4j.Logger log = LOG;
        Runnable r = new Runnable() {

            @Override
            public void run() {
                try {
                    triggerDueNotifications();
                } catch (final Exception e) {
                    log.warn("Failed to trigger notifications.", e);
                }
            }
        };
        int delay = 3000;
        timerTask = timerService.scheduleWithFixedDelay(r, delay, delay);
    }

    private boolean hasPermanentPush(int userId, int contextId) {
        try {
            PushListenerService pushListenerService = Services.getService(PushListenerService.class, true);
            return pushListenerService.hasRegistration(new PushUser(userId, contextId));
        } catch (Exception e) {
            LOG.warn("Failed to check for push registration for user {} in context {}", I(userId), I(contextId), e);
            return false;
        }
    }

    private Session generateSessionFor(int userId, int contextId) throws OXException {
        PushListenerService pushListenerService = Services.getService(PushListenerService.class, true);
        return pushListenerService.generateSessionFor(new PushUser(userId, contextId));
    }

    /**
     * Cancels the timer.
     */
    public void cancel() {
        timerTask.cancel();
    }

    // ------------------------------------------------------- UDP event handling -------------------------------------------------------

    /**
     * Schedules to notify specified mailbox identifier (if not yet scheduled).
     *
     * @param mboxid The mailbox identifier
     */
    public void scheduleEvent(String mboxid) {
        notificationsQueue.offerIfAbsent(new DelayedNotification(mboxid, false));
        triggerDueNotifications();
    }

    /**
     * Triggers all due notifications.
     */
    public synchronized void triggerDueNotifications() {
        DelayedNotification polled = notificationsQueue.poll();
        if (null != polled) {
            // Collect due notifications
            List<String> mboxIds = new LinkedList<String>();
            do {
                mboxIds.add(polled.getMboxid());
                polled = notificationsQueue.poll();
            } while (polled != null);

            // Fire event for collected due notifications
            notifyNow(mboxIds);
        }
    }

    /**
     * (Immediately) Notifies specified mailbox identifiers.
     *
     * @param mboxIds The mailbox identifiers to notify
     */
    private void notifyNow(Collection<String> mboxIds) {
        if (false == mboxIds.isEmpty()) {
            for (String mboxId : mboxIds) {
                try {
                    fireEvent(mboxId);
                } catch (Exception e) {
                    LOG.error("Failed firing push event", e);
                }
            }
        }
    }

    /**
     * If the given mailbox identifier has been registered for receiving events, fire event...
     *
     * @param mboxid The mailbox identifier
     * @throws OXException If firing event fails
     */
    private void fireEvent(String mboxid) throws OXException {
        LOG.debug("Checking whether to fire event for {}", mboxid);
        PushListener listener = mboxId2Listener.get(mboxid);
        if (null != listener) {
            LOG.debug("fireEvent, mboxid={}", mboxid);
            listener.notifyNewMail();
        }
    }

    // --------------------------------------------------- End of UDP event handling ---------------------------------------------------

    // ------------------------------------------------------ Listener management ------------------------------------------------------

    /**
     * Clears this registry.
     */
    public void clear() {
        mboxId2Listener.clear();
    }

    /**
     * Adds specified push listener.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param pushListener The push listener to add
     * @return <code>true</code> if push listener service could be successfully added; otherwise <code>false</code>
     */
    public boolean addPushListener(int userId, int contextId, MailNotifyPushListener pushListener) throws OXException {
        Set<String> mboxIds = getMboxIdsFor(userId, contextId);
        if (mboxIds.isEmpty()) {
            LOG.warn("No resolvable aliases for user {} in context {}", Integer.valueOf(userId), Integer.valueOf(contextId));
            return false;
        }

        boolean notYetPushed = true;
        for (String mboxId : mboxIds) {
            MailNotifyPushListener current = mboxId2Listener.putIfAbsent(mboxId, pushListener);
            if (null == current) {
                LOG.debug("Added UDP-based mail listener {} for user {} in context {}", mboxId, Integer.valueOf(userId), Integer.valueOf(contextId));
            } else {
                boolean replaced = false;

                if (pushListener.isPermanent()) {
                    boolean keepOn;
                    do {
                        if (current.isPermanent()) {
                            keepOn = false;
                        } else {
                            replaced = mboxId2Listener.replace(mboxId, current, pushListener);
                            keepOn = !replaced;
                        }
                    } while (keepOn);
                }

                if (replaced) {
                    LOG.debug("Replaced UDP-based mail listener {} for user {} in context {}", mboxId, Integer.valueOf(userId), Integer.valueOf(contextId));
                } else {
                    // Listener wasn't put into map
                    LOG.debug("UDP-based mail listener {} was not put into map (as already present) for user {} in context {}", mboxId, Integer.valueOf(userId), Integer.valueOf(contextId));
                    if (notYetPushed) {
                        notYetPushed = false;
                    }
                }
            }
        }
        return notYetPushed;
    }

    /**
     * Stops push listener for specified user.
     *
     * @param tryToReconnect <code>true</code> to signal that a reconnect using another sessions should be performed; otherwise <code>false</code>
     * @param stopIfPermanent <code>true</code> to signal that current listener is supposed to be stopped even though it might be associated with a permanent push registration; otherwise <code>false</code>
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return <code>true</code> if listener has been successfully stopped; otherwise <code>false</code>
     * @throws OXException If stop attempt fails
     */
    public boolean stopPushListener(boolean tryToReconnect, boolean stopIfPermanent, int userId, int contextId) throws OXException {
        Set<String> mboxIds = getMboxIdsFor(userId, contextId);
        if (mboxIds.isEmpty()) {
            LOG.warn("No resolvable aliases for user {} in context {}", Integer.valueOf(userId), Integer.valueOf(contextId));
            return false;
        }

        boolean stopped = false;
        for (String mboxId : mboxIds) {
            StopResult stopResult = stopListener(tryToReconnect, stopIfPermanent, mboxId, userId, contextId);

            stopped |= (StopResult.STOPPED == stopResult);

            switch (stopResult) {
            case RECONNECTED:
                LOG.info("Reconnected UDP-based mail listener {} for user {} in context {} using another session", mboxId, I(userId), I(contextId));
                return true;
            case STOPPED:
                LOG.info("Stopped UDP-based mail listener {} for user {} in context {}", mboxId, I(userId), I(contextId));
                return true;
            default:
                break;
            }

        }

        return stopped;
    }

    /**
     * Stops the listener associated with given user.
     *
     * @param tryToReconnect <code>true</code> to signal that a reconnect using another sessions should be performed; otherwise <code>false</code>
     * @param stopIfPermanent <code>true</code> to signal that current listener is supposed to be stopped even though it might be associated with a permanent push registration; otherwise <code>false</code>
     * @param mboxId The mailbox identifier
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The stop result
     */
    private StopResult stopListener(boolean tryToReconnect, boolean stopIfPermanent, String mboxId, int userId, int contextId) {
        MailNotifyPushListener listener = mboxId2Listener.remove(mboxId);
        if (null != listener) {
            if (!stopIfPermanent && listener.isPermanent()) {
                mboxId2Listener.put(mboxId, listener);
                return StopResult.NONE;
            }

            boolean reconnected;
            {
                boolean tryRecon = tryToReconnect || (!listener.isPermanent() && hasPermanentPush(userId, contextId));
                if (tryRecon) {
                    MailNotifyPushListener newListener = injectAnotherListenerFor(listener.getSession(), mboxId, userId, contextId);
                    reconnected = null != newListener;
                } else {
                    reconnected = false;
                }
            }

            return reconnected ? StopResult.RECONNECTED : StopResult.STOPPED;
        }

        MailNotifyPushListener newListener = injectAnotherListenerFor(null, mboxId, userId, contextId);
        return null == newListener ? StopResult.STOPPED : StopResult.NONE;
    }

    /**
     * Tries to look-up another valid session and injects a new listener for it (discarding the existing one bound to given <code>oldSession</code>)
     *
     * @param optionalOldSession The expired/outdated session or <code>null</code>
     * @param mboxId The associated mailbox identifier
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The new listener or <code>null</code>
     * @throws OXException If operation fails
     */
    public MailNotifyPushListener injectAnotherListenerFor(Session optionalOldSession, String mboxId, int userId, int contextId) {
        // Prefer permanent listener prior to performing look-up for another valid session
        if (hasPermanentPush(userId, contextId)) {
            try {
                Session session = generateSessionFor(userId, contextId);
                MailNotifyPushListener newListener = MailNotifyPushListener.newInstance(session, true);
                mboxId2Listener.put(mboxId, newListener);
                return newListener;
            } catch (OXException e) {
                // Failed to inject a permanent listener
            }
        }

        // Look-up sessions
        if (null != optionalOldSession) {
            SessiondService sessiondService = Services.optService(SessiondService.class);
            if (null != sessiondService) {
                String oldSessionId = optionalOldSession.getSessionID();

                // Query local ones first
                Collection<Session> sessions = sessiondService.getSessions(userId, contextId);
                for (Session session : sessions) {
                    if (!oldSessionId.equals(session.getSessionID()) && PushUtility.allowedClient(session.getClient())) {
                        MailNotifyPushListener newListener = MailNotifyPushListener.newInstance(session, false);
                        mboxId2Listener.put(mboxId, newListener);
                        return newListener;
                    }
                }

                // Look-up remote sessions, too, if possible
                if (sessiondService instanceof SessiondServiceExtended) {
                    sessions = ((SessiondServiceExtended) sessiondService).getSessions(userId, contextId, true);
                    for (Session session : sessions) {
                        if (!oldSessionId.equals(session.getSessionID()) && PushUtility.allowedClient(session.getClient())) {
                            MailNotifyPushListener newListener = MailNotifyPushListener.newInstance(session, false);
                            mboxId2Listener.put(mboxId, newListener);
                            return newListener;
                        }
                    }
                }
            }
        }

        return null;
    }











    /**
     * Purges specified user's push listener and all of user-associated session identifiers from this registry.
     *
     * @param contextId The context identifier
     * @param userId The user identifier
     * @return <code>true</code> if a push listener for given user-context-pair was found and purged; otherwise <code>false</code>
     * @throws OXException
     */
    public boolean purgeUserPushListener(final int contextId, final int userId) throws OXException {
        return removeListener(getMboxIdsFor(userId, contextId));
    }

    /**
     * Removes specified session identifier associated with given user-context-pair and the push listener as well, if no more
     * user-associated session identifiers are present.
     *
     * @param contextId The context identifier
     * @param userId The user identifier
     * @return <code>true</code> if a push listener for given user-context-pair was found and removed; otherwise <code>false</code>
     * @throws OXException
     */
    public boolean removePushListener(final int contextId, final int userId) throws OXException {
        final SessiondService sessiondService = Services.optService(SessiondService.class);
        if (null == sessiondService || null == sessiondService.getAnyActiveSessionForUser(userId, contextId)) {
            return removeListener(getMboxIdsFor(userId, contextId));
        }
        return false;
    }

    private boolean removeListener(final Collection<String> mboxIds) {
        for(final String id : mboxIds) {
            LOG.debug("Removing alias {} from map", id);
            mboxId2Listener.remove(id);
        }
        return true;
    }

    /**
     * Gets all users aliases with domain stripped off and local part to lower-case.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The list of mailbox identifiers
     * @throws OXException If mailbox identifiers cannot be returned
     */
    private Set<String> getMboxIdsFor(int userId, int contextId) throws OXException {
        User user = UserStorage.getInstance().getUser(userId, contextId);

        String[] aliases = user.getAliases();
        Set<String> mboxIds = new LinkedHashSet<String>(aliases.length + 1);
        for (String alias : aliases) {
            if (useEmailAddress) {
                mboxIds.add(Strings.toLowerCase(alias));
            } else {
                int idx = alias.indexOf('@');
                mboxIds.add(Strings.toLowerCase( (idx > 0) ? alias.substring(0, idx) : alias) );
            }
        }

        if (useOXLogin) {
            mboxIds.add(user.getLoginInfo().toLowerCase());
        }

        return mboxIds;
    }

}
