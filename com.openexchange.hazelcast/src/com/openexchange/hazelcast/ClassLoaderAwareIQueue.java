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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.hazelcast;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import com.hazelcast.core.IQueue;
import com.hazelcast.core.ItemListener;
import com.hazelcast.monitor.LocalQueueStats;

/**
 * {@link ClassLoaderAwareIQueue}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ClassLoaderAwareIQueue<E extends Serializable> extends AbstractClassLoaderAware implements IQueue<E> {

    private final IQueue<Serializable> delegate;

    /**
     * Initializes a new {@link ClassLoaderAwareIQueue}.
     */
    public ClassLoaderAwareIQueue(final IQueue<Serializable> delegate, final boolean kryorize) {
        super(kryorize);
        this.delegate = delegate;
    }

    @Override
    public String getName() {
        return delegate.getName();
    }

    @Override
    public LocalQueueStats getLocalQueueStats() {
        return delegate.getLocalQueueStats();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void addItemListener(final ItemListener<E> listener, final boolean includeValue) {
        delegate.addItemListener((ItemListener<Serializable>) listener, includeValue);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void removeItemListener(final ItemListener<E> listener) {
        delegate.removeItemListener((ItemListener<Serializable>) listener);
    }

    @Override
    public InstanceType getInstanceType() {
        return delegate.getInstanceType();
    }

    @Override
    public void destroy() {
        delegate.destroy();
    }

    @Override
    public Object getId() {
        return delegate.getId();
    }

    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    @Override
    public boolean add(final E e) {
        final Class<?> clazz = classLoaderSourceRef.get();
        applyClassLoader(clazz);
        try {
            return delegate.add(wrapper(e));
        } finally {
            unsetClassLoader();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Iterator<E> iterator() {
        final Class<?> clazz = classLoaderSourceRef.get();
        applyClassLoader(clazz);
        try {
            return (Iterator<E>) delegate.iterator();
        } finally {
            unsetClassLoader();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public E remove() {
        final Class<?> clazz = classLoaderSourceRef.get();
        applyClassLoader(clazz);
        try {
            return (E) delegate.remove();
        } finally {
            unsetClassLoader();
        }
    }

    @Override
    public boolean offer(final E e) {
        final Class<?> clazz = classLoaderSourceRef.get();
        applyClassLoader(clazz);
        try {
            return delegate.offer(wrapper(e));
        } finally {
            unsetClassLoader();
        }
    }

    @Override
    public Object[] toArray() {
        final Class<?> clazz = classLoaderSourceRef.get();
        applyClassLoader(clazz);
        try {
            return delegate.toArray();
        } finally {
            unsetClassLoader();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public E poll() {
        final Class<?> clazz = classLoaderSourceRef.get();
        applyClassLoader(clazz);
        try {
            return (E) delegate.poll();
        } finally {
            unsetClassLoader();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public E element() {
        final Class<?> clazz = classLoaderSourceRef.get();
        applyClassLoader(clazz);
        try {
            return (E) delegate.element();
        } finally {
            unsetClassLoader();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public E peek() {
        final Class<?> clazz = classLoaderSourceRef.get();
        applyClassLoader(clazz);
        try {
            return (E) delegate.peek();
        } finally {
            unsetClassLoader();
        }
    }

    @Override
    public <T> T[] toArray(final T[] a) {
        final Class<?> clazz = classLoaderSourceRef.get();
        applyClassLoader(clazz);
        try {
            return delegate.toArray(a);
        } finally {
            unsetClassLoader();
        }
    }

    @Override
    public void put(final E e) throws InterruptedException {
        final Class<?> clazz = classLoaderSourceRef.get();
        applyClassLoader(clazz);
        try {
            delegate.put(wrapper(e));
        } finally {
            unsetClassLoader();
        }
    }

    @Override
    public boolean offer(final E e, final long timeout, final TimeUnit unit) throws InterruptedException {
        final Class<?> clazz = classLoaderSourceRef.get();
        applyClassLoader(clazz);
        try {
            return delegate.offer(wrapper(e), timeout, unit);
        } finally {
            unsetClassLoader();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public E take() throws InterruptedException {
        final Class<?> clazz = classLoaderSourceRef.get();
        applyClassLoader(clazz);
        try {
            return (E) delegate.take();
        } finally {
            unsetClassLoader();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public E poll(final long timeout, final TimeUnit unit) throws InterruptedException {
        final Class<?> clazz = classLoaderSourceRef.get();
        applyClassLoader(clazz);
        try {
            return (E) delegate.poll(timeout, unit);
        } finally {
            unsetClassLoader();
        }
    }

    @Override
    public int remainingCapacity() {
        return delegate.remainingCapacity();
    }

    @Override
    public boolean remove(final Object o) {
        final Class<?> clazz = classLoaderSourceRef.get();
        applyClassLoader(clazz);
        try {
            return delegate.remove(wrapper(o));
        } finally {
            unsetClassLoader();
        }
    }

    @Override
    public boolean contains(final Object o) {
        final Class<?> clazz = classLoaderSourceRef.get();
        applyClassLoader(clazz);
        try {
            return delegate.contains(wrapper(o));
        } finally {
            unsetClassLoader();
        }
    }

    @Override
    public int drainTo(final Collection<? super E> c) {
        final Class<?> clazz = classLoaderSourceRef.get();
        applyClassLoader(clazz);
        try {
            final Collection<Serializable> col = new ArrayList<Serializable>(c.size());
            for (final Object object : c) {
                col.add(wrapper(object));
            }
            return delegate.drainTo(col);
        } finally {
            unsetClassLoader();
        }
    }

    @Override
    public boolean containsAll(final Collection<?> c) {
        final Class<?> clazz = classLoaderSourceRef.get();
        applyClassLoader(clazz);
        try {
            final Collection<Serializable> col = new ArrayList<Serializable>(c.size());
            for (final Object object : c) {
                col.add(wrapper(object));
            }
            return delegate.containsAll(col);
        } finally {
            unsetClassLoader();
        }
    }

    @Override
    public int drainTo(final Collection<? super E> c, final int maxElements) {
        final Class<?> clazz = classLoaderSourceRef.get();
        applyClassLoader(clazz);
        try {
            final Collection<Serializable> col = new ArrayList<Serializable>(c.size());
            for (final Object object : c) {
                col.add(wrapper(object));
            }
            return delegate.drainTo(col, maxElements);
        } finally {
            unsetClassLoader();
        }
    }

    @Override
    public boolean addAll(final Collection<? extends E> c) {
        final Class<?> clazz = classLoaderSourceRef.get();
        applyClassLoader(clazz);
        try {
            final Collection<Serializable> col = new ArrayList<Serializable>(c.size());
            for (final Object object : c) {
                col.add(wrapper(object));
            }
            return delegate.addAll(col);
        } finally {
            unsetClassLoader();
        }
    }

    @Override
    public boolean removeAll(final Collection<?> c) {
        final Class<?> clazz = classLoaderSourceRef.get();
        applyClassLoader(clazz);
        try {
            final Collection<Serializable> col = new ArrayList<Serializable>(c.size());
            for (final Object object : c) {
                col.add(wrapper(object));
            }
            return delegate.removeAll(col);
        } finally {
            unsetClassLoader();
        }
    }

    @Override
    public boolean retainAll(final Collection<?> c) {
        final Class<?> clazz = classLoaderSourceRef.get();
        applyClassLoader(clazz);
        try {
            final Collection<Serializable> col = new ArrayList<Serializable>(c.size());
            for (final Object object : c) {
                col.add(wrapper(object));
            }
            return delegate.retainAll(col);
        } finally {
            unsetClassLoader();
        }
    }

    @Override
    public void clear() {
        delegate.clear();
    }

    @Override
    public boolean equals(final Object o) {
        return delegate.equals(o);
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }

}
