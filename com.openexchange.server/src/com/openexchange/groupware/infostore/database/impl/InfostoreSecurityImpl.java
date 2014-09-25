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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.openexchange.database.Databases;
import com.openexchange.database.tx.DBService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.container.EffectiveObjectPermission;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.EffectiveInfostorePermission;
import com.openexchange.groupware.infostore.InfostoreExceptionCodes;
import com.openexchange.groupware.infostore.utils.Metadata;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.userconfiguration.UserPermissionBits;
import com.openexchange.server.impl.EffectivePermission;
import com.openexchange.tools.collections.Injector;
import com.openexchange.tools.collections.OXCollections;
import com.openexchange.tools.iterator.SearchIteratorException;
import com.openexchange.tools.oxfolder.OXFolderAccess;

public class InfostoreSecurityImpl extends DBService implements InfostoreSecurity {

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
        Connection con = null;
        try {
            con = getReadConnection(ctx);
            final EffectivePermission isperm = new OXFolderAccess(con, ctx).getFolderPermission((int)document.getFolderId(), user.getId(), userPermissions);
            final EffectiveObjectPermission objectPermission = getObjectPermission(ctx, user, document.getFolderId(), document.getId(), con);
            //final EffectivePermission isperm = OXFolderTools.getEffectiveFolderOCL((int)documentData.get(0).getFolderId(), user.getId(), user.getGroups(), ctx, userConfig, con);
            return new EffectiveInfostorePermission(isperm, objectPermission, document,user);
        } finally {
            releaseReadConnection(ctx, con);
        }
    }

    @Override
    public EffectivePermission getFolderPermission(final long folderId, final Context ctx, final User user, final UserPermissionBits userPermissions) throws OXException {
    	return getFolderPermission(folderId, ctx, user, userPermissions, null);
    }

    @Override
    public EffectivePermission getFolderPermission(final long folderId, final Context ctx, final User user, final UserPermissionBits userPermissions, Connection readConArg) throws OXException {
        Connection readCon = null;
        try {
            readCon = readConArg != null ? readConArg : getReadConnection(ctx);
            return new OXFolderAccess(readCon, ctx).getFolderPermission((int) folderId, user.getId(), userPermissions);
            //return OXFolderTools.getEffectiveFolderOCL((int)folderId, user.getId(), user.getGroups(),ctx, userConfig, readCon);
        } finally {
        	if (readConArg == null) {
                releaseReadConnection(ctx, readCon);
        	}
        }
    }

    @Override
    public <L> L injectInfostorePermissions(final int[] ids, final Context ctx, final User user, final UserPermissionBits userPermissions, final L list, final Injector<L, EffectiveInfostorePermission> injector) throws OXException {
        final Map<Integer, EffectivePermission> fpCache = new HashMap<Integer, EffectivePermission>();
        final Map<Integer, EffectiveObjectPermission> opCache = new HashMap<Integer, EffectiveObjectPermission>();
        final List<EffectiveInfostorePermission> permissions = new ArrayList<EffectiveInfostorePermission>();
        Connection con = null;
        final List<DocumentMetadata> metadata = getFolderIdAndCreatorForDocuments(ids, ctx);
        try {
            con = getReadConnection(ctx);
            final OXFolderAccess access = new OXFolderAccess(con, ctx);
            for(final DocumentMetadata m : metadata) {
                final EffectivePermission isperm;
                if(fpCache.containsKey(Integer.valueOf((int) m.getFolderId()))) {
                    isperm = fpCache.get(Integer.valueOf((int) m.getFolderId()));
                } else {
                    isperm = access.getFolderPermission((int) m.getFolderId(), user.getId(), userPermissions);
                    fpCache.put(Integer.valueOf((int) m.getFolderId()), isperm);
                }

                final EffectiveObjectPermission objectPermission;
                if (opCache.containsKey(m.getId())) {
                    objectPermission = opCache.get(m.getId());
                } else {
                    objectPermission = getObjectPermission(ctx, user, m.getFolderId(), m.getId(), con);
                    opCache.put(m.getId(), objectPermission);
                }

                permissions.add(new EffectiveInfostorePermission(isperm, objectPermission, m,user));
            }

        } finally {
            releaseReadConnection(ctx, con);
        }

        return OXCollections.inject(list, permissions, injector);

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

    @Override
    public EffectiveObjectPermission getObjectPermission(Context ctx, User user, long folderId, int id) throws OXException {
        Connection readCon = null;
        try {
            readCon = getReadConnection(ctx);
            return getObjectPermission(ctx, user, folderId, id);
        } finally {
            if (readCon != null) {
                releaseReadConnection(ctx, readCon);
            }
        }
    }

    @Override
    public EffectiveObjectPermission getObjectPermission(Context ctx, User user, long folderId, int id, Connection con) throws OXException {
        int[] groups = user.getGroups();
        boolean hasGroups = groups != null && groups.length > 0;
        StringBuilder sb = new StringBuilder(128).append("SELECT bits FROM object_permission WHERE cid = ").append(ctx.getContextId()).append(" AND module = 8");
        sb.append(" AND folder_id = ").append(folderId).append(" AND object_id = ").append(id);
        if (hasGroups) {
            sb.append(" AND ((group_flag <> 1 AND permission_id = ").append(user.getId()).append(") OR (group_flag = 1 AND permission_id IN (");
            boolean first = true;
            for (int group : groups) {
                if (first) {
                    sb.append(group);
                    first = false;
                } else {
                    sb.append(", ").append(group);
                }
            }
            sb.append(")))");
        } else {
            sb.append(" AND (group_flag <> 1 AND permission_id = ").append(user.getId()).append(")");
        }

        int bits = 0;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.createStatement();
            rs = stmt.executeQuery(sb.toString());
            if (rs.next()) {
                bits = rs.getInt(1);
            }
        } catch (SQLException e) {
            throw InfostoreExceptionCodes.SQL_PROBLEM.create(e.getMessage());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }

        return new EffectiveObjectPermission((int) folderId, id, bits);
    }


    private List<DocumentMetadata> getFolderIdAndCreatorForDocuments(final int[] is, final Context ctx) throws OXException {
        final InfostoreIterator iter = InfostoreIterator.list(is, new Metadata[]{Metadata.FOLDER_ID_LITERAL, Metadata.ID_LITERAL, Metadata.CREATED_BY_LITERAL}, getProvider(), ctx);
        try {
            return iter.asList();
        } catch (final SearchIteratorException e) {
            throw InfostoreExceptionCodes.COULD_NOT_LOAD.create(e);
        }
    }
}
