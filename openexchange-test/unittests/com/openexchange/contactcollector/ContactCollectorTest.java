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

package com.openexchange.contactcollector;

import java.util.ArrayList;
import java.util.List;
import javax.mail.internet.InternetAddress;
import junit.framework.TestCase;
import com.openexchange.api2.OXException;
import com.openexchange.api2.RdbContactSQLInterface;
import com.openexchange.contactcollector.internal.ContactCollectorServiceImpl;
import com.openexchange.groupware.Init;
import com.openexchange.groupware.calendar.tools.CalendarContextToolkit;
import com.openexchange.groupware.calendar.tools.CalendarTestConfig;
import com.openexchange.groupware.contact.ContactException;
import com.openexchange.groupware.contact.ContactInterface;
import com.openexchange.groupware.contact.ContactServices;
import com.openexchange.groupware.container.ContactObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.search.ContactSearchObject;
import com.openexchange.preferences.ServerUserSetting;
import com.openexchange.session.Session;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorException;
import com.openexchange.tools.oxfolder.OXFolderAccess;

/**
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 */
public class ContactCollectorTest extends TestCase {

    private String user;

    private int userId;

    private Context ctx;

    private Session session;

    private FolderObject contactFolder;

    private final String mail = "test-contact-collector@example.invalid";

    @Override
    public void setUp() throws Exception {
        Init.startServer();
        final CalendarTestConfig config = new CalendarTestConfig();
        user = config.getUser();

        final CalendarContextToolkit tools = new CalendarContextToolkit();
        ctx = tools.getDefaultContext();
        userId = tools.resolveUser(user, ctx);
        session = tools.getSessionForUser(user, ctx);
        contactFolder = getStandardContactFolder();

        ServerUserSetting.setContactCollectionFolder(ctx.getContextId(), userId, contactFolder.getObjectID());
        ServerUserSetting.setContactColletion(ctx.getContextId(), userId, true);

        deleteContactFromFolder(mail);
    }

    @Override
    public void tearDown() throws Exception {
        ServerUserSetting.setContactColletion(ctx.getContextId(), userId, false);
        Init.stopServer();
        deleteContactFromFolder(mail);
    }

    public void testNewContact() throws Throwable {
        final ContactCollectorService collector = new ContactCollectorServiceImpl();
        final InternetAddress address = new InternetAddress(mail);
        final List<InternetAddress> addresses = new ArrayList<InternetAddress>();
        addresses.add(address);

        collector.memorizeAddresses(addresses, session);

        Thread.sleep(1000);
        final List<ContactObject> contacts = searchContact(mail);
        assertEquals("No object found", 1, contacts.size());
        assertEquals("Count does not match", "1", contacts.get(0).getUserField20());
    }

    public void testExistingContact() throws Throwable {
        final ContactCollectorService collector = new ContactCollectorServiceImpl();
        final InternetAddress address = new InternetAddress(mail);
        final List<InternetAddress> addresses = new ArrayList<InternetAddress>();
        addresses.add(address);

        collector.memorizeAddresses(addresses, session);
        Thread.sleep(1000);
        collector.memorizeAddresses(addresses, session);
        Thread.sleep(1000);
        collector.memorizeAddresses(addresses, session);

        Thread.sleep(1000);
        final List<ContactObject> contacts = searchContact(mail);
        assertEquals("Ammount of objects found is not correct", 1, contacts.size());
        assertEquals("Count does not match", "3", contacts.get(0).getUserField20());
    }

    private FolderObject getStandardContactFolder() {
        final OXFolderAccess access = new OXFolderAccess(ctx);
        FolderObject fo = null;
        try {
            fo = access.getDefaultFolder(userId, FolderObject.CONTACT);
        } catch (final OXException e) {
            e.printStackTrace();
            return null;
        }
        return fo;
    }

    private List<ContactObject> searchContact(final String pattern) throws Exception {
        ContactInterface contactInterface = ContactServices.getInstance().getService(contactFolder.getObjectID(), ctx.getContextId());
        if (contactInterface == null) {
            contactInterface = new RdbContactSQLInterface(session, ctx);
        }
        final ContactSearchObject searchObject = new ContactSearchObject();
        searchObject.setEmail1(pattern);
        searchObject.setEmail2(pattern);
        searchObject.setEmail3(pattern);
        searchObject.setOrSearch(true);
        searchObject.addFolder(contactFolder.getObjectID());
        contactInterface.setSession(session);

        final int[] columns = new int[] {
            ContactObject.FOLDER_ID, ContactObject.LAST_MODIFIED, ContactObject.OBJECT_ID, ContactObject.USERFIELD20 };
        final SearchIterator<ContactObject> iterator = contactInterface.getContactsByExtendedSearch(searchObject, 0, null, columns);

        final List<ContactObject> contacts = new ArrayList<ContactObject>();
        while (iterator.hasNext()) {
            ContactObject foundContact;
            try {
                foundContact = iterator.next();
            } catch (final SearchIteratorException e) {
                throw new ContactException(e);
            }
            contacts.add(foundContact);
        }

        return contacts;
    }

    private void deleteContactFromFolder(final String pattern) throws Exception {
        final List<ContactObject> contacts = searchContact(pattern);

        for (final ContactObject contact : contacts) {
            ContactInterface contactInterface = ContactServices.getInstance().getService(contactFolder.getObjectID(), ctx.getContextId());
            if (contactInterface == null) {
                contactInterface = new RdbContactSQLInterface(session, ctx);
            }
            contactInterface.deleteContactObject(contact.getObjectID(), contact.getParentFolderID(), contact.getLastModified());
        }
    }
}
