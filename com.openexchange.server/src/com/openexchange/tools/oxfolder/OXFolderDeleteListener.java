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

package com.openexchange.tools.oxfolder;

import java.sql.Connection;
import java.sql.SQLException;
import com.openexchange.cache.impl.FolderCacheManager;
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
        final long lastModified = System.currentTimeMillis();

        if (delEvent.getType() == DeleteEvent.TYPE_USER) {
            handleUserDeletion(delEvent, readCon, writeCon, ctx, lastModified);
        } else if (delEvent.getType() == DeleteEvent.TYPE_GROUP) {
            handleGroupDeletion(delEvent, readCon, writeCon, ctx, lastModified);
        }
        OXFolderDeleteListenerHelper.ensureConsistency(ctx, writeCon);
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
		        } catch (final OXException e) {
		            LOG.error("", e);
		        }
		    }
		} catch (final OXException e) {
		    LOG.error("", e);
		    throw e;
		} catch (final SQLException e) {
		    LOG.error("", e);
		    throw DeleteFailedExceptionCodes.SQL_ERROR.create(e, e.getMessage());
		}
	}

	protected void handleUserDeletion(final DeleteEvent delEvent,
			final Connection readCon, final Connection writeCon,
			final Context ctx, final long lastModified)
			throws OXException {
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
		            } catch (final OXException e) {
		                LOG.error("", e);
		            }
		        }
		    }
		} catch (final OXException e) {
		    LOG.error("", e);
		    throw e;
		} catch (final SQLException e) {
		    LOG.error("", e);
		    throw DeleteFailedExceptionCodes.SQL_ERROR.create(e, e.getMessage());
		}
	}

}
