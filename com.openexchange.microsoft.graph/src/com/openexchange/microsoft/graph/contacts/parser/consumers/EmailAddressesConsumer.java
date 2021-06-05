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

package com.openexchange.microsoft.graph.contacts.parser.consumers;

import java.util.function.BiConsumer;
import org.json.JSONArray;
import org.json.JSONObject;
import com.openexchange.groupware.container.Contact;

/**
 * {@link EmailAddressesConsumer} - Parses the contact's e-mail addresses. Note that Microsoft
 * can store an unlimited mount of e-mail addresses for a contact due to their different
 * data model (probably EAV). Our contacts API however can only store three, therefore
 * we only fetch the first three we encounter.
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.2
 */
public class EmailAddressesConsumer implements BiConsumer<JSONObject, Contact> {

    /**
     * Initialises a new {@link EmailAddressesConsumer}.
     */
    public EmailAddressesConsumer() {
        super();
    }

    @Override
    public void accept(JSONObject t, Contact u) {
        if (!t.hasAndNotNull("emailAddresses")) {
            return;
        }
        JSONArray addresses = t.optJSONArray("emailAddresses");
        int count = 0;
        for (int index = 0; index < addresses.length(); index++) {
            JSONObject email = addresses.optJSONObject(index);
            String address = email.optString("address");
            switch (count++) {
                case 0:
                    u.setEmail1(address);
                    break;
                case 1:
                    u.setEmail2(address);
                    break;
                case 2:
                    u.setEmail3(address);
                    break;
                default:
                    return;
            }
        }
    }
}
