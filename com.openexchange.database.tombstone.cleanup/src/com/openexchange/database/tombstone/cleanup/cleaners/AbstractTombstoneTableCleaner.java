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

package com.openexchange.database.tombstone.cleanup.cleaners;

import static com.openexchange.java.Autoboxing.L;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;
import com.openexchange.exception.OXException;

/**
 * {@link AbstractTombstoneTableCleaner}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.2
 */
public abstract class AbstractTombstoneTableCleaner implements TombstoneTableCleaner {

    protected static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AbstractTombstoneTableCleaner.class);

    @Override
    public Map<String, Integer> cleanup(Connection connection, long timestamp) throws SQLException {
        try {
            checkTables(connection);
        } catch (SQLException e) {
            LOG.error("Error while checking table design: {}. Skip cleaning up this table.", e.getMessage(), e);
            return Collections.emptyMap();
        } catch (OXException e) {
            LOG.warn("Table(s) is/are in an inappropriate design: {}. Skip cleaning up this table.", e);
            return Collections.emptyMap();
        }
        return cleanupSafe(connection, timestamp);
    }

    /**
     * Ensures to have tables in a state to be cleaned up. If a table is not in the desired state an {@link OXException} will be thrown.
     * 
     * @param connection {@link Connection} to retrieve table meta information
     * @throws OXException In case table validation fails
     * @throws SQLException If an error occurred while retrieving table information
     */
    public abstract void checkTables(Connection connection) throws OXException, SQLException;

    /**
     * Delegate for {@link TombstoneTableCleaner#cleanup(Connection, long)} that will be called after the table design has been verified by {@link #checkTables(Connection)}
     * 
     * @param connection Write connection to the destination schema
     * @param timestamp Timestamp defining the border of what will be removed which means older entries than the given timestamp will be removed
     * @return {@link Map} Containing the number of items that have been deleted by the {@link TombstoneTableCleaner} mapped to the table
     * @throws SQLException In case data can't be removed
     */
    public abstract Map<String, Integer> cleanupSafe(Connection connection, long timestamp) throws SQLException;

    /**
     * Executes the given delete statement by setting the timestamp
     * 
     * @param connection The write connection used for deletion
     * @param timestamp The timestamp to delete all entries before it
     * @param deleteStatement The statement that will be used for deletion
     * @throws SQLException
     */
    protected int delete(Connection connection, long timestamp, String deleteStatement) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(deleteStatement)) {
            int parameterIndex = 1;
            stmt.setLong(parameterIndex++, timestamp);
            return logExecuteUpdate(stmt);
        }
    }

    /**
     * Logs & executes a prepared statement's SQL update.
     *
     * @param stmt The statement to execute the SQL update from
     * @return The number of affected rows
     */
    protected final static int logExecuteUpdate(final PreparedStatement stmt) throws SQLException {
        try {
            if (false == LOG.isDebugEnabled()) {
                return stmt.executeUpdate();
            }
            long start = System.currentTimeMillis();
            final int rowCount = stmt.executeUpdate();
            LOG.debug("executeUpdate: {} - {} rows affected, {} ms elapsed.", stmt.toString(), rowCount, (System.currentTimeMillis() - start));
            return rowCount;
        } catch (SQLException e) {
            LOG.warn("Error executing \"{}\": {}", stmt, e.getMessage());
            throw e;
        }
    }

    /**
     * Logs & executes a prepared statement's SQL query.
     *
     * @param stmt The statement to execute the SQL query from
     * @return The result set
     */
    protected final static ResultSet logExecuteQuery(PreparedStatement stmt) throws SQLException {
        if (false == LOG.isDebugEnabled()) {
            return stmt.executeQuery();
        }
        String statementString = String.valueOf(stmt);
        long start = System.currentTimeMillis();
        ResultSet resultSet = stmt.executeQuery();
        LOG.debug("executeQuery: {} - {} ms elapsed.", statementString, L(System.currentTimeMillis() - start));
        return resultSet;
    }
}
