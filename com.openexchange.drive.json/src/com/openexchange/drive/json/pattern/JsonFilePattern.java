/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.drive.json.pattern;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.drive.FilePattern;
import com.openexchange.exception.OXException;
import com.openexchange.java.Enums;

/**
 * {@link JsonFilePattern}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class JsonFilePattern extends AbstractJsonPattern implements FilePattern {

    private final Pattern pathPattern;
    private final Pattern namePattern;

    /**
     * Deserializes a file pattern from the supplied JSON object.
     *
     * @param jsonObject The JSON object to create the pattern from
     * @return The pattern
     */
    public static FilePattern deserialize(JSONObject jsonObject) throws JSONException, OXException {
        if (null == jsonObject) {
            return null;
        }
        PatternType type = Enums.parse(PatternType.class, jsonObject.getString("type"));
        String path = jsonObject.optString("path");
        String name = jsonObject.optString("name");
        boolean caseSensitive = jsonObject.optBoolean("caseSensitive");
        return new JsonFilePattern(type, path, name, caseSensitive);
    }

    /**
     * Deserializes multiple file patterns from the supplied JSON arrays.
     *
     * @param jsonArray The JSON array to create the patterns from
     * @return The pattern
     */
    public static List<FilePattern> deserialize(JSONArray jsonArray) throws JSONException, OXException {
        if (null == jsonArray) {
            return null;
        }
        List<FilePattern> patterns = new ArrayList<FilePattern>(jsonArray.length());
        for (int i = 0; i < jsonArray.length(); i++) {
            patterns.add(deserialize(jsonArray.getJSONObject(i)));
        }
        return patterns;
    }

    /**
     * Initializes a new {@link JsonFilePattern}.
     *
     * @param type The pattern type
     * @param path The path pattern
     * @param name The name pattern
     * @param caseSensitive <code>true</code> to match the patterns in a case sensitive way, <code>false</code>, otherwise
     * @throws OXException
     */
    private JsonFilePattern(PatternType type, String path, String name, boolean caseSensitive) throws OXException {
        super(type, caseSensitive);
        this.pathPattern = createPattern(type, path, caseSensitive);
        this.namePattern = createPattern(type, name, caseSensitive);
    }

    @Override
    public boolean matches(String path, String name) {
        return pathPattern.matcher(path).matches() && namePattern.matcher(name).matches();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((namePattern == null) ? 0 : namePattern.pattern().hashCode());
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
        if (!(obj instanceof JsonFilePattern)) {
            return false;
        }
        JsonFilePattern other = (JsonFilePattern) obj;
        if (namePattern == null) {
            if (other.namePattern != null) {
                return false;
            }
        } else if (!namePattern.pattern().equals(other.namePattern.pattern())) {
            return false;
        }
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
        return String.valueOf(type) + ": " + pathPattern + " | " + namePattern;
    }

}
