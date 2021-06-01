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

package com.openexchange.chronos.storage.rdb;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import com.google.common.collect.Lists;
import com.openexchange.chronos.Availability;
import com.openexchange.chronos.Available;
import com.openexchange.chronos.FieldAware;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.service.AvailableField;
import com.openexchange.chronos.storage.CalendarAvailabilityStorage;
import com.openexchange.database.provider.DBProvider;
import com.openexchange.database.provider.DBTransactionPolicy;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.tools.mappings.database.DefaultDbMapper;

/**
 * {@link RdbCalendarAvailabilityStorage}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class RdbCalendarAvailabilityStorage extends RdbStorage implements CalendarAvailabilityStorage {

    private static final AvailableMapper AVAILABLE_MAPPER = AvailableMapper.getInstance();
    private static final String AVAILABLE_TABLE_NAME = "calendar_available";

    private static final int INSERT_CHUNK_SIZE = 100;

    /**
     * Initialises a new {@link RdbCalendarAvailabilityStorage}.
     *
     * @param context The context
     * @param dbProvider The database provider to use
     * @param txPolicy The transaction policy
     */
    public RdbCalendarAvailabilityStorage(Context context, DBProvider dbProvider, DBTransactionPolicy txPolicy) {
        super(context, dbProvider, txPolicy);

    }

    @Override
    public String nextAvailableId() throws OXException {
        return nextId("calendar_available_sequence");
    }

    @Override
    public void insertAvailable(List<Available> available) throws OXException {
        Connection connection = null;
        int updated = 0;
        try {
            connection = dbProvider.getWriteConnection(context);
            txPolicy.setAutoCommit(connection, false);
            // Insert the available chunk-wise
            for (List<Available> partition : Lists.partition(available, INSERT_CHUNK_SIZE)) {
                updated += insertAvailableItems(partition, AVAILABLE_TABLE_NAME, AVAILABLE_MAPPER, connection);
            }
            txPolicy.commit(connection);
        } catch (SQLException e) {
            throw CalendarExceptionCodes.DB_ERROR.create(e, e.getMessage());
        } finally {
            release(connection, updated);
        }
    }

    @Override
    public void setAvailable(int userId, List<Available> available) throws OXException {
        Connection connection = null;
        int updated = 0;
        try {
            connection = dbProvider.getWriteConnection(context);
            txPolicy.setAutoCommit(connection, false);
            // Delete existing ones
            deleteAvailable(connection, userId);
            // Insert the available chunk-wise
            for (List<Available> partition : Lists.partition(available, INSERT_CHUNK_SIZE)) {
                updated += insertAvailableItems(partition, AVAILABLE_TABLE_NAME, AVAILABLE_MAPPER, connection);
            }
            txPolicy.commit(connection);
        } catch (SQLException e) {
            throw CalendarExceptionCodes.DB_ERROR.create(e, e.getMessage());
        } finally {
            release(connection, updated);
        }
    }

    @Override
    public List<Available> loadAvailable(int userId) throws OXException {
        Connection connection = null;
        int updated = 0;
        try {
            connection = dbProvider.getReadConnection(context);
            return loadAvailable(connection, userId);
        } catch (SQLException e) {
            throw CalendarExceptionCodes.DB_ERROR.create(e, e.getMessage());
        } finally {
            release(connection, updated);
        }
    }

    @Override
    public List<Available> loadAvailable(List<Integer> userIds) throws OXException {
        Connection connection = null;
        int updated = 0;
        try {
            connection = dbProvider.getReadConnection(context);
            return loadAvailable(connection, userIds);
        } catch (SQLException e) {
            throw CalendarExceptionCodes.DB_ERROR.create(e, e.getMessage());
        } finally {
            release(connection, updated);
        }
    }

    @Override
    public void deleteAvailable(int userId) throws OXException {
        Connection connection = null;
        int updated = 0;
        try {
            connection = dbProvider.getWriteConnection(context);
            txPolicy.setAutoCommit(connection, false);
            updated = deleteAvailable(connection, userId);
            txPolicy.commit(connection);
        } catch (SQLException e) {
            throw CalendarExceptionCodes.DB_ERROR.create(e, e.getMessage());
        } finally {
            release(connection, updated);
        }
    }

    @Override
    public void deleteAvailable(String availableUid) throws OXException {
        Connection connection = null;
        int updated = 0;
        try {
            connection = dbProvider.getWriteConnection(context);
            txPolicy.setAutoCommit(connection, false);
            updated = deleteAvailable(connection, availableUid);
            txPolicy.commit(connection);
        } catch (SQLException e) {
            throw CalendarExceptionCodes.DB_ERROR.create(e, e.getMessage());
        } finally {
            release(connection, updated);
        }
    }

    @Override
    public void deleteAvailable(int userId, int availableId) throws OXException {
        Connection connection = null;
        int updated = 0;
        try {
            connection = dbProvider.getWriteConnection(context);
            txPolicy.setAutoCommit(connection, false);
            updated = deleteAvailable(connection, userId, availableId);
            txPolicy.commit(connection);
        } catch (SQLException e) {
            throw CalendarExceptionCodes.DB_ERROR.create(e, e.getMessage());
        } finally {
            release(connection, updated);
        }
    }

    @Override
    public void deleteAvailableByUid(List<String> availableIds) throws OXException {
        Connection connection = null;
        int updated = 0;
        try {
            connection = dbProvider.getWriteConnection(context);
            txPolicy.setAutoCommit(connection, false);
            updated = deleteAvailableByUid(connection, availableIds);
            txPolicy.commit(connection);
        } catch (SQLException e) {
            throw CalendarExceptionCodes.DB_ERROR.create(e, e.getMessage());
        } finally {
            release(connection, updated);
        }
    }

    @Override
    public void deleteAvailableById(List<Integer> availableIds) throws OXException {
        Connection connection = null;
        int updated = 0;
        try {
            connection = dbProvider.getWriteConnection(context);
            txPolicy.setAutoCommit(connection, false);
            updated = deleteAvailableById(connection, availableIds);
            txPolicy.commit(connection);
        } catch (SQLException e) {
            throw CalendarExceptionCodes.DB_ERROR.create(e, e.getMessage());
        } finally {
            release(connection, updated);
        }
    }

    @Override
    public void deleteAvailableByUserId(List<Integer> userIds) throws OXException {
        Connection connection = null;
        int updated = 0;
        try {
            connection = dbProvider.getWriteConnection(context);
            txPolicy.setAutoCommit(connection, false);
            updated = deleteAvailableByUserId(connection, userIds);
            txPolicy.commit(connection);
        } catch (SQLException e) {
            throw CalendarExceptionCodes.DB_ERROR.create(e, e.getMessage());
        } finally {
            release(connection, updated);
        }
    }

    ///////////////////////////////////////////////////////// HELPERS /////////////////////////////////////////////////////////

    /**
     * Inserts multiple {@link Available} items to the storage
     *
     * @param items The items to insert
     * @param tableName The table name
     * @param mapper The mapper to use
     * @param connection The writeable connection
     * @return The amount of affected rows
     * @throws OXException if an error is occurred
     * @throws SQLException if an SQL error is occurred
     */
    private <O extends FieldAware, E extends Enum<E>> int insertAvailableItems(List<O> items, String tableName, DefaultDbMapper<O, E> mapper, Connection connection) throws OXException, SQLException {
        if (items == null || items.size() == 0) {
            return 0;
        }

        E[] mappedFields = mapper.getMappedFields();
        // Prepare the initial query
        StringBuilder sb = SQLStatementBuilder.buildInsertQueryBuilder(tableName, mapper);
        // Prepare for all items
        for (int index = 1; index < items.size(); index++) {
            sb.append(",(?,").append(mapper.getParameters(mappedFields)).append(")");

        }
        sb.append(";");

        // Fill the statement with chunks' values
        try (PreparedStatement stmt = connection.prepareStatement(sb.toString())) {
            int parameterIndex = 1;
            for (O item : items) {
                stmt.setInt(parameterIndex++, context.getContextId());
                parameterIndex = mapper.setParameters(stmt, parameterIndex, item, mappedFields);
            }
            return logExecuteUpdate(stmt);
        }
    }

    /**
     * Loads from the storage all {@link Available} blocks for the specified user
     *
     * @param connection The read-only {@link Connection} to the storage
     * @param userId The user identifier
     * @return A {@link List} with all {@link Available} blocks
     * @throws OXException if the blocks cannot be loaded or any other error is occurred
     */
    private List<Available> loadAvailable(Connection connection, int userId) throws OXException, SQLException {
        AvailableField[] mappedFields = AVAILABLE_MAPPER.getMappedFields();
        StringBuilder sb = SQLStatementBuilder.buildSelectQueryBuilder(AVAILABLE_TABLE_NAME, AVAILABLE_MAPPER);
        sb.append(" AND user=?;");

        int parameterIndex = 1;
        try (PreparedStatement stmt = connection.prepareStatement(sb.toString())) {
            stmt.setInt(parameterIndex++, context.getContextId());
            stmt.setInt(parameterIndex++, userId);

            return AVAILABLE_MAPPER.listFromResultSet(logExecuteQuery(stmt), mappedFields);
        }
    }

    /**
     * Loads all {@link Availability} blocks for the users with the specified identifiers
     *
     * @param connection The read-only {@link Connection} to the storage
     * @param userIds The {@link List} of user identifiers
     * @return A {@link List} with the {@link Availability} blocks of the specified users
     * @throws OXException if an error is occurred
     */
    private List<Available> loadAvailable(Connection connection, List<Integer> userIds) throws SQLException, OXException {
        // 1) Fetch all calendar availability items for the specified users
        AvailableField[] mappedFields = AVAILABLE_MAPPER.getMappedFields();
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ").append(AVAILABLE_MAPPER.getColumns(mappedFields));
        sb.append(" FROM ").append(AVAILABLE_TABLE_NAME);
        sb.append(" WHERE cid=?");
        sb.append(" AND user IN (").append(AvailabilityMapper.getParameters(userIds.size())).append(");");

        List<Available> available = null;
        int parameterIndex = 1;
        try (PreparedStatement stmt = connection.prepareStatement(sb.toString())) {
            stmt.setInt(parameterIndex++, context.getContextId());
            for (Integer id : userIds) {
                stmt.setInt(parameterIndex++, id.intValue());
            }
            available = AVAILABLE_MAPPER.listFromResultSet(logExecuteQuery(stmt), mappedFields);
        }

        return available;
    }

    /**
     * Deletes from the storage all {@link Available} blocks for the specified user
     *
     * @param connection The writeable connection
     * @param userID The user's identifier
     * @return The amount of affected rows
     * @throws SQLException if an error is occurred
     */
    private int deleteAvailable(Connection connection, int userId) throws SQLException {
        int parameterIndex = 1;
        try (PreparedStatement stmt = connection.prepareStatement(SQLStatementBuilder.buildDeleteQueryBuilder(AVAILABLE_TABLE_NAME).append(" AND user=?;").toString())) {
            stmt.setInt(parameterIndex++, context.getContextId());
            stmt.setInt(parameterIndex++, userId);
            return logExecuteUpdate(stmt);
        }
    }

    /**
     * Deletes from the storage the {@link Available} block with the specified unique identifier
     *
     * @param connection The writeable connection
     * @param availableUid The unique identifier of the {@link Available} block
     * @return The amount of affected rows
     * @throws SQLException if an error is occurred
     */
    private int deleteAvailable(Connection connection, String availableUid) throws SQLException {
        int parameterIndex = 1;
        try (PreparedStatement stmt = connection.prepareStatement(SQLStatementBuilder.buildDeleteQueryBuilder(AVAILABLE_TABLE_NAME).append(" AND uid=?;").toString())) {
            stmt.setInt(parameterIndex++, context.getContextId());
            stmt.setString(parameterIndex++, availableUid);
            return logExecuteUpdate(stmt);
        }
    }

    /**
     * Deletes from the storage the {@link Available} block with the specified identifier
     * for the specified user
     *
     * @param connection The writeable connection
     * @param userId The user identifier
     * @param availableId The identifier of the {@link Available} block
     * @return The amount of affected rows
     * @throws SQLException if an error is occurred
     */
    private int deleteAvailable(Connection connection, int userId, int availableId) throws SQLException {
        int parameterIndex = 1;
        try (PreparedStatement stmt = connection.prepareStatement(SQLStatementBuilder.buildDeleteQueryBuilder(AVAILABLE_TABLE_NAME).append(" AND user=? AND id=?;").toString())) {
            stmt.setInt(parameterIndex++, context.getContextId());
            stmt.setInt(parameterIndex++, userId);
            stmt.setInt(parameterIndex++, availableId);
            return logExecuteUpdate(stmt);
        }
    }

    /**
     * Deletes from the storage the {@link Available} block with the specified unique identifier
     *
     * @param connection The writeable connection
     * @param availableUid The unique identifier of the {@link Available} block
     * @return The amount of affected rows
     * @throws SQLException if an error is occurred
     */
    private int deleteAvailableByUid(Connection connection, List<String> availableUids) throws SQLException {
        int parameterIndex = 1;
        try (PreparedStatement stmt = connection.prepareStatement(SQLStatementBuilder.buildDeleteQueryBuilder(AVAILABLE_TABLE_NAME).append(" AND uid IN (").append(AvailabilityMapper.getParameters(availableUids.size())).append(");").toString())) {
            stmt.setInt(parameterIndex++, context.getContextId());
            for (String uid : availableUids) {
                stmt.setString(parameterIndex++, uid);
            }
            return logExecuteUpdate(stmt);
        }
    }

    /**
     * Deletes from the storage the {@link Available} block with the specified unique identifier
     *
     * @param connection The writeable connection
     * @param availableUid The unique identifier of the {@link Available} block
     * @return The amount of affected rows
     * @throws SQLException if an error is occurred
     */
    private int deleteAvailableById(Connection connection, List<Integer> availableIds) throws SQLException {
        int parameterIndex = 1;
        try (PreparedStatement stmt = connection.prepareStatement(SQLStatementBuilder.buildDeleteQueryBuilder(AVAILABLE_TABLE_NAME).append(" AND id IN (").append(AvailabilityMapper.getParameters(availableIds.size())).append(");").toString())) {
            stmt.setInt(parameterIndex++, context.getContextId());
            for (Integer id : availableIds) {
                stmt.setInt(parameterIndex++, id.intValue());
            }
            return logExecuteUpdate(stmt);
        }
    }

    /**
     * Deletes from the storage the {@link Available} block with the specified unique identifier
     *
     * @param connection The writeable connection
     * @param availableUid The unique identifier of the {@link Available} block
     * @return The amount of affected rows
     * @throws SQLException if an error is occurred
     */
    private int deleteAvailableByUserId(Connection connection, List<Integer> userIds) throws SQLException {
        int parameterIndex = 1;
        try (PreparedStatement stmt = connection.prepareStatement(SQLStatementBuilder.buildDeleteQueryBuilder(AVAILABLE_TABLE_NAME).append(" AND user IN (").append(AvailabilityMapper.getParameters(userIds.size())).append(");").toString())) {
            stmt.setInt(parameterIndex++, context.getContextId());
            for (int userId : userIds) {
                stmt.setInt(parameterIndex++, userId);
            }
            return logExecuteUpdate(stmt);
        }
    }
}
