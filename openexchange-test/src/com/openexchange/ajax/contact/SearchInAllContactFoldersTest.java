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

import static org.junit.Assert.assertTrue;
import java.util.Date;
import java.util.Objects;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.contact.action.InsertRequest;
import com.openexchange.ajax.contact.action.InsertResponse;
import com.openexchange.ajax.contact.action.SearchRequest;
import com.openexchange.ajax.contact.action.SearchResponse;
import com.openexchange.ajax.folder.Create;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.search.ContactSearchObject;

/**
 * This test creates one folder and two users (one user in the new folder and one user in the private contacts folder). Then a search is performed for their common first name.
 * The search is asserted to return both contacts.
 *
 * @author <a href="mailto:karsten.will@open-xchange.org">Karsten Will</a>
 */
public class SearchInAllContactFoldersTest extends AbstractAJAXSession {

    private Contact contact1;
    private Contact contact2;
    private FolderObject newFolder;

    public SearchInAllContactFoldersTest() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        //create a new folder
        newFolder = Create.createPublicFolder(getClient(), "SearchInAllFoldersTest (" + new Date().getTime() + ")", FolderObject.CONTACT);
        //create a contact in the private folder
        contact1 = new Contact();
        contact1.setDisplayName("Herbert Meier");
        contact1.setSurName("Meier");
        contact1.setGivenName("Herbert");
        contact1.setDisplayName("Herbert Meier");
        contact1.setEmail1("herbert.meier@example.com");
        contact1.setParentFolderID(getClient().getValues().getPrivateContactFolder());
        InsertRequest insertContact1 = new InsertRequest(contact1);
        InsertResponse insertResponse = getClient().execute(insertContact1);
        insertResponse.fillObject(contact1);
        //create a contact in the new folder
        contact2 = new Contact();
        contact2.setDisplayName("Herbert M\u00fcller");
        contact2.setSurName("M\u00fcller");
        contact2.setGivenName("Herbert");
        contact2.setEmail1("herbert.mueller@example.com");
        contact2.setParentFolderID(newFolder.getObjectID());
        InsertRequest insertContact2 = new InsertRequest(contact2);
        insertResponse = getClient().execute(insertContact2);
        insertResponse.fillObject(contact2);
    }

    @Test
    public void testAllContactFoldersSearch() throws Throwable {
        //execute a search over first name and last name in all folders (folder id -1) that matches both contacts
        int[] columns = new int[] { Contact.OBJECT_ID };

        //        SearchRequest searchRequest = new SearchRequest("Herbert", -1, columns, true);
        ContactSearchObject cso = new ContactSearchObject();
        cso.setGivenName("Herbert");
        SearchRequest searchRequest = new SearchRequest(cso, columns, true);

        SearchResponse searchResponse = getClient().execute(searchRequest);
        assertFoundContacts(searchResponse);
    }

    @Test
    public void testAutoCompleteSearchForAllFolders() throws Throwable {
        int[] columns = { Contact.OBJECT_ID, Contact.FOLDER_ID, Contact.DISPLAY_NAME, Contact.INTERNAL_USERID, Contact.EMAIL1, Contact.EMAIL2, Contact.EMAIL3, Contact.DISTRIBUTIONLIST, Contact.MARK_AS_DISTRIBUTIONLIST };
        ContactSearchObject cso = new ContactSearchObject();
        cso.setDisplayName("herb*");
        cso.setEmail1("herb*");
        cso.setEmail2("herb*");
        cso.setEmail3("herb*");
        cso.setDisplayName("herb*");
        cso.setDisplayName("herb*");
        cso.setEmailAutoComplete(true);
        cso.setOrSearch(true);
        SearchRequest request = new SearchRequest(cso, columns, Contact.USE_COUNT_GLOBAL_FIRST, null);
        SearchResponse response = getClient().execute(request);

        assertFoundContacts(response);
    }

    private void assertFoundContacts(SearchResponse response) {
        boolean foundFirst = false;
        boolean foundSecond = false;
        final int idPos = response.getColumnPos(Contact.OBJECT_ID);
        for (Object[] obj : response) {
            if (Objects.equals(contact1.getId(true), obj[idPos])) {
                foundFirst = true;
            }
            if (Objects.equals(contact2.getId(true), obj[idPos])) {
                foundSecond = true;
            }
        }
        assertTrue("Search did not return first inserted contact.", foundFirst);
        assertTrue("Search did not return second inserted contact.", foundSecond);
    }
}
