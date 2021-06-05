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

import org.json.JSONArray;
import org.json.JSONException;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AbstractAJAXResponse;
import com.openexchange.exception.OXException;
import com.openexchange.resource.Resource;
import com.openexchange.resource.json.ResourceParser;

/**
 * {@link ResourceListResponse} - The response corresponding to LIST request
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 *
 */
public final class ResourceListResponse extends AbstractAJAXResponse {

    /**
     * Initializes a new {@link ResourceListResponse}
     *
     * @param response
     *            The JSON response container
     */
    ResourceListResponse(final Response response) {
        super(response);
    }

    /**
     * Parses the resources out of this LIST response
     *
     * @return The IDs as an array of <code>int</code>
     * @throws OXException If data is null or reading from JSON object fails
     * @throws JSONException If JSONObject cannot be created
     */
    public Resource[] getResources() throws OXException, JSONException {
        final JSONArray jsonArray = (JSONArray) getResponse().getData();
        final int len = jsonArray.length();
        final Resource[] retval = new Resource[len];
        for (int i = 0; i < len; i++) {
            retval[i] = ResourceParser.parseResource(jsonArray.getJSONObject(i));
        }
        return retval;
    }
}
