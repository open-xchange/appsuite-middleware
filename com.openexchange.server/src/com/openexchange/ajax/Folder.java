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

package com.openexchange.ajax;

import static com.openexchange.java.util.Tools.getUnsignedInteger;
import static com.openexchange.tools.oxfolder.OXFolderUtility.folderModule2String;
import java.io.IOException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.regex.Pattern;
import javax.mail.MessagingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONWriter;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.customizer.folder.AdditionalFolderFieldList;
import com.openexchange.ajax.customizer.folder.BulkAdditionalFolderFieldsList;
import com.openexchange.ajax.fields.FolderChildFields;
import com.openexchange.ajax.fields.ResponseFields;
import com.openexchange.ajax.helper.ParamContainer;
import com.openexchange.ajax.parser.FolderParser;
import com.openexchange.ajax.parser.MessagingFolderParser;
import com.openexchange.ajax.writer.FolderWriter;
import com.openexchange.ajax.writer.FolderWriter.FolderFieldWriter;
import com.openexchange.ajax.writer.MessagingFolderWriter;
import com.openexchange.ajax.writer.MessagingFolderWriter.MessagingFolderFieldWriter;
import com.openexchange.ajax.writer.ResponseWriter;
import com.openexchange.api2.FolderSQLInterface;
import com.openexchange.api2.RdbFolderSQLInterface;
import com.openexchange.cache.impl.FolderCacheManager;
import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXException.Generic;
import com.openexchange.exception.OXExceptionConstants;
import com.openexchange.folderstorage.FolderServiceDecorator;
import com.openexchange.folderstorage.messaging.MessagingFolderIdentifier;
import com.openexchange.groupware.container.DataObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.i18n.Groups;
import com.openexchange.groupware.i18n.MailStrings;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.tools.iterator.FolderObjectIterator;
import com.openexchange.groupware.userconfiguration.UserConfigurationStorage;
import com.openexchange.groupware.userconfiguration.UserPermissionBits;
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.java.Collators;
import com.openexchange.json.OXJSONWriter;
import com.openexchange.mail.FullnameArgument;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.MailServletInterface;
import com.openexchange.mail.MailSessionCache;
import com.openexchange.mail.MailSessionParameterNames;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.cache.SessionMailCache;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mail.dataobjects.MailFolderDescription;
import com.openexchange.mail.json.writer.FolderWriter.MailFolderFieldWriter;
import com.openexchange.mail.messaging.MailMessagingService;
import com.openexchange.mail.mime.MimeMailException;
import com.openexchange.mail.utils.MailFolderUtility;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.mailaccount.UnifiedInboxManagement;
import com.openexchange.messaging.DefaultMessagingFolder;
import com.openexchange.messaging.MessagingAccount;
import com.openexchange.messaging.MessagingAccountAccess;
import com.openexchange.messaging.MessagingExceptionCodes;
import com.openexchange.messaging.MessagingFolder;
import com.openexchange.messaging.MessagingFolderAccess;
import com.openexchange.messaging.MessagingService;
import com.openexchange.messaging.registry.MessagingServiceRegistry;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.threadpool.AbstractTask;
import com.openexchange.threadpool.CompletionFuture;
import com.openexchange.threadpool.Task;
import com.openexchange.threadpool.ThreadPools;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.oxfolder.OXFolderAccess;
import com.openexchange.tools.oxfolder.OXFolderExceptionCode;
import com.openexchange.tools.oxfolder.OXFolderManager;
import com.openexchange.tools.servlet.OXJSONExceptionCodes;
import com.openexchange.tools.servlet.http.Tools;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link Folder} - The folder servlet.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class Folder extends SessionServlet implements OXExceptionConstants {

    private static final String STR_INBOX = "INBOX";

    private static final int DEFAULT_MAX_RUNNING_MILLIS = 120000;

    private static final long serialVersionUID = -889739420660750770L;

    private static transient final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Folder.class);

    private static final AdditionalFolderFieldList FIELDS = new AdditionalFolderFieldList();

    public static AdditionalFolderFieldList getAdditionalFields() {
        return FIELDS;
    }

    private static final OXException getWrappingOXException(final Throwable cause) {
        LOG.warn("An unexpected exception occurred, which is going to be wrapped for proper display.\nFor safety reason its original content is display here.", cause);
        final String message = cause.getMessage();
        return OXFolderExceptionCode.UNKNOWN_EXCEPTION.create(cause, null == message ? "[Not available]" : message);
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
     * The actual max permission that can be transfered in field 'bits' or JSON's permission object
     */
    public static final int MAX_PERMISSION = 64;

    private static final String STRING_1 = "1";

    private static final String STRING_DELETED = "deleted";

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

    @Override
    protected void doPut(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        resp.setContentType(CONTENTTYPE_JAVASCRIPT);
        Tools.disableCaching(resp);
        try {
            actionPut(req, resp);
        } catch (final Exception e) {
            LOG.error("doPut", e);
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
            throw getWrappingOXException(new Exception("Action \"" + actionStr + "\" NOT supported via GET on module folders"));
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
        } else if (actionStr.equalsIgnoreCase(ACTION_CLEAR)) {
            actionPutClearFolder(req, resp);
        } else if (actionStr.equalsIgnoreCase("removetestfolders")) {
            actionPutRemoveTestFolder(req, resp);
        } else {
            throw getWrappingOXException(new Exception("Action \"" + actionStr + "\" NOT supported via PUT on module folders"));
        }
    }

    /**
     * Performs the GET request to send back root folders
     */
    public void actionGetRoot(final ServerSession session, final JSONWriter w, final JSONObject requestObj) throws JSONException {
        ResponseWriter.write(actionGetRoot(session, ParamContainer.getInstance(requestObj)), w, localeFrom(session));
    }

    private final void actionGetRoot(final HttpServletRequest req, final HttpServletResponse resp) throws JSONException, IOException {
        final ServerSession session = getSessionObject(req);
        ResponseWriter.write(
            actionGetRoot(session, ParamContainer.getInstance(req, resp)),
            resp.getWriter(), localeFrom(session));
    }

    /**
     * Performs the GET request to send back root folders
     */
    private final Response actionGetRoot(final ServerSession session, final ParamContainer paramContainer) throws JSONException {
        /*
         * Some variables
         */
        final Response response = new Response(session);
        final OXJSONWriter jsonWriter = new OXJSONWriter();
        long lastModified = 0;
        /*
         * Start response
         */
        jsonWriter.array();
        try {
            /*
             * Read in parameters
             */

            final Context ctx = session.getContext();
            final int[] columns = paramContainer.checkIntArrayParam(PARAMETER_COLUMNS);

            final FolderSQLInterface foldersqlinterface = new RdbFolderSQLInterface(session);
            final String timeZoneId = paramContainer.getStringParam(PARAMETER_TIMEZONE);
            final FolderWriter folderWriter = new FolderWriter(jsonWriter, session, ctx, timeZoneId, FIELDS);
            final FolderFieldWriter[] writers = folderWriter.getFolderFieldWriter(columns);
            final Queue<FolderObject> q = ((FolderObjectIterator) foldersqlinterface.getRootFolderForUser()).asQueue();
            final int size = q.size();
            final Iterator<FolderObject> iter = q.iterator();
            NextRootFolder: for (int i = 0; i < size; i++) {
                final FolderObject rootFolder = iter.next();
                int hasSubfolder = -1;
                if (rootFolder.getObjectID() == FolderObject.SYSTEM_FOLDER_ID || rootFolder.getObjectID() == FolderObject.SYSTEM_OX_FOLDER_ID) {
                    /*
                     * Ignore 'system' and 'ox folder' folder
                     */
                    continue NextRootFolder;
                } else if (rootFolder.getObjectID() == FolderObject.SYSTEM_SHARED_FOLDER_ID && !session.getUserPermissionBits().hasFullSharedFolderAccess()) {
                    /*
                     * User does not hold READ_CREATE_SHARED_FOLDERS in user configuration; mark system shared folder to have no subfolders
                     */
                    hasSubfolder = 0;
                }
                final Date modified = rootFolder.getLastModified();
                lastModified = modified == null ? lastModified : Math.max(lastModified, modified.getTime());
                jsonWriter.array();
                try {
                    for (final FolderFieldWriter ffw : writers) {
                        ffw.writeField(jsonWriter, rootFolder, false, FolderObject.getFolderString(
                            rootFolder.getObjectID(),
                            session.getUser().getLocale()), hasSubfolder);
                    }
                } finally {
                    jsonWriter.endArray();
                }
            }
        } catch (final OXException e) {
            LOG.error("", e);
            response.setException(e);
        } catch (final Exception e) {
            final OXException wrapper = getWrappingOXException(e);
            LOG.error("", wrapper);
            response.setException(wrapper);
        }
        /*
         * Close response and flush print writer
         */
        jsonWriter.endArray();
        response.setData(jsonWriter.getObject());
        response.setTimestamp(lastModified == 0 ? null : new Date(lastModified));
        return response;
    }

    /**
     * Performs the GET request to back certain folder's subfolders
     *
     * @param session
     * @param w
     * @param requestObj
     * @throws JSONException
     */
    public void actionGetSubfolders(final ServerSession session, final JSONWriter w, final JSONObject requestObj) throws JSONException {
        ResponseWriter.write(actionGetSubfolders(session, ParamContainer.getInstance(requestObj)), w, localeFrom(session));
    }

    private final void actionGetSubfolders(final HttpServletRequest req, final HttpServletResponse resp) throws IOException, ServletException {
        ServerSession session = getSessionObject(req);
        try {
            ResponseWriter.write(
                actionGetSubfolders(session, ParamContainer.getInstance(req, resp)),
                resp.getWriter(), localeFrom(session));
        } catch (final JSONException e) {
            try {
                ResponseWriter.writeException(OXJSONExceptionCodes.JSON_WRITE_ERROR.create(e, new Object[0]), new JSONWriter(
                    resp.getWriter()), localeFrom(session));
            } catch (final JSONException jsonError) {
                throw new ServletException(e.getMessage(), jsonError);
            }
        }
    }

    private final Response actionGetSubfolders(final ServerSession session, final ParamContainer paramContainer) throws JSONException {
        /*
         * Some variables
         */
        final Response response = new Response(session);
        final OXJSONWriter jsonWriter = new OXJSONWriter();
        OXException warning = null;
        Date lastModifiedDate = null;
        /*
         * Start response
         */
        jsonWriter.array();
        try {
            final Context ctx = session.getContext();
            final Locale locale = session.getUser().getLocale();
            final StringHelper strHelper = StringHelper.valueOf(locale);
            /*
             * Read in parameters
             */
            final int[] columns = paramContainer.checkIntArrayParam(PARAMETER_COLUMNS);
            final String parentIdentifier = paramContainer.checkStringParam(PARAMETER_PARENT);
            final String ignore = paramContainer.getStringParam(PARAMETER_IGNORE);
            final String timeZoneId = paramContainer.getStringParam(PARAMETER_TIMEZONE);
            boolean ignoreMailfolder = false;
            if (ignore != null && "mailfolder".equalsIgnoreCase(ignore)) {
                ignoreMailfolder = true;
            }
            final BulkAdditionalFolderFieldsList fieldList = new BulkAdditionalFolderFieldsList(FIELDS);
            final FolderWriter folderWriter = new FolderWriter(jsonWriter, session, ctx, timeZoneId, fieldList);
            int parentId = -1;
            if ((parentId = getUnsignedInteger(parentIdentifier)) >= 0) {
                // TODO: DELEGATE TO getRootFolder() if parentId is "0"
                long lastModified = Long.MIN_VALUE;
                final FolderSQLInterface foldersqlinterface = new RdbFolderSQLInterface(session);
                final FolderFieldWriter[] writers = folderWriter.getFolderFieldWriter(columns);
                /*
                 * Write requested child folders
                 */
                if (parentId == FolderObject.VIRTUAL_LIST_TASK_FOLDER_ID) {
                    /*
                     * Append non-tree visible task folders
                     */
                    final Queue<FolderObject> q =
                        ((FolderObjectIterator) foldersqlinterface.getNonTreeVisiblePublicTaskFolders()).asQueue();
                    fieldList.warmUp(q, session);
                    final int size = q.size();
                    final Iterator<FolderObject> iter = q.iterator();
                    for (int i = 0; i < size; i++) {
                        final FolderObject listFolder = iter.next();
                        final Date modified = listFolder.getLastModified();
                        lastModified = null == modified ? lastModified : Math.max(lastModified, modified.getTime());
                        jsonWriter.array();
                        try {
                            for (final FolderFieldWriter writer : writers) {
                                writer.writeField(jsonWriter, listFolder, false);
                            }
                        } finally {
                            jsonWriter.endArray();
                        }
                    }
                } else if (parentId == FolderObject.VIRTUAL_LIST_CALENDAR_FOLDER_ID) {
                    /*
                     * Append non-tree visible calendar folders
                     */
                    final Queue<FolderObject> q =
                        ((FolderObjectIterator) foldersqlinterface.getNonTreeVisiblePublicCalendarFolders()).asQueue();
                    fieldList.warmUp(q, session);
                    final int size = q.size();
                    final Iterator<FolderObject> iter = q.iterator();
                    for (int i = 0; i < size; i++) {
                        final FolderObject listFolder = iter.next();
                        final Date modified = listFolder.getLastModified();
                        lastModified = null == modified ? lastModified : Math.max(lastModified, modified.getTime());
                        jsonWriter.array();
                        try {
                            for (final FolderFieldWriter writer : writers) {
                                writer.writeField(jsonWriter, listFolder, false);
                            }
                        } finally {
                            jsonWriter.endArray();
                        }
                    }
                } else if (parentId == FolderObject.VIRTUAL_LIST_CONTACT_FOLDER_ID) {
                    /*
                     * Append non-tree visible contact folders
                     */
                    final Queue<FolderObject> q =
                        ((FolderObjectIterator) foldersqlinterface.getNonTreeVisiblePublicContactFolders()).asQueue();
                    fieldList.warmUp(q, session);
                    final int size = q.size();
                    final Iterator<FolderObject> iter = q.iterator();
                    for (int i = 0; i < size; i++) {
                        final FolderObject listFolder = iter.next();
                        final Date modified = listFolder.getLastModified();
                        lastModified = null == modified ? lastModified : Math.max(lastModified, modified.getTime());
                        jsonWriter.array();
                        try {
                            for (final FolderFieldWriter writer : writers) {
                                writer.writeField(jsonWriter, listFolder, false);
                            }
                        } finally {
                            jsonWriter.endArray();
                        }
                    }
                } else if (parentId == FolderObject.VIRTUAL_LIST_INFOSTORE_FOLDER_ID) {
                    /*
                     * Append non-tree visible infostore folders
                     */
                    final Queue<FolderObject> q =
                        ((FolderObjectIterator) foldersqlinterface.getNonTreeVisiblePublicInfostoreFolders()).asQueue();
                    fieldList.warmUp(q, session);
                    final int size = q.size();
                    final Iterator<FolderObject> iter = q.iterator();
                    for (int i = 0; i < size; i++) {
                        final FolderObject listFolder = iter.next();
                        final Date modified = listFolder.getLastModified();
                        lastModified = modified == null ? lastModified : Math.max(lastModified, modified.getTime());
                        jsonWriter.array();
                        try {
                            for (final FolderFieldWriter writer : writers) {
                                writer.writeField(jsonWriter, listFolder, false);
                            }
                        } finally {
                            jsonWriter.endArray();
                        }
                    }
                } else if (parentId == FolderObject.SYSTEM_INFOSTORE_FOLDER_ID) {
                    if (!session.getUserPermissionBits().hasInfostore()) {
                        throw OXFolderExceptionCode.NO_MODULE_ACCESS.create(session.getUserId(), folderModule2String(FolderObject.INFOSTORE), Integer.valueOf(ctx.getContextId()));
                    }
                    /*
                     * Get subfolders' iterator
                     */
                    if (FolderCacheManager.isEnabled()) {
                        lastModified = FolderCacheManager.getInstance().getFolderObject(parentId, true, ctx, null).getLastModified().getTime();
                    } else {
                        lastModified = FolderObject.loadFolderObjectFromDB(parentId, ctx).getLastModified().getTime();
                    }
                    final List<FolderObject> l;
                    final int size;
                    {
                        final Queue<FolderObject> q = ((FolderObjectIterator) foldersqlinterface.getSubfolders(parentId, null)).asQueue();
                        size = q.size();
                        /*
                         * Write UserStore first
                         */
                        final Iterator<FolderObject> iter = q.iterator();
                        l = new ArrayList<FolderObject>(size);
                        for (int j = 0; j < size; j++) {
                            final FolderObject fo = iter.next();
                            if (fo.getObjectID() == FolderObject.SYSTEM_USER_INFOSTORE_FOLDER_ID) {
                                l.add(0, fo);
                            } else {
                                l.add(fo);
                            }
                        }
                    }
                    fieldList.warmUp(l, session);
                    final Iterator<FolderObject> iter = l.iterator();
                    for (int i = 0; i < size; i++) {
                        final FolderObject fo = iter.next();
                        final Date modified = fo.getLastModified();
                        lastModified = null == modified ? lastModified : Math.max(lastModified, modified.getTime());
                        jsonWriter.array();
                        try {
                            for (final FolderFieldWriter writer : writers) {
                                writer.writeField(jsonWriter, fo, false, FolderObject.getFolderString(fo.getObjectID(), locale), -1);
                            }
                        } finally {
                            jsonWriter.endArray();
                        }
                    }
                    /*
                     * Append virtual root folder for non-tree visible infostore folders
                     */
                    SearchIterator<FolderObject> it = null;
                    try {
                        it = foldersqlinterface.getNonTreeVisiblePublicInfostoreFolders();
                        if (it.hasNext()) {
                            final FolderObject virtualListFolder = FolderObject.createVirtualFolderObject(FolderObject.VIRTUAL_LIST_INFOSTORE_FOLDER_ID, FolderObject.getFolderString(FolderObject.VIRTUAL_LIST_INFOSTORE_FOLDER_ID, locale), FolderObject.INFOSTORE, true, FolderObject.SYSTEM_TYPE);
                            folderWriter.writeOXFolderFieldsAsArray(columns, virtualListFolder, locale);
                        }
                    } finally {
                        if (it != null) {
                            it.close();
                            it = null;
                        }
                    }
                } else if (parentId == FolderObject.SYSTEM_SHARED_FOLDER_ID) {
                    final Map<String, Integer> displayNames;
                    {
                        final UserStorage us = UserStorage.getInstance();
                        final Queue<FolderObject> q =
                            ((FolderObjectIterator) foldersqlinterface.getSubfolders(FolderObject.SYSTEM_SHARED_FOLDER_ID, null)).asQueue();
                        /*
                         * Gather all display names
                         */
                        final int size = q.size();
                        displayNames = new HashMap<String, Integer>(size);
                        final Iterator<FolderObject> iter = q.iterator();
                        for (int i = 0; i < size; i++) {
                            final FolderObject sharedFolder = iter.next();
                            String creatorDisplayName;
                            try {
                                creatorDisplayName = us.getUser(sharedFolder.getCreatedBy(), ctx).getDisplayName();
                            } catch (final OXException e) {
                                if (sharedFolder.getCreatedBy() != OCLPermission.ALL_GROUPS_AND_USERS) {
                                    throw e;
                                }
                                creatorDisplayName = strHelper.getString(Groups.ALL_USERS);
                            }
                            if (displayNames.containsKey(creatorDisplayName)) {
                                continue;
                            }
                            displayNames.put(creatorDisplayName, Integer.valueOf(sharedFolder.getCreatedBy()));
                        }
                    }
                    /*
                     * Sort display names and write corresponding virtual owner folder
                     */
                    final List<String> sortedDisplayNames = new ArrayList<String>(displayNames.keySet());
                    Collections.sort(sortedDisplayNames, new DisplayNameComparator(locale));
                    for (final String displayName : sortedDisplayNames) {
                        final FolderObject virtualOwnerFolder =
                            FolderObject.createVirtualSharedFolderObject(displayNames.get(displayName).intValue(), displayName);
                        jsonWriter.array();
                        try {
                            for (final FolderFieldWriter writer : writers) {
                                writer.writeField(jsonWriter, virtualOwnerFolder, false, null, 1);
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
                         * Append mail root folders to system 'private' folder
                         */
                        if (session.getUserPermissionBits().hasWebMail() && !ignoreMailfolder) {
                            /*
                             * Get all user mail accounts
                             */
                            final List<MailAccount> accounts;
                            if (session.getUserPermissionBits().isMultipleMailAccounts()) {
                                final MailAccountStorageService storageService =
                                    ServerServiceRegistry.getInstance().getService(MailAccountStorageService.class, true);
                                final MailAccount[] accountsArr =
                                    storageService.getUserMailAccounts(session.getUserId(), session.getContextId());
                                final List<MailAccount> tmp = new ArrayList<MailAccount>(accountsArr.length);
                                tmp.addAll(Arrays.asList(accountsArr));
                                // Sort them
                                Collections.sort(tmp, new MailAccountComparator(locale));
                                accounts = tmp;
                            } else {
                                accounts = new ArrayList<MailAccount>(1);
                                final MailAccountStorageService storageService =
                                    ServerServiceRegistry.getInstance().getService(MailAccountStorageService.class, true);
                                accounts.add(storageService.getDefaultMailAccount(session.getUserId(), session.getContextId()));
                            }
                            /*
                             * Messaging accounts; except mail
                             */
                            final List<MessagingAccount> messagingAccounts = new ArrayList<MessagingAccount>();
                            {
                                final MessagingServiceRegistry msr =
                                    ServerServiceRegistry.getInstance().getService(MessagingServiceRegistry.class);
                                if (null != msr) {
                                    final List<MessagingService> allServices = msr.getAllServices(session.getUserId(), session.getContextId());
                                    for (final MessagingService messagingService : allServices) {
                                        if (!messagingService.getId().equals(MailMessagingService.ID)) {
                                            /*
                                             * Only non-mail services
                                             */
                                            messagingAccounts.addAll(messagingService.getAccountManager().getAccounts(session));
                                        }
                                    }
                                }
                            }
                            if (!accounts.isEmpty() || !messagingAccounts.isEmpty()) {
                                if (!accounts.isEmpty() && UnifiedInboxManagement.PROTOCOL_UNIFIED_INBOX.equals(accounts.get(0).getMailProtocol())) {
                                    /*
                                     * Ensure Unified Mail is enabled; meaning at least one account is subscribed to Unified Mail
                                     */
                                    final UnifiedInboxManagement uim =
                                        ServerServiceRegistry.getInstance().getService(UnifiedInboxManagement.class);
                                    if (null == uim || !uim.isEnabled(session.getUserId(), session.getContextId())) {
                                        accounts.remove(0);
                                    }
                                }
                                /*
                                 * Request root folder for each account
                                 */
                                final int size = accounts.size() + messagingAccounts.size();
                                final JSONArray[] arrays = new JSONArray[size];
                                final CompletionFuture<Object> completionFuture;
                                {
                                    final org.slf4j.Logger logger = LOG;
                                    final List<Task<Object>> tasks = new ArrayList<Task<Object>>(size);
                                    int sz = accounts.size();
                                    int index = 0;
                                    for (int i = 0; i < sz; i++) {
                                        final MailAccount mailAccount = accounts.get(i);
                                        tasks.add(new MailRootFolderWriter(arrays, session, logger, mailAccount, columns, index++));
                                    }
                                    sz = messagingAccounts.size();
                                    for (int i = 0; i < sz; i++) {
                                        final MessagingAccount ma = messagingAccounts.get(i);
                                        tasks.add(new MessagingRootFolderWriter(arrays, session, logger, ma, columns, index++));
                                    }
                                    completionFuture = ThreadPools.getThreadPool().invoke(tasks);
                                }
                                /*
                                 * Wait for completion
                                 */
                                try {
                                    for (int i = 0; i < size; i++) {
                                        final Future<Object> f = completionFuture.take();
                                        if (null != f) {
                                            try {
                                                f.get();
                                            } catch (final ExecutionException e) {
                                                final Throwable t = e.getCause();
                                                if (t instanceof OXException) {
                                                    if (null == warning) {
                                                        /*
                                                         * TODO: Does UI already accept warnings?
                                                         */
                                                        warning = (OXException) t;
                                                        warning.setCategory(Category.CATEGORY_WARNING);
                                                    }
                                                } else if (t instanceof MessagingException) {
                                                    if (null == warning) {
                                                        /*
                                                         * TODO: Does UI already accept warnings?
                                                         */
                                                        warning = MimeMailException.handleMessagingException((MessagingException) t);
                                                        warning.setCategory(Category.CATEGORY_WARNING);
                                                    }
                                                } else {
                                                    throw e;
                                                }
                                            }
                                        }
                                    }
                                } catch (final InterruptedException e) {
                                    Thread.currentThread().interrupt();
                                    throw MailExceptionCode.INTERRUPT_ERROR.create(e);
                                } catch (final ExecutionException e) {
                                    throw ThreadPools.launderThrowable(e, OXException.class);
                                }
                                /*
                                 * Iterate sorted arrays
                                 */
                                for (int k = 0; k < size; k++) {
                                    final JSONArray array = arrays[k];
                                    if (null != array) {
                                        jsonWriter.value(array);
                                    }
                                }
                            }
                        }
                    } else if (isSystemPublicFolder) {
                        /*
                         * Append internal users folder
                         */
                        try {
                            final FolderObject internalUsers = foldersqlinterface.getFolderById(FolderObject.SYSTEM_LDAP_FOLDER_ID);
                            lastModified = Math.max(lastModified, internalUsers.getLastModified().getTime());
                            folderWriter.writeOXFolderFieldsAsArray(columns, internalUsers, FolderObject.getFolderString(
                                internalUsers.getObjectID(),
                                locale), -1);
                        } catch (final OXException e) {
                            // Internal users folder not visible to current user
                            if (e.isGeneric(Generic.NO_PERMISSION)) {
                                LOG.debug("", e);
                            } else {
                                throw e;
                            }
                        }
                    }
                    final Queue<FolderObject> q = ((FolderObjectIterator) foldersqlinterface.getSubfolders(parentId, null)).asQueue();
                    fieldList.warmUp(q, session);
                    final int size = q.size();
                    final Iterator<FolderObject> iter = q.iterator();
                    for (int i = 0; i < size; i++) {
                        final FolderObject fo = iter.next();
                        lastModified = fo.getLastModified() == null ? lastModified : Math.max(lastModified, fo.getLastModified().getTime());
                        jsonWriter.array();
                        try {
                            for (final FolderFieldWriter writer : writers) {
                                writer.writeField(jsonWriter, fo, false);
                            }
                        } finally {
                            jsonWriter.endArray();
                        }
                    }
                    // MyInfostore link was removed.
                    if (isSystemPublicFolder) {
                        /*
                         * Append virtual root folder for non-tree visible infostore folders
                         */
                        SearchIterator<FolderObject> it = null;
                        try {
                            if ((it = foldersqlinterface.getNonTreeVisiblePublicCalendarFolders()).hasNext()) {
                                final FolderObject virtualListFolder =
                                    FolderObject.createVirtualFolderObject(
                                        FolderObject.VIRTUAL_LIST_CALENDAR_FOLDER_ID,
                                        FolderObject.getFolderString(FolderObject.VIRTUAL_LIST_CALENDAR_FOLDER_ID, locale),
                                        FolderObject.SYSTEM_MODULE,
                                        true,
                                        FolderObject.SYSTEM_TYPE);
                                if (FolderCacheManager.isInitialized()) {
                                    FolderCacheManager.getInstance().putFolderObject(virtualListFolder, ctx);
                                }
                                folderWriter.writeOXFolderFieldsAsArray(columns, virtualListFolder, locale);
                            }
                        } catch (final OXException e) {
                            if (e.getCode() == OXFolderExceptionCode.NO_MODULE_ACCESS.getNumber() && CATEGORY_PERMISSION_DENIED.equals(e.getCategory())) {
                                LOG.trace("", e);
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
                                final FolderObject virtualListFolder =
                                    FolderObject.createVirtualFolderObject(
                                        FolderObject.VIRTUAL_LIST_CONTACT_FOLDER_ID,
                                        FolderObject.getFolderString(FolderObject.VIRTUAL_LIST_CONTACT_FOLDER_ID, locale),
                                        FolderObject.SYSTEM_MODULE,
                                        true,
                                        FolderObject.SYSTEM_TYPE);
                                if (FolderCacheManager.isInitialized()) {
                                    FolderCacheManager.getInstance().putFolderObject(virtualListFolder, ctx);
                                }
                                folderWriter.writeOXFolderFieldsAsArray(columns, virtualListFolder, locale);
                            }
                        } catch (final OXException e) {
                            if (OXFolderExceptionCode.NO_MODULE_ACCESS.getNumber() == e.getCode() && CATEGORY_PERMISSION_DENIED.equals(e.getCategory())) {
                                LOG.trace("", e);
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
                                final FolderObject virtualListFolder =
                                    FolderObject.createVirtualFolderObject(
                                        FolderObject.VIRTUAL_LIST_TASK_FOLDER_ID,
                                        FolderObject.getFolderString(FolderObject.VIRTUAL_LIST_TASK_FOLDER_ID, locale),
                                        FolderObject.SYSTEM_MODULE,
                                        true,
                                        FolderObject.SYSTEM_TYPE);
                                if (FolderCacheManager.isInitialized()) {
                                    FolderCacheManager.getInstance().putFolderObject(virtualListFolder, ctx);
                                }
                                folderWriter.writeOXFolderFieldsAsArray(columns, virtualListFolder, locale);
                            }
                        } catch (final OXException e) {
                            if (e.getCode() == OXFolderExceptionCode.NO_MODULE_ACCESS.getNumber() && CATEGORY_PERMISSION_DENIED.equals(e.getCategory())) {
                                LOG.trace("", e);
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
                final FolderSQLInterface foldersqlinterface = new RdbFolderSQLInterface(session);
                int sharedOwner;
                try {
                    sharedOwner = Integer.parseInt(parentIdentifier.substring(2));
                } catch (final NumberFormatException exc) {
                    throw getWrappingOXException(exc);
                }
                final FolderFieldWriter[] writers = folderWriter.getFolderFieldWriter(columns);
                final Queue<FolderObject> q = ((FolderObjectIterator) foldersqlinterface.getSharedFoldersFrom(sharedOwner, null)).asQueue();
                fieldList.warmUp(q, session);
                final int size = q.size();
                final Iterator<FolderObject> iter = q.iterator();
                for (int i = 0; i < size; i++) {
                    final FolderObject sharedFolder = iter.next();
                    lastModified =
                        sharedFolder.getLastModified() == null ? lastModified : Math.max(
                            lastModified,
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
                 * Determine if all folders, regardless of their subscription status, shall be included
                 */
                final boolean all = (STRING_1.equals(paramContainer.getStringParam(PARAMETER_ALL)));
                final MessagingFolderIdentifier mfi = MessagingFolderIdentifier.parseFQN(parentIdentifier);
                if (null == mfi) {
                    SearchIterator<MailFolder> it = null;
                    MailServletInterface mailInterface = null;
                    try {
                        mailInterface = MailServletInterface.getInstance(session);
                        /*
                         * E-Mail folder
                         */
                        it = mailInterface.getChildFolders(parentIdentifier, all);
                        /*
                         * Check for possible warning
                         */
                        {
                            final Collection<OXException> warnings = mailInterface.getWarnings();
                            if (!warnings.isEmpty()) {
                                warning = warnings.iterator().next();
                            }
                        }
                        final MailFolderFieldWriter[] writers =
                            com.openexchange.mail.json.writer.FolderWriter.getMailFolderFieldWriter(
                                columns,
                                mailInterface.getMailConfig(),
                                session);
                        final int size = it.size();
                        boolean inboxFound = false;
                        final com.openexchange.mail.json.writer.FolderWriter.JSONArrayPutter putter = newArrayPutter();
                        for (int i = 0; i < size; i++) {
                            final MailFolder f = it.next();
                            if (!inboxFound && STR_INBOX.equals(f.getFullname())) {
                                inboxFound = true;
                                final JSONArray ja = new JSONArray();
                                putter.setJSONArray(ja);
                                // TODO: Translation for INBOX?!
                                for (final MailFolderFieldWriter writer : writers) {
                                    writer.writeField(putter, mailInterface.getAccountID(), f, strHelper.getString(MailStrings.INBOX), -1, null, -1, all);
                                }
                                jsonWriter.value(ja);
                            } else {
                                final JSONArray ja = new JSONArray();
                                putter.setJSONArray(ja);
                                for (final MailFolderFieldWriter writer : writers) {
                                    writer.writeField(putter, mailInterface.getAccountID(), f, null, -1, null, -1, all);
                                }
                                jsonWriter.value(ja);
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
                                LOG.error("", e);
                            }
                        }
                    }
                } else {
                    /*
                     * A messaging folder identifier
                     */
                    final String serviceId = mfi.getServiceId();
                    final MessagingService messagingService = messagingServiceRegistry().getMessagingService(serviceId, session.getUserId(), session.getContextId());
                    final int accountId = mfi.getAccountId();
                    final MessagingAccountAccess accountAccess = messagingService.getAccountAccess(accountId, session);
                    accountAccess.connect();
                    try {
                        final MessagingFolder[] subfolders = accountAccess.getFolderAccess().getSubfolders(mfi.getFullname(), all);
                        final MessagingFolderFieldWriter[] writers =
                            com.openexchange.ajax.writer.MessagingFolderWriter.getMessagingFolderFieldWriter(columns, session);
                        final com.openexchange.ajax.writer.MessagingFolderWriter.JSONArrayPutter putter = newMessagingArrayPutter();
                        for (final MessagingFolder subfolder : subfolders) {
                            final JSONArray ja = new JSONArray();
                            putter.setJSONArray(ja);
                            for (final MessagingFolderFieldWriter writer : writers) {
                                writer.writeField(putter, serviceId, accountId, subfolder, null, -1, null, -1, all);
                            }
                            jsonWriter.value(ja);
                        }
                    } finally {
                        accountAccess.close();
                    }
                }
            }
        } catch (final OXException e) {
            LOG.error("", e);
            response.setException(e);
        } catch (final Exception e) {
            final OXException wrapper = getWrappingOXException(e);
            LOG.error("", wrapper);
            response.setException(wrapper);
        }
        /*
         * Close response and flush print writer
         */
        jsonWriter.endArray();
        if (null != warning) {
            response.addWarning(warning);
        }
        response.setData(jsonWriter.getObject());
        response.setTimestamp(lastModifiedDate);
        return response;
    }

    /**
     * Performs the GET request to send back the path from a certain folder to root folder
     *
     * @throws JSONException
     */
    public void actionGetPath(final ServerSession session, final JSONWriter w, final JSONObject requestObj) throws JSONException {
        ResponseWriter.write(actionGetPath(session, ParamContainer.getInstance(requestObj)), w, localeFrom(session));
    }

    private final void actionGetPath(final HttpServletRequest req, final HttpServletResponse resp) throws IOException, ServletException {
        final ServerSession session = getSessionObject(req);
        try {
            ResponseWriter.write(
                actionGetPath(session, ParamContainer.getInstance(req, resp)),
                resp.getWriter(), localeFrom(session));
        } catch (final JSONException e) {
            try {
                ResponseWriter.writeException(OXJSONExceptionCodes.JSON_WRITE_ERROR.create(e, new Object[0]), new JSONWriter(
                    resp.getWriter()), localeFrom(session));
            } catch (final JSONException jsonError) {
                throw new ServletException(e.getMessage(), jsonError);
            }
        }
    }

    private final Response actionGetPath(final ServerSession session, final ParamContainer paramContainer) throws JSONException {
        /*
         * Some variables
         */
        final Response response = new Response(session);
        final OXJSONWriter jsonWriter = new OXJSONWriter();
        long lastModified = 0;
        /*
         * Start response
         */
        jsonWriter.array();
        try {
            /*
             * Read in parameters
             */
            final Context ctx = session.getContext();
            final String folderIdentifier = paramContainer.checkStringParam(PARAMETER_ID);
            final int[] columns = paramContainer.checkIntArrayParam(PARAMETER_COLUMNS);
            final String timeZoneId = paramContainer.getStringParam(PARAMETER_TIMEZONE);

            final FolderWriter folderWriter = new FolderWriter(jsonWriter, session, ctx, timeZoneId, FIELDS);
            int folderId = -1;
            if ((folderId = getUnsignedInteger(folderIdentifier)) >= 0) {
                final FolderSQLInterface foldersqlinterface = new RdbFolderSQLInterface(session);
                /*
                 * Pre-Select field writers
                 */
                final FolderFieldWriter[] writers = folderWriter.getFolderFieldWriter(columns);
                final Queue<FolderObject> q = ((FolderObjectIterator) foldersqlinterface.getPathToRoot(folderId)).asQueue();
                final int size = q.size();
                final Iterator<FolderObject> iter = q.iterator();
                for (int i = 0; i < size; i++) {
                    final FolderObject fo = iter.next();
                    if (fo.containsLastModified()) {
                        lastModified = fo.getLastModified().getTime() > lastModified ? fo.getLastModified().getTime() : lastModified;
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
            } else if (folderIdentifier.startsWith(FolderObject.SHARED_PREFIX)) {
                final int userId = Integer.parseInt(folderIdentifier.substring(2));
                final List<FolderObject> list = new ArrayList<FolderObject>(2);
                final User user = UserStorage.getInstance().getUser(userId, ctx);
                list.add(FolderObject.createVirtualSharedFolderObject(userId, user.getDisplayName()));
                final FolderObject systemShared = new OXFolderAccess(ctx).getFolderObject(FolderObject.SYSTEM_SHARED_FOLDER_ID);
                systemShared.setFolderName(FolderObject.getFolderString(FolderObject.SYSTEM_SHARED_FOLDER_ID, user.getLocale()));
                list.add(systemShared);
                /*
                 * Pre-Select field writers
                 */
                final FolderFieldWriter[] writers = folderWriter.getFolderFieldWriter(columns);
                for (int i = 0; i < 2; i++) {
                    final FolderObject fo = list.get(i);
                    if (fo.containsLastModified()) {
                        lastModified = fo.getLastModified().getTime() > lastModified ? fo.getLastModified().getTime() : lastModified;
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
                final MessagingFolderIdentifier mfi = MessagingFolderIdentifier.parseFQN(folderIdentifier);
                final Locale locale = session.getUser().getLocale();
                if (null == mfi) {
                    MailServletInterface mailInterface = null;
                    SearchIterator<MailFolder> it = null;
                    try {
                        mailInterface = MailServletInterface.getInstance(session);
                        /*
                         * Pre-Select field writers
                         */
                        it = mailInterface.getPathToDefaultFolder(folderIdentifier);
                        final MailFolderFieldWriter[] writers =
                            com.openexchange.mail.json.writer.FolderWriter.getMailFolderFieldWriter(
                                columns,
                                mailInterface.getMailConfig(),
                                session);
                        final int size = it.size();
                        final int accountID = mailInterface.getAccountID();
                        final com.openexchange.mail.json.writer.FolderWriter.JSONArrayPutter putter = newArrayPutter();
                        for (int i = 0; i < size; i++) {
                            final MailFolder fld = it.next();
                            final JSONArray ja = new JSONArray();
                            putter.setJSONArray(ja);
                            for (final MailFolderFieldWriter w : writers) {
                                w.writeField(putter, accountID, fld);
                            }
                            jsonWriter.value(ja);
                        }
                        it.close();
                        it = null;
                        {
                            final String preparedFullname = MailFolderUtility.prepareFullname(accountID, MailFolder.DEFAULT_FOLDER_ID);
                            /*
                             * Write virtual folder "E-Mail"
                             */
                            final MailFolder defaultFolder = mailInterface.getFolder(preparedFullname, true);
                            if (defaultFolder != null) {
                                final MailAccountStorageService storageService =
                                    ServerServiceRegistry.getInstance().getService(MailAccountStorageService.class, true);
                                final MailAccount mailAccount =
                                    storageService.getMailAccount(accountID, session.getUserId(), session.getContextId());
                                final JSONArray ja = new JSONArray();
                                putter.setJSONArray(ja);
                                for (final MailFolderFieldWriter w : writers) {
                                    w.writeField(
                                        putter,
                                        accountID,
                                        defaultFolder,
                                        mailAccount.getName(),
                                        1,
                                        preparedFullname,
                                        FolderObject.SYSTEM_MODULE,
                                        false);
                                }
                                jsonWriter.value(ja);
                            }
                        }
                        /*
                         * Finally, write "private" folder
                         */
                        FolderObject privateFolder;
                        if (FolderCacheManager.isEnabled()) {
                            privateFolder =
                                FolderCacheManager.getInstance().getFolderObject(FolderObject.SYSTEM_PRIVATE_FOLDER_ID, true, ctx, null);
                        } else {
                            privateFolder = FolderObject.loadFolderObjectFromDB(FolderObject.SYSTEM_PRIVATE_FOLDER_ID, ctx);
                        }
                        folderWriter.writeOXFolderFieldsAsArray(columns, privateFolder, FolderObject.getFolderString(
                            FolderObject.SYSTEM_PRIVATE_FOLDER_ID,
                            locale), -1);
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
                                LOG.error("", e);
                            }
                        }
                    }
                } else {
                    /*
                     * A messaging folder identifier
                     */
                    final String serviceId = mfi.getServiceId();
                    final MessagingService messagingService = messagingServiceRegistry().getMessagingService(serviceId, session.getUserId(), session.getContextId());
                    final int accountId = mfi.getAccountId();
                    final MessagingAccountAccess accountAccess = messagingService.getAccountAccess(accountId, session);
                    accountAccess.connect();
                    try {
                        final MessagingFolderAccess folderAccess = accountAccess.getFolderAccess();
                        final List<MessagingFolder> path = new ArrayList<MessagingFolder>();
                        MessagingFolder folder = folderAccess.getFolder(mfi.getFullname());
                        path.add(folder);
                        String parentId;
                        while (!MessagingFolder.ROOT_FULLNAME.equals((parentId = folder.getParentId())) && parentId != null) {
                            folder = folderAccess.getFolder(parentId);
                            path.add(folder);
                        }
                        final MessagingFolderFieldWriter[] writers =
                            com.openexchange.ajax.writer.MessagingFolderWriter.getMessagingFolderFieldWriter(columns, session);
                        final com.openexchange.ajax.writer.MessagingFolderWriter.JSONArrayPutter putter = newMessagingArrayPutter();
                        for (final MessagingFolder messagingFolder : path) {
                            final JSONArray ja = new JSONArray();
                            putter.setJSONArray(ja);
                            for (final MessagingFolderFieldWriter w : writers) {
                                w.writeField(putter, serviceId, accountId, messagingFolder);
                            }
                            jsonWriter.value(ja);
                        }
                        {
                            /*
                             * Write virtual folder "E-Mail"
                             */
                            final MessagingFolder rootFolder = folderAccess.getFolder(MessagingFolder.ROOT_FULLNAME);
                            final JSONArray ja = new JSONArray();
                            putter.setJSONArray(ja);
                            final String fqn = MessagingFolderIdentifier.getFQN(serviceId, accountId, MessagingFolder.ROOT_FULLNAME);
                            final String name = messagingService.getAccountManager().getAccount(accountId, session).getDisplayName();
                            for (final MessagingFolderFieldWriter w : writers) {
                                w.writeField(putter, serviceId, accountId, rootFolder, name, 1, fqn, FolderObject.MESSAGING, false);
                            }
                            jsonWriter.value(ja);
                        }
                        /*
                         * Finally, write "private" folder
                         */
                        FolderObject privateFolder;
                        if (FolderCacheManager.isEnabled()) {
                            privateFolder =
                                FolderCacheManager.getInstance().getFolderObject(FolderObject.SYSTEM_PRIVATE_FOLDER_ID, true, ctx, null);
                        } else {
                            privateFolder = FolderObject.loadFolderObjectFromDB(FolderObject.SYSTEM_PRIVATE_FOLDER_ID, ctx);
                        }
                        folderWriter.writeOXFolderFieldsAsArray(columns, privateFolder, FolderObject.getFolderString(
                            FolderObject.SYSTEM_PRIVATE_FOLDER_ID,
                            locale), -1);
                    } finally {
                        accountAccess.close();
                    }
                }
            }
        } catch (final OXException e) {
            LOG.error("", e);
            response.setException(e);
        } catch (final Exception e) {
            final OXException wrapper = getWrappingOXException(e);
            LOG.error("", wrapper);
            response.setException(wrapper);
        }
        /*
         * Close response and flush print writer
         */
        jsonWriter.endArray();
        response.setData(jsonWriter.getObject());
        response.setTimestamp(lastModified == 0 ? null : new Date(lastModified));
        return response;
    }

    /**
     * Performs the GET request to send back all modified folders since a certain timestamp
     *
     * @param session
     * @param w
     * @param requestObj
     * @throws JSONException
     */
    public void actionGetUpdatedFolders(final ServerSession session, final JSONWriter w, final JSONObject requestObj) throws JSONException {
        ResponseWriter.write(actionGetUpdatedFolders(session, ParamContainer.getInstance(requestObj)), w, localeFrom(session));
    }

    private final void actionGetUpdatedFolders(final HttpServletRequest req, final HttpServletResponse resp) throws IOException, ServletException {
        ServerSession session = getSessionObject(req);
        try {
            ResponseWriter.write(
                actionGetUpdatedFolders(session, ParamContainer.getInstance(req, resp)),
                resp.getWriter(), localeFrom(session));
        } catch (final JSONException e) {
            try {
                ResponseWriter.writeException(OXJSONExceptionCodes.JSON_WRITE_ERROR.create(e, new Object[0]), new JSONWriter(
                    resp.getWriter()), localeFrom(session));
            } catch (final JSONException jsonError) {
                throw new ServletException(e.getMessage(), jsonError);
            }
        }
    }

    private static final Date DATE_0 = new Date(0);

    private final Response actionGetUpdatedFolders(final ServerSession session, final ParamContainer paramContainer) throws JSONException {
        /*
         * Some variables
         */
        final Response response = new Response(session);
        final OXJSONWriter jsonWriter = new OXJSONWriter();
        Date lastModifiedDate = null;
        OXException warning = null;
        /*
         * Start response
         */
        jsonWriter.array();
        try {
            long lastModified = 0;
            /*
             * Read in parameters
             */
            final Context ctx = session.getContext();
            final int[] columns = paramContainer.checkIntArrayParam(PARAMETER_COLUMNS);
            final String timeZoneId = paramContainer.getStringParam(PARAMETER_TIMEZONE);
            final BulkAdditionalFolderFieldsList fieldList = new BulkAdditionalFolderFieldsList(FIELDS);
            final FolderWriter folderWriter = new FolderWriter(jsonWriter, session, ctx, timeZoneId, fieldList);
            final Date timestamp = paramContainer.checkDateParam(PARAMETER_TIMESTAMP);
            final boolean includeMailFolders = STRING_1.equals(paramContainer.getStringParam(PARAMETER_MAIL));
            final boolean ignoreDeleted = STRING_DELETED.equalsIgnoreCase(paramContainer.getStringParam(PARAMETER_IGNORE));
            lastModified = Math.max(timestamp.getTime(), lastModified);

            final FolderSQLInterface foldersqlinterface = new RdbFolderSQLInterface(session);
            final FolderFieldWriter[] writers = folderWriter.getFolderFieldWriter(columns);
            /*
             * Get all updated OX folders
             */
            Queue<FolderObject> q = ((FolderObjectIterator) foldersqlinterface.getAllModifiedFolders(timestamp)).asQueue();
            fieldList.warmUp(q, session);
            final OXFolderAccess access = new OXFolderAccess(ctx);
            final Queue<FolderObject> updatedQueue = new LinkedList<FolderObject>();
            final Queue<FolderObject> deletedQueue = ignoreDeleted ? null : new LinkedList<FolderObject>();
            final Map<String, Integer> displayNames = new HashMap<String, Integer>();
            boolean addSystemSharedFolder = false;
            boolean checkVirtualListFolders = false;
            int size = q.size();
            Iterator<FolderObject> iter = q.iterator();
            final User user = session.getUser();
            {
                final UserPermissionBits userPerm = session.getUserPermissionBits();
                final UserStorage us = UserStorage.getInstance();
                final StringHelper strHelper = StringHelper.valueOf(user.getLocale());
                final boolean sharedFolderAccess = userPerm.hasFullSharedFolderAccess();
                for (int i = 0; i < size; i++) {
                    final FolderObject fo = iter.next();
                    if (fo.isVisible(session.getUserId(), userPerm)) {
                        if (fo.isShared(session.getUserId())) {
                            if (sharedFolderAccess) {
                                /*
                                 * Add display name of shared folder owner
                                 */
                                String creatorDisplayName;
                                try {
                                    creatorDisplayName = us.getUser(fo.getCreatedBy(), ctx).getDisplayName();
                                } catch (final OXException e) {
                                    if (fo.getCreatedBy() != OCLPermission.ALL_GROUPS_AND_USERS) {
                                        throw e;
                                    }
                                    creatorDisplayName = strHelper.getString(Groups.ALL_USERS);
                                }
                                if (!displayNames.containsKey(creatorDisplayName)) {
                                    displayNames.put(creatorDisplayName, Integer.valueOf(fo.getCreatedBy()));
                                }
                                /*
                                 * Remember to include system shared folder
                                 */
                                addSystemSharedFolder = true;
                            } else {
                                if (!ignoreDeleted) {
                                    deletedQueue.add(fo);
                                }
                            }
                        } else if (FolderObject.PUBLIC == fo.getType()) {
                            if (access.getFolderPermission(fo.getParentFolderID(), session.getUserId(), userPerm).isFolderVisible()) {
                                /*
                                 * Parent is already visible: Add real parent
                                 */
                                updatedQueue.add(access.getFolderObject(fo.getParentFolderID()));
                            } else {
                                /*
                                 * Parent is not visible: Update superior system folder to let the newly visible folder appear underneath
                                 * virtual "Other XYZ folders"
                                 */
                                updatedQueue.add(fo.getModule() == FolderObject.INFOSTORE ? access.getFolderObject(FolderObject.SYSTEM_INFOSTORE_FOLDER_ID) : access.getFolderObject(FolderObject.SYSTEM_PUBLIC_FOLDER_ID));
                            }
                        }
                        updatedQueue.add(fo);
                    } else {
                        checkVirtualListFolders |= (FolderObject.PUBLIC == fo.getType());
                        if (!ignoreDeleted) {
                            deletedQueue.add(fo);
                        }
                    }
                }
                /*
                 * Check virtual list folders
                 */
                if (checkVirtualListFolders && !ignoreDeleted) {
                    if (userPerm.hasTask() && !foldersqlinterface.getNonTreeVisiblePublicTaskFolders().hasNext()) {
                        final FolderObject virtualTasks = new FolderObject(FolderObject.VIRTUAL_LIST_TASK_FOLDER_ID);
                        virtualTasks.setLastModified(DATE_0);
                        deletedQueue.add(virtualTasks);
                    }
                    if (userPerm.hasCalendar() && !foldersqlinterface.getNonTreeVisiblePublicCalendarFolders().hasNext()) {
                        final FolderObject virtualCalendar = new FolderObject(FolderObject.VIRTUAL_LIST_CALENDAR_FOLDER_ID);
                        virtualCalendar.setLastModified(DATE_0);
                        deletedQueue.add(virtualCalendar);
                    }
                    if (userPerm.hasContact() && !foldersqlinterface.getNonTreeVisiblePublicContactFolders().hasNext()) {
                        final FolderObject virtualContact = new FolderObject(FolderObject.VIRTUAL_LIST_CONTACT_FOLDER_ID);
                        virtualContact.setLastModified(DATE_0);
                        deletedQueue.add(virtualContact);
                    }
                    if (userPerm.hasInfostore() && !foldersqlinterface.getNonTreeVisiblePublicInfostoreFolders().hasNext()) {
                        final FolderObject virtualInfostore = new FolderObject(FolderObject.VIRTUAL_LIST_INFOSTORE_FOLDER_ID);
                        virtualInfostore.setLastModified(DATE_0);
                        deletedQueue.add(virtualInfostore);
                    }
                }
            }
            /*
             * Check if shared folder must be updated, too
             */
            if (addSystemSharedFolder) {
                final FolderObject sharedFolder = access.getFolderObject(FolderObject.SYSTEM_SHARED_FOLDER_ID);
                sharedFolder.setFolderName(FolderObject.getFolderString(FolderObject.SYSTEM_SHARED_FOLDER_ID, user.getLocale()));
                updatedQueue.add(sharedFolder);
                if (!displayNames.isEmpty()) {
                    for (final Entry<String, Integer> entry : displayNames.entrySet()) {
                        updatedQueue.add(FolderObject.createVirtualSharedFolderObject(entry.getValue().intValue(), entry.getKey()));
                    }
                }
            }
            /*
             * Output updated folders
             */
            size = updatedQueue.size();
            iter = updatedQueue.iterator();
            for (int i = 0; i < size; i++) {
                final FolderObject fo = iter.next();
                {
                    final Date modified = fo.getLastModified();
                    if (null != modified) {
                        lastModified = Math.max(modified.getTime(), lastModified);
                    }
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
            if (!ignoreDeleted) {
                /*
                 * Get deleted OX folders
                 */
                q = ((FolderObjectIterator) foldersqlinterface.getDeletedFolders(timestamp)).asQueue();
                /*
                 * Add deleted OX folders from above
                 */
                q.addAll(deletedQueue);
                final FolderFieldWriter idWriter = folderWriter.getFolderFieldWriter(new int[] { DataObject.OBJECT_ID })[0];
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
            }
            if (includeMailFolders) {
                /*
                 * Get all user mail accounts
                 */
                final List<MailAccount> accounts;
                if (session.getUserPermissionBits().isMultipleMailAccounts()) {
                    final MailAccountStorageService storageService =
                        ServerServiceRegistry.getInstance().getService(MailAccountStorageService.class, true);
                    final MailAccount[] accountsArr = storageService.getUserMailAccounts(session.getUserId(), session.getContextId());
                    final List<MailAccount> tmp = new ArrayList<MailAccount>(accountsArr.length);
                    tmp.addAll(Arrays.asList(accountsArr));
                    // Sort them
                    Collections.sort(tmp, new MailAccountComparator(user.getLocale()));
                    accounts = tmp;
                } else {
                    accounts = new ArrayList<MailAccount>(1);
                    final MailAccountStorageService storageService =
                        ServerServiceRegistry.getInstance().getService(MailAccountStorageService.class, true);
                    accounts.add(storageService.getDefaultMailAccount(session.getUserId(), session.getContextId()));
                }
                /*
                 * Messaging accounts; except mail
                 */
                final List<MessagingAccount> messagingAccounts = new ArrayList<MessagingAccount>();
                {
                    final MessagingServiceRegistry msr = ServerServiceRegistry.getInstance().getService(MessagingServiceRegistry.class);
                    if (null != msr) {
                        final List<MessagingService> allServices = msr.getAllServices(session.getUserId(), session.getContextId());
                        for (final MessagingService messagingService : allServices) {
                            if (!messagingService.getId().equals(MailMessagingService.ID)) {
                                /*
                                 * Only non-mail services
                                 */
                                messagingAccounts.addAll(messagingService.getAccountManager().getAccounts(session));
                            }
                        }
                    }
                }
                if (!accounts.isEmpty() || !messagingAccounts.isEmpty()) {
                    if (!accounts.isEmpty() && UnifiedInboxManagement.PROTOCOL_UNIFIED_INBOX.equals(accounts.get(0).getMailProtocol())) {
                        /*
                         * Ensure Unified Mail is enabled; meaning at least one account is subscribed to Unified Mail
                         */
                        final UnifiedInboxManagement uim = ServerServiceRegistry.getInstance().getService(UnifiedInboxManagement.class);
                        if (null == uim || !uim.isEnabled(session.getUserId(), session.getContextId())) {
                            accounts.remove(0);
                        }
                    }
                    final int accountSize = accounts.size() + messagingAccounts.size();
                    final JSONArray[] arrays = new JSONArray[accountSize];
                    final CompletionFuture<Object> completionFuture;
                    {
                        final org.slf4j.Logger logger = LOG;
                        final List<Task<Object>> tasks = new ArrayList<Task<Object>>(accountSize);
                        int sz = accounts.size();
                        for (int i = 0; i < sz; i++) {
                            final MailAccount mailAccount = accounts.get(i);
                            /*
                             * Check if current account has been initialized before that is if its default folders were checked.
                             */
                            final boolean initialized;
                            {
                                final MailSessionCache mailSessionCache = MailSessionCache.getInstance(session);
                                final Boolean b =
                                    mailSessionCache.getParameter(
                                        mailAccount.getId(),
                                        MailSessionParameterNames.getParamDefaultFolderChecked());
                                initialized = (b != null) && b.booleanValue();
                            }
                            if (initialized) {
                                /*
                                 * Clean session caches
                                 */
                                SessionMailCache.getInstance(session, mailAccount.getId()).clear();
                                /*
                                 * Add root folders
                                 */
                                tasks.add(new MailRootFolderWriter(arrays, session, logger, mailAccount, columns, i));
                            } else {
                                // Add dummy callable
                                final int index = i;
                                tasks.add(new AbstractTask<Object>() {

                                    @Override
                                    public Object call() throws Exception {
                                        arrays[index] = null;
                                        return null;
                                    }
                                });
                            }
                        }
                        sz = messagingAccounts.size();
                        for (int i = 0; i < sz; i++) {
                            final MessagingAccount ma = messagingAccounts.get(i);
                            tasks.add(new MessagingRootFolderWriter(arrays, session, logger, ma, columns, i));
                        }
                        completionFuture = ThreadPools.getThreadPool().invoke(tasks);
                    }
                    // Wait for completion
                    try {
                        for (int i = 0; i < accountSize; i++) {
                            try {
                                completionFuture.take().get();
                            } catch (final ExecutionException e) {
                                final Throwable t = e.getCause();
                                if (t instanceof OXException) {
                                    if (null == warning) {
                                        /*
                                         * TODO: Does UI already accept warnings?
                                         */
                                        warning = (OXException) t;
                                        warning.setCategory(Category.CATEGORY_WARNING);
                                    }
                                } else if (t instanceof MessagingException) {
                                    if (null == warning) {
                                        /*
                                         * TODO: Does UI already accept warnings?
                                         */
                                        warning = MimeMailException.handleMessagingException((MessagingException) t);
                                        warning.setCategory(Category.CATEGORY_WARNING);
                                    }
                                } else {
                                    throw e;
                                }
                            }
                        }
                    } catch (final InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw MailExceptionCode.INTERRUPT_ERROR.create(e);
                    } catch (final ExecutionException e) {
                        throw ThreadPools.launderThrowable(e, OXException.class);
                    }
                    // Write arrays
                    for (final JSONArray array : arrays) {
                        if (null != array) {
                            jsonWriter.value(array);
                        }
                    }
                }
            }
            /*
             * Set timestamp
             */
            lastModifiedDate = lastModified == 0 ? null : new Date(lastModified);
        } catch (final OXException e) {
            LOG.error("", e);
            response.setException(e);
        } catch (final Exception e) {
            final OXException wrapper = getWrappingOXException(e);
            LOG.error("", wrapper);
            response.setException(wrapper);
        }
        /*
         * Close response and flush print writer
         */
        jsonWriter.endArray();
        if (null != warning) {
            response.addWarning(warning);
        }
        response.setData(jsonWriter.getObject());
        response.setTimestamp(lastModifiedDate);
        return response;
    }

    public void actionGetFolder(final ServerSession session, final JSONWriter w, final JSONObject requestObj) throws JSONException {
        ResponseWriter.write(actionGetFolder(session, ParamContainer.getInstance(requestObj)), w, localeFrom(session));
    }

    private final void actionGetFolder(final HttpServletRequest req, final HttpServletResponse resp) throws IOException, ServletException {
        final ServerSession session = getSessionObject(req);
        try {
            ResponseWriter.write(
                actionGetFolder(session, ParamContainer.getInstance(req, resp)),
                resp.getWriter(), localeFrom(session));
        } catch (final JSONException e) {
            try {
                ResponseWriter.writeException(OXJSONExceptionCodes.JSON_WRITE_ERROR.create(e, new Object[0]), new JSONWriter(
                    resp.getWriter()), localeFrom(session));
            } catch (final JSONException jsonError) {
                throw new ServletException(e.getMessage(), jsonError);
            }
        }
    }

    private final Response actionGetFolder(final ServerSession session, final ParamContainer paramContainer) {
        /*
         * Some variables
         */
        final Locale locale = session.getUser().getLocale();
        final Response response = new Response(locale);
        OXJSONWriter jsonWriter = null;
        Date lastModifiedDate = null;
        /*
         * Start response
         */
        try {
            final Context ctx = session.getContext();
            final String folderIdentifier = paramContainer.checkStringParam(PARAMETER_ID);
            final int[] columns = paramContainer.checkIntArrayParam(PARAMETER_COLUMNS);
            final String timeZoneId = paramContainer.getStringParam(PARAMETER_TIMEZONE);

            int folderId = -1;
            if ((folderId = getUnsignedInteger(folderIdentifier)) >= 0) {
                final FolderSQLInterface foldersqlinterface = new RdbFolderSQLInterface(session);
                final FolderObject fo = foldersqlinterface.getFolderById(folderId);
                lastModifiedDate = fo.getLastModified();
                jsonWriter = new OXJSONWriter();
                new FolderWriter(jsonWriter, session, ctx, timeZoneId, FIELDS).writeOXFolderFieldsAsObject(
                    columns,
                    fo,
                    locale);
            } else if (folderIdentifier.startsWith(FolderObject.SHARED_PREFIX)) {
                int userId = -1;
                try {
                    userId = Integer.parseInt(folderIdentifier.substring(2));
                } catch (final NumberFormatException exc) {
                    throw getWrappingOXException(exc);
                }
                final User user = UserStorage.getInstance().getUser(userId, ctx);
                final FolderObject fo = FolderObject.createVirtualSharedFolderObject(userId, user.getDisplayName());
                jsonWriter = new OXJSONWriter();
                new FolderWriter(jsonWriter, session, ctx, timeZoneId, FIELDS).writeOXFolderFieldsAsObject(columns, fo, user.getLocale());
            } else {
                final MessagingFolderIdentifier mfi = MessagingFolderIdentifier.parseFQN(folderIdentifier);
                if (null == mfi) {
                    MailServletInterface mailInterface = null;
                    try {
                        mailInterface = MailServletInterface.getInstance(session);
                        final MailFolder f = mailInterface.getFolder(folderIdentifier, true);
                        final MailFolderFieldWriter[] writers =
                            com.openexchange.mail.json.writer.FolderWriter.getMailFolderFieldWriter(
                                columns,
                                mailInterface.getMailConfig(),
                                session);
                        final JSONObject jo = new JSONObject();
                        final com.openexchange.mail.json.writer.FolderWriter.JSONObjectPutter putter = newObjectPutter().setJSONObject(jo);
                        for (final MailFolderFieldWriter writer : writers) {
                            writer.writeField(putter, mailInterface.getAccountID(), f);
                        }
                        jsonWriter = new OXJSONWriter(jo);
                    } finally {
                        try {
                            if (mailInterface != null) {
                                mailInterface.close(true);
                            }
                        } catch (final OXException e) {
                            LOG.error("", e);
                        }
                    }
                } else {
                    /*
                     * A messaging folder identifier
                     */
                    final String serviceId = mfi.getServiceId();
                    final MessagingService messagingService = messagingServiceRegistry().getMessagingService(serviceId, session.getUserId(), session.getContextId());
                    final int accountId = mfi.getAccountId();
                    final MessagingAccountAccess accountAccess = messagingService.getAccountAccess(accountId, session);
                    accountAccess.connect();
                    try {
                        final MessagingFolder f = accountAccess.getFolderAccess().getFolder(mfi.getFullname());
                        final MessagingFolderFieldWriter[] writers =
                            com.openexchange.ajax.writer.MessagingFolderWriter.getMessagingFolderFieldWriter(columns, session);
                        final JSONObject jo = new JSONObject();
                        final com.openexchange.ajax.writer.MessagingFolderWriter.JSONObjectPutter putter =
                            newMessagingObjectPutter().setJSONObject(jo);
                        for (final MessagingFolderFieldWriter writer : writers) {
                            writer.writeField(putter, serviceId, accountId, f);
                        }
                        jsonWriter = new OXJSONWriter(jo);
                    } finally {
                        accountAccess.close();
                    }
                }
            }
        } catch (final OXException e) {
            LOG.error("", e);
            response.setException(e);
        } catch (final Exception e) {
            final OXException wrapper = getWrappingOXException(e);
            LOG.error("", wrapper);
            response.setException(wrapper);
        }
        /*
         * Close response and flush print writer
         */
        response.setData(jsonWriter == null ? JSONObject.NULL : jsonWriter.getObject());
        response.setTimestamp(lastModifiedDate);
        return response;
    }

    public void actionPutUpdateFolder(final ServerSession session, final JSONWriter w, final JSONObject requestObj) throws JSONException {
        ResponseWriter.write(actionPutUpdateFolder(session, requestObj.getString(ResponseFields.DATA), ParamContainer.getInstance(
            requestObj)), w, localeFrom(session));
    }

    private final void actionPutUpdateFolder(final HttpServletRequest req, final HttpServletResponse resp) throws IOException, ServletException {
        ServerSession session = getSessionObject(req);
        try {
            ResponseWriter.write(actionPutUpdateFolder(session, getBody(req), ParamContainer.getInstance(
                req,
                resp)), resp.getWriter(), localeFrom(session));
        } catch (final JSONException e) {
            try {
                ResponseWriter.writeException(OXJSONExceptionCodes.JSON_WRITE_ERROR.create(e, new Object[0]), new JSONWriter(
                    resp.getWriter()), localeFrom(session));
            } catch (final JSONException jsonError) {
                throw new ServletException(e.getMessage(), jsonError);
            }
        }
    }

    private final Response actionPutUpdateFolder(final ServerSession session, final String body, final ParamContainer paramContainer) {
        /*
         * Some variables
         */
        final Response response = new Response(session);
        Date lastModifiedDate = null;
        Object retval = JSONObject.NULL;
        /*
         * Start response
         */
        try {
            final Context ctx = session.getContext();
            final String folderIdentifier = paramContainer.checkStringParam(PARAMETER_ID);
            Date timestamp = null;
            final JSONObject jsonObj = new JSONObject(body);
            int updateFolderId = -1;
            if ((updateFolderId = getUnsignedInteger(folderIdentifier)) >= 0) {
                timestamp = paramContainer.checkDateParam(PARAMETER_TIMESTAMP);
                final FolderSQLInterface foldersqlinterface = new RdbFolderSQLInterface(session);
                FolderObject fo = new FolderObject(updateFolderId);
                new FolderParser(session.getUserPermissionBits()).parse(fo, jsonObj);
                fo = foldersqlinterface.saveFolderObject(fo, timestamp);
                retval = Integer.toString(fo.getObjectID());
                lastModifiedDate = fo.getLastModified();
            } else if (folderIdentifier.startsWith(FolderObject.SHARED_PREFIX)) {
                throw OXFolderExceptionCode.NO_ADMIN_ACCESS.create(session.getUserId(),
                    folderIdentifier,
                    Integer.valueOf(ctx.getContextId()));
            } else {
                final MessagingFolderIdentifier mfi = MessagingFolderIdentifier.parseFQN(folderIdentifier);
                if (null == mfi) {
                    final MailServletInterface mailInterface = MailServletInterface.getInstance(session);
                    try {
                        final MailFolder updateFolder = mailInterface.getFolder(folderIdentifier, true);
                        if (updateFolder != null) {
                            final MailFolderDescription mfd = new MailFolderDescription();
                            mfd.setFullname(updateFolder.getFullname());
                            mfd.setAccountId(MailFolderUtility.prepareMailFolderParam(folderIdentifier).getAccountId());
                            mfd.setExists(updateFolder.exists());
                            mfd.setSeparator(updateFolder.getSeparator());
                            com.openexchange.mail.json.parser.FolderParser.parse(jsonObj, mfd, session, mailInterface.getAccountID());
                            retval = mailInterface.saveFolder(mfd);
                        }
                    } finally {
                        try {
                            mailInterface.close(true);
                        } catch (final OXException e) {
                            LOG.error("", e);
                        }
                    }
                } else {
                    /*-
                     * A messaging folder identifier
                     *
                     * Check for move to another account
                     */
                    final int accountId = mfi.getAccountId();
                    boolean done = false;
                    if (jsonObj.has(FolderChildFields.FOLDER_ID)) {
                        final MessagingFolderIdentifier pfi = new MessagingFolderIdentifier(jsonObj.getString(FolderChildFields.FOLDER_ID));
                        if (accountId != pfi.getAccountId()) {
                            /*
                             * Move to another account... Use new folder storage API
                             */
                            final MessagingFolderParser.ParsedMessagingFolder pmf = new MessagingFolderParser.ParsedMessagingFolder();
                            pmf.setID(mfi.toString());
                            pmf.parse(jsonObj);
                            final com.openexchange.folderstorage.internal.performers.UpdatePerformer up =
                                new com.openexchange.folderstorage.internal.performers.UpdatePerformer(session, new FolderServiceDecorator().put("permissions", paramContainer.getStringParam("permissions")));
                            up.doUpdate(pmf, null);
                            retval = mfi.toString();
                            done = true;
                        }
                    }
                    if (!done) {
                        final String serviceId = mfi.getServiceId();
                        final MessagingService messagingService = messagingServiceRegistry().getMessagingService(serviceId, session.getUserId(), session.getContextId());
                        final MessagingAccountAccess accountAccess = messagingService.getAccountAccess(accountId, session);
                        accountAccess.connect();
                        try {
                            /*
                             * Update
                             */
                            final MessagingFolder updateFolder = accountAccess.getFolderAccess().getFolder(mfi.getFullname());
                            final DefaultMessagingFolder dmf = new DefaultMessagingFolder();
                            dmf.setId(updateFolder.getId());
                            dmf.setExists(true);
                            dmf.setSeparator(updateFolder.getSeparator());
                            com.openexchange.ajax.parser.MessagingFolderParser.parse(jsonObj, dmf, session);
                            accountAccess.getFolderAccess().updateFolder(updateFolder.getId(), dmf);
                            retval = mfi.toString();
                        } finally {
                            accountAccess.close();
                        }
                    }
                }
            }
        } catch (final OXException e) {
            LOG.error("", e);
            response.setException(e);
        } catch (final Exception e) {
            final OXException wrapper = getWrappingOXException(e);
            LOG.error("", wrapper);
            response.setException(wrapper);
        }
        /*
         * Close response and flush print writer
         */
        response.setData(retval);
        response.setTimestamp(lastModifiedDate);
        return response;
    }

    public void actionPutInsertFolder(final ServerSession session, final JSONWriter w, final JSONObject requestObj) throws JSONException {
        ResponseWriter.write(actionPutInsertFolder(session, requestObj.getJSONObject(ResponseFields.DATA), ParamContainer.getInstance(
            requestObj)), w, localeFrom(session));
    }

    private final void actionPutInsertFolder(final HttpServletRequest req, final HttpServletResponse resp) throws IOException, ServletException {
        ServerSession session = getSessionObject(req);
        try {
            ResponseWriter.write(actionPutInsertFolder(session, new JSONObject(getBody(req)), ParamContainer.getInstance(
                req,
                resp)), resp.getWriter(), localeFrom(session));
        } catch (final JSONException e) {
            try {
                ResponseWriter.writeException(OXJSONExceptionCodes.JSON_WRITE_ERROR.create(e, new Object[0]), new JSONWriter(
                    resp.getWriter()), localeFrom(session));
            } catch (final JSONException jsonError) {
                throw new ServletException(e.getMessage(), jsonError);
            }
        }
    }

    private final Response actionPutInsertFolder(final ServerSession session, final JSONObject jsonObj, final ParamContainer paramContainer) {
        /*
         * Some variables
         */
        final Response response = new Response(session);
        Date lastModifiedDate = null;
        Object retval = JSONObject.NULL;
        /*
         * Start response
         */
        try {
            final Context ctx = session.getContext();
            final String parentFolder = paramContainer.checkStringParam(FolderChildFields.FOLDER_ID);
            int parentFolderId = -1;
            if ((parentFolderId = getUnsignedInteger(parentFolder)) >= 0) {
                final FolderSQLInterface foldersqlinterface = new RdbFolderSQLInterface(session);
                FolderObject fo = new FolderObject();
                fo.setParentFolderID(parentFolderId);
                new FolderParser(session.getUserPermissionBits()).parse(fo, jsonObj);
                fo = foldersqlinterface.saveFolderObject(fo, null);
                retval = Integer.toString(fo.getObjectID());
                lastModifiedDate = fo.getLastModified();
            } else if (parentFolder.startsWith(FolderObject.SHARED_PREFIX)) {
                throw OXFolderExceptionCode.NO_CREATE_SUBFOLDER_PERMISSION.create(session.getUserId(),
                    parentFolder,
                    Integer.valueOf(ctx.getContextId()));
            } else {
                final MessagingFolderIdentifier mfi = MessagingFolderIdentifier.parseFQN(parentFolder);
                if (null == mfi) {
                    final MailServletInterface mailInterface = MailServletInterface.getInstance(session);
                    try {
                        final FullnameArgument arg = MailFolderUtility.prepareMailFolderParam(parentFolder);
                        final MailFolder parent = mailInterface.getFolder(parentFolder, true);
                        final MailFolderDescription mfd = new MailFolderDescription();
                        mfd.setParentFullname(arg.getFullname());
                        mfd.setParentAccountId(arg.getAccountId());
                        mfd.setSeparator(parent.getSeparator());
                        com.openexchange.mail.json.parser.FolderParser.parse(jsonObj, mfd, session, arg.getAccountId());
                        mfd.setExists(false);
                        retval = mailInterface.saveFolder(mfd);
                    } finally {
                        try {
                            mailInterface.close(true);
                        } catch (final OXException e) {
                            LOG.error("", e);
                        }
                    }
                } else {
                    /*
                     * A messaging folder identifier
                     */
                    final String serviceId = mfi.getServiceId();
                    final MessagingService messagingService = messagingServiceRegistry().getMessagingService(serviceId,session.getUserId(), session.getContextId());
                    final int accountId = mfi.getAccountId();
                    final MessagingAccountAccess accountAccess = messagingService.getAccountAccess(accountId, session);
                    accountAccess.connect();
                    try {
                        final MessagingFolder parent = accountAccess.getFolderAccess().getFolder(mfi.getFullname());
                        final DefaultMessagingFolder dmf = new DefaultMessagingFolder();
                        dmf.setParentId(mfi.getFullname());
                        dmf.setSeparator(parent.getSeparator());
                        com.openexchange.ajax.parser.MessagingFolderParser.parse(jsonObj, dmf, session);
                        dmf.setExists(false);
                        final String newId = accountAccess.getFolderAccess().createFolder(dmf);
                        retval = MessagingFolderIdentifier.getFQN(serviceId, accountId, newId);
                    } finally {
                        accountAccess.close();
                    }
                }
            }
        } catch (final OXException e) {
            LOG.error("", e);
            response.setException(e);
        } catch (final Exception e) {
            final OXException wrapper = getWrappingOXException(e);
            LOG.error("", wrapper);
            response.setException(wrapper);
        }
        /*
         * Close response and flush print writer
         */
        response.setData(retval);
        response.setTimestamp(lastModifiedDate);
        return response;
    }

    public void actionPutDeleteFolder(final ServerSession session, final JSONWriter w, final JSONObject requestObj) throws JSONException {
        ResponseWriter.write(actionPutDeleteFolder(session, requestObj.getString(ResponseFields.DATA), ParamContainer.getInstance(
            requestObj)), w, localeFrom(session));
    }

    private final void actionPutDeleteFolder(final HttpServletRequest req, final HttpServletResponse resp) throws IOException, ServletException {
        ServerSession session = getSessionObject(req);
        try {
            ResponseWriter.write(actionPutDeleteFolder(session, getBody(req), ParamContainer.getInstance(
                req,
                resp)), resp.getWriter(), localeFrom(session));
        } catch (final JSONException e) {
            try {
                ResponseWriter.writeException(OXJSONExceptionCodes.JSON_WRITE_ERROR.create(e, new Object[0]), new JSONWriter(
                    resp.getWriter()), localeFrom(session));
            } catch (final JSONException jsonError) {
                throw new ServletException(e.getMessage(), jsonError);
            }
        }
    }

    private final Response actionPutDeleteFolder(final ServerSession session, final String body, final ParamContainer paramContainer) throws JSONException {
        /*
         * Some variables
         */
        final Response response = new Response(session);
        final OXJSONWriter jsonWriter = new OXJSONWriter();
        Date lastModifiedDate = null;
        /*
         * Start response
         */
        jsonWriter.array();
        try {
            Date timestamp = null;
            final JSONArray jsonArr = new JSONArray(body);
            FolderSQLInterface foldersqlinterface = null;
            MailServletInterface mailInterface = null;
            try {
                long lastModified = 0;
                final int arrayLength = jsonArr.length();
                final Context ctx = session.getContext();
                final OXFolderAccess access = new OXFolderAccess(ctx);
                NextId: for (int i = 0; i < arrayLength; i++) {
                    final String deleteIdentifier = jsonArr.getString(i);
                    int delFolderId = -1;
                    if ((delFolderId = getUnsignedInteger(deleteIdentifier)) >= 0) {
                        if (timestamp == null) {
                            timestamp = paramContainer.checkDateParam(PARAMETER_TIMESTAMP);
                        }
                        if (foldersqlinterface == null) {
                            foldersqlinterface = new RdbFolderSQLInterface(session, access);
                        }
                        FolderObject delFolderObj;
                        try {
                            delFolderObj = access.getFolderObject(delFolderId);
                        } catch (final OXException exc) {
                            /*
                             * Folder could not be found and therefore need not to be deleted
                             */
                            continue NextId;
                        }
                        if (delFolderObj.getLastModified().getTime() > timestamp.getTime()) {
                            jsonWriter.value(delFolderObj.getObjectID());
                            continue NextId;
                        }
                        foldersqlinterface.deleteFolderObject(delFolderObj, timestamp);
                        lastModified = Math.max(lastModified, delFolderObj.getLastModified().getTime());
                    } else if (deleteIdentifier.startsWith(FolderObject.SHARED_PREFIX)) {
                        throw OXFolderExceptionCode.NO_ADMIN_ACCESS.create(session.getUserId(),
                            deleteIdentifier,
                            Integer.valueOf(ctx.getContextId()));
                    } else {
                        final MessagingFolderIdentifier mfi = MessagingFolderIdentifier.parseFQN(deleteIdentifier);
                        if (null == mfi) {
                            if (session.getUserPermissionBits().hasWebMail()) {
                                if (mailInterface == null) {
                                    mailInterface = MailServletInterface.getInstance(session);
                                }
                                mailInterface.deleteFolder(deleteIdentifier);
                            } else {
                                jsonWriter.value(deleteIdentifier);
                            }
                        } else {
                            final MessagingService messagingService = messagingServiceRegistry().getMessagingService(mfi.getServiceId(), session.getUserId(), session.getContextId());
                            final MessagingAccountAccess accountAccess = messagingService.getAccountAccess(mfi.getAccountId(), session);
                            accountAccess.connect();
                            try {
                                accountAccess.getFolderAccess().deleteFolder(mfi.getFullname());
                            } finally {
                                accountAccess.close();
                            }
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
        } catch (final OXException e) {
            LOG.error("", e);
            response.setException(e);
        } catch (final Exception e) {
            final OXException wrapper = getWrappingOXException(e);
            LOG.error("", wrapper);
            response.setException(wrapper);
        }
        /*
         * Close response and flush print writer
         */
        jsonWriter.endArray();
        response.setData(jsonWriter.getObject());
        response.setTimestamp(lastModifiedDate);
        return response;
    }

    public void actionPutClearFolder(final ServerSession session, final JSONWriter w, final JSONObject requestObj) throws JSONException {
        ResponseWriter.write(actionPutClearFolder(session, requestObj.getString(ResponseFields.DATA), ParamContainer.getInstance(
            requestObj)), w, localeFrom(session));
    }

    private final void actionPutClearFolder(final HttpServletRequest req, final HttpServletResponse resp) throws IOException, ServletException {
        ServerSession session = getSessionObject(req);
        try {
            ResponseWriter.write(actionPutClearFolder(session, getBody(req), ParamContainer.getInstance(
                req,
                resp)), resp.getWriter(), localeFrom(session));
        } catch (final JSONException e) {
            try {
                ResponseWriter.writeException(OXJSONExceptionCodes.JSON_WRITE_ERROR.create(e, new Object[0]), new JSONWriter(
                    resp.getWriter()), localeFrom(session));
            } catch (final JSONException jsonError) {
                throw new ServletException(e.getMessage(), jsonError);
            }
        }
    }

    private final Response actionPutClearFolder(final ServerSession session, final String body, final ParamContainer paramContainer) throws JSONException {
        /*
         * Some variables
         */
        final Response response = new Response(session);
        final OXJSONWriter jsonWriter = new OXJSONWriter();
        Date lastModifiedDate = null;
        /*
         * Start response
         */
        jsonWriter.array();
        try {
            final Context ctx = ContextStorage.getStorageContext(session.getContextId());
            Date timestamp = null;
            final JSONArray jsonArr = new JSONArray(body);
            final int length = jsonArr.length();
            FolderSQLInterface folderInterface = null;
            MailServletInterface mailInterface = null;
            try {
                long lastModified = 0;
                final OXFolderAccess access = new OXFolderAccess(ctx);
                NextId: for (int i = 0; i < length; i++) {
                    final String deleteIdentifier = jsonArr.getString(i);
                    int delFolderId = -1;
                    if ((delFolderId = getUnsignedInteger(deleteIdentifier)) >= 0) {
                        if (timestamp == null) {
                            timestamp = paramContainer.checkDateParam(PARAMETER_TIMESTAMP);
                        }
                        if (folderInterface == null) {
                            folderInterface = new RdbFolderSQLInterface(session, access);
                        }
                        FolderObject delFolderObj;
                        try {
                            delFolderObj = access.getFolderObject(delFolderId);
                        } catch (final OXException exc) {
                            LOG.warn("", exc);
                            continue NextId;
                        }
                        if (delFolderObj.getLastModified().getTime() > timestamp.getTime()) {
                            jsonWriter.value(delFolderObj.getObjectID());
                            continue NextId;
                        }
                        folderInterface.clearFolder(delFolderObj, timestamp);
                        lastModified = Math.max(lastModified, delFolderObj.getLastModified().getTime());
                    } else if (deleteIdentifier.startsWith(FolderObject.SHARED_PREFIX)) {
                        throw OXFolderExceptionCode.NO_ADMIN_ACCESS.create(session.getUserId(), deleteIdentifier, Integer.valueOf(ctx.getContextId()));
                    } else {
                        final MessagingFolderIdentifier mfi = MessagingFolderIdentifier.parseFQN(deleteIdentifier);
                        if (null == mfi) {
                            if (UserConfigurationStorage.getInstance().getUserConfigurationSafe(session.getUserId(), ctx).hasWebMail()) {
                                if (mailInterface == null) {
                                    mailInterface = MailServletInterface.getInstance(session);
                                }
                                mailInterface.clearFolder(deleteIdentifier);
                            } else {
                                jsonWriter.value(deleteIdentifier);
                            }
                        } else {
                            final MessagingService messagingService = messagingServiceRegistry().getMessagingService(mfi.getServiceId(), session.getUserId(), session.getContextId());
                            final MessagingAccountAccess accountAccess = messagingService.getAccountAccess(mfi.getAccountId(), session);
                            accountAccess.connect();
                            try {
                                accountAccess.getFolderAccess().clearFolder(mfi.getFullname());
                            } finally {
                                accountAccess.close();
                            }
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
        } catch (final OXException e) {
            LOG.error("", e);
            if (!e.getCategory().equals(Category.CATEGORY_PERMISSION_DENIED)) {
                response.setException(e);
            }
        } catch (final Exception e) {
            final OXException wrapper = getWrappingOXException(e);
            LOG.error("", wrapper);
            response.setException(wrapper);
        }
        /*
         * Close response and flush print writer
         */
        jsonWriter.endArray();
        response.setData(jsonWriter.getObject());
        response.setTimestamp(lastModifiedDate);
        return response;
    }

    private final void actionPutRemoveTestFolder(final HttpServletRequest req, final HttpServletResponse resp) throws Exception {
        /*
         * Some variables
         */
        final OXJSONWriter jsonWriter = new OXJSONWriter();
        final ServerSession session = getSessionObject(req);
        final long lastModified = 0;
        String error = null;
        /*
         * Start response
         */
        AJAXServlet.startResponse(jsonWriter);
        String dataObj = "FAILED";
        try {
            final int[] delids = checkIntArrayParam(req, "del_ids");
            final OXFolderManager oxma = OXFolderManager.getInstance(session);
            oxma.cleanUpTestFolders(delids, session.getContext());
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

    private static final String checkStringParam(final HttpServletRequest req, final String paramName) throws OXException {
        final String paramVal = req.getParameter(paramName);
        if (paramVal == null) {
            throw OXFolderExceptionCode.MISSING_PARAMETER.create(paramName);
        }
        return paramVal;
    }

    private static final Pattern PATERN_SPLIT = Pattern.compile(" *, *");

    private static final int[] checkIntArrayParam(final HttpServletRequest req, final String paramName) throws OXException {
        String tmp = req.getParameter(paramName);
        if (tmp == null) {
            throw OXFolderExceptionCode.MISSING_PARAMETER.create(paramName);
        }
        final String[] sa = PATERN_SPLIT.split(tmp, 0);
        tmp = null;
        final int intArray[] = new int[sa.length];
        for (int a = 0; a < sa.length; a++) {
            try {
                intArray[a] = Integer.parseInt(sa[a]);
            } catch (final NumberFormatException e) {
                throw OXFolderExceptionCode.BAD_PARAM_VALUE.create(e, sa[a], paramName);
            }
        }
        return intArray;
    }

    /**
     * Writes an account's root folder into a JSON array.
     */
    private static final class MailRootFolderWriter extends AbstractTask<Object> {

        private final JSONArray[] arrays;

        private final ServerSession session;

        private final org.slf4j.Logger logger;

        private final MailAccount mailAccount;

        private final int[] columns;

        private final int index;

        MailRootFolderWriter(final JSONArray[] arrays, final ServerSession session, final org.slf4j.Logger logger, final MailAccount mailAccount, final int[] columns, final int index) {
            this.arrays = arrays;
            this.session = session;
            this.logger = logger;
            this.mailAccount = mailAccount;
            this.columns = columns;
            this.index = index;
        }

        @Override
        public Object call() throws OXException {
            MailAccess<?, ?> mailAccess = null;
            try {
                final int accountId = mailAccount.getId();
                try {
                    mailAccess = MailAccess.getInstance(session, accountId);
                } catch (final OXException e) {
                    arrays[index] = null;
                    if (MailExceptionCode.ACCOUNT_DOES_NOT_EXIST.getNumber() == e.getCode()) {
                        logger.debug("", e);
                        return null;
                    }
                    logger.error("", e);
                    throw e;
                }
                final MailFolder rootFolder = mailAccess.getRootFolder();
                final MailFolderFieldWriter[] mailFolderWriters =
                    com.openexchange.mail.json.writer.FolderWriter.getMailFolderFieldWriter(columns, mailAccess.getMailConfig(), session);
                final JSONArray ja = new JSONArray();
                final com.openexchange.mail.json.writer.FolderWriter.JSONArrayPutter putter =
                    new com.openexchange.mail.json.writer.FolderWriter.JSONArrayPutter().setJSONArray(ja);
                for (final MailFolderFieldWriter mailFolderWriter : mailFolderWriters) {
                    mailFolderWriter.writeField(
                        putter,
                        accountId,
                        rootFolder,
                        (mailAccount.isDefaultAccount()) ? MailFolder.DEFAULT_FOLDER_NAME : mailAccount.getName(),
                        1,
                        MailFolderUtility.prepareFullname(accountId, MailFolder.DEFAULT_FOLDER_ID),
                        FolderObject.SYSTEM_MODULE,
                        false);
                }
                arrays[index] = ja;
                return null;
            } catch (final OXException e) {
                logger.error("", e);
                arrays[index] = null;
                throw e;
            } finally {
                if (null != mailAccess) {
                    mailAccess.close(true);
                }
            }
        }
    }

    /**
     * Writes a messaging account's root folder into a JSON array.
     */
    private static final class MessagingRootFolderWriter extends AbstractTask<Object> {

        private final JSONArray[] arrays;

        private final ServerSession session;

        private final org.slf4j.Logger logger;

        private final MessagingAccount messagingAccount;

        private final int[] columns;

        private final int index;

        MessagingRootFolderWriter(final JSONArray[] arrays, final ServerSession session, final org.slf4j.Logger logger, final MessagingAccount messagingAccount, final int[] columns, final int index) {
            this.arrays = arrays;
            this.session = session;
            this.logger = logger;
            this.messagingAccount = messagingAccount;
            this.columns = columns;
            this.index = index;
        }

        @Override
        public Object call() throws OXException {
            final MessagingFolder rootFolder;
            final MessagingAccountAccess access;
            final int accountId = messagingAccount.getId();
            final String serviceId;
            try {
                final MessagingService service = messagingAccount.getMessagingService();
                access = service.getAccountAccess(accountId, session);
                rootFolder = access.getRootFolder();
                serviceId = service.getId();
            } catch (final OXException e) {
                arrays[index] = null;
                logger.error("", e);
                throw e;
            } catch (final Exception e) {
                arrays[index] = null;
                logger.error("", e);
                throw MessagingExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
            }
            try {
                final MessagingFolderFieldWriter[] writers = MessagingFolderWriter.getMessagingFolderFieldWriter(columns, session);
                final JSONArray ja = new JSONArray();
                final com.openexchange.ajax.writer.MessagingFolderWriter.JSONValuePutter putter =
                    new com.openexchange.ajax.writer.MessagingFolderWriter.JSONArrayPutter(ja);
                final String displayName = messagingAccount.getDisplayName();
                final String fqn = MessagingFolderIdentifier.getFQN(serviceId, accountId, MessagingFolder.ROOT_FULLNAME);
                for (final MessagingFolderFieldWriter writer : writers) {
                    writer.writeField(putter, serviceId, accountId, rootFolder, displayName, -1, fqn, FolderObject.MESSAGING, false);
                }
                arrays[index] = ja;
                return null;
            } catch (final OXException e) {
                logger.error("", e);
                arrays[index] = null;
                throw e;
            } finally {
                access.close();
            }
        }
    }

    /**
     * {@link DisplayNameComparator} - Sorts display names with respect to a certain locale
     *
     * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
     */
    private static final class DisplayNameComparator implements Comparator<String> {

        private final Collator collator;

        public DisplayNameComparator(final Locale locale) {
            super();
            collator = Collators.getSecondaryInstance(locale);
        }

        @Override
        public int compare(final String displayName1, final String displayName2) {
            return collator.compare(displayName1, displayName2);
        }

    }

    private static final class MailAccountComparator implements Comparator<MailAccount> {

        private final Collator collator;

        public MailAccountComparator(final Locale locale) {
            super();
            collator = Collators.getSecondaryInstance(locale);
        }

        @Override
        public int compare(final MailAccount o1, final MailAccount o2) {
            if (UnifiedInboxManagement.PROTOCOL_UNIFIED_INBOX.equals(o1.getMailProtocol())) {
                if (UnifiedInboxManagement.PROTOCOL_UNIFIED_INBOX.equals(o2.getMailProtocol())) {
                    return 0;
                }
                return -1;
            } else if (UnifiedInboxManagement.PROTOCOL_UNIFIED_INBOX.equals(o2.getMailProtocol())) {
                return 1;
            }
            if (o1.isDefaultAccount()) {
                if (o2.isDefaultAccount()) {
                    return 0;
                }
                return -1;
            } else if (o2.isDefaultAccount()) {
                return 1;
            }
            return collator.compare(o1.getName(), o2.getName());
        }

    }

    private static com.openexchange.mail.json.writer.FolderWriter.JSONArrayPutter newArrayPutter() {
        return new com.openexchange.mail.json.writer.FolderWriter.JSONArrayPutter();
    }

    private static com.openexchange.ajax.writer.MessagingFolderWriter.JSONArrayPutter newMessagingArrayPutter() {
        return new com.openexchange.ajax.writer.MessagingFolderWriter.JSONArrayPutter();
    }

    private static com.openexchange.mail.json.writer.FolderWriter.JSONObjectPutter newObjectPutter() {
        return new com.openexchange.mail.json.writer.FolderWriter.JSONObjectPutter();
    }

    private static com.openexchange.ajax.writer.MessagingFolderWriter.JSONObjectPutter newMessagingObjectPutter() {
        return new com.openexchange.ajax.writer.MessagingFolderWriter.JSONObjectPutter();
    }

    private static MessagingServiceRegistry messagingServiceRegistry() throws OXException {
        return ServerServiceRegistry.getInstance().getService(MessagingServiceRegistry.class, true);
    }

}
