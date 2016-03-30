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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import com.openexchange.databaseold.Database;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.Attributes;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.TaskAttributes;
import com.openexchange.groupware.update.UpdateConcurrency;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskAdapter;

/**
 * {@link FolderInheritTrashFolderTypeTask}
 *
 * Ensures that each folder located below a user's default infostore trash folder is of type 16.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public final class FolderInheritTrashFolderTypeTask extends UpdateTaskAdapter {

    /**
     * Default constructor.
     */
    public FolderInheritTrashFolderTypeTask() {
        super();
    }

    @Override
    public String[] getDependencies() {
        return new String[] { FolderExtendNameTask.class.getName() };
    }

    @Override
    public TaskAttributes getAttributes() {
        return new Attributes(UpdateConcurrency.BLOCKING);
    }

    @Override
    public void perform(PerformParameters params) throws OXException {
        Logger log = org.slf4j.LoggerFactory.getLogger(FolderInheritTrashFolderTypeTask.class);
        log.info("Performing update task {}", FolderInheritTrashFolderTypeTask.class.getSimpleName());
        Connection connection = Database.getNoTimeout(params.getContextId(), true);
        boolean rollback = false;
        try {
            connection.setAutoCommit(false);
            rollback = true;
            List<Integer> trashFolderIDs = getDefaultTrashFolderIDs(connection, params.getContextId());
            for (Integer trashFolderID : trashFolderIDs) {
                List<Integer> subfolderIDs = getSubfolderIDsRecursively(connection, params.getContextId(), trashFolderID.intValue());
                updateFolderType(connection, params.getContextId(), 16, subfolderIDs);
            }
            connection.commit();
            rollback = false;
        } catch (SQLException e) {
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw UpdateExceptionCodes.OTHER_PROBLEM.create(e, e.getMessage());
        } finally {
            if (rollback) {
                rollback(connection);
            }
            autocommit(connection);
            Database.backNoTimeout(params.getContextId(), true, connection);
        }
        log.info("{} successfully performed.", FolderInheritTrashFolderTypeTask.class.getSimpleName());
    }

    private static List<Integer> getDefaultTrashFolderIDs(Connection connection, int contextID) throws SQLException {
        List<Integer> trashFolderIDs = new ArrayList<Integer>();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = connection.prepareStatement("SELECT fuid FROM oxfolder_tree WHERE cid=? AND type=16 AND default_flag=1;");
            stmt.setInt(1, contextID);
            rs = stmt.executeQuery();
            while (rs.next()) {
                trashFolderIDs.add(Integer.valueOf(rs.getInt(1)));
            }
        } finally {
            closeSQLStuff(rs, stmt);
        }
        return trashFolderIDs;
    }

    private static List<Integer> getSubfolderIDsRecursively(Connection connection, int contextID, int folderID) throws SQLException {
        List<Integer> subfolderIDs = new ArrayList<Integer>();
        List<Integer> parentFolderIDs = new ArrayList<Integer>();
        parentFolderIDs.add(folderID);
        do {
            StringBuilder stringBuilder = new StringBuilder("SELECT fuid FROM oxfolder_tree WHERE cid=? AND parent");
            if (1 == parentFolderIDs.size()) {
                stringBuilder.append("=?;");
            } else {
                stringBuilder.append(" IN (?");
                for (int i = 1; i < parentFolderIDs.size(); i++) {
                    stringBuilder.append(",?");
                }
                stringBuilder.append(");");
            }
            PreparedStatement stmt = null;
            ResultSet rs = null;
            try {
                stmt = connection.prepareStatement(stringBuilder.toString());
                stmt.setInt(1, contextID);
                for (int i = 0; i < parentFolderIDs.size(); i++) {
                    stmt.setInt(i + 2, parentFolderIDs.get(i).intValue());
                }
                parentFolderIDs.clear();
                rs = stmt.executeQuery();
                while (rs.next()) {
                    Integer id = Integer.valueOf(rs.getInt(1));
                    subfolderIDs.add(id);
                    parentFolderIDs.add(id);
                }
            } finally {
                closeSQLStuff(rs, stmt);
            }
        } while (false == parentFolderIDs.isEmpty());
        return subfolderIDs;
    }

    private static int updateFolderType(Connection connection, int contextID, int type, List<Integer> folderIDs) throws SQLException {
        if (null == folderIDs || 0 == folderIDs.size()) {
            return 0;
        }
        PreparedStatement stmt = null;
        try {
            StringBuilder stringBuilder = new StringBuilder("UPDATE oxfolder_tree SET type=? WHERE cid=? AND fuid");
            if (1 == folderIDs.size()) {
                stringBuilder.append("=?;");
            } else {
                stringBuilder.append(" IN (?");
                for (int i = 1; i < folderIDs.size(); i++) {
                    stringBuilder.append(",?");
                }
                stringBuilder.append(");");
            }
            stmt = connection.prepareStatement(stringBuilder.toString());
            stmt.setInt(1, type);
            stmt.setInt(2, contextID);
            for (int i = 0; i < folderIDs.size(); i++) {
                stmt.setInt(i + 3, folderIDs.get(i).intValue());
            }
            return stmt.executeUpdate();
        } finally {
            closeSQLStuff(stmt);
        }
    }

}
