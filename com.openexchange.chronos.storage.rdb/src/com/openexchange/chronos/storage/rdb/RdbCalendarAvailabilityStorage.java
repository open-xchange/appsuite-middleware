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
import java.util.Date;
import java.util.List;
import com.google.common.collect.Lists;
import com.openexchange.chronos.CalendarAvailability;
import com.openexchange.chronos.CalendarFreeSlot;
import com.openexchange.chronos.FieldAware;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.service.AvailabilityField;
import com.openexchange.chronos.service.FreeSlotField;
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

    private static final CalendarAvailabilityMapper calendarAvailabilityMapper = CalendarAvailabilityMapper.getInstance();
    private static final CalendarFreeSlotMapper freeSlotMapper = CalendarFreeSlotMapper.getInstance();
    private static final String CA_TABLE_NAME = "calendar_availability";
    private static final String CA_FREE_SLOT_NAME = "calendar_free_slot";

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
     * @see com.openexchange.chronos.storage.CalendarAvailabilityStorage#nextCalendarAvailabilityId()
     */
    @Override
    public String nextCalendarAvailabilityId() throws OXException {
        return nextId("calendar_availability_sequence");
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.chronos.storage.CalendarAvailabilityStorage#nextCalendarFreeSlotId()
     */
    @Override
    public String nextCalendarFreeSlotId() throws OXException {
        return nextId("calendar_free_slot_sequence");
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.chronos.storage.CalendarAvailabilityStorage#insertCalendarAvailability(com.openexchange.chronos.CalendarAvailability)
     */
    @Override
    public void insertCalendarAvailability(CalendarAvailability calendarAvailability) throws OXException {
        Connection connection = null;
        int updated = 0;
        try {
            connection = dbProvider.getWriteConnection(context);
            txPolicy.setAutoCommit(connection, false);
            int caAmount = insertCalendarAvailabilityItem(calendarAvailability, CA_TABLE_NAME, calendarAvailabilityMapper, connection);
            int freeSlotsCount = 0;
            for (List<CalendarFreeSlot> slots : Lists.partition(calendarAvailability.getCalendarFreeSlots(), INSERT_CHUNK_SIZE)) {
                freeSlotsCount += insertCalendarAvailabilityItems(slots, CA_FREE_SLOT_NAME, freeSlotMapper, connection);
            }
            txPolicy.commit(connection);
            updated = caAmount + freeSlotsCount;
            LOG.debug("Inserted {} availability block(s) and {} free slot(s) for user {} in context {}.", caAmount, freeSlotsCount, calendarAvailability.getCalendarUser(), context.getContextId());
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
    public void insertCalendarAvailabilities(List<CalendarAvailability> calendarAvailabilities) throws OXException {
        Connection connection = null;
        int updated = 0;
        try {
            connection = dbProvider.getWriteConnection(context);
            txPolicy.setAutoCommit(connection, false);
            for (List<CalendarAvailability> chunk : Lists.partition(calendarAvailabilities, INSERT_CHUNK_SIZE)) {
                updated += insertCalendarAvailabilityItems(chunk, CA_TABLE_NAME, calendarAvailabilityMapper, connection);
            }
            //TODO: insert the free slots as well
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
    public void deleteCalendarAvailability(String calendarAvailabilityId) throws OXException {
        Connection connection = null;
        int updated = 0;
        try {
            connection = dbProvider.getWriteConnection(context);
            txPolicy.setAutoCommit(connection, false);
            updated = deleteCalendarAvailabilityItem(calendarAvailabilityId, connection);
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
     * @see com.openexchange.chronos.storage.CalendarAvailabilityStorage#insertCalendarFreeSlot(java.lang.String, com.openexchange.chronos.CalendarFreeSlot)
     */
    @Override
    public void insertCalendarFreeSlot(String calendarAvailabilityId, CalendarFreeSlot freeSlot) throws OXException {
        Connection connection = null;
        int updated = 0;
        try {
            connection = dbProvider.getWriteConnection(context);
            txPolicy.setAutoCommit(connection, false);
            updated = insertCalendarAvailabilityItem(freeSlot, CA_FREE_SLOT_NAME, freeSlotMapper, connection);
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
     * @see com.openexchange.chronos.storage.CalendarAvailabilityStorage#loadCalendarAvailabilities(int)
     */
    @Override
    public List<CalendarAvailability> loadCalendarAvailabilities(int userId) throws OXException {
        Connection connection = null;
        int updated = 0;
        try {
            connection = dbProvider.getReadConnection(context);
            return loadCalendarAvailabilities(connection, userId);
        } catch (SQLException e) {
            throw CalendarExceptionCodes.DB_ERROR.create(e, e.getMessage());
        } finally {
            release(connection, updated);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.chronos.storage.CalendarAvailabilityStorage#loadCalenarAvailabilityInRange(int, java.util.Date, java.util.Date)
     */
    @Override
    public List<CalendarAvailability> loadCalenarAvailabilityInRange(int userId, Date from, Date until) throws OXException {
        Connection connection = null;
        int updated = 0;
        try {
            connection = dbProvider.getReadConnection(context);
            return loadCalendarAvailabilitiesInRange(connection, userId, from, until);
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
    public CalendarAvailability loadCalendarAvailability(String calendarAvailabilityId) throws OXException {
        Connection connection = null;
        int updated = 0;
        try {
            connection = dbProvider.getReadConnection(context);
            return loadCalendarAvailability(connection, calendarAvailabilityId);
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
    public CalendarFreeSlot loadCalendarFreeSlot(String calendarAvailabilityId, String freeSlotId) throws OXException {
        Connection connection = null;
        int updated = 0;
        try {
            connection = dbProvider.getReadConnection(context);
            return loadCalendarFreeSlot(connection, calendarAvailabilityId, freeSlotId);
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
    public List<CalendarFreeSlot> loadCalendarFreeSlots(String calendarAvailabilityId) throws OXException {
        Connection connection = null;
        int updated = 0;
        try {
            connection = dbProvider.getReadConnection(context);
            return loadCalendarFreeSlots(connection, calendarAvailabilityId);
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
    public void deleteCalendarAvailabilities(List<String> calendarAvailabilityIds) throws OXException {
        Connection connection = null;
        int updated = 0;
        try {
            connection = dbProvider.getWriteConnection(context);
            txPolicy.setAutoCommit(connection, false);
            updated = deleteCalendarAvailabilityItems(calendarAvailabilityIds, connection);
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
    public void purgeCalendarAvailabilities(int userId) throws OXException {
        Connection connection = null;
        int updated = 0;
        try {
            connection = dbProvider.getWriteConnection(context);
            txPolicy.setAutoCommit(connection, false);
            updated = purgeCalendarAvailabilityItems(connection, userId);
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
    private <O extends FieldAware, E extends Enum<E>> int insertCalendarAvailabilityItem(O item, String tableName, DefaultDbMapper<O, E> mapper, Connection connection) throws OXException, SQLException {
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
    private <O extends FieldAware, E extends Enum<E>> int insertCalendarAvailabilityItems(List<O> items, String tableName, DefaultDbMapper<O, E> mapper, Connection connection) throws OXException, SQLException {
        if (items == null || items.size() == 0) {
            return 0;
        }

        E[] mappedFields = mapper.getMappedFields();
        // Prepare the initial query
        StringBuilder sb = SQLStatementBuilder.buildInsertQueryBuilder(tableName, mapper);
        // Prepare for all items
        for (int index = 1; index < items.size(); index++) {
            sb.append(",(?,?,").append(mapper.getParameters(mappedFields)).append(")");

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
     * Loads all {@link CalendarAvailability} blocks for the specified user
     * 
     * @param A read-only {@link Connection} to the storage
     * @param userId The user identifier
     * @return A {@link List} with all the {@link CalendarAvailability} blocks for the specified user
     * @throws OXException if the items cannot be loaded from the storage or any other error occurs
     * @throws SQLException if an SQL error is occurred
     */
    private CalendarAvailability loadCalendarAvailability(Connection connection, String availabilityId) throws OXException, SQLException {
        AvailabilityField[] mappedFields = calendarAvailabilityMapper.getMappedFields();
        StringBuilder sb = SQLStatementBuilder.buildSelectQueryBuilder(CA_TABLE_NAME, calendarAvailabilityMapper).append(" AND id=?;");

        int parameterIndex = 1;
        try (PreparedStatement stmt = connection.prepareStatement(sb.toString())) {
            stmt.setInt(parameterIndex++, context.getContextId());
            stmt.setInt(parameterIndex++, Integer.parseInt(availabilityId));
            CalendarAvailability availability = calendarAvailabilityMapper.fromResultSet(logExecuteQuery(stmt), mappedFields);
            availability.setCalendarFreeSlots(loadCalendarFreeSlots(connection, availability.getId()));
            return availability;
        }
    }

    /**
     * Loads all {@link CalendarAvailability} blocks for the specified user
     * 
     * @param A read-only {@link Connection} to the storage
     * @param userId The user identifier
     * @return A {@link List} with all the {@link CalendarAvailability} blocks for the specified user
     * @throws OXException if the items cannot be loaded from the storage or any other error occurs
     * @throws SQLException if an SQL error is occurred
     */
    private List<CalendarAvailability> loadCalendarAvailabilitiesInRange(Connection connection, int userId, Date from, Date until) throws OXException, SQLException {
        AvailabilityField[] mappedFields = calendarAvailabilityMapper.getMappedFields();
        StringBuilder sb = SQLStatementBuilder.buildSelectQueryBuilder(CA_TABLE_NAME, calendarAvailabilityMapper).append(" AND user=? AND start >= ? AND end <= ?;");

        int parameterIndex = 1;
        try (PreparedStatement stmt = connection.prepareStatement(sb.toString())) {
            stmt.setInt(parameterIndex++, context.getContextId());
            stmt.setInt(parameterIndex++, userId);
            stmt.setLong(parameterIndex++, from.getTime());
            stmt.setLong(parameterIndex++, until.getTime());
            List<CalendarAvailability> availabilities = calendarAvailabilityMapper.listFromResultSet(logExecuteQuery(stmt), mappedFields);

            for (CalendarAvailability availability : availabilities) {
                availability.setCalendarFreeSlots(loadCalendarFreeSlots(connection, availability.getId()));
            }
            return availabilities;
        }
    }

    /**
     * Loads all {@link CalendarAvailability} blocks for the specified user
     * 
     * @param A read-only {@link Connection} to the storage
     * @param userId The user identifier
     * @return A {@link List} with all the {@link CalendarAvailability} blocks for the specified user
     * @throws OXException if the items cannot be loaded from the storage or any other error occurs
     * @throws SQLException if an SQL error is occurred
     */
    private List<CalendarAvailability> loadCalendarAvailabilities(Connection connection, int userId) throws OXException, SQLException {
        AvailabilityField[] mappedFields = calendarAvailabilityMapper.getMappedFields();
        StringBuilder sb = SQLStatementBuilder.buildSelectQueryBuilder(CA_TABLE_NAME, calendarAvailabilityMapper).append(" AND user=?;");

        int parameterIndex = 1;
        try (PreparedStatement stmt = connection.prepareStatement(sb.toString())) {
            stmt.setInt(parameterIndex++, context.getContextId());
            stmt.setInt(parameterIndex++, userId);
            List<CalendarAvailability> availabilities = calendarAvailabilityMapper.listFromResultSet(logExecuteQuery(stmt), mappedFields);

            for (CalendarAvailability availability : availabilities) {
                availability.setCalendarFreeSlots(loadCalendarFreeSlots(connection, availability.getId()));
            }
            return availabilities;
        }
    }

    /**
     * Loads the {@link CalendarFreeSlot} with the specified identifier
     * 
     * @param connection The read-only {@link Connection} to the storage
     * @param availabilityId The calendar availability identifier
     * @param freeSlotId The free slot identifier
     * @return The {@link CalendarFreeSlot
     * @throws OXException if the items cannot be loaded from the storage or any other error occurs
     * @throws SQLException if an SQL error is occurred
     */
    private CalendarFreeSlot loadCalendarFreeSlot(Connection connection, String availabilityId, String freeSlotId) throws OXException, SQLException {
        FreeSlotField[] mappedFields = freeSlotMapper.getMappedFields();
        StringBuilder sb = SQLStatementBuilder.buildSelectQueryBuilder(CA_FREE_SLOT_NAME, freeSlotMapper).append(" AND calendarAvailability=? AND id=?;");

        int parameterIndex = 0;
        try (PreparedStatement stmt = connection.prepareStatement(sb.toString())) {
            stmt.setInt(parameterIndex++, context.getContextId());
            stmt.setInt(parameterIndex++, Integer.parseInt(availabilityId));
            stmt.setInt(parameterIndex++, Integer.parseInt(freeSlotId));

            return freeSlotMapper.fromResultSet(logExecuteQuery(stmt), mappedFields);
        }
    }

    /**
     * Loads all {@link CalendarFreeSlot} items bound to the {@link CalendarAvailability} with the specified identifier
     * 
     * @param connection The read-only {@link Connection}
     * @param availabilityId The {@link CalendarAvailability} identifier
     * @return A {@link List} with all {@link CalendarFreeSlot} items for the specified {@link CalendarAvailability} identifier
     * @throws OXException if the items cannot be loaded from the storage or any other error occurs
     * @throws SQLException
     */
    private List<CalendarFreeSlot> loadCalendarFreeSlots(Connection connection, String availabilityId) throws OXException, SQLException {
        FreeSlotField[] mappedFields = freeSlotMapper.getMappedFields();
        StringBuilder sb = SQLStatementBuilder.buildSelectQueryBuilder(CA_FREE_SLOT_NAME, freeSlotMapper).append(" AND calendarAvailability=?;");

        int parameterIndex = 1;
        try (PreparedStatement stmt = connection.prepareStatement(sb.toString())) {
            stmt.setInt(parameterIndex++, context.getContextId());
            stmt.setInt(parameterIndex++, Integer.parseInt(availabilityId));

            return freeSlotMapper.listFromResultSet(logExecuteQuery(stmt), mappedFields);
        }
    }

    /**
     * Deletes from the storage the {@link CalendarAvailability} item with the specified id and all {@link CalendarFreeSlot}s assigned to it.
     * 
     * @param calendarAvailabilityId The identifier of the calendar availability item
     * @param connection The writeable connection
     * @return The amount of affected rows
     * @throws SQLException if an error is occurred
     */
    private int deleteCalendarAvailabilityItem(String calendarAvailabilityId, Connection connection) throws SQLException {
        int updated = deleteCalendarFreeSlots(calendarAvailabilityId, connection);

        int parameterIndex = 1;
        try (PreparedStatement stmt = connection.prepareStatement(SQLStatementBuilder.buildDeleteQueryBuilder(CA_TABLE_NAME).append(" AND id=?;").toString())) {
            stmt.setInt(parameterIndex++, context.getContextId());
            stmt.setInt(parameterIndex++, asInt(calendarAvailabilityId));
            return logExecuteUpdate(stmt) + updated;
        }
    }

    /**
     * Deletes from the storage the {@link CalendarAvailability} item with the specified id and all {@link CalendarFreeSlot}s assigned to it.
     * 
     * @param calendarAvailabilityId The identifier of the calendar availability item
     * @param connection The writeable connection
     * @return The amount of affected rows
     * @throws SQLException if an error is occurred
     */
    private int deleteCalendarAvailabilityItems(List<String> calendarAvailabilityIds, Connection connection) throws SQLException {
        int affectedRows = 0;
        StringBuilder deleteCABuilder = SQLStatementBuilder.buildDeleteQueryBuilder(CA_TABLE_NAME).append(" AND id IN (");
        for (String calendarAvailabilityId : calendarAvailabilityIds) {
            affectedRows += deleteCalendarFreeSlots(calendarAvailabilityId, connection);
            deleteCABuilder.append("?,");
        }
        deleteCABuilder.setLength(deleteCABuilder.length() - 1);
        deleteCABuilder.append(");");

        int parameterIndex = 1;
        try (PreparedStatement stmt = connection.prepareStatement(deleteCABuilder.toString())) {
            stmt.setInt(parameterIndex++, context.getContextId());
            for (String calendarAvailabilityId : calendarAvailabilityIds) {
                stmt.setInt(parameterIndex++, asInt(calendarAvailabilityId));
            }
            affectedRows += logExecuteUpdate(stmt);
        }
        return affectedRows;
    }

    /**
     * Deletes from the storage all {@link CalendarFreeSlot}s assigned to the {@link CalendarAvailability} with the specified id.
     * 
     * @param calendarAvailabilityId The identifier of the calendar availability item
     * @param connection The writeable connection
     * @return The amount of affected rows
     * @throws SQLException if an error is occurred
     */
    private int deleteCalendarFreeSlots(String calendarAvailabilityId, Connection connection) throws SQLException {
        int parameterIndex = 1;
        try (PreparedStatement stmt = connection.prepareStatement(SQLStatementBuilder.buildDeleteQueryBuilder(CA_FREE_SLOT_NAME).append(" AND calendarAvailability=?;").toString())) {
            stmt.setInt(parameterIndex++, context.getContextId());
            stmt.setInt(parameterIndex++, asInt(calendarAvailabilityId));
            return logExecuteUpdate(stmt);
        }
    }

    /**
     * Purges all {@link CalendarAvailability} and {@link CalendarFreeSlot} items for the specified user
     * 
     * @param connection The writeable {@link Connection} to the storage
     * @param userId The user identifier
     * @return The amount of rows affected
     * @throws SQLException if an SQL error is occurred
     */
    private int purgeCalendarAvailabilityItems(Connection connection, int userId) throws SQLException {
        int updated = 0;
        updated += purgeCalendarAvailabilityItems(connection, CA_FREE_SLOT_NAME, userId);
        updated += purgeCalendarAvailabilityItems(connection, CA_TABLE_NAME, userId);
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
    private int purgeCalendarAvailabilityItems(Connection connection, String tableName, int userId) throws SQLException {
        int parameterIndex = 1;
        try (PreparedStatement stmt = connection.prepareStatement(SQLStatementBuilder.buildDeleteQueryBuilder(tableName).append(" AND user=?;").toString())) {
            stmt.setInt(parameterIndex++, context.getContextId());
            stmt.setInt(parameterIndex++, userId);
            return logExecuteUpdate(stmt);
        }
    }
}
