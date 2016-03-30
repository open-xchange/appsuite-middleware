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

package com.openexchange.drive.json.json;

import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.drive.DirectoryVersion;


/**
 * {@link JsonDirectoryVersion}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class JsonDirectoryVersion extends JsonDriveVersion implements DirectoryVersion {

    private final String path;

    public JsonDirectoryVersion(String checksum, String path) {
        super(checksum);
        this.path = path;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public String toString() {
        return getPath() + " | " + getChecksum();
    }

    public static JSONObject serialize(DirectoryVersion version) throws JSONException {
        if (null == version) {
            return null;
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.putOpt("checksum", version.getChecksum());
        jsonObject.putOpt("path", version.getPath());
        return jsonObject;
    }

    public static JSONArray serialize(List<DirectoryVersion> versions) throws JSONException {
        JSONArray jsonArray = new JSONArray();
        for (DirectoryVersion version : versions) {
            jsonArray.put(serialize(version));
        }
        return jsonArray;
    }

    public static JsonDirectoryVersion deserialize(JSONObject jsonObject) throws JSONException {
        if (null == jsonObject) {
            return null;
        }
        return new JsonDirectoryVersion(jsonObject.optString("checksum"), jsonObject.optString("path"));
    }

    public static List<DirectoryVersion> deserialize(JSONArray jsonArray) throws JSONException {
        if (null == jsonArray) {
            return null;
        }
        List<DirectoryVersion> versions = new ArrayList<DirectoryVersion>();
        for (int i = 0; i < jsonArray.length(); i++) {
            versions.add(deserialize(jsonArray.getJSONObject(i)));
        }
        return versions;
    }

}
