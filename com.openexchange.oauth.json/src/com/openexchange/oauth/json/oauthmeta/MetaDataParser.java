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

package com.openexchange.oauth.json.oauthmeta;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Parses the JSON representation of an OAuth service meta data.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class MetaDataParser {

    private MetaDataParser() {
        super();
    }

    /**
     * Parses JSON to an {@link ParsedOAuthServiceMetaData} instance.
     *
     * @param metaDataJSON The JSON to parse
     * @return The parsed {@link ParsedOAuthServiceMetaData} instance
     * @throws JSONException If a JSON error occurs
     */
    public static ParsedOAuthServiceMetaData parse(final JSONObject metaDataJSON) throws JSONException {
        final ParsedOAuthServiceMetaData metaData = new ParsedOAuthServiceMetaData();

        if (metaDataJSON.hasAndNotNull(MetaDataField.ID.getName())) {
            metaData.setId(metaDataJSON.getString(MetaDataField.ID.getName()));
        }
        if (metaDataJSON.hasAndNotNull(MetaDataField.DISPLAY_NAME.getName())) {
            metaData.setDisplayName(metaDataJSON.getString(MetaDataField.DISPLAY_NAME.getName()));
        }

        return metaData;
    }

}
