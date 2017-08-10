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
 *     Copyright (C) 2017-2020 OX Software GmbH
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
import com.openexchange.chronos.service.AvailabilityField;
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

    private static final AvailabilityMapper AVAILABILITY_MAPPER = AvailabilityMapper.getInstance();
    private static final AvailableMapper AVAILABLE_MAPPER = AvailableMapper.getInstance();
    private static final String AVAILABILITY_TABLE_NAME = "calendar_availability";
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

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.chronos.storage.CalendarAvailabilityStorage#nextCalendarFreeSlotId()
     */
    @Override
    public String nextAvailableId() throws OXException {
        return nextId("calendar_available_sequence");
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.chronos.storage.CalendarAvailabilityStorage#insertCalendarAvailability(com.openexchange.chronos.CalendarAvailability)
     */
    @Override
    public void insertAvailability(Availability availability) throws OXException {
        Connection connection = null;
        int updated = 0;
        try {
            connection = dbProvider.getWriteConnection(context);
            txPolicy.setAutoCommit(connection, false);
            int availabilityCount = insertAvailabilityItem(availability, AVAILABILITY_TABLE_NAME, AVAILABILITY_MAPPER, connection);
            int availableCount = 0;
            for (List<Available> slots : Lists.partition(availability.getAvailable(), INSERT_CHUNK_SIZE)) {
                availableCount += insertAvailabilityItems(slots, AVAILABLE_TABLE_NAME, AVAILABLE_MAPPER, connection);
            }
            txPolicy.commit(connection);
            updated = availabilityCount + availableCount;
            LOG.debug("Inserted {} availability block(s) and {} available block(s) for user {} in context {}.", availabilityCount, availableCount, availability.getCalendarUser(), context.getContextId());
        } catch (SQLException e) {
            throw CalendarExceptionCodes.DB_ERROR.create(e, e.getMessage());
        } finally {
            release(connection, updated);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.chronos.storage.CalendarAvailabilityStorage#insertCalendarAvailabilities(java.util.List)
     */
    @Override
    public void insertAvailabilities(List<Availability> calendarAvailabilities) throws OXException {
        Connection connection = null;
        int updated = 0;
        try {
            connection = dbProvider.getWriteConnection(context);
            txPolicy.setAutoCommit(connection, false);
            for (List<Availability> chunk : Lists.partition(calendarAvailabilities, INSERT_CHUNK_SIZE)) {
                updated += insertAvailabilityItems(chunk, AVAILABILITY_TABLE_NAME, AVAILABILITY_MAPPER, connection);

                // Insert the free slots chunk-wise
                for (Availability availability : chunk) {
                    for (List<Available> slots : Lists.partition(availability.getAvailable(), INSERT_CHUNK_SIZE)) {
                        updated += insertAvailabilityItems(slots, AVAILABLE_TABLE_NAME, AVAILABLE_MAPPER, connection);
                    }
                }
            }
            txPolicy.commit(connection);
        } catch (SQLException e) {
            throw CalendarExceptionCodes.DB_ERROR.create(e, e.getMessage());
        } finally {
            release(connection, updated);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.chronos.storage.CalendarAvailabilityStorage#deleteCalendarAvailability(java.lang.String)
     */
    @Override
    public void deleteAvailability(String availabilityId) throws OXException {
        Connection connection = null;
        int updated = 0;
        try {
            connection = dbProvider.getWriteConnection(context);
            txPolicy.setAutoCommit(connection, false);
            updated = deleteAvailabilityItem(availabilityId, connection);
            txPolicy.commit(connection);
        } catch (SQLException e) {
            throw CalendarExceptionCodes.DB_ERROR.create(e, e.getMessage());
        } finally {
            release(connection, updated);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.chronos.storage.CalendarAvailabilityStorage#insertCalendarFreeSlot(com.openexchange.chronos.CalendarFreeSlot)
     */
    @Override
    public void insertAvailable(List<Available> available) throws OXException {
        Connection connection = null;
        int updated = 0;
        try {
            connection = dbProvider.getWriteConnection(context);
            txPolicy.setAutoCommit(connection, false);
            // Insert the available chunk-wise
            for (List<Available> partition : Lists.partition(available, INSERT_CHUNK_SIZE)) {
                updated += insertAvailabilityItems(partition, AVAILABLE_TABLE_NAME, AVAILABLE_MAPPER, connection);
            }
            txPolicy.commit(connection);
        } catch (SQLException e) {
            throw CalendarExceptionCodes.DB_ERROR.create(e, e.getMessage());
        } finally {
            release(connection, updated);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.chronos.storage.CalendarAvailabilityStorage#loadAvailable(int)
     */
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

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.chronos.storage.CalendarAvailabilityStorage#loadAvailable(java.util.List)
     */
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

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.chronos.storage.CalendarAvailabilityStorage#deleteAvailable(int)
     */
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

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.chronos.storage.CalendarAvailabilityStorage#deleteAvailable(java.lang.String)
     */
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

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.chronos.storage.CalendarAvailabilityStorage#deleteAvailable(int)
     */
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

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.chronos.storage.CalendarAvailabilityStorage#deleteAvailable(java.util.List)
     */
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

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.chronos.storage.CalendarAvailabilityStorage#deleteAvailableByUserId(java.util.List)
     */
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

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.chronos.storage.CalendarAvailabilityStorage#loadUserCalendarAvailability(java.util.List, java.util.Date, java.util.Date)
     */
    @Override
    public List<Availability> loadAvailabilities(List<Integer> userIds) throws OXException {
        Connection connection = null;
        int updated = 0;
        try {
            connection = dbProvider.getReadConnection(context);
            return loadAvailabilities(connection, userIds);
        } catch (SQLException e) {
            throw CalendarExceptionCodes.DB_ERROR.create(e, e.getMessage());
        } finally {
            release(connection, updated);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.chronos.storage.CalendarAvailabilityStorage#loadCalendarAvailabilities(int)
     */
    @Override
    public List<Availability> loadCalendarAvailabilities(int userId) throws OXException {
        Connection connection = null;
        int updated = 0;
        try {
            connection = dbProvider.getReadConnection(context);
            return loadAvailabilities(connection, userId);
        } catch (SQLException e) {
            throw CalendarExceptionCodes.DB_ERROR.create(e, e.getMessage());
        } finally {
            release(connection, updated);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.chronos.storage.CalendarAvailabilityStorage#loadCalendarAvailability(java.lang.String)
     */
    @Override
    public Availability loadAvailability(String availabilityId) throws OXException {
        Connection connection = null;
        int updated = 0;
        try {
            connection = dbProvider.getReadConnection(context);
            return loadAvailability(connection, availabilityId);
        } catch (SQLException e) {
            throw CalendarExceptionCodes.DB_ERROR.create(e, e.getMessage());
        } finally {
            release(connection, updated);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.chronos.storage.CalendarAvailabilityStorage#loadCalendarFreeSlot(java.lang.String, java.lang.String)
     */
    @Override
    public Available loadAvailable(String availabilityId, String availableId) throws OXException {
        Connection connection = null;
        int updated = 0;
        try {
            connection = dbProvider.getReadConnection(context);
            return loadAvailable(connection, availabilityId, availableId);
        } catch (SQLException e) {
            throw CalendarExceptionCodes.DB_ERROR.create(e, e.getMessage());
        } finally {
            release(connection, updated);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.chronos.storage.CalendarAvailabilityStorage#loadCalendarFreeSlots(java.lang.String)
     */
    @Override
    public List<Available> loadAvailable(String availabilityId) throws OXException {
        Connection connection = null;
        int updated = 0;
        try {
            connection = dbProvider.getReadConnection(context);
            return loadAvailable(connection, availabilityId);
        } catch (SQLException e) {
            throw CalendarExceptionCodes.DB_ERROR.create(e, e.getMessage());
        } finally {
            release(connection, updated);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.chronos.storage.CalendarAvailabilityStorage#deleteCalendarAvailabilities(java.util.List)
     */
    @Override
    public void deleteAvailabilities(List<String> availabilityIds) throws OXException {
        Connection connection = null;
        int updated = 0;
        try {
            connection = dbProvider.getWriteConnection(context);
            txPolicy.setAutoCommit(connection, false);
            updated = deleteAvailabilityItems(availabilityIds, connection);
            txPolicy.commit(connection);
        } catch (SQLException e) {
            throw CalendarExceptionCodes.DB_ERROR.create(e, e.getMessage());
        } finally {
            release(connection, updated);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.chronos.storage.CalendarAvailabilityStorage#purgeCalendarAvailabilities(int)
     */
    @Override
    public void purgeAvailabilities(int userId) throws OXException {
        Connection connection = null;
        int updated = 0;
        try {
            connection = dbProvider.getWriteConnection(context);
            txPolicy.setAutoCommit(connection, false);
            updated = purgeAvailabilityItems(connection, userId);
            txPolicy.commit(connection);
        } catch (SQLException e) {
            throw CalendarExceptionCodes.DB_ERROR.create(e, e.getMessage());
        } finally {
            release(connection, updated);
        }
    }

    ///////////////////////////////////////////////////////// HELPERS /////////////////////////////////////////////////////////

    /**
     * Inserts the specified item to the storage
     * 
     * @param item The item to insert
     * @param mapper The database mapper
     * @param tableName The table name
     * @param connection The writeable connection to the storage
     * @return The amount of affected rows
     * @throws OXException if an error is occurred
     * @throws SQLException if an SQL error is occurred
     */
    private <O extends FieldAware, E extends Enum<E>> int insertAvailabilityItem(O item, String tableName, DefaultDbMapper<O, E> mapper, Connection connection) throws OXException, SQLException {
        String sql = SQLStatementBuilder.buildInsertQueryBuilder(tableName, mapper).append(";").toString();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            int parameterIndex = 1;
            stmt.setInt(parameterIndex++, context.getContextId());
            parameterIndex = mapper.setParameters(stmt, parameterIndex, item, mapper.getMappedFields());
            return logExecuteUpdate(stmt);
        }
    }

    /**
     * Inserts multiple items to the storage
     * 
     * @param items The items to insert
     * @param tableName The table name
     * @param mapper The mapper to use
     * @param connection The writeable connection
     * @return The amount of affected rows
     * @throws OXException if an error is occurred
     * @throws SQLException if an SQL error is occurred
     */
    private <O extends FieldAware, E extends Enum<E>> int insertAvailabilityItems(List<O> items, String tableName, DefaultDbMapper<O, E> mapper, Connection connection) throws OXException, SQLException {
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
     * Loads all {@link Availability} blocks for the specified user
     * 
     * @param A read-only {@link Connection} to the storage
     * @param userId The user identifier
     * @return A {@link List} with all the {@link Availability} blocks for the specified user
     * @throws OXException if the items cannot be loaded from the storage or any other error occurs
     * @throws SQLException if an SQL error is occurred
     */
    private Availability loadAvailability(Connection connection, String availabilityId) throws OXException, SQLException {
        AvailabilityField[] mappedFields = AVAILABILITY_MAPPER.getMappedFields();
        StringBuilder sb = SQLStatementBuilder.buildSelectQueryBuilder(AVAILABILITY_TABLE_NAME, AVAILABILITY_MAPPER).append(" AND id=?;");

        int parameterIndex = 1;
        try (PreparedStatement stmt = connection.prepareStatement(sb.toString())) {
            stmt.setInt(parameterIndex++, context.getContextId());
            stmt.setInt(parameterIndex++, Integer.parseInt(availabilityId));
            Availability availability = AVAILABILITY_MAPPER.fromResultSet(logExecuteQuery(stmt), mappedFields);
            availability.setAvailable(loadAvailable(connection, availability.getId()));
            return availability;
        }
    }

    /**
     * Loads all {@link Availability} blocks for the specified user
     * 
     * @param A read-only {@link Connection} to the storage
     * @param userId The user identifier
     * @return A {@link List} with all the {@link Availability} blocks for the specified user
     * @throws OXException if the items cannot be loaded from the storage or any other error occurs
     * @throws SQLException if an SQL error is occurred
     */
    private List<Availability> loadAvailabilities(Connection connection, int userId) throws OXException, SQLException {
        AvailabilityField[] mappedFields = AVAILABILITY_MAPPER.getMappedFields();
        StringBuilder sb = SQLStatementBuilder.buildSelectQueryBuilder(AVAILABILITY_TABLE_NAME, AVAILABILITY_MAPPER).append(" AND user=?;");

        int parameterIndex = 1;
        try (PreparedStatement stmt = connection.prepareStatement(sb.toString())) {
            stmt.setInt(parameterIndex++, context.getContextId());
            stmt.setInt(parameterIndex++, userId);
            List<Availability> availabilities = AVAILABILITY_MAPPER.listFromResultSet(logExecuteQuery(stmt), mappedFields);

            for (Availability availability : availabilities) {
                availability.setAvailable(loadAvailable(connection, availability.getId()));
            }
            return availabilities;
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
                stmt.setInt(parameterIndex++, id);
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
        try (PreparedStatement stmt = connection.prepareStatement(SQLStatementBuilder.buildDeleteQueryBuilder(AVAILABLE_TABLE_NAME).append(" AND id IN (").append(AvailabilityMapper.getParameters(availableUids.size())).append(");").toString())) {
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

    /**
     * Loads the {@link Available} with the specified identifier
     * 
     * @param connection The read-only {@link Connection} to the storage
     * @param availabilityId The calendar availability identifier
     * @param availableId The free slot identifier
     * @return The {@link Available
     * @throws OXException if the items cannot be loaded from the storage or any other error occurs
     * @throws SQLException if an SQL error is occurred
     */
    private Available loadAvailable(Connection connection, String availabilityId, String availableId) throws OXException, SQLException {
        AvailableField[] mappedFields = AVAILABLE_MAPPER.getMappedFields();
        StringBuilder sb = SQLStatementBuilder.buildSelectQueryBuilder(AVAILABLE_TABLE_NAME, AVAILABLE_MAPPER).append(" AND availability=? AND id=?;");

        int parameterIndex = 0;
        try (PreparedStatement stmt = connection.prepareStatement(sb.toString())) {
            stmt.setInt(parameterIndex++, context.getContextId());
            stmt.setInt(parameterIndex++, Integer.parseInt(availabilityId));
            stmt.setInt(parameterIndex++, Integer.parseInt(availableId));

            return AVAILABLE_MAPPER.fromResultSet(logExecuteQuery(stmt), mappedFields);
        }
    }

    /**
     * Loads all {@link Available} items bound to the {@link Availability} with the specified identifier
     * 
     * @param connection The read-only {@link Connection}
     * @param availabilityId The {@link Availability} identifier
     * @return A {@link List} with all {@link Available} items for the specified {@link Availability} identifier
     * @throws OXException if the items cannot be loaded from the storage or any other error occurs
     * @throws SQLException
     */
    private List<Available> loadAvailable(Connection connection, String availabilityId) throws OXException, SQLException {
        AvailableField[] mappedFields = AVAILABLE_MAPPER.getMappedFields();
        StringBuilder sb = SQLStatementBuilder.buildSelectQueryBuilder(AVAILABLE_TABLE_NAME, AVAILABLE_MAPPER).append(" AND availability=?;");

        int parameterIndex = 1;
        try (PreparedStatement stmt = connection.prepareStatement(sb.toString())) {
            stmt.setInt(parameterIndex++, context.getContextId());
            stmt.setInt(parameterIndex++, Integer.parseInt(availabilityId));

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
    private List<Availability> loadAvailabilities(Connection connection, List<Integer> userIds) throws SQLException, OXException {
        // 1) Fetch all calendar availability items for the specified users
        AvailabilityField[] mappedFields = AVAILABILITY_MAPPER.getMappedFields();
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT ").append(AVAILABILITY_MAPPER.getColumns(mappedFields));
        sb.append(" FROM ").append(AVAILABILITY_TABLE_NAME);
        sb.append(" WHERE cid=?");
        sb.append(" AND user IN (").append(AvailabilityMapper.getParameters(userIds.size())).append(");");

        List<Availability> availabilities = null;
        int parameterIndex = 1;
        try (PreparedStatement stmt = connection.prepareStatement(sb.toString())) {
            stmt.setInt(parameterIndex++, context.getContextId());
            for (Integer id : userIds) {
                stmt.setInt(parameterIndex++, id);
            }
            availabilities = AVAILABILITY_MAPPER.listFromResultSet(logExecuteQuery(stmt), mappedFields);
        }

        // 2) Then fetch all free slots of the calendar availability items
        for (Availability ca : availabilities) {
            ca.setAvailable(loadAvailable(ca.getId()));
        }
        return availabilities;
    }

    /**
     * Deletes from the storage the {@link Availability} item with the specified id and all {@link Available}s assigned to it.
     * 
     * @param availabilityId The identifier of the calendar availability item
     * @param connection The writeable connection
     * @return The amount of affected rows
     * @throws SQLException if an error is occurred
     */
    private int deleteAvailabilityItem(String availabilityId, Connection connection) throws SQLException {
        int updated = deleteCalendarFreeSlots(availabilityId, connection);

        int parameterIndex = 1;
        try (PreparedStatement stmt = connection.prepareStatement(SQLStatementBuilder.buildDeleteQueryBuilder(AVAILABILITY_TABLE_NAME).append(" AND id=?;").toString())) {
            stmt.setInt(parameterIndex++, context.getContextId());
            stmt.setInt(parameterIndex++, asInt(availabilityId));
            return logExecuteUpdate(stmt) + updated;
        }
    }

    /**
     * Deletes from the storage the {@link Availability} item with the specified id and all {@link Available}s assigned to it.
     * 
     * @param availabilityId The identifier of the calendar availability item
     * @param connection The writeable connection
     * @return The amount of affected rows
     * @throws SQLException if an error is occurred
     */
    private int deleteAvailabilityItems(List<String> availabilityIds, Connection connection) throws SQLException {
        int affectedRows = 0;
        StringBuilder deleteCABuilder = SQLStatementBuilder.buildDeleteQueryBuilder(AVAILABILITY_TABLE_NAME).append(" AND id IN (");
        for (String availabilityId : availabilityIds) {
            affectedRows += deleteCalendarFreeSlots(availabilityId, connection);
            deleteCABuilder.append("?,");
        }
        deleteCABuilder.setLength(deleteCABuilder.length() - 1);
        deleteCABuilder.append(");");

        int parameterIndex = 1;
        try (PreparedStatement stmt = connection.prepareStatement(deleteCABuilder.toString())) {
            stmt.setInt(parameterIndex++, context.getContextId());
            for (String availabilityId : availabilityIds) {
                stmt.setInt(parameterIndex++, asInt(availabilityId));
            }
            affectedRows += logExecuteUpdate(stmt);
        }
        return affectedRows;
    }

    /**
     * Deletes from the storage all {@link Available}s assigned to the {@link Availability} with the specified id.
     * 
     * @param availabilityId The identifier of the calendar availability item
     * @param connection The writeable connection
     * @return The amount of affected rows
     * @throws SQLException if an error is occurred
     */
    private int deleteCalendarFreeSlots(String availabilityId, Connection connection) throws SQLException {
        int parameterIndex = 1;
        try (PreparedStatement stmt = connection.prepareStatement(SQLStatementBuilder.buildDeleteQueryBuilder(AVAILABLE_TABLE_NAME).append(" AND availability=?;").toString())) {
            stmt.setInt(parameterIndex++, context.getContextId());
            stmt.setInt(parameterIndex++, asInt(availabilityId));
            return logExecuteUpdate(stmt);
        }
    }

    /**
     * Purges all {@link Availability} and {@link Available} items for the specified user
     * 
     * @param connection The writeable {@link Connection} to the storage
     * @param userId The user identifier
     * @return The amount of rows affected
     * @throws SQLException if an SQL error is occurred
     */
    private int purgeAvailabilityItems(Connection connection, int userId) throws SQLException {
        int updated = 0;
        updated += purgeAvailabilityItems(connection, AVAILABLE_TABLE_NAME, userId);
        updated += purgeAvailabilityItems(connection, AVAILABILITY_TABLE_NAME, userId);
        return updated;
    }

    /**
     * Purges all items in the specified table for the specified user
     * 
     * @param connection The writeable {@link Connection} to the storage
     * @param tableName The table name
     * @param userId The user identifier
     * @return The amount of rows affected
     * @throws SQLException if an SQL error is occurred
     */
    private int purgeAvailabilityItems(Connection connection, String tableName, int userId) throws SQLException {
        int parameterIndex = 1;
        try (PreparedStatement stmt = connection.prepareStatement(SQLStatementBuilder.buildDeleteQueryBuilder(tableName).append(" AND user=?;").toString())) {
            stmt.setInt(parameterIndex++, context.getContextId());
            stmt.setInt(parameterIndex++, userId);
            return logExecuteUpdate(stmt);
        }
    }
}
