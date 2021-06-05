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

package com.openexchange.tools.oxfolder.treeconsistency;

import static com.openexchange.java.Autoboxing.I;
import java.sql.Connection;
import java.sql.SQLException;
import org.osgi.service.event.EventAdmin;
import com.openexchange.cache.impl.FolderCacheManager;
import com.openexchange.cache.impl.FolderQueryCacheManager;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.FolderPermissionType;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.tools.oxfolder.OXFolderExceptionCode;
import com.openexchange.tools.oxfolder.OXFolderSQL;
import com.openexchange.tools.oxfolder.memory.ConditionTreeMapManagement;
import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.TIntSet;

/**
 * {@link CheckPermissionOnUpdate} - Checks for system permissions which shall be updated.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class CheckPermissionOnUpdate extends CheckPermission {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(CheckPermissionOnUpdate.class);

    /**
     * Initializes a new {@link CheckPermissionOnUpdate}
     *
     * @param session The session
     * @param writeCon A connection with write capability
     * @param ctx The context
     */
    public CheckPermissionOnUpdate(final Session session, final Connection writeCon, final Context ctx) {
        super(session, writeCon, ctx);
    }

    /**
     * Checks for parental visibility permissions and adds a folder-read-only-permission for non-tree-visible parent folder if user has
     * admin permission (optional).
     *
     * @param parent The parent folder ID
     * @param newPerms The current permissions that shall be applied to affected folder
     * @param lastModified The last-modified time stamp to use when adding permissions
     * @param alreadyCheckedParents The set of already checked parents
     * @throws OXException If checking parental visibility permissions fails
     */
    public void checkParentPermissions(final int parent, final OCLPermission[] newPerms, final OCLPermission[] oldPerms, final long lastModified, TIntObjectMap<TIntSet> alreadyCheckedParents) throws OXException {
        try {
            TIntObjectMap<ToDoPermission> map = new TIntObjectHashMap<>();
            for (int i = 0, k = newPerms.length; k-- > 0; i++) {
                OCLPermission assignedPerm = newPerms[i];
                if (assignedPerm.isFolderVisible() && !isFolderVisible(assignedPerm.getEntity(), oldPerms) && alreadyCheckedParents.get(assignedPerm.getEntity()).add(parent)) {
                    /*
                     * Grant system-permission for this entity to parent folders
                     */
                    ensureParentVisibility(parent, assignedPerm.getEntity(), assignedPerm.isGroupPermission(), map, alreadyCheckedParents);
                }
            }
            /*
             * Auto-insert system-folder-read permission to make possible non-visible parent folders visible in folder tree
             */
            if (!map.isEmpty()) {
                final TIntObjectIterator<ToDoPermission> it = map.iterator();
                for ( int i = map.size(); i-- > 0; ) {
                    it.advance();
                    final int folderId = it.key();
                    /*
                     * Insert read permissions
                     */
                    final ToDoPermission toDoPermission = it.value();
                    final int[] users = toDoPermission.getUsers();
                    for (int j = 0; j < users.length; j++) {
                        LOG.debug("Auto-Insert system-folder-read permission for user {} to folder {}", I(users[j]), I(folderId));
                        addSystemFolderReadPermission(folderId, users[j], false);
                    }
                    final int[] groups = toDoPermission.getGroups();
                    for (int j = 0; j < groups.length; j++) {
                        LOG.debug("Auto-Insert system-folder-read permission for group {} to folder {}", I(groups[j]), I(folderId));
                        addSystemFolderReadPermission(folderId, groups[j], true);
                    }
                    /*
                     * Update folders last-modified
                     */
                    OXFolderSQL.updateLastModified(folderId, lastModified, ctx.getMailadmin(), writeCon, ctx);
                    /*
                     * Update caches
                     */
                    ConditionTreeMapManagement.dropFor(ctx.getContextId());
                    try {
                        if (FolderCacheManager.isEnabled()) {
                            FolderCacheManager.getInstance().removeFolderObject(folderId, ctx);
                        }
                        broadcastEvent(folderId, true, ServerServiceRegistry.getInstance().getService(EventAdmin.class));
                        if (FolderQueryCacheManager.isInitialized()) {
                            FolderQueryCacheManager.getInstance().invalidateContextQueries(session);
                        }
                    } catch (OXException e) {
                        LOG.error("", e);
                    }
                }
            }
        } catch (SQLException e) {
            throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Checks if specified entity is allowed to see the folder according to given permissions.
     *
     * @param entity The entity to check
     * @param permissions The permissions tom check against
     * @return <code>true</code> if specified entity is allowed to see the folder; otherwise <code>false</code>
     */
    private boolean isFolderVisible(int entity, OCLPermission[] permissions) {
        for (OCLPermission permission : permissions) {
            if (entity == permission.getEntity() && permission.isFolderVisible()) {
                return true;
            }
        }
        return false;
    }

    private void ensureParentVisibility(final int parent, final int entity, final boolean isGroup, final TIntObjectMap<ToDoPermission> map, TIntObjectMap<TIntSet> alreadyCheckedParents) throws OXException, SQLException {
        if (parent < FolderObject.MIN_FOLDER_ID) {
            /*
             * We reached a context-created folder
             */
            return;
        }
        final FolderObject parentFolder = getFolderFromMaster(parent);
        /*
         * Check for system-read-folder permission for current entity
         */
        if (!containsSystemPermission(parentFolder.getPermissions(), entity)) {
            /*
             * Add system-read-folder permission for current entity
             */
            ToDoPermission todo = map.get(parent);
            if (todo == null) {
                todo = new ToDoPermission(parent);
                map.put(parent, todo);
            }
            if (isGroup) {
                todo.addGroup(entity);
            } else {
                todo.addUser(entity);
            }
        }
        /*
         * Recursive call with parent's parent
         */
        if (alreadyCheckedParents.get(entity).add(parentFolder.getParentFolderID())) {
            ensureParentVisibility(parentFolder.getParentFolderID(), entity, isGroup, map, alreadyCheckedParents);
        }
    }

    /**
     * Adds system-read-folder permission to specified folder for given entity
     *
     * @param folderId The folder ID
     * @param entity The entity
     * @param isGroup whether entity denotes a group
     * @throws OXException If a pooling error occurs
     * @throws SQLException If a SQL error occurs
     */
    private void addSystemFolderReadPermission(final int folderId, final int entity, final boolean isGroup) throws OXException, SQLException {
        /*
         * Add folder-read permission
         */
        OXFolderSQL.addSinglePermission(
            folderId,
            entity,
            isGroup,
            OCLPermission.READ_FOLDER,
            OCLPermission.NO_PERMISSIONS,
            OCLPermission.NO_PERMISSIONS,
            OCLPermission.NO_PERMISSIONS,
            false,
            OCLPermission.SYSTEM_SYSTEM,
            FolderPermissionType.NORMAL,
            null,
            writeCon,
            ctx);
    }

}
