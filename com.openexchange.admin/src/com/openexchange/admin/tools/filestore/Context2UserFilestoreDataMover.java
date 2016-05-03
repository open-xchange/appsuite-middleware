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

package com.openexchange.admin.tools.filestore;

import static com.openexchange.filestore.FileStorages.ensureEndingSlash;
import static com.openexchange.filestore.FileStorages.getFullyQualifyingUriForContext;
import static com.openexchange.filestore.FileStorages.getFullyQualifyingUriForUser;
import static com.openexchange.filestore.FileStorages.getQuotaFileStorageService;
import java.io.IOException;
import java.net.URI;
import java.sql.SQLException;
import java.util.Set;
import com.openexchange.admin.daemons.ClientAdminThreadExtended;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Filestore;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.exceptions.ProgrammErrorException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.services.AdminServiceRegistry;
import com.openexchange.admin.storage.interfaces.OXUtilStorageInterface;
import com.openexchange.admin.storage.utils.Filestore2UserUtil;
import com.openexchange.caching.Cache;
import com.openexchange.caching.CacheService;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.FileStorage;
import com.openexchange.filestore.FileStorages;

/**
 * {@link Context2UserFilestoreDataMover} - The implementation to move files from a context to a dedicated user storage.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public class Context2UserFilestoreDataMover extends FilestoreDataMover {

    private final User user;
    private final long maxQuota;

    /**
     * Initializes a new {@link Context2UserFilestoreDataMover}.
     */
    protected Context2UserFilestoreDataMover(Filestore srcFilestore, Filestore dstFilestore, long maxQuota, User user, Context ctx) {
        super(srcFilestore, dstFilestore, ctx);
        this.user = user;
        this.maxQuota = maxQuota;
    }

    /**
     * <ul>
     * <li>Copies the files from source storage to destination storage
     * <li>Propagates new file locations throughout registered FilestoreLocationUpdater instances (if necessary)
     * <li>Deletes source files (if necessary)
     * <li>Applies changes & clears caches
     * </ul>
     *
     * @param srcBaseUri The base URI from source storage
     * @param dstBaseUri The base URI from destination storage
     * @throws StorageException If a storage error occurs
     * @throws InterruptedException If copy operation gets interrupted
     * @throws IOException If an I/O error occurs
     * @throws ProgrammErrorException If a program error occurs
     */
    @Override
    protected void doCopy(URI srcBaseUri, URI dstBaseUri) throws StorageException, IOException, InterruptedException, ProgrammErrorException {
        final int contextId = ctx.getId().intValue();
        final int userId = user.getId().intValue();

        Runnable finalTask = null;
        Reverter reverter = null;
        try {
            // Ensure "filestore_usage" entry exists for user
            OXUtilStorageInterface oxcox = OXUtilStorageInterface.getInstance();
            oxcox.prepareFilestoreUsageFor(user, ctx);

            // Grab associated quota-aware file storages
            FileStorage userStorage = getQuotaFileStorageService().getUnlimitedQuotaFileStorage(dstBaseUri, userId, contextId);
            final FileStorage srcStorage = getQuotaFileStorageService().getUnlimitedQuotaFileStorage(srcBaseUri, -1, contextId);

            // Determine the files to move
            final Set<String> srcFiles = determineFileLocationsFor(userId, contextId);
            if (false == srcFiles.isEmpty()) {
                // Copy each file from source to destination
                URI srcFullUri = ensureEndingSlash(getFullyQualifyingUriForContext(contextId, srcBaseUri));
                URI dstFullUri = ensureEndingSlash(getFullyQualifyingUriForUser(userId, contextId, dstBaseUri));
                final CopyResult copyResult = copyFiles(srcFiles, srcStorage, userStorage, srcFullUri, dstFullUri);
                reverter = copyResult.reverter;

                final Reverter rev = reverter;
                finalTask = new Runnable() {

                    @Override
                    public void run() {
                        if (Operation.COPIED.equals(copyResult.operation)) {
                            // Propagate new file locations throughout registered FilestoreLocationUpdater instances
                            try {
                                propagateNewLocations(copyResult.prevFileName2newFileName);
                            } catch (Exception e) {
                                LOGGER.error("{} failed to propagate new file storage locations. Going to revert copied files (if applicable) and cancel.", Thread.currentThread().getName(), e);
                                if (null != rev) {
                                    try {
                                        rev.revertCopy();
                                    } catch (Exception x) {
                                        LOGGER.error("{} failed to revert copied files", Thread.currentThread().getName(), x);
                                    }
                                }

                                // Abort...
                                return;
                            }

                            try {
                                srcStorage.deleteFiles(srcFiles.toArray(new String[srcFiles.size()]));
                            } catch (OXException e) {
                                LOGGER.error("{} failed to delete copied files", Thread.currentThread().getName(), e);
                            }
                        } else {
                            // Simply adjust filestore_usage entries
                            try {
                                long totalSize = copyResult.totalSize;
                                changeUsage(decUsage(totalSize, 0, contextId), incUsage(totalSize, userId, contextId), contextId);
                            } catch (Exception e) {
                                LOGGER.error("{} failed to update filestore_usage entries. Going to revert copied files (if applicable) and cancel.", Thread.currentThread().getName(), e);
                                if (null != rev) {
                                    try {
                                        rev.revertCopy();
                                    } catch (Exception x) {
                                        LOGGER.error("{} failed to revert copied files", Thread.currentThread().getName(), x);
                                    }
                                }
                            }
                        }
                    }
                };
            }
        } catch (OXException e) {
            throw new StorageException(e);
        } catch (SQLException e) {
            throw new StorageException(e);
        }

        // Apply changes to user & clear caches
        boolean error = true;
        try {
            user.setFilestoreId(dstFilestore.getId());
            user.setFilestore_name(FileStorages.getNameForUser(userId, contextId));
            user.setFilestoreOwner(user.getId());
            user.setMaxQuota(maxQuota);

            OXUtilStorageInterface oxcox = OXUtilStorageInterface.getInstance();
            oxcox.changeFilestoreDataFor(user, ctx);
            Filestore2UserUtil.addFilestore2UserEntry(contextId, user.getId().intValue(), dstFilestore.getId().intValue(), ClientAdminThreadExtended.cache);
            error = false;

            try {
                CacheService cacheService = AdminServiceRegistry.getInstance().getService(CacheService.class);
                Cache cache = cacheService.getCache("Filestore");
                cache.clear();
                Cache qfsCache = cacheService.getCache("QuotaFileStorages");
                qfsCache.invalidateGroup(Integer.toString(contextId));
                Cache userCache = cacheService.getCache("User");
                userCache.remove(cacheService.newCacheKey(contextId, userId));
                Cache contextCache = cacheService.getCache("Context");
                contextCache.remove(ctx.getId());
            } catch (Exception e) {
                LOGGER.error("Failed to invalidate caches. Restart recommended.", e);
            }
        } finally {
            if (error && null != reverter) {
                reverter.revertCopy();
            }
        }

        runSafely(finalTask);

        LOGGER.info("Successfully moved files from context file storage to individual file storage for user {}", user.getId());
    }

}
