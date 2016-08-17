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

package com.sun.mail.imap;

import java.io.IOException;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.AbstractQueue;
import java.util.ArrayList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.StringTokenizer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.URLName;
import javax.security.auth.Subject;
import org.slf4j.Logger;
import com.sun.mail.iap.ConnectQuotaExceededException;
import com.sun.mail.iap.ProtocolException;
import com.sun.mail.imap.protocol.IMAPProtocol;
import com.sun.mail.util.MailLogger;
import com.sun.mail.util.PropUtil;

/**
 * {@link QueuingIMAPStore}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class QueuingIMAPStore extends IMAPStore {

    /** The logger */
    static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(QueuingIMAPStore.class);

    /**
     * Gets the logger.
     *
     * @return The logger
     */
    public static Logger getLog() {
        return LOG;
    }

    /** The global <code>ScheduledThreadPoolExecutor</code> instance */
    private static volatile ScheduledThreadPoolExecutor executor;

    /** Gets the executor */
    private static ScheduledThreadPoolExecutor executor() {
        ScheduledThreadPoolExecutor exec = executor;
        if (null == exec) {
            synchronized (QueuingIMAPStore.class) {
                exec = executor;
                if (null == exec) {
                    final SecurityManager s = System.getSecurityManager();
                    final ThreadGroup group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
                    final AtomicInteger threadNumber = new AtomicInteger(1);
                    final String namePrefix = "com.sun.mail.imap.PeriodicRunner-";
                    final ThreadFactory factory = new ThreadFactory() {

                        @Override
                        public Thread newThread(final Runnable r) {
                            final Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
                            if (t.isDaemon()) {
                                t.setDaemon(false);
                            }
                            if (t.getPriority() != Thread.NORM_PRIORITY) {
                                t.setPriority(Thread.NORM_PRIORITY);
                            }
                            return t;
                        }
                    };
                    exec = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors() + 1, factory);
                    exec.prestartCoreThread();
                    final ScheduledThreadPoolExecutor texec = exec;
                    // Schedule purging task
                    exec.scheduleAtFixedRate(new Runnable() {

                        @Override
                        public void run() {
                            texec.purge();
                        }
                    }, 60000, 60000, TimeUnit.MILLISECONDS);
                    executor = exec;
                    // Initialize watcher, too
                    initWatcher(exec);
                }
            }
        }
        return exec;
    }

    /**
     * Shuts-down the executor.
     */
    public static void shutdown() {
        final QueuedIMAPProtocolWatcher tmp = watcher;
        if (null != tmp) {
            tmp.shutdown();
            watcher = null;
        }
        final ScheduledThreadPoolExecutor exec = executor;
        if (null != exec) {
            exec.shutdown();
            executor = null;
        }
    }

    // ----------------------------------------------------------------------------------------------------------------------------- //

    private static QueuedIMAPProtocolWatcher watcher;

    private static void initWatcher(final ScheduledThreadPoolExecutor executor) {
        QueuedIMAPProtocolWatcher tmp = new QueuedIMAPProtocolWatcher();
        tmp.initWatcher(queues, executor);
        watcher = tmp;
    }

    // ----------------------------------------------------------------------------------------------------------------------------- //

    /** Mapping for the queues */
    static final ConcurrentMap<URLName, CountingQueue> queues = new ConcurrentHashMap<URLName, CountingQueue>(16);

    private static CountingQueue initQueue(final URLName url, final int permits, final MailLogger logger) {
        CountingQueue q = queues.get(url);
        if (null == q) {
            final CountingQueue ns = new CountingQueue(permits, logger, QueuedIMAPProtocolWatcher.isEnabled());
            q = queues.putIfAbsent(url, ns);
            if (null == q) {
                q = ns;
                final boolean debug = logger.isLoggable(Level.FINE) || LOG.isDebugEnabled();
                final CountingQueue tq = q;
                final ConcurrentMap<URLName, CountingQueue> tqueues = queues;
                final AtomicInteger noneCount = new AtomicInteger();
                final AtomicReference<ScheduledFuture<?>> futureRef = new AtomicReference<ScheduledFuture<?>>();
                final Runnable t = new Runnable() {

                    @Override
                    public void run() {
                        if (debug) {
                            final String msg = "Cleaner run. Elements in queue " + tq.hashCode() + ": " + tq.size();
                            logger.fine(msg);
                            LOG.debug(msg);
                        }
                        final long minStamp = System.currentTimeMillis() - 4000;
                        final Lock lock = tq.lock;
                        lock.lock();
                        try {
                            final boolean notInUse = tq.closeElapsed0(minStamp);
                            if (notInUse) {
                                if (noneCount.incrementAndGet() >= 3 && tqueues.remove(url, tq)) {
                                    // Atomically removed queue, because seen this queue as "not in use" for 3 times
                                    tq.deprecated = true;
                                    final ScheduledFuture<?> future = futureRef.getAndSet(null);
                                    if (null != future) {
                                        future.cancel(false);
                                    }
                                }
                            } else {
                                noneCount.set(0);
                            }
                        } finally {
                            lock.unlock();
                        }
                    }
                };
                final ScheduledFuture<?> future = executor().scheduleWithFixedDelay(t, 3, 3, TimeUnit.SECONDS);
                futureRef.set(future);
                if (debug) {
                    final String msg = "QueueingIMAPStore.initQueue(): New queue for \"" + url + "\": BlockingQueue@" + q.hashCode() + " " + q.toString();
                    logger.fine(msg);
                    LOG.debug(msg);
                }
            }
        }
        return q;
    }

    // ----------------------------------------------------------------------------------------------------------------------------- //

    /** Whether SASL is enabled. */
    private final boolean enableSASL;

    /** The Kerberos subject. */
    private transient Subject kerberosSubject;

    /** The SASL mechansims */
    protected String[] saslMechanisms;

    /** The SASL realm */
    protected String m_saslRealm;

    /** Authorization ID */
    private String m_authorizationID;

    /** Proxy auth user */
    private String m_proxyAuthUser;

    /**
     * Initializes a new {@link QueuingIMAPStore}.
     *
     * @param session The session
     * @param url The URL
     * @param name The store's name
     * @param isSSL Whether to perform SSL or not
     */
    public QueuingIMAPStore(final Session session, final URLName url, final String name, final boolean isSSL) {
        super(session, url, name, isSSL);
        enableSASL = PropUtil.getBooleanSessionProperty(session, "mail.imap.sasl.enable", false);
        if (enableSASL) {
            // Kerberos subject
            kerberosSubject = (Subject) session.getProperties().get("mail.imap.sasl.kerberosSubject");
            // SASL mechansims
            String s = session.getProperty("mail.imap.sasl.mechanisms");
            if (s != null && s.length() > 0) {
                final List<String> v = new ArrayList<String>(5);
                final StringTokenizer st = new StringTokenizer(s, " ,");
                while (st.hasMoreTokens()) {
                    final String m = st.nextToken();
                    if (m.length() > 0) {
                        v.add(m);
                    }
                }
                saslMechanisms = v.toArray(new String[0]);
            }
            // SASL realm
            s = session.getProperty("mail.imap.sasl.realm");
            if (s != null) {
                m_saslRealm = s;
            }
            // Check if an authorization ID has been specified
            s = session.getProperty("mail." + name + ".sasl.authorizationid");
            if (s != null) {
                m_authorizationID = s;
            }
            // Check if we should do a PROXYAUTH login
            s = session.getProperty("mail." + name + ".proxyauth.user");
            if (s != null) {
                m_proxyAuthUser = s;
            }
        }
    }

    /**
     * Initializes a new {@link QueuingIMAPStore}.
     *
     * @param session The session
     * @param url The URL
     */
    public QueuingIMAPStore(final Session session, final URLName url) {
        this(session, url, "imap", false);
    }

    @Override
    protected IMAPProtocol newIMAPProtocol(final String host, final int port, final String user, final String password) throws IOException, ProtocolException {
        try {
            if (PropUtil.getBooleanSessionProperty(session, "mail.imap.forceAuthenticated", false)) {
                // Forced -- delegate to super implementation
                return super.newIMAPProtocol(host, port, user, password);
            }
            while (true) {
                try {
                    final CountingQueue q = initQueue(new URLName("imap", host, port, /* Integer.toString(accountId) */null, user, password), PropUtil.getIntSessionProperty(session, "mail.imap.maxNumAuthenticated", 0), logger);
                    final boolean debug = logger.isLoggable(Level.FINE) || LOG.isDebugEnabled();
                    if (debug) {
                        final String msg = "QueueingIMAPStore.newIMAPProtocol(): " + Thread.currentThread().getName() + " is trying to create/fetch for user '" + user + "@" + host + "'. Pending threads " + q.trackedThreads();
                        logger.fine(msg);
                        LOG.debug(msg);
                    }
                    // Acquire protocol
                    QueuedIMAPProtocol protocol = q.takeOrIncrement(this);
                    if (null != protocol) {
                        if (debug) {
                            final String msg = "QueueingIMAPStore.newIMAPProtocol(): Fetched from queue " + protocol.toString();
                            logger.fine(msg);
                            LOG.debug(msg);
                        }
                        q.addTrackingInfo(protocol.setStore(this));
                        return protocol;
                    }
                    // Create a new protocol instance
                    protocol = new QueuedIMAPProtocol(name, host, port, session.getProperties(), isSSL, logger, q, this);
                    if (debug) {
                        final String msg = "\nQueueingIMAPStore.newIMAPProtocol(): Created new protocol instance " + protocol.toString() + "\n\t(total=" + q.getNewCount() + ")";
                        logger.fine(msg);
                        LOG.debug(msg);
                    }
                    q.addTrackingInfo(protocol);
                    return protocol;
                } catch (final DeprecatedQueueException e) {
                    // Retry
                }
            }
        } catch (final InterruptedException e) {
            // Keep interrupted status
            Thread.currentThread().interrupt();
            throw new ProtocolException("Interrupted.", e);
        }
    }

    @Override
    protected void login(final IMAPProtocol p, final String u, final String pw) throws ProtocolException {
        if (p.isAuthenticated()) {
            super.login(p, u, pw);
            return;
        }
        /*
         * Check for possible Kerberos authentication
         */
        if (!enableSASL || null == kerberosSubject) {
            // Do non-Kerberos authentication
            super.login(p, u, pw);
            return;
        }
        // Do Kerberos authentication
        final String authzid;
        if (m_authorizationID != null) {
            authzid = m_authorizationID;
        } else if (m_proxyAuthUser != null) {
            authzid = m_proxyAuthUser;
        } else {
            authzid = null;
        }
        try {
            Subject.doAs(kerberosSubject, new PrivilegedExceptionAction<Object>() {

                @Override
                public Object run() throws Exception {
                    p.sasllogin(saslMechanisms, m_saslRealm, authzid, u, pw);
                    return null;
                }
            });
        } catch (final PrivilegedActionException e) {
            handlePrivilegedActionException(e);
        }
    }

    private static void handlePrivilegedActionException(final PrivilegedActionException e) throws ProtocolException {
        if (null == e) {
            return;
        }
        final Exception cause = e.getException();
        if (null == cause) {
            throw new ProtocolException(e.getMessage(), e);
        }
        if (cause instanceof ProtocolException) {
            throw (ProtocolException) cause;
        }
        if (cause instanceof MessagingException) {
            final MessagingException me = (MessagingException) cause;
            final Exception nextException = me.getNextException();
            if (nextException instanceof ProtocolException) {
                throw (ProtocolException) nextException;
            }
            throw new ProtocolException(me.getMessage(), me);
        }
        throw new ProtocolException(e.getMessage(), cause);
    }

    // --------------------------------------------------------------------------------------------------------- //

    static final class CountingQueue extends AbstractQueue<QueuedIMAPProtocol> implements BlockingQueue<QueuedIMAPProtocol>, java.io.Serializable {

        private static final long serialVersionUID = 5595510919245408276L;

        final PriorityQueue<QueuedIMAPProtocol> q;
        final ReentrantLock lock = new ReentrantLock(true);
        boolean deprecated;
        private final MailLogger logger;
        private final Condition notEmpty = lock.newCondition();

        private final int max;
        private int newCount;

        private final ConcurrentMap<QueuingIMAPStore, QueuingIMAPStore> stores;
        private final ConcurrentMap<Thread, ThreadTrace> threads;

        /**
         * Initializes a new {@link CountingQueue}.
         */
        public CountingQueue(final int max, final MailLogger logger, final boolean trackThreads) {
            super();
            deprecated = false;
            newCount = 0;
            this.logger = logger;
            q = new PriorityQueue<QueuedIMAPProtocol>(max < 1 ? 11 : max);
            this.max = max <= 0 ? Integer.MAX_VALUE : max;
            stores = new ConcurrentHashMap<QueuingIMAPStore, QueuingIMAPStore>(max < 1 ? 11 : max);
            threads = trackThreads ? new ConcurrentHashMap<Thread, ThreadTrace>(max < 1 ? 11 : max) : null;
        }

        /**
         * Gets the logger
         *
         * @return The logger
         */
        public MailLogger getLogger() {
            return logger;
        }

        /**
         * Gets a formatted output for currently tracked threads
         *
         * @return The formatted output of currently tracked threads
         */
        public ConcurrentMap<Thread, ThreadTrace> trackedThreads() {
            return this.threads;
        }

        /**
         * Adds tracking information to this queue.
         */
        public void addTrackingInfo(final QueuedIMAPProtocol protocol) {
            final QueuingIMAPStore store = protocol.store;
            stores.putIfAbsent(store, store);
            final ConcurrentMap<Thread, ThreadTrace> threads = this.threads;
            if (null != threads) {
                threads.putIfAbsent(Thread.currentThread(), new ThreadTrace(protocol, System.currentTimeMillis()));
            }
        }

        /**
         * Removes tracking information from this queue.
         */
        public void removeTrackingInfo(final QueuedIMAPProtocol protocol) {
            if (null != protocol) {
                stores.remove(protocol.store);
            }
            final ConcurrentMap<Thread, ThreadTrace> threads = this.threads;
            if (null != threads) {
                threads.remove(Thread.currentThread());
            }
        }

        /**
         * Gets the newCount
         *
         * @return The newCount
         */
        public int getNewCount() {
            final ReentrantLock lock = this.lock;
            lock.lock();
            try {
                return newCount;
            } finally {
                lock.unlock();
            }
        }

        /**
         * Closes elapsed elements. Signals <code>true</code> if queue is not in use at the time of invocation.
         *
         * @param minStamp The minimum allowed time stamp
         * @return <code>true</code> if nothing happened because queue was not in use; otherwise <code>false</code>
         */
        public boolean closeElapsed(final long minStamp) {
            final ReentrantLock lock = this.lock;
            lock.lock();
            try {
                return closeElapsed0(minStamp);
            } finally {
                lock.unlock();
            }
        }

        /**
         * Closes elapsed elements. Signals <code>true</code> if queue is not in use at the time of invocation.
         * <p>
         * Lock <b>MUST</b> be acquired prior to invocation!
         *
         * @param minStamp The minimum allowed time stamp
         * @return <code>true</code> if nothing happened because queue was not in use; otherwise <code>false</code>
         */
        boolean closeElapsed0(final long minStamp) {
            if (q.isEmpty() && newCount <= 0) {
                return true;
            }
            QueuedIMAPProtocol x;
            while (((x = q.peek()) != null) && (x.getAuthenticatedStamp() < minStamp)) {
                x = q.poll();
                assert x != null;
                // Perform LOGOUT on polled IMAP protocol
                safeLogout(x);
            }
            return false;
        }

        private void safeLogout(final QueuedIMAPProtocol x) {
            boolean loggedOut = false;
            try {
                x.realLogout(); // Implicitly triggers #decrementNewCount() through QueuedIMAPProtocol#authenticatedStatusChanging()
                loggedOut = true;
            } catch (final Exception e) {
                // Ignore
            } finally {
                if (!loggedOut) {
                    // An error occurred during LOGOUT attempt
                    decrementNewCount();
                }
            }
        }

        /**
         * Takes or increments the new count. Waiting for available elements if none in queue and count is exceeded.
         * <p>
         * Waits for default time of 20 seconds.
         *
         * @return Either <code>null</code> to signal a new instance is allowed to be created; or a connected {@link QueuedIMAPProtocol} instance fetched from queue
         * @throws InterruptedException If interrupted while waiting
         * @throws ConnectQuotaExceededException If no condition was met to await or create a connection
         * @throws DeprecatedQueueException If queue has been deprecated in the meantime
         */
        public QueuedIMAPProtocol takeOrIncrement(final QueuingIMAPStore store) throws InterruptedException, ProtocolException {
            return takeOrIncrement(store, 20, TimeUnit.SECONDS);
        }

        /**
         * Takes or increments the new count. Waiting for available elements if none in queue and count is exceeded.
         *
         * @return Either <code>null</code> to signal a new instance is allowed to be created; or a connected {@link QueuedIMAPProtocol} instance fetched from queue
         * @throws InterruptedException If interrupted while waiting
         * @throws ConnectQuotaExceededException If no condition was met to await or create a connection
         * @throws DeprecatedQueueException If queue has been deprecated in the meantime
         */
        public QueuedIMAPProtocol takeOrIncrement(final QueuingIMAPStore store, final long timeout, final TimeUnit unit) throws InterruptedException, ProtocolException {
            final ReentrantLock lock = this.lock;
            lock.lock();
            try {
                if (deprecated) {
                    throw new DeprecatedQueueException("Queue is deprecated");
                }
                long nanos = unit.toNanos(timeout);
                QueuedIMAPProtocol x;
                try {
                    while (((x = q.poll()) == null) && (newCount >= max) && (nanos > 0)) {
                        // None available and not allowed to open further connections
                        if (stores.containsKey(store)) {
                            // Might wait for itself to release a connection -- outta here!
                            throw new com.sun.mail.iap.ConnectQuotaExceededException("No connection available and not allowed to open further ones");
                        }
                        // Await until a connection is released
                        nanos = notEmpty.awaitNanos(nanos);
                    }
                } catch (final InterruptedException ie) {
                    notEmpty.signal(); // propagate to non-interrupted thread
                    throw ie;
                }
                // Not-null polled one
                if (null != x) {
                    return x;
                }
                // Wait time exceeded
                if ((nanos <= 0) || (newCount >= max)) {
                    throw new com.sun.mail.iap.ConnectQuotaExceededException("No connection available and not allowed to open more than " + max + " connections. Waited " + unit.toSeconds(timeout) + " seconds");
                }
                // Increment new count
                newCount++;
                return null;
            } finally {
                lock.unlock();
            }
        }

        /**
         * Decrements the new count.
         */
        public void decrementNewCount() {
            final ReentrantLock lock = this.lock;
            lock.lock();
            try {
                if (newCount > 0) {
                    newCount--;
                }
                notEmpty.signal();
            } finally {
                lock.unlock();
            }
        }

        /**
         * Inserts the specified element into this queue.
         *
         * @param e the element to add
         * @return <tt>true</tt> (as specified by {@link Collection#add})
         * @throws ClassCastException if the specified element cannot be compared with elements currently in the priority queue according to
         *             the priority queue's ordering
         * @throws NullPointerException if the specified element is null
         */
        @Override
        public boolean add(final QueuedIMAPProtocol e) {
            return offer(e);
        }

        /**
         * Inserts the specified element into this queue.
         *
         * @param e the element to add
         * @return <tt>true</tt> (as specified by {@link Queue#offer})
         * @throws ClassCastException if the specified element cannot be compared with elements currently in the priority queue according to
         *             the priority queue's ordering
         * @throws NullPointerException if the specified element is null
         */
        @Override
        public boolean offer(final QueuedIMAPProtocol e) {
            final ReentrantLock lock = this.lock;
            lock.lock();
            try {
                if (deprecated) {
                    throw new DeprecatedQueueException("Queue is deprecated");
                }
                final boolean ok = q.offer(e);
                assert ok;
                notEmpty.signal();
                return true;
            } finally {
                lock.unlock();
            }
        }

        /**
         * Inserts the specified element into this queue if not already contained.
         *
         * @param e the element to add
         * @return <tt>true</tt> (as specified by {@link Queue#offer})
         * @throws ClassCastException if the specified element cannot be compared with elements currently in the priority queue according to
         *             the priority queue's ordering
         * @throws NullPointerException if the specified element is null
         */
        public boolean offerIfAbsent(final QueuedIMAPProtocol e) {
            final ReentrantLock lock = this.lock;
            lock.lock();
            try {
                if (deprecated) {
                    throw new DeprecatedQueueException("Queue is deprecated");
                }
                if (q.contains(e)) {
                    return true;
                }
                final boolean ok = q.offer(e);
                assert ok;
                notEmpty.signal();
                return true;
            } finally {
                lock.unlock();
            }
        }

        /**
         * Inserts the specified element into this queue. As the queue is unbounded this method will never block.
         *
         * @param e the element to add
         * @throws ClassCastException if the specified element cannot be compared with elements currently in the priority queue according to
         *             the priority queue's ordering
         * @throws NullPointerException if the specified element is null
         */
        @Override
        public void put(final QueuedIMAPProtocol e) {
            offer(e); // never need to block
        }

        /**
         * Inserts the specified element into this queue. As the queue is unbounded this method will never block.
         *
         * @param e the element to add
         * @param timeout This parameter is ignored as the method never blocks
         * @param unit This parameter is ignored as the method never blocks
         * @return <tt>true</tt>
         * @throws ClassCastException if the specified element cannot be compared with elements currently in the priority queue according to
         *             the priority queue's ordering
         * @throws NullPointerException if the specified element is null
         */
        @Override
        public boolean offer(final QueuedIMAPProtocol e, final long timeout, final TimeUnit unit) {
            return offer(e); // never need to block
        }

        @Override
        public QueuedIMAPProtocol poll() {
            final ReentrantLock lock = this.lock;
            lock.lock();
            try {
                if (deprecated) {
                    throw new DeprecatedQueueException("Queue is deprecated");
                }
                return q.poll();
            } finally {
                lock.unlock();
            }
        }

        @Override
        public QueuedIMAPProtocol take() throws InterruptedException {
            final ReentrantLock lock = this.lock;
            lock.lockInterruptibly();
            try {
                if (deprecated) {
                    throw new DeprecatedQueueException("Queue is deprecated");
                }
                try {
                    while (q.size() == 0) {
                        notEmpty.await();
                    }
                } catch (final InterruptedException ie) {
                    notEmpty.signal(); // propagate to non-interrupted thread
                    throw ie;
                }
                final QueuedIMAPProtocol x = q.poll();
                assert x != null;
                return x;
            } finally {
                lock.unlock();
            }
        }

        @Override
        public QueuedIMAPProtocol poll(final long timeout, final TimeUnit unit) throws InterruptedException {
            long nanos = unit.toNanos(timeout);
            final ReentrantLock lock = this.lock;
            lock.lockInterruptibly();
            try {
                if (deprecated) {
                    throw new IllegalStateException("Queue is deprecated");
                }
                for (;;) {
                    final QueuedIMAPProtocol x = q.poll();
                    if (x != null) {
                        return x;
                    }
                    if (nanos <= 0) {
                        return null;
                    }
                    try {
                        nanos = notEmpty.awaitNanos(nanos);
                    } catch (final InterruptedException ie) {
                        notEmpty.signal(); // propagate to non-interrupted thread
                        throw ie;
                    }
                }
            } finally {
                lock.unlock();
            }
        }

        @Override
        public QueuedIMAPProtocol peek() {
            final ReentrantLock lock = this.lock;
            lock.lock();
            try {
                return q.peek();
            } finally {
                lock.unlock();
            }
        }

        @Override
        public int size() {
            final ReentrantLock lock = this.lock;
            lock.lock();
            try {
                return q.size();
            } finally {
                lock.unlock();
            }
        }

        /**
         * Always returns <tt>Integer.MAX_VALUE</tt> because a <tt>PriorityBlockingQueue</tt> is not capacity constrained.
         *
         * @return <tt>Integer.MAX_VALUE</tt>
         */
        @Override
        public int remainingCapacity() {
            return Integer.MAX_VALUE;
        }

        /**
         * Removes a single instance of the specified element from this queue, if it is present. More formally, removes an element {@code e}
         * such that {@code o.equals(e)}, if this queue contains one or more such elements. Returns {@code true} if and only if this queue
         * contained the specified element (or equivalently, if this queue changed as a result of the call).
         *
         * @param o element to be removed from this queue, if present
         * @return <tt>true</tt> if this queue changed as a result of the call
         */
        @Override
        public boolean remove(final Object o) {
            final ReentrantLock lock = this.lock;
            lock.lock();
            try {
                if (deprecated) {
                    throw new DeprecatedQueueException("Queue is deprecated");
                }
                return q.remove(o);
            } finally {
                lock.unlock();
            }
        }

        /**
         * Returns {@code true} if this queue contains the specified element. More formally, returns {@code true} if and only if this queue
         * contains at least one element {@code e} such that {@code o.equals(e)}.
         *
         * @param o object to be checked for containment in this queue
         * @return <tt>true</tt> if this queue contains the specified element
         */
        @Override
        public boolean contains(final Object o) {
            final ReentrantLock lock = this.lock;
            lock.lock();
            try {
                return q.contains(o);
            } finally {
                lock.unlock();
            }
        }

        /**
         * Returns an array containing all of the elements in this queue. The returned array elements are in no particular order.
         * <p>
         * The returned array will be "safe" in that no references to it are maintained by this queue. (In other words, this method must
         * allocate a new array). The caller is thus free to modify the returned array.
         * <p>
         * This method acts as bridge between array-based and collection-based APIs.
         *
         * @return an array containing all of the elements in this queue
         */
        @Override
        public Object[] toArray() {
            final ReentrantLock lock = this.lock;
            lock.lock();
            try {
                return q.toArray();
            } finally {
                lock.unlock();
            }
        }

        @Override
        public String toString() {
            final ReentrantLock lock = this.lock;
            lock.lock();
            try {
                return q.toString();
            } finally {
                lock.unlock();
            }
        }

        /**
         * @throws UnsupportedOperationException {@inheritDoc}
         * @throws ClassCastException {@inheritDoc}
         * @throws NullPointerException {@inheritDoc}
         * @throws IllegalArgumentException {@inheritDoc}
         */
        @Override
        public int drainTo(final Collection<? super QueuedIMAPProtocol> c) {
            if (c == null) {
                throw new NullPointerException();
            }
            if (c == this) {
                throw new IllegalArgumentException();
            }
            final ReentrantLock lock = this.lock;
            lock.lock();
            try {
                int n = 0;
                QueuedIMAPProtocol e;
                while ((e = q.poll()) != null) {
                    c.add(e);
                    ++n;
                }
                return n;
            } finally {
                lock.unlock();
            }
        }

        /**
         * @throws UnsupportedOperationException {@inheritDoc}
         * @throws ClassCastException {@inheritDoc}
         * @throws NullPointerException {@inheritDoc}
         * @throws IllegalArgumentException {@inheritDoc}
         */
        @Override
        public int drainTo(final Collection<? super QueuedIMAPProtocol> c, final int maxElements) {
            if (c == null) {
                throw new NullPointerException();
            }
            if (c == this) {
                throw new IllegalArgumentException();
            }
            if (maxElements <= 0) {
                return 0;
            }
            final ReentrantLock lock = this.lock;
            lock.lock();
            try {
                int n = 0;
                QueuedIMAPProtocol e;
                while (n < maxElements && (e = q.poll()) != null) {
                    c.add(e);
                    ++n;
                }
                return n;
            } finally {
                lock.unlock();
            }
        }

        /**
         * Atomically removes all of the elements from this queue. The queue will be empty after this call returns.
         */
        @Override
        public void clear() {
            final ReentrantLock lock = this.lock;
            lock.lock();
            try {
                q.clear();
            } finally {
                lock.unlock();
            }
        }

        /**
         * Returns an array containing all of the elements in this queue; the runtime type of the returned array is that of the specified
         * array. The returned array elements are in no particular order. If the queue fits in the specified array, it is returned therein.
         * Otherwise, a new array is allocated with the runtime type of the specified array and the size of this queue.
         * <p>
         * If this queue fits in the specified array with room to spare (i.e., the array has more elements than this queue), the element in
         * the array immediately following the end of the queue is set to <tt>null</tt>.
         * <p>
         * Like the {@link #toArray()} method, this method acts as bridge between array-based and collection-based APIs. Further, this
         * method allows precise control over the runtime type of the output array, and may, under certain circumstances, be used to save
         * allocation costs.
         * <p>
         * Suppose <tt>x</tt> is a queue known to contain only strings. The following code can be used to dump the queue into a newly
         * allocated array of <tt>String</tt>:
         *
         * <pre>
         *
         *
         *
         * String[] y = x.toArray(new String[0]);
         * </pre>
         *
         * Note that <tt>toArray(new Object[0])</tt> is identical in function to <tt>toArray()</tt>.
         *
         * @param a the array into which the elements of the queue are to be stored, if it is big enough; otherwise, a new array of the same
         *            runtime type is allocated for this purpose
         * @return an array containing all of the elements in this queue
         * @throws ArrayStoreException if the runtime type of the specified array is not a supertype of the runtime type of every element in
         *             this queue
         * @throws NullPointerException if the specified array is null
         */
        @Override
        public <T> T[] toArray(final T[] a) {
            final ReentrantLock lock = this.lock;
            lock.lock();
            try {
                return q.toArray(a);
            } finally {
                lock.unlock();
            }
        }

        /**
         * Returns an iterator over the elements in this queue. The iterator does not return the elements in any particular order. The
         * returned <tt>Iterator</tt> is a "weakly consistent" iterator that will never throw {@link ConcurrentModificationException}, and
         * guarantees to traverse elements as they existed upon construction of the iterator, and may (but is not guaranteed to) reflect any
         * modifications subsequent to construction.
         *
         * @return an iterator over the elements in this queue
         */
        @Override
        public Iterator<QueuedIMAPProtocol> iterator() {
            return new Itr(toArray());
        }

        /**
         * Snapshot iterator that works off copy of underlying q array.
         */
        private class Itr implements Iterator<QueuedIMAPProtocol> {

            final Object[] array; // Array of all elements
            int cursor; // index of next element to return;
            int lastRet; // index of last element, or -1 if no such

            Itr(final Object[] array) {
                lastRet = -1;
                this.array = array;
            }

            @Override
            public boolean hasNext() {
                return cursor < array.length;
            }

            @SuppressWarnings("unchecked")
            @Override
            public QueuedIMAPProtocol next() {
                if (cursor >= array.length) {
                    throw new NoSuchElementException();
                }
                lastRet = cursor;
                return (QueuedIMAPProtocol) array[cursor++];
            }

            @Override
            public void remove() {
                if (lastRet < 0) {
                    throw new IllegalStateException();
                }
                final Object x = array[lastRet];
                lastRet = -1;
                // Traverse underlying queue to find == element,
                // not just a .equals element.
                lock.lock();
                try {
                    for (final Iterator<QueuedIMAPProtocol> it = q.iterator(); it.hasNext();) {
                        if (it.next() == x) {
                            it.remove();
                            return;
                        }
                    }
                } finally {
                    lock.unlock();
                }
            }
        }

        /**
         * Saves the state to a stream (that is, serializes it). This merely wraps default serialization within lock. The serialization
         * strategy for items is left to underlying Queue. Note that locking is not needed on deserialization, so readObject is not defined,
         * just relying on default.
         */
        private void writeObject(final java.io.ObjectOutputStream s) throws java.io.IOException {
            lock.lock();
            try {
                s.defaultWriteObject();
            } finally {
                lock.unlock();
            }
        }

    } // End of class CountingQueue

    static final class ThreadTrace {

        final QueuedIMAPProtocol protocol;
        final long stamp;

        /**
         * Initializes a new {@link ThreadTrace}.
         */
        ThreadTrace(final QueuedIMAPProtocol protocol, final long stamp) {
            super();
            this.protocol = protocol;
            this.stamp = stamp;
        }
    } // End of class ThreadTrace

    static class DeprecatedQueueException extends RuntimeException {

        /**
         * Constructs an DeprecatedQueueException with no detail message. A detail message is a String that describes this particular
         * exception.
         */
        DeprecatedQueueException() {
            super();
        }

        /**
         * Constructs an DeprecatedQueueException with the specified detail message. A detail message is a String that describes this
         * particular exception.
         *
         * @param s The String that contains a detailed message
         */
        DeprecatedQueueException(String s) {
            super(s);
        }

        /**
         * Constructs a new exception with the specified detail message and cause.
         * <p>
         * Note that the detail message associated with <code>cause</code> is <i>not</i> automatically incorporated in this exception's
         * detail message.
         *
         * @param message The detail message (which is saved for later retrieval by the {@link Throwable#getMessage()} method).
         * @param cause The cause (which is saved for later retrieval by the {@link Throwable#getCause()} method). (A <tt>null</tt> value is
         *            permitted, and indicates that the cause is nonexistent or unknown.)
         */
        DeprecatedQueueException(String message, Throwable cause) {
            super(message, cause);
        }

        /**
         * Constructs a new exception with the specified cause and a detail message of <tt>(cause==null ? null : cause.toString())</tt>
         * (which typically contains the class and detail message of <tt>cause</tt>). This constructor is useful for exceptions that are
         * little more than wrappers for other throwables (for example, {@link java.security.PrivilegedActionException}).
         *
         * @param cause The cause (which is saved for later retrieval by the {@link Throwable#getCause()} method). (A <tt>null</tt> value is
         *            permitted, and indicates that the cause is nonexistent or unknown.)
         */
        DeprecatedQueueException(Throwable cause) {
            super(cause);
        }

        static final long serialVersionUID = -1848914673093228596L;
    } // End of class DeprecatedQueueException

}
