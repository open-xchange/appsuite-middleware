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

package com.openexchange.drive.checksum.rdb;

import static com.openexchange.tools.sql.DBUtils.autocommit;
import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;
import static com.openexchange.tools.sql.DBUtils.rollback;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import com.openexchange.database.DatabaseService;
import com.openexchange.drive.impl.internal.DriveServiceLookup;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskAdapter;
import com.openexchange.tools.update.Column;
import com.openexchange.tools.update.Tools;

/**
 * {@link DirectoryChecksumsAddUserAndETagColumnTask}
 *
 * Deletes all existing directory checksums if the <code>user</code> column not yet exists, then
 * <ul>
 * <li>modifies the column <code>sequence</code> to <code>sequence BIGINT(20) DEFAULT NULL</code></li>
 * <li>adds the column <code>user INT4 UNSIGNED DEFAULT NULL</code></li>
 * <li>adds the column <code>etag VARCHAR(255) DEFAULT NULL</code></li>
 * </ul>
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class DirectoryChecksumsAddUserAndETagColumnTask extends UpdateTaskAdapter {

    @Override
    public String[] getDependencies() {
        return new String[] { DriveCreateTableTask.class.getName() };
    }

    @Override
    public void perform(PerformParameters params) throws OXException {
        int contextID = params.getContextId();
        DatabaseService dbService = DriveServiceLookup.getService(DatabaseService.class);
        Connection connection = dbService.getForUpdateTask(contextID);
        boolean committed = false;
        try {
            connection.setAutoCommit(false);
            if (false == Tools.columnExists(connection, "directoryChecksums", "user")) {
                deleteDirectoryChecksums(connection);
            }
            if (false == Tools.isNullable(connection, "directoryChecksums", "sequence")) {
                Tools.modifyColumns(connection, "directoryChecksums", new Column("sequence", "BIGINT(20) DEFAULT NULL"));
            }
            Tools.checkAndAddColumns(connection, "directoryChecksums",
                new Column("user", "INT4 UNSIGNED DEFAULT NULL"), new Column("etag", "VARCHAR(255) DEFAULT NULL"));
            connection.commit();
            committed = true;
        } catch (SQLException e) {
            rollback(connection);
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } catch (RuntimeException e) {
            rollback(connection);
            throw UpdateExceptionCodes.OTHER_PROBLEM.create(e, e.getMessage());
        } finally {
            autocommit(connection);
            if (committed) {
                dbService.backForUpdateTask(contextID, connection);
            } else {
                dbService.backForUpdateTaskAfterReading(contextID, connection);
            }
        }
    }

    /**
     * Deletes all entries from the <code>directoryChecksums</code> table.
     *
     * @param connection The connection
     * @throws SQLException
     */
    private static void deleteDirectoryChecksums(Connection connection) throws SQLException {
        Statement stmt = null;
        try {
            stmt = connection.createStatement();
            stmt.execute("DELETE FROM directoryChecksums;");
        } finally {
            closeSQLStuff(stmt);
        }
    }

}
