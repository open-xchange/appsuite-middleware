/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 * 
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.ajax.folder;

import static org.junit.Assert.assertEquals;
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
