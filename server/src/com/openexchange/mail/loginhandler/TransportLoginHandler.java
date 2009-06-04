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

package com.openexchange.mail.loginhandler;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import com.openexchange.ajax.Infostore;
import com.openexchange.api2.OXException;
import com.openexchange.authentication.LoginException;
import com.openexchange.database.DBPoolingException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.InfostoreFacade;
import com.openexchange.groupware.infostore.utils.Metadata;
import com.openexchange.groupware.tx.TransactionException;
import com.openexchange.login.Login;
import com.openexchange.login.LoginHandlerService;
import com.openexchange.mail.MailSessionParameterNames;
import com.openexchange.mail.transport.config.TransportProperties;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.session.Session;
import com.openexchange.tools.Collections.SmartIntArray;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorException;
import com.openexchange.tools.oxfolder.OXFolderException;
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

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(TransportLoginHandler.class);

    /**
     * Initializes a new {@link TransportLoginHandler}.
     */
    public TransportLoginHandler() {
        super();
    }

    private static final Metadata[] METADATA = new Metadata[] {
        Metadata.ID_LITERAL, Metadata.CREATION_DATE_LITERAL, Metadata.CREATED_BY_LITERAL };

    public void handleLogin(final Login login) throws LoginException {
        /*
         * Ensure publishing infostore folder exists
         */
        final Context ctx = login.getContext();
        final Session session = login.getSession();
        if (TransportProperties.getInstance().isPublishOnExceededQuota()) {
            final String name = TransportProperties.getInstance().getPublishingInfostoreFolder();
            final int folderId;
            try {
                final int lookUpFolder = OXFolderSQL.lookUpFolder(
                    FolderObject.SYSTEM_PUBLIC_INFOSTORE_FOLDER_ID,
                    name,
                    FolderObject.INFOSTORE,
                    null,
                    ctx);
                if (-1 == lookUpFolder) {
                    synchronized (TransportLoginHandler.class) {
                        folderId = createIfAbsent(session, ctx, name);
                    }
                } else {
                    folderId = lookUpFolder;
                }
                session.setParameter(MailSessionParameterNames.getParamPublishingInfostoreFolderID(), Integer.valueOf(folderId));
            } catch (final DBPoolingException e) {
                throw new LoginException(e);
            } catch (final SQLException e) {
                throw new LoginException(new OXFolderException(
                    OXFolderException.FolderCode.SQL_ERROR,
                    e,
                    Integer.valueOf(ctx.getContextId())));
            } catch (final OXFolderException e) {
                throw new LoginException(e);
            } catch (final OXException e) {
                throw new LoginException(e);
            }
            /*
             * Check for elapsed documents inside infostore folder
             */
            final InfostoreFacade infostoreFacade = Infostore.FACADE;
            final ServerSession serverSession;
            if (session instanceof ServerSession) {
                serverSession = (ServerSession) session;
            } else {
                serverSession = new ServerSessionAdapter(session, ctx);
            }
            final long now = System.currentTimeMillis();
            final int[] toRemove = getElapsedDocuments(folderId, infostoreFacade, serverSession, ctx, now);
            if (toRemove.length > 0) {
                /*
                 * Remove elapsed documents
                 */
                try {
                    infostoreFacade.startTransaction();
                    try {
                        infostoreFacade.removeDocument(toRemove, now, serverSession);
                        infostoreFacade.commit();
                    } catch (final OXException e) {
                        infostoreFacade.rollback();
                        throw new LoginException(e);
                    } finally {
                        infostoreFacade.finish();
                    }
                } catch (final TransactionException e) {
                    throw new LoginException(e);
                }
            }
        }
    }

    private int[] getElapsedDocuments(final int folderId, final InfostoreFacade infostoreFacade, final ServerSession serverSession, final Context ctx, final long now) throws LoginException {
        final SearchIterator<DocumentMetadata> searchIterator;
        try {
            searchIterator = infostoreFacade.getDocuments(
                folderId,
                METADATA,
                ctx,
                serverSession.getUser(),
                serverSession.getUserConfiguration()).results();
        } catch (final OXException e) {
            throw new LoginException(e);
        }
        try {
            final long timeToLive = TransportProperties.getInstance().getPublishedDocumentTimeToLive();
            final SmartIntArray sia = new SmartIntArray(128);
            final int userId = serverSession.getUserId();
            if (searchIterator.hasSize()) {
                final int size = searchIterator.size();
                for (int i = 0; i < size; i++) {
                    final DocumentMetadata documentMetadata = searchIterator.next();
                    if (isOwner(userId, documentMetadata.getCreatedBy()) && isElapsed(
                        now,
                        documentMetadata.getCreationDate().getTime(),
                        timeToLive)) {
                        sia.append(documentMetadata.getId());
                    }
                }
            } else {
                while (searchIterator.hasNext()) {
                    final DocumentMetadata documentMetadata = searchIterator.next();
                    if (isOwner(userId, documentMetadata.getCreatedBy()) && isElapsed(
                        now,
                        documentMetadata.getCreationDate().getTime(),
                        timeToLive)) {
                        sia.append(documentMetadata.getId());
                    }
                }
            }
            return sia.toArray();
        } catch (final SearchIteratorException e) {
            throw new LoginException(e);
        } catch (final OXException e) {
            throw new LoginException(e);
        } finally {
            try {
                searchIterator.close();
            } catch (final SearchIteratorException e) {
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

    private int createIfAbsent(final Session session, final Context ctx, final String name) throws DBPoolingException, SQLException, OXException, OXFolderException {
        final int lookUpFolder = OXFolderSQL.lookUpFolder(
            FolderObject.SYSTEM_PUBLIC_INFOSTORE_FOLDER_ID,
            name,
            FolderObject.INFOSTORE,
            null,
            ctx);
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

    public void handleLogout(final Login logout) throws LoginException {
        // Nothing to do
    }
}
