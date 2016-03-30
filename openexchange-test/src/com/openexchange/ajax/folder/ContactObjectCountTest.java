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

package com.openexchange.ajax.folder;

import java.util.Random;
import org.junit.Test;
import com.openexchange.folderstorage.Folder;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.test.ContactTestManager;
import com.openexchange.test.FolderTestManager;

/**
 * Tests if the object count on the folder works successfully for contact folders.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public final class ContactObjectCountTest extends AbstractObjectCountTest {

    private static final Random rand = new Random(System.currentTimeMillis());

    public ContactObjectCountTest(String name) {
        super(name);
    }

    @Test
    public void testCountInPrivateFolder() throws Exception {
        FolderTestManager ftm = new FolderTestManager(client1);
        ContactTestManager ctm = new ContactTestManager(client1);
        try {
            FolderObject created = createPrivateFolder(client1, ftm, FolderObject.CONTACT);
            Folder folder = getFolder(client1, created.getObjectID(), DEFAULT_COLUMNS);
            assertEquals("Wrong object count", 0, folder.getTotal());

            int numContacts = rand.nextInt(20) + 1;
            createContacts(ctm, numContacts, created.getObjectID(), false);
            Folder reloaded = getFolder(client1, created.getObjectID(), DEFAULT_COLUMNS);
            assertEquals("Wrong object count", numContacts, reloaded.getTotal());
        } finally {
            ctm.cleanUp();
            ftm.cleanUp();
        }
    }

    @Test
    public void testCountInSharedFolder() throws Exception {
        FolderTestManager ftm = new FolderTestManager(client1);
        ContactTestManager ctm1 = new ContactTestManager(client1);
        ContactTestManager ctm2 = new ContactTestManager(client2);
        try {
            FolderObject created = createSharedFolder(client1, FolderObject.CONTACT, client2.getValues().getUserId(), ftm);
            Folder folder = getFolder(client1, created.getObjectID(), DEFAULT_COLUMNS);
            assertEquals("Wrong object count", 0, folder.getTotal());

            int numContacts1 = rand.nextInt(20) + 1;
            int numPrivateContacts1 = rand.nextInt(20) + 1;
            int numContacts2 = rand.nextInt(20) + 1;
            createContacts(ctm1, numContacts1, created.getObjectID(), false);
            createContacts(ctm1, numPrivateContacts1, created.getObjectID(), true);
            createContacts(ctm2, numContacts2, created.getObjectID(), false);
            Folder reloaded = getFolder(client1, created.getObjectID(), DEFAULT_COLUMNS);
            assertEquals("Wrong object count", numContacts1 + numPrivateContacts1 + numContacts2, reloaded.getTotal());

            reloaded = getFolder(client2, created.getObjectID(), DEFAULT_COLUMNS);
            assertEquals("Wrong object count", numContacts1 + numContacts2, reloaded.getTotal());
        } finally {
            ctm1.cleanUp();
            ctm2.cleanUp();
            ftm.cleanUp();
        }
    }

    @Test
    public void testCountInPublicFolder() throws Exception {
        FolderTestManager ftm = new FolderTestManager(client1);
        ContactTestManager ctm1 = new ContactTestManager(client1);
        ContactTestManager ctm2 = new ContactTestManager(client2);
        try {
            FolderObject created = createPublicFolder(client1, FolderObject.CONTACT, client2.getValues().getUserId(), ftm);
            Folder folder = getFolder(client1, created.getObjectID(), DEFAULT_COLUMNS);
            assertEquals("Wrong object count", 0, folder.getTotal());

            int numContacts1 = rand.nextInt(20) + 1;
            int numContacts2 = rand.nextInt(20) + 1;
            createContacts(ctm1, numContacts1, created.getObjectID(), false);
            createContacts(ctm2, numContacts2, created.getObjectID(), false);
            Folder reloaded = getFolder(client1, created.getObjectID(), DEFAULT_COLUMNS);
            assertEquals("Wrong object count", numContacts1 + numContacts2, reloaded.getTotal());

            reloaded = getFolder(client2, created.getObjectID(), DEFAULT_COLUMNS);
            assertEquals("Wrong object count", numContacts2, reloaded.getTotal());
        } finally {
            ctm1.cleanUp();
            ctm2.cleanUp();
            ftm.cleanUp();
        }
    }

    private static void createContacts(ContactTestManager ctm, int numContacts, int folderId, boolean isPrivate) {
        final Contact[] contacts = new Contact[numContacts];
        for (int i = 0; i < numContacts; i++) {
            Contact contact = ContactTestManager.generateContact(folderId);
            contact.setPrivateFlag(isPrivate);
            contacts[i] = contact;
        }

        ctm.newActionMultiple(contacts);
    }
}
