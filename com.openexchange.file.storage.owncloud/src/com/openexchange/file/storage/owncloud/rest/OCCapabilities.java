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

package com.openexchange.file.storage.owncloud.rest;

import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageExceptionCodes;

/**
 * {@link OCCapabilities}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.4
 */
public class OCCapabilities extends AbstractOCJSONResponse {

    private static final String SHARING_FIELD = "files_sharing";
    private static final String FILES_FIELD = "files";

    /**
     * Parses the {@link JSONObject} to a {@link OCCapabilities} object
     *
     * @param json The JSON to parse
     * @return The {@link OCCapabilities}
     * @throws OXException
     */
    public static OCCapabilities parse(JSONObject json) throws OXException {
        OCCapabilities result = new OCCapabilities(json);
        try {
            if (result.getData() instanceof JSONObject == false) {
                throw FileStorageExceptionCodes.JSON_ERROR.create("Unexpected response.");
            }
            result.capabilities = ((JSONObject) result.getData()).getJSONObject("capabilities");
            return result;
        } catch (JSONException e) {
            throw FileStorageExceptionCodes.JSON_ERROR.create(e.getMessage(), e);
        }
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private JSONObject capabilities;
    // Data fields for important entries
    private final Integer searchMinLength = null;
    private Boolean versioning = null;
    private Boolean trash = null;

    /**
     * Initializes a new {@link OCCapabilities}.
     *
     * @param json
     * @throws OXException
     */
    public OCCapabilities(JSONObject json) throws OXException {
        super(json);
    }

    /**
     * Gets the minimum search length
     *
     * @return The minimum search length
     * @throws OXException In case of a parsing error
     */
    public int getSearchMinLength() throws OXException {
        if (searchMinLength != null) {
            return searchMinLength.intValue();
        }
        try {
            JSONObject sharing = capabilities.getJSONObject(SHARING_FIELD);
            Integer searchMinLength = null;
            if (sharing.has("search_min_length")) {
                searchMinLength = Integer.valueOf(sharing.getInt("search_min_length"));
            }
            return searchMinLength == null ? 0 : searchMinLength.intValue();
        } catch (JSONException e) {
            throw FileStorageExceptionCodes.JSON_ERROR.create(e.getMessage(), e);
        }
    }

    /**
     * Checks whether versioning is enabled or not
     *
     * @return <code>true</code> if versioning is enabled, <code>false</code> otherwise
     * @throws OXException In case of a parsing error
     */
    public boolean supportsVersioning() throws OXException {
        if (versioning != null) {
            return versioning.booleanValue();
        }
        try {
            versioning = Boolean.valueOf(capabilities.getJSONObject(FILES_FIELD).getBoolean("versioning"));
            return versioning == null ? false : versioning.booleanValue();
        } catch (JSONException e) {
            throw FileStorageExceptionCodes.JSON_ERROR.create(e.getMessage(), e);
        }
    }

    /**
     * Checks whether trash is supported
     *
     * @return <code>true</code> if trash is supported, <code>false</code> otherwise
     * @throws OXException In case of a parsing error
     */
    public boolean supportsTrash() throws OXException {
        if (trash != null) {
            return trash.booleanValue();
        }
        try {
            trash = Boolean.valueOf(capabilities.getJSONObject(FILES_FIELD).getBoolean("undelete"));
            return trash == null ? false : trash.booleanValue();
        } catch (JSONException e) {
            throw FileStorageExceptionCodes.JSON_ERROR.create(e.getMessage(), e);
        }
    }

}
