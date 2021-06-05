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

package com.openexchange.ajax.contact.action;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.util.Calendar;
import java.util.Date;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;

/**
 * This contains some examples of tests created for ContactTestManager
 *
 * @author <a href="mailto:karsten.will@open-xchange.org">Karsten Will</a>
 */
public class ExemplaryContactTestManagerTest extends AbstractAJAXSession {

    Contact contactObject1;
    Contact contactObject2;
    private FolderObject folder;

    public ExemplaryContactTestManagerTest() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        //create a folder for testing
        folder = ftm.generatePublicFolder("contacts cotm tests (" + new Date().getTime() + ")", FolderObject.CONTACT, getClient().getValues().getPrivateContactFolder(), getClient().getValues().getUserId());
        ftm.insertFolderOnServer(folder);

        //create a contact in the private folder
        contactObject1 = new Contact();
        contactObject1.setDisplayName("Herbert Meier");
        contactObject1.setEmail1("herbert.meier@example.com");
        contactObject1.setNote("created by ExemplaryContactTestManagerTest");
        contactObject1.setParentFolderID(folder.getObjectID());
        cotm.newAction(contactObject1);

        //create a second contact in the private folder
        contactObject2 = new Contact();
        contactObject2.setDisplayName("Herbert M\u00fcller");
        contactObject2.setEmail1("herbert.mueller@example.com");
        contactObject2.setParentFolderID(folder.getObjectID());
        contactObject2.setNote("created by ExemplaryContactTestManagerTest");
        cotm.newAction(contactObject2);
    }

    @Test
    public void testCreatedContactsAreReturnedByGetRequest() {
        Contact co = cotm.getAction(contactObject1.getParentFolderID(), contactObject1.getObjectID());
        assertEquals("The contact was not returned.", co.getDisplayName(), contactObject1.getDisplayName());
    }

    @Test
    public void testCreatedContactsAppearInAllRequestForSameFolder() {
        boolean found1 = false;
        boolean found2 = false;
        Contact[] allContacts = cotm.allAction(folder.getObjectID());
        for (int i = 0; i < allContacts.length; i++) {
            Contact co = allContacts[i];
            if (co.getObjectID() == contactObject1.getObjectID()) {
                found1 = true;
            }
            if (co.getObjectID() == contactObject2.getObjectID()) {
                found2 = true;
            }
        }
        assertTrue("First contact was not found.", found1);
        assertTrue("Second contact was not found.", found2);
    }

    @Test
    public void testCreatedContactsAppearInListRequest() {
        boolean found1 = false;
        boolean found2 = false;
        int[] firstContact = new int[] { contactObject1.getParentFolderID(), contactObject1.getObjectID() };
        int[] secondContact = new int[] { contactObject2.getParentFolderID(), contactObject2.getObjectID() };
        Contact[] allContacts = cotm.listAction(firstContact, secondContact);
        for (int i = 0; i < allContacts.length; i++) {
            Contact co = allContacts[i];
            if (co.getObjectID() == contactObject1.getObjectID()) {
                found1 = true;
            }
            if (co.getObjectID() == contactObject2.getObjectID()) {
                found2 = true;
            }
        }
        assertTrue("First contact was not found.", found1);
        assertTrue("Second contact was not found.", found2);
    }

    @Test
    public void testCreatedContactsAppearInSearchRequestOverAllFolders() {
        boolean found1 = false;
        boolean found2 = false;
        // folderId "-1" means searching in all folders
        Contact[] contacts_1 = cotm.searchAction(contactObject1.getDisplayName(), -1);
        Contact[] contacts_2 = cotm.searchAction(contactObject2.getDisplayName(), -1);

        for (int i = 0; i < contacts_1.length; i++) {
            if (contacts_1[i].getObjectID() == contactObject1.getObjectID()) {
                found1 = true;
            }
        }

        for (int i = 0; i < contacts_2.length; i++) {
            if (contacts_2[i].getObjectID() == contactObject2.getObjectID()) {
                found2 = true;
            }
        }

        assertTrue("First contact was not found.", found1);
        assertTrue("Second contact was not found.", found2);
    }

    @Test
    public void testCreatedContactsAppearAsUpdatedSinceYesterday() {
        boolean found1 = false;
        boolean found2 = false;
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -1);
        Contact[] allContacts = cotm.updatesAction(folder.getObjectID(), cal.getTime());
        for (int i = 0; i < allContacts.length; i++) {
            Contact co = allContacts[i];
            if (co.getObjectID() == contactObject1.getObjectID()) {
                found1 = true;
            }
            if (co.getObjectID() == contactObject2.getObjectID()) {
                found2 = true;
            }
        }
        assertTrue("First contact was not found.", found1);
        assertTrue("Second contact was not found.", found2);
    }
}
