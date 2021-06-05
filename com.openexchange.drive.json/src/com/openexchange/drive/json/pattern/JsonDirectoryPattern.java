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

package com.openexchange.drive.json.pattern;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.drive.DirectoryPattern;
import com.openexchange.exception.OXException;
import com.openexchange.java.Enums;

/**
 * {@link JsonDirectoryPattern}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class JsonDirectoryPattern extends AbstractJsonPattern implements DirectoryPattern {

    private final Pattern pathPattern;

    /**
     * Deserializes a directory pattern from the supplied JSON object.
     *
     * @param jsonObject The JSON object to create the pattern from
     * @return The pattern
     */
    public static DirectoryPattern deserialize(JSONObject jsonObject) throws JSONException, OXException {
        if (null == jsonObject) {
            return null;
        }
        PatternType type = Enums.parse(PatternType.class, jsonObject.getString("type"));
        String path = jsonObject.optString("path");
        boolean caseSensitive = jsonObject.optBoolean("caseSensitive");
        return new JsonDirectoryPattern(type, path, caseSensitive);
    }

    /**
     * Deserializes multiple directory patterns from the supplied JSON arrays.
     *
     * @param jsonArray The JSON array to create the patterns from
     * @return The pattern
     */
    public static List<DirectoryPattern> deserialize(JSONArray jsonArray) throws JSONException, OXException {
        if (null == jsonArray) {
            return null;
        }
        List<DirectoryPattern> patterns = new ArrayList<DirectoryPattern>(jsonArray.length());
        for (int i = 0; i < jsonArray.length(); i++) {
            patterns.add(deserialize(jsonArray.getJSONObject(i)));
        }
        return patterns;
    }

    /**
     * Initializes a new {@link JsonDirectoryPattern}.
     *
     * @param type The pattern type
     * @param pathPattern The path pattern
     * @param caseSensitive <code>true</code> to match the patterns in a case sensitive way, <code>false</code>, otherwise
     * @throws OXException
     */
    private JsonDirectoryPattern(PatternType type, String pathPattern, boolean caseSensitive) throws OXException {
        super(type, caseSensitive);
        this.pathPattern = createPattern(type, pathPattern, caseSensitive);
    }

    @Override
    public boolean matches(String path) {
        return pathPattern.matcher(path).matches();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((pathPattern == null) ? 0 : pathPattern.pattern().hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (!(obj instanceof JsonDirectoryPattern)) {
            return false;
        }
        JsonDirectoryPattern other = (JsonDirectoryPattern) obj;
        if (pathPattern == null) {
            if (other.pathPattern != null) {
                return false;
            }
        } else if (!pathPattern.pattern().equals(other.pathPattern.pattern())) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return String.valueOf(type) + ": " + pathPattern;
    }

}
