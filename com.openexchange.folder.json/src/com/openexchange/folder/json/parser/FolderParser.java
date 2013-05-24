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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.folder.json.FolderField;
import com.openexchange.folderstorage.ContentType;
import com.openexchange.folderstorage.ContentTypeDiscoveryService;
import com.openexchange.folderstorage.Folder;
import com.openexchange.folderstorage.FolderExceptionErrorMessage;
import com.openexchange.folderstorage.Permission;

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
     * @return The parsed folder
     * @throws OXException If parsing folder fails
     */
    public Folder parseFolder(final JSONObject folderJsonObject) throws OXException {
        try {
            final ParsedFolder folder = new ParsedFolder();

            if (folderJsonObject.hasAndNotNull(FolderField.ID.getName())) {
                folder.setID(folderJsonObject.getString(FolderField.ID.getName()));
            }

            if (folderJsonObject.hasAndNotNull(FolderField.FOLDER_ID.getName())) {
                folder.setParentID(folderJsonObject.getString(FolderField.FOLDER_ID.getName()));
            }

            if (folderJsonObject.hasAndNotNull(FolderField.FOLDER_NAME.getName())) {
                folder.setName(folderJsonObject.getString(FolderField.FOLDER_NAME.getName()));
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
                final Permission[] permissions = parsePermission(jsonArr);
                folder.setPermissions(permissions);
            }

            if (folderJsonObject.hasAndNotNull(FolderField.TOTAL.getName())) {
                int total = folderJsonObject.getInt(FolderField.TOTAL.getName());
                folder.setTotal(total);
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
     * @return The parsed permissions
     * @throws OXException If parsing permissions fails
     */
    public static Permission[] parsePermission(final JSONArray permissionsAsJSON) throws OXException {
        try {
            final int numberOfPermissions = permissionsAsJSON.length();
            final Permission[] perms = new Permission[numberOfPermissions];
            for (int i = 0; i < numberOfPermissions; i++) {
                final JSONObject elem = permissionsAsJSON.getJSONObject(i);

                if (!elem.hasAndNotNull(FolderField.ENTITY.getName())) {
                    throw FolderExceptionErrorMessage.MISSING_PARAMETER.create(FolderField.ENTITY.getName());
                }
                final int entity = elem.getInt(FolderField.ENTITY.getName());

                final ParsedPermission oclPerm = new ParsedPermission();
                oclPerm.setEntity(entity);
                if (!elem.has(FolderField.BITS.getName())) {
                    throw FolderExceptionErrorMessage.MISSING_PARAMETER.create(FolderField.BITS.getName());
                }
                final int[] permissionBits = parsePermissionBits(elem.getInt(FolderField.BITS.getName()));
                oclPerm.setFolderPermission(permissionBits[0]);
                oclPerm.setReadPermission(permissionBits[1]);
                oclPerm.setWritePermission(permissionBits[2]);
                oclPerm.setDeletePermission(permissionBits[3]);

                oclPerm.setAdmin(permissionBits[4] > 0 ? true : false);

                if (!elem.has(FolderField.GROUP.getName())) {
                    throw FolderExceptionErrorMessage.MISSING_PARAMETER.create(FolderField.GROUP.getName());
                }
                oclPerm.setGroup(elem.getBoolean(FolderField.GROUP.getName()));

                perms[i] = oclPerm;
            }
            return perms;
        } catch (final JSONException e) {
            throw FolderExceptionErrorMessage.JSON_ERROR.create(e, e.getMessage());
        }
    }

    private static final int[] mapping = { 0, 2, 4, -1, 8 };

    /**
     * The actual max permission that can be transfered in field 'bits' or JSON's permission object
     */
    private static final int MAX_PERMISSION = 64;

    private static final int[] parsePermissionBits(final int bitsArg) {
        int bits = bitsArg;
        final int[] retval = new int[5];
        for (int i = retval.length - 1; i >= 0; i--) {
            final int shiftVal = (i * 7); // Number of bits to be shifted
            retval[i] = bits >> shiftVal;
            bits -= (retval[i] << shiftVal);
            if (retval[i] == MAX_PERMISSION) {
                retval[i] = Permission.MAX_PERMISSION;
            } else if (i < (retval.length - 1)) {
                retval[i] = mapping[retval[i]];
            } else {
                retval[i] = retval[i];
            }
        }
        return retval;
    }

}
