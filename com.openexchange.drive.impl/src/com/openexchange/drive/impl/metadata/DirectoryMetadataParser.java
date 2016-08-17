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
