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

package gnu.trove;

import gnu.trove.function.TObjectFunction;
import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.procedure.TIntObjectProcedure;
import gnu.trove.procedure.TIntProcedure;
import gnu.trove.procedure.TObjectProcedure;
import gnu.trove.set.TIntSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * {@link EmptyTIntObjectMap}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class EmptyTIntObjectMap<V> implements TIntObjectMap<V> {

    private static final EmptyTIntObjectMap<Object> INSTANCE = new EmptyTIntObjectMap<Object>();

    /**
     * Gets the instance
     *
     * @return The instance
     */
    @SuppressWarnings("unchecked")
    public static <V> EmptyTIntObjectMap<V> getInstance() {
        return (EmptyTIntObjectMap<V>) INSTANCE;
    }

    private final TIntObjectIterator<V> emptyObjectIterator;

    /**
     * Initializes a new {@link EmptyTIntObjectMap}.
     */
    private EmptyTIntObjectMap() {
        super();
        emptyObjectIterator = new TIntObjectIterator<V>() {

            @Override
            public void advance() {
                throw new NoSuchElementException();
            }

            @Override
            public boolean hasNext() {
                return false;
            }

            @Override
            public void remove() {
                // Nope
            }

            @Override
            public int key() {
                return 0;
            }

            @Override
            public V value() {
                return null;
            }

            @Override
            public V setValue(final V val) {
                return null;
            }
        };

    }

    @Override
    public int getNoEntryKey() {
        return 0;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public boolean containsKey(final int key) {
        return false;
    }

    @Override
    public boolean containsValue(final Object value) {
        return false;
    }

    @Override
    public V get(final int key) {
        return null;
    }

    @Override
    public V put(final int key, final V value) {
        return null;
    }

    @Override
    public V putIfAbsent(final int key, final V value) {
        return null;
    }

    @Override
    public V remove(final int key) {
        return null;
    }

    @Override
    public void putAll(final Map<? extends Integer, ? extends V> m) {
        // Nothing to do
    }

    @Override
    public void putAll(final TIntObjectMap<? extends V> map) {
        // Nothing to do
    }

    @Override
    public void clear() {
        // Nothing to do
    }

    @Override
    public TIntSet keySet() {
        return EmptyTIntSet.getInstance();
    }

    @Override
    public int[] keys() {
        return new int[0];
    }

    @Override
    public int[] keys(final int[] array) {
        return new int[0];
    }

    @Override
    public Collection<V> valueCollection() {
        return Collections.<V> emptyList();
    }

    @SuppressWarnings("unchecked")
    @Override
    public V[] values() {
        return (V[]) new Object[0];
    }

    @SuppressWarnings("unchecked")
    @Override
    public V[] values(final V[] array) {
        return (V[]) new Object[0];
    }

    @Override
    public TIntObjectIterator<V> iterator() {
        return emptyObjectIterator;
    }

    @Override
    public boolean forEachKey(final TIntProcedure procedure) {
        return true;
    }

    @Override
    public boolean forEachValue(final TObjectProcedure<? super V> procedure) {
        return true;
    }

    @Override
    public boolean forEachEntry(final TIntObjectProcedure<? super V> procedure) {
        return true;
    }

    @Override
    public void transformValues(final TObjectFunction<V, V> function) {
        // Nothing to do
    }

    @Override
    public boolean retainEntries(final TIntObjectProcedure<? super V> procedure) {
        return false;
    }

}
