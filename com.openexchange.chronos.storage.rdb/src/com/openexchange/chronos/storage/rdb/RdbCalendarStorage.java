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
import static com.openexchange.tools.arrays.Arrays.contains;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.Attachment;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.CalendarStorage;
import com.openexchange.chronos.CalendarUserType;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.ParticipationStatus;
import com.openexchange.chronos.compat.Appointment2Event;
import com.openexchange.chronos.compat.Event2Appointment;
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
import com.openexchange.java.Autoboxing;

/**
 * {@link CalendarStorage}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.10.0
 */
public class RdbCalendarStorage implements CalendarStorage {

    private static final EventMapper MAPPER = new EventMapper();

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
    public List<Alarm> loadAlarms(int objectID, int userID) throws OXException {
        Connection connection = null;
        try {
            connection = dbProvider.getReadConnection(context);
            Alarm alarm = selectReminder(connection, context.getContextId(), userID, objectID);
            return null != alarm ? Collections.singletonList(alarm) : null;
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
                event.setAttendees(selectAttendees(connection, context.getContextId(), objectID));
                event.setAttachments(selectAttachments(connection, context.getContextId(), objectID));
            }
            return event;
        } catch (SQLException e) {
            throw EventExceptionCode.MYSQL.create(e);
        } finally {
            dbProvider.releaseReadConnection(context, connection);
        }
    }

    @Override
    public int insertEvent(Event event) throws OXException {
        int updated = 0;
        Connection connection = null;
        try {
            connection = dbProvider.getWriteConnection(context);
            txPolicy.setAutoCommit(connection, false);
            int objectID = IDGenerator.getId(context, Types.APPOINTMENT, connection);
            event.setId(objectID);
            updated = insertEvent(connection, context.getContextId(), event);
            if (null != event.getAttendees()) {
                updated += insertAttendees(connection, context.getContextId(), objectID, event.getAttendees());
            }
            txPolicy.commit(connection);
            return objectID;
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
            if (event.containsAttachments()) {
                //TODO
            }
            if (event.containsAttendees()) {
                updated += deleteAttendees(connection, context.getContextId(), event.getId());
                if (null != event.getAttendees()) {
                    updated += insertAttendees(connection, context.getContextId(), event.getId(), event.getAttendees());
                }
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
            txPolicy.commit(connection);
        } catch (SQLException e) {
            throw EventExceptionCode.MYSQL.create(e);
        } finally {
            release(connection, updated);
        }
    }

    @Override
    public List<Event> loadEventsInFolder(int folderID, Date from, Date until, int createdBy, Date updatedSince, EventField[] fields) throws OXException {
        return loadEventsInFolder(folderID, false, from, until, createdBy, updatedSince, fields);
    }

    @Override
    public List<Event> loadDeletedEventsInFolder(int folderID, Date from, Date until, int createdBy, Date deletedSince) throws OXException {
        return loadEventsInFolder(folderID, true, from, until, createdBy, deletedSince, null);
    }

    @Override
    public List<Event> loadEventsOfUser(int userID, Date from, Date until, Date updatedSince, EventField[] fields) throws OXException {
        return loadEventsOfUser(userID, false, from, until, updatedSince, fields);
    }

    @Override
    public List<Event> loadDeletedEventsOfUser(int userID, Date from, Date until, Date deletedSince) throws OXException {
        return loadEventsOfUser(userID, true, from, until, deletedSince, null);
    }

    private List<Event> loadEventsInFolder(int folderID, boolean deleted, Date from, Date until, int createdBy, Date updatedSince, EventField[] fields) throws OXException {
        Connection connection = null;
        try {
            connection = dbProvider.getReadConnection(context);
            List<Event> events = selectEventsInFolder(connection, deleted, context.getContextId(), folderID, from, until, createdBy, updatedSince, fields);
            if (false == deleted) {
                events = selectAdditionalEventData(connection, context.getContextId(), events, fields);
            }
            return events;
        } catch (SQLException e) {
            throw EventExceptionCode.MYSQL.create(e);
        } finally {
            dbProvider.releaseReadConnection(context, connection);
        }
    }

    private List<Event> loadEventsOfUser(int userID, boolean deleted, Date from, Date until, Date updatedSince, EventField[] fields) throws OXException {
        Connection connection = null;
        try {
            connection = dbProvider.getReadConnection(context);
            List<Event> events = selectEventsOfUser(connection, deleted, context.getContextId(), userID, from, until, updatedSince, fields);
            if (false == deleted) {
                events = selectAdditionalEventData(connection, context.getContextId(), events, fields);
            }
            return events;
        } catch (SQLException e) {
            throw EventExceptionCode.MYSQL.create(e);
        } finally {
            dbProvider.releaseReadConnection(context, connection);
        }
    }

    private List<Event> selectAdditionalEventData(Connection connection, int contextID, List<Event> events, EventField[] fields) throws OXException, SQLException {
        if (null == fields || contains(fields, EventField.ATTENDEES)) {
            for (Event event : events) {
                event.setAttendees(selectAttendees(connection, context.getContextId(), event.getId()));
            }
        }
        if (null == fields || contains(fields, EventField.ATTACHMENTS)) {
            for (Event event : events) {
                event.setAttachments(selectAttachments(connection, context.getContextId(), event.getId()));
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
        String sql = new StringBuilder()
            .append(replace ? "REPLACE" : "INSERT").append(" INTO ").append(tableName)
            .append(" (cid,objectId,mailAddress,displayName,confirm,reason) VALUES (?,?,?,?,?,?);")
        .toString();
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
        String sql = new StringBuilder()
            .append(replace ? "REPLACE" : "INSERT").append(" INTO ").append(tableName)
            .append(" (object_id,member_uid,confirm,reason,pfid,reminder,cid) VALUES (?,?,?,?,?,?,?);")
        .toString();
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
        String sql = new StringBuilder()
            .append(replace ? "REPLACE" : "INSERT").append(" INTO ").append(tableName)
            .append(" (object_id,cid,id,type,ma,dn) VALUES (?,?,?,?,?,?);")
        .toString();
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
                usedEntities.add(Integer.valueOf(entity));
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
        EventField[] mappedFields = MAPPER.getMappedFields();
        String sql = new StringBuilder()
            .append(replace ? "REPLACE" : "INSERT").append(" INTO ").append(tableName).append(' ')
            .append("(cid,").append(MAPPER.getColumns(mappedFields)).append(") ")
            .append("VALUES (?,").append(MAPPER.getParameters(mappedFields)).append(");")
        .toString();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            int parameterIndex = 1;
            stmt.setInt(parameterIndex++, contextID);
            MAPPER.setParameters(stmt, parameterIndex, adjustDatesPriorSave(event), mappedFields);
            return logExecuteUpdate(stmt);
        }
    }

    private static int updateEvent(Connection connection, int contextID, int objectID, Event event) throws SQLException, OXException {
        EventField[] assignedfields = MAPPER.getAssignedFields(event);
        String sql = new StringBuilder()
            .append("UPDATE prg_dates SET ").append(MAPPER.getAssignments(assignedfields)).append(' ')
            .append("WHERE cid=? AND intfield01=?;")
        .toString();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            int parameterIndex = 1;
            parameterIndex = MAPPER.setParameters(stmt, parameterIndex, adjustDatesPriorSave(event), assignedfields);
            stmt.setInt(parameterIndex++, contextID);
            stmt.setInt(parameterIndex++, objectID);
            return logExecuteUpdate(stmt);
        }
    }

    private static Event selectEvent(Connection connection, int contextID, int objectID, EventField[] fields) throws SQLException, OXException {
        EventField[] mappedFields = MAPPER.getMappedFields(fields);
        String sql = new StringBuilder()
            .append("SELECT ").append(MAPPER.getColumns(mappedFields)).append(" FROM prg_dates ")
            .append("WHERE cid=? AND ").append(MAPPER.get(EventField.ID).getColumnLabel()).append("=?;")
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

    private static List<Attendee> selectInternalUserAttendees(Connection connection, int contextID, int objectID) throws SQLException {
        List<Attendee> attendees = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement("SELECT * FROM prg_dates_members WHERE cid=? AND object_id=?;")) {
            stmt.setInt(1, contextID);
            stmt.setInt(2, objectID);
            try (ResultSet resultSet = logExecuteQuery(stmt)) {
                while (resultSet.next()) {
                    attendees.add(readInternalUserAttendee(resultSet));
                }
            }
        }
        return attendees;
    }

    private static List<Attendee> selectInternalNonUserAttendees(Connection connection, int contextID, int objectID) throws SQLException {
        List<Attendee> attendees = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement("SELECT * FROM prg_date_rights WHERE cid=? AND object_id=? AND type in (2,3);")) {
            stmt.setInt(1, contextID);
            stmt.setInt(2, objectID);
            try (ResultSet resultSet = logExecuteQuery(stmt)) {
                while (resultSet.next()) {
                    attendees.add(readInternalNonUserAttendee(resultSet));
                }
            }
        }
        return attendees;
    }

    private static List<Attachment> selectAttachments(Connection connection, int contextID, int objectID) throws SQLException {
        List<Attachment> attachments = new ArrayList<Attachment>();
        try (PreparedStatement stmt = connection.prepareStatement(
            "SELECT id,file_mimetype,file_size,filename,file_id FROM prg_attachment WHERE cid=? AND attached=? AND module=?;")) {
            stmt.setInt(1, contextID);
            stmt.setInt(2, objectID);
            stmt.setInt(3, com.openexchange.groupware.Types.APPOINTMENT);
            try (ResultSet resultSet = logExecuteQuery(stmt)) {
                while (resultSet.next()) {
                    attachments.add(readAttachment(resultSet));
                }
            }
        }
        return attachments;
    }

    private static List<Attendee> selectAttendees(Connection connection, int contextID, int objectID) throws SQLException {
        List<Attendee> attendees = new ArrayList<>();
        attendees.addAll(selectExternalAttendees(connection, contextID, objectID));
        attendees.addAll(selectInternalUserAttendees(connection, contextID, objectID));
        attendees.addAll(selectInternalNonUserAttendees(connection, contextID, objectID));
        return attendees;
    }

    private static List<Attendee> selectExternalAttendees(Connection connection, int contextID, int objectID) throws SQLException {
        List<Attendee> attendees = new ArrayList<Attendee>();
        try (PreparedStatement stmt = connection.prepareStatement(
            "SELECT mailAddress,displayName,confirm,reason FROM dateexternal WHERE cid=? AND objectId=?;")) {
            stmt.setInt(1, contextID);
            stmt.setInt(2, objectID);
            try (ResultSet resultSet = logExecuteQuery(stmt)) {
                while (resultSet.next()) {
                    attendees.add(readExternalAttendee(resultSet));
                }
            }
        }
        return attendees;
    }

    private static Attendee readExternalAttendee(ResultSet resultSet) throws SQLException {
        Attendee attendee = new Attendee();
        attendee.setUri(Appointment2Event.getURI(resultSet.getString("mailAddress")));
        attendee.setCommonName(resultSet.getString("displayName"));
        attendee.setPartStat(Appointment2Event.getParticipationStatus(resultSet.getInt("confirm")));
        attendee.setComment(resultSet.getString("reason"));
        return attendee;
    }

    private static Attendee readInternalUserAttendee(ResultSet resultSet) throws SQLException {
        Attendee attendee = new Attendee();
        attendee.setCuType(CalendarUserType.INDIVIDUAL);
        attendee.setEntity(resultSet.getInt("member_uid"));
        int confirm = resultSet.getInt("confirm");
        attendee.setPartStat(resultSet.wasNull() ? ParticipationStatus.NEEDS_ACTION : Appointment2Event.getParticipationStatus(confirm));
        attendee.setComment(resultSet.getString("reason"));
        attendee.setFolderID(resultSet.getInt("pfid"));
        return attendee;
    }

    private static Attendee readInternalNonUserAttendee(ResultSet resultSet) throws SQLException {
        Attendee attendee = new Attendee();
        attendee.setEntity(resultSet.getInt("id"));
        attendee.setCuType(Appointment2Event.getCalendarUserType(resultSet.getInt("type")));
        attendee.setUri(Appointment2Event.getURI(resultSet.getString("ma")));
        attendee.setCommonName(resultSet.getString("dn"));
        return attendee;
    }

    private static Attachment readAttachment(ResultSet resultSet) throws SQLException {
        Attachment attachment = new Attachment();
        attachment.setManagedId(String.valueOf(resultSet.getInt("id")));
        attachment.setFormatType(resultSet.getString("file_mimetype"));
        attachment.setSize(Long.valueOf(resultSet.getString("file_size")));
        attachment.setManagedId(resultSet.getString("filename"));
        attachment.setContentId(resultSet.getString("file_id"));
        return attachment;
    }

    private static Event readEvent(ResultSet resultSet, EventField[] fields, String columnLabelPrefix) throws SQLException, OXException {
        return adjustDatesAfterLoad(MAPPER.fromResultSet(resultSet, fields, columnLabelPrefix));
    }

    private static Alarm selectReminder(Connection connection, int contextID, int objectID, int userID) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement("SELECT reminder FROM prg_dates_members WHERE cid=? AND object_id=? AND member_uid=?;")) {
            stmt.setInt(1, contextID);
            stmt.setInt(2, objectID);
            stmt.setInt(3, userID);
            try (ResultSet resultSet = logExecuteQuery(stmt)) {
                if (resultSet.next()) {
                    int reminder = resultSet.getInt("reminder");
                    if (false == resultSet.wasNull()) {
                        return Appointment2Event.getAlarm(reminder);
                    }
                }
            }
        }
        return null;
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

    private static List<Event> selectEventsInFolder(Connection connection, boolean deleted, int contextID, int folderID, Date from, Date until, int createdBy, Date updatedSince, EventField[] fields) throws SQLException, OXException {
        EventField[] mappedFields = MAPPER.getMappedFields(fields);
        StringBuilder stringBuilder = new StringBuilder()
            .append("SELECT ").append(MAPPER.getColumns(mappedFields, "d.")).append(' ')
            .append("FROM ").append(deleted ? "del_dates" : "prg_dates").append(" AS d ")
            .append("LEFT JOIN ").append(deleted ? "del_dates_members" : "prg_dates_members").append(" AS m ")
            .append("ON d.cid=m.cid AND d.intfield01=m.object_id ")
            .append("WHERE d.cid=? AND (d.fid=? OR m.pfid=?) ");
        if (null != from) {
            stringBuilder.append("AND ").append(MAPPER.get(EventField.END_DATE).getColumnLabel("d.")).append(">=? ");
        }
        if (null != until) {
            stringBuilder.append("AND d.timestampfield01<? ");
        }
        if (null != updatedSince) {
            stringBuilder.append("AND d.changing_date>? ");
        }
        if (0 < createdBy) {
            stringBuilder.append("AND d.created_from=? ");
        }
        List<Event> events = new ArrayList<Event>();
        try (PreparedStatement stmt = connection.prepareStatement(stringBuilder.append(';').toString())) {
            int parameterIndex = 1;
            stmt.setInt(parameterIndex++, contextID);
            stmt.setInt(parameterIndex++, folderID);
            stmt.setInt(parameterIndex++, folderID);
            if (null != from) {
                stmt.setTimestamp(parameterIndex++, new Timestamp(from.getTime()));
            }
            if (null != until) {
                stmt.setTimestamp(parameterIndex++, new Timestamp(until.getTime()));
            }
            if (null != updatedSince) {
                stmt.setLong(parameterIndex++, updatedSince.getTime());
            }
            if (0 < createdBy) {
                stmt.setInt(parameterIndex++, createdBy);
            }
            ResultSet resultSet = logExecuteQuery(stmt);
            while (resultSet.next()) {
                events.add(readEvent(resultSet, mappedFields, "d."));
            }
        }
        return events;
    }

    private static List<Event> selectEventsOfUser(Connection connection, boolean deleted, int contextID, int userID, Date from, Date until, Date updatedSince, EventField[] fields) throws SQLException, OXException {
        EventField[] mappedFields = MAPPER.getMappedFields(fields);
        StringBuilder stringBuilder = new StringBuilder()
            .append("SELECT ").append(MAPPER.getColumns(mappedFields, "d.")).append(' ')
            .append("FROM ").append(deleted ? "del_dates" : "prg_dates").append(" AS d ")
            .append("LEFT JOIN ").append(deleted ? "del_dates_members" : "prg_dates_members").append(" AS m ")
            .append("ON d.cid=m.cid AND d.intfield01=m.object_id ")
            .append("WHERE d.cid=? AND m.member_uid=? ");
        if (null != from) {
            stringBuilder.append("AND d.timestampfield02>=? ");
        }
        if (null != until) {
            stringBuilder.append("AND d.timestampfield01<=? ");
        }
        if (null != updatedSince) {
            stringBuilder.append("AND d.changing_date>? ");
        }
        List<Event> events = new ArrayList<Event>();
        try (PreparedStatement stmt = connection.prepareStatement(stringBuilder.append(';').toString())) {
            int parameterIndex = 1;
            stmt.setInt(parameterIndex++, contextID);
            stmt.setInt(parameterIndex++, userID);
            if (null != from) {
                stmt.setTimestamp(parameterIndex++, new Timestamp(from.getTime()));
            }
            if (null != until) {
                stmt.setTimestamp(parameterIndex++, new Timestamp(until.getTime()));
            }
            if (null != updatedSince) {
                stmt.setLong(parameterIndex++, updatedSince.getTime());
            }
            ResultSet resultSet = logExecuteQuery(stmt);
            while (resultSet.next()) {
                events.add(readEvent(resultSet, mappedFields, "d."));
            }
        }
        return events;
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

    private static Event adjustDatesAfterLoad(Event event) {
        if (event.containsRecurrenceRule()) {
            //TODO: richtig machen
            Calendar calendar = Calendar.getInstance();
            if (null != event.getStartTimezone()) {
                calendar.setTimeZone(TimeZone.getTimeZone(event.getStartTimezone()));
            }
            calendar.setTime(event.getStartDate());
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int date = calendar.get(Calendar.DATE);
            calendar.setTime(event.getEndDate());
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DATE, date);
            event.setEndDate(calendar.getTime());
        }
        return event;
    }

    private static Event adjustDatesPriorSave(Event event) {
        if (event.containsRecurrenceRule()) {
            //TODO: richtig machen

        }
        return event;
    }

}
