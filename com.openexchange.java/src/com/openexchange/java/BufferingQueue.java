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

package com.openexchange.java;

import java.util.AbstractQueue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.PriorityQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * {@link BufferingQueue}
 * <p/>
 * Wraps an event queue as a buffer for elements that should be available in the queue after a defined timespan. Offering the same
 * elements again optionally resets the buffering time via {@link #offerIfAbsentElseReset(BufferedElement)}, up to a defined maximum
 * duration, or may replace the existing element via {@link #offerOrReplace(BufferedElement)}.
 * <p/>
 * Useful to construct send- or receive-buffers capable of eliminating or stalling multiple duplicate elements before they get available
 * for consumers.
 *
 * @param <E> the type of elements held in this collection
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.0
 */
public class BufferingQueue<E> extends AbstractQueue<E> implements BlockingQueue<E> {

    /** The lock instance */
    transient final ReentrantLock lock = new ReentrantLock();

    /** The backing priority queue */
    final PriorityQueue<BufferedElement<E>> q = new PriorityQueue<BufferedElement<E>>();

    /**
     * Thread designated to wait for the element at the head of
     * the queue.  This variant of the Leader-Follower pattern
     * (http://www.cs.wustl.edu/~schmidt/POSA/POSA2/) serves to
     * minimize unnecessary timed waiting.  When a thread becomes
     * the leader, it waits only for the next delay to elapse, but
     * other threads await indefinitely.  The leader thread must
     * signal some other thread before returning from take() or
     * poll(...), unless some other thread becomes leader in the
     * interim.  Whenever the head of the queue is replaced with
     * an element with an earlier expiration time, the leader
     * field is invalidated by being reset to null, and some
     * waiting thread, but not necessarily the current leader, is
     * signalled.  So waiting threads must be prepared to acquire
     * and lose leadership while waiting.
     */
    private Thread leader = null;

    /**
     * Condition signalled when a newer element becomes available
     * at the head of the queue or a new thread may need to
     * become leader.
     */
    private final Condition available = lock.newCondition();

    private final long defaultDelayDuration;
    private final long defaultMaxDelayDuration;

    /**
     * Creates a new {@link BufferingQueue} that is initially empty, without specific default buffer durations, i.e. each added element
     * is available immediately after adding.
     */
    public BufferingQueue() {
        this(0L, 0L);
    }

    /**
     * Creates a new {@link BufferingQueue} that is initially empty, using the supplied duration as default for newly added elements.
     *
     * @param defaultDelayDuration The delay duration (in milliseconds) to use initially and for reseting due to repeated offer operations
     */
    public BufferingQueue(long defaultDelayDuration) {
        this(defaultDelayDuration, 0L);
    }

    /**
     * Creates a new {@link BufferingQueue} that is initially empty, using the supplied durations as default for newly added elements.
     *
     * @param defaultDelayDuration The delay duration (in milliseconds) to use initially and for reseting due to repeated offer operations
     * @param defaultMaxDelayDuration The the maximum delay duration (in milliseconds) to use, independently of repeated offer operations
     */
    public BufferingQueue(long defaultDelayDuration, long defaultMaxDelayDuration) {
        super();
        this.defaultDelayDuration = defaultDelayDuration;
        this.defaultMaxDelayDuration = defaultMaxDelayDuration;
    }

    /**
     * Creates a {@link BufferingQueue} initially containing the elements of the given collection, without specific default durations.
     *
     * @param c the collection of elements to initially contain
     * @throws NullPointerException if the specified collection or any of its elements are null
     */
    public BufferingQueue(Collection<? extends E> c) {
        this(c, 0L);
    }

    /**
     * Creates a {@link BufferingQueue} initially containing the elements of the given collection, using the supplied duration as
     * default for newly added elements.
     *
     * @param defaultDelayDuration The delay duration (in milliseconds) to use initially and for reseting due to repeated offer operations
     * @param c the collection of elements to initially contain
     * @throws NullPointerException if the specified collection or any of its elements are null
     */
    public BufferingQueue(Collection<? extends E> c, long defaultDelayDuration) {
        this(c, defaultDelayDuration, 0L);
    }

    /**
     * Creates a {@link BufferingQueue} initially containing the elements of the given collection, using the supplied durations as
     * default for newly added elements.
     *
     * @param defaultDelayDuration The delay duration (in milliseconds) to use initially and for reseting due to repeated offer operations
     * @param defaultMaxDelayDuration The the maximum delay duration (in milliseconds) to use, independently of repeated offer operations
     * @param c the collection of elements to initially contain
     * @throws NullPointerException if the specified collection or any of its elements are null
     */
    public BufferingQueue(Collection<? extends E> c, long defaultDelayDuration, long defaultMaxDelayDuration) {
        this(defaultDelayDuration, defaultMaxDelayDuration);
        this.addAll(c);
    }

    /**
     * Inserts the specified element into this delay queue.
     *
     * @param e the element to add
     * @return <code>true</code> (as specified by {@link Collection#add})
     * @throws NullPointerException if the specified element is null
     */
    @Override
    public boolean add(E e) {
        return offer(e);
    }

    /**
     * Inserts the specified element into this delay queue, using the default delay duration of the queue.
     *
     * @param e the element to add
     * @return <code>true</code>
     * @throws NullPointerException if the specified element is null
     */
    @Override
    public boolean offer(E e) {
        return offer(e, defaultDelayDuration, defaultMaxDelayDuration);
    }

    /**
     * Inserts the specified element into this delay queue, applying the supplied buffer duration for the added element.
     *
     * @param e the element to add
     * @param delayDuration The delay duration (in milliseconds) to use initially and for reseting due to repeated offer operations
     * @return <code>true</code>
     * @throws NullPointerException if the specified element is null
     */
    public boolean offer(E e, long delayDuration) {
        return offer(e, delayDuration, 0L);
    }

    /**
     * Inserts the specified element into this delay queue, applying the supplied buffer durations for the added element.
     *
     * @param e the element to add
     * @param delayDuration The delay duration (in milliseconds) to use initially and for reseting due to repeated offer operations
     * @param maxDelayDuration The the maximum delay duration (in milliseconds) to use, independently of repeated offer operations
     * @return <code>true</code>
     * @throws NullPointerException if the specified element is null
     */
    public boolean offer(E e, long delayDuration, long maxDelayDuration) {
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            BufferedElement<E> delayedE = new BufferedElement<E>(e, delayDuration, maxDelayDuration);
            q.offer(delayedE);
            if (q.peek() == delayedE) {
                leader = null;
                available.signal();
            }
            return true;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Inserts the specified element into this delay queue without delays, i.e. the element is available immediately.
     *
     * @param e the element to add
     * @return <code>true</code>
     * @throws NullPointerException if the specified element is null
     */
    public boolean offerImmediately(E e) {
        return offer(e, 0, 0);
    }

    /**
     * Inserts the specified element into this delay queue if not already present, applying the supplied buffer durations for the added
     * element.
     *
     * @param e the element to add
     * @return <tt>true</tt> if added; otherwise <code>false</code> if already contained
     * @throws NullPointerException if the specified element is <code>null</code>
     */
    public boolean offerIfAbsent(E e) {
        return offerIfAbsent(e, defaultDelayDuration, defaultMaxDelayDuration);
    }

    /**
     * Inserts the specified element into this delay queue if not already present.
     *
     * @param e the element to add
     * @param delayDuration The delay duration (in milliseconds) to use initially and for reseting due to repeated offer operations
     * @param maxDelayDuration The the maximum delay duration (in milliseconds) to use, independently of repeated offer operations
     * @return <tt>true</tt> if added; otherwise <code>false</code> if already contained
     * @throws NullPointerException if the specified element is <code>null</code>
     */
    public boolean offerIfAbsent(E e, long delayDuration, long maxDelayDuration) {
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            BufferedElement<E> delayedE = new BufferedElement<E>(e, delayDuration, maxDelayDuration);
            if (contains(delayedE)) {
                return false;
            }
            q.offer(delayedE);
            if (q.peek() == delayedE) {
                leader = null;
                available.signal();
            }
            return true;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Inserts the specified element into this delay queue if not already contained. Otherwise (meaning already contained), the
     * contained elements delay is reseted (up to contained element's defined maxDelayDuration).
     *
     * @param e The element to add
     * @return <tt>true</tt> if added; otherwise <code>false</code> if already contained
     * @throws NullPointerException if the specified element is <code>null</code>
     */
    public boolean offerIfAbsentElseReset(E e) {
        return offerIfAbsentElseReset(e, defaultDelayDuration, defaultMaxDelayDuration);
    }

    /**
     * Inserts the specified element into this delay queue if not already contained. Otherwise (meaning already contained), the
     * contained elements delay is reseted (up to contained element's defined maxDelayDuration).
     *
     * @param e The element to add
     * @param delayDuration The delay duration (in milliseconds) to use initially and for reseting due to repeated offer operations
     * @param maxDelayDuration The the maximum delay duration (in milliseconds) to use, independently of repeated offer operations
     * @return <tt>true</tt> if added; otherwise <code>false</code> if already contained
     * @throws NullPointerException if the specified element is <code>null</code>
     */
    public boolean offerIfAbsentElseReset(E e, long delayDuration, long maxDelayDuration) {
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            // Check if already contained
            BufferedElement<E> delayedE = new BufferedElement<E>(e, delayDuration, maxDelayDuration);
            {
                BufferedElement<E> prev = null;
                for (Iterator<BufferedElement<E>> it = q.iterator(); null == prev && it.hasNext();) {
                    BufferedElement<E> next = it.next();
                    if (delayedE.equals(next)) {
                        prev = next;
                        it.remove();
                    }
                }
                if (null != prev) {
                    prev.reset();
                    q.offer(prev);
                    return false;
                }
            }
            q.offer(delayedE);
            if (q.peek() == delayedE) {
                leader = null;
                available.signal();
            }
            return true;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Inserts multiple elements into this queue if not already contained. Otherwise (meaning already contained), the
     * contained elements delay is reseted (up to contained element's defined maxDelayDuration).
     *
     * @param c The elements to add
     * @return <tt>true</tt> if at least one element was added; otherwise <code>false</code> if all elements were already contained
     * @throws NullPointerException if the specified element is <code>null</code>
     */
    public boolean offerIfAbsentElseReset(Collection<? extends E> c) {
        return offerIfAbsentElseReset(c, defaultDelayDuration, defaultMaxDelayDuration);
    }

    /**
     * Inserts multiple elements into this queue if not already contained. Otherwise (meaning already contained), the
     * contained elements delay is reseted (up to contained element's defined maxDelayDuration).
     *
     * @param c The elements to add
     * @param delayDuration The delay duration (in milliseconds) to use initially and for reseting due to repeated offer operations
     * @param maxDelayDuration The the maximum delay duration (in milliseconds) to use, independently of repeated offer operations
     * @return <tt>true</tt> if at least one element was added; otherwise <code>false</code> if all elements were already contained
     * @throws NullPointerException if the specified element is <code>null</code>
     */
    public boolean offerIfAbsentElseReset(Collection<? extends E> c, long delayDuration, long maxDelayDuration) {
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            /*
             * reset already contained elements, remember elements to add
             */
            List<E> elementsToAdd = new ArrayList<E>(c);
            Iterator<BufferedElement<E>> iterator = q.iterator();
            while (iterator.hasNext()) {
                BufferedElement<E> bufferedElement = iterator.next();
                Iterator<E> elementsIterator = elementsToAdd.iterator();
                while (elementsIterator.hasNext()) {
                    E element = elementsIterator.next();
                    if (bufferedElement.equals(element)) {
                        bufferedElement.reset();
                        elementsIterator.remove();
                        break;
                    }
                }
            }
            if (elementsToAdd.isEmpty()) {
                /*
                 * all already contained
                 */
                return false;
            }
            /*
             * add not contained elements, signal as needed
             */
            boolean signal = false;
            for (E e : elementsToAdd) {
                BufferedElement<E> delayedE = new BufferedElement<E>(e, delayDuration, maxDelayDuration);
                q.offer(delayedE);
                signal |= q.peek() == delayedE;
            }
            if (signal) {
                leader = null;
                available.signal();
            }
            return true;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Inserts the specified element into this delay queue without delays, i.e. the element is available immediately. An already existing
     * element is replaced.
     *
     * @param e the element to add
     * @return The previous value if it was replaced, or <code>null</code> if there was no equal element in the queue before
     */
    public E offerOrReplaceImmediately(E e) {
        return offerOrReplace(e, 0, 0);
    }

    /**
     * Inserts the specified element into this delay queue. An already existing element is replaced.
     *
     * @param e The element to add
     * @return The previous value if it was replaced, or <code>null</code> if there was no equal element in the queue before
     */
    public E offerOrReplace(E e) {
        return offerOrReplace(e, defaultDelayDuration, defaultMaxDelayDuration);
    }

    /**
     * Inserts the specified element into this delay queue. An already existing element is replaced.
     *
     * @param e The element to add
     * @param delayDuration The delay duration (in milliseconds) to use initially and for reseting due to repeated offer operations
     * @param maxDelayDuration The the maximum delay duration (in milliseconds) to use, independently of repeated offer operations
     * @return The previous value if it was replaced, or <code>null</code> if there was no equal element in the queue before
     */
    public E offerOrReplace(E e, long delayDuration, long maxDelayDuration) {
        BufferedElement<E> delayedE = new BufferedElement<E>(e, delayDuration, maxDelayDuration);
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            // Check if already contained
            BufferedElement<E> prev = null;
            for (Iterator<BufferedElement<E>> it = q.iterator(); null == prev && it.hasNext();) {
                BufferedElement<E> next = it.next();
                if (delayedE.equals(next)) {
                    prev = next;
                    it.remove();
                }
            }
            q.offer(delayedE);
            if (q.peek() == delayedE) {
                leader = null;
                available.signal();
            }
            return null == prev ? null : prev.getElement();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Inserts the specified element into this delay queue.<br>
     * An already existing element is replaced while transferring its delay arguments to the inserted one and performing a reset.
     *
     * @param e The element to add
     * @return The previous value if it was replaced, or <code>null</code> if there was no equal element in the queue before
     * @see #transfer(Object, BufferedElement)
     */
    public E offerOrReplaceAndReset(E e) {
        return offerOrReplaceAndReset(e, defaultDelayDuration, defaultMaxDelayDuration);
    }

    /**
     * Inserts the specified element into this delay queue.<br>
     * An already existing element is replaced while transferring its delay arguments to the inserted one and performing a reset.
     *
     * @param e The element to add
     * @param delayDuration The delay duration (in milliseconds) to use initially and for reseting due to repeated offer operations
     * @param maxDelayDuration The the maximum delay duration (in milliseconds) to use, independently of repeated offer operations
     * @return The previous value if it was replaced, or <code>null</code> if there was no equal element in the queue before
     * @see #transfer(Object, BufferedElement)
     */
    public E offerOrReplaceAndReset(E e, long delayDuration, long maxDelayDuration) {
        BufferedElement<E> delayedE = new BufferedElement<E>(e, delayDuration, maxDelayDuration);
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            // Check if already contained
            BufferedElement<E> prev = null;
            for (Iterator<BufferedElement<E>> it = q.iterator(); null == prev && it.hasNext();) {
                BufferedElement<E> next = it.next();
                if (delayedE.equals(next)) {
                    prev = next;
                    delayedE = transfer(e, prev);
                    delayedE.reset(); // Resets to prev
                    it.remove();
                }
            }
            q.offer(delayedE);
            if (q.peek() == delayedE) {
                leader = null;
                available.signal();
            }
            return null == prev ? null : prev.getElement();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Transfers the element to offer to the existing <code>BufferedElement</code> wrapper.
     *
     * @param toOffer The element to offer
     * @param existing The existing <code>BufferedElement</code> wrapper
     * @return The <code>BufferedElement</code> wrapper holding the transferred element
     */
    protected BufferedElement<E> transfer(E toOffer, BufferedElement<E> existing) {
        return new BufferedElement<E>(toOffer, existing);
    }

    /**
     * Inserts the specified element into this delay queue. As the queue is
     * unbounded this method will never block.
     *
     * @param e the element to add
     * @throws NullPointerException {@inheritDoc}
     */
    @Override
    public void put(E e) {
        offer(e);
    }

    /**
     * Inserts the specified element into this delay queue. As the queue is
     * unbounded this method will never block.
     *
     * @param e the element to add
     * @param timeout This parameter is ignored as the method never blocks
     * @param unit This parameter is ignored as the method never blocks
     * @return <tt>true</tt>
     * @throws NullPointerException {@inheritDoc}
     */
    @Override
    public boolean offer(E e, long timeout, TimeUnit unit) {
        return offer(e);
    }

    /**
     * Retrieves and removes the head of this queue, or returns <tt>null</tt>
     * if this queue has no elements with an expired delay.
     *
     * @return the head of this queue, or <tt>null</tt> if this
     *         queue has no elements with an expired delay
     */
    @Override
    public E poll() {
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            BufferedElement<E> first = q.peek();
            return first == null || first.getDelay(TimeUnit.NANOSECONDS) > 0 ? null : q.poll().getElement();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Retrieves and removes the head of this queue, waiting if necessary
     * until an element with an expired delay is available on this queue.
     *
     * @return the head of this queue
     * @throws InterruptedException {@inheritDoc}
     */
    @Override
    public E take() throws InterruptedException {
        ReentrantLock lock = this.lock;
        lock.lockInterruptibly();
        try {
            for (;;) {
                BufferedElement<E> first = q.peek();
                if (first == null) {
                    available.await();
                } else {
                    long delay = first.getDelay(TimeUnit.NANOSECONDS);
                    if (delay <= 0) {
                        return q.poll().getElement();
                    } else if (leader != null) {
                        available.await();
                    } else {
                        Thread thisThread = Thread.currentThread();
                        leader = thisThread;
                        try {
                            available.awaitNanos(delay);
                        } finally {
                            if (leader == thisThread) {
                                leader = null;
                            }
                        }
                    }
                }
            }
        } finally {
            if (leader == null && q.peek() != null) {
                available.signal();
            }
            lock.unlock();
        }
    }

    /**
     * Retrieves and removes the head of this queue, waiting if necessary
     * until an element with an expired delay is available on this queue,
     * or the specified wait time expires.
     *
     * @return the head of this queue, or <tt>null</tt> if the
     *         specified waiting time elapses before an element with
     *         an expired delay becomes available
     * @throws InterruptedException {@inheritDoc}
     */
    @Override
    public E poll(long timeout, TimeUnit unit) throws InterruptedException {
        long nanos = unit.toNanos(timeout);
        ReentrantLock lock = this.lock;
        lock.lockInterruptibly();
        try {
            for (;;) {
                BufferedElement<E> first = q.peek();
                if (first == null) {
                    if (nanos <= 0) {
                        return null;
                    } else {
                        nanos = available.awaitNanos(nanos);
                    }
                } else {
                    long delay = first.getDelay(TimeUnit.NANOSECONDS);
                    if (delay <= 0) {
                        return q.poll().getElement();
                    }
                    if (nanos <= 0) {
                        return null;
                    }
                    if (nanos < delay || leader != null) {
                        nanos = available.awaitNanos(nanos);
                    } else {
                        Thread thisThread = Thread.currentThread();
                        leader = thisThread;
                        try {
                            long timeLeft = available.awaitNanos(delay);
                            nanos -= delay - timeLeft;
                        } finally {
                            if (leader == thisThread) {
                                leader = null;
                            }
                        }
                    }
                }
            }
        } finally {
            if (leader == null && q.peek() != null) {
                available.signal();
            }
            lock.unlock();
        }
    }

    /**
     * Retrieves, but does not remove, the head of this queue, or
     * returns <tt>null</tt> if this queue is empty.  Unlike
     * <tt>poll</tt>, if no expired elements are available in the queue,
     * this method returns the element that will expire next,
     * if one exists.
     *
     * @return the head of this queue, or <tt>null</tt> if this
     *         queue is empty.
     */
    @Override
    public E peek() {
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            BufferedElement<E> delayedE = q.peek();
            return null != delayedE ? delayedE.getElement() : null;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public int size() {
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            return q.size();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Removes all elements with an expired delay from this queue and returns them.
     *
     * @return The drained elements
     */
    public Collection<E> drain() {
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            BufferedElement<E> first = q.peek();
            if (first == null || first.getDelay(TimeUnit.NANOSECONDS) > 0) {
                return Collections.emptyList();
            }

            Collection<E> c = new LinkedList<>();
            c.add(q.poll().getElement());

            boolean peek = true;
            while (peek) {
                BufferedElement<E> be = q.peek();
                if (be == null || be.getDelay(TimeUnit.NANOSECONDS) > 0) {
                    peek = false;
                } else {
                    c.add(q.poll().getElement());
                }
            }
            return c;
        } finally {
            lock.unlock();
        }
    }

    /**
     * @throws UnsupportedOperationException {@inheritDoc}
     * @throws ClassCastException            {@inheritDoc}
     * @throws NullPointerException          {@inheritDoc}
     * @throws IllegalArgumentException      {@inheritDoc}
     */
    @Override
    public int drainTo(Collection<? super E> c) {
        return drainTo(c, Integer.MAX_VALUE);
    }

    /**
     * @throws UnsupportedOperationException {@inheritDoc}
     * @throws ClassCastException            {@inheritDoc}
     * @throws NullPointerException          {@inheritDoc}
     * @throws IllegalArgumentException      {@inheritDoc}
     */
    @Override
    public int drainTo(Collection<? super E> c, int maxElements) {
        if (c == null) {
            throw new NullPointerException();
        }
        if (c == this) {
            throw new IllegalArgumentException();
        }
        if (maxElements <= 0) {
            return 0;
        }
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            int n = 0;
            while (n < maxElements) {
                BufferedElement<E> first = q.peek();
                if (first == null || first.getDelay(TimeUnit.NANOSECONDS) > 0) {
                    break;
                }
                c.add(q.poll().getElement());
                ++n;
            }
            return n;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Atomically removes all of the elements from this delay queue.
     * The queue will be empty after this call returns.
     * Elements with an unexpired delay are not waited for; they are
     * simply discarded from the queue.
     */
    @Override
    public void clear() {
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            q.clear();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Always returns <tt>Integer.MAX_VALUE</tt> because
     * a <tt>DelayQueue</tt> is not capacity constrained.
     *
     * @return <tt>Integer.MAX_VALUE</tt>
     */
    @Override
    public int remainingCapacity() {
        return Integer.MAX_VALUE;
    }

    /**
     * Returns an array containing all of the elements in this queue.
     * The returned array elements are in no particular order.
     *
     * <p>The returned array will be "safe" in that no references to it are
     * maintained by this queue.  (In other words, this method must allocate
     * a new array).  The caller is thus free to modify the returned array.
     *
     * <p>This method acts as bridge between array-based and collection-based
     * APIs.
     *
     * @return an array containing all of the elements in this queue
     */
    @Override
    public Object[] toArray() {
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            Object[] delayedArray = q.toArray();
            Object[] array = new Object[delayedArray.length];
            for (int i = 0; i < delayedArray.length; i++) {
                array[i] = ((BufferedElement<E>) delayedArray[i]).getElement();
            }
            return array;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Returns an array containing all of the elements in this queue; the
     * runtime type of the returned array is that of the specified array.
     * The returned array elements are in no particular order.
     * If the queue fits in the specified array, it is returned therein.
     * Otherwise, a new array is allocated with the runtime type of the
     * specified array and the size of this queue.
     *
     * <p>If this queue fits in the specified array with room to spare
     * (i.e., the array has more elements than this queue), the element in
     * the array immediately following the end of the queue is set to
     * <tt>null</tt>.
     *
     * <p>Like the {@link #toArray()} method, this method acts as bridge between
     * array-based and collection-based APIs.  Further, this method allows
     * precise control over the runtime type of the output array, and may,
     * under certain circumstances, be used to save allocation costs.
     *
     * <p>The following code can be used to dump a delay queue into a newly
     * allocated array of <tt>Delayed</tt>:
     *
     * <pre>
     *     Delayed[] a = q.toArray(new Delayed[0]);</pre>
     *
     * Note that <tt>toArray(new Object[0])</tt> is identical in function to
     * <tt>toArray()</tt>.
     *
     * @param a the array into which the elements of the queue are to
     *          be stored, if it is big enough; otherwise, a new array of the
     *          same runtime type is allocated for this purpose
     * @return an array containing all of the elements in this queue
     * @throws ArrayStoreException if the runtime type of the specified array
     *         is not a supertype of the runtime type of every element in
     *         this queue
     * @throws NullPointerException if the specified array is null
     */
    @Override
    public <T> T[] toArray(T[] a) {
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            Object[] elements = this.toArray();
            int size = elements.length;
            if (a.length < size) {
                return (T[]) Arrays.copyOf(elements, size, a.getClass());
            }
            System.arraycopy(elements, 0, a, 0, size);
            if (a.length > size) {
                a[size] = null;
            }
            return a;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Removes a single instance of the specified element from this
     * queue, if it is present, whether or not it has expired.
     */
    @Override
    public boolean remove(Object o) {
        ReentrantLock lock = this.lock;
        lock.lock();
        try {
            for (Iterator<BufferedElement<E>> it = q.iterator(); it.hasNext();) {
                BufferedElement<E> next = it.next();
                if (o.equals(next.getElement())) {
                    it.remove();
                    return true;
                }
            }
            return false;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Returns an iterator over all the elements (both expired and
     * unexpired) in this queue. The iterator does not return the
     * elements in any particular order.
     *
     * <p>The returned iterator is a "weakly consistent" iterator that
     * will never throw {@link java.util.ConcurrentModificationException
     * ConcurrentModificationException}, and guarantees to traverse
     * elements as they existed upon construction of the iterator, and
     * may (but is not guaranteed to) reflect any modifications
     * subsequent to construction.
     *
     * @return an iterator over the elements in this queue
     */
    @Override
    public Iterator<E> iterator() {
        return new Itr(q.toArray());
    }

    /**
     * Snapshot iterator that works off copy of underlying q array.
     */
    private class Itr implements Iterator<E> {
        final Object[] array; // Array of all elements
        int cursor;           // index of next element to return;
        int lastRet;          // index of last element, or -1 if no such

        Itr(Object[] array) {
            lastRet = -1;
            this.array = array;
        }

        @Override
        public boolean hasNext() {
            return cursor < array.length;
        }

        @Override
        @SuppressWarnings("unchecked")
        public E next() {
            if (cursor >= array.length) {
                throw new NoSuchElementException();
            }
            lastRet = cursor;
            return ((BufferedElement<E>)array[cursor++]).getElement();
        }

        @Override
        public void remove() {
            if (lastRet < 0) {
                throw new IllegalStateException();
            }
            Object x = array[lastRet];
            lastRet = -1;
            // Traverse underlying queue to find == element,
            // not just a .equals element.
            lock.lock();
            try {
                for (Iterator<BufferedElement<E>> it = q.iterator(); it.hasNext(); ) {
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
     * {@link BufferedElement}
     *
     * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
     * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
     */
    protected static class BufferedElement<T> implements Delayed {

        private volatile long stamp;
        private final long delayDuration;
        private final long maxStamp;
        private final T element;
        private final int hash;

        /**
         * Initializes a new {@link BufferedElement} with arbitrary delay durations by wrapping the given element.
         *
         * @param element The actual payload element
         * @param delayDuration The delay duration (in milliseconds) to use initially and as increment for each reset-operation
         * @param maxDelayDuration The the maximum delay duration (in milliseconds) to apply, or <code>0</code> for no maximum
         * @throws IllegalArgumentException If <code>delayDuration</code> is greater than <code>maxDelayDuration</code>
         */
        public BufferedElement(T element, long delayDuration, long maxDelayDuration) {
            super();
            if (delayDuration > maxDelayDuration && 0 != maxDelayDuration) {
                throw new IllegalArgumentException("delayDuration is greater than maxDelayDuration.");
            }
            this.element = element;
            this.delayDuration = delayDuration;
            long now = System.currentTimeMillis();
            stamp = now + delayDuration;
            maxStamp = 0L == maxDelayDuration ? 0L : now + maxDelayDuration;
            hash = 31 * 1 + ((element == null) ? 0 : element.hashCode());
        }

        /**
         * Initializes a new {@link BufferedElement} from given element.
         *
         * @param element The actual payload element
         * @param source The other element
         */
        public BufferedElement(T element, BufferedElement<T> source) {
            super();
            this.element = element;
            this.delayDuration = source.delayDuration;
            stamp = source.stamp;
            maxStamp = source.maxStamp;
            hash = source.hash;
        }

        @Override
        public int compareTo(Delayed o) {
            long thisStamp = this.stamp;
            long otherStamp = ((BufferedElement) o).stamp;
            return (thisStamp < otherStamp ? -1 : (thisStamp == otherStamp ? 0 : 1));
        }

        /*
         * The Delay has elapsed if Either: the delayDuration since last time this object was touched has elapsed Or: the maxDelayDuration was
         * reached
         */
        @Override
        public long getDelay(TimeUnit unit) {
            long toGo = stamp - System.currentTimeMillis();
            return unit.convert(toGo, TimeUnit.MILLISECONDS);
        }

        /**
         * Gets the wrapped element.
         *
         * @return the wrapped element
         */
        public T getElement() {
            return element;
        }

        /**
         * Resets the internal delay, up to the configured maximum delay duration.
         */
        public void reset() {
            long stamp = System.currentTimeMillis() + delayDuration;
            // Stamp must not be greater than maxStamp
            this.stamp = 0L != maxStamp && stamp >= maxStamp ? maxStamp : stamp;
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof BufferedElement)) {
                return obj.equals(element);
            }
            BufferedElement<T> other = (BufferedElement<T>) obj;
            if (element == null) {
                if (other.element != null) {
                    return false;
                }
            } else if (!element.equals(other.element)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return "BufferedElement [stamp=" + stamp + ", element=" + element + "]";
        }

    }

}
