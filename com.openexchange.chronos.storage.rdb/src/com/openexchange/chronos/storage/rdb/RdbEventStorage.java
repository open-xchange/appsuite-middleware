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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.Period;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.compat.Appointment2Event;
import com.openexchange.chronos.compat.Event2Appointment;
import com.openexchange.chronos.compat.Recurrence;
import com.openexchange.chronos.compat.SeriesPattern;
import com.openexchange.chronos.service.EntityResolver;
import com.openexchange.chronos.service.SortOptions;
import com.openexchange.chronos.service.SortOrder;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.chronos.storage.EventStorage;
import com.openexchange.chronos.storage.rdb.exception.EventExceptionCode;
import com.openexchange.database.provider.DBProvider;
import com.openexchange.database.provider.DBTransactionPolicy;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.impl.IDGenerator;
import com.openexchange.groupware.tools.mappings.database.DbMapping;
import com.openexchange.search.SearchTerm;

/**
 * {@link CalendarStorage}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class RdbEventStorage extends RdbStorage implements EventStorage {

    /**
     * Initializes a new {@link RdbEventStorage}.
     *
     * @param context The context
     * @param entityResolver The entity resolver to use
     * @param dbProvider The database provider to use
     * @param The transaction policy
     */
    public RdbEventStorage(Context context, EntityResolver entityResolver, DBProvider dbProvider, DBTransactionPolicy txPolicy) {
        super(context, entityResolver, dbProvider, txPolicy);
    }

    @Override
    public List<Event> searchEvents(SearchTerm<?> searchTerm, SortOptions sortOptions, EventField[] fields) throws OXException {
        Connection connection = null;
        try {
            connection = dbProvider.getReadConnection(context);
            return selectEvents(connection, false, context.getContextId(), searchTerm, sortOptions, fields);
        } catch (SQLException e) {
            throw EventExceptionCode.MYSQL.create(e);
        } finally {
            dbProvider.releaseReadConnection(context, connection);
        }
    }

    @Override
    public List<Event> searchDeletedEvents(SearchTerm<?> searchTerm, SortOptions sortOptions, EventField[] fields) throws OXException {
        Connection connection = null;
        try {
            connection = dbProvider.getReadConnection(context);
            return selectEvents(connection, true, context.getContextId(), searchTerm, sortOptions, fields);
        } catch (SQLException e) {
            throw EventExceptionCode.MYSQL.create(e);
        } finally {
            dbProvider.releaseReadConnection(context, connection);
        }
    }

    @Override
    public Event loadEvent(int objectID, EventField[] fields) throws OXException {
        Connection connection = null;
        try {
            connection = dbProvider.getReadConnection(context);
            return selectEvent(connection, context.getContextId(), objectID, fields);
        } catch (SQLException e) {
            throw EventExceptionCode.MYSQL.create(e);
        } finally {
            dbProvider.releaseReadConnection(context, connection);
        }
    }

    @Override
    public int nextObjectID() throws OXException {
        Connection connection = null;
        try {
            connection = dbProvider.getWriteConnection(context);
            if (connection.getAutoCommit()) {
                throw new SQLException("Generating unique identifier is threadsafe if and only if it is executed in a transaction.");
            }
            return IDGenerator.getId(context, Types.APPOINTMENT, connection);
        } catch (SQLException e) {
            throw EventExceptionCode.MYSQL.create(e);
        } finally {
            release(connection, 1);
        }
    }

    @Override
    public void insertEvent(Event event) throws OXException {
        int updated = 0;
        Connection connection = null;
        try {
            connection = dbProvider.getWriteConnection(context);
            txPolicy.setAutoCommit(connection, false);
            updated = insertEvent(connection, context.getContextId(), event);
            txPolicy.commit(connection);
        } catch (SQLException e) {
            throw EventExceptionCode.MYSQL.create(e);
        } finally {
            release(connection, updated);
        }
    }

    @Override
    public void updateEvent(Event event) throws OXException {
        int updated = 0;
        Connection connection = null;
        try {
            connection = dbProvider.getWriteConnection(context);
            txPolicy.setAutoCommit(connection, false);
            updated = updateEvent(connection, context.getContextId(), event.getId(), event);
            txPolicy.commit(connection);
        } catch (SQLException e) {
            throw EventExceptionCode.MYSQL.create(e);
        } finally {
            release(connection, updated);
        }
    }

    @Override
    public void insertTombstoneEvent(Event event) throws OXException {
        int updated = 0;
        Connection connection = null;
        try {
            connection = dbProvider.getWriteConnection(context);
            txPolicy.setAutoCommit(connection, false);
            updated = insertTombstoneEvent(connection, context.getContextId(), event);
            txPolicy.commit(connection);
        } catch (SQLException e) {
            throw EventExceptionCode.MYSQL.create(e);
        } finally {
            release(connection, updated);
        }
    }

    @Override
    public void deleteEvent(int objectID) throws OXException {
        int updated = 0;
        Connection connection = null;
        try {
            connection = dbProvider.getWriteConnection(context);
            txPolicy.setAutoCommit(connection, false);
            updated = deleteEvent(connection, context.getContextId(), objectID);
            txPolicy.commit(connection);
        } catch (SQLException e) {
            throw EventExceptionCode.MYSQL.create(e);
        } finally {
            release(connection, updated);
        }
    }

    private static int deleteEvent(Connection connection, int contextID, int objectID) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement("DELETE FROM prg_dates WHERE cid=? AND intfield01=?;")) {
            stmt.setInt(1, contextID);
            stmt.setInt(2, objectID);
            return logExecuteUpdate(stmt);
        }
    }
    private static int insertTombstoneEvent(Connection connection, int contextID, Event event) throws SQLException, OXException {
        return insertOrReplaceEvent(connection, "del_dates", true, contextID, event);
    }

    private static int insertEvent(Connection connection, int contextID, Event event) throws SQLException, OXException {
        return insertOrReplaceEvent(connection, "prg_dates", false, contextID, event);
    }

    private static int insertOrReplaceEvent(Connection connection, String tableName, boolean replace, int contextID, Event event) throws SQLException, OXException {
        EventField[] mappedFields = EventMapper.getInstance().getMappedFields();
        String sql = new StringBuilder()
            .append(replace ? "REPLACE" : "INSERT").append(" INTO ").append(tableName).append(' ')
            .append("(cid,").append(EventMapper.getInstance().getColumns(mappedFields)).append(") ")
            .append("VALUES (?,").append(EventMapper.getInstance().getParameters(mappedFields)).append(");")
        .toString();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            int parameterIndex = 1;
            stmt.setInt(parameterIndex++, contextID);
            EventMapper.getInstance().setParameters(stmt, parameterIndex, adjustPriorSave(event), mappedFields);
            return logExecuteUpdate(stmt);
        }
    }

    private static int updateEvent(Connection connection, int contextID, int objectID, Event event) throws SQLException, OXException {
        EventField[] assignedfields = EventMapper.getInstance().getAssignedFields(event);
        String sql = new StringBuilder()
            .append("UPDATE prg_dates SET ").append(EventMapper.getInstance().getAssignments(assignedfields)).append(' ')
            .append("WHERE cid=? AND intfield01=?;")
        .toString();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            int parameterIndex = 1;
            parameterIndex = EventMapper.getInstance().setParameters(stmt, parameterIndex, adjustPriorSave(event), assignedfields);
            stmt.setInt(parameterIndex++, contextID);
            stmt.setInt(parameterIndex++, objectID);
            return logExecuteUpdate(stmt);
        }
    }

    private static Event selectEvent(Connection connection, int contextID, int objectID, EventField[] fields) throws SQLException, OXException {
        EventField[] mappedFields = EventMapper.getInstance().getMappedFields(fields);
        String sql = new StringBuilder()
            .append("SELECT ").append(EventMapper.getInstance().getColumns(mappedFields)).append(" FROM prg_dates ")
            .append("WHERE cid=? AND intfield01=?;")
        .toString();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, contextID);
            stmt.setInt(2, objectID);
            ResultSet resultSet = logExecuteQuery(stmt);
            if (resultSet.next()) {
                return readEvent(resultSet, mappedFields, null);
            }
        }
        return null;
    }

    private static Event readEvent(ResultSet resultSet, EventField[] fields, String columnLabelPrefix) throws SQLException, OXException {
        return adjustAfterLoad(EventMapper.getInstance().fromResultSet(resultSet, fields, columnLabelPrefix));
    }

    private static List<Event> selectEvents(Connection connection, boolean deleted, int contextID, SearchTerm<?> searchTerm, SortOptions sortOptions, EventField[] fields) throws SQLException, OXException {
        EventField[] mappedFields = EventMapper.getInstance().getMappedFields(fields);
        SearchTermAdapter adapter = new SearchTermAdapter(searchTerm, null, "d.", "m.", "e.");
        StringBuilder stringBuilder = new StringBuilder().append("SELECT DISTINCT ").append(EventMapper.getInstance().getColumns(mappedFields, "d.")).append(' ')
            .append("FROM ").append(deleted ? "del_dates" : "prg_dates").append(" AS d ");
        if (adapter.usesInternalAttendees()) {
            stringBuilder.append("LEFT JOIN ").append(deleted ? "del_dates_members" : "prg_dates_members").append(" AS m ")
                .append("ON d.cid=m.cid AND d.intfield01=m.object_id ");
        }
        if (adapter.usesExternalAttendees()) {
            stringBuilder.append("LEFT JOIN ").append(deleted ? "deldateexternal" : "dateexternal").append(" AS e ")
                .append("ON d.cid=e.cid AND d.intfield01=e.objectId ");
        }
        stringBuilder.append("WHERE d.cid=? AND ").append(adapter.getClause()).append(getSortOptions(sortOptions, "d.")).append(';');
        List<Event> events = new ArrayList<Event>();
        try (PreparedStatement stmt = connection.prepareStatement(stringBuilder.toString())) {
            stmt.setInt(1, contextID);
            adapter.setParameters(stmt, 2);
            ResultSet resultSet = logExecuteQuery(stmt);
            while (resultSet.next()) {
                events.add(readEvent(resultSet, mappedFields, "d."));
            }
        }
        return events;
    }

    /**
     * Gets the SQL representation of the supplied sort options, optionally prefixing any used column identifiers.
     *
     * @param sortOptions The sort options to get the SQL representation for
     * @param prefix The prefix to use, or <code>null</code> if not needed
     * @return The <code>ORDER BY ... LIMIT ...</code> clause, or an empty string if no sort options were specified
     */
    private static String getSortOptions(SortOptions sortOptions, String prefix) throws OXException {
        if (null == sortOptions || SortOptions.EMPTY.equals(sortOptions)) {
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder();
        SortOrder[] sortOrders = sortOptions.getSortOrders();
        if (null != sortOrders && 0 < sortOrders.length) {
            stringBuilder.append(" ORDER BY ").append(getColumnLabel(sortOrders[0].getBy(), prefix)).append(sortOrders[0].isDescending() ? " DESC" : " ASC");
            for (int i = 1; i < sortOrders.length; i++) {
                stringBuilder.append(", ").append(getColumnLabel(sortOrders[i].getBy(), prefix)).append(sortOrders[i].isDescending() ? " DESC" : " ASC");
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

    private static String getColumnLabel(EventField field, String prefix) throws OXException {
        DbMapping<? extends Object, Event> mapping = EventMapper.getInstance().get(field);
        return null != prefix ? mapping.getColumnLabel(prefix) : mapping.getColumnLabel();
    }

    /**
     * Adjusts certain properties of an event after loading it from the database.
     * <p/>
     * <b>Note:</b> This method requires that the properties {@link EventField#ALL_DAY}, {@link EventField#RECURRENCE_RULE},
     * {@link EventField#START_TIMEZONE}, {@link EventField#START_DATE} and {@link EventField#END_DATE} were loaded.
     *
     * @param event The event to adjust
     * @return The (possibly adjusted) event reference
     */
    private static Event adjustAfterLoad(Event event) throws OXException {
        if (event.containsRecurrenceRule() && null != event.getRecurrenceRule()) {
            /*
             * extract series pattern and "absolute duration" / "recurrence calculator" field
             */
            String value = event.getRecurrenceRule();
            int idx = value.indexOf('~');
            int absoluteDuration = Integer.parseInt(value.substring(0, idx));
            String databasePattern = value.substring(idx + 1);
            String timeZone = null != event.getStartTimeZone() ? event.getStartTimeZone() : "UTC";
            boolean allDay = event.isAllDay();
            /*
             * convert legacy series pattern into proper recurrence rule
             */
            SeriesPattern seriesPattern = new SeriesPattern(databasePattern, timeZone, allDay);
            String recurrenceRule = Recurrence.getRecurrenceRule(seriesPattern);
            if (CalendarUtils.isSeriesMaster(event)) {
                /*
                 * apply recurrence rule & adjust the recurrence master's actual start- and enddate
                 */
                event.setRecurrenceRule(recurrenceRule);
                Period seriesPeriod = new Period(event);
                Period masterPeriod = Recurrence.getRecurrenceMasterPeriod(seriesPeriod, absoluteDuration);
                event.setStartDate(masterPeriod.getStartDate());
                event.setEndDate(masterPeriod.getEndDate());
                /*
                 * transform legacy "recurrence date positions" for exceptions to recurrence ids
                 */
                if (event.containsDeleteExceptionDates() && null != event.getDeleteExceptionDates()) {
                    event.setDeleteExceptionDates(Appointment2Event.getRecurrenceIDs(recurrenceRule, new Date(seriesPattern.getSeriesStart().longValue()), seriesPattern.getTimeZone(), allDay, event.getDeleteExceptionDates()));
                }
                if (event.containsChangeExceptionDates() && null != event.getChangeExceptionDates()) {
                    event.setChangeExceptionDates(Appointment2Event.getRecurrenceIDs(recurrenceRule, new Date(seriesPattern.getSeriesStart().longValue()), seriesPattern.getTimeZone(), allDay, event.getChangeExceptionDates()));
                }
            } else if (CalendarUtils.isSeriesException(event)) {
                /*
                 * drop recurrence information for change exceptions
                 */
                //                event.removeRecurrenceRule(); // better keep?
                /*
                 * transform exception's legacy "recurrence date position" to recurrence ids
                 */
                if (event.containsRecurrenceId() && null != event.getRecurrenceId()) {
                    event.setRecurrenceId(Appointment2Event.getRecurrenceID(recurrenceRule, new Date(seriesPattern.getSeriesStart().longValue()), seriesPattern.getTimeZone(), allDay, event.getRecurrenceId()));
                }
            }
        }

        return event;
    }

    /**
     * Adjusts certain properties of an event prior saving it in the database.
     *
     * @param event The event to adjust
     * @return The (possibly adjusted) event reference
     * @throws OXException
     */
    private static Event adjustPriorSave(Event event) throws OXException {
        /*
         * convert recurrence rule extract series pattern and "absolute duration" / "recurrence calculator" field
         */
        if (event.containsRecurrenceRule() && null != event.getRecurrenceRule()) {
            String recurrenceRule = event.getRecurrenceRule();
            long absoluteDuration = new Period(event).getTotalDays();
            TimeZone timeZone = event.containsStartTimeZone() && null != event.getStartTimeZone() ? TimeZone.getTimeZone(event.getStartTimeZone()) : null;
            Calendar calendar = null != timeZone ? GregorianCalendar.getInstance(timeZone) : GregorianCalendar.getInstance();
            calendar.setTime(event.getStartDate());
            SeriesPattern seriesPattern = Recurrence.generatePattern(event.getRecurrenceRule(), calendar);
            String value = absoluteDuration + "~" + seriesPattern.getDatabasePattern();
            event.setRecurrenceRule(value);
            /*
             * expand recurrence master start- and enddate to cover the whole series period
             */
            if (event.getId() == event.getSeriesId()) {
                Period masterPeriod = new Period(event);
                TimeZone tz = TimeZone.getTimeZone(event.isAllDay() || null == event.getStartTimeZone() ? "UTC" : event.getStartTimeZone());
                Period seriesPeriod = Recurrence.getImplicitSeriesPeriod(masterPeriod, tz, recurrenceRule);
                event.setStartDate(seriesPeriod.getStartDate());
                event.setEndDate(seriesPeriod.getEndDate());
            }
        }
        /*
         * truncate milliseconds from creation date to avoid bad rounding in MySQL versions >= 5.6.4.
         * See: http://dev.mysql.com/doc/refman/5.6/en/fractional-seconds.html
         * See: com.openexchange.sql.tools.SQLTools.toTimestamp(Date)
         */
        if (event.containsCreated() && null != event.getCreated()) {
            event.setCreated(new Date((event.getCreated().getTime() / 1000) * 1000));
        }
        /*
         * transform recurrence ids to legacy "recurrence date positions" (UTC dates with truncated time fraction)
         */
        if (event.containsRecurrenceId() && null != event.getRecurrenceId()) {
            event.setRecurrenceId(Event2Appointment.getRecurrenceDatePosition(event.getRecurrenceId()));
        }
        if (event.containsDeleteExceptionDates()) {
            event.setDeleteExceptionDates(Event2Appointment.getRecurrenceDatePositions(event.getDeleteExceptionDates()));
        }
        if (event.containsChangeExceptionDates()) {
            event.setChangeExceptionDates(Event2Appointment.getRecurrenceDatePositions(event.getChangeExceptionDates()));
        }

        return event;
    }

}
