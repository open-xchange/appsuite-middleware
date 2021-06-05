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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.junit.Assert;
import org.junit.Test;
import com.openexchange.ajax.contact.action.AllRequest;
import com.openexchange.ajax.framework.CommonAllResponse;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.tools.arrays.Arrays;

public class AllTest extends AbstractManagedContactTest {

    @Test
    public void testAll() {
        int columnIDs[] = new int[] { Contact.OBJECT_ID, Contact.FOLDER_ID };
        Contact[] contacts = cotm.allAction(FolderObject.SYSTEM_LDAP_FOLDER_ID, columnIDs);
        assertNotNull("got no contacts", contacts);
        assertTrue("got no contacts", 0 < contacts.length);
    }

    // Node 2652    @Test
    @Test
    public void testLastModifiedUTC() {
        cotm.newAction(generateContact("testLastModifiedUTC1"), generateContact("testLastModifiedUTC2"), generateContact("testLastModifiedUTC3"));
        int columnIDs[] = new int[] { Contact.OBJECT_ID, Contact.FOLDER_ID, Contact.LAST_MODIFIED_UTC };
        Contact[] contacts = cotm.allAction(folderID, columnIDs);
        assertNotNull("got no contacts", contacts);
        assertTrue("got no contacts", 0 < contacts.length);
        JSONArray arr = (JSONArray) cotm.getLastResponse().getData();
        assertNotNull("no json array in response data", arr);
        int size = arr.length();
        assertTrue("no data in json array", 0 < arr.length());
        for (int i = 0; i < size; i++) {
            JSONArray objectData = arr.optJSONArray(i);
            assertNotNull(objectData);
            assertNotNull("no last modified utc found in contact data", objectData.opt(2));
        }
    }

    @Test
    public void testExcludeAdmin() throws Exception {
        int columnIDs[] = new int[] { Contact.OBJECT_ID, Contact.FOLDER_ID, Contact.INTERNAL_USERID };
        /*
         * perform different all requests
         */
        Contact[] allContactsDefault = cotm.allAction(FolderObject.SYSTEM_LDAP_FOLDER_ID, columnIDs);
        assertNotNull("got no contacts", allContactsDefault);
        assertTrue("got no contacts", 0 < allContactsDefault.length);
        Contact[] allContactsWithAdmin = allAction(FolderObject.SYSTEM_LDAP_FOLDER_ID, columnIDs, Boolean.TRUE);
        assertNotNull("got no contacts", allContactsWithAdmin);
        assertTrue("got no contacts", 0 < allContactsWithAdmin.length);
        Contact[] allContactsWithoutAdmin = allAction(FolderObject.SYSTEM_LDAP_FOLDER_ID, columnIDs, Boolean.FALSE);
        assertNotNull("got no contacts", allContactsWithoutAdmin);
        assertTrue("got no contacts", 0 < allContactsWithoutAdmin.length);
        /*
         * check results
         */
        Assert.assertArrayEquals("'admin=true' differs from default result", allContactsDefault, allContactsWithAdmin);
        assertEquals("unexpected number of contacts in result", allContactsWithAdmin.length, allContactsWithoutAdmin.length);
    }

    private Contact[] allAction(int folderId, int[] columns, Boolean admin) throws OXException, IOException, JSONException {
        List<Contact> allContacts = new LinkedList<Contact>();
        AllRequest request = new AllRequestWithAdmin(folderId, columns, admin);
        CommonAllResponse response = getClient().execute(request, cotm.getSleep());
        JSONArray data = (JSONArray) response.getResponse().getData();
        allContacts = cotm.transform(data, columns);
        return allContacts.toArray(new Contact[] {});
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
            return null != admin ? Arrays.add(params, new Parameter("admin", admin.booleanValue())) : params;
        }

    }

}
