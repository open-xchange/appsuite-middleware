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

package com.openexchange.admin.tools.filestore;

import static com.openexchange.filestore.FileStorages.ensureEndingSlash;
import static com.openexchange.filestore.FileStorages.getFullyQualifyingUriForContext;
import static com.openexchange.java.Autoboxing.I;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Set;
import org.apache.commons.io.FileUtils;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Filestore;
import com.openexchange.admin.rmi.exceptions.ProgrammErrorException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.services.AdminServiceRegistry;
import com.openexchange.admin.storage.interfaces.OXUtilStorageInterface;
import com.openexchange.admin.tools.ShellExecutor;
import com.openexchange.admin.tools.ShellExecutor.ArrayOutput;
import com.openexchange.caching.Cache;
import com.openexchange.caching.CacheService;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.FileStorage;
import com.openexchange.filestore.FileStorages;
import com.openexchange.filestore.FilestoreDataMoveListener;
import com.openexchange.osgi.ServiceListing;

/**
 * {@link ContextFilestoreDataMover} - The implementation to move files from one storage to another for a single context.
 * <p>
 * E.g. Move a context's storage from HDD to S3.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public class ContextFilestoreDataMover extends FilestoreDataMover {

    /**
     * Initializes a new {@link ContextFilestoreDataMover}.
     */
    protected ContextFilestoreDataMover(Filestore srcFilestore, Filestore dstFilestore, Context ctx, ServiceListing<FilestoreDataMoveListener> listeners) {
        super(srcFilestore, dstFilestore, ctx, listeners);
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
    protected void doCopy(final URI srcBaseUri, final URI dstBaseUri) throws StorageException, IOException, InterruptedException, ProgrammErrorException {
        final int contextId = ctx.getId().intValue();

        // rsync can be used in case both file storages are disk-based storages
        boolean useRsync = "file".equalsIgnoreCase(srcBaseUri.getScheme()) && "file".equalsIgnoreCase(dstBaseUri.getScheme());

        final URI srcFullUri;
        final URI dstFullUri;

        Runnable finalTask = null;
        Reverter reverter = null;
        if (useRsync) {
            // Invoke rsync process
            srcFullUri = getFullyQualifyingUriForContext(contextId, srcBaseUri);
            dstFullUri = getFullyQualifyingUriForContext(contextId, dstBaseUri);

            try {
                for (FilestoreDataMoveListener listener : getListeners()) {
                    listener.onBeforeContextDataMove(contextId, srcFullUri, dstFullUri);
                }
            } catch (OXException e) {
                throw StorageException.wrapForRMI(e);
            }

            File fsDirectory = new File(srcFullUri);
            if (fsDirectory.exists()) {
                ArrayOutput output = new ShellExecutor().executeprocargs(new String[] { "rsync", "-a", fsDirectory.getAbsolutePath(), ensureEndingSlash(dstBaseUri.getPath()).toString() });
                if (0 != output.exitstatus) {
                    throw new ProgrammErrorException("Wrong exit status. Exit status was: " + output.exitstatus + " Stderr was: \n" + output.errOutput.toString() + '\n' + "and stdout was: \n" + output.stdOutput.toString());
                }
                try {
                    FileUtils.deleteDirectory(fsDirectory);
                } catch (Exception e) {
                    LOGGER.error("{} failed to delete source location {}", Thread.currentThread().getName(), fsDirectory.getAbsolutePath(), e);
                }

                reverter = new Reverter() {

                    @Override
                    public void revertCopy() throws StorageException {
                        File dfsDirectory = new File(dstFullUri);
                        try {
                            ArrayOutput output = new ShellExecutor().executeprocargs(new String[] { "rsync", "-a", dfsDirectory.getAbsolutePath(), ensureEndingSlash(srcBaseUri.getPath()).toString() });
                            if (0 != output.exitstatus) {
                                throw new ProgrammErrorException("Wrong exit status. Exit status was: " + output.exitstatus + " Stderr was: \n" + output.errOutput.toString() + '\n' + "and stdout was: \n" + output.stdOutput.toString());
                            }
                            try {
                                FileUtils.deleteDirectory(dfsDirectory);
                            } catch (Exception e) {
                                LOGGER.error("{} failed to delete destination location {}", Thread.currentThread().getName(), fsDirectory.getAbsolutePath(), e);
                            }
                        } catch (Exception e) {
                            LOGGER.error("{} failed to revert directory {}", Thread.currentThread().getName(), dfsDirectory.getAbsolutePath(), e);
                        }
                    }
                };
            }
        } else {
            // Not possible to use rsync; e.g. move from disk to S3
            srcFullUri = ensureEndingSlash(getFullyQualifyingUriForContext(contextId, srcBaseUri));
            dstFullUri = ensureEndingSlash(getFullyQualifyingUriForContext(contextId, dstBaseUri));

            try {
                for (FilestoreDataMoveListener listener : getListeners()) {
                    listener.onBeforeContextDataMove(contextId, srcFullUri, dstFullUri);
                }

                // Grab associated file storages
                final FileStorage srcStorage = FileStorages.getFileStorageService().getFileStorage(srcFullUri);
                FileStorage dstStorage = FileStorages.getFileStorageService().getFileStorage(dstFullUri);

                // Copy each file from source to destination
                final Set<String> srcFiles = srcStorage.getFileList();
                if (false == srcFiles.isEmpty()) {
                    final CopyResult copyResult = copyFiles(srcFiles, srcStorage, dstStorage, srcFullUri, dstFullUri);
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
                                    // Delete copied files
                                    srcStorage.deleteFiles(srcFiles.toArray(new String[srcFiles.size()]));
                                } catch (Exception e) {
                                    LOGGER.error("{} failed to delete copied files", Thread.currentThread().getName(), e);
                                }
                            } else {
                                // Nothing to do...
                            }

                            // Delete source location
                            if ("file".equalsIgnoreCase(srcBaseUri.getScheme())) {
                                File fsDirectory = new File(srcFullUri);
                                if (fsDirectory.exists()) {
                                    try {
                                        FileUtils.deleteDirectory(fsDirectory);
                                    } catch (Exception e) {
                                        LOGGER.error("{} failed to delete source location {}", Thread.currentThread().getName(), fsDirectory.getAbsolutePath(), e);
                                    }
                                }
                            }
                        }
                    };
                } else {
                    finalTask = new Runnable() {
                        @Override
                        public void run() {
                            if ("file".equalsIgnoreCase(srcBaseUri.getScheme())) {
                                File fsDirectory = new File(srcFullUri);
                                if (fsDirectory.exists()) {
                                    try {
                                        FileUtils.deleteDirectory(fsDirectory);
                                    } catch (Exception e) {
                                        LOGGER.error("{} failed to delete source location {}", Thread.currentThread().getName(), fsDirectory.getAbsolutePath(), e);
                                    }
                                }
                            }
                        }
                    };
                }
            } catch (OXException e) {
                throw StorageException.wrapForRMI(e);
            }
        }

        // Apply changes to context & clear caches
        boolean error = true;
        try {
            ctx.setFilestoreId(dstFilestore.getId());

            OXUtilStorageInterface oxcox = OXUtilStorageInterface.getInstance();
            oxcox.changeFilestoreDataFor(ctx);
            error = false;

            for (FilestoreDataMoveListener listener : getListeners()) {
                listener.onAfterContextDataMoved(contextId, srcFullUri, dstFullUri);
            }

            try {
                CacheService cacheService = AdminServiceRegistry.getInstance().getService(CacheService.class);
                Cache cache = cacheService.getCache("Filestore");
                cache.clear();
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

        LOGGER.info("Successfully moved files of context {}", I(contextId));
    }

}
