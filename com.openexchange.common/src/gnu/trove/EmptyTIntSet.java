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

import gnu.trove.iterator.TIntIterator;
import gnu.trove.procedure.TIntProcedure;
import gnu.trove.set.TIntSet;
import java.util.Collection;
import java.util.NoSuchElementException;

/**
 * {@link EmptyTIntSet} - The empty {@link TIntSet} implementation.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class EmptyTIntSet implements TIntSet {

    private static final EmptyTIntSet INSTANCE = new EmptyTIntSet();

    /**
     * Gets the instance.
     *
     * @return The instance
     */
    public static EmptyTIntSet getInstance() {
        return INSTANCE;
    }

    private final TIntIterator emptyIter;

    /**
     * Initializes a new {@link EmptyTIntSet}.
     */
    private EmptyTIntSet() {
        super();
        emptyIter = new TIntIterator() {

            @Override
            public void remove() {
                //
            }

            @Override
            public boolean hasNext() {
                return false;
            }

            @Override
            public int next() {
                throw new NoSuchElementException();
            }
        };
    }

    @Override
    public int getNoEntryValue() {
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
    public boolean contains(final int entry) {
        return false;
    }

    @Override
    public TIntIterator iterator() {
        return emptyIter;
    }

    @Override
    public int[] toArray() {
        return new int[0];
    }

    @Override
    public int[] toArray(final int[] dest) {
        return new int[0];
    }

    @Override
    public boolean add(final int entry) {
        return false;
    }

    @Override
    public boolean remove(final int entry) {
        return false;
    }

    @Override
    public boolean containsAll(final Collection<?> collection) {
        return false;
    }

    @Override
    public boolean containsAll(final TIntCollection collection) {
        return false;
    }

    @Override
    public boolean containsAll(final int[] array) {
        return false;
    }

    @Override
    public boolean addAll(final Collection<? extends Integer> collection) {
        return false;
    }

    @Override
    public boolean addAll(final TIntCollection collection) {
        return false;
    }

    @Override
    public boolean addAll(final int[] array) {
        return false;
    }

    @Override
    public boolean retainAll(final Collection<?> collection) {
        return false;
    }

    @Override
    public boolean retainAll(final TIntCollection collection) {
        return false;
    }

    @Override
    public boolean retainAll(final int[] array) {
        return false;
    }

    @Override
    public boolean removeAll(final Collection<?> collection) {
        return false;
    }

    @Override
    public boolean removeAll(final TIntCollection collection) {
        return false;
    }

    @Override
    public boolean removeAll(final int[] array) {
        return false;
    }

    @Override
    public void clear() {
        // Nope
    }

    @Override
    public boolean forEach(final TIntProcedure procedure) {
        return true;
    }

}
