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

import static com.openexchange.chronos.common.CalendarUtils.isInternal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.AttendeeField;
import com.openexchange.chronos.ParticipationStatus;
import com.openexchange.chronos.service.EntityResolver;
import com.openexchange.chronos.storage.AttendeeStorage;
import com.openexchange.database.provider.DBProvider;
import com.openexchange.database.provider.DBTransactionPolicy;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.tools.arrays.Collections;

/**
 * {@link RdbAttendeeStorage}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class RdbAttendeeStorage extends RdbStorage implements AttendeeStorage {

    private static final int INSERT_CHUNK_SIZE = 200;
    private static final int DELETE_CHUNK_SIZE = 200;
    private static final AttendeeMapper MAPPER = AttendeeMapper.getInstance();

    private final int accountId;
    private final EntityProcessor entityProcessor;

    /**
     * Initializes a new {@link RdbAttendeeStorage}.
     *
     * @param context The context
     * @param accountId The account identifier
     * @param entityResolver The entity resolver to use, or <code>null</code> if not available
     * @param dbProvider The database provider to use
     * @param txPolicy The transaction policy
     */
    public RdbAttendeeStorage(Context context, int accountId, EntityResolver entityResolver, DBProvider dbProvider, DBTransactionPolicy txPolicy) {
        super(context, dbProvider, txPolicy);
        this.accountId = accountId;
        this.entityProcessor = new EntityProcessor(context.getContextId(), entityResolver);
    }

    @Override
    public List<Attendee> loadAttendees(String eventId) throws OXException {
        return loadAttendees(new String[] { eventId }).get(eventId);
    }

    @Override
    public Map<String, List<Attendee>> loadAttendees(String[] eventIds) throws OXException {
        return loadAttendees(eventIds, null);
    }

    @Override
    public Map<String, List<Attendee>> loadAttendees(String[] eventIds, Boolean internal) throws OXException {
        Connection connection = null;
        try {
            connection = dbProvider.getReadConnection(context);
            return selectAttendees(connection, eventIds, internal, false, null);
        } catch (SQLException e) {
            throw asOXException(e);
        } finally {
            dbProvider.releaseReadConnection(context, connection);
        }
    }

    @Override
    public Map<String, ParticipationStatus> loadPartStats(String[] eventIds, Attendee attendee) throws OXException {
        Connection connection = null;
        try {
            connection = dbProvider.getReadConnection(context);
            return selectPartStats(connection, eventIds, attendee);
        } catch (SQLException e) {
            throw asOXException(e);
        } finally {
            dbProvider.releaseReadConnection(context, connection);
        }
    }

    @Override
    public Map<String, List<Attendee>> loadAttendeeTombstones(String[] eventIds) throws OXException {
        Connection connection = null;
        try {
            connection = dbProvider.getReadConnection(context);
            return selectAttendees(connection, eventIds, null, false, null);
        } catch (SQLException e) {
            throw asOXException(e);
        } finally {
            dbProvider.releaseReadConnection(context, connection);
        }
    }

    @Override
    public void insertAttendees(String eventId, List<Attendee> attendees) throws OXException {
        if (null != attendees) {
            insertAttendees(java.util.Collections.singletonMap(eventId, attendees), false);
        }
    }

    @Override
    public void insertAttendees(Map<String, List<Attendee>> attendeesByEventId) throws OXException {
        insertAttendees(attendeesByEventId, false);
    }

    @Override
    public void deleteAttendees(String eventId) throws OXException {
        deleteAttendees(java.util.Collections.singletonList(eventId));
    }

    @Override
    public void deleteAttendees(List<String> eventIds) throws OXException {
        int updated = 0;
        Connection connection = null;
        try {
            connection = dbProvider.getWriteConnection(context);
            txPolicy.setAutoCommit(connection, false);
            for (List<String> chunk : Lists.partition(eventIds, DELETE_CHUNK_SIZE)) {
                updated += deleteAttendees(connection, chunk);
            }
            txPolicy.commit(connection);
        } catch (SQLException e) {
            throw asOXException(e, MAPPER, (Attendee) null, connection, "calendar_attendee");
        } finally {
            release(connection, updated);
        }
    }

    @Override
    public void deleteAttendees(String eventId, List<Attendee> attendees) throws OXException {
        if (null == attendees || 0 == attendees.size()) {
            return;
        }
        int updated = 0;
        Connection connection = null;
        try {
            connection = dbProvider.getWriteConnection(context);
            txPolicy.setAutoCommit(connection, false);
            updated += deleteAttendees(connection, eventId, attendees);
            txPolicy.commit(connection);
        } catch (SQLException e) {
            throw asOXException(e);
        } finally {
            release(connection, updated);
        }
    }

    @Override
    public void deleteAllAttendees() throws OXException {
        int updated = 0;
        Connection connection = null;
        try {
            connection = dbProvider.getWriteConnection(context);
            txPolicy.setAutoCommit(connection, false);
            updated += deleteAttendees(connection);
            updated += deleteAttendeesTombstones(connection);
            txPolicy.commit(connection);
        } catch (SQLException e) {
            throw asOXException(e);
        } finally {
            release(connection, updated);
        }
    }

    private int deleteAttendees(Connection connection) throws SQLException {
        int updated = 0;
        try (PreparedStatement stmt = connection.prepareStatement("DELETE FROM calendar_attendee WHERE cid=? AND account=?;")) {
            stmt.setInt(1, context.getContextId());
            stmt.setInt(2, accountId);
            updated += logExecuteUpdate(stmt);
        }
        return updated;
    }

    private int deleteAttendeesTombstones(Connection connection) throws SQLException {
        int updated = 0;
        try (PreparedStatement stmt = connection.prepareStatement("DELETE FROM calendar_attendee_tombstone WHERE cid=? AND account=?;")) {
            stmt.setInt(1, context.getContextId());
            stmt.setInt(2, accountId);
            updated += logExecuteUpdate(stmt);
        }
        return updated;
    }

    @Override
    public void updateAttendee(String eventId, Attendee attendee) throws OXException {
        updateAttendees(eventId, java.util.Collections.singletonList(attendee));
    }

    @Override
    public void updateAttendees(String eventId, List<Attendee> attendees) throws OXException {
        int updated = 0;
        Connection connection = null;
        try {
            connection = dbProvider.getWriteConnection(context);
            txPolicy.setAutoCommit(connection, false);
            updated += updateAttendees(connection, eventId, attendees);
            txPolicy.commit(connection);
        } catch (SQLException e) {
            throw asOXException(e);
        } finally {
            release(connection, updated);
        }
    }

    @Override
    public void insertAttendeeTombstone(String eventId, Attendee attendee) throws OXException {
        insertAttendeeTombstones(eventId, java.util.Collections.singletonList(attendee));
    }

    @Override
    public void insertAttendeeTombstones(String eventId, List<Attendee> attendees) throws OXException {
        if (null != attendees && 0 < attendees.size()) {
            insertAttendees(java.util.Collections.singletonMap(eventId, attendees), true);
        }
    }

    @Override
    public void insertAttendeeTombstones(Map<String, List<Attendee>> attendeesByEventId) throws OXException {
        insertAttendees(attendeesByEventId, true);
    }

    private void insertAttendees(Map<String, List<Attendee>> attendeesByEventId, boolean tombstones) throws OXException {
        if (null == attendeesByEventId || 0 == attendeesByEventId.size()) {
            return;
        }
        int updated = 0;
        Connection connection = null;
        try {
            connection = dbProvider.getWriteConnection(context);
            txPolicy.setAutoCommit(connection, false);
            if (1 == attendeesByEventId.size()) {
                updated = insertAttendees(connection, attendeesByEventId, tombstones);
            } else {
                updated = insertAttendees(connection, attendeesByEventId, tombstones, INSERT_CHUNK_SIZE);
            }
            txPolicy.commit(connection);
        } catch (SQLException e) {
            throw asOXException(e, MAPPER, Lists.newArrayList(Iterables.concat(attendeesByEventId.values())), connection, "calendar_attendee");
        } finally {
            release(connection, updated);
        }
    }

    private int updateAttendees(Connection connection, String eventId, List<Attendee> attendees) throws OXException {
        int updated = 0;
        for (Attendee attendee : attendees) {
            AttendeeField[] fields = MAPPER.getMappedFields(MAPPER.getAssignedFields(attendee));
            String sql = new StringBuilder()
                .append("UPDATE calendar_attendee SET ").append(MAPPER.getAssignments(fields))
                .append(" WHERE cid=? AND account=? AND event=? AND ")
                .append(isInternal(attendee) ? "entity" : "uri").append("=?;")
            .toString();
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                int parameterIndex = 1;
                parameterIndex = MAPPER.setParameters(stmt, parameterIndex, attendee, fields);
                stmt.setInt(parameterIndex++, context.getContextId());
                stmt.setInt(parameterIndex++, accountId);
                stmt.setInt(parameterIndex++, asInt(eventId));
                if (isInternal(attendee)) {
                    stmt.setInt(parameterIndex++, attendee.getEntity());
                } else {
                    stmt.setString(parameterIndex++, attendee.getUri());
                }
                updated += logExecuteUpdate(stmt);
            } catch (SQLException e) {
                throw asOXException(e, MAPPER, attendee, connection, "calendar_attendee");
            }
        }
        return updated;
    }

    private int deleteAttendees(Connection connection, List<String> eventIds) throws SQLException {
        if (null == eventIds || 0 == eventIds.size()) {
            return 0;
        }
        StringBuilder stringBuilder = new StringBuilder()
            .append("DELETE FROM calendar_attendee WHERE cid=? AND account=? AND event")
            .append(getPlaceholders(eventIds.size())).append(';');
        ;
        try (PreparedStatement stmt = connection.prepareStatement(stringBuilder.toString())) {
            int parameterIndex = 1;
            stmt.setInt(parameterIndex++, context.getContextId());
            stmt.setInt(parameterIndex++, accountId);
            for (String id : eventIds) {
                stmt.setInt(parameterIndex++, asInt(id));
            }
            return logExecuteUpdate(stmt);
        }
    }

    private int deleteAttendees(Connection connection, String eventId, List<Attendee> attendees) throws SQLException {
        int updated = 0;
        for (Attendee attendee : attendees) {
            String sql = new StringBuilder()
                .append("DELETE FROM calendar_attendee WHERE cid=? AND account=? AND event=? AND ")
                .append(isInternal(attendee) ? "entity" : "uri").append("=?;")
            .toString();
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setInt(1, context.getContextId());
                stmt.setInt(2, accountId);
                stmt.setInt(3, asInt(eventId));
                if (isInternal(attendee)) {
                    stmt.setInt(4, attendee.getEntity());
                } else {
                    stmt.setString(4, attendee.getUri());
                }
                updated += logExecuteUpdate(stmt);
            }
        }
        return updated;
    }

    private int insertAttendees(Connection connection, Map<String, List<Attendee>> attendeesByEventId, boolean tombstones, int chunkSize) throws SQLException, OXException {
        int updated = 0;
        Map<String, List<Attendee>> currentChunk = new HashMap<String, List<Attendee>>();
        int currentSize = 0;
        for (Entry<String, List<Attendee>> entry : attendeesByEventId.entrySet()) {
            /*
             * add to current chunk
             */
            currentChunk.put(entry.getKey(), entry.getValue());
            currentSize += entry.getValue().size();
            if (currentSize >= chunkSize) {
                /*
                 * insert & reset current chunk
                 */
                updated += insertAttendees(connection, currentChunk, tombstones);
                currentChunk.clear();
                currentSize = 0;
            }
        }
        /*
         * finally insert remaining chunk
         */
        if (0 < currentSize) {
            updated += insertAttendees(connection, currentChunk, tombstones);
        }
        return updated;
    }

    private int insertAttendees(Connection connection, Map<String, List<Attendee>> attendeesByEventId, boolean tombstones) throws SQLException, OXException {
        if (null == attendeesByEventId || 0 == attendeesByEventId.size()) {
            return 0;
        }
        AttendeeField[] mappedFields = MAPPER.getMappedFields();
        StringBuilder stringBuilder = new StringBuilder()
            .append(tombstones ? "REPLACE INTO calendar_attendee_tombstone " : "INSERT INTO calendar_attendee ")
            .append("(cid,account,event,").append(MAPPER.getColumns(mappedFields)).append(") VALUES ")
        ;
        for (List<Attendee> attendees : attendeesByEventId.values()) {
            for (int i = 0; i < attendees.size(); i++) {
                stringBuilder.append("(?,?,?,").append(MAPPER.getParameters(mappedFields)).append("),");
            }
        }
        stringBuilder.setLength(stringBuilder.length() - 1);
        stringBuilder.append(';');
        try (PreparedStatement stmt = connection.prepareStatement(stringBuilder.toString())) {
            int parameterIndex = 1;
            boolean attendeesToStore = false;
            for (Entry<String, List<Attendee>> entry : attendeesByEventId.entrySet()) {
                Set<Integer> usedEntities = new HashSet<Integer>(entry.getValue().size());
                int eventId = asInt(entry.getKey());
                List<Attendee> attendeeList = entry.getValue();
                if (attendeeList != null && attendeeList.size() > 0) {
                    attendeesToStore = true;
                    for (Attendee attendee : entry.getValue()) {
                        attendee = entityProcessor.adjustPriorInsert(attendee, usedEntities);
                        stmt.setInt(parameterIndex++, context.getContextId());
                        stmt.setInt(parameterIndex++, accountId);
                        stmt.setInt(parameterIndex++, eventId);
                        parameterIndex = MAPPER.setParameters(stmt, parameterIndex, attendee, mappedFields);
                    }
                }
            }
            return attendeesToStore ? logExecuteUpdate(stmt) : 0;
        }
    }

    private Map<String, List<Attendee>> selectAttendees(Connection connection, String[] eventIds, Boolean internal, boolean tombstones, AttendeeField[] fields) throws SQLException, OXException {
        AttendeeField[] mappedFields = MAPPER.getMappedFields(fields);
        StringBuilder stringBuilder = new StringBuilder()
            .append("SELECT event,").append(MAPPER.getColumns(mappedFields))
            .append(" FROM ").append(tombstones ? "calendar_attendee_tombstone" : "calendar_attendee")
            .append(" WHERE cid=? AND account=? AND event").append(getPlaceholders(eventIds.length))
        ;
        if (null != internal) {
            stringBuilder.append(" AND entity").append(internal.booleanValue() ? ">=0" : "<0");
        }
        stringBuilder.append(';');
        Map<String, List<Attendee>> attendeesByEventId = new HashMap<String, List<Attendee>>(eventIds.length);
        try (PreparedStatement stmt = connection.prepareStatement(stringBuilder.toString())) {
            int parameterIndex = 1;
            stmt.setInt(parameterIndex++, context.getContextId());
            stmt.setInt(parameterIndex++, accountId);
            for (String eventId : eventIds) {
                stmt.setInt(parameterIndex++, Integer.parseInt(eventId));
            }
            try (ResultSet resultSet = logExecuteQuery(stmt)) {
                while (resultSet.next()) {
                    Collections.put(attendeesByEventId, resultSet.getString(1), readAttendee(resultSet, mappedFields));
                }
            }
        }
        return attendeesByEventId;
    }

    private Map<String, ParticipationStatus> selectPartStats(Connection connection, String[] eventIds, Attendee attendee) throws SQLException {
        if (null == eventIds || 0 == eventIds.length) {
            return java.util.Collections.emptyMap();
        }
        StringBuilder stringBuilder = new StringBuilder()
            .append("SELECT event,partStat FROM calendar_attendee WHERE cid=? AND account=? AND ")
            .append(isInternal(attendee) ? "entity" : "uri").append("=?")
            .append(" AND event").append(getPlaceholders(eventIds.length)).append(';')
        ;
        Map<String, ParticipationStatus> statusByEventId = new HashMap<String, ParticipationStatus>(eventIds.length);
        try (PreparedStatement stmt = connection.prepareStatement(stringBuilder.toString())) {
            int parameterIndex = 1;
            stmt.setInt(parameterIndex++, context.getContextId());
            stmt.setInt(parameterIndex++, accountId);
            if (isInternal(attendee)) {
                stmt.setInt(parameterIndex++, attendee.getEntity());
            } else {
                stmt.setString(parameterIndex++, attendee.getUri());
            }
            for (String eventId : eventIds) {
                stmt.setInt(parameterIndex++, Integer.parseInt(eventId));
            }
            try (ResultSet resultSet = logExecuteQuery(stmt)) {
                while (resultSet.next()) {
                    String eventId = resultSet.getString(1);
                    String value = resultSet.getString(2);
                    statusByEventId.put(eventId, null == value ? null : new ParticipationStatus(value));
                }
            }
        }
        return statusByEventId;
    }

    private Attendee readAttendee(ResultSet resultSet, AttendeeField[] fields) throws SQLException, OXException {
        Attendee attendee = MAPPER.fromResultSet(resultSet, fields);
        return entityProcessor.adjustAfterLoad(attendee);
    }

}
