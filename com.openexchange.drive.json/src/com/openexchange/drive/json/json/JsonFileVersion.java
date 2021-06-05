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

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.drive.FileVersion;

/**
 * {@link JsonFileVersion}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class JsonFileVersion extends JsonDriveVersion implements FileVersion {

    private final String name;

    public JsonFileVersion(String checksum, String name) {
        super(checksum);
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return getName() + " | " + getChecksum();
    }

    public static JSONObject serialize(FileVersion version) throws JSONException {
        if (null == version) {
            return null;
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.putOpt("checksum", version.getChecksum());
        jsonObject.putOpt("name", version.getName());
        return jsonObject;
    }

    public static JSONArray serialize(List<FileVersion> versions) throws JSONException {
        JSONArray jsonArray = new JSONArray();
        for (FileVersion version : versions) {
            jsonArray.put(serialize(version));
        }
        return jsonArray;
    }

    public static JsonFileVersion deserialize(JSONObject jsonObject) {
        if (null == jsonObject) {
            return null;
        }
        return new JsonFileVersion(jsonObject.optString("checksum"), jsonObject.optString("name"));
    }

    public static List<FileVersion> deserialize(JSONArray jsonArray) throws JSONException {
        if (null == jsonArray) {
            return null;
        }
        List<FileVersion> versions = new ArrayList<FileVersion>();
        for (int i = 0; i < jsonArray.length(); i++) {
            versions.add(deserialize(jsonArray.getJSONObject(i)));
        }
        return versions;
    }

}
