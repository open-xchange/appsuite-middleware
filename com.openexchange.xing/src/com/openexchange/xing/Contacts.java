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

package com.openexchange.xing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import com.openexchange.xing.exception.XingException;

/**
 * {@link Contacts} - Represents a XING account's contacts.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class Contacts {

    private final int total;
    private final List<User> users;

    /**
     * Initializes a new {@link Contacts}.
     */
    public Contacts(final int total, final List<User> users) {
        super();
        this.total = total;
        this.users = users;
    }

    /**
     * Initializes a new {@link Contacts}.
     *
     * @throws XingException If initialization fails
     */
    public Contacts(final JSONObject contactsInformation) throws XingException {
        super();
        total = contactsInformation.optInt("total", 0);
        if (contactsInformation.hasAndNotNull("users")) {
            final JSONArray usersInformation = contactsInformation.optJSONArray("users");
            final int length = usersInformation.length();
            users = new ArrayList<User>(length);
            for (int i = 0; i < length; i++) {
                users.add(new User(usersInformation.optJSONObject(i)));
            }
        } else {
            users = Collections.emptyList();
        }
    }

    /**
     * Gets the total
     *
     * @return The total
     */
    public int getTotal() {
        return total;
    }

    /**
     * Gets the users
     *
     * @return The users
     */
    public List<User> getUsers() {
        return users;
    }

}
