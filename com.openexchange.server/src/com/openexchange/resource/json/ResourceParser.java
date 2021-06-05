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

package com.openexchange.resource.json;

import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.resource.Resource;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

/**
 * {@link ResourceParser} - Parses a {@link Resource resource} out of a JSON object
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ResourceParser {

    /**
     * Initializes a new {@link ResourceParser}
     */
    private ResourceParser() {
        super();
    }

    /**
     * Parses a {@link Resource resource} out of given JSON object
     *
     * @param jsonResource The JSON object containing the resource's data
     * @return The parsed {@link Resource resource}
     * @throws OXException If data is null or reading from JSON object fails
     */
    public static Resource parseResource(final JSONObject jsonResource) throws OXException {
        if (jsonResource == null) {
            throw AjaxExceptionCodes.MISSING_REQUEST_BODY.create();
        }
        try {
            final Resource retval = new Resource();
            if (jsonResource.has(ResourceFields.ID) && !jsonResource.isNull(ResourceFields.ID)) {
                retval.setIdentifier(jsonResource.getInt(ResourceFields.ID));
            }
            if (jsonResource.has(ResourceFields.NAME) && !jsonResource.isNull(ResourceFields.NAME)) {
                retval.setSimpleName(jsonResource.getString(ResourceFields.NAME));
            }
            if (jsonResource.has(ResourceFields.DISPLAY_NAME) && !jsonResource.isNull(ResourceFields.DISPLAY_NAME)) {
                retval.setDisplayName(jsonResource.getString(ResourceFields.DISPLAY_NAME));
            }
            if (jsonResource.has(ResourceFields.MAIL) && !jsonResource.isNull(ResourceFields.MAIL)) {
                retval.setMail(jsonResource.getString(ResourceFields.MAIL));
            }
            if (jsonResource.has(ResourceFields.AVAILABILITY) && !jsonResource.isNull(ResourceFields.AVAILABILITY)) {
                retval.setAvailable(jsonResource.getBoolean(ResourceFields.AVAILABILITY));
            }
            if (jsonResource.has(ResourceFields.DESCRIPTION) && !jsonResource.isNull(ResourceFields.DESCRIPTION)) {
                retval.setDescription(jsonResource.getString(ResourceFields.DESCRIPTION));
            }
            return retval;
        } catch (JSONException e) {
            throw AjaxExceptionCodes.INVALID_JSON_REQUEST_BODY.create(e);
        }
    }
}
