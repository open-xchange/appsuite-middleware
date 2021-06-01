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
import org.json.JSONArray;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.groupware.container.Contact;

public class Bug25300Test extends AbstractManagedContactTest {

    private Contact contact;

    public Bug25300Test() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        contact = generateContact();
        contact.setYomiFirstName("\u30a2\u30b9\u30ab\u30ab");
        contact.setYomiLastName("\u30b5\u30c8\u30a6");
        contact.setYomiCompany("\u30b7\u30c4\u30a2\u30a2\u30a2");
        contact.setAddressHome("TestAddressHome 31");
        contact.setAddressBusiness("Test Address Business 34");
        contact.setAddressOther("TestAddressOther 42");
        cotm.newAction(contact);
    }

    @Test
    public void testYomiAndAddressFields() throws Exception {
        int columnIDs[] = new int[] { Contact.OBJECT_ID, Contact.FOLDER_ID, Contact.YOMI_FIRST_NAME, Contact.YOMI_LAST_NAME, Contact.YOMI_COMPANY, Contact.ADDRESS_HOME, Contact.ADDRESS_BUSINESS, Contact.ADDRESS_OTHER };
        Contact[] contacts = cotm.allAction(contact.getParentFolderID(), columnIDs);
        assertNotNull("got no contacts", contacts);
        assertTrue("got no contacts", 0 < contacts.length);
        JSONArray arr = (JSONArray) cotm.getLastResponse().getData();
        assertNotNull("no json array in response data", arr);
        int size = arr.length();
        assertTrue("no data in json array", 0 < arr.length());
        for (int i = 0; i < size; i++) {
            JSONArray objectData = arr.optJSONArray(i);
            assertNotNull(objectData);
            final int objectIdData = objectData.getInt(0);
            final int folderIdData = objectData.getInt(1);
            final String yomiFirstNameData = objectData.getString(2);
            final String yomiLastNameData = objectData.getString(3);
            final String yomiCompanyData = objectData.getString(4);
            final String addressHomeData = objectData.getString(5);
            final String addressBusinessData = objectData.getString(6);
            final String addressOtherData = objectData.getString(7);

            assertEquals("Unexpected objectId: ", objectIdData, contact.getObjectID());
            assertEquals("Unexpected folderId: ", folderIdData, contact.getParentFolderID());
            assertEquals("Unexpected yomiFirstName: ", yomiFirstNameData, contact.getYomiFirstName());
            assertEquals("Unexpected yomiLastName: ", yomiLastNameData, contact.getYomiLastName());
            assertEquals("Unexpected yomiCompany: ", yomiCompanyData, contact.getYomiCompany());
            assertEquals("Unexpected addressHome: ", addressHomeData, contact.getAddressHome());
            assertEquals("Unexpected addressBusiness: ", addressBusinessData, contact.getAddressBusiness());
            assertEquals("Unexpected addressOther: ", addressOtherData, contact.getAddressOther());
        }
    }

}
