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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.contact.storage;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import com.openexchange.contact.SortOptions;
import com.openexchange.contact.storage.internal.SearchAdapter;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.ContactExceptionCodes;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.search.ContactSearchObject;
import com.openexchange.search.SearchTerm;
import com.openexchange.session.Session;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorAdapter;

/**
 * {@link DefaultContactStorage}
 * 
 * Abstract {@link ContactStorage} implementation.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public abstract class DefaultContactStorage implements ContactStorage {
    
    /**
     * Initializes a new {@link DefaultContactStorage}.
     */
    public DefaultContactStorage() {
        super();
    }
    
    @Override
    public SearchIterator<Contact> all(Session session, final String folderId, final ContactField[] fields) throws OXException {
        return this.all(session, folderId, fields, SortOptions.EMPTY);
    }

    @Override
    public SearchIterator<Contact> list(Session session, final String folderId, final String[] ids, final ContactField[] fields) throws OXException {
        return this.list(session, folderId, ids, fields, SortOptions.EMPTY);        
    }
    
    @Override
    public <O> SearchIterator<Contact> search(Session session, SearchTerm<O> term, ContactField[] fields) throws OXException {
        return this.search(session, term, fields, SortOptions.EMPTY);
    }

    @Override
    public SearchIterator<Contact> search(Session session, ContactSearchObject contactSearch, ContactField[] fields) throws OXException {
        return this.search(session, contactSearch, fields, SortOptions.EMPTY);
    }

    @Override
    public SearchIterator<Contact> deleted(Session session, String folderId, Date since, ContactField[] fields) throws OXException {
        return deleted(session, folderId, since, fields, SortOptions.EMPTY);
    }

    @Override
    public SearchIterator<Contact> modified(Session session, String folderId, Date since, ContactField[] fields) throws OXException {
        return modified(session, folderId, since, fields, SortOptions.EMPTY);
    }

    /**
     * Default implementation converting the {@link ContactSearchObject} 
     * to a {@link SearchTerm}. Override if applicable for storage.
     */
    @Override
    public SearchIterator<Contact> search(Session session, ContactSearchObject contactSearch, ContactField[] fields, SortOptions sortOptions) throws OXException {
        return search(session, getSearchTerm(contactSearch), fields, sortOptions);
    }

    /**
     * Gets all contact fields.
     * 
     * @return the fields
     */
    protected static ContactField[] allFields() {
        return ContactField.values();
    }
    
    /**
     * Creates a new {@link SearchIterator} for the supplied contact collection.
     * 
     * @param contacts the contacts, or <code>null</code> to create an empty iterator
     * @return the contact search iterator
     */
    protected static SearchIterator<Contact> getSearchIterator(Collection<Contact> contacts) {
        if (null == contacts) {
            List<Contact> emptyList = Collections.emptyList(); 
            return new SearchIteratorAdapter<Contact>(emptyList.iterator(), 0);
        } else {
            return new SearchIteratorAdapter<Contact>(contacts.iterator(), contacts.size());
        }
    }
    
    /**
     * Parses a numerical identifier from a string, wrapping a possible 
     * NumberFormatException into an OXException.
     * 
     * @param id the id string
     * @return the parsed identifier
     * @throws OXException
     */
    protected static int parse(String id) throws OXException {
        try {
            return Integer.parseInt(id);
        } catch (NumberFormatException e) {
            throw ContactExceptionCodes.ID_PARSING_FAILED.create(e, id); 
        }
    }

    /**
     * Parses an array of numerical identifiers from a string, wrapping a 
     * possible NumberFormatException into an OXException.
     * 
     * @param id the id string
     * @return the parsed identifier
     * @throws OXException
     */
    protected static int[] parse(String[] ids) throws OXException {
        try {
            int[] intIDs = new int[ids.length];
            for (int i = 0; i < intIDs.length; i++) {
                intIDs[i] = Integer.parseInt(ids[i]);
            }
            return intIDs;
        } catch (NumberFormatException e) {
            throw ContactExceptionCodes.ID_PARSING_FAILED.create(e, null != ids ? ids.toString() : null); 
        }
    }
    
    /**
     * Converts the supplied contact search object into a search term.
     * 
     * @param contactSearch the contact search
     * @return the search term
     * @throws OXException
     */
    protected static SearchTerm<?> getSearchTerm(ContactSearchObject contactSearch) throws OXException {
        return new SearchAdapter(contactSearch).getSearchTerm();
    }
    
}
