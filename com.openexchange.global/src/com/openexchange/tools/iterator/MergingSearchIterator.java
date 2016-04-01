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

    public MergingSearchIterator(final Comparator<T> criterion, boolean ascending, final SearchIterator<T>... iterators) throws OXException {
        this(criterion, ascending, Arrays.asList(iterators));
    }

    public MergingSearchIterator(final Comparator<T> criterion, boolean ascending, final List<SearchIterator<T>> iterators) throws OXException {
        this.iterators = iterators;
        this.topmost = new ArrayList<T>(iterators.size());
        for(final SearchIterator<T> iterator : iterators) {
            if(iterator.hasNext()) {
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
            if(iterator.hasWarnings()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public T next() throws OXException {
        if(!hasNext) {
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
        if(iterators.get(largestIndex).hasNext()) {
            topmost.set(largestIndex, iterators.get(largestIndex).next());
        } else {
            topmost.set(largestIndex, null);
        }

        // hasNext?
        hasNext = false;
        for(final T thing : topmost) {
            if(thing != null) {
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
