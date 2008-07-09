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

import java.sql.SQLException;
import java.util.Locale;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONWriter;

import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.Folder;
import com.openexchange.ajax.fields.FolderFields;
import com.openexchange.api2.OXException;
import com.openexchange.cache.impl.FolderCacheManager;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.groupware.userconfiguration.UserConfigurationStorage;
import com.openexchange.server.impl.DBPoolingException;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.session.Session;
import com.openexchange.tools.iterator.SearchIteratorException;
import com.openexchange.tools.oxfolder.OXFolderException;
import com.openexchange.tools.oxfolder.OXFolderException.FolderCode;

/**
 * FolderWriter
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class FolderWriter extends DataWriter {

	private static final int[] mapping = { 0, -1, 1, -1, 2, -1, -1, -1, 4 };

	private static final String STR_UNKNOWN_COLUMN = "Unknown column";

	final User user;

	final UserConfiguration userConfig;

	final Context ctx;

	/**
	 * {@link FolderFieldWriter} - A writer for folder fields
	 * 
	 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben
	 *         Betten</a>
	 * 
	 */
	public static abstract class FolderFieldWriter {

		/**
		 * Initializes a new {@link FolderFieldWriter}
		 */
		protected FolderFieldWriter() {
			super();
		}

		/**
		 * Writes this writer's folder field from given {@link FolderObject} to
		 * specified {@link JSONWriter}.
		 * 
		 * @param jsonwriter
		 *            The JSON writer to write to
		 * @param fo
		 *            The folder object
		 * @param withKey
		 *            <code>true</code> to include JSON key; otherwise
		 *            <code>false</code>
		 * @throws JSONException
		 *             If a JSON error occurs
		 * @throws SQLException
		 *             If a SQL error occurs
		 * @throws AbstractOXException
		 *             If an OX error occurs
		 */
		public void writeField(final JSONWriter jsonwriter, final FolderObject fo, final boolean withKey)
				throws JSONException, SQLException, AbstractOXException {
			writeField(jsonwriter, fo, withKey, null, -1);
		}

		/**
		 * Writes this writer's folder field from given {@link FolderObject} to
		 * specified {@link JSONWriter}.
		 * 
		 * @param jsonwriter
		 *            The JSON writer to write to
		 * @param fo
		 *            The folder object
		 * @param withKey
		 *            <code>true</code> to include JSON key; otherwise
		 *            <code>false</code>
		 * @param name
		 *            The preferred folder name or <code>null</code> to take
		 *            folder name from given folder object.
		 * @param hasSubfolders
		 *            <code>1</code> to indicate subfolders, <code>0</code> to
		 *            indicate no subfolders, or <code>-1</code> to omit
		 * @throws JSONException
		 *             If a JSON error occurs
		 * @throws SQLException
		 *             If a SQL error occurs
		 * @throws AbstractOXException
		 *             If an OX error occurs
		 */
		public abstract void writeField(JSONWriter jsonwriter, FolderObject fo, boolean withKey, String name,
				int hasSubfolders) throws JSONException, SQLException, AbstractOXException;
	}

	/**
	 * Initializes a new {@link FolderWriter}
	 * 
	 * @param jw
	 *            The JSON writer to write to
	 * @param session
	 *            The session providing neeed user data
	 * @param ctx
	 *            The session's context
	 */
	public FolderWriter(final JSONWriter jw, final Session session, final Context ctx) {
		super(TimeZone.getTimeZone(UserStorage.getStorageUser(session.getUserId(), ctx).getTimeZone()), jw);
		this.user = UserStorage.getStorageUser(session.getUserId(), ctx);
		this.userConfig = UserConfigurationStorage.getInstance().getUserConfigurationSafe(session.getUserId(), ctx);
		this.ctx = ctx;
	}

	/**
	 * Writes specified fields from given folder into a JSON object
	 * 
	 * @param fields
	 *            The fields to write
	 * @param fo
	 *            The folder object
	 * @param locale
	 *            The user's locale to get appropriate folder name used in
	 *            display
	 * @throws OXException
	 *             If an OX error occurs
	 */
	public void writeOXFolderFieldsAsObject(final int[] fields, final FolderObject fo, final Locale locale)
			throws OXException {
		writeOXFolderFieldsAsObject(fields, fo, FolderObject.getFolderString(fo.getObjectID(), locale), -1);
	}

	/**
	 * Writes specified fields from given folder into a JSON object
	 * 
	 * @param fields
	 *            The fields to write
	 * @param fo
	 *            The folder object
	 * @param name
	 *            The preferred name or <code>null</code>
	 * @param hasSubfolders
	 *            <code>1</code> to indicate subfolders, <code>0</code> to
	 *            indicate no subfolders, or <code>-1</code> to omit
	 * @throws OXException
	 *             If an OX error occurs
	 */
	public void writeOXFolderFieldsAsObject(final int[] fields, final FolderObject fo, final String name,
			final int hasSubfolders) throws OXException {
		try {
			final int[] fs;
			if (fields == null) {
				fs = ALL_FLD_FIELDS;
			} else {
				fs = new int[fields.length];
				System.arraycopy(fields, 0, fs, 0, fields.length);
			}
			final FolderFieldWriter[] writers = getFolderFieldWriter(fs);
			jsonwriter.object();
			try {
				for (int i = 0; i < fs.length; i++) {
					writers[i].writeField(jsonwriter, fo, true, name, hasSubfolders);
				}
			} finally {
				jsonwriter.endObject();
			}
		} catch (final JSONException e) {
			throw new OXFolderException(OXFolderException.FolderCode.JSON_ERROR, e, e.getMessage());
		} catch (final SQLException e) {
			throw new OXFolderException(OXFolderException.FolderCode.SQL_ERROR, e, Integer.valueOf(ctx.getContextId()));
		} catch (final AbstractOXException e) {
			throw new OXFolderException(e);
		}
	}

	/**
	 * Writes specified fields from given folder into a JSON array
	 * 
	 * @param fields
	 *            The fields to write
	 * @param fo
	 *            The folder object
	 * @param locale
	 *            The user's locale to get appropriate folder name used in
	 *            display
	 * @throws OXException
	 *             If an OX error occurs
	 */
	public void writeOXFolderFieldsAsArray(final int[] fields, final FolderObject fo, final Locale locale)
			throws OXException {
		writeOXFolderFieldsAsArray(fields, fo, FolderObject.getFolderString(fo.getObjectID(), locale), -1);
	}

	/**
	 * Writes specified fields from given folder into a JSON array
	 * 
	 * @param fields
	 *            The fields to write
	 * @param fo
	 *            The folder object
	 * @param name
	 *            The preferred name or <code>null</code>
	 * @param hasSubfolders
	 *            <code>1</code> to indicate subfolders, <code>0</code> to
	 *            indicate no subfolders, or <code>-1</code> to omit
	 * @throws OXException
	 *             If an OX error occurs
	 */
	public void writeOXFolderFieldsAsArray(final int[] fields, final FolderObject fo, final String name,
			final int hasSubfolders) throws OXException {
		try {
			final FolderFieldWriter[] writers = getFolderFieldWriter(fields);
			jsonwriter.array();
			try {
				for (int i = 0; i < fields.length; i++) {
					writers[i].writeField(jsonwriter, fo, false, name, hasSubfolders);
				}
			} finally {
				jsonwriter.endArray();
			}
		} catch (final JSONException e) {
			throw new OXFolderException(OXFolderException.FolderCode.JSON_ERROR, e, e.getMessage());
		} catch (final SQLException e) {
			throw new OXFolderException(OXFolderException.FolderCode.SQL_ERROR, e, Integer.valueOf(ctx.getContextId()));
		} catch (final AbstractOXException e) {
			throw new OXFolderException(e);
		}
	}

	private static final int[] ALL_FLD_FIELDS = { FolderObject.OBJECT_ID, FolderObject.CREATED_BY,
			FolderObject.MODIFIED_BY, FolderObject.CREATION_DATE, FolderObject.LAST_MODIFIED, FolderObject.FOLDER_ID,
			FolderObject.FOLDER_NAME, FolderObject.MODULE, FolderObject.TYPE, FolderObject.SUBFOLDERS,
			FolderObject.OWN_RIGHTS, FolderObject.PERMISSIONS_BITS, FolderObject.SUMMARY, FolderObject.STANDARD_FOLDER,
			FolderObject.TOTAL, FolderObject.NEW, FolderObject.UNREAD, FolderObject.DELETED, FolderObject.CAPABILITIES,
			FolderObject.SUBSCRIBED, FolderObject.SUBSCR_SUBFLDS };

	/**
	 * Returns all known folder fields
	 * 
	 * @return All known folder fields
	 */
	public static int[] getAllFolderFields() {
		final int[] retval = new int[ALL_FLD_FIELDS.length];
		System.arraycopy(ALL_FLD_FIELDS, 0, retval, 0, retval.length);
		return retval;
	}

	public FolderFieldWriter[] getFolderFieldWriter(final int[] fields) {
		final FolderFieldWriter[] retval = new FolderFieldWriter[fields.length];
		for (int i = 0; i < retval.length; i++) {
			Fields: switch (fields[i]) {
			case FolderObject.OBJECT_ID:
				retval[i] = new FolderFieldWriter() {
					@Override
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
					@Override
					public void writeField(final JSONWriter jsonwriter, final FolderObject fo, final boolean withKey,
							final String name, final int hasSubfolders) throws JSONException {
						if (withKey) {
							jsonwriter.key(FolderFields.CREATED_BY);
						}
						jsonwriter.value(fo.containsCreatedBy() ? Integer.valueOf(fo.getCreatedBy()) : JSONObject.NULL);
					}
				};
				break Fields;
			case FolderObject.MODIFIED_BY:
				retval[i] = new FolderFieldWriter() {
					@Override
					public void writeField(final JSONWriter jsonwriter, final FolderObject fo, final boolean withKey,
							final String name, final int hasSubfolders) throws JSONException {
						if (withKey) {
							jsonwriter.key(FolderFields.MODIFIED_BY);
						}
						jsonwriter.value(fo.containsModifiedBy() ? Integer.valueOf(fo.getModifiedBy())
								: JSONObject.NULL);
					}
				};
				break Fields;
			case FolderObject.CREATION_DATE:
				retval[i] = new FolderFieldWriter() {
					@Override
					public void writeField(final JSONWriter jsonwriter, final FolderObject fo, final boolean withKey,
							final String name, final int hasSubfolders) throws JSONException {
						if (withKey) {
							jsonwriter.key(FolderFields.CREATION_DATE);
						}
						jsonwriter.value(fo.containsCreationDate() ? Long.valueOf(addTimeZoneOffset(fo
								.getCreationDate().getTime())) : JSONObject.NULL);
					}
				};
				break Fields;
			case FolderObject.LAST_MODIFIED:
				retval[i] = new FolderFieldWriter() {
					@Override
					public void writeField(final JSONWriter jsonwriter, final FolderObject fo, final boolean withKey,
							final String name, final int hasSubfolders) throws JSONException {
						if (withKey) {
							jsonwriter.key(FolderFields.LAST_MODIFIED);
						}
						jsonwriter.value(fo.containsLastModified() ? Long.valueOf(addTimeZoneOffset(fo
								.getLastModified().getTime())) : JSONObject.NULL);
					}
				};
				break Fields;
			case FolderObject.FOLDER_ID:
				retval[i] = new FolderFieldWriter() {
					@Override
					public void writeField(final JSONWriter jsonwriter, final FolderObject fo, final boolean withKey,
							final String name, final int hasSubfolders) throws JSONException {
						if (withKey) {
							jsonwriter.key(FolderFields.FOLDER_ID);
						}
						jsonwriter.value(fo.containsParentFolderID() ? Integer.valueOf(fo.getParentFolderID())
								: JSONObject.NULL);
					}
				};
				break Fields;
			case FolderObject.FOLDER_NAME:
				retval[i] = new FolderFieldWriter() {
					@Override
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
					@Override
					public void writeField(final JSONWriter jsonwriter, final FolderObject fo, final boolean withKey,
							final String name, final int hasSubfolders) throws JSONException {
						if (withKey) {
							jsonwriter.key(FolderFields.MODULE);
						}
						jsonwriter.value(fo.containsModule() ? AJAXServlet.getModuleString(fo.getModule(), fo
								.getObjectID()) : JSONObject.NULL);
					}
				};
				break Fields;
			case FolderObject.TYPE:
				retval[i] = new FolderFieldWriter() {
					@Override
					public void writeField(final JSONWriter jsonwriter, final FolderObject fo, final boolean withKey,
							final String name, final int hasSubfolders) throws JSONException {
						if (withKey) {
							jsonwriter.key(FolderFields.TYPE);
						}
						jsonwriter.value(fo.containsType() ? Integer.valueOf(fo.getType(user.getId()))
								: JSONObject.NULL);
					}
				};
				break Fields;
			case FolderObject.SUBFOLDERS:
				retval[i] = new FolderFieldWriter() {
					@Override
					public void writeField(final JSONWriter jsonwriter, final FolderObject fo, final boolean withKey,
							final String name, final int hasSubfolders) throws JSONException, DBPoolingException,
							OXException, SearchIteratorException, SQLException {
						if (withKey) {
							jsonwriter.key(FolderFields.SUBFOLDERS);
						}
						final boolean shared = fo.containsCreatedBy() && fo.containsType() && fo.isShared(user.getId());
						jsonwriter.value(hasSubfolders == -1 ? (shared ? Boolean.FALSE
								: (fo.containsSubfolderFlag() ? Boolean.valueOf(fo.hasVisibleSubfolders(user,
										userConfig, ctx)) : JSONObject.NULL)) : Boolean.valueOf(hasSubfolders > 0));
					}
				};
				break Fields;
			case FolderObject.OWN_RIGHTS:
				retval[i] = new FolderFieldWriter() {
					@Override
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
							} catch (final SQLException e) {
								throw new OXFolderException(FolderCode.MISSING_FOLDER_ATTRIBUTE,
										FolderFields.OWN_RIGHTS, Integer.valueOf(fo.getObjectID()), Integer.valueOf(ctx
												.getContextId()));
							} catch (final DBPoolingException e) {
								throw new OXFolderException(FolderCode.MISSING_FOLDER_ATTRIBUTE,
										FolderFields.OWN_RIGHTS, Integer.valueOf(fo.getObjectID()), Integer.valueOf(ctx
												.getContextId()));
							}
						}
						if (withKey) {
							jsonwriter.key(FolderFields.OWN_RIGHTS);
						}
						final OCLPermission effectivePerm = fo.getEffectiveUserPermission(user.getId(), userConfig);
						jsonwriter.value(createPermissionBits(effectivePerm.getFolderPermission(), effectivePerm
								.getReadPermission(), effectivePerm.getWritePermission(), effectivePerm
								.getDeletePermission(), effectivePerm.isFolderAdmin()));
					}
				};
				break Fields;
			case FolderObject.PERMISSIONS_BITS:
				retval[i] = new FolderFieldWriter() {
					@Override
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
							} catch (final SQLException e) {
								throw new OXFolderException(FolderCode.MISSING_PARAMETER, FolderFields.PERMISSIONS);
							} catch (final DBPoolingException e) {
								throw new OXFolderException(FolderCode.MISSING_PARAMETER, FolderFields.PERMISSIONS);
							}
						}
						if (withKey) {
							jsonwriter.key(FolderFields.PERMISSIONS);
						}
						final JSONArray ja = new JSONArray();
						final OCLPermission[] perms = fo.getPermissionsAsArray();
						final UserConfigurationStorage userConfStorage = UserConfigurationStorage.getInstance();
						try {
							for (int k = 0; k < perms.length; k++) {
								final OCLPermission perm;
								if (perms[k].isGroupPermission()) {
									perm = perms[k];
								} else {
									perm = fo.getEffectiveUserPermission(perms[k].getEntity(), userConfStorage
											.getUserConfiguration(perms[k].getEntity(), ctx));
								}
								final JSONObject jo = new JSONObject();
								jo.put(FolderFields.BITS, createPermissionBits(perm));
								jo.put(FolderFields.ENTITY, perm.getEntity());
								jo.put(FolderFields.GROUP, perm.isGroupPermission());
								ja.put(jo);
							}
						} catch (final DBPoolingException e) {
							throw new OXException(e);
						} catch (final SQLException e) {
							throw new OXFolderException(FolderCode.SQL_ERROR, e, Integer.valueOf(ctx.getContextId()));
						}
						jsonwriter.value(ja);
					}
				};
				break Fields;
			case FolderObject.SUMMARY:
				retval[i] = new FolderFieldWriter() {
					@Override
					public void writeField(final JSONWriter jsonwriter, final FolderObject fo, final boolean withKey,
							final String name, final int hasSubfolders) throws JSONException {
						if (withKey) {
							jsonwriter.key(FolderFields.SUMMARY);
						}
						jsonwriter.value("");
					}
				};
				break Fields;
			case FolderObject.STANDARD_FOLDER:
				retval[i] = new FolderFieldWriter() {
					@Override
					public void writeField(final JSONWriter jsonwriter, final FolderObject fo, final boolean withKey,
							final String name, final int hasSubfolders) throws JSONException {
						if (withKey) {
							jsonwriter.key(FolderFields.STANDARD_FOLDER);
						}
						jsonwriter.value(fo.isDefaultFolder());
					}
				};
				break Fields;
			case FolderObject.TOTAL:
				retval[i] = new FolderFieldWriter() {
					@Override
					public void writeField(final JSONWriter jsonwriter, final FolderObject fo, final boolean withKey,
							final String name, final int hasSubfolders) throws JSONException {
						if (withKey) {
							jsonwriter.key(FolderFields.TOTAL);
						}
						jsonwriter.value(JSONObject.NULL);
					}
				};
				break Fields;
			case FolderObject.NEW:
				retval[i] = new FolderFieldWriter() {
					@Override
					public void writeField(final JSONWriter jsonwriter, final FolderObject fo, final boolean withKey,
							final String name, final int hasSubfolders) throws JSONException {
						if (withKey) {
							jsonwriter.key(FolderFields.NEW);
						}
						jsonwriter.value(JSONObject.NULL);
					}
				};
				break Fields;
			case FolderObject.UNREAD:
				retval[i] = new FolderFieldWriter() {
					@Override
					public void writeField(final JSONWriter jsonwriter, final FolderObject fo, final boolean withKey,
							final String name, final int hasSubfolders) throws JSONException {
						if (withKey) {
							jsonwriter.key(FolderFields.UNREAD);
						}
						jsonwriter.value(JSONObject.NULL);
					}
				};
				break;
			case FolderObject.DELETED:
				retval[i] = new FolderFieldWriter() {
					@Override
					public void writeField(final JSONWriter jsonwriter, final FolderObject fo, final boolean withKey,
							final String name, final int hasSubfolders) throws JSONException {
						if (withKey) {
							jsonwriter.key(FolderFields.DELETED);
						}
						jsonwriter.value(JSONObject.NULL);
					}
				};
				break Fields;
			case FolderObject.CAPABILITIES:
				retval[i] = new FolderFieldWriter() {
					@Override
					public void writeField(final JSONWriter jsonwriter, final FolderObject fo, final boolean withKey,
							final String name, final int hasSubfolders) throws JSONException {
						if (withKey) {
							jsonwriter.key(FolderFields.CAPABILITIES);
						}
						jsonwriter.value(JSONObject.NULL);
					}
				};
				break Fields;
			case FolderObject.SUBSCRIBED:
				retval[i] = new FolderFieldWriter() {
					@Override
					public void writeField(final JSONWriter jsonwriter, final FolderObject fo, final boolean withKey,
							final String name, final int hasSubfolders) throws JSONException {
						if (withKey) {
							jsonwriter.key(FolderFields.SUBSCRIBED);
						}
						jsonwriter.value(JSONObject.NULL);
					}
				};
				break Fields;
			case FolderObject.SUBSCR_SUBFLDS:
				retval[i] = new FolderFieldWriter() {
					@Override
					public void writeField(final JSONWriter jsonwriter, final FolderObject fo, final boolean withKey,
							final String name, final int hasSubfolders) throws JSONException {
						if (withKey) {
							jsonwriter.key(FolderFields.SUBSCR_SUBFLDS);
						}
						jsonwriter.value(JSONObject.NULL);
					}
				};
				break Fields;
			default:
				retval[i] = new FolderFieldWriter() {
					@Override
					public void writeField(final JSONWriter jsonwriter, final FolderObject fo, final boolean withKey,
							final String name, final int hasSubfolders) throws JSONException {
						if (withKey) {
							jsonwriter.key(STR_UNKNOWN_COLUMN);
						}
						jsonwriter.value(JSONObject.NULL);
					}
				};
			}
		}
		return retval;
	}

	static int createPermissionBits(final OCLPermission perm) throws OXFolderException {
		return createPermissionBits(perm.getFolderPermission(), perm.getReadPermission(), perm.getWritePermission(),
				perm.getDeletePermission(), perm.isFolderAdmin());
	}

	static int createPermissionBits(final int fp, final int orp, final int owp, final int odp, final boolean adminFlag)
			throws OXFolderException {
		final int[] perms = new int[5];
		perms[0] = fp == Folder.MAX_PERMISSION ? OCLPermission.ADMIN_PERMISSION : fp;
		perms[1] = orp == Folder.MAX_PERMISSION ? OCLPermission.ADMIN_PERMISSION : orp;
		perms[2] = owp == Folder.MAX_PERMISSION ? OCLPermission.ADMIN_PERMISSION : owp;
		perms[3] = odp == Folder.MAX_PERMISSION ? OCLPermission.ADMIN_PERMISSION : odp;
		perms[4] = adminFlag ? 1 : 0;
		return createPermissionBits(perms);
	}

	private static int createPermissionBits(final int[] permission) throws OXFolderException {
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
					} catch (final Exception e) {
						throw new OXFolderException(FolderCode.MAP_PERMISSION_FAILED, e, Integer.valueOf(permission[i]));
					}
				}
			}
		}
		return retval;
	}

	protected long addTimeZoneOffset(final long date) {
		return (date + timeZone.getOffset(date));
	}

}
