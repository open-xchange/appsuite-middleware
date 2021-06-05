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
import com.openexchange.ajax.fields.FolderFields;
import com.openexchange.ajax.folder.actions.Parser;
import com.openexchange.ajax.parser.FolderParser;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;

/**
 * {@link FolderShare}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class FolderShare extends FolderObject {

    /**
     * Parses a {@link FolderShare}.
     *
     * @param jsonFolder The json array from a <code>shares</code> response
     * @param columns The requested columns
     * @param timeZone The client timezone
     */
    public static FolderShare parse(JSONArray jsonFolder, int[] columns, TimeZone timeZone) throws JSONException, OXException {
        FolderShare folderShare = new FolderShare();
        for (int i = 0; i < columns.length; i++) {
            switch (columns[i]) {
                case 3060:
                    folderShare.extendedFolderPermissions = ExtendedPermissionEntity.parse(jsonFolder.optJSONArray(i), timeZone);
                    break;
                default:
                    Parser.parse(jsonFolder.get(i), columns[i], folderShare);
                    break;
            }
        }
        return folderShare;
    }

    /**
     * Parses a {@link FolderShare}.
     *
     * @param jsonObject The json object from a <code>folder/get</code> response
     * @param timeZone The client timezone
     */
    public static FolderShare parse(JSONObject jsonObject, TimeZone timeZone) throws JSONException, OXException {
        FolderShare folderShare = new FolderShare();
        if (jsonObject.has(FolderFields.FOLDER_ID)) {
            String tmp = jsonObject.getString(FolderFields.FOLDER_ID);
            if (tmp.startsWith(FolderObject.SHARED_PREFIX)) {
                jsonObject.put(FolderFields.FOLDER_ID, Integer.toString(FolderObject.SYSTEM_SHARED_FOLDER_ID));
            }
        }
        new FolderParser().parse(folderShare, jsonObject);
        folderShare.extendedFolderPermissions = ExtendedPermissionEntity.parse(jsonObject.optJSONArray("com.openexchange.share.extendedPermissions"), timeZone);
        return folderShare;
    }

    private static final long serialVersionUID = 4389215025150629747L;

    private List<ExtendedPermissionEntity> extendedFolderPermissions;

    /**
     * Initializes a new {@link FolderShare}.
     */
    public FolderShare() {
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
