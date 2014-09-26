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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

import static com.openexchange.java.Autoboxing.L;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.database.tx.DBService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.EffectiveObjectPermission;
import com.openexchange.groupware.container.EffectiveObjectPermissions;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.ObjectPermission;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.EffectiveInfostorePermission;
import com.openexchange.groupware.infostore.InfostoreExceptionCodes;
import com.openexchange.groupware.infostore.utils.Metadata;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.userconfiguration.UserPermissionBits;
import com.openexchange.java.util.Pair;
import com.openexchange.server.impl.EffectivePermission;
import com.openexchange.tools.collections.Injector;
import com.openexchange.tools.collections.OXCollections;
import com.openexchange.tools.iterator.SearchIteratorException;
import com.openexchange.tools.oxfolder.OXFolderAccess;

public class InfostoreSecurityImpl extends DBService implements InfostoreSecurity {

    private static final Logger LOG = LoggerFactory.getLogger(InfostoreSecurityImpl.class);

    @Override
    public EffectiveInfostorePermission getInfostorePermission(final int id, final Context ctx, final User user, final UserPermissionBits userPermissions) throws OXException {
        final List<DocumentMetadata> documentData = getFolderIdAndCreatorForDocuments(new int[]{id}, ctx);
        if (documentData == null || documentData.size() <= 0 || documentData.get(0) == null) {
            throw InfostoreExceptionCodes.NOT_EXIST.create();
        }
        return getInfostorePermission(documentData.get(0), ctx, user, userPermissions);
    }

    @Override
    public EffectiveInfostorePermission getInfostorePermission(final DocumentMetadata document, final Context ctx, final User user, final UserPermissionBits userPermissions) throws OXException {
        long start = System.currentTimeMillis();
        Connection con = null;
        try {
            con = getReadConnection(ctx);
            final EffectivePermission isperm = new OXFolderAccess(con, ctx).getFolderPermission((int)document.getFolderId(), user.getId(), userPermissions);
            final EffectiveObjectPermission effectiveObjectPermission = getEffectiveObjectPermission(ctx, user, userPermissions, document, con);
            //final EffectivePermission isperm = OXFolderTools.getEffectiveFolderOCL((int)documentData.get(0).getFolderId(), user.getId(), user.getGroups(), ctx, userConfig, con);
            return new EffectiveInfostorePermission(isperm, effectiveObjectPermission, document,user);
        } finally {
            releaseReadConnection(ctx, con);
            LOG.debug("Loaded effective permission for one document in {}ms.", System.currentTimeMillis() - start);
        }
    }

    @Override
    public List<EffectiveInfostorePermission> getInfostorePermissions(List<DocumentMetadata> documents, Context ctx, User user, UserPermissionBits userPermissions) throws OXException {
        long start = System.currentTimeMillis();
        try {
            return getInfostorePermissions0(documents, ctx, user, userPermissions);
        } finally {
            LOG.debug("Loaded effective permissions for {} documents in {}ms.", documents.size(), System.currentTimeMillis() - start);
        }
    }

    @Override
    public EffectivePermission getFolderPermission(final long folderId, final Context ctx, final User user, final UserPermissionBits userPermissions) throws OXException {
    	return getFolderPermission(folderId, ctx, user, userPermissions, null);
    }

    @Override
    public EffectivePermission getFolderPermission(final long folderId, final Context ctx, final User user, final UserPermissionBits userPermissions, Connection readConArg) throws OXException {
        long start = System.currentTimeMillis();
        Connection readCon = null;
        try {
            readCon = readConArg != null ? readConArg : getReadConnection(ctx);
            return new OXFolderAccess(readCon, ctx).getFolderPermission((int) folderId, user.getId(), userPermissions);
            //return OXFolderTools.getEffectiveFolderOCL((int)folderId, user.getId(), user.getGroups(),ctx, userConfig, readCon);
        } finally {
        	if (readConArg == null) {
                releaseReadConnection(ctx, readCon);
        	}
        	LOG.debug("Loaded folder permission for one document in {}ms.", System.currentTimeMillis() - start);
        }
    }

    @Override
    public <L> L injectInfostorePermissions(final int[] ids, final Context ctx, final User user, final UserPermissionBits userPermissions, final L list, final Injector<L, EffectiveInfostorePermission> injector) throws OXException {
        long start = System.currentTimeMillis();
        try {
            final List<DocumentMetadata> metadata = getFolderIdAndCreatorForDocuments(ids, ctx);
            final List<EffectiveInfostorePermission> infostorePermissions = getInfostorePermissions0(metadata, ctx, user, userPermissions);
            return OXCollections.inject(list, infostorePermissions, injector);
        } finally {
            LOG.debug("Loaded effective permissions for {} documents in {}ms.", ids.length, System.currentTimeMillis() - start);
        }
    }

    @Override
    public void checkFolderId(final long folderId, final Context ctx) throws OXException {
        final FolderObject fo;
        Connection readCon = null;
        try {
            readCon = getReadConnection(ctx);
            final OXFolderAccess access = new OXFolderAccess(readCon, ctx);
            fo = access.getFolderObject((int)folderId);
        } finally {
            releaseReadConnection(ctx, readCon);
        }
        if(fo.getModule() != FolderObject.INFOSTORE) {
            throw InfostoreExceptionCodes.NOT_INFOSTORE_FOLDER.create(L(folderId));
        }
    }

    private List<DocumentMetadata> getFolderIdAndCreatorForDocuments(final int[] is, final Context ctx) throws OXException {
        final InfostoreIterator iter = InfostoreIterator.list(is, new Metadata[]{Metadata.FOLDER_ID_LITERAL, Metadata.ID_LITERAL, Metadata.CREATED_BY_LITERAL}, getProvider(), ctx);
        try {
            return iter.asList();
        } catch (final SearchIteratorException e) {
            throw InfostoreExceptionCodes.COULD_NOT_LOAD.create(e);
        }
    }

    private List<EffectiveInfostorePermission> getInfostorePermissions0(List<DocumentMetadata> documents, Context ctx, User user, UserPermissionBits userPermissions) throws OXException {
        Connection con = null;
        try {
            con = getReadConnection(ctx);
            OXFolderAccess oxFolderAccess = new OXFolderAccess(con, ctx);
            Map<Long, EffectivePermission> folderPermissions = new HashMap<Long, EffectivePermission>(documents.size() * 2);
            List<Pair<Integer, Integer>> foldersAndDocuments = new ArrayList<Pair<Integer, Integer>>(documents.size());
            Map<Integer, Map<Integer, EffectiveObjectPermission>> objectPermissionsByFolder = EffectiveObjectPermissions.load(ctx, user, userPermissions, FolderObject.INFOSTORE, foldersAndDocuments, con);
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
                    Map<Integer, EffectiveObjectPermission> permissionsByDocument = objectPermissionsByFolder.get((int)folderId);
                    if (permissionsByDocument == null) {
                        permissionsByDocument = new HashMap<Integer, EffectiveObjectPermission>();
                        objectPermissionsByFolder.put((int)folderId, permissionsByDocument);
                    }

                    permissionsByDocument.put(document.getId(), effectiveObjectPermission);
                }

                EffectivePermission folderPermission = folderPermissions.get(folderId);
                if (folderPermission == null) {
                    folderPermission = oxFolderAccess.getFolderPermission((int)folderId, user.getId(), userPermissions);
                    folderPermissions.put(folderId, folderPermission);
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
                EffectiveObjectPermission objectPermission = null;
                Map<Integer, EffectiveObjectPermission> objectPermissionsByDocument = objectPermissionsByFolder.get((int)folderId);
                if (objectPermissionsByDocument != null) {
                    objectPermission = objectPermissionsByDocument.get(document.getId());
                }

                permissions.add(new EffectiveInfostorePermission(
                    folderPermission,
                    objectPermission,
                    document,
                    user));
            }

            return permissions;
        } finally {
            releaseReadConnection(ctx, con);
        }
    }

    private static EffectiveObjectPermission getEffectiveObjectPermission(Context ctx, User user, UserPermissionBits userPermissions, DocumentMetadata document, Connection con) throws OXException {
        ObjectPermission objectPermission = EffectiveObjectPermissions.find(user, document.getObjectPermissions());
        if (objectPermission != null) {
            return EffectiveObjectPermissions.convert(FolderObject.INFOSTORE, (int) document.getFolderId(), document.getId(), objectPermission, userPermissions);
        }

        return EffectiveObjectPermissions.load(ctx, user, userPermissions, FolderObject.INFOSTORE, (int) document.getFolderId(), document.getId(), con);
    }
}
