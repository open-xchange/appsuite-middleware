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

package com.openexchange.filestore.sproxyd.chunkstorage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.sproxyd.SproxydExceptionCode;
import com.openexchange.java.util.UUIDs;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;


/**
 * {@link RdbChunkStorage}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class RdbChunkStorage implements ChunkStorage {

    private final ServiceLookup services;
    private final int contextId;
    private final int userId;

    /**
     * Initializes a new {@link RdbChunkStorage}.
     *
     * @param A service lookup reference
     * @param contextId The context identifier
     * @param userId The user identifier
     */
    public RdbChunkStorage(ServiceLookup services, int contextId, int userId) {
        super();
        this.services = services;
        this.contextId = contextId;
        this.userId = userId;
    }

    private DatabaseService getDbService() throws OXException {
        DatabaseService service = services.getOptionalService(DatabaseService.class);
        if (null == service) {
            throw ServiceExceptionCode.absentService(DatabaseService.class);
        }
        return service;
    }

    @Override
    public List<UUID> getDocuments() throws OXException {
        DatabaseService dbService = getDbService();
        Connection con = dbService.getReadOnly(contextId);
        try {
            return getDocuments(userId, contextId, con);
        } finally {
            dbService.backReadOnly(contextId, con);
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
            List<UUID> documentIds = new LinkedList<UUID>();
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
        DatabaseService dbService = getDbService();
        Connection con = dbService.getReadOnly(contextId);
        try {
            return getChunks(documentId, userId, contextId, con);
        } finally {
            dbService.backReadOnly(contextId, con);
        }
    }

    private static List<Chunk> getChunks(UUID documentId, int userId, int contextId, Connection con) throws OXException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT scality_id, offset, length FROM scality_filestore WHERE cid=? AND user=? AND document_id=?");
            stmt.setInt(1, contextId);
            stmt.setInt(2, userId);
            stmt.setBytes(3, UUIDs.toByteArray(documentId));
            rs = stmt.executeQuery();
            if (!rs.next()) {
                throw SproxydExceptionCode.NO_SUCH_DOCUMENT.create(UUIDs.getUnformattedString(documentId));
            }
            List<Chunk> chunks = new LinkedList<Chunk>();
            do {
                chunks.add(new Chunk(documentId, UUIDs.toUUID(rs.getBytes(1)), rs.getLong(2), rs.getLong(3)));
            } while (rs.next());
            Collections.sort(chunks);
            return chunks;
        } catch (SQLException e) {
            throw SproxydExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

    @Override
    public Chunk getChunk(UUID chunkId) throws OXException {
        DatabaseService dbService = getDbService();
        Connection con = dbService.getReadOnly(contextId);
        try {
            return getChunk(chunkId, contextId, con);
        } finally {
            dbService.backReadOnly(contextId, con);
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
        DatabaseService dbService = getDbService();
        Connection con = dbService.getReadOnly(contextId);
        try {
            return getNextChunk(chunkId, documentId, userId, contextId, con);
        } finally {
            dbService.backReadOnly(contextId, con);
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
    public Chunk getLastChunk(UUID documentId) throws OXException {
        DatabaseService dbService = getDbService();
        Connection con = dbService.getReadOnly(contextId);
        try {
            return getLastChunk(documentId, userId, contextId, con);
        } finally {
            dbService.backReadOnly(contextId, con);
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
        DatabaseService dbService = getDbService();
        Connection con = dbService.getWritable(contextId);
        try {
            return storeChunk(chunk, contextId, userId, con);
        } finally {
            dbService.backWritable(contextId, con);
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
        DatabaseService dbService = getDbService();
        Connection con = dbService.getWritable(contextId);
        try {
            return deleteChunk(chunkId, contextId, con);
        } finally {
            dbService.backWritable(contextId, con);
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
        DatabaseService dbService = getDbService();
        Connection con = dbService.getWritable(contextId);
        try {
            return deleteDocument(documentId, userId, contextId, con);
        } finally {
            dbService.backWritable(contextId, con);
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
