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

package com.openexchange.groupware.results;

import java.util.LinkedList;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorAdapter;
import com.openexchange.tools.iterator.SearchIterators;

public abstract class AbstractTimedResult<T> implements TimedResult<T> {

    private final LastModifiedExtractorIterator results;
    long sequenceNumber;

    public AbstractTimedResult(final SearchIterator<T> results) {
        this.results = new LastModifiedExtractorIterator(results);
    }

    @Override
    public SearchIterator<T> results() throws OXException {
        return results;
    }

    @Override
    public long sequenceNumber() throws OXException {
        if (results.hasNext()) {
            results.fastForward();
        }
        return sequenceNumber;
    }

    protected abstract long extractTimestamp(T object);

    private class LastModifiedExtractorIterator implements SearchIterator<T> {

        @SuppressWarnings("hiding")
        private SearchIterator<T> results;

        private OXException oxexception;

        private boolean fastForwardDone;

        private final int size;

        public LastModifiedExtractorIterator(final SearchIterator<T> results) {
            this.results = results;
            this.size = results.size();
        }

        @Override
        public void addWarning(final OXException warning) {
            results.addWarning(warning);
        }

        @Override
        public void close() {
            SearchIterators.close(results);
        }

        @Override
        public OXException[] getWarnings() {
            return results.getWarnings();
        }

        @Override
        public boolean hasNext() throws OXException {
            if (oxexception != null) {
                throw oxexception;
            }
            return results.hasNext();
        }

        @Override
        public int size() {
            return size;
        }

        @Override
        public boolean hasWarnings() {
            return results.hasWarnings();
        }

        @Override
        public T next() throws OXException {

            if (this.oxexception != null) {
                throw this.oxexception;
            }

            final T nextObject = results.next();
            if (fastForwardDone) {
                return nextObject;
            }
            final long timestamp = extractTimestamp(nextObject);
            if (timestamp > sequenceNumber) {
                sequenceNumber = timestamp;
            }
            return nextObject;
        }

        void fastForward() throws OXException {
            List<T> moreValues = new LinkedList<T>();
            while (hasNext()) {
                try {
                    moreValues.add(next());
                } catch (OXException e) {
                    this.oxexception = e;
                    break;
                }
            }
            final OXException[] warnings = results.getWarnings();
            results = new SearchIteratorAdapter<T>(moreValues.iterator());
            if (warnings != null) {
                for (final OXException warning : warnings) {
                    results.addWarning(warning);
                }
            }
            this.fastForwardDone = true;
        }

    }

}
