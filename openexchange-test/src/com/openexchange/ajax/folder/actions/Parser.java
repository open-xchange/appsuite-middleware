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

package com.openexchange.ajax.folder.actions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.fields.FolderFields;
import com.openexchange.ajax.parser.FolderParser;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.Permissions;
import com.openexchange.groupware.container.FolderChildObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.tools.oxfolder.OXFolderExceptionCode;

/**
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class Parser {

    /**
     * Logger.
     */
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Parser.class);

    /**
     * Prevent instanciation.
     */
    private Parser() {
        super();
    }

    public static void parse(final Object value, final int column, final FolderObject folder) throws OXException {
        switch (column) {
        case FolderObject.OBJECT_ID:
            if (value instanceof Integer) {
                folder.setObjectID(((Number) value).intValue());
            } else if (value instanceof String) {
                String valueS = (String) value;
                try {
                    folder.setObjectID(Integer.parseInt(valueS));
                } catch (NumberFormatException e) {
                    folder.setFullName((String) value);
                }
            }
            break;
        case FolderObject.MODULE:
            folder.setModule(FolderParser.getModuleFromString((String) value, folder.containsObjectID() ? folder.getObjectID() : -1));
            break;
        case FolderObject.TYPE:
            folder.setType(((Integer) value).intValue());
            break;
        case FolderObject.FOLDER_NAME:
            folder.setFolderName((String) value);
            break;
        case FolderObject.SUBFOLDERS:
            folder.setSubfolderFlag(((Boolean) value).booleanValue());
            break;
        case FolderObject.STANDARD_FOLDER:
            if (null != value && JSONObject.NULL != value) {
                folder.setDefaultFolder(((Boolean) value).booleanValue());
            } else {
                folder.setDefaultFolder(false);
            }
            break;
        case FolderObject.CREATED_BY:
            if (null != value && JSONObject.NULL != value) {
                folder.setCreatedBy(((Number) value).intValue());
            }
            break;
        case FolderChildObject.FOLDER_ID:
            if (null != value && JSONObject.NULL != value) {
                if (value instanceof Integer) {
                    folder.setParentFolderID(((Number) value).intValue());
                } else if (value instanceof String) {
                    try {
                        folder.setParentFolderID(Integer.valueOf((String) value).intValue());
                    } catch (NumberFormatException e) {
                        // just ignore, mail folders have no integer as id
                    }
                }
            }
            break;
        case FolderObject.PERMISSIONS_BITS:
            if (null != value && JSONObject.NULL != value) {
                JSONArray permissionsAsJSON = (JSONArray) value;
                final int numberOfPermissions = permissionsAsJSON.length();
                final OCLPermission[] perms = new OCLPermission[numberOfPermissions];
                for (int i = 0; i < numberOfPermissions; i++) {
                    try {
                        JSONObject elem = permissionsAsJSON.getJSONObject(i);
                        if (!elem.has(FolderFields.ENTITY)) {
                            throw OXFolderExceptionCode.MISSING_PARAMETER.create(FolderFields.ENTITY);
                        }
                        int entity = elem.getInt(FolderFields.ENTITY);
                        final OCLPermission oclPerm = new OCLPermission();
                        oclPerm.setEntity(entity);
                        oclPerm.setFuid(folder.getObjectID());
                        if (!elem.has(FolderFields.BITS)) {
                            throw OXFolderExceptionCode.MISSING_PARAMETER.create(FolderFields.BITS);
                        }
                        final int[] permissionBits = Permissions.parsePermissionBits(elem.getInt(FolderFields.BITS));
                        if (!oclPerm.setAllPermission(permissionBits[0], permissionBits[1], permissionBits[2],
                            permissionBits[3])) {
                            throw OXFolderExceptionCode.INVALID_PERMISSION.create(Integer.valueOf(permissionBits[0]), Integer.valueOf(permissionBits[1]),
                                Integer.valueOf(permissionBits[2]), Integer.valueOf(permissionBits[3]));
                        }
                        oclPerm.setFolderAdmin(permissionBits[4] > 0 ? true : false);
                        if (!elem.has(FolderFields.GROUP)) {
                            throw OXFolderExceptionCode.MISSING_PARAMETER.create(FolderFields.GROUP);
                        }
                        oclPerm.setGroupPermission(elem.getBoolean(FolderFields.GROUP));
                        perms[i] = oclPerm;
                    } catch (JSONException e1) {
                        throw new OXException(e1);
                    }
                }
                folder.setPermissionsAsArray(perms);
            }
            break;
        case 3060: // ExtendedFolderPermissionsField
            if (null != value && JSONObject.NULL != value) {
                JSONArray jsonPermissions = (JSONArray) value;
                for (int i = 0; i < jsonPermissions.length(); i++) {


                }

                final int numberOfPermissions = jsonPermissions.length();
                final OCLPermission[] perms = new OCLPermission[numberOfPermissions];
                for (int i = 0; i < numberOfPermissions; i++) {
                    try {
                        JSONObject elem = jsonPermissions.getJSONObject(i);
                        if (!elem.has(FolderFields.ENTITY)) {
                            throw OXFolderExceptionCode.MISSING_PARAMETER.create(FolderFields.ENTITY);
                        }
                        int entity = elem.getInt(FolderFields.ENTITY);
                        final OCLPermission oclPerm = new OCLPermission();
                        oclPerm.setEntity(entity);
                        oclPerm.setFuid(folder.getObjectID());
                        if (!elem.has(FolderFields.BITS)) {
                            throw OXFolderExceptionCode.MISSING_PARAMETER.create(FolderFields.BITS);
                        }
                        final int[] permissionBits = Permissions.parsePermissionBits(elem.getInt(FolderFields.BITS));
                        if (!oclPerm.setAllPermission(permissionBits[0], permissionBits[1], permissionBits[2],
                            permissionBits[3])) {
                            throw OXFolderExceptionCode.INVALID_PERMISSION.create(Integer.valueOf(permissionBits[0]), Integer.valueOf(permissionBits[1]),
                                Integer.valueOf(permissionBits[2]), Integer.valueOf(permissionBits[3]));
                        }
                        oclPerm.setFolderAdmin(permissionBits[4] > 0 ? true : false);
                        if (!elem.has(FolderFields.GROUP)) {
                            throw OXFolderExceptionCode.MISSING_PARAMETER.create(FolderFields.GROUP);
                        }
                        oclPerm.setGroupPermission(elem.getBoolean(FolderFields.GROUP));
                        perms[i] = oclPerm;
                    } catch (JSONException e1) {
                        throw new OXException(e1);
                    }
                }
                folder.setPermissionsAsArray(perms);
            }




            break;
        default:
            LOG.error("Can't parse column: {}", column);
        }
    }
}
