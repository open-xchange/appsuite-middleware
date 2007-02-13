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
import com.openexchange.groupware.container.MailFolderObject;
import com.openexchange.groupware.ldap.LdapException;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.sessiond.SessionObject;
import com.openexchange.tools.oxfolder.OXFolderException;
import com.openexchange.tools.oxfolder.OXFolderException.FolderCode;
import com.sun.mail.imap.ACL;
import com.sun.mail.imap.Rights;

/**
 * MailFolderParser
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 *
 */
public class MailFolderParser {
	
	private final SessionObject sessionObj;
	
	public MailFolderParser(SessionObject sessionObj) {
		super();
		this.sessionObj = sessionObj;
	}
	
	
	public void parse(final MailFolderObject mfo, final JSONObject jsonObj) throws OXException {
		try {
			parseMailFolder(mfo, jsonObj);
		} catch (JSONException exc) {
			throw new OXFolderException(FolderCode.JSON_ERROR, exc, exc.getMessage());
		}
	}
	
	private static final String parseFullname(final String fullname) {
		if (MailFolderObject.DEFAULT_IMAP_FOLDER.equals(fullname)) {
			return fullname;
		}
		return fullname.substring(8);
	}
	
	private void parseMailFolder(final MailFolderObject mfo, final JSONObject jsonObj) throws JSONException,
			OXException {
		if (jsonObj.has(FolderFields.TITLE)) {
			mfo.setName(jsonObj.getString(FolderFields.TITLE));
		}
		if (jsonObj.has(FolderFields.FOLDER_ID)) {
			mfo.setParentFullName(parseFullname(jsonObj.getString(FolderFields.FOLDER_ID)));
		}
		if (jsonObj.has(FolderFields.MODULE)
				&& !jsonObj.getString(FolderFields.MODULE).equalsIgnoreCase(Folder.MODULE_MAIL)) {
			throw new OXFolderException(FolderCode.MISSING_PARAMETER, FolderFields.MODULE);
		}
		if (jsonObj.has("permissions") && jsonObj.getJSONArray("permissions").length() > 0) {
			try {
				final JSONArray perms = jsonObj.getJSONArray("permissions");
				boolean applyACLs = false;
				ACL[] acls = new ACL[perms.length()];
				UserStorage us = UserStorage.getInstance(sessionObj.getContext());
				final int size = perms.length();
				for (int i = 0; i < size; i++) {
					final JSONObject jsonPerms = perms.getJSONObject(i);
					if (!jsonPerms.has("entity")) {
						throw new OXFolderException(FolderCode.MISSING_PARAMETER, FolderFields.ENTITY);
					}
					if (!jsonPerms.has("rights")) {
						throw new OXFolderException(FolderCode.MISSING_PARAMETER, FolderFields.RIGHTS);
					}
					if (jsonPerms.getString("rights").length() > 0) {
						applyACLs = true;
						final Rights entityRights = new Rights(jsonPerms.getString("rights"));
						int entity = -1;
						try {
							entity = jsonPerms.getInt("entity");
						} catch (JSONException e) {
							try {
								final String entityStr = jsonPerms.getString(FolderFields.ENTITY);
								entity = us.getUserId(entityStr);
							} catch (LdapException e1) {
								throw new OXException(e1);
							}
						}
						String entityIMAPLogin = us.getUser(entity).getMail();
						int pos = -1;
						if ((pos = entityIMAPLogin.indexOf('@')) > -1) {
							entityIMAPLogin = entityIMAPLogin.substring(0, pos);
						}
						acls[i] = new ACL(entityIMAPLogin, entityRights);
					}
				}
				us = null;
				/*
				 * If at least one valid ACL found
				 */
				if (applyACLs) {
					mfo.setACL(acls);
				}
			} catch (LdapException e2) {
				throw new OXException(e2);
			}
		}
	}
}
