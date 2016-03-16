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
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * {@link HashKeySet} - The <code>String</code> set backed by a <code>HashKey</code> set.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class HashKeySet extends AbstractHashKeyCollection<HashKeySet> implements Set<String> {

    private final Set<HashKey> set;

    /**
     * Initializes a new {@link HashKeySet}.
     */
    public HashKeySet(final Set<HashKey> set) {
        super();
        this.set = set;
    }

    @Override
    protected HashKeySet thisCollection() {
        return this;
    }

    @Override
    public int size() {
        return set.size();
    }

    @Override
    public boolean isEmpty() {
        return set.isEmpty();
    }

    @Override
    public boolean contains(final Object o) {
        return set.contains(newKey(o.toString()));
    }

    @Override
    public Iterator<String> iterator() {
        final List<String> list = new ArrayList<String>(set.size());
        for (final HashKey key : set) {
            list.add(key.toString());
        }
        return list.iterator();
    }

    @Override
    public Object[] toArray() {
        final List<String> list = new ArrayList<String>(set.size());
        for (final HashKey key : set) {
            list.add(key.toString());
        }
        return list.toArray();
    }

    @Override
    public <T> T[] toArray(final T[] a) {
        final List<String> list = new ArrayList<String>(set.size());
        for (final HashKey key : set) {
            list.add(key.toString());
        }
        return list.toArray(a);
    }

    @Override
    public boolean add(final String e) {
        return set.add(newKey(e));
    }

    @Override
    public boolean remove(final Object o) {
        return set.remove(newKey(o.toString()));
    }

    @Override
    public boolean containsAll(final Collection<?> c) {
        final Collection<HashKey> col = new ArrayList<HashKey>(c.size());
        for (final Object o : c) {
            col.add(newKey(o.toString()));
        }
        return set.containsAll(col);
    }

    @Override
    public boolean addAll(final Collection<? extends String> c) {
        final Collection<HashKey> col = new ArrayList<HashKey>(c.size());
        for (final Object o : c) {
            col.add(newKey(o.toString()));
        }
        return set.addAll(col);
    }

    @Override
    public boolean retainAll(final Collection<?> c) {
        final Collection<HashKey> col = new ArrayList<HashKey>(c.size());
        for (final Object o : c) {
            col.add(newKey(o.toString()));
        }
        return set.retainAll(col);
    }

    @Override
    public boolean removeAll(final Collection<?> c) {
        final Collection<HashKey> col = new ArrayList<HashKey>(c.size());
        for (final Object o : c) {
            col.add(newKey(o.toString()));
        }
        return set.removeAll(col);
    }

    @Override
    public void clear() {
        set.clear();
    }

    @Override
    public boolean equals(final Object o) {
        return set.equals(o);
    }

    @Override
    public int hashCode() {
        return set.hashCode();
    }

    @Override
    public String toString() {
        return set.toString();
    }

}
