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

package com.openexchange.groupware.contact;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorException;
import com.openexchange.tools.iterator.SearchIterators;


/**
 * {@link ContactMergerator}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class ContactMergerator implements SearchIterator<Contact>{

    private Contact next = null;
    private List<RememberingIterator> iterators = null;
    private List<SearchIterator<Contact>> delegates = null;
    private OXException e;
    private OXException oxe;
    private final Comparator<Contact> comparator;


    public ContactMergerator(final Comparator<Contact> comparator, final SearchIterator<Contact>...iterators) throws OXException{
        this(comparator, Arrays.asList(iterators));
    }

    public ContactMergerator(final Comparator<Contact> comparator, final List<SearchIterator<Contact>> iterators) throws OXException{
        super();
        this.iterators = new ArrayList<RememberingIterator>(iterators.size());
        this.delegates = iterators;
        for (final SearchIterator<Contact> searchIterator : iterators) {
            if (searchIterator.hasNext()) {
                this.iterators.add(new RememberingIterator(searchIterator));
            }
        }
        this.comparator = comparator;
        grabNext();
    }

    private void grabNext() throws OXException{
        if (this.iterators.isEmpty()) {
            next = null;
            return;
        }
        try {
            if (this.iterators.size() > 1) {
                Collections.sort(iterators, new TopMostComparator(comparator));
            }
            next = this.iterators.get(0).currentOrNext();
        } catch (ExceptionTransporter transporter) {
            this.e = transporter.e;
        } catch (SearchIteratorException e) {
            this.e = e;
        } catch (OXException e) {
            this.oxe = e;
        } finally {
            if (this.iterators.get(0).hasNext()) {
                this.iterators.get(0).forgetCurrent();
            } else {
                this.iterators.remove(0);
            }
        }
    }

    @Override
    public void addWarning(final OXException warning) {
    }

    @Override
    public void close() {
        for (final SearchIterator<Contact> iter : delegates) {
            SearchIterators.close(iter);
        }
    }

    @Override
    public OXException[] getWarnings() {
        return null;
    }

    @Override
    public boolean hasNext() throws OXException {
        return next != null;
    }

    @Override
    public boolean hasWarnings() {
        return false;
    }

    @Override
    public Contact next() throws OXException {
        throwExceptions();
        final Contact nextContact = next;
        grabNext();
        return nextContact;
    }

    private void throwExceptions() throws OXException {
        if (e != null) {
            throw e;
        }
        if (oxe != null) {
            throw oxe;
        }
    }

    @Override
    public int size() {
        return -1;
    }

    private static final class RememberingIterator {
        private final SearchIterator<Contact> iterator;
        private Contact element;

        public RememberingIterator(final SearchIterator<Contact> iterator) {
            this.iterator = iterator;
        }

        public void forgetCurrent() {
            element = null;
        }

        public Contact currentOrNext() throws OXException {
            return (element == null) ? next() : getCurrent();
        }

        public Contact getCurrent() {
            return element;
        }

        public boolean hasNext() throws OXException {
            return iterator.hasNext();
        }

        public Contact next() throws OXException {
            return element = iterator.next();
        }
    }

    private static final class TopMostComparator implements Comparator<RememberingIterator> {

        private Comparator<Contact> contactComparator = null;


        public TopMostComparator(final Comparator<Contact> comparator) {
            this.contactComparator = comparator;
        }

        @Override
        public int compare(final RememberingIterator o1, final RememberingIterator o2) {
            try {
                final Contact v1 = o1.currentOrNext();
                final Contact v2 = o2.currentOrNext();
                return contactComparator.compare(v1, v2);
            } catch (OXException e) {
                throw new ExceptionTransporter(e);
            }
        }

    }

    private static final class ExceptionTransporter extends RuntimeException {

        /**
         * serialVersionUID
         */
        private static final long serialVersionUID = 1L;

        public OXException e;


        public ExceptionTransporter(final OXException x) {
            this.e = x;
        }
    }

}
