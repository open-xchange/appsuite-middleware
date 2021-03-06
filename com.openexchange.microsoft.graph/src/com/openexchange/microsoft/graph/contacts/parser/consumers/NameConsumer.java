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
import org.json.JSONObject;
import com.openexchange.groupware.container.Contact;

/**
 * {@link NameConsumer} - Parses the given name, family name and full name of the specified contact
 * along with their yomi representations if available.
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.2
 */
public class NameConsumer implements BiConsumer<JSONObject, Contact> {

    /**
     * Initialises a new {@link NameConsumer}.
     */
    public NameConsumer() {
        super();
    }

    @Override
    public void accept(JSONObject t, Contact u) {
        if (t.hasAndNotNull("displayName")) {
            u.setDisplayName(t.optString("displayName"));
        }
        if (t.hasAndNotNull("givenName")) {
            u.setGivenName(t.optString("givenName"));
        }
        if (t.hasAndNotNull("middleName")) {
            u.setMiddleName(t.optString("middleName"));
        }
        if (t.hasAndNotNull("nickName")) {
            u.setNickname(t.optString("nickName"));
        }
        if (t.hasAndNotNull("surname")) {
            u.setSurName(t.optString("surname"));
        }
        if (t.hasAndNotNull("title")) {
            u.setTitle(t.optString("title"));
        }
        if (t.hasAndNotNull("yomiGivenName")) {
            u.setYomiFirstName(t.optString("yomiGivenName"));
        }
        if (t.hasAndNotNull("yomiSurname")) {
            u.setYomiLastName(t.optString("yomiSurname"));
        }
    }
}
