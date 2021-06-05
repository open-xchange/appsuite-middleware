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

package com.openexchange.tools.oxfolder;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import com.openexchange.cache.impl.FolderCacheManager;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.delete.DeleteEvent;
import com.openexchange.groupware.delete.DeleteFailedExceptionCodes;
import com.openexchange.groupware.delete.DeleteListener;
import com.openexchange.tools.oxfolder.deletelistener.OXFolderDeleteListenerHelper;
import com.openexchange.tools.oxfolder.memory.ConditionTreeMapManagement;

/**
 * Implements interface {@link DeleteListener}.
 * <p>
 * In case of a normal user all his private permissions (working & backup) are going to be deleted first, whereby his public permissions
 * (working & backup) are reassigned to context's admin. In next step all private folders owned by this user are going to be completely
 * deleted while checking any left references in corresponding permission table AND 'oxfolder_specialfolders' table. All public folders
 * owned by this user are reassigned to context's admin. Finally folder table is checked if any references in column 'changed_from' points
 * to this user. If any, they are going to be reassigned to context's admin, too.
 * </p>
 * <p>
 * In case of a group, only permission references are examined, since a group cannot occur in folder fields 'owner' or 'modifedBy'
 * </p>
 * <p>
 * In case of context's admin, every reference located in any folder or permission table (working & backup) are removed, that either points
 * to admin himself or point to virtual group 'All Groups & Users'
 * </p>
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class OXFolderDeleteListener implements DeleteListener {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(OXFolderDeleteListener.class);

    private static final String TABLE_WORKING_FOLDER = "oxfolder_tree";

    private static final String TABLE_WORKING_PERMS = "oxfolder_permissions";

    private static final String TABLE_BACKUP_FOLDER = "del_oxfolder_tree";

    private static final String TABLE_BACKUP_PERMS = "del_oxfolder_permissions";

    public OXFolderDeleteListener() {
        super();
    }

    @Override
    public void deletePerformed(final DeleteEvent delEvent, final Connection readCon, final Connection writeCon) throws OXException {
        final Context ctx = delEvent.getContext();
        if (delEvent.getType() == DeleteEvent.TYPE_CONTEXT) {
            handleContextDeletion(writeCon, ctx);
            return;
        }

        if (delEvent.getType() == DeleteEvent.TYPE_USER) {
            long lastModified = System.currentTimeMillis();
            handleUserDeletion(delEvent, readCon, writeCon, ctx, lastModified);
            OXFolderDeleteListenerHelper.ensureConsistency(ctx, writeCon);
        } else if (delEvent.getType() == DeleteEvent.TYPE_GROUP) {
            long lastModified = System.currentTimeMillis();
            handleGroupDeletion(delEvent, readCon, writeCon, ctx, lastModified);
            OXFolderDeleteListenerHelper.ensureConsistency(ctx, writeCon);
        }
    }

    private void handleContextDeletion(Connection writeCon, Context ctx) throws OXException {
        Statement stmt = null;
        try {
            stmt = writeCon.createStatement();

            stmt.addBatch("DELETE FROM oxfolder_property WHERE cid=" + ctx.getContextId());
            stmt.addBatch("DELETE FROM oxfolder_lock WHERE cid=" + ctx.getContextId());
            stmt.addBatch("DELETE FROM del_oxfolder_permissions WHERE cid=" + ctx.getContextId());
            stmt.addBatch("DELETE FROM del_oxfolder_tree WHERE cid=" + ctx.getContextId());
            stmt.addBatch("DELETE FROM oxfolder_userfolders_standardfolders WHERE cid=" + ctx.getContextId());
            stmt.addBatch("DELETE FROM oxfolder_userfolders WHERE cid=" + ctx.getContextId());
            stmt.addBatch("DELETE FROM oxfolder_specialfolders WHERE cid=" + ctx.getContextId());
            stmt.addBatch("DELETE FROM oxfolder_permissions WHERE cid=" + ctx.getContextId());
            stmt.addBatch("DELETE FROM oxfolder_tree WHERE cid=" + ctx.getContextId());

            stmt.executeBatch();
        } catch (SQLException e) {
            throw DeleteFailedExceptionCodes.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }

	private void handleGroupDeletion(final DeleteEvent delEvent,
			final Connection readCon, final Connection writeCon,
			final Context ctx, final long lastModified)
			throws OXException {
		try {
		    final int groupId = delEvent.getId();
		    /*
		     * Get context's mailadmin
		     */
		    int mailadmin = ctx.getMailadmin();
		    if (mailadmin == -1) {
		        mailadmin = OXFolderSQL.getContextMailAdmin(readCon, ctx);
		        if (mailadmin == -1) {
		            throw OXFolderExceptionCode.NO_ADMIN_USER_FOUND_IN_CONTEXT.create(Integer.valueOf(ctx.getContextId()));
		        }
		    }
		    /*
		     * Drop system permissions for group
		     */
		    OXFolderSQL.cleanseSystemPermissions(groupId, TABLE_WORKING_PERMS, writeCon, ctx);
		    OXFolderSQL.cleanseSystemPermissions(groupId, TABLE_BACKUP_PERMS, writeCon, ctx);
		    /*
		     * Hander group's permissions
		     */
		    OXFolderSQL.handleEntityPermissions(
		        groupId,
		        mailadmin,
                null,
		        lastModified,
		        TABLE_WORKING_FOLDER,
		        TABLE_WORKING_PERMS,
		        readCon,
		        writeCon,
		        ctx);
		    /*
		     * Backup
		     */
		    OXFolderSQL.handleEntityPermissions(
		        groupId,
		        mailadmin,
                null,
		        lastModified,
		        TABLE_BACKUP_FOLDER,
		        TABLE_BACKUP_PERMS,
		        readCon,
		        writeCon,
		        ctx);
		    /*
		     * Update shared and public folders' last-modified timestamp to enforce a folder repaint in AJAX-UI
		     */
		    OXFolderSQL.updateLastModified(FolderObject.SYSTEM_SHARED_FOLDER_ID, lastModified, mailadmin, writeCon, ctx);
		    OXFolderSQL.updateLastModified(FolderObject.SYSTEM_PUBLIC_FOLDER_ID, lastModified, mailadmin, writeCon, ctx);
		    /*
		     * Remove from cache
		     */
		    ConditionTreeMapManagement.dropFor(ctx.getContextId());
		    if (FolderCacheManager.isInitialized()) {
		        /*
		         * Invalidate cache
		         */
		        try {
		            FolderCacheManager.getInstance().removeFolderObject(FolderObject.SYSTEM_SHARED_FOLDER_ID, ctx);
		            FolderCacheManager.getInstance().removeFolderObject(FolderObject.SYSTEM_PUBLIC_FOLDER_ID, ctx);
		        } catch (OXException e) {
		            LOG.error("", e);
		        }
		    }
		} catch (OXException e) {
		    LOG.error("", e);
		    throw e;
		} catch (SQLException e) {
		    LOG.error("", e);
		    throw DeleteFailedExceptionCodes.SQL_ERROR.create(e, e.getMessage());
		}
	}

    protected void handleUserDeletion(DeleteEvent delEvent, Connection readCon, Connection writeCon, Context ctx, long lastModified) throws OXException {
        try {
		    final int userId = delEvent.getId();
		    /*
		     * Get context's admin
		     */
		    int mailadmin = ctx.getMailadmin();
		    if (mailadmin == -1) {
		        mailadmin = OXFolderSQL.getContextMailAdmin(readCon, ctx);
		        if (mailadmin == -1) {
		            throw OXFolderExceptionCode.NO_ADMIN_USER_FOUND_IN_CONTEXT.create(Integer.valueOf(ctx.getContextId()));
		        }
		    }
		    /*
		     * Drop system permissions
		     */
		    OXFolderSQL.cleanseSystemPermissions(userId, TABLE_WORKING_PERMS, writeCon, ctx);
		    OXFolderSQL.cleanseSystemPermissions(userId, TABLE_BACKUP_PERMS, writeCon, ctx);
		    final boolean isMailAdmin = (mailadmin == userId);

            Integer destUserID = delEvent.getDestinationUserID();

		    /*
		     * Handle user's permissions
		     */
		    if (isMailAdmin) {
		        /*
		         * Working
		         */
		        OXFolderSQL.handleMailAdminPermissions(userId, TABLE_WORKING_FOLDER, TABLE_WORKING_PERMS, readCon, writeCon, ctx);
		        /*
		         * Backup
		         */
		        OXFolderSQL.handleMailAdminPermissions(userId, TABLE_BACKUP_FOLDER, TABLE_BACKUP_PERMS, readCon, writeCon, ctx);
            } else {
                /*
                 * Working
                 */
                OXFolderSQL.handleEntityPermissions(userId, mailadmin, destUserID, lastModified, TABLE_WORKING_FOLDER, TABLE_WORKING_PERMS, readCon, writeCon, ctx);
                /*
                 * Backup
                 */
                OXFolderSQL.handleEntityPermissions(userId, mailadmin, destUserID, lastModified, TABLE_BACKUP_FOLDER, TABLE_BACKUP_PERMS, readCon, writeCon, ctx);
            }
		    /*
		     * Handle user's folders
		     */
		    if (isMailAdmin) {
		        /*
		         * Working
		         */
		        OXFolderSQL.handleMailAdminFolders(userId, TABLE_WORKING_FOLDER, TABLE_WORKING_PERMS, readCon, writeCon, ctx);
		        /*
		         * Backup
		         */
		        OXFolderSQL.handleMailAdminFolders(userId, TABLE_BACKUP_FOLDER, TABLE_BACKUP_PERMS, readCon, writeCon, ctx);
            } else {
                /*
                 * Working
                 */
                OXFolderSQL.handleEntityFolders(userId, mailadmin, destUserID, lastModified, TABLE_WORKING_FOLDER, TABLE_WORKING_PERMS, readCon, writeCon, ctx);
                /*
                 * Backup
                 */
                OXFolderSQL.handleEntityFolders(userId, mailadmin, destUserID, lastModified, TABLE_BACKUP_FOLDER, TABLE_BACKUP_PERMS, readCon, writeCon, ctx);
            }
		    if (!isMailAdmin) {
		        /*
		         * Update shared folder's last-modified timestamp to enforce a folder repaint in AJAX-UI
		         */
		        OXFolderSQL.updateLastModified(FolderObject.SYSTEM_SHARED_FOLDER_ID, lastModified, mailadmin, writeCon, ctx);
		        /*
		         * Remove from cache
		         */
		        ConditionTreeMapManagement.dropFor(ctx.getContextId());
		        if (FolderCacheManager.isInitialized()) {
		            /*
		             * Invalidate cache
		             */
		            try {
		                FolderCacheManager.getInstance().removeFolderObject(FolderObject.SYSTEM_SHARED_FOLDER_ID, ctx);
		            } catch (OXException e) {
		                LOG.error("", e);
		            }
		        }
		    }
		} catch (OXException e) {
		    LOG.error("", e);
		    throw e;
		} catch (SQLException e) {
		    LOG.error("", e);
		    throw DeleteFailedExceptionCodes.SQL_ERROR.create(e, e.getMessage());
		}
	}

}
