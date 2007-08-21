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

package com.openexchange.mail.dataobjects.parser;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.openexchange.ajax.Folder;
import com.openexchange.ajax.fields.FolderFields;
import com.openexchange.groupware.ldap.LdapException;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.mail.MailException;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.server.IMAPPermission;
import com.openexchange.server.OCLPermission;
import com.openexchange.sessiond.SessionObject;

/**
 * MailFolderParser
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class MailFolderParser {

	/**
	 * Prevent instantiation
	 */
	private MailFolderParser() {
		super();
	}

	public static MailFolder parseJSONObject(final JSONObject jsonObj, final SessionObject session)
			throws MailException {
		try {
			final MailFolder retval = new MailFolder();
			if (jsonObj.has(FolderFields.TITLE)) {
				retval.setName(jsonObj.getString(FolderFields.TITLE));
			}
			if (jsonObj.has(FolderFields.FOLDER_ID)) {
				retval.setParentFullname(jsonObj.getString(FolderFields.FOLDER_ID));
			}
			if (jsonObj.has(FolderFields.MODULE)
					&& !jsonObj.getString(FolderFields.MODULE).equalsIgnoreCase(Folder.MODULE_MAIL)) {

				throw new MailException(MailException.Code.MISSING_PARAMETER, FolderFields.MODULE);
			}
			if (jsonObj.has(FolderFields.SUBSCRIBED) && !jsonObj.isNull(FolderFields.SUBSCRIBED)) {
				retval.setSubscribed(jsonObj.getInt(FolderFields.SUBSCRIBED) > 0);
			}
			if (jsonObj.has(FolderFields.PERMISSIONS) && !jsonObj.isNull(FolderFields.PERMISSIONS)) {
				final JSONArray jsonArr = jsonObj.getJSONArray(FolderFields.PERMISSIONS);
				if (jsonArr.length() > 0) {
					final int arrayLength = jsonArr.length();
					final List<IMAPPermission> iPerms = new ArrayList<IMAPPermission>(arrayLength);
					final UserStorage us = UserStorage.getInstance(session.getContext());
					for (int i = 0; i < arrayLength; i++) {
						final JSONObject elem = jsonArr.getJSONObject(i);
						if (!elem.has(FolderFields.ENTITY)) {
							throw new MailException(MailException.Code.MISSING_PARAMETER, FolderFields.ENTITY);
						}
						int entity;
						try {
							entity = elem.getInt(FolderFields.ENTITY);
						} catch (final JSONException e) {
							final String entityStr = elem.getString(FolderFields.ENTITY);
							entity = us.getUserId(entityStr);
						}
						final IMAPPermission imapPerm = new IMAPPermission(session.getUserObject().getId(), us);
						imapPerm.setEntity(entity);
						if (jsonObj.has(FolderFields.ID) && !jsonObj.isNull(FolderFields.ID)) {
							imapPerm.setFolderFullname(jsonObj.getString(FolderFields.ID));
						}
						if (!elem.has(FolderFields.BITS)) {
							throw new MailException(MailException.Code.MISSING_PARAMETER, FolderFields.BITS);
						}
						final int[] permissionBits = parsePermissionBits(elem.getInt(FolderFields.BITS));
						if (!imapPerm.setAllPermission(permissionBits[0], permissionBits[1], permissionBits[2],
								permissionBits[3])) {
							throw new MailException(MailException.Code.INVALID_PERMISSION, Integer
									.valueOf(permissionBits[0]), Integer.valueOf(permissionBits[1]), Integer
									.valueOf(permissionBits[2]), Integer.valueOf(permissionBits[3]));
						}
						imapPerm.setFolderAdmin(permissionBits[4] > 0 ? true : false);
						if (!elem.has(FolderFields.GROUP)) {
							throw new MailException(MailException.Code.MISSING_PARAMETER, FolderFields.GROUP);
						}
						imapPerm.setGroupPermission(elem.getBoolean(FolderFields.GROUP));
						iPerms.add(imapPerm);
					}
					retval.addPermissions(iPerms.toArray(new IMAPPermission[iPerms.size()]));
				}
			}
			return retval;
		} catch (final JSONException e) {
			throw new MailException(MailException.Code.JSON_ERROR, e, e.getLocalizedMessage());
		} catch (final LdapException e) {
			throw new MailException(e);
		}
	}

	private static final int[] mapping = { 0, 2, 4, -1, 8 };

	private static int[] parsePermissionBits(final int bitsArg) {
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
