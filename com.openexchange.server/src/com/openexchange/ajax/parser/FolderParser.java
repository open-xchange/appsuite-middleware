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

package com.openexchange.ajax.parser;

import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.Folder;
import com.openexchange.ajax.fields.DataFields;
import com.openexchange.ajax.fields.FolderChildFields;
import com.openexchange.ajax.fields.FolderFields;
import com.openexchange.ajax.tools.JSONCoercion;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.composition.FolderID;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.groupware.userconfiguration.UserPermissionBits;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.tools.oxfolder.OXFolderExceptionCode;

/**
 * FolderParser
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class FolderParser {

    private final UserPermissionBits userPermissionBits;

    private static final int[] mapping = { 0, 2, 4, -1, 8 };

    private static final String STR_EMPTY = "";

    public FolderParser(final UserConfiguration userConfig) {
        super();
        this.userPermissionBits = userConfig.getUserPermissionBits();
    }

    public FolderParser(final UserPermissionBits userPermissionBits) {
        super();
        this.userPermissionBits = userPermissionBits;
    }

    public FolderParser() {
        this((UserPermissionBits) null);
    }

    public void parse(final FolderObject fo, final JSONObject jsonObj) throws OXException {
        try {
            parseElementFolder(fo, jsonObj);
        } catch (final JSONException exc) {
            throw OXFolderExceptionCode.JSON_ERROR.create(exc, exc.getMessage());
        }
    }

    public static int getModuleFromString(final String moduleStr, final int objectId) throws OXException {
        if (moduleStr.equalsIgnoreCase(AJAXServlet.MODULE_TASK)) {
            return FolderObject.TASK;
        } else if (moduleStr.equalsIgnoreCase(AJAXServlet.MODULE_CALENDAR)) {
            return FolderObject.CALENDAR;
        } else if (moduleStr.equalsIgnoreCase(AJAXServlet.MODULE_CONTACT)) {
            return FolderObject.CONTACT;
        } else if (moduleStr.equalsIgnoreCase(AJAXServlet.MODULE_UNBOUND)) {
            return FolderObject.UNBOUND;
        } else if (moduleStr.equalsIgnoreCase(AJAXServlet.MODULE_MAIL)) {
            return FolderObject.MAIL;
        } else if (moduleStr.equalsIgnoreCase(AJAXServlet.MODULE_INFOSTORE)) {
            return FolderObject.INFOSTORE;
        } else if (moduleStr.equals(AJAXServlet.MODULE_MESSAGING)) {
            return FolderObject.MESSAGING;
        } else if (moduleStr.equalsIgnoreCase(AJAXServlet.MODULE_SYSTEM)) {
            if (objectId == FolderObject.SYSTEM_INFOSTORE_FOLDER_ID) {
                return FolderObject.INFOSTORE;
            } else {
                return FolderObject.SYSTEM_MODULE;
            }
        } else {
            throw OXFolderExceptionCode.UNKNOWN_MODULE.create(moduleStr, STR_EMPTY);
        }
    }

    protected void parseElementFolder(final FolderObject fo, final JSONObject jsonObj) throws OXException,
    JSONException {
        if (jsonObj.has(DataFields.ID)) {
            if (fo.containsObjectID() && fo.getObjectID() != jsonObj.getInt(DataFields.ID)) {
                throw OXFolderExceptionCode.PARAMETER_MISMATCH.create(DataFields.ID, DataFields.ID);
            }
            if (!fo.containsObjectID()) {
                fo.setObjectID(jsonObj.getInt(DataFields.ID));
            }
        }
        if (jsonObj.has(FolderChildFields.FOLDER_ID)) {
            Object folderId = jsonObj.get(FolderChildFields.FOLDER_ID);
            if (folderId instanceof String) {
                fo.setFullName((String) folderId);
            } else {
                fo.setParentFolderID(jsonObj.getInt(FolderChildFields.FOLDER_ID));
            }
        }
        if (jsonObj.has(FolderFields.TITLE)) {
            fo.setFolderName(jsonObj.getString(FolderFields.TITLE));
        }
        if (jsonObj.has(FolderFields.MODULE)) {
            fo.setModule(getModuleFromString(jsonObj.getString(FolderFields.MODULE), fo.containsObjectID() ? fo
                .getObjectID() : -1));
        }
        if (jsonObj.has(FolderFields.TYPE)) {
            fo.setType(jsonObj.getInt(FolderFields.TYPE));
        }
        if (jsonObj.has(FolderFields.SUBFOLDERS)) {
            fo.setSubfolderFlag(jsonObj.getBoolean(FolderFields.SUBFOLDERS));
        }
        if (jsonObj.has(FolderFields.STANDARD_FOLDER)) {
            fo.setDefaultFolder(jsonObj.getBoolean(FolderFields.STANDARD_FOLDER));
        }
        if (jsonObj.has(FolderFields.PERMISSIONS) && !jsonObj.isNull(FolderFields.PERMISSIONS)) {
            final JSONArray jsonArr = jsonObj.getJSONArray(FolderFields.PERMISSIONS);
            final OCLPermission[] perms = parseOCLPermission(jsonArr, fo.containsObjectID() ? Integer.valueOf(fo.getObjectID()) : null);
            fo.setPermissionsAsArray(perms);
        }
        if (jsonObj.has(FolderFields.META)) {
            if (jsonObj.isNull(FolderFields.META)) {
                fo.setMeta(null);
            } else {
                fo.setMeta((Map<String, Object>)JSONCoercion.coerceToNative(jsonObj.getJSONObject("meta")));
            }
        }
    }

    public OCLPermission[] parseOCLPermission(final JSONArray permissionsAsJSON, final Integer objectID) throws JSONException, OXException {
        final int numberOfPermissions = permissionsAsJSON.length();
        final OCLPermission[] perms = new OCLPermission[numberOfPermissions];
        for (int i = 0; i < numberOfPermissions; i++) {
            final JSONObject elem = permissionsAsJSON.getJSONObject(i);
            if (!elem.has(FolderFields.ENTITY)) {
                throw OXFolderExceptionCode.MISSING_PARAMETER.create(FolderFields.ENTITY);
            }
            int entity;
            try {
                entity = elem.getInt(FolderFields.ENTITY);
            } catch (final JSONException e) {
                if (null == userPermissionBits) {
                    throw e;
                }
                final String entityStr = elem.getString(FolderFields.ENTITY);
                entity = UserStorage.getInstance().getUserId(entityStr, userPermissionBits.getContext());
            }
            final OCLPermission oclPerm = new OCLPermission();
            oclPerm.setEntity(entity);
            if (objectID != null) {
                oclPerm.setFuid(objectID.intValue());
            }
            if (!elem.has(FolderFields.BITS)) {
                throw OXFolderExceptionCode.MISSING_PARAMETER.create(FolderFields.BITS);
            }
            final int[] permissionBits = parsePermissionBits(elem.getInt(FolderFields.BITS));
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
        }
        return perms;
    }

    private static final int[] parsePermissionBits(final int bitsArg) {
        int bits = bitsArg;
        final int[] retval = new int[5];
        for (int i = retval.length - 1; i >= 0; i--) {
            final int shiftVal = (i * 7); // Number of bits to be shifted
            retval[i] = bits >> shiftVal;
            bits -= (retval[i] << shiftVal);
            if (retval[i] == Folder.MAX_PERMISSION) {
                retval[i] = OCLPermission.ADMIN_PERMISSION;
            } else if (i < (retval.length - 1)) {
                retval[i] = mapping[retval[i]];
            } else {
                retval[i] = retval[i];
            }
        }
        return retval;
    }
}
