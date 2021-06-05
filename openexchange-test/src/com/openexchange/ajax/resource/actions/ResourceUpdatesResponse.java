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
import com.openexchange.exception.OXException;
import com.openexchange.resource.Resource;
import com.openexchange.resource.json.ResourceParser;

/**
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class ResourceUpdatesResponse extends AbstractAJAXResponse {

    protected ResourceUpdatesResponse(Response response) {
        super(response);
    }

    public List<Resource> getModified() throws OXException, JSONException {
        return getGroups("modified");
    }

    public List<Resource> getNew() throws OXException, JSONException {
        return getGroups("new");
    }

    public List<Resource> getDeleted() throws OXException, JSONException {
        return getGroups("deleted");
    }

    protected List<Resource> getGroups(String field) throws OXException, JSONException {
        LinkedList<Resource> resources = new LinkedList<Resource>();

        JSONObject data = (JSONObject) getData();
        if (!data.isNull(field)) {
            JSONArray jsonResources = data.getJSONArray(field);
            for (int i = 0, length = jsonResources.length(); i < length; i++) {
                Resource resource = ResourceParser.parseResource(jsonResources.getJSONObject(i));
                resources.add(resource);
            }
        }

        return resources;
    }
}
