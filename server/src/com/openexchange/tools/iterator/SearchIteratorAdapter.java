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

package com.openexchange.tools.iterator;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.openexchange.api2.OXException;
import com.openexchange.groupware.AbstractOXException;

/**
 * {@link SearchIteratorAdapter} - An implementation of {@link SearchIterator} backed by a common instance of {@link Iterator} to which
 * calls are delegated.
 * <p>
 * Moreover this class provides several convenience implementations of {@link SearchIterator} accessible via {@link #createEmptyIterator()},
 * {@link #createArrayIterator(Object)} and {@link #toIterable(SearchIterator)}.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class SearchIteratorAdapter<T> implements SearchIterator<T> {

    private static final Log LOG = LogFactory.getLog(SearchIteratorAdapter.class);

    private final Iterator<T> delegate;

    private int size;

    private boolean b_size;

    private final List<AbstractOXException> warnings;

    public SearchIteratorAdapter(final Iterator<T> iter) {
        delegate = iter;
        warnings = new ArrayList<AbstractOXException>(2);
    }

    public SearchIteratorAdapter(final Iterator<T> iter, final int size) {
        delegate = iter;
        this.size = size;
        warnings = new ArrayList<AbstractOXException>(2);
        b_size = true;
    }

    public boolean hasNext() {
        return delegate.hasNext();
    }

    public T next() throws SearchIteratorException {
        return delegate.next();
    }

    public void close() {
    }

    public int size() {
        if (!b_size) {
            throw new UnsupportedOperationException("Size has not been set for this iterator");
        }
        return size;
    }

    public boolean hasSize() {
        return b_size;
    }

    public void addWarning(final AbstractOXException warning) {
        warnings.add(warning);
    }

    public AbstractOXException[] getWarnings() {
        return warnings.isEmpty() ? null : warnings.toArray(new AbstractOXException[warnings.size()]);
    }

    public boolean hasWarnings() {
        return !warnings.isEmpty();
    }

    public static <T> SearchIterator<T> createEmptyIterator() {
        return new SearchIterator<T>() {

            public boolean hasNext() {
                return false;
            }

            public T next() throws SearchIteratorException, OXException {
                return null;
            }

            public void close() throws SearchIteratorException {
            }

            public int size() {
                return 0;
            }

            public boolean hasSize() {
                return true;
            }

            public void addWarning(final AbstractOXException warning) {
            }

            public AbstractOXException[] getWarnings() {
                return null;
            }

            public boolean hasWarnings() {
                return false;
            }
        };
    }

    public static <T> SearchIterator<T> createArrayIterator(final T[] array) {
        if (null == array) {
            return createEmptyIterator();
        }
        /*
         * Tiny iterator implementation for arrays
         */
        class ArrayIterator implements SearchIterator<T> {

            private final int size;

            private int cursor;

            private final T[] arr;

            private final List<AbstractOXException> warnings;

            ArrayIterator(final T[] array) {
                super();
                this.arr = array;
                size = Array.getLength(array);
                warnings = new ArrayList<AbstractOXException>(2);
            }

            @SuppressWarnings("unused")
            public void remove() {
                throw new UnsupportedOperationException();
            }

            public boolean hasNext() {
                return (cursor < size);
            }

            public T next() {
                return arr[cursor++];
            }

            public void close() throws SearchIteratorException {
            }

            public int size() {
                return Array.getLength(arr);
            }

            public boolean hasSize() {
                return true;
            }

            public void addWarning(final AbstractOXException warning) {
                warnings.add(warning);
            }

            public AbstractOXException[] getWarnings() {
                return warnings.isEmpty() ? null : warnings.toArray(new AbstractOXException[warnings.size()]);
            }

            public boolean hasWarnings() {
                return !warnings.isEmpty();
            }

        }
        return new ArrayIterator(array);
    }

    public static <T> Iterable<T> toIterable(final SearchIterator<T> iterator) {
        class SIIterator implements Iterator<T> {

            public boolean hasNext() {
                return iterator.hasNext();
            }

            public T next() {
                try {
                    return iterator.next();
                } catch (final SearchIteratorException e) {
                    LOG.error(e.getMessage(), e);
                } catch (final OXException e) {
                    LOG.error(e.getMessage(), e);
                }
                return null;
            }

            public void remove() {
                throw new UnsupportedOperationException();
            }
        }

        return new Iterable<T>() {

            public Iterator<T> iterator() {
                return new SIIterator();
            }
        };
    }
}
