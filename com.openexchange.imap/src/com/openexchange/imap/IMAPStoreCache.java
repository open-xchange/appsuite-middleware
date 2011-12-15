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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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

package com.openexchange.imap;

import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Lock;
import javax.mail.AuthenticationFailedException;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.imap.services.IMAPServiceRegistry;
import com.openexchange.imap.util.CountingCondition;
import com.openexchange.imap.util.LockProvidingBlockingQueue;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.Protocol;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.session.Session;
import com.openexchange.threadpool.ThreadPools;
import com.openexchange.timer.ScheduledTimerTask;
import com.openexchange.timer.TimerService;
import com.sun.mail.imap.IMAPStore;

/**
 * {@link IMAPStoreCache} - A volatile cache for connected {@link IMAPStore} instances.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class IMAPStoreCache {

    protected static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(IMAPStoreCache.class));

    private static final int SHRINKER_MILLIS =
        (MailProperties.getInstance().getMailAccessCacheShrinkerSeconds() <= 0 ? 3 : MailProperties.getInstance().getMailAccessCacheShrinkerSeconds()) * 1000;

    protected static final int IDLE_MILLIS = MailProperties.getInstance().getMailAccessCacheIdleSeconds() * 1000;

    private static volatile IMAPStoreCache instance;

    /**
     * Initializes this cache.
     */
    public static void initInstance() {
        IMAPStoreCache tmp = instance;
        if (null != tmp) {
            return;
        }
        final ConfigurationService service = IMAPServiceRegistry.getService(ConfigurationService.class);
        final boolean checkConnected = null == service ? false : service.getBoolProperty("com.openexchange.imap.checkConnected", false);
        tmp = instance = new IMAPStoreCache(checkConnected);
        tmp.init();
    }

    /**
     * Shuts-down this cache.
     */
    public static void shutDownInstance() {
        final IMAPStoreCache tmp = instance;
        if (null == tmp) {
            return;
        }
        tmp.shutDown();
        instance = null;
    }

    /**
     * Gets the cache instance.
     * 
     * @return The instance
     */
    public static IMAPStoreCache getInstance() {
        return instance;
    }

    /*-
     * -------------------------------------- Runnable stuff --------------------------------------
     */

    private final class CloseElapsedRunnable implements Runnable {

        protected CloseElapsedRunnable() {
            super();
        }

        @Override
        public void run() {
            try {
                closeElapsed();
            } catch (final Exception e) {
                LOG.error(e.getMessage(), e);
            }
        }

    }

    /*-
     * -------------------------------------- Member stuff --------------------------------------
     */

    protected final Protocol protocol;

    protected final String name;

    protected final ConcurrentMap<Key, LockProvidingBlockingQueue<IMAPStoreWrapper>> map;

    private final ConcurrentMap<User, Queue<Key>> keys;

    private final boolean checkConnected;

    private volatile ScheduledTimerTask timerTask;

    /**
     * Initializes a new {@link IMAPStoreCache}.
     */
    private IMAPStoreCache(final boolean checkConnected) {
        super();
        this.checkConnected = checkConnected;
        protocol = IMAPProvider.PROTOCOL_IMAP;
        name = protocol.getName();
        map = new ConcurrentHashMap<Key, LockProvidingBlockingQueue<IMAPStoreWrapper>>();
        keys = new ConcurrentHashMap<User, Queue<Key>>();
    }

    private void init() {
        final TimerService timer = IMAPServiceRegistry.getService(TimerService.class);
        final Runnable task = new CloseElapsedRunnable();
        final int shrinkerMillis = SHRINKER_MILLIS;
        timerTask = timer.scheduleWithFixedDelay(task, shrinkerMillis, shrinkerMillis);
    }

    private void shutDown() {
        final ScheduledTimerTask timerTask = this.timerTask;
        if (null != timerTask) {
            timerTask.cancel();
            this.timerTask = null;
        }
    }

    /**
     * Drops all associated with specified user.
     * 
     * @param userId The user identifier
     * @param contextId The context identifier
     */
    public void dropFor(final int userId, final int contextId) {
        final Queue<Key> keyQueue = keys.remove(new User(userId, contextId));
        if (null != keyQueue) {
            for (final Key key : keyQueue) {
                final LockProvidingBlockingQueue<IMAPStoreWrapper> queue = map.remove(key);
                final Lock queueLock = queue.getLock();
                queueLock.lock();
                try {
                    for (final Iterator<IMAPStoreWrapper> iter = queue.getBlockingQueue().iterator(); iter.hasNext();) {
                        final IMAPStore imapStore = iter.next().getIMAPStore();
                        if (null != imapStore) {
                            iter.remove();
                            closeSafe(imapStore);
                        }
                    }
                } finally {
                    queueLock.unlock();
                }
            }
        }
    }

    /**
     * Close elapsed {@link IMAPStore} instances.
     */
    public void closeElapsed() {
        final Iterator<LockProvidingBlockingQueue<IMAPStoreWrapper>> queues = map.values().iterator();
        if (queues.hasNext()) {
            final StringBuilder debugBuilder = LOG.isDebugEnabled() ? new StringBuilder(64) : null;
            final long stamp = System.currentTimeMillis() - IDLE_MILLIS;
            do {
                final LockProvidingBlockingQueue<IMAPStoreWrapper> queue = queues.next();
                final Lock queueLock = queue.getLock();
                queueLock.lock();
                try {
                    for (final Iterator<IMAPStoreWrapper> iter = queue.getBlockingQueue().iterator(); iter.hasNext();) {
                        final IMAPStore imapStore = iter.next().getIfElapsed(stamp);
                        if (null != imapStore) {
                            iter.remove();
                            final String info = null == debugBuilder ? null : imapStore.toString();
                            closeSafe(imapStore);
                            if (null != debugBuilder) {
                                debugBuilder.setLength(0);
                                LOG.debug(debugBuilder.append("Closed elapsed IMAP store: ").append(info).toString());
                            }
                            // Signal
                            if (queue.isBounded()) {
                                final CountingCondition condition = queue.getCondition();
                                condition.signalAll();
                                if (null != debugBuilder) {
                                    debugBuilder.setLength(0);
                                    LOG.debug(debugBuilder.append("Signaled available cached IMAP store for: ").append(info).toString());
                                }
                            }
                        }
                    }
                } finally {
                    queueLock.unlock();
                }
            } while (queues.hasNext());
        }
    }

    private LockProvidingBlockingQueue<IMAPStoreWrapper> getBlockingQueue(final int accountId, final String server, final int port, final String login, final Session session) throws OXException {
        /*
         * Check for a cached one
         */
        final Key key = newKey(server, port, login);
        /*
         * Get queue
         */
        LockProvidingBlockingQueue<IMAPStoreWrapper> blockingQueue = map.get(key);
        if (null == blockingQueue) {
            final int maxCount = protocol.getMaxCount(server, MailAccount.DEFAULT_ID == accountId);
            final LockProvidingBlockingQueue<IMAPStoreWrapper> newQueue = new LockProvidingBlockingQueue<IMAPStoreWrapper>(maxCount);
            blockingQueue = map.putIfAbsent(key, newQueue);
            if (null == blockingQueue) {
                blockingQueue = newQueue;
                // Remember key
                final User uk = new User(session.getUserId(), session.getContextId());
                Queue<Key> keyQueue = keys.get(uk);
                if (null == keyQueue) {
                    final Queue<Key> nq = new LinkedBlockingQueue<Key>();
                    keyQueue = keys.putIfAbsent(uk, nq);
                    if (null == keyQueue) {
                        keyQueue = nq;
                    }
                }
                keyQueue.offer(key);
            }
        }
        return blockingQueue;
    }

    /**
     * Gets a connected IMAP store for specified arguments.
     * 
     * @param accountId The account identifier
     * @param imapSession The IMAP session
     * @param server The host name of the IMAP server
     * @param port The port
     * @param login The login/user name
     * @param pw The password
     * @return The connected IMAP store
     * @throws MessagingException If connecting IMAP store fails
     * @throws MailException If a mail error occurs
     */
    public IMAPStore borrowIMAPStore(final int accountId, final javax.mail.Session imapSession, final String server, final int port, final String login, final String pw, final Session session) throws MessagingException, OXException {
        /*
         * Return connected IMAP store
         */
        return connectedIMAPStore(false, getBlockingQueue(accountId, server, port, login, session), imapSession, server, port, login, pw);
    }

    private IMAPStore connectedIMAPStore(final boolean await, final LockProvidingBlockingQueue<IMAPStoreWrapper> queue, final javax.mail.Session imapSession, final String server, final int port, final String login, final String pw) throws MessagingException, OXException {
        final Lock queueLock = queue.getLock();
        queueLock.lock();
        try {
            /*
             * Check for available IMAP store
             */
            final StringBuilder debugBuilder = LOG.isDebugEnabled() ? new StringBuilder(64) : null;
            if (queue.isBounded() && (await || (queue.getCondition().getCount() > 0))) {
                /*
                 * Requested to await or other threads already waiting
                 */
                try {
                    if (null != debugBuilder) {
                        debugBuilder.setLength(0);
                        LOG.debug(debugBuilder.append("Awaiting free IMAP store for: imap://").append(login).append('@').append(server).append(
                            ':').append(port).toString());
                    }
                    final CountingCondition condition = queue.getCondition();
                    condition.await();
                } catch (final InterruptedException e) {
                    // Should not occur
                    ThreadPools.unexpectedlyInterrupted(Thread.currentThread());
                    throw MailExceptionCode.INTERRUPT_ERROR.create(e);
                }
            }
            /*
             * Check for available IMAP store
             */
            final BlockingQueue<IMAPStoreWrapper> daQueue = queue.getBlockingQueue();
            {
                final IMAPStore occupiedStore = occupyStore(imapSession, server, port, login, pw, debugBuilder, daQueue);
                if (null != occupiedStore) {
                    return occupiedStore;
                }
            }
            /*
             * Check if space available
             */
            final IMAPStoreWrapper newWrapper = newWrapper();
            while (!daQueue.offer(newWrapper)) {
                /*
                 * No space available, await for space to become available
                 */
                try {
                    if (null != debugBuilder) {
                        debugBuilder.setLength(0);
                        LOG.debug(debugBuilder.append("Awaiting free IMAP store for: imap://").append(login).append('@').append(server).append(
                            ':').append(port).toString());
                    }
                    final CountingCondition condition = queue.getCondition();
                    condition.await();
                } catch (final InterruptedException e) {
                    // Should not occur
                    ThreadPools.unexpectedlyInterrupted(Thread.currentThread());
                    throw MailExceptionCode.INTERRUPT_ERROR.create(e);
                }
                /*
                 * Re-check if a free store is available
                 */
                final IMAPStore occupiedStore = occupyStore(imapSession, server, port, login, pw, debugBuilder, daQueue);
                if (null != occupiedStore) {
                    return occupiedStore;
                }
            }
            try {
                /*
                 * Get new store...
                 */
                IMAPStore imapStore = (IMAPStore) imapSession.getStore(name);
                /*
                 * ... and connect it
                 */
                try {
                    imapStore.connect(server, port, login, pw);
                } catch (final AuthenticationFailedException e) {
                    /*
                     * Retry connect with AUTH=PLAIN disabled
                     */
                    imapSession.getProperties().put("mail.imap.auth.login.disable", "true");
                    imapStore = (IMAPStore) imapSession.getStore(name);
                    imapStore.connect(server, port, login, pw);
                }
                /*
                 * Done
                 */
                newWrapper.setOccupiedIMAPStore(imapStore);
                if (null != debugBuilder) {
                    debugBuilder.setLength(0);
                    LOG.debug(debugBuilder.append("Using newly established (cached) IMAP store for: imap://").append(login).append('@').append(
                        server).append(':').append(port).toString());
                }
                return imapStore;
            } catch (final MessagingException e) {
                // Establishing a new IMAP store failed
                daQueue.remove(newWrapper);
                throw e;
            } catch (final RuntimeException e) {
                // Establishing a new IMAP store failed
                daQueue.remove(newWrapper);
                throw e;
            }
        } finally {
            queueLock.unlock();
        }
    }

    private IMAPStore occupyStore(final javax.mail.Session imapSession, final String server, final int port, final String login, final String pw, final StringBuilder debugBuilder, final BlockingQueue<IMAPStoreWrapper> daQueue) throws MessagingException, NoSuchProviderException {
        for (final IMAPStoreWrapper wrapper : daQueue) {
            IMAPStore imapStore = wrapper.markOccupied();
            if (null != imapStore) {
                if (checkConnected && !imapStore.isConnected()) {
                    /*
                     * How is that possible? Check IMAPStore.finalize()
                     */
                    try {
                        imapStore.connect(server, port, login, pw);
                    } catch (final AuthenticationFailedException e) {
                        /*
                         * Retry connect with AUTH=PLAIN disabled
                         */
                        imapSession.getProperties().put("mail.imap.auth.login.disable", "true");
                        imapStore = (IMAPStore) imapSession.getStore(name);
                        imapStore.connect(server, port, login, pw);
                    }
                    if (null != debugBuilder) {
                        debugBuilder.setLength(0);
                        LOG.debug(debugBuilder.append("\n--- /!\\ --- Re-connected cached IMAP store for: imap://").append(login).append(
                            '@').append(server).append(':').append(port).append("--- /!\\ ---\n").toString());
                    }
                }
                if (null != debugBuilder) {
                    debugBuilder.setLength(0);
                    LOG.debug(debugBuilder.append(
                        "Using cached " + (imapStore.isConnected() ? "connected" : "UNCONNECTED") + " IMAP store for: imap://").append(
                        login).append('@').append(server).append(':').append(port).toString());
                }
                return imapStore;
            }
        }
        return null;
    }

    /**
     * Returns given connected IMAP store to cache.
     * 
     * @param imapStore The connected IMAP store to return
     * @param server The host name of the IMAP server
     * @param port The port
     * @param login The login/user name
     */
    public void returnIMAPStore(final IMAPStore imapStore, final String server, final int port, final String login) {
        if (null == imapStore) {
            // Nothing to close
            return;
        }
        /*
         * Get queue
         */
        final LockProvidingBlockingQueue<IMAPStoreWrapper> queue = map.get(newKey(server, port, login));
        if (null == queue) {
            closeSafe(imapStore);
            return;
        }
        final StringBuilder debugBuilder = LOG.isDebugEnabled() ? new StringBuilder(64) : null;
        final Lock queueLock = queue.getLock();
        queueLock.lock();
        try {
            /*
             * Orderly return to pool
             */
            final BlockingQueue<IMAPStoreWrapper> daQueue = queue.getBlockingQueue();
            {
                IMAPStoreWrapper wrapper = null;
                for (final IMAPStoreWrapper cur : daQueue) {
                    if (cur.clearOccupiedIfEqual(imapStore)) {
                        wrapper = cur;
                        break;
                    }
                }
                if (null != wrapper) { // Associated wrapper found in queue
                    if (checkConnected && !imapStore.isConnected()) {
                        // Already closed
                        daQueue.remove(wrapper);
                    }
                    if (null != debugBuilder) {
                        debugBuilder.setLength(0);
                        LOG.debug(debugBuilder.append(
                            "Returned cached " + (imapStore.isConnected() ? "connected" : "UNCONNECTED") + " IMAP store for: imap://").append(
                            login).append('@').append(server).append(':').append(port).toString());
                    }
                    return;
                }
            }
            if (null != debugBuilder) {
                debugBuilder.setLength(0);
                LOG.debug(debugBuilder.append("Couldn't return cached IMAP store for: imap://").append(login).append('@').append(server).append(
                    ':').append(port).toString());
            }
            if (checkConnected && !imapStore.isConnected()) {
                // Already closed
                return;
            }
            /*
             * Check if space available
             */
            final IMAPStoreWrapper newWrapper = newWrapper();
            if (!daQueue.offer(newWrapper)) {
                /*
                 * No space available
                 */
                closeSafe(imapStore);
                return;
            }
            newWrapper.setIMAPStore(imapStore);
            if (null != debugBuilder) {
                debugBuilder.setLength(0);
                LOG.debug(debugBuilder.append("Return uncached IMAP store for: imap://").append(login).append('@').append(server).append(
                    ':').append(port).toString());
            }
        } finally {
            if (queue.isBounded()) {
                try {
                    final CountingCondition condition = queue.getCondition();
                    condition.signalAll();
                    if (null != debugBuilder) {
                        debugBuilder.setLength(0);
                        LOG.debug(debugBuilder.append("Signaled available cached IMAP store for: imap://").append(login).append('@').append(
                            server).append(':').append(port).toString());
                    }
                } catch (final Exception e) {
                    LOG.error(
                        new StringBuilder("Failed signaling available cached IMAP store for: imap://").append(login).append('@').append(
                            server).append(':').append(port).toString(),
                        e);
                }
            }
            queueLock.unlock();
        }
    }

    private static void closeSafe(final IMAPStore imapStore) {
        if (null != imapStore) {
            try {
                imapStore.close();
            } catch (final Exception e) {
                // Ignore
            }
        }
    }

    private static IMAPStoreWrapper newWrapper() {
        return new IMAPStoreWrapper();
    }

    private static final class IMAPStoreWrapper {

        private IMAPStore value;
        private long lastAccessed;
        private boolean occupied;

        protected IMAPStoreWrapper() {
            super();
            value = null;
            occupied = false;
        }

        protected IMAPStore getIMAPStore() {
            return value;
        }

        protected IMAPStore getIfElapsed(final long stamp) {
            if (occupied || (null == value) || (lastAccessed >= stamp)) {
                return null;
            }
            // Elapsed
            final IMAPStore ret = value;
            value = null; // Release reference
            return ret;
        }

        protected void setIMAPStore(final IMAPStore value) {
            this.value = value;
            lastAccessed = System.currentTimeMillis();
        }

        protected void setOccupiedIMAPStore(final IMAPStore value) {
            this.occupied = true;
            this.value = value;
            lastAccessed = System.currentTimeMillis();
        }

        protected boolean clearOccupiedIfEqual(final IMAPStore candidate) {
            if (candidate.equals(value)) {
                occupied = false;
                lastAccessed = System.currentTimeMillis();
                return true;
            }
            // False
            return false;
        }

        protected IMAPStore markOccupied() {
            if (occupied || (null == value)) {
                return null;
            }
            occupied = true;
            lastAccessed = System.currentTimeMillis();
            return value;
        }

    }

    private static Key newKey(final String host, final int port, final String user) {
        return new Key(host, port, user);
    }

    private static final class Key {

        private final String host;
        private final int port;
        private final String user;
        private final int hash;

        protected Key(final String host, final int port, final String user) {
            super();
            this.host = host;
            this.port = port;
            this.user = user;
            final int prime = 31;
            int result = 1;
            result = prime * result + ((host == null) ? 0 : host.hashCode());
            result = prime * result + port;
            result = prime * result + ((user == null) ? 0 : user.hashCode());
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
            if (!(obj instanceof Key)) {
                return false;
            }
            final Key other = (Key) obj;
            if (host == null) {
                if (other.host != null) {
                    return false;
                }
            } else if (!host.equals(other.host)) {
                return false;
            }
            if (port != other.port) {
                return false;
            }
            if (user == null) {
                if (other.user != null) {
                    return false;
                }
            } else if (!user.equals(other.user)) {
                return false;
            }
            return true;
        }

    }

    private static final class User {
        private final int userId;
        private final int contextId;
        private final int hash;

        public User(final int userId, final int contextId) {
            super();
            this.userId = userId;
            this.contextId = contextId;
            final int prime = 31;
            int result = 1;
            result = prime * result + contextId;
            result = prime * result + userId;
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
            if (!(obj instanceof User)) {
                return false;
            }
            final User other = (User) obj;
            if (contextId != other.contextId) {
                return false;
            }
            if (userId != other.userId) {
                return false;
            }
            return true;
        }
    }

}
