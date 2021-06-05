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

package com.openexchange.ajax.group.actions;

import java.util.LinkedList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AbstractAJAXResponse;
import com.openexchange.ajax.parser.GroupParser;
import com.openexchange.exception.OXException;
import com.openexchange.group.Group;

/**
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class UpdatesResponse extends AbstractAJAXResponse {

    protected UpdatesResponse(Response response) {
        super(response);
    }

    public List<Group> getModified() throws OXException, JSONException {
        return getGroups("modified");
    }

    public List<Group> getNew() throws OXException, JSONException {
        return getGroups("new");
    }

    public List<Group> getDeleted() throws OXException, JSONException {
        return getGroups("deleted");
    }

    protected List<Group> getGroups(String field) throws OXException, JSONException {
        LinkedList<Group> groups = new LinkedList<Group>();
        JSONObject data = (JSONObject) getData();

        if (data.isNull(field)) {
            return new LinkedList<Group>();
        }

        JSONArray grp = data.getJSONArray(field);

        GroupParser parser = new GroupParser();

        for (int i = 0, length = grp.length(); i < length; i++) {
            Group temp = new Group();
            parser.parse(temp, grp.getJSONObject(i));
            groups.add(temp);
        }
        return groups;
    }
}
