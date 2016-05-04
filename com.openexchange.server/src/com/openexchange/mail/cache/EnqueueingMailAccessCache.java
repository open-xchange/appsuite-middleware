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

package com.openexchange.mail.cache;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.concurrent.ConcurrentMap;
import org.cliffc.high_scale_lib.NonBlockingHashMap;
import com.openexchange.exception.OXException;
import com.openexchange.mail.MailProviderRegistry;
import com.openexchange.mail.api.IMailFolderStorage;
import com.openexchange.mail.api.IMailMessageStorage;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.cache.queue.MailAccessQueue;
import com.openexchange.mail.cache.queue.MailAccessQueueImpl;
import com.openexchange.mail.cache.queue.SingletonMailAccessQueue;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.timer.ScheduledTimerTask;
import com.openexchange.timer.TimerService;

/**
 * {@link EnqueueingMailAccessCache} - A very volatile cache for already connected instances of {@link MailAccess}.
 * <p>
 * A bounded {@link Queue} is used to store {@link MailAccess} instances to improve subsequent mail requests.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class EnqueueingMailAccessCache implements IMailAccessCache {

    /**
     * The logger instance.
     */
    protected static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(EnqueueingMailAccessCache.class);

    /**
     * Drop those queues of which all elements timed-out.
     */
    private static final boolean DROP_TIMED_OUT_QUEUES = false;

    /**
     * Creates a new {@link EnqueueingMailAccessCache}
     *
     * @return A new {@link EnqueueingMailAccessCache}
     * @throws OXException If initialization fails
     */
    public static EnqueueingMailAccessCache newInstance(final int queueCapacity) throws OXException {
        return new EnqueueingMailAccessCache(queueCapacity);
    }

    private static volatile EnqueueingMailAccessCache singleton;

    /**
     * Gets the singleton instance.
     *
     * @queueCapacity The max. queue capacity
     * @return The singleton instance
     * @throws OXException If instance initialization fails
     */
    public static EnqueueingMailAccessCache getInstance(final int queueCapacity) throws OXException {
        EnqueueingMailAccessCache tmp = singleton;
        if (null == tmp) {
            synchronized (EnqueueingMailAccessCache.class) {
                tmp = singleton;
                if (null == tmp) {
                    singleton = tmp = new EnqueueingMailAccessCache(queueCapacity);
                }
            }
        }
        return tmp;
    }

    /**
     * Releases the singleton instance.
     */
    public static void releaseInstance() {
        if (null != singleton) {
            synchronized (EnqueueingMailAccessCache.class) {
                if (null != singleton) {
                    singleton.dispose();
                    singleton = null;
                }
            }
        }
    }

    /*
     * Field members
     */

    private final ConcurrentMap<Key, MailAccessQueue> map;

    private final int defaultIdleSeconds;

    private final ScheduledTimerTask timerTask;

    /**
     * The number of {@link MailAccess} instances which may be concurrently active/opened per account for a user.
     */
    private final int fallbackQueueCapacity;

    /**
     * Prevent instantiation.
     *
     * @param fallbackQueueCapacity The max. queue capacity
     * @throws OXException If an error occurs
     */
    private EnqueueingMailAccessCache(final int fallbackQueueCapacity) throws OXException {
        super();
        this.fallbackQueueCapacity = fallbackQueueCapacity;
        map = new NonBlockingHashMap<Key, MailAccessQueue>();
        final int configuredIdleSeconds = MailProperties.getInstance().getMailAccessCacheIdleSeconds();
        defaultIdleSeconds = configuredIdleSeconds <= 0 ? 7 : configuredIdleSeconds;
        /*
         * Add timer task
         */
        final TimerService service = ServerServiceRegistry.getInstance().getService(TimerService.class, true);
        final int configuredShrinkerSeconds = MailProperties.getInstance().getMailAccessCacheShrinkerSeconds();
        final int shrinkerMillis = (configuredShrinkerSeconds <= 0 ? 3 : configuredShrinkerSeconds) * 1000;
        timerTask = service.scheduleWithFixedDelay(new PurgeExpiredRunnable(map), shrinkerMillis, shrinkerMillis);
    }

    @Override
    public int numberOfMailAccesses(Session session, int accountId) throws OXException {
        final Key key = keyFor(accountId, session);
        final MailAccessQueue accessQueue = map.get(key);
        if (null == accessQueue) {
            return 0;
        }
        synchronized (accessQueue) {
            return accessQueue.size();
        }
    }

    /**
     * Removes and returns a mail access from cache.
     *
     * @param session The session
     * @param accountId The account ID
     * @return An active instance of {@link MailAccess} or <code>null</code>
     */
    @Override
    public MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> removeMailAccess(final Session session, final int accountId) {
        final Key key = keyFor(accountId, session);
        final MailAccessQueue accessQueue = map.get(key);
        if (null == accessQueue) {
            return null;
        }
        synchronized (accessQueue) {
            if (accessQueue.isDeprecated()) {
                return null;
            }
            final PooledMailAccess pooledMailAccess = accessQueue.poll();
            if (null == pooledMailAccess) {
                return null;
            }
            final MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = pooledMailAccess.getMailAccess();
            mailAccess.setCached(false);
            LOG.debug("Remove&Get for {}", key);
            return mailAccess;
        }
    }

    /**
     * Puts given mail access into cache if none user-bound connection is already contained in cache.
     *
     * @param session The session
     * @param accountId The account ID
     * @param mailAccess The mail access to put into cache
     * @return <code>true</code> if mail access could be successfully cached; otherwise <code>false</code>
     */
    @Override
    public boolean putMailAccess(final Session session, final int accountId, final MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess) {
        int idleSeconds = mailAccess.getCacheIdleSeconds();
        if (idleSeconds <= 0) {
            idleSeconds = defaultIdleSeconds;
        }
        final Key key = keyFor(accountId, session);
        MailAccessQueue accessQueue = map.get(key);
        if (null == accessQueue || accessQueue.isDeprecated()) {
            // First check capacity boundary per mail provider and fall back to default if check fails
            int capacity;
            try {
                capacity = MailProviderRegistry.getMailProviderBySession(session, accountId).getProtocol().getMaxCount(mailAccess.getMailConfig().getServer(), MailAccount.DEFAULT_ID == accountId);
            } catch (final OXException e) {
                capacity = fallbackQueueCapacity;
            }
            final MailAccessQueue tmp = capacity > 0 ? (1 == capacity ? new SingletonMailAccessQueue() : new MailAccessQueueImpl(capacity)) : new MailAccessQueueImpl(-1);
            accessQueue = map.putIfAbsent(key, tmp);
            if (null == accessQueue) {
                accessQueue = tmp;
            }
        }
        synchronized (accessQueue) {
            if (accessQueue.isDeprecated()) {
                return false;
            }
            /*
             * Insert subsequent MailAccess instances with halved time-to-live seconds
             */
            idleSeconds = accessQueue.isEmpty() ? idleSeconds : (idleSeconds >> 1);
            if (accessQueue.offer(PooledMailAccess.valueFor(mailAccess, idleSeconds * 1000L))) {
                mailAccess.setCached(true);
                return true;
            }
            return false;
        }
    }

    /**
     * Checks if cache already holds a user-bound mail access for specified account.
     *
     * @param session The session
     * @param accountId The account ID
     * @return <code>true</code> if a user-bound mail access is already present in cache; otherwise <code>false</code>
     */
    @Override
    public boolean containsMailAccess(final Session session, final int accountId) {
        final MailAccessQueue accessQueue = map.get(keyFor(accountId, session));
        if (null == accessQueue) {
            return false;
        }
        synchronized (accessQueue) {
            return !accessQueue.isDeprecated() && !accessQueue.isEmpty();
        }
    }

    @Override
    public void close() {
        dispose();
    }

    /**
     * Disposes this cache.
     */
    protected void dispose() {
        timerTask.cancel(false);
        for (final Key key : new HashSet<Key>(map.keySet())) {
            orderlyClearQueue(key);
        }
    }

    /**
     * Clears specified queue orderly.
     *
     * @param key The key associated with the queue
     */
    protected void orderlyClearQueue(final Key key) {
        final MailAccessQueue accessQueue = map.remove(key);
        if (null == accessQueue) {
            return;
        }
        synchronized (accessQueue) {
            accessQueue.markDeprecated();
            PooledMailAccess pooledMailAccess;
            while (null != (pooledMailAccess = accessQueue.poll())) {
                final MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess = pooledMailAccess.getMailAccess();
                LOG.debug("Dropping: {}", mailAccess);
                mailAccess.setCached(false);
                mailAccess.close(false);
            }
        }
    }

    /**
     * Clears the cache entries kept for specified user.
     *
     * @param session The session
     * @throws OXException If clearing user entries fails
     */
    @Override
    public void clearUserEntries(final Session session) throws OXException {
        /*
         * Check if last...
         */
        final SessiondService service = ServerServiceRegistry.getInstance().getService(SessiondService.class);
        final int user = session.getUserId();
        final int cid = session.getContextId();
        if (null == service || null == service.getAnyActiveSessionForUser(user, cid)) {
            final MailAccountStorageService storageService =
                ServerServiceRegistry.getInstance().getService(MailAccountStorageService.class, true);
            final MailAccount[] accounts = storageService.getUserMailAccounts(user, cid);
            for (final MailAccount mailAccount : accounts) {
                orderlyClearQueue(keyFor(mailAccount.getId(), session));
            }
        }
    }

    private static Key keyFor(final int accountId, final Session session) {
        return new Key(accountId, session.getUserId(), session.getContextId());
    }

    private static final class PurgeExpiredRunnable implements Runnable {

        private final ConcurrentMap<Key, MailAccessQueue> map;

        protected PurgeExpiredRunnable(final ConcurrentMap<Key, MailAccessQueue> map) {
            super();
            this.map = map;
        }

        @Override
        public void run() {
            try {
                /*
                 * Shall timed-out queues be removed from mapping?
                 */
                final boolean dropQueue = isDropQueue();
                /*
                 * Iterate mapping
                 */
                for (final Iterator<Entry<Key, MailAccessQueue>> iterator = map.entrySet().iterator(); iterator.hasNext();) {
                    final Entry<Key, MailAccessQueue> entry = iterator.next();
                    final MailAccessQueue accessQueue = entry.getValue();
                    synchronized (accessQueue) {
                        PooledMailAccess pooledMailAccess;
                        while (null != (pooledMailAccess = accessQueue.pollDelayed())) {
                            final MailAccess<? extends IMailFolderStorage, ? extends IMailMessageStorage> mailAccess =
                                pooledMailAccess.getMailAccess();
                            mailAccess.setCached(false);
                            LOG.debug("Timed-out mail access for {}", entry.getKey());

                            //System.out.println(new StringBuilder("Timed-out mail access for ").append(entry.getKey()).toString());

                            mailAccess.close(false);
                        }
                        if (dropQueue && accessQueue.isEmpty()) {
                            /*
                             * Current queue is empty. Mark as deprecated
                             */
                            accessQueue.markDeprecated();
                            LOG.debug("Dropped queue for {}", entry.getKey());
                            iterator.remove();
                        }
                    }
                }
            } catch (final RuntimeException e) {
                // A runtime exception
                LOG.warn("Purge-expired run failed", e);
            }
        }

        private boolean isDropQueue() {
            return DROP_TIMED_OUT_QUEUES;
        }
    }

    private static final class Key {

        private final int user;

        private final int context;

        private final int accountId;

        private final int hash;

        protected Key(final int accountId, final int user, final int context) {
            super();
            this.user = user;
            this.context = context;
            this.accountId = accountId;
            hash = hashCode0();
        }

        private int hashCode0() {
            final int prime = 31;
            int result = 1;
            result = prime * result + accountId;
            result = prime * result + context;
            result = prime * result + user;
            return result;
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
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Key other = (Key) obj;
            if (accountId != other.accountId) {
                return false;
            }
            if (context != other.context) {
                return false;
            }
            if (user != other.user) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            final StringBuilder builder = new StringBuilder(16);
            builder.append("{ Key [accountId=").append(accountId).append(", user=").append(user).append(", context=").append(context).append(
                "] }");
            return builder.toString();
        }

    }

}
