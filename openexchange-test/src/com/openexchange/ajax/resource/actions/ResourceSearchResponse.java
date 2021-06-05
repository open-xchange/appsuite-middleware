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

package com.openexchange.ajax.resource.actions;

import java.util.LinkedList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AbstractAJAXResponse;
import com.openexchange.resource.Resource;

/**
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class ResourceSearchResponse extends AbstractAJAXResponse {

    protected ResourceSearchResponse(Response response) {
        super(response);
    }

    public List<Resource> getResources() throws JSONException {
        List<Resource> list = new LinkedList<Resource>();
        JSONArray data = (JSONArray) getData();
        for (int i = 0, length = data.length(); i < length; i++) {
            JSONObject obj = data.getJSONObject(i);
            Resource temp = new Resource();
            temp.setIdentifier(obj.getInt("id"));
            temp.setDisplayName(obj.getString("display_name"));
            temp.setMail(obj.getString("mailaddress"));
            temp.setSimpleName(obj.getString("name"));
            temp.setAvailable(obj.getBoolean("availability"));
            temp.setDescription(obj.getString("description"));
            list.add(temp);
        }
        return list;
    }

}
