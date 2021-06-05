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
import static com.openexchange.filestore.FileStorages.getFileStorageService;
import static com.openexchange.filestore.FileStorages.getFullyQualifyingUriForUser;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.sql.SQLException;
import java.util.Set;
import org.apache.commons.io.FileUtils;
import com.openexchange.admin.daemons.ClientAdminThreadExtended;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Filestore;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.exceptions.ProgrammErrorException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.services.AdminServiceRegistry;
import com.openexchange.admin.storage.interfaces.OXUtilStorageInterface;
import com.openexchange.admin.storage.utils.Filestore2UserUtil;
import com.openexchange.admin.tools.ShellExecutor;
import com.openexchange.admin.tools.ShellExecutor.ArrayOutput;
import com.openexchange.caching.Cache;
import com.openexchange.caching.CacheService;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.FileStorage;
import com.openexchange.filestore.FilestoreDataMoveListener;
import com.openexchange.osgi.ServiceListing;

/**
 * {@link UserFilestoreDataMover} - The implementation to move files from one storage to another for a single user.
 * <p>
 * E.g. Move a user's storage from HDD to S3.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public class UserFilestoreDataMover extends FilestoreDataMover {

    private final User user;

    /**
     * Initializes a new {@link UserFilestoreDataMover}.
     */
    protected UserFilestoreDataMover(Filestore srcFilestore, Filestore dstFilestore, User user, Context ctx, ServiceListing<FilestoreDataMoveListener> listeners) {
        super(srcFilestore, dstFilestore, ctx, listeners);
        this.user = user;
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
        // rsync can be used in case both file storages are disk-based storages
        boolean useRsync = "file".equalsIgnoreCase(srcBaseUri.getScheme()) && "file".equalsIgnoreCase(dstBaseUri.getScheme());

        URI srcFullUri;
        URI dstFullUri;

        int contextId = ctx.getId().intValue();
        int userId = user.getId().intValue();
        if (useRsync) {
            // Invoke rsync process
            srcFullUri = getFullyQualifyingUriForUser(userId, contextId, srcBaseUri);
            dstFullUri = getFullyQualifyingUriForUser(userId, contextId, dstBaseUri);

            try {
                for (FilestoreDataMoveListener listener : getListeners()) {
                    listener.onBeforeUserDataMove(contextId, userId, srcFullUri, dstFullUri);
                }
            } catch (OXException e) {
                throw new StorageException(e);
            }

            File fsDirectory = new File(srcFullUri);
            if (fsDirectory.exists()) {
                ArrayOutput output = new ShellExecutor().executeprocargs(new String[] { "rsync", "-a", fsDirectory.getAbsolutePath(), ensureEndingSlash(dstBaseUri.getPath()).toString() });
                if (0 != output.exitstatus) {
                    throw new ProgrammErrorException("Wrong exit status. Exit status was: " + output.exitstatus + " Stderr was: \n" + output.errOutput.toString() + '\n' + "and stdout was: \n" + output.stdOutput.toString());
                }
                FileUtils.deleteDirectory(fsDirectory);
            }
        } else {
            // Not possible to use rsync; e.g. move from HDD to S3
            srcFullUri = ensureEndingSlash(getFullyQualifyingUriForUser(userId, contextId, srcBaseUri));
            dstFullUri = ensureEndingSlash(getFullyQualifyingUriForUser(userId, contextId, dstBaseUri));

            try {
                for (FilestoreDataMoveListener listener : getListeners()) {
                    listener.onBeforeUserDataMove(contextId, userId, srcFullUri, dstFullUri);
                }

                // Grab associated file storages
                FileStorage srcStorage = getFileStorageService().getFileStorage(srcFullUri);
                FileStorage dstStorage = getFileStorageService().getFileStorage(dstFullUri);

                // Copy each file from source to destination
                Set<String> srcFiles = srcStorage.getFileList();
                if (false == srcFiles.isEmpty()) {
                    CopyResult copyResult = copyFiles(srcFiles, srcStorage, dstStorage, srcFullUri, dstFullUri);

                    if (Operation.COPIED.equals(copyResult.operation)) {
                        // Propagate new file locations throughout registered FilestoreLocationUpdater instances
                        propagateNewLocations(copyResult.prevFileName2newFileName);

                        srcStorage.deleteFiles(srcFiles.toArray(new String[srcFiles.size()]));
                    } else {
                        // Nothing to do...
                    }
                }

                if ("file".equalsIgnoreCase(srcBaseUri.getScheme())) {
                    File fsDirectory = new File(srcFullUri);
                    if (fsDirectory.exists()) {
                        FileUtils.deleteDirectory(fsDirectory);
                    }
                }
            } catch (OXException e) {
                throw StorageException.wrapForRMI(e);
            } catch (SQLException e) {
                throw new StorageException(e);
            }
        }

        // Apply changes to context & clear caches
        try {
            user.setFilestoreId(dstFilestore.getId());

            OXUtilStorageInterface oxcox = OXUtilStorageInterface.getInstance();
            Filestore2UserUtil.replaceFilestore2UserEntry(contextId, user.getId().intValue(), dstFilestore.getId().intValue(), ClientAdminThreadExtended.cache);
            oxcox.changeFilestoreDataFor(user, ctx);

            for (FilestoreDataMoveListener listener : getListeners()) {
                listener.onAfterUserDataMoved(contextId, userId, srcFullUri, dstFullUri);
            }

            CacheService cacheService = AdminServiceRegistry.getInstance().getService(CacheService.class);
            Cache cache = cacheService.getCache("Filestore");
            cache.clear();
            Cache userCache = cacheService.getCache("User");
            userCache.remove(cacheService.newCacheKey(contextId, userId));
        } catch (OXException e) {
            throw StorageException.wrapForRMI(e);
        }
    }

}
