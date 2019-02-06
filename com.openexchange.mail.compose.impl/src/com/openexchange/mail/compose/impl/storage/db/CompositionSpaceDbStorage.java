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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.mail.compose.impl.storage.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
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
import com.openexchange.mail.compose.CompositionSpaceErrorCode;
import com.openexchange.mail.compose.MessageDescription;
import com.openexchange.mail.compose.MessageField;
import com.openexchange.mail.compose.impl.storage.db.mapping.MessageMapper;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;

/**
 * {@link CompositionSpaceDbStorage}
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

    public CompositionSpaceDbStorage(DBProvider dbProvider, /*DBTransactionPolicy txPolicy,*/ Session session, ServiceLookup services) {
        this(dbProvider, session.getUserId(), session.getContextId(), services);
    }

    public CompositionSpaceDbStorage(DBProvider dbProvider, /*DBTransactionPolicy txPolicy,*/ int userId, int contextId, ServiceLookup services) {
        super();
        // this.txPolicy = txPolicy;
        this.userId = userId;
        this.contextId = contextId;
        this.dbProvider = dbProvider;
        this.services = services;
        this.context = getContext(contextId);
    }

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
        Connection connection = dbProvider.getWriteConnection(context);
        try {
            insert(connection, compositionSpace, maxSpacesPerUser);
        } catch (SQLException e) {
            throw handleException(e);
        } finally {
            dbProvider.releaseWriteConnection(context, connection);
        }
    }

    /**
     * Updates the composition space
     *
     * @param compositionSpace The instance providing the changes to apply
     * @return The updated composition space
     * @throws OXException If update fails
     */
    public CompositionSpaceContainer updateCompositionSpace(CompositionSpaceContainer compositionSpace) throws OXException {
        Connection connection = dbProvider.getWriteConnection(context);
        try {
            // Update the composition space and acquire new last-modified time-stamp
            long newLastModified = update(connection, compositionSpace, compositionSpace.getLastModified());

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
        }
    }

    private void insert(Connection connection, CompositionSpaceContainer compositionSpace, int maxSpacesPerUser) throws SQLException, OXException {
        MessageField[] mappedFields = MAPPER.getMappedFields();

        StringBuilder sb = new StringBuilder().append("INSERT INTO compositionSpace (uuid,cid,user,lastModified,").append(MAPPER.getColumns(mappedFields)).append(") ");
        if (maxSpacesPerUser > 0) {
            sb.append("SELECT ?,?,?,?,").append(MAPPER.getParameters(mappedFields)).append(" FROM DUAL ");
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
        }
    }

    private CompositionSpaceContainer select(Connection connection, UUID compositionSpaceId) throws SQLException, OXException {
        MessageField[] mappedFields = MAPPER.getMappedFields();
        String sql = new StringBuilder().append("SELECT lastModified, ").append(MAPPER.getColumns(mappedFields)).append(" FROM compositionSpace WHERE cid=? AND user=? AND uuid=?").toString();
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
            sql = "SELECT uuid, lastModified FROM compositionSpace WHERE cid=? AND user=?";
        } else {
            sql = new StringBuilder().append("SELECT uuid, lastModified, ").append(MAPPER.getColumns(fields)).append(" FROM compositionSpace WHERE cid=? AND user=?").toString();
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
                    MessageDescription messageDescription = noFields ? null : MAPPER.fromResultSet(rs, fields);
                    CompositionSpaceContainer csc = new CompositionSpaceContainer();
                    csc.setLastModified(new Date(rs.getLong("lastModified")));
                    csc.setMessage(messageDescription);
                    csc.setUuid(UUIDs.toUUID(rs.getBytes("uuid")));
                    list.add(csc);
                } while (rs.next());
                return list;
            }
        }
    }

    private long update(Connection connection, CompositionSpaceContainer compositionSpace, Date optLastModified) throws SQLException, OXException {
        MessageField[] assignedfields = MAPPER.getAssignedFields(compositionSpace.getMessage());
        if (assignedfields.length == 0) {
            return 0;
        }
        String sql = new StringBuilder().append("UPDATE compositionSpace SET lastModified=?,").append(MAPPER.getAssignments(assignedfields)).append(" WHERE cid=? AND user=? AND uuid=?").append(null == optLastModified ? "" : " AND lastModified=?").toString();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            int parameterIndex = 1;
            long newLastModified = System.currentTimeMillis();
            stmt.setLong(parameterIndex++, newLastModified);
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

    private Context getContext(int contextId) {
        try {
            return services.getService(ContextService.class).getContext(contextId);
        } catch (OXException e) {
            LoggerHolder.LOG.error("Unable to resolve context.", e);
        }
        return null;
    }

    private OXException handleException(SQLException e) {
        return CompositionSpaceErrorCode.SQL_ERROR.create(e, e.getMessage());
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
