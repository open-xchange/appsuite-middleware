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

import static com.openexchange.chronos.common.CalendarUtils.isSeriesException;
import static com.openexchange.chronos.common.CalendarUtils.isSeriesMaster;
import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.I2i;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.CalendarUserType;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.Period;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.common.DefaultRecurrenceData;
import com.openexchange.chronos.compat.Appointment2Event;
import com.openexchange.chronos.compat.Event2Appointment;
import com.openexchange.chronos.compat.Recurrence;
import com.openexchange.chronos.compat.SeriesPattern;
import com.openexchange.chronos.service.EntityResolver;
import com.openexchange.chronos.service.RecurrenceData;
import com.openexchange.chronos.service.SortOptions;
import com.openexchange.chronos.service.SortOrder;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.chronos.storage.EventStorage;
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
            throw asOXException(e);
        } finally {
            dbProvider.releaseReadConnection(context, connection);
        }
    }

    @Override
    public List<Event> searchOverlappingEvents(Date from, Date until, Attendee attendee, boolean includeTransparent, boolean includeDeclined, SortOptions sortOptions, EventField[] fields) throws OXException {
        if (CalendarUserType.INDIVIDUAL.equals(attendee.getCuType()) && 0 < attendee.getEntity()) {
            Connection connection = null;
            try {
                connection = dbProvider.getReadConnection(context);
                return selectOverlappingEvents(connection, context.getContextId(), from, until, attendee.getEntity(), includeTransparent, includeDeclined, sortOptions, fields);
            } catch (SQLException e) {
                throw asOXException(e);
            } finally {
                dbProvider.releaseReadConnection(context, connection);
            }
        }
        return searchOverlappingEvents(from, until, Collections.singletonList(attendee), includeTransparent, sortOptions, fields);
    }

    @Override
    public List<Event> searchOverlappingEvents(Date from, Date until, List<Attendee> attendees, boolean includeTransparent, SortOptions sortOptions, EventField[] fields) throws OXException {
        Set<Integer> userIDs = new HashSet<Integer>();
        Set<Integer> resourceIDs = new HashSet<Integer>();
        for (Attendee attendee : attendees) {
            if (CalendarUserType.INDIVIDUAL.equals(attendee.getCuType()) && 0 < attendee.getEntity()) {
                userIDs.add(I(attendee.getEntity()));
            } else if (CalendarUserType.RESOURCE.equals(attendee.getCuType()) && 0 < attendee.getEntity()) {
                resourceIDs.add(I(attendee.getEntity()));
            }
        }
        Connection connection = null;
        try {
            connection = dbProvider.getReadConnection(context);
            return selectOverlappingEvents(connection, context.getContextId(), from, until, I2i(userIDs), I2i(resourceIDs), includeTransparent, sortOptions, fields);
        } catch (SQLException e) {
            throw asOXException(e);
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
            throw asOXException(e);
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
            throw asOXException(e);
        } finally {
            dbProvider.releaseReadConnection(context, connection);
        }
    }

    @Override
    public Event loadException(int seriesID, RecurrenceId recurrenceID, EventField[] fields) throws OXException {
        long recurrenceDatePosition = Event2Appointment.getRecurrenceDatePosition(recurrenceID).getTime();
        Connection connection = null;
        try {
            connection = dbProvider.getReadConnection(context);
            return selectException(connection, context.getContextId(), seriesID, recurrenceDatePosition, fields);
        } catch (SQLException e) {
            throw asOXException(e);
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
            throw asOXException(e);
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
            throw asOXException(e, EventMapper.getInstance(), event, connection, "prg_dates");
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
            throw asOXException(e, EventMapper.getInstance(), event, connection, "prg_dates");
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
            throw asOXException(e, EventMapper.getInstance(), event, connection, "del_dates");
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
            throw asOXException(e, EventMapper.getInstance(), null, connection, "prg_dates");
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
        Event eventData = adjustPriorSave(connection, contextID, event);
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            int parameterIndex = 1;
            stmt.setInt(parameterIndex++, contextID);
            EventMapper.getInstance().setParameters(stmt, parameterIndex, eventData, mappedFields);
            return logExecuteUpdate(stmt);
        }
    }

    private static int updateEvent(Connection connection, int contextID, int objectID, Event event) throws SQLException, OXException {
        EventField[] assignedfields = EventMapper.getInstance().getAssignedFields(event);
        String sql = new StringBuilder()
            .append("UPDATE prg_dates SET ").append(EventMapper.getInstance().getAssignments(assignedfields)).append(' ')
            .append("WHERE cid=? AND intfield01=?;")
        .toString();
        Event eventData = adjustPriorSave(connection, contextID, event);
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            int parameterIndex = 1;
            parameterIndex = EventMapper.getInstance().setParameters(stmt, parameterIndex, eventData, assignedfields);
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

    private static Event selectException(Connection connection, int contextID, int seriesID, long recurrenceDatePosition, EventField[] fields) throws SQLException, OXException {
        EventField[] mappedFields = EventMapper.getInstance().getMappedFields(fields);
        String sql = new StringBuilder()
            .append("SELECT ").append(EventMapper.getInstance().getColumns(mappedFields)).append(" FROM prg_dates ")
            .append("WHERE cid=? AND intfield02=? AND field08=? AND intfield01<>intfield02;")
        .toString();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, contextID);
            stmt.setInt(2, seriesID);
            stmt.setString(3, String.valueOf(recurrenceDatePosition));
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
            stringBuilder.append("LEFT JOIN ").append(deleted ? "delDateExternal" : "dateExternal").append(" AS e ")
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

    private static List<Event> selectOverlappingEvents(Connection connection, int contextID, Date from, Date until, int[] userIDs, int[] resourceIDs, boolean includeTransparent, SortOptions sortOptions, EventField[] fields) throws SQLException, OXException {
        EventField[] mappedFields = EventMapper.getInstance().getMappedFields(fields);
        StringBuilder stringBuilder = new StringBuilder()
            .append("SELECT DISTINCT ").append(EventMapper.getInstance().getColumns(mappedFields, "d.")).append(" FROM prg_dates AS d");
        if (null != userIDs && 0 < userIDs.length) {
            stringBuilder.append(" LEFT JOIN prg_dates_members AS m ON d.cid=m.cid AND d.intfield01=m.object_id");
        }
        if (null != resourceIDs && 0 < resourceIDs.length) {
            stringBuilder.append(" LEFT JOIN prg_date_rights AS r ON d.cid=r.cid AND d.intfield01=r.object_id");
        }
        stringBuilder.append(" WHERE d.cid=?");
        if (false == includeTransparent) {
            stringBuilder.append(" AND d.intfield06<>4");
        }
        if (null != from) {
            stringBuilder.append(" AND d.timestampfield02>=?");
        }
        if (null != until) {
            stringBuilder.append(" AND d.timestampfield01<=?");
        }
        if (null != userIDs && 0 < userIDs.length || null != resourceIDs && 0 < resourceIDs.length) {
            stringBuilder.append(" AND (");
            if (null != userIDs && 0 < userIDs.length) {
                if (1 == userIDs.length) {
                    stringBuilder.append("m.member_uid=?");
                } else {
                    stringBuilder.append("m.member_uid IN (").append(EventMapper.getParameters(userIDs.length)).append(')');
                }
                if (null != resourceIDs && 0 < resourceIDs.length) {
                    stringBuilder.append(" OR ");
                }
            }
            if (null != resourceIDs && 0 < resourceIDs.length) {
                if (1 == resourceIDs.length) {
                    stringBuilder.append("r.id=?");
                } else {
                    stringBuilder.append("r.id IN (").append(EventMapper.getParameters(resourceIDs.length)).append(')');
                }
            }
            stringBuilder.append(')');
        }
        stringBuilder.append(getSortOptions(sortOptions, "d.")).append(';');
        List<Event> events = new ArrayList<Event>();
        try (PreparedStatement stmt = connection.prepareStatement(stringBuilder.toString())) {
            int parameterIndex = 1;
            stmt.setInt(parameterIndex++, contextID);
            if (null != from) {
                stmt.setTimestamp(parameterIndex++, new Timestamp(from.getTime()));
            }
            if (null != until) {
                stmt.setTimestamp(parameterIndex++, new Timestamp(until.getTime()));
            }
            if (null != userIDs && 0 < userIDs.length) {
                for (int id : userIDs) {
                    stmt.setInt(parameterIndex++, id);
                }
            }
            if (null != resourceIDs && 0 < resourceIDs.length) {
                for (int id : resourceIDs) {
                    stmt.setInt(parameterIndex++, id);
                }
            }
            ResultSet resultSet = logExecuteQuery(stmt);
            while (resultSet.next()) {
                events.add(readEvent(resultSet, mappedFields, "d."));
            }
        }
        return events;
    }

    private static List<Event> selectOverlappingEvents(Connection connection, int contextID, Date from, Date until, int userID, boolean includeDeclined, boolean includeTransparent, SortOptions sortOptions, EventField[] fields) throws SQLException, OXException {
        EventField[] mappedFields = EventMapper.getInstance().getMappedFields(fields);
        StringBuilder stringBuilder = new StringBuilder().append("SELECT DISTINCT ").append(EventMapper.getInstance().getColumns(mappedFields, "d.")).append(" FROM prg_dates AS d").append(" LEFT JOIN prg_dates_members AS m ON d.cid=m.cid AND d.intfield01=m.object_id").append(" WHERE d.cid=? AND m.member_uid=?");
        if (false == includeTransparent) {
            stringBuilder.append(" AND d.intfield06<>4");
        }
        if (false == includeDeclined) {
            stringBuilder.append(" AND m.confirm<>2");
        }
        if (null != from) {
            stringBuilder.append(" AND d.timestampfield02>=?");
        }
        if (null != until) {
            stringBuilder.append(" AND d.timestampfield01<=?");
        }
        stringBuilder.append(getSortOptions(sortOptions, "d.")).append(';');
        List<Event> events = new ArrayList<Event>();
        try (PreparedStatement stmt = connection.prepareStatement(stringBuilder.toString())) {
            int parameterIndex = 1;
            stmt.setInt(parameterIndex++, contextID);
            stmt.setInt(parameterIndex++, userID);
            if (null != from) {
                stmt.setTimestamp(parameterIndex++, new Timestamp(from.getTime()));
            }
            if (null != until) {
                stmt.setTimestamp(parameterIndex++, new Timestamp(until.getTime()));
            }
            ResultSet resultSet = logExecuteQuery(stmt);
            while (resultSet.next()) {
                events.add(readEvent(resultSet, mappedFields, "d."));
            }
        }
        return events;
    }

    private static RecurrenceData selectRecurrenceData(Connection connection, int contextID, int seriesID, boolean deleted) throws SQLException, OXException {
        EventField[] fields = EventMapper.getInstance().getMappedFields(new EventField[] {
            EventField.ID, EventField.SERIES_ID, EventField.RECURRENCE_RULE, EventField.ALL_DAY, EventField.START_DATE,
            EventField.START_TIMEZONE, EventField.END_DATE, EventField.END_TIMEZONE
        });
        String sql = new StringBuilder()
            .append("SELECT ").append(EventMapper.getInstance().getColumns(fields))
            .append(" FROM ").append(deleted ? "del_dates" : "prg_dates")
            .append(" WHERE cid=? AND intfield01=? AND intfield02=?;")
        .toString();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, contextID);
            stmt.setInt(2, seriesID);
            stmt.setInt(3, seriesID);
            ResultSet resultSet = logExecuteQuery(stmt);
            if (resultSet.next()) {
                Event event = readEvent(resultSet, fields, null);
                return new DefaultRecurrenceData(event);
            }
        }
        return null;
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
            RecurrenceData recurrenceData = Appointment2Event.getRecurrenceData(seriesPattern);
            if (isSeriesMaster(event)) {
                /*
                 * apply recurrence rule & adjust the recurrence master's actual start- and enddate
                 */
                event.setRecurrenceRule(recurrenceData.getRecurrenceRule());
                Period seriesPeriod = new Period(event);
                Period masterPeriod = Recurrence.getRecurrenceMasterPeriod(seriesPeriod, absoluteDuration);
                event.setStartDate(masterPeriod.getStartDate());
                event.setEndDate(masterPeriod.getEndDate());
                /*
                 * transform legacy "recurrence date positions" for exceptions to recurrence ids
                 */
                if (event.containsDeleteExceptionDates() && null != event.getDeleteExceptionDates()) {
                    event.setDeleteExceptionDates(Appointment2Event.getRecurrenceIDs(recurrenceData, event.getDeleteExceptionDates()));
                }
                if (event.containsChangeExceptionDates() && null != event.getChangeExceptionDates()) {
                    event.setChangeExceptionDates(Appointment2Event.getRecurrenceIDs(recurrenceData, event.getChangeExceptionDates()));
                }
            } else if (isSeriesException(event)) {
                /*
                 * drop recurrence information for change exceptions
                 */
                //                event.removeRecurrenceRule(); // better keep?
                event.setRecurrenceRule(recurrenceData.getRecurrenceRule());
                /*
                 * transform change exception's legacy "recurrence date position" to recurrence id & apply actual recurrence id
                 */
                if (event.containsRecurrenceId() && null != event.getRecurrenceId() && StoredRecurrenceId.class.isInstance(event.getRecurrenceId())) {
                    event.setRecurrenceId(Appointment2Event.getRecurrenceID(recurrenceData, ((StoredRecurrenceId) event.getRecurrenceId()).getRecurrencePosition()));
                }
                if (event.containsChangeExceptionDates() && null != event.getChangeExceptionDates()) {
                    event.setChangeExceptionDates(Appointment2Event.getRecurrenceIDs(recurrenceData, event.getChangeExceptionDates()));
                }
            }
        }
        /*
         * take over timezone
         */
        if (event.containsStartTimeZone()) {
            event.setEndTimeZone(event.getStartTimeZone());
        }

        return event;
    }

    /**
     * Adjusts certain properties of an event prior inserting it into the database.
     *
     * @param event The event to adjust
     * @param connection The connection to use
     * @param contextID The context identifier
     * @return The adjusted event data to store
     */
    private static Event adjustPriorSave(Connection connection, int contextID, Event event) throws OXException, SQLException {
        /*
         * prepare event data for insert
         */
        Event eventData = new Event();
        EventMapper.getInstance().copy(event, eventData, EventMapper.getInstance().getMappedFields());
        if (isSeriesMaster(eventData)) {
            RecurrenceData recurrenceData = new DefaultRecurrenceData(eventData);
            if (eventData.containsRecurrenceRule() && null != eventData.getRecurrenceRule()) {
                /*
                 * convert recurrence rule to legacy pattern & derive "absolute duration" / "recurrence calculator" field
                 */
                SeriesPattern seriesPattern = Event2Appointment.getSeriesPattern(recurrenceData);
                long absoluteDuration = new Period(eventData).getTotalDays();
                eventData.setRecurrenceRule(absoluteDuration + "~" + seriesPattern.getDatabasePattern());
            }
            if (eventData.containsStartDate() || eventData.containsEndDate()) {
                /*
                 * expand recurrence master start- and enddate to cover the whole series period
                 */
                Period seriesPeriod = Recurrence.getImplicitSeriesPeriod(recurrenceData, new Period(eventData));
                eventData.setStartDate(seriesPeriod.getStartDate());
                eventData.setEndDate(seriesPeriod.getEndDate());
            }
        }
        if (isSeriesException(eventData)) {
            RecurrenceData recurrenceData;
            if (null != eventData.getRecurrenceId() && RecurrenceData.class.isInstance(eventData.getRecurrenceId())) {
                recurrenceData = (RecurrenceData) eventData.getRecurrenceId();
            } else {
                recurrenceData = selectRecurrenceData(connection, contextID, eventData.getSeriesId(), false);
            }
            if (eventData.containsRecurrenceRule() && null != eventData.getRecurrenceRule()) {
                // TODO really required to also store series pattern for exceptions?
                /*
                 * convert recurrence rule to legacy pattern & derive "absolute duration" / "recurrence calculator" field
                 */
                SeriesPattern seriesPattern = Event2Appointment.getSeriesPattern(recurrenceData);
                long absoluteDuration = new Period(eventData).getTotalDays();
                eventData.setRecurrenceRule(absoluteDuration + "~" + seriesPattern.getDatabasePattern());
            }
            /*
             * transform recurrence ids to legacy "recurrence date positions" (UTC dates with truncated time fraction)
             */
            if (eventData.containsRecurrenceId() && null != eventData.getRecurrenceId()) {
                int recurrencePosition = Event2Appointment.getRecurrencePosition(recurrenceData, eventData.getRecurrenceId());
                eventData.setRecurrenceId(new StoredRecurrenceId(recurrencePosition));
            }
        }
        /*
         * truncate milliseconds from creation date to avoid bad rounding in MySQL versions >= 5.6.4.
         * See: http://dev.mysql.com/doc/refman/5.6/en/fractional-seconds.html
         * See: com.openexchange.sql.tools.SQLTools.toTimestamp(Date)
         */
        if (eventData.containsCreated() && null != eventData.getCreated()) {
            eventData.setCreated(new Date((eventData.getCreated().getTime() / 1000) * 1000));
        }
        if (eventData.containsDeleteExceptionDates()) {
            eventData.setDeleteExceptionDates(Event2Appointment.getRecurrenceDatePositions(eventData.getDeleteExceptionDates()));
        }
        if (eventData.containsChangeExceptionDates()) {
            eventData.setChangeExceptionDates(Event2Appointment.getRecurrenceDatePositions(eventData.getChangeExceptionDates()));
        }

        return eventData;
    }

}
