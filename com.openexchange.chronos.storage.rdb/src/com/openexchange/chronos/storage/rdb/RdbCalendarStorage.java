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
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.CalendarStorage;
import com.openexchange.chronos.CalendarUserType;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.Organizer;
import com.openexchange.chronos.ParticipationStatus;
import com.openexchange.chronos.compat.Appointment2Event;
import com.openexchange.chronos.compat.Event2Appointment;
import com.openexchange.chronos.compat.SeriesPattern;
import com.openexchange.chronos.storage.rdb.exception.EventExceptionCode;
import com.openexchange.chronos.storage.rdb.osgi.Services;
import com.openexchange.context.ContextService;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.group.Group;
import com.openexchange.group.GroupService;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.impl.IDGenerator;
import com.openexchange.java.Autoboxing;
import com.openexchange.java.Strings;
import com.openexchange.tools.sql.DBUtils;

/**
 * {@link CalendarStorage}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.10.0
 */
public class RdbCalendarStorage extends AbstractRdbStorage implements CalendarStorage {

    private final int contextID;
    private final DatabaseService databaseService;

    /**
     * Initializes a new {@link RdbChecksumStore}.
     *
     * @param contextID The context ID
     */
    public RdbCalendarStorage(int contextID) throws OXException {
        super(contextID);
        this.contextID = contextID;
        this.databaseService = Services.getService(DatabaseService.class, true);
    }

    public Event loadEvent(int objectID) throws OXException {
        Connection connection = null;
        try {
            connection = databaseService.getReadOnly(contextID);
            Event event = selectEvent(connection, contextID, objectID);
            event.setAttendees(selectAttendees(connection, contextID, objectID));
            return event;
        } catch (SQLException e) {
            throw EventExceptionCode.MYSQL.create(e);
        } finally {
            databaseService.backReadOnly(contextID, connection);
        }
    }

    public List<Alarm> loadAlarms(int objectID, int userID) throws OXException {
        Connection connection = null;
        try {
            connection = databaseService.getReadOnly(contextID);
            return selectAlarms(connection, contextID, userID, objectID);
        } catch (SQLException e) {
            throw EventExceptionCode.MYSQL.create(e);
        } finally {
            databaseService.backReadOnly(contextID, connection);
        }
    }

    @Override
    public List<Event> loadEventsInFolder(int folderID, Date from, Date until) throws OXException {
        Connection connection = null;
        try {
            connection = databaseService.getReadOnly(contextID);
            List<Event> events = selectEventsInFolder(connection, contextID, folderID, from, until);
            for (Event event : events) {
                event.setAttendees(selectAttendees(connection, contextID, event.getId()));
            }
            return events;
        } catch (SQLException e) {
            throw EventExceptionCode.MYSQL.create(e);
        } finally {
            databaseService.backReadOnly(contextID, connection);
        }
    }

    @Override
    public List<Event> loadEventsInFolderCreatedBy(int folderID, int createdBy, Date from, Date until) throws OXException {
        Connection connection = null;
        try {
            connection = databaseService.getReadOnly(contextID);
            List<Event> events = selectEventsInFolderCreatedBy(connection, contextID, folderID, createdBy, from, until);
            for (Event event : events) {
                event.setAttendees(selectAttendees(connection, contextID, event.getId()));
            }
            return events;
        } catch (SQLException e) {
            throw EventExceptionCode.MYSQL.create(e);
        } finally {
            databaseService.backReadOnly(contextID, connection);
        }
    }

    @Override
    public List<Event> loadEventsOfUser(int userID, Date from, Date until) throws OXException {
        Connection connection = null;
        try {
            connection = databaseService.getReadOnly(contextID);
            List<Event> events = selectEventsOfUser(connection, contextID, userID, from, until);
            for (Event event : events) {
                event.setAttendees(selectAttendees(connection, contextID, event.getId()));
            }
            return events;
        } catch (SQLException e) {
            throw EventExceptionCode.MYSQL.create(e);
        } finally {
            databaseService.backReadOnly(contextID, connection);
        }
    }

    @Override
    public int insertEvent(Event event) throws OXException {
        Connection connection = null;
        try {
            connection = databaseService.getWritable(contextID);
            DBUtils.startTransaction(connection);
            int objectID = IDGenerator.getId(contextID, Types.APPOINTMENT, connection);
            insertEvent(connection, contextID, objectID, event);
            if (null != event.getAttendees()) {
                insertAttendees(connection, contextID, objectID, event.getAttendees());
            }
            connection.commit();
            return objectID;
        } catch (SQLException e) {
            DBUtils.rollback(connection);
            throw EventExceptionCode.MYSQL.create(e);
        } finally {
            DBUtils.autocommit(connection);
            databaseService.backReadOnly(contextID, connection);
        }
    }

    private static int insertAttendees(Connection connection, int contextID, int objectID, List<Attendee> attendees) throws SQLException, OXException {
        int updated = 0;
        Set<Integer> usedEntities = new HashSet<Integer>();
        Map<Integer, Set<Integer>> groups = loadGroups(contextID, attendees);
        for (Attendee attendee : attendees) {
            if (0 >= attendee.getEntity()) {
                /*
                 * insert additional record into dateExternal for external users
                 */
                try (PreparedStatement stmt = connection.prepareStatement("INSERT INTO dateexternal " +
                    "(cid,objectId,mailAddress,displayName,confirm,reason) VALUES (?,?,?,?,?,?);")) {
                    int parameterIndex = 1;
                    stmt.setInt(parameterIndex++, contextID);
                    stmt.setInt(parameterIndex++, objectID);
                    stmt.setString(parameterIndex++, Event2Appointment.getEMailAddress(attendee.getUri()));
                    stmt.setString(parameterIndex++, attendee.getCommonName());
                    stmt.setInt(parameterIndex++, Event2Appointment.getConfirm(attendee.getPartStat()));
                    stmt.setString(parameterIndex++, attendee.getComment());
                    updated += stmt.executeUpdate();
                }
            } else if (CalendarUserType.INDIVIDUAL.equals(attendee.getCuType())) {
                /*
                 * insert additional record into prg_dates_members for each internal user
                 */
                try (PreparedStatement stmt = connection.prepareStatement("INSERT INTO prg_dates_members " +
                    "(object_id,member_uid,confirm,reason,pfid,reminder,cid) VALUES (?,?,?,?,?,?,?);")) {
                    int parameterIndex = 1;
                    stmt.setInt(parameterIndex++, objectID);
                    stmt.setInt(parameterIndex++, attendee.getEntity());
                    stmt.setInt(parameterIndex++, Event2Appointment.getConfirm(attendee.getPartStat()));
                    stmt.setString(parameterIndex++, attendee.getComment());
                    stmt.setInt(parameterIndex++, attendee.getFolderID());
                    stmt.setNull(parameterIndex++, java.sql.Types.INTEGER);
                    stmt.setInt(parameterIndex++, contextID);
                    updated += stmt.executeUpdate();
                }
            }
            if (false == isGroupMember(attendee, groups)) {
                /*
                 * insert record into prg_date_rights for each attendee, skipping group members
                 */
                try (PreparedStatement stmt = connection.prepareStatement("INSERT INTO prg_date_rights " +
                    "(object_id,cid,id,type,ma,dn) VALUES (?,?,?,?,?,?);")) {
                    int parameterIndex = 1;
                    stmt.setInt(parameterIndex++, objectID);
                    stmt.setInt(parameterIndex++, contextID);
                    if (0 < attendee.getEntity()) {
                        usedEntities.add(Integer.valueOf(attendee.getEntity()));
                        stmt.setInt(parameterIndex++, attendee.getEntity());
                        stmt.setInt(parameterIndex++, Event2Appointment.getParticipantType(attendee.getCuType(), true));
                        stmt.setNull(parameterIndex++, java.sql.Types.VARCHAR);
                        stmt.setNull(parameterIndex++, java.sql.Types.VARCHAR);
                    } else {
                        int syntheticEntity = generateSyntheticEntity(attendee);
                        while (usedEntities.contains(Integer.valueOf(syntheticEntity))) {
                            syntheticEntity--;
                        }
                        usedEntities.add(Integer.valueOf(syntheticEntity));
                        stmt.setInt(parameterIndex++, syntheticEntity);
                        stmt.setInt(parameterIndex++, Event2Appointment.getParticipantType(attendee.getCuType(), false));
                        stmt.setString(parameterIndex++, Event2Appointment.getEMailAddress(attendee.getUri()));
                        stmt.setString(parameterIndex++, attendee.getCommonName());
                    }
                    updated += stmt.executeUpdate();
                }
            }
        }
        return updated;
    }

    private static int generateSyntheticEntity(Attendee externalAttendee) {
        String uri = externalAttendee.getUri();
        return -1 * Math.abs(null != uri ? uri.hashCode() : 1);
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
        Map<Integer, Set<Integer>> groups = new HashMap<Integer, Set<Integer>>();
        for (Attendee attendee : attendees) {
            if (CalendarUserType.GROUP.equals(attendee.getCuType()) && 0 < attendee.getEntity()) {
                Context context = Services.getService(ContextService.class).getContext(contextID);
                Group group = Services.getService(GroupService.class).getGroup(context, attendee.getEntity());
                HashSet<Integer> members = new HashSet<Integer>(Arrays.asList(Autoboxing.i2I(group.getMember())));
                groups.put(Autoboxing.I(group.getIdentifier()), members);
            }
        }
        return groups;
    }

    private static int insertEvent(Connection connection, int contextID, int objectID, Event event) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement("INSERT INTO prg_dates " +
            "(creating_date,created_from,changing_date,changed_from,fid,pflag,cid,timestampfield01,timestampfield02,timezone,intfield01," +
            "intfield02,intfield03,intfield04,intfield05,intfield06,intfield07,intfield08,field01,field02,field04,field06,field07,field08," +
            "field09,uid,organizer,sequence,organizerId,principal,principalId,filename) " +
            "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);")) {
            int parameterIndex = 1;
            stmt.setTimestamp(parameterIndex++, new Timestamp(event.getCreated().getTime()));
            stmt.setInt(parameterIndex++, event.getCreatedBy());
            stmt.setLong(parameterIndex++, event.getLastModified().getTime());
            stmt.setInt(parameterIndex++, event.getModifiedBy());
            stmt.setInt(parameterIndex++, event.getPublicFolderId());
            if (null != event.getClassification() && Event2Appointment.getPrivateFlag(event.getClassification())) {
                stmt.setInt(parameterIndex++, 1);
            } else {
                stmt.setInt(parameterIndex++, 0);
            }
            stmt.setInt(parameterIndex++, contextID);
            stmt.setTimestamp(parameterIndex++, new Timestamp(event.getStartDate().getTime()));
            stmt.setTimestamp(parameterIndex++, new Timestamp(event.getEndDate().getTime()));
            stmt.setString(parameterIndex++, event.getStartTimezone());
            stmt.setInt(parameterIndex++, objectID);
            stmt.setInt(parameterIndex++, event.getRecurrenceId());
            stmt.setInt(parameterIndex++, Event2Appointment.getColorLabel(event.getColor()));
            stmt.setInt(parameterIndex++, 0); // intfield04
            stmt.setInt(parameterIndex++, 0); // intfield05
            stmt.setInt(parameterIndex++, null != event.getStatus() ? Event2Appointment.getShownAs(event.getStatus()) : 0);
            stmt.setInt(parameterIndex++, event.isAllDay() ? 1 : 0);
            stmt.setInt(parameterIndex++, 0); // intfield08
            stmt.setString(parameterIndex++, event.getSummary());
            stmt.setString(parameterIndex++, event.getLocation());
            stmt.setString(parameterIndex++, event.getDescription());
            if (null != event.getRecurrenceRule()) {
                stmt.setString(parameterIndex++, Event2Appointment.getSeriesPattern(event.getRecurrenceRule()).toString());
            } else {
                stmt.setString(parameterIndex++, null);
            }
            if (null != event.getDeleteExceptionDates()) {
                stmt.setString(parameterIndex++, null); //todo
            } else {
                stmt.setString(parameterIndex++, null);
            }
            if (null != event.getChangeExceptionDates()) {
                stmt.setString(parameterIndex++, null); //todo
            } else {
                stmt.setString(parameterIndex++, null);
            }
            stmt.setString(parameterIndex++, Event2Appointment.getCategories(event.getCategories()));
            stmt.setString(parameterIndex++, event.getUid());
            stmt.setString(parameterIndex++, null != event.getOrganizer() ? Event2Appointment.getEMailAddress(event.getOrganizer().getUri()) : null);
            stmt.setInt(parameterIndex++, null != event.getSequence() ? event.getSequence().intValue() : 0);
            stmt.setInt(parameterIndex++, null != event.getOrganizer() ? event.getOrganizer().getEntity() : 0);
            stmt.setString(parameterIndex++, null); // principal
            stmt.setInt(parameterIndex++, 1); // principalId
            stmt.setString(parameterIndex++, event.getFilename());
            return stmt.executeUpdate();
        }
    }

    private static List<Alarm> selectAlarms(Connection connection, int contextID, int objectID, int userID) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement("SELECT reminder FROM prg_dates_members WHERE cid=? AND object_id=? AND member_uid=?;")) {
            stmt.setInt(1, contextID);
            stmt.setInt(2, objectID);
            stmt.setInt(3, userID);
            try (ResultSet resultSet = SQL.logExecuteQuery(stmt)) {
                if (resultSet.next()) {
                    int reminder = resultSet.getInt("reminder");
                    if (false == resultSet.wasNull()) {
                        return Collections.singletonList(Appointment2Event.getAlarm(reminder));
                    }
                }
            }
        }
        return null;
    }

    private static Event selectEvent(Connection connection, int contextID, int objectID) throws SQLException {
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(SQL.SELECT_EVENT_STMT);
            stmt.setInt(1, contextID);
            stmt.setInt(2, objectID);
            ResultSet resultSet = SQL.logExecuteQuery(stmt);
            if (resultSet.next()) {
                return readEvent(resultSet);
            }
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
        return null;
    }

    private static List<Event> selectEventsInFolder(Connection connection, int contextID, int folderID, Date from, Date until) throws SQLException {
        List<Event> events = new ArrayList<Event>();
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(SQL.SELECT_EVENTS_IN_FOLDER_STMT);
            stmt.setInt(1, contextID);
            stmt.setTimestamp(2, new Timestamp(until.getTime()));
            stmt.setTimestamp(3, new Timestamp(from.getTime()));
            stmt.setInt(4, folderID);
            stmt.setInt(5, folderID);
            ResultSet resultSet = SQL.logExecuteQuery(stmt);
            while (resultSet.next()) {
                events.add(readEvent(resultSet));
            }
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
        return events;
    }

    private static List<Event> selectEventsInFolderCreatedBy(Connection connection, int contextID, int folderID, int createdBy, Date from, Date until) throws SQLException {
        //TODO
        return null;
    }

    private static List<Event> selectEventsOfUser(Connection connection, int contextID, int userID, Date from, Date until) throws SQLException {
        List<Event> events = new ArrayList<Event>();
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(SQL.SELECT_EVENTS_OF_USER_STMT);
            stmt.setInt(1, contextID);
            stmt.setTimestamp(2, new Timestamp(until.getTime()));
            stmt.setTimestamp(3, new Timestamp(from.getTime()));
            stmt.setInt(4, userID);
            ResultSet resultSet = SQL.logExecuteQuery(stmt);
            while (resultSet.next()) {
                events.add(readEvent(resultSet));
            }
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
        return events;
    }
    
    private static List<Attendee> selectInternalUserAttendees(Connection connection, int contextID, int objectID) throws SQLException {
        List<Attendee> attendees = new ArrayList<Attendee>();
        try (PreparedStatement stmt = connection.prepareStatement("SELECT * FROM prg_dates_members WHERE cid=? AND object_id=?;")) {
            stmt.setInt(1, contextID);
            stmt.setInt(2, objectID);
            try (ResultSet resultSet = SQL.logExecuteQuery(stmt)) {
                while (resultSet.next()) {
                    attendees.add(readInternalUserAttendee(resultSet));
                }
            }
        }
        return attendees;
    }

    private static List<Attendee> selectInternalNonUserAttendees(Connection connection, int contextID, int objectID) throws SQLException {
        List<Attendee> attendees = new ArrayList<Attendee>();
        try (PreparedStatement stmt = connection.prepareStatement("SELECT * FROM prg_date_rights WHERE cid=? AND object_id=? AND type in (2,3);")) {
            stmt.setInt(1, contextID);
            stmt.setInt(2, objectID);
            try (ResultSet resultSet = SQL.logExecuteQuery(stmt)) {
                while (resultSet.next()) {
                    attendees.add(readInternalNonUserAttendee(resultSet));
                }
            }
        }
        return attendees;
    }

    private static List<Attendee> selectAttendees(Connection connection, int contextID, int objectID) throws SQLException {
        List<Attendee> attendees = new ArrayList<Attendee>();
        attendees.addAll(selectExternalAttendees(connection, contextID, objectID));
        attendees.addAll(selectInternalUserAttendees(connection, contextID, objectID));
        attendees.addAll(selectInternalNonUserAttendees(connection, contextID, objectID));
        return attendees;
    }

    private static List<Attendee> selectExternalAttendees(Connection connection, int contextID, int objectID) throws SQLException {
        List<Attendee> attendees = new ArrayList<Attendee>();
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement(SQL.SELECT_EXTERNAL_ATTENDEES_STMT);
            stmt.setInt(1, contextID);
            stmt.setInt(2, objectID);
            ResultSet resultSet = SQL.logExecuteQuery(stmt);
            while (resultSet.next()) {
                attendees.add(readExternalAttendee(resultSet));
            }
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
        return attendees;
    }

    private static List<Date> parseExceptionDates(String timestamps) {

        return null;
    }

    private static List<String> parseSeparatedStrings(String strings) {
        String[] splittedStrings = Strings.splitByCommaNotInQuotes(strings);
        return null == splittedStrings ? null : Arrays.asList(splittedStrings);
    }

    private static Attendee readExternalAttendee(ResultSet resultSet) throws SQLException {
        Attendee attendee = new Attendee();
        String mailAddress = resultSet.getString("mailAddress");
        if (null != mailAddress) {
            attendee.setUri("mailto:" + mailAddress);
        }
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
        String mailAddress = resultSet.getString("ma");
        if (null != mailAddress) {
            attendee.setUri("mailto:" + mailAddress);
        }
        attendee.setCommonName(resultSet.getString("dn"));
        return attendee;
    }

    private static Event readEvent(ResultSet resultSet) throws SQLException {
        Event event = new Event();
        event.setId(resultSet.getInt("intfield01"));
        event.setCreated(resultSet.getTimestamp("creating_date"));
        event.setCreatedBy(resultSet.getInt("created_from"));
        event.setLastModified(new Date(resultSet.getLong("changing_date")));
        event.setModifiedBy(resultSet.getInt("changed_from"));
        event.setPublicFolderId(resultSet.getInt("fid"));
        event.setClassification(Appointment2Event.getClassification(resultSet.getBoolean("pflag")));
        event.setStartDate(resultSet.getTimestamp("timestampfield01"));
        event.setEndDate(resultSet.getTimestamp("timestampfield02"));
        event.setStartTimezone(resultSet.getString("timezone"));
        event.setRecurrenceId(resultSet.getInt("intfield02"));
        event.setColor(Appointment2Event.getColor(resultSet.getInt("intfield03")));
        // intfield04
        // intfield05
        event.setStatus(Appointment2Event.getEventStatus(resultSet.getInt("intfield06")));
        event.setAllDay(resultSet.getBoolean("intfield07"));
        // intfield08
        event.setSummary(resultSet.getString("field01"));
        event.setLocation(resultSet.getString("field02"));
        event.setDescription(resultSet.getString("field04"));
        event.setRecurrenceRule(Appointment2Event.getRecurrenceRule(SeriesPattern.parse(resultSet.getString("field06"))));
        event.setDeleteExceptionDates(parseExceptionDates(resultSet.getString("field07")));
        event.setChangeExceptionDates(parseExceptionDates(resultSet.getString("field08")));
        event.setCategories(parseSeparatedStrings(resultSet.getString("field09")));
        event.setUid(resultSet.getString("uid"));
        String organizerMail = resultSet.getString("organizer");
        int organizerId = resultSet.getInt("organizerId");
        if (Strings.isNotEmpty(organizerMail) || 0 < organizerId) {
            Organizer organizer = new Organizer();
            if (Strings.isNotEmpty(organizerMail)) {
                organizer.setUri("mailto:" + organizerMail);
            }
            if (0 < organizerId) {
                organizer.setEntity(organizerId);
            }
            event.setOrganizer(organizer);
        }
        int sequence = resultSet.getInt("sequence");
        if (false == resultSet.wasNull()) {
            event.setSequence(Integer.valueOf(sequence));
        }
        // principal
        // principalId
        event.setFilename(resultSet.getString("filename"));

        //oo
        if (Strings.isNotEmpty(resultSet.getString("field06"))) {
            //            SeriesPattern pattern = SeriesPattern.parse(resultSet.getString("field06"));
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
        //oo

        return event;
    }

}
