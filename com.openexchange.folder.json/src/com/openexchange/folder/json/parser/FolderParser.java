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

package com.openexchange.folder.json.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.tools.JSONCoercion;
import com.openexchange.exception.OXException;
import com.openexchange.folder.json.FolderField;
import com.openexchange.folder.json.FolderFieldRegistry;
import com.openexchange.folderstorage.BasicGuestPermission;
import com.openexchange.folderstorage.BasicPermission;
import com.openexchange.folderstorage.ContentType;
import com.openexchange.folderstorage.ContentTypeDiscoveryService;
import com.openexchange.folderstorage.FolderExceptionErrorMessage;
import com.openexchange.folderstorage.FolderProperty;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.Permissions;
import com.openexchange.folderstorage.UsedForSync;
import com.openexchange.java.Enums;
import com.openexchange.java.Strings;
import com.openexchange.share.core.tools.ShareTool;
import com.openexchange.share.recipient.RecipientType;

/**
 * {@link FolderParser} - Parses a folder from JSON data.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class FolderParser {

    private final ContentTypeDiscoveryService discoveryService;

    /**
     * Initializes a new {@link FolderParser}.
     */
    public FolderParser(ContentTypeDiscoveryService discoveryService) {
        super();
        this.discoveryService = discoveryService;
    }

    /**
     * Parses a folder from given JSON object.
     *
     * @param folderJsonObject The JSON object containing folder data
     * @param timeZone The timezone to use
     * @return The parsed folder
     * @throws OXException If parsing folder fails
     */
    public ParsedFolder parseFolder(final JSONObject folderJsonObject, TimeZone timeZone) throws OXException {
        try {
            final ParsedFolder folder = new ParsedFolder();

            if (folderJsonObject.hasAndNotNull(FolderField.ID.getName())) {
                folder.setID(folderJsonObject.getString(FolderField.ID.getName()));
            }

            if (folderJsonObject.hasAndNotNull(FolderField.FOLDER_ID.getName())) {
                folder.setParentID(folderJsonObject.getString(FolderField.FOLDER_ID.getName()));
            }

            if (folderJsonObject.hasAndNotNull(FolderField.FOLDER_NAME.getName())) {
                folder.setName(folderJsonObject.getString(FolderField.FOLDER_NAME.getName()).trim());
            }

            if (folderJsonObject.hasAndNotNull(FolderField.MODULE.getName())) {
                try {
                    final String contentTypeString = folderJsonObject.getString(FolderField.MODULE.getName());
                    final ContentType contentType = discoveryService.getByString(contentTypeString);
                    if (null == contentType) {
                        throw FolderExceptionErrorMessage.UNKNOWN_CONTENT_TYPE.create(contentTypeString);
                    }
                    folder.setContentType(contentType);
                } catch (OXException e) {
                    throw e;
                }
            }

            if (folderJsonObject.hasAndNotNull(FolderField.TYPE.getName())) {
                // TODO: Discovery service for types
            }

            if (folderJsonObject.hasAndNotNull(FolderField.SUBSCRIBED.getName())) {
                try {
                    folder.setSubscribed(folderJsonObject.getInt(FolderField.SUBSCRIBED.getName()) > 0);
                } catch (JSONException e) {
                    /*
                     * Not an integer value
                     */
                    folder.setSubscribed(folderJsonObject.getBoolean(FolderField.SUBSCRIBED.getName()));
                }
            }
            
            if (folderJsonObject.hasAndNotNull(FolderField.USED_FOR_SYNC.getName())) {
                JSONObject usedForSyncJSON = folderJsonObject.getJSONObject((FolderField.USED_FOR_SYNC.getName()));
                folder.setUsedForSync(UsedForSync.of(usedForSyncJSON.getBoolean("value")));
            }

            if (folderJsonObject.hasAndNotNull(FolderField.SUBFOLDERS.getName())) {
                // TODO: Support for this?
            }

            if (folderJsonObject.hasAndNotNull(FolderField.STANDARD_FOLDER.getName())) {
                // TODO: Support for this?
            }

            if (folderJsonObject.hasAndNotNull(FolderField.STANDARD_FOLDER_TYPE.getName())) {
                // TODO: Support for this?
            }

            if (folderJsonObject.hasAndNotNull(FolderField.PERMISSIONS_BITS.getName())) {
                final JSONArray jsonArr = folderJsonObject.getJSONArray(FolderField.PERMISSIONS_BITS.getName());
                folder.setPermissions(parsePermission(jsonArr, timeZone).toArray(new Permission[0]));
            }

            if (folderJsonObject.hasAndNotNull(FolderField.TOTAL.getName())) {
                int total = folderJsonObject.getInt(FolderField.TOTAL.getName());
                folder.setTotal(total);
            }

            final String metaName = FolderField.META.getName();
            if (folderJsonObject.has(metaName)) {
                if (folderJsonObject.isNull(metaName)) {
                    folder.setMeta(Collections.<String, Object> emptyMap());
                } else {
                    folder.setMeta((Map<String, Object>)JSONCoercion.coerceToNative(folderJsonObject.getJSONObject(metaName)));
                }
            }

            folder.setProperties(parseProperties(folderJsonObject));

            return folder;
        } catch (JSONException e) {
            throw FolderExceptionErrorMessage.JSON_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Parses permissions from given JSON array.
     *
     * @param permissionsAsJSON The JSON array containing permissions data
     * @param timeZone The timezone to use
     * @return The parsed permissions
     * @throws OXException If parsing permissions fails
     */
    public static List<Permission> parsePermission(final JSONArray permissionsAsJSON, TimeZone timeZone) throws OXException {
        try {
            final int numberOfPermissions = permissionsAsJSON.length();
            final List<Permission> perms = new ArrayList<Permission>(numberOfPermissions);
            for (int i = 0; i < numberOfPermissions; i++) {
                perms.add(parsePermission(permissionsAsJSON.getJSONObject(i), timeZone));
            }
            return perms;
        } catch (JSONException e) {
            throw FolderExceptionErrorMessage.JSON_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Parses a single permission from JSON.
     *
     * @param jsonObject The JSON object to parse
     * @param timeZone The timezone to use
     * @return The parsed permission
     */
    private static Permission parsePermission(JSONObject jsonObject, TimeZone timeZone) throws OXException, JSONException {
        Permission permission;
        /*
         * check for external guest permissions
         */
        RecipientType type = Enums.parse(RecipientType.class, jsonObject.optString("type"), null);
        if (null != type && (RecipientType.ANONYMOUS == type || RecipientType.GUEST == type)) {
            /*
             * parse as guest permission entity
             */
            BasicGuestPermission parsedGuestPermission = new BasicGuestPermission();
            parsedGuestPermission.setRecipient(ShareTool.parseRecipient(jsonObject, timeZone));
            permission = parsedGuestPermission;
        } else {
            /*
             * parse as already known permission entity
             */
            Permission parsedPermission = new BasicPermission();
            if (jsonObject.has(FolderField.ENTITY.getName())) {
                parsedPermission.setEntity(jsonObject.getInt(FolderField.ENTITY.getName()));
            }
            if (jsonObject.has(FolderField.IDENTIFIER.getName())) {
                parsedPermission.setIdentifier(jsonObject.getString(FolderField.IDENTIFIER.getName()));
            }
            if (jsonObject.has(FolderField.GROUP.getName())) {
                parsedPermission.setGroup(jsonObject.getBoolean(FolderField.GROUP.getName()));
            } else if (null != type) {
                parsedPermission.setGroup(RecipientType.GROUP == type);
            } else {
                throw FolderExceptionErrorMessage.MISSING_PARAMETER.create(FolderField.GROUP.getName());
            }
            /*
             * ensure the targeted entity can be identified properly
             */
            if ((0 > parsedPermission.getEntity() || 0 == parsedPermission.getEntity() && false == parsedPermission.isGroup()) && Strings.isEmpty(parsedPermission.getIdentifier())) {
                throw FolderExceptionErrorMessage.MISSING_PARAMETER.create(FolderField.ENTITY.getName());
            }
            permission = parsedPermission;
        }
        /*
         * apply common properties
         */
        if (false == jsonObject.hasAndNotNull(FolderField.BITS.getName())) {
            throw FolderExceptionErrorMessage.MISSING_PARAMETER.create(FolderField.BITS.getName());
        }
        int[] permissionBits = Permissions.parsePermissionBits(jsonObject.getInt(FolderField.BITS.getName()));
        permission.setFolderPermission(permissionBits[0]);
        permission.setReadPermission(permissionBits[1]);
        permission.setWritePermission(permissionBits[2]);
        permission.setDeletePermission(permissionBits[3]);
        permission.setAdmin(permissionBits[4] > 0 ? true : false);
        return permission;
    }

    /**
     * Parses arbitrary folder field properties from the supplied JSON object. Any registered folder field (as supplied via the
     * {@link FolderFieldRegistry}) is considered.
     *
     * @param folderJsonObject The JSON representation of the folder as sent by the client
     * @return A map of parsed folder properties, or <code>null</code> if no folder field properties were parsed
     */
    private static Map<com.openexchange.folderstorage.FolderField, FolderProperty> parseProperties(JSONObject folderJsonObject) {
        Map<com.openexchange.folderstorage.FolderField, FolderProperty> properties = new HashMap<com.openexchange.folderstorage.FolderField, FolderProperty>();
        for (com.openexchange.folderstorage.FolderField field : FolderFieldRegistry.getInstance().getPairs()) {
            if (folderJsonObject.has(field.getName())) {
                properties.put(field, field.parse(folderJsonObject.opt(field.getName())));
            }
        }
        return properties.isEmpty() ? null : properties;
    }

}
