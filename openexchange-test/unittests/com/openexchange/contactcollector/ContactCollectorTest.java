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

package com.openexchange.contactcollector;

import static com.openexchange.java.Autoboxing.I;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import javax.mail.internet.InternetAddress;
import junit.framework.TestCase;
import com.openexchange.contact.ContactService;
import com.openexchange.contactcollector.folder.ContactCollectorFolderCreator;
import com.openexchange.contactcollector.internal.ContactCollectorServiceImpl;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.Init;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.i18n.FolderStrings;
import com.openexchange.groupware.search.ContactSearchObject;
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.java.util.UUIDs;
import com.openexchange.preferences.ServerUserSetting;
import com.openexchange.server.SimpleServiceLookup;
import com.openexchange.server.impl.DBPool;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.session.Session;
import com.openexchange.setuptools.TestConfig;
import com.openexchange.setuptools.TestContextToolkit;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.timer.TimerService;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.oxfolder.OXFolderAccess;
import com.openexchange.tools.oxfolder.OXFolderManager;
import com.openexchange.user.UserService;
import com.openexchange.userconf.UserConfigurationService;

/**
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 */
public class ContactCollectorTest extends TestCase {

    private SimpleServiceLookup services;

    private String user;

    private int userId;

    private Context ctx;

    private Session session;

    private TestContextToolkit tools;

    private FolderObject contactFolder;

    private final String mail = "test-contact-collector@example.invalid";

    @Override
    public void setUp() throws Exception {
        Init.startServer();

        services = new SimpleServiceLookup();
        {
            services.add(TimerService.class, Init.LOOKUP.getService(TimerService.class));
            services.add(ThreadPoolService.class, Init.LOOKUP.getService(ThreadPoolService.class));
            services.add(ContextService.class, Init.LOOKUP.getService(ContextService.class));
            services.add(UserConfigurationService.class, Init.LOOKUP.getService(UserConfigurationService.class));
            services.add(UserService.class, Init.LOOKUP.getService(UserService.class));
            services.add(ContactService.class, Init.LOOKUP.getService(ContactService.class));
        }

        final TestConfig config = new TestConfig();
        user = prepareUser(config.getUser());

        tools = new TestContextToolkit();
        ctx = tools.getContextByName(config.getContextName());
        userId = tools.resolveUser(user, ctx);
        session = tools.getSessionForUser(user, ctx);
        contactFolder = getStandardContactFolder();

        deleteContactFromFolder(mail);
    }

    private static String prepareUser(final String user) {
        final int pos = user.indexOf('@');
        if (-1 == pos) {
            return user;
        }
        return user.substring(0, pos);
    }

    @Override
    public void tearDown() throws Exception {
        ServerUserSetting.getInstance().setContactCollectOnMailAccess(ctx.getContextId(), userId, false);
        ServerUserSetting.getInstance().setContactCollectOnMailTransport(ctx.getContextId(), userId, false);
        Init.stopServer();
        deleteContactFromFolder(mail);
    }

    public void testNoFolder() throws Throwable {
        ServerUserSetting.getInstance().setContactCollectOnMailAccess(ctx.getContextId(), userId, true);
        setFolderNULL();
        Connection con = DBPool.pickupWriteable(ctx);
        ContactCollectorFolderCreator.create(session, ctx, StringHelper.valueOf(Locale.ENGLISH).getString(FolderStrings.DEFAULT_CONTACT_COLLECT_FOLDER_NAME), con);
        DBPool.closeWriterSilent(ctx, con);
        Integer folder = ServerUserSetting.getInstance().getContactCollectionFolder(ctx.getContextId(), userId);
        assertNotNull("Folder should not be NULL", folder);
        assertTrue("Invalid folder id", folder > 0);
    }

    public void testNewFeature() throws Throwable {
        removeUserEntry();
        Connection con = DBPool.pickupWriteable(ctx);
        ContactCollectorFolderCreator.create(session, ctx, StringHelper.valueOf(Locale.ENGLISH).getString(FolderStrings.DEFAULT_CONTACT_COLLECT_FOLDER_NAME), con);
        ServerUserSetting setting = ServerUserSetting.getInstance();
        assertNotNull("No folder for contact collection", setting.getContactCollectionFolder(ctx.getContextId(), userId));
        assertTrue("No folder for contact collection", setting.getContactCollectionFolder(ctx.getContextId(), userId) > 0);
        assertFalse("Should not collect on incoming mail", setting.isContactCollectOnMailAccess(ctx.getContextId(), userId));
        assertFalse("Should not collect on outgoing mail", setting.isContactCollectOnMailTransport(ctx.getContextId(), userId));
    }

    public void testNewContact() throws Throwable {
        ServerUserSetting.getInstance().setContactCollectionFolder(ctx.getContextId(), userId, I(contactFolder.getObjectID()));
        ServerUserSetting.getInstance().setContactCollectOnMailAccess(ctx.getContextId(), userId, true);

        final ContactCollectorServiceImpl collector = new ContactCollectorServiceImpl(services);
        collector.start();
        try {
            final InternetAddress address = new InternetAddress(mail);
            final List<InternetAddress> addresses = new ArrayList<InternetAddress>();
            addresses.add(address);
            collector.memorizeAddresses(addresses, false,  session, false);
            final List<Contact> contacts = searchContact(mail);
            assertEquals("No object found", 1, contacts.size());
        } finally {
            collector.stop();
        }
    }

    public void testExistingContact() throws Throwable {
        ServerUserSetting.getInstance().setContactCollectionFolder(ctx.getContextId(), userId, I(contactFolder.getObjectID()));
        ServerUserSetting.getInstance().setContactCollectOnMailAccess(ctx.getContextId(), userId, true);

        final ContactCollectorServiceImpl collector = new ContactCollectorServiceImpl(services);
        collector.start();
        try {
            final InternetAddress address = new InternetAddress(mail);
            final List<InternetAddress> addresses = new ArrayList<InternetAddress>();
            addresses.add(address);
            collector.memorizeAddresses(addresses, false, session, false);
            collector.memorizeAddresses(addresses, false, session, false);
            collector.memorizeAddresses(addresses, false, session, false);
            final List<Contact> contacts = searchContact(mail);
            assertEquals("Ammount of objects found is not correct", 1, contacts.size());
        } finally {
            collector.stop();
        }
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

    private List<Contact> searchContact(final String pattern) throws Exception {

        ContactService contactService = services.getService(ContactService.class);
        final ContactSearchObject searchObject = new ContactSearchObject();
        searchObject.setEmail1(pattern);
        searchObject.setEmail2(pattern);
        searchObject.setEmail3(pattern);
        searchObject.setOrSearch(true);
        searchObject.addFolder(contactFolder.getObjectID());
        ContactField[] fields = new ContactField[] { ContactField.FOLDER_ID, ContactField.LAST_MODIFIED, ContactField.OBJECT_ID, ContactField.USERFIELD20 };
        SearchIterator<Contact> iterator = contactService.searchContacts(session, searchObject, fields);

        final List<Contact> contacts = new ArrayList<Contact>();
        while (iterator.hasNext()) {
            Contact foundContact;
            foundContact = iterator.next();
            contacts.add(foundContact);
        }

        return contacts;
    }

    private void deleteContactFromFolder(final String pattern) throws Exception {
        final List<Contact> contacts = searchContact(pattern);
        ContactService contactService = services.getService(ContactService.class);
        for (final Contact contact : contacts) {
            contactService.deleteContact(session, String.valueOf(contactFolder.getObjectID()), String.valueOf(contact.getObjectID()),
                contact.getLastModified());
        }
    }

    private void removeUserEntry() throws Throwable {
        Connection con = DBPool.pickupWriteable(ctx);
        PreparedStatement stmt = con.prepareStatement("DELETE FROM user_setting_server WHERE cid = ? AND user = ?");
        stmt.setInt(1, ctx.getContextId());
        stmt.setInt(2, userId);
        stmt.execute();
        stmt.close();
        DBPool.closeWriterSilent(ctx, con);
    }

    private void setFolderNULL() throws Throwable {
        Connection con = DBPool.pickupWriteable(ctx);
        PreparedStatement stmt = con.prepareStatement("UPDATE user_setting_server SET contact_collect_folder = ? WHERE cid = ? AND user = ?");
        stmt.setNull(1, Types.INTEGER);
        stmt.setInt(2, ctx.getContextId());
        stmt.setInt(3, userId);
        int count = stmt.executeUpdate();
        stmt.close();
        if (count == 0) {
            stmt = con.prepareStatement("INSERT INTO user_setting_server (cid, user, contact_collect_folder, uuid) VALUES (?, ?, ?, ?)");
            stmt.setInt(1, ctx.getContextId());
            stmt.setInt(2, userId);
            stmt.setNull(3, Types.INTEGER);
            UUID uuid = UUID.randomUUID();
            byte[] uuidBinary = UUIDs.toByteArray(uuid);
            stmt.setBytes(4, uuidBinary);
            stmt.execute();
            stmt.close();
        }
        DBPool.closeWriterSilent(ctx, con);
    }

    private FolderObject createSubFolder() throws Throwable {
        FolderObject fo = new FolderObject();
        fo.setFolderName("Contact Collect Folder" + System.currentTimeMillis());
        fo.setParentFolderID(FolderObject.SYSTEM_PRIVATE_FOLDER_ID);
        fo.setModule(FolderObject.CONTACT);
        fo.setType(FolderObject.PRIVATE);
        final OCLPermission ocl = new OCLPermission();
        ocl.setEntity(userId);
        ocl.setAllPermission(OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION, OCLPermission.ADMIN_PERMISSION);
        ocl.setGroupPermission(false);
        ocl.setFolderAdmin(true);
        fo.setPermissionsAsArray(new OCLPermission[] { ocl });
        OXFolderManager oxma = OXFolderManager.getInstance(session);
        int fuid = oxma.createFolder(fo, true, System.currentTimeMillis()).getObjectID();
        fo.setObjectID(fuid);
        return fo;
    }

    private void deleteFolder(FolderObject fo) throws Throwable {
        OXFolderManager oxma = OXFolderManager.getInstance(session);
        oxma.deleteFolder(new FolderObject(fo.getObjectID()), true, System.currentTimeMillis());
    }
}
