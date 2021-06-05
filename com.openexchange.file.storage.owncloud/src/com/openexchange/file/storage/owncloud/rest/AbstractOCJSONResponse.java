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
import org.json.JSONValue;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageExceptionCodes;

/**
 * {@link AbstractOCJSONResponse}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.4
 */
public class AbstractOCJSONResponse {
    
    // json object containing the data
    protected final JSONValue data;
    
    /**
     * Initializes a new {@link AbstractOCJSONResponse}.
     * 
     * @param json The response as a {@link JSONObject}
     * @throws OXException
     */
    public AbstractOCJSONResponse(JSONObject json) throws OXException {
        super();
        try {
            this.data = (JSONValue) json.getJSONObject("ocs").getRaw("data");
        } catch (JSONException e) {
            throw FileStorageExceptionCodes.JSON_ERROR.create(e.getMessage(), e);
        }
    }
    
    /**
     * Gets the data element of this response
     *
     * @return The data as a {@link JSONValue}
     */
    protected JSONValue getData() {
        return data;
    }
    
    @Override
    public String toString() {
        return this.data.toString();
    }

}
