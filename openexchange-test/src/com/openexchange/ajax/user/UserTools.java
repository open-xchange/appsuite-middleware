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
import java.util.ArrayList;
import java.util.List;
import org.json.JSONException;
import org.junit.Assert;
import com.openexchange.ajax.UserTest;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.Executor;
import com.openexchange.ajax.user.actions.GetRequest;
import com.openexchange.ajax.user.actions.GetResponse;
import com.openexchange.ajax.user.actions.SearchRequest;
import com.openexchange.ajax.user.actions.SearchResponse;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.search.ContactSearchObject;
import com.openexchange.user.User;

/**
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class UserTools extends Assert {

    /**
     * Prevent instantiation.
     */
    public UserTools() {
        super();
    }

    public static UserImpl4Test[] searchUser(AJAXClient client, String searchpattern) throws OXException, IOException, JSONException {
        final ContactSearchObject search = new ContactSearchObject();
        search.setPattern(searchpattern);
        search.addFolder(FolderObject.SYSTEM_LDAP_FOLDER_ID);
        final SearchRequest request = new SearchRequest(search, UserTest.CONTACT_FIELDS);
        final SearchResponse response = Executor.execute(client, request);
        assertNotNull("timestamp", response.getTimestamp());
        return response.getUser();
    }

    public static Contact getUserContact(AJAXClient client, int userId) throws OXException, IOException, JSONException {
            GetRequest request = new GetRequest(userId, client.getValues().getTimeZone());
            GetResponse response = client.execute(request);
            return response.getContact();
        }

    public static User getUser(AJAXClient client, int userId) throws OXException, IOException, JSONException {
        return new UserResolver(client).getUser(userId);
    }

    public static User[] listUser(AJAXClient client, int[] ids) throws OXException, IOException, JSONException {
        List<User> users = new ArrayList<>();
        for (int id : ids) {
            users.add(getUser(client, id));
        }
        return users.toArray(new User[users.size()]);
    }
}
