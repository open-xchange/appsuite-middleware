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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Set;
import java.util.UUID;
import com.openexchange.databaseold.Database;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.java.util.UUIDs;
import com.openexchange.tools.sql.DBUtils;
import com.openexchange.tools.update.Column;
import com.openexchange.tools.update.Tools;

/**
 * {@link MigrateUUIDsForUserAliasTable}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class MigrateUUIDsForUserAliasTable extends AbstractUserAliasTableUpdateTask {

    /**
     * Initialises a new {@link MigrateUUIDsForUserAliasTable}.
     */
    public MigrateUUIDsForUserAliasTable() {
        super();
    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.groupware.update.UpdateTaskV2#perform(com.openexchange.groupware.update.PerformParameters)
     */
    @Override
    public void perform(PerformParameters params) throws OXException {
        int contextId = params.getContextId();
        Connection connection = Database.getNoTimeout(contextId, true);
        try {
            DBUtils.startTransaction(connection);
            if (!Tools.columnExists(connection, "user_alias", "uuid")) {
                // Create the 'uuid' column
                Tools.addColumns(connection, "user_alias", new Column("uuid", "BINARY(16) DEFAULT NULL"));
            }
            // Get the aliases
            Set<Alias> aliases = getAllAliasesInUserAttributes(connection);
            // Migrate the UUIDs
            if (!aliases.isEmpty()) {
                migrateUUIDs(connection, aliases);
            }
            // Generate random UUIDs for those aliases that have none
            insertUUIDs(connection);
            // Commit changes
            connection.commit();

        } catch (SQLException e) {
            DBUtils.rollback(connection);
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } finally {
            DBUtils.autocommit(connection);
            Database.backNoTimeout(contextId, true, connection);
        }

    }

    /*
     * (non-Javadoc)
     *
     * @see com.openexchange.groupware.update.UpdateTaskV2#getDependencies()
     */
    @Override
    public String[] getDependencies() {
        return new String[0];
    }

    /**
     * Insert random UUIDs to the 'uuid' column of the 'user_alias' table for all aliases that have none
     *
     * @param connection The writable connection
     * @throws SQLException If an SQL error occurs
     */
    private void insertUUIDs(Connection connection) throws SQLException {
        Statement statement = null;
        ResultSet resultSet = null;
        PreparedStatement preparedStatment = null;
        try {
            statement = connection.createStatement();
            resultSet = statement.executeQuery("SELECT cid, user, alias FROM user_alias WHERE uuid IS NULL");
            preparedStatment = connection.prepareStatement("UPDATE user_alias SET uuid = ? WHERE cid = ? AND user = ? AND alias = ?");
            while (resultSet.next()) {
                addBatch(preparedStatment, resultSet.getInt(1), resultSet.getInt(2), resultSet.getString(3), UUIDs.toByteArray(UUID.randomUUID()));
            }
            preparedStatment.executeBatch();
        } finally {
            DBUtils.closeSQLStuff(resultSet, statement);
            DBUtils.closeSQLStuff(preparedStatment);
        }
    }

    /**
     * Migrate the existing UUIDs from the 'user_attribute' table to the 'user_alias' table only if not previously migrated
     *
     * @param connection The writable connection
     * @param aliases The set of a
     * @throws SQLException
     */
    private void migrateUUIDs(Connection connection, Set<Alias> aliases) throws SQLException {
        PreparedStatement preparedStatment = null;
        try {
            preparedStatment = connection.prepareStatement("UPDATE user_alias SET uuid = ? WHERE cid = ? AND user = ? AND alias = ? AND uuid IS NULL");
            for (Alias alias : aliases) {
                addBatch(preparedStatment, alias.getCid(), alias.getUserId(), alias.getAlias(), UUIDs.toByteArray(alias.getUuid()));
            }
            preparedStatment.executeBatch();
        } finally {
            DBUtils.closeSQLStuff(preparedStatment);
        }
    }

    /**
     * Add a batch to the prepared statement
     *
     * @param preparedStatement The prepared statement
     * @param contextId The context identifier
     * @param userId The user identifier
     * @param alias The alias
     * @param uuid The UUID as byte array
     * @throws SQLException if an SQL error is occurred
     */
    private void addBatch(PreparedStatement preparedStatement, int contextId, int userId, String alias, byte[] uuid) throws SQLException {
        int columnIndex = 0;
        preparedStatement.setBytes(++columnIndex, uuid);
        preparedStatement.setInt(++columnIndex, contextId);
        preparedStatement.setInt(++columnIndex, userId);
        preparedStatement.setString(++columnIndex, alias);
        preparedStatement.addBatch();
    }
}
