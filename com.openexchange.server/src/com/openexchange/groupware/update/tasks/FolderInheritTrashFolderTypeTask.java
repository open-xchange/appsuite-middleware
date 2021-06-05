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

package com.openexchange.groupware.update.tasks;

import static com.openexchange.database.Databases.closeSQLStuff;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import com.openexchange.database.Databases;
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

        Connection connection = params.getConnection();
        int rollback = 0;
        try {
            connection.setAutoCommit(false);
            rollback = 1;

            for (int contextId : params.getContextsInSameSchema()) {
                List<Integer> trashFolderIDs = getDefaultTrashFolderIDs(connection, contextId);
                for (Integer trashFolderID : trashFolderIDs) {
                    List<Integer> subfolderIDs = getSubfolderIDsRecursively(connection, contextId, trashFolderID.intValue());
                    updateFolderType(connection, contextId, 16, subfolderIDs);
                }
            }

            connection.commit();
            rollback = 2;
            log.info("{} successfully performed.", FolderInheritTrashFolderTypeTask.class.getSimpleName());
        } catch (SQLException e) {
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw UpdateExceptionCodes.OTHER_PROBLEM.create(e, e.getMessage());
        } finally {
            if (rollback > 0) {
                if (rollback == 1) {
                    Databases.rollback(connection);
                }
                Databases.autocommit(connection);
            }
        }
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
        parentFolderIDs.add(Integer.valueOf(folderID));
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
