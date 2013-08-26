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

import gnu.trove.ConcurrentTIntObjectHashMap;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.sql.Connection;
import java.sql.DataTruncation;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import com.openexchange.config.ConfigurationService;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageEventConstants;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.filestore.FilestoreStorage;
import com.openexchange.java.Streams;
import com.openexchange.preview.PreviewExceptionCodes;
import com.openexchange.preview.cache.CachedPreview;
import com.openexchange.preview.cache.PreviewCache;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.tools.file.FileStorage;
import com.openexchange.tools.file.QuotaFileStorage;
import com.openexchange.tools.file.external.FileStorageCodes;

/**
 * {@link FileStorePreviewCacheImpl} - The database-backed preview cache implementation for documents.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class FileStorePreviewCacheImpl implements PreviewCache, EventHandler {

    private static final Log LOG = com.openexchange.log.Log.loggerFor(FileStorePreviewCacheImpl.class);

    private static final ConcurrentTIntObjectHashMap<FileStorage> FILE_STORE_CACHE = new ConcurrentTIntObjectHashMap<FileStorage>();

    private static FileStorage getFileStorage(final Context ctx, final boolean quotaAware) throws OXException {
        final int key = ctx.getContextId();
        FileStorage fs = FILE_STORE_CACHE.get(key);
        if (null == fs) {
            final URI uri = FilestoreStorage.createURI(ctx);
            final FileStorage newFileStorage = quotaAware ? QuotaFileStorage.getInstance(uri, ctx) : FileStorage.getInstance(uri);
            fs = FILE_STORE_CACHE.putIfAbsent(key, newFileStorage);
            if (null == fs) {
                fs = newFileStorage;
            }
        }
        return fs;
    }

    private static FileStorage getFileStorage(final int contextId, final boolean quotaAware) throws OXException {
        return getFileStorage(ContextStorage.getStorageContext(contextId), quotaAware);
    }

    // ------------------------------------------------------------------------------- //

    private final boolean quotaAware;

    /**
     * Initializes a new {@link FileStorePreviewCacheImpl}.
     */
    public FileStorePreviewCacheImpl(final boolean quotaAware) {
        super();
        this.quotaAware = quotaAware;
    }

    private void batchDeleteFiles(final Collection<String> ids, final FileStorage fileStorage) {
        try {
            fileStorage.deleteFiles(ids.toArray(new String[0]));
        } catch (final OXException e) {
            // Retry one-by-one
            for (final String id : ids) {
                try {
                    fileStorage.deleteFile(id);
                } catch (final Exception x) {
                    // Ignore
                }
            }
        }
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
        String refId = optRefId(id, userId, contextId);
        final boolean exists = null != refId;
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

        // Save file
        final FileStorage fileStorage = getFileStorage(contextId, quotaAware);
        if (null != refId) {
            fileStorage.deleteFile(refId);
        }
        refId = fileStorage.saveNewFile(Streams.newByteArrayInputStream(bytes));

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
                stmt = con.prepareStatement("UPDATE preview SET refId = ?, size = ?, createdAt = ?, fileName = ?, fileType = ?, data = NULL WHERE cid = ? AND user = ? AND id = ?");
                stmt.setString(pos++, refId);
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
            } else {
                /*
                 * Insert
                 */
                stmt = con.prepareStatement("INSERT INTO preview (cid, user, id, size, createdAt, refId, fileName, fileType, data) VALUES (?, ?, ?, ?, ?, ?, ?, ?, NULL)");
                stmt.setLong(pos++, contextId);
                stmt.setLong(pos++, userId);
                stmt.setString(pos++, id);
                stmt.setLong(pos++, bytes.length);
                stmt.setLong(pos++, now);
                stmt.setString(pos++, refId);
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
            }
            stmt.executeUpdate();
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
            stmt = null;
            // Delete entry
            stmt = con.prepareStatement("SELECT id, refId FROM preview WHERE cid = ? AND createdAt <= ?");
            stmt.setLong(1, contextId);
            stmt.setLong(2, oldestStamp);
            rs = stmt.executeQuery();
            final Map<String, String> map = new HashMap<String, String>(16);
            while (rs.next()) {
                map.put(rs.getString(1), rs.getString(2));
            }
            Databases.closeSQLStuff(rs, stmt);
            rs = null;
            stmt = null;
            if (!map.isEmpty()) {
                stmt = con.prepareStatement("DELETE FROM preview WHERE cid = ? AND createdAt <= ?");
                stmt.setLong(1, contextId);
                stmt.setLong(2, oldestStamp);
                stmt.executeUpdate();

                // Remove from file storage
                final FileStorage fileStorage = getFileStorage(contextId, quotaAware);
                batchDeleteFiles(map.values(), fileStorage);
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
        boolean changed = false;
        try {
            changed = clearFor(contextId, con);
        } finally {
            if (changed) {
                dbService.backWritable(contextId, con);
            } else {
                dbService.backWritableAfterReading(contextId, con);
            }
        }
    }

    private boolean clearFor(final int contextId, final Connection con) throws OXException {
        boolean changed = false;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        boolean rollback = false;
        try {
            Databases.startTransaction(con);
            rollback = true;

            stmt = con.prepareStatement("SELECT id, refId FROM preview WHERE cid=?");
            stmt.setInt(1, contextId);
            rs = stmt.executeQuery();
            final Map<String, String> map = new HashMap<String, String>(16);
            while (rs.next()) {
                map.put(rs.getString(1), rs.getString(2));
            }
            Databases.closeSQLStuff(rs, stmt);
            rs = null;
            stmt = null;

            if (!map.isEmpty()) {
                stmt = con.prepareStatement("DELETE FROM preview WHERE cid=?");
                stmt.setInt(1, contextId);
                stmt.executeUpdate();
                changed = true;

                // Remove from file storage
                final FileStorage fileStorage = getFileStorage(contextId, quotaAware);
                batchDeleteFiles(map.values(), fileStorage);
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
        return changed;
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
                stmt = con.prepareStatement("SELECT refId, fileName, fileType, size FROM preview WHERE cid = ? AND user = ? AND id = ?");
                stmt.setLong(1, contextId);
                stmt.setLong(2, userId);
                stmt.setString(3, id);
            } else {
                // A context-global document
                stmt = con.prepareStatement("SELECT refId, fileName, fileType, size FROM preview WHERE cid = ? AND id = ?");
                stmt.setLong(1, contextId);
                stmt.setString(2, id);
            }
            rs = stmt.executeQuery();
            if (!rs.next()) {
                return null;
            }
            final String refIf = rs.getString(1);
            final FileStorage fileStorage = getFileStorage(contextId, quotaAware);
            return new CachedPreview(fileStorage.getFile(refIf), rs.getString(2), rs.getString(3), rs.getLong(4));
        } catch (final SQLException e) {
            throw PreviewExceptionCodes.ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw PreviewExceptionCodes.ERROR.create(e, e.getMessage());
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

        boolean rollback = false;
        boolean deletePerformed = false;
        boolean transactionInitiated = false;

        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            // Determine entries to remove
            if (userId > 0) {
                stmt = con.prepareStatement("SELECT id, refId FROM preview WHERE cid=? AND user=?");
                int pos = 1;
                stmt.setInt(pos++, contextId);
                stmt.setInt(pos, userId);
            } else {
                stmt = con.prepareStatement("SELECT id, refId FROM preview WHERE cid=?");
                stmt.setInt(1, contextId);
            }
            rs = stmt.executeQuery();
            final Map<String, String> map = new HashMap<String, String>(16);
            while (rs.next()) {
                map.put(rs.getString(1), rs.getString(2));
            }
            Databases.closeSQLStuff(rs, stmt);
            rs = null;
            stmt = null;

            if (!map.isEmpty()) {
                Databases.startTransaction(con);
                rollback = true;
                transactionInitiated = true;

                // Remove from database
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
                deletePerformed = true;
                con.commit();
                rollback = false;

                // Remove from file storage
                final FileStorage fileStorage = getFileStorage(contextId, quotaAware);
                batchDeleteFiles(map.values(), fileStorage);
            }

        } catch (final DataTruncation e) {
            throw PreviewExceptionCodes.ERROR.create(e, e.getMessage());
        } catch (final SQLException e) {
            throw PreviewExceptionCodes.ERROR.create(e, e.getMessage());
        } finally {
            if (rollback) {
                Databases.rollback(con);
            }
            Databases.closeSQLStuff(rs, stmt);
            if (transactionInitiated) {
                Databases.autocommit(con);
            }
            if (deletePerformed) {
                databaseService.backWritable(contextId, con);
            } else {
                databaseService.backWritableAfterReading(contextId, con);
            }
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

        boolean rollback = false;
        boolean deletePerformed = false;
        boolean transactionInitiated = false;

        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            // Determine entries to remove
            stmt = con.prepareStatement("SELECT id, refId FROM preview WHERE cid=? AND user=? AND id LIKE ?");
            int pos = 1;
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos++, userId);
            stmt.setString(pos, id + "%");
            rs = stmt.executeQuery();
            final Map<String, String> map = new HashMap<String, String>(16);
            while (rs.next()) {
                map.put(rs.getString(1), rs.getString(2));
            }
            Databases.closeSQLStuff(rs, stmt);
            rs = null;
            stmt = null;

            if (!map.isEmpty()) {
                Databases.startTransaction(con);
                rollback = true;
                transactionInitiated = true;

                // Remove from database
                stmt = con.prepareStatement("DELETE FROM preview WHERE cid=? AND user=? AND id=?");
                pos = 1;
                stmt.setInt(pos++, contextId);
                stmt.setInt(pos++, userId);
                for (final String currentId : map.keySet()) {
                    stmt.setString(pos, currentId);
                    stmt.addBatch();
                }
                stmt.executeBatch();
                deletePerformed = true;
                con.commit();
                rollback = false;

                // Remove from file storage
                final FileStorage fileStorage = getFileStorage(contextId, quotaAware);
                batchDeleteFiles(map.values(), fileStorage);
            }
        } catch (final DataTruncation e) {
            throw PreviewExceptionCodes.ERROR.create(e, e.getMessage());
        } catch (final SQLException e) {
            throw PreviewExceptionCodes.ERROR.create(e, e.getMessage());
        } finally {
            if (rollback) {
                Databases.rollback(con);
            }
            Databases.closeSQLStuff(rs, stmt);
            if (transactionInitiated) {
                Databases.autocommit(con);
            }
            if (deletePerformed) {
                databaseService.backWritable(contextId, con);
            } else {
                databaseService.backWritableAfterReading(contextId, con);
            }
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
            return null != optRefId(id, userId, contextId, con);
        } finally {
            dbService.backReadOnly(contextId, con);
        }
    }

    private String optRefId(final String id, final int userId, final int contextId) throws OXException {
        if (null == id || contextId <= 0) {
            return null;
        }
        final DatabaseService dbService = ServerServiceRegistry.getInstance().getService(DatabaseService.class);
        if (dbService == null) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(DatabaseService.class.getName());
        }
        final Connection con = dbService.getReadOnly(contextId);
        try {
            return optRefId(id, userId, contextId, con);
        } finally {
            dbService.backReadOnly(contextId, con);
        }
    }

    private String optRefId(final String id, final int userId, final int contextId, final Connection con) throws OXException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            if (userId > 0) {
                // A user-sensitive document
                stmt = con.prepareStatement("SELECT refId FROM preview WHERE cid = ? AND user = ? AND id = ?");
                stmt.setLong(1, contextId);
                stmt.setLong(2, userId);
                stmt.setString(3, id);
            } else {
                // A context-global document
                stmt = con.prepareStatement("SELECT refId FROM preview WHERE cid = ? AND id = ?");
                stmt.setLong(1, contextId);
                stmt.setString(2, id);
            }
            rs = stmt.executeQuery();
            if (!rs.next()) {
                return null;
            }
            final String refId = rs.getString(1);
            final FileStorage fileStorage = getFileStorage(contextId, quotaAware);
            try {
                Streams.close(fileStorage.getFile(refId));
            } catch (final OXException e) {
                if (!FileStorageCodes.FILE_NOT_FOUND.equals(e)) {
                    throw e;
                }
                dropFromTable(id, userId, contextId);
                return null;
            }
            return refId;
        } catch (final SQLException e) {
            throw PreviewExceptionCodes.ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

    private void dropFromTable(final String id, final int userId, final int contextId) {
        final DatabaseService dbService = ServerServiceRegistry.getInstance().getService(DatabaseService.class);
        if (dbService != null) {
            Connection con = null;
            PreparedStatement stmt = null;
            try {
                con = dbService.getWritable(contextId);
                if (userId > 0) {
                    // A user-sensitive document
                    stmt = con.prepareStatement("DELETE FROM preview WHERE cid = ? AND user = ? AND id = ?");
                    stmt.setLong(1, contextId);
                    stmt.setLong(2, userId);
                    stmt.setString(3, id);
                } else {
                    // A context-global document
                    stmt = con.prepareStatement("DELETE FROM preview WHERE cid = ? AND id = ?");
                    stmt.setLong(1, contextId);
                    stmt.setString(2, id);
                }
                stmt.executeUpdate();
            } catch (final Exception e) {
                // Ignore
            } finally {
                Databases.closeSQLStuff(stmt);
                if (null != con) {
                    dbService.backWritable(contextId, con);
                }
            }
        }
    }

}
