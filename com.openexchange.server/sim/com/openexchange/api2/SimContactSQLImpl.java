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

package com.openexchange.api2;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import com.openexchange.contact.LdapServer;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.ContactInterface;
import com.openexchange.groupware.contact.ContactUnificationState;
import com.openexchange.groupware.contact.OverridingContactInterface;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.search.ContactSearchObject;
import com.openexchange.groupware.search.Order;
import com.openexchange.search.SearchTerm;
import com.openexchange.tools.iterator.SearchIterator;


/**
 * {@link SimContactSQLImpl}
 *
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 */
public class SimContactSQLImpl implements ContactSQLInterface, ContactInterface, OverridingContactInterface, FinalContactInterface {

    private final HashMap<String, ContactUnificationState> contactUnificationStates = new HashMap<String, ContactUnificationState>();
    private final HashMap<UUID, List<UUID>> associatedContacts = new HashMap<UUID, List <UUID>>();
    private final HashMap<UUID, Contact> contactsByUUID = new HashMap<UUID, Contact>();

    /* (non-Javadoc)
     * @see com.openexchange.api2.ContactSQLInterface#deleteContactObject(int, int, java.util.Date)
     */
    @Override
    public void deleteContactObject(final int objectId, final int inFolder, final Date clientLastModified) throws OXException {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see com.openexchange.api2.ContactSQLInterface#insertContactObject(com.openexchange.groupware.container.Contact)
     */
    @Override
    public void insertContactObject(final Contact contactObj) throws OXException {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see com.openexchange.api2.ContactSQLInterface#updateContactObject(com.openexchange.groupware.container.Contact, int, java.util.Date)
     */
    @Override
    public void updateContactObject(final Contact contactObj, final int inFolder, final Date clientLastModified) throws OXException {
        contactsByUUID.put(UUID.fromString(contactObj.getUserField20()), contactObj);
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.contact.ContactInterface#getContactsByExtendedSearch(com.openexchange.groupware.search.ContactSearchObject, int, com.openexchange.groupware.search.Order, int[])
     *
     */
    @Override
    public SearchIterator<Contact> getContactsByExtendedSearch(final ContactSearchObject searchobject, final int orderBy, final Order order, final String collation, final int[] cols) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.contact.ContactInterface#getContactsInFolder(int, int, int, int, com.openexchange.groupware.search.Order, int[])
     */
    @Override
    public SearchIterator<Contact> getContactsInFolder(final int folderId, final int from, final int to, final int orderBy, final Order order, final String collation, final int[] cols) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.contact.ContactInterface#getDeletedContactsInFolder(int, int[], java.util.Date)
     */
    @Override
    public SearchIterator<Contact> getDeletedContactsInFolder(final int folderId, final int[] cols, final Date since) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.contact.ContactInterface#getFolderId()
     */
    @Override
    public int getFolderId() {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.contact.ContactInterface#getLdapServer()
     */
    @Override
    public LdapServer getLdapServer() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.contact.ContactInterface#getModifiedContactsInFolder(int, int[], java.util.Date)
     */
    @Override
    public SearchIterator<Contact> getModifiedContactsInFolder(final int folderId, final int[] cols, final Date since) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.contact.ContactInterface#getNumberOfContacts(int)
     */
    @Override
    public int getNumberOfContacts(final int folderId) throws OXException {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.contact.ContactInterface#getObjectById(int, int)
     */
    @Override
    public Contact getObjectById(final int objectId, final int inFolder) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.contact.ContactInterface#getObjectsById(int[][], int[])
     */
    @Override
    public SearchIterator<Contact> getObjectsById(final int[][] objectIdAndInFolder, final int[] cols) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.contact.ContactInterface#getUserById(int)
     */
    @Override
    public Contact getUserById(final int userId) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.contact.ContactInterface#getUserById(int, boolean)
     */
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

    /* (non-Javadoc)
     * @see com.openexchange.groupware.contact.ContactInterface#searchContacts(java.lang.String, int, int, com.openexchange.groupware.search.Order, int[])
     */
    @Override
    public SearchIterator<Contact> searchContacts(final String searchpattern, final int folderId, final int orderBy, final Order order, final int[] cols) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.contact.ContactInterface#updateUserContact(com.openexchange.groupware.container.Contact, java.util.Date)
     */
    @Override
    public void updateUserContact(final Contact contact, final Date lastmodified) throws OXException {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.contact.OverridingContactInterface#forceInsertContactObject(com.openexchange.groupware.container.Contact)
     */
    @Override
    public void forceInsertContactObject(final Contact co) throws OXException {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see com.openexchange.api2.FinalContactInterface#associateTwoContacts(com.openexchange.groupware.container.Contact, com.openexchange.groupware.container.Contact)
     */
    @Override
    public void associateTwoContacts(final Contact aggregator, final Contact contributor) throws OXException {
        contactUnificationStates.put(Integer.toString(aggregator.getObjectID()) + "X" + Integer.toString(contributor.getObjectID()), ContactUnificationState.GREEN);
        final UUID uuid = UUID.fromString(aggregator.getUserField20());
        if (associatedContacts.containsKey(uuid)){
            final List<UUID> uuids = associatedContacts.get(uuid);
            uuids.add(UUID.fromString(contributor.getUserField20()));
        } else {
            final ArrayList<UUID> list = new ArrayList<UUID>();
            list.add(UUID.fromString(contributor.getUserField20()));
            associatedContacts.put(uuid, list);
        }
    }

    /* (non-Javadoc)
     * @see com.openexchange.api2.FinalContactInterface#getAssociatedContacts(com.openexchange.groupware.container.Contact)
     */
    @Override
    public List<UUID> getAssociatedContacts(final Contact contact) throws OXException {
        final UUID uuid = UUID.fromString(contact.getUserField20());
        if (associatedContacts.containsKey(uuid)){
            return associatedContacts.get(uuid);
        } else {
            return new ArrayList<UUID>();
        }
    }

    /* (non-Javadoc)
     * @see com.openexchange.api2.FinalContactInterface#getAssociationBetween(com.openexchange.groupware.container.Contact, com.openexchange.groupware.container.Contact)
     */
    @Override
    public ContactUnificationState getAssociationBetween(final Contact c1, final Contact c2) throws OXException {
        final String key = Integer.toString(c1.getObjectID()) + "X" + Integer.toString(c2.getObjectID());
        if (contactUnificationStates.containsKey(key)) {
            return contactUnificationStates.get(key);
        } else {
            return ContactUnificationState.UNDEFINED;
        }
    }

    /* (non-Javadoc)
     * @see com.openexchange.api2.FinalContactInterface#getContactByUUID(java.util.UUID)
     */
    @Override
    public Contact getContactByUUID(final UUID uuid) throws OXException {
        return contactsByUUID.get(uuid);
    }

    /* (non-Javadoc)
     * @see com.openexchange.api2.FinalContactInterface#separateTwoContacts(com.openexchange.groupware.container.Contact, com.openexchange.groupware.container.Contact)
     */
    @Override
    public void separateTwoContacts(final Contact aggregator, final Contact contributor) throws OXException {
        contactUnificationStates.put(Integer.toString(aggregator.getObjectID()) + "X" + Integer.toString(contributor.getObjectID()), ContactUnificationState.RED);
        final UUID uuid = UUID.fromString(aggregator.getUserField20());
        if (associatedContacts.containsKey(uuid)){
            final List<UUID> uuids = associatedContacts.get(uuid);
            uuids.remove(UUID.fromString(contributor.getUserField20()));
        }
    }

    /* (non-Javadoc)
     * @see com.openexchange.api2.FinalContactInterface#setUnificationStateForContacts(com.openexchange.groupware.container.Contact, com.openexchange.groupware.container.Contact, com.openexchange.groupware.contact.ContactUnificationState)
     */
    @Override
    public void setUnificationStateForContacts(final Contact aggregator, final Contact contributor, final ContactUnificationState state) {

    }

    public void addContact(final Contact contact){
        contactsByUUID.put(UUID.fromString(contact.getUserField20()), contact);
    }

	public SearchIterator<Contact> searchContacts(final SearchTerm term, final int orderBy,
			final String orderDir, final int[] cols) throws OXException {
		// TODO Auto-generated method stub
		return null;
	}

	public <T> SearchIterator<Contact> getContactsByExtendedSearch(
			final SearchTerm<T> searchterm, final int orderBy, final String orderDir, final int[] cols)
			throws OXException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
    public <T> SearchIterator<Contact> getContactsByExtendedSearch(
			final SearchTerm<T> searchterm, final int orderBy, final Order order,
			final String collation, final int[] cols) throws OXException {
		// TODO Auto-generated method stub
		return null;
	}

}
