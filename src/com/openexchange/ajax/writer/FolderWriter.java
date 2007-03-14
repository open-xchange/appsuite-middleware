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

package com.openexchange.ajax.writer;

import static com.openexchange.tools.oxfolder.OXFolderManagerImpl.getFolderName;

import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.TimeZone;

import javax.mail.MessagingException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONWriter;

import com.openexchange.ajax.Folder;
import com.openexchange.ajax.fields.FolderFields;
import com.openexchange.api2.OXException;
import com.openexchange.cache.FolderCacheManager;
import com.openexchange.groupware.UserConfiguration;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.MailFolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.imap.IMAPProperties;
import com.openexchange.groupware.ldap.User;
import com.openexchange.server.DBPoolingException;
import com.openexchange.server.OCLPermission;
import com.openexchange.tools.iterator.SearchIteratorException;
import com.openexchange.tools.oxfolder.OXFolderException;
import com.openexchange.tools.oxfolder.OXFolderException.FolderCode;

/**
 * FolderWriter
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public class FolderWriter extends DataWriter {

	private static final int[] mapping = { 0, -1, 1, -1, 2, -1, -1, -1, 4 };
	
	private static final String STR_EMPTY = "";
	
	private static final String STR_INBOX = "INBOX";

	private final User userObj;

	private final UserConfiguration userConfig;

	private final Context ctx;

	public static abstract class IMAPFolderFieldWriter {
		public void writeField(final JSONWriter jsonwriter, final MailFolderObject folder, final boolean withKey)
				throws JSONException, OXException, MessagingException {
			writeField(jsonwriter, folder, withKey, null, -1);
		}

		public void writeField(final JSONWriter jsonwriter, final MailFolderObject folder, final boolean withKey,
				final String name, final int hasSubfolders) throws JSONException, OXException, MessagingException {
			writeField(jsonwriter, folder, withKey, name, hasSubfolders, null, -1);
		}

		public abstract void writeField(JSONWriter jsonwriter, MailFolderObject folder, boolean withKey, String name,
				int hasSubfolders, String fullName, int module) throws JSONException, OXException, MessagingException;
	}

	public static abstract class FolderFieldWriter {
		public void writeField(final JSONWriter jsonwriter, final FolderObject fo, final boolean withKey)
				throws JSONException, DBPoolingException, OXException, SearchIteratorException, SQLException {
			writeField(jsonwriter, fo, withKey, null, -1);
		}

		public abstract void writeField(JSONWriter jsonwriter, FolderObject fo, boolean withKey, String name,
				int hasSubfolders) throws JSONException, DBPoolingException, OXException, SearchIteratorException,
				SQLException;
	}

	public FolderWriter(PrintWriter w, User userObj, UserConfiguration userConfig, Context ctx) {
		super();
		this.jsonwriter = new JSONWriter(w);
		this.userObj = userObj;
		this.userConfig = userConfig;
		this.ctx = ctx;
	}

	public FolderWriter(JSONWriter jw, User userObj, UserConfiguration userConfig, Context ctx) {
		super();
		this.jsonwriter = jw;
		this.userObj = userObj;
		this.userConfig = userConfig;
		this.ctx = ctx;
	}

	public void writeIMAPFolderAsObject(final int[] fields, final MailFolderObject folder) throws JSONException,
			OXException {
		writeIMAPFolderAsObject(fields, folder, null, -1);
	}

	public void writeIMAPFolderAsObject(final int[] fields, final MailFolderObject folder, final String name,
			final int hasSubfolders) throws JSONException, OXException {
		writeIMAPFolderAsObject(fields, folder, name, hasSubfolders, null);
	}

	public void writeIMAPFolderAsObject(int[] fields, final MailFolderObject folder, final String name,
			final int hasSubfolders, final String fullName) throws JSONException, OXException {
		try {
			jsonwriter.object();
			if (fields == null) {
				fields = getAllFolderFields();
			}
			for (int i = 0; i < fields.length; i++) {
				writeIMAPFolderField(fields[i], folder, true, name, hasSubfolders, fullName, -1);
			}
		} finally {
			jsonwriter.endObject();
		}
	}

	public void writeIMAPFolderAsArray(final int[] fields, final MailFolderObject folder) throws JSONException,
			OXException {
		writeIMAPFolderAsArray(fields, folder, null, -1);
	}

	public void writeIMAPFolderAsArray(final int[] fields, final MailFolderObject folder, final String name,
			final int hasSubfolders) throws JSONException, OXException {
		writeIMAPFolderAsArray(fields, folder, name, hasSubfolders, null, -1);
	}

	public void writeIMAPFolderAsArray(final int[] fields, final MailFolderObject folder, final String name,
			final int hasSubfolders, final String fullName, final int module) throws JSONException, OXException {
		try {
			jsonwriter.array();
			for (int i = 0; i < fields.length; i++) {
				writeIMAPFolderField(fields[i], folder, false, name, hasSubfolders, fullName, module);
			}
		} finally {
			jsonwriter.endArray();
		}
	}

	public final IMAPFolderFieldWriter[] getIMAPFolderFieldWriter(final int[] fields) {
		IMAPFolderFieldWriter[] retval = new IMAPFolderFieldWriter[fields.length];
		for (int i = 0; i < retval.length; i++) {
			Fields: switch (fields[i]) {
			case FolderObject.OBJECT_ID:
				retval[i] = new IMAPFolderFieldWriter() {
					public void writeField(final JSONWriter jsonwriter, final MailFolderObject folder,
							final boolean withKey, final String name, final int hasSubfolders, final String fullName,
							final int module) throws JSONException, MessagingException {
						if (withKey) {
							jsonwriter.key(FolderFields.ID);
						}
						jsonwriter.value(fullName == null ? folder.getFullName() : fullName);
					}
				};
				break Fields;
			case FolderObject.CREATED_BY:
				retval[i] = new IMAPFolderFieldWriter() {
					public void writeField(final JSONWriter jsonwriter, final MailFolderObject folder,
							final boolean withKey, final String name, final int hasSubfolders, final String fullName,
							final int module) throws JSONException {
						if (withKey) {
							jsonwriter.key(FolderFields.CREATED_BY);
						}
						jsonwriter.value(-1);
					}
				};
				break Fields;
			case FolderObject.MODIFIED_BY:
				retval[i] = new IMAPFolderFieldWriter() {
					public void writeField(final JSONWriter jsonwriter, final MailFolderObject folder,
							final boolean withKey, final String name, final int hasSubfolders, final String fullName,
							final int module) throws JSONException {
						if (withKey) {
							jsonwriter.key(FolderFields.MODIFIED_BY);
						}
						jsonwriter.value(-1);
					}
				};
				break Fields;
			case FolderObject.CREATION_DATE:
				retval[i] = new IMAPFolderFieldWriter() {
					public void writeField(final JSONWriter jsonwriter, final MailFolderObject folder,
							final boolean withKey, final String name, final int hasSubfolders, final String fullName,
							final int module) throws JSONException {
						if (withKey) {
							jsonwriter.key(FolderFields.CREATION_DATE);
						}
						jsonwriter.value(0);
					}
				};
				break Fields;
			case FolderObject.LAST_MODIFIED:
				retval[i] = new IMAPFolderFieldWriter() {
					public void writeField(final JSONWriter jsonwriter, final MailFolderObject folder,
							final boolean withKey, final String name, final int hasSubfolders, final String fullName,
							final int module) throws JSONException {
						if (withKey) {
							jsonwriter.key(FolderFields.LAST_MODIFIED);
						}
						jsonwriter.value(0);
					}
				};
				break Fields;
			case FolderObject.FOLDER_ID:
				retval[i] = new IMAPFolderFieldWriter() {
					public void writeField(final JSONWriter jsonwriter, final MailFolderObject folder,
							final boolean withKey, final String name, final int hasSubfolders, final String fullName,
							final int module) throws JSONException, MessagingException {
						if (withKey) {
							jsonwriter.key(FolderFields.FOLDER_ID);
						}
						jsonwriter.value(folder.getParentFullName());
					}
				};
				break Fields;
			case FolderObject.FOLDER_NAME:
				retval[i] = new IMAPFolderFieldWriter() {
					public void writeField(final JSONWriter jsonwriter, final MailFolderObject folder,
							final boolean withKey, final String name, final int hasSubfolders, final String fullName,
							final int module) throws JSONException {
						if (withKey) {
							jsonwriter.key(FolderFields.TITLE);
						}
						jsonwriter.value(name == null ? folder.getName() : name);
					}
				};
				break Fields;
			case FolderObject.MODULE:
				retval[i] = new IMAPFolderFieldWriter() {
					public void writeField(final JSONWriter jsonwriter, final MailFolderObject folder,
							final boolean withKey, final String name, final int hasSubfolders, final String fullName,
							final int module) throws JSONException {
						if (withKey) {
							jsonwriter.key(FolderFields.MODULE);
						}
						jsonwriter.value(getModuleString(module == -1 ? FolderObject.MAIL : module, -1));
					}
				};
				break Fields;
			case FolderObject.TYPE:
				retval[i] = new IMAPFolderFieldWriter() {
					public void writeField(final JSONWriter jsonwriter, final MailFolderObject folder,
							final boolean withKey, final String name, final int hasSubfolders, final String fullName,
							final int module) throws JSONException {
						if (withKey) {
							jsonwriter.key(FolderFields.TYPE);
						}
						jsonwriter.value(FolderObject.MAIL);
					}
				};
				break Fields;
			case FolderObject.SUBFOLDERS:
				retval[i] = new IMAPFolderFieldWriter() {
					public void writeField(final JSONWriter jsonwriter, final MailFolderObject folder,
							final boolean withKey, final String name, final int hasSubfolders, final String fullName,
							final int module) throws JSONException, OXException, MessagingException {
						if (withKey) {
							jsonwriter.key(FolderFields.SUBFOLDERS);
						}
						if (hasSubfolders == -1) {
							jsonwriter.value(folder.hasSubfolders());
							return;
						}
						jsonwriter.value(hasSubfolders > 0);
					}
				};
				break Fields;
			case FolderObject.OWN_RIGHTS:
				retval[i] = new IMAPFolderFieldWriter() {
					public void writeField(final JSONWriter jsonwriter, final MailFolderObject folder,
							final boolean withKey, final String name, final int hasSubfolders, final String fullName,
							final int module) throws JSONException, OXException, MessagingException {
						if (withKey) {
							jsonwriter.key(FolderFields.OWN_RIGHTS);
						}
						if (folder.isRootFolder()) {
							jsonwriter.value(createPermissionBits(OCLPermission.CREATE_SUB_FOLDERS,
									OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS,
									OCLPermission.NO_PERMISSIONS, false));
						} else {
							jsonwriter.value(folder.getOwnRights());
						}
					}
				};
				break Fields;
			case FolderObject.PERMISSIONS_BITS:
				retval[i] = new IMAPFolderFieldWriter() {
					public void writeField(final JSONWriter jsonwriter, final MailFolderObject folder,
							final boolean withKey, final String name, final int hasSubfolders, final String fullName,
							final int module) throws JSONException, OXException, MessagingException {
						if (withKey) {
							jsonwriter.key(FolderFields.PERMISSIONS);
						}
						jsonwriter.value(JSONObject.NULL);
						ctx.getContextId();
					}
				};
				break Fields;
			case FolderObject.SUMMARY:
				retval[i] = new IMAPFolderFieldWriter() {
					public void writeField(final JSONWriter jsonwriter, final MailFolderObject folder,
							final boolean withKey, final String name, final int hasSubfolders, final String fullName,
							final int module) throws JSONException, OXException, MessagingException {
						if (withKey) {
							jsonwriter.key(FolderFields.SUMMARY);
						}
						if (folder.isRootFolder()) {
							jsonwriter.value("");
							return;
						}
						jsonwriter.value(folder.getSummary());
					}
				};
				break Fields;
			case FolderObject.STANDARD_FOLDER:
				retval[i] = new IMAPFolderFieldWriter() {
					public void writeField(final JSONWriter jsonwriter, final MailFolderObject folder,
							final boolean withKey, final String name, final int hasSubfolders, final String fullName,
							final int module) throws JSONException, OXException, MessagingException {
						if (withKey) {
							jsonwriter.key(FolderFields.STANDARD_FOLDER);
						}
						jsonwriter.value(false);
					}
				};
				break Fields;
			case FolderObject.TOTAL:
				retval[i] = new IMAPFolderFieldWriter() {
					public void writeField(final JSONWriter jsonwriter, final MailFolderObject folder,
							final boolean withKey, final String name, final int hasSubfolders, final String fullName,
							final int module) throws JSONException, OXException, MessagingException {
						if (withKey) {
							jsonwriter.key(FolderFields.TOTAL);
						}
						jsonwriter.value(folder.getTotal());
					}
				};
				break Fields;
			case FolderObject.NEW:
				retval[i] = new IMAPFolderFieldWriter() {
					public void writeField(final JSONWriter jsonwriter, final MailFolderObject folder,
							final boolean withKey, final String name, final int hasSubfolders, final String fullName,
							final int module) throws JSONException, OXException, MessagingException {
						if (withKey) {
							jsonwriter.key(FolderFields.NEW);
						}
						jsonwriter.value(folder.getNew());
					}
				};
				break Fields;
			case FolderObject.UNREAD:
				retval[i] = new IMAPFolderFieldWriter() {
					public void writeField(final JSONWriter jsonwriter, final MailFolderObject folder,
							final boolean withKey, final String name, final int hasSubfolders, final String fullName,
							final int module) throws JSONException, OXException, MessagingException {
						if (withKey) {
							jsonwriter.key(FolderFields.UNREAD);
						}
						jsonwriter.value(folder.getUnread());
					}
				};
				break Fields;
			case FolderObject.DELETED:
				retval[i] = new IMAPFolderFieldWriter() {
					public void writeField(final JSONWriter jsonwriter, final MailFolderObject folder,
							final boolean withKey, final String name, final int hasSubfolders, final String fullName,
							final int module) throws JSONException, OXException, MessagingException {
						if (withKey) {
							jsonwriter.key(FolderFields.DELETED);
						}
						jsonwriter.value(folder.getDeleted());
					}
				};
				break Fields;
			case FolderObject.CAPABILITIES:
				retval[i] = new IMAPFolderFieldWriter() {
					public void writeField(final JSONWriter jsonwriter, final MailFolderObject folder,
							final boolean withKey, final String name, final int hasSubfolders, final String fullName,
							final int module) throws JSONException, OXException, MessagingException {
						if (withKey) {
							jsonwriter.key(FolderFields.CAPABILITIES);
						}
						if (IMAPProperties.isCapabilitiesLoaded()) {
							jsonwriter.value(IMAPProperties.getImapCapabilities().getCapabilities());
							return;
						}
						jsonwriter.value(JSONObject.NULL);
					}
				};
				break Fields;
			case FolderObject.SUBSCRIBED:
				retval[i] = new IMAPFolderFieldWriter() {
					public void writeField(final JSONWriter jsonwriter, final MailFolderObject folder,
							final boolean withKey, final String name, final int hasSubfolders, final String fullName,
							final int module) throws JSONException, OXException, MessagingException {
						if (withKey) {
							jsonwriter.key(FolderFields.SUBSCRIBED);
						}
						final String fn = fullName == null ? folder.getFullName() : fullName;
						if (fn != null && fn.endsWith(STR_INBOX)) {
							jsonwriter.value(true);
						} else {
							jsonwriter.value(folder.containsSubscribe() ? folder.isSubscribed() : JSONObject.NULL);
						}
					}
				};
				break Fields;
			default:
				retval[i] = new IMAPFolderFieldWriter() {
					public void writeField(final JSONWriter jsonwriter, final MailFolderObject folder,
							final boolean withKey, final String name, final int hasSubfolders, final String fullName,
							final int module) throws JSONException {
						if (withKey) {
							jsonwriter.key("Unknown column");
						}
						jsonwriter.value(JSONObject.NULL);
					}
				};
			}
		}
		return retval;
	}

	public void writeIMAPFolderField(final int field, final MailFolderObject folder, final boolean withKey,
			final String name, final int hasSubfolders, final String fullName, final int module) throws OXException,
			JSONException {
		switch (field) {
		case FolderObject.OBJECT_ID:
			if (withKey) {
				jsonwriter.key(FolderFields.ID);
			}
			jsonwriter.value(fullName == null ? folder.getFullName() : fullName);
			break;
		case FolderObject.CREATED_BY:
			if (withKey) {
				jsonwriter.key(FolderFields.CREATED_BY);
			}
			jsonwriter.value(-1);
			break;
		case FolderObject.MODIFIED_BY:
			if (withKey) {
				jsonwriter.key(FolderFields.MODIFIED_BY);
			}
			jsonwriter.value(-1);
			break;
		case FolderObject.CREATION_DATE:
			if (withKey) {
				jsonwriter.key(FolderFields.CREATION_DATE);
			}
			jsonwriter.value(0);
			break;
		case FolderObject.LAST_MODIFIED:
			if (withKey) {
				jsonwriter.key(FolderFields.LAST_MODIFIED);
			}
			jsonwriter.value(0);
			break;
		case FolderObject.FOLDER_ID:
			if (withKey) {
				jsonwriter.key(FolderFields.FOLDER_ID);
			}
			jsonwriter.value(folder.getParentFullName());
			break;
		case FolderObject.FOLDER_NAME:
			if (withKey) {
				jsonwriter.key(FolderFields.TITLE);
			}
			jsonwriter.value(name == null ? folder.getName() : name);
			break;
		case FolderObject.MODULE:
			if (withKey) {
				jsonwriter.key(FolderFields.MODULE);
			}
			jsonwriter.value(getModuleString(module == -1 ? FolderObject.MAIL : module, -1));
			break;
		case FolderObject.TYPE:
			if (withKey) {
				jsonwriter.key(FolderFields.TYPE);
			}
			jsonwriter.value(FolderObject.MAIL);
			break;
		case FolderObject.SUBFOLDERS:
			if (withKey) {
				jsonwriter.key(FolderFields.SUBFOLDERS);
			}
			if (hasSubfolders == -1) {
				jsonwriter.value(folder.hasSubfolders());
				break;
			}
			jsonwriter.value(hasSubfolders > 0);
			break;
		case FolderObject.OWN_RIGHTS:
			if (withKey) {
				jsonwriter.key(FolderFields.OWN_RIGHTS);
			}
			if (folder.isRootFolder()) {
				jsonwriter.value(createPermissionBits(OCLPermission.CREATE_SUB_FOLDERS, OCLPermission.NO_PERMISSIONS,
						OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS, false));
			} else {
				jsonwriter.value(folder.getOwnRights());
			}
			break;
		case FolderObject.PERMISSIONS_BITS:
			if (withKey) {
				jsonwriter.key(FolderFields.PERMISSIONS);
			}
			jsonwriter.value(JSONObject.NULL);
			ctx.getContextId();
			/*
			 * JSONArray ja = new JSONArray(); ACL[] aclArr =
			 * imapFolder.getACL(); for (int i = 0; i < aclArr.length; i++) {
			 * ACL current = aclArr[i]; JSONObject jo = new JSONObject();
			 * //TODO: Determine user/group id from given ACL name }
			 * jsonwriter.value(ja);
			 */
			break;
		case FolderObject.SUMMARY:
			if (withKey) {
				jsonwriter.key(FolderFields.SUMMARY);
			}
			if (folder.isRootFolder()) {
				jsonwriter.value("");
				break;
			}
			jsonwriter.value(folder.getSummary());
			break;
		case FolderObject.STANDARD_FOLDER:
			if (withKey) {
				jsonwriter.key(FolderFields.STANDARD_FOLDER);
			}
			jsonwriter.value(false);
			break;
		case FolderObject.TOTAL:
			if (withKey) {
				jsonwriter.key(FolderFields.TOTAL);
			}
			jsonwriter.value(folder.getTotal());
			break;
		case FolderObject.NEW:
			if (withKey) {
				jsonwriter.key(FolderFields.NEW);
			}
			jsonwriter.value(folder.getNew());
			break;
		case FolderObject.UNREAD:
			if (withKey) {
				jsonwriter.key(FolderFields.UNREAD);
			}
			jsonwriter.value(folder.getUnread());
			break;
		case FolderObject.DELETED:
			if (withKey) {
				jsonwriter.key(FolderFields.DELETED);
			}
			jsonwriter.value(folder.getDeleted());
			break;
		case FolderObject.CAPABILITIES:
			if (withKey) {
				jsonwriter.key(FolderFields.CAPABILITIES);
			}
			if (IMAPProperties.isCapabilitiesLoaded()) {
				jsonwriter.value(IMAPProperties.getImapCapabilities().getCapabilities());
				break;
			}
			jsonwriter.value(JSONObject.NULL);
			break;
		case FolderObject.SUBSCRIBED:
			if (withKey) {
				jsonwriter.key(FolderFields.SUBSCRIBED);
			}
			final String fn = fullName == null ? folder.getFullName() : fullName;
			if (STR_INBOX.equals(fn)) {
				jsonwriter.value(true);
			} else {
				jsonwriter.value(folder.containsSubscribe() ? folder.isSubscribed() : JSONObject.NULL);
			}
			break;
		default:
			break;
		}

	}

	public void writeOXFolderFieldsAsObject(final int[] fields, final FolderObject fo) throws Exception {
		writeOXFolderFieldsAsObject(fields, fo, null, -1);
	}

	public void writeOXFolderFieldsAsObject(int[] fields, final FolderObject fo, final String name,
			final int hasSubfolders) throws Exception {
		try {
			jsonwriter.object();
			if (fields == null) {
				fields = getAllFolderFields();
			}
			for (int i = 0; i < fields.length; i++) {
				writeOXFolderField(fields[i], fo, true, name, hasSubfolders);
			}
		} finally {
			jsonwriter.endObject();
		}
	}

	public void writeOXFolderFieldsAsArray(final int[] fields, final FolderObject fo) throws Exception {
		writeOXFolderFieldsAsArray(fields, fo, null, -1);
	}

	public void writeOXFolderFieldsAsArray(final int[] fields, final FolderObject fo, final String name,
			final int hasSubfolders) throws Exception {
		try {
			jsonwriter.array();
			for (int i = 0; i < fields.length; i++) {
				writeOXFolderField(fields[i], fo, false, name, hasSubfolders);
			}
		} finally {
			jsonwriter.endArray();
		}
	}

	public static int[] getAllFolderFields() {
		return new int[] { FolderObject.OBJECT_ID, FolderObject.CREATED_BY, FolderObject.MODIFIED_BY,
				FolderObject.CREATION_DATE, FolderObject.LAST_MODIFIED, FolderObject.FOLDER_ID,
				FolderObject.FOLDER_NAME, FolderObject.MODULE, FolderObject.TYPE, FolderObject.SUBFOLDERS,
				FolderObject.OWN_RIGHTS, FolderObject.PERMISSIONS_BITS, FolderObject.SUMMARY,
				FolderObject.STANDARD_FOLDER, FolderObject.TOTAL, FolderObject.NEW, FolderObject.UNREAD,
				FolderObject.DELETED, FolderObject.CAPABILITIES, FolderObject.SUBSCRIBED };
	}

	public final FolderFieldWriter[] getFolderFieldWriter(final int[] fields) {
		final FolderFieldWriter[] retval = new FolderFieldWriter[fields.length];
		for (int i = 0; i < retval.length; i++) {
			Fields: switch (fields[i]) {
			case FolderObject.OBJECT_ID:
				retval[i] = new FolderFieldWriter() {
					public void writeField(final JSONWriter jsonwriter, final FolderObject fo, final boolean withKey,
							final String name, final int hasSubfolders) throws JSONException {
						if (!fo.containsObjectID()) {
							if (withKey) {
								jsonwriter.key(FolderFields.ID);
							}
							jsonwriter.value(fo.containsFullName() ? fo.getFullName() : JSONObject.NULL);
							return;
						}
						if (withKey) {
							jsonwriter.key(FolderFields.ID);
						}
						jsonwriter.value(fo.getObjectID());
					}
				};
				break Fields;
			case FolderObject.CREATED_BY:
				retval[i] = new FolderFieldWriter() {
					public void writeField(final JSONWriter jsonwriter, final FolderObject fo, final boolean withKey,
							final String name, final int hasSubfolders) throws JSONException {
						if (withKey) {
							jsonwriter.key(FolderFields.CREATED_BY);
						}
						jsonwriter.value(fo.containsCreatedBy() ? fo.getCreatedBy() : JSONObject.NULL);
					}
				};
				break Fields;
			case FolderObject.MODIFIED_BY:
				retval[i] = new FolderFieldWriter() {
					public void writeField(final JSONWriter jsonwriter, final FolderObject fo, final boolean withKey,
							final String name, final int hasSubfolders) throws JSONException {
						if (withKey) {
							jsonwriter.key(FolderFields.MODIFIED_BY);
						}
						jsonwriter.value(fo.containsModifiedBy() ? fo.getModifiedBy() : JSONObject.NULL);
					}
				};
				break Fields;
			case FolderObject.CREATION_DATE:
				retval[i] = new FolderFieldWriter() {
					public void writeField(final JSONWriter jsonwriter, final FolderObject fo, final boolean withKey,
							final String name, final int hasSubfolders) throws JSONException {
						if (withKey) {
							jsonwriter.key(FolderFields.CREATION_DATE);
						}
						jsonwriter.value(fo.containsCreationDate() ? addTimeZoneOffset(fo.getCreationDate().getTime())
								: JSONObject.NULL);
					}
				};
				break Fields;
			case FolderObject.LAST_MODIFIED:
				retval[i] = new FolderFieldWriter() {
					public void writeField(final JSONWriter jsonwriter, final FolderObject fo, final boolean withKey,
							final String name, final int hasSubfolders) throws JSONException {
						if (withKey) {
							jsonwriter.key(FolderFields.LAST_MODIFIED);
						}
						jsonwriter.value(fo.containsLastModified() ? addTimeZoneOffset(fo.getLastModified().getTime())
								: JSONObject.NULL);
					}
				};
				break Fields;
			case FolderObject.FOLDER_ID:
				retval[i] = new FolderFieldWriter() {
					public void writeField(final JSONWriter jsonwriter, final FolderObject fo, final boolean withKey,
							final String name, final int hasSubfolders) throws JSONException {
						if (withKey) {
							jsonwriter.key(FolderFields.FOLDER_ID);
						}
						jsonwriter.value(fo.containsParentFolderID() ? fo.getParentFolderID() : JSONObject.NULL);
					}
				};
				break Fields;
			case FolderObject.FOLDER_NAME:
				retval[i] = new FolderFieldWriter() {
					public void writeField(final JSONWriter jsonwriter, final FolderObject fo, final boolean withKey,
							final String name, final int hasSubfolders) throws JSONException {
						if (withKey) {
							jsonwriter.key(FolderFields.TITLE);
						}
						jsonwriter
								.value(name == null ? (fo.containsFolderName() ? fo.getFolderName() : JSONObject.NULL)
										: name);
					}
				};
				break Fields;
			case FolderObject.MODULE:
				retval[i] = new FolderFieldWriter() {
					public void writeField(final JSONWriter jsonwriter, final FolderObject fo, final boolean withKey,
							final String name, final int hasSubfolders) throws JSONException {
						if (withKey) {
							jsonwriter.key(FolderFields.MODULE);
						}
						jsonwriter.value(fo.containsModule() ? getModuleString(fo.getModule(), fo.getObjectID())
								: JSONObject.NULL);
					}
				};
				break Fields;
			case FolderObject.TYPE:
				retval[i] = new FolderFieldWriter() {
					public void writeField(final JSONWriter jsonwriter, final FolderObject fo, final boolean withKey,
							final String name, final int hasSubfolders) throws JSONException {
						if (withKey) {
							jsonwriter.key(FolderFields.TYPE);
						}
						jsonwriter.value(fo.containsType() ? fo.getType(userObj.getId()) : JSONObject.NULL);
					}
				};
				break Fields;
			case FolderObject.SUBFOLDERS:
				retval[i] = new FolderFieldWriter() {
					public void writeField(final JSONWriter jsonwriter, final FolderObject fo, final boolean withKey,
							final String name, final int hasSubfolders) throws JSONException, DBPoolingException,
							OXException, SearchIteratorException, SQLException {
						if (withKey) {
							jsonwriter.key(FolderFields.SUBFOLDERS);
						}
						jsonwriter.value(hasSubfolders == -1 ? (fo.containsSubfolderFlag() ? fo.hasVisibleSubfolders(
								userObj, userConfig, ctx) : JSONObject.NULL) : (hasSubfolders > 0));
					}
				};
				break Fields;
			case FolderObject.OWN_RIGHTS:
				retval[i] = new FolderFieldWriter() {
					public void writeField(final JSONWriter jsonwriter, final FolderObject fo, final boolean withKey,
							final String name, final int hasSubfolders) throws JSONException, OXException,
							DBPoolingException, SQLException {
						if (!fo.containsPermissions()) {
							try {
								fo
										.setPermissionsAsArray(FolderObject.getFolderPermissions(fo.getObjectID(), ctx,
												null));
								if (FolderCacheManager.isEnabled()) {
									FolderCacheManager.getInstance().putFolderObject(fo, ctx);
								}
							} catch (SQLException e) {
								throw new OXFolderException(FolderCode.MISSING_FOLDER_ATTRIBUTE, STR_EMPTY,
										FolderFields.OWN_RIGHTS, fo.getObjectID(), ctx.getContextId());
							} catch (DBPoolingException e) {
								throw new OXFolderException(FolderCode.MISSING_FOLDER_ATTRIBUTE, STR_EMPTY,
										FolderFields.OWN_RIGHTS, fo.getObjectID(), ctx.getContextId());
							}
						}
						if (withKey) {
							jsonwriter.key(FolderFields.OWN_RIGHTS);
						}
						final OCLPermission effectivePerm = fo.getEffectiveUserPermission(userObj.getId(), userConfig);
						jsonwriter.value(createPermissionBits(effectivePerm.getFolderPermission(), effectivePerm
								.getReadPermission(), effectivePerm.getWritePermission(), effectivePerm
								.getDeletePermission(), effectivePerm.isFolderAdmin()));
					}
				};
				break Fields;
			case FolderObject.PERMISSIONS_BITS:
				retval[i] = new FolderFieldWriter() {
					public void writeField(final JSONWriter jsonwriter, final FolderObject fo, final boolean withKey,
							final String name, final int hasSubfolders) throws JSONException, OXException {
						if (!fo.containsPermissions()) {
							try {
								fo
										.setPermissionsAsArray(FolderObject.getFolderPermissions(fo.getObjectID(), ctx,
												null));
								if (FolderCacheManager.isEnabled()) {
									FolderCacheManager.getInstance().putFolderObject(fo, ctx);
								}
							} catch (SQLException e) {
								throw new OXFolderException(FolderCode.MISSING_PARAMETER, STR_EMPTY,
										FolderFields.PERMISSIONS);
							} catch (DBPoolingException e) {
								throw new OXFolderException(FolderCode.MISSING_PARAMETER, STR_EMPTY,
										FolderFields.PERMISSIONS);
							}
						}
						if (withKey) {
							jsonwriter.key(FolderFields.PERMISSIONS);
						}
						final JSONArray ja = new JSONArray();
						final OCLPermission[] perms = fo.getPermissionsAsArray();
						for (int i = 0; i < perms.length; i++) {
							final JSONObject jo = new JSONObject();
							jo.put(FolderFields.BITS, createPermissionBits(perms[i].getFolderPermission(), perms[i]
									.getReadPermission(), perms[i].getWritePermission(),
									perms[i].getDeletePermission(), perms[i].isFolderAdmin()));
							jo.put(FolderFields.ENTITY, perms[i].getEntity());
							jo.put(FolderFields.GROUP, perms[i].isGroupPermission());
							ja.put(jo);
						}
						jsonwriter.value(ja);
					}
				};
				break Fields;
			case FolderObject.SUMMARY:
				retval[i] = new FolderFieldWriter() {
					public void writeField(final JSONWriter jsonwriter, final FolderObject fo, final boolean withKey,
							final String name, final int hasSubfolders) throws JSONException, OXException {
						if (withKey) {
							jsonwriter.key(FolderFields.SUMMARY);
						}
						jsonwriter.value("");
					}
				};
				break Fields;
			case FolderObject.STANDARD_FOLDER:
				retval[i] = new FolderFieldWriter() {
					public void writeField(final JSONWriter jsonwriter, final FolderObject fo, final boolean withKey,
							final String name, final int hasSubfolders) throws JSONException, OXException {
						if (withKey) {
							jsonwriter.key(FolderFields.STANDARD_FOLDER);
						}
						jsonwriter.value(fo.isDefaultFolder());
					}
				};
				break Fields;
			case FolderObject.TOTAL:
				retval[i] = new FolderFieldWriter() {
					public void writeField(final JSONWriter jsonwriter, final FolderObject fo, final boolean withKey,
							final String name, final int hasSubfolders) throws JSONException, OXException {
						if (withKey) {
							jsonwriter.key(FolderFields.TOTAL);
						}
						jsonwriter.value(JSONObject.NULL);
					}
				};
				break Fields;
			case FolderObject.NEW:
				retval[i] = new FolderFieldWriter() {
					public void writeField(final JSONWriter jsonwriter, final FolderObject fo, final boolean withKey,
							final String name, final int hasSubfolders) throws JSONException, OXException {
						if (withKey) {
							jsonwriter.key(FolderFields.NEW);
						}
						jsonwriter.value(JSONObject.NULL);
					}
				};
				break Fields;
			case FolderObject.UNREAD:
				retval[i] = new FolderFieldWriter() {
					public void writeField(final JSONWriter jsonwriter, final FolderObject fo, final boolean withKey,
							final String name, final int hasSubfolders) throws JSONException, OXException {
						if (withKey) {
							jsonwriter.key(FolderFields.UNREAD);
						}
						jsonwriter.value(JSONObject.NULL);
					}
				};
				break;
			case FolderObject.DELETED:
				retval[i] = new FolderFieldWriter() {
					public void writeField(final JSONWriter jsonwriter, final FolderObject fo, final boolean withKey,
							final String name, final int hasSubfolders) throws JSONException, OXException {
						if (withKey) {
							jsonwriter.key(FolderFields.DELETED);
						}
						jsonwriter.value(JSONObject.NULL);
					}
				};
				break Fields;
			case FolderObject.CAPABILITIES:
				retval[i] = new FolderFieldWriter() {
					public void writeField(final JSONWriter jsonwriter, final FolderObject fo, final boolean withKey,
							final String name, final int hasSubfolders) throws JSONException, OXException {
						if (withKey) {
							jsonwriter.key(FolderFields.CAPABILITIES);
						}
						jsonwriter.value(JSONObject.NULL);
					}
				};
				break Fields;
			case FolderObject.SUBSCRIBED:
				retval[i] = new FolderFieldWriter() {
				public void writeField(final JSONWriter jsonwriter, final FolderObject fo, final boolean withKey,
						final String name, final int hasSubfolders) throws JSONException, OXException {
					if (withKey) {
						jsonwriter.key(FolderFields.SUBSCRIBED);
					}
					jsonwriter.value(JSONObject.NULL);
				}
			};
			break Fields;
			default:
				retval[i] = new FolderFieldWriter() {
					public void writeField(final JSONWriter jsonwriter, final FolderObject fo, final boolean withKey,
							final String name, final int hasSubfolders) throws JSONException {
						if (withKey) {
							jsonwriter.key("Unknown column");
						}
						jsonwriter.value(JSONObject.NULL);
					}
				};
			}
		}
		return retval;
	}

	public void writeOXFolderField(final int field, final FolderObject fo, final boolean withKey, final String name,
			final int hasSubfolders) throws Exception {
		switch (field) {
		case FolderObject.OBJECT_ID:
			if (!fo.containsObjectID()) {
				if (withKey) {
					jsonwriter.key(FolderFields.ID);
				}
				jsonwriter.value(fo.containsFullName() ? fo.getFullName() : JSONObject.NULL);
				break;
			}
			if (withKey) {
				jsonwriter.key(FolderFields.ID);
			}
			jsonwriter.value(fo.getObjectID());
			break;
		case FolderObject.CREATED_BY:
			if (withKey) {
				jsonwriter.key(FolderFields.CREATED_BY);
			}
			jsonwriter.value(fo.containsCreatedBy() ? fo.getCreatedBy() : JSONObject.NULL);
			break;
		case FolderObject.MODIFIED_BY:
			if (withKey) {
				jsonwriter.key(FolderFields.MODIFIED_BY);
			}
			jsonwriter.value(fo.containsModifiedBy() ? fo.getModifiedBy() : JSONObject.NULL);
			break;
		case FolderObject.CREATION_DATE:
			if (withKey) {
				jsonwriter.key(FolderFields.CREATION_DATE);
			}
			jsonwriter.value(fo.containsCreationDate() ? addTimeZoneOffset(fo.getCreationDate().getTime())
					: JSONObject.NULL);
			break;
		case FolderObject.LAST_MODIFIED:
			if (withKey) {
				jsonwriter.key(FolderFields.LAST_MODIFIED);
			}
			jsonwriter.value(fo.containsLastModified() ? addTimeZoneOffset(fo.getLastModified().getTime())
					: JSONObject.NULL);
			break;
		case FolderObject.FOLDER_ID:
			if (withKey) {
				jsonwriter.key(FolderFields.FOLDER_ID);
			}
			jsonwriter.value(fo.containsParentFolderID() ? fo.getParentFolderID() : JSONObject.NULL);
			break;
		case FolderObject.FOLDER_NAME:
			if (withKey) {
				jsonwriter.key(FolderFields.TITLE);
			}
			jsonwriter.value(name == null ? (fo.containsFolderName() ? fo.getFolderName() : JSONObject.NULL) : name);
			break;
		case FolderObject.MODULE:
			if (withKey) {
				jsonwriter.key(FolderFields.MODULE);
			}
			jsonwriter.value(fo.containsModule() ? getModuleString(fo.getModule(), fo.getObjectID()) : JSONObject.NULL);
			break;
		case FolderObject.TYPE:
			if (withKey) {
				jsonwriter.key(FolderFields.TYPE);
			}
			jsonwriter.value(fo.containsType() ? fo.getType(userObj.getId()) : JSONObject.NULL);
			break;
		case FolderObject.SUBFOLDERS:
			if (withKey) {
				jsonwriter.key(FolderFields.SUBFOLDERS);
			}
			jsonwriter.value(hasSubfolders == -1 ? (fo.containsSubfolderFlag() ? fo.hasVisibleSubfolders(userObj,
					userConfig, ctx) : JSONObject.NULL) : (hasSubfolders > 0));
			break;
		case FolderObject.OWN_RIGHTS:
			if (!fo.containsPermissions()) {
				try {
					fo.setPermissionsAsArray(FolderObject.getFolderPermissions(fo.getObjectID(), ctx, null));
					if (FolderCacheManager.isEnabled()) {
						FolderCacheManager.getInstance().putFolderObject(fo, ctx);
					}
				} catch (SQLException e) {
					throw new OXFolderException(FolderCode.MISSING_FOLDER_ATTRIBUTE, STR_EMPTY, "own_rights",
							getFolderName(fo), ctx.getContextId());
				} catch (DBPoolingException e) {
					throw new OXFolderException(FolderCode.MISSING_FOLDER_ATTRIBUTE, STR_EMPTY, "own_rights",
							getFolderName(fo), ctx.getContextId());
				}
			}
			if (withKey) {
				jsonwriter.key(FolderFields.OWN_RIGHTS);
			}
			final OCLPermission effectivePerm = fo.getEffectiveUserPermission(userObj.getId(), userConfig);
			jsonwriter.value(createPermissionBits(effectivePerm.getFolderPermission(), effectivePerm
					.getReadPermission(), effectivePerm.getWritePermission(), effectivePerm.getDeletePermission(),
					effectivePerm.isFolderAdmin()));
			break;
		case FolderObject.PERMISSIONS_BITS:
			if (!fo.containsPermissions()) {
				try {
					fo.setPermissionsAsArray(FolderObject.getFolderPermissions(fo.getObjectID(), ctx, null));
					if (FolderCacheManager.isEnabled()) {
						FolderCacheManager.getInstance().putFolderObject(fo, ctx);
					}
				} catch (SQLException e) {
					throw new OXFolderException(FolderCode.MISSING_PARAMETER, STR_EMPTY, FolderFields.PERMISSIONS);
				} catch (DBPoolingException e) {
					throw new OXFolderException(FolderCode.MISSING_PARAMETER, STR_EMPTY, FolderFields.PERMISSIONS);
				}
			}
			if (withKey) {
				jsonwriter.key(FolderFields.PERMISSIONS);
			}
			final JSONArray ja = new JSONArray();
			final OCLPermission[] perms = fo.getPermissionsAsArray();
			for (int i = 0; i < perms.length; i++) {
				final JSONObject jo = new JSONObject();
				jo.put(FolderFields.BITS, createPermissionBits(perms[i].getFolderPermission(), perms[i]
						.getReadPermission(), perms[i].getWritePermission(), perms[i].getDeletePermission(), perms[i]
						.isFolderAdmin()));
				jo.put(FolderFields.ENTITY, perms[i].getEntity());
				jo.put(FolderFields.GROUP, perms[i].isGroupPermission());
				ja.put(jo);
			}
			jsonwriter.value(ja);
			break;
		case FolderObject.SUMMARY:
			if (withKey) {
				jsonwriter.key(FolderFields.SUMMARY);
			}
			jsonwriter.value("");
			break;
		case FolderObject.STANDARD_FOLDER:
			if (withKey) {
				jsonwriter.key(FolderFields.STANDARD_FOLDER);
			}
			jsonwriter.value(fo.isDefaultFolder());
			break;
		case FolderObject.TOTAL:
			if (withKey) {
				jsonwriter.key(FolderFields.TOTAL);
			}
			jsonwriter.value(JSONObject.NULL);
			break;
		case FolderObject.NEW:
			if (withKey) {
				jsonwriter.key(FolderFields.NEW);
			}
			jsonwriter.value(JSONObject.NULL);
			break;
		case FolderObject.UNREAD:
			if (withKey) {
				jsonwriter.key(FolderFields.UNREAD);
			}
			jsonwriter.value(JSONObject.NULL);
			break;
		case FolderObject.DELETED:
			if (withKey) {
				jsonwriter.key(FolderFields.DELETED);
			}
			jsonwriter.value(JSONObject.NULL);
			break;
		case FolderObject.CAPABILITIES:
			if (withKey) {
				jsonwriter.key(FolderFields.CAPABILITIES);
			}
			jsonwriter.value(JSONObject.NULL);
		case FolderObject.SUBSCRIBED:
			if (withKey) {
				jsonwriter.key(FolderFields.SUBSCRIBED);
			}
			jsonwriter.value(JSONObject.NULL);
		default:
			break;
		}
	}

	private int createPermissionBits(final int fp, final int orp, final int owp, final int odp, final boolean adminFlag)
			throws OXException {
		int[] perms = new int[5];
		perms[0] = fp == Folder.MAX_PERMISSION ? OCLPermission.ADMIN_PERMISSION : fp;
		perms[1] = orp == Folder.MAX_PERMISSION ? OCLPermission.ADMIN_PERMISSION : orp;
		perms[2] = owp == Folder.MAX_PERMISSION ? OCLPermission.ADMIN_PERMISSION : owp;
		perms[3] = odp == Folder.MAX_PERMISSION ? OCLPermission.ADMIN_PERMISSION : odp;
		perms[4] = adminFlag ? 1 : 0;
		return createPermissionBits(perms);
	}

	public static String getModuleString(final int module, final int objectId) {
		String moduleStr = null;
		switch (module) {
		case FolderObject.TASK:
			moduleStr = Folder.MODULE_TASK;
			break;
		case FolderObject.CONTACT:
			moduleStr = Folder.MODULE_CONTACT;
			break;
		case FolderObject.CALENDAR:
			moduleStr = Folder.MODULE_CALENDAR;
			break;
		case FolderObject.UNBOUND:
			moduleStr = Folder.MODULE_UNBOUND;
		case FolderObject.MAIL:
			moduleStr = Folder.MODULE_MAIL;
			break;
		case FolderObject.PROJECT:
			moduleStr = Folder.MODULE_PROJECT;
			break;
		case FolderObject.INFOSTORE:
			moduleStr = Folder.MODULE_INFOSTORE;
			break;
		case FolderObject.SYSTEM_MODULE:
			if (objectId == FolderObject.SYSTEM_OX_PROJECT_FOLDER_ID) {
				moduleStr = Folder.MODULE_PROJECT;
			} else if (objectId == FolderObject.SYSTEM_INFOSTORE_FOLDER_ID) {
				moduleStr = Folder.MODULE_INFOSTORE;
			} else {
				moduleStr = Folder.MODULE_SYSTEM;
			}
			break;
		default:
			moduleStr = "";
			break;
		}
		return moduleStr;
	}

	private int createPermissionBits(final int[] permission) throws OXException {
		int retval = 0;
		boolean first = true;
		for (int i = permission.length - 1; i >= 0; i--) {
			final int shiftVal = (i * 7); // Number of bits to be shifted
			if (first) {
				retval += permission[i] << shiftVal;
				first = false;
			} else {
				if (permission[i] == OCLPermission.ADMIN_PERMISSION) {
					retval += Folder.MAX_PERMISSION << shiftVal;
				} else {
					try {
						retval += mapping[permission[i]] << shiftVal;
					} catch (Exception e) {
						throw new OXFolderException(FolderCode.MAP_PERMISSION_FAILED, e, permission[i]);
					}
				}
			}
		}
		return retval;
	}

	private long addTimeZoneOffset(final long date) {
		return (date + TimeZone.getTimeZone(userObj.getTimeZone()).getOffset(date));
	}

}
