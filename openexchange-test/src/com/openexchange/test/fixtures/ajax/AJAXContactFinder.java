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

package com.openexchange.test.fixtures.ajax;

import java.io.IOException;
import java.util.HashMap;
import org.json.JSONArray;
import org.json.JSONException;
import com.openexchange.ajax.contact.action.AllRequest;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.CommonAllResponse;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contact.helpers.ContactField;
import com.openexchange.groupware.contact.helpers.ContactSetter;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.test.fixtures.ContactFinder;
import com.openexchange.test.fixtures.SimpleCredentials;

public class AJAXContactFinder implements ContactFinder {

	private final AJAXClient client;
	private HashMap<Integer, Contact> globalAddressBook;

	public AJAXContactFinder(AJAXClient client) {
		this.client = client;
	}

	private void loadGlobalAddressBook() {
		AllRequest all = new AllRequest(FolderObject.SYSTEM_LDAP_FOLDER_ID, Contact.ALL_COLUMNS);

		try {
			CommonAllResponse response = client.execute(all);
			globalAddressBook = new HashMap<Integer, Contact>();
			JSONArray rows = (JSONArray) response.getData();
			for(int i = 0, size = rows.length(); i < size; i++) {
				JSONArray row = rows.getJSONArray(i);
				Contact contact = new Contact();
				ContactSetter setter = new ContactSetter();
				for(int index = 0; index < Contact.ALL_COLUMNS.length; index++) {
					int column = Contact.ALL_COLUMNS[index];
					ContactField field = ContactField.getByValue(column);
					field.doSwitch(setter, contact, row.get(index));
				}
				globalAddressBook.put(contact.getInternalUserId(), contact);
			}
		} catch (OXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	@Override
    public Contact getContact(SimpleCredentials credentials) {
		return getContact( credentials.getUserId() );
	}

	public Contact getContact(int userId){
		if(globalAddressBook == null) {
			loadGlobalAddressBook();
		}
		return globalAddressBook.get(userId);
	}
}
