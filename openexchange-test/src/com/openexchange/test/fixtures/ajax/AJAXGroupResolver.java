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

import java.io.IOException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AbstractAJAXResponse;
import com.openexchange.ajax.group.actions.GetRequest;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.test.fixtures.GroupResolver;

public class AJAXGroupResolver implements GroupResolver {

    private final AJAXClient client;
    private final AJAXContactFinder contactFinder;

    public AJAXGroupResolver(AJAXClient client) {
        super();
        this.client = client;
        this.contactFinder = new AJAXContactFinder(client);
    }

    @Override
    public Contact[] resolveGroup(String simpleName) {
        // Nothing to do
        return null;
    }

    @Override
    public Contact[] resolveGroup(int groupId) {
        GetRequest group = new GetRequest(groupId);
        try {
            AbstractAJAXResponse response = client.execute(group);
            JSONObject data = (JSONObject) response.getData();
            JSONArray members = data.getJSONArray("members");
            Contact[] groupMembers = new Contact[members.length()];
            for (int i = 0; i < groupMembers.length; i++) {
                int userId = members.getInt(i);
                groupMembers[i] = contactFinder.getContact(userId);
            }
            return groupMembers;
        } catch (OXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

}
