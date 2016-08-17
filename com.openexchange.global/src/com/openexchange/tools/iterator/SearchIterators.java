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
import java.util.List;
import java.util.NoSuchElementException;
import java.util.LinkedList;
import org.slf4j.Logger;
import com.openexchange.exception.OXException;

/**
 * {@link SearchIterators} - Utility class for {@link SearchIterator}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class SearchIterators {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(SearchIterators.class);

    /**
     * Initializes a new {@link SearchIterators}.
     */
    private SearchIterators() {
        super();
    }

    /**
     * (Safely) Closes specified {@link SearchIterator} instance.
     *
     * @param iterator The iterator to close
     */
    public static void close(final SearchIterator<?> iterator) {
        if (null != iterator) {
            try {
                iterator.close();
            } catch (final Exception e) {
                // Ignore
                LOGGER.error("Closing SearchIterator instance failed", e);
            }
        }
    }

    /**
     * Iterates through the supplied search iterator and puts all elements into a list.
     *
     * @param iterator The search iterator to get the elements from
     * @return The iterator's elements in a list
     */
    public static <T> List<T> asList(SearchIterator<T> iterator) throws OXException {
        if (null == iterator) {
            return null;
        }
        List<T> list = new ArrayList<T>();
        while (iterator.hasNext()) {
            list.add(iterator.next());
        }
        return list;
    }

    @SuppressWarnings("rawtypes")
    private static final SearchIterator EMPTY_ITERATOR = new EmptySearchIterator<>();

    /**
     * Gets the singleton iterator for specified element
     *
     * @param element The element
     * @return The singleton iterator
     */
    @SuppressWarnings("unchecked")
    public static <T> SearchIterator<T> singletonIterator(T element) {
        if (null == element) {
            return EMPTY_ITERATOR;
        }

        return new SingletonSearchIterator<T>(element);
    }

    /**
     * Gets an empty iterator.
     *
     * @param <T> type of elements, if there were any, in the list
     * @return An empty iterator
     */
    @SuppressWarnings("unchecked")
    public static final <T> SearchIterator<T> emptyIterator() {
        return EMPTY_ITERATOR;
    }

    // ------------------------------------------------------------------------------------------------------------------------------

    private static final class EmptySearchIterator<T> implements SearchIterator<T> {

        EmptySearchIterator() {
            super();
        }

        @Override
        public boolean hasNext() throws OXException {
            return false;
        }

        @Override
        public T next() throws OXException {
            throw new NoSuchElementException("Empty iterator has no elements");
        }

        @Override
        public void close() {
            // Nothing
        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public boolean hasWarnings() {
            return false;
        }

        @Override
        public void addWarning(OXException warning) {
            // Nothing
        }

        @Override
        public OXException[] getWarnings() {
            return null;
        }
    }

    private static final class SingletonSearchIterator<T> implements SearchIterator<T> {

        private final List<OXException> warnings;
        private T element;

        SingletonSearchIterator(T element) {
            super();
            this.element = element;
            warnings = new LinkedList<OXException>();
        }

        @Override
        public boolean hasNext() throws OXException {
            return null != element;
        }

        @Override
        public T next() throws OXException {
            T retval = this.element;
            if (null == retval) {
                throw new NoSuchElementException();
            }
            this.element = null;
            return retval;
        }

        @Override
        public void close() {
            // Nothing
        }

        @Override
        public int size() {
            return null == element ? 0 : 1;
        }

        @Override
        public boolean hasWarnings() {
            return !warnings.isEmpty();
        }

        @Override
        public void addWarning(OXException warning) {
            if (null != warning) {
                warnings.add(warning);
            }
        }

        @Override
        public OXException[] getWarnings() {
            int size = warnings.size();
            return size == 0 ? null : warnings.toArray(new OXException[size]);
        }
    }

}
