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

package com.openexchange.ajax.requesthandler.converters.preview.cache;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DataTruncation;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.openexchange.ajax.requesthandler.cache.AbstractResourceCache;
import com.openexchange.ajax.requesthandler.cache.CachedResource;
import com.openexchange.ajax.requesthandler.cache.ResourceCacheMetadata;
import com.openexchange.ajax.requesthandler.cache.ResourceCacheMetadataStore;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.java.Streams;
import com.openexchange.preview.PreviewExceptionCodes;
import com.openexchange.server.ServiceLookup;

/**
 * {@link RdbResourceCacheImpl} - The database-backed preview cache implementation for documents.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class RdbResourceCacheImpl extends AbstractResourceCache {

    /**
     * Initializes a new {@link RdbResourceCacheImpl}.
     */
    public RdbResourceCacheImpl(final ServiceLookup serviceLookup) {
        super(serviceLookup);
    }

    @Override
    public boolean save(final String id, final CachedResource resource, final int userId, final int contextId) throws OXException {
        final InputStream in = resource.getInputStream();
        if (null == in) {
            return save(id, resource.getBytes(), resource.getFileName(), resource.getFileType(), userId, contextId);
        }
        return save(id, in, resource.getFileName(), resource.getFileType(), userId, contextId);
    }

    private boolean save(final String id, final InputStream in, final String optName, final String optType, final int userId, final int contextId) throws OXException {
        try {
            return save(id, Streams.stream2bytes(in), optName, optType, userId, contextId);
        } catch (final IOException e) {
            throw PreviewExceptionCodes.IO_ERROR.create(e, e.getMessage());
        }
    }

    private boolean save(final String id, final byte[] bytes, final String optName, final String optType, final int userId, final int contextId) throws OXException {
        final ResourceCacheMetadataStore metadataStore = getMetadataStore();
        final DatabaseService dbService = getDBService();

        final Connection con = dbService.getWritable(contextId);
        ResourceCacheMetadata existingMetadata = loadExistingEntry(metadataStore, con, contextId, userId, id);
        long existingSize = existingMetadata == null ? 0L : existingMetadata.getSize();
        if (!ensureUnexceededContextQuota(con, bytes.length, userId, contextId, existingSize)) {
            dbService.backWritable(contextId, con);
            return false;
        }

        boolean committed = false;
        PreparedStatement stmt = null;
        try {
            Databases.startTransaction(con);
            ResourceCacheMetadata metadata = new ResourceCacheMetadata();
            metadata.setContextId(contextId);
            metadata.setUserId(userId);
            metadata.setResourceId(id);
            metadata.setFileName(optName);
            metadata.setFileType(prepareFileName(optName));
            metadata.setSize(bytes.length);
            metadata.setCreatedAt(System.currentTimeMillis());
            if (existingMetadata == null) {
                metadataStore.store(con, metadata);
                int pos = 1;
                stmt = con.prepareStatement("INSERT INTO previewData (cid, user, id, data) VALUES (?, ?, ?, ?)");
                stmt.setLong(pos++, contextId);
                stmt.setLong(pos++, userId);
                stmt.setString(pos++, id);
                stmt.setBinaryStream(pos++, Streams.newByteArrayInputStream(bytes));
                stmt.executeUpdate();
            } else {
                metadataStore.update(con, metadata);
                int pos = 1;
                stmt = con.prepareStatement("UPDATE previewData SET data = ? WHERE cid = ? AND user = ? AND id = ?");
                stmt.setBinaryStream(pos++, Streams.newByteArrayInputStream(bytes));
                stmt.setLong(pos++, contextId);
                stmt.setLong(pos++, userId);
                stmt.setString(pos++, id);
                stmt.executeUpdate();
            }

            con.commit();
            committed = true;
            return true;
        } catch (final DataTruncation e) {
            throw PreviewExceptionCodes.ERROR.create(e, e.getMessage());
        } catch (java.sql.SQLIntegrityConstraintViolationException e) {
            // Duplicate key conflict; just leave
            return false;
        } catch (final SQLException e) {
            // duplicate key conflict
            if (e.getErrorCode() == 1022) {
                return false;
            }
            throw PreviewExceptionCodes.ERROR.create(e, e.getMessage());
        } finally {
            if (!committed) {
                Databases.rollback(con);
            }
            Databases.closeSQLStuff(stmt);
            Databases.autocommit(con);
            dbService.backWritable(contextId, con);
        }
    }

    private boolean ensureUnexceededContextQuota(Connection con, long desiredSize, int userId, int contextId, long existingSize) throws OXException {
        final ResourceCacheMetadataStore metadataStore = getMetadataStore();
        final long total = getGlobalQuota(userId, contextId);
        final long totalPerDocument = getDocumentQuota(userId, contextId);
        if (total > 0L || totalPerDocument > 0L) {
            if (total <= 0L) {
                return (totalPerDocument <= 0 || desiredSize <= totalPerDocument);
            }

            // Check if document's size fits into quota limits at all
            if (desiredSize > total || desiredSize > totalPerDocument) {
                return false;
            }

            // Try to create space through removing oldest entries
            // until enough space is available
            boolean commited = false;
            try {
                Databases.startTransaction(con);
                long usedContextQuota = metadataStore.getUsedSize(con, contextId) - existingSize;
                while (usedContextQuota + desiredSize > total) {
                    ResourceCacheMetadata metadata = metadataStore.removeOldest(con, contextId);
                    if (metadata == null) {
                        return false;
                    }

                    PreparedStatement stmt = null;
                    try {
                        stmt = con.prepareStatement("DELETE FROM previewData WHERE cid=? AND user=? AND id=?");
                        stmt.setInt(1, contextId);
                        stmt.setInt(2, metadata.getUserId());
                        stmt.setString(3, metadata.getResourceId());
                        stmt.executeUpdate();
                    } finally {
                        Databases.closeSQLStuff(stmt);
                    }

                    usedContextQuota = metadataStore.getUsedSize(con, contextId) - existingSize;
                    if (usedContextQuota <= 0 && desiredSize > total) {
                        return false;
                    }
                }
                con.commit();
                commited = true;
            } catch (SQLException e) {
                throw PreviewExceptionCodes.ERROR.create(e, e.getMessage());
            } finally {
                if (!commited) {
                    Databases.rollback(con);
                }

                Databases.autocommit(con);
            }
        }

        return true;
    }

    @Override
    public CachedResource get(final String id, final int userId, final int contextId) throws OXException {
        if (null == id || contextId <= 0) {
            return null;
        }

        final DatabaseService dbService = getDBService();
        final ResourceCacheMetadataStore metadataStore = getMetadataStore();
        final Connection con = dbService.getReadOnly(contextId);
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            ResourceCacheMetadata metadata = metadataStore.load(con, contextId, userId, id);
            if (metadata == null) {
                return null;
            }

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

            return new CachedResource(Streams.stream2bytes(rs.getBinaryStream(1)), metadata.getFileName(), metadata.getFileType(), metadata.getSize());
        } catch (final SQLException e) {
            throw PreviewExceptionCodes.ERROR.create(e, e.getMessage());
        } catch (final IOException e) {
            throw PreviewExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
            dbService.backReadOnly(contextId, con);
        }
    }

    @Override
    public void remove(final int userId, final int contextId) throws OXException {
        final ResourceCacheMetadataStore metadataStore = getMetadataStore();
        final DatabaseService dbService = getDBService();
        final Connection con = dbService.getWritable(contextId);

        PreparedStatement stmt = null;
        boolean committed = false;
        try {
            Databases.startTransaction(con);
            List<ResourceCacheMetadata> removed = metadataStore.removeAll(con, contextId, -1);
            if (!removed.isEmpty()) {
                stmt = con.prepareStatement("DELETE FROM previewData WHERE cid=? AND user=? AND id=?");
                for (ResourceCacheMetadata metadata : removed) {
                    stmt.setInt(1, contextId);
                    stmt.setInt(2, userId);
                    stmt.setString(3, metadata.getResourceId());
                    stmt.addBatch();
                }

                stmt.executeBatch();
            }

            con.commit();
            committed = true;
        } catch (SQLException e) {
            throw PreviewExceptionCodes.ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(stmt);
            if (!committed) {
                Databases.rollback(con);
            }

            Databases.autocommit(con);
            dbService.backWritable(contextId, con);
        }
    }

    @Override
    public void removeAlikes(final String id, final int userId, final int contextId) throws OXException {
        if (null == id) {
            throw PreviewExceptionCodes.ERROR.create("Missing identifier.");
        }

        final ResourceCacheMetadataStore metadataStore = getMetadataStore();
        final DatabaseService dbService = getDBService();
        final Connection con = dbService.getWritable(contextId);
        boolean committed = false;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            Databases.startTransaction(con);
            List<ResourceCacheMetadata> removed = metadataStore.removeAll(con, contextId, userId, id);
            if (!removed.isEmpty()) {
                final Set<String> ids = new HashSet<String>(16);
                for (ResourceCacheMetadata metadata : removed) {
                    ids.add(metadata.getResourceId());
                }

                stmt = con.prepareStatement("DELETE FROM previewData WHERE cid=? AND user=? AND id=?");
                int pos = 1;
                stmt.setInt(pos++, contextId);
                stmt.setInt(pos++, userId);
                for (final String ide : ids) {
                    stmt.setString(pos, ide);
                    stmt.addBatch();
                }
                stmt.executeBatch();
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
            dbService.backWritable(contextId, con);
        }
    }

    @Override
    public void clearFor(final int contextId) throws OXException {
        final ResourceCacheMetadataStore metadataStore = getMetadataStore();
        final DatabaseService dbService = getDBService();
        final Connection con = dbService.getWritable(contextId);

        PreparedStatement stmt = null;
        boolean committed = false;
        try {
            Databases.startTransaction(con);
            List<ResourceCacheMetadata> removed = metadataStore.removeAll(con, contextId, -1);
            if (!removed.isEmpty()) {
                stmt = con.prepareStatement("DELETE FROM previewData WHERE cid=? AND id=?");
                for (ResourceCacheMetadata metadata : removed) {
                    stmt.setInt(1, contextId);
                    stmt.setString(2, metadata.getResourceId());
                    stmt.addBatch();
                }

                stmt.executeBatch();
            }

            con.commit();
            committed = true;
        } catch (SQLException e) {
            throw PreviewExceptionCodes.ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(stmt);
            if (!committed) {
                Databases.rollback(con);
            }

            Databases.autocommit(con);
            dbService.backWritable(contextId, con);
        }
    }

    @Override
    public boolean exists(final String id, final int userId, final int contextId) throws OXException {
        if (null == id || contextId <= 0) {
            return false;
        }

        ResourceCacheMetadataStore metadataStore = getMetadataStore();
        ResourceCacheMetadata metadata = metadataStore.load(contextId, userId, id);
        if (metadata == null) {
            return false;
        }

        return true;
    }

}
