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

package com.openexchange.ajax.user;

import java.io.IOException;
import org.json.JSONException;
import com.openexchange.ajax.UserTest;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.user.actions.GetRequest;
import com.openexchange.ajax.user.actions.GetResponse;
import com.openexchange.ajax.user.actions.SearchRequest;
import com.openexchange.ajax.user.actions.SearchResponse;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.search.ContactSearchObject;
import com.openexchange.user.User;

/**
 * {@link UserResolver}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a> - fix to work like GUI
 */
public class UserResolver {

    private final AJAXClient client;

    public UserResolver(AJAXClient client) {
        this.client = client;
    }

    /**
     * Finds users that match the search pattern.
     */
    public User[] resolveUser(String searchPattern) throws OXException, IOException, JSONException {
        final ContactSearchObject search = new ContactSearchObject();
        search.setDisplayName(searchPattern);
        search.setGivenName(searchPattern);
        search.setSurname(searchPattern);
        search.setEmail1(searchPattern);
        search.setEmail2(searchPattern);
        search.setEmail3(searchPattern);
        search.setOrSearch(true);
        final SearchRequest request = new SearchRequest(search, UserTest.CONTACT_FIELDS);
        final SearchResponse response = client.execute(request);
        return response.getUser();
    }

    /**
     * Loads a user by its user id.
     */
    public User getUser(int identifier) throws OXException, IOException, JSONException {
        GetRequest request = new GetRequest(identifier, client.getValues().getTimeZone());
        GetResponse response = client.execute(request);
        return response.getUser();
    }
}
