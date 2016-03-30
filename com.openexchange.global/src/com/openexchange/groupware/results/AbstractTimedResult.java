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
