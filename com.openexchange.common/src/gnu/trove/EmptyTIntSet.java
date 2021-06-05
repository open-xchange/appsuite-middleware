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

package gnu.trove;

import java.io.Serializable;
import java.util.Collection;
import java.util.NoSuchElementException;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.procedure.TIntProcedure;
import gnu.trove.set.TIntSet;

/**
 * {@link EmptyTIntSet} - The empty {@link TIntSet} implementation.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class EmptyTIntSet implements TIntSet, Serializable {

    private static final long serialVersionUID = 912737656030728279L;

    private static final EmptyTIntSet INSTANCE = new EmptyTIntSet();

    /**
     * Gets the instance.
     *
     * @return The instance
     */
    public static EmptyTIntSet getInstance() {
        return INSTANCE;
    }

    // -------------------------------------------------------------------------------

    private final transient EmptyTIntIterator emptyIter;

    /**
     * Initializes a new {@link EmptyTIntSet}.
     */
    private EmptyTIntSet() {
        super();
        emptyIter = new EmptyTIntIterator();
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

    private static class EmptyTIntIterator implements TIntIterator, Serializable {

        private static final long serialVersionUID = 1L;

        EmptyTIntIterator() {
            super();
        }

        @Override
        public void remove() {
            // Nothing to do
        }

        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public int next() {
            throw new NoSuchElementException();
        }
    }

}
