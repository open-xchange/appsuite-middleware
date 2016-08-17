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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.atomic.AtomicReference;

/**
 * {@link ConcurrentList} - A {@link List} backed by an {@link AtomicReference} holding a delegate list.
 * <p>
 * Beware that this list is concurrently accessed. Therefore avoid index-based access.<br>
 * Use iterator for traversing the list. Use {@link #getSnapshot()} for index-based access patterns.
 *
 * <pre>
 * Don't:
 *
 *   ConcurrentList&lt;String&gt; list = new ConcurrentList&lt;String&gt;(Arrays.asList(&quot;foo&quot;, &quot;bar&quot;, &quot;peter&quot;, &quot;pan&quot;, &quot;hero&quot;));
 *   for (int i = 0; i &lt; list.size(); i++) {
 *       String next = list.get(i);
 *       // continue...
 *   }
 *
 * Do:
 *
 *   ConcurrentList&lt;String&gt; list = new ConcurrentList&lt;String&gt;(Arrays.asList(&quot;foo&quot;, &quot;bar&quot;, &quot;peter&quot;, &quot;pan&quot;, &quot;hero&quot;));
 *   for (String next : list) {
 *       // continue...
 *   }
 *
 *   ConcurrentList&lt;String&gt; list = new ConcurrentList&lt;String&gt;(Arrays.asList(&quot;foo&quot;, &quot;bar&quot;, &quot;peter&quot;, &quot;pan&quot;, &quot;hero&quot;));
 *   List&lt;String&gt; snapshot = list.getSnapshot();
 *   for (int i = 0; i < snapshot.size(); i++) {
 *       String next = snapshot.get(i);
 *       // continue...
 *   }
 *
 * </pre>
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class ConcurrentList<E> implements List<E> {

    /**
     * The reference to current list.
     */
    protected final AtomicReference<List<E>> ref;

    /**
     * Initializes a new {@link ConcurrentList}.
     */
    public ConcurrentList() {
        super();
        ref = new AtomicReference<List<E>>(Collections.<E> emptyList());
    }

    /**
     * Initializes a new {@link ConcurrentList}.
     *
     * @param c The collection whose elements are to be placed into this list
     */
    public ConcurrentList(final Collection<? extends E> c) {
        super();
        ref = new AtomicReference<List<E>>(new ArrayList<E>(c));
    }

    /**
     * Gets an unmodifiable snapshot view for this concurrent list.
     *
     * @return The unmodifiable snapshot list
     */
    public List<E> getSnapshot() {
        return Collections.unmodifiableList(ref.get());
    }

    @Override
    public int size() {
        return ref.get().size();
    }

    @Override
    public boolean isEmpty() {
        return ref.get().isEmpty();
    }

    @Override
    public boolean contains(final Object o) {
        return ref.get().contains(o);
    }

    @Override
    public Iterator<E> iterator() {
        return ref.get().iterator();
    }

    @Override
    public Object[] toArray() {
        return ref.get().toArray();
    }

    @Override
    public <T> T[] toArray(final T[] a) {
        return ref.get().toArray(a);
    }

    @Override
    public boolean add(final E e) {
        boolean added;
        List<E> expected;
        List<E> list;
        do {
            expected = ref.get();
            list = new ArrayList<E>(expected);
            added = list.add(e);
        } while (!ref.compareAndSet(expected, list));

        return added;
    }

    /**
     * Adds specified element if not already contained.
     *
     * @param e The element
     * @return <code>true</code> if added; otherwise <code>false</code>
     */
    public boolean addIfAbsent(final E e) {
        boolean added;
        List<E> expected;
        List<E> list;
        do {
            expected = ref.get();
            list = new ArrayList<E>(expected);
            if (list.contains(e)) {
                return false;
            }
            added = list.add(e);
        } while (!ref.compareAndSet(expected, list));

        return added;
    }

    @Override
    public boolean remove(final Object o) {
        boolean removed;
        List<E> expected;
        List<E> list;
        do {
            expected = ref.get();
            list = new ArrayList<E>(expected);
            removed = list.remove(o);
        } while (!ref.compareAndSet(expected, list));

        return removed;
    }

    @Override
    public boolean containsAll(final Collection<?> c) {
        return ref.get().containsAll(c);
    }

    @Override
    public boolean addAll(final Collection<? extends E> c) {
        boolean added;
        List<E> expected;
        List<E> list;
        do {
            expected = ref.get();
            list = new ArrayList<E>(expected);
            added = list.addAll(c);
        } while (!ref.compareAndSet(expected, list));

        return added;
    }

    @Override
    public boolean addAll(final int index, final Collection<? extends E> c) {
        boolean added;
        List<E> expected;
        List<E> list;
        do {
            expected = ref.get();
            list = new ArrayList<E>(expected);
            added = list.addAll(index, c);
        } while (!ref.compareAndSet(expected, list));

        return added;
    }

    @Override
    public boolean removeAll(final Collection<?> c) {
        boolean removed;
        List<E> expected;
        List<E> list;
        do {
            expected = ref.get();
            list = new ArrayList<E>(expected);
            removed = list.removeAll(c);
        } while (!ref.compareAndSet(expected, list));

        return removed;
    }

    @Override
    public boolean retainAll(final Collection<?> c) {
        boolean retained;
        List<E> expected;
        List<E> list;
        do {
            expected = ref.get();
            list = new ArrayList<E>(expected);
            retained = list.retainAll(c);
        } while (!ref.compareAndSet(expected, list));

        return retained;
    }

    @Override
    public void clear() {
        // Clearing is setting to an empty list
        List<E> expected;
        List<E> list;
        do {
            expected = ref.get();
            list = Collections.emptyList();
        } while (!ref.compareAndSet(expected, list));
    }

    @Override
    public E get(final int index) {
        return ref.get().get(index);
    }

    @Override
    public E set(final int index, final E element) {
        E ret;
        List<E> expected;
        List<E> list;
        do {
            expected = ref.get();
            list = new ArrayList<E>(expected);
            ret = list.set(index, element);
        } while (!ref.compareAndSet(expected, list));

        return ret;
    }

    @Override
    public void add(final int index, final E element) {
        List<E> expected;
        List<E> list;
        do {
            expected = ref.get();
            list = new ArrayList<E>(expected);
            list.add(index, element);
        } while (!ref.compareAndSet(expected, list));
    }

    @Override
    public E remove(final int index) {
        E ret;
        List<E> expected;
        List<E> list;
        do {
            expected = ref.get();
            list = new ArrayList<E>(expected);
            ret = list.remove(index);
        } while (!ref.compareAndSet(expected, list));

        return ret;
    }

    @Override
    public int indexOf(final Object o) {
        return ref.get().indexOf(o);
    }

    @Override
    public int lastIndexOf(final Object o) {
        return ref.get().lastIndexOf(o);
    }

    @Override
    public ListIterator<E> listIterator() {
        return ref.get().listIterator();
    }

    @Override
    public ListIterator<E> listIterator(final int index) {
        return ref.get().listIterator(index);
    }

    @Override
    public List<E> subList(final int fromIndex, final int toIndex) {
        return ref.get().subList(fromIndex, toIndex);
    }

    /**
     * Returns a string representation of this collection.  The string
     * representation consists of a list of the collection's elements in the
     * order they are returned by its iterator, enclosed in square brackets
     * (<tt>"[]"</tt>).  Adjacent elements are separated by the characters
     * <tt>", "</tt> (comma and space).  Elements are converted to strings as
     * by {@link String#valueOf(Object)}.
     *
     * @return a string representation of this collection
     */
    @Override
    public String toString() {
        Iterator<E> it = iterator();
        if (! it.hasNext()) {
            return "[]";
        }

        StringBuilder sb = new StringBuilder(32);
        sb.append('[');
        for (;;) {
            E e = it.next();
            sb.append(e == this ? "(this Collection)" : e);
            if (! it.hasNext()) {
                return sb.append(']').toString();
            }
            sb.append(',').append(' ');
        }
    }

}
