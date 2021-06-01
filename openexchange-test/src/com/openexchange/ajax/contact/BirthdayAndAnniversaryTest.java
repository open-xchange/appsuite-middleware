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

import static com.openexchange.test.common.groupware.calendar.TimeTools.D;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import java.util.List;
import org.json.JSONArray;
import org.junit.Test;
import com.openexchange.ajax.contact.action.SearchByBirthdayRequest;
import com.openexchange.ajax.framework.CommonSearchResponse;
import com.openexchange.groupware.container.Contact;

/**
 * {@link BirthdayAndAnniversaryTest}
 * 
 * Checks the requests to get upcoming birthdays and anniversaries.
 * 
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class BirthdayAndAnniversaryTest extends AbstractManagedContactTest {

    @Test
    public void testSearchByBirthday() throws Exception {
        /*
         * create contacts
         */
        Contact contact1 = super.generateContact("M\u00e4rz");
        contact1.setBirthday(D("1988-03-03 00:00"));
        contact1 = cotm.newAction(contact1);
        Contact contact2 = super.generateContact("Juli");
        contact2.setBirthday(D("1977-07-07 00:00:00"));
        contact2 = cotm.newAction(contact2);
        Contact contact3 = super.generateContact("Oktober");
        contact3.setBirthday(D("1910-10-10 00:00:00"));
        contact3 = cotm.newAction(contact3);
        /*
         * search birthdays in different timeframes
         */
        String parentFolderID = String.valueOf(contact1.getParentFolderID());
        int[] columns = { Contact.OBJECT_ID, Contact.BIRTHDAY, Contact.FOLDER_ID };
        SearchByBirthdayRequest request;
        CommonSearchResponse response;
        List<Contact> contacts;

        request = new SearchByBirthdayRequest(D("2013-01-01 00:00:00"), D("2013-09-01 00:00:00"), parentFolderID, columns, true);
        response = getClient().execute(request);
        contacts = cotm.transform((JSONArray) response.getResponse().getData(), columns);
        assertNotNull(contacts);
        assertEquals("wrong number of results", 2, contacts.size());

        request = new SearchByBirthdayRequest(D("2013-01-01 00:00:00"), D("2014-01-01 00:00:00"), parentFolderID, columns, true);
        response = getClient().execute(request);
        contacts = cotm.transform((JSONArray) response.getResponse().getData(), columns);
        assertNotNull(contacts);
        assertEquals("wrong number of results", 3, contacts.size());

        request = new SearchByBirthdayRequest(D("2013-06-01 00:00:00"), D("2014-01-01 00:00:00"), parentFolderID, columns, true);
        response = getClient().execute(request);
        contacts = cotm.transform((JSONArray) response.getResponse().getData(), columns);
        assertNotNull(contacts);
        assertEquals("wrong number of results", 2, contacts.size());

        request = new SearchByBirthdayRequest(D("2013-03-04 00:00:00"), D("2013-07-06 00:00:00"), parentFolderID, columns, true);
        response = getClient().execute(request);
        contacts = cotm.transform((JSONArray) response.getResponse().getData(), columns);
        assertNotNull(contacts);
        assertEquals("wrong number of results", 0, contacts.size());

        request = new SearchByBirthdayRequest(D("2085-03-03 00:00:00"), D("2085-03-03 01:01:00"), parentFolderID, columns, true);
        response = getClient().execute(request);
        contacts = cotm.transform((JSONArray) response.getResponse().getData(), columns);
        assertNotNull(contacts);
        assertEquals("wrong number of results", 1, contacts.size());

    }

}
