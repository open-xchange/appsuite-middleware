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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.tools.collections;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * {@link ConcurrentHashSet} - This class implements the <tt>Set</tt> interface, backed by a map (actually a <tt>ConcurrentHashMap</tt>
 * instance). It makes no guarantees as to the iteration order of the set; in particular, it does not guarantee that the order will remain
 * constant over time. This class permits the <tt>null</tt> element.
 * <p>
 * Moreover it supports full concurrency of retrievals and adjustable expected concurrency for updates.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ConcurrentHashSet<E> implements Set<E>, Serializable {

    private static final long serialVersionUID = -2608324279213322648L;

    /**
     * Dummy value to associate with an Object in the backing map.
     */
    private static final Object PRESENT = new Object();

    /**
     * The default initial number of table slots for this table. Used when not otherwise specified in constructor.
     */
    private static int DEFAULT_INITIAL_CAPACITY = 16;

    /**
     * The default load factor for this table. Used when not otherwise specified in constructor.
     */
    private static final float DEFAULT_LOAD_FACTOR = 0.75f;

    /**
     * The default number of concurrency control segments.
     **/
    private static final int DEFAULT_SEGMENTS = 16;

    /*-
     * ############################# Members #############################
     */

    /**
     * The backing concurrent map.
     */
    private final ConcurrentHashMap<E, Object> map;

    /**
     * Creates a new, empty set with the specified initial capacity, load factor, and concurrency level.
     * 
     * @param initialCapacity The initial capacity. The implementation performs internal sizing to accommodate this many elements.
     * @param loadFactor The load factor threshold, used to control resizing. Resizing may be performed when the average number of elements
     *            per bin exceeds this threshold.
     * @param concurrencyLevel The estimated number of concurrently updating threads. The implementation performs internal sizing to try to
     *            accommodate this many threads.
     * @throws IllegalArgumentException If the initial capacity is negative or the load factor or concurrencyLevel are non-positive.
     */
    public ConcurrentHashSet(final int initialCapacity, final float loadFactor, final int concurrencyLevel) {
        super();
        map = new ConcurrentHashMap<E, Object>(initialCapacity, loadFactor, concurrencyLevel);
    }

    /**
     * Creates a new, empty set with the specified initial capacity, and with default load factor and concurrency level.
     * 
     * @param initialCapacity the initial capacity. The implementation performs internal sizing to accommodate this many elements.
     * @throws IllegalArgumentException if the initial capacity of elements is negative.
     */
    public ConcurrentHashSet(final int initialCapacity) {
        this(initialCapacity, DEFAULT_LOAD_FACTOR, DEFAULT_SEGMENTS);
    }

    /**
     * Creates a new, empty set with a default initial capacity, load factor, and concurrency level.
     */
    public ConcurrentHashSet() {
        this(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR, DEFAULT_SEGMENTS);
    }

    /**
     * Creates a new set with the same elements as the given set. The set is created with a capacity of twice the number of elements in the
     * given map or <code>11</code> (whichever is greater), and a default load factor and concurrency level.
     * 
     * @param t the map
     */
    public ConcurrentHashSet(final Set<? extends E> t) {
        this(Math.max((int) (t.size() / DEFAULT_LOAD_FACTOR) + 1, 11), DEFAULT_LOAD_FACTOR, DEFAULT_SEGMENTS);
        addAll(t);
    }

    public Iterator<E> iterator() {
        return map.keySet().iterator();
    }

    public int size() {
        return map.size();
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public boolean contains(final Object o) {
        return map.containsKey(o);
    }

    public boolean add(final E o) {
        return map.put(o, PRESENT) == null;
    }

    public boolean remove(final Object o) {
        return map.remove(o) == PRESENT;
    }

    public void clear() {
        map.clear();
    }

    public boolean addAll(final Collection<? extends E> c) {
        boolean modified = false;
        final Iterator<? extends E> e = c.iterator();
        while (e.hasNext()) {
            if (add(e.next())) {
                modified = true;
            }
        }
        return modified;
    }

    public boolean containsAll(final Collection<?> c) {
        final Iterator<?> e = c.iterator();
        while (e.hasNext()) {
            if (!contains(e.next())) {
                return false;
            }
        }
        return true;
    }

    public boolean removeAll(final Collection<?> c) {
        boolean modified = false;

        if (size() > c.size()) {
            for (final Iterator<?> i = c.iterator(); i.hasNext();) {
                modified |= remove(i.next());
            }
        } else {
            for (final Iterator<?> i = iterator(); i.hasNext();) {
                if (c.contains(i.next())) {
                    i.remove();
                    modified = true;
                }
            }
        }
        return modified;
    }

    public boolean retainAll(final Collection<?> c) {
        boolean modified = false;
        final Iterator<E> e = iterator();
        while (e.hasNext()) {
            if (!c.contains(e.next())) {
                e.remove();
                modified = true;
            }
        }
        return modified;
    }

    public Object[] toArray() {
        final Object[] result = new Object[size()];
        final Iterator<E> e = iterator();
        for (int i = 0; e.hasNext(); i++) {
            result[i] = e.next();
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] a) {
        final int size = size();
        if (a.length < size) {
            a = (T[]) java.lang.reflect.Array.newInstance(a.getClass().getComponentType(), size);
        }
        final Iterator<E> it = iterator();
        final Object[] result = a;
        for (int i = 0; i < size; i++) {
            result[i] = it.next();
        }
        if (a.length > size) {
            a[size] = null;
        }
        return a;
    }

}
