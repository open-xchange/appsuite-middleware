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

import com.openexchange.exception.OXException;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorAdapter;

/**
 * {@link Results}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class Results {

    public static <T> SearchIterator<T> emptyIterator() {
        return SearchIteratorAdapter.emptyIterator();
    }

    public static <T> TimedResult<T> emptyTimedResult() {
        return new EmptyTimedResult<T>();
    }

    public static <T> Delta<T> emptyDelta() {
        return new EmptyDelta<T>();
    }

    private static final class EmptyTimedResult<T> implements TimedResult<T> {

        public EmptyTimedResult() {
            // Do Noting
        }

        @Override
        public SearchIterator<T> results() throws OXException {
            return emptyIterator();
        }

        @Override
        public long sequenceNumber() throws OXException {
            return 0;
        }

    }

    private static final class EmptyDelta<T> implements Delta<T> {

        public EmptyDelta() {
            // Do Nothing
        }

        @Override
        public SearchIterator<T> getDeleted() {
            return emptyIterator();
        }

        @Override
        public SearchIterator<T> getModified() {
            return emptyIterator();
        }

        @Override
        public SearchIterator<T> getNew() {
            return emptyIterator();
        }

        @Override
        public SearchIterator<T> results() throws OXException {
            return emptyIterator();
        }

        @Override
        public long sequenceNumber() throws OXException {
            return 0;
        }

    }
}
