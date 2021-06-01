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
import com.openexchange.resource.Resource;

/**
 * {@link ResourceWriter} - Writes a {@link Resource resource} to a JSON object
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ResourceWriter {

    /**
     * Initializes a new {@link ResourceWriter}
     */
    private ResourceWriter() {
        super();
    }

    /**
     * Writes specified {@link Resource resource} to a JSON object
     *
     * @param resource The resource to write
     * @return The written JSON object
     * @throws JSONException If writing to JSON object fails
     */
    public static JSONObject writeResource(final Resource resource) throws JSONException {
        final JSONObject retval = new JSONObject(10);
        retval.put(ResourceFields.ID, resource.getIdentifier() == -1 ? JSONObject.NULL : Integer.valueOf(resource.getIdentifier()));
        retval.put(ResourceFields.NAME, resource.getSimpleName() == null ? JSONObject.NULL : resource.getSimpleName());
        retval.put(ResourceFields.DISPLAY_NAME, resource.getDisplayName() == null ? JSONObject.NULL : resource.getDisplayName());
        retval.put(ResourceFields.MAIL, resource.getMail() == null ? JSONObject.NULL : resource.getMail());
        retval.put(ResourceFields.AVAILABILITY, resource.isAvailable());
        retval.put(ResourceFields.DESCRIPTION, resource.getDescription() == null ? JSONObject.NULL : resource.getDescription());
        retval.put(ResourceFields.LAST_MODIFIED, resource.getLastModified() == null ? JSONObject.NULL : resource.getLastModified());
        retval.put(ResourceFields.LAST_MODIFIED_UTC, resource.getLastModified() == null ? JSONObject.NULL : resource.getLastModified());
        return retval;
    }

}
