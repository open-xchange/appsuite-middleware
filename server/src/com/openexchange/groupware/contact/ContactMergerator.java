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

package com.openexchange.groupware.contact;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import com.openexchange.api2.OXException;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorException;


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
    private SearchIteratorException se;
    private OXException oxe;
    private Comparator<Contact> comparator;
    
    
    public ContactMergerator(Comparator<Contact> comparator, SearchIterator<Contact>...iterators) {
        this(comparator, Arrays.asList(iterators));
    }

    public ContactMergerator(Comparator<Contact> comparator, List<SearchIterator<Contact>> iterators) {
        super();
        this.iterators = new ArrayList<RememberingIterator>(iterators.size());
        this.delegates = iterators;
        for (SearchIterator<Contact> searchIterator : iterators) {
            if(searchIterator.hasNext()) {
                this.iterators.add(new RememberingIterator(searchIterator));
            }
        }
        this.comparator = comparator;
        grabNext();
    }

    private void grabNext() {
        if(this.iterators.isEmpty()) {
            next = null;
            return;
        }
        try {
            if(this.iterators.size() > 1) {
                Collections.sort(iterators, new TopMostComparator(comparator));
            }
            next = this.iterators.get(0).currentOrNext();
        } catch (ExceptionTransporter transporter) {
            this.oxe = transporter.oxe;
            this.se = transporter.se;
        } catch (SearchIteratorException e) {
            this.se = e;
        } catch (OXException e) {
            this.oxe = e;
        } finally {
            if(this.iterators.get(0).hasNext()) {
                this.iterators.get(0).forgetCurrent();
            } else {
                this.iterators.remove(0);
            }
        }
    }

    public void addWarning(AbstractOXException warning) {
    }

    public void close() throws SearchIteratorException {
        for (SearchIterator<Contact> iter : delegates) {
            iter.close();
        }
    }

    public AbstractOXException[] getWarnings() {
        return null;
    }

    public boolean hasNext() {
        return next != null;
    }

    public boolean hasSize() {
        return false;
    }

    public boolean hasWarnings() {
        return false;
    }

    public Contact next() throws SearchIteratorException, OXException {
        throwExceptions();
        Contact nextContact = next;
        grabNext();
        return nextContact;
    }

    private void throwExceptions() throws OXException, SearchIteratorException {
        if(se != null) {
            throw se;
        }
        if(oxe != null) {
            throw oxe;
        }
    }

    public int size() {
        return 0;
    }
    
    private static final class RememberingIterator {
        private SearchIterator<Contact> iterator;
        private Contact element;
        
        public RememberingIterator(SearchIterator<Contact> iterator) {
            this.iterator = iterator;
        }
        
        public void forgetCurrent() {
            element = null;
        }

        public Contact currentOrNext() throws SearchIteratorException, OXException {
            return (element == null) ? next() : getCurrent();
        }
        
        public Contact getCurrent() {
            return element;
        }
        
        public boolean hasNext() {
            return iterator.hasNext();
        }
        
        public Contact next() throws SearchIteratorException, OXException {
            return element = iterator.next();
        }
    }
    
    private static final class TopMostComparator implements Comparator<RememberingIterator> {

        private Comparator<Contact> contactComparator = null;
        

        public TopMostComparator(Comparator<Contact> comparator) {
            this.contactComparator = comparator;
        }
        
        public int compare(RememberingIterator o1, RememberingIterator o2) {
            try {
                Contact v1 = o1.currentOrNext();
                Contact v2 = o2.currentOrNext();
                return contactComparator.compare(v1, v2);
            } catch (SearchIteratorException e) {
                throw new ExceptionTransporter(e);
            } catch (OXException e) {
                throw new ExceptionTransporter(e);
            }
        }

    }
    
    private static final class ExceptionTransporter extends RuntimeException {
        public OXException oxe;
        public SearchIteratorException se;

        public ExceptionTransporter(OXException x) {
            this.oxe = x;
        }
        
        public ExceptionTransporter(SearchIteratorException x) {
            this.se = x;
        }
    }

}
