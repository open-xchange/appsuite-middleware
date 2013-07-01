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

import java.util.Collections;
import java.util.Date;
import java.util.List;
import com.openexchange.contact.SortOptions;
import com.openexchange.contact.SortOrder;
import com.openexchange.contact.storage.DefaultContactStorage;
import com.openexchange.contacts.json.mapping.ContactMapper;
import com.openexchange.contacts.ldap.contacts.LdapContactInterfaceProvider;
import com.openexchange.contacts.ldap.property.Mappings;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.ContactInterface;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.search.ContactSearchObject;
import com.openexchange.groupware.search.Order;
import com.openexchange.groupware.tools.mappings.json.JsonMapping;
import com.openexchange.search.SearchTerm;
import com.openexchange.session.Session;
import com.openexchange.tools.iterator.SearchIterator;


/**
 * {@link DelegatingLdapStorage}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class DelegatingLdapStorage extends DefaultContactStorage {

    private final int contextID;
    private final int priority;
    private final LdapContactInterfaceProvider provider;

    /**
     * Initializes a new {@link DelegatingLdapStorage}.
     *
     * @param contextID the context ID
     * @param provider the ldap contact interface provider
     * @param priority the storage priority
     */
    public DelegatingLdapStorage(int contextID, LdapContactInterfaceProvider provider, int priority) {
        super();
        this.contextID = contextID;
        this.provider = provider;
        this.priority = priority;
    }

    @Override
    public boolean supports(Session session, String folderId) throws OXException {
        return Integer.toString(delegate(session).getFolderId()).equals(folderId) && session.getContextId() == this.contextID;
    }

    @Override
    public int getPriority() {
        return this.priority;
    }

    @Override
    public Contact get(Session session, String folderId, String id, ContactField[] fields) throws OXException {
        return delegate(session).getObjectById(parse(id), parse(folderId));
    }

    @Override
    public SearchIterator<Contact> all(Session session, String folderId, ContactField[] fields, SortOptions sortOptions) throws OXException {
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
        return delegate(session).getContactsInFolder(parse(folderId), 0, Integer.MAX_VALUE, orderBy, order, collation, getColumns(fields));
    }

    @Override
    public SearchIterator<Contact> list(Session session, String folderId, String[] ids, ContactField[] fields, SortOptions sortOptions) throws OXException {
        int[][] objectIDandFolderID = new int[ids.length][];
        int folderID = parse(folderId);
        for (int i = 0; i < ids.length; i++) {
            objectIDandFolderID[i] = new int[] { parse(ids[i]), folderID };
        }
        return delegate(session).getObjectsById(objectIDandFolderID, getColumns(fields));
    }

    @Override
    public SearchIterator<Contact> deleted(Session session, String folderId, Date since, ContactField[] fields, SortOptions sortOptions) throws OXException {
        return delegate(session).getDeletedContactsInFolder(parse(folderId), getColumns(fields), since);
    }

    @Override
    public SearchIterator<Contact> modified(Session session, String folderID, Date since, ContactField[] fields, SortOptions sortOptions) throws OXException {
        return delegate(session).getModifiedContactsInFolder(parse(folderID), getColumns(fields), since);
    }

    @Override
    public int count(Session session, String folderID, boolean canReadAll) throws OXException {
        SearchIterator<Contact> contactsInFolder = delegate(session).getContactsInFolder(parse(folderID), 0, Integer.MAX_VALUE, 0, Order.NO_ORDER, null, getColumns(new ContactField[] { ContactField.OBJECT_ID }));
        int retval = contactsInFolder.size();
        contactsInFolder.close();
        return retval;
    }

    @Override
    public <O> SearchIterator<Contact> search(Session session, SearchTerm<O> term, ContactField[] fields, SortOptions sortOptions) throws OXException {
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
        return delegate(session).getContactsByExtendedSearch(term, orderBy, order, collation, getColumns(fields));
    }

    @Override
    public SearchIterator<Contact> search(Session session, ContactSearchObject contactSearch, ContactField[] fields, SortOptions sortOptions) throws OXException {
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
        return delegate(session).getContactsByExtendedSearch(contactSearch, orderBy, order, collation, getColumns(fields));
    }

    @Override
    public void create(Session session, String folderId, Contact contact) throws OXException {
        delegate(session).insertContactObject(contact);
    }

    @Override
    public void update(Session session, String folderId, String id, Contact contact, Date lastRead) throws OXException {
        delegate(session).updateContactObject(contact, parse(folderId), lastRead);
    }

    @Override
    public void updateReferences(Session session, Contact originalContact, Contact updatedContact) throws OXException {
        // no write access, do nothing
    }

    @Override
    public void delete(Session session, String folderId, String id, Date lastRead) throws OXException {
        delegate(session).deleteContactObject(parse(id), parse(folderId), lastRead);
    }

    @Override
    public SearchIterator<Contact> searchByBirthday(Session session, List<String> folderIDs, Date from, Date until, ContactField[] fields, SortOptions sortOptions) throws OXException {
        Mappings mappings = provider.getProperties().getMappings();
        if (null != mappings && null != mappings.getBirthday() && 0 < mappings.getBirthday().length()) {
            // use default implementation
            return super.searchByBirthday(session, folderIDs, from, until, fields, sortOptions);
        } else {
            LOG.debug("No mapping found, unable to search contacts by birthday.");
            return getSearchIterator(Collections.<Contact>emptyList());
        }
    }

    @Override
    public SearchIterator<Contact> searchByAnniversary(Session session, List<String> folderIDs, Date from, Date until, ContactField[] fields, SortOptions sortOptions) throws OXException {
        Mappings mappings = provider.getProperties().getMappings();
        if (null != mappings && null != mappings.getAnniversary() && 0 < mappings.getAnniversary().length()) {
            // use default implementation
            return super.searchByAnniversary(session, folderIDs, from, until, fields, sortOptions);
        } else {
            LOG.debug("No mapping found, unable to search contacts by anniversary.");
            return getSearchIterator(Collections.<Contact>emptyList());
        }
    }

    private ContactInterface delegate(Session session) throws OXException {
        return provider.newContactInterface(session);
    }

    /**
     * Gets the column IDs representing the supplied contact fields.
     *
     * @param fields
     * @return
     * @throws OXException
     */
    private static int[] getColumns(ContactField[] fields) throws OXException {
        if (null == fields) {
            return null;
        } else {
            int[] columnIDs = new int[fields.length];
            for (int i = 0; i < fields.length; i++) {
                JsonMapping<? extends Object, Contact> mapping = ContactMapper.getInstance().opt(fields[i]);
                if (null != mapping) {
                    columnIDs[i] = mapping.getColumnID();
                } else if (ContactField.CONTEXTID.equals(fields[i])) {
                    columnIDs[i] = Contact.CONTEXTID;
                } else if (ContactField.FILENAME.equals(fields[i])) {
                    columnIDs[i] = Contact.FILENAME;
                } else  {
                    throw OXException.notFound(fields[i].toString());
                }
            }
            return columnIDs;
        }
    }

}
