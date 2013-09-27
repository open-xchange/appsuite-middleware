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
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import com.openexchange.config.ConfigurationService;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageEventConstants;
import com.openexchange.java.Streams;
import com.openexchange.preview.PreviewExceptionCodes;
import com.openexchange.preview.cache.CachedPreview;
import com.openexchange.preview.cache.PreviewCache;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;

/**
 * {@link RdbPreviewCacheImpl} - The database-backed preview cache implementation for documents.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class RdbPreviewCacheImpl implements PreviewCache, EventHandler {

    private static final Log LOG = com.openexchange.log.Log.loggerFor(RdbPreviewCacheImpl.class);

    /**
     * Initializes a new {@link RdbPreviewCacheImpl}.
     */
    public RdbPreviewCacheImpl() {
        super();
    }

    @Override
    public void handleEvent(final Event event) {
        final String topic = event.getTopic();
        if (FileStorageEventConstants.UPDATE_TOPIC.equals(topic)) {
            try {
                final Session session = (Session) event.getProperty(FileStorageEventConstants.SESSION);
                final int userId = session.getUserId();
                final int contextId = session.getContextId();
                removeAlikes(event.getProperty(FileStorageEventConstants.E_TAG).toString(), userId, contextId);
            } catch (final OXException e) {
                LOG.warn("Couldn't remove cache entry.", e);
            }
        } else if (FileStorageEventConstants.DELETE_TOPIC.equals(topic)) {
            try {
                final Session session = (Session) event.getProperty(FileStorageEventConstants.SESSION);
                final int userId = session.getUserId();
                final int contextId = session.getContextId();
                removeAlikes(event.getProperty(FileStorageEventConstants.E_TAG).toString(), userId, contextId);
            } catch (final OXException e) {
                LOG.warn("Couldn't remove cache entry.", e);
            }
        }
    }

    @Override
    public boolean save(final String id, final CachedPreview preview, final int userId, final int contextId) throws OXException {
        final InputStream in = preview.getInputStream();
        if (null == in) {
            return save(id, preview.getBytes(), preview.getFileName(), preview.getFileType(), userId, contextId);
        }
        return save(id, in, preview.getFileName(), preview.getFileType(), userId, contextId);
    }

    @Override
    public boolean save(final String id, final InputStream in, final String optName, final String optType, final int userId, final int contextId) throws OXException {
        try {
            return save(id, Streams.stream2bytes(in), optName, optType, userId, contextId);
        } catch (final IOException e) {
            throw PreviewExceptionCodes.IO_ERROR.create(e, e.getMessage());
        }
    }

    @Override
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
            int pos = 1;
            if (exists) {
                /*
                 * Update
                 */
                stmt = con.prepareStatement("UPDATE preview SET size = ?, createdAt = ?, fileName = ?, fileType = ? WHERE cid = ? AND user = ? AND id = ?");
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
                Databases.closeSQLStuff(stmt);

                pos = 1;
                stmt = con.prepareStatement("UPDATE previewData SET data = ? WHERE cid = ? AND user = ? AND id = ?");
                stmt.setBinaryStream(pos++, Streams.newByteArrayInputStream(bytes));
                stmt.setLong(pos++, contextId);
                stmt.setLong(pos++, userId);
                stmt.setString(pos++, id);
                stmt.executeUpdate();
            } else {
                /*
                 * Insert
                 */
                stmt = con.prepareStatement("INSERT INTO preview (cid, user, id, size, createdAt, fileName, fileType) VALUES (?, ?, ?, ?, ?, ?, ?)");
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
                Databases.closeSQLStuff(stmt);

                pos = 1;
                stmt = con.prepareStatement("INSERT INTO previewData (cid, user, id, data) VALUES (?, ?, ?, ?)");
                stmt.setLong(pos++, contextId);
                stmt.setLong(pos++, userId);
                stmt.setString(pos++, id);
                stmt.setBinaryStream(pos++, Streams.newByteArrayInputStream(bytes));
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

    @Override
    public long[] getContextQuota(final int contextId) {
        long quota = -1L;
        long quotaPerDocument = -1L;

        // TODO: Check context-wise quota values

        final ConfigurationService confService = ServerServiceRegistry.getInstance().getService(ConfigurationService.class);
        if (null != confService) {
            String property = confService.getProperty("com.openexchange.preview.cache.quota", "-1").trim();
            try {
                quota = Long.parseLong(property);
            } catch (final NumberFormatException e) {
                quota = -1L;
            }
            property = confService.getProperty("com.openexchange.preview.cache.quotaPerDocument", "-1").trim();
            try {
                quotaPerDocument = Long.parseLong(property);
            } catch (final NumberFormatException e) {
                quotaPerDocument = -1L;
            }
        }
        return new long[] { quota, quotaPerDocument };
    }

    @Override
    public boolean ensureUnexceededContextQuota(final long desiredSize, final long total, final long totalPerDocument, final int contextId, final String ignoree) throws OXException {
        if (total <= 0L) {
            // Unlimited total quota
            return (totalPerDocument <= 0 || desiredSize <= totalPerDocument);
        }
        // Check if document's size fits into quota limits at all
        if (desiredSize > total || desiredSize > totalPerDocument) {
            return false;
        }
        // Try to create space through removing oldest entries
        // until enough space is available
        final DatabaseService dbService = ServerServiceRegistry.getInstance().getService(DatabaseService.class);
        if (dbService == null) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(DatabaseService.class.getName());
        }
        Connection con = dbService.getReadOnly(contextId);
        boolean readOnly = true;
        try {
            long usedContextQuota = getUsedContextQuota(contextId, ignoree, con);
            if (usedContextQuota <= 0 && desiredSize > total) {
                return false;
            }
            while (usedContextQuota + desiredSize > total) {
                // Upgrade to writable connection
                if (readOnly) {
                    dbService.backReadOnly(contextId, con);
                    con = dbService.getWritable(contextId);
                    readOnly = false;
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
            if (readOnly) {
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
            stmt = con.prepareStatement("SELECT id FROM preview WHERE cid = ? AND createdAt <= ?");
            stmt.setLong(1, contextId);
            stmt.setLong(2, oldestStamp);
            rs = stmt.executeQuery();
            final Set<String> ids = new HashSet<String>(4);
            while (rs.next()) {
                ids.add(rs.getString(1));
            }
            Databases.closeSQLStuff(rs, stmt);
            rs = null;

            stmt = con.prepareStatement("DELETE FROM preview WHERE cid = ? AND createdAt <= ?");
            stmt.setLong(1, contextId);
            stmt.setLong(2, oldestStamp);
            stmt.executeUpdate();
            Databases.closeSQLStuff(stmt);
            stmt = null;

            if (!ids.isEmpty()) {
                stmt = con.prepareStatement("DELETE FROM previewData WHERE cid = ? AND id = ?");
                stmt.setLong(1, contextId);
                for (final String id : ids) {
                    stmt.setString(2, id);
                    stmt.addBatch();
                }
                stmt.executeBatch();
                Databases.closeSQLStuff(stmt);
                stmt = null;
            }
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

    @Override
    public void clearFor(final int contextId) throws OXException {
        final DatabaseService dbService = ServerServiceRegistry.getInstance().getService(DatabaseService.class);
        if (dbService == null) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(DatabaseService.class.getName());
        }
        final Connection con = dbService.getWritable(contextId);
        try {
            clearFor(contextId, con);
        } finally {
            dbService.backWritable(contextId, con);
        }
    }

    private void clearFor(final int contextId, final Connection con) throws OXException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        boolean rollback = false;
        try {
            Databases.startTransaction(con);
            rollback = true;

            stmt = con.prepareStatement("SELECT id FROM preview WHERE cid=?");
            stmt.setInt(1, contextId);
            rs = stmt.executeQuery();
            final Set<String> ids = new HashSet<String>(4);
            while (rs.next()) {
                ids.add(rs.getString(1));
            }
            Databases.closeSQLStuff(rs, stmt);
            rs = null;

            stmt = con.prepareStatement("DELETE FROM preview WHERE cid=?");
            stmt.setLong(1, contextId);
            stmt.executeUpdate();
            Databases.closeSQLStuff(stmt);
            stmt = null;

            if (!ids.isEmpty()) {
                stmt = con.prepareStatement("DELETE FROM previewData WHERE cid=? AND id=?");
                stmt.setLong(1, contextId);
                for (final String id : ids) {
                    stmt.setString(2, id);
                    stmt.addBatch();
                }
                stmt.executeBatch();
                Databases.closeSQLStuff(stmt);
                stmt = null;
            }

            con.commit();
            rollback = false;
        } catch (final SQLException e) {
            throw PreviewExceptionCodes.ERROR.create(e, e.getMessage());
        } finally {
            if (rollback) {
                Databases.rollback(con);
            }
            Databases.autocommit(con);
            Databases.closeSQLStuff(rs, stmt);
        }
    }

    @Override
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
                stmt = con.prepareStatement("SELECT fileName, fileType, size FROM preview WHERE cid = ? AND user = ? AND id = ?");
                stmt.setLong(1, contextId);
                stmt.setLong(2, userId);
                stmt.setString(3, id);
            } else {
                // A context-global document
                stmt = con.prepareStatement("SELECT fileName, fileType, size FROM preview WHERE cid = ? AND id = ?");
                stmt.setLong(1, contextId);
                stmt.setString(2, id);
            }
            rs = stmt.executeQuery();
            if (!rs.next()) {
                return null;
            }
            // Remember meta data
            final String fileName = rs.getString(1);
            final String fileType = rs.getString(2);
            final long size = rs.getLong(3);
            Databases.closeSQLStuff(rs, stmt);
            // Load binary data
            if (userId > 0) {
                // A user-sensitive document
                stmt = con.prepareStatement("SELECT data FROM previewData WHERE cid = ? AND user = ? AND id = ?");
                stmt.setLong(1, contextId);
                stmt.setLong(2, userId);
                stmt.setString(3, id);
            } else {
                // A context-global document
                stmt = con.prepareStatement("SELECT data FROM previewData WHERE cid = ? AND id = ?");
                stmt.setLong(1, contextId);
                stmt.setString(2, id);
            }
            rs = stmt.executeQuery();
            if (!rs.next()) {
                return null;
            }
            // Return CachedPreview instance
            return new CachedPreview(Streams.stream2bytes(rs.getBinaryStream(1)), fileName, fileType, size);
        } catch (final SQLException e) {
            throw PreviewExceptionCodes.ERROR.create(e, e.getMessage());
        } catch (final IOException e) {
            throw PreviewExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

    @Override
    public void remove(final int userId, final int contextId) throws OXException {
        final DatabaseService databaseService = ServerServiceRegistry.getInstance().getService(DatabaseService.class);
        if (databaseService == null) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(DatabaseService.class.getName());
        }
        final Connection con = databaseService.getWritable(contextId);
        boolean committed = true;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            con.setAutoCommit(false);
            committed = false;
            if (userId > 0) {
                stmt = con.prepareStatement("SELECT id FROM preview WHERE cid=? AND user=?");
                int pos = 1;
                stmt.setInt(pos++, contextId);
                stmt.setInt(pos, userId);
            } else {
                stmt = con.prepareStatement("SELECT id FROM preview WHERE cid=?");
                stmt.setInt(1, contextId);
            }
            rs = stmt.executeQuery();
            final Set<String> ids = new HashSet<String>(16);
            while (rs.next()) {
                ids.add(rs.getString(1));
            }
            Databases.closeSQLStuff(rs, stmt);
            rs = null;

            if (userId > 0) {
                stmt = con.prepareStatement("DELETE FROM preview WHERE cid=? AND user=?");
                int pos = 1;
                stmt.setInt(pos++, contextId);
                stmt.setInt(pos, userId);
            } else {
                stmt = con.prepareStatement("DELETE FROM preview WHERE cid=?");
                stmt.setInt(1, contextId);
            }
            stmt.executeUpdate();
            Databases.closeSQLStuff(stmt);
            stmt = null;

            if (!ids.isEmpty()) {
                int pos = 1;
                if (userId > 0) {
                    stmt = con.prepareStatement("DELETE FROM previewData WHERE cid=? AND user=? AND id=?");
                    stmt.setInt(pos++, contextId);
                    stmt.setInt(pos++, userId);
                } else {
                    stmt = con.prepareStatement("DELETE FROM previewData WHERE cid=? AND id=?");
                    stmt.setInt(pos++, contextId);
                }
                for (final String id : ids) {
                    stmt.setString(pos, id);
                    stmt.addBatch();
                }
                stmt.executeBatch();
                Databases.closeSQLStuff(stmt);
                stmt = null;
            }
            con.commit();
            committed = true;
        } catch (final DataTruncation e) {
            throw PreviewExceptionCodes.ERROR.create(e, e.getMessage());
        } catch (final SQLException e) {
            throw PreviewExceptionCodes.ERROR.create(e, e.getMessage());
        } finally {
            if (!committed) {
                Databases.rollback(con);
            }
            Databases.closeSQLStuff(rs, stmt);
            Databases.autocommit(con);
            databaseService.backWritable(contextId, con);
        }
    }

    @Override
    public void removeAlikes(final String id, final int userId, final int contextId) throws OXException {
        final DatabaseService databaseService = ServerServiceRegistry.getInstance().getService(DatabaseService.class);
        if (databaseService == null) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(DatabaseService.class.getName());
        }
        if (null == id) {
            throw PreviewExceptionCodes.ERROR.create("Missing identifier.");
        }
        final Connection con = databaseService.getWritable(contextId);
        boolean committed = true;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            con.setAutoCommit(false);
            committed = false;

            stmt = con.prepareStatement("SELECT id FROM preview WHERE cid=? AND user=? AND id LIKE ?");
            int pos = 1;
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos++, userId);
            stmt.setString(pos, id + "%");
            rs = stmt.executeQuery();
            final Set<String> ids = new HashSet<String>(16);
            while (rs.next()) {
                ids.add(rs.getString(1));
            }
            Databases.closeSQLStuff(rs, stmt);
            rs = null;

            stmt = con.prepareStatement("DELETE FROM preview WHERE cid=? AND user=? AND id LIKE ?");
            pos = 1;
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos++, userId);
            stmt.setString(pos, id + "%");
            stmt.executeUpdate();
            Databases.closeSQLStuff(stmt);
            stmt = null;

            if (!ids.isEmpty()) {
                stmt = con.prepareStatement("DELETE FROM previewData WHERE cid=? AND user=? AND id=?");
                pos = 1;
                stmt.setInt(pos++, contextId);
                stmt.setInt(pos++, userId);
                for (final String ide : ids) {
                    stmt.setString(pos, ide);
                    stmt.addBatch();
                }
                stmt.executeBatch();
                Databases.closeSQLStuff(stmt);
                stmt = null;
            }

            con.commit();
            committed = true;
        } catch (final DataTruncation e) {
            throw PreviewExceptionCodes.ERROR.create(e, e.getMessage());
        } catch (final SQLException e) {
            throw PreviewExceptionCodes.ERROR.create(e, e.getMessage());
        } finally {
            if (!committed) {
                Databases.rollback(con);
            }
            Databases.closeSQLStuff(rs, stmt);
            Databases.autocommit(con);
            databaseService.backWritable(contextId, con);
        }
    }

    @Override
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
