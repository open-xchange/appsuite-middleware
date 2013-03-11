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
import java.sql.Types;
import com.openexchange.config.ConfigurationService;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.java.Streams;
import com.openexchange.preview.PreviewExceptionCodes;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.services.ServerServiceRegistry;

/**
 * {@link PreviewCache} - The preview document cache.
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

    /**
     * Stores given preview document's binary content.
     * 
     * @param id The identifier
     * @param preview The cached preview
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return <code>true</code> on insertion or <code>false</code> if impossible to store
     * @throws OXException If operations fails
     */
    public boolean save(final String id, final CachedPreview preview, final int userId, final int contextId) throws OXException {
        final InputStream in = preview.getInputStream();
        if (null == in) {
            return save(id, preview.getBytes(), preview.getFileName(), preview.getFileType(), userId, contextId);
        }
        return save(id, in, preview.getFileName(), preview.getFileType(), userId, contextId);
    }

    /**
     * Stores given preview document's binary content.
     * 
     * @param id The identifier
     * @param in The binary stream
     * @param optName The optional file name
     * @param optType The optional file MIME type; e.g. <code>"image/jpeg"</code>
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return <code>true</code> on insertion or <code>false</code> if impossible to store
     * @throws OXException If operations fails
     */
    public boolean save(final String id, final InputStream in, final String optName, final String optType, final int userId, final int contextId) throws OXException {
        try {
            return save(id, Streams.stream2bytes(in), optName, optType, userId, contextId);
        } catch (final IOException e) {
            throw PreviewExceptionCodes.IO_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Stores given preview document's binary content.
     * 
     * @param id The identifier
     * @param bytes The binary content
     * @param optName The optional file name
     * @param optType The optional file MIME type; e.g. <code>"image/jpeg"</code>
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return <code>true</code> on insertion or <code>false</code> if impossible to store
     * @throws OXException If operations fails
     */
    public boolean save(final String id, final byte[] bytes, final String optName, final String optType, final int userId, final int contextId) throws OXException {
        // Check existence
        final boolean exists = exists(id, userId, contextId);
        // Get quota
        final long[] qts = getContextQuota(contextId);
        final long total = qts[0];
        final long totalPerDocument = qts[0];
        if (total > 0 || totalPerDocument > 0) {
            final String ignoree = exists ? id : null;
            if (!ensureUnexceededContextQuota(bytes.length, total, totalPerDocument, contextId, ignoree)) {
                return false;
            }
        }
        final DatabaseService databaseService = ServerServiceRegistry.getInstance().getService(DatabaseService.class);
        if (databaseService == null) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(DatabaseService.class.getName());
        }
        final Connection con = databaseService.getWritable(contextId);
        boolean committed = true;
        PreparedStatement stmt = null;
        try {
            /*
             * Load
             */
            con.setAutoCommit(false);
            committed = false;
            final long now = System.currentTimeMillis();
            if (exists) {
                /*
                 * Update
                 */
                stmt = con.prepareStatement("UPDATE preview SET data = ?, size = ?, createdAt = ?, fileName = ?, fileType = ? WHERE cid = ? AND user = ? AND id = ?");
                int pos = 1;
                stmt.setBinaryStream(pos++, Streams.newByteArrayInputStream(bytes));
                stmt.setLong(pos++, bytes.length);
                stmt.setLong(pos++, now);
                if (null == optName) {
                    stmt.setNull(pos++, Types.VARCHAR);
                } else {
                    stmt.setString(pos++, optName);
                }
                if (null == optType) {
                    stmt.setNull(pos++, Types.VARCHAR);
                } else {
                    stmt.setString(pos++, optType);
                }
                stmt.setLong(pos++, contextId);
                stmt.setLong(pos++, userId);
                stmt.setString(pos++, id);
                stmt.executeUpdate();
            } else {
                /*
                 * Insert
                 */
                stmt = con.prepareStatement("INSERT INTO preview (cid, user, id, size, createdAt, data, fileName, fileType) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");
                int pos = 1;
                stmt.setLong(pos++, contextId);
                stmt.setLong(pos++, userId);
                stmt.setString(pos++, id);
                stmt.setLong(pos++, bytes.length);
                stmt.setLong(pos++, now);
                stmt.setBinaryStream(pos++, Streams.newByteArrayInputStream(bytes));
                if (null == optName) {
                    stmt.setNull(pos++, Types.VARCHAR);
                } else {
                    stmt.setString(pos++, optName);
                }
                if (null == optType) {
                    stmt.setNull(pos++, Types.VARCHAR);
                } else {
                    stmt.setString(pos++, optType);
                }
                stmt.executeUpdate();
            }
            con.commit();
            committed = true;
            return true;
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
    public long[] getContextQuota(final int contextId) {
        long quota = -1L;
        long quotaPerDocument = -1L;

        // TODO: Check context-wise quota values

        final ConfigurationService confService = ServerServiceRegistry.getInstance().getService(ConfigurationService.class);
        if (null != confService) {
            String property = confService.getProperty("com.openexchange.preview.cache.quota", "-1").trim();
            try {
                quota = Long.parseLong(property);
            } catch (NumberFormatException e) {
                quota = -1L;
            }
            property = confService.getProperty("com.openexchange.preview.cache.quotaPerDocument", "-1").trim();
            try {
                quotaPerDocument = Long.parseLong(property);
            } catch (NumberFormatException e) {
                quotaPerDocument = -1L;
            }
        }
        return new long[] { quota, quotaPerDocument };
    }

    /**
     * Ensures enough space is available for desired size if context-sensitive caching quota is specified.
     * 
     * @param desiredSize The desired size
     * @param total The context-sensitive caching quota
     * @param totalPerDocument The context-sensitive caching quota per document
     * @param contextId The context identifier
     * @param ignoree The optional identifier to ignore while checking
     * @return <code>true</code> If enough space is available; otherwise <code>false</code>
     * @throws OXException If an error occurs
     */
    public boolean ensureUnexceededContextQuota(final long desiredSize, final long total, final long totalPerDocument, final int contextId, final String ignoree) throws OXException {
        if (total <= 0L) {
            // Unlimited quota
            if (totalPerDocument > 0 && desiredSize > totalPerDocument) {
                return false;
            }
            return true;
        }
        if (desiredSize > total || desiredSize > totalPerDocument) {
            return false;
        }
        // Create space
        final DatabaseService dbService = ServerServiceRegistry.getInstance().getService(DatabaseService.class);
        if (dbService == null) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(DatabaseService.class.getName());
        }
        Connection con = dbService.getReadOnly(contextId);
        boolean readOlny = true;
        try {
            long usedContextQuota = getUsedContextQuota(contextId, ignoree, con);
            if (usedContextQuota <= 0 && desiredSize > total) {
                return false;
            }
            while (usedContextQuota + desiredSize > total) {
                // Upgrade to writable connection
                if (readOlny) {
                    dbService.backReadOnly(contextId, con);
                    con = dbService.getWritable(contextId);
                    readOlny = false;
                }
                // Drop oldest entry
                dropOldestEntry(contextId, con);
                // Re-Calculate used quota
                usedContextQuota = getUsedContextQuota(contextId, ignoree, con);
                if (usedContextQuota <= 0 && desiredSize > total) {
                    return false;
                }
            }
            return true;
        } finally {
            if (readOlny) {
                dbService.backReadOnly(contextId, con);
            } else {
                dbService.backWritable(contextId, con);
            }
        }
    }

    private void dropOldestEntry(final int contextId, final Connection con) throws OXException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT MIN(createdAt) FROM preview WHERE cid = ?");
            stmt.setLong(1, contextId);
            rs = stmt.executeQuery();
            if (!rs.next()) {
                return;
            }
            final long oldestStamp = rs.getLong(1);
            if (rs.wasNull()) {
                return;
            }
            Databases.closeSQLStuff(rs, stmt);
            rs = null;
            // Delete entry
            stmt = con.prepareStatement("DELETE FROM preview WHERE cid = ? AND createdAt <= ?");
            stmt.setLong(1, contextId);
            stmt.setLong(2, oldestStamp);
            stmt.executeUpdate();
        } catch (final SQLException e) {
            throw PreviewExceptionCodes.ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
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
            if (rs.wasNull()) {
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
     * Gets the preview document's binary content.
     * 
     * @param id The document identifier
     * @param userId The user identifier or <code>-1</code> for context-global document
     * @param contextId The context identifier
     * @return The binary content or <code>null</code>
     * @throws OXException If retrieving document data fails
     */
    public CachedPreview get(final String id, final int userId, final int contextId) throws OXException {
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

    private CachedPreview load(final String id, final int userId, final int contextId, final Connection con) throws OXException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            if (userId > 0) {
                // A user-sensitive document
                stmt = con.prepareStatement("SELECT data, fileName, fileType, size FROM preview WHERE cid = ? AND user = ? AND id = ?");
                stmt.setLong(1, contextId);
                stmt.setLong(2, userId);
                stmt.setString(3, id);
            } else {
                // A context-global document
                stmt = con.prepareStatement("SELECT data, fileName, fileType, size FROM preview WHERE cid = ? AND id = ?");
                stmt.setLong(1, contextId);
                stmt.setString(2, id);
            }
            rs = stmt.executeQuery();
            if (!rs.next()) {
                return null;
            }
            return new CachedPreview(Streams.stream2bytes(rs.getBinaryStream(1)), rs.getString(2), rs.getString(3), rs.getLong(4));
        } catch (final SQLException e) {
            throw PreviewExceptionCodes.ERROR.create(e, e.getMessage());
        } catch (final IOException e) {
            throw PreviewExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

    /**
     * Tests for existence of denoted preview document.
     * 
     * @param id The identifier
     * @param userId The user identifier or <code>-1</code> for context-global document
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

    private boolean exists(final String id, final int userId, final int contextId, final Connection con) throws OXException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            if (userId > 0) {
                // A user-sensitive document
                stmt = con.prepareStatement("SELECT 1 FROM preview WHERE cid = ? AND user = ? AND id = ?");
                stmt.setLong(1, contextId);
                stmt.setLong(2, userId);
                stmt.setString(3, id);
            } else {
                // A context-global document
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
