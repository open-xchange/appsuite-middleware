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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.junit.Assert;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.contact.action.AllRequest;
import com.openexchange.ajax.framework.CommonAllResponse;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.tools.arrays.Arrays;

public class AllTest extends AbstractManagedContactTest {

	public AllTest(final String name) {
		super(name);
	}

	@Override
	public void setUp() throws Exception {
		super.setUp();
	}

    public void testAll() throws Exception {
		int columnIDs[] = new int[] { Contact.OBJECT_ID, Contact.FOLDER_ID };
    	Contact[] contacts = manager.allAction(FolderObject.SYSTEM_LDAP_FOLDER_ID, columnIDs);
    	assertNotNull("got no contacts", contacts);
    	assertTrue("got no contacts", 0 < contacts.length);
	}

    // Node 2652
    public void testLastModifiedUTC() throws Exception {
    	manager.newAction(
    			generateContact("testLastModifiedUTC1"), 
    			generateContact("testLastModifiedUTC2"), 
    			generateContact("testLastModifiedUTC3"));
        int columnIDs[] = new int[] { Contact.OBJECT_ID, Contact.FOLDER_ID, Contact.LAST_MODIFIED_UTC };
    	Contact[] contacts = manager.allAction(folderID, columnIDs);
    	assertNotNull("got no contacts", contacts);
    	assertTrue("got no contacts", 0 < contacts.length);
        JSONArray arr = (JSONArray) manager.getLastResponse().getData();
        assertNotNull("no json array in response data", arr);
        int size = arr.length();
        assertTrue("no data in json array", 0 < arr.length());
        for (int i = 0; i < size; i++ ){
            JSONArray objectData = arr.optJSONArray(i);
            assertNotNull(objectData);
            assertNotNull("no last modified utc found in contact data", objectData.opt(2));
        }
    }
    
    public void testExcludeAdmin() throws Exception {
        int columnIDs[] = new int[] { Contact.OBJECT_ID, Contact.FOLDER_ID, Contact.INTERNAL_USERID };
        /*
         * perform different all requests
         */
        Contact[] allContactsDefault = manager.allAction(FolderObject.SYSTEM_LDAP_FOLDER_ID, columnIDs);
        assertNotNull("got no contacts", allContactsDefault);
        assertTrue("got no contacts", 0 < allContactsDefault.length);
        Contact[] allContactsWithAdmin = allAction(FolderObject.SYSTEM_LDAP_FOLDER_ID, columnIDs, true);
        assertNotNull("got no contacts", allContactsWithAdmin);
        assertTrue("got no contacts", 0 < allContactsWithAdmin.length);
        Contact[] allContactsWithoutAdmin = allAction(FolderObject.SYSTEM_LDAP_FOLDER_ID, columnIDs, false);
        assertNotNull("got no contacts", allContactsWithoutAdmin);
        assertTrue("got no contacts", 0 < allContactsWithoutAdmin.length);
        /*
         * check results
         */
        Assert.assertArrayEquals("'admin=true' differs from default result", allContactsDefault, allContactsWithAdmin);
        assertEquals("unexpected number of contacts in result", allContactsWithAdmin.length, allContactsWithoutAdmin.length + 1);
    }
        
    public void testAllVisibleFolders() throws Exception {
        /*
         * prepare special all request without folder ID
         */
        int columnIDs[] = new int[] { Contact.OBJECT_ID, Contact.FOLDER_ID };
        AllRequest allRequest = new AllRequest(-1, columnIDs) {
            @Override
            public Parameter[] getParameters() {
                List<Parameter> paramsWithoutFolder = new ArrayList<Parameter>();
                Parameter[] params = super.getParameters();
                for (Parameter param : params) {
                    if (false == AJAXServlet.PARAMETER_FOLDERID.equals(param.getName())) {
                        paramsWithoutFolder.add(param);
                    }
                }
                return paramsWithoutFolder.toArray(new Parameter[paramsWithoutFolder.size()]); 
            }
        };
        /*
         * check results
         */
        CommonAllResponse response = manager.getClient().execute(allRequest);
        JSONArray data = (JSONArray) response.getResponse().getData();
        List<Contact> contacts = manager.transform(data, columnIDs);
        assertNotNull("got no contacts", contacts);
        assertTrue("got no contacts", 0 < contacts.size());
        Set<String> folderIDs = new HashSet<String>();
        for (Contact contact : contacts) {
            folderIDs.add(String.valueOf(contact.getParentFolderID()));
        }        
        assertTrue("got no results from different folders", 1 < folderIDs.size());
    }
        
    private Contact[] allAction(int folderId, int[] columns, Boolean admin) throws OXException, IOException, JSONException {
        List<Contact> allContacts = new LinkedList<Contact>();
        AllRequest request = new AllRequestWithAdmin(folderId, columns, admin);
        CommonAllResponse response = getClient().execute(request, manager.getSleep());
        JSONArray data = (JSONArray) response.getResponse().getData();
        allContacts = manager.transform(data, columns);
        return allContacts.toArray(new Contact[]{});
    }
    
    private static final class AllRequestWithAdmin extends AllRequest {
    	
    	private final Boolean admin;

		public AllRequestWithAdmin(int folderId, int[] columns, Boolean admin) {
			super(folderId, columns);
			this.admin = admin;
		}

		@Override
	    public Parameter[] getParameters() {
	        Parameter[] params = super.getParameters();
	        return null != admin ? Arrays.add(params, new Parameter("admin", admin)) : params;
	    }

    }

}
