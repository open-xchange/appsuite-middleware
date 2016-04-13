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

package com.openexchange.continuation.utils;

import java.util.AbstractQueue;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * {@link TimeAwareBlockingQueue}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since 7.6.0
 */
public class TimeAwareBlockingQueue<E> extends AbstractQueue<E> implements BlockingQueue<E>, java.io.Serializable {

    /**
     * Linked list node class
     */
    static class Node<E> {

        E item;
        /**
         * One of: - the real successor Node - this Node, meaning the successor is head.next - null, meaning there is no successor (this is
         * the last node)
         */

        Node<E> next;

        Node(final E x) {
            item = x;
        }
    }

    /** The capacity bound, or Integer.MAX_VALUE if none */
    private final int capacity;

    /** Current number of elements */
    private final AtomicInteger count = new AtomicInteger(0);

    /** Head of linked list */
    private transient Node<E> head;

    /** Tail of linked list */
    private transient Node<E> last;

    /** Lock held by take, poll, etc */
    private final ReentrantLock takeLock = new ReentrantLock();

    /** Wait queue for waiting takes */
    private final Condition notEmpty = takeLock.newCondition();

    /** Lock held by put, offer, etc */
    private final ReentrantLock putLock = new ReentrantLock();

    /** Wait queue for waiting puts */
    private final Condition notFull = putLock.newCondition();

    /**
     * Signals a waiting take. Called only from put/offer (which do not otherwise ordinarily lock takeLock.)
     */
    private void signalNotEmpty() {
        final ReentrantLock takeLock = this.takeLock;
        takeLock.lock();
        try {
            notEmpty.signal();
        } finally {
            takeLock.unlock();
        }
    }

    /**
     * Signals a waiting put. Called only from take/poll.
     */
    private void signalNotFull() {
        final ReentrantLock putLock = this.putLock;
        putLock.lock();
        try {
            notFull.signal();
        } finally {
            putLock.unlock();
        }
    }

    /**
     * Creates a node and links it at end of queue.
     *
     * @param x the item
     */
    private void enqueue(final E x) {
        // assert putLock.isHeldByCurrentThread();
        last = last.next = new Node<E>(x);
    }

    /**
     * Removes a node from head of queue.
     *
     * @return the node
     */
    private E dequeue() {
        // assert takeLock.isHeldByCurrentThread();
        final Node<E> h = head;
        final Node<E> first = h.next;
        h.next = h; // help GC
        head = first;
        final E x = first.item;
        first.item = null;
        return x;
    }

    /**
     * Lock to prevent both puts and takes.
     */
    void fullyLock() {
        putLock.lock();
        takeLock.lock();
    }

    /**
     * Unlock to allow both puts and takes.
     */
    void fullyUnlock() {
        takeLock.unlock();
        putLock.unlock();
    }

    /**
     * Tells whether both locks are held by current thread.
     */
    boolean isFullyLocked() {
        return (putLock.isHeldByCurrentThread() && takeLock.isHeldByCurrentThread());
    }

    /**
     * Creates a <tt>TimeAwareBlockingQueue</tt> with a capacity of {@link Integer#MAX_VALUE}.
     */
    public TimeAwareBlockingQueue() {
        this(Integer.MAX_VALUE);
    }

    /**
     * Creates a <tt>TimeAwareBlockingQueue</tt> with the given (fixed) capacity.
     *
     * @param capacity the capacity of this queue
     * @throws IllegalArgumentException if <tt>capacity</tt> is not greater than zero
     */
    public TimeAwareBlockingQueue(final int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException();
        }
        this.capacity = capacity;
        last = head = new Node<E>(null);
    }

    /**
     * Creates a <tt>TimeAwareBlockingQueue</tt> with a capacity of {@link Integer#MAX_VALUE}, initially containing the elements of the
     * given collection, added in traversal order of the collection's iterator.
     *
     * @param c the collection of elements to initially contain
     * @throws NullPointerException if the specified collection or any of its elements are null
     */
    public TimeAwareBlockingQueue(final Collection<? extends E> c) {
        this(Integer.MAX_VALUE);
        final ReentrantLock putLock = this.putLock;
        putLock.lock(); // Never contended, but necessary for visibility
        try {
            int n = 0;
            for (final E e : c) {
                if (e == null) {
                    throw new NullPointerException();
                }
                if (n == capacity) {
                    throw new IllegalStateException("Queue full");
                }
                enqueue(e);
                ++n;
            }
            count.set(n);
        } finally {
            putLock.unlock();
        }
    }

    // this doc comment is overridden to remove the reference to collections
    // greater in size than Integer.MAX_VALUE
    /**
     * Returns the number of elements in this queue.
     *
     * @return the number of elements in this queue
     */
    @Override
    public int size() {
        return count.get();
    }

    // this doc comment is a modified copy of the inherited doc comment,
    // without the reference to unlimited queues.
    /**
     * Returns the number of additional elements that this queue can ideally (in the absence of memory or resource constraints) accept
     * without blocking. This is always equal to the initial capacity of this queue less the current <tt>size</tt> of this queue.
     * <p>
     * Note that you <em>cannot</em> always tell if an attempt to insert an element will succeed by inspecting <tt>remainingCapacity</tt>
     * because it may be the case that another thread is about to insert or remove an element.
     */
    @Override
    public int remainingCapacity() {
        return capacity - count.get();
    }

    /**
     * Inserts the specified element at the tail of this queue, waiting if necessary for space to become available.
     *
     * @throws InterruptedException {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     */
    @Override
    public void put(final E e) throws InterruptedException {
        if (e == null) {
            throw new NullPointerException();
        }
        // Note: convention in all put/take/etc is to preset local var
        // holding count negative to indicate failure unless set.
        int c = -1;
        final ReentrantLock putLock = this.putLock;
        final AtomicInteger count = this.count;
        putLock.lockInterruptibly();
        try {
            /*
             * Note that count is used in wait guard even though it is not protected by lock. This works because count can only decrease at
             * this point (all other puts are shut out by lock), and we (or some other waiting put) are signalled if it ever changes from
             * capacity. Similarly for all other uses of count in other wait guards.
             */
            while (count.get() == capacity) {
                notFull.await();
            }
            enqueue(e);
            c = count.getAndIncrement();
            if (c + 1 < capacity) {
                notFull.signal();
            }
        } finally {
            putLock.unlock();
        }
        if (c == 0) {
            signalNotEmpty();
        }
    }

    /**
     * Inserts the specified element at the tail of this queue, waiting if necessary up to the specified wait time for space to become
     * available.
     *
     * @return <tt>true</tt> if successful, or <tt>false</tt> if the specified waiting time elapses before space is available.
     * @throws InterruptedException {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     */
    @Override
    public boolean offer(final E e, final long timeout, final TimeUnit unit) throws InterruptedException {

        if (e == null) {
            throw new NullPointerException();
        }
        long nanos = unit.toNanos(timeout);
        int c = -1;
        final ReentrantLock putLock = this.putLock;
        final AtomicInteger count = this.count;
        putLock.lockInterruptibly();
        try {
            while (count.get() == capacity) {

                if (nanos <= 0) {
                    return false;
                }
                nanos = notFull.awaitNanos(nanos);
            }
            enqueue(e);
            c = count.getAndIncrement();
            if (c + 1 < capacity) {
                notFull.signal();
            }
        } finally {
            putLock.unlock();
        }
        if (c == 0) {
            signalNotEmpty();
        }
        return true;
    }

    /**
     * Inserts the specified element at the tail of this queue if it is possible to do so immediately without exceeding the queue's
     * capacity, returning <tt>true</tt> upon success and <tt>false</tt> if this queue is full. When using a capacity-restricted queue, this
     * method is generally preferable to method {@link BlockingQueue#add add}, which can fail to insert an element only by throwing an
     * exception.
     *
     * @throws NullPointerException if the specified element is null
     */
    @Override
    public boolean offer(final E e) {
        if (e == null) {
            throw new NullPointerException();
        }
        final AtomicInteger count = this.count;
        if (count.get() == capacity) {
            return false;
        }
        int c = -1;
        final ReentrantLock putLock = this.putLock;
        putLock.lock();
        try {
            if (count.get() < capacity) {
                enqueue(e);
                c = count.getAndIncrement();
                if (c + 1 < capacity) {
                    notFull.signal();
                }
            }
        } finally {
            putLock.unlock();
        }
        if (c == 0) {
            signalNotEmpty();
        }
        return c >= 0;
    }

    @Override
    public E take() throws InterruptedException {
        E x;
        int c = -1;
        final AtomicInteger count = this.count;
        final ReentrantLock takeLock = this.takeLock;
        takeLock.lockInterruptibly();
        try {
            while (count.get() == 0) {
                notEmpty.await();
            }
            x = dequeue();
            c = count.getAndDecrement();
            if (c > 1) {
                notEmpty.signal();
            }
        } finally {
            takeLock.unlock();
        }
        if (c == capacity) {
            signalNotFull();
        }
        return x;
    }

    public void await() throws InterruptedException {
        if (count.get() > 0) {
            return;
        }
        final ReentrantLock takeLock = this.takeLock;
        takeLock.lock();
        try {
            while (null == head.next) {
                notEmpty.await();
            }
        } finally {
            takeLock.unlock();
        }
    }

    @Override
    public E poll(final long timeout, final TimeUnit unit) throws InterruptedException {
        E x = null;
        int c = -1;
        long nanos = unit.toNanos(timeout);
        final AtomicInteger count = this.count;
        final ReentrantLock takeLock = this.takeLock;
        takeLock.lockInterruptibly();
        try {
            while (count.get() == 0) {
                if (nanos <= 0) {
                    return null;
                }
                nanos = notEmpty.awaitNanos(nanos);
            }
            x = dequeue();
            c = count.getAndDecrement();
            if (c > 1) {
                notEmpty.signal();
            }
        } finally {
            takeLock.unlock();
        }
        if (c == capacity) {
            signalNotFull();
        }
        return x;
    }

    /**
     * Retrieves and removes elements from this queue until specified time is elapsed.
     *
     * @param timeout How long to wait before giving up, in units of <tt>unit</tt>
     * @param unit A <tt>TimeUnit</tt> determining how to interpret the <tt>timeout</tt> parameter
     * @param maxToAwait The max. number of elements to await
     * @return The polled elements from this queue
     * @throws InterruptedException If interrupted while waiting
     */
    public List<E> pollUntilElapsed(final long timeout, final TimeUnit unit, final int maxToAwait) throws InterruptedException {
        final List<E> retval = new LinkedList<E>();
        final AtomicInteger count = this.count;
        final ReentrantLock takeLock = this.takeLock;
        long nanos = unit.toNanos(timeout);
        int awaits = maxToAwait;
        while (nanos > 0) {
            E x = null;
            int c = -1;
            takeLock.lockInterruptibly();
            try {
                while (count.get() == 0) {
                    if (nanos <= 0 || awaits <= 0) {
                        // No more time left or no more elements to expect
                        return retval;
                    }
                    nanos = notEmpty.awaitNanos(nanos);
                }
                x = dequeue();
                c = count.getAndDecrement();
                awaits--;
                if (c > 1) {
                    notEmpty.signal();
                }
            } finally {
                takeLock.unlock();
            }
            if (c == capacity) {
                signalNotFull();
            }
            if (null != x) {
                retval.add(x);
            }
        }
        return retval;
    }

    @Override
    public E poll() {
        final AtomicInteger count = this.count;
        if (count.get() == 0) {
            return null;
        }
        E x = null;
        int c = -1;
        final ReentrantLock takeLock = this.takeLock;
        takeLock.lock();
        try {
            if (count.get() > 0) {
                x = dequeue();
                c = count.getAndDecrement();
                if (c > 1) {
                    notEmpty.signal();
                }
            }
        } finally {
            takeLock.unlock();
        }
        if (c == capacity) {
            signalNotFull();
        }
        return x;
    }

    @Override
    public E peek() {
        if (count.get() == 0) {
            return null;
        }
        final ReentrantLock takeLock = this.takeLock;
        takeLock.lock();
        try {
            final Node<E> first = head.next;
            if (first == null) {
                return null;
            } else {
                return first.item;
            }
        } finally {
            takeLock.unlock();
        }
    }

    /*
     * Unlinks interior Node p with predecessor trail.
     */
    void unlink(final Node<E> p, final Node<E> trail) {
        // assert isFullyLocked();
        // p.next is not changed, to allow iterators that are
        // traversing p to maintain their weak-consistency guarantee.
        p.item = null;
        trail.next = p.next;
        if (last == p) {
            last = trail;
        }
        if (count.getAndDecrement() == capacity) {
            notFull.signal();
        }
    }

    /**
     * Removes a single instance of the specified element from this queue, if it is present. More formally, removes an element <tt>e</tt>
     * such that <tt>o.equals(e)</tt>, if this queue contains one or more such elements. Returns <tt>true</tt> if this queue contained the
     * specified element (or equivalently, if this queue changed as a result of the call).
     *
     * @param o element to be removed from this queue, if present
     * @return <tt>true</tt> if this queue changed as a result of the call
     */
    @Override
    public boolean remove(final Object o) {
        if (o == null) {
            return false;
        }
        fullyLock();
        try {
            for (Node<E> trail = head, p = trail.next; p != null; trail = p, p = p.next) {
                if (o.equals(p.item)) {
                    unlink(p, trail);
                    return true;
                }
            }
            return false;
        } finally {
            fullyUnlock();
        }
    }

    /**
     * Returns an array containing all of the elements in this queue, in proper sequence.
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
        fullyLock();
        try {
            final int size = count.get();
            final Object[] a = new Object[size];
            int k = 0;
            for (Node<E> p = head.next; p != null; p = p.next) {
                a[k++] = p.item;
            }
            return a;
        } finally {
            fullyUnlock();
        }
    }

    /**
     * Returns an array containing all of the elements in this queue, in proper sequence; the runtime type of the returned array is that of
     * the specified array. If the queue fits in the specified array, it is returned therein. Otherwise, a new array is allocated with the
     * runtime type of the specified array and the size of this queue.
     * <p>
     * If this queue fits in the specified array with room to spare (i.e., the array has more elements than this queue), the element in the
     * array immediately following the end of the queue is set to <tt>null</tt>.
     * <p>
     * Like the {@link #toArray()} method, this method acts as bridge between array-based and collection-based APIs. Further, this method
     * allows precise control over the runtime type of the output array, and may, under certain circumstances, be used to save allocation
     * costs.
     * <p>
     * Suppose <tt>x</tt> is a queue known to contain only strings. The following code can be used to dump the queue into a newly allocated
     * array of <tt>String</tt>:
     *
     * <pre>
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
    // @SuppressWarnings("unchecked")
    @Override
    public <T> T[] toArray(T[] a) {
        fullyLock();
        try {
            final int size = count.get();
            if (a.length < size) {
                a = (T[]) java.lang.reflect.Array.newInstance(a.getClass().getComponentType(), size);
            }

            int k = 0;
            for (Node<E> p = head.next; p != null; p = p.next) {
                a[k++] = (T) p.item;
            }
            if (a.length > k) {
                a[k] = null;
            }
            return a;
        } finally {
            fullyUnlock();
        }
    }

    @Override
    public String toString() {
        fullyLock();
        try {
            return super.toString();
        } finally {
            fullyUnlock();
        }
    }

    /**
     * Atomically removes all of the elements from this queue. The queue will be empty after this call returns.
     */
    @Override
    public void clear() {
        fullyLock();
        try {
            for (Node<E> p, h = head; (p = h.next) != null; h = p) {
                h.next = h;
                p.item = null;
            }
            head = last;
            // assert head.item == null && head.next == null;
            if (count.getAndSet(0) == capacity) {
                notFull.signal();
            }
        } finally {
            fullyUnlock();
        }
    }

    /**
     * @throws UnsupportedOperationException {@inheritDoc}
     * @throws ClassCastException {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     * @throws IllegalArgumentException {@inheritDoc}
     */
    @Override
    public int drainTo(final Collection<? super E> c) {
        return drainTo(c, Integer.MAX_VALUE);

    }

    /**
     * @throws UnsupportedOperationException {@inheritDoc}
     * @throws ClassCastException {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     * @throws IllegalArgumentException {@inheritDoc}
     */
    @Override
    public int drainTo(final Collection<? super E> c, final int maxElements) {
        if (c == null) {
            throw new NullPointerException();
        }
        if (c == this) {
            throw new IllegalArgumentException();
        }
        boolean signalNotFull = false;
        final ReentrantLock takeLock = this.takeLock;
        takeLock.lock();
        try {
            final int n = Math.min(maxElements, count.get());
            Node<E> h = head;
            int i = 0;
            try {
                while (i < n) {
                    final Node<E> p = h.next;
                    c.add(p.item);
                    p.item = null;
                    h.next = h;
                    h = p;
                    ++i;
                }
                return n;
            } finally {
                // Restore invariants even if c.add() threw
                if (i > 0) {
                    // assert h.item == null;
                    head = h;
                    signalNotFull = (count.getAndAdd(-i) == capacity);
                }
            }
        } finally {
            takeLock.unlock();
            if (signalNotFull) {
                signalNotFull();
            }
        }
    }

    /**
     * Returns an iterator over the elements in this queue in proper sequence. The returned <tt>Iterator</tt> is a "weakly consistent"
     * iterator that will never throw {@link ConcurrentModificationException}, and guarantees to traverse elements as they existed upon
     * construction of the iterator, and may (but is not guaranteed to) reflect any modifications subsequent to construction.
     *
     * @return an iterator over the elements in this queue in proper sequence
     */
    @Override
    public Iterator<E> iterator() {
        return new Itr();
    }

    private class Itr implements Iterator<E> {

        /*
         * Basic weakly-consistent iterator. At all times hold the next item to hand out so that if hasNext() reports true, we will still
         * have it to return even if lost race with a take etc.
         */
        private volatile Node<E> current;
        private volatile Node<E> lastRet;
        private E currentElement;

        Itr() {
            fullyLock();
            try {
                current = head.next;
                if (current != null) {
                    currentElement = current.item;
                }
            } finally {
                fullyUnlock();
            }
        }

        @Override
        public boolean hasNext() {
            return current != null;
        }

        /**
         * Returns the next live successor of p, or null if no such. Unlike other traversal methods, iterators need to handle both: -
         * dequeued nodes (p.next == p) - (possibly multiple) interior removed nodes (p.item == null)
         */
        private Node<E> nextNode(Node<E> p) {
            for (;;) {
                final Node s = p.next;
                if (s == p) {
                    return head.next;
                }
                if (s == null || s.item != null) {
                    return s;
                }
                p = s;
            }
        }

        @Override
        public E next() {
            fullyLock();
            try {
                if (current == null) {
                    throw new NoSuchElementException();
                }
                final E x = currentElement;
                lastRet = current;
                current = nextNode(current);
                currentElement = (current == null) ? null : current.item;
                return x;
            } finally {
                fullyUnlock();
            }
        }

        @Override
        public void remove() {
            Node<E> node = lastRet;
            if (node == null) {
                throw new IllegalStateException();
            }

            fullyLock();
            try {
                node = lastRet;
                if (node == null) {
                    throw new IllegalStateException();
                }
                lastRet = null;
                for (Node<E> trail = head, p = trail.next; p != null; trail = p, p = p.next) {
                    if (p == node) {
                        unlink(p, trail);
                        break;
                    }
                }
            } finally {
                fullyUnlock();
            }
        }
    }

    /**
     * Save the state to a stream (that is, serialize it).
     *
     * @serialData The capacity is emitted (int), followed by all of its elements (each an <tt>Object</tt>) in the proper order, followed by
     *             a null
     * @param s the stream
     */
    private void writeObject(final java.io.ObjectOutputStream s) throws java.io.IOException {

        fullyLock();
        try {
            // Write out any hidden stuff, plus capacity
            s.defaultWriteObject();

            // Write out all elements in the proper order.
            for (Node<E> p = head.next; p != null; p = p.next) {
                s.writeObject(p.item);
            }

            // Use trailing null as sentinel
            s.writeObject(null);
        } finally {
            fullyUnlock();
        }
    }

    /**
     * Reconstitute this queue instance from a stream (that is, deserialize it).
     *
     * @param s the stream
     */
    private void readObject(final java.io.ObjectInputStream s) throws java.io.IOException, ClassNotFoundException {
        // Read in capacity, and any hidden stuff
        s.defaultReadObject();

        count.set(0);
        last = head = new Node<E>(null);

        // Read in all elements and place in queue
        for (;;) {
            // @SuppressWarnings("unchecked")
            final E item = (E) s.readObject();
            if (item == null) {
                break;
            }
            add(item);
        }
    }

}
