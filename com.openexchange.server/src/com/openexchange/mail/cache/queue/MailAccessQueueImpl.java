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
    public MailAccessQueueImpl(final int capacity) {
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
    public boolean contains(final Object o) {
        return priorityQueue.contains(o);
    }

    @Override
    public PooledMailAccess element() {
        return priorityQueue.element();
    }

    @Override
    public boolean equals(final Object obj) {
        return priorityQueue.equals(obj);
    }

    @Override
    public boolean addAll(final Collection<? extends PooledMailAccess> c) {
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
    public <T> T[] toArray(final T[] a) {
        return priorityQueue.toArray(a);
    }

    @Override
    public boolean containsAll(final Collection<?> c) {
        return priorityQueue.containsAll(c);
    }

    @Override
    public boolean offer(final PooledMailAccess o) {
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
    public boolean add(final PooledMailAccess o) {
        if ((capacity > 0) && (capacity <= priorityQueue.size())) {
            return false;
        }
        return priorityQueue.add(o);
    }

    @Override
    public boolean removeAll(final Collection<?> c) {
        return priorityQueue.removeAll(c);
    }

    @Override
    public boolean remove(final Object o) {
        return priorityQueue.remove(o);
    }

    @Override
    public Iterator<PooledMailAccess> iterator() {
        return priorityQueue.iterator();
    }

    @Override
    public boolean retainAll(final Collection<?> c) {
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
