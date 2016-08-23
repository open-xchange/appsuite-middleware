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

package com.openexchange.tools.oxfolder.treeconsistency;

import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import org.osgi.service.event.EventAdmin;
import com.openexchange.cache.impl.FolderCacheManager;
import com.openexchange.cache.impl.FolderQueryCacheManager;
import com.openexchange.exception.OXException;
import com.openexchange.group.GroupStorage;
import com.openexchange.groupware.calendar.CalendarCache;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.tools.oxfolder.OXFolderExceptionCode;
import com.openexchange.tools.oxfolder.OXFolderSQL;
import com.openexchange.tools.oxfolder.memory.ConditionTreeMapManagement;

/**
 * {@link CheckPermissionOnRemove} - Checks for system permissions which shall be removed.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class CheckPermissionOnRemove extends CheckPermission {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(CheckPermissionOnRemove.class);

    /**
     * Initializes a new {@link CheckPermissionOnRemove}
     *
     * @param session The session
     * @param writeCon A connection with write capability
     * @param ctx The context
     */
    public CheckPermissionOnRemove(final Session session, final Connection writeCon, final Context ctx) {
        super(session, writeCon, ctx);
    }

    /**
     * Checks for system permissions which shall be removed due to an update operation
     *
     * @param folderId The current folder ID
     * @param removedPerms The removed permissions (by an update operation)
     * @param lastModified The last-modified time stamp
     * @throws OXException If checking for possible non-visible subfolders fails
     */
    public void checkPermissionsOnUpdate(final int folderId, final OCLPermission[] removedPerms, final long lastModified)
            throws OXException {
        try {
            /*
             * Remove system permissions from previous parent
             */
            final FolderObject folder = getFolderFromMaster(folderId);
            final TIntObjectMap<ToDoPermission> toRemove = new TIntObjectHashMap<ToDoPermission>();
            final List<OCLPermission> permissions = folder.getPermissions();
            for (final OCLPermission perm : removedPerms) {
                final OCLPermission removedPerm = perm;
                if (!containsSystemPermission(permissions, removedPerm.getEntity())) {
                    final int parent = folder.getParentFolderID();
                    /*
                     * Folder does NOT grant system-read-folder permission, therefore check if system-read-folder permission can be removed
                     * from parent if no sibling is visible
                     */
                    hasVisibleSibling(parent, folderId, parent, removedPerm.getEntity(), removedPerm.isGroupPermission(), toRemove);
                }
            }
            if (!toRemove.isEmpty()) {
                removeSystemPermissions(lastModified, toRemove);
            }
        } catch (final SQLException e) {
            throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Checks for system permissions which shall be removed due to a delete operation
     *
     * @param parent The parent's folder ID
     * @param deletedId The ID of the deleted folder
     * @param formerPerms The former child's permissions
     * @param lastModified The last-modified time stamp
     * @throws OXException If checking for possible non-visible subfolders fails
     */
    public void checkPermissionsOnDelete(final int parent, final int deletedId, final OCLPermission[] formerPerms, final long lastModified) throws OXException {
        try {
            final TIntObjectMap<ToDoPermission> toRemove = new TIntObjectHashMap<ToDoPermission>();
            for (final OCLPermission formerPerm : formerPerms) {
                hasVisibleSibling(parent, deletedId, parent, formerPerm.getEntity(), formerPerm.isGroupPermission(), toRemove);
            }
            if (!toRemove.isEmpty()) {
                removeSystemPermissions(lastModified, toRemove);
            }
        } catch (final SQLException e) {
            throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
        }
    }

    private void hasVisibleSibling(final int parent, final int exclude, final int origin, final int entity, final boolean isGroup,
            final TIntObjectMap<ToDoPermission> toRemove) throws OXException, OXException, SQLException {
        if (parent < FolderObject.MIN_FOLDER_ID) {
            /*
             * Stop recursive check
             */
            return;
        }
        /*
         * Iterate folder's subfolders
         */
        final List<Integer> siblingIDs = FolderObject.getSubfolderIds(parent, ctx, writeCon);
        for (final Integer siblingID : siblingIDs) {
            if (siblingID.intValue() != exclude && getFolderFromMaster(siblingID.intValue()).isVisible(entity)) {
                /*
                 * Visible sibling detected
                 */
                return;
            }
        }
        ToDoPermission todo = toRemove.get(parent);
        if (todo == null) {
            todo = new ToDoPermission(parent);
            toRemove.put(parent, todo);
        }
        if (isGroup) {
            todo.addGroup(entity);
        } else {
            todo.addUser(entity);
        }
        /*
         * Check if recursive call is needed that is current parent folder is no more visible to entity if system permission is removed
         */
        FolderObject parentFolder;
        try {
            parentFolder = getFolderFromMaster(parent, false);
        } catch (OXException e) {
            if ("FLD-0008".equals(e.getErrorCode())) {
                // parent no longer found, abort
                return;
            }
            throw e;
        }
        if (false == parentFolder.isNonSystemVisible(entity)) {
            /*
             * Recursively check ancestor folders
             */
            hasVisibleSibling(parentFolder.getParentFolderID(), parent, origin, entity, isGroup, toRemove);
        }
    }

    /**
     * Delete system-read-folder permission to specified folder for given entity
     *
     * @param folderId The folder ID
     * @param entity The entity
     * @throws OXException If a pooling error occurs
     * @throws SQLException If a SQL error occurs
     */
    private void deleteSystemFolderReadPermission(final int folderId, final int entity) throws OXException, SQLException {
        /*
         * Delete folder-read permission
         */
        OXFolderSQL.deleteSingleSystemPermission(folderId, entity, writeCon, ctx);
    }

    /**
     * Iterates specified map and deletes denoted permissions
     *
     * @param lastModified The last-modified time stamp
     * @param toRemove The map containing the permissions to delete
     * @throws OXException If a pooling error occurs
     * @throws SQLException If a SQL error occurs
     */
    private void removeSystemPermissions(final long lastModified, final TIntObjectMap<ToDoPermission> toRemove) throws OXException, SQLException {
        final TIntObjectIterator<ToDoPermission> iterator = toRemove.iterator();
        for ( int i = toRemove.size(); i-- > 0; ) {
            iterator.advance();
            final int fid = iterator.key();
            /*
             * Delete read permissions
             */
            final ToDoPermission toDoPermission = iterator.value();
            final int[] users = toDoPermission.getUsers();
            for (final int user : users) {
                LOG.debug("Auto-Delete system-folder-read permission for user {} from folder {}", UserStorage.getInstance().getUser(user, ctx).getDisplayName(), fid);
                deleteSystemFolderReadPermission(fid, user);
            }
            final int[] groups = toDoPermission.getGroups();
            for (final int group : groups) {
                LOG.debug("Auto-Delete system-folder-read permission for group {} from folder {}", GroupStorage.getInstance().getGroup(group, ctx).getDisplayName(), fid);

                deleteSystemFolderReadPermission(fid, group);
            }
            /*
             * Update folders last-modified
             */
            OXFolderSQL.updateLastModified(fid, lastModified, ctx.getMailadmin(), writeCon, ctx);
            /*
             * Update caches
             */
            ConditionTreeMapManagement.dropFor(ctx.getContextId());
            try {
                if (FolderCacheManager.isEnabled()) {
                    FolderCacheManager.getInstance().removeFolderObject(fid, ctx);
                }
                broadcastEvent(fid, true, ServerServiceRegistry.getInstance().getService(EventAdmin.class));
                if (FolderQueryCacheManager.isInitialized()) {
                    FolderQueryCacheManager.getInstance().invalidateContextQueries(session);
                }
                if (CalendarCache.isInitialized()) {
                    CalendarCache.getInstance().invalidateGroup(ctx.getContextId());
                }
            } catch (final OXException e) {
                LOG.error("", e);
            }
        }
    }

}
