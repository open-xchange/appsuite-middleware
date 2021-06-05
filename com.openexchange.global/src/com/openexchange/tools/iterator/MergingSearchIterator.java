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

package com.openexchange.tools.iterator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import com.openexchange.exception.OXException;

/**
 * A {@link MergingSearchIterator} merges multiple (already sorted) search iterators according to a given criterion.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class MergingSearchIterator<T> implements SearchIterator<T> {

    private List<SearchIterator<T>> iterators = null;
    private Comparator<T> comparator;
    private List<T> topmost = null;
    private boolean ascending = true;
    private boolean hasNext;

    @SafeVarargs
    public MergingSearchIterator(final Comparator<T> criterion, boolean ascending, final SearchIterator<T>... iterators) throws OXException {
        this(criterion, ascending, Arrays.asList(iterators));
    }

    public MergingSearchIterator(final Comparator<T> criterion, boolean ascending, final List<SearchIterator<T>> iterators) throws OXException {
        this.iterators = iterators;
        this.topmost = new ArrayList<T>(iterators.size());
        for(final SearchIterator<T> iterator : iterators) {
            if (iterator.hasNext()) {
                topmost.add(iterator.next());
                hasNext = true;
            } else {
                topmost.add(null);
            }
        }
        this.comparator = criterion;
        this.ascending = ascending;
    }


    @Override
    public void addWarning(final OXException warning) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void close() {
        for (final SearchIterator<T> iterator : iterators) {
            SearchIterators.close(iterator);
        }
    }

    @Override
    public OXException[] getWarnings() {
        final List<OXException> exceptions = new ArrayList<OXException>(10);
        for (final SearchIterator<T> iterator : iterators) {
            if (iterator.hasWarnings()) {
                final OXException[] warnings = iterator.getWarnings();
                if (warnings != null) {
                    exceptions.addAll(Arrays.asList(warnings));
                }
            }
        }
        return exceptions.toArray(new OXException[exceptions.size()]);
    }

    @Override
    public boolean hasNext() throws OXException {
        return hasNext;
    }

    @Override
    public boolean hasWarnings() {
        for(final SearchIterator<T> iterator : iterators) {
            if (iterator.hasWarnings()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public T next() throws OXException {
        if (!hasNext) {
            return null;
        }
        // Find largest index
        T largest = null;
        int i = -1;
        int largestIndex = 0;
        int direction = ascending ? 1 : -1;
        for(final T candidate : topmost) {
            i++;
            if (candidate != null && (largest == null || 0 > direction * comparator.compare(largest, candidate))) {
                largest = candidate;
                largestIndex = i;
            }
        }
        // Replenish
        if (iterators.get(largestIndex).hasNext()) {
            topmost.set(largestIndex, iterators.get(largestIndex).next());
        } else {
            topmost.set(largestIndex, null);
        }

        // hasNext?
        hasNext = false;
        for(final T thing : topmost) {
            if (thing != null) {
                hasNext = true;
            }
        }

        return largest;
    }

    @Override
    public int size() {
        int size = 0;
        for (final SearchIterator<T> iterator : iterators) {
            final int s = iterator.size();
            if (s == -1) {
                return -1;
            }
            size += s;
        }
        return size;
    }

}
