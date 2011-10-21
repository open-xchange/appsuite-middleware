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

package com.openexchange.groupware.contact;

import com.openexchange.exception.OXException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import junit.framework.TestCase;
import com.openexchange.api2.ContactSQLInterface;
import com.openexchange.contact.LdapServer;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.contexts.SimContext;
import com.openexchange.groupware.ldap.MockUser;
import com.openexchange.groupware.ldap.SimUser;
import com.openexchange.groupware.search.ContactSearchObject;
import com.openexchange.groupware.search.Order;
import com.openexchange.search.SearchTerm;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorAdapter;
import com.openexchange.tools.session.SimServerSession;


/**
 * {@link ContactSearchMultiplexerTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class ContactSearchMultiplexerTest extends TestCase {

    private ContactSearchMultiplexer searchMultiplexer;
    private SimServerSession session;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        final ContactSQLInterface iface1 = contactsWithSearchResult(11,12,13);
        final ContactSQLInterface iface2 = contactsWithSearchResult(21,22,23);
        final ContactSQLInterface iface3 = contactsWithSearchResult(31,32,33);

        final ContactSQLInterface standard = contactsWithSearchResult(1,2,3);

        final SimContactInterfaceDiscoveryService discoveryService = new SimContactInterfaceDiscoveryService();
        discoveryService.register(iface1, 1);
        discoveryService.register(iface2, 2);
        discoveryService.register(iface3, 3);
        discoveryService.setDefaultContactInterface(standard);

        searchMultiplexer = new ContactSearchMultiplexer(discoveryService);
        MockUser user = new SimUser();
        user.setLocale(Locale.ENGLISH);
        session = new SimServerSession(new SimContext(1), user, null);
    }


    public void testSearchInSpecificFolders() throws Exception {

        final ContactSearchObject contactSearchObject = new ContactSearchObject();
        contactSearchObject.addFolder(1);
        contactSearchObject.addFolder(3);
        contactSearchObject.addFolder(1337);


        final SearchIterator<Contact> contacts = searchMultiplexer.extendedSearch(session, contactSearchObject, Contact.OBJECT_ID, Order.ASCENDING, null, new int[]{Contact.OBJECT_ID});

        assertNotNull(contacts);
        // Folder 2 is not searched, so 21,22 and 23 will be missing
        assertIDs(contacts, 1,2,3,11,12,13,31,32,33);

    }

    public void testSearchEverywhere() throws Exception {
        final ContactSearchObject contactSearchObject = new ContactSearchObject();


        final SearchIterator<Contact> contacts = searchMultiplexer.extendedSearch(session, contactSearchObject, Contact.OBJECT_ID, Order.ASCENDING, null, new int[]{Contact.OBJECT_ID});

        assertNotNull(contacts);
        assertIDs(contacts, 1,2,3,11,12,13,21,22,23,31,32,33);
    }

    private void assertIDs(final SearchIterator<Contact> results, final int...ids) throws Exception {
        int index = 0;
        while(results.hasNext()) {
            final Contact c = results.next();
            final int currentId = ids[index++];
            assertEquals("Unexpected element at index: "+(index-1), currentId, c.getObjectID());
        }
        assertEquals(ids.length, index);
    }

    private ContactSQLInterface contactsWithSearchResult(final int...ids) {
        final List<Contact> contacts = new LinkedList<Contact>();
        for(final int id : ids) {
            final Contact contact = new Contact();
            contact.setObjectID(id);
            contacts.add(contact);
        }

        return new ContactSQLInterface() {

            public void deleteContactObject(final int objectId, final int inFolder, final Date clientLastModified) throws OXException, OXException, OXException {
                // TODO Auto-generated method stub

            }

            public void insertContactObject(final Contact contactObj) throws OXException {
                // TODO Auto-generated method stub

            }

            public void updateContactObject(final Contact contactObj, final int inFolder, final Date clientLastModified) throws OXException, OXException {
                // TODO Auto-generated method stub

            }

            public void updateUserContact(Contact contact, Date lastmodified) throws OXException {
                // Nothing to do.
            }

            public SearchIterator<Contact> getContactsByExtendedSearch(final ContactSearchObject searchobject, final int orderBy, final Order order, final String collation, final int[] cols) throws OXException {
                return new SearchIteratorAdapter<Contact>(contacts.iterator());
            }

            public SearchIterator<Contact> getContactsInFolder(final int folderId, final int from, final int to, final int orderBy, final Order order, String collation, final int[] cols) throws OXException {
                // TODO Auto-generated method stub
                return null;
            }

            public SearchIterator<Contact> getDeletedContactsInFolder(final int folderId, final int[] cols, final Date since) throws OXException {
                // TODO Auto-generated method stub
                return null;
            }

            public int getFolderId() {
                // TODO Auto-generated method stub
                return 0;
            }

            public LdapServer getLdapServer() {
                // TODO Auto-generated method stub
                return null;
            }

            public SearchIterator<Contact> getModifiedContactsInFolder(final int folderId, final int[] cols, final Date since) throws OXException {
                // TODO Auto-generated method stub
                return null;
            }

            public int getNumberOfContacts(final int folderId) throws OXException {
                // TODO Auto-generated method stub
                return 0;
            }

            public Contact getObjectById(final int objectId, final int inFolder) throws OXException {
                // TODO Auto-generated method stub
                return null;
            }

            public SearchIterator<Contact> getObjectsById(final int[][] objectIdAndInFolder, final int[] cols) throws OXException {
                // TODO Auto-generated method stub
                return null;
            }

            public Contact getUserById(final int userId, final boolean performReadCheck) throws OXException {
                // TODO Auto-generated method stub
                return null;
            }

            public Contact[] getUsersById(int[] userIds, boolean performReadCheck) throws OXException {
                // TODO Auto-generated method stub
                return null;
            }

            public Contact getUserById(final int userId) throws OXException {
                // TODO Auto-generated method stub
                return null;
            }

            public SearchIterator<Contact> searchContacts(final String searchpattern, final int folderId, final int orderBy, final Order order, final int[] cols) throws OXException {
                // TODO Auto-generated method stub
                return null;
            }

            public void associateTwoContacts(Contact master, Contact slave) throws OXException {
                // TODO Auto-generated method stub

            }

            public List<Contact> getAssociatedContacts(Contact contact) throws OXException {
                // TODO Auto-generated method stub
                return null;
            }

            public ContactUnificationState getAssociationBetween(Contact c1, Contact c2) throws OXException {
                // TODO Auto-generated method stub
                return null;
            }

            public Contact getContactByUUID(String uuid) throws OXException {
                // TODO Auto-generated method stub
                return null;
            }

            public void separateTwoContacts(Contact master, Contact slave) throws OXException {
                // TODO Auto-generated method stub

            }

			public <T> SearchIterator<Contact> getContactsByExtendedSearch(
					SearchTerm<T> searchterm, int orderBy, Order order,
					String collation, int[] cols) throws OXException {
                return new SearchIteratorAdapter<Contact>(contacts.iterator());
			}

        };
    }

}
