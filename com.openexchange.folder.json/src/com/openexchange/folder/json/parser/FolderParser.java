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

package com.openexchange.folder.json.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.tools.JSONCoercion;
import com.openexchange.exception.OXException;
import com.openexchange.folder.json.FolderField;
import com.openexchange.folderstorage.ContentType;
import com.openexchange.folderstorage.ContentTypeDiscoveryService;
import com.openexchange.folderstorage.FolderExceptionErrorMessage;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.Permissions;
import com.openexchange.java.Enums;
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
                } catch (final OXException e) {
                    throw e;
                }
            }

            if (folderJsonObject.hasAndNotNull(FolderField.TYPE.getName())) {
                // TODO: Discovery service for types
            }

            if (folderJsonObject.hasAndNotNull(FolderField.SUBSCRIBED.getName())) {
                try {
                    folder.setSubscribed(folderJsonObject.getInt(FolderField.SUBSCRIBED.getName()) > 0);
                } catch (final JSONException e) {
                    /*
                     * Not an integer value
                     */
                    folder.setSubscribed(folderJsonObject.getBoolean(FolderField.SUBSCRIBED.getName()));
                }
            } else {
                // If not present consider as subscribed
                folder.setSubscribed(true);
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

            return folder;
        } catch (final JSONException e) {
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
        } catch (final JSONException e) {
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
            ParsedGuestPermission parsedGuestPermission = new ParsedGuestPermission();
            parsedGuestPermission.setRecipient(ShareTool.parseRecipient(jsonObject, timeZone));
            permission = parsedGuestPermission;
        } else {
            /*
             * parse as already known permission entity
             */
            ParsedPermission parsedPermission = new ParsedPermission();
            if (false == jsonObject.has(FolderField.ENTITY.getName())) {
                throw FolderExceptionErrorMessage.MISSING_PARAMETER.create(FolderField.ENTITY.getName());
            }
            parsedPermission.setEntity(jsonObject.getInt(FolderField.ENTITY.getName()));
            if (jsonObject.has(FolderField.GROUP.getName())) {
                parsedPermission.setGroup(jsonObject.getBoolean(FolderField.GROUP.getName()));
            } else if (null != type) {
                parsedPermission.setGroup(RecipientType.GROUP == type);
            } else {
                throw FolderExceptionErrorMessage.MISSING_PARAMETER.create(FolderField.GROUP.getName());
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

}
