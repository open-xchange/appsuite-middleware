/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.chronos.storage.rdb;

import static com.openexchange.groupware.tools.mappings.database.DefaultDbMapper.getParameters;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import com.google.common.collect.Lists;
import com.openexchange.chronos.Conference;
import com.openexchange.chronos.ConferenceField;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.chronos.storage.ConferenceStorage;
import com.openexchange.database.Databases;
import com.openexchange.database.provider.DBProvider;
import com.openexchange.database.provider.DBTransactionPolicy;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;

/**
 * {@link CalendarStorage}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.4
 */
public class RdbConferenceStorage extends RdbStorage implements ConferenceStorage {

    private static final int DELETE_CHUNK_SIZE = 200;
    private static final ConferenceMapper MAPPER = ConferenceMapper.getInstance();

    private final int accountId;

    /**
     * Initializes a new {@link RdbConferenceStorage}.
     *
     * @param context The context
     * @param accountId The account identifier
     * @param dbProvider The database provider to use
     * @param txPolicy The transaction policy
     */
    public RdbConferenceStorage(Context context, int accountId, DBProvider dbProvider, DBTransactionPolicy txPolicy) {
        super(context, dbProvider, txPolicy);
        this.accountId = accountId;
    }

    @Override
    public int nextId() throws OXException {
        int value;
        int updated = 0;
        Connection connection = null;
        try {
            connection = dbProvider.getWriteConnection(context);
            txPolicy.setAutoCommit(connection, false);
            value = nextId(connection, accountId, "calendar_conference_sequence");
            updated = 1;
            txPolicy.commit(connection);
        } catch (SQLException e) {
            throw asOXException(e);
        } finally {
            release(connection, updated);
        }
        return value;
    }

    @Override
    public List<Conference> loadConferences(String eventId) throws OXException {
        return loadConferences(new String[] { eventId }).get(eventId);
    }

    @Override
    public Map<String, List<Conference>> loadConferences(String[] eventIds) throws OXException {
        Connection connection = null;
        try {
            connection = dbProvider.getReadConnection(context);
            return selectConferences(connection, context.getContextId(), accountId, eventIds, ConferenceField.values());
        } catch (SQLException e) {
            throw asOXException(e);
        } finally {
            dbProvider.releaseReadConnection(context, connection);
        }
    }

    @Override
    public Set<String> hasConferences(String[] eventIds) throws OXException {
        Connection connection = null;
        try {
            connection = dbProvider.getReadConnection(context);
            return selectHasConferences(connection, context.getContextId(), accountId, eventIds);
        } catch (SQLException e) {
            throw asOXException(e);
        } finally {
            dbProvider.releaseReadConnection(context, connection);
        }
    }

    @Override
    public void deleteConferences(String eventId) throws OXException {
        deleteConferences(Collections.singletonList(eventId));
    }

    @Override
    public void deleteConferences(List<String> eventIds) throws OXException {
        int updated = 0;
        Connection connection = null;
        try {
            connection = dbProvider.getWriteConnection(context);
            txPolicy.setAutoCommit(connection, false);
            for (List<String> chunk : Lists.partition(eventIds, DELETE_CHUNK_SIZE)) {
                updated += deleteConferences(connection, context.getContextId(), accountId, chunk);
            }
            txPolicy.commit(connection);
        } catch (SQLException e) {
            throw asOXException(e);
        } finally {
            release(connection, updated);
        }
    }

    @Override
    public void deleteConferences(String eventId, int[] conferencesIds) throws OXException {
        int updated = 0;
        Connection connection = null;
        try {
            connection = dbProvider.getWriteConnection(context);
            txPolicy.setAutoCommit(connection, false);
            updated += deleteConferences(connection, context.getContextId(), accountId, conferencesIds);
            txPolicy.commit(connection);
        } catch (SQLException e) {
            throw asOXException(e);
        } finally {
            release(connection, updated);
        }
    }

    @Override
    public boolean deleteAllConferences() throws OXException {
        int updated = 0;
        Connection connection = null;
        try {
            connection = dbProvider.getWriteConnection(context);
            txPolicy.setAutoCommit(connection, false);
            updated += deleteConferences(connection, context.getContextId(), accountId);
            txPolicy.commit(connection);
        } catch (SQLException e) {
            throw asOXException(e);
        } finally {
            release(connection, updated);
        }
        return 0 < updated;
    }

    @Override
    public void insertConferences(String eventId, List<Conference> conferences) throws OXException {
        if (null == conferences) {
            return;
        }
        int updated = 0;
        Connection connection = null;
        try {
            connection = dbProvider.getWriteConnection(context);
            txPolicy.setAutoCommit(connection, false);
            updated += insertConferences(connection, context.getContextId(), accountId, Collections.singletonMap(eventId, conferences));
            txPolicy.commit(connection);
        } catch (SQLException e) {
            throw asOXException(e);
        } finally {
            release(connection, updated);
        }
    }

    @Override
    public void updateConferences(String eventId, List<Conference> conferences) throws OXException {
        int updated = 0;
        Connection connection = null;
        try {
            connection = dbProvider.getWriteConnection(context);
            txPolicy.setAutoCommit(connection, false);
            for (Conference conference : conferences) {
                updated += updateConference(connection, context.getContextId(), accountId, conference.getId(), conference);
            }
            txPolicy.commit(connection);
        } catch (SQLException e) {
            throw asOXException(e);
        } finally {
            release(connection, updated);
        }
    }

    private Map<String, List<Conference>> selectConferences(Connection connection, int cid, int account, String[] eventIds, ConferenceField[] fields) throws SQLException, OXException {
        if (null == eventIds || 0 == eventIds.length) {
            return Collections.emptyMap();
        }
        ConferenceField[] mappedFields = MAPPER.getMappedFields(fields);
        StringBuilder stringBuilder = new StringBuilder().append("SELECT event,")
            .append(MAPPER.getColumns(mappedFields)).append(" FROM calendar_conference WHERE cid=? AND account=?");
        if (1 == eventIds.length) {
            stringBuilder.append(" AND event=?");
        } else if (eventIds.length > 1) {
            stringBuilder.append(" AND event IN (").append(getParameters(eventIds.length)).append(')');
        }
        stringBuilder.append(';');
        Map<String, List<Conference>> conferencesByEventId = new HashMap<String, List<Conference>>(eventIds.length);
        try (PreparedStatement stmt = connection.prepareStatement(stringBuilder.toString())) {
            int parameterIndex = 1;
            stmt.setInt(parameterIndex++, cid);
            stmt.setInt(parameterIndex++, account);
            for (String eventId : eventIds) {
                stmt.setInt(parameterIndex++, asInt(eventId));
            }
            try (ResultSet resultSet = logExecuteQuery(stmt)) {
                while (resultSet.next()) {
                    com.openexchange.tools.arrays.Collections.put(conferencesByEventId, String.valueOf(resultSet.getInt(1)), MAPPER.fromResultSet(resultSet, mappedFields));
                }
            }
        }
        return conferencesByEventId;
    }

    private static Set<String> selectHasConferences(Connection connection, int cid, int account, String[] eventIds) throws SQLException {
        if (null == eventIds || 0 == eventIds.length) {
            return Collections.emptySet();
        }
        Set<String> eventIdsWithConference = new HashSet<String>();
        String sql = new StringBuilder()
            .append("SELECT DISTINCT(event) FROM calendar_conference ")
            .append("WHERE cid=? AND account=? AND event").append(Databases.getPlaceholders(eventIds.length)).append(';')
        .toString();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            int parameterIndex = 1;
            stmt.setInt(parameterIndex++, cid);
            stmt.setInt(parameterIndex++, account);
            for (String id : eventIds) {
                stmt.setInt(parameterIndex++, asInt(id));
            }
            try (ResultSet resultSet = logExecuteQuery(stmt)) {
                while (resultSet.next()) {
                    eventIdsWithConference.add(String.valueOf(resultSet.getInt("event")));
                }
            }
        }
        return eventIdsWithConference;
    }

    private static int deleteConferences(Connection connection, int cid, int account, List<String> eventIds) throws SQLException {
        if (null == eventIds || 0 == eventIds.size()) {
            return 0;
        }
        StringBuilder stringBuilder = new StringBuilder()
            .append("DELETE FROM calendar_conference WHERE cid=? AND account=? AND event")
            .append(Databases.getPlaceholders(eventIds.size())).append(';');
        try (PreparedStatement stmt = connection.prepareStatement(stringBuilder.toString())) {
            int parameterIndex = 1;
            stmt.setInt(parameterIndex++, cid);
            stmt.setInt(parameterIndex++, account);
            for (String id : eventIds) {
                stmt.setInt(parameterIndex++, asInt(id));
            }
            return logExecuteUpdate(stmt);
        }
    }

    private static int deleteConferences(Connection connection, int cid, int account, int[] conferenceIds) throws SQLException {
        if (null == conferenceIds || 0 == conferenceIds.length) {
            return 0;
        }
        StringBuilder stringBuilder = new StringBuilder()
            .append("DELETE FROM calendar_conference WHERE cid=? AND account=? AND id").append(Databases.getPlaceholders(conferenceIds.length)).append(';');
        try (PreparedStatement stmt = connection.prepareStatement(stringBuilder.toString())) {
            int parameterIndex = 1;
            stmt.setInt(parameterIndex++, cid);
            stmt.setInt(parameterIndex++, account);
            for (int id : conferenceIds) {
                stmt.setInt(parameterIndex++, id);
            }
            return logExecuteUpdate(stmt);
        }
    }

    private static int deleteConferences(Connection connection, int cid, int account) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement("DELETE FROM calendar_conference WHERE cid=? AND account=?;")) {
            stmt.setInt(1, cid);
            stmt.setInt(2, account);
            return logExecuteUpdate(stmt);
        }
    }

    private int insertConferences(Connection connection, int cid, int account, Map<String, List<Conference>> conferencesByEventId) throws SQLException, OXException {
        if (null == conferencesByEventId || 0 == conferencesByEventId.size()) {
            return 0;
        }
        ConferenceField[] mappedFields = MAPPER.getMappedFields();
        StringBuilder stringBuilder = new StringBuilder()
            .append("INSERT INTO calendar_conference (cid,account,event,").append(MAPPER.getColumns(mappedFields)).append(") VALUES ");
        for (List<Conference> conferences : conferencesByEventId.values()) {
            for (int i = 0; i < conferences.size(); i++) {
                stringBuilder.append("(?,?,?,").append(MAPPER.getParameters(mappedFields)).append("),");
            }
        }
        stringBuilder.setLength(stringBuilder.length() - 1);
        stringBuilder.append(';');
        try (PreparedStatement stmt = connection.prepareStatement(stringBuilder.toString())) {
            int parameterIndex = 1;
            for (Entry<String, List<Conference>> entry : conferencesByEventId.entrySet()) {
                String eventId = entry.getKey();
                for (Conference conference : entry.getValue()) {
                    stmt.setInt(parameterIndex++, cid);
                    stmt.setInt(parameterIndex++, account);
                    stmt.setInt(parameterIndex++, asInt(eventId));
                    parameterIndex = MAPPER.setParameters(stmt, parameterIndex, conference, mappedFields);
                }
            }
            return logExecuteUpdate(stmt);
        }
    }

    private int updateConference(Connection connection, int cid, int account, int id, Conference conference) throws SQLException, OXException {
        ConferenceField[] assignedfields = MAPPER.getAssignedFields(conference);
        String sql = new StringBuilder()
            .append("UPDATE calendar_conference SET ").append(MAPPER.getAssignments(assignedfields))
            .append(" WHERE cid=? AND account=? AND id=?;")
        .toString();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            int parameterIndex = 1;
            parameterIndex = MAPPER.setParameters(stmt, parameterIndex, conference, assignedfields);
            stmt.setInt(parameterIndex++, cid);
            stmt.setInt(parameterIndex++, account);
            stmt.setInt(parameterIndex++, id);
            return logExecuteUpdate(stmt);
        }
    }

}
