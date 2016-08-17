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

import org.json.JSONArray;
import com.openexchange.groupware.container.Contact;

public class Bug25300Test extends AbstractManagedContactTest {

    private Contact contact;

    public Bug25300Test(String name) {
        super(name);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        contact = generateContact();
        contact.setYomiFirstName("\u30a2\u30b9\u30ab\u30ab");
        contact.setYomiLastName("\u30b5\u30c8\u30a6");
        contact.setYomiCompany("\u30b7\u30c4\u30a2\u30a2\u30a2");
        contact.setAddressHome("TestAddressHome 31");
        contact.setAddressBusiness("Test Address Business 34");
        contact.setAddressOther("TestAddressOther 42");
        manager.newAction(contact);
    }

    public void testYomiAndAddressFields() throws Exception {
        int columnIDs[] = new int[] {
            Contact.OBJECT_ID, Contact.FOLDER_ID, Contact.YOMI_FIRST_NAME, Contact.YOMI_LAST_NAME, Contact.YOMI_COMPANY,
            Contact.ADDRESS_HOME, Contact.ADDRESS_BUSINESS, Contact.ADDRESS_OTHER };
        Contact[] contacts = manager.allAction(contact.getParentFolderID(), columnIDs);
        assertNotNull("got no contacts", contacts);
        assertTrue("got no contacts", 0 < contacts.length);
        JSONArray arr = (JSONArray) manager.getLastResponse().getData();
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

    public void testBackwardCompatibilityWithExchangedColumnsId() throws Exception {
        // old column ids in contact details
        final int yomiFirstName = 610;
        final int yomiLastName = 611;
        final int yomiCompany = 612;
        final int addressHome = 613;
        final int addressBusiness = 614;
        final int addressOther = 615;

        int columnIDs[] = new int[] {
            Contact.OBJECT_ID, Contact.FOLDER_ID, yomiFirstName, yomiLastName, yomiCompany, addressHome, addressBusiness, addressOther };
        Contact[] contacts = manager.allAction(contact.getParentFolderID(), columnIDs);
        assertNotNull("got no contacts", contacts);
        assertTrue("got no contacts", 0 < contacts.length);
        JSONArray arr = (JSONArray) manager.getLastResponse().getData();
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
