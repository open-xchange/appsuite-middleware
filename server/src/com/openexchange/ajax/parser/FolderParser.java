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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.openexchange.ajax.Folder;
import com.openexchange.ajax.fields.FolderFields;
import com.openexchange.api2.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.ldap.LdapException;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.server.OCLPermission;
import com.openexchange.tools.oxfolder.OXFolderException;
import com.openexchange.tools.oxfolder.OXFolderException.FolderCode;

/**
 * FolderParser
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public class FolderParser {

	private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(FolderParser.class);

	private final UserConfiguration userConfig;

	private static final int[] mapping = { 0, 2, 4, -1, 8 };

	private static final String STR_EMPTY = "";

	public FolderParser(UserConfiguration userConfig) {
		super();
		this.userConfig = userConfig;
	}

	public void parse(final FolderObject fo, final JSONObject jsonObj) throws OXException {
		try {
			parseElementFolder(fo, jsonObj);
		} catch (JSONException exc) {
			throw new OXFolderException(FolderCode.JSON_ERROR, exc, exc.getMessage());
		}
	}

	public static int getModuleFromString(final String moduleStr, final int objectId) throws OXException {
		if (moduleStr.equalsIgnoreCase(Folder.MODULE_TASK)) {
			return FolderObject.TASK;
		} else if (moduleStr.equalsIgnoreCase(Folder.MODULE_CALENDAR)) {
			return FolderObject.CALENDAR;
		} else if (moduleStr.equalsIgnoreCase(Folder.MODULE_CONTACT)) {
			return FolderObject.CONTACT;
		} else if (moduleStr.equalsIgnoreCase(Folder.MODULE_UNBOUND)) {
			return FolderObject.UNBOUND;
		} else if (moduleStr.equalsIgnoreCase(Folder.MODULE_MAIL)) {
			return FolderObject.MAIL;
		} else if (moduleStr.equalsIgnoreCase(Folder.MODULE_PROJECT)) {
			return FolderObject.PROJECT;
		} else if (moduleStr.equalsIgnoreCase(Folder.MODULE_INFOSTORE)) {
			return FolderObject.INFOSTORE;
		} else if (moduleStr.equalsIgnoreCase(Folder.MODULE_SYSTEM)) {
			if (objectId == FolderObject.SYSTEM_OX_PROJECT_FOLDER_ID) {
				return FolderObject.PROJECT;
			} else if (objectId == FolderObject.SYSTEM_INFOSTORE_FOLDER_ID) {
				return FolderObject.INFOSTORE;
			} else {
				return FolderObject.SYSTEM_MODULE;
			}
		} else {
			throw new OXFolderException(FolderCode.UNKNOWN_MODULE, moduleStr, STR_EMPTY);
		}
	}

	protected void parseElementFolder(final FolderObject fo, final JSONObject jsonObj) throws OXException,
			JSONException {
		if (jsonObj.has(FolderFields.ID)) {
			if (fo.containsObjectID() && fo.getObjectID() != jsonObj.getInt(FolderFields.ID)) {
				throw new OXFolderException(FolderCode.PARAMETER_MISMATCH, FolderFields.ID, FolderFields.ID);
			}
			if (!fo.containsObjectID()) {
				fo.setObjectID(jsonObj.getInt(FolderFields.ID));
			}
		}
		if (jsonObj.has(FolderFields.FOLDER_ID)) {
			fo.setParentFolderID(FolderObject.mapVirtualID2SystemID(jsonObj.getInt(FolderFields.FOLDER_ID)));
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
			final int arrayLength = jsonArr.length();
			OCLPermission[] perms = new OCLPermission[arrayLength];
			for (int i = 0; i < arrayLength; i++) {
				final JSONObject elem = jsonArr.getJSONObject(i);
				if (!elem.has(FolderFields.ENTITY)) {
					throw new OXFolderException(FolderCode.MISSING_PARAMETER, FolderFields.ENTITY);
				}
				int entity;
				try {
					entity = elem.getInt(FolderFields.ENTITY);
				} catch (JSONException e) {
					try {
						final String entityStr = elem.getString(FolderFields.ENTITY);
						entity = UserStorage.getInstance().getUserId(entityStr, userConfig.getContext());
					} catch (LdapException e1) {
						LOG.error(e.getMessage(), e);
						throw new OXException(e1);
					}
				}
				final OCLPermission oclPerm = new OCLPermission();
				oclPerm.setEntity(entity);
				if (fo.containsObjectID()) {
					oclPerm.setFuid(fo.getObjectID());
				}
				if (!elem.has(FolderFields.BITS)) {
					throw new OXFolderException(FolderCode.MISSING_PARAMETER, FolderFields.BITS);
				}
				final int[] permissionBits = parsePermissionBits(elem.getInt(FolderFields.BITS));
				if (!oclPerm.setAllPermission(permissionBits[0], permissionBits[1], permissionBits[2],
						permissionBits[3])) {
					throw new OXFolderException(FolderCode.INVALID_PERMISSION, Integer.valueOf(permissionBits[0]), Integer.valueOf(permissionBits[1]),
							Integer.valueOf(permissionBits[2]), Integer.valueOf(permissionBits[3]));
				}
				oclPerm.setFolderAdmin(permissionBits[4] > 0 ? true : false);
				if (!elem.has(FolderFields.GROUP)) {
					throw new OXFolderException(FolderCode.MISSING_PARAMETER, FolderFields.GROUP);
				}
				oclPerm.setGroupPermission(elem.getBoolean(FolderFields.GROUP));
				perms[i] = oclPerm;
			}
			fo.setPermissionsAsArray(perms);
		}
	}

	private static final int[] parsePermissionBits(final int bitsArg) {
		int bits = bitsArg;
		int[] retval = new int[5];
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
