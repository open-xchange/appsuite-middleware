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

package com.openexchange.folderstorage.outlook.sql;

import static com.openexchange.folderstorage.outlook.sql.Utility.debugSQL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.Folder;
import com.openexchange.folderstorage.FolderExceptionErrorMessage;
import com.openexchange.folderstorage.outlook.osgi.Services;

/**
 * {@link Insert} - SQL for inserting a virtual folder.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class Insert {

    /**
     * Initializes a new {@link Insert}.
     */
    private Insert() {
        super();
    }

    private static final String SQL_INSERT =
        "INSERT INTO virtualTree (cid, tree, user, folderId, parentId, name, modifiedBy, lastModified, shadow) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

    /**
     * Inserts specified folder.
     *
     * @param cid The context identifier
     * @param tree The tree identifier
     * @param user The user identifier
     * @param folder The folder
     * @throws OXException If insertion fails
     */
    public static void insertFolder(final int cid, final int tree, final int user, final Folder folder) throws OXException {
        final DatabaseService databaseService = Services.getService(DatabaseService.class);
        // Get a connection
        final Connection con = databaseService.getWritable(cid);
        try {
            con.setAutoCommit(false); // BEGIN
            insertFolder(cid, tree, user, folder, con);
            con.commit(); // COMMIT
        } catch (SQLException e) {
            Databases.rollback(con); // ROLLBACK
            throw FolderExceptionErrorMessage.SQL_ERROR.create(e, e.getMessage());
        } catch (OXException e) {
            Databases.rollback(con); // ROLLBACK
            throw e;
        } catch (Exception e) {
            Databases.rollback(con); // ROLLBACK
            throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            Databases.autocommit(con);
            databaseService.backWritable(cid, con);
        }
    }

    /**
     * Inserts specified folder.
     *
     * @param cid The context identifier
     * @param tree The tree identifier
     * @param user The user identifier
     * @param folder The folder
     * @param con The connection
     * @throws OXException If insertion fails
     */
    public static void insertFolder(final int cid, final int tree, final int user, final Folder folder, final Connection con) throws OXException {
        if (null == con) {
            insertFolder(cid, tree, user, folder);
            return;
        }
        final String folderId = folder.getID();
        // Insert folder data
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(SQL_INSERT);
            int pos = 1;
            stmt.setInt(pos++, cid);
            stmt.setInt(pos++, tree);
            stmt.setInt(pos++, user);
            stmt.setString(pos++, folderId);
            stmt.setString(pos++, folder.getParentID());
            stmt.setString(pos++, folder.getName());
            final int modifiedBy = folder.getModifiedBy();
            if (modifiedBy == -1) {
                stmt.setNull(pos++, Types.INTEGER);
            } else {
                stmt.setInt(pos++, modifiedBy);
            }
            final Date lastModified = folder.getLastModified();
            if (lastModified == null) {
                stmt.setNull(pos++, Types.BIGINT);
            } else {
                stmt.setLong(pos++, lastModified.getTime());
            }
            stmt.setString(pos, ""); // TODO: Shadow
            stmt.executeUpdate();
        } catch (SQLException e) {
            debugSQL(stmt);
            if (isConstraintViolation(e) && e.getMessage().indexOf("Duplicate entry") >= 0) {
                /*
                 * Ignore already existing entry, try to update
                 */
                Update.updateFolder(cid, tree, user, folder);
                return;
            }
            /*
             * Throw appropriate error
             */
            throw FolderExceptionErrorMessage.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(stmt);
        }
        // TODO: Wanna insert permission data if non-null and not empty?
    }

    private static boolean isConstraintViolation(final SQLException sqlException) {
        final String sqlState = sqlException.getSQLState();
        return ((null != sqlState) && sqlState.startsWith("23"));
    }

}
