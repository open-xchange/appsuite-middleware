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
import java.util.Date;
import java.util.List;
import org.json.JSONArray;
import org.junit.Test;
import com.openexchange.ajax.contact.action.SearchByBirthdayRequest;
import com.openexchange.ajax.framework.CommonSearchResponse;
import com.openexchange.groupware.container.Contact;

/**
 * {@link Bug31993Test}
 *
 * The sorting of the displayed birthdays in the birthday-widget seems to be wrong.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class Bug31993Test extends AbstractManagedContactTest {

    @Test
    public void testSortOrder() throws Exception {
        /*
         * create contacts
         */
        Contact contact1 = super.generateContact("Mike");
        contact1.setBirthday(D("1969-04-11 00:00:00"));
        contact1 = cotm.newAction(contact1);
        Contact contact2 = super.generateContact("Frank");
        contact2.setBirthday(D("1980-04-10 00:00:00"));
        contact2 = cotm.newAction(contact2);
        Contact contact3 = super.generateContact("Oliver");
        contact3.setBirthday(D("1988-04-11 00:00:00"));
        contact3 = cotm.newAction(contact3);
        /*
         * search birthdays
         */
        String parentFolderID = String.valueOf(contact1.getParentFolderID());
        int[] columns = { Contact.OBJECT_ID, Contact.SUR_NAME, Contact.BIRTHDAY, Contact.FOLDER_ID };
        SearchByBirthdayRequest request;
        CommonSearchResponse response;
        List<Contact> contacts;

        request = new SearchByBirthdayRequest(new Date(1397088000000L), new Date(1404345600000L), parentFolderID, columns, true);
        response = getClient().execute(request);
        contacts = cotm.transform((JSONArray) response.getResponse().getData(), columns);
        assertNotNull(contacts);
        assertEquals("wrong number of results", 3, contacts.size());

        assertEquals("Contact order wrong", contact2.getSurName(), contacts.get(0).getSurName());
        assertEquals("Contact order wrong", contact1.getSurName(), contacts.get(1).getSurName());
        assertEquals("Contact order wrong", contact3.getSurName(), contacts.get(2).getSurName());

    }

}
