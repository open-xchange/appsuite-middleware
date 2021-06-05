/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.mail.cache.queue;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import com.openexchange.mail.cache.PooledMailAccess;

/**
 * {@link MailAccessQueueImpl} - A {@link Queue} additionally providing {@link #pollDelayed()} method to obtain expired elements.
 * <p>
 * <b>Note</b>: This queue is not thread-safe!
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MailAccessQueueImpl implements MailAccessQueue {

    /**
     * The backing priority queue.
     */
    private final PriorityQueue<PooledMailAccess> priorityQueue;

    /**
     * The queue's capacity or <code>-1</code> if unbounded.
     */
    private final int capacity;

    /**
     * The atomic boolean for deprecated flag.
     */
    private final AtomicBoolean deprecated;

    /**
     * Creates a new <tt>MailAccessQueue</tt> that is initially empty.
     *
     * @param capacity The queue's capacity or <code>-1</code> if unbounded
     */
    public MailAccessQueueImpl(int capacity) {
        super();
        deprecated = new AtomicBoolean();
        this.capacity = capacity;
        priorityQueue = new PriorityQueue<PooledMailAccess>();
    }

    @Override
    public int getCapacity() {
        return capacity;
    }

    /**
     * Marks this queue as deprecated.
     */
    @Override
    public void markDeprecated() {
        deprecated.set(true);
    }

    /**
     * Checks if this queue is marked as deprecated.
     *
     * @return <code>true</code> if this queue is marked as deprecated; otherwise <code>false</code>
     */
    @Override
    public boolean isDeprecated() {
        return deprecated.get();
    }

    /**
     * Retrieves and removes the head of this queue, or <tt>null</tt> if head has not expired, yet.
     *
     * @return The head of this queue or <tt>null</tt> if head has not expired, yet.
     */
    @Override
    public PooledMailAccess pollDelayed() {
        final PooledMailAccess first = priorityQueue.peek();
        if (first == null || first.getDelay(TimeUnit.MILLISECONDS) > 0) {
            return null;
        }
        return priorityQueue.poll();
    }

    @Override
    public int hashCode() {
        return priorityQueue.hashCode();
    }

    @Override
    public PooledMailAccess remove() {
        return priorityQueue.remove();
    }

    @Override
    public boolean isEmpty() {
        return priorityQueue.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return priorityQueue.contains(o);
    }

    @Override
    public PooledMailAccess element() {
        return priorityQueue.element();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof MailAccessQueueImpl)) {
            return false;
        }
        MailAccessQueueImpl other = (MailAccessQueueImpl) obj;
        if (capacity != other.capacity) {
            return false;
        }
        if (isDeprecated() != other.isDeprecated()) {
            return false;
        }
        return priorityQueue.equals(other.priorityQueue);
    }

    @Override
    public boolean addAll(Collection<? extends PooledMailAccess> c) {
        if ((capacity > 0) && ((capacity - priorityQueue.size()) < c.size())) {
            return false;
        }
        return priorityQueue.addAll(c);
    }

    @Override
    public Object[] toArray() {
        return priorityQueue.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return priorityQueue.toArray(a);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return priorityQueue.containsAll(c);
    }

    @Override
    public boolean offer(PooledMailAccess o) {
        if ((capacity > 0) && (capacity <= priorityQueue.size())) {
            return false;
        }
        return priorityQueue.offer(o);
    }

    @Override
    public PooledMailAccess peek() {
        return priorityQueue.peek();
    }

    @Override
    public boolean add(PooledMailAccess o) {
        if ((capacity > 0) && (capacity <= priorityQueue.size())) {
            return false;
        }
        return priorityQueue.add(o);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return priorityQueue.removeAll(c);
    }

    @Override
    public boolean remove(Object o) {
        return priorityQueue.remove(o);
    }

    @Override
    public Iterator<PooledMailAccess> iterator() {
        return priorityQueue.iterator();
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return priorityQueue.retainAll(c);
    }

    @Override
    public String toString() {
        return priorityQueue.toString();
    }

    @Override
    public int size() {
        return priorityQueue.size();
    }

    @Override
    public void clear() {
        priorityQueue.clear();
    }

    @Override
    public PooledMailAccess poll() {
        return priorityQueue.poll();
    }

    public Comparator<? super PooledMailAccess> comparator() {
        return priorityQueue.comparator();
    }

}
