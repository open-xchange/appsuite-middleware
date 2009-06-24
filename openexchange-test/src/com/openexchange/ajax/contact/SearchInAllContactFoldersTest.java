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

package com.openexchange.ajax.contact;

import com.openexchange.ajax.folder.Create;
import com.openexchange.ajax.contact.action.InsertRequest;
import com.openexchange.ajax.contact.action.InsertResponse;
import com.openexchange.ajax.contact.action.SearchRequest;
import com.openexchange.ajax.contact.action.DeleteRequest;
import com.openexchange.ajax.contact.action.SearchResponse;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;

/**
* This test creates one folder and two users (one user in the new folder and one user in the private contacts folder). Then a search is performed for their common first name.
* The search is asserted to return both contacts.
* @author <a href="mailto:karsten.will@open-xchange.org">Karsten Will</a>
*/
public class SearchInAllContactFoldersTest extends AbstractAJAXSession {
	
	Contact contactObject1;
	Contact contactObject2;
	FolderObject newFolderObject;
	int privateFolderId;
	int newFolderId;
	
	public SearchInAllContactFoldersTest(final String name) {
		super(name);
	}

	@Override
    public void setUp() throws Exception {
		super.setUp();
		final AJAXClient client = getClient();
		//get the id of the private contacts-folder
        privateFolderId = client.getValues().getPrivateContactFolder();
        //create a new folder
        newFolderObject = Create.createPublicFolder(client, "Testfolder2", FolderObject.CONTACT);
        newFolderId = newFolderObject.getObjectID();
        //create a contact in the private folder
        contactObject1 = new Contact();
        contactObject1.setDisplayName("Herbert Meier");
        contactObject1.setEmail1("herbert.meier@example.com");
        contactObject1.setParentFolderID(privateFolderId);
        InsertRequest insertContact1 = new InsertRequest(contactObject1);
        InsertResponse insertResponse = client.execute(insertContact1);
        insertResponse.fillObject(contactObject1);
        //create a contact in the new folder
        contactObject2 = new Contact();
        contactObject2.setDisplayName("Herbert M\u00fcller");
        contactObject2.setEmail1("herbert.mueller@example.com");
        contactObject2.setParentFolderID(newFolderId);
        InsertRequest insertContact2 = new InsertRequest(contactObject2);
        insertResponse = client.execute(insertContact2);
        insertResponse.fillObject(contactObject2);
	}
	
	@Override
    public void tearDown() throws Exception {
		final AJAXClient client = getClient();
		//delete the two contacts
		DeleteRequest contactDeleteRequest = new DeleteRequest(contactObject1);
		client.execute(contactDeleteRequest);
		contactDeleteRequest = new DeleteRequest(contactObject2);
		client.execute(contactDeleteRequest);
		//delete the new folder
		com.openexchange.ajax.folder.actions.DeleteRequest folderDeleteRequest  = new com.openexchange.ajax.folder.actions.DeleteRequest(newFolderObject);
		client.execute(folderDeleteRequest);
		super.tearDown();
	}

	public void testAllContactFoldersSearch() throws Throwable {
    	final AJAXClient client = getClient();
		//execute a search over first name and last name in all folders (folder id -1) that matches both contacts
		int [] columns = new int [] {Contact.OBJECT_ID};
		SearchRequest searchRequest = new SearchRequest("Herbert", -1, columns, true);
		
		SearchResponse searchResponse = client.execute(searchRequest);
		boolean foundFirst = false;
		boolean foundSecond = false;
		final int idPos = searchResponse.getColumnPos(Contact.OBJECT_ID);
		for (Object[] obj : searchResponse) {
		    if (contactObject1.getObjectID() == ((Integer) obj[idPos]).intValue()) {
		        foundFirst = true;
		    }
            if (contactObject2.getObjectID() == ((Integer) obj[idPos]).intValue()) {
                foundSecond = true;
            }
		}
		assertTrue("Search did not return first inserted contact.", foundFirst);
        assertTrue("Search did not return second inserted contact.", foundSecond);
	}
}
