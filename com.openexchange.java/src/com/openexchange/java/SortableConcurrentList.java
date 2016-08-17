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
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * {@link SortableConcurrentList} - A sortable {@link ConcurrentList}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class SortableConcurrentList<E extends Comparable<E>> extends ConcurrentList<E> {

    /**
     * Initializes a new {@link SortableConcurrentList}.
     */
    public SortableConcurrentList() {
        super();
    }

    /**
     * Initializes a new {@link SortableConcurrentList}.
     *
     * @param c The collection of initial elements
     */
    protected SortableConcurrentList(Collection<? extends E> c) {
        super(c);
    }

    /**
     * Appends the specified element to the end of this list and sorts its elements afterwards.
     * <p>
     * Thus it is basically the chained invocation of {@link #add(Object)} followed by {@link #sort()}; except that it is performed atomically.
     *
     * @param e The element to be appended to this list
     * @return <tt>true</tt> if this collection changed; otherwise <code>false</code>
     * @see Comparable
     */
    public boolean addAndSort(E e) {
        boolean added;
        List<E> expected;
        List<E> list;
        do {
            expected = ref.get();
            list = new ArrayList<E>(expected);
            added = list.add(e);
            if (added) {
                Collections.sort(list);
            }
        } while (!ref.compareAndSet(expected, list));

        return added;
    }

    /**
     * Appends the specified element to the end of this list and sorts its elements afterwards.
     * <p>
     * Thus it is basically the chained invocation of {@link #add(Object)} followed by {@link #sort()}; except that it is performed atomically.
     *
     * @param e The element to be appended to this list
     * @return <tt>true</tt> if this collection changed; otherwise <code>false</code>
     * @see Comparable
     */
    public boolean addAndSortIfAbsent(E e) {
        boolean added;
        List<E> expected;
        List<E> list;
        do {
            expected = ref.get();
            list = new ArrayList<E>(expected);
            if (list.contains(e)) {
                added = false;
            } else {
                added = list.add(e);
                if (added) {
                    Collections.sort(list);
                }
            }
        } while (!ref.compareAndSet(expected, list));

        return added;
    }

    /**
     * Sorts the specified list into ascending order, according to the <i>natural ordering</i> of its elements. All elements in the list
     * must implement the <tt>Comparable</tt> interface. Furthermore, all elements in the list must be <i>mutually comparable</i> (that is,
     * <tt>e1.compareTo(e2)</tt> must not throw a <tt>ClassCastException</tt> for any elements <tt>e1</tt> and <tt>e2</tt> in the list).
     * <p>
     * This sort is guaranteed to be <i>stable</i>: equal elements will not be reordered as a result of the sort.
     * <p>
     * The specified list must be modifiable, but need not be resizable.
     * <p>
     * The sorting algorithm is a modified mergesort (in which the merge is omitted if the highest element in the low sublist is less than
     * the lowest element in the high sublist). This algorithm offers guaranteed n log(n) performance. This implementation dumps the
     * specified list into an array, sorts the array, and iterates over the list resetting each element from the corresponding position in
     * the array. This avoids the n<sup>2</sup> log(n) performance that would result from attempting to sort a linked list in place.
     *
     * @throws ClassCastException if the list contains elements that are not <i>mutually comparable</i> (for example, strings and integers).
     * @throws UnsupportedOperationException if the specified list's list-iterator does not support the <tt>set</tt> operation.
     * @see Comparable
     */
    public void sort() {
        List<E> expected;
        List<E> list;
        do {
            expected = ref.get();
            list = new ArrayList<E>(expected);
            Collections.sort(list);
        } while (!ref.compareAndSet(expected, list));
    }

    /**
     * Sorts the specified list according to the order induced by the specified comparator. All elements in the list must be <i>mutually
     * comparable</i> using the specified comparator (that is, <tt>c.compare(e1, e2)</tt> must not throw a <tt>ClassCastException</tt> for
     * any elements <tt>e1</tt> and <tt>e2</tt> in the list).
     * <p>
     * This sort is guaranteed to be <i>stable</i>: equal elements will not be reordered as a result of the sort.
     * <p>
     * The sorting algorithm is a modified mergesort (in which the merge is omitted if the highest element in the low sublist is less than
     * the lowest element in the high sublist). This algorithm offers guaranteed n log(n) performance. The specified list must be
     * modifiable, but need not be resizable. This implementation dumps the specified list into an array, sorts the array, and iterates over
     * the list resetting each element from the corresponding position in the array. This avoids the n<sup>2</sup> log(n) performance that
     * would result from attempting to sort a linked list in place.
     *
     * @param c the comparator to determine the order of the list. A <tt>null</tt> value indicates that the elements' <i>natural
     *            ordering</i> should be used.
     * @throws ClassCastException if the list contains elements that are not <i>mutually comparable</i> using the specified comparator.
     * @throws UnsupportedOperationException if the specified list's list-iterator does not support the <tt>set</tt> operation.
     * @see Comparator
     */
    public void sort(Comparator<? super E> c) {
        List<E> expected;
        List<E> list;
        do {
            expected = ref.get();
            list = new ArrayList<E>(expected);
            Collections.sort(list, c);
        } while (!ref.compareAndSet(expected, list));
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
            sb.append(e);
            if (! it.hasNext()) {
                return sb.append(']').toString();
            }
            sb.append(',').append(' ');
        }
    }

}
