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

import static com.openexchange.groupware.update.UpdateConcurrency.BACKGROUND;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.Attributes;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.TaskAttributes;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskAdapter;

/**
 * {@link FolderClearDelTablesTasks} - Removes obsolete data from the 'del_oxfolder_tree', and 'virtualBackupTree' tables.
 * <p>
 * <ul>
 * <li>del_oxfolder_tree<ul><li>fname</ul>
 * <li>virtualBackupTree<ul><li>name</ul>
 * </ul>
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class FolderClearDelTablesTasks extends UpdateTaskAdapter {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(FolderClearDelTablesTasks.class);

    /**
     * Initializes a new {@link FolderClearDelTablesTasks}.
     */
    public FolderClearDelTablesTasks() {
        super();
    }

    @Override
    public String[] getDependencies() {
        return new String[0];
    }

    @Override
    public void perform(final PerformParameters params) throws OXException {
        Connection connection = params.getConnection();
        int rollback = 0;
        try {
            connection.setAutoCommit(false);
            rollback = 1;

            LOG.info("Clearing obsolete fields in 'del_oxfolder_tree'...");
            int cleared = clearDelOxfolderTree(connection);
            LOG.info("Cleared {} rows in 'del_oxfolder_tree'.", Integer.valueOf(cleared));

            LOG.info("Clearing obsolete fields in 'virtualBackupTree'...");
            cleared = clearVirtuelBackupTree(connection);
            LOG.info("Cleared {} rows in 'virtualBackupTree'.", Integer.valueOf(cleared));

            connection.commit();
            rollback = 2;
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

    private int clearDelOxfolderTree(final Connection connection) throws SQLException {
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement("UPDATE del_oxfolder_tree SET fname=?");
            statement.setString(1, "");
            return statement.executeUpdate();
        } finally {
            Databases.closeSQLStuff(statement);
        }
    }

    private int clearVirtuelBackupTree(final Connection connection) throws SQLException {
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement("UPDATE virtualBackupTree SET name=?");
            statement.setString(1, "");
            return statement.executeUpdate();
        } finally {
            Databases.closeSQLStuff(statement);
        }
    }

    @Override
    public TaskAttributes getAttributes() {
        return new Attributes(BACKGROUND);
    }

}
