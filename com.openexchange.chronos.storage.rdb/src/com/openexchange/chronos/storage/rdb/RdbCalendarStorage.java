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

import static com.openexchange.chronos.storage.rdb.SQL.logExecuteQuery;
import static com.openexchange.chronos.storage.rdb.SQL.logExecuteUpdate;
import static com.openexchange.groupware.tools.mappings.database.DefaultDbMapper.getParameters;
import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.tools.arrays.Arrays.contains;
import static com.openexchange.tools.arrays.Collections.put;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.Attachment;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.AttendeeField;
import com.openexchange.chronos.CalendarStorage;
import com.openexchange.chronos.CalendarUserType;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.Period;
import com.openexchange.chronos.SortOptions;
import com.openexchange.chronos.SortOrder;
import com.openexchange.chronos.compat.Appointment2Event;
import com.openexchange.chronos.compat.Event2Appointment;
import com.openexchange.chronos.compat.Recurrence;
import com.openexchange.chronos.compat.SeriesPattern;
import com.openexchange.chronos.storage.rdb.exception.EventExceptionCode;
import com.openexchange.chronos.storage.rdb.osgi.Services;
import com.openexchange.context.ContextService;
import com.openexchange.database.provider.DBProvider;
import com.openexchange.database.provider.DBTransactionPolicy;
import com.openexchange.exception.OXException;
import com.openexchange.group.Group;
import com.openexchange.group.GroupService;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.impl.IDGenerator;
import com.openexchange.groupware.tools.mappings.database.DbMapping;
import com.openexchange.java.Autoboxing;
import com.openexchange.search.SearchTerm;

/**
 * {@link CalendarStorage}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.10.0
 */
public class RdbCalendarStorage implements CalendarStorage {

    private final Context context;
    private final DBProvider dbProvider;
    private final DBTransactionPolicy txPolicy;

    /**
     * Initializes a new {@link RdbCalendarStorage}.
     *
     * @param context The context
     * @param dbProvider The database provider to use
     * @param The transaction policy
     */
    public RdbCalendarStorage(Context context, DBProvider dbProvider, DBTransactionPolicy txPolicy) {
        super();
        this.context = context;
        this.dbProvider = dbProvider;
        this.txPolicy = txPolicy;
    }

    @Override
    public List<Event> searchEvents(SearchTerm<?> searchTerm, SortOptions sortOptions, EventField[] fields) throws OXException {
        Connection connection = null;
        try {
            connection = dbProvider.getReadConnection(context);
            List<Event> events = selectEvents(connection, false, context.getContextId(), searchTerm, sortOptions, fields);
            return selectAdditionalEventData(connection, context.getContextId(), events, fields);
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
    public List<Alarm> loadAlarms(int objectID, int userID) throws OXException {
        return loadAlarms(new int[] { objectID }, userID).get(I(objectID));
    }

    @Override
    public Map<Integer, List<Alarm>> loadAlarms(int[] objectIDs, int userID) throws OXException {
        Connection connection = null;
        try {
            connection = dbProvider.getReadConnection(context);
            return selectAlarms(connection, context.getContextId(), objectIDs, userID);
        } catch (SQLException e) {
            throw EventExceptionCode.MYSQL.create(e);
        } finally {
            dbProvider.releaseReadConnection(context, connection);
        }
    }

    @Override
    public void insertAlarms(int objectID, int userID, List<Alarm> alarms) throws OXException {
        updateAlarms(objectID, userID, alarms);
    }

    @Override
    public void updateAlarms(int objectID, int userID, List<Alarm> alarms) throws OXException {
        int updated = 0;
        Connection connection = null;
        try {
            connection = dbProvider.getWriteConnection(context);
            txPolicy.setAutoCommit(connection, false);
            updated = updateReminder(connection, context.getContextId(), objectID, userID, Event2Appointment.getReminder(alarms));
            txPolicy.commit(connection);
        } catch (SQLException e) {
            throw EventExceptionCode.MYSQL.create(e);
        } finally {
            release(connection, updated);
        }
    }

    @Override
    public void deleteAlarms(int objectID, int userID) throws OXException {
        updateAlarms(objectID, userID, null);
    }

    @Override
    public void deleteAlarms(int objectID) throws OXException {
        int updated = 0;
        Connection connection = null;
        try {
            connection = dbProvider.getWriteConnection(context);
            txPolicy.setAutoCommit(connection, false);
            updated = updateReminders(connection, context.getContextId(), objectID, null);
            txPolicy.commit(connection);
        } catch (SQLException e) {
            throw EventExceptionCode.MYSQL.create(e);
        } finally {
            release(connection, updated);
        }
    }

    @Override
    public Event loadEvent(int objectID, EventField[] fields) throws OXException {
        Connection connection = null;
        try {
            connection = dbProvider.getReadConnection(context);
            Event event = selectEvent(connection, context.getContextId(), objectID, fields);
            if (null != event) {
                selectAdditionalEventData(connection, context.getContextId(), Collections.singletonList(event), fields);
            }
            return event;
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
            if (event.containsAttendees() && null != event.getAttendees()) {
                updated += insertAttendees(connection, context.getContextId(), event.getId(), event.getAttendees());
            }
            if (event.containsAttachments() && null != event.getAttachments()) {
                //TODO
            }
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
            if (event.containsAttendees()) {
                //TODO: merge - loosing reminder otherwise
                updated += deleteAttendees(connection, context.getContextId(), event.getId());
                if (null != event.getAttendees()) {
                    updated += insertAttendees(connection, context.getContextId(), event.getId(), event.getAttendees());
                }
            }
            if (event.containsAttachments()) {
                //TODO
            }
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
            if (null != event.getAttendees()) {
                updated += insertTombstoneAttendees(connection, context.getContextId(), event.getId(), event.getAttendees());
            }
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
            updated += deleteAttendees(connection, context.getContextId(), objectID);
            //TODO: updated += deleteAttachments(connection, context.getContextId(), objectID);
            txPolicy.commit(connection);
        } catch (SQLException e) {
            throw EventExceptionCode.MYSQL.create(e);
        } finally {
            release(connection, updated);
        }
    }

    private static List<Event> selectAdditionalEventData(Connection connection, int contextID, List<Event> events, EventField[] fields) throws OXException, SQLException {
        if (null != events && 0 < events.size() && (null == fields || contains(fields, EventField.ATTENDEES) || contains(fields, EventField.ATTACHMENTS))) {
            int[] objectIDs = getObjectIDs(events);
            if (null == fields || contains(fields, EventField.ATTENDEES)) {
                Map<Integer, List<Attendee>> attendeesById = selectAttendees(connection, contextID, objectIDs, AttendeeField.values());
                for (Event event : events) {
                    event.setAttendees(attendeesById.get(I(event.getId())));
                }
            }
            if (null == fields || contains(fields, EventField.ATTACHMENTS)) {
                Map<Integer, List<Attachment>> attachmentsById = selectAttachments(connection, contextID, objectIDs);
                for (Event event : events) {
                    event.setAttachments(attachmentsById.get(I(event.getId())));
                }
            }
        }
        return events;
    }

    private static int deleteEvent(Connection connection, int contextID, int objectID) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement("DELETE FROM prg_dates WHERE cid=? AND intfield01=?;")) {
            stmt.setInt(1, contextID);
            stmt.setInt(2, objectID);
            return logExecuteUpdate(stmt);
        }
    }

    private static int deleteAttendees(Connection connection, int contextID, int objectID) throws SQLException {
        int updated = 0;
        try (PreparedStatement stmt = connection.prepareStatement("DELETE FROM dateexternal WHERE cid=? AND objectId=?;")) {
            stmt.setInt(1, contextID);
            stmt.setInt(2, objectID);
            updated += logExecuteUpdate(stmt);
        }
        try (PreparedStatement stmt = connection.prepareStatement("DELETE FROM prg_dates_members WHERE cid=? AND object_id=?;")) {
            stmt.setInt(1, contextID);
            stmt.setInt(2, objectID);
            updated += logExecuteUpdate(stmt);
        }
        try (PreparedStatement stmt = connection.prepareStatement("DELETE FROM prg_date_rights WHERE cid=? AND object_id=?;")) {
            stmt.setInt(1, contextID);
            stmt.setInt(2, objectID);
            updated += logExecuteUpdate(stmt);
        }
        return updated;
    }

    private static int insertOrReplaceDateExternal(Connection connection, String tableName, boolean replace, int contextID, int objectID, Attendee attendee) throws SQLException {
        String sql = new StringBuilder().append(replace ? "REPLACE" : "INSERT").append(" INTO ").append(tableName).append(" (cid,objectId,mailAddress,displayName,confirm,reason) VALUES (?,?,?,?,?,?);").toString();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            int parameterIndex = 1;
            stmt.setInt(parameterIndex++, contextID);
            stmt.setInt(parameterIndex++, objectID);
            stmt.setString(parameterIndex++, Event2Appointment.getEMailAddress(attendee.getUri()));
            stmt.setString(parameterIndex++, attendee.getCommonName());
            stmt.setInt(parameterIndex++, Event2Appointment.getConfirm(attendee.getPartStat()));
            stmt.setString(parameterIndex++, attendee.getComment());
            return logExecuteUpdate(stmt);
        }
    }

    private static int insertOrReplaceDatesMembers(Connection connection, String tableName, boolean replace, int contextID, int objectID, Attendee attendee) throws SQLException {
        String sql = new StringBuilder().append(replace ? "REPLACE" : "INSERT").append(" INTO ").append(tableName).append(" (object_id,member_uid,confirm,reason,pfid,reminder,cid) VALUES (?,?,?,?,?,?,?);").toString();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            int parameterIndex = 1;
            stmt.setInt(parameterIndex++, objectID);
            stmt.setInt(parameterIndex++, attendee.getEntity());
            stmt.setInt(parameterIndex++, Event2Appointment.getConfirm(attendee.getPartStat()));
            stmt.setString(parameterIndex++, attendee.getComment());
            stmt.setInt(parameterIndex++, attendee.getFolderID());
            stmt.setNull(parameterIndex++, java.sql.Types.INTEGER);
            stmt.setInt(parameterIndex++, contextID);
            return logExecuteUpdate(stmt);
        }
    }

    private static int insertOrReplaceDateRights(Connection connection, String tableName, boolean replace, int contextID, int objectID, int entity, Attendee attendee) throws SQLException {
        String sql = new StringBuilder().append(replace ? "REPLACE" : "INSERT").append(" INTO ").append(tableName).append(" (object_id,cid,id,type,ma,dn) VALUES (?,?,?,?,?,?);").toString();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            int parameterIndex = 1;
            stmt.setInt(parameterIndex++, objectID);
            stmt.setInt(parameterIndex++, contextID);
            if (0 < attendee.getEntity()) {
                stmt.setInt(parameterIndex++, entity);
                stmt.setInt(parameterIndex++, Event2Appointment.getParticipantType(attendee.getCuType(), true));
                stmt.setNull(parameterIndex++, java.sql.Types.VARCHAR);
                stmt.setNull(parameterIndex++, java.sql.Types.VARCHAR);
            } else {
                stmt.setInt(parameterIndex++, entity);
                stmt.setInt(parameterIndex++, Event2Appointment.getParticipantType(attendee.getCuType(), false));
                stmt.setString(parameterIndex++, Event2Appointment.getEMailAddress(attendee.getUri()));
                stmt.setString(parameterIndex++, attendee.getCommonName());
            }
            return logExecuteUpdate(stmt);
        }
    }

    private static int insertTombstoneAttendees(Connection connection, int contextID, int objectID, List<Attendee> attendees) throws SQLException, OXException {
        int updated = 0;
        Set<Integer> usedEntities = new HashSet<>();
        Map<Integer, Set<Integer>> groups = loadGroups(contextID, attendees);
        for (Attendee attendee : attendees) {
            if (0 >= attendee.getEntity()) {
                /*
                 * insert additional record into deldateexternal for external users
                 */
                updated += insertOrReplaceDateExternal(connection, "deldateexternal", true, contextID, objectID, attendee);
            } else if (CalendarUserType.INDIVIDUAL.equals(attendee.getCuType())) {
                /*
                 * insert additional record into del_dates_members for each internal user
                 */
                updated += insertOrReplaceDatesMembers(connection, "del_dates_members", true, contextID, objectID, attendee);
            }
            if (false == isGroupMember(attendee, groups)) {
                /*
                 * insert record into del_date_rights for each attendee, skipping group members
                 */
                int entity = determineEntity(attendee, usedEntities);
                updated += insertOrReplaceDateRights(connection, "del_date_rights", true, contextID, objectID, entity, attendee);
                usedEntities.add(I(entity));
            }
        }
        return updated;
    }

    private static int insertAttendees(Connection connection, int contextID, int objectID, List<Attendee> attendees) throws SQLException, OXException {
        int updated = 0;
        Set<Integer> usedEntities = new HashSet<>();
        Map<Integer, Set<Integer>> groups = loadGroups(contextID, attendees);
        for (Attendee attendee : attendees) {
            if (0 >= attendee.getEntity()) {
                /*
                 * insert additional record into dateExternal for external users
                 */
                updated += insertOrReplaceDateExternal(connection, "dateexternal", false, contextID, objectID, attendee);
            } else if (CalendarUserType.INDIVIDUAL.equals(attendee.getCuType())) {
                /*
                 * insert additional record into prg_dates_members for each internal user
                 */
                updated += insertOrReplaceDatesMembers(connection, "prg_dates_members", false, contextID, objectID, attendee);
            }
            if (false == isGroupMember(attendee, groups)) {
                /*
                 * insert record into prg_date_rights for each attendee, skipping group members
                 */
                int entity = determineEntity(attendee, usedEntities);
                updated += insertOrReplaceDateRights(connection, "prg_date_rights", false, contextID, objectID, entity, attendee);
                usedEntities.add(Integer.valueOf(entity));
            }
        }
        return updated;
    }

    private static int determineEntity(Attendee attendee, Set<Integer> usedEntities) {
        if (0 < attendee.getEntity()) {
            usedEntities.add(Integer.valueOf(attendee.getEntity()));
            return attendee.getEntity();
        } else {
            String uri = attendee.getUri();
            int entity = -1 * Math.abs(null != uri ? uri.hashCode() : 1);
            while (false == usedEntities.add(Integer.valueOf(entity))) {
                entity--;
            }
            return entity;
        }
    }

    private static boolean isGroupMember(Attendee attendee, Map<Integer, Set<Integer>> groups) {
        if (CalendarUserType.INDIVIDUAL.equals(attendee.getCuType()) && 0 < attendee.getEntity() && null != groups && 0 < groups.size()) {
            Integer id = Autoboxing.I(attendee.getEntity());
            for (Set<Integer> members : groups.values()) {
                if (members.contains(id)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static Map<Integer, Set<Integer>> loadGroups(int contextID, List<Attendee> attendees) throws OXException {
        Map<Integer, Set<Integer>> groups = new HashMap<>();
        for (Attendee attendee : attendees) {
            if (CalendarUserType.GROUP.equals(attendee.getCuType()) && 0 < attendee.getEntity()) {
                Context context = Services.getService(ContextService.class).getContext(contextID);
                Group group = Services.getService(GroupService.class).getGroup(context, attendee.getEntity());
                HashSet<Integer> members = new HashSet<>(Arrays.asList(Autoboxing.i2I(group.getMember())));
                groups.put(Autoboxing.I(group.getIdentifier()), members);
            }
        }
        return groups;
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
            .append(replace ? "REPLACE" : "INSERT").append(" INTO ").append(tableName).append(' ').append("(cid,").append(EventMapper.getInstance().getColumns(mappedFields)).append(") ").append("VALUES (?,").append(EventMapper.getInstance().getParameters(mappedFields)).append(");")
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

    private static Map<Integer, List<Attendee>> selectAttendees(Connection connection, int contextID, int objectIDs[], AttendeeField[] fields) throws SQLException, OXException {
        Map<Integer, List<Attendee>> attendeesById = new HashMap<Integer, List<Attendee>>();
        selectAndAddInternalUserAttendees(attendeesById, connection, contextID, objectIDs, fields);
        selectAndAddExternalAttendees(attendeesById, connection, contextID, objectIDs, fields);
        selectAndAddInternalNonUserAttendees(attendeesById, connection, contextID, objectIDs);
        return attendeesById;
    }

    private static void selectAndAddInternalUserAttendees(Map<Integer, List<Attendee>> attendeesById, Connection connection, int contextID, int objectIDs[], AttendeeField[] fields) throws SQLException, OXException {
        AttendeeField[] mappedFields = InternalAttendeeMapper.getInstance().getMappedFields(fields);
        String sql = new StringBuilder()
            .append("SELECT object_id,").append(InternalAttendeeMapper.getInstance().getColumns(mappedFields)).append(" FROM prg_dates_members ")
            .append("WHERE cid=? AND object_id IN (").append(getParameters(objectIDs.length)).append(");")
        .toString();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            int parameterIndex = 1;
            stmt.setInt(parameterIndex++, contextID);
            for (int objectID : objectIDs) {
                stmt.setInt(parameterIndex++, objectID);
            }
            try (ResultSet resultSet = logExecuteQuery(stmt)) {
                while (resultSet.next()) {
                    Attendee attendee = InternalAttendeeMapper.getInstance().fromResultSet(resultSet, mappedFields);
                    attendee.setCuType(CalendarUserType.INDIVIDUAL);
                    put(attendeesById, I(resultSet.getInt("object_id")), attendee);
                }
            }
        }
    }

    private static void selectAndAddExternalAttendees(Map<Integer, List<Attendee>> attendeesById, Connection connection, int contextID, int objectIDs[], AttendeeField[] fields) throws SQLException, OXException {
        AttendeeField[] mappedFields = ExternalAttendeeMapper.getInstance().getMappedFields(fields);
        String sql = new StringBuilder()
            .append("SELECT objectId,").append(ExternalAttendeeMapper.getInstance().getColumns(mappedFields)).append(" FROM dateexternal ")
            .append("WHERE cid=? AND objectId IN (").append(getParameters(objectIDs.length)).append(");")
        .toString();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            int parameterIndex = 1;
            stmt.setInt(parameterIndex++, contextID);
            for (int objectID : objectIDs) {
                stmt.setInt(parameterIndex++, objectID);
            }
            try (ResultSet resultSet = logExecuteQuery(stmt)) {
                while (resultSet.next()) {
                    Attendee attendee = ExternalAttendeeMapper.getInstance().fromResultSet(resultSet, mappedFields);
                    attendee.setCuType(CalendarUserType.INDIVIDUAL);
                    put(attendeesById, I(resultSet.getInt("objectId")), attendee);
                }
            }
        }
    }

    private static void selectAndAddInternalNonUserAttendees(Map<Integer, List<Attendee>> attendeesById, Connection connection, int contextID, int[] objectIDs) throws SQLException {
        String sql = new StringBuilder()
            .append("SELECT object_id,id,type,ma,dn FROM prg_date_rights ")
            .append("WHERE cid=? AND object_id IN (").append(getParameters(objectIDs.length)).append(") ")
            .append("AND type IN (2,3);")
        .toString();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            int parameterIndex = 1;
            stmt.setInt(parameterIndex++, contextID);
            for (int objectID : objectIDs) {
                stmt.setInt(parameterIndex++, objectID);
            }
            try (ResultSet resultSet = logExecuteQuery(stmt)) {
                while (resultSet.next()) {
                    Attendee attendee = new Attendee();
                    attendee.setEntity(resultSet.getInt("id"));
                    attendee.setCuType(Appointment2Event.getCalendarUserType(resultSet.getInt("type")));
                    attendee.setUri(Appointment2Event.getURI(resultSet.getString("ma")));
                    attendee.setCommonName(resultSet.getString("dn"));
                    put(attendeesById, I(resultSet.getInt("object_id")), attendee);
                }
            }
        }
    }

    private static Map<Integer, List<Attachment>> selectAttachments(Connection connection, int contextID, int[] objectIDs) throws SQLException {
        Map<Integer, List<Attachment>> attachmentsById = new HashMap<Integer, List<Attachment>>();
        String sql = new StringBuilder()
            .append("SELECT attached,id,file_mimetype,file_size,filename,file_id FROM prg_attachment ")
            .append("WHERE cid=? AND attached IN (").append(getParameters(objectIDs.length)).append(") AND module=?;")
        .toString();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            int parameterIndex = 1;
            stmt.setInt(parameterIndex++, contextID);
            for (int objectID : objectIDs) {
                stmt.setInt(parameterIndex++, objectID);
            }
            stmt.setInt(parameterIndex++, com.openexchange.groupware.Types.APPOINTMENT);
            try (ResultSet resultSet = logExecuteQuery(stmt)) {
                while (resultSet.next()) {
                    Attachment attachment = new Attachment();
                    attachment.setManagedId(String.valueOf(resultSet.getInt("id")));
                    attachment.setFormatType(resultSet.getString("file_mimetype"));
                    attachment.setSize(Long.valueOf(resultSet.getString("file_size")));
                    attachment.setManagedId(resultSet.getString("filename"));
                    attachment.setContentId(resultSet.getString("file_id"));
                    put(attachmentsById, I(resultSet.getInt("attached")), attachment);
                }
            }
        }
        return attachmentsById;
    }

    private static Event readEvent(ResultSet resultSet, EventField[] fields, String columnLabelPrefix) throws SQLException, OXException {
        return adjustAfterLoad(EventMapper.getInstance().fromResultSet(resultSet, fields, columnLabelPrefix));
    }

    private static Map<Integer, List<Alarm>> selectAlarms(Connection connection, int contextID, int[] objectIDs, int userID) throws SQLException {
        Map<Integer, List<Alarm>> alarmsById = new HashMap<Integer, List<Alarm>>();
        String sql = new StringBuilder()
            .append("SELECT object_id,reminder FROM prg_dates_members ")
            .append("WHERE cid=? AND member_uid=? AND object_id IN (").append(getParameters(objectIDs.length)).append(");")
        .toString();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            int parameterIndex = 1;
            stmt.setInt(parameterIndex++, contextID);
            stmt.setInt(parameterIndex++, userID);
            for (int objectID : objectIDs) {
                stmt.setInt(parameterIndex++, objectID);
            }
            try (ResultSet resultSet = logExecuteQuery(stmt)) {
                if (resultSet.next()) {
                    int reminder = resultSet.getInt("reminder");
                    if (false == resultSet.wasNull()) {
                        alarmsById.put(I(resultSet.getInt("object_id")), Collections.singletonList(Appointment2Event.getAlarm(reminder)));
                    }
                }
            }
        }
        return alarmsById;
    }

    private static int updateReminder(Connection connection, int contextID, int objectID, int userID, Integer reminder) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement("UPDATE prg_dates_members SET reminder=? WHERE cid=? AND object_id=? AND member_uid=?;")) {
            if (null == reminder) {
                stmt.setNull(1, java.sql.Types.INTEGER);
            } else {
                stmt.setInt(1, reminder.intValue());
            }
            stmt.setInt(2, contextID);
            stmt.setInt(3, objectID);
            stmt.setInt(4, userID);
            return logExecuteUpdate(stmt);
        }
    }

    private static int updateReminders(Connection connection, int contextID, int objectID, Integer reminder) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement("UPDATE prg_dates_members SET reminder=? WHERE cid=? AND object_id=?;")) {
            if (null == reminder) {
                stmt.setNull(1, java.sql.Types.INTEGER);
            } else {
                stmt.setInt(1, reminder.intValue());
            }
            stmt.setInt(2, contextID);
            stmt.setInt(3, objectID);
            return logExecuteUpdate(stmt);
        }
    }

    private static List<Event> selectEvents(Connection connection, boolean deleted, int contextID, SearchTerm<?> searchTerm, SortOptions sortOptions, EventField[] fields) throws SQLException, OXException {
        EventField[] mappedFields = EventMapper.getInstance().getMappedFields(fields);
        SearchTermAdapter adapter = new SearchTermAdapter(searchTerm, null, "d.", "m.", "e.");
        StringBuilder stringBuilder = new StringBuilder().append("SELECT ").append(EventMapper.getInstance().getColumns(mappedFields, "d.")).append(' ')
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

    private static int[] getObjectIDs(List<Event> events) {
        int[] objectIDs = new int[events.size()];
        for (int i = 0; i < events.size(); i++) {
            objectIDs[i] = events.get(i).getId();
        }
        return objectIDs;
    }

    /**
     * Safely releases a write connection obeying the configured transaction policy, rolling back automatically if not committed before.
     *
     * @param connection The write connection to release
     * @param updated The number of actually updated rows to
     */
    private void release(Connection connection, int updated) throws OXException {
        if (null != connection) {
            try {
                if (false == connection.getAutoCommit()) {
                    txPolicy.rollback(connection);
                }
                txPolicy.setAutoCommit(connection, true);
            } catch (SQLException e) {
                throw EventExceptionCode.MYSQL.create(e);
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
     * Adjusts certain properties of an event after loading it from the database.
     * <p/>
     * <b>Note:</b> This method requires that the properties {@link EventField#ALL_DAY}, {@link EventField#RECURRENCE_RULE},
     * {@link EventField#START_TIMEZONE}, {@link EventField#START_DATE} and {@link EventField#END_DATE} were loaded.
     *
     * @param event The event to adjust
     * @return The (possibly adjusted) event reference
     */
    private static Event adjustAfterLoad(Event event) {
        if (event.containsRecurrenceRule() && null != event.getRecurrenceRule()) {
            /*
             * drop recurrence information for change exceptions
             */
            if (event.getId() != event.getSeriesId()) {
                event.removeRecurrenceRule();
            } else {
                /*
                 * extract series pattern and "absolute duration" / "recurrence calculator" field
                 */
                String value = event.getRecurrenceRule();
                int idx = value.indexOf('~');
                int absoluteDuration = Integer.parseInt(value.substring(0, idx));
                String databasePattern = value.substring(idx + 1);
                String timeZone = null != event.getStartTimezone() ? event.getStartTimezone() : "UTC";
                boolean allDay = event.isAllDay();
                /*
                 * convert legacy series pattern into proper recurrence rule
                 */
                SeriesPattern seriesPattern = new SeriesPattern(databasePattern, timeZone, allDay);
                String recurrenceRule = Recurrence.getRecurrenceRule(seriesPattern);
                event.setRecurrenceRule(recurrenceRule);
                /*
                 * adjust the recurrence master's actual start- and enddate
                 */
                Period seriesPeriod = new Period(event);
                Period masterPeriod = Recurrence.getRecurrenceMasterPeriod(seriesPeriod, absoluteDuration);
                event.setStartDate(masterPeriod.getStartDate());
                event.setEndDate(masterPeriod.getEndDate());
            }
        }
        return event;
    }


    /**
     * Adjusts certain properties of an event prior saving it in the database.
     *
     * @param event The event to adjust
     * @return The (possibly adjusted) event reference
     */
    private static Event adjustPriorSave(Event event) {
        /*
         * convert recurrence rule extract series pattern and "absolute duration" / "recurrence calculator" field
         */
        if (event.containsRecurrenceRule() && null != event.getRecurrenceRule()) {
            long absoluteDuration = new Period(event).getTotalDays();
            TimeZone timeZone = event.containsStartTimezone() && null != event.getStartTimezone() ? TimeZone.getTimeZone(event.getStartTimezone()) : null;
            Calendar calendar = null != timeZone ? GregorianCalendar.getInstance(timeZone) : GregorianCalendar.getInstance();
            calendar.setTime(event.getStartDate());
            SeriesPattern seriesPattern = Recurrence.generatePattern(event.getRecurrenceRule(), calendar);
            String value = absoluteDuration + "~" + seriesPattern.getDatabasePattern();
            event.setRecurrenceRule(value);
            /*
             * expand recurrence master start- and enddate to cover the whole series period
             */
            if (event.getId() == event.getSeriesId()) {
                Period seriesPeriod = Recurrence.getImplicitSeriesPeriod(seriesPattern);
                event.setStartDate(seriesPeriod.getStartDate());
                event.setEndDate(seriesPeriod.getEndDate());
            }
        }
        return event;
    }

}
