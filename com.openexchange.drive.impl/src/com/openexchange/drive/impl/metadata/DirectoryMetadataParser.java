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

package com.openexchange.drive.impl.metadata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.drive.DriveExceptionCodes;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.DefaultFileStorageFolder;
import com.openexchange.file.storage.DefaultFileStorageGuestPermission;
import com.openexchange.file.storage.DefaultFileStoragePermission;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.FileStoragePermission;
import com.openexchange.folderstorage.Permissions;
import com.openexchange.java.Enums;
import com.openexchange.java.util.TimeZones;
import com.openexchange.share.core.tools.ShareTool;
import com.openexchange.share.recipient.RecipientType;

/**
 * {@link DirectoryMetadataParser}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class DirectoryMetadataParser {

    /**
     * Parses file metadata from the supplied JSON object.
     *
     * @param jsonObject the JSON object to parse
     * @return The parsed file
     */
    public static FileStorageFolder parse(JSONObject jsonObject) throws OXException {
        try {
            DefaultFileStorageFolder folder = new DefaultFileStorageFolder();
            if (jsonObject.has("permissions")) {
                if (jsonObject.isNull("permissions")) {
                    folder.setPermissions(Collections.<FileStoragePermission>emptyList());
                } else {
                    folder.setPermissions(parsePermission(jsonObject.getJSONArray("permissions"), TimeZones.UTC));
                }
            }
            return folder;
        } catch (OXException | JSONException e) {
            throw DriveExceptionCodes.METDATA_PARSE_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Parses the permissions from the supplied JSON array.
     *
     * @param jsonPermissions The JSON permissions
     * @param timeZone The timezone to consider
     * @return The parsed permissions
     */
    private static List<FileStoragePermission> parsePermission(JSONArray jsonPermissions, TimeZone timeZone) throws OXException, JSONException {
        List<FileStoragePermission> permissions = new ArrayList<FileStoragePermission>();
        for (int i = 0; i < jsonPermissions.length(); i++) {
            permissions.add(parsePermission(jsonPermissions.getJSONObject(i), timeZone));
        }
        return permissions;
    }

    /**
     * Parses a single permission from JSON.
     *
     * @param jsonObject The JSON object to parse
     * @param timeZone The timezone to use
     * @return The parsed permission
     */
    private static FileStoragePermission parsePermission(JSONObject jsonObject, TimeZone timeZone) throws OXException, JSONException {
        DefaultFileStoragePermission permission;
        /*
         * check for external guest permissions
         */
        RecipientType type = Enums.parse(RecipientType.class, jsonObject.optString("type"), null);
        if (null != type && (RecipientType.ANONYMOUS == type || RecipientType.GUEST == type)) {
            /*
             * parse as guest permission entity
             */
            DefaultFileStorageGuestPermission guestPermission = new DefaultFileStorageGuestPermission();
            guestPermission.setRecipient(ShareTool.parseRecipient(jsonObject, timeZone));
            permission = guestPermission;
        } else {
            /*
             * parse as already known permission entity
             */
            permission = DefaultFileStoragePermission.newInstance();
            int entity = jsonObject.optInt("entity", 0);
            if (0 >= entity) {
                throw OXException.mandatoryField("entity");
            }
            permission.setEntity(entity);
            if (false == jsonObject.has("group")) {
                throw OXException.mandatoryField("group");
            }
            permission.setGroup(jsonObject.getBoolean("group"));
        }
        /*
         * apply common properties
         */
        if (false == jsonObject.hasAndNotNull("bits")) {
            throw OXException.mandatoryField("bits");
        }
        int[] permissionBits = Permissions.parsePermissionBits(jsonObject.getInt("bits"));
        permission.setFolderPermission(permissionBits[0]);
        permission.setReadPermission(permissionBits[1]);
        permission.setWritePermission(permissionBits[2]);
        permission.setDeletePermission(permissionBits[3]);
        permission.setAdmin(permissionBits[4] > 0 ? true : false);
        return permission;
    }

}
