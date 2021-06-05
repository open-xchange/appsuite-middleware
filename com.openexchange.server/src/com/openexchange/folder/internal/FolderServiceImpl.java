/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.folder.internal;

import java.sql.Connection;
import java.sql.SQLException;
import com.openexchange.databaseold.Database;
import com.openexchange.exception.OXException;
import com.openexchange.folder.FolderService;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.userconfiguration.UserPermissionBits;
import com.openexchange.groupware.userconfiguration.UserPermissionBitsStorage;
import com.openexchange.server.impl.EffectivePermission;
import com.openexchange.tools.oxfolder.OXFolderAccess;
import com.openexchange.tools.oxfolder.OXFolderExceptionCode;
import com.openexchange.tools.oxfolder.OXFolderLoader;

/**
 * {@link FolderServiceImpl}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class FolderServiceImpl implements FolderService {

    private static final String DEL_OXFOLDER_TREE = "del_oxfolder_tree";
    private static final String DEL_OXFOLDER_PERMISSIONS = "del_oxfolder_permissions";

    /**
     * Initializes a new {@link FolderServiceImpl}.
     */
    public FolderServiceImpl() {
        super();
    }

    @Override
    public FolderObject getFolderObject(final int folderId, final int contextId) throws OXException {
        try {
            return new OXFolderAccess(ContextStorage.getStorageContext(contextId)).getFolderObject(folderId);
        } catch (OXException e) {
            if (!OXFolderExceptionCode.NOT_EXISTS.equals(e)) {
                throw e;
            }
            final Context ctx = ContextStorage.getStorageContext(contextId);
            final Connection con = Database.get(ctx, false);
            try {
                return OXFolderLoader.loadFolderObjectFromDB(folderId, ctx, con, true, false, DEL_OXFOLDER_TREE, DEL_OXFOLDER_PERMISSIONS);
            } finally {
                Database.back(ctx, false, con);
            }
        }
    }

    @Override
    public FolderObject getFolderObject(final int folderId, final int contextId, final boolean working) throws OXException {
        if (working) {
            return new OXFolderAccess(ContextStorage.getStorageContext(contextId)).getFolderObject(folderId);
        }
        final Context ctx = ContextStorage.getStorageContext(contextId);
        final Connection con = Database.get(ctx, false);
        try {
            return OXFolderLoader.loadFolderObjectFromDB(folderId, ctx, con, true, false, DEL_OXFOLDER_TREE, DEL_OXFOLDER_PERMISSIONS);
        } finally {
            Database.back(ctx, false, con);
        }
    }

    @Override
    public FolderObject getFolderObject(final int folderId, final int contextId, final Storage storage) throws OXException {
        if (Storage.WORKING.equals(storage)) {
            return new OXFolderAccess(ContextStorage.getStorageContext(contextId)).getFolderObject(folderId);
        }
        final Context ctx = ContextStorage.getStorageContext(contextId);
        if (Storage.BACKUP.equals(storage)) {
            final Connection con = Database.get(ctx, false);
            try {
                return OXFolderLoader.loadFolderObjectFromDB(folderId, ctx, con, true, false, DEL_OXFOLDER_TREE, DEL_OXFOLDER_PERMISSIONS);
            } finally {
                Database.back(ctx, false, con);
            }
        }
        // Connect to master
        final Connection con = Database.get(ctx, true);
        try {
            final String table = Storage.LIVE_BACKUP.equals(storage) ? DEL_OXFOLDER_TREE : "oxfolder_tree";
            final String permTable = Storage.LIVE_BACKUP.equals(storage) ? DEL_OXFOLDER_PERMISSIONS : "oxfolder_permissions";
            return OXFolderLoader.loadFolderObjectFromDB(folderId, ctx, con, true, false, table, permTable);
        } finally {
            Database.back(ctx, true, con);
        }
    }

    @Override
    public EffectivePermission getFolderPermission(final int folderId, final int userId, final int contextId) throws OXException {
        try {
            return getFolderPermission(folderId, userId, contextId, true);
        } catch (OXException e) {
            if (OXFolderExceptionCode.NOT_EXISTS.equals(e)) {
                return getFolderPermission(folderId, userId, contextId, false);
            }
            throw e;
        }
    }

    @Override
    public EffectivePermission getFolderPermission(final int folderId, final int userId, final int contextId, final boolean working) throws OXException {
        final Context ctx = ContextStorage.getStorageContext(contextId);
        final UserPermissionBits permissionBits = UserPermissionBitsStorage.getInstance().getUserPermissionBits(userId, ctx);
        if (working) {
            return new OXFolderAccess(ctx).getFolderPermission(folderId, userId, permissionBits);
        }
        final Connection con = Database.get(ctx, false);
        try {
            final FolderObject delFolder =
                OXFolderLoader.loadFolderObjectFromDB(folderId, ctx, con, true, false, DEL_OXFOLDER_TREE, DEL_OXFOLDER_PERMISSIONS);
            return delFolder.getEffectiveUserPermission(userId, permissionBits, con);
        } catch (SQLException e) {
            throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Database.back(ctx, false, con);
        }
    }

}
