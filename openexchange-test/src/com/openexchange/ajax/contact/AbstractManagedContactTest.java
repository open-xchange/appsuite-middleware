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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.util.Date;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import com.openexchange.ajax.framework.AbstractAJAXSession;
import com.openexchange.ajax.framework.UserValues;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.modules.Module;

public abstract class AbstractManagedContactTest extends AbstractAJAXSession {

    protected int folderID;
    protected int secondFolderID;
    
    protected String folderName1;
    protected String folderName2;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        UserValues values = getClient().getValues();
        this.folderName1 = ("ManagedContactTest_" + (new Date().getTime()));
        FolderObject folder = ftm.generatePublicFolder(folderName1, Module.CONTACTS.getFolderConstant(), values.getPrivateContactFolder(), values.getUserId());
        folder = ftm.insertFolderOnServer(folder);
        folderID = folder.getObjectID();
        
        this.folderName2 = ("ManagedContactTest_2" + (new Date().getTime()));
        folder = ftm.generatePublicFolder(folderName2, Module.CONTACTS.getFolderConstant(), values.getPrivateContactFolder(), values.getUserId());
        folder = ftm.insertFolderOnServer(folder);
        secondFolderID = folder.getObjectID();
    }

    protected Contact generateContact(String lastname) {
        return this.generateContact(lastname, folderID);
    }
    
    protected Contact generateContact(String lastname, int folderId) {
        Contact contact = new Contact();
        contact.setSurName(lastname);
        contact.setGivenName("Given name");
        contact.setDisplayName(contact.getSurName() + ", " + contact.getGivenName());
        contact.setParentFolderID(folderId);
        return contact;
    }

    protected Contact generateContact() {
        return generateContact("Surname");
    }
    
    protected void assertFileName(HttpResponse httpResp, String expectedFileName) {
        Header[] headers = httpResp.getHeaders("Content-Disposition");
        for (Header header : headers) {
            assertNotNull(header.getValue());
            assertTrue(header.getValue().contains(expectedFileName));
        }
    }    
    
    protected JSONObject addRequestIds(int folderId, int objectId) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("id", objectId);
        json.put("folder_id", folderId);
        return json;
    }   
}
