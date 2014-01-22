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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import com.openexchange.ajax.requesthandler.cache.AbstractResourceCache;
import com.openexchange.ajax.requesthandler.cache.CachedResource;
import com.openexchange.ajax.requesthandler.cache.ResourceCacheMetadata;
import com.openexchange.ajax.requesthandler.cache.ResourceCacheMetadataStore;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.filestore.FilestoreStorage;
import com.openexchange.java.Streams;
import com.openexchange.preview.PreviewExceptionCodes;
import com.openexchange.tools.file.FileStorage;
import com.openexchange.tools.file.QuotaFileStorage;
import com.openexchange.tools.file.external.FileStorageCodes;

/**
 * {@link FileStoreResourceCacheImpl} - The database-backed preview cache implementation for documents.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class FileStoreResourceCacheImpl extends AbstractResourceCache {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(FileStoreResourceCacheImpl.class);

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
     * Initializes a new {@link FileStoreResourceCacheImpl}.
     */
    public FileStoreResourceCacheImpl(final boolean quotaAware) {
        super();
        this.quotaAware = quotaAware;
    }

    private void batchDeleteFiles(final Collection<String> ids, final FileStorage fileStorage) {
        try {
            fileStorage.deleteFiles(ids.toArray(new String[0]));
        } catch (final Exception e) {
            // Retry one-by-one
            for (final String id : ids) {
                if (null != id) {
                    try {
                        fileStorage.deleteFile(id);
                    } catch (final Exception x) {
                        // Ignore
                    }
                }
            }
        }
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
        final FileStorage fileStorage = getFileStorage(contextId, quotaAware);
        final DatabaseService dbService = getDBService();

        final Connection con = dbService.getWritable(contextId);
        ResourceCacheMetadata existingMetadata = loadExistingEntry(metadataStore, con, contextId, userId, id);
        long existingSize = existingMetadata == null ? 0L : existingMetadata.getSize();
        if (!ensureUnexceededContextQuota(con, bytes.length, contextId, existingSize)) {
            dbService.backWritable(contextId, con);
            return false;
        }

        String refId = null;
        boolean committed = false;
        try {
            Databases.startTransaction(con);
            refId = fileStorage.saveNewFile(Streams.newByteArrayInputStream(bytes));
            ResourceCacheMetadata metadata = new ResourceCacheMetadata();
            metadata.setContextId(contextId);
            metadata.setUserId(userId);
            metadata.setResourceId(id);
            metadata.setFileName(optName);
            metadata.setFileType(optType);
            metadata.setSize(bytes.length);
            metadata.setCreatedAt(System.currentTimeMillis());
            metadata.setRefId(refId);
            if (existingMetadata == null) {
                metadataStore.store(con, metadata);
            } else {
                metadataStore.update(con, metadata);
            }
            con.commit();
            committed = true;
            return true;
        } catch (final DataTruncation e) {
            throw PreviewExceptionCodes.ERROR.create(e, e.getMessage());
        } catch (final SQLException e) {
            throw PreviewExceptionCodes.ERROR.create(e, e.getMessage());
        } finally {
            if (committed) {
                Databases.autocommit(con);
                dbService.backWritable(contextId, con);
                if (existingMetadata != null) {
                    try {
                        if (!fileStorage.deleteFile(existingMetadata.getRefId())) {
                            LOG.warn("Could not remove stored file '{}' after updating cached resource. Consider using 'checkconsistency' to clean up the filestore.", existingMetadata.getRefId());
                        }
                    } catch (OXException e) {
                        LOG.warn("Could not remove stored file '{}' after updating cached resource. Consider using 'checkconsistency' to clean up the filestore.", existingMetadata.getRefId(), e);
                    }
                }
            } else {
                if (con != null) {
                    try {
                        con.rollback();
                    } catch (SQLException e) {
                        LOG.warn("Could not rollback database transaction after failing to cache a resource. Consider using 'checkconsistency' to clean up the database.");
                    }
                    Databases.autocommit(con);
                    dbService.backWritableAfterReading(contextId, con);
                }

                try {
                    if (refId != null && !fileStorage.deleteFile(refId)) {
                        LOG.warn("Could not remove stored file '{}' during transaction rollback. Consider using 'checkconsistency' to clean up the filestore.", refId);
                    }
                } catch (OXException e) {
                    LOG.warn("Could not remove stored file '{}' during transaction rollback. Consider using 'checkconsistency' to clean up the filestore.", refId, e);
                }
            }
        }
    }

    @Override
    public void remove(final int userId, final int contextId) throws OXException {
        ResourceCacheMetadataStore metadataStore = getMetadataStore();
        FileStorage fileStorage = getFileStorage(contextId, quotaAware);
        List<ResourceCacheMetadata> removed = metadataStore.removeAll(contextId, userId);
        List<String> refIds = new ArrayList<String>();
        for (ResourceCacheMetadata metadata : removed) {
            if (metadata.getRefId() != null) {
                refIds.add(metadata.getRefId());
            }
        }
        batchDeleteFiles(refIds, fileStorage);
    }

    @Override
    public void removeAlikes(final String id, final int userId, final int contextId) throws OXException {
        if (null == id) {
            throw PreviewExceptionCodes.ERROR.create("Missing identifier.");
        }

        ResourceCacheMetadataStore metadataStore = getMetadataStore();
        FileStorage fileStorage = getFileStorage(contextId, quotaAware);
        List<ResourceCacheMetadata> removed = metadataStore.removeAll(contextId, userId, id);
        List<String> refIds = new ArrayList<String>();
        for (ResourceCacheMetadata metadata : removed) {
            if (metadata.getRefId() != null) {
                refIds.add(metadata.getRefId());
            }
        }
        batchDeleteFiles(refIds, fileStorage);
    }

    @Override
    public void clearFor(final int contextId) throws OXException {
        remove(-1, contextId);
    }

    @Override
    public CachedResource get(final String id, final int userId, final int contextId) throws OXException {
        if (null == id || contextId <= 0) {
            return null;
        }

        ResourceCacheMetadataStore metadataStore = getMetadataStore();
        FileStorage fileStorage = getFileStorage(contextId, quotaAware);
        ResourceCacheMetadata metadata = metadataStore.load(contextId, userId, id);
        if (metadata == null) {
            return null;
        }

        if (metadata.getRefId() == null) {
            // drop invalid entry
            metadataStore.remove(contextId, userId, id);
            return null;
        }

        InputStream file = fileStorage.getFile(metadata.getRefId());
        return new CachedResource(file, metadata.getFileName(), metadata.getFileType(), metadata.getSize());
    }

    @Override
    public boolean exists(final String id, final int userId, final int contextId) throws OXException {
        if (null == id || contextId <= 0) {
            return false;
        }

        FileStorage fileStorage = getFileStorage(contextId, quotaAware);
        ResourceCacheMetadataStore metadataStore = getMetadataStore();
        ResourceCacheMetadata metadata = metadataStore.load(contextId, userId, id);
        if (metadata == null) {
            return false;
        }

        if (metadata.getRefId() == null) {
            // drop invalid entry
            metadataStore.remove(contextId, userId, id);
            return false;
        }

        try {
            Streams.close(fileStorage.getFile(metadata.getRefId()));
        } catch (final OXException e) {
            if (!FileStorageCodes.FILE_NOT_FOUND.equals(e)) {
                throw e;
            }

            metadataStore.remove(contextId, userId, id);
            return false;
        }

        return true;
    }

    private boolean ensureUnexceededContextQuota(final Connection con, final long desiredSize, final int contextId, final long existingSize) throws OXException {
        final ResourceCacheMetadataStore metadataStore = getMetadataStore();
        final long[] qts = getContextQuota(contextId);
        final long total = qts[0];
        final long totalPerDocument = qts[1];
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
            List<ResourceCacheMetadata> toRemove = new ArrayList<ResourceCacheMetadata>();
            boolean commited = false;
            try {
                Databases.startTransaction(con);
                long usedContextQuota = metadataStore.getUsedSize(con, contextId) - existingSize;
                while (usedContextQuota + desiredSize > total) {
                    ResourceCacheMetadata metadata = metadataStore.removeOldest(con, contextId);
                    if (metadata == null) {
                        return false;
                    }

                    toRemove.add(metadata);
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
                if (commited) {
                    deleteResources(contextId, toRemove);
                } else {
                    Databases.rollback(con);
                }

                Databases.autocommit(con);
            }
        }

        return true;
    }

    private void deleteResources(int contextId, List<ResourceCacheMetadata> toRemove) throws OXException {
        List<String> refIds = new ArrayList<String>(toRemove.size());
        for (ResourceCacheMetadata metadata : toRemove) {
            if (metadata.getRefId() != null) {
                refIds.add(metadata.getRefId());
            }
        }

        if (refIds.size() > 0) {
            batchDeleteFiles(refIds, getFileStorage(contextId, quotaAware));
        }
    }

}
