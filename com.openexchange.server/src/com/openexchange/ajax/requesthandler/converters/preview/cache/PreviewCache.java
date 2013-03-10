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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.ajax.requesthandler.converters.preview.cache;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DataTruncation;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.java.Streams;
import com.openexchange.preview.PreviewExceptionCodes;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.services.ServerServiceRegistry;

/**
 * {@link PreviewCache} - The preview image cache.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class PreviewCache {

    /**
     * Initializes a new {@link PreviewCache}.
     */
    public PreviewCache() {
        super();
    }

    public boolean save(final String id, final InputStream in, final int userId, final int contextId) throws OXException, IOException {
        return save(id, Streams.stream2bytes(in), userId, contextId);
    }

    public boolean save(final String id, final byte[] bytes, final int userId, final int contextId) throws OXException {
        final long total = getContextQuota(contextId);
        final boolean exists = exists(id, userId, contextId);
        if (total > 0) {
            final String ignoree = exists ? id : null;
            ensureUnexceededContextQuota(bytes.length, total, contextId, ignoree);
        }
        final DatabaseService databaseService = ServerServiceRegistry.getInstance().getService(DatabaseService.class);
        if (databaseService == null) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(DatabaseService.class.getName());
        }
        final Connection con = databaseService.getWritable(contextId);
        final boolean insert;
        boolean committed = true;
        PreparedStatement stmt = null;
        try {
            /*
             * Load
             */
            con.setAutoCommit(false);
            committed = false;
            if (exists) {
                /*
                 * Update
                 */
                stmt = con.prepareStatement("UPDATE preview SET data = ?, size = ?, createdAt = ? WHERE cid = ? AND user = ? AND id = ?");
                stmt.setBinaryStream(1, Streams.newByteArrayInputStream(bytes));
                stmt.setLong(2, bytes.length);
                stmt.setLong(3, System.currentTimeMillis());
                stmt.setLong(4, contextId);
                stmt.setLong(5, userId);
                stmt.setString(6, id);
                stmt.executeUpdate();
                insert = true;
            } else {
                /*
                 * Insert
                 */
                stmt = con.prepareStatement("INSERT INTO preview (cid, user, id, size, createdAt, data) VALUES (?, ?, ?, ?, ?, ?)");
                stmt.setLong(1, contextId);
                stmt.setLong(2, userId);
                stmt.setString(3, id);
                stmt.setLong(4, bytes.length);
                stmt.setLong(5, System.currentTimeMillis());
                stmt.setBinaryStream(6, Streams.newByteArrayInputStream(bytes));
                stmt.executeUpdate();
                insert = false;
            }
            con.commit();
            committed = true;
            return insert;
        } catch (final DataTruncation e) {
            throw PreviewExceptionCodes.ERROR.create(e, e.getMessage());
        } catch (final SQLException e) {
            throw PreviewExceptionCodes.ERROR.create(e, e.getMessage());
        } finally {
            if (!committed) {
                Databases.rollback(con);
            }
            Databases.closeSQLStuff(stmt);
            Databases.autocommit(con);
            databaseService.backWritable(contextId, con);
        }
    }

    /**
     * Gets the caching quota for denoted context.
     * 
     * @param contextId The context identifier
     * @return The context quota or <code>-1</code> if unlimited
     */
    public long getContextQuota(final int contextId) {
        // TODO:
        return -1L;
    }

    /**
     * Ensures enough space is available for desired size if context-sensitive caching quota is specified.
     * 
     * @param desiredSize The desired size
     * @param total The context-sensitive caching quota
     * @param contextId The context identifier
     * @param ignoree The optional identifier to ignore while checking
     * @throws OXException If an error occurs
     */
    public void ensureUnexceededContextQuota(final long desiredSize, final long total, final int contextId, final String ignoree) throws OXException {
        if (total > 0L) {
            final DatabaseService dbService = ServerServiceRegistry.getInstance().getService(DatabaseService.class);
            if (dbService == null) {
                throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(DatabaseService.class.getName());
            }
            Connection con = dbService.getReadOnly(contextId);
            boolean readOlny = true;
            try {
                while (getUsedContextQuota(contextId, ignoree, con) + desiredSize > total) {
                    // Upgrade to writable connection
                    if (readOlny) {
                        dbService.backReadOnly(contextId, con);
                        con = dbService.getWritable(contextId);
                        readOlny = false;
                    }
                    // Drop oldest entry
                    dropOldestEntry(contextId, con);
                }
            } finally {
                if (readOlny) {
                    dbService.backReadOnly(contextId, con);
                } else {
                    dbService.backWritable(contextId, con);
                }
            }
            
        }
    }

    private void dropOldestEntry(final int contextId, final Connection con) throws OXException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("DELETE FROM preview WHERE cid = ? AND createdAt <= MIN(createdAt)");
            stmt.setLong(1, contextId);
            stmt.executeUpdate();
        } catch (final SQLException e) {
            throw PreviewExceptionCodes.ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }

    private long getUsedContextQuota(final int contextId, final String ignoree, final Connection con) throws OXException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            if (null == ignoree) {
                stmt = con.prepareStatement("SELECT SUM(size) FROM preview WHERE cid = ?");
                stmt.setLong(1, contextId);
            } else {
                stmt = con.prepareStatement("SELECT SUM(size) FROM preview WHERE cid = ? AND id <> ?");
                stmt.setLong(1, contextId);
                stmt.setString(2, ignoree);
            }
            rs = stmt.executeQuery();
            if (!rs.next()) {
                return 0L;
            }
            return rs.getLong(1);
        } catch (final SQLException e) {
            throw PreviewExceptionCodes.ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }
    
    /**
     * Gets the preview image's binary content.
     * 
     * @param id The image identifier
     * @param userId The user identifier or <code>-1</code> for context-global image
     * @param contextId The context identifier
     * @return The binary content or <code>null</code>
     * @throws OXException If retrieving image data fails
     */
    public byte[] get(final String id, final int userId, final int contextId) throws OXException {
        if (null == id || contextId <= 0) {
            return null;
        }
        final DatabaseService dbService = ServerServiceRegistry.getInstance().getService(DatabaseService.class);
        if (dbService == null) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(DatabaseService.class.getName());
        }
        final Connection con = dbService.getReadOnly(contextId);
        try {
            return load(id, userId, contextId, con);
        } finally {
            dbService.backReadOnly(contextId, con);
        }
    }

    private byte[] load(final String id, final int userId, final int contextId, final Connection con) throws OXException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            if (userId > 0) {
                // A user-sensitive image
                stmt = con.prepareStatement("SELECT data FROM preview WHERE cid = ? AND user = ? AND id = ?");
                stmt.setLong(1, contextId);
                stmt.setLong(2, userId);
                stmt.setString(3, id);
            } else {
                // A context-global image
                stmt = con.prepareStatement("SELECT data FROM preview WHERE cid = ? AND id = ?");
                stmt.setLong(1, contextId);
                stmt.setString(2, id);
            }
            rs = stmt.executeQuery();
            if (!rs.next()) {
                return null;
            }
            return Streams.stream2bytes(rs.getBinaryStream(1));
        } catch (final SQLException e) {
            throw PreviewExceptionCodes.ERROR.create(e, e.getMessage());
        } catch (final IOException e) {
            throw PreviewExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

    /**
     * Tests for existence of denoted preview image.
     * 
     * @param id The identifier
     * @param userId The user identifier or <code>-1</code> for context-global image
     * @param contextId The context identifier
     * @return <code>true</code> if exists; otherwise <code>false</code>
     * @throws OXException If an error occurs while checking existence
     */
    public boolean exists(final String id, final int userId, final int contextId) throws OXException {
        if (null == id || contextId <= 0) {
            return false;
        }
        final DatabaseService dbService = ServerServiceRegistry.getInstance().getService(DatabaseService.class);
        if (dbService == null) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(DatabaseService.class.getName());
        }
        final Connection con = dbService.getReadOnly(contextId);
        try {
            return exists(id, userId, contextId, con);
        } finally {
            dbService.backReadOnly(contextId, con);
        }
    }

    private boolean exists(final String id, final int userId, final int contextId, final DatabaseService dbService) throws OXException {
        final Connection con = dbService.getReadOnly(contextId);
        try {
            return exists(id, userId, contextId, con);
        } finally {
            dbService.backReadOnly(contextId, con);
        }
    }

    private boolean exists(final String id, final int userId, final int contextId, final Connection con) throws OXException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            if (userId > 0) {
                // A user-sensitive image
                stmt = con.prepareStatement("SELECT 1 FROM preview WHERE cid = ? AND user = ? AND id = ?");
                stmt.setLong(1, contextId);
                stmt.setLong(2, userId);
                stmt.setString(3, id);
            } else {
                // A context-global image
                stmt = con.prepareStatement("SELECT 1 FROM preview WHERE cid = ? AND id = ?");
                stmt.setLong(1, contextId);
                stmt.setString(2, id);
            }
            rs = stmt.executeQuery();
            return rs.next();
        } catch (final SQLException e) {
            throw PreviewExceptionCodes.ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

}
