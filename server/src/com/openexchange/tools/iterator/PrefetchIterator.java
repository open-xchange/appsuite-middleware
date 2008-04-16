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

import java.util.LinkedList;
import java.util.Queue;

import com.openexchange.api2.OXException;
import com.openexchange.configuration.ServerConfig;
import com.openexchange.configuration.ServerConfig.Property;
import com.openexchange.groupware.EnumComponent;

/**
 * This iterator prefetches the delegating iterator results if the server
 * configuration contains <code>true</code> for the prefetch parameter.
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class PrefetchIterator<T> implements SearchIterator<T> {

    /**
     * The used implementation.
     */
    private final SearchIterator<T> impl;

    /**
     * Default constructor.
     * @param delegate Delegating iterator.
     */
    public PrefetchIterator(final SearchIterator<T> delegate) {
        final boolean prefetch = ServerConfig.getBoolean(Property
            .PrefetchEnabled);
        if (prefetch) {
            impl = new Prefetch<T>(delegate);
        } else {
            impl = new NoPrefetch<T>(delegate);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void close() throws SearchIteratorException {
        impl.close();
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasNext() {
        return impl.hasNext();
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasSize() {
        return impl.hasSize();
    }

    /**
     * {@inheritDoc}
     */
    public T next() throws SearchIteratorException, OXException {
        return impl.next();
    }

    /**
     * {@inheritDoc}
     */
    public int size() {
        return impl.size();
    }

    /**
     * This class prefetches the result.
     * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
     * @param <T> type of objects in this iterator.
     */
    private static final class Prefetch<T> implements SearchIterator<T> {

        /**
         * Iterator for the object.
         */
        private final SearchIterator<T> delegate;

        private Queue<T> data;

        private OXException oxExc;

        private SearchIteratorException sie;

        private SearchIteratorException closeexc;

        /**
         * Default constructor.
         * @param delegate Iterator for the object.
         */
        Prefetch(final SearchIterator<T> delegate) {
            super();
            this.delegate = delegate;
            fetch();
        }

        /**
         * Reads all data from the delegate.
         */
        private void fetch() {
            if (delegate.hasSize()) {
                data = new LinkedList<T>();
            } else {
                data = new LinkedList<T>();
            }
            while (delegate.hasNext()) {
                try {
                    data.offer(delegate.next());
                } catch (OXException e) {
                    oxExc = e;
                    break;
                } catch (SearchIteratorException e) {
                    sie = e;
                    break;
                }
            }
            try {
                delegate.close();
            } catch (SearchIteratorException e) {
                closeexc = e;
            }
        }

        /**
         * {@inheritDoc}
         */
        public void close() throws SearchIteratorException {
            data.clear();
            if (null != closeexc) {
                throw closeexc;
            }
        }

        /**
         * {@inheritDoc}
         */
        public boolean hasNext() {
            return !data.isEmpty() || null != oxExc || null != sie;
        }

        /**
         * {@inheritDoc}
         */
        public boolean hasSize() {
            return true;
        }

        /**
         * {@inheritDoc}
         */
        public T next() throws SearchIteratorException, OXException {
            if (data.isEmpty()) {
                if (null != oxExc) {
                    throw oxExc;
                }
                if (null != sie) {
                    throw sie;
                }
                throw new SearchIteratorException(SearchIteratorException
                    .SearchIteratorCode.NO_SUCH_ELEMENT, EnumComponent.APPOINTMENT);
            }
            return data.poll();
        }

        /**
         * {@inheritDoc}
         */
        public int size() {
            return data.size();
        }
    }

    /**
     * This class doesn't prefetch the result.
     * @param <T> type of objects in this iterator.
     */
    private static final class NoPrefetch<T> implements SearchIterator<T> {

        /**
         * Iterator for the object.
         */
        private final SearchIterator<T> delegate;

        /**
         * Default constructor.
         * @param delegate Iterator for the object.
         */
        NoPrefetch(final SearchIterator<T> delegate) {
            super();
            this.delegate = delegate;
        }

        /**
         * {@inheritDoc}
         */
        public void close() throws SearchIteratorException {
            delegate.close();
        }

        /**
         * {@inheritDoc}
         */
        public boolean hasNext() {
            return delegate.hasNext();
        }

        /**
         * {@inheritDoc}
         */
        public boolean hasSize() {
            return delegate.hasSize();
        }

        /**
         * {@inheritDoc}
         */
        public T next() throws SearchIteratorException, OXException {
            return delegate.next();
        }

        /**
         * {@inheritDoc}
         */
        public int size() {
            return delegate.size();
        }
    }
}
