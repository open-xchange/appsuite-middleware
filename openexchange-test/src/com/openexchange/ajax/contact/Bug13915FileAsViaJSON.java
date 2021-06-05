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

package com.openexchange.ajax.contact;

import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.groupware.container.Contact;
import com.openexchange.test.ContactTestManager;

public class Bug13915FileAsViaJSON extends AbstractManagedContactTest {

    private Contact contact;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        contact = ContactTestManager.generateContact(folderID);
        contact.removeFileAs();
    }

    @Test
    public void testFileAsViaCreate() {
        contact.setFileAs("filed as");
        cotm.newAction(contact);

        Contact actual = cotm.getAction(contact);

        assertEquals("filed as", actual.getFileAs());
    }

    @Test
    public void testFileAsViaUpdate() {
        cotm.newAction(contact);

        Contact update = new Contact();
        update.setParentFolderID(contact.getParentFolderID());
        update.setObjectID(contact.getObjectID());
        update.setLastModified(contact.getLastModified());
        update.setFileAs("filed as");
        cotm.updateAction(update);

        Contact actual = cotm.getAction(contact);

        assertEquals("filed as", actual.getFileAs());
    }

    @Test
    public void testFileAsViaUpdate2() {
        contact.setFileAs("filed as something else");
        cotm.newAction(contact);

        Contact update = new Contact();
        update.setParentFolderID(contact.getParentFolderID());
        update.setObjectID(contact.getObjectID());
        update.setLastModified(contact.getLastModified());
        update.setFileAs("filed as");
        cotm.updateAction(update);

        Contact actual = cotm.getAction(contact);

        assertEquals("filed as", actual.getFileAs());
    }
}
