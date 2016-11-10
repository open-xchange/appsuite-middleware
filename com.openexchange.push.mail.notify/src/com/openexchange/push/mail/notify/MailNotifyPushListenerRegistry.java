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

package com.openexchange.push.mail.notify;

import static com.openexchange.java.Autoboxing.I;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.java.Strings;
import com.openexchange.push.PushListenerService;
import com.openexchange.push.PushUser;
import com.openexchange.push.PushUserInfo;
import com.openexchange.push.PushUtility;
import com.openexchange.push.mail.notify.osgi.Services;
import com.openexchange.push.mail.notify.util.DelayedNotification;
import com.openexchange.push.mail.notify.util.MailNotifyDelayQueue;
import com.openexchange.push.mail.notify.util.SimpleKey;
import com.openexchange.session.Session;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.sessiond.SessiondServiceExtended;
import com.openexchange.timer.ScheduledTimerTask;
import com.openexchange.timer.TimerService;
import com.openexchange.user.UserService;

/**
 * {@link MailNotifyPushListenerRegistry} - The registry for {@code MailNotifyPushListener}s.
 *
 */
public final class MailNotifyPushListenerRegistry {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(MailNotifyPushListenerRegistry.class);

    private static enum StopResult {
        NONE, RECONNECTED, RECONNECTED_AS_PERMANENT, STOPPED;
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
        mboxId2Listener = new ConcurrentHashMap<String, MailNotifyPushListener>(2048);
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

    private boolean isUserValid(Session session, Map<SimpleKey, Boolean> validityMap) {
        try {
            SimpleKey key = SimpleKey.valueOf(session);
            Boolean validity = validityMap.get(key);
            if (null != validity) {
                return validity.booleanValue();
            }

            ContextService contextService = Services.getService(ContextService.class, true);
            Context context = contextService.loadContext(session.getContextId());
            if (!context.isEnabled()) {
                validityMap.put(key, Boolean.FALSE);
                return false;
            }

            UserService userService = Services.getService(UserService.class, true);
            User user = userService.getUser(session.getUserId(), context);
            boolean mailEnabled = user.isMailEnabled();
            validityMap.put(key, Boolean.valueOf(mailEnabled));
            return mailEnabled;
        } catch (OXException e) {
            return false;
        }
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
        if (mboxId2Listener.containsKey(mboxid)) {
            notificationsQueue.offerIfAbsent(new DelayedNotification(mboxid, false));
            triggerDueNotifications();
        } else {
            LOG.debug("Denied scheduling an event for mboxid {} as there is no associated listener available.", mboxid);
        }
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
            Map<SimpleKey, Boolean> validityMap = new HashMap<SimpleKey, Boolean>(mboxIds.size());
            for (String mboxId : mboxIds) {
                try {
                    fireEvent(mboxId, validityMap);
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
    private void fireEvent(String mboxid, Map<SimpleKey, Boolean> validityMap) throws OXException {
        LOG.debug("Checking whether to fire event for {}", mboxid);

        MailNotifyPushListener listener = mboxId2Listener.get(mboxid);
        if (null != listener) {
            if (isUserValid(listener.getSession(), validityMap)) {
                // Valid user
                LOG.debug("fireEvent, mboxid={}", mboxid);
                listener.notifyNewMail();
            } else {
                // User is invalid/disabled
                LOG.debug("Denied fireEvent for mboxid {} as associated user and/or context has been disabled.", mboxid);
            }
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
     * Gets the available push users
     *
     * @return The push users
     */
    public List<PushUserInfo> getAvailablePushUsers() {
        Set<PushUserInfo> set = new HashSet<PushUserInfo>();
        for (MailNotifyPushListener listener : mboxId2Listener.values()) {
            Session ses = listener.getSession();
            set.add(new PushUserInfo(new PushUser(ses.getUserId(), ses.getContextId()), listener.isPermanent()));
        }
        return new LinkedList<PushUserInfo>(set);
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

        synchronized (this) {
            boolean notYetPushed = true;
            for (String mboxId : mboxIds) {
                MailNotifyPushListener current = mboxId2Listener.putIfAbsent(mboxId, pushListener);
                if (null == current) {
                    LOG.debug("Added UDP-based mail listener {} for user {} in context {}", mboxId, Integer.valueOf(userId), Integer.valueOf(contextId));
                } else {
                    boolean replaced = false;

                    if (pushListener.isPermanent() && !current.isPermanent()) {
                        // Replace non-permanent with permanent listener
                        mboxId2Listener.put(mboxId, pushListener);
                        replaced = true;
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

        synchronized (this) {
            boolean stopped = false;
            for (String mboxId : mboxIds) {
                StopResult stopResult = stopListener(tryToReconnect, stopIfPermanent, mboxId, userId, contextId);

                stopped |= (StopResult.STOPPED == stopResult);

                switch (stopResult) {
                    case RECONNECTED:
                        LOG.info("Reconnected UDP-based mail listener {} for user {} in context {} using another session", mboxId, I(userId), I(contextId));
                        return true;
                    case RECONNECTED_AS_PERMANENT:
                        LOG.info("Reconnected as permanent UDP-based mail listener {} for user {} in context {}", mboxId, I(userId), I(contextId));
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

            if (!reconnected) {
                return StopResult.STOPPED;
            }

            MailNotifyPushListener newListener = mboxId2Listener.get(mboxId);
            return (null != newListener && newListener.isPermanent()) ? StopResult.RECONNECTED_AS_PERMANENT : StopResult.RECONNECTED;
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
    private MailNotifyPushListener injectAnotherListenerFor(Session optionalOldSession, String mboxId, int userId, int contextId) {
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
                    if (!oldSessionId.equals(session.getSessionID()) && PushUtility.allowedClient(session.getClient(), session, true)) {
                        MailNotifyPushListener newListener = MailNotifyPushListener.newInstance(session, false);
                        mboxId2Listener.put(mboxId, newListener);
                        return newListener;
                    }
                }

                // Look-up remote sessions, too, if possible
                if (sessiondService instanceof SessiondServiceExtended) {
                    sessions = ((SessiondServiceExtended) sessiondService).getSessions(userId, contextId, true);
                    for (Session session : sessions) {
                        if (!oldSessionId.equals(session.getSessionID()) && PushUtility.allowedClient(session.getClient(), session, true)) {
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
        Set<String> mboxIds = getMboxIdsFor(userId, contextId);
        if (mboxIds.isEmpty()) {
            LOG.warn("No resolvable aliases for user {} in context {}", Integer.valueOf(userId), Integer.valueOf(contextId));
            return false;
        }

        synchronized (this) {
            for (String id : mboxIds) {
                LOG.debug("Removing alias {} from map", id);
                mboxId2Listener.remove(id);
            }
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
        // Get the associated user
        User user = Services.getService(UserService.class, true).getUser(userId, contextId);

        // Get user aliases
        String[] aliases = user.getAliases();

        // Iterate aliases and fill into set
        Set<String> mboxIds = new LinkedHashSet<String>(aliases.length + 1);
        if (useEmailAddress) {
            for (String alias : aliases) {
                mboxIds.add(Strings.toLowerCase(alias));
            }
        } else {
            for (String alias : aliases) {
                int idx = alias.indexOf('@');
                mboxIds.add(Strings.toLowerCase((idx > 0) ? alias.substring(0, idx) : alias));
            }
        }

        // Add login-info as well (if demanded)
        if (useOXLogin) {
            String loginInfo = user.getLoginInfo();
            if (loginInfo != null) {
                mboxIds.add(loginInfo.toLowerCase());
                LOG.debug("Added login info from user with id {} in context {}.", userId, contextId);
            }
        }

        return mboxIds;
    }

}
