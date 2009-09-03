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

package com.openexchange.folderstorage.virtual;

import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import com.openexchange.api2.OXException;
import com.openexchange.database.DBPoolingException;
import com.openexchange.databaseold.Database;
import com.openexchange.folderstorage.ContentType;
import com.openexchange.folderstorage.Folder;
import com.openexchange.folderstorage.FolderException;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.Type;
import com.openexchange.folderstorage.virtual.sql.Insert;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.EnumComponent;
import com.openexchange.groupware.OXExceptionSource;
import com.openexchange.groupware.OXThrowsMultiple;
import com.openexchange.groupware.AbstractOXException.Category;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextImpl;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.update.Schema;
import com.openexchange.groupware.update.UpdateTask;
import com.openexchange.groupware.update.exception.Classes;
import com.openexchange.groupware.update.exception.UpdateException;
import com.openexchange.groupware.update.exception.UpdateExceptionFactory;
import com.openexchange.groupware.userconfiguration.UserConfigurationException;
import com.openexchange.groupware.userconfiguration.UserConfigurationStorage;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mail.utils.MailFolderUtility;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountException;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.mailaccount.UnifiedINBOXManagement;
import com.openexchange.server.ServiceException;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.oxfolder.OXFolderAccess;

/**
 * {@link VirtualTreeMigrationTask} - Inserts necessary tables to support missing POP3 features.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@OXExceptionSource(classId = Classes.UPDATE_TASK, component = EnumComponent.UPDATE)
public class VirtualTreeMigrationTask implements UpdateTask {

    private static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(VirtualTreeMigrationTask.class);

    private static final UpdateExceptionFactory EXCEPTION = new UpdateExceptionFactory(VirtualTreeMigrationTask.class);

    public int addedWithVersion() {
        return 76;
    }

    public int getPriority() {
        return UpdateTaskPriority.HIGH.priority;
    }

    public void perform(final Schema schema, final int contextId) throws AbstractOXException {
        final Map<Integer, List<Integer>> m = getAllUsers(contextId);

        final int size = m.size();
        final StringBuilder sb = new StringBuilder(128);
        if (LOG.isInfoEnabled()) {
            LOG.info(sb.append("Processing ").append(size).append(" contexts in schema ").append(schema.getSchema()).toString());
            sb.setLength(0);
        }

        int processed = 0;
        for (final Iterator<Map.Entry<Integer, List<Integer>>> it = m.entrySet().iterator(); it.hasNext();) {
            final Map.Entry<Integer, List<Integer>> me = it.next();
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
            throw createSQLError(e);
        } finally {
            closeSQLStuff(rs, stmt);
            Database.back(contextId, false, writeCon);
        }
    }

    private static void iterateUsersPerContext(final List<Integer> users, final int contextId) throws UpdateException {
        try {
            /*
             * Get context
             */
            final Context ctx;
            {
                final int mailAdmin = getMailAdmin(contextId);
                if (-1 == mailAdmin) {
                    throw missingAdminError(contextId);
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
        } catch (final ServiceException e) {
            throw new UpdateException(e);
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
            folder.setName("Inbox");
            folder.setID(MailFolderUtility.prepareFullname(MailAccount.DEFAULT_ID, "INBOX"));
            folder.setTreeID("1");
            folder.setParentID(String.valueOf(FolderObject.SYSTEM_PRIVATE_FOLDER_ID));
            folder.setSubscribed(true);
            Insert.insertFolder(ctx.getContextId(), 1, user, folder);
        } catch (final OXException e) {
            throw new UpdateException(e);
        } catch (final FolderException e) {
            throw new UpdateException(e);
        }
    }

    private static void createVirtualTopLevel(final int user, final Context ctx) throws ServiceException, UpdateException {
        final int treeId = 1;
        final String treeIdentifier = String.valueOf(treeId);
        {
            final Connection con;
            try {
                con = Database.get(ctx, true);
            } catch (final DBPoolingException e) {
                throw new UpdateException(e);
            }
            final int mailAdmin = ctx.getMailadmin();
            final long creatingTime = System.currentTimeMillis();
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
        try {
            if (UserConfigurationStorage.getInstance().getUserConfiguration(user, ctx).isMultipleMailAccounts()) {
                final User userObj = UserStorage.getStorageUser(user, ctx);
                /*
                 * Now insert user's external mail accounts on top level
                 */
                final List<MailAccount> accounts;
                {
                    final MailAccountStorageService storageService = ServerServiceRegistry.getInstance().getService(
                        MailAccountStorageService.class,
                        true);
                    final MailAccount[] mailAccounts = storageService.getUserMailAccounts(user, ctx.getContextId());
                    accounts = new ArrayList<MailAccount>(mailAccounts.length);
                    accounts.addAll(Arrays.asList(mailAccounts));
                    Collections.sort(accounts, new MailAccountComparator(userObj.getLocale()));
                }
                if (!accounts.isEmpty()) {
                    for (final MailAccount mailAccount : accounts) {
                        final DummyFolder mailFolder = new DummyFolder();
                        if (!mailAccount.isDefaultAccount()) {
                            mailFolder.setName(mailAccount.getName());
                            mailFolder.setID(MailFolderUtility.prepareFullname(mailAccount.getId(), MailFolder.DEFAULT_FOLDER_ID));
                            mailFolder.setTreeID(treeIdentifier);
                            mailFolder.setParentID(FolderStorage.ROOT_ID);
                            mailFolder.setSubscribed(true);
                            Insert.insertFolder(ctx.getContextId(), treeId, user, mailFolder);
                        }
                    }
                }
            }
        } catch (final MailAccountException e) {
            throw new UpdateException(e);
        } catch (final UserConfigurationException e) {
            throw new UpdateException(e);
        } catch (final FolderException e) {
            throw new UpdateException(e);
        }
    }

    @OXThrowsMultiple(category = { Category.CODE_ERROR }, desc = { "" }, exceptionId = { 1 }, msg = { "A SQL error occurred while performing task VirtualTreeMigrationTask: %1$s." })
    private static UpdateException createSQLError(final SQLException e) {
        return EXCEPTION.create(1, e, e.getMessage());
    }

    @OXThrowsMultiple(category = { Category.CODE_ERROR }, desc = { "" }, exceptionId = { 2 }, msg = { "Error while performing task VirtualTreeMigrationTask: No context admin exists for context %1$s." })
    private static UpdateException missingAdminError(final int contextId) {
        return EXCEPTION.create(2, Integer.valueOf(contextId));
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
            throw createSQLError(e);
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
            throw createSQLError(e);
        } finally {
            closeSQLStuff(rs, stmt);
            Database.back(ctx, false, con);
        }
    }

    private static final class DummyFolder implements Folder {

        private static final long serialVersionUID = 8179196440833088118L;

        private Date lastModified;

        private int modifiedBy;

        private String treeId;

        private String id;

        private String name;

        private String parent;

        private Permission[] permissions;

        private boolean subscribed;

        public DummyFolder() {
            super();
            modifiedBy = -1;
        }

        @Override
        public Object clone() {
            try {
                return super.clone();
            } catch (final CloneNotSupportedException e) {
                throw new InternalError(e.getMessage());
            }
        }

        public int getCapabilities() {
            throw new UnsupportedOperationException("VirtualTreeMigrationTask.DummyFolder.getCapabilities()");
        }

        public ContentType getContentType() {
            throw new UnsupportedOperationException("VirtualTreeMigrationTask.DummyFolder.getContentType()");
        }

        public int getCreatedBy() {
            throw new UnsupportedOperationException("VirtualTreeMigrationTask.DummyFolder.getCreatedBy()");
        }

        public java.util.Date getCreationDate() {
            throw new UnsupportedOperationException("VirtualTreeMigrationTask.DummyFolder.getCreationDate()");
        }

        public int getDeleted() {
            throw new UnsupportedOperationException("VirtualTreeMigrationTask.DummyFolder.getDeleted()");
        }

        public String getID() {
            return id;
        }

        public java.util.Date getLastModified() {
            return lastModified;
        }

        public String getLocalizedName(final Locale locale) {
            return name;
        }

        public int getModifiedBy() {
            return modifiedBy;
        }

        public String getName() {
            return name;
        }

        public int getNew() {
            throw new UnsupportedOperationException("VirtualTreeMigrationTask.DummyFolder.getNew()");
        }

        public String getParentID() {
            return parent;
        }

        public Permission[] getPermissions() {
            return permissions;
        }

        public String[] getSubfolderIDs() {
            throw new UnsupportedOperationException("VirtualTreeMigrationTask.DummyFolder.getSubfolderIDs()");
        }

        public String getSummary() {
            throw new UnsupportedOperationException("VirtualTreeMigrationTask.DummyFolder.getSummary()");
        }

        public int getTotal() {
            throw new UnsupportedOperationException("VirtualTreeMigrationTask.DummyFolder.getTotal()");
        }

        public String getTreeID() {
            return treeId;
        }

        public Type getType() {
            throw new UnsupportedOperationException("VirtualTreeMigrationTask.DummyFolder.getType()");
        }

        public int getUnread() {
            throw new UnsupportedOperationException("VirtualTreeMigrationTask.DummyFolder.getUnread()");
        }

        public boolean isCacheable() {
            throw new UnsupportedOperationException("VirtualTreeMigrationTask.DummyFolder.isCacheable()");
        }

        public boolean isDefault() {
            throw new UnsupportedOperationException("VirtualTreeMigrationTask.DummyFolder.isDefault()");
        }

        public boolean isGlobalID() {
            throw new UnsupportedOperationException("VirtualTreeMigrationTask.DummyFolder.isGlobalID()");
        }

        public boolean isSubscribed() {
            return subscribed;
        }

        public boolean isVirtual() {
            return true;
        }

        public void setCapabilities(final int capabilities) {
            throw new UnsupportedOperationException("VirtualTreeMigrationTask.DummyFolder.setCapabilities()");
        }

        public void setContentType(final ContentType contentType) {
            throw new UnsupportedOperationException("VirtualTreeMigrationTask.DummyFolder.setContentType()");
        }

        public void setCreatedBy(final int createdBy) {
            throw new UnsupportedOperationException("VirtualTreeMigrationTask.DummyFolder.setCreatedBy()");
        }

        public void setCreationDate(final java.util.Date creationDate) {
            throw new UnsupportedOperationException("VirtualTreeMigrationTask.DummyFolder.setCreationDate()");
        }

        public void setDefault(final boolean deefault) {
            throw new UnsupportedOperationException("VirtualTreeMigrationTask.DummyFolder.setDefault()");
        }

        public void setDeleted(final int deleted) {
            throw new UnsupportedOperationException("VirtualTreeMigrationTask.DummyFolder.setDeleted()");
        }

        public void setID(final String id) {
            this.id = id;
        }

        public void setLastModified(final Date lastModified) {
            this.lastModified = lastModified;
        }

        public void setModifiedBy(final int modifiedBy) {
            this.modifiedBy = modifiedBy;
        }

        public void setName(final String name) {
            this.name = name;
        }

        public void setNew(final int nu) {
            throw new UnsupportedOperationException("VirtualTreeMigrationTask.DummyFolder.setNew()");
        }

        public void setParentID(final String parentId) {
            this.parent = parentId;
        }

        public void setPermissions(final Permission[] permissions) {
            this.permissions = permissions;
        }

        public void setSubfolderIDs(final String[] subfolderIds) {
            throw new UnsupportedOperationException("VirtualTreeMigrationTask.DummyFolder.setSubfolderIDs()");
        }

        public void setSubscribed(final boolean subscribed) {
            this.subscribed = subscribed;
        }

        public void setSummary(final String summary) {
            throw new UnsupportedOperationException("VirtualTreeMigrationTask.DummyFolder.setSummary()");
        }

        public void setTotal(final int total) {
            throw new UnsupportedOperationException("VirtualTreeMigrationTask.DummyFolder.setTotal()");
        }

        public void setTreeID(final String id) {
            this.treeId = id;
        }

        public void setType(final Type type) {
            throw new UnsupportedOperationException("VirtualTreeMigrationTask.DummyFolder.setType()");
        }

        public void setUnread(final int unread) {
            throw new UnsupportedOperationException("VirtualTreeMigrationTask.DummyFolder.setUnread()");
        }

    } // End of DummyFolder

    private static final class MailAccountComparator implements Comparator<MailAccount> {

        private final Collator collator;

        public MailAccountComparator(final Locale locale) {
            super();
            collator = Collator.getInstance(locale);
            collator.setStrength(Collator.SECONDARY);
        }

        public int compare(final MailAccount o1, final MailAccount o2) {
            if (UnifiedINBOXManagement.PROTOCOL_UNIFIED_INBOX.equals(o1.getMailProtocol())) {
                if (UnifiedINBOXManagement.PROTOCOL_UNIFIED_INBOX.equals(o2.getMailProtocol())) {
                    return 0;
                }
                return -1;
            } else if (UnifiedINBOXManagement.PROTOCOL_UNIFIED_INBOX.equals(o2.getMailProtocol())) {
                return 1;
            }
            if (o1.isDefaultAccount()) {
                if (o2.isDefaultAccount()) {
                    return 0;
                }
                return -1;
            } else if (o2.isDefaultAccount()) {
                return 1;
            }
            return collator.compare(o1.getName(), o2.getName());
        }

    } // End of MailAccountComparator

}
