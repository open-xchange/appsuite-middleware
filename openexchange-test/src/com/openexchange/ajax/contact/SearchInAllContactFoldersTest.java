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

package com.openexchange.ajax.contact;

import java.util.Date;

import com.openexchange.ajax.contact.action.DeleteRequest;
import com.openexchange.ajax.contact.action.InsertRequest;
import com.openexchange.ajax.contact.action.InsertResponse;
import com.openexchange.ajax.contact.action.SearchRequest;
import com.openexchange.ajax.contact.action.SearchResponse;
import com.openexchange.ajax.folder.Create;
import com.openexchange.ajax.folder.actions.EnumAPI;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.search.ContactSearchObject;

/**
* This test creates one folder and two users (one user in the new folder and one user in the private contacts folder). Then a search is performed for their common first name.
* The search is asserted to return both contacts.
* @author <a href="mailto:karsten.will@open-xchange.org">Karsten Will</a>
*/
public class SearchInAllContactFoldersTest extends AbstractAJAXSession {

    private AJAXClient client;
    private Contact contact1;
    private Contact contact2;
    private FolderObject newFolder;

    public SearchInAllContactFoldersTest(final String name) {
        super(name);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        client = getClient();
        //create a new folder
        newFolder = Create.createPublicFolder(client, "SearchInAllFoldersTest ("+new Date().getTime()+")", FolderObject.CONTACT);
        //create a contact in the private folder
        contact1 = new Contact();
        contact1.setDisplayName("Herbert Meier");
        contact1.setSurName("Meier");
        contact1.setGivenName("Herbert");
        contact1.setDisplayName("Herbert Meier");
        contact1.setEmail1("herbert.meier@example.com");
        contact1.setParentFolderID(client.getValues().getPrivateContactFolder());
        InsertRequest insertContact1 = new InsertRequest(contact1);
        InsertResponse insertResponse = client.execute(insertContact1);
        insertResponse.fillObject(contact1);
        //create a contact in the new folder
        contact2 = new Contact();
        contact2.setDisplayName("Herbert M\u00fcller");
        contact2.setSurName("M\u00fcller");
        contact2.setGivenName("Herbert");
        contact2.setEmail1("herbert.mueller@example.com");
        contact2.setParentFolderID(newFolder.getObjectID());
        InsertRequest insertContact2 = new InsertRequest(contact2);
        insertResponse = client.execute(insertContact2);
        insertResponse.fillObject(contact2);
    }

    @Override
    public void tearDown() throws Exception {
        //delete the two contacts
        DeleteRequest contactDeleteRequest = new DeleteRequest(contact1);
        client.execute(contactDeleteRequest);
        contactDeleteRequest = new DeleteRequest(contact2);
        client.execute(contactDeleteRequest);
        //delete the new folder
        com.openexchange.ajax.folder.actions.DeleteRequest folderDeleteRequest  = new com.openexchange.ajax.folder.actions.DeleteRequest(EnumAPI.OX_OLD, newFolder);
        client.execute(folderDeleteRequest);
        super.tearDown();
    }

    public void testAllContactFoldersSearch() throws Throwable {
        //execute a search over first name and last name in all folders (folder id -1) that matches both contacts
        int [] columns = new int [] {Contact.OBJECT_ID};
        
//        SearchRequest searchRequest = new SearchRequest("Herbert", -1, columns, true);
        ContactSearchObject cso = new ContactSearchObject();
        cso.setGivenName("Herbert");
        SearchRequest searchRequest = new SearchRequest(cso, columns, true);

        SearchResponse searchResponse = client.execute(searchRequest);
        assertFoundContacts(searchResponse);
    }

    public void testAutoCompleteSearchForAllFolders() throws Throwable {
        int[] columns = {
            Contact.OBJECT_ID, Contact.FOLDER_ID, Contact.DISPLAY_NAME, Contact.INTERNAL_USERID, Contact.EMAIL1, Contact.EMAIL2,
            Contact.EMAIL3, Contact.DISTRIBUTIONLIST, Contact.MARK_AS_DISTRIBUTIONLIST };
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
        SearchResponse response = client.execute(request);

        assertFoundContacts(response);
    }

    private void assertFoundContacts(SearchResponse response) {
        boolean foundFirst = false;
        boolean foundSecond = false;
        final int idPos = response.getColumnPos(Contact.OBJECT_ID);
        for (Object[] obj : response) {
            if (contact1.getObjectID() == ((Integer) obj[idPos]).intValue()) {
                foundFirst = true;
            }
            if (contact2.getObjectID() == ((Integer) obj[idPos]).intValue()) {
                foundSecond = true;
            }
        }
        assertTrue("Search did not return first inserted contact.", foundFirst);
        assertTrue("Search did not return second inserted contact.", foundSecond);
    }
}
