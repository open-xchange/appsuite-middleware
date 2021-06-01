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

package com.openexchange.filestore.sproxyd.chunkstorage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.DatabaseAccess;
import com.openexchange.filestore.DatabaseTable;
import com.openexchange.filestore.sproxyd.SproxydExceptionCode;
import com.openexchange.filestore.sproxyd.groupware.SproxydCreateTableService;
import com.openexchange.java.util.UUIDs;


/**
 * {@link RdbChunkStorage}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class RdbChunkStorage implements ChunkStorage {

    /**
     * Gets the required database tables for this storage.
     *
     * @return The required tables
     */
    public static DatabaseTable[] getRequiredTables() {
        String[] names = SproxydCreateTableService.getTablesToCreate();
        String[] stmts = SproxydCreateTableService.getCreateStmts();

        DatabaseTable[] databaseTables = new DatabaseTable[stmts.length];
        for (int i = 0; i < stmts.length; i++) {
            databaseTables[i] = new DatabaseTable(names[i], stmts[i]);
        }
        return databaseTables;
    }

    // ------------------------------------------------------------------------------

    private final DatabaseAccess databaseAccess;
    private final int contextId;
    private final int userId;

    /**
     * Initializes a new {@link RdbChunkStorage}.
     *
     * @param databaseAccess The database access to use
     * @param contextId The context identifier
     * @param userId The user identifier
     */
    public RdbChunkStorage(DatabaseAccess databaseAccess, int contextId, int userId) {
        super();
        this.databaseAccess = databaseAccess;
        this.contextId = contextId;
        this.userId = userId;
    }

    @Override
    public List<UUID> getDocuments() throws OXException {
        Connection con = databaseAccess.acquireReadOnly();
        try {
            return getDocuments(userId, contextId, con);
        } finally {
            databaseAccess.releaseReadOnly(con);
        }
    }

    private static List<UUID> getDocuments(int userId, int contextId, Connection con) throws OXException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT DISTINCT document_id FROM scality_filestore WHERE cid=? AND user=?");
            stmt.setInt(1, contextId);
            stmt.setInt(2, userId);
            rs = stmt.executeQuery();
            if (!rs.next()) {
                return Collections.emptyList();
            }
            List<UUID> documentIds = new ArrayList<UUID>();
            do {
                documentIds.add(UUIDs.toUUID(rs.getBytes(1)));
            } while (rs.next());
            return documentIds;
        } catch (SQLException e) {
            throw SproxydExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

    @Override
    public List<Chunk> getChunks(UUID documentId) throws OXException {
        Connection con = databaseAccess.acquireReadOnly();
        try {
            Optional<List<Chunk>> optionalChunks = getChunks(documentId, userId, contextId, con);
            if (optionalChunks.isPresent()) {
                return optionalChunks.get();
            }
            throw SproxydExceptionCode.NO_SUCH_DOCUMENT.create(UUIDs.getUnformattedString(documentId));
        } finally {
            databaseAccess.releaseReadOnly(con);
        }
    }

    @Override
    public Optional<List<Chunk>> optChunks(UUID documentId) throws OXException {
        Connection con = databaseAccess.acquireReadOnly();
        try {
            return getChunks(documentId, userId, contextId, con);
        } finally {
            databaseAccess.releaseReadOnly(con);
        }
    }

    private static Optional<List<Chunk>> getChunks(UUID documentId, int userId, int contextId, Connection con) throws OXException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT scality_id, offset, length FROM scality_filestore WHERE cid=? AND user=? AND document_id=?");
            stmt.setInt(1, contextId);
            stmt.setInt(2, userId);
            stmt.setBytes(3, UUIDs.toByteArray(documentId));
            rs = stmt.executeQuery();
            if (!rs.next()) {
                return Optional.empty();
            }

            List<Chunk> chunks = new ArrayList<Chunk>();
            do {
                chunks.add(new Chunk(documentId, UUIDs.toUUID(rs.getBytes(1)), rs.getLong(2), rs.getLong(3)));
            } while (rs.next());
            Collections.sort(chunks);
            return Optional.of(chunks);
        } catch (SQLException e) {
            throw SproxydExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

    @Override
    public Chunk getChunk(UUID chunkId) throws OXException {
        Connection con = databaseAccess.acquireReadOnly();
        try {
            return getChunk(chunkId, contextId, con);
        } finally {
            databaseAccess.releaseReadOnly(con);
        }
    }

    private static Chunk getChunk(UUID chunkId, int contextId, Connection con) throws OXException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT document_id, offset, length FROM scality_filestore WHERE cid=? AND scality_id=?");
            stmt.setInt(1, contextId);
            stmt.setBytes(2, UUIDs.toByteArray(chunkId));
            rs = stmt.executeQuery();
            if (!rs.next()) {
                throw SproxydExceptionCode.NO_SUCH_CHUNK.create(UUIDs.getUnformattedString(chunkId));
            }
            return new Chunk(UUIDs.toUUID(rs.getBytes(1)), chunkId, rs.getLong(2), rs.getLong(3));
        } catch (SQLException e) {
            throw SproxydExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

    @Override
    public Chunk getNextChunk(UUID chunkId, UUID documentId) throws OXException {
        Connection con = databaseAccess.acquireReadOnly();
        try {
            return getNextChunk(chunkId, documentId, userId, contextId, con);
        } finally {
            databaseAccess.releaseReadOnly(con);
        }
    }

    private static Chunk getNextChunk(UUID chunkId, UUID documentId, int userId, int contextId, Connection con) throws OXException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            Chunk chunk = getChunk(chunkId, contextId, con);

            stmt = con.prepareStatement("SELECT scality_id, offset, length FROM scality_filestore WHERE cid=? AND user=? AND document_id=? AND offset=?");
            stmt.setInt(1, contextId);
            stmt.setInt(2, userId);
            stmt.setBytes(3, UUIDs.toByteArray(documentId));
            stmt.setLong(4, chunk.getOffset() + chunk.getLength());
            rs = stmt.executeQuery();
            if (!rs.next()) {
                throw SproxydExceptionCode.NO_NEXT_CHUNK.create(UUIDs.getUnformattedString(chunkId));
            }
            return new Chunk(documentId, UUIDs.toUUID(rs.getBytes(1)), rs.getLong(2), rs.getLong(3));
        } catch (SQLException e) {
            throw SproxydExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

    @Override
    public Optional<Chunk> getLastChunk(UUID documentId) throws OXException {
        Connection con = databaseAccess.acquireReadOnly();
        try {
            return Optional.ofNullable(getLastChunk(documentId, userId, contextId, con));
        } finally {
            databaseAccess.releaseReadOnly(con);
        }
    }

    private static Chunk getLastChunk(UUID documentId, int userId, int contextId, Connection con) throws OXException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT scality_id, offset, length FROM scality_filestore WHERE cid=? AND user=? AND document_id=? ORDER BY offset DESC LIMIT 1");
            stmt.setInt(1, contextId);
            stmt.setInt(2, userId);
            stmt.setBytes(3, UUIDs.toByteArray(documentId));
            rs = stmt.executeQuery();
            if (!rs.next()) {
                return null;
            }
            return new Chunk(documentId, UUIDs.toUUID(rs.getBytes(1)), rs.getLong(2), rs.getLong(3));
        } catch (SQLException e) {
            throw SproxydExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

    @Override
    public Chunk storeChunk(Chunk chunk) throws OXException {
        Connection con = databaseAccess.acquireWritable();
        try {
            return storeChunk(chunk, contextId, userId, con);
        } finally {
            databaseAccess.releaseWritable(con, true);
        }
    }

    private static Chunk storeChunk(Chunk chunk, int contextId, int userId, Connection con) throws OXException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("INSERT INTO scality_filestore (cid, user, document_id, scality_id, offset, length) VALUES (?, ?, ?, ?, ?, ?)");
            stmt.setInt(1, contextId);
            stmt.setInt(2, userId);
            stmt.setBytes(3, UUIDs.toByteArray(chunk.getDocumentId()));
            stmt.setBytes(4, UUIDs.toByteArray(chunk.getScalityId()));
            stmt.setLong(5, chunk.getOffset());
            stmt.setLong(6, chunk.getLength());
            stmt.executeUpdate();
            return chunk;
        } catch (SQLException e) {
            throw SproxydExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }

    @Override
    public boolean deleteChunk(UUID chunkId) throws OXException {
        Connection con = databaseAccess.acquireWritable();
        boolean deleted = false;
        try {
            deleted = deleteChunk(chunkId, contextId, con);
            return deleted;
        } finally {
            databaseAccess.releaseWritable(con, !deleted);
        }
    }

    private static boolean deleteChunk(UUID chunkId, int contextId, Connection con) throws OXException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("DELETE FROM scality_filestore WHERE cid=? AND scality_id=?");
            stmt.setInt(1, contextId);
            stmt.setBytes(2, UUIDs.toByteArray(chunkId));
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw SproxydExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }

    @Override
    public boolean deleteDocument(UUID documentId) throws OXException {
        Connection con = databaseAccess.acquireWritable();
        boolean deleted = false;
        try {
            deleted = deleteDocument(documentId, userId, contextId, con);
            return deleted;
        } finally {
            databaseAccess.releaseWritable(con, !deleted);
        }
    }

    private static boolean deleteDocument(UUID documentId, int userId, int contextId, Connection con) throws OXException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("DELETE FROM scality_filestore WHERE cid=? AND user=? AND document_id=?");
            stmt.setInt(1, contextId);
            stmt.setInt(2, userId);
            stmt.setBytes(3, UUIDs.toByteArray(documentId));
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw SproxydExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }

}
