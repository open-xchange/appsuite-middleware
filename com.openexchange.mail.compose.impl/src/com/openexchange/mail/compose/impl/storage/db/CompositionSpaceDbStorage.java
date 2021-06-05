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

package com.openexchange.mail.compose.impl.storage.db;

import java.sql.Connection;
import java.sql.DataTruncation;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import org.json.JSONArray;
import org.json.JSONException;
import org.slf4j.Logger;
import com.openexchange.context.ContextService;
import com.openexchange.database.Databases;
import com.openexchange.database.provider.DBProvider;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.java.util.UUIDs;
import com.openexchange.mail.compose.Attachment;
import com.openexchange.mail.compose.ClientToken;
import com.openexchange.mail.compose.CompositionSpaceErrorCode;
import com.openexchange.mail.compose.MessageDescription;
import com.openexchange.mail.compose.MessageField;
import com.openexchange.mail.compose.impl.storage.db.mapping.MessageMapper;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSessionAdapter;
import com.openexchange.tools.sql.DBUtils;

/**
 * {@link CompositionSpaceDbStorage} - The database storage managing composition spaces' meta-data and content.
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.10.2
 */
public class CompositionSpaceDbStorage {

    /** Simple class to delay initialization until needed */
    private static class LoggerHolder {
        static final Logger LOG = org.slf4j.LoggerFactory.getLogger(CompositionSpaceDbStorage.class);
    }

    private static final MessageMapper MAPPER = MessageMapper.getInstance();

    // private final DBTransactionPolicy txPolicy;
    private final int userId;
    private final int contextId;
    private final DBProvider dbProvider;
    private final Context context;
    private final ServiceLookup services;

    /**
     * Initializes a new {@link CompositionSpaceDbStorage}.
     *
     * @param dbProvider The database provider
     * @param session The session providing user information
     * @param services The service look-up
     * @throws OXException in case the context can't be resolved
     */
    public CompositionSpaceDbStorage(DBProvider dbProvider, /* DBTransactionPolicy txPolicy, */ Session session, ServiceLookup services) throws OXException {
        this.userId = session.getUserId();
        this.contextId = session.getContextId();
        this.dbProvider = dbProvider;
        this.services = services;
        this.context = ServerSessionAdapter.valueOf(session).getContext();
    }

    /**
     * Initializes a new {@link CompositionSpaceDbStorage}.
     *
     * @param dbProvider The database provider
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param services The service look-up
     * @throws OXException in case the context can't be resolved
     */
    public CompositionSpaceDbStorage(DBProvider dbProvider, /* DBTransactionPolicy txPolicy, */ int userId, int contextId, ServiceLookup services) throws OXException {
        super();
        // this.txPolicy = txPolicy;
        this.userId = userId;
        this.contextId = contextId;
        this.dbProvider = dbProvider;
        this.services = services;
        this.context = getContext(contextId);
    }

    /**
     * Gets the value for 'max_allowed_packet' setting.
     *
     * @return The max. allowed packet size in bytes or <code>-1</code> if unknown
     * @throws OXException If max. allowed packet size cannot be returned
     */
    public long getMaxAllowedPacketSize() throws OXException {
        Connection connection = dbProvider.getReadConnection(context);
        try {
            return Databases.getMaxAllowedPacketSize(connection);
        } catch (SQLException e) {
            LoggerHolder.LOG.error("Failed to retrieve the value for 'max_allowed_packet' setting. Assuming \"-1\" instead.", e);
            return -1L;
        } finally {
            dbProvider.releaseReadConnection(context, connection);
        }
    }

    /**
     * Counts all currently opened composition spaces of the user.
     *
     * @return The number of currently opened composition spaces
     * @throws OXException If the number of currently opened composition spaces cannot be returned; e.g. due to an SQL error
     */
    public int countAll() throws OXException {
        Connection connection = dbProvider.getReadConnection(context);
        try {
            return countAll(connection);
        } catch (SQLException e) {
            throw handleException(e);
        } finally {
            dbProvider.releaseReadConnection(context, connection);
        }
    }

    /**
     * Lists all currently open composition spaces and pre-fills given fields.
     *
     * @param fields The fields to pre-fill; pass <code>null</code> or empty array to fill all fields
     * @return The currently open composition spaces
     * @throws OXException If currently open composition spaces cannot be returned
     */
    public List<CompositionSpaceContainer> selectAll(MessageField[] fields) throws OXException {
        Connection connection = dbProvider.getReadConnection(context);
        try {
            return selectAll(connection, fields);
        } catch (SQLException e) {
            throw handleException(e);
        } finally {
            dbProvider.releaseReadConnection(context, connection);
        }
    }

    /**
     * Checks if the content of denoted composition space is marked as encrypted.
     *
     * @param compositionSpaceId The composition space identifier
     * @return <code>true</code> if content is encrypted; otherwise <code>false</code>
     * @throws OXException If check fails
     */
    public boolean isContentEncrypted(UUID compositionSpaceId) throws OXException {
        Connection connection = dbProvider.getReadConnection(context);
        try {
            return isContentEncrypted(connection, compositionSpaceId);
        } catch (SQLException e) {
            throw handleException(e);
        } finally {
            dbProvider.releaseReadConnection(context, connection);
        }
    }

    /**
     * Checks if such a composition space exists; e.g. is currently open.
     *
     * @param compositionSpaceId The composition space identifier
     * @return <code>true</code> if existent; otherwise <code>false</code>
     * @throws OXException If existence cannot be checked
     */
    public boolean exists(UUID compositionSpaceId) throws OXException {
        Connection connection = dbProvider.getReadConnection(context);
        try {
            return exists(connection, compositionSpaceId);
        } catch (SQLException e) {
            throw handleException(e);
        } finally {
            dbProvider.releaseReadConnection(context, connection);
        }
    }

    /**
     * Loads the denoted composition space.
     *
     * @param compositionSpaceId The composition space identifier
     * @return The composition space
     * @throws OXException If composition space cannot be returned
     */
    public CompositionSpaceContainer select(UUID compositionSpaceId) throws OXException {
        Connection connection = dbProvider.getReadConnection(context);
        try {
            return select(connection, compositionSpaceId);
        } catch (SQLException e) {
            throw handleException(e);
        } finally {
            dbProvider.releaseReadConnection(context, connection);
        }
    }

    /**
     * Inserts the composition space
     *
     * @param compositionSpace The instance to insert
     * @throws OXException If insertion fails
     */
    public void insert(CompositionSpaceContainer compositionSpace, int maxSpacesPerUser) throws OXException {
        try {
            DBUtils.TransactionRollbackCondition condition = new DBUtils.TransactionRollbackCondition(3);
            do {
                Connection connection = dbProvider.getWriteConnection(context);
                try {
                    insert(connection, compositionSpace, maxSpacesPerUser);
                } catch (SQLException e) {
                    if (!condition.isFailedTransactionRollback(e)) {
                        throw handleException(e);
                    }
                } finally {
                    dbProvider.releaseWriteConnection(context, connection);
                }
            } while (retryInsert(condition));
        } catch (SQLException e) {
            throw handleException(e);
        }
    }

    private boolean retryInsert(DBUtils.TransactionRollbackCondition condition) throws SQLException {
        boolean retry = condition.checkRetry();
        if (retry) {
            // Wait with exponential backoff
            int retryCount = condition.getCount();
            long nanosToWait = TimeUnit.NANOSECONDS.convert((retryCount * 1000) + ((long) (Math.random() * 1000)), TimeUnit.MILLISECONDS);
            LockSupport.parkNanos(nanosToWait);
        }
        return retry;
    }

    /**
     * Updates the composition space
     *
     * @param compositionSpace The instance providing the changes to apply
     * @return The updated composition space
     * @throws OXException If update fails
     */
    public CompositionSpaceContainer updateCompositionSpace(CompositionSpaceContainer compositionSpace, boolean updateLastModified) throws OXException {
        Connection connection = dbProvider.getWriteConnection(context);
        try {
            // Update the composition space and acquire new last-modified time-stamp
            long newLastModified = update(connection, compositionSpace, compositionSpace.getLastModified(), updateLastModified);

            // Load the updated composition space
            CompositionSpaceContainer cs = select(connection, compositionSpace.getUuid());
            cs.setLastModified(new Date(newLastModified));
            return cs;
        } catch (SQLException e) {
            throw handleException(e);
        } finally {
            dbProvider.releaseWriteConnection(context, connection);
        }
    }

    /**
     * Deletes denoted composition space.
     *
     * @param compositionSpaceId The composition space identifier
     * @return <code>true</code> for successful deletion; otherwise <code>false</code>
     * @throws OXException If deletion fails due to an error
     */
    public boolean delete(UUID compositionSpaceId) throws OXException {
        Connection connection = dbProvider.getWriteConnection(context);
        try {
            return delete(connection, compositionSpaceId);
        } catch (SQLException e) {
            throw handleException(e);
        } finally {
            dbProvider.releaseWriteConnection(context, connection);
        }
    }

    /**
     * Deletes those composition spaces, which are idle for longer than given max. idle time.
     *
     * @param maxIdleTimeMillis The max. idle time in milliseconds
     * @return The identifiers of the composition spaces that were deleted
     * @throws OXException If composition spaces cannot be deleted
     */
    public List<UUID> deleteExpired(long maxIdleTimeMillis) throws OXException {
        Connection connection = dbProvider.getWriteConnection(context);
        boolean modified = true;
        try {
            List<UUID> deleteExpired = deleteExpired(connection, maxIdleTimeMillis);
            modified = deleteExpired.size() > 0;
            return deleteExpired;
        } catch (SQLException e) {
            throw handleException(e);
        } finally {
            if (modified) {
                dbProvider.releaseWriteConnection(context, connection);
            } else {
                dbProvider.releaseWriteConnectionAfterReading(context, connection);
            }
        }
    }

    /**
     * Adds given attachment to specified composition space.
     *
     * @param compositionSpaceId The composition space identifier
     * @param attachment The attachment to add
     * @throws OXException If adding fails
     */
    public void addAttachment(UUID compositionSpaceId, Attachment attachment) throws OXException {
        Connection connection = dbProvider.getWriteConnection(context);
        try {
            updateAttachments(compositionSpaceId, attachment, true, connection);
        } catch (SQLException e) {
            throw handleException(e);
        } finally {
            dbProvider.releaseWriteConnection(context, connection);
        }
    }

    /**
     * Removes given attachment from specified composition space.
     *
     * @param compositionSpaceId The composition space identifier
     * @param attachment The attachment to remove
     * @throws OXException If removal fails
     */
    public void removeAttachment(UUID compositionSpaceId, Attachment attachment) throws OXException {
        Connection connection = dbProvider.getWriteConnection(context);
        try {
            updateAttachments(compositionSpaceId, attachment, false, connection);
        } catch (SQLException e) {
            throw handleException(e);
        } finally {
            dbProvider.releaseWriteConnection(context, connection);
        }
    }

    /**
     * Either adds or removes specified attachment to/from given composition space.
     *
     * @param compositionSpaceId The composition space identifier
     * @param attachment The attachment to add/remove
     * @param add <code>true</code> to add attachment; otherwise <code>false</code> to remove it
     * @param connection The connection to use
     * @throws SQLException If an SQL error occurs
     * @throws OXException If an Open-Xchange error occurs
     */
    private void updateAttachments(UUID compositionSpaceId, Attachment attachment, boolean add, Connection connection) throws SQLException, OXException {
        JSONArray jAttachments;
        try (PreparedStatement stmt = connection.prepareStatement("SELECT attachments FROM compositionSpace WHERE cid=? AND user=? AND uuid=?")) {
            stmt.setInt(1, contextId);
            stmt.setInt(2, userId);
            stmt.setBytes(3, UUIDs.toByteArray(compositionSpaceId));

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    throw CompositionSpaceErrorCode.NO_SUCH_COMPOSITION_SPACE.create(UUIDs.getUnformattedString(compositionSpaceId));
                }

                String oldAttachments = rs.getString("attachments");
                if (rs.wasNull()) {
                    // No attachments
                    jAttachments = new JSONArray(1);
                } else {
                    // Parse attachments to JSON
                    jAttachments = new JSONArray(oldAttachments);
                }
            } catch (JSONException e) {
                LoggerHolder.LOG.error("Unable to parse JSON.", e);
                throw CompositionSpaceErrorCode.ERROR.create(e, e.getMessage());
            }
        }

        // Either add given attachment identifier or remove matching one
        String sAttachmentId = UUIDs.getUnformattedString(attachment.getId());
        if (add) {
            jAttachments.put(sAttachmentId);
        } else {
            boolean found = false;
            for (Iterator<Object> i = jAttachments.iterator(); !found && i.hasNext();) {
                if (i.next().equals(sAttachmentId)) {
                    i.remove();
                    found = true;
                }
            }

            if (!found) {
                // No-Op
                return;
            }
        }

        try (PreparedStatement stmt = connection.prepareStatement("UPDATE compositionSpace SET attachments=? WHERE cid=? AND user=? AND uuid=?")) {
            stmt.setString(1, jAttachments.toString());
            stmt.setInt(2, contextId);
            stmt.setInt(3, userId);
            stmt.setBytes(4, UUIDs.toByteArray(compositionSpaceId));
            stmt.executeUpdate();
        } catch (DataTruncation truncation) {
            OXException handled = handleDataTruncation(truncation);
            if (handled != null) {
                throw handled;
            }
            throw truncation;
        }
    }

    private void insert(Connection connection, CompositionSpaceContainer compositionSpace, int maxSpacesPerUser) throws SQLException, OXException {
        MessageField[] mappedFields = MAPPER.getMappedFields();

        StringBuilder sb = new StringBuilder().append("INSERT INTO compositionSpace (uuid,cid,user,lastModified,clientToken,").append(MAPPER.getColumns(mappedFields)).append(") ");
        if (maxSpacesPerUser > 0) {
            sb.append("SELECT ?,?,?,?,?,").append(MAPPER.getParameters(mappedFields)).append(" FROM DUAL ");
            sb.append("WHERE ?>(SELECT COUNT(*) FROM compositionSpace WHERE cid=? AND user=?)");
        } else {
            sb.append(") VALUES (?,?,?,?,").append(MAPPER.getParameters(mappedFields)).append(")");
        }

        try (PreparedStatement stmt = connection.prepareStatement(sb.toString())) {
            int parameterIndex = 1;
            stmt.setBytes(parameterIndex++, UUIDs.toByteArray(compositionSpace.getUuid()));
            stmt.setInt(parameterIndex++, contextId);
            stmt.setInt(parameterIndex++, userId);
            stmt.setLong(parameterIndex++, compositionSpace.getLastModified().getTime());
            {
                ClientToken clientToken = compositionSpace.getMessage() == null ? null : compositionSpace.getMessage().getClientToken();
                if (clientToken == null || clientToken.isAbsent()) {
                    stmt.setNull(parameterIndex++, Types.VARCHAR);
                } else {
                    stmt.setString(parameterIndex++, clientToken.getToken());
                }
            }
            parameterIndex = MAPPER.setParameters(stmt, parameterIndex, compositionSpace.getMessage(), mappedFields);
            if (maxSpacesPerUser > 0) {
                stmt.setInt(parameterIndex++, maxSpacesPerUser);
                stmt.setInt(parameterIndex++, contextId);
                stmt.setInt(parameterIndex++, userId);
            }
            int rows = stmt.executeUpdate();
            if (rows <= 0) {
                throw CompositionSpaceErrorCode.MAX_NUMBER_OF_COMPOSITION_SPACE_REACHED.create(Integer.valueOf(maxSpacesPerUser));
            }
        } catch (DataTruncation truncation) {
            OXException handled = handleDataTruncation(truncation);
            if (handled != null) {
                throw handled;
            }
            throw truncation;
        }
    }

    private boolean isContentEncrypted(Connection connection, UUID compositionSpaceId) throws SQLException, OXException {
        String sql = "SELECT contentEncrypted FROM compositionSpace WHERE cid=? AND user=? AND uuid=?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, contextId);
            stmt.setInt(2, userId);
            stmt.setBytes(3, UUIDs.toByteArray(compositionSpaceId));
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    throw CompositionSpaceErrorCode.NO_SUCH_COMPOSITION_SPACE.create(UUIDs.getUnformattedString(compositionSpaceId));
                }

                return rs.getBoolean(1);
            }
        }
    }

    private boolean exists(Connection connection, UUID compositionSpaceId) throws SQLException {
        String sql = "SELECT 1 FROM compositionSpace WHERE cid=? AND user=? AND uuid=?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, contextId);
            stmt.setInt(2, userId);
            stmt.setBytes(3, UUIDs.toByteArray(compositionSpaceId));
            try (ResultSet rs = stmt.executeQuery()) {
                return (rs.next());
            }
        }
    }

    private CompositionSpaceContainer select(Connection connection, UUID compositionSpaceId) throws SQLException, OXException {
        MessageField[] mappedFields = MAPPER.getMappedFields();
        String sql = new StringBuilder().append("SELECT lastModified, clientToken, ").append(MAPPER.getColumns(mappedFields)).append(" FROM compositionSpace WHERE cid=? AND user=? AND uuid=?").toString();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, contextId);
            stmt.setInt(2, userId);
            stmt.setBytes(3, UUIDs.toByteArray(compositionSpaceId));
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                MessageDescription messageDescription = MAPPER.fromResultSet(rs, mappedFields);
                CompositionSpaceContainer retval = new CompositionSpaceContainer();
                retval.setLastModified(new Date(rs.getLong("lastModified")));
                messageDescription.setClientToken(ClientToken.of(rs.getString("clientToken")));
                retval.setMessage(messageDescription);
                retval.setUuid(compositionSpaceId);
                return retval;
            }
        }
    }

    private int countAll(Connection connection) throws SQLException {
        String sql = "SELECT COUNT(uuid) FROM compositionSpace WHERE cid=? AND user=?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, contextId);
            stmt.setInt(2, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    private List<CompositionSpaceContainer> selectAll(Connection connection, MessageField[] fields) throws SQLException, OXException {
        boolean noFields = fields == null || fields.length == 0;
        String sql;
        if (noFields) {
            sql = "SELECT uuid, lastModified, clientToken FROM compositionSpace WHERE cid=? AND user=?";
        } else {
            sql = new StringBuilder().append("SELECT uuid, lastModified, clientToken, ").append(MAPPER.getColumns(fields)).append(" FROM compositionSpace WHERE cid=? AND user=?").toString();
        }
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, contextId);
            stmt.setInt(2, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return Collections.emptyList();
                }

                List<CompositionSpaceContainer> list = new LinkedList<>();
                do {
                    MessageDescription messageDescription = noFields ? new MessageDescription() : MAPPER.fromResultSet(rs, fields);
                    CompositionSpaceContainer csc = new CompositionSpaceContainer();
                    csc.setLastModified(new Date(rs.getLong("lastModified")));
                    messageDescription.setClientToken(ClientToken.of(rs.getString("clientToken")));
                    csc.setMessage(messageDescription);
                    csc.setUuid(UUIDs.toUUID(rs.getBytes("uuid")));
                    list.add(csc);
                } while (rs.next());
                return list;
            }
        }
    }

    private long update(Connection connection, CompositionSpaceContainer compositionSpace, Date optLastModified, boolean updateLastModified) throws SQLException, OXException {
        MessageField[] assignedfields = MAPPER.getAssignedFields(compositionSpace.getMessage());
        if (assignedfields.length == 0) {
            return 0;
        }

        String token;
        {
            ClientToken clientToken = compositionSpace.getMessage() == null ? null : (compositionSpace.getMessage().containsValidClientToken() ? compositionSpace.getMessage().getClientToken() : null);
            token = clientToken != null && clientToken.isPresent() ? clientToken.getToken() : null;
        }

        String sql;
        {
            StringBuilder sb = new StringBuilder("UPDATE compositionSpace SET ");
            if (updateLastModified) {
                sb.append("lastModified=?,");
            }
            if (token != null) {
                sb.append("clientToken=?,");
            }
            sb.append(MAPPER.getAssignments(assignedfields));
            sb.append(" WHERE cid=? AND user=? AND uuid=?");
            if (null != optLastModified) {
                sb.append(" AND lastModified=?");
            }
            sql = sb.toString();
        }

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            int parameterIndex = 1;
            long newLastModified = System.currentTimeMillis();
            if (updateLastModified) {
                stmt.setLong(parameterIndex++, newLastModified);
            }
            if (token != null) {
                stmt.setString(parameterIndex++, token);
            }
            parameterIndex = MAPPER.setParameters(stmt, parameterIndex, compositionSpace.getMessage(), assignedfields);
            stmt.setInt(parameterIndex++, contextId);
            stmt.setInt(parameterIndex++, userId);
            stmt.setBytes(parameterIndex++, UUIDs.toByteArray(compositionSpace.getUuid()));
            if (null != optLastModified) {
                stmt.setLong(parameterIndex, optLastModified.getTime());
            }
            int updated = stmt.executeUpdate();
            if (updated == 0) {
                throw CompositionSpaceErrorCode.CONCURRENT_UPDATE.create();
            }
            return newLastModified;
        } catch (DataTruncation truncation) {
            OXException handled = handleDataTruncation(truncation);
            if (handled != null) {
                throw handled;
            }
            throw truncation;
        }
    }

    private boolean delete(Connection connection, UUID compositionSpaceId) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement("DELETE FROM compositionSpace WHERE cid=? AND user=? AND uuid=?")) {
            stmt.setInt(1, contextId);
            stmt.setInt(2, userId);
            stmt.setBytes(3, UUIDs.toByteArray(compositionSpaceId));
            return stmt.executeUpdate() > 0;
        }
    }

    private List<UUID> deleteExpired(Connection connection, long maxIdleTimeMillis) throws SQLException {
        if (!Databases.tableExists(connection, "compositionSpace")) {
            return Collections.emptyList();
        }

        long maxLastModifiedStamp = System.currentTimeMillis() - maxIdleTimeMillis;

        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = connection.prepareStatement("SELECT uuid, lastModified FROM compositionSpace WHERE cid=? AND user=? AND lastModified<?");
            stmt.setInt(1, contextId);
            stmt.setInt(2, userId);
            stmt.setLong(3, maxLastModifiedStamp);
            rs = stmt.executeQuery();
            if (!rs.next()) {
                return Collections.emptyList();
            }

            List<UuidAndLastModified> toDelete = new LinkedList<>();
            do {
                toDelete.add(new UuidAndLastModified(UUIDs.toUUID(rs.getBytes(1)), rs.getLong(2)));
            } while (rs.next());
            Databases.closeSQLStuff(rs, stmt);
            rs = null;
            stmt = null;

            List<UUID> deleted = new ArrayList<UUID>(toDelete.size());
            for (UuidAndLastModified uuidAndLastModified : toDelete) {
                stmt = connection.prepareStatement("DELETE FROM compositionSpace WHERE uuid=? AND lastModified=?");
                stmt.setBytes(1, UUIDs.toByteArray(uuidAndLastModified.uuid));
                stmt.setLong(2, uuidAndLastModified.lastModified);
                if (stmt.executeUpdate() > 0) {
                    deleted.add(uuidAndLastModified.uuid);
                }
                Databases.closeSQLStuff(stmt);
                stmt = null;
            }
            return deleted;
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

    private Context getContext(int contextId) throws OXException {
        try {
            return services.getService(ContextService.class).getContext(contextId);
        } catch (OXException e) {
            LoggerHolder.LOG.debug("Unable to resolve context.", e);
            throw e;
        }
    }

    private OXException handleException(SQLException e) {
        return CompositionSpaceErrorCode.SQL_ERROR.create(e, e.getMessage());
    }

    private OXException handleDataTruncation(DataTruncation truncation) {
        String[] truncatedFields = Databases.parseTruncatedFields(truncation);
        if (truncatedFields.length > 0) {
            String truncatedField = truncatedFields[0];
            switch (truncatedField) {
                case "fromAddr":
                    return CompositionSpaceErrorCode.FROM_TOO_LONG.create(truncation, new Object[0]);
                case "senderAddr":
                    return CompositionSpaceErrorCode.SENDER_TOO_LONG.create(truncation, new Object[0]);
                case "replyToAddr":
                    return CompositionSpaceErrorCode.REPLY_TO_TOO_LONG.create(truncation, new Object[0]);
                case "toAddr":
                    return CompositionSpaceErrorCode.TO_TOO_LONG.create(truncation, new Object[0]);
                case "ccAddr":
                    return CompositionSpaceErrorCode.CC_TOO_LONG.create(truncation, new Object[0]);
                case "bccAddr":
                    return CompositionSpaceErrorCode.BCC_TOO_LONG.create(truncation, new Object[0]);
                case "subject":
                    return CompositionSpaceErrorCode.SUBJECT_TOO_LONG.create(truncation, new Object[0]);
                default:
                    break;
            }
        }
        return null;
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private static class UuidAndLastModified {

        final UUID uuid;
        final long lastModified;

        /**
         * Initializes a new {@link UuidAndLastModified}.
         */
        UuidAndLastModified(UUID uuid, long lastModified) {
            super();
            this.uuid = uuid;
            this.lastModified = lastModified;
        }
    }

}
