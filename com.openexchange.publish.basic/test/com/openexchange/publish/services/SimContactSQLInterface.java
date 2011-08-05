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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

package com.openexchange.publish.services;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import com.openexchange.api2.ContactSQLInterface;
import com.openexchange.contact.LdapServer;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.ContactUnificationState;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.search.ContactSearchObject;
import com.openexchange.groupware.search.Order;
import com.openexchange.search.SearchTerm;
import com.openexchange.session.Session;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorAdapter;

/**
 * {@link SimContactSQLInterface}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class SimContactSQLInterface implements ContactSQLInterface {

    private final Map<Integer, List<Contact>> folders = new HashMap<Integer, List<Contact>>();

    /*
     * (non-Javadoc)
     * @see com.openexchange.api2.ContactSQLInterface#deleteContactObject(int, int, java.util.Date)
     */
    @Override
    public void deleteContactObject(final int objectId, final int inFolder, final Date clientLastModified) throws OXException {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.api2.ContactSQLInterface#insertContactObject(com.openexchange.groupware.container.ContactObject)
     */
    @Override
    public void insertContactObject(final Contact contactObj) throws OXException {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.api2.ContactSQLInterface#updateContactObject(com.openexchange.groupware.container.ContactObject, int,
     * java.util.Date)
     */
    @Override
    public void updateContactObject(final Contact contactObj, final int inFolder, final Date clientLastModified) throws OXException {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateUserContact(final Contact contact, final Date lastmodified) throws OXException {
        // Nothing to do.
    }

    /*
     * (non-Javadoc)
     * @see
     * com.openexchange.groupware.contact.ContactInterface#getContactsByExtendedSearch(com.openexchange.groupware.search.ContactSearchObject
     * , int, java.lang.String, int[])
     */
    @Override
    public SearchIterator<Contact> getContactsByExtendedSearch(final ContactSearchObject searchobject, final int orderBy, final Order order, final String collation, final int[] cols) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public SearchIterator<Contact> getContactsInFolder(final int folderId, final int from, final int to, final int orderBy, final Order order, final String collation, final int[] cols) throws OXException {
        return new SearchIteratorAdapter(getFolderList(folderId).iterator());
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.groupware.contact.ContactInterface#getDeletedContactsInFolder(int, int[], java.util.Date)
     */
    @Override
    public SearchIterator<Contact> getDeletedContactsInFolder(final int folderId, final int[] cols, final Date since) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.groupware.contact.ContactInterface#getFolderId()
     */
    @Override
    public int getFolderId() {
        // TODO Auto-generated method stub
        return 0;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.groupware.contact.ContactInterface#getLdapServer()
     */
    @Override
    public LdapServer getLdapServer() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.groupware.contact.ContactInterface#getModifiedContactsInFolder(int, int[], java.util.Date)
     */
    @Override
    public SearchIterator<Contact> getModifiedContactsInFolder(final int folderId, final int[] cols, final Date since) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.groupware.contact.ContactInterface#getNumberOfContacts(int)
     */
    @Override
    public int getNumberOfContacts(final int folderId) throws OXException {
        return getFolderList(folderId).size();
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.groupware.contact.ContactInterface#getObjectById(int, int)
     */
    @Override
    public Contact getObjectById(final int objectId, final int inFolder) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.groupware.contact.ContactInterface#getObjectsById(int[][], int[])
     */
    @Override
    public SearchIterator<Contact> getObjectsById(final int[][] objectIdAndInFolder, final int[] cols) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Contact getUserById(final int userId, final boolean performReadCheck) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Contact[] getUsersById(final int[] userIds, final boolean performReadCheck) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.groupware.contact.ContactInterface#getUserById(int)
     */
    @Override
    public Contact getUserById(final int userId) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.groupware.contact.ContactInterface#searchContacts(java.lang.String, int, int, java.lang.String, int[])
     */
    @Override
    public SearchIterator<Contact> searchContacts(final String searchpattern, final int folderId, final int orderBy, final Order order, final int[] cols) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.groupware.contact.ContactInterface#setSession(com.openexchange.session.Session)
     */
    public void setSession(final Session s) throws OXException {
        // TODO Auto-generated method stub

    }

    public void simulateContact(final int cid, final int folderId, final int id1, final String surname) {
        final Contact contact = new Contact();
        contact.setContextId(cid);
        contact.setParentFolderID(folderId);
        contact.setObjectID(id1);
        contact.setSurName(surname);

        getFolderList(folderId).add(contact);
    }

    public void simulateDistributionList(final int cid, final int folderId, final int id1, final String name) {
        final Contact contact = new Contact();
        contact.setContextId(cid);
        contact.setParentFolderID(folderId);
        contact.setObjectID(id1);
        contact.setDisplayName(name);
        contact.setMarkAsDistributionlist(true);

        getFolderList(folderId).add(contact);
    }

    private List<Contact> getFolderList(final int folderId) {
        if (folders.containsKey(folderId)) {
            return folders.get(folderId);
        }
        folders.put(folderId, new LinkedList<Contact>());
        return folders.get(folderId);
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.contact.ContactInterface#associateTwoContacts(com.openexchange.groupware.container.Contact, com.openexchange.groupware.container.Contact)
     */
    public void associateTwoContacts(final Contact master, final Contact slave) throws OXException {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.contact.ContactInterface#getAssociatedContacts(com.openexchange.groupware.container.Contact)
     */
    public List<Contact> getAssociatedContacts(final Contact contact) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.contact.ContactInterface#getAssociationBetween(com.openexchange.groupware.container.Contact, com.openexchange.groupware.container.Contact)
     */
    public ContactUnificationState getAssociationBetween(final Contact c1, final Contact c2) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.contact.ContactInterface#getContactByUUID(java.lang.String)
     */
    public Contact getContactByUUID(final String uuid) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.contact.ContactInterface#separateTwoContacts(com.openexchange.groupware.container.Contact, com.openexchange.groupware.container.Contact)
     */
    public void separateTwoContacts(final Contact master, final Contact slave) throws OXException {
        // TODO Auto-generated method stub

    }

	public SearchIterator<Contact> searchContacts(final SearchTerm term, final int orderBy,
			final String orderDir, final int[] cols) throws OXException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
    public <T> SearchIterator<Contact> getContactsByExtendedSearch(
			final SearchTerm<T> searchterm, final int orderBy, final Order order, final String collation, final int[] cols)
			throws OXException {
		// TODO Auto-generated method stub
		return null;
	}

}
