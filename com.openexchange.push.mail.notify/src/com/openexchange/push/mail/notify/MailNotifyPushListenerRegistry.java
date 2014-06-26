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

import java.util.Collection;
import java.util.Iterator;
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
import com.openexchange.sessiond.SessiondService;
import com.openexchange.timer.ScheduledTimerTask;
import com.openexchange.timer.TimerService;
import com.openexchange.tools.iterator.ReadOnlyIterator;

/**
 * {@link MailNotifyPushListenerRegistry} - The registry for {@link PushListener}s.
 *
 */
public final class MailNotifyPushListenerRegistry {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(MailNotifyPushListenerRegistry.class);

    private final ConcurrentMap<String, MailNotifyPushListener> listenerMap;
    private final boolean useOXLogin;
    private final boolean useEmailAddress;
    private final MailNotifyDelayQueue notificationsQueue;
    private final ScheduledTimerTask timerTask;

    /**
     * Initializes a new {@link MailNotifyPushListenerRegistry}.
     */
    public MailNotifyPushListenerRegistry(final boolean useOXLogin, final boolean useEmailAddress) {
        super();
        listenerMap = new ConcurrentHashMap<String, MailNotifyPushListener>();
        this.useOXLogin = useOXLogin;
        this.useEmailAddress = useEmailAddress;
        notificationsQueue = new MailNotifyDelayQueue();
        // Timer task
        final TimerService timerService = Services.optService(TimerService.class);
        final org.slf4j.Logger log = LOG;
        final Runnable r = new Runnable() {

            @Override
            public void run() {
                try {
                    triggerNotification();
                } catch (final Exception e) {
                    log.warn("Failed to trigger notifications.", e);
                }
            }
        };
        final int delay = 3000;
        timerTask = timerService.scheduleWithFixedDelay(r, delay, delay);
    }

    /**
     * Cancels the timer.
     */
    public void cancel() {
        timerTask.cancel();
    }

    /**
     * Schedules to notify specified mbox identifier (if not yet scheduled).
     *
     * @param mboxid The mbox identifier
     */
    public void scheduleEvent(final String mboxid) {
        notificationsQueue.offerIfAbsent(new DelayedNotification(mboxid, false));
        triggerNotification();
    }

    /**
     * Triggers all due notifications.
     */
    public synchronized void triggerNotification() {
        DelayedNotification polled = notificationsQueue.poll();
        if (null != polled) {
            final List<String> mboxIds = new LinkedList<String>();
            do {
                mboxIds.add(polled.getMboxid());
                polled = notificationsQueue.poll();
            } while (polled != null);
            notifyNow(mboxIds);
        }
    }

    /**
     * (Immediately) Notifies specified mbox identifiers.
     *
     * @param mboxIds The mbox identifiers to notify
     */
    private void notifyNow(final Collection<String> mboxIds) {
        if (mboxIds.isEmpty()) {
            return;
        }
        for (final String mboxId : mboxIds) {
            try {
                fireEvent(mboxId);
            } catch (final OXException e) {
                LOG.error("Failed to create push event", e);
            }
        }
    }

    /**
     * If the given mboxid is registered for receiving of events, fire event...
     *
     * @param mboxid
     * @throws OXException
     */
    private void fireEvent(final String mboxid) throws OXException {
        LOG.debug("checking whether to fire event for {}", mboxid);
        final PushListener listener = listenerMap.get(mboxid);
        if (null != listener) {
            LOG.debug("fireEvent, mboxid={}", mboxid);
            listener.notifyNewMail();
        }
    }

    /**
     * Adds specified push listener.
     *
     * @param contextId The context identifier
     * @param userId The user identifier
     * @param pushListener The push listener to add
     * @return <code>true</code> if push listener service could be successfully added; otherwise <code>false</code>
     */
    public boolean addPushListener(final int contextId, final int userId, final MailNotifyPushListener pushListener) throws OXException {
        boolean notYetPushed = true;
        for (final String id : getMboxIds(contextId, userId)) {
            if (null == listenerMap.putIfAbsent(id, pushListener)) {
                LOG.debug("added mboxid {} to map for user {} in context {}", id, Integer.valueOf(userId), Integer.valueOf(contextId));
            } else {
                // Listener wasn't put into map
                LOG.debug("mboxid {} was not put into map (as already present) for user {} in context {}", id, Integer.valueOf(userId), Integer.valueOf(contextId));
                if (notYetPushed) {
                    notYetPushed = false;
                }
            }
        }
        return notYetPushed;
    }

    /**
     * return all the users aliases with domain stripped off and localpart to lowercase
     *
     * @param contextId
     * @param userId
     * @return List of mbox identifiers
     * @throws OXException
     */
    private final Set<String> getMboxIds(final int contextId, final int userId) throws OXException {
        final User user = UserStorage.getInstance().getUser(userId, contextId);

        final String[] aliases = user.getAliases();
        final int alength = aliases.length;
        final Set<String> ret = new LinkedHashSet<String>(alength + 1);
        for (int i = 0; i < alength; i++) {
            final String alias = aliases[i];
            if( useEmailAddress ) {
                ret.add(Strings.toLowerCase(alias));
            } else {
                final int idx = alias.indexOf('@');
                if( idx > 0) {
                    ret.add(Strings.toLowerCase(alias.substring(0, idx)));
                } else {
                    ret.add(Strings.toLowerCase(alias));
                }
            }
        }
        if( useOXLogin ) {
            ret.add(user.getLoginInfo().toLowerCase());
        }
        return ret;
    }
    /**
     * Clears this registry. <br>
     * <b>Note</b>: {@link MailNotifyPushListener#close()} is called for each instance.
     */
    public void clear() {
        for (final Iterator<MailNotifyPushListener> i = listenerMap.values().iterator(); i.hasNext();) {
            i.next().close();
            i.remove();
        }
        listenerMap.clear();
    }

    /**
     * Closes all listeners contained in this registry.
     */
    public void closeAll() {
        for (final Iterator<MailNotifyPushListener> i = listenerMap.values().iterator(); i.hasNext();) {
            i.next().close();
        }
    }

    /**
     * Gets a read-only {@link Iterator iterator} over the push listeners in this registry.
     * <p>
     * Invoking {@link Iterator#remove() remove} will throw an {@link UnsupportedOperationException}.
     *
     * @return A read-only {@link Iterator iterator} over the push listeners in this registry.
     */
    public Iterator<MailNotifyPushListener> getPushListeners() {
        return new ReadOnlyIterator<MailNotifyPushListener>(listenerMap.values().iterator());
    }

    /**
     * Opens all listeners contained in this registry.
     */
    public void openAll() {
        for (final Iterator<MailNotifyPushListener> i = listenerMap.values().iterator(); i.hasNext();) {
            final MailNotifyPushListener l = i.next();
            try {
                l.open();
            } catch (final OXException e) {
                org.slf4j.LoggerFactory.getLogger(MailNotifyPushListenerRegistry.class).error("Opening mail push UDP listener failed. Removing listener from registry: {}", l.toString(), e);
                i.remove();
            }
        }
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
        return removeListener(getMboxIds(contextId, userId));
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
            return removeListener(getMboxIds(contextId, userId));
        }
        return false;
    }

    private boolean removeListener(final Collection<String> mboxIds) {
        for(final String id : mboxIds) {
            LOG.debug("removing alias {} from map", id);
            final MailNotifyPushListener listener = listenerMap.remove(id);
            if (null != listener) {
                listener.close();
            }
        }
        return true;
    }
}
