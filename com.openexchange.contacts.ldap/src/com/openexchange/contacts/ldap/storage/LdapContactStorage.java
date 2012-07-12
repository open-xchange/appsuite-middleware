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

package com.openexchange.contacts.ldap.storage;

import java.util.Date;
import com.openexchange.contact.SortOptions;
import com.openexchange.contact.SortOrder;
import com.openexchange.contact.storage.DefaultContactStorage;
import com.openexchange.contacts.json.mapping.ContactMapper;
import com.openexchange.contacts.ldap.contacts.LdapContactInterface;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.ContactExceptionCodes;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.search.ContactSearchObject;
import com.openexchange.groupware.search.Order;
import com.openexchange.search.SearchTerm;
import com.openexchange.session.Session;
import com.openexchange.tools.iterator.SearchIterator;


/**
 * {@link LdapContactStorage}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 *
 */
public class LdapContactStorage extends DefaultContactStorage {

    private final int contextID;
    private final LdapContactInterface delegate;

    /**
     * Initializes a new {@link LdapContactStorage}.
     *
     * @param folderProperties The folder properties
     * @param adminId The admin ID
     * @param folderId The folder ID
     * @param contextId The context ID
     */
    public LdapContactStorage(int contextID, LdapContactInterface delegate) {
        super();
        this.contextID = contextID;
        this.delegate = delegate;
    }
    
    @Override
    public boolean supports(Session session, String folderId) throws OXException {
        return Integer.toString(delegate.getFolderId()).equals(folderId) && session.getContextId() == this.contextID;
    }

    @Override
    public Contact get(Session session, String folderId, String id, ContactField[] fields) throws OXException {
        return delegate.getObjectById(parse(id), parse(folderId));
    }

    @Override
    public SearchIterator<Contact> all(Session session, String folderId, ContactField[] fields, SortOptions sortOptions) throws OXException {
        int[] columnIDs = ContactMapper.getInstance().getColumnIDs(fields);
        Order order = Order.NO_ORDER;    
        int orderBy = 0;
        String collation = null;
        if (null != sortOptions) {
            collation = sortOptions.getCollation();
            if (null != sortOptions.getOrder() && 0 < sortOptions.getOrder().length) {
                SortOrder sortOrder = sortOptions.getOrder()[0];
                order = sortOrder.getOrder();            
                orderBy = ContactMapper.getInstance().get(sortOrder.getBy()).getColumnID();
            }
        }
        return delegate.getContactsInFolder(parse(folderId), 0, Integer.MAX_VALUE, orderBy, order, collation, columnIDs); 
    }

    @Override
    public SearchIterator<Contact> list(Session session, String folderId, String[] ids, ContactField[] fields, SortOptions sortOptions) throws OXException {
        int[] columnIDs = ContactMapper.getInstance().getColumnIDs(fields);
        int[][] objectIDandFolderID = new int[1][];
        objectIDandFolderID[0] = new int[ids.length];
        for (int i = 0; i < ids.length; i++) {
            objectIDandFolderID[0][i] = parse(ids[i]);
        }
        return delegate.getObjectsById(objectIDandFolderID, columnIDs);
    }

    @Override
    public SearchIterator<Contact> deleted(Session session, String folderId, Date since, ContactField[] fields) throws OXException {
        return deleted(session, folderId, since, fields, SortOptions.EMPTY);
    }

    @Override
    public SearchIterator<Contact> deleted(Session session, String folderId, Date since, ContactField[] fields, SortOptions sortOptions) throws OXException {
        int[] columnIDs = ContactMapper.getInstance().getColumnIDs(fields);
        return delegate.getDeletedContactsInFolder(parse(folderId), columnIDs, since);
    }

    @Override
    public SearchIterator<Contact> modified(Session session, String folderId, Date since, ContactField[] fields) throws OXException {
        return modified(session, folderId, since, fields, SortOptions.EMPTY);
    }

    @Override
    public SearchIterator<Contact> modified(Session session, String folderID, Date since, ContactField[] fields, SortOptions sortOptions) throws OXException {
        int[] columnIDs = ContactMapper.getInstance().getColumnIDs(fields);
        return delegate.getModifiedContactsInFolder(parse(folderID), columnIDs, since);
    }

    @Override
    public <O> SearchIterator<Contact> search(Session session, SearchTerm<O> term, ContactField[] fields, SortOptions sortOptions) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SearchIterator<Contact> search(Session session, ContactSearchObject contactSearch, ContactField[] fields, SortOptions sortOptions) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void create(Session session, String folderId, Contact contact) throws OXException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void update(Session session, String folderId, String id, Contact contact, Date lastRead) throws OXException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void updateReferences(Session session, Contact contact) throws OXException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void delete(Session session, int userID, String folderId, String id, Date lastRead) throws OXException {
        // TODO Auto-generated method stub
        
    }

    /**
     * Parses a numerical identifier from a string, wrapping a possible 
     * NumberFormatException into an OXException.
     * 
     * @param id the id string
     * @return the parsed identifier
     * @throws OXException
     */
    private static int parse(final String id) throws OXException {
        try {
            return Integer.parseInt(id);
        } catch (final NumberFormatException e) {
            throw ContactExceptionCodes.ID_PARSING_FAILED.create(e, id); 
        }
    }

}
