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

import static com.openexchange.database.Databases.autocommit;
import static com.openexchange.database.Databases.rollback;
import static com.openexchange.groupware.update.WorkingLevel.SCHEMA;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.slf4j.Logger;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.update.Attributes;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.TaskAttributes;
import com.openexchange.groupware.update.UpdateConcurrency;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskAdapter;
import com.openexchange.server.impl.OCLPermission;

/**
 * {@link FolderPermissionReadAllForUserInfostore}
 *
 * Grants "read all" permissions for the user infostore folder.
 *
 * @author <a href="mailto:tobias.Friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public final class FolderPermissionReadAllForUserInfostore extends UpdateTaskAdapter {

    /**
     * Default constructor.
     */
    public FolderPermissionReadAllForUserInfostore() {
        super();
    }

    @Override
    public String[] getDependencies() {
        return new String[] { com.openexchange.groupware.update.tasks.FolderPermissionAddGuestGroup.class.getName() };
    }

    @Override
    public TaskAttributes getAttributes() {
        return new Attributes(UpdateConcurrency.BLOCKING, SCHEMA);
    }

    @Override
    public void perform(PerformParameters params) throws OXException {
        Logger log = org.slf4j.LoggerFactory.getLogger(FolderPermissionReadAllForUserInfostore.class);
        log.info("Performing update task {}", FolderPermissionReadAllForUserInfostore.class.getSimpleName());
        Connection connection = params.getConnection();
        boolean committed = false;
        try {
            connection.setAutoCommit(false);
            adjustReadPermission(connection, FolderObject.SYSTEM_USER_INFOSTORE_FOLDER_ID, OCLPermission.READ_ALL_OBJECTS);
            connection.commit();
            committed = true;
        } catch (SQLException e) {
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw UpdateExceptionCodes.OTHER_PROBLEM.create(e, e.getMessage());
        } finally {
            if (false == committed) {
                rollback(connection);
            }
            autocommit(connection);
        }
        log.info("{} successfully performed.", FolderPermissionReadAllForUserInfostore.class.getSimpleName());
    }

    /**
     * Adjusts the object read permissions for all permission entities in all contexts of a specific folder.
     *
     * @param connection A writable database connection
     * @param folderID The folder identifier
     * @param orp The object read permissions to apply
     * @return The update count
     */
    private static int adjustReadPermission(Connection connection, int folderID, int orp) throws SQLException {
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement("UPDATE oxfolder_permissions SET orp=? WHERE fuid=?;");
            stmt.setInt(1, orp);
            stmt.setInt(2, folderID);
            return stmt.executeUpdate();
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }

}
