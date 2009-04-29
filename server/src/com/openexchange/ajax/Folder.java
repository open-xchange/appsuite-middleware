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

import static com.openexchange.tools.oxfolder.OXFolderUtility.folderModule2String;
import static com.openexchange.tools.oxfolder.OXFolderUtility.getUserName;
import java.io.IOException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;
import java.util.regex.Pattern;
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
import com.openexchange.ajax.fields.ResponseFields;
import com.openexchange.ajax.helper.ParamContainer;
import com.openexchange.ajax.parser.FolderParser;
import com.openexchange.ajax.writer.FolderWriter;
import com.openexchange.ajax.writer.ResponseWriter;
import com.openexchange.ajax.writer.FolderWriter.FolderFieldWriter;
import com.openexchange.api2.FolderSQLInterface;
import com.openexchange.api2.OXException;
import com.openexchange.api2.RdbFolderSQLInterface;
import com.openexchange.cache.impl.FolderCacheManager;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.EnumComponent;
import com.openexchange.groupware.AbstractOXException.Category;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.i18n.Groups;
import com.openexchange.groupware.ldap.LdapException;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.json.OXJSONWriter;
import com.openexchange.mail.FullnameArgument;
import com.openexchange.mail.MailException;
import com.openexchange.mail.MailServletInterface;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.cache.SessionMailCache;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mail.dataobjects.MailFolderDescription;
import com.openexchange.mail.json.writer.FolderWriter.MailFolderFieldWriter;
import com.openexchange.mail.utils.MailFolderUtility;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.mailaccount.UnifiedINBOXManagement;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.iterator.FolderObjectIterator;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.oxfolder.OXFolderAccess;
import com.openexchange.tools.oxfolder.OXFolderException;
import com.openexchange.tools.oxfolder.OXFolderManager;
import com.openexchange.tools.oxfolder.OXFolderException.FolderCode;
import com.openexchange.tools.servlet.http.Tools;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link Folder} - The folder servlet.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class Folder extends SessionServlet {

    /**
     * The constant for Inbox mail folder. TODO: Should be read from StringHelper utility class!
     */
    private static final String DEF_NAME_INBOX = "Inbox";

    private static final String STR_INBOX = "INBOX";

    private static final long serialVersionUID = -889739420660750770L;

    private static transient final Log LOG = LogFactory.getLog(Folder.class);

    private static final AbstractOXException getWrappingOXException(final Throwable cause) {
        if (LOG.isWarnEnabled()) {
            final StringBuilder warnBuilder = new StringBuilder(140);
            warnBuilder.append("An unexpected exception occurred, which is going to be wrapped for proper display.\n");
            warnBuilder.append("For safety reason its original content is display here.");
            LOG.warn(warnBuilder.toString(), cause);
        }
        final String message = cause.getMessage();
        return new AbstractOXException(
            EnumComponent.FOLDER,
            Category.INTERNAL_ERROR,
            9999,
            null == message ? "[Not available]" : message,
            cause);
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
            throw getWrappingOXException(new Exception("Action \"" + actionStr + "\" NOT supported via GET on /ajax/folders"));
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
            throw getWrappingOXException(new Exception("Action \"" + actionStr + "\" NOT supported via PUT on /ajax/folders"));
        }
    }

    /**
     * Performs the GET request to send back root folders
     */
    public void actionGetRoot(final ServerSession session, final JSONWriter w, final JSONObject requestObj) throws JSONException {
        ResponseWriter.write(actionGetRoot(session, ParamContainer.getInstance(requestObj, EnumComponent.FOLDER)), w);
    }

    private final void actionGetRoot(final HttpServletRequest req, final HttpServletResponse resp) throws JSONException, IOException {
        ResponseWriter.write(
            actionGetRoot(getSessionObject(req), ParamContainer.getInstance(req, EnumComponent.FOLDER, resp)),
            resp.getWriter());
    }

    /**
     * Performs the GET request to send back root folders
     */
    private final Response actionGetRoot(final ServerSession session, final ParamContainer paramContainer) throws JSONException {
        /*
         * Some variables
         */
        final Response response = new Response();
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
            final FolderWriter folderWriter = new FolderWriter(jsonWriter, session, ctx);
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
                } else if (rootFolder.getObjectID() == FolderObject.SYSTEM_SHARED_FOLDER_ID && !session.getUserConfiguration().hasFullSharedFolderAccess()) {
                    /*
                     * User does not hold READ_CREATE_SHARED_FOLDERS in user configuration; mark system shared folder to have no subfolders
                     */
                    hasSubfolder = 0;
                }
                lastModified = rootFolder.getLastModified() == null ? lastModified : Math.max(
                    lastModified,
                    rootFolder.getLastModified().getTime());
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
        } catch (final OXFolderException e) {
            LOG.error(e.getMessage(), e);
            response.setException(e);
        } catch (final AbstractOXException e) {
            LOG.error(e.getMessage(), e);
            response.setException(e);
        } catch (final Exception e) {
            final AbstractOXException wrapper = getWrappingOXException(e);
            LOG.error(wrapper.getMessage(), wrapper);
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
        ResponseWriter.write(actionGetSubfolders(session, ParamContainer.getInstance(requestObj, EnumComponent.FOLDER)), w);
    }

    private final void actionGetSubfolders(final HttpServletRequest req, final HttpServletResponse resp) throws IOException, ServletException {
        try {
            ResponseWriter.write(
                actionGetSubfolders(getSessionObject(req), ParamContainer.getInstance(req, EnumComponent.FOLDER, resp)),
                resp.getWriter());
        } catch (final JSONException e) {
            sendErrorAsJS(resp, RESPONSE_ERROR);
        }
    }

    private final Response actionGetSubfolders(final ServerSession session, final ParamContainer paramContainer) throws JSONException {
        /*
         * Some variables
         */
        final Response response = new Response();
        final OXJSONWriter jsonWriter = new OXJSONWriter();
        Date lastModifiedDate = null;
        /*
         * Start response
         */
        jsonWriter.array();
        try {
            final Context ctx = session.getContext();
            final Locale locale = session.getUser().getLocale();
            final StringHelper strHelper = new StringHelper(locale);
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
            final FolderWriter folderWriter = new FolderWriter(jsonWriter, session, ctx);
            int parentId = -1;
            if ((parentId = getUnsignedInteger(parentIdentifier)) >= 0) {
                // TODO: DELEGATE TO getRootFolder() if parentId is "0"
                long lastModified = 0;
                final FolderSQLInterface foldersqlinterface = new RdbFolderSQLInterface(session);
                final FolderFieldWriter[] writers = folderWriter.getFolderFieldWriter(columns);
                /*
                 * Write requested child folders
                 */
                if (parentId == FolderObject.VIRTUAL_LIST_TASK_FOLDER_ID) {
                    /*
                     * Append non-tree visible task folders
                     */
                    final Queue<FolderObject> q = ((FolderObjectIterator) foldersqlinterface.getNonTreeVisiblePublicTaskFolders()).asQueue();
                    final int size = q.size();
                    final Iterator<FolderObject> iter = q.iterator();
                    for (int i = 0; i < size; i++) {
                        final FolderObject listFolder = iter.next();
                        lastModified = listFolder.getLastModified() == null ? lastModified : Math.max(
                            lastModified,
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
                    final Queue<FolderObject> q = ((FolderObjectIterator) foldersqlinterface.getNonTreeVisiblePublicCalendarFolders()).asQueue();
                    final int size = q.size();
                    final Iterator<FolderObject> iter = q.iterator();
                    for (int i = 0; i < size; i++) {
                        final FolderObject listFolder = iter.next();
                        lastModified = listFolder.getLastModified() == null ? lastModified : Math.max(
                            lastModified,
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
                    final Queue<FolderObject> q = ((FolderObjectIterator) foldersqlinterface.getNonTreeVisiblePublicContactFolders()).asQueue();
                    final int size = q.size();
                    final Iterator<FolderObject> iter = q.iterator();
                    for (int i = 0; i < size; i++) {
                        final FolderObject listFolder = iter.next();
                        lastModified = listFolder.getLastModified() == null ? lastModified : Math.max(
                            lastModified,
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
                    final Queue<FolderObject> q = ((FolderObjectIterator) foldersqlinterface.getNonTreeVisiblePublicInfostoreFolders()).asQueue();
                    final int size = q.size();
                    final Iterator<FolderObject> iter = q.iterator();
                    for (int i = 0; i < size; i++) {
                        final FolderObject listFolder = iter.next();
                        lastModified = listFolder.getLastModified() == null ? lastModified : Math.max(
                            lastModified,
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
                    if (!session.getUserConfiguration().hasInfostore()) {
                        throw new OXFolderException(
                            FolderCode.NO_MODULE_ACCESS,
                            getUserName(session),
                            folderModule2String(FolderObject.INFOSTORE),
                            Integer.valueOf(ctx.getContextId()));
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
                    final Iterator<FolderObject> iter = l.iterator();
                    for (int i = 0; i < size; i++) {
                        final FolderObject fo = iter.next();
                        lastModified = fo.getLastModified() == null ? lastModified : Math.max(lastModified, fo.getLastModified().getTime());
                        jsonWriter.array();
                        try {
                            for (int j = 0; j < writers.length; j++) {
                                writers[j].writeField(jsonWriter, fo, false, FolderObject.getFolderString(fo.getObjectID(), locale), -1);
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
                            final FolderObject virtualListFolder = FolderObject.createVirtualFolderObject(
                                FolderObject.VIRTUAL_LIST_INFOSTORE_FOLDER_ID,
                                FolderObject.getFolderString(FolderObject.VIRTUAL_LIST_INFOSTORE_FOLDER_ID, locale),
                                FolderObject.INFOSTORE,
                                true,
                                FolderObject.SYSTEM_TYPE);
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
                        final Queue<FolderObject> q = ((FolderObjectIterator) foldersqlinterface.getSubfolders(
                            FolderObject.SYSTEM_SHARED_FOLDER_ID,
                            null)).asQueue();
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
                            } catch (final LdapException e) {
                                if (sharedFolder.getCreatedBy() != OCLPermission.ALL_GROUPS_AND_USERS) {
                                    throw new AbstractOXException(e);
                                }
                                creatorDisplayName = strHelper.getString(Groups.ZERO_DISPLAYNAME);
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
                        final FolderObject virtualOwnerFolder = FolderObject.createVirtualSharedFolderObject(
                            displayNames.get(displayName).intValue(),
                            displayName);
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
                        if (session.getUserConfiguration().hasWebMail() && !ignoreMailfolder) {
                            /*
                             * Get all user mail accounts
                             */
                            final List<MailAccount> accounts;
                            {
                                final MailAccountStorageService storageService = ServerServiceRegistry.getInstance().getService(
                                    MailAccountStorageService.class,
                                    true);
                                final MailAccount[] accountsArr = storageService.getUserMailAccounts(
                                    session.getUserId(),
                                    session.getContextId());
                                final List<MailAccount> tmp = new ArrayList<MailAccount>(accountsArr.length);
                                tmp.addAll(Arrays.asList(accountsArr));
                                // Sort them
                                Collections.sort(tmp, new MailAccountComparator(locale));
                                accounts = tmp;
                            }

                            for (final MailAccount mailAccount : accounts) {
                                final MailAccess<?, ?> mailAccess = MailAccess.getInstance(session, mailAccount.getId());
                                boolean close = false;
                                try {
                                    final MailFolder rootFolder = mailAccess.getRootFolder();
                                    close = true;
                                    final MailFolderFieldWriter[] mailFolderWriters = com.openexchange.mail.json.writer.FolderWriter.getMailFolderFieldWriter(
                                        columns,
                                        mailAccess.getMailConfig());
                                    final JSONArray ja = new JSONArray();
                                    if (mailAccount.isDefaultAccount()) {
                                        for (int i = 0; i < mailFolderWriters.length; i++) {
                                            mailFolderWriters[i].writeField(
                                                ja,
                                                mailAccount.getId(),
                                                rootFolder,
                                                false,
                                                MailFolder.DEFAULT_FOLDER_NAME,
                                                1,
                                                MailFolderUtility.prepareFullname(mailAccount.getId(), MailFolder.DEFAULT_FOLDER_ID),
                                                FolderObject.SYSTEM_MODULE,
                                                false);
                                        }
                                    } else {
                                        for (int i = 0; i < mailFolderWriters.length; i++) {
                                            mailFolderWriters[i].writeField(
                                                ja,
                                                mailAccount.getId(),
                                                rootFolder,
                                                false,
                                                mailAccount.getName(),
                                                1,
                                                MailFolderUtility.prepareFullname(mailAccount.getId(), MailFolder.DEFAULT_FOLDER_ID),
                                                FolderObject.SYSTEM_MODULE,
                                                false);
                                        }
                                    }
                                    jsonWriter.value(ja);
                                } catch (final MailException e) {
                                    LOG.error(e.getMessage(), e);
                                } finally {
                                    if (close) {
                                        mailAccess.close(true);
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
                            folderWriter.writeOXFolderFieldsAsArray(columns, internalUsers, FolderObject.getFolderString(
                                internalUsers.getObjectID(),
                                locale), -1);
                        } catch (final OXException e) {
                            /*
                             * Internal users folder not visible to current user
                             */
                            LOG.warn(e.getMessage(), e);
                        }
                    }
                    final Queue<FolderObject> q = ((FolderObjectIterator) foldersqlinterface.getSubfolders(parentId, null)).asQueue();
                    final int size = q.size();
                    final Iterator<FolderObject> iter = q.iterator();
                    for (int i = 0; i < size; i++) {
                        final FolderObject fo = iter.next();
                        lastModified = fo.getLastModified() == null ? lastModified : Math.max(lastModified, fo.getLastModified().getTime());
                        jsonWriter.array();
                        try {
                            for (int j = 0; j < writers.length; j++) {
                                writers[j].writeField(jsonWriter, fo, false);
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
                                final FolderObject virtualListFolder = FolderObject.createVirtualFolderObject(
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
                        } catch (final OXFolderException e) {
                            if (e.getDetailNumber() == FolderCode.NO_MODULE_ACCESS.getNumber() && Category.USER_CONFIGURATION.equals(e.getCategory())) {
                                /*
                                 * No non-tree-visible public calendar folders due to user configuration
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
                        } catch (final OXFolderException e) {
                            if (e.getDetailNumber() == FolderCode.NO_MODULE_ACCESS.getNumber() && Category.USER_CONFIGURATION.equals(e.getCategory())) {
                                /*
                                 * No non-tree-visible public contact folders due to user configuration
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
                        } catch (final OXFolderException e) {
                            if (e.getDetailNumber() == FolderCode.NO_MODULE_ACCESS.getNumber() && Category.USER_CONFIGURATION.equals(e.getCategory())) {
                                /*
                                 * No non-tree-visible public task folders due to user configuration
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
                final FolderSQLInterface foldersqlinterface = new RdbFolderSQLInterface(session);
                int sharedOwner;
                try {
                    sharedOwner = Integer.parseInt(parentIdentifier.substring(2));
                } catch (final NumberFormatException exc) {
                    throw getWrappingOXException(exc);
                }
                final FolderFieldWriter[] writers = folderWriter.getFolderFieldWriter(columns);
                final Queue<FolderObject> q = ((FolderObjectIterator) foldersqlinterface.getSharedFoldersFrom(sharedOwner, null)).asQueue();
                final int size = q.size();
                final Iterator<FolderObject> iter = q.iterator();
                for (int i = 0; i < size; i++) {
                    final FolderObject sharedFolder = iter.next();
                    lastModified = sharedFolder.getLastModified() == null ? lastModified : Math.max(
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
                SearchIterator<MailFolder> it = null;
                MailServletInterface mailInterface = null;
                try {
                    mailInterface = MailServletInterface.getInstance(session);
                    /*
                     * E-Mail folder
                     */
                    it = mailInterface.getChildFolders(parentIdentifier, all);
                    final MailFolderFieldWriter[] writers = com.openexchange.mail.json.writer.FolderWriter.getMailFolderFieldWriter(
                        columns,
                        mailInterface.getMailConfig());
                    final int size = it.size();
                    boolean inboxFound = false;
                    for (int i = 0; i < size; i++) {
                        final MailFolder f = it.next();
                        if (!inboxFound && STR_INBOX.equals(f.getFullname())) {
                            inboxFound = true;
                            final JSONArray ja = new JSONArray();
                            // TODO: Translation for INBOX?!
                            for (int j = 0; j < writers.length; j++) {
                                writers[j].writeField(ja, mailInterface.getAccountID(), f, false, DEF_NAME_INBOX, -1, null, -1, all);
                            }
                            jsonWriter.value(ja);
                        } else {
                            final JSONArray ja = new JSONArray();
                            for (int j = 0; j < writers.length; j++) {
                                writers[j].writeField(ja, mailInterface.getAccountID(), f, false, null, -1, null, -1, all);
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
                        } catch (final MailException e) {
                            LOG.error(e.getMessage(), e);
                        }
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
            final AbstractOXException wrapper = getWrappingOXException(e);
            LOG.error(wrapper.getMessage(), wrapper);
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

    /**
     * Performs the GET request to send back the path from a certain folder to root folder
     * 
     * @throws JSONException
     */
    public void actionGetPath(final ServerSession session, final JSONWriter w, final JSONObject requestObj) throws JSONException {
        ResponseWriter.write(actionGetPath(session, ParamContainer.getInstance(requestObj, EnumComponent.FOLDER)), w);
    }

    private final void actionGetPath(final HttpServletRequest req, final HttpServletResponse resp) throws IOException, ServletException {
        try {
            ResponseWriter.write(
                actionGetPath(getSessionObject(req), ParamContainer.getInstance(req, EnumComponent.FOLDER, resp)),
                resp.getWriter());
        } catch (final JSONException e) {
            sendErrorAsJS(resp, RESPONSE_ERROR);
        }
    }

    private final Response actionGetPath(final ServerSession session, final ParamContainer paramContainer) throws JSONException {
        /*
         * Some variables
         */
        final Response response = new Response();
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
            final FolderWriter folderWriter = new FolderWriter(jsonWriter, session, ctx);
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
                MailServletInterface mailInterface = null;
                SearchIterator<MailFolder> it = null;
                try {
                    mailInterface = MailServletInterface.getInstance(session);
                    /*
                     * Pre-Select field writers
                     */
                    it = mailInterface.getPathToDefaultFolder(folderIdentifier);
                    final MailFolderFieldWriter[] writers = com.openexchange.mail.json.writer.FolderWriter.getMailFolderFieldWriter(
                        columns,
                        mailInterface.getMailConfig());
                    final int size = it.size();
                    for (int i = 0; i < size; i++) {
                        final MailFolder fld = it.next();
                        final JSONArray ja = new JSONArray();
                        for (final MailFolderFieldWriter w : writers) {
                            w.writeField(ja, mailInterface.getAccountID(), fld, false);
                        }
                        jsonWriter.value(ja);
                    }
                    it.close();
                    it = null;
                    /*
                     * Write virtual folder "E-Mail"
                     */
                    final MailFolder defaultFolder = mailInterface.getFolder(MailFolder.DEFAULT_FOLDER_ID, true);
                    if (defaultFolder != null) {
                        final JSONArray ja = new JSONArray();
                        for (final MailFolderFieldWriter w : writers) {
                            w.writeField(
                                ja,
                                mailInterface.getAccountID(),
                                defaultFolder,
                                false,
                                MailFolder.DEFAULT_FOLDER_NAME,
                                1,
                                MailFolder.DEFAULT_FOLDER_ID,
                                FolderObject.SYSTEM_MODULE,
                                false);
                        }
                        jsonWriter.value(ja);
                    }
                    /*
                     * Finally, write "private" folder
                     */
                    FolderObject privateFolder;
                    if (FolderCacheManager.isEnabled()) {
                        privateFolder = FolderCacheManager.getInstance().getFolderObject(
                            FolderObject.SYSTEM_PRIVATE_FOLDER_ID,
                            true,
                            ctx,
                            null);
                    } else {
                        privateFolder = FolderObject.loadFolderObjectFromDB(FolderObject.SYSTEM_PRIVATE_FOLDER_ID, ctx);
                    }
                    folderWriter.writeOXFolderFieldsAsArray(columns, privateFolder, FolderObject.getFolderString(
                        FolderObject.SYSTEM_PRIVATE_FOLDER_ID,
                        session.getUser().getLocale()), -1);
                } finally {
                    if (it != null) {
                        it.close();
                        it = null;
                    }
                    if (mailInterface != null) {
                        try {
                            mailInterface.close(true);
                            mailInterface = null;
                        } catch (final MailException e) {
                            LOG.error(e.getMessage(), e);
                        }
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
            final AbstractOXException wrapper = getWrappingOXException(e);
            LOG.error(wrapper.getMessage(), wrapper);
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
        ResponseWriter.write(actionGetUpdatedFolders(session, ParamContainer.getInstance(requestObj, EnumComponent.FOLDER)), w);
    }

    private final void actionGetUpdatedFolders(final HttpServletRequest req, final HttpServletResponse resp) throws IOException, ServletException {
        try {
            ResponseWriter.write(
                actionGetUpdatedFolders(getSessionObject(req), ParamContainer.getInstance(req, EnumComponent.FOLDER, resp)),
                resp.getWriter());
        } catch (final JSONException e) {
            sendErrorAsJS(resp, RESPONSE_ERROR);
        }
    }

    private static final Date DATE_0 = new Date(0);

    private final Response actionGetUpdatedFolders(final ServerSession session, final ParamContainer paramContainer) throws JSONException {
        /*
         * Some variables
         */
        final Response response = new Response();
        final OXJSONWriter jsonWriter = new OXJSONWriter();
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
            final Context ctx = session.getContext();
            final int[] columns = paramContainer.checkIntArrayParam(PARAMETER_COLUMNS);
            final FolderWriter folderWriter = new FolderWriter(jsonWriter, session, ctx);
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
            final OXFolderAccess access = new OXFolderAccess(ctx);
            final Queue<FolderObject> updatedQueue = new LinkedList<FolderObject>();
            final Queue<FolderObject> deletedQueue = ignoreDeleted ? null : new LinkedList<FolderObject>();
            final UserConfiguration userConf = session.getUserConfiguration();
            boolean addSystemSharedFolder = false;
            boolean checkVirtualListFolders = false;
            int size = q.size();
            Iterator<FolderObject> iter = q.iterator();
            for (int i = 0; i < size; i++) {
                final FolderObject fo = iter.next();
                if (fo.isVisible(session.getUserId(), userConf)) {
                    if (fo.isShared(session.getUserId())) {
                        addSystemSharedFolder = true;
                    } else if (FolderObject.PUBLIC == fo.getType()) {
                        if (access.getFolderPermission(fo.getParentFolderID(), session.getUserId(), userConf).isFolderVisible()) {
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
                    if (deletedQueue != null) {
                        deletedQueue.add(fo);
                    }
                }
            }
            /*
             * Check virtual list folders
             */
            if (checkVirtualListFolders && deletedQueue != null) {
                if (userConf.hasTask() && !foldersqlinterface.getNonTreeVisiblePublicTaskFolders().hasNext()) {
                    final FolderObject virtualTasks = new FolderObject(FolderObject.VIRTUAL_LIST_TASK_FOLDER_ID);
                    virtualTasks.setLastModified(DATE_0);
                    deletedQueue.add(virtualTasks);
                }
                if (userConf.hasCalendar() && !foldersqlinterface.getNonTreeVisiblePublicCalendarFolders().hasNext()) {
                    final FolderObject virtualCalendar = new FolderObject(FolderObject.VIRTUAL_LIST_CALENDAR_FOLDER_ID);
                    virtualCalendar.setLastModified(DATE_0);
                    deletedQueue.add(virtualCalendar);
                }
                if (userConf.hasContact() && !foldersqlinterface.getNonTreeVisiblePublicContactFolders().hasNext()) {
                    final FolderObject virtualContact = new FolderObject(FolderObject.VIRTUAL_LIST_CONTACT_FOLDER_ID);
                    virtualContact.setLastModified(DATE_0);
                    deletedQueue.add(virtualContact);
                }
                if (userConf.hasInfostore() && !foldersqlinterface.getNonTreeVisiblePublicInfostoreFolders().hasNext()) {
                    final FolderObject virtualInfostore = new FolderObject(FolderObject.VIRTUAL_LIST_INFOSTORE_FOLDER_ID);
                    virtualInfostore.setLastModified(DATE_0);
                    deletedQueue.add(virtualInfostore);
                }
            }
            /*
             * Check if shared folder must be updated, too
             */
            if (addSystemSharedFolder) {
                final FolderObject sharedFolder = access.getFolderObject(FolderObject.SYSTEM_SHARED_FOLDER_ID);
                sharedFolder.setFolderName(FolderObject.getFolderString(FolderObject.SYSTEM_SHARED_FOLDER_ID, session.getUser().getLocale()));
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
            if (!ignoreDeleted) {
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
            }
            if (includeMailFolders) {
                /*
                 * Get all user mail accounts
                 */
                final MailAccountStorageService storageService = ServerServiceRegistry.getInstance().getService(
                    MailAccountStorageService.class,
                    true);
                final MailAccount[] accounts = storageService.getUserMailAccounts(session.getUserId(), session.getContextId());
                for (final MailAccount mailAccount : accounts) {
                    /*
                     * Clean session caches
                     */
                    SessionMailCache.getInstance(session, mailAccount.getId()).clear();
                    /*
                     * Add root folders
                     */
                    final MailAccess<?, ?> mailAccess = MailAccess.getInstance(session, mailAccount.getId());
                    boolean close = false;
                    try {
                        mailAccess.connect();
                        close = true;
                        final MailFolderFieldWriter[] mailFolderWriters = com.openexchange.mail.json.writer.FolderWriter.getMailFolderFieldWriter(
                            columns,
                            mailAccess.getMailConfig());
                        final MailFolder rootFolder = mailAccess.getFolderStorage().getRootFolder();
                        final JSONArray ja = new JSONArray();
                        if (mailAccount.isDefaultAccount()) {
                            for (int i = 0; i < mailFolderWriters.length; i++) {
                                mailFolderWriters[i].writeField(
                                    ja,
                                    mailAccount.getId(),
                                    rootFolder,
                                    false,
                                    MailFolder.DEFAULT_FOLDER_NAME,
                                    1,
                                    MailFolderUtility.prepareFullname(mailAccount.getId(), MailFolder.DEFAULT_FOLDER_ID),
                                    FolderObject.SYSTEM_MODULE,
                                    false);
                            }
                        } else {
                            for (int i = 0; i < mailFolderWriters.length; i++) {
                                mailFolderWriters[i].writeField(
                                    ja,
                                    mailAccount.getId(),
                                    rootFolder,
                                    false,
                                    mailAccount.getName(),
                                    1,
                                    MailFolderUtility.prepareFullname(mailAccount.getId(), MailFolder.DEFAULT_FOLDER_ID),
                                    FolderObject.SYSTEM_MODULE,
                                    false);
                            }
                        }
                        jsonWriter.value(ja);
                    } catch (final MailException e) {
                        LOG.error(e.getMessage(), e);
                    } finally {
                        if (close) {
                            mailAccess.close(true);
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
            final AbstractOXException wrapper = getWrappingOXException(e);
            LOG.error(wrapper.getMessage(), wrapper);
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

    public void actionGetFolder(final ServerSession session, final JSONWriter w, final JSONObject requestObj) throws JSONException {
        ResponseWriter.write(actionGetFolder(session, ParamContainer.getInstance(requestObj, EnumComponent.FOLDER)), w);
    }

    private final void actionGetFolder(final HttpServletRequest req, final HttpServletResponse resp) throws IOException, ServletException {
        try {
            ResponseWriter.write(
                actionGetFolder(getSessionObject(req), ParamContainer.getInstance(req, EnumComponent.FOLDER, resp)),
                resp.getWriter());
        } catch (final JSONException e) {
            sendErrorAsJS(resp, RESPONSE_ERROR);
        }
    }

    private final Response actionGetFolder(final ServerSession session, final ParamContainer paramContainer) throws JSONException {
        /*
         * Some variables
         */
        final Response response = new Response();
        OXJSONWriter jsonWriter = null;
        Date lastModifiedDate = null;
        /*
         * Start response
         */
        try {
            final Context ctx = session.getContext();
            final String folderIdentifier = paramContainer.checkStringParam(PARAMETER_ID);
            final int[] columns = paramContainer.checkIntArrayParam(PARAMETER_COLUMNS);
            int folderId = -1;
            if ((folderId = getUnsignedInteger(folderIdentifier)) >= 0) {
                final FolderSQLInterface foldersqlinterface = new RdbFolderSQLInterface(session);
                final FolderObject fo = foldersqlinterface.getFolderById(folderId);
                lastModifiedDate = fo.getLastModified();
                jsonWriter = new OXJSONWriter();
                new FolderWriter(jsonWriter, session, ctx).writeOXFolderFieldsAsObject(columns, fo, session.getUser().getLocale());
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
                new FolderWriter(jsonWriter, session, ctx).writeOXFolderFieldsAsObject(columns, fo, user.getLocale());
            } else {
                MailServletInterface mailInterface = null;
                try {
                    mailInterface = MailServletInterface.getInstance(session);
                    final MailFolder f = mailInterface.getFolder(folderIdentifier, true);
                    final MailFolderFieldWriter[] writers = com.openexchange.mail.json.writer.FolderWriter.getMailFolderFieldWriter(
                        columns,
                        mailInterface.getMailConfig());
                    final JSONObject jo = new JSONObject();
                    for (final MailFolderFieldWriter writer : writers) {
                        writer.writeField(jo, mailInterface.getAccountID(), f, true);
                    }
                    jsonWriter = new OXJSONWriter(jo);
                } finally {
                    try {
                        if (mailInterface != null) {
                            mailInterface.close(true);
                        }
                    } catch (final MailException e) {
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
            final AbstractOXException wrapper = getWrappingOXException(e);
            LOG.error(wrapper.getMessage(), wrapper);
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
            requestObj,
            EnumComponent.FOLDER)), w);
    }

    private final void actionPutUpdateFolder(final HttpServletRequest req, final HttpServletResponse resp) throws IOException, ServletException {
        try {
            ResponseWriter.write(actionPutUpdateFolder(getSessionObject(req), getBody(req), ParamContainer.getInstance(
                req,
                EnumComponent.FOLDER,
                resp)), resp.getWriter());
        } catch (final JSONException e) {
            sendErrorAsJS(resp, RESPONSE_ERROR);
        }
    }

    private final Response actionPutUpdateFolder(final ServerSession session, final String body, final ParamContainer paramContainer) {
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
            final Context ctx = session.getContext();
            final String folderIdentifier = paramContainer.checkStringParam(PARAMETER_ID);
            Date timestamp = null;
            final JSONObject jsonObj = new JSONObject(body);
            int updateFolderId = -1;
            if ((updateFolderId = getUnsignedInteger(folderIdentifier)) >= 0) {
                timestamp = paramContainer.checkDateParam(PARAMETER_TIMESTAMP);
                final FolderSQLInterface foldersqlinterface = new RdbFolderSQLInterface(session);
                FolderObject fo = new FolderObject(updateFolderId);
                new FolderParser(session.getUserConfiguration()).parse(fo, jsonObj);
                fo = foldersqlinterface.saveFolderObject(fo, timestamp);
                retval = String.valueOf(fo.getObjectID());
                lastModifiedDate = fo.getLastModified();
            } else if (folderIdentifier.startsWith(FolderObject.SHARED_PREFIX)) {
                throw new OXFolderException(
                    OXFolderException.FolderCode.NO_ADMIN_ACCESS,
                    getUserName(session),
                    folderIdentifier,
                    Integer.valueOf(ctx.getContextId()));
            } else {
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
                    } catch (final MailException e) {
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
            final AbstractOXException wrapper = getWrappingOXException(e);
            LOG.error(wrapper.getMessage(), wrapper);
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
        ResponseWriter.write(actionPutInsertFolder(session, requestObj.getString(ResponseFields.DATA), ParamContainer.getInstance(
            requestObj,
            EnumComponent.FOLDER)), w);
    }

    private final void actionPutInsertFolder(final HttpServletRequest req, final HttpServletResponse resp) throws IOException, ServletException {
        try {
            ResponseWriter.write(actionPutInsertFolder(getSessionObject(req), getBody(req), ParamContainer.getInstance(
                req,
                EnumComponent.FOLDER,
                resp)), resp.getWriter());
        } catch (final JSONException e) {
            sendErrorAsJS(resp, RESPONSE_ERROR);
        }
    }

    private final Response actionPutInsertFolder(final ServerSession session, final String body, final ParamContainer paramContainer) {
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
            final Context ctx = session.getContext();
            final String parentFolder = paramContainer.checkStringParam(FolderFields.FOLDER_ID);
            final JSONObject jsonObj = new JSONObject(body);
            int parentFolderId = -1;
            if ((parentFolderId = getUnsignedInteger(parentFolder)) >= 0) {
                final FolderSQLInterface foldersqlinterface = new RdbFolderSQLInterface(session);
                FolderObject fo = new FolderObject();
                fo.setParentFolderID(parentFolderId);
                new FolderParser(session.getUserConfiguration()).parse(fo, jsonObj);
                fo = foldersqlinterface.saveFolderObject(fo, null);
                retval = String.valueOf(fo.getObjectID());
                lastModifiedDate = fo.getLastModified();
            } else if (parentFolder.startsWith(FolderObject.SHARED_PREFIX)) {
                throw new OXFolderException(
                    OXFolderException.FolderCode.NO_CREATE_SUBFOLDER_PERMISSION,
                    getUserName(session),
                    parentFolder,
                    Integer.valueOf(ctx.getContextId()));
            } else {
                final MailServletInterface mailInterface = MailServletInterface.getInstance(session);
                try {
                    final FullnameArgument arg = MailFolderUtility.prepareMailFolderParam(parentFolder);
                    final MailFolder parent = mailInterface.getFolder(parentFolder, true);
                    final MailFolderDescription mfd = new MailFolderDescription();
                    mfd.setParentFullname(arg.getFullname());
                    mfd.setParentAccountId(arg.getAccountId());
                    mfd.setSeparator(parent.getSeparator());
                    com.openexchange.mail.json.parser.FolderParser.parse(jsonObj, mfd, session, mailInterface.getAccountID());
                    mfd.setExists(false);
                    retval = mailInterface.saveFolder(mfd);
                } finally {
                    try {
                        mailInterface.close(true);
                    } catch (final MailException e) {
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
            final AbstractOXException wrapper = getWrappingOXException(e);
            LOG.error(wrapper.getMessage(), wrapper);
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
            requestObj,
            EnumComponent.FOLDER)), w);
    }

    private final void actionPutDeleteFolder(final HttpServletRequest req, final HttpServletResponse resp) throws IOException, ServletException {
        try {
            ResponseWriter.write(actionPutDeleteFolder(getSessionObject(req), getBody(req), ParamContainer.getInstance(
                req,
                EnumComponent.FOLDER,
                resp)), resp.getWriter());
        } catch (final JSONException e) {
            sendErrorAsJS(resp, RESPONSE_ERROR);
        }
    }

    private final Response actionPutDeleteFolder(final ServerSession session, final String body, final ParamContainer paramContainer) throws JSONException {
        /*
         * Some variables
         */
        final Response response = new Response();
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
                        throw new OXFolderException(
                            OXFolderException.FolderCode.NO_ADMIN_ACCESS,
                            getUserName(session),
                            deleteIdentifier,
                            Integer.valueOf(ctx.getContextId()));
                    } else {
                        if (session.getUserConfiguration().hasWebMail()) {
                            if (mailInterface == null) {
                                mailInterface = MailServletInterface.getInstance(session);
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
            response.setException(e);
        } catch (final AbstractOXException e) {
            LOG.error(e.getMessage(), e);
            response.setException(e);
        } catch (final Exception e) {
            final AbstractOXException wrapper = getWrappingOXException(e);
            LOG.error(wrapper.getMessage(), wrapper);
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
            throw new OXFolderException(FolderCode.MISSING_PARAMETER, paramName);
        }
        return paramVal;
    }

    private static final Pattern PATERN_SPLIT = Pattern.compile(" *, *");

    private static final int[] checkIntArrayParam(final HttpServletRequest req, final String paramName) throws OXException {
        String tmp = req.getParameter(paramName);
        if (tmp == null) {
            throw new OXFolderException(FolderCode.MISSING_PARAMETER, paramName);
        }
        final String[] sa = PATERN_SPLIT.split(tmp, 0);
        tmp = null;
        final int intArray[] = new int[sa.length];
        for (int a = 0; a < sa.length; a++) {
            try {
                intArray[a] = Integer.parseInt(sa[a]);
            } catch (final NumberFormatException e) {
                throw new OXFolderException(FolderCode.BAD_PARAM_VALUE, e, sa[a], paramName);
            }
        }
        return intArray;
    }

    /**
     * Parses specified string into an unsigned <code>int</code> value.
     * 
     * @param str The string to parse
     * @return The parsed unsigned <code>int</code> value or a value less than zero if string does not denote an unsigned integer.
     */
    private static final int getUnsignedInteger(final String str) {
        try {
            return Integer.parseInt(str);
        } catch (final NumberFormatException e) {
            return -1;
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
            collator = Collator.getInstance(locale);
            collator.setStrength(Collator.SECONDARY);
        }

        public int compare(final String displayName1, final String displayName2) {
            return collator.compare(displayName1, displayName2);
        }

    }

    private static final class MailAccountComparator implements Comparator<MailAccount> {

        private final Collator collator;

        public MailAccountComparator(final Locale locale) {
            super();
            collator = Collator.getInstance(locale);
            collator.setStrength(Collator.SECONDARY);
        }

        public int compare(final MailAccount o1, final MailAccount o2) {
            if (UnifiedINBOXManagement.PROTOCOL_UNIFIED_INBOX.equals(o1.getMailProtocol())) {
                if (UnifiedINBOXManagement.PROTOCOL_UNIFIED_INBOX.equals(o2.getMailProtocol())) {
                    return 0;
                }
                return -1;
            } else if (UnifiedINBOXManagement.PROTOCOL_UNIFIED_INBOX.equals(o2.getMailProtocol())) {
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

}
