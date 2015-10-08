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

import static com.openexchange.ajax.requesthandler.cache.ResourceCacheProperties.QUOTA_AWARE;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.sql.Connection;
import java.sql.DataTruncation;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.RejectedExecutionException;
import com.openexchange.ajax.requesthandler.cache.AbstractResourceCache;
import com.openexchange.ajax.requesthandler.cache.CachedResource;
import com.openexchange.ajax.requesthandler.cache.ResourceCacheMetadata;
import com.openexchange.ajax.requesthandler.cache.ResourceCacheMetadataStore;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.FileStorageCodes;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.filestore.FilestoreStorage;
import com.openexchange.java.Streams;
import com.openexchange.preview.PreviewExceptionCodes;
import com.openexchange.server.ServiceLookup;
import com.openexchange.timer.TimerService;
import com.openexchange.tools.file.FileStorage;
import com.openexchange.tools.file.QuotaFileStorage;

/**
 * {@link FileStoreResourceCacheImpl} - The filestore-backed preview cache implementation for documents.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class FileStoreResourceCacheImpl extends AbstractResourceCache {

    protected static long ALIGNMENT_DELAY = 10000L;

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(FileStoreResourceCacheImpl.class);

    private static FileStorage getFileStorage(final Context ctx, final boolean quotaAware) throws OXException {
        final URI uri = FilestoreStorage.createURI(ctx);
        return quotaAware ? QuotaFileStorage.getInstance(uri, ctx) : FileStorage.getInstance(uri);
    }

    private static FileStorage getFileStorage(final int contextId, final boolean quotaAware) throws OXException {
        return getFileStorage(ContextStorage.getStorageContext(contextId), quotaAware);
    }

    // ------------------------------------------------------------------------------- //

    private final boolean quotaAware;

    /**
     * Initializes a new {@link FileStoreResourceCacheImpl}.
     * @throws OXException
     */
    public FileStoreResourceCacheImpl(final ServiceLookup serviceLookup) throws OXException {
        super(serviceLookup);
        quotaAware = getConfigurationService().getBoolProperty(QUOTA_AWARE, false);
    }

    private void batchDeleteFiles(final Collection<String> ids, final FileStorage fileStorage) {
        try {
            Set<String> notDeleted = fileStorage.deleteFiles(ids.toArray(new String[0]));
            if (!notDeleted.isEmpty()) {
                LOG.warn("Some cached files could not be deleted from filestore. Consider using 'checkconsistency' to clean up manually.");
            }
        } catch (final Exception e) {
            LOG.warn("Error while deleting a batch of preview files. Trying one-by-one now...", e);
            // Retry one-by-one
            for (final String id : ids) {
                if (null != id) {
                    try {
                        fileStorage.deleteFile(id);
                    } catch (final Exception x) {
                        LOG.warn("Could not remove preview file '{}'. Consider using 'checkconsistency' to clean up the filestore.", id);
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

    private static final Object SCHEDULED = new Object();
    private static final Object RUNNING = new Object();
    private final ConcurrentMap<Integer, Object> alignmentRequests = new ConcurrentHashMap<Integer, Object>();

    private boolean save(final String id, final byte[] bytes, final String optName, final String optType, final int userId, final int contextId) throws OXException {
        LOG.debug("Trying to cache resource {}.", id);
        final ResourceCacheMetadataStore metadataStore = getMetadataStore();
        final FileStorage fileStorage = getFileStorage(contextId, quotaAware);
        final DatabaseService dbService = getDBService();
        if (fitsQuotas(bytes.length)) {
            /*
             * If the resource fits the quotas we store it even if we exceed the quota when storing it.
             * Removing old cache entries is done asynchronously in the finally block.
             */
            final String refId = fileStorage.saveNewFile(Streams.newByteArrayInputStream(bytes));
            final ResourceCacheMetadata newMetadata = new ResourceCacheMetadata();
            newMetadata.setContextId(contextId);
            newMetadata.setUserId(userId);
            newMetadata.setResourceId(id);
            newMetadata.setFileName(prepareFileName(optName));
            newMetadata.setFileType(prepareFileType(optType));
            newMetadata.setSize(bytes.length);
            newMetadata.setCreatedAt(System.currentTimeMillis());
            newMetadata.setRefId(refId);

            final Connection con = dbService.getWritable(contextId);
            ResourceCacheMetadata existingMetadata = null;
            boolean committed = false;
            boolean triggerAlignment = false;
            long start = System.currentTimeMillis();
            try {
                /*
                 * We have to deal with high concurrency here. Selecting an entry with FOR UPDATE leads to
                 * a gap lock and causes deadlocks between insertion requests. Therefore we use a double-check
                 * idiom here. Only if an entry exists we lock it with 'FOR UPDATE'. Updates on existing entries
                 * should happen rarely and are mostly performed asynchronous so performance should not be a
                 * big problem.
                 */
                Databases.startTransaction(con);
                if (entryExists(metadataStore, con, contextId, userId, id)) {
                    existingMetadata = loadExistingEntryForUpdate(metadataStore, con, contextId, userId, id);
                    if (existingMetadata == null) {
                        metadataStore.store(con, newMetadata);
                    } else {
                        metadataStore.update(con, newMetadata);
                    }
                } else {
                    metadataStore.store(con, newMetadata);
                }

                long globalQuota = getGlobalQuota();
                if (globalQuota > 0) {
                    long usedSize = metadataStore.getUsedSize(con, contextId);
                    if (usedSize > globalQuota) {
                        triggerAlignment = true;
                    }
                }

                con.commit();
                committed = true;
                return true;
            } catch (DataTruncation e) {
                throw PreviewExceptionCodes.ERROR.create(e, e.getMessage());
            } catch (java.sql.SQLIntegrityConstraintViolationException e) {
                // Duplicate key conflict; just leave
                long transactionDuration = System.currentTimeMillis() - start;
                LOG.warn("Caching a resource failed due to a duplicate key conflict, this should happen very rarely otherwise this may indicate a performance problem."
                    + " The transaction lasted {}ms. Original message: {}.", transactionDuration, e.getMessage());
            } catch (SQLException e) {
                // duplicate key conflict
                if (e.getErrorCode() == 1022) {
                    long transactionDuration = System.currentTimeMillis() - start;
                    LOG.warn("Caching a resource failed due to a duplicate key conflict, this should happen very rarely otherwise this may indicate a performance problem."
                        + " The transaction lasted {}ms. Original message: {}.", transactionDuration, e.getMessage());
                } else {
                    throw PreviewExceptionCodes.ERROR.create(e, e.getMessage());
                }
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

                    // Storing the resource exceeded the quota. We schedule an alignment task if this wasn't already done.
                    if (triggerAlignment && alignmentRequests.putIfAbsent(contextId, SCHEDULED) == null && scheduleAlignmentTask(contextId)) {
                        LOG.debug("Scheduling alignment task for context {}.", contextId);
                    } else {
                        LOG.debug("Skipping scheduling of alignment task for context {}.", contextId);
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

        return false;
    }

    protected boolean scheduleAlignmentTask(final int contextId) {
        try {
            TimerService timerService = optTimerService();
            if (timerService != null) {
                timerService.schedule(new Runnable() {
                    @Override
                    public void run() {
                        alignToQuota(contextId);
                    }
                }, ALIGNMENT_DELAY);

                return true;
            }
        } catch (RejectedExecutionException e) {
            alignmentRequests.remove(contextId);
            LOG.warn("Could not schedule alignment task for context {}.", contextId, e);
        }

        return false;
    }

    protected void alignToQuota(final int contextId) {
        if (!alignmentRequests.replace(contextId, SCHEDULED, RUNNING)) {
            return;
        }

        try {
            final ResourceCacheMetadataStore metadataStore = getMetadataStore();
            final DatabaseService dbService = getDBService();
            final Connection con = dbService.getWritable(contextId);
            final Set<String> refIds = new HashSet<String>();
            boolean transactionStarted = false;
            try {
                long globalQuota = getGlobalQuota();
                long usedContextQuota = metadataStore.getUsedSize(con, contextId);
                if (globalQuota > 0 && usedContextQuota > globalQuota) {
                    long neededSpace = usedContextQuota - globalQuota;
                    long collected = 0L;

                    Databases.startTransaction(con);
                    transactionStarted = true;
                    List<ResourceCacheMetadata> entries = metadataStore.loadForCleanUp(con, contextId);
                    Iterator<ResourceCacheMetadata> it = entries.iterator();
                    while (collected < neededSpace && it.hasNext()) {
                        ResourceCacheMetadata metadata = it.next();
                        String refId = metadata.getRefId();
                        if (refId != null) {
                            refIds.add(refId);
                        }
                        collected += (metadata.getSize() > 0 ? metadata.getSize() : 0);
                    }

                    if (!refIds.isEmpty()) {
                        metadataStore.removeByRefIds(con, contextId, refIds);
                    }
                    con.commit();
                }
            } catch (SQLException s) {
                if (transactionStarted) {
                    Databases.rollback(con);
                }
                LOG.error("Could not align preview cache for context {} to quota.", contextId, s);
            } finally {
                alignmentRequests.remove(contextId);
                if (transactionStarted) {
                    Databases.autocommit(con);
                }

                if (refIds.isEmpty()) {
                    dbService.backWritableAfterReading(contextId, con);
                } else {
                    dbService.backWritable(contextId, con);
                }
            }

            if (refIds.isEmpty()) {
                LOG.debug("No need to align preview cache for context {} to quota.", contextId);
            } else {
                LOG.debug("Aligning preview cache for context {} to quota.", contextId);
                batchDeleteFiles(refIds, getFileStorage(contextId, quotaAware));
            }
        } catch (Exception e) {
            LOG.error("Could not align preview cache for context {} to quota.", contextId, e);
        }
    }

    @Override
    public void remove(final int userId, final int contextId) throws OXException {
        remove0(userId, contextId);
    }

    @Override
    public void clearFor(final int contextId) throws OXException {
        remove0(-1, contextId);
    }

    private void remove0(int userId, int contextId) throws OXException {
        long start = System.currentTimeMillis();
        ResourceCacheMetadataStore metadataStore = getMetadataStore();
        List<ResourceCacheMetadata> removed = metadataStore.removeAll(contextId, userId);
        List<String> refIds = new ArrayList<String>();
        for (ResourceCacheMetadata metadata : removed) {
            if (metadata.getRefId() != null) {
                refIds.add(metadata.getRefId());
            }
        }
        FileStorage fileStorage = getFileStorage(contextId, quotaAware);
        batchDeleteFiles(refIds, fileStorage);
        LOG.info("Cleared resource cache for user {} in context {} in {}ms.", userId, contextId, System.currentTimeMillis() - start);
    }

    @Override
    public void removeAlikes(final String id, final int userId, final int contextId) throws OXException {
        if (null == id) {
            throw PreviewExceptionCodes.ERROR.create("Missing identifier.");
        }

        ResourceCacheMetadataStore metadataStore = getMetadataStore();
        List<ResourceCacheMetadata> removed = metadataStore.removeAll(contextId, userId, id);
        List<String> refIds = new ArrayList<String>();
        for (ResourceCacheMetadata metadata : removed) {
            if (metadata.getRefId() != null) {
                refIds.add(metadata.getRefId());
            }
        }
        FileStorage fileStorage = getFileStorage(contextId, quotaAware);
        batchDeleteFiles(refIds, fileStorage);
    }

    @Override
    public CachedResource get(final String id, final int userId, final int contextId) throws OXException {
        if (null == id || contextId <= 0) {
            return null;
        }

        ResourceCacheMetadataStore metadataStore = getMetadataStore();
        ResourceCacheMetadata metadata = metadataStore.load(contextId, userId, id);
        if (metadata == null) {
            return null;
        }

        if (metadata.getRefId() == null) {
            // drop invalid entry
            metadataStore.remove(contextId, userId, id);
            return null;
        }

        FileStorage fileStorage = getFileStorage(contextId, quotaAware);
        InputStream file = fileStorage.getFile(metadata.getRefId());
        return new CachedResource(file, metadata.getFileName(), metadata.getFileType(), metadata.getSize());
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

        if (metadata.getRefId() == null) {
            // drop invalid entry
            metadataStore.remove(contextId, userId, id);
            return false;
        }

        FileStorage fileStorage = getFileStorage(contextId, quotaAware);
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

    private boolean fitsQuotas(final long desiredSize) {
        final long globalQuota = getGlobalQuota();
        final long documentQuota = getDocumentQuota();
        if (globalQuota > 0L || documentQuota > 0L) {
            if (globalQuota <= 0L) {
                return (documentQuota <= 0 || desiredSize <= documentQuota);
            }

            // Check if document's size fits into quota limits at all
            if (desiredSize > globalQuota || desiredSize > documentQuota) {
                return false;
            }
        }

        return true;
    }

}
