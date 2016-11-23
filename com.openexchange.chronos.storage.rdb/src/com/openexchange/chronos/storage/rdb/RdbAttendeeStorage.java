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

import static com.openexchange.chronos.common.CalendarUtils.isInternal;
import static com.openexchange.java.Autoboxing.I;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.AttendeeField;
import com.openexchange.chronos.CalendarUserType;
import com.openexchange.chronos.compat.Event2Appointment;
import com.openexchange.chronos.service.EntityResolver;
import com.openexchange.chronos.storage.AttendeeStorage;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.chronos.storage.rdb.exception.EventExceptionCode;
import com.openexchange.database.provider.DBProvider;
import com.openexchange.database.provider.DBTransactionPolicy;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;

/**
 * {@link CalendarStorage}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class RdbAttendeeStorage extends RdbStorage implements AttendeeStorage {

    /**
     * Initializes a new {@link RdbAttendeeStorage}.
     *
     * @param context The context
     * @param entityResolver The entity resolver to use
     * @param dbProvider The database provider to use
     * @param The transaction policy
     */
    public RdbAttendeeStorage(Context context, EntityResolver entityResolver, DBProvider dbProvider, DBTransactionPolicy txPolicy) {
        super(context, entityResolver, dbProvider, txPolicy);
    }

    @Override
    public void insertAttendees(int objectID, List<Attendee> attendees) throws OXException {
        int updated = 0;
        Connection connection = null;
        try {
            connection = dbProvider.getWriteConnection(context);
            txPolicy.setAutoCommit(connection, false);
            updated += insertOrReplaceAttendees(connection, false, false, context.getContextId(), objectID, attendees);
            txPolicy.commit(connection);
        } catch (SQLException e) {
            throw EventExceptionCode.MYSQL.create(e);
        } finally {
            release(connection, updated);
        }
    }

    @Override
    public void updateAttendee(int objectID, Attendee attendee) throws OXException {
        updateAttendees(objectID, Collections.singletonList(attendee));
    }

    @Override
    public void updateAttendees(int objectID, List<Attendee> attendees) throws OXException {
        int updated = 0;
        Connection connection = null;
        try {
            connection = dbProvider.getWriteConnection(context);
            txPolicy.setAutoCommit(connection, false);
            updated += updateAttendees(connection, context.getContextId(), objectID, attendees);
            txPolicy.commit(connection);
        } catch (SQLException e) {
            throw EventExceptionCode.MYSQL.create(e);
        } finally {
            release(connection, updated);
        }
    }

    @Override
    public void insertTombstoneAttendees(int objectID, List<Attendee> attendees) throws OXException {
        int updated = 0;
        Connection connection = null;
        try {
            connection = dbProvider.getWriteConnection(context);
            txPolicy.setAutoCommit(connection, false);
            updated += insertTombstoneAttendees(connection, context.getContextId(), objectID, attendees);
            txPolicy.commit(connection);
        } catch (SQLException e) {
            throw EventExceptionCode.MYSQL.create(e);
        } finally {
            release(connection, updated);
        }
    }

    @Override
    public void insertTombstoneAttendee(int objectID, Attendee attendee) throws OXException {
        insertTombstoneAttendees(objectID, Collections.singletonList(attendee));
    }

    @Override
    public List<Attendee> loadAttendees(int objectID) throws OXException {
        Connection connection = null;
        try {
            connection = dbProvider.getReadConnection(context);
            return new AttendeeLoader(connection, entityResolver).loadAttendees(objectID, AttendeeField.values());
        } finally {
            dbProvider.releaseReadConnection(context, connection);
        }
    }

    @Override
    public Map<Integer, List<Attendee>> loadAttendees(int[] objectIDs) throws OXException {
        Connection connection = null;
        try {
            connection = dbProvider.getReadConnection(context);
            return new AttendeeLoader(connection, entityResolver).loadAttendees(objectIDs, AttendeeField.values());
        } finally {
            dbProvider.releaseReadConnection(context, connection);
        }
    }

    @Override
    public void deleteAttendees(int objectID, List<Attendee> attendees) throws OXException {
        int updated = 0;
        Connection connection = null;
        try {
            connection = dbProvider.getWriteConnection(context);
            txPolicy.setAutoCommit(connection, false);
            updated = deleteAttendees(connection, context.getContextId(), objectID, attendees);
            txPolicy.commit(connection);
        } catch (SQLException e) {
            throw EventExceptionCode.MYSQL.create(e);
        } finally {
            release(connection, updated);
        }
    }

    @Override
    public void deleteAttendees(int objectID) throws OXException {
        int updated = 0;
        Connection connection = null;
        try {
            connection = dbProvider.getWriteConnection(context);
            txPolicy.setAutoCommit(connection, false);
            updated = deleteAttendees(connection, context.getContextId(), objectID);
            txPolicy.commit(connection);
        } catch (SQLException e) {
            throw EventExceptionCode.MYSQL.create(e);
        } finally {
            release(connection, updated);
        }
    }

    private static int deleteAttendees(Connection connection, int contextID, int objectID, List<Attendee> attendees) throws SQLException {
        int updated = 0;
        for (Attendee attendee : attendees) {
            if (0 >= attendee.getEntity()) {
                /*
                 * delete records in dateExternal and prg_date_rights for external users
                 */
                try (PreparedStatement stmt = connection.prepareStatement("DELETE FROM dateExternal WHERE cid=? AND objectId=? AND mailAddress=?;")) {
                    stmt.setInt(1, contextID);
                    stmt.setInt(2, objectID);
                    stmt.setString(3, Event2Appointment.getEMailAddress(attendee.getUri()));
                    updated += logExecuteUpdate(stmt);
                }
                try (PreparedStatement stmt = connection.prepareStatement("DELETE FROM prg_date_rights WHERE cid=? AND object_id=? AND ma=?;")) {
                    stmt.setInt(1, contextID);
                    stmt.setInt(2, objectID);
                    stmt.setString(3, Event2Appointment.getEMailAddress(attendee.getUri()));
                    updated += logExecuteUpdate(stmt);
                }
            } else {
                /*
                 * delete record in prg_dates_members for each internal user
                 */
                if (CalendarUserType.INDIVIDUAL.equals(attendee.getCuType())) {
                    try (PreparedStatement stmt = connection.prepareStatement("DELETE FROM prg_dates_members WHERE cid=? AND object_id=? AND member_uid=?;")) {
                        stmt.setInt(1, contextID);
                        stmt.setInt(2, objectID);
                        stmt.setInt(3, attendee.getEntity());
                        updated += logExecuteUpdate(stmt);
                    }
                }
                /*
                 * delete record in prg_date_rights for each attendee, skipping group members
                 */
                if (null == attendee.getMember()) {
                    try (PreparedStatement stmt = connection.prepareStatement("DELETE FROM prg_date_rights WHERE cid=? AND object_id=? AND id=?;")) {
                        stmt.setInt(1, contextID);
                        stmt.setInt(2, objectID);
                        stmt.setInt(3, attendee.getEntity());
                        updated += logExecuteUpdate(stmt);
                    }
                }
            }
        }
        return updated;
    }

    private static int updateAttendees(Connection connection, int contextID, int objectID, List<Attendee> attendees) throws SQLException, OXException {
        int updated = 0;
        for (Attendee attendee : attendees) {
            if (0 >= attendee.getEntity()) {
                /*
                 * update records in dateExternal for external users
                 */
                ExternalAttendeeMapper mapper = ExternalAttendeeMapper.getInstance();
                AttendeeField[] fields = mapper.getMappedFields(mapper.getAssignedFields(attendee));
                String sql = new StringBuilder()
                    .append("UPDATE dateExternal SET ").append(mapper.getAssignments(fields))
                    .append(" WHERE cid=? AND objectId=? AND mailAddress=?;")
                .toString();
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    int parameterIndex = 1;
                    parameterIndex = mapper.setParameters(stmt, parameterIndex, attendee, fields);
                    stmt.setInt(parameterIndex++, contextID);
                    stmt.setInt(parameterIndex++, objectID);
                    stmt.setString(parameterIndex++, Event2Appointment.getEMailAddress(attendee.getUri()));
                    updated += logExecuteUpdate(stmt);
                }
            } else if (CalendarUserType.INDIVIDUAL.equals(attendee.getCuType())) {
                /*
                 * update record in prg_dates_members for internal users
                 */
                InternalAttendeeMapper mapper = InternalAttendeeMapper.getInstance();
                AttendeeField[] fields = mapper.getMappedFields(mapper.getAssignedFields(attendee));
                String sql = new StringBuilder()
                    .append("UPDATE prg_dates_members SET ").append(mapper.getAssignments(fields))
                    .append(" WHERE cid=? AND object_id=? AND member_uid=?;")
                .toString();
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    int parameterIndex = 1;
                    parameterIndex = mapper.setParameters(stmt, parameterIndex, attendee, fields);
                    stmt.setInt(parameterIndex++, contextID);
                    stmt.setInt(parameterIndex++, objectID);
                    stmt.setInt(parameterIndex++, attendee.getEntity());
                    updated += logExecuteUpdate(stmt);
                }
            }
        }
        return updated;
    }

    private static int deleteAttendees(Connection connection, int contextID, int objectID) throws SQLException {
        int updated = 0;
        try (PreparedStatement stmt = connection.prepareStatement("DELETE FROM dateExternal WHERE cid=? AND objectId=?;")) {
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
            stmt.setString(parameterIndex++, attendee.getCn());
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
            if (isInternal(attendee)) {
                stmt.setInt(parameterIndex++, entity);
                stmt.setInt(parameterIndex++, Event2Appointment.getParticipantType(attendee.getCuType(), true));
                stmt.setNull(parameterIndex++, java.sql.Types.VARCHAR);
                stmt.setNull(parameterIndex++, java.sql.Types.VARCHAR);
            } else {
                stmt.setInt(parameterIndex++, entity);
                stmt.setInt(parameterIndex++, Event2Appointment.getParticipantType(attendee.getCuType(), false));
                stmt.setString(parameterIndex++, Event2Appointment.getEMailAddress(attendee.getUri()));
                stmt.setString(parameterIndex++, attendee.getCn());
            }
            return logExecuteUpdate(stmt);
        }
    }

    private static int insertTombstoneAttendees(Connection connection, int contextID, int objectID, List<Attendee> attendees) throws SQLException, OXException {
        return insertOrReplaceAttendees(connection, true, true, contextID, objectID, attendees);
    }

    private static int insertOrReplaceAttendees(Connection connection, boolean deleted, boolean replace, int contextID, int objectID, List<Attendee> attendees) throws SQLException, OXException {
        int updated = 0;
        Set<Integer> usedEntities = new HashSet<Integer>();
        for (Attendee attendee : attendees) {
            if (0 > attendee.getEntity() || 0 == attendee.getEntity() && false == CalendarUserType.GROUP.equals(attendee.getCuType())) {
                /*
                 * insert additional record into dateExternal for external users
                 */
                updated += insertOrReplaceDateExternal(connection, deleted ? "delDateExternal" : "dateExternal", replace, contextID, objectID, attendee);
            } else if (CalendarUserType.INDIVIDUAL.equals(attendee.getCuType())) {
                /*
                 * insert additional record into prg_dates_members for each internal user
                 */
                updated += insertOrReplaceDatesMembers(connection, deleted ? "del_dates_members" : "prg_dates_members", replace, contextID, objectID, attendee);
            }
            if (null == attendee.getMember()) {
                /*
                 * insert record into prg_date_rights for each attendee, skipping group members
                 */
                int entity = determineEntity(attendee, usedEntities);
                updated += insertOrReplaceDateRights(connection, deleted ? "del_date_rights" : "prg_date_rights", replace, contextID, objectID, entity, attendee);
            }
        }
        return updated;
    }

    /**
     * Determines the next unique entity identifier to use when inserting an entry into the <code>prg_date_rights</code> table. For
     * <i>internal</i> attendees, this is always the (already unique) entity identifier itself. For <i>external</i> attendees, the
     * identifier is always negative and based on the hash code of the URI.
     *
     * @param attendee The attendee to determine the entity for
     * @param usedEntities The so far used entities to avoid hash collisions
     * @return The entity
     */
    private static int determineEntity(Attendee attendee, Set<Integer> usedEntities) {
        if (isInternal(attendee)) {
            usedEntities.add((attendee.getEntity()));
            return attendee.getEntity();
        } else {
            String uri = attendee.getUri();
            int entity = -1 * Math.abs(null != uri ? uri.hashCode() : 1);
            while (false == usedEntities.add(I(entity))) {
                entity--;
            }
            return entity;
        }
    }

}
