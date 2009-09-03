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

package com.openexchange.groupware.update.tasks;

import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import com.openexchange.database.DBPoolingException;
import com.openexchange.databaseold.Database;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.EnumComponent;
import com.openexchange.groupware.OXExceptionSource;
import com.openexchange.groupware.OXThrowsMultiple;
import com.openexchange.groupware.AbstractOXException.Category;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.update.Schema;
import com.openexchange.groupware.update.UpdateTask;
import com.openexchange.groupware.update.exception.Classes;
import com.openexchange.groupware.update.exception.UpdateException;
import com.openexchange.groupware.update.exception.UpdateExceptionFactory;
import com.openexchange.tools.sql.DBUtils;

/**
 * {@link RemoveAdminPermissionOnInfostoreTask} - Removed incorrect admin permission on top level infostore folder.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@OXExceptionSource(classId = Classes.UPDATE_TASK, component = EnumComponent.UPDATE)
public class RemoveAdminPermissionOnInfostoreTask implements UpdateTask {

    private static final UpdateExceptionFactory EXCEPTION = new UpdateExceptionFactory(RemoveAdminPermissionOnInfostoreTask.class);

    public int addedWithVersion() {
        return 76;
    }

    public int getPriority() {
        return UpdateTaskPriority.HIGH.priority;
    }

    public void perform(final Schema schema, final int contextId) throws AbstractOXException {
        final Set<Integer> set = getAllContexts(contextId);

        final int size = set.size();
        final StringBuilder sb = new StringBuilder(128);
        final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(RemoveAdminPermissionOnInfostoreTask.class);

        if (LOG.isInfoEnabled()) {
            LOG.info(sb.append("Processing ").append(size).append(" contexts in schema ").append(schema.getSchema()).toString());
            sb.setLength(0);
        }

        int processed = 0;
        for (final Integer cid : set) {
            try {
                dropTopLevelInfostoreFolderPermissionFromAdmin(cid.intValue());
            } catch (final Exception e) {
                sb.append("RemoveAdminPermissionOnInfostoreTask experienced an error while dropping ");
                sb.append("incorrect admin permission on top level infostore folder in context ");
                sb.append(cid);
                sb.append(":\n");
                sb.append(e.getMessage());
                LOG.error(sb.toString(), e);
                sb.setLength(0);
            }
            processed++;
            if (LOG.isInfoEnabled()) {
                sb.append("Processed ").append(processed);
                if (1 == processed) {
                    sb.append(" context of ");
                } else {
                    sb.append(" contexts of ");
                }
                sb.append(size).append(" contexts in schema ").append(schema.getSchema());
                LOG.info(sb.toString());

                sb.setLength(0);
            }
        }

        if (LOG.isInfoEnabled()) {
            LOG.info("UpdateTask 'RemoveAdminPermissionOnInfostoreTask' successfully performed!");
        }
    }

    private static Set<Integer> getAllContexts(final int contextId) throws UpdateException {
        final Connection con;
        try {
            con = Database.get(contextId, false);
        } catch (final DBPoolingException e) {
            throw new UpdateException(e);
        }
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT cid FROM user");
            rs = stmt.executeQuery();
            if (!rs.next()) {
                return Collections.emptySet();
            }
            final Set<Integer> set = new HashSet<Integer>();
            do {
                set.add(Integer.valueOf(rs.getInt(1)));
            } while (rs.next());
            return set;
        } catch (final SQLException e) {
            throw createSQLError(e);
        } finally {
            closeSQLStuff(rs, stmt);
            Database.back(contextId, false, con);
        }
    }

    private static void dropTopLevelInfostoreFolderPermissionFromAdmin(final int contextId) throws UpdateException {
        /*
         * Get context's admin
         */
        final int mailAdmin = getMailAdmin(contextId);
        if (-1 == mailAdmin) {
            throw missingAdminError(contextId);
        }
        /*
         * Drop permission on top level infostore folder
         */
        final Connection con;
        try {
            con = Database.get(contextId, true);
        } catch (final DBPoolingException e) {
            throw new UpdateException(e);
        }
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("DELETE FROM oxfolder_permissions WHERE cid = ? AND fuid = ? AND permission_id = ?");
            stmt.setInt(1, contextId);
            stmt.setInt(2, FolderObject.SYSTEM_INFOSTORE_FOLDER_ID);
            stmt.setInt(3, mailAdmin);
            stmt.executeUpdate();
        } catch (final SQLException e) {
            throw createSQLError(e);
        } finally {
            DBUtils.closeSQLStuff(stmt);
            Database.back(contextId, true, con);
        }
    }

    @OXThrowsMultiple(category = { Category.CODE_ERROR }, desc = { "" }, exceptionId = { 1 }, msg = { "A SQL error occurred while performing task RemoveAdminPermissionOnInfostoreTask: %1$s." })
    private static UpdateException createSQLError(final SQLException e) {
        return EXCEPTION.create(1, e, e.getMessage());
    }

    @OXThrowsMultiple(category = { Category.CODE_ERROR }, desc = { "" }, exceptionId = { 2 }, msg = { "Error while performing task RemoveAdminPermissionOnInfostoreTask: No context admin exists for context %1$s." })
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

}
