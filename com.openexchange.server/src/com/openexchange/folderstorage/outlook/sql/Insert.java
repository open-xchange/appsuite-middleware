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
        } catch (final SQLException e) {
            Databases.rollback(con); // ROLLBACK
            throw FolderExceptionErrorMessage.SQL_ERROR.create(e, e.getMessage());
        } catch (final OXException e) {
            Databases.rollback(con); // ROLLBACK
            throw e;
        } catch (final Exception e) {
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
        } catch (final SQLException e) {
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
