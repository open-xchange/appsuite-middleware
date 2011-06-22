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

package com.openexchange.mail.cache;

import java.util.AbstractQueue;
import java.util.Collection;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * {@link MailAccessQueue} - A {@link BlockingQueue} providing behavior similar to {@link java.util.concurrent.DelayQueue} with its
 * <code>xxxDelayed()</code> methods.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MailAccessQueue extends AbstractQueue<PooledMailAccess> implements BlockingQueue<PooledMailAccess> {

    private transient final ReentrantLock lock = new ReentrantLock();

    private transient final Condition available = lock.newCondition();

    /**
     * The backing priority queue.
     */
    private final PriorityQueue<PooledMailAccess> priorityQueue;

    /**
     * The deprecated flag;
     */
    private final AtomicBoolean deprecated;

    /**
     * Creates a new <tt>MailAccessQueue</tt> that is initially empty.
     */
    public MailAccessQueue() {
        super();
        priorityQueue = new PriorityQueue<PooledMailAccess>();
        deprecated = new AtomicBoolean();
    }

    /**
     * Marks this queue as deprecated.
     * 
     * @return <code>true</code> if queue has been successfully marked as deprecated; otherwise <code>false</code>
     */
    public boolean markDeprecated() {
        return deprecated.compareAndSet(false, true);
    }

    /**
     * Checks if this queue is deprecated.
     * 
     * @return <code>true</code> if this queue is deprecated; otherwise <code>false</code>
     */
    public boolean isDeprecated() {
        return deprecated.get();
    }

    /**
     * Inserts the specified pooled mail access into this delay queue.
     * 
     * @param pooledMailAccess The pooled mail access to add
     * @return <tt>true</tt>
     * @throws NullPointerException if the specified pooled mail access is <tt>null</tt>.
     */
    public boolean offer(final PooledMailAccess pooledMailAccess) {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            final PooledMailAccess first = priorityQueue.peek();
            priorityQueue.offer(pooledMailAccess);
            if (first == null) {
                available.signalAll();
            }
            return true;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Adds the specified pooled mail access to this delay queue. As the queue is unbounded this method will never block.
     * 
     * @param pooledMailAccess The pooled mail access to add
     * @throws NullPointerException if the specified pooled mail access is <tt>null</tt>.
     */
    public void put(final PooledMailAccess pooledMailAccess) {
        offer(pooledMailAccess);
    }

    /**
     * Inserts the specified pooled mail access into this delay queue. As the queue is unbounded this method will never block.
     * 
     * @param pooledMailAccess the pooled mail access to add
     * @param timeout This parameter is ignored as the method never blocks
     * @param unit This parameter is ignored as the method never blocks
     * @return <tt>true</tt>
     * @throws NullPointerException if the specified pooled mail access is <tt>null</tt>.
     */
    public boolean offer(final PooledMailAccess pooledMailAccess, final long timeout, final TimeUnit unit) {
        return offer(pooledMailAccess);
    }

    /**
     * Adds the specified pooled mail access to this queue.
     * 
     * @param pooledMailAccess the pooled mail access to add
     * @return <tt>true</tt> (as per the general contract of <tt>Collection.add</tt>).
     * @throws NullPointerException if the specified pooled mail access is <tt>null</tt>.
     */
    @Override
    public boolean add(final PooledMailAccess pooledMailAccess) {
        return offer(pooledMailAccess);
    }

    /**
     * Retrieves and removes the head of this queue, waiting if no pooled mail accesses with an unexpired delay are present on this queue.
     * 
     * @return the head of this queue
     * @throws InterruptedException if interrupted while waiting.
     */
    public PooledMailAccess take() throws InterruptedException {
        final ReentrantLock lock = this.lock;
        lock.lockInterruptibly();
        try {
            for (;;) {
                final PooledMailAccess first = priorityQueue.peek();
                if (first == null) {
                    available.await();
                } else {
                    final PooledMailAccess x = priorityQueue.poll();
                    assert x != null;
                    if (priorityQueue.size() != 0) {
                        available.signalAll(); // wake up other takers
                    }
                    return x;
                }
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Retrieves and removes the head of this queue, waiting if no pooled mail accesses with an unexpired delay are present on this queue.
     * 
     * @return the head of this queue
     * @throws InterruptedException if interrupted while waiting.
     */
    public PooledMailAccess takeDelayed() throws InterruptedException {
        final ReentrantLock lock = this.lock;
        lock.lockInterruptibly();
        try {
            for (;;) {
                final PooledMailAccess first = priorityQueue.peek();
                if (first == null) {
                    available.await();
                } else {
                    final long delay = first.getDelay(TimeUnit.NANOSECONDS);
                    if (delay > 0) {
                        available.awaitNanos(delay);
                    } else {
                        final PooledMailAccess x = priorityQueue.poll();
                        assert x != null;
                        if (priorityQueue.size() != 0) {
                            available.signalAll(); // wake up other takers
                        }
                        return x;

                    }
                }
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Retrieves and removes the head of this queue, waiting if necessary up to the specified wait time if no elements with expired delay
     * are present on this queue.
     * 
     * @param timeout how long to wait before giving up, in units of <tt>unit</tt>
     * @param unit a <tt>TimeUnit</tt> determining how to interpret the <tt>timeout</tt> parameter
     * @return the head of this queue, or <tt>null</tt> if the specified waiting time elapses before an pooled mail access with an unexpired
     *         delay is present.
     * @throws InterruptedException if interrupted while waiting.
     */
    public PooledMailAccess poll(final long timeout, final TimeUnit unit) throws InterruptedException {
        final ReentrantLock lock = this.lock;
        lock.lockInterruptibly();
        long nanos = unit.toNanos(timeout);
        try {
            for (;;) {
                final PooledMailAccess first = priorityQueue.peek();
                if (first == null) {
                    if (nanos <= 0) {
                        return null;
                    }
                    nanos = available.awaitNanos(nanos);
                } else {
                    final PooledMailAccess x = priorityQueue.poll();
                    assert x != null;
                    if (priorityQueue.size() != 0) {
                        available.signalAll();
                    }
                    return x;
                }
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Retrieves and removes the head of this queue, waiting if necessary up to the specified wait time if no elements with an unexpired
     * delay are present on this queue.
     * 
     * @param timeout how long to wait before giving up, in units of <tt>unit</tt>
     * @param unit a <tt>TimeUnit</tt> determining how to interpret the <tt>timeout</tt> parameter
     * @return the head of this queue, or <tt>null</tt> if the specified waiting time elapses before an pooled mail access with an unexpired
     *         delay is present.
     * @throws InterruptedException if interrupted while waiting.
     */
    public PooledMailAccess pollDelayed(final long timeout, final TimeUnit unit) throws InterruptedException {
        final ReentrantLock lock = this.lock;
        lock.lockInterruptibly();
        long nanos = unit.toNanos(timeout);
        try {
            for (;;) {
                final PooledMailAccess first = priorityQueue.peek();
                if (first == null) {
                    if (nanos <= 0) {
                        return null;
                    }
                    nanos = available.awaitNanos(nanos);
                } else {
                    long delay = first.getDelay(TimeUnit.NANOSECONDS);
                    if (delay > 0) {
                        if (nanos <= 0) {
                            return null;
                        }
                        if (delay > nanos) {
                            delay = nanos;
                        }
                        final long timeLeft = available.awaitNanos(delay);
                        nanos -= delay - timeLeft;
                    } else {
                        final PooledMailAccess x = priorityQueue.poll();
                        assert x != null;
                        if (!priorityQueue.isEmpty()) {
                            available.signalAll();
                        }
                        return x;
                    }
                }
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Retrieves and removes the head of this queue (<em>least</em> pooled mail access), or <tt>null</tt> if this queue is empty.
     * 
     * @return the head of this queue, or <tt>null</tt> if this queue is empty
     */
    public PooledMailAccess poll() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            final PooledMailAccess first = priorityQueue.peek();
            if (first == null) {
                return null;
            }
            final PooledMailAccess x = priorityQueue.poll();
            assert x != null;
            if (!priorityQueue.isEmpty()) {
                available.signalAll();
            }
            return x;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Retrieves and removes the head of this queue, or <tt>null</tt> if this queue has no elements with an unexpired delay.
     * 
     * @return the head of this queue, or <tt>null</tt> if this queue has no elements with an unexpired delay.
     */
    public PooledMailAccess pollDelayed() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            final PooledMailAccess first = priorityQueue.peek();
            if (first == null || first.getDelay(TimeUnit.NANOSECONDS) > 0) {
                return null;
            }
            final PooledMailAccess x = priorityQueue.poll();
            assert x != null;
            if (priorityQueue.size() != 0) {
                available.signalAll();
            }
            return x;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Retrieves, but does not remove, the head of this queue, returning <tt>null</tt> if this queue has no elements with an unexpired
     * delay.
     * 
     * @return the head of this queue, or <tt>null</tt> if this queue has no elements with an unexpired delay.
     */
    public PooledMailAccess peek() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            return priorityQueue.peek();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public int size() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            return priorityQueue.size();
        } finally {
            lock.unlock();
        }
    }

    public int drainTo(final Collection<? super PooledMailAccess> c) {
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
            for (;;) {
                final PooledMailAccess first = priorityQueue.peek();
                if (first == null || first.getDelay(TimeUnit.NANOSECONDS) > 0) {
                    break;
                }
                c.add(priorityQueue.poll());
                ++n;
            }
            if (n > 0) {
                available.signalAll();
            }
            return n;
        } finally {
            lock.unlock();
        }
    }

    public int drainTo(final Collection<? super PooledMailAccess> c, final int maxElements) {
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
            while (n < maxElements) {
                final PooledMailAccess first = priorityQueue.peek();
                if (first == null || first.getDelay(TimeUnit.NANOSECONDS) > 0) {
                    break;
                }
                c.add(priorityQueue.poll());
                ++n;
            }
            if (n > 0) {
                available.signalAll();
            }
            return n;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Atomically removes all of the elements from this delay queue. The queue will be empty after this call returns.
     */
    @Override
    public void clear() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            priorityQueue.clear();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Always returns <tt>Integer.MAX_VALUE</tt> because a <tt>MailAccessQueue</tt> is not capacity constrained.
     * 
     * @return <tt>Integer.MAX_VALUE</tt>
     */
    public int remainingCapacity() {
        return Integer.MAX_VALUE;
    }

    @Override
    public Object[] toArray() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            return priorityQueue.toArray();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public <T> T[] toArray(final T[] array) {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            return priorityQueue.toArray(array);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Removes a single instance of the specified pooled mail access from this queue, if it is present.
     */
    @Override
    public boolean remove(final Object o) {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            return priorityQueue.remove(o);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Returns an iterator over the elements in this queue. The iterator does not return the elements in any particular order. The returned
     * iterator is a thread-safe "fast-fail" iterator that will throw {@link java.util.ConcurrentModificationException} upon detected
     * interference.
     * 
     * @return an iterator over the elements in this queue.
     */
    @Override
    public Iterator<PooledMailAccess> iterator() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            return new Itr(priorityQueue.iterator());
        } finally {
            lock.unlock();
        }
    }

    private class Itr implements Iterator<PooledMailAccess> {

        private final Iterator<PooledMailAccess> iter;

        protected Itr(final Iterator<PooledMailAccess> i) {
            iter = i;
        }

        public boolean hasNext() {
            return iter.hasNext();
        }

        public PooledMailAccess next() {
            final ReentrantLock lock = MailAccessQueue.this.lock;
            lock.lock();
            try {
                return iter.next();
            } finally {
                lock.unlock();
            }
        }

        public void remove() {
            final ReentrantLock lock = MailAccessQueue.this.lock;
            lock.lock();
            try {
                iter.remove();
            } finally {
                lock.unlock();
            }
        }
    }

}
