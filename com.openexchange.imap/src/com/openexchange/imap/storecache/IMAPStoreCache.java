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

package com.openexchange.imap.storecache;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import javax.mail.MessagingException;
import org.cliffc.high_scale_lib.NonBlockingHashMap;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.Interests;
import com.openexchange.config.Reloadable;
import com.openexchange.config.Reloadables;
import com.openexchange.exception.OXException;
import com.openexchange.imap.IMAPAccess;
import com.openexchange.imap.config.IMAPReloadable;
import com.openexchange.imap.services.Services;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.session.Session;
import com.openexchange.session.UserAndContext;
import com.openexchange.threadpool.RefusedExecutionBehavior;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.threadpool.ThreadPools;
import com.openexchange.threadpool.behavior.CallerRunsBehavior;
import com.openexchange.timer.ScheduledTimerTask;
import com.openexchange.timer.TimerService;
import com.sun.mail.imap.IMAPStore;

/**
 * {@link IMAPStoreCache} - A volatile cache for connected {@link IMAPStore} instances.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class IMAPStoreCache {

    /** The logger instance */
    static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(IMAPStoreCache.class);

    /** Holder for shrinker frequency in milliseconds */
    static final AtomicInteger SHRINKER_MILLIS = new AtomicInteger((MailProperties.getInstance().getMailAccessCacheShrinkerSeconds() <= 0 ? 3 : MailProperties.getInstance().getMailAccessCacheShrinkerSeconds()) * 1000);

    /** Holder for idle time in milliseconds */
    static final AtomicInteger IDLE_MILLIS = new AtomicInteger(MailProperties.getInstance().getMailAccessCacheIdleSeconds() * 1000);

    private static volatile IMAPStoreCache instance;

    /**
     * Gets the cache instance.
     *
     * @return The instance
     */
    public static IMAPStoreCache getInstance() {
        return instance;
    }

    /**
     * Initializes this cache.
     */
    public static synchronized void initInstance() {
        IMAPStoreCache tmp = instance;
        if (null != tmp) {
            return;
        }
        final ConfigurationService service = Services.getService(ConfigurationService.class);
        Container container = null == service ? Container.getDefault() : Container.containerFor(service.getProperty("com.openexchange.imap.storeContainerType", Container.getDefault().getId()));
        if (Container.UNBOUNDED.equals(container) && (null != service && service.getIntProperty("com.openexchange.imap.maxNumConnections", 0) > 0)) {
            LOG.warn("Property \"com.openexchange.imap.storeContainerType\" is set to \"unbounded\", but \"com.openexchange.imap.maxNumConnections\" is greater than zero. Using default container \"{}\" instead.", Container.getDefault().getId());
            container = Container.getDefault();
        }
        tmp = new IMAPStoreCache(container);
        tmp.reinit(true);
        instance = tmp;
    }

    /**
     * Shuts-down this cache.
     */
    public static synchronized void shutDownInstance() {
        final IMAPStoreCache tmp = instance;
        if (null != tmp) {
            instance = null;
            tmp.shutDown();
        }
    }

    static {
        IMAPReloadable.getInstance().addReloadable(new Reloadable() {

            @SuppressWarnings("synthetic-access")
            @Override
            public void reloadConfiguration(final ConfigurationService configService) {
                SHRINKER_MILLIS.set(configService.getIntProperty("com.openexchange.mail.mailAccessCacheShrinkerSeconds", 3) * 1000);
                IDLE_MILLIS.set(configService.getIntProperty("com.openexchange.mail.mailAccessCacheIdleSeconds", 4) * 1000);
                IMAPStoreCache tmp = instance;
                if (null != tmp) {
                    tmp.reinit(false);
                }
            }

            @Override
            public Interests getInterests() {
                return Reloadables.interestsForProperties("com.openexchange.mail.mailAccessCacheShrinkerSeconds", "com.openexchange.mail.mailAccessCacheIdleSeconds");
            }
        });
    }

    /*-
     * -------------------------------------------------------------- Runnable stuff -------------------------------------------------------
     */

    private static final class ContainerCloseElapsedRunnable implements Runnable {

        private final IMAPStoreContainer container;
        private final long stamp;

        /**
         * Initializes a new {@link ContainerCloseElapsedRunnable}.
         *
         * @param container The container to consider
         * @param stamp The minimum time stamp; any cached entry that has a last-accessed time stamp lower than given one is considered as elapsed
         */
        ContainerCloseElapsedRunnable(IMAPStoreContainer container, long stamp) {
            super();
            this.container = container;
            this.stamp = stamp;
        }

        @Override
        public void run() {
            container.closeElapsed(stamp);
        }
    }

    private static final class CloseElapsedRunnable implements Runnable {

        private final IMAPStoreCache storeCache;

        /**
         * Initializes a new {@link CloseElapsedRunnable}.
         *
         * @param storeCache The cache instance
         */
        CloseElapsedRunnable(IMAPStoreCache storeCache) {
            super();
            this.storeCache = storeCache;
        }

        @Override
        public void run() {
            try {
                storeCache.closeElapsed();
            } catch (final Exception e) {
                LOG.error("", e);
            }
        }
    }

    /*-
     * ---------------------------------------------------------- Member stuff -------------------------------------------------------------
     */

    private final Container containerType;
    private final ConcurrentMap<Key, IMAPStoreContainer> map;
    private final ConcurrentMap<UserAndContext, Queue<Key>> keys;
    private final AtomicReference<ScheduledTimerTask> timerTaskReference;
    private final RefusedExecutionBehavior<Object> behavior;

    /**
     * Initializes a new {@link IMAPStoreCache}.
     *
     * @param container
     */
    private IMAPStoreCache(Container container) {
        super();
        containerType = null == container ? Container.getDefault() : container;
        behavior = CallerRunsBehavior.getInstance();
        map = new NonBlockingHashMap<Key, IMAPStoreContainer>();
        keys = new NonBlockingHashMap<UserAndContext, Queue<Key>>();
        timerTaskReference = new AtomicReference<>();
    }

    /**
     * Gets the class of the associated IMAP store.
     *
     * @return The IMAP store class
     */
    public Class<? extends IMAPStore> getStoreClass() {
        return containerType.getStoreClass();
    }

    void reinit(boolean withInitialDelay) {
        TimerService timer = Services.getService(TimerService.class);

        ScheduledTimerTask timerTask = timerTaskReference.getAndSet(null);
        if (null != timerTask) {
            timerTask.cancel();
            timer.purge();
        }

        int delayMillis = SHRINKER_MILLIS.get();
        timerTaskReference.set(timer.scheduleWithFixedDelay(new CloseElapsedRunnable(this), withInitialDelay ? delayMillis : 0L, delayMillis));
    }

    private void shutDown() {
        List<IMAPStoreContainer> containers = new ArrayList<IMAPStoreContainer>(this.map.values());
        this.map.clear();

        ScheduledTimerTask timerTask = timerTaskReference.getAndSet(null);
        if (null != timerTask) {
            timerTask.cancel();
        }

        if (!containers.isEmpty()) {
            for (IMAPStoreContainer container : containers) {
                container.clear();
            }
        }
    }

    /**
     * Drops all associated with specified user.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     */
    public void dropFor(int userId, int contextId) {
        final Queue<Key> keyQueue = keys.remove(UserAndContext.newInstance(userId, contextId));
        if (null != keyQueue) {
            for (final Key key : keyQueue) {
                final IMAPStoreContainer container = map.remove(key);
                if (null != container) {
                    container.clear();
                }
            }
        }
    }

    /**
     * (Internal method) Removes a container by given key.
     *
     * @param key The key
     */
    void remove(Key key) {
        IMAPStoreContainer removed = map.remove(key);
        if (removed != null) {
            Queue<Key> keyQueue = keys.get(UserAndContext.newInstance(key.userId, key.contextId));
            if (keyQueue != null) {
                keyQueue.remove(key);
            }
        }
    }

    /**
     * Closes elapsed {@link IMAPStore} instances.
     */
    void closeElapsed() {
        final Iterator<IMAPStoreContainer> containers = map.values().iterator();
        if (!containers.hasNext()) {
            // Cache is empty
            return;
        }

        long stamp = System.currentTimeMillis() - IDLE_MILLIS.get();

        ThreadPoolService threadPool = ThreadPools.getThreadPool();
        if (null == threadPool) {
            // Consider each container with running thread
            do {
                IMAPStoreContainer container = containers.next();
                if (null != container) {
                    container.closeElapsed(stamp);
                }
            } while (containers.hasNext());
        } else {
            // Schedule a task for each container
            do {
                IMAPStoreContainer container = containers.next();
                if (null != container && container.hasElapsed(stamp)) {
                    threadPool.submit(ThreadPools.trackableTask(new ContainerCloseElapsedRunnable(container, stamp)), behavior);
                }
            } while (containers.hasNext());
        }
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
     * @param propagateClientIp <code>true</code> to signal client IP address; otherwise <code>false</code>
     * @param checkConnectivityIfPolled <code>true</code> to explicitly check an <code>IMAPStore</code> instance's connectivity status (through issuing a <code>"NOOP"</code> command) if polled; otherwise <code>false</code> to pass as-is
     * @return The connected IMAP store or <code>null</code> if currently impossible to do so
     * @throws MessagingException If connecting IMAP store fails
     * @throws OXException If a mail error occurs
     */
    public IMAPStore borrowIMAPStore(int accountId, javax.mail.Session imapSession, String server, int port, String login, String pw, Session session, boolean propagateClientIp, boolean checkConnectivityIfPolled) throws MessagingException, OXException {
        /*
         * Return connected IMAP store
         */
        while (true) {
            try {
                IMAPStoreContainer container = getContainer(accountId, server, port, login, session, propagateClientIp, checkConnectivityIfPolled);
                return container.getStore(imapSession, login, pw, session);
            } catch (IMAPStoreContainerInvalidException e) {
                // Try again by re-fetching appropriate container
                LOG.debug("Encountered invalidated IMAP store container. Trying again.", e);
            } catch (InterruptedException e) {
                // Should not occur
                Thread.currentThread().interrupt();
                throw MailExceptionCode.INTERRUPT_ERROR.create(e);
            } catch (MessagingException e) {
                final Exception nested = e.getNextException();
                if (nested instanceof OXException) {
                    throw (OXException) nested;
                }
                throw e;
            }
        }
    }

    private IMAPStoreContainer getContainer(int accountId, String server, int port, String login, Session session, boolean propagateClientIp, boolean checkConnectivityIfPolled) {
        // Generate appropriate key for desired container
        Key key = newKey(accountId, server, port, login, session.getUserId(), session.getContextId());

        // Fetch existing or create a new one
        IMAPStoreContainer container = map.get(key);
        if (null == container) {
            IMAPStoreContainer newContainer = newContainer(server, port, accountId, session, propagateClientIp, checkConnectivityIfPolled, key);
            container = map.putIfAbsent(key, newContainer);
            if (null == container) {
                container = newContainer;
                // Remember key
                UserAndContext uk = UserAndContext.newInstance(session);
                Queue<Key> keyQueue = keys.get(uk);
                if (null == keyQueue) {
                    Queue<Key> nq = new LinkedBlockingQueue<Key>();
                    keyQueue = keys.putIfAbsent(uk, nq);
                    if (null == keyQueue) {
                        keyQueue = nq;
                    }
                }
                keyQueue.offer(key);
            }
        }
        return container;
    }

    private IMAPStoreContainer newContainer(String server, int port, int accountId, Session session, boolean propagateClientIp, boolean checkConnectivityIfPolled, Key key) {
        switch (containerType) {
            case UNBOUNDED:
                return new UnboundedIMAPStoreContainer(accountId, session, server, port, propagateClientIp, checkConnectivityIfPolled, key);
            case BOUNDARY_AWARE:
                return new BoundaryAwareIMAPStoreContainer(accountId, session, server, port, propagateClientIp, checkConnectivityIfPolled, key);
            case NON_CACHING:
                return new NonCachingIMAPStoreContainer(accountId, session, server, port, propagateClientIp);
            default:
                return new BoundaryAwareIMAPStoreContainer(accountId, session, server, port, propagateClientIp, checkConnectivityIfPolled, key);
        }
    }

    /**
     * Returns given connected IMAP store to cache.
     *
     * @param imapStore The connected IMAP store to return
     * @param accountId The account identifier
     * @param server The host name of the IMAP server
     * @param port The port
     * @param login The login/user name
     * @param session The associated session
     */
    public void returnIMAPStore(IMAPStore imapStore, int accountId, String server, int port, String login, Session session) {
        if (null == imapStore) {
            // Nothing to close
            return;
        }
        /*
         * Get queue
         */
        IMAPStoreContainer container = map.get(newKey(accountId, server, port, login, session.getUserId(), session.getContextId()));
        if (null == container) {
            IMAPAccess.closeSafely(imapStore);
            return;
        }
        container.backStore(imapStore);
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private static Key newKey(int accountId, String host, int port, String user, int userId, int contextId) {
        return new Key(accountId, host, port, user, userId, contextId);
    }

    /**
     * The key for an <code>IMAPStoreContainer</code> managed in cache.
     */
    static final class Key {

        private final int accountId;
        private final String host;
        private final int port;
        private final String user;
        final int userId;
        final int contextId;
        private final int hash;

        Key(int accountId, String host, int port, String user, int userId, int contextId) {
            super();
            this.accountId = accountId;
            this.host = host;
            this.port = port;
            this.user = user;
            this.userId = userId;
            this.contextId = contextId;
            final int prime = 31;
            int result = 1;
            result = prime * result + accountId;
            result = prime * result + ((host == null) ? 0 : host.hashCode());
            result = prime * result + port;
            result = prime * result + ((user == null) ? 0 : user.hashCode());
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
            if (!(obj instanceof Key)) {
                return false;
            }
            final Key other = (Key) obj;
            if (port != other.port) {
                return false;
            }
            if (accountId != other.accountId) {
                return false;
            }
            if (userId != other.userId) {
                return false;
            }
            if (contextId != other.contextId) {
                return false;
            }
            if (host == null) {
                if (other.host != null) {
                    return false;
                }
            } else if (!host.equals(other.host)) {
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

}
