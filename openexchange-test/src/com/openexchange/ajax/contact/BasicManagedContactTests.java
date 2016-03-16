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

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.search.Order;
import com.openexchange.test.ContactTestManager;

/**
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class BasicManagedContactTests extends AbstractManagedContactTest {

    public BasicManagedContactTests(String name) {
        super(name);
    }

    public void testCreateAndGetContact() {
        Contact expected = generateContact();

        manager.newAction(expected);

        Contact actual = manager.getAction(folderID, expected.getObjectID());

        assertEquals("Surname should match", expected.getSurName(), actual.getSurName());
        assertEquals("Given name should match", expected.getGivenName(), actual.getGivenName());
    }

    public void testDeleteContact() {
        Contact expected = generateContact();

        manager.newAction(expected);

        manager.deleteAction(expected);

        Contact found = manager.getAction(folderID, expected.getObjectID());
        assertNull("Should not find a contact after deletion", found);
    }

    public void testGetAllContacts() {
        int numberBefore = manager.allAction(folderID).length;

        Contact expected = generateContact();

        manager.newAction(expected);

        Contact[] allContactsOnServer = manager.allAction(folderID);

        assertEquals("Should find exactly one more contact", numberBefore + 1, allContactsOnServer.length);
    }

    public void testGetAllContactsWithColumns() {
        int numberBefore = manager.allAction(folderID).length;

        Contact expected = generateContact();

        manager.newAction(expected);

        Contact[] allContactsOnServer = manager.allAction(folderID, new int[] { 1, 4, 5, 20 });

        assertEquals("Should find exactly one more contact", numberBefore + 1, allContactsOnServer.length);


        Contact actual = null;
        for(Contact temp: allContactsOnServer){
            if(temp.getObjectID() == expected.getObjectID()) {
                actual = temp;
            }
        }
        assertNotNull("Should find new contact in response of AllRequest", actual);
        assertTrue("Should contain field #1", actual.contains(1));
        assertTrue("Should contain field #4", actual.contains(4));
        assertTrue("Should contain field #5", actual.contains(5));
        assertTrue("Should contain field #20", actual.contains(20));
    }

    public void testGetAllContactsOrderedByCollationAscending() {
		List<String> sinograph = Arrays.asList( "\u963f", "\u6ce2","\u6b21","\u7684","\u9e45","\u5bcc","\u54e5","\u6cb3","\u6d01","\u79d1","\u4e86","\u4e48","\u5462","\u54e6","\u6279","\u4e03","\u5982","\u56db","\u8e22","\u5c4b","\u897f","\u8863","\u5b50");

		for(String graphem: sinograph){
			manager.newAction( ContactTestManager.generateContact(folderID, graphem) );
		}

		int fieldNum = ContactField.SUR_NAME.getNumber();
        Contact[] allContacts = manager.allAction(folderID, new int[] { 1, 4, 5, 20, fieldNum }, fieldNum, Order.ASCENDING, "gb2312" );

        for(int i = 0, len = sinograph.size(); i < len; i++){
        	String expected = sinograph.get(i);
        	assertEquals("Element #"+i, expected, allContacts[i].getSurName());
        }
    }

    public void testGetAllContactsOrderedByCollationDescending() {
		List<String> sinograph = Arrays.asList( "\u963f", "\u6ce2","\u6b21","\u7684","\u9e45","\u5bcc","\u54e5","\u6cb3","\u6d01","\u79d1","\u4e86","\u4e48","\u5462","\u54e6","\u6279","\u4e03","\u5982","\u56db","\u8e22","\u5c4b","\u897f","\u8863","\u5b50");

		for(String graphem: sinograph){
			manager.newAction( ContactTestManager.generateContact(folderID, graphem) );
		}

		int fieldNum = ContactField.SUR_NAME.getNumber();
        Contact[] allContacts = manager.allAction(folderID, new int[] { 1, 4, 5, 20, fieldNum }, fieldNum, Order.DESCENDING, "gb2312" );

        for(int i = 0, len = sinograph.size(); i < len; i++){
        	String expected = sinograph.get(len-i-1);
        	assertEquals("Element #"+i, expected, allContacts[i].getSurName());
        }
    }


    /**
     * The "wonder field" is 607, which implies sorting by last name, company name, email1, email2 and display name.
     * Currently not testing e-mail since it is not supported yet.
     */
	public void testGetAllContactsOrderedByCollationOrderedByWonderField() {
		List<String> sinograph = Arrays.asList( "\u963f", "\u6ce2","\u6b21","\u7684","\u9e45","\u5bcc","\u54e5","\u6cb3","\u6d01"); //,"\u79d1","\u4e86","\u4e48","\u5462","\u54e6","\u6279","\u4e03","\u5982","\u56db","\u8e22","\u5c4b","\u897f","\u8863","\u5b50");

    	List<String> lastNames = Arrays.asList( "\u963f", "\u6ce2","\u6b21","\u7684");
    	List<String> displayNames = Arrays.asList( "\u9e45", "\u5bcc");
    	List<String> companyNames = Arrays.asList( "\u54e5","\u6cb3","\u6d01");
    	//List<String> email1s = Arrays.asList( "\u79d1@somewhere.invalid","\u4e86@somewhere.invalid","\u4e48@somewhere.invalid");
    	//List<String> email2s = Arrays.asList( "\u5462@somewhere.invalid","\u54e6@somewhere.invalid","\u6279@somewhere.invalid","\u4e03@somewhere.invalid");

    	List<List<String>> values = Arrays.asList(lastNames, displayNames, companyNames); //,email1s, email2s);
    	List<ContactField> fields = Arrays.asList( ContactField.SUR_NAME, ContactField.DISPLAY_NAME, ContactField.COMPANY); //ContactField.EMAIL1, ContactField.EMAIL2 );

    	int valPos = 0;
		for(ContactField field: fields) {
			List<String> values2 = values.get(valPos++);
			for(String value: values2){
				Contact tmp = new Contact();
				tmp.setParentFolderID(folderID);
				tmp.set(field.getNumber(), value);
				tmp.setInfo(value); //we'll use this field to compare afterwards
				manager.newAction(tmp);
			}
		}

		int fieldNum = ContactField.SUR_NAME.getNumber();
        Contact[] allContacts = manager.allAction(folderID, new int[] { 1, 4, 5, 20, fieldNum, ContactField.INFO.getNumber() }, -1, Order.ASCENDING, "gb2312" );

        for(int i = 0, len = sinograph.size(); i < len; i++){
        	String expected = sinograph.get(i);
        	assertEquals("Element #"+i, expected, allContacts[i].getInfo());
        }
    }

    public void testUpdateContactAndGetUpdates() {
        Contact expected = generateContact();

        manager.newAction(expected);

        expected.setDisplayName("Display name");

        Contact update = manager.updateAction(expected);

        Contact[] updated = manager.updatesAction(folderID, new Date(update.getLastModified().getTime() - 1));

        assertEquals("Only one contact should have been updated", 1, updated.length);

        Contact actual = updated[0];
        assertEquals("Display name should have been updated", expected.getDisplayName(), actual.getDisplayName());
    }

    public void testAllWithGBK() throws Exception {
    	//{"action":"all","module":"contacts","columns":"20,1,5,2,602","folder":"66","collation":"gbk","sort":"502","order":"asc"}
    	Contact[] allAction = manager.allAction(getClient().getValues().getPrivateContactFolder(), new int[]{20,1,5,2,602}, 502, Order.ASCENDING, "gbk");
    	assertTrue("Should find more than 0 contacts in the private contact folder", allAction.length > 0);
    }
}
