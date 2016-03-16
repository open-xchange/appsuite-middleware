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

package com.openexchange.groupware.update.tasks;

import static com.openexchange.tools.sql.DBUtils.autocommit;
import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;
import static com.openexchange.tools.sql.DBUtils.rollback;
import static com.openexchange.tools.sql.DBUtils.startTransaction;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import com.openexchange.databaseold.Database;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.impl.RdbContextStorage;
import com.openexchange.groupware.update.Attributes;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.ProgressState;
import com.openexchange.groupware.update.TaskAttributes;
import com.openexchange.groupware.update.UpdateConcurrency;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskAdapter;
import com.openexchange.groupware.update.WorkingLevel;
import com.openexchange.server.impl.OCLPermission;

/**
 * Restores the initial permissions on the public root folder.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class DropIndividualUserPermissionsOnPublicFolderTask extends UpdateTaskAdapter {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DropIndividualUserPermissionsOnPublicFolderTask.class);

    public DropIndividualUserPermissionsOnPublicFolderTask() {
        super();
    }

    @Override
    public String[] getDependencies() {
        return new String[] { "com.openexchange.groupware.update.tasks.GlobalAddressBookPermissionsResolverTask" };
    }

    @Override
    public TaskAttributes getAttributes() {
        return new Attributes(UpdateConcurrency.BACKGROUND, WorkingLevel.SCHEMA);
    }

    @Override
    public void perform(final PerformParameters params) throws OXException {
        final int ctxId = params.getContextId();
        final ProgressState progress = params.getProgressState();
        final Connection con = Database.getNoTimeout(ctxId, true);
        try {
            startTransaction(con);
            Exception re = null;
            final int[] contextIds = Database.getContextsInSameSchema(ctxId);
            progress.setTotal(contextIds.length);
            int pos = 0;
            for (final int contextId : contextIds) {
                progress.setState(pos++);
                try {
                    final List<OCLPermission> permissions = getPermissions(con, contextId);
                    final int contextAdminId = RdbContextStorage.getAdmin(con, contextId);
                    correctGroupZero(con, contextId, permissions);
                    correctContextAdmin(con, contextId, permissions, contextAdminId);
                    dropAllOtherPermissions(con, contextId, isContained(permissions, contextAdminId), contextAdminId);
                } catch (final SQLException e) {
                    LOG.error("", e);
                    if (null == re) {
                        re = e;
                    }
                }
            }
            con.commit();
        } catch (final SQLException e) {
            rollback(con);
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } finally {
            autocommit(con);
            Database.backNoTimeout(ctxId, true, con);
        }
    }

    private void correctGroupZero(final Connection con, final int ctxId, final List<OCLPermission> permissions) throws SQLException {
        if (isGroupZeroCorrect(permissions)) {
            return;
        }
        correctPermission(con, ctxId, permissions, OCLPermission.ALL_GROUPS_AND_USERS, false, true);
    }

    private boolean isGroupZeroCorrect(final List<OCLPermission> permissions) {
        for (final OCLPermission permission : permissions) {
            if (OCLPermission.ALL_GROUPS_AND_USERS == permission.getEntity()) {
                return !permission.isFolderAdmin() && OCLPermission.CREATE_SUB_FOLDERS == permission.getFolderPermission() && OCLPermission.NO_PERMISSIONS == permission.getReadPermission() && OCLPermission.NO_PERMISSIONS == permission.getWritePermission() && OCLPermission.NO_PERMISSIONS == permission.getDeletePermission();
            }
        }
        return false;
    }

    private void correctContextAdmin(final Connection con, final int ctxId, final List<OCLPermission> permissions, final int contextAdminId) throws SQLException {
        if (!isContextAdminWrong(permissions, contextAdminId)) {
            return;
        }
        correctPermission(con, ctxId, permissions, contextAdminId, true, false);
    }

    private void correctPermission(final Connection con, final int ctxId, final List<OCLPermission> permissions, final int permId, final boolean admin, final boolean group) throws SQLException {
        PreparedStatement stmt = null;
        try {
            final String sql;
            final boolean update = isContained(permissions, permId);
            if (update) {
                sql = "UPDATE oxfolder_permissions SET fp=?,orp=?,owp=?,odp=?,admin_flag=?,system=? WHERE cid=? AND fuid=? AND permission_id=?";
            } else {
                sql = "INSERT INTO oxfolder_permissions (fp,orp,owp,odp,admin_flag,system,cid,fuid,permission_id,group_flag) VALUE (?,?,?,?,?,?,?,?,?,?)";
            }
            stmt = con.prepareStatement(sql);
            int pos = 1;
            stmt.setInt(pos++, OCLPermission.CREATE_SUB_FOLDERS);
            stmt.setInt(pos++, OCLPermission.NO_PERMISSIONS);
            stmt.setInt(pos++, OCLPermission.NO_PERMISSIONS);
            stmt.setInt(pos++, OCLPermission.NO_PERMISSIONS);
            stmt.setBoolean(pos++, admin); // admin_flag
            stmt.setBoolean(pos++, false);
            stmt.setInt(pos++, ctxId);
            stmt.setInt(pos++, FolderObject.SYSTEM_PUBLIC_FOLDER_ID);
            stmt.setInt(pos++, permId);
            if (!update) {
                stmt.setBoolean(pos++, group);
            }
            stmt.executeUpdate();
        } finally {
            closeSQLStuff(stmt);
        }
    }

    private boolean isContained(final List<OCLPermission> permissions, final int adminId) {
        for (final OCLPermission permission : permissions) {
            if (adminId == permission.getEntity()) {
                return true;
            }
        }
        return false;
    }

    private boolean isContextAdminWrong(final List<OCLPermission> permissions, final int adminId) {
        for (final OCLPermission permission : permissions) {
            if (adminId == permission.getEntity()) {
                return !permission.isFolderAdmin();
            }
            if (adminId != permission.getEntity() && !permission.isGroupPermission() && permission.isFolderAdmin()) {
                return true;
            }
        }
        return false;
    }

    private void dropAllOtherPermissions(final Connection con, final int ctxId, final boolean addedAdmin, final int adminId) throws SQLException {
        PreparedStatement stmt = null;
        try {
            String sql = "DELETE FROM oxfolder_permissions WHERE cid=? AND fuid=? AND permission_id!=?";
            if (addedAdmin) {
                sql += " AND permission_id!=?";
            }
            stmt = con.prepareStatement(sql);
            int pos = 1;
            stmt.setInt(pos++, ctxId);
            stmt.setInt(pos++, FolderObject.SYSTEM_PUBLIC_FOLDER_ID);
            stmt.setInt(pos++, OCLPermission.ALL_GROUPS_AND_USERS);
            if (addedAdmin) {
                stmt.setInt(pos++, adminId);
            }
            stmt.executeUpdate();
        } finally {
            closeSQLStuff(stmt);
        }
    }

    private List<OCLPermission> getPermissions(final Connection con, final int ctxId) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet result = null;
        final List<OCLPermission> retval = new ArrayList<OCLPermission>();
        try {
            stmt = con.prepareStatement("SELECT permission_id,fp,orp,owp,odp,admin_flag,group_flag,system FROM oxfolder_permissions WHERE cid=? AND fuid=?");
            stmt.setInt(1, ctxId);
            stmt.setInt(2, FolderObject.SYSTEM_PUBLIC_FOLDER_ID);
            result = stmt.executeQuery();
            while (result.next()) {
                final OCLPermission p = new OCLPermission();
                p.setEntity(result.getInt(1));
                p.setAllPermission(result.getInt(2), result.getInt(3), result.getInt(4), result.getInt(5));
                p.setFolderAdmin(result.getInt(6) > 0 ? true : false);
                p.setGroupPermission(result.getInt(7) > 0 ? true : false);
                p.setSystem(result.getInt(8));
                retval.add(p);
            }
        } finally {
            closeSQLStuff(result, stmt);
        }
        return retval;
    }
}
