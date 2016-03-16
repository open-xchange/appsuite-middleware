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

package com.openexchange.groupware.infostore.database.impl;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.openexchange.database.tx.DBService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.EffectiveObjectPermission;
import com.openexchange.groupware.container.EffectiveObjectPermissions;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.ObjectPermission;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.EffectiveInfostoreFolderPermission;
import com.openexchange.groupware.infostore.EffectiveInfostorePermission;
import com.openexchange.groupware.infostore.InfostoreExceptionCodes;
import com.openexchange.groupware.infostore.utils.Metadata;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.userconfiguration.UserPermissionBits;
import com.openexchange.java.util.Pair;
import com.openexchange.server.impl.EffectivePermission;
import com.openexchange.tools.collections.Injector;
import com.openexchange.tools.collections.OXCollections;
import com.openexchange.tools.iterator.SearchIterators;
import com.openexchange.tools.oxfolder.OXFolderAccess;
import com.openexchange.tools.oxfolder.OXFolderExceptionCode;
import com.openexchange.tools.session.ServerSession;

public class InfostoreSecurityImpl extends DBService implements InfostoreSecurity {

    /**
     * Initializes a new {@link InfostoreSecurityImpl}.
     */
    public InfostoreSecurityImpl() {
        super();
    }

    /**
     * Determines the identifier of the folder owner
     *
     * @param folder The folder to examine
     * @return The folder owner identifier or <code>-1</code>
     */
    public static int getFolderOwner(FolderObject folder) {
        return null == folder ? -1 : folder.getCreatedBy();
    }

    @Override
    public int getFolderOwner(long folderId, Context ctx) throws OXException {
        Connection readCon = null;
        try {
            readCon = getReadConnection(ctx);
            OXFolderAccess folderAccess = new OXFolderAccess(readCon, ctx);
            return getFolderOwner(folderAccess.getFolderObject((int) folderId));
        } finally {
            releaseReadConnection(ctx, readCon);
        }
    }

    @Override
    public int[] getFolderOwners(Collection<DocumentMetadata> documents, Context ctx) throws OXException {
        Connection readCon = null;
        try {
            readCon = getReadConnection(ctx);
            OXFolderAccess folderAccess = new OXFolderAccess(readCon, ctx);

            TIntList admins = new TIntArrayList();
            for (DocumentMetadata document : documents) {
                FolderObject folder = folderAccess.getFolderObject((int) document.getFolderId());
                admins.add(getFolderOwner(folder));
            }
            return admins.toArray();
        } finally {
            releaseReadConnection(ctx, readCon);
        }
    }

    @Override
    public int getFolderOwner(DocumentMetadata document, Context ctx) throws OXException {
        Connection readCon = null;
        try {
            readCon = getReadConnection(ctx);
            OXFolderAccess folderAccess = new OXFolderAccess(readCon, ctx);
            FolderObject folder = folderAccess.getFolderObject((int) document.getFolderId());
            return getFolderOwner(folder);
        } finally {
            releaseReadConnection(ctx, readCon);
        }
    }

    @Override
    public EffectiveInfostorePermission getInfostorePermission(ServerSession session, int id) throws OXException {
        List<DocumentMetadata> documents = getFolderIdAndCreatorForDocuments(new int[] { id }, session.getContext());
        if (documents == null || documents.size() <= 0 || documents.get(0) == null) {
            throw InfostoreExceptionCodes.NOT_EXIST.create();
        }
        return getInfostorePermission(session, documents.get(0));
    }

    @Override
    public EffectiveInfostorePermission getInfostorePermission(Context context, User user, UserPermissionBits userPermissions, int id) throws OXException {
        List<DocumentMetadata> documents = getFolderIdAndCreatorForDocuments(new int[] { id }, context);
        if (documents == null || documents.size() <= 0 || documents.get(0) == null) {
            throw InfostoreExceptionCodes.NOT_EXIST.create();
        }

        DocumentMetadata document = documents.iterator().next();
        Connection connection = null;
        try {
            connection = getReadConnection(context);
            OXFolderAccess folderAccess = new OXFolderAccess(connection, context);
            FolderObject folder = folderAccess.getFolderObject((int) document.getFolderId());
            EffectivePermission isperm = folder.getEffectiveUserPermission(user.getId(), userPermissions);
            EffectiveObjectPermission effectiveObjectPermission = getEffectiveObjectPermission(context, user, userPermissions, document, connection);
            return new EffectiveInfostorePermission(isperm, effectiveObjectPermission, document, user, getFolderOwner(folder));
        } finally {
            releaseReadConnection(context, connection);
        }
    }

    @Override
    public EffectiveInfostorePermission getInfostorePermission(ServerSession session, DocumentMetadata document) throws OXException {
        Connection connection = null;
        try {
            connection = getReadConnection(session.getContext());
            OXFolderAccess folderAccess = new OXFolderAccess(connection, session.getContext());
            FolderObject folder = folderAccess.getFolderObject((int) document.getFolderId());
            EffectivePermission isperm = folder.getEffectiveUserPermission(session.getUserId(), session.getUserPermissionBits());
            EffectiveObjectPermission effectiveObjectPermission = getEffectiveObjectPermission(session, document, connection);
            return new EffectiveInfostorePermission(isperm, effectiveObjectPermission, document, session.getUser(), getFolderOwner(folder));
        } finally {
            releaseReadConnection(session.getContext(), connection);
        }
    }

    @Override
    public List<EffectiveInfostorePermission> getInfostorePermissions(List<DocumentMetadata> documents, Context ctx, User user, UserPermissionBits userPermissions) throws OXException {
        return getInfostorePermissions0(documents, ctx, user, userPermissions);
    }

    @Override
    public EffectiveInfostoreFolderPermission getFolderPermission(ServerSession session, long folderId) throws OXException {
        return getFolderPermission(folderId, session.getContext(), session.getUser(), session.getUserPermissionBits());
    }

    @Override
    public EffectiveInfostoreFolderPermission getFolderPermission(final long folderId, final Context ctx, final User user, final UserPermissionBits userPermissions) throws OXException {
        return getFolderPermission(folderId, ctx, user, userPermissions, null);
    }

    @Override
    public EffectiveInfostoreFolderPermission getFolderPermission(final long folderId, final Context ctx, final User user, final UserPermissionBits userPermissions, Connection readConArg) throws OXException {
        Connection readCon = null;
        try {
            readCon = getReadConnection(ctx);
            OXFolderAccess folderAccess = new OXFolderAccess(readCon, ctx);
            FolderObject folder = folderAccess.getFolderObject((int) folderId);
            EffectivePermission isperm = folder.getEffectiveUserPermission(user.getId(), userPermissions);
            return new EffectiveInfostoreFolderPermission(isperm, getFolderOwner(folder));
        } finally {
            releaseReadConnection(ctx, readCon);
        }
    }

    @Override
    public <L> L injectInfostorePermissions(final int[] ids, final Context ctx, final User user, final UserPermissionBits userPermissions, final L list, final Injector<L, EffectiveInfostorePermission> injector) throws OXException {
        final List<DocumentMetadata> metadata = getFolderIdAndCreatorForDocuments(ids, ctx);
        final List<EffectiveInfostorePermission> infostorePermissions = getInfostorePermissions0(metadata, ctx, user, userPermissions);
        return OXCollections.inject(list, infostorePermissions, injector);
    }

    /**
     * Gets some basic permission-related properties for a list infostore documents.
     *
     * @param ids The identifiers of the documents to get the metadata for
     * @param context The context
     * @return The document metadata in a list, in the same order as the identifiers in the requested array
     */
    private List<DocumentMetadata> getFolderIdAndCreatorForDocuments(int[] ids, Context context) throws OXException {
        Metadata[] metadata = new Metadata[] { Metadata.FOLDER_ID_LITERAL, Metadata.ID_LITERAL, Metadata.CREATED_BY_LITERAL };
        InfostoreIterator searchIterator = null;
        try {
            searchIterator = InfostoreIterator.list(ids, metadata, this, context);
            return searchIterator.asList();
        } finally {
            SearchIterators.close(searchIterator);
        }
    }

    private List<EffectiveInfostorePermission> getInfostorePermissions0(List<DocumentMetadata> documents, Context ctx, User user, UserPermissionBits userPermissions) throws OXException {
        Connection con = getReadConnection(ctx);
        try {
            OXFolderAccess oxFolderAccess = new OXFolderAccess(con, ctx);
            Map<Long, EffectivePermission> folderPermissions = new HashMap<Long, EffectivePermission>(documents.size() * 2);
            Map<Long, FolderObject> folders = new HashMap<Long, FolderObject>(documents.size() * 2);

            List<Pair<Integer, Integer>> foldersAndDocuments = new ArrayList<Pair<Integer, Integer>>(documents.size());
            Map<Integer, Map<Integer, EffectiveObjectPermission>> objectPermissionsByFolder = new HashMap<Integer, Map<Integer, EffectiveObjectPermission>>();
            for (DocumentMetadata document : documents) {
                long folderId = document.getFolderId();
                EffectiveObjectPermission effectiveObjectPermission = null;
                ObjectPermission objectPermission = EffectiveObjectPermissions.find(user, document.getObjectPermissions());
                if (objectPermission != null) {
                    effectiveObjectPermission = EffectiveObjectPermissions.convert(FolderObject.INFOSTORE, (int) document.getFolderId(), document.getId(), objectPermission, userPermissions);
                }

                if (effectiveObjectPermission == null) {
                    foldersAndDocuments.add(new Pair<Integer, Integer>((int) folderId, document.getId()));
                } else {
                    Map<Integer, EffectiveObjectPermission> permissionsByDocument = objectPermissionsByFolder.get((int) folderId);
                    if (permissionsByDocument == null) {
                        permissionsByDocument = new HashMap<Integer, EffectiveObjectPermission>();
                        objectPermissionsByFolder.put((int) folderId, permissionsByDocument);
                    }

                    permissionsByDocument.put(document.getId(), effectiveObjectPermission);
                }

                EffectivePermission folderPermission = folderPermissions.get(folderId);
                if (folderPermission == null) {
                    FolderObject folder = oxFolderAccess.getFolderObject((int) folderId);
                    folderPermission = folder.getEffectiveUserPermission(user.getId(), userPermissions, con);
                    folderPermissions.put(folderId, folderPermission);
                    folders.put(folderId, folder);
                }
            }

            Map<Integer, Map<Integer, EffectiveObjectPermission>> objectPermissionsByFolderFromDB = EffectiveObjectPermissions.load(ctx, user, userPermissions, FolderObject.INFOSTORE, foldersAndDocuments, con);
            if (!objectPermissionsByFolderFromDB.isEmpty()) {
                if (objectPermissionsByFolder.isEmpty()) {
                    objectPermissionsByFolder = objectPermissionsByFolderFromDB;
                } else {
                    List<EffectiveObjectPermission> objectPermissions = EffectiveObjectPermissions.flatten(objectPermissionsByFolderFromDB);
                    for (EffectiveObjectPermission permission : objectPermissions) {
                        int folderId = permission.getFolderId();
                        Map<Integer, EffectiveObjectPermission> permissionsByDocument = objectPermissionsByFolder.get(folderId);
                        if (permissionsByDocument == null) {
                            permissionsByDocument = new HashMap<Integer, EffectiveObjectPermission>();
                            objectPermissionsByFolder.put(folderId, permissionsByDocument);
                        }

                        permissionsByDocument.put(permission.getObjectId(), permission);
                    }
                }
            }

            List<EffectiveInfostorePermission> permissions = new ArrayList<EffectiveInfostorePermission>(documents.size());
            for (DocumentMetadata document : documents) {
                long folderId = document.getFolderId();
                EffectivePermission folderPermission = folderPermissions.get(folderId);
                FolderObject folder = folders.get(folderId);
                EffectiveObjectPermission objectPermission = null;
                Map<Integer, EffectiveObjectPermission> objectPermissionsByDocument = objectPermissionsByFolder.get((int) folderId);
                if (objectPermissionsByDocument != null) {
                    objectPermission = objectPermissionsByDocument.get(document.getId());
                }

                permissions.add(new EffectiveInfostorePermission(folderPermission, objectPermission, document, user, getFolderOwner(folder)));
            }

            return permissions;
        } catch (SQLException e) {
            throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            releaseReadConnection(ctx, con);
        }
    }

    private static EffectiveObjectPermission getEffectiveObjectPermission(ServerSession session, DocumentMetadata document, Connection con) throws OXException {
        ObjectPermission objectPermission = EffectiveObjectPermissions.find(session.getUser(), document.getObjectPermissions());
        if (objectPermission != null) {
            return EffectiveObjectPermissions.convert(FolderObject.INFOSTORE, (int) document.getFolderId(), document.getId(), objectPermission, session.getUserPermissionBits());
        }
        return EffectiveObjectPermissions.load(session, FolderObject.INFOSTORE, (int) document.getFolderId(), document.getId(), con);
    }

    private static EffectiveObjectPermission getEffectiveObjectPermission(Context context, User user, UserPermissionBits permissionBits, DocumentMetadata document, Connection con) throws OXException {
        ObjectPermission objectPermission = EffectiveObjectPermissions.find(user, document.getObjectPermissions());
        if (objectPermission != null) {
            return EffectiveObjectPermissions.convert(FolderObject.INFOSTORE, (int) document.getFolderId(), document.getId(), objectPermission, permissionBits);
        }
        return EffectiveObjectPermissions.load(context, user, permissionBits, FolderObject.INFOSTORE, (int) document.getFolderId(), document.getId(), con);
    }

}
