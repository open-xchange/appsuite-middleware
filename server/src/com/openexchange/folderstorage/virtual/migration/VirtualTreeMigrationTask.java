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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

package com.openexchange.folderstorage.virtual.migration;

import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.openexchange.api2.OXException;
import com.openexchange.database.DBPoolingException;
import com.openexchange.databaseold.Database;
import com.openexchange.folderstorage.FolderException;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.virtual.VirtualPermission;
import com.openexchange.folderstorage.virtual.VirtualTreeCreateTableTask;
import com.openexchange.folderstorage.virtual.sql.Insert;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextException;
import com.openexchange.groupware.contexts.impl.ContextImpl;
import com.openexchange.groupware.i18n.MailStrings;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.Schema;
import com.openexchange.groupware.update.UpdateException;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskAdapter;
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.mail.utils.MailFolderUtility;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.tools.oxfolder.OXFolderAccess;

/**
 * {@link VirtualTreeMigrationTask} - Migrates folder data to new outlook-like tree structure.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class VirtualTreeMigrationTask extends UpdateTaskAdapter {

    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(VirtualTreeMigrationTask.class));

    private static final String[] DEPENDENCIES = { VirtualTreeCreateTableTask.class.getName() };

    public String[] getDependencies() {
        return DEPENDENCIES;
    }

    public void perform(final PerformParameters params) throws AbstractOXException {
        final int contextId = params.getContextId();
        final Schema schema = params.getSchema();
        final Map<Integer, List<Integer>> m = getAllUsers(contextId);

        final int size = m.size();
        final StringBuilder sb = new StringBuilder(128);
        if (LOG.isInfoEnabled()) {
            LOG.info(sb.append("Processing ").append(size).append(" contexts in schema ").append(schema.getSchema()).toString());
            sb.setLength(0);
        }

        int processed = 0;
        for (final Entry<Integer, List<Integer>> me : m.entrySet()) {
            final int currentContextId = me.getKey().intValue();
            try {
                iterateUsersPerContext(me.getValue(), currentContextId);
            } catch (final AbstractOXException e) {
                sb.append("VirtualTreeMigrationTask experienced an error while migrating folder trees for users in context ");
                sb.append(currentContextId);
                sb.append(":\n");
                sb.append(e.getMessage());
                LOG.error(sb.toString(), e);
                sb.setLength(0);
            }
            processed++;
            if (LOG.isInfoEnabled()) {
                LOG.info(sb.append("Processed ").append(processed).append(" contexts of ").append(size).append(" contexts in schema ").append(
                    schema.getSchema()).toString());
                sb.setLength(0);
            }
        }

        if (LOG.isInfoEnabled()) {
            LOG.info("UpdateTask 'VirtualTreeMigrationTask' successfully performed!");
        }
    }

    private static Map<Integer, List<Integer>> getAllUsers(final int contextId) throws UpdateException {
        final Connection writeCon;
        try {
            writeCon = Database.get(contextId, false);
        } catch (final DBPoolingException e) {
            throw new UpdateException(e);
        }
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = writeCon.prepareStatement("SELECT cid, id FROM user");
            rs = stmt.executeQuery();
            if (!rs.next()) {
                return Collections.emptyMap();
            }
            final Map<Integer, List<Integer>> m = new HashMap<Integer, List<Integer>>();
            do {
                final Integer cid = Integer.valueOf(rs.getInt(1));
                final Integer user = Integer.valueOf(rs.getInt(2));
                final List<Integer> l;
                if (!m.containsKey(cid)) {
                    l = new ArrayList<Integer>();
                    m.put(cid, l);
                } else {
                    l = m.get(cid);
                }
                l.add(user);
            } while (rs.next());
            return m;
        } catch (final SQLException e) {
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } finally {
            closeSQLStuff(rs, stmt);
            Database.back(contextId, false, writeCon);
        }
    }

    private static void iterateUsersPerContext(final List<Integer> users, final int contextId) throws UpdateException {
        /*
         * Get context
         */
        final Context ctx;
        {
            final int mailAdmin = getMailAdmin(contextId);
            if (-1 == mailAdmin) {
                throw new UpdateException(new ContextException(ContextException.Code.NO_MAILADMIN));
            }
            final ContextImpl ctxi = new ContextImpl(contextId);
            ctxi.setMailadmin(mailAdmin);
            ctx = ctxi;
        }
        /*
         * Iterate users
         */

        final int size = users.size();
        final StringBuilder sb = new StringBuilder(128);
        if (LOG.isInfoEnabled()) {
            LOG.info(sb.append("Processing ").append(size).append(" users in context ").append(contextId).toString());
            sb.setLength(0);
        }

        int processed = 0;
        for (final Integer userId : users) {
            final int user = userId.intValue();
            if (!virtualTreeExists(user, ctx)) {
                /*
                 * Create root folder
                 */
                createVirtualRootFolder(user, ctx);
                /*
                 * Create user top level
                 */
                createVirtualTopLevel(user, ctx);
                /*
                 * Create default folders below private folder
                 */
                createVirtualPrivateFolderLevel(user, ctx);
            }
            processed++;
            if (LOG.isInfoEnabled()) {
                LOG.info(sb.append("Processed ").append(processed).append(" users of ").append(size).append(" users in context ").append(
                    contextId).toString());
                sb.setLength(0);
            }
        }
    }

    private static void createVirtualRootFolder(final int user, final Context ctx) throws UpdateException {
        try {
            final DummyFolder folder = new DummyFolder();
            folder.setName("Root");
            folder.setID(FolderStorage.ROOT_ID);
            folder.setTreeID("1");
            folder.setParentID("");
            folder.setSubscribed(true);
            Insert.insertFolder(ctx.getContextId(), 1, user, folder);
        } catch (final FolderException e) {
            throw new UpdateException(e);
        }
    }

    private static void createVirtualPrivateFolderLevel(final int user, final Context ctx) throws UpdateException {
        try {
            /*
             * Insert default database folders (tasks, calendar, and contacts)
             */
            final OXFolderAccess folderAccess = new OXFolderAccess(ctx);
            for (final int module : new int[] { FolderObject.CALENDAR, FolderObject.TASK, FolderObject.CONTACT }) {
                final FolderObject defaultFolder = folderAccess.getDefaultFolder(user, module);
                final DummyFolder folder = new DummyFolder();
                folder.setName(defaultFolder.getFolderName());
                folder.setID(String.valueOf(defaultFolder.getObjectID()));
                folder.setTreeID("1");
                folder.setParentID(String.valueOf(FolderObject.SYSTEM_PRIVATE_FOLDER_ID));
                folder.setSubscribed(true);
                Insert.insertFolder(ctx.getContextId(), 1, user, folder);
            }
            /*
             * Insert default primary mail account folder (INBOX)
             */
            final DummyFolder folder = new DummyFolder();
            folder.setName(new StringHelper(UserStorage.getStorageUser(user, ctx).getLocale()).getString(MailStrings.INBOX));
            folder.setID(MailFolderUtility.prepareFullname(MailAccount.DEFAULT_ID, "INBOX"));
            folder.setTreeID(FolderStorage.PRIVATE_ID);
            folder.setParentID(String.valueOf(FolderObject.SYSTEM_PRIVATE_FOLDER_ID));
            folder.setSubscribed(true);
            Insert.insertFolder(ctx.getContextId(), 1, user, folder);
            /*
             * Insert other mail default folders: Drafts, Sent, Spam, and Trash
             */

        } catch (final OXException e) {
            throw new UpdateException(e);
        } catch (final FolderException e) {
            throw new UpdateException(e);
        }
    }

    private static void createVirtualTopLevel(final int user, final Context ctx) throws UpdateException {
        final int treeId = 1;
        final String treeIdentifier = String.valueOf(treeId);
        {
            final Connection con;
            try {
                con = Database.get(ctx, true);
            } catch (final DBPoolingException e) {
                throw new UpdateException(e);
            }
            try {
                final Permission systemPermission = new VirtualPermission();
                systemPermission.setEntity(OCLPermission.ALL_GROUPS_AND_USERS);
                systemPermission.setGroup(true);
                /*
                 * Insert system private folder
                 */
                systemPermission.setAllPermissions(
                    OCLPermission.CREATE_SUB_FOLDERS,
                    OCLPermission.NO_PERMISSIONS,
                    OCLPermission.NO_PERMISSIONS,
                    OCLPermission.NO_PERMISSIONS);
                systemPermission.setAdmin(false);
                final DummyFolder systemFolder = new DummyFolder();
                systemFolder.setID(String.valueOf(FolderObject.SYSTEM_PRIVATE_FOLDER_ID));
                systemFolder.setName(FolderObject.SYSTEM_PRIVATE_FOLDER_NAME); // TODO: Empty string?
                systemFolder.setPermissions(/* new Permission[] { systemPermission } */null); // TODO: Ignore?
                systemFolder.setParentID(FolderStorage.ROOT_ID);
                systemFolder.setModifiedBy(/* mailAdmin */-1); // TODO: Ignore?
                systemFolder.setLastModified(/* new Date(creatingTime) */null); // TODO: Ignore?
                systemFolder.setSubscribed(true);
                systemFolder.setTreeID(treeIdentifier);
                Insert.insertFolder(ctx.getContextId(), treeId, user, systemFolder, con);
                /*
                 * Insert system public folder
                 */
                systemPermission.setAllPermissions(
                    OCLPermission.CREATE_SUB_FOLDERS,
                    OCLPermission.NO_PERMISSIONS,
                    OCLPermission.NO_PERMISSIONS,
                    OCLPermission.NO_PERMISSIONS);
                systemPermission.setAdmin(false);
                systemFolder.setID(String.valueOf(FolderObject.SYSTEM_PUBLIC_FOLDER_ID));
                systemFolder.setName(FolderObject.SYSTEM_PUBLIC_FOLDER_NAME); // TODO: Empty string?
                systemFolder.setPermissions(/* new Permission[] { systemPermission } */null); // TODO: Ignore?
                Insert.insertFolder(ctx.getContextId(), treeId, user, systemFolder, con);
                /*
                 * Insert system shared folder
                 */
                systemPermission.setAllPermissions(
                    OCLPermission.READ_FOLDER,
                    OCLPermission.NO_PERMISSIONS,
                    OCLPermission.NO_PERMISSIONS,
                    OCLPermission.NO_PERMISSIONS);
                systemPermission.setAdmin(false);
                systemFolder.setID(String.valueOf(FolderObject.SYSTEM_SHARED_FOLDER_ID));
                systemFolder.setName(FolderObject.SYSTEM_SHARED_FOLDER_NAME); // TODO: Empty string?
                systemFolder.setPermissions(/* new Permission[] { systemPermission } */null); // TODO: Ignore?
                Insert.insertFolder(ctx.getContextId(), treeId, user, systemFolder, con);
            } catch (final FolderException e) {
                throw new UpdateException(e);
            } finally {
                Database.back(ctx, true, con);
            }
        }
    }

    private static int getMailAdmin(final int contextId) throws UpdateException {
        final Connection con;
        try {
            con = Database.get(contextId, false);
        } catch (final DBPoolingException e) {
            throw new UpdateException(e);
        }
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT user FROM user_setting_admin WHERE cid = ?");
            stmt.setInt(1, contextId);
            rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return -1;
        } catch (final SQLException e) {
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } finally {
            closeSQLStuff(rs, stmt);
            Database.back(contextId, false, con);
        }
    }

    private static boolean virtualTreeExists(final int user, final Context ctx) throws UpdateException {
        final Connection con;
        try {
            con = Database.get(ctx, false);
        } catch (final DBPoolingException e) {
            throw new UpdateException(e);
        }
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT folderId FROM virtualTree WHERE cid = ? AND tree = ? AND user = ?");
            stmt.setInt(1, ctx.getContextId());
            stmt.setInt(2, 1);
            stmt.setInt(3, user);
            rs = stmt.executeQuery();
            return rs.next();
        } catch (final SQLException e) {
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } finally {
            closeSQLStuff(rs, stmt);
            Database.back(ctx, false, con);
        }
    }

}
