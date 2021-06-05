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

package com.openexchange.groupware.tools.iterator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import com.openexchange.configuration.ServerConfig;
import com.openexchange.configuration.ServerConfig.Property;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.EnumComponent;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorExceptionCodes;
import com.openexchange.tools.iterator.SearchIterators;

/**
 * This iterator prefetches the delegating iterator results if the server configuration contains <code>true</code> for the prefetch
 * parameter.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class PrefetchIterator<T> implements SearchIterator<T> {

    /**
     * The used implementation.
     */
    private final SearchIterator<T> impl;

    /**
     * Default constructor.
     *
     * @param delegate Delegating iterator.
     * @throws OXException
     */
    public PrefetchIterator(final SearchIterator<T> delegate) throws OXException {
        final boolean prefetch = ServerConfig.getBoolean(Property.PrefetchEnabled);
        if (prefetch) {
            impl = new Prefetch<T>(delegate);
        } else {
            impl = new NoPrefetch<T>(delegate);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        SearchIterators.close(impl);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasNext() throws OXException{
        return impl.hasNext();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T next() throws OXException {
        return impl.next();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int size() {
        return impl.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addWarning(final OXException warning) {
        impl.addWarning(warning);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OXException[] getWarnings() {
        return impl.getWarnings();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasWarnings() {
        return impl.hasWarnings();
    }

    /**
     * This class prefetches the result.
     *
     * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
     * @param <T> type of objects in this iterator.
     */
    private static final class Prefetch<T> implements SearchIterator<T> {

        /**
         * Iterator for the object.
         */
        private final SearchIterator<T> delegate;
        private Queue<T> data;
        private final List<OXException> warnings;
        private OXException oxExc;

        /**
         * Default constructor.
         *
         * @param delegate Iterator for the object.
         * @throws OXException
         */
        Prefetch(final SearchIterator<T> delegate) throws OXException {
            super();
            warnings = new ArrayList<OXException>(2);
            this.delegate = delegate;
            fetch();
            if (delegate.hasWarnings()) {
                warnings.addAll(Arrays.asList(delegate.getWarnings()));
            }
        }

        /**
         * Reads all data from the delegate.
         * @throws OXException
         */
        private void fetch() throws OXException {
            data = new LinkedList<T>();
            while (delegate.hasNext()) {
                try {
                    data.add(delegate.next());
                } catch (OXException e) {
                    oxExc = e;
                    break;
                }
            }
            SearchIterators.close(delegate);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void close() {
            data.clear();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean hasNext() throws OXException {
            return !data.isEmpty() || (null != oxExc);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public T next() throws OXException {
            if (data.isEmpty()) {
                if (null != oxExc) {
                    throw oxExc;
                }
                throw SearchIteratorExceptionCodes.NO_SUCH_ELEMENT.create().setPrefix(EnumComponent.APPOINTMENT.getAbbreviation());
            }
            return data.poll();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int size() {
            return data.size();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void addWarning(final OXException warning) {
            warnings.add(warning);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public OXException[] getWarnings() {
            return warnings.isEmpty() ? null : warnings.toArray(new OXException[warnings.size()]);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean hasWarnings() {
            return !warnings.isEmpty();
        }
    }

    /**
     * This class doesn't prefetch the result.
     *
     * @param <T> type of objects in this iterator.
     */
    private static final class NoPrefetch<T> implements SearchIterator<T> {

        /**
         * Iterator for the object.
         */
        private final SearchIterator<T> delegate;

        /**
         * Default constructor.
         *
         * @param delegate Iterator for the object.
         */
        NoPrefetch(final SearchIterator<T> delegate) {
            super();
            this.delegate = delegate;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void close() {
            SearchIterators.close(delegate);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean hasNext() throws OXException {
            return delegate.hasNext();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public T next() throws OXException {
            return delegate.next();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int size() {
            return delegate.size();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void addWarning(final OXException warning) {
            delegate.addWarning(warning);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public OXException[] getWarnings() {
            return delegate.getWarnings();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean hasWarnings() {
            return delegate.hasWarnings();
        }
    }
}
