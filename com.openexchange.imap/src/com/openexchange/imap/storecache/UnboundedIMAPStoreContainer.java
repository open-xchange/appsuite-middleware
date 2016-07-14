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

import java.util.AbstractQueue;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.PriorityQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import javax.mail.MessagingException;
import com.openexchange.imap.IMAPClientParameters;
import com.openexchange.log.LogProperties;
import com.openexchange.session.Session;
import com.sun.mail.imap.IMAPStore;

/**
 * {@link UnboundedIMAPStoreContainer} - The unbounded {@link IMAPStoreContainer}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class UnboundedIMAPStoreContainer extends AbstractIMAPStoreContainer {

    private final InheritedPriorityBlockingQueue availableQueue;

    protected final String server;
    protected final int port;
    protected final int maxRetryCount;
    private final AtomicInteger inUseCount;
    private final boolean checkConnectivityIfPolled;

    /**
     * Initializes a new {@link UnboundedIMAPStoreContainer}.
     */
    public UnboundedIMAPStoreContainer(int accountId, Session session, String server, int port, boolean propagateClientIp, boolean checkConnectivityIfPolled) {
        super(accountId, session, propagateClientIp);
        maxRetryCount = 10;
        availableQueue = new InheritedPriorityBlockingQueue();
        this.port = port;
        this.server = server;
        inUseCount = new AtomicInteger();
        this.checkConnectivityIfPolled = checkConnectivityIfPolled;
    }

    /**
     * Gets the backing blocking queue.
     *
     * @return The queue
     */
    protected BlockingQueue<IMAPStoreWrapper> getQueue() {
        return availableQueue;
    }

    @Override
    public int getInUseCount() {
        return inUseCount.get();
    }

    @Override
    public IMAPStore getStore(javax.mail.Session imapSession, String login, String pw, Session session) throws MessagingException, InterruptedException {
        IMAPStore imapStore = pollOrCreateIMAPStore(imapSession, login, pw, session);
        inUseCount.incrementAndGet();
        return imapStore;
    }

    private IMAPStore pollOrCreateIMAPStore(javax.mail.Session imapSession, String login, String pw, Session session) throws MessagingException {
        IMAPStoreWrapper imapStoreWrapper = availableQueue.poll();
        if (null == imapStoreWrapper) {
            IMAPStore imapStore = newStore(server, port, login, pw, imapSession, session);
            LOG.debug("UnboundedIMAPStoreContainer.getStore(): Returning newly established IMAPStore instance. {} -- {}", imapStore.toString(), imapStore.hashCode());
            return imapStore;
        }

        // Polled an existing instance
        IMAPStore imapStore = imapStoreWrapper.imapStore;

        if (checkConnectivityIfPolled && (false == imapStore.isConnected())) {
            // IMAPStore instance is no more connected
            imapStore = newStore(server, port, login, pw, imapSession, session);
            LOG.debug("UnboundedIMAPStoreContainer.getStore(): Returning newly established IMAPStore instance. {} -- {}", imapStore.toString(), imapStore.hashCode());
            return imapStore;
        }

        // Grab associated IMAP session identifier (as advertised via "ID" command)
        String sessionInformation = imapStore.getClientParameter(IMAPClientParameters.SESSION_ID.getParamName());
        if (null != sessionInformation) {
            LogProperties.put(LogProperties.Name.MAIL_SESSION, sessionInformation);
        }

        // Should we set properties from passed session?
        // imapStore.getServiceSession().getProperties().putAll(imapSession.getProperties());
        // imapStore.setPropagateClientIpAddress(imapSession.getProperty("mail.imap.propagate.clientipaddress"));
        LOG.debug("IMAPStoreContainer.getStore(): Returning _cached_ IMAPStore instance. {} -- {}", imapStore.toString(), imapStore.hashCode());
        return imapStore;
    }

    @Override
    public void backStore(final IMAPStore imapStore) {
        backStoreNoValidityCheck(imapStore);
        inUseCount.decrementAndGet();
    }

    protected void backStoreNoValidityCheck(final IMAPStore imapStore) {
        if (!availableQueue.offer(new IMAPStoreWrapper(imapStore))) {
            closeSafe(imapStore);
        } else {
            // System.out.println("IMAPStoreContainer.backStore(): Added IMAPStore instance to cache." + imapStore.toString() + " -- " +
            // imapStore.hashCode());
            LOG.debug("IMAPStoreContainer.backStore(): Added IMAPStore instance to cache. {} -- {}", imapStore.toString(), imapStore.hashCode());
        }
    }

    @Override
    public void closeElapsed(final long stamp, final StringBuilder debugBuilder) {
        LOG.debug("IMAPStoreContainer.closeElapsed(): {} IMAPStore instances in queue for {}:{}", availableQueue.size(), server, port);
        IMAPStoreWrapper imapStoreWrapper;
        do {
            imapStoreWrapper = availableQueue.pollIfElapsed(stamp);
            if (null == imapStoreWrapper) {
                return;
            }
            try {
                if (null == debugBuilder) {
                    // System.out.println("IMAPStoreContainer.closeElapsed(): Closing elapsed IMAP store: " +
                    // imapStoreWrapper.imapStore.toString() + "-" + imapStoreWrapper.imapStore.hashCode());
                    closeSafe(imapStoreWrapper.imapStore);
                } else {
                    final String info = imapStoreWrapper.imapStore.toString() + " -- " + imapStoreWrapper.imapStore.hashCode();
                    closeSafe(imapStoreWrapper.imapStore);
                    debugBuilder.setLength(0);
                    LOG.debug(debugBuilder.append("IMAPStoreContainer.closeElapsed(): Closed elapsed IMAP store: ").append(info).toString());
                }
            } catch (final IllegalStateException e) {
                // Ignore
            }
        } while (true);
    }

    @Override
    public void clear() {
        InheritedPriorityBlockingQueue availableQueue = this.availableQueue;
        for (IMAPStoreWrapper wrapper; (wrapper = availableQueue.poll()) != null;) {
            closeSafe(wrapper.imapStore);
        }
    }

    @Override
    public boolean hasElapsed(long millis) {
        return availableQueue.hasElapsed(millis);
    }

    private static class InheritedPriorityBlockingQueue extends AbstractQueue<IMAPStoreWrapper> implements BlockingQueue<IMAPStoreWrapper>, java.io.Serializable {

        private static final long serialVersionUID = 1337510919245408276L;

        final transient PriorityQueue<IMAPStoreWrapper> q;
        final ReentrantLock lock;
        private final Condition notEmpty;

        /**
         * Creates a <tt>InheritedPriorityBlockingQueue</tt> with the default initial capacity (11) that orders its elements according to
         * their {@linkplain Comparable natural ordering}.
         */
        protected InheritedPriorityBlockingQueue() {
            super();
            q = new PriorityQueue<IMAPStoreWrapper>();
            lock = new ReentrantLock(true);
            notEmpty = lock.newCondition();
        }

        public boolean hasElapsed(long millis) {
            final ReentrantLock lock = this.lock;
            lock.lock();
            try {
                final IMAPStoreWrapper e = q.peek();
                return (null != e && e.lastAccessed < millis);
            } finally {
                lock.unlock();
            }
        }

        @Override
        public boolean add(final IMAPStoreWrapper e) {
            return offer(e);
        }

        @Override
        public boolean offer(final IMAPStoreWrapper e) {
            final ReentrantLock lock = this.lock;
            lock.lock();
            try {
                final boolean ok = q.offer(e);
                assert ok;
                notEmpty.signal();
                return true;
            } finally {
                lock.unlock();
            }
        }

        @Override
        public void put(final IMAPStoreWrapper e) {
            offer(e);
        }

        @Override
        public boolean offer(final IMAPStoreWrapper e, final long timeout, final TimeUnit unit) {
            return offer(e);
        }

        /**
         * Retrieves and removes the head of this queue if elapsed compared to given time stamp, or returns <code>null</code> if this queue
         * is empty or head is not elapsed.
         *
         * @param stamp The time stamp
         * @return The elapsed head of this queue
         */
        public IMAPStoreWrapper pollIfElapsed(final long stamp) {
            final ReentrantLock lock = this.lock;
            lock.lock();
            try {
                final IMAPStoreWrapper e = q.peek();
                if ((null != e) && (e.lastAccessed < stamp)) {
                    return q.poll();
                }
                return null;
            } finally {
                lock.unlock();
            }
        }

        @Override
        public IMAPStoreWrapper poll() {
            final ReentrantLock lock = this.lock;
            lock.lock();
            try {
                return q.poll();
            } finally {
                lock.unlock();
            }
        }

        @Override
        public IMAPStoreWrapper take() throws InterruptedException {
            final ReentrantLock lock = this.lock;
            lock.lockInterruptibly();
            try {
                try {
                    while (q.size() == 0) {
                        notEmpty.await();
                    }
                } catch (final InterruptedException ie) {
                    notEmpty.signal(); // propagate to non-interrupted thread
                    throw ie;
                }
                final IMAPStoreWrapper x = q.poll();
                assert x != null;
                return x;
            } finally {
                lock.unlock();
            }
        }

        /**
         * Awaits until an element arrives in queue, waiting up to the specified wait time if necessary for an element to become available.
         *
         * @param timeout How long to wait before giving up, in units of <tt>unit</tt>
         * @param unit A <tt>TimeUnit</tt> determining how to interpret the <tt>timeout</tt> parameter
         * @return <code>true</code> if an element arrived in queue before time elapsed; otherwise <code>false</code> to signal time out
         * @throws InterruptedException If interrupted while waiting
         */
        public boolean awaitNotEmpty(final long timeout, final TimeUnit unit) throws InterruptedException {
            long nanos = unit.toNanos(timeout);
            final ReentrantLock lock = this.lock;
            lock.lockInterruptibly();
            try {
                while (q.size() == 0) {
                    try {
                        nanos = notEmpty.awaitNanos(nanos);
                    } catch (final InterruptedException ie) {
                        notEmpty.signal(); // propagate to non-interrupted thread
                        throw ie;
                    }
                    if (nanos <= 0L) {
                        // A value less than or equal to zero indicates that no time remains.
                        return false;
                    }
                }
            } finally {
                lock.unlock();
            }
            return true;
        }

        @Override
        public IMAPStoreWrapper poll(final long timeout, final TimeUnit unit) throws InterruptedException {
            long nanos = unit.toNanos(timeout);
            final ReentrantLock lock = this.lock;
            lock.lockInterruptibly();
            try {
                for (;;) {
                    final IMAPStoreWrapper x = q.poll();
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
        public IMAPStoreWrapper peek() {
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

        @Override
        public int remainingCapacity() {
            return Integer.MAX_VALUE;
        }

        @Override
        public boolean remove(final Object o) {
            final ReentrantLock lock = this.lock;
            lock.lock();
            try {
                return q.remove(o);
            } finally {
                lock.unlock();
            }
        }

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

        @Override
        public int drainTo(final Collection<? super IMAPStoreWrapper> c) {
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
                IMAPStoreWrapper e;
                while ((e = q.poll()) != null) {
                    c.add(e);
                    ++n;
                }
                return n;
            } finally {
                lock.unlock();
            }
        }

        @Override
        public int drainTo(final Collection<? super IMAPStoreWrapper> c, final int maxElements) {
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
                IMAPStoreWrapper e;
                while (n < maxElements && (e = q.poll()) != null) {
                    c.add(e);
                    ++n;
                }
                return n;
            } finally {
                lock.unlock();
            }
        }

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

        @Override
        public Iterator<IMAPStoreWrapper> iterator() {
            return new Itr(toArray());
        }

        /**
         * Snapshot iterator that works off copy of underlying q array.
         */
        private class Itr implements Iterator<IMAPStoreWrapper> {

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

            @Override
            public IMAPStoreWrapper next() {
                if (cursor >= array.length) {
                    throw new NoSuchElementException();
                }
                lastRet = cursor;
                return (IMAPStoreWrapper) array[cursor++];
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
                    for (final Iterator<IMAPStoreWrapper> it = q.iterator(); it.hasNext();) {
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

        private void writeObject(final java.io.ObjectOutputStream s) throws java.io.IOException {
            lock.lock();
            try {
                s.defaultWriteObject();
            } finally {
                lock.unlock();
            }
        }

    } // End of class

}
