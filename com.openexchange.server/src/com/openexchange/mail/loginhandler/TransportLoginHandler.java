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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.mail.loginhandler;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import com.openexchange.authentication.LoginExceptionCodes;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;
import com.openexchange.file.storage.composition.IDBasedFileAccess;
import com.openexchange.file.storage.composition.IDBasedFileAccessFactory;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.i18n.FolderStrings;
import com.openexchange.groupware.userconfiguration.UserPermissionBits;
import com.openexchange.login.LoginHandlerService;
import com.openexchange.login.LoginResult;
import com.openexchange.mail.MailSessionParameterNames;
import com.openexchange.mail.transport.config.TransportProperties;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.oxfolder.OXFolderAccess;
import com.openexchange.tools.oxfolder.OXFolderManager;
import com.openexchange.tools.oxfolder.OXFolderSQL;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;

/**
 * {@link TransportLoginHandler} - The login handler for mail transport.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class TransportLoginHandler implements LoginHandlerService {

    private static final org.apache.commons.logging.Log LOG =
        com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(TransportLoginHandler.class));

    /**
     * Initializes a new {@link TransportLoginHandler}.
     */
    public TransportLoginHandler() {
        super();
    }

    private static final List<Field> FIELDS = Collections.unmodifiableList(new ArrayList<Field>(Arrays.asList(
        Field.ID,
        Field.CREATED,
        Field.CREATED_BY)));

    @Override
    public void handleLogin(final LoginResult login) throws OXException {
        try {
            /*
             * Ensure publishing infostore folder exists
             */
            final Context ctx = login.getContext();
            final ServerSession serverSession = getServerSessionFrom(login.getSession(), ctx);
            final UserPermissionBits userConfiguration = serverSession.getUserPermissionBits();
            if (TransportProperties.getInstance().isPublishOnExceededQuota() && userConfiguration.hasInfostore() && new OXFolderAccess(ctx).getFolderObject(
                FolderObject.SYSTEM_PUBLIC_INFOSTORE_FOLDER_ID).getEffectiveUserPermission(serverSession.getUserId(), userConfiguration).canCreateSubfolders()) {
                String name = TransportProperties.getInstance().getPublishingInfostoreFolder();
                if ("i18n-defined".equals(name)) {
                    name = FolderStrings.DEFAULT_EMAIL_ATTACHMENTS_FOLDER_NAME;
                }
                final int folderId;
                final int lookUpFolder =
                    OXFolderSQL.lookUpFolder(FolderObject.SYSTEM_PUBLIC_INFOSTORE_FOLDER_ID, name, FolderObject.INFOSTORE, null, ctx);
                if (-1 == lookUpFolder) {
                    synchronized (TransportLoginHandler.class) {
                        folderId = createIfAbsent(serverSession, ctx, name);
                    }
                } else {
                    folderId = lookUpFolder;
                }
                serverSession.setParameter(MailSessionParameterNames.getParamPublishingInfostoreFolderID(), Integer.valueOf(folderId));
                /*
                 * Check for elapsed documents inside infostore folder
                 */
                if (!TransportProperties.getInstance().publishedDocumentsExpire()) {
                    return;
                }
                final IDBasedFileAccess fileAccess =
                    ServerServiceRegistry.getInstance().getService(IDBasedFileAccessFactory.class).createAccess(serverSession);
                final long now = System.currentTimeMillis();
                final List<String> toRemove = getElapsedDocuments(folderId, fileAccess, serverSession, now);
                if (!toRemove.isEmpty()) {
                    /*
                     * Remove elapsed documents
                     */
                    fileAccess.startTransaction();
                    try {
                        fileAccess.removeDocument(toRemove, now);
                        fileAccess.commit();
                    } finally {
                        fileAccess.finish();
                    }
                }
            }
        } catch (final SQLException e) {
            throw LoginExceptionCodes.UNKNOWN.create(e, e.getMessage());
        }
    }

    private List<String> getElapsedDocuments(final int folderId, final IDBasedFileAccess fileAccess, final ServerSession serverSession, final long now) throws OXException {
        final SearchIterator<File> searchIterator = fileAccess.getDocuments(String.valueOf(folderId), FIELDS).results();
        try {
            final long timeToLive = TransportProperties.getInstance().getPublishedDocumentTimeToLive();
            final List<String> ret;
            final int userId = serverSession.getUserId();
            if (searchIterator.size() != -1) {
                final int size = searchIterator.size();
                ret = new ArrayList<String>(size);
                for (int i = 0; i < size; i++) {
                    final File file = searchIterator.next();
                    if (isOwner(userId, file.getCreatedBy()) && isElapsed(now, file.getCreated().getTime(), timeToLive)) {
                        ret.add(file.getId());
                    }
                }
            } else {
                ret = new ArrayList<String>();
                while (searchIterator.hasNext()) {
                    final File file = searchIterator.next();
                    if (isOwner(userId, file.getCreatedBy()) && isElapsed(now, file.getCreated().getTime(), timeToLive)) {
                        ret.add(file.getId());
                    }
                }
            }
            return ret;
        } finally {
            try {
                searchIterator.close();
            } catch (final OXException e) {
                LOG.error(e.getMessage(), e);
            }
        }
    }

    private static boolean isOwner(final int sessionUser, final int createdBy) {
        return (sessionUser == createdBy);
    }

    private static boolean isElapsed(final long now, final long creationDate, final long ttl) {
        return ((now - creationDate) > ttl);
    }

    private int createIfAbsent(final Session session, final Context ctx, final String name) throws SQLException, OXException {
        final int lookUpFolder =
            OXFolderSQL.lookUpFolder(FolderObject.SYSTEM_PUBLIC_INFOSTORE_FOLDER_ID, name, FolderObject.INFOSTORE, null, ctx);
        if (-1 == lookUpFolder) {
            /*
             * Create folder
             */
            final FolderObject fo = createNewInfostoreFolder(ctx.getMailadmin(), name, FolderObject.SYSTEM_PUBLIC_INFOSTORE_FOLDER_ID);
            return OXFolderManager.getInstance(session).createFolder(fo, true, System.currentTimeMillis()).getObjectID();
        }
        return lookUpFolder;
    }

    private FolderObject createNewInfostoreFolder(final int adminId, final String name, final int parent) {
        final FolderObject newFolder = new FolderObject();
        newFolder.setFolderName(name);
        newFolder.setParentFolderID(parent);
        newFolder.setType(FolderObject.PUBLIC);
        newFolder.setModule(FolderObject.INFOSTORE);

        final List<OCLPermission> perms = new ArrayList<OCLPermission>(2);
        // Admin permission
        OCLPermission perm = new OCLPermission();
        perm.setEntity(adminId);
        perm.setFolderAdmin(true);
        perm.setFolderPermission(OCLPermission.ADMIN_PERMISSION);
        perm.setReadObjectPermission(OCLPermission.ADMIN_PERMISSION);
        perm.setWriteObjectPermission(OCLPermission.ADMIN_PERMISSION);
        perm.setDeleteObjectPermission(OCLPermission.ADMIN_PERMISSION);
        perm.setGroupPermission(false);
        perms.add(perm);
        // All groups and users permission
        perm = new OCLPermission();
        perm.setEntity(OCLPermission.ALL_GROUPS_AND_USERS);
        perm.setFolderAdmin(false);
        perm.setFolderPermission(OCLPermission.CREATE_OBJECTS_IN_FOLDER);
        perm.setReadObjectPermission(OCLPermission.READ_OWN_OBJECTS);
        perm.setWriteObjectPermission(OCLPermission.WRITE_OWN_OBJECTS);
        perm.setDeleteObjectPermission(OCLPermission.DELETE_OWN_OBJECTS);
        perm.setGroupPermission(true);
        perms.add(perm);
        newFolder.setPermissions(perms);

        return newFolder;
    }

    @Override
    public void handleLogout(final LoginResult logout) throws OXException {
        // Nothing to do
    }

    private static ServerSession getServerSessionFrom(final Session session, final Context context) {
        if (session instanceof ServerSession) {
            return (ServerSession) session;
        }
        return ServerSessionAdapter.valueOf(session, context);
    }
}
