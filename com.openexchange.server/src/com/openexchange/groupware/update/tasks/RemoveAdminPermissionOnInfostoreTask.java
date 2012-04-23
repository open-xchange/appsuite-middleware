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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.tools.sql.DBUtils.autocommit;
import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;
import static com.openexchange.tools.sql.DBUtils.rollback;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.commons.logging.Log;
import com.openexchange.log.LogFactory;
import com.openexchange.databaseold.Database;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.impl.ContextExceptionCodes;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.ProgressState;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskAdapter;

/**
 * {@link RemoveAdminPermissionOnInfostoreTask} - Removed incorrect admin permission on top level infostore folder.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class RemoveAdminPermissionOnInfostoreTask extends UpdateTaskAdapter {

    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(RemoveAdminPermissionOnInfostoreTask.class));

    public RemoveAdminPermissionOnInfostoreTask() {
        super();
    }

    @Override
    public int addedWithVersion() {
        return 76;
    }

    @Override
    public int getPriority() {
        return UpdateTaskPriority.HIGH.priority;
    }

    private static final String[] DEPENDENCIES = { ContactsAddIndex4AutoCompleteSearch.class.getName() };

    @Override
    public String[] getDependencies() {
        return DEPENDENCIES;
    }

    @Override
    public void perform(final PerformParameters params) throws OXException {
        final int triggeringContextId = params.getContextId();
        final int[] ctxIds = Database.getContextsInSameSchema(triggeringContextId);

        final ProgressState state = params.getProgressState();
        state.setTotal(ctxIds.length);
        final StringBuilder sb = new StringBuilder(128);

        final Connection con;
        try {
            con = Database.getNoTimeout(triggeringContextId, true);
        } catch (final OXException e) {
            throw new OXException(e);
        }
        try {
            con.setAutoCommit(false);
            for (final int contextId : ctxIds) {
                try {
                    dropTopLevelInfostoreFolderPermissionFromAdmin(con, contextId);
                } catch (final Exception e) {
                    sb.append("RemoveAdminPermissionOnInfostoreTask experienced an error while dropping ");
                    sb.append("incorrect admin permission on top level infostore folder in context ");
                    sb.append(contextId);
                    sb.append(":\n");
                    sb.append(e.getMessage());
                    LOG.error(sb.toString(), e);
                    sb.setLength(0);
                }
                state.incrementState();
            }
            con.commit();
        } catch (SQLException e) {
            rollback(con);
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } finally {
            autocommit(con);
            Database.backNoTimeout(triggeringContextId, true, con);
        }
    }

    private void dropTopLevelInfostoreFolderPermissionFromAdmin(Connection con, final int contextId) throws OXException, OXException {
        /*
         * Get context's admin
         */
        final int mailAdmin = getMailAdmin(con, contextId);
        if (-1 == mailAdmin) {
            throw missingAdminError(contextId);
        }
        /*
         * Drop permission on top level infostore folder
         */
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("DELETE FROM oxfolder_permissions WHERE cid = ? AND fuid = ? AND permission_id = ?");
            stmt.setInt(1, contextId);
            stmt.setInt(2, FolderObject.SYSTEM_INFOSTORE_FOLDER_ID);
            stmt.setInt(3, mailAdmin);
            stmt.executeUpdate();
        } catch (final SQLException e) {
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } finally {
            closeSQLStuff(stmt);
        }
    }

    private OXException missingAdminError(final int contextId) {
        return ContextExceptionCodes.NO_MAILADMIN.create(I(contextId));
    }

    private int getMailAdmin(final Connection con, final int contextId) throws OXException {
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
        }
    }
}
