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

/**
 * {@link ResourceAllResponse} - The response corresponding to ALL request
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 *
 */
public final class ResourceAllResponse extends AbstractAJAXResponse {

    /**
     * Initializes a new {@link ResourceAllResponse}
     *
     * @param response
     *            The JSON response container
     */
    ResourceAllResponse(final Response response) {
        super(response);
    }

    /**
     * Parses the IDs out of this ALL response
     *
     * @return The IDs as an array of <code>int</code>
     * @throws JSONException
     *             If a JSON error occurs
     */
    public int[] getIDs() throws JSONException {
        final JSONArray jsonArray = (JSONArray) getResponse().getData();
        final int len = jsonArray.length();
        final int[] retval = new int[len];
        for (int i = 0; i < len; i++) {
            retval[i] = jsonArray.getInt(i);
        }
        return retval;
    }
}
