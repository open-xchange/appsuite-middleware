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

package com.openexchange.ajax.share.actions;

import java.util.List;
import java.util.TimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.DefaultFile;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.json.FileMetadataFieldParser;
import com.openexchange.file.storage.meta.FileFieldSet;

/**
 * {@link FileShare}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class FileShare extends DefaultFile {

    /**
     * Parses a {@link FileShare}.
     *
     * @param jsonFile The json array from a <code>shares</code> response
     * @param columns The requested columns
     * @param timeZone The client timezone
     */
    public static FileShare parse(JSONArray jsonFile, int[] columns, TimeZone timeZone) throws JSONException, OXException {
        FileShare fileShare = new FileShare();
        FileFieldSet fileFieldSet = new FileFieldSet();
        for (int i = 0; i < columns.length; i++) {
            switch (columns[i]) {
                case 7010:
                    fileShare.extendedFolderPermissions = ExtendedPermissionEntity.parse(jsonFile.optJSONArray(i), timeZone);
                    break;
                default:
                    Field field = File.Field.get(columns[i]);
                    Object orig = jsonFile.get(i);
                    Object converted = FileMetadataFieldParser.convert(field, orig, timeZone);
                    field.doSwitch(fileFieldSet, fileShare, converted);
                    break;
            }
        }
        return fileShare;
    }

    /**
     * Parses a {@link FileShare}.
     *
     * @param jsonObject The json object from an <code>infostore/get</code> response
     * @param timeZone The client timezone
     */
    public static FileShare parse(JSONObject jsonObject, TimeZone timeZone) throws OXException, JSONException {
        FileShare fileShare = new FileShare();
        FileFieldSet fileFieldSet = new FileFieldSet();
        for (String key : jsonObject.keySet()) {
            if ("com.openexchange.share.extendedObjectPermissions".equals(key)) {
                fileShare.extendedFolderPermissions = ExtendedPermissionEntity.parse(jsonObject.optJSONArray(key), timeZone);
            } else {
                Field field = File.Field.get(key);
                if (null != field) {
                    Object orig = jsonObject.get(key);
                    Object converted = FileMetadataFieldParser.convert(field, orig, timeZone);
                    field.doSwitch(fileFieldSet, fileShare, converted);
                }
            }
        }
        return fileShare;
    }

    private List<ExtendedPermissionEntity> extendedFolderPermissions;

    private FileShare() {
        super();
    }

    /**
     * Gets the extended permission entities.
     *
     * @return The extended permissions
     */
    public List<ExtendedPermissionEntity> getExtendedPermissions() {
        return extendedFolderPermissions;
    }

}
