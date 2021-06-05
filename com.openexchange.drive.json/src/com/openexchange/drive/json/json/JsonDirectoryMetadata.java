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

package com.openexchange.drive.json.json;

import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.drive.DirectoryMetadata;


/**
 * {@link JsonDirectoryMetadata}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class JsonDirectoryMetadata {

    public static JSONArray serialize(List<DirectoryMetadata> directoryMetadata) throws JSONException {
        JSONArray jsonArray = new JSONArray(directoryMetadata.size());
        for (DirectoryMetadata metadata : directoryMetadata) {
            jsonArray.put(serialize(metadata));
        }
        return jsonArray;
    }

    public static JSONObject serialize(DirectoryMetadata metadata) throws JSONException {
        JSONObject jsonObject = new JSONObject(3);
        jsonObject.put("path", metadata.getPath());
        jsonObject.put("checksum", metadata.getChecksum());
        jsonObject.put("directLink", metadata.getDirectLink());
        jsonObject.put("directLinkFragments", metadata.getDirectLinkFragments());
        return jsonObject;
    }

}
