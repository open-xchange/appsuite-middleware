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

import static com.openexchange.chronos.common.CalendarUtils.filter;
import static com.openexchange.chronos.common.CalendarUtils.find;
import static com.openexchange.chronos.storage.rdb.SQL.logExecuteQuery;
import static com.openexchange.groupware.tools.mappings.database.DefaultDbMapper.getParameters;
import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.tools.arrays.Collections.put;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.AttendeeField;
import com.openexchange.chronos.CalendarUserType;
import com.openexchange.chronos.ResourceId;
import com.openexchange.chronos.compat.Appointment2Event;
import com.openexchange.chronos.service.EntityResolver;
import com.openexchange.chronos.storage.rdb.exception.EventExceptionCode;
import com.openexchange.exception.OXException;

/**
 * {@link AttendeeLoader}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class AttendeeLoader {

    public static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AttendeeLoader.class);

    private final EntityResolver entityResolver;
    private final Connection connection;
    private final int contextID;

    /**
     * Initializes a new {@link AttendeeLoader}.
     *
     * @param connection A (readable) database connection
     * @param entityResolver The entity resolver to use
     */
    public AttendeeLoader(Connection connection, EntityResolver entityResolver) throws OXException {
        super();
        this.connection = connection;
        this.entityResolver = entityResolver;
        this.contextID = entityResolver.getContextID();
    }

    /**
     * Loads the attendees for a specific event.
     *
     * @param objectID The object identifier of the event to load the attendees for
     * @param fields The attendee fields to load, or <code>null</code> to load all available fields
     * @return The attendees
     */
    public List<Attendee> loadAttendees(int objectID, AttendeeField[] fields) throws OXException {
        return loadAttendees(new int[] { objectID }, fields).get(I(objectID));
    }

    /**
     * Loads the attendees for multiple events.
     *
     * @param objectIDs The object identifiers of the events to load the attendees for
     * @param fields The attendee fields to load, or <code>null</code> to load all available fields
     * @return The attendee lists mapped to the corresponding event identifier
     */
    public Map<Integer, List<Attendee>> loadAttendees(int objectIDs[], AttendeeField[] fields) throws OXException {
        Map<Integer, List<Attendee>> attendeesById = new HashMap<Integer, List<Attendee>>();
        try {
            /*
             * select raw attendee data & pre-fetch referenced internal entities
             */
            Map<Integer, List<Attendee>> userAttendeeData = selectUserAttendeeData(objectIDs);
            Map<Integer, List<Attendee>> internalAttendeeData = selectInternalAttendeeData(objectIDs);
            Map<Integer, List<Attendee>> externalAttendeeData = selectExternalAttendeeData(objectIDs);
            prefetchEntities(internalAttendeeData.values(), userAttendeeData.values());
            /*
             * generate resulting attendee lists per object ID
             */
            for (int objectID : objectIDs) {
                Integer key = I(objectID);
                attendeesById.put(key, getAttendees(internalAttendeeData.get(key), userAttendeeData.get(key), externalAttendeeData.get(key)));
            }
        } catch (SQLException e) {
            throw EventExceptionCode.MYSQL.create(e);
        }
        return attendeesById;
    }

    private int findGroupID(List<Attendee> internalAttendees, int member) throws OXException {
        for (Attendee internalAttendee : filter(internalAttendees, Boolean.TRUE, CalendarUserType.GROUP)) {
            int[] members = entityResolver.getGroupMembers(internalAttendee.getEntity());
            if (0 < Arrays.binarySearch(members, member)) {
                return internalAttendee.getEntity();
            }
        }
        return -1;
    }

    private List<Attendee> getAttendees(List<Attendee> internalAttendees, List<Attendee> userAttendees, List<Attendee> externalAttendees) throws OXException {
        List<Attendee> attendees = new ArrayList<Attendee>();
        /*
         * add user attendees individually if listed in internal attendees, or as member of a group if not
         */
        if (null != userAttendees) {
            for (Attendee userAttendee : userAttendees) {
                if (null != find(internalAttendees, userAttendee.getEntity())) {
                    attendees.add(entityResolver.applyEntityData(userAttendee));
                } else {
                    int groupID = findGroupID(internalAttendees, userAttendee.getEntity());
                    if (0 < groupID) {
                        userAttendee.setMember(ResourceId.forGroup(contextID, groupID));
                        attendees.add(entityResolver.applyEntityData(userAttendee));
                    }
                }
            }
        }
        /*
         * add other internal, non-user attendees as well as external attendees as-is
         */
        if (null != internalAttendees) {
            for (Attendee internalAttendee : internalAttendees) {
                if (false == CalendarUserType.INDIVIDUAL.equals(internalAttendee.getCuType())) {
                    attendees.add(entityResolver.applyEntityData(internalAttendee));
                }
            }
        }
        if (null != externalAttendees) {
            attendees.addAll(externalAttendees);
        }
        return attendees;
    }

    private void prefetchEntities(Collection<List<Attendee>> internalAttendees, Collection<List<Attendee>> userAttendees) throws OXException {
        if (null != internalAttendees && null != userAttendees) {
            List<Attendee> attendees = new ArrayList<Attendee>();
            for (List<Attendee> attendeeList : internalAttendees) {
                attendees.addAll(attendeeList);
            }
            for (List<Attendee> attendeeList : userAttendees) {
                attendees.addAll(attendeeList);
            }
            entityResolver.prefetch(attendees);
        }
    }

    private Map<Integer, List<Attendee>> selectInternalAttendeeData(int objectIDs[]) throws SQLException, OXException {
        Map<Integer, List<Attendee>> attendeesByObjectId = new HashMap<Integer, List<Attendee>>(objectIDs.length);
        String sql =
            new StringBuilder().append("SELECT object_id,id,type,ma,dn FROM prg_date_rights ")
            .append("WHERE cid=? AND object_id IN (").append(getParameters(objectIDs.length)).append(") AND type IN (1,2,3);")
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
                    attendee.setCn(resultSet.getString("dn"));
                    put(attendeesByObjectId, I(resultSet.getInt("object_id")), attendee);
                }
            }
        }
        return attendeesByObjectId;
    }

    private Map<Integer, List<Attendee>> selectUserAttendeeData(int objectIDs[]) throws SQLException, OXException {
        Map<Integer, List<Attendee>> attendeesByObjectId = new HashMap<Integer, List<Attendee>>(objectIDs.length);
        String sql = new StringBuilder()
            .append("SELECT object_id,member_uid,confirm,reason,pfid FROM prg_dates_members ")
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
                    Attendee attendee = new Attendee();
                    attendee.setEntity(resultSet.getInt("member_uid"));
                    attendee.setCuType(CalendarUserType.INDIVIDUAL);
                    attendee.setPartStat(Appointment2Event.getParticipationStatus(resultSet.getInt("confirm")));
                    attendee.setComment(resultSet.getString("reason"));
                    attendee.setFolderID(resultSet.getInt("pfid"));
                    put(attendeesByObjectId, I(resultSet.getInt("object_id")), attendee);
                }
            }
        }
        return attendeesByObjectId;
    }

    private Map<Integer, List<Attendee>> selectExternalAttendeeData(int objectIDs[]) throws SQLException, OXException {
        Map<Integer, List<Attendee>> attendeesByObjectId = new HashMap<Integer, List<Attendee>>(objectIDs.length);
        String sql = new StringBuilder()
            .append("SELECT objectId,mailAddress,displayName,confirm,reason FROM dateexternal ")
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
                    Attendee attendee = new Attendee();
                    attendee.setCuType(CalendarUserType.INDIVIDUAL);
                    attendee.setUri(Appointment2Event.getURI(resultSet.getString("mailAddress")));
                    attendee.setCn(resultSet.getString("displayName"));
                    attendee.setPartStat(Appointment2Event.getParticipationStatus(resultSet.getInt("confirm")));
                    attendee.setComment(resultSet.getString("reason"));
                    put(attendeesByObjectId, I(resultSet.getInt("objectId")), attendee);
                }
            }
        }
        return attendeesByObjectId;
    }

}
