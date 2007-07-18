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

package com.openexchange.ajax;

import static com.openexchange.tools.oxfolder.OXFolderManagerImpl.folderModule2String;
import static com.openexchange.tools.oxfolder.OXFolderManagerImpl.getUserName;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONWriter;

import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.fields.FolderFields;
import com.openexchange.ajax.helper.ParamContainer;
import com.openexchange.ajax.parser.FolderParser;
import com.openexchange.ajax.parser.MailFolderParser;
import com.openexchange.ajax.writer.FolderWriter;
import com.openexchange.ajax.writer.FolderWriter.FolderFieldWriter;
import com.openexchange.ajax.writer.FolderWriter.IMAPFolderFieldWriter;
import com.openexchange.api2.FolderSQLInterface;
import com.openexchange.api2.MailInterface;
import com.openexchange.api2.MailInterfaceImpl;
import com.openexchange.api2.OXException;
import com.openexchange.api2.RdbFolderSQLInterface;
import com.openexchange.cache.FolderCacheManager;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.Component;
import com.openexchange.groupware.AbstractOXException.Category;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.MailFolderObject;
import com.openexchange.groupware.i18n.FolderStrings;
import com.openexchange.groupware.i18n.Groups;
import com.openexchange.groupware.ldap.LdapException;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.i18n.StringHelper;
import com.openexchange.server.OCLPermission;
import com.openexchange.sessiond.SessionObject;
import com.openexchange.tools.iterator.FolderObjectIterator;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorException;
import com.openexchange.tools.oxfolder.OXFolderAccess;
import com.openexchange.tools.oxfolder.OXFolderException;
import com.openexchange.tools.oxfolder.OXFolderManagerImpl;
import com.openexchange.tools.oxfolder.OXFolderException.FolderCode;
import com.openexchange.tools.servlet.http.Tools;

/**
 * Folder
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class Folder extends SessionServlet {

	private static final String SPLIT_PAT = " *, *";

	private static final String JSON_KEY_DATA = "data";

	/**
	 * The constant for Inbox mail folder. TODO: Should be read from StringHelper utility class!
	 */
	private static final String DEF_NAME_INBOX = "Inbox";

	private static final String STR_INBOX = "INBOX";

	private static final long serialVersionUID = -889739420660750770L;

	private static transient final Log LOG = LogFactory.getLog(Folder.class);

	/**
	 * Error message if writing the response fails.
	 */
	private static final String RESPONSE_ERROR = "Error while writing response object.";

	private static final AbstractOXException getWrappingOXException(final Throwable cause) {
		return new AbstractOXException(Component.FOLDER, Category.INTERNAL_ERROR, 9999, cause.getMessage(), cause);
	}

	/**
	 * The parameter 'parent' contains the grand parent folder's id
	 */
	public static final String PARAMETER_PARENT = "parent";

	/**
	 * The parameter 'mail'
	 */
	public static final String PARAMETER_MAIL = "mail";

	/**
	 * The actual max permission that can be transfered in field 'bits' or
	 * JSON's permission object
	 */
	public static final int MAX_PERMISSION = 64;

	private static final String STRING_1 = "1";

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
		resp.setContentType(CONTENTTYPE_JAVASCRIPT);
		Tools.disableCaching(resp);
		try {
			actionGet(req, resp);
		} catch (final Exception e) {
			LOG.error("doGet", e);
			writeError(e.toString(), new JSONWriter(resp.getWriter()));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.http.HttpServlet#doPut(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doPut(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
		resp.setContentType(CONTENTTYPE_JAVASCRIPT);
		Tools.disableCaching(resp);
		try {
			actionPut(req, resp);
		} catch (final Exception e) {
			LOG.error("doGet", e);
			writeError(e.toString(), new JSONWriter(resp.getWriter()));
		}
	}

	/**
	 * Writes given error message into JSON response
	 * 
	 * @param error
	 * @param jsonWriter
	 */
	private final static void writeError(final String error, final JSONWriter jsonWriter) {
		try {
			startResponse(jsonWriter);
			jsonWriter.value(JSONObject.NULL);
			endResponse(jsonWriter, null, error);
		} catch (final Exception exc) {
			LOG.error("writeError", exc);
		}
	}

	/**
	 * Assigns incoming GET request to corresponding method
	 * 
	 * @param req
	 * @param resp
	 * @throws Exception
	 */
	private final void actionGet(final HttpServletRequest req, final HttpServletResponse resp) throws Exception {
		final String actionStr = checkStringParam(req, PARAMETER_ACTION);
		if (actionStr.equalsIgnoreCase(ACTION_ROOT)) {
			actionGetRoot(req, resp);
		} else if (actionStr.equalsIgnoreCase(ACTION_LIST)) {
			actionGetSubfolders(req, resp);
		} else if (actionStr.equalsIgnoreCase(ACTION_PATH)) {
			actionGetPath(req, resp);
		} else if (actionStr.equalsIgnoreCase(ACTION_UPDATES)) {
			actionGetUpdatedFolders(req, resp);
		} else if (actionStr.equalsIgnoreCase(ACTION_GET)) {
			actionGetFolder(req, resp);
		} else {
			throw getWrappingOXException(new Exception("Action \"" + actionStr
					+ "\" NOT supported via GET on /ajax/folders"));
		}
	}

	/**
	 * Assigns incoming PUT request to corresponding method
	 */
	private final void actionPut(final HttpServletRequest req, final HttpServletResponse resp) throws Exception {
		final String actionStr = checkStringParam(req, PARAMETER_ACTION);
		if (actionStr.equalsIgnoreCase(ACTION_UPDATE)) {
			actionPutUpdateFolder(req, resp);
		} else if (actionStr.equalsIgnoreCase(ACTION_NEW)) {
			actionPutInsertFolder(req, resp);
		} else if (actionStr.equalsIgnoreCase(ACTION_DELETE)) {
			actionPutDeleteFolder(req, resp);
		} else if (actionStr.equalsIgnoreCase("removetestfolders")) {
			actionPutRemoveTestFolder(req, resp);
		} else {
			throw getWrappingOXException(new Exception("Action \"" + actionStr
					+ "\" NOT supported via PUT on /ajax/folders"));
		}
	}

	/**
	 * Performs the GET request to send back root folders
	 */
	public void actionGetRoot(final SessionObject sessionObj, final Writer pw, final JSONObject requestObj)
			throws JSONException {
		actionGetRoot(sessionObj, pw, ParamContainer.getInstance(requestObj, Component.FOLDER));
	}

	private final void actionGetRoot(final HttpServletRequest req, final HttpServletResponse resp)
			throws JSONException, IOException {
		actionGetRoot(getSessionObject(req), resp.getWriter(), ParamContainer.getInstance(req, Component.FOLDER, resp));
	}

	/**
	 * Performs the GET request to send back root folders
	 */
	private final void actionGetRoot(final SessionObject sessionObj, final Writer pw,
			final ParamContainer paramContainer) throws JSONException {
		/*
		 * Some variables
		 */
		final Response response = new Response();
		final StringWriter strWriter = new StringWriter();
		final JSONWriter jsonWriter = new JSONWriter(strWriter);
		long lastModified = 0;
		/*
		 * Start response
		 */
		jsonWriter.array();
		try {
			/*
			 * Read in parameters
			 */
			final int[] columns = paramContainer.checkIntArrayParam(PARAMETER_COLUMNS);
			final FolderSQLInterface foldersqlinterface = new RdbFolderSQLInterface(sessionObj);
			final FolderWriter folderWriter = new FolderWriter(jsonWriter, sessionObj);
			final FolderFieldWriter[] writers = folderWriter.getFolderFieldWriter(columns);

			final Queue<FolderObject> q = ((FolderObjectIterator) foldersqlinterface.getRootFolderForUser()).asQueue();
			final int size = q.size();
			final Iterator<FolderObject> iter = q.iterator();
			final OCLPermission perm = new OCLPermission();
			NextRootFolder: for (int i = 0; i < size; i++) {
				final FolderObject rootFolder = iter.next();
				int hasSubfolder = -1;
				if (rootFolder.getObjectID() == FolderObject.SYSTEM_FOLDER_ID
						|| rootFolder.getObjectID() == FolderObject.SYSTEM_OX_FOLDER_ID) {
					/*
					 * Ignore 'system' and 'ox folder' folder
					 */
					continue NextRootFolder;
				} else if (rootFolder.getObjectID() == FolderObject.SYSTEM_INFOSTORE_FOLDER_ID) {
					/*
					 * Reset infostore's permission to read-only, cause virtual
					 * folder 'UserStore' is going to be used instead
					 */
					perm.reset();
					perm.setEntity(OCLPermission.ALL_GROUPS_AND_USERS);
					perm.setFolderAdmin(false);
					perm.setGroupPermission(true);
					perm.setAllPermission(OCLPermission.READ_FOLDER, OCLPermission.NO_PERMISSIONS,
							OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS);
					rootFolder.setPermissionsAsArray(new OCLPermission[] { (OCLPermission) perm.clone() });
				} else if (rootFolder.getObjectID() == FolderObject.SYSTEM_SHARED_FOLDER_ID
						&& !sessionObj.getUserConfiguration().hasFullSharedFolderAccess()) {
					/*
					 * User does not hold READ_CREATE_SHARED_FOLDERS in user
					 * configuration; mark system shared folder to have no
					 * subfolders
					 */
					hasSubfolder = 0;
				}
				lastModified = rootFolder.getLastModified() == null ? lastModified : Math.max(lastModified, rootFolder
						.getLastModified().getTime());
				jsonWriter.array();
				try {
					for (final FolderFieldWriter ffw : writers) {
						ffw.writeField(jsonWriter, rootFolder, false, FolderObject.getFolderString(rootFolder
								.getObjectID(), sessionObj.getLocale()), hasSubfolder);
					}
				} finally {
					jsonWriter.endArray();
				}
			}
		} catch (final OXFolderException e) {
			LOG.error(e.getMessage(), e);
			//if (!e.getCategory().equals(Category.USER_CONFIGURATION)) {
				response.setException(e);
			//}
		} catch (final AbstractOXException e) {
			LOG.error(e.getMessage(), e);
			response.setException(e);
		} catch (final Exception e) {
			LOG.error("actionGetRoot", e);
			response.setException(getWrappingOXException(e));
		}
		/*
		 * Close response and flush print writer
		 */
		jsonWriter.endArray();
		response.setData(new JSONArray(strWriter.toString()));
		response.setTimestamp(lastModified == 0 ? null : new Date(lastModified));
		Response.write(response, pw);
	}

	/**
	 * Performs the GET request to back certain folder's subfolders
	 * 
	 * @param sessionObj
	 * @param pw
	 * @param requestObj
	 * @throws JSONException
	 * @throws SearchIteratorException
	 */
	public void actionGetSubfolders(final SessionObject sessionObj, final Writer pw, final JSONObject requestObj)
			throws JSONException {
		actionGetSubfolders(sessionObj, pw, ParamContainer.getInstance(requestObj, Component.FOLDER));
	}

	private final void actionGetSubfolders(final HttpServletRequest req, final HttpServletResponse resp)
			throws IOException, ServletException {
		try {
			actionGetSubfolders(getSessionObject(req), resp.getWriter(), ParamContainer.getInstance(req,
					Component.FOLDER, resp));
		} catch (final JSONException e) {
			sendErrorAsJS(resp, RESPONSE_ERROR);
		}
	}

	private final void actionGetSubfolders(final SessionObject sessionObj, final Writer pw,
			final ParamContainer paramContainer) throws JSONException {
		/*
		 * Some variables
		 */
		final Response response = new Response();
		final StringHelper strHelper = new StringHelper(sessionObj.getLocale());
		final StringWriter strWriter = new StringWriter();
		final JSONWriter jsonWriter = new JSONWriter(strWriter);
		Date lastModifiedDate = null;
		/*
		 * Start response
		 */
		jsonWriter.array();
		try {
			/*
			 * Read in parameters
			 */
			final int[] columns = paramContainer.checkIntArrayParam(PARAMETER_COLUMNS);
			final String parentIdentifier = paramContainer.checkStringParam(PARAMETER_PARENT);
			final String ignore = paramContainer.getStringParam(PARAMETER_IGNORE);
			boolean ignoreMailfolder = false;
			if (ignore != null && "mailfolder".equalsIgnoreCase(ignore)) {
				ignoreMailfolder = true;
			}
			final FolderWriter folderWriter = new FolderWriter(jsonWriter, sessionObj);
			int parentId = -1;
			if ((parentId = getUnsignedInteger(parentIdentifier)) != -1) {
				long lastModified = 0;
				final FolderSQLInterface foldersqlinterface = new RdbFolderSQLInterface(sessionObj);
				final FolderFieldWriter[] writers = folderWriter.getFolderFieldWriter(columns);
				/*
				 * Write requested child folders
				 */
				if (parentId == FolderObject.VIRTUAL_USER_INFOSTORE_FOLDER_ID) {
					/*
					 * Special treatment for virtual user infostore folder
					 */
					final Queue<FolderObject> q = ((FolderObjectIterator) foldersqlinterface.getSubfolders(
							FolderObject.SYSTEM_INFOSTORE_FOLDER_ID, null)).asQueue();
					final int size = q.size();
					final Iterator<FolderObject> iter = q.iterator();
					for (int i = 0; i < size; i++) {
						final FolderObject fo = iter.next();
						lastModified = fo.getLastModified() == null ? lastModified : Math.max(lastModified, fo
								.getLastModified().getTime());
						jsonWriter.array();
						try {
							for (final FolderFieldWriter ffw : writers) {
								ffw.writeField(jsonWriter, fo, false);
							}
						} finally {
							jsonWriter.endArray();
						}
					}
				} else if (parentId == FolderObject.VIRTUAL_LIST_TASK_FOLDER_ID) {
					/*
					 * Append non-tree visible task folders
					 */
					final Queue<FolderObject> q = ((FolderObjectIterator) foldersqlinterface
							.getNonTreeVisiblePublicTaskFolders()).asQueue();
					final int size = q.size();
					final Iterator<FolderObject> iter = q.iterator();
					for (int i = 0; i < size; i++) {
						final FolderObject listFolder = iter.next();
						lastModified = listFolder.getLastModified() == null ? lastModified : Math.max(lastModified,
								listFolder.getLastModified().getTime());
						jsonWriter.array();
						try {
							for (int j = 0; j < writers.length; j++) {
								writers[j].writeField(jsonWriter, listFolder, false);
							}
						} finally {
							jsonWriter.endArray();
						}
					}
				} else if (parentId == FolderObject.VIRTUAL_LIST_CALENDAR_FOLDER_ID) {
					/*
					 * Append non-tree visible calendar folders
					 */
					final Queue<FolderObject> q = ((FolderObjectIterator) foldersqlinterface
							.getNonTreeVisiblePublicCalendarFolders()).asQueue();
					final int size = q.size();
					final Iterator<FolderObject> iter = q.iterator();
					for (int i = 0; i < size; i++) {
						final FolderObject listFolder = iter.next();
						lastModified = listFolder.getLastModified() == null ? lastModified : Math.max(lastModified,
								listFolder.getLastModified().getTime());
						jsonWriter.array();
						try {
							for (int j = 0; j < writers.length; j++) {
								writers[j].writeField(jsonWriter, listFolder, false);
							}
						} finally {
							jsonWriter.endArray();
						}
					}
				} else if (parentId == FolderObject.VIRTUAL_LIST_CONTACT_FOLDER_ID) {
					/*
					 * Append non-tree visible contact folders
					 */
					final Queue<FolderObject> q = ((FolderObjectIterator) foldersqlinterface
							.getNonTreeVisiblePublicContactFolders()).asQueue();
					final int size = q.size();
					final Iterator<FolderObject> iter = q.iterator();
					for (int i = 0; i < size; i++) {
						final FolderObject listFolder = iter.next();
						lastModified = listFolder.getLastModified() == null ? lastModified : Math.max(lastModified,
								listFolder.getLastModified().getTime());
						jsonWriter.array();
						try {
							for (int j = 0; j < writers.length; j++) {
								writers[j].writeField(jsonWriter, listFolder, false);
							}
						} finally {
							jsonWriter.endArray();
						}
					}
				} else if (parentId == FolderObject.VIRTUAL_LIST_INFOSTORE_FOLDER_ID) {
					/*
					 * Append non-tree visible infostore folders
					 */
					final Queue<FolderObject> q = ((FolderObjectIterator) foldersqlinterface
							.getNonTreeVisiblePublicInfostoreFolders()).asQueue();
					final int size = q.size();
					final Iterator<FolderObject> iter = q.iterator();
					for (int i = 0; i < size; i++) {
						final FolderObject listFolder = iter.next();
						lastModified = listFolder.getLastModified() == null ? lastModified : Math.max(lastModified,
								listFolder.getLastModified().getTime());
						jsonWriter.array();
						try {
							for (int j = 0; j < writers.length; j++) {
								writers[j].writeField(jsonWriter, listFolder, false);
							}
						} finally {
							jsonWriter.endArray();
						}
					}
				} else if (parentId == FolderObject.SYSTEM_INFOSTORE_FOLDER_ID) {
					if (!sessionObj.getUserConfiguration().hasInfostore()) {
						throw new OXFolderException(FolderCode.NO_MODULE_ACCESS, getUserName(sessionObj),
								folderModule2String(FolderObject.INFOSTORE), Integer
										.valueOf(sessionObj.getContext().getContextId()));
					}
					/*
					 * Append virtual folder 'Userstore'
					 */
					if (FolderCacheManager.isEnabled()) {
						lastModified = FolderCacheManager.getInstance().getFolderObject(parentId, true,
								sessionObj.getContext(), null).getLastModified().getTime();
					} else {
						lastModified = FolderObject.loadFolderObjectFromDB(parentId, sessionObj.getContext())
								.getLastModified().getTime();
					}
					final OCLPermission virtualPerm = new OCLPermission();
					virtualPerm.setEntity(OCLPermission.ALL_GROUPS_AND_USERS);
					virtualPerm.setFolderAdmin(false);
					virtualPerm.setGroupPermission(true);
					virtualPerm.setAllPermission(OCLPermission.CREATE_SUB_FOLDERS, OCLPermission.NO_PERMISSIONS,
							OCLPermission.NO_PERMISSIONS, OCLPermission.NO_PERMISSIONS);
					final FolderObject virtualUserstoreFolder = FolderObject.createVirtualFolderObject(
							FolderObject.VIRTUAL_USER_INFOSTORE_FOLDER_ID, FolderObject.getFolderString(
									FolderObject.VIRTUAL_USER_INFOSTORE_FOLDER_ID, sessionObj.getLocale()),
							FolderObject.INFOSTORE, true, FolderObject.SYSTEM_TYPE, virtualPerm);
					folderWriter.writeOXFolderFieldsAsArray(columns, virtualUserstoreFolder);
					/*
					 * Append virtual root folder for non-tree visible infostore
					 * folders
					 */
					SearchIterator it = null;
					try {
						it = foldersqlinterface.getNonTreeVisiblePublicInfostoreFolders();
						if (it.hasNext()) {
							final FolderObject virtualListFolder = FolderObject.createVirtualFolderObject(
									FolderObject.VIRTUAL_LIST_INFOSTORE_FOLDER_ID, FolderObject.getFolderString(
											FolderObject.VIRTUAL_LIST_INFOSTORE_FOLDER_ID, sessionObj.getLocale()),
									FolderObject.INFOSTORE, true, FolderObject.SYSTEM_TYPE);
							folderWriter.writeOXFolderFieldsAsArray(columns, virtualListFolder);
						}
					} finally {
						if (it != null) {
							it.close();
							it = null;
						}
					}
				} else if (parentId == FolderObject.SYSTEM_SHARED_FOLDER_ID) {
					final Set<String> displayNames = new HashSet<String>();
					UserStorage us = null;
					final Queue<FolderObject> q = ((FolderObjectIterator) foldersqlinterface.getSubfolders(
							FolderObject.SYSTEM_SHARED_FOLDER_ID, null)).asQueue();
					final int size = q.size();
					final Iterator<FolderObject> iter = q.iterator();
					for (int i = 0; i < size; i++) {
						final FolderObject sharedFolder = iter.next();
						if (us == null) {
							us = UserStorage.getInstance(sessionObj.getContext());
						}
						String creatorDisplayName;
						try {
							creatorDisplayName = us.getUser(sharedFolder.getCreatedBy()).getDisplayName();
						} catch (final LdapException e) {
							if (sharedFolder.getCreatedBy() != OCLPermission.ALL_GROUPS_AND_USERS) {
								throw new AbstractOXException(e);
							}
							creatorDisplayName = strHelper.getString(Groups.ZERO_DISPLAYNAME);
						}
						if (displayNames.contains(creatorDisplayName)) {
							continue;
						}
						displayNames.add(creatorDisplayName);
						final FolderObject virtualOwnerFolder = FolderObject.createVirtualSharedFolderObject(
								sharedFolder.getCreatedBy(), creatorDisplayName);
						jsonWriter.array();
						try {
							for (int j = 0; j < writers.length; j++) {
								writers[j].writeField(jsonWriter, virtualOwnerFolder, false, null, 1);
							}
						} finally {
							jsonWriter.endArray();
						}
					}
				} else {
					/*
					 * Append child folders
					 */
					final boolean isSystemPrivateFolder = (parentId == FolderObject.SYSTEM_PRIVATE_FOLDER_ID);
					final boolean isSystemPublicFolder = (parentId == FolderObject.SYSTEM_PUBLIC_FOLDER_ID);
					if (isSystemPrivateFolder) {
						/*
						 * Append mail inbox to system 'private' folder
						 */
						if (sessionObj.getUserConfiguration().hasWebMail() && !ignoreMailfolder) {
							MailInterface mailInterface = null;
							SearchIterator it = null;
							try {
								mailInterface = MailInterfaceImpl.getInstance(sessionObj);
								it = mailInterface.getRootFolders();
								final int size = it.size();
								for (int a = 0; a < size; a++) {
									final MailFolderObject rootFolder = (MailFolderObject) it.next();
									folderWriter.writeIMAPFolderAsArray(columns, rootFolder,
											MailFolderObject.DEFAULT_IMAP_FOLDER_NAME, 1,
											MailFolderObject.DEFAULT_IMAP_FOLDER_ID, FolderObject.SYSTEM_MODULE);
								}
							} catch (final OXException e) {
								LOG.error(e.getMessage(), e);
							} finally {
								if (it != null) {
									it.close();
									it = null;
								}
								if (mailInterface != null) {
									try {
										mailInterface.close(true);
										mailInterface = null;
									} catch (final OXException e) {
										LOG.error(e.getMessage(), e);
									}
								}
							}
						}
					} else if (isSystemPublicFolder) {
						/*
						 * Append internal users folder
						 */
						try {
							final FolderObject internalUsers = foldersqlinterface
									.getFolderById(FolderObject.SYSTEM_LDAP_FOLDER_ID);
							folderWriter.writeOXFolderFieldsAsArray(columns, internalUsers, FolderObject
									.getFolderString(internalUsers.getObjectID(), sessionObj.getLocale()), -1);
						} catch (final OXException e) {
							/*
							 * Internal users folder not visible to current user
							 */
							LOG.warn(e.getMessage(), e);
						}
					}
					final Queue<FolderObject> q = ((FolderObjectIterator) foldersqlinterface.getSubfolders(parentId,
							null)).asQueue();
					final int size = q.size();
					final Iterator<FolderObject> iter = q.iterator();
					for (int i = 0; i < size; i++) {
						final FolderObject fo = iter.next();
						lastModified = fo.getLastModified() == null ? lastModified : Math.max(lastModified, fo
								.getLastModified().getTime());
						jsonWriter.array();
						try {
							for (int j = 0; j < writers.length; j++) {
								writers[j].writeField(jsonWriter, fo, false);
							}
						} finally {
							jsonWriter.endArray();
						}
					}
					if (isSystemPrivateFolder) {
						if (sessionObj.getUserConfiguration().hasInfostore()) {
							/*
							 * Append linked 'MyInfostore'
							 */
							final FolderObject myInfostore = foldersqlinterface.getUsersInfostoreFolder();
							lastModified = myInfostore.getLastModified() == null ? lastModified : Math.max(
									lastModified, myInfostore.getLastModified().getTime());
							folderWriter.writeOXFolderFieldsAsArray(columns, myInfostore, strHelper
									.getString(FolderStrings.MY_INFOSTORE_FOLDER_NAME), -1);
						}
					} else if (isSystemPublicFolder) {
						/*
						 * Append virtual root folder for non-tree visible
						 * infostore folders
						 */
						SearchIterator it = null;
						try {
							if ((it = foldersqlinterface.getNonTreeVisiblePublicCalendarFolders()).hasNext()) {
								final FolderObject virtualListFolder = FolderObject.createVirtualFolderObject(
										FolderObject.VIRTUAL_LIST_CALENDAR_FOLDER_ID, FolderObject.getFolderString(
												FolderObject.VIRTUAL_LIST_CALENDAR_FOLDER_ID, sessionObj.getLocale()),
										FolderObject.SYSTEM_MODULE, true, FolderObject.SYSTEM_TYPE);
								if (FolderCacheManager.isInitialized()) {
									FolderCacheManager.getInstance().putFolderObject(virtualListFolder,
											sessionObj.getContext());
								}
								folderWriter.writeOXFolderFieldsAsArray(columns, virtualListFolder);
							}
						} catch (final OXFolderException e) {
							if (e.getDetailNumber() == FolderCode.NO_MODULE_ACCESS.getNumber()
									&& Category.USER_CONFIGURATION.equals(e.getCategory())) {
								/*
								 * No non-tree-visible public calendar folders
								 * due to user configuration
								 */
								if (LOG.isTraceEnabled()) {
									LOG.trace(e.getMessage(), e);
								}
							} else {
								throw e;
							}
						} finally {
							if (it != null) {
								it.close();
								it = null;
							}
						}
						try {
							if ((it = foldersqlinterface.getNonTreeVisiblePublicContactFolders()).hasNext()) {
								final FolderObject virtualListFolder = FolderObject.createVirtualFolderObject(
										FolderObject.VIRTUAL_LIST_CONTACT_FOLDER_ID, FolderObject.getFolderString(
												FolderObject.VIRTUAL_LIST_CONTACT_FOLDER_ID, sessionObj.getLocale()),
										FolderObject.SYSTEM_MODULE, true, FolderObject.SYSTEM_TYPE);
								if (FolderCacheManager.isInitialized()) {
									FolderCacheManager.getInstance().putFolderObject(virtualListFolder,
											sessionObj.getContext());
								}
								folderWriter.writeOXFolderFieldsAsArray(columns, virtualListFolder);
							}
						} catch (final OXFolderException e) {
							if (e.getDetailNumber() == FolderCode.NO_MODULE_ACCESS.getNumber()
									&& Category.USER_CONFIGURATION.equals(e.getCategory())) {
								/*
								 * No non-tree-visible public contact folders
								 * due to user configuration
								 */
								if (LOG.isTraceEnabled()) {
									LOG.trace(e.getMessage(), e);
								}
							} else {
								throw e;
							}
						} finally {
							if (it != null) {
								it.close();
								it = null;
							}
						}
						try {
							if ((it = foldersqlinterface.getNonTreeVisiblePublicTaskFolders()).hasNext()) {
								final FolderObject virtualListFolder = FolderObject.createVirtualFolderObject(
										FolderObject.VIRTUAL_LIST_TASK_FOLDER_ID, FolderObject.getFolderString(
												FolderObject.VIRTUAL_LIST_TASK_FOLDER_ID, sessionObj.getLocale()),
										FolderObject.SYSTEM_MODULE, true, FolderObject.SYSTEM_TYPE);
								if (FolderCacheManager.isInitialized()) {
									FolderCacheManager.getInstance().putFolderObject(virtualListFolder,
											sessionObj.getContext());
								}
								folderWriter.writeOXFolderFieldsAsArray(columns, virtualListFolder);
							}
						} catch (final OXFolderException e) {
							if (e.getDetailNumber() == FolderCode.NO_MODULE_ACCESS.getNumber()
									&& Category.USER_CONFIGURATION.equals(e.getCategory())) {
								/*
								 * No non-tree-visible public task folders due
								 * to user configuration
								 */
								if (LOG.isTraceEnabled()) {
									LOG.trace(e.getMessage(), e);
								}
							} else {
								throw e;
							}
						} finally {
							if (it != null) {
								it.close();
								it = null;
							}
						}
					}
				}
				lastModifiedDate = lastModified == 0 ? null : new Date(lastModified);
			} else if (parentIdentifier.startsWith(FolderObject.SHARED_PREFIX)) {
				/*
				 * Client requests shared folders
				 */
				long lastModified = 0;
				final FolderSQLInterface foldersqlinterface = new RdbFolderSQLInterface(sessionObj);
				int sharedOwner;
				try {
					sharedOwner = Integer.parseInt(parentIdentifier.substring(2));
				} catch (final NumberFormatException exc) {
					LOG.error(exc.getMessage(), exc);
					throw getWrappingOXException(exc);
				}
				final FolderFieldWriter[] writers = folderWriter.getFolderFieldWriter(columns);
				final Queue<FolderObject> q = ((FolderObjectIterator) foldersqlinterface.getSharedFoldersFrom(
						sharedOwner, null)).asQueue();
				final int size = q.size();
				final Iterator<FolderObject> iter = q.iterator();
				for (int i = 0; i < size; i++) {
					final FolderObject sharedFolder = iter.next();
					lastModified = sharedFolder.getLastModified() == null ? lastModified : Math.max(lastModified,
							sharedFolder.getLastModified().getTime());
					jsonWriter.array();
					try {
						for (final FolderFieldWriter ffw : writers) {
							ffw.writeField(jsonWriter, sharedFolder, false, null, 0);
						}
					} finally {
						jsonWriter.endArray();
					}
				}
				lastModifiedDate = lastModified == 0 ? null : new Date(lastModified);
			} else {
				/*
				 * Determine if all folders, regardless of their subscription
				 * status, shall be included
				 */
				final boolean all = (STRING_1.equals(paramContainer.getStringParam(PARAMETER_ALL)));
				SearchIterator it = null;
				MailInterface mailInterface = null;
				try {
					final IMAPFolderFieldWriter[] writers = folderWriter.getIMAPFolderFieldWriter(columns);
					/*
					 * E-Mail folder
					 */
					mailInterface = MailInterfaceImpl.getInstance(sessionObj);
					it = mailInterface.getChildFolders(parentIdentifier, all);
					final int size = it.size();
					for (int i = 0; i < size; i++) {
						final MailFolderObject f = (MailFolderObject) it.next();
						if (f.getName().equals(STR_INBOX)) {
							jsonWriter.array();
							try {
								// TODO: Translation for INBOX?!
								for (int j = 0; j < writers.length; j++) {
									writers[j].writeField(jsonWriter, f, false, DEF_NAME_INBOX, -1, null, -1, all);
								}
							} finally {
								jsonWriter.endArray();
							}
						} else {
							jsonWriter.array();
							try {
								for (int j = 0; j < writers.length; j++) {
									writers[j].writeField(jsonWriter, f, false, null, -1, null, -1, all);
								}
							} finally {
								jsonWriter.endArray();
							}
						}
					}
				} finally {
					if (it != null) {
						it.close();
						it = null;
					}
					if (mailInterface != null) {
						try {
							mailInterface.close(true);
							mailInterface = null;
						} catch (final OXException e) {
							LOG.error(e.getMessage(), e);
						}
					}
				}
			}
		} catch (final OXFolderException e) {
			LOG.error(e.getMessage(), e);
			//if (!e.getCategory().equals(Category.USER_CONFIGURATION)) {
				response.setException(e);
			//}
		} catch (final AbstractOXException e) {
			LOG.error(e.getMessage(), e);
			response.setException(e);
		} catch (final Exception e) {
			LOG.error("actionGetSubfolders", e);
			response.setException(getWrappingOXException(e));
		}
		/*
		 * Close response and flush print writer
		 */
		jsonWriter.endArray();
		response.setData(new JSONArray(strWriter.toString()));
		response.setTimestamp(lastModifiedDate);
		Response.write(response, pw);
	}

	/**
	 * Performs the GET request to send back the path from a certain folder to
	 * root folder
	 * 
	 * @throws JSONException
	 * @throws SearchIteratorException
	 */
	public void actionGetPath(final SessionObject sessionObj, final Writer pw, final JSONObject requestObj)
			throws JSONException {
		actionGetPath(sessionObj, pw, ParamContainer.getInstance(requestObj, Component.FOLDER));
	}

	private final void actionGetPath(final HttpServletRequest req, final HttpServletResponse resp) throws IOException,
			ServletException {
		try {
			actionGetPath(getSessionObject(req), resp.getWriter(), ParamContainer.getInstance(req, Component.FOLDER,
					resp));
		} catch (final JSONException e) {
			sendErrorAsJS(resp, RESPONSE_ERROR);
		}
	}

	private final void actionGetPath(final SessionObject sessionObj, final Writer pw,
			final ParamContainer paramContainer) throws JSONException {
		/*
		 * Some variables
		 */
		final Response response = new Response();
		final StringWriter strWriter = new StringWriter();
		final JSONWriter jsonWriter = new JSONWriter(strWriter);
		long lastModified = 0;
		/*
		 * Start response
		 */
		jsonWriter.array();
		try {
			/*
			 * Read in parameters
			 */
			final String folderIdentifier = paramContainer.checkStringParam(PARAMETER_ID);
			final int[] columns = paramContainer.checkIntArrayParam(PARAMETER_COLUMNS);
			final FolderWriter folderWriter = new FolderWriter(jsonWriter, sessionObj);
			int folderId = -1;
			if ((folderId = getUnsignedInteger(folderIdentifier)) != -1) {
				folderId = FolderObject.mapVirtualID2SystemID(folderId);
				final FolderSQLInterface foldersqlinterface = new RdbFolderSQLInterface(sessionObj);
				/*
				 * Pre-Select field writers
				 */
				final FolderFieldWriter[] writers = folderWriter.getFolderFieldWriter(columns);
				final Queue<FolderObject> q = ((FolderObjectIterator) foldersqlinterface.getPathToRoot(folderId))
						.asQueue();
				final int size = q.size();
				final Iterator<FolderObject> iter = q.iterator();
				for (int i = 0; i < size; i++) {
					final FolderObject fo = iter.next();
					if (fo.containsLastModified()) {
						lastModified = fo.getLastModified().getTime() > lastModified ? fo.getLastModified().getTime()
								: lastModified;
					}
					jsonWriter.array();
					try {
						for (final FolderFieldWriter ffw : writers) {
							ffw.writeField(jsonWriter, fo, false);
						}
					} finally {
						jsonWriter.endArray();
					}
				}
			} else {
				MailInterface mailInterface = null;
				SearchIterator it = null;
				try {
					mailInterface = MailInterfaceImpl.getInstance(sessionObj);
					/*
					 * Pre-Select field writers
					 */
					final IMAPFolderFieldWriter[] writers = folderWriter.getIMAPFolderFieldWriter(columns);
					it = mailInterface.getPathToDefaultFolder(folderIdentifier);
					final int size = it.size();
					for (int i = 0; i < size; i++) {
						final MailFolderObject fld = (MailFolderObject) it.next();
						jsonWriter.array();
						try {
							for (final IMAPFolderFieldWriter ffw : writers) {
								ffw.writeField(jsonWriter, fld, false);
							}
						} finally {
							jsonWriter.endArray();
						}
					}
					it.close();
					it = null;
					/*
					 * Write virtual folder "E-Mail"
					 */
					final MailFolderObject defaultFolder = mailInterface.getFolder(
							MailFolderObject.DEFAULT_IMAP_FOLDER_ID, true);
					if (defaultFolder != null) {
						folderWriter.writeIMAPFolderAsArray(columns, defaultFolder,
								MailFolderObject.DEFAULT_IMAP_FOLDER_NAME, 1, MailFolderObject.DEFAULT_IMAP_FOLDER_ID,
								FolderObject.SYSTEM_MODULE);
					}
					/*
					 * Finally, write "private" folder
					 */
					FolderObject privateFolder;
					if (FolderCacheManager.isEnabled()) {
						privateFolder = FolderCacheManager.getInstance().getFolderObject(
								FolderObject.SYSTEM_PRIVATE_FOLDER_ID, true, sessionObj.getContext(), null);
					} else {
						privateFolder = FolderObject.loadFolderObjectFromDB(FolderObject.SYSTEM_PRIVATE_FOLDER_ID,
								sessionObj.getContext());
					}
					folderWriter.writeOXFolderFieldsAsArray(columns, privateFolder, FolderObject.getFolderString(
							FolderObject.SYSTEM_PRIVATE_FOLDER_ID, sessionObj.getLocale()), -1);
				} finally {
					if (it != null) {
						it.close();
						it = null;
					}
					if (mailInterface != null) {
						try {
							mailInterface.close(true);
							mailInterface = null;
						} catch (final OXException e) {
							LOG.error(e.getMessage(), e);
						}
					}
				}
			}
		} catch (final OXFolderException e) {
			LOG.error(e.getMessage(), e);
			//if (!e.getCategory().equals(Category.USER_CONFIGURATION)) {
				response.setException(e);
			//}
		} catch (final AbstractOXException e) {
			LOG.error(e.getMessage(), e);
			response.setException(e);
		} catch (final Exception e) {
			LOG.error("actionGetPath", e);
			response.setException(getWrappingOXException(e));
		}
		/*
		 * Close response and flush print writer
		 */
		jsonWriter.endArray();
		response.setData(new JSONArray(strWriter.toString()));
		response.setTimestamp(lastModified == 0 ? null : new Date(lastModified));
		Response.write(response, pw);
	}

	/**
	 * Performs the GET request to send back all modified folders since a
	 * certain timestamp
	 * 
	 * @param sessionObj
	 * @param pw
	 * @param requestObj
	 * @throws JSONException
	 * @throws SearchIteratorException
	 */
	public void actionGetUpdatedFolders(final SessionObject sessionObj, final Writer pw, final JSONObject requestObj)
			throws JSONException {
		actionGetUpdatedFolders(sessionObj, pw, ParamContainer.getInstance(requestObj, Component.FOLDER));
	}

	private final void actionGetUpdatedFolders(final HttpServletRequest req, final HttpServletResponse resp)
			throws IOException, ServletException {
		try {
			actionGetUpdatedFolders(getSessionObject(req), resp.getWriter(), ParamContainer.getInstance(req,
					Component.FOLDER, resp));
		} catch (final JSONException e) {
			sendErrorAsJS(resp, RESPONSE_ERROR);
		}
	}

	private final void actionGetUpdatedFolders(final SessionObject sessionObj, final Writer pw,
			final ParamContainer paramContainer) throws JSONException {
		/*
		 * Some variables
		 */
		final Response response = new Response();
		final StringWriter strWriter = new StringWriter();
		final JSONWriter jsonWriter = new JSONWriter(strWriter);
		Date lastModifiedDate = null;
		/*
		 * Start response
		 */
		jsonWriter.array();
		try {
			long lastModified = 0;
			/*
			 * Read in parameters
			 */
			final int[] columns = paramContainer.checkIntArrayParam(PARAMETER_COLUMNS);
			final FolderWriter folderWriter = new FolderWriter(jsonWriter, sessionObj);
			final Date timestamp = paramContainer.checkDateParam(PARAMETER_TIMESTAMP);
			final boolean includeMailFolders = STRING_1.equals(paramContainer.getStringParam(PARAMETER_MAIL));
			lastModified = Math.max(timestamp.getTime(), lastModified);
			final FolderSQLInterface foldersqlinterface = new RdbFolderSQLInterface(sessionObj);
			final FolderFieldWriter[] writers = folderWriter.getFolderFieldWriter(columns);
			/*
			 * Get all updated OX folders
			 */
			Queue<FolderObject> q = ((FolderObjectIterator) foldersqlinterface.getAllModifiedFolders(timestamp))
					.asQueue();
			final Queue<FolderObject> updatedQueue = new LinkedList<FolderObject>();
			final Queue<FolderObject> deletedQueue = new LinkedList<FolderObject>();
			boolean add2Update = false;
			int size = q.size();
			Iterator<FolderObject> iter = q.iterator();
			for (int i = 0; i < size; i++) {
				final FolderObject fo = iter.next();
				if (fo.isVisible(sessionObj.getUserObject().getId(), sessionObj.getUserConfiguration())) {
					if (fo.isShared(sessionObj.getUserObject().getId())) {
						add2Update = true;
					}
					updatedQueue.add(fo);
				} else {
					deletedQueue.add(fo);
				}
			}
			/*
			 * Check if shared folder must be updated, too
			 */
			if (add2Update) {
				final FolderObject sharedFolder = new OXFolderAccess(sessionObj.getContext())
						.getFolderObject(FolderObject.SYSTEM_SHARED_FOLDER_ID);
				sharedFolder.setFolderName(FolderObject.getFolderString(FolderObject.SYSTEM_SHARED_FOLDER_ID,
						sessionObj.getLocale()));
				updatedQueue.add(sharedFolder);
			}
			/*
			 * Output updated folders
			 */
			size = updatedQueue.size();
			iter = updatedQueue.iterator();
			for (int i = 0; i < size; i++) {
				final FolderObject fo = iter.next();
				lastModified = Math.max(fo.getLastModified().getTime(), lastModified);
				jsonWriter.array();
				try {
					for (final FolderFieldWriter ffw : writers) {
						ffw.writeField(jsonWriter, fo, false);
					}
				} finally {
					jsonWriter.endArray();
				}
			}
			/*
			 * Get deleted OX folders
			 */
			q = ((FolderObjectIterator) foldersqlinterface.getDeletedFolders(timestamp)).asQueue();
			/*
			 * Add deleted OX folders from above
			 */
			q.addAll(deletedQueue);
			final FolderFieldWriter idWriter = folderWriter.getFolderFieldWriter(new int[] { FolderObject.OBJECT_ID })[0];
			size = q.size();
			iter = q.iterator();
			for (int i = 0; i < size; i++) {
				final FolderObject fo = iter.next();
				lastModified = Math.max(fo.getLastModified().getTime(), lastModified);
				jsonWriter.array();
				try {
					idWriter.writeField(jsonWriter, fo, false);
				} finally {
					jsonWriter.endArray();
				}
			}
			if (includeMailFolders) {
				/*
				 * Clean session caches
				 */
				sessionObj.cleanIMAPCaches();
				/*
				 * Append mail folders
				 */
				MailInterface mailInterface = null;
				SearchIterator it = null;
				try {
					mailInterface = MailInterfaceImpl.getInstance(sessionObj);
					it = mailInterface.getRootFolders();
					final int size2 = it.size();
					for (int a = 0; a < size2; a++) {
						final MailFolderObject rootFolder = (MailFolderObject) it.next();
						folderWriter.writeIMAPFolderAsArray(columns, rootFolder,
								MailFolderObject.DEFAULT_IMAP_FOLDER_NAME, 1, MailFolderObject.DEFAULT_IMAP_FOLDER_ID,
								FolderObject.SYSTEM_MODULE);
					}
				} catch (final OXException e) {
					LOG.error(e.getMessage(), e);
				} finally {
					if (it != null) {
						it.close();
						it = null;
					}
					if (mailInterface != null) {
						try {
							mailInterface.close(true);
							mailInterface = null;
						} catch (final OXException e) {
							LOG.error(e.getMessage(), e);
						}
					}
				}
			}
			/*
			 * Set timestamp
			 */
			lastModifiedDate = lastModified == 0 ? null : new Date(lastModified);
		} catch (final OXFolderException e) {
			LOG.error(e.getMessage(), e);
			response.setException(e);
		} catch (final AbstractOXException e) {
			LOG.error(e.getMessage(), e);
			response.setException(e);
		} catch (final Exception e) {
			LOG.error("actionGetUpdatedFolders", e);
			response.setException(getWrappingOXException(e));
		}
		/*
		 * Close response and flush print writer
		 */
		jsonWriter.endArray();
		response.setData(new JSONArray(strWriter.toString()));
		response.setTimestamp(lastModifiedDate);
		Response.write(response, pw);
	}

	public void actionGetFolder(final SessionObject sessionObj, final Writer pw, final JSONObject requestObj)
			throws JSONException {
		actionGetFolder(sessionObj, pw, ParamContainer.getInstance(requestObj, Component.FOLDER));
	}

	private final void actionGetFolder(final HttpServletRequest req, final HttpServletResponse resp)
			throws IOException, ServletException {
		try {
			actionGetFolder(getSessionObject(req), resp.getWriter(), ParamContainer.getInstance(req, Component.FOLDER,
					resp));
		} catch (final JSONException e) {
			sendErrorAsJS(resp, RESPONSE_ERROR);
		}
	}

	private final void actionGetFolder(final SessionObject sessionObj, final Writer pw,
			final ParamContainer paramContainer) throws JSONException {
		/*
		 * Some variables
		 */
		final Response response = new Response();
		final StringWriter strWriter = new StringWriter();
		final JSONWriter jsonWriter = new JSONWriter(strWriter);
		Date lastModifiedDate = null;
		/*
		 * Start response
		 */
		boolean valueWritten = true;
		try {
			final String folderIdentifier = paramContainer.checkStringParam(PARAMETER_ID);
			final int[] columns = paramContainer.checkIntArrayParam(PARAMETER_COLUMNS);
			final FolderWriter folderWriter = new FolderWriter(jsonWriter, sessionObj);
			int folderId = -1;
			if ((folderId = getUnsignedInteger(folderIdentifier)) != -1) {
				folderId = FolderObject.mapVirtualID2SystemID(folderId);
				final FolderSQLInterface foldersqlinterface = new RdbFolderSQLInterface(sessionObj);
				final FolderObject fo = foldersqlinterface.getFolderById(folderId);
				lastModifiedDate = fo.getLastModified();
				valueWritten = false;
				folderWriter.writeOXFolderFieldsAsObject(columns, fo);
				valueWritten = true;
			} else {
				MailInterface mailInterface = null;
				try {
					mailInterface = MailInterfaceImpl.getInstance(sessionObj);
					final MailFolderObject f = mailInterface.getFolder(folderIdentifier, true);
					valueWritten = false;
					folderWriter.writeIMAPFolderAsObject(columns, f);
					valueWritten = true;
				} finally {
					try {
						if (mailInterface != null) {
							mailInterface.close(true);
						}
					} catch (final OXException e) {
						LOG.error(e.getMessage(), e);
					}
				}
			}
		} catch (final OXFolderException e) {
			LOG.error(e.getMessage(), e);
			response.setException(e);
		} catch (final AbstractOXException e) {
			LOG.error(e.getMessage(), e);
			response.setException(e);
		} catch (final Exception e) {
			LOG.error("actionGetFolder", e);
			response.setException(getWrappingOXException(e));
		} finally {
			if (!valueWritten) {
				jsonWriter.value(JSONObject.NULL);
			}
		}
		/*
		 * Close response and flush print writer
		 */
		final String data = strWriter.toString();
		if (data.length() > 0) {
			response.setData(new JSONObject(data));
		}
		response.setTimestamp(lastModifiedDate);
		Response.write(response, pw);
	}

	public void actionPutUpdateFolder(final SessionObject sessionObj, final Writer pw, final JSONObject requestObj)
			throws JSONException {
		actionPutUpdateFolder(sessionObj, pw, requestObj.getString(JSON_KEY_DATA), ParamContainer.getInstance(
				requestObj, Component.FOLDER));
	}

	private final void actionPutUpdateFolder(final HttpServletRequest req, final HttpServletResponse resp)
			throws IOException, ServletException {
		try {
			actionPutUpdateFolder(getSessionObject(req), resp.getWriter(), getBody(req), ParamContainer.getInstance(
					req, Component.FOLDER, resp));
		} catch (final JSONException e) {
			sendErrorAsJS(resp, RESPONSE_ERROR);
		}
	}

	private final void actionPutUpdateFolder(final SessionObject sessionObj, final Writer pw, final String body,
			final ParamContainer paramContainer) throws JSONException {
		/*
		 * Some variables
		 */
		final Response response = new Response();
		Date lastModifiedDate = null;
		Object retval = JSONObject.NULL;
		/*
		 * Start response
		 */
		try {
			final String folderIdentifier = paramContainer.checkStringParam(PARAMETER_ID);
			Date timestamp = null;
			final JSONObject jsonObj = new JSONObject(body);
			int updateFolderId = -1;
			if ((updateFolderId = getUnsignedInteger(folderIdentifier)) != -1) {
				updateFolderId = FolderObject.mapVirtualID2SystemID(updateFolderId);
				timestamp = paramContainer.checkDateParam(PARAMETER_TIMESTAMP);
				final FolderSQLInterface foldersqlinterface = new RdbFolderSQLInterface(sessionObj);
				FolderObject fo = new FolderObject(updateFolderId);
				new FolderParser(sessionObj.getUserConfiguration()).parse(fo, jsonObj);
				fo = foldersqlinterface.saveFolderObject(fo, timestamp);
				retval = String.valueOf(fo.getObjectID());
				lastModifiedDate = fo.getLastModified();
			} else {
				final MailInterface mailInterface = MailInterfaceImpl.getInstance(sessionObj);
				try {
					final MailFolderObject updateFolder = mailInterface.getFolder(folderIdentifier, true);
					if (updateFolder != null) {
						final MailFolderObject mfo = new MailFolderObject(sessionObj.getUserObject().getId(), updateFolder.getFullName(), updateFolder
								.exists());
						mfo.setImapFolder(updateFolder.getImapFolder());
						mfo.setSeparator(updateFolder.getSeparator());
						new MailFolderParser(sessionObj).parse(mfo, jsonObj);
						retval = mailInterface.saveFolder(mfo);
					}
				} finally {
					try {
						mailInterface.close(true);
					} catch (final OXException e) {
						LOG.error(e.getMessage(), e);
					}
				}
			}
		} catch (final OXFolderException e) {
			LOG.error(e.getMessage(), e);
			//if (!e.getCategory().equals(Category.USER_CONFIGURATION)) {
				response.setException(e);
			//}
		} catch (final AbstractOXException e) {
			LOG.error(e.getMessage(), e);
			response.setException(e);
		} catch (final Exception e) {
			LOG.error("actionPutUpdateFolder", e);
			response.setException(getWrappingOXException(e));
		}
		/*
		 * Close response and flush print writer
		 */
		response.setData(retval);
		response.setTimestamp(lastModifiedDate);
		Response.write(response, pw);
	}

	public void actionPutInsertFolder(final SessionObject sessionObj, final Writer pw, final JSONObject requestObj)
			throws JSONException {
		actionPutInsertFolder(sessionObj, pw, requestObj.getString(JSON_KEY_DATA), ParamContainer.getInstance(
				requestObj, Component.FOLDER));
	}

	private final void actionPutInsertFolder(final HttpServletRequest req, final HttpServletResponse resp)
			throws IOException, ServletException {
		try {
			actionPutInsertFolder(getSessionObject(req), resp.getWriter(), getBody(req), ParamContainer.getInstance(
					req, Component.FOLDER, resp));
		} catch (final JSONException e) {
			sendErrorAsJS(resp, RESPONSE_ERROR);
		}
	}

	private final void actionPutInsertFolder(final SessionObject sessionObj, final Writer pw, final String body,
			final ParamContainer paramContainer) throws JSONException {
		/*
		 * Some variables
		 */
		final Response response = new Response();
		Date lastModifiedDate = null;
		Object retval = JSONObject.NULL;
		/*
		 * Start response
		 */
		try {
			final String parentFolder = paramContainer.checkStringParam(FolderFields.FOLDER_ID);
			final JSONObject jsonObj = new JSONObject(body);
			int parentFolderId = -1;
			if ((parentFolderId = getUnsignedInteger(parentFolder)) != -1) {
				parentFolderId = FolderObject.mapVirtualID2SystemID(parentFolderId);
				final FolderSQLInterface foldersqlinterface = new RdbFolderSQLInterface(sessionObj);
				FolderObject fo = new FolderObject();
				fo.setParentFolderID(parentFolderId);
				new FolderParser(sessionObj.getUserConfiguration()).parse(fo, jsonObj);
				fo = foldersqlinterface.saveFolderObject(fo, null);
				retval = String.valueOf(fo.getObjectID());
				lastModifiedDate = fo.getLastModified();
			} else {
				final MailInterface mailInterface = MailInterfaceImpl.getInstance(sessionObj);
				try {
					final MailFolderObject mfo = new MailFolderObject(sessionObj.getUserObject().getId());
					mfo.setParentFullName(parentFolder);
					new MailFolderParser(sessionObj).parse(mfo, jsonObj);
					retval = mailInterface.saveFolder(mfo);
				} finally {
					try {
						mailInterface.close(true);
					} catch (final OXException e) {
						LOG.error(e.getMessage(), e);
					}
				}
			}
		} catch (final OXFolderException e) {
			LOG.error(e.getMessage(), e);
			//if (!e.getCategory().equals(Category.USER_CONFIGURATION)) {
				response.setException(e);
			//}
		} catch (final AbstractOXException e) {
			LOG.error(e.getMessage(), e);
			response.setException(e);
		} catch (final Exception e) {
			LOG.error("actionPutInsertFolder", e);
			response.setException(getWrappingOXException(e));
		}
		/*
		 * Close response and flush print writer
		 */
		response.setData(retval);
		response.setTimestamp(lastModifiedDate);
		Response.write(response, pw);
	}

	public void actionPutDeleteFolder(final SessionObject sessionObj, final Writer pw, final JSONObject requestObj)
			throws JSONException {
		actionPutDeleteFolder(sessionObj, pw, requestObj.getString(JSON_KEY_DATA), ParamContainer.getInstance(
				requestObj, Component.FOLDER));
	}

	private final void actionPutDeleteFolder(final HttpServletRequest req, final HttpServletResponse resp)
			throws IOException, ServletException {
		try {
			actionPutDeleteFolder(getSessionObject(req), resp.getWriter(), getBody(req), ParamContainer.getInstance(
					req, Component.FOLDER, resp));
		} catch (final JSONException e) {
			sendErrorAsJS(resp, RESPONSE_ERROR);
		}
	}

	private final void actionPutDeleteFolder(final SessionObject sessionObj, final Writer pw, final String body,
			final ParamContainer paramContainer) throws JSONException {
		/*
		 * Some variables
		 */
		final Response response = new Response();
		final StringWriter strWriter = new StringWriter();
		final JSONWriter jsonWriter = new JSONWriter(strWriter);
		Date lastModifiedDate = null;
		/*
		 * Start response
		 */
		jsonWriter.array();
		try {
			Date timestamp = null;
			final JSONArray jsonArr = new JSONArray(body);
			FolderSQLInterface foldersqlinterface = null;
			MailInterface mailInterface = null;
			try {
				long lastModified = 0;
				final int arrayLength = jsonArr.length();
				final OXFolderAccess access = new OXFolderAccess(sessionObj.getContext());
				NextId: for (int i = 0; i < arrayLength; i++) {
					final String deleteIdentifier = jsonArr.getString(i);
					int delFolderId = -1;
					if ((delFolderId = getUnsignedInteger(deleteIdentifier)) != -1) {
						delFolderId = FolderObject.mapVirtualID2SystemID(delFolderId);
						if (timestamp == null) {
							timestamp = paramContainer.checkDateParam(PARAMETER_TIMESTAMP);
						}
						if (foldersqlinterface == null) {
							foldersqlinterface = new RdbFolderSQLInterface(sessionObj, access);
						}
						FolderObject delFolderObj;
						try {
							delFolderObj = access.getFolderObject(delFolderId);
						} catch (final OXException exc) {
							/*
							 * Folder could not be found and therefore need not
							 * to be deleted
							 */
							continue NextId;
						}
						if (delFolderObj.getLastModified().getTime() > timestamp.getTime()) {
							jsonWriter.value(delFolderObj.getObjectID());
							continue NextId;
						}
						foldersqlinterface.deleteFolderObject(delFolderObj, timestamp);
						lastModified = Math.max(lastModified, delFolderObj.getLastModified().getTime());
					} else {
						if (sessionObj.getUserConfiguration().hasWebMail()) {
							if (mailInterface == null) {
								mailInterface = MailInterfaceImpl.getInstance(sessionObj);
							}
							mailInterface.deleteFolder(deleteIdentifier);
						} else {
							jsonWriter.value(deleteIdentifier);
						}
					}
				}
				if (lastModified != 0) {
					lastModifiedDate = new Date(lastModified);
				}
			} finally {
				if (mailInterface != null) {
					mailInterface.close(true);
					mailInterface = null;
				}
			}
		} catch (final OXFolderException e) {
			LOG.error(e.getMessage(), e);
			//if (!e.getCategory().equals(Category.USER_CONFIGURATION)) {
				response.setException(e);
			//}
		} catch (final AbstractOXException e) {
			LOG.error(e.getMessage(), e);
			response.setException(e);
		} catch (final Exception e) {
			LOG.error("actionPutInsertFolder", e);
			response.setException(getWrappingOXException(e));
		}
		/*
		 * Close response and flush print writer
		 */
		jsonWriter.endArray();
		response.setData(new JSONArray(strWriter.toString()));
		response.setTimestamp(lastModifiedDate);
		Response.write(response, pw);
	}

	private final void actionPutRemoveTestFolder(final HttpServletRequest req, final HttpServletResponse resp)
			throws Exception {
		/*
		 * Some variables
		 */
		final JSONWriter jsonWriter = new JSONWriter(resp.getWriter());
		final SessionObject sessionObj = getSessionObject(req);
		final long lastModified = 0;
		String error = null;
		/*
		 * Start response
		 */
		AJAXServlet.startResponse(jsonWriter);
		String dataObj = "FAILED";
		try {
			final int[] delids = checkIntArrayParam(req, "del_ids");
			final OXFolderManagerImpl oxma = new OXFolderManagerImpl(sessionObj);
			oxma.cleanUpTestFolders(delids, sessionObj.getContext());
			dataObj = "OK";
		} catch (final Exception e) {
			LOG.error("actionPutRemoveTestFolder", e);
			error = e.toString();
		}
		/*
		 * Close response and flush print writer
		 */
		jsonWriter.value(dataObj);
		AJAXServlet.endResponse(jsonWriter, new Date(lastModified), error);
		resp.getWriter().flush();
	}

	private static final String checkStringParam(final HttpServletRequest req, final String paramName)
			throws OXException {
		final String paramVal = req.getParameter(paramName);
		if (paramVal == null) {
			throw new OXFolderException(FolderCode.MISSING_PARAMETER, paramName);
		}
		return paramVal;
	}

	private static final int[] checkIntArrayParam(final HttpServletRequest req, final String paramName)
			throws OXException {
		String tmp = req.getParameter(paramName);
		if (tmp == null) {
			throw new OXFolderException(FolderCode.MISSING_PARAMETER, paramName);
		}
		final String[] sa = tmp.split(SPLIT_PAT);
		tmp = null;
		final int intArray[] = new int[sa.length];
		for (int a = 0; a < sa.length; a++) {
			try {
				intArray[a] = Integer.parseInt(sa[a]);
			} catch (final NumberFormatException e) {
				throw new OXFolderException(FolderCode.BAD_PARAM_VALUE, sa[a], paramName);
			}
		}
		return intArray;
	}

	public static final int getUnsignedInteger(final String str) {
		try {
			return Integer.parseInt(str);
		} catch (final NumberFormatException e) {
			return -1;
		}
	}

}
