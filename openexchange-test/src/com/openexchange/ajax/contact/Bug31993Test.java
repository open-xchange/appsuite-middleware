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

import static com.openexchange.groupware.calendar.TimeTools.D;
import java.util.Date;
import java.util.List;
import org.json.JSONArray;
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

    public Bug31993Test(String name) {
        super(name);
    }

	@Override
	public void setUp() throws Exception {
	    super.setUp();
	}

    public void testSortOrder() throws Exception {
    	/*
    	 * create contacts
    	 */
        Contact contact1 = super.generateContact("Mike");
        contact1.setBirthday(D("1969-04-11 00:00:00"));
        contact1 = manager.newAction(contact1);
        Contact contact2 = super.generateContact("Frank");
        contact2.setBirthday(D("1980-04-10 00:00:00"));
        contact2 = manager.newAction(contact2);
        Contact contact3 = super.generateContact("Oliver");
        contact3.setBirthday(D("1988-04-11 00:00:00"));
        contact3 = manager.newAction(contact3);
    	/*
    	 * search birthdays
    	 */
        String parentFolderID = String.valueOf(contact1.getParentFolderID());
        int[] columns = { Contact.OBJECT_ID, Contact.SUR_NAME, Contact.BIRTHDAY, Contact.FOLDER_ID };
        SearchByBirthdayRequest request;
        CommonSearchResponse response;
        List<Contact> contacts;

        request = new SearchByBirthdayRequest(new Date(1397088000000L), new Date(1404345600000L), parentFolderID, columns, true);
        response = client.execute(request);
        contacts = manager.transform((JSONArray) response.getResponse().getData(), columns);
        assertNotNull(contacts);
        assertEquals("wrong number of results", 3, contacts.size());

        assertEquals("Contact order wrong", contact2.getSurName(), contacts.get(0).getSurName());
        assertEquals("Contact order wrong", contact1.getSurName(), contacts.get(1).getSurName());
        assertEquals("Contact order wrong", contact3.getSurName(), contacts.get(2).getSurName());

    }

}
