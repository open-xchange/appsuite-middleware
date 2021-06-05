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
 * {@link PhoneNumbersConsumer} - Parses the contact's phone numbers. Note that Microsoft
 * can store an unlimited mount of phone numbers for a contact due to their different
 * data model (probably EAV). Our contacts API however can only store a handful, therefore
 * we only fetch the first seven we encounter.
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.2
 */
public class PhoneNumbersConsumer implements BiConsumer<JSONObject, Contact> {

    /**
     * Initialises a new {@link PhoneNumbersConsumer}.
     */
    public PhoneNumbersConsumer() {
        super();
    }

    @Override
    public void accept(JSONObject t, Contact u) {
        parseHomePhones(t, u);
        parseBusinessPhones(t, u);
        parseOtherPhones(t, u);
    }

    private void parseHomePhones(JSONObject t, Contact u) {
        if (!t.hasAndNotNull("homePhones")) {
            return;
        }
        JSONArray phonesArray = t.optJSONArray("homePhones");

        int count = 0;
        for (int index = 0; index < phonesArray.length(); index++) {
            String phone = phonesArray.optString(index);
            switch (count++) {
                case 0:
                    u.setTelephoneHome1(phone);
                    break;
                case 1:
                    u.setTelephoneHome2(phone);
                    break;
                default:
                    return;
            }
        }
    }

    private void parseBusinessPhones(JSONObject t, Contact u) {
        if (!t.hasAndNotNull("businessPhones")) {
            return;
        }
        JSONArray phonesArray = t.optJSONArray("businessPhones");

        int count = 0;
        for (int index = 0; index < phonesArray.length(); index++) {
            String phone = phonesArray.optString(index);
            switch (count++) {
                case 0:
                    u.setTelephoneBusiness1(phone);
                    break;
                case 1:
                    u.setTelephoneBusiness2(phone);
                    break;
                default:
                    return;
            }
        }
    }

    private void parseOtherPhones(JSONObject t, Contact u) {
        if (t.hasAndNotNull("mobilePhone")) {
            u.setCellularTelephone1(t.optString("mobilePhone"));
        }
    }
}
