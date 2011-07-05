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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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
import com.openexchange.groupware.AbstractOXException;

/**
 * A {@link MergingSearchIterator} merges multiple (already sorted) search iterators according to a given criterion.
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class MergingSearchIterator<T> implements SearchIterator<T> {

    private List<SearchIterator<T>> iterators = null;
    private Comparator<T> comparator;
    private List<T> topmost = null;
    private boolean hasNext;
    
    public MergingSearchIterator(Comparator<T> criterion, SearchIterator<T>...iterators) throws AbstractOXException {
        this(criterion, Arrays.asList(iterators));
    }

    public MergingSearchIterator(Comparator<T> criterion, List<SearchIterator<T>> iterators) throws AbstractOXException {
        this.iterators = iterators;
        this.topmost = new ArrayList<T>(iterators.size());
        for(SearchIterator<T> iterator : iterators) {
            if(iterator.hasNext()) {
                topmost.add(iterator.next());
                hasNext = true;
            } else {
                topmost.add(null);
            }
        }
        this.comparator = criterion;
    }


    public void addWarning(AbstractOXException warning) {
        throw new UnsupportedOperationException();
    }

    public void close() throws OXException {
        AbstractOXException exception = null;
        for (SearchIterator<T> iterator : iterators) {
            try {
                iterator.close();
            } catch (AbstractOXException x) {
                exception = x;
            }
        }
        if (exception != null) {
            throw exception;
        }
    }

    public AbstractOXException[] getWarnings() {
        List<AbstractOXException> exceptions = new ArrayList<AbstractOXException>(10);
        for (SearchIterator<T> iterator : iterators) {
            if (iterator.hasWarnings()) {
                AbstractOXException[] warnings = iterator.getWarnings();
                if (warnings != null) {
                    exceptions.addAll(Arrays.asList(warnings));
                }
            }
        }
        return exceptions.toArray(new AbstractOXException[exceptions.size()]);
    }

    public boolean hasNext() throws OXException {
        return hasNext;
    }

    public boolean hasWarnings() {
        for(SearchIterator<T> iterator : iterators) {
            if(iterator.hasWarnings()) {
                return true;
            }
        }
        return false;
    }

    public T next() throws OXException {
        if(!hasNext) {
            return null;
        }
        // Find largest index
        T largest = null;
        int i = -1;
        int largestIndex = 0;
        for(T candidate : topmost) {
            i++;
            if(candidate != null && (largest == null || 0 > comparator.compare(largest, candidate))) {
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
        for(T thing : topmost) {
            if(thing != null) {
                hasNext = true;
            }
        }
        
        return largest;
    }

    public int size() {
        int size = 0;
        for (SearchIterator<T> iterator : iterators) {
            int s = iterator.size();
            if (s == -1) {
                return -1;
            }
            size += s;
        }
        return size;
    }

}
