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

package com.openexchange.test.fixtures.ajax;

import static com.openexchange.java.Autoboxing.I;
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

    @SuppressWarnings("deprecation")
    private void loadGlobalAddressBook() {
        AllRequest all = new AllRequest(FolderObject.SYSTEM_LDAP_FOLDER_ID, Contact.ALL_COLUMNS);

        try {
            CommonAllResponse response = client.execute(all);
            globalAddressBook = new HashMap<Integer, Contact>();
            JSONArray rows = (JSONArray) response.getData();
            for (int i = 0, size = rows.length(); i < size; i++) {
                JSONArray row = rows.getJSONArray(i);
                Contact contact = new Contact();
                ContactSetter setter = new ContactSetter();
                for (int index = 0; index < Contact.ALL_COLUMNS.length; index++) {
                    int column = Contact.ALL_COLUMNS[index];
                    ContactField field = ContactField.getByValue(column);
                    field.doSwitch(setter, contact, row.get(index));
                }
                globalAddressBook.put(I(contact.getInternalUserId()), contact);
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
        return getContact(credentials.getUserId());
    }

    public Contact getContact(int userId) {
        if (globalAddressBook == null) {
            loadGlobalAddressBook();
        }
        return globalAddressBook.get(I(userId));
    }
}
