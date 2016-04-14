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

package com.openexchange.ajax.requesthandler.cache;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.preview.PreviewExceptionCodes;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.services.ServerServiceRegistry;


/**
 * A{@link ResourceCacheMetadataStore} manages stored metadata for cached resources.
 * Resources are tracked via database records. A record is identified by an instance
 * of {@link ResourceCacheMetadata}.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class ResourceCacheMetadataStore {

    private static final ResourceCacheMetadataStore INSTANCE = new ResourceCacheMetadataStore();

    private ResourceCacheMetadataStore() {
        super();
    }

    public static ResourceCacheMetadataStore getInstance() {
        return INSTANCE;
    }

    /**
     * Stores a metadata record in the database. Every field of {@link ResourceCacheMetadata}
     * is taken into account.
     *
     * @param metadata The {@link ResourceCacheMetadata}.
     * @throws OXException if storing the record fails.
     */
    public void store(ResourceCacheMetadata metadata) throws OXException {
        DatabaseService dbService = getDBService();
        Connection con = dbService.getWritable(metadata.getContextId());
        try {
            store(con, metadata);
        } catch (SQLException e) {
            throw PreviewExceptionCodes.ERROR.create(e, e.getMessage());
        } finally {
            dbService.backWritable(metadata.getContextId(), con);
        }
    }

    /**
     * Convenience method to re-use an existing writable database connection.
     *
     * @see ResourceCacheMetadataStore#store(ResourceCacheMetadata)
     * @param con The writable database connection.
     * @param metadata The {@link ResourceCacheMetadata}.
     * @throws OXException if storing the record fails.
     * @throws SQLException
     */
    public void store(Connection con, ResourceCacheMetadata metadata) throws SQLException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("INSERT INTO preview (cid, user, id, size, createdAt, refId, fileName, fileType) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
            int pos = 1;
            stmt.setInt(pos++, metadata.getContextId());
            stmt.setInt(pos++, metadata.getUserId() > 0 ? metadata.getUserId() : 0);
            stmt.setString(pos++, metadata.getResourceId());
            stmt.setLong(pos++, metadata.getSize());
            stmt.setLong(pos++, metadata.getCreatedAt());
            if (metadata.getRefId() == null) {
                stmt.setNull(pos++, java.sql.Types.VARCHAR);
            } else {
                stmt.setString(pos++, metadata.getRefId());
            }
            if (null == metadata.getFileName()) {
                stmt.setNull(pos++, Types.VARCHAR);
            } else {
                stmt.setString(pos++, metadata.getFileName());
            }
            if (null == metadata.getFileType()) {
                stmt.setNull(pos++, Types.VARCHAR);
            } else {
                stmt.setString(pos++, metadata.getFileType());
            }

            stmt.executeUpdate();
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }

    /**
     * Updates a metadata record in the database. Every field of {@link ResourceCacheMetadata}
     * is taken into account.
     *
     * @param metadata The {@link ResourceCacheMetadata}.
     * @throws OXException if updating the record fails.
     */
    public boolean update(ResourceCacheMetadata metadata) throws OXException {
        DatabaseService dbService = getDBService();
        Connection con = dbService.getWritable(metadata.getContextId());
        try {
            return update(con, metadata);
        } catch (SQLException e) {
            throw PreviewExceptionCodes.ERROR.create(e, e.getMessage());
        } finally {
            dbService.backWritable(metadata.getContextId(), con);
        }
    }

    /**
     * Convenience method to re-use an existing writable database connection.
     * @see ResourceCacheMetadataStore#update(ResourceCacheMetadata).
     * @throws SQLException
     */
    public boolean update(Connection con, ResourceCacheMetadata metadata) throws SQLException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("UPDATE preview SET size = ?, createdAt = ?, refId = ?, fileName = ?, fileType = ? WHERE cid = ? AND user = ? AND id = ?");
            int pos = 1;
            stmt.setLong(pos++, metadata.getSize());
            stmt.setLong(pos++, metadata.getCreatedAt());
            if (metadata.getRefId() == null) {
                stmt.setNull(pos++, java.sql.Types.VARCHAR);
            } else {
                stmt.setString(pos++, metadata.getRefId());
            }
            if (null == metadata.getFileName()) {
                stmt.setNull(pos++, Types.VARCHAR);
            } else {
                stmt.setString(pos++, metadata.getFileName());
            }
            if (null == metadata.getFileType()) {
                stmt.setNull(pos++, Types.VARCHAR);
            } else {
                stmt.setString(pos++, metadata.getFileType());
            }
            stmt.setInt(pos++, metadata.getContextId());
            stmt.setInt(pos++, metadata.getUserId() > 0 ? metadata.getUserId() : 0);
            stmt.setString(pos++, metadata.getResourceId());

            int rows = stmt.executeUpdate();
            return rows > 0;
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }

    /**
     * Loads a possible metadata record for the given (contextId, userId, resourceId) tuple.
     *
     * @param contextId The context id.
     * @param userId The user id. If the record references a globally cached resource, userId must be <= 0.
     * @param resourceId The resource id. Never <code>null</code>.
     * @return An instance of {@link ResourceCacheMetadata} or <code>null</code>, if no record was found.
     * @throws OXException if loading the record fails.
     */
    public ResourceCacheMetadata load(int contextId, int userId, String resourceId) throws OXException {
        DatabaseService dbService = getDBService();
        Connection con = dbService.getReadOnly(contextId);
        try {
            return load(con, contextId, userId, resourceId);
        } catch (SQLException e) {
            throw PreviewExceptionCodes.ERROR.create(e, e.getMessage());
        } finally {
            dbService.backReadOnly(contextId, con);
        }
    }

    /**
     * Convenience method to re-use an existing database connection.
     * @see ResourceCacheMetadataStore#load(int, int, String)
     */
    public ResourceCacheMetadata load(Connection con, int contextId, int userId, String resourceId) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            if (userId > 0) {
                stmt = con.prepareStatement("SELECT refId, fileName, fileType, size, createdAt FROM preview WHERE cid = ? AND user = ? AND id = ?");
                stmt.setInt(1, contextId);
                stmt.setInt(2, userId);
                stmt.setString(3, resourceId);
            } else {
                stmt = con.prepareStatement("SELECT refId, fileName, fileType, size, createdAt FROM preview WHERE cid = ? AND id = ?");
                stmt.setInt(1, contextId);
                stmt.setString(2, resourceId);
            }
            rs = stmt.executeQuery();
            if (!rs.next()) {
                return null;
            }

            ResourceCacheMetadata metadata = new ResourceCacheMetadata();
            metadata.setContextId(contextId);
            metadata.setUserId(userId);
            metadata.setResourceId(resourceId);
            metadata.setRefId(rs.getString(1));
            metadata.setFileName(rs.getString(2));
            metadata.setFileType(rs.getString(3));
            metadata.setSize(rs.getLong(4));
            metadata.setCreatedAt(rs.getLong(5));

            return metadata;
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

    /**
     * Loads a possible metadata record for the given (contextId, userId, resourceId) tuple and locks the according row.
     *
     * @param con The writable database connection. Must be in an transaction.
     * @param contextId The context id.
     * @param userId The user id. If the record references a globally cached resource, userId must be <= 0.
     * @param resourceId The resource id. Never <code>null</code>.
     * @return An instance of {@link ResourceCacheMetadata} or <code>null</code>, if no record was found.
     * @throws OXException if loading the record fails.
     */
    public ResourceCacheMetadata loadForUpdate(Connection con, int contextId, int userId, String resourceId) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            if (userId > 0) {
                stmt = con.prepareStatement("SELECT refId, fileName, fileType, size, createdAt FROM preview WHERE cid = ? AND user = ? AND id = ? FOR UPDATE");
                stmt.setInt(1, contextId);
                stmt.setInt(2, userId);
                stmt.setString(3, resourceId);
            } else {
                stmt = con.prepareStatement("SELECT refId, fileName, fileType, size, createdAt FROM preview WHERE cid = ? AND id = ? FOR UPDATE");
                stmt.setInt(1, contextId);
                stmt.setString(2, resourceId);
            }
            rs = stmt.executeQuery();
            if (!rs.next()) {
                return null;
            }

            ResourceCacheMetadata metadata = new ResourceCacheMetadata();
            metadata.setContextId(contextId);
            metadata.setUserId(userId);
            metadata.setResourceId(resourceId);
            metadata.setRefId(rs.getString(1));
            metadata.setFileName(rs.getString(2));
            metadata.setFileType(rs.getString(3));
            metadata.setSize(rs.getLong(4));
            metadata.setCreatedAt(rs.getLong(5));

            return metadata;
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

    /**
     * Checks if an entry does already exist for the given (contextId, userId, resourceId) tuple.
     *
     * @param con The database connection.
     * @param contextId The context id.
     * @param userId The user id. If the record references a globally cached resource, userId must be <= 0.
     * @param resourceId The resource id. Never <code>null</code>.
     * @return An instance of {@link ResourceCacheMetadata} or <code>null</code>, if no record was found.
     * @throws OXException if loading the record fails.
     */
    public boolean exists(Connection con, int contextId, int userId, String resourceId) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            if (userId > 0) {
                stmt = con.prepareStatement("SELECT COUNT(*) FROM preview WHERE cid = ? AND user = ? AND id = ?");
                stmt.setInt(1, contextId);
                stmt.setInt(2, userId);
                stmt.setString(3, resourceId);
            } else {
                stmt = con.prepareStatement("SELECT COUNT(*) FROM preview WHERE cid = ? AND id = ?");
                stmt.setInt(1, contextId);
                stmt.setString(2, resourceId);
            }

            rs = stmt.executeQuery();
            if (rs.next()) {
                int count = rs.getInt(1);
                return count > 0;
            }

            return false;
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

    /**
     * Loads all filestore references for the given context.
     *
     * @param contextId The context id.
     * @return A set containing all found reference ids.
     * @throws OXException
     */
    public Set<String> loadRefIds(int contextId) throws OXException {
        Set<String> refIds = new HashSet<String>();
        DatabaseService dbService = getDBService();
        Connection con = dbService.getReadOnly(contextId);
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT refId FROM preview WHERE cid=? AND refId IS NOT NULL");
            stmt.setInt(1, contextId);
            rs = stmt.executeQuery();
            while (rs.next()) {
                refIds.add(rs.getString(1));
            }
        } catch (SQLException e) {
            throw PreviewExceptionCodes.ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
            dbService.backReadOnly(contextId, con);
        }

        return refIds;
    }

    /**
     * Convenience method to re-use an existing database connection.
     * @throws SQLException
     * @see ResourceCacheMetadataStore#load(String)
     */
    public ResourceCacheMetadata load(Connection con, String refId) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT cid, user, id, fileName, fileType, size, createdAt FROM preview WHERE refId = ?");
            stmt.setString(1, refId);
            rs = stmt.executeQuery();
            if (!rs.next()) {
                return null;
            }

            ResourceCacheMetadata metadata = new ResourceCacheMetadata();
            metadata.setContextId(rs.getInt(1));
            metadata.setUserId(rs.getInt(2));
            metadata.setResourceId(rs.getString(3));
            metadata.setFileName(rs.getString(4));
            metadata.setFileType(rs.getString(5));
            metadata.setSize(rs.getLong(6));
            metadata.setCreatedAt(rs.getLong(7));
            metadata.setRefId(refId);

            return metadata;
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

    /**
     * Removes a metadata record for the given (contextId, userId, resourceId) tuple.
     *
     * @param contextId The context id.
     * @param userId The user id. If the record references a globally cached resource, userId must be <= 0.
     * @param resourceId The resource id. Never <code>null</code>.
     * @return <code>true</code> if at least one record was deleted. Otherwise <code>false</code>.
     * @throws OXException if deleting the record fails.
     */
    public boolean remove(int contextId, int userId, String resourceId) throws OXException {
        DatabaseService dbService = getDBService();
        Connection con = dbService.getWritable(contextId);
        try {
            return remove(con, contextId, userId, resourceId);
        } catch (SQLException e) {
            throw PreviewExceptionCodes.ERROR.create(e, e.getMessage());
        } finally {
            dbService.backWritable(contextId, con);
        }
    }

    /**
     * Convenience method to re-use an existing database connection.
     * @see {@link ResourceCacheMetadataStore#remove(int, int, String)}
     */
    public boolean remove(Connection con, int contextId, int userId, String resourceId) throws SQLException {
        PreparedStatement stmt = null;
        try {
            if (userId > 0) {
                stmt = con.prepareStatement("DELETE FROM preview WHERE cid = ? AND user = ? AND id = ?");
                stmt.setInt(1, contextId);
                stmt.setInt(2, userId);
                stmt.setString(3, resourceId);
            } else {
                stmt = con.prepareStatement("DELETE FROM preview WHERE cid = ? AND id = ?");
                stmt.setInt(1, contextId);
                stmt.setString(2, resourceId);
            }

            int rows = stmt.executeUpdate();
            return rows > 0;
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }

    /**
     * Removes all metadata records for a given (contextId, userId, resourceIdPrefix) tuple.
     * All records are deleted where the parameter resourceIdPrefix is a valid prefix of their resource id.
     * If userId <= 0, all records for the given context are deleted.
     *
     * @param contextId The context id.
     * @param userId The user id.
     * @param resourceIdPrefix A full resource id or a prefix thereof.
     * @return A list of deleted {@link ResourceCacheMetadata}.
     * @throws OXException if the deletion fails.
     */
    public List<ResourceCacheMetadata> removeAll(int contextId, int userId, String resourceIdPrefix) throws OXException {
        DatabaseService dbService = getDBService();
        Connection con = dbService.getWritable(contextId);
        boolean commited = false;
        try {
            Databases.startTransaction(con);
            List<ResourceCacheMetadata> result = removeAll(con, contextId, userId, resourceIdPrefix);
            con.commit();
            return result;
        } catch (SQLException e) {
            throw PreviewExceptionCodes.ERROR.create(e, e.getMessage());
        } finally {
            if (!commited) {
                Databases.rollback(con);
            }

            Databases.autocommit(con);
            dbService.backWritable(contextId, con);
        }
    }

    /**
     * Convenience method to re-use an existing database connection.
     * @param con The writable database connection in a transactional state.
     * @throws SQLException
     * @see {@link ResourceCacheMetadataStore#removeAll(int, int, String)}
     */
    public List<ResourceCacheMetadata> removeAll(Connection con, int contextId, int userId, String resourceIdPrefix) throws SQLException {
        List<ResourceCacheMetadata> result = new ArrayList<ResourceCacheMetadata>();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            if (userId < 0) {
                stmt = con.prepareStatement("SELECT user, id, fileName, fileType, size, createdAt, refId FROM preview WHERE cid = ? AND id LIKE ?");
                stmt.setInt(1, contextId);
                stmt.setString(2, resourceIdPrefix + "%");
            } else {
                stmt = con.prepareStatement("SELECT id, fileName, fileType, size, createdAt, refId FROM preview WHERE cid = ? AND user = ? AND id LIKE ?");
                stmt.setInt(1, contextId);
                stmt.setInt(2, userId);
                stmt.setString(3, resourceIdPrefix + "%");
            }

            rs = stmt.executeQuery();
            while (rs.next()) {
                int i = 1;
                ResourceCacheMetadata metadata = new ResourceCacheMetadata();
                metadata.setContextId(contextId);
                metadata.setUserId(userId < 0 ? rs.getInt(i++) : userId);
                metadata.setResourceId(rs.getString(i++));
                metadata.setFileName(rs.getString(i++));
                metadata.setFileType(rs.getString(i++));
                metadata.setSize(rs.getLong(i++));
                metadata.setCreatedAt(rs.getLong(i++));
                metadata.setRefId(rs.getString(i++));
                result.add(metadata);
            }
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
        removeAllInternal(con, contextId, userId, result);
        return result;
    }

    /**
     * @param con The writable database connection in a transactional state.
     * @throws SQLException
     */
    private void removeAllInternal(Connection con, int contextId, int userId, List<ResourceCacheMetadata> result) throws SQLException {
        PreparedStatement stmtWithRefId = null;
        PreparedStatement stmtWithoutRefId = null;
        try {
            if (userId > 0) {
                stmtWithRefId = con.prepareStatement("DELETE FROM preview WHERE cid = ? AND user = ? AND id = ? AND refId = ?");
                stmtWithoutRefId = con.prepareStatement("DELETE FROM preview WHERE cid = ? AND user = ? AND id = ?");
                for (ResourceCacheMetadata toDelete : result) {
                    if (null != toDelete.getRefId()) {
                        stmtWithRefId.setInt(1, contextId);
                        stmtWithRefId.setInt(2, userId);
                        stmtWithRefId.setString(3, toDelete.getResourceId());
                        stmtWithRefId.setString(4, toDelete.getRefId());
                        stmtWithRefId.addBatch();
                    } else {
                        stmtWithoutRefId.setInt(1, contextId);
                        stmtWithoutRefId.setInt(2, userId);
                        stmtWithoutRefId.setString(3, toDelete.getResourceId());
                        stmtWithoutRefId.addBatch();
                    }
                }
            } else {
                stmtWithRefId = con.prepareStatement("DELETE FROM preview WHERE cid = ? AND id = ? AND refId = ?");
                stmtWithoutRefId = con.prepareStatement("DELETE FROM preview WHERE cid = ? AND id = ?");
                for (ResourceCacheMetadata toDelete : result) {
                    if (null != toDelete.getRefId()) {
                        stmtWithRefId.setInt(1, contextId);
                        stmtWithRefId.setString(2, toDelete.getResourceId());
                        stmtWithRefId.setString(3, toDelete.getRefId());
                        stmtWithRefId.addBatch();
                    } else {
                        stmtWithoutRefId.setInt(1, contextId);
                        stmtWithoutRefId.setString(2, toDelete.getResourceId());
                        stmtWithoutRefId.addBatch();
                    }

                }
            }
            stmtWithRefId.executeBatch();
            stmtWithoutRefId.executeBatch();
        } finally {
            Databases.closeSQLStuff(stmtWithRefId);
            Databases.closeSQLStuff(stmtWithoutRefId);
        }
    }

    /**
     * Removes all metadata records for a given (contextId, userId) tuple. If userId <= 0, all records for the given
     * context are deleted.
     *
     * @param contextId The context id.
     * @param userId The user id.
     * @return A list of deleted {@link ResourceCacheMetadata}.
     * @throws OXException if the deletion fails.
     */
    public List<ResourceCacheMetadata> removeAll(int contextId, int userId) throws OXException {
        DatabaseService dbService = getDBService();
        Connection con = dbService.getWritable(contextId);
        boolean commited = false;
        try {
            Databases.startTransaction(con);
            List<ResourceCacheMetadata> result = removeAll(con, contextId, userId);
            con.commit();
            return result;
        } catch (SQLException e) {
            throw PreviewExceptionCodes.ERROR.create(e, e.getMessage());
        } finally {
            if (!commited) {
                Databases.rollback(con);
            }

            Databases.autocommit(con);
            dbService.backWritable(contextId, con);
        }
    }

    /**
     * Convenience method to re-use an existing database connection.
     * @param con The writable database connection in a transactional state.
     * @throws SQLException
     * @see {@link ResourceCacheMetadataStore#removeAll(int, int)}
     */
    public List<ResourceCacheMetadata> removeAll(Connection con, int contextId, int userId) throws SQLException {
        List<ResourceCacheMetadata> result = new ArrayList<ResourceCacheMetadata>();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            if (userId > 0) {
                stmt = con.prepareStatement("SELECT id, fileName, fileType, size, createdAt, refId FROM preview WHERE cid = ? AND user = ?");
                stmt.setInt(1, contextId);
                stmt.setInt(2, userId);
            } else {
                stmt = con.prepareStatement("SELECT user, id, fileName, fileType, size, createdAt, refId FROM preview WHERE cid = ?");
                stmt.setInt(1, contextId);
            }

            rs = stmt.executeQuery();
            while (rs.next()) {
                int i = 1;
                ResourceCacheMetadata metadata = new ResourceCacheMetadata();
                metadata.setContextId(contextId);
                metadata.setUserId(userId < 0 ? rs.getInt(i++) : userId);
                metadata.setResourceId(rs.getString(i++));
                metadata.setFileName(rs.getString(i++));
                metadata.setFileType(rs.getString(i++));
                metadata.setSize(rs.getLong(i++));
                metadata.setCreatedAt(rs.getLong(i++));
                metadata.setRefId(rs.getString(i++));
                result.add(metadata);
            }
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }

        removeAllInternal(con, contextId, userId, result);

        return result;
    }

    /**
     * Removes the oldest metadata record for a given context.
     *
     * @param con The writable database connection. Should be in an transactional state.
     * @param contextId The context id.
     * @return The {@link ResourceCacheMetadata} for the deleted record or <code>null</code>, if no entry existed.
     * @throws SQLException
     */
    public ResourceCacheMetadata removeOldest(Connection con, int contextId) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        ResourceCacheMetadata metadata = null;
        try {
            stmt = con.prepareStatement("SELECT user, id, size, createdAt, fileName, fileType, refId FROM preview WHERE createdAt = (SELECT MIN(createdAt) FROM preview WHERE cid = ?) FOR UPDATE");
            stmt.setInt(1, contextId);
            rs = stmt.executeQuery();
            if (rs.next()) {
                metadata = new ResourceCacheMetadata();
                metadata.setContextId(contextId);
                metadata.setUserId(rs.getInt(1));
                metadata.setResourceId(rs.getString(2));
                metadata.setSize(rs.getLong(3));
                metadata.setCreatedAt(rs.getLong(4));
                metadata.setFileName(rs.getString(5));
                metadata.setFileType(rs.getString(6));
                metadata.setRefId(rs.getString(7));
            }
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }

        if (metadata != null) {
            remove(con, contextId, metadata.getUserId(), metadata.getResourceId());
        }

        return metadata;
    }

    /**
     * Loads preview records for the given context ordered by creation date (oldest first) and locks those entries.
     *
     * @param con The writable database connection in a transactional state.
     * @param contextId The context id.
     * @return At most 500 entries to keep performance reasonable.
     */
    public List<ResourceCacheMetadata> loadForCleanUp(Connection con, int contextId) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<ResourceCacheMetadata> metadatas = new LinkedList<ResourceCacheMetadata>();
        try {
            stmt = con.prepareStatement("SELECT user, id, size, createdAt, fileName, fileType, refId FROM preview WHERE cid = ? ORDER BY createdAt ASC LIMIT 500");
            stmt.setInt(1, contextId);
            rs = stmt.executeQuery();
            while (rs.next()) {
                ResourceCacheMetadata metadata = new ResourceCacheMetadata();
                metadata.setContextId(contextId);
                metadata.setUserId(rs.getInt(1));
                metadata.setResourceId(rs.getString(2));
                metadata.setSize(rs.getLong(3));
                metadata.setCreatedAt(rs.getLong(4));
                metadata.setFileName(rs.getString(5));
                metadata.setFileType(rs.getString(6));
                metadata.setRefId(rs.getString(7));
                metadatas.add(metadata);
            }
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }

        return metadatas;
    }

    /**
     * Deletes a set of records in a context for a given set of reference ids.
     *
     * @param contextId The context id.
     * @param refIds The reference ids.
     * @throws OXException if deletion fails.
     */
    public void removeByRefId(int contextId, Set<String> refIds) throws OXException {
        DatabaseService dbService = getDBService();
        Connection con = dbService.getWritable(contextId);
        try {
            removeByRefIds(con, contextId, refIds);
        } catch (SQLException e) {
            throw PreviewExceptionCodes.ERROR.create(e, e.getMessage());
        } finally {
            dbService.backWritable(contextId, con);
        }
    }

    /**
     * Convenience method to re-use an existing database connection.
     * @param con The writable database connection in a transactional state.
     * @throws SQLException
     * @see {@link ResourceCacheMetadataStore#removeByRefId(int, Set)}
     */
    public void removeByRefIds(Connection con, int contextId, Set<String> refIds) throws SQLException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("DELETE FROM preview WHERE cid = ? AND refId = ?");
            stmt.setInt(1, contextId);
            for (String refId : refIds) {
                stmt.setString(2, refId);
                stmt.addBatch();
            }
            stmt.executeBatch();
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }

    /**
     * Calculates the sum of all resource sizes based on their metadata records.
     *
     * @param contextId The context id.
     * @return The sum of all sizes.
     * @throws SQLException
     */
    public long getUsedSize(int contextId) throws OXException {
        DatabaseService dbService = getDBService();
        Connection con = dbService.getReadOnly(contextId);
        try {
            return getUsedSize(con, contextId);
        } catch (SQLException e) {
            throw PreviewExceptionCodes.ERROR.create(e, e.getMessage());
        } finally {
            dbService.backReadOnly(contextId, con);
        }
    }

    /**
     * Calculates the sum of all resource sizes based on their metadata records.
     *
     * @param con The database connection.
     * @param contextId The context id.
     * @return The sum of all sizes.
     * @throws SQLException
     */
    public long getUsedSize(Connection con, int contextId) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT SUM(size) FROM preview WHERE cid = ?");
            stmt.setLong(1, contextId);
            rs = stmt.executeQuery();
            if (!rs.next()) {
                return 0L;
            }
            if (rs.wasNull()) {
                return 0L;
            }
            return rs.getLong(1);
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

    private DatabaseService getDBService() throws OXException {
        final DatabaseService dbService = ServerServiceRegistry.getInstance().getService(DatabaseService.class);
        if (dbService == null) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(DatabaseService.class.getName());
        }

        return dbService;
    }

}
