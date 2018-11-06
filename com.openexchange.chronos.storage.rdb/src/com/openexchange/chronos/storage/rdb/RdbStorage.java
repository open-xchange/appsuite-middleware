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

package com.openexchange.chronos.storage.rdb;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.L;
import java.sql.Connection;
import java.sql.DataTruncation;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.service.SearchOptions;
import com.openexchange.chronos.service.SortOrder;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.database.IncorrectStringSQLException;
import com.openexchange.database.provider.DBProvider;
import com.openexchange.database.provider.DBTransactionPolicy;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.tools.mappings.MappedIncorrectString;
import com.openexchange.groupware.tools.mappings.MappedTruncation;
import com.openexchange.groupware.tools.mappings.database.DbMapper;
import com.openexchange.groupware.tools.mappings.database.DbMapping;
import com.openexchange.groupware.tools.mappings.database.DbMultiMapping;
import com.openexchange.java.Charsets;
import com.openexchange.tools.sql.DBUtils;

/**
 * {@link CalendarStorage}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public abstract class RdbStorage extends CalendarStorageWarnings {

    /** A named logger instance */
    protected static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(RdbStorage.class);

    protected final Context context;
    protected final DBProvider dbProvider;
    protected final DBTransactionPolicy txPolicy;

    /**
     * Initializes a new {@link RdbStorage}.
     *
     * @param context The context
     * @param dbProvider The database provider to use
     * @param txPolicy The transaction policy
     */
    protected RdbStorage(Context context, DBProvider dbProvider, DBTransactionPolicy txPolicy) {
        super();
        this.context = context;
        this.dbProvider = dbProvider;
        this.txPolicy = txPolicy;
    }

    /**
     * Safely releases a write connection obeying the configured transaction policy, rolling back automatically if not committed before.
     *
     * @param connection The write connection to release
     * @param updated The number of actually updated rows to
     */
    protected void release(Connection connection, int updated) throws OXException {
        if (null != connection) {
            try {
                if (false == connection.getAutoCommit()) {
                    txPolicy.rollback(connection);
                }
                txPolicy.setAutoCommit(connection, true);
            } catch (SQLException e) {
                throw asOXException(e);
            } finally {
                if (0 < updated) {
                    dbProvider.releaseWriteConnection(context, connection);
                } else {
                    dbProvider.releaseWriteConnectionAfterReading(context, connection);
                }
            }
        }
    }

    /**
     * Helper method for generating the next sequence identifier based on the specified sequence table
     *
     * @param accountId The account identifier
     * @param sequenceTable The sequence table name
     * @return The next sequential identifier
     * @throws OXException if the next seq id cannot be generated
     */
    protected String nextId(String sequenceTable) throws OXException {
        String value = null;
        int updated = 0;
        Connection connection = null;
        try {
            connection = dbProvider.getWriteConnection(context);
            txPolicy.setAutoCommit(connection, false);
            value = asString(nextId(connection, sequenceTable));
            updated = 1;
            txPolicy.commit(connection);
        } catch (SQLException e) {
            throw asOXException(e);
        } finally {
            release(connection, updated);
        }
        return value;
    }

    /**
     * Generates the next sequential identifier in the context based on the given sequence table.
     *
     * @param connection A connection within an active transaction
     * @param sequenceTable The sequence number to use for identifier generation
     * @return The next sequential identifier
     */
    //TODO: consolidate somehow with the duplicate method....
    protected int nextId(Connection connection, String sequenceTable) throws SQLException {
        if (connection.getAutoCommit()) {
            throw new SQLException("Generating unique identifier is threadsafe if and only if it is executed in a transaction.");
        }
        try (PreparedStatement stmt = connection.prepareStatement("UPDATE " + sequenceTable + " SET id=LAST_INSERT_ID(id+1) WHERE cid=?;")) {
            stmt.setInt(1, context.getContextId());
            if (0 == logExecuteUpdate(stmt)) {
                try (PreparedStatement stmt2 = connection.prepareStatement("INSERT INTO " + sequenceTable + " (cid,id) VALUES (?,0);")) {
                    stmt2.setInt(1, context.getContextId());
                    logExecuteUpdate(stmt2);
                }
                if (0 == logExecuteUpdate(stmt)) {
                    throw new SQLException("Unable to initialize sequence table \"" + sequenceTable + "\" in context " + context.getContextId());
                }
            }
        }
        try (PreparedStatement stmt = connection.prepareStatement("SELECT LAST_INSERT_ID();")) {
            try (ResultSet resultSet = logExecuteQuery(stmt)) {
                if (resultSet.next()) {
                    return resultSet.getInt(1);
                }
            }
        }
        throw new SQLException("Unable to generate next sequential identifier via table \"" + sequenceTable + "\" in context " + context.getContextId());
    }

    /**
     * Generates the next sequential identifier in the context for the supplied calendar account, based on the given sequence table.
     *
     * @param connection A connection within an active transaction
     * @param account The identifier of the account to generate the next identifier for
     * @param sequenceTable The sequence number to use for identifier generation
     * @return The next sequential identifier
     */
    protected int nextId(Connection connection, int account, String sequenceTable) throws SQLException {
        if (connection.getAutoCommit()) {
            throw new SQLException("Generating unique identifier is threadsafe if and only if it is executed in a transaction.");
        }
        try (PreparedStatement stmt = connection.prepareStatement("UPDATE " + sequenceTable + " SET id=LAST_INSERT_ID(id+1) WHERE cid=? AND account=?;")) {
            stmt.setInt(1, context.getContextId());
            stmt.setInt(2, account);
            if (0 == logExecuteUpdate(stmt)) {
                try (PreparedStatement stmt2 = connection.prepareStatement("INSERT INTO " + sequenceTable + " (cid,account,id) VALUES (?,?,0);")) {
                    stmt2.setInt(1, context.getContextId());
                    stmt2.setInt(2, account);
                    logExecuteUpdate(stmt2);
                }
                if (0 == logExecuteUpdate(stmt)) {
                    throw new SQLException("Unable to initialize sequence table \"" + sequenceTable + "\" for account " + account + " in context " + context.getContextId());
                }
            }
        }
        try (PreparedStatement stmt = connection.prepareStatement("SELECT LAST_INSERT_ID();")) {
            try (ResultSet resultSet = logExecuteQuery(stmt)) {
                if (resultSet.next()) {
                    return resultSet.getInt(1);
                }
            }
        }
        throw new SQLException("Unable to generate next sequential identifier via table \"" + sequenceTable + "\" for account " + account + " in context " + context.getContextId());
    }

    /**
     * Logs & executes a prepared statement's SQL query.
     *
     * @param stmt The statement to execute the SQL query from
     * @return The result set
     */
    protected static ResultSet logExecuteQuery(PreparedStatement stmt) throws SQLException {
        if (false == LOG.isDebugEnabled()) {
            return stmt.executeQuery();
        } else {
            String statementString = String.valueOf(stmt);
            long start = System.currentTimeMillis();
            ResultSet resultSet = stmt.executeQuery();
            LOG.debug("executeQuery: {} - {} ms elapsed.", statementString, L(System.currentTimeMillis() - start));
            return resultSet;
        }
    }

    /**
     * Logs & executes a prepared statement's SQL update.
     *
     * @param stmt The statement to execute the SQL update from
     * @return The number of affected rows
     */
    protected static int logExecuteUpdate(PreparedStatement stmt) throws SQLException {
        if (false == LOG.isDebugEnabled()) {
            return stmt.executeUpdate();
        } else {
            String statementString = String.valueOf(stmt);
            long start = System.currentTimeMillis();
            int rowCount = stmt.executeUpdate();
            LOG.debug("executeUpdate: {} - {} rows affected, {} ms elapsed.", statementString, I(rowCount), L(System.currentTimeMillis() - start));
            return rowCount;
        }
    }

    /**
     * Gets an {@link OXException} appropriate for the supplied {@link SQLException}.
     * <p/>
     * For <i>write</i>-operations, {@link RdbStorage#asOXException(SQLException, DbMapper, E, String)} should be called to include more data.
     *
     * @param e The SQL exception to get the OX exception for
     * @return The OX exception
     */
    protected static OXException asOXException(SQLException e) {
        if (DBUtils.isTransactionRollbackException(e)) {
            return CalendarExceptionCodes.DB_ERROR_TRY_AGAIN.create(e, e.getMessage());
        }
        return CalendarExceptionCodes.DB_ERROR.create(e, e.getMessage());
    }

    /**
     * Gets an {@link OXException} appropriate for the supplied {@link SQLException} that occurred during a write-operation.
     *
     * @param e The SQL exception to get the OX exception for
     * @param mapper The corresponding db mapper, or <code>null</code> if not available
     * @param object The corresponding object being written, or <code>null</code> if not available
     * @param connection The current database connection, or <code>null</code> if not available
     * @param table The name of the targeted database table, or <code>null</code> if not available
     * @return The OX exception
     */
    protected <O, E extends Enum<E>> OXException asOXException(SQLException e, DbMapper<O, E> mapper, O object, Connection connection, String table) {
        return asOXException(e, mapper, null == object ? null : Collections.singleton(object), connection, table);
    }

    /**
     * Gets an {@link OXException} appropriate for the supplied {@link SQLException} that occurred during a write-operation.
     *
     * @param e The SQL exception to get the OX exception for
     * @param mapper The corresponding db mapper, or <code>null</code> if not available
     * @param objects The corresponding objects being written, or <code>null</code> if not available
     * @param connection The current database connection, or <code>null</code> if not available
     * @param table The name of the targeted database table, or <code>null</code> if not available
     * @return The OX exception
     */
    protected <O, E extends Enum<E>> OXException asOXException(SQLException e, DbMapper<O, E> mapper, Collection<O> objects, Connection connection, String table) {
        if (IncorrectStringSQLException.class.isInstance(e)) {
            IncorrectStringSQLException incorrectStringException = (IncorrectStringSQLException) e;
            /*
             * derive mapped field & decorate with corresponding "problematic" if available
             */
            MappedIncorrectString<O> mappedIncorrectString = getMappedIncorrectString(incorrectStringException, mapper);
            if (null != mappedIncorrectString) {
                OXException oxException = CalendarExceptionCodes.INCORRECT_STRING.create(e, incorrectStringException.getIncorrectString(), mappedIncorrectString.getReadableName(), incorrectStringException.getColumn());
                oxException.addProblematic(mappedIncorrectString);
                return oxException;
            }
            /*
             * wrap in default incorrect string exception, otherwise
             */
            return CalendarExceptionCodes.INCORRECT_STRING.create(e, incorrectStringException.getIncorrectString(), incorrectStringException.getColumn(), incorrectStringException.getColumn());
        }
        if (DataTruncation.class.isInstance(e)) {
            /*
             * extract additional information if possible & decorate with corresponding "problematic" if available
             */
            List<MappedTruncation<O>> mappedTruncations = getMappedTruncations((DataTruncation) e, mapper, objects, connection, table);
            if (null != mappedTruncations && 0 < mappedTruncations.size()) {
                MappedTruncation<O> firstTruncation = mappedTruncations.get(0);
                OXException oxException = CalendarExceptionCodes.DATA_TRUNCATION.create(firstTruncation.getReadableName(), I(firstTruncation.getMaxSize()), I(firstTruncation.getLength()));
                for (MappedTruncation<O> mappedTruncation : mappedTruncations) {
                    oxException.addProblematic(mappedTruncation);
                }
                return oxException;
            }
        }
        return asOXException(e);
    }

    /**
     * Parses the supplied identifier to its numerical integer value.
     *
     * @param id The identifier to get the integer value for
     * @return The integer value of the supplied identifier
     * @throws NumberFormatException
     */
    protected static int asInt(String id) {
        return Integer.parseInt(id);
    }

    /**
     * Parses the supplied identifiers to their numerical integer values.
     *
     * @param ids The identifiers to get the integer values for
     * @return The integer values of the supplied identifiers
     * @throws NumberFormatException
     */
    protected static int[] asInt(String[] ids) {
        int[] numericalIds = new int[ids.length];
        for (int i = 0; i < ids.length; i++) {
            numericalIds[i] = Integer.parseInt(ids[i]);
        }
        return numericalIds;
    }

    /**
     * Parses the supplied identifier to its numerical integer value.
     *
     * @param id The identifier to get the integer value for
     * @return The integer value of the supplied identifier
     * @throws NumberFormatException
     */
    protected static Integer asInteger(String id) {
        return null != id ? Integer.valueOf(id) : null;
    }

    /**
     * Gets the string representation of the supplied numerical identifier.
     *
     * @param id The identifier to get the string representation for
     * @return The string representation of the supplied numerical identifier
     */
    protected static String asString(int id) {
        return String.valueOf(id);
    }

    /**
     * Gets the string representation of the supplied numerical identifier.
     *
     * @param id The identifier to get the string representation for
     * @return The string representation of the supplied numerical identifier
     */
    protected static String asString(Integer id) {
        return null == id ? null : id.toString();
    }

    private <O, E extends Enum<E>> MappedIncorrectString<O> getMappedIncorrectString(IncorrectStringSQLException e, DbMapper<O, E> mapper) {
        if (null != mapper && null != e) {
            E field = mapper.getMappedField(e.getColumn());
            if (null != field) {
                DbMapping<? extends Object, O> mapping = mapper.opt(field);
                if (null != mapping) {
                    return new MappedIncorrectString<O>(mapping, e.getIncorrectString(), getReadableName(field, mapping));
                }
            }
        }
        return null;
    }

    private <O, E extends Enum<E>> List<MappedTruncation<O>> getMappedTruncations(DataTruncation e, DbMapper<O, E> mapper, Collection<O> objects, Connection connection, String table) {
        if (null != mapper && null != objects && null != table && null != e) {
            String[] truncatedColumns = DBUtils.parseTruncatedFields(e);
            if (null == truncatedColumns || 0 == truncatedColumns.length) {
                return null;
            }
            List<MappedTruncation<O>> mappedTruncations = new ArrayList<MappedTruncation<O>>(truncatedColumns.length);
            for (O object : objects) {
                for (String column : truncatedColumns) {
                    MappedTruncation<O> mappedTruncation = getMappedTruncation(mapper, object, connection, table, column);
                    if (null != mappedTruncation) {
                        mappedTruncations.add(mappedTruncation);
                    }
                }
            }
            return 0 < mappedTruncations.size() ? mappedTruncations : null;
        }
        return null;
    }

    private <O, E extends Enum<E>> MappedTruncation<O> getMappedTruncation(DbMapper<O, E> mapper, O object, Connection connection, String table, String column) {
        E field = mapper.getMappedField(column);
        if (null != field) {
            DbMapping<? extends Object, O> mapping = mapper.opt(field);
            if (null != mapping) {
                Object value = mapping.get(object);
                int maximumSize = getMaximumSize(connection, table, column);
                int actualSize = null != value && String.class.isInstance(value) ? Charsets.getBytes((String) value, Charsets.UTF_8).length : 0;
                return new MappedTruncation<O>(mapping, maximumSize, actualSize, getReadableName(field, mapping));
            }
        }
        return null;
    }

    private static int getMaximumSize(Connection connection, String table, String columnLabel) {
        try {
            return DBUtils.getColumnSize(connection, table, columnLabel);
        } catch (SQLException e) {
            LOG.warn("Error determining maximum size of column {} in table {}", columnLabel, table, e);
            return -1;
        }
    }

    /**
     * Gets the SQL representation of the supplied sort options, optionally prefixing any used column identifiers.
     *
     * @param mapper The underlying mapper
     * @param sortOptions The sort options to get the SQL representation for
     * @param prefix The prefix to use, or <code>null</code> if not needed
     * @return The <code>ORDER BY ... LIMIT ...</code> clause, or an empty string if no sort options were specified
     */
    protected static String getSortOptions(DbMapper<Event, EventField> mapper, SearchOptions sortOptions, String prefix) throws OXException {
        if (null == sortOptions || SearchOptions.EMPTY.equals(sortOptions)) {
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder(32);
        SortOrder[] sortOrders = sortOptions.getSortOrders();
        if (null != sortOrders && 0 < sortOrders.length) {
            stringBuilder.append(" ORDER BY ").append(getColumnLabel(mapper, sortOrders[0].getBy(), prefix)).append(sortOrders[0].isDescending() ? " DESC" : " ASC");
            for (int i = 1; i < sortOrders.length; i++) {
                stringBuilder.append(", ").append(getColumnLabel(mapper, sortOrders[i].getBy(), prefix)).append(sortOrders[i].isDescending() ? " DESC" : " ASC");
            }
        }
        if (0 < sortOptions.getLimit()) {
            stringBuilder.append(" LIMIT ");
            if (0 < sortOptions.getOffset()) {
                stringBuilder.append(sortOptions.getOffset()).append(", ");
            }
            stringBuilder.append(sortOptions.getLimit());
        }
        return stringBuilder.toString();
    }

    private static <O, E extends Enum<E>> String getColumnLabel(DbMapper<O, E> mapper, E field, String prefix) throws OXException {
        DbMapping<? extends Object, O> mapping = mapper.get(field);
        if (DbMultiMapping.class.isInstance(mapping)) {
            DbMultiMapping<?, O> multiMapping = (DbMultiMapping<?, O>) mapping;
            return null != prefix ? multiMapping.getColumnLabels(prefix)[0] : multiMapping.getColumnLabels()[0];
        }
        return null != prefix ? mapping.getColumnLabel(prefix) : mapping.getColumnLabel();
    }

}
