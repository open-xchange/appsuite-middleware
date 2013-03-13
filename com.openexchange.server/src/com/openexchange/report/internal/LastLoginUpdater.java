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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.report.internal;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.commons.logging.Log;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import com.openexchange.context.ContextService;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.java.StringAllocator;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.sessiond.SessionMatcher;
import com.openexchange.sessiond.SessiondEventConstants;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.timer.ScheduledTimerTask;
import com.openexchange.timer.TimerService;
import com.openexchange.user.UserService;

/**
 * {@link LastLoginUpdater} - Periodically updates certain client's last-accessed time stamp (if an appropriate session is active).
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class LastLoginUpdater implements EventHandler {

    private static final Log LOG = com.openexchange.log.Log.loggerFor(LastLoginUpdater.LoginKey.class);

    private static final long MILLIS_DAY = 86400000L;

    private final ConcurrentMap<LoginKey, Reference<ScheduledTimerTask>> tasks;
    private final Set<String> topicsStart;
    private final Set<String> topicsStop;
    private final Set<String> allowedClients;
    private final Lock lock;

    /**
     * Initializes a new {@link LastLoginUpdater}.
     */
    public LastLoginUpdater() {
        super();
        tasks = new ConcurrentHashMap<LastLoginUpdater.LoginKey, Reference<ScheduledTimerTask>>(1024);
        Set<String> set = new HashSet<String>(3);
        set.add(SessiondEventConstants.TOPIC_ADD_SESSION);
        set.add(SessiondEventConstants.TOPIC_REACTIVATE_SESSION);
        set.add(SessiondEventConstants.TOPIC_RESTORED_SESSION);
        this.topicsStart = set;
        set = new HashSet<String>(3);
        set.add(SessiondEventConstants.TOPIC_REMOVE_DATA);
        set.add(SessiondEventConstants.TOPIC_REMOVE_CONTAINER);
        set.add(SessiondEventConstants.TOPIC_REMOVE_SESSION);
        this.topicsStop = set;
        set = new HashSet<String>(1);
        set.add("usm-eas");
        allowedClients = set;
        lock = new ReentrantLock();
    }

    @Override
    public void handleEvent(final Event event) {
        final String topic = event.getTopic();
        if (topicsStart.contains(topic)) {
            scheduleTimerIfAbsent((Session) event.getProperty(SessiondEventConstants.PROP_SESSION));
        } else if (topicsStop.contains(topic)) {
            if (SessiondEventConstants.TOPIC_REMOVE_CONTAINER.equals(topic)) {
                @SuppressWarnings("unchecked") final Map<String, Session> sessions =
                    (Map<String, Session>) event.getProperty(SessiondEventConstants.PROP_CONTAINER);
                for (final Session session : sessions.values()) {
                    cancelTimerIfLastActiveGone(session);
                }
            } else {
                cancelTimerIfLastActiveGone((Session) event.getProperty(SessiondEventConstants.PROP_SESSION));
            }
        }
    }

    private void scheduleTimerIfAbsent(final Session session) {
        final String client = session.getClient();
        if (!isEmpty(client) && allowedClients.contains(toLowerCase(client))) {
            final LoginKey key = new LoginKey(client, session.getUserId(), session.getContextId());
            Reference<ScheduledTimerTask> task = tasks.get(key);
            if (null == task) {
                // Not yet scheduled
                final Lock lock = this.lock;
                lock.lock();
                try {
                    final Reference<ScheduledTimerTask> newTask = new Reference<ScheduledTimerTask>();
                    task = tasks.putIfAbsent(key, newTask);
                    if (null == task) {
                        task = newTask;
                        // Schedule timer task for every 24h
                        final TimerService service = ServerServiceRegistry.getInstance().getService(TimerService.class);
                        task.set(service.scheduleAtFixedRate(new PeriodicTask(client, session.getUserId(), session.getContextId(), LOG), MILLIS_DAY, MILLIS_DAY));
                    }
                } finally {
                    lock.unlock();
                }
            }
        }
    }

    private void cancelTimerIfLastActiveGone(final Session session) {
        final String client = session.getClient();
        if (!isEmpty(client) && allowedClients.contains(toLowerCase(client))) {
            final Lock lock = this.lock;
            lock.lock();
            try {
                final int contextId = session.getContextId();
                final int userId = session.getUserId();
                final LoginKey key = new LoginKey(client, userId, contextId);
                final Reference<ScheduledTimerTask> task = tasks.get(key);
                if (null != task) {
                    final SessiondService sessiondService = ServerServiceRegistry.getInstance().getService(SessiondService.class);
                    final Session found = sessiondService.findFirstMatchingSessionForUser(userId, contextId, new ByClientSessionMatcher(client));
                    if (null == found) {
                        // No further session for that client
                        task.reference.cancel(false);
                        tasks.remove(key);
                        ServerServiceRegistry.getInstance().getService(TimerService.class).purge();
                    }
                }
            } finally {
                lock.unlock();
            }
        }
    }

    /** ASCII-wise to lower-case */
    private String toLowerCase(final CharSequence chars) {
        if (null == chars) {
            return null;
        }
        final int length = chars.length();
        final StringAllocator builder = new StringAllocator(length);
        for (int i = 0; i < length; i++) {
            final char c = chars.charAt(i);
            builder.append((c >= 'A') && (c <= 'Z') ? (char) (c ^ 0x20) : c);
        }
        return builder.toString();
    }

    /** Check for an empty string */
    private boolean isEmpty(final String string) {
        if (null == string) {
            return true;
        }
        final int len = string.length();
        boolean isWhitespace = true;
        for (int i = 0; isWhitespace && i < len; i++) {
            isWhitespace = Character.isWhitespace(string.charAt(i));
        }
        return isWhitespace;
    }

    // --------------------------------------- Helper classes ------------------------------------------ //

    private static final class PeriodicTask implements Runnable {

        private final int contextId;
        private final String client;
        private final int userId;
        private final Log logger;

        PeriodicTask(String client, int userId, int contextId, Log logger) {
            super();
            this.contextId = contextId;
            this.client = client;
            this.userId = userId;
            this.logger = logger;
        }

        @Override
        public void run() {
            final ContextService contextService = ServerServiceRegistry.getInstance().getService(ContextService.class);
            final UserService userService = ServerServiceRegistry.getInstance().getService(UserService.class);
            if (null != contextService && null != userService) {
                try {
                    final Context context = contextService.getContext(contextId);
                    final User user = userService.getUser(userId, context);
                    LastLoginRecorder.updateLastLogin(client, user, context);
                } catch (final Exception e) {
                    logger.warn("Failed updating last login timestamp for client \"" + client + "\" (user=" + userId + ", context=" + contextId + ").", e);
                }
            }
        }
    } // End of class PeriodicTask

    private static final class Reference<T> {

        volatile T reference;

        Reference() {
            super();
        }

        void set(final T reference) {
            this.reference = reference;
        }
    } // End of class Reference

    private static final class ByClientSessionMatcher implements SessionMatcher {

        private static final Set<Flag> flags = EnumSet.allOf(SessionMatcher.Flag.class);
        private final String client;;

        /**
         * Initializes a new {@link SessionMatcherImplementation}.
         */
        ByClientSessionMatcher(final String client) {
            super();
            this.client = client;
        }

        @Override
        public Set<Flag> flags() {
            return flags;
        }

        @Override
        public boolean accepts(final Session session) {
            return client.equals(session.getClient());
        }
    } // End of class ByClientSessionMatcher

    public static final class LoginKey {

        /** The context identifier */
        public final int contextId;

        /** The user identifier */
        public final int userId;

        /** The client identifier */
        public final String clientId;

        private final int hash;

        /**
         * Initializes a new {@link LoginKey}.
         */
        public LoginKey(final String clientId, final int userId, final int contextId) {
            super();
            this.clientId = clientId;
            this.userId = userId;
            this.contextId = contextId;
            final int prime = 31;
            int result = 1;
            result = prime * result + ((clientId == null) ? 0 : clientId.hashCode());
            result = prime * result + userId;
            result = prime * result + contextId;
            hash = result;
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof LoginKey)) {
                return false;
            }
            final LoginKey other = (LoginKey) obj;
            if (clientId == null) {
                if (other.clientId != null) {
                    return false;
                }
            } else if (!clientId.equals(other.clientId)) {
                return false;
            }
            if (contextId != other.contextId) {
                return false;
            }
            if (hash != other.hash) {
                return false;
            }
            if (userId != other.userId) {
                return false;
            }
            return true;
        }

    } // End of class LoginKey

}
