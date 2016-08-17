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

package com.openexchange.drive.impl.checksum.events;

import java.util.AbstractQueue;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * {@link DelayedChecksumInvalidationQueue}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public final class DelayedChecksumInvalidationQueue extends AbstractQueue<DelayedChecksumInvalidation> implements BlockingQueue<DelayedChecksumInvalidation> {

    transient final ReentrantLock lock = new ReentrantLock();
    private transient final Condition available = lock.newCondition();
    final PriorityQueue<DelayedChecksumInvalidation> q = new PriorityQueue<DelayedChecksumInvalidation>();
    final Set<DelayedChecksumInvalidation> contained = new HashSet<DelayedChecksumInvalidation>(64);

    /**
     * Creates a new <tt>DelayedStoreOpDelayQueue</tt> that is initially empty.
     */
    public DelayedChecksumInvalidationQueue() {
        super();
    }

    /**
     * Creates a <tt>DelayedStoreOpDelayQueue</tt> initially containing the elements of the given collection of {@link Delayed} instances.
     *
     * @param c the collection of elements to initially contain
     * @throws NullPointerException if the specified collection or any of its elements are null
     */
    public DelayedChecksumInvalidationQueue(final Collection<? extends DelayedChecksumInvalidation> c) {
        this.addAll(c);
    }

    /**
     * Inserts the specified element into this delay queue.
     *
     * @param e the element to add
     * @return <tt>true</tt> (as specified by {@link Collection#add})
     * @throws NullPointerException if the specified element is null
     */
    @Override
    public boolean add(final DelayedChecksumInvalidation e) {
        return offer(e);
    }

    /**
     * Inserts the specified element into this delay queue.
     *
     * @param e the element to add
     * @return <tt>true</tt>
     * @throws NullPointerException if the specified element is null
     */
    @Override
    public boolean offer(final DelayedChecksumInvalidation e) {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            if (contained.contains(e)) {
                return true;
            }
            final DelayedChecksumInvalidation first = q.peek();
            q.offer(e);
            contained.add(e);
            if (first == null || e.compareTo(first) < 0) {
                available.signalAll();
            }
            return true;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Inserts the specified element into this delay queue. As the queue is unbounded this method will never block.
     *
     * @param e the element to add
     * @throws NullPointerException {@inheritDoc}
     */
    @Override
    public void put(final DelayedChecksumInvalidation e) {
        offer(e);
    }

    /**
     * Inserts the specified element into this delay queue. As the queue is unbounded this method will never block.
     *
     * @param e the element to add
     * @param timeout This parameter is ignored as the method never blocks
     * @param unit This parameter is ignored as the method never blocks
     * @return <tt>true</tt>
     * @throws NullPointerException {@inheritDoc}
     */
    @Override
    public boolean offer(final DelayedChecksumInvalidation e, final long timeout, final TimeUnit unit) {
        return offer(e);
    }

    /**
     * Retrieves and removes the head of this queue, or returns <tt>null</tt> if this queue has no elements with an expired delay.
     *
     * @return the head of this queue, or <tt>null</tt> if this queue has no elements with an expired delay
     */
    @Override
    public DelayedChecksumInvalidation poll() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            final DelayedChecksumInvalidation first = q.peek();
            if (first == null || first.getDelay(TimeUnit.NANOSECONDS) > 0) {
                return null;
            }
            // Delay exceeded
            final DelayedChecksumInvalidation x = q.poll();
            assert x != null;
            contained.remove(x);
            if (q.size() != 0) {
                available.signalAll();
            }
            return x;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Retrieves and removes the head of this queue, waiting if necessary until an element with an expired delay is available on this queue.
     *
     * @return the head of this queue
     * @throws InterruptedException {@inheritDoc}
     */
    @Override
    public DelayedChecksumInvalidation take() throws InterruptedException {
        final ReentrantLock lock = this.lock;
        lock.lockInterruptibly();
        try {
            for (;;) {
                final DelayedChecksumInvalidation first = q.peek();
                if (first == null) {
                    available.await();
                } else {
                    final long delay = first.getDelay(TimeUnit.NANOSECONDS);
                    if (delay > 0) {
                        /*final long tl = */available.awaitNanos(delay);
                    } else {
                        final DelayedChecksumInvalidation x = q.poll();
                        assert x != null;
                        contained.remove(x);
                        if (q.size() != 0) {
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
     * Retrieves and removes the head of this queue, waiting if necessary until an element with an expired delay is available on this queue,
     * or the specified wait time expires.
     *
     * @return the head of this queue, or <tt>null</tt> if the specified waiting time elapses before an element with an expired delay
     *         becomes available
     * @throws InterruptedException {@inheritDoc}
     */
    @Override
    public DelayedChecksumInvalidation poll(final long timeout, final TimeUnit unit) throws InterruptedException {
        long nanos = unit.toNanos(timeout);
        final ReentrantLock lock = this.lock;
        lock.lockInterruptibly();
        try {
            for (;;) {
                final DelayedChecksumInvalidation first = q.peek();
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
                        final DelayedChecksumInvalidation x = q.poll();
                        assert x != null;
                        contained.remove(x);
                        if (q.size() != 0) {
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
     * Retrieves, but does not remove, the head of this queue, or returns <tt>null</tt> if this queue is empty. Unlike <tt>poll</tt>, if no
     * expired elements are available in the queue, this method returns the element that will expire next, if one exists.
     *
     * @return the head of this queue, or <tt>null</tt> if this queue is empty.
     */
    @Override
    public DelayedChecksumInvalidation peek() {
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
     * @throws UnsupportedOperationException {@inheritDoc}
     * @throws ClassCastException {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     * @throws IllegalArgumentException {@inheritDoc}
     */
    @Override
    public int drainTo(final Collection<? super DelayedChecksumInvalidation> c) {
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
                final DelayedChecksumInvalidation first = q.peek();
                if (first == null || first.getDelay(TimeUnit.NANOSECONDS) > 0) {
                    break;
                }
                final DelayedChecksumInvalidation polled = q.poll();
                contained.remove(polled);
                c.add(polled);
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
     * @throws UnsupportedOperationException {@inheritDoc}
     * @throws ClassCastException {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     * @throws IllegalArgumentException {@inheritDoc}
     */
    @Override
    public int drainTo(final Collection<? super DelayedChecksumInvalidation> c, final int maxElements) {
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
                final DelayedChecksumInvalidation first = q.peek();
                if (first == null || first.getDelay(TimeUnit.NANOSECONDS) > 0) {
                    break;
                }
                final DelayedChecksumInvalidation polled = q.poll();
                contained.remove(polled);
                c.add(polled);
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
     * Atomically removes all of the elements from this delay queue. The queue will be empty after this call returns. Elements with an
     * unexpired delay are not waited for; they are simply discarded from the queue.
     */
    @Override
    public void clear() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            q.clear();
            contained.clear();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Always returns <tt>Integer.MAX_VALUE</tt> because a <tt>DelayedStoreOpDelayQueue</tt> is not capacity constrained.
     *
     * @return <tt>Integer.MAX_VALUE</tt>
     */
    @Override
    public int remainingCapacity() {
        return Integer.MAX_VALUE;
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

    /**
     * Returns an array containing all of the elements in this queue; the runtime type of the returned array is that of the specified array.
     * The returned array elements are in no particular order. If the queue fits in the specified array, it is returned therein. Otherwise,
     * a new array is allocated with the runtime type of the specified array and the size of this queue.
     * <p>
     * If this queue fits in the specified array with room to spare (i.e., the array has more elements than this queue), the element in the
     * array immediately following the end of the queue is set to <tt>null</tt>.
     * <p>
     * Like the {@link #toArray()} method, this method acts as bridge between array-based and collection-based APIs. Further, this method
     * allows precise control over the runtime type of the output array, and may, under certain circumstances, be used to save allocation
     * costs.
     * <p>
     * The following code can be used to dump a delay queue into a newly allocated array of <tt>Delayed</tt>:
     *
     * <pre>
     *
     * Delayed[] a = q.toArray(new Delayed[0]);
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
     * Removes a single instance of the specified element from this queue, if it is present, whether or not it has expired.
     */
    @Override
    public boolean remove(final Object o) {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            final boolean removed = q.remove(o);
            if (removed) {
                contained.remove(o);
            }
            return removed;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Returns an iterator over all the elements (both expired and unexpired) in this queue. The iterator does not return the elements in
     * any particular order. The returned <tt>Iterator</tt> is a "weakly consistent" iterator that will never throw
     * {@link ConcurrentModificationException}, and guarantees to traverse elements as they existed upon construction of the iterator, and
     * may (but is not guaranteed to) reflect any modifications subsequent to construction.
     *
     * @return an iterator over the elements in this queue
     */
    @Override
    public Iterator<DelayedChecksumInvalidation> iterator() {
        return new Itr(toArray());
    }

    /**
     * Snapshot iterator that works off copy of underlying q array.
     */
    private class Itr implements Iterator<DelayedChecksumInvalidation> {

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
        public DelayedChecksumInvalidation next() {
            if (cursor >= array.length) {
                throw new NoSuchElementException();
            }
            lastRet = cursor;
            return (DelayedChecksumInvalidation) array[cursor++];
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
                for (final Iterator<DelayedChecksumInvalidation> it = q.iterator(); it.hasNext();) {
                    if (it.next() == x) {
                        it.remove();
                        contained.remove(x);
                        return;
                    }
                }
            } finally {
                lock.unlock();
            }
        }
    }

}
