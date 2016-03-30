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

import java.util.Collection;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * {@link LockableQueue} - Delegates to specified {@link Queue queue} with a wrapping (read-write) lock.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class LockableQueue<E> implements Queue<E> {

    private final class RWIterator implements Iterator<E> {

        private final Iterator<E> it;

        protected RWIterator(final Iterator<E> it) {
            super();
            this.it = it;
        }

        @Override
        public boolean hasNext() {
            final Lock l = readLock;
            l.lock();
            try {
                return it.hasNext();
            } finally {
                l.unlock();
            }
        }

        @Override
        public E next() {
            final Lock l = readLock;
            l.lock();
            try {
                return it.next();
            } finally {
                l.unlock();
            }
        }

        @Override
        public void remove() {
            final Lock l = writeLock;
            l.lock();
            try {
                it.remove();
            } finally {
                l.unlock();
            }
        }
    } // End of RWIterator

    private final Queue<E> queue;

    protected final Lock readLock;

    protected final Lock writeLock;

    /**
     * Initializes a new {@link LockableQueue}.
     *
     * @param queue The queue
     */
    public LockableQueue(Queue<E> queue) {
        super();
        this.queue = queue;
        final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
        this.readLock = readWriteLock.readLock();
        this.writeLock = readWriteLock.writeLock();
    }

    /**
     * Initializes a new {@link LockableQueue}.
     *
     * @param queue The queue
     * @param lock The lock
     */
    public LockableQueue(Queue<E> queue, Lock lock) {
        super();
        this.queue = queue;
        this.readLock = lock;
        this.writeLock = lock;
    }

    @Override
    public boolean add(E e) {
        final Lock l = writeLock;
        l.lock();
        try {
            return queue.add(e);
        } finally {
            l.unlock();
        }
    }

    @Override
    public int size() {
        final Lock l = readLock;
        l.lock();
        try {
            return queue.size();
        } finally {
            l.unlock();
        }
    }

    @Override
    public boolean isEmpty() {
        final Lock l = readLock;
        l.lock();
        try {
            return queue.isEmpty();
        } finally {
            l.unlock();
        }
    }

    @Override
    public boolean offer(E e) {
        final Lock l = writeLock;
        l.lock();
        try {
            return queue.offer(e);
        } finally {
            l.unlock();
        }
    }

    @Override
    public boolean contains(Object o) {
        final Lock l = readLock;
        l.lock();
        try {
            return queue.contains(o);
        } finally {
            l.unlock();
        }
    }

    @Override
    public Iterator<E> iterator() {
        return new RWIterator(queue.iterator());
    }

    @Override
    public E remove() {
        final Lock l = writeLock;
        l.lock();
        try {
            return queue.remove();
        } finally {
            l.unlock();
        }
    }

    @Override
    public Object[] toArray() {
        final Lock l = readLock;
        l.lock();
        try {
            return queue.toArray();
        } finally {
            l.unlock();
        }
    }

    @Override
    public E poll() {
        final Lock l = writeLock;
        l.lock();
        try {
            return queue.poll();
        } finally {
            l.unlock();
        }
    }

    @Override
    public E element() {
        final Lock l = readLock;
        l.lock();
        try {
            return queue.element();
        } finally {
            l.unlock();
        }
    }

    @Override
    public E peek() {
        final Lock l = readLock;
        l.lock();
        try {
            return queue.peek();
        } finally {
            l.unlock();
        }
    }

    @Override
    public <T> T[] toArray(T[] a) {
        final Lock l = readLock;
        l.lock();
        try {
            return queue.toArray(a);
        } finally {
            l.unlock();
        }
    }

    @Override
    public boolean remove(Object o) {
        final Lock l = writeLock;
        l.lock();
        try {
            return queue.remove(o);
        } finally {
            l.unlock();
        }
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        final Lock l = readLock;
        l.lock();
        try {
            return queue.containsAll(c);
        } finally {
            l.unlock();
        }
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        final Lock l = writeLock;
        l.lock();
        try {
            return queue.addAll(c);
        } finally {
            l.unlock();
        }
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        final Lock l = writeLock;
        l.lock();
        try {
            return queue.removeAll(c);
        } finally {
            l.unlock();
        }
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        final Lock l = writeLock;
        l.lock();
        try {
            return queue.retainAll(c);
        } finally {
            l.unlock();
        }
    }

    @Override
    public void clear() {
        final Lock l = writeLock;
        l.lock();
        try {
            queue.clear();
        } finally {
            l.unlock();
        }
    }

    @Override
    public boolean equals(Object o) {
        final Lock l = readLock;
        l.lock();
        try {
            return queue.equals(o);
        } finally {
            l.unlock();
        }
    }

    @Override
    public int hashCode() {
        final Lock l = readLock;
        l.lock();
        try {
            return queue.hashCode();
        } finally {
            l.unlock();
        }
    }

}
