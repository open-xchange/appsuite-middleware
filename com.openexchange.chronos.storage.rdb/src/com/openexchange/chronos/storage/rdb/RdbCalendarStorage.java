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
import java.util.Collections;
import java.util.Date;
import java.util.List;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.CalendarStorage;
import com.openexchange.chronos.CalendarUserType;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.Organizer;
import com.openexchange.chronos.ParticipationStatus;
import com.openexchange.chronos.compat.Appointment2Event;
import com.openexchange.chronos.compat.Recurrence;
import com.openexchange.chronos.storage.rdb.exception.EventExceptionCode;
import com.openexchange.exception.OXException;
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

    /**
     * Initializes a new {@link RdbChecksumStore}.
     *
     * @param contextID The context ID
     */
    public RdbCalendarStorage(int contextID) throws OXException {
        super(contextID);
    }

    public Event loadEvent(int objectID) throws OXException {
        try (Connection connection = getConnection(false)) {
            Event event = selectEvent(connection, contextID, objectID);
            event.setAttendees(selectAttendees(connection, contextID, objectID));
            return event;
        } catch (SQLException e) {
            throw EventExceptionCode.MYSQL.create(e);
        } finally {
            close();
        }
    }

    public List<Alarm> loadAlarms(int objectID, int userID) throws OXException {
        try (Connection connection = getConnection(false)) {
            return selectAlarms(connection, contextID, userID, objectID);
        } catch (SQLException e) {
            throw EventExceptionCode.MYSQL.create(e);
        } finally {
            close();
        }
    }

    @Override
    public List<Event> loadEventsInFolder(int folderID, Date from, Date until) throws OXException {
        try (Connection connection = getConnection(false)) {
            List<Event> events = selectEventsInFolder(connection, contextID, folderID, from, until);
            for (Event event : events) {
                event.setAttendees(selectAttendees(connection, contextID, event.getId()));
            }
            return events;
        } catch (SQLException e) {
            throw EventExceptionCode.MYSQL.create(e);
        } finally {
            close();
        }
    }

    @Override
    public List<Event> loadEventsInFolderCreatedBy(int folderID, int createdBy, Date from, Date until) throws OXException {
        try (Connection connection = getConnection(false)) {
            List<Event> events = selectEventsInFolderCreatedBy(connection, contextID, folderID, createdBy, from, until);
            for (Event event : events) {
                event.setAttendees(selectAttendees(connection, contextID, event.getId()));
            }
            return events;
        } catch (SQLException e) {
            throw EventExceptionCode.MYSQL.create(e);
        } finally {
            close();
        }
    }

    @Override
    public List<Event> loadEventsOfUser(int userID, Date from, Date until) throws OXException {
        try (Connection connection = getConnection(false)) {
            List<Event> events = selectEventsOfUser(connection, contextID, userID, from, until);
            for (Event event : events) {
                event.setAttendees(selectAttendees(connection, contextID, event.getId()));
            }
            return events;
        } catch (SQLException e) {
            throw new OXException(e);
        } finally {
            close();
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
        event.setRecurrenceRule(Recurrence.getRecurrenceRule(resultSet.getString("field06")));
        event.setDeleteExceptionDates(parseExceptionDates(resultSet.getString("field07")));
        event.setChangeExceptionDates(parseExceptionDates(resultSet.getString("field08")));
        event.setCategories(parseSeparatedStrings(resultSet.getString("field09")));
        event.setUid(resultSet.getString("uid"));
        String organizerMail = resultSet.getString("organizer");
        int organizerId = resultSet.getInt("organizerId");
        if (Strings.isNotEmpty(organizerMail) || 0 < organizerId) {
            Organizer organizer = new Organizer();
            organizer.setCuType(CalendarUserType.INDIVIDUAL);
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
        return event;
    }

}
