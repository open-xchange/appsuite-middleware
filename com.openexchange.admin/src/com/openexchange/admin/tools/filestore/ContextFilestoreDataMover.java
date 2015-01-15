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

package com.openexchange.admin.tools.filestore;

import static com.openexchange.filestore.FileStorages.ensureEndingSlash;
import static com.openexchange.filestore.FileStorages.getFullyQualifyingUriForContext;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.sql.SQLException;
import java.util.Map;
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
    protected ContextFilestoreDataMover(Filestore srcFilestore, Filestore dstFilestore, Context ctx) {
        super(srcFilestore, dstFilestore, ctx);
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
        int contextId = ctx.getId().intValue();

        // rsync can be used in case both file storages are disk-based storages
        boolean useRsync = "file".equalsIgnoreCase(srcBaseUri.getScheme()) && "file".equalsIgnoreCase(dstBaseUri.getScheme());

        if (useRsync) {
            // Invoke rsync process
            URI srcFullUri = getFullyQualifyingUriForContext(contextId, srcBaseUri);
            File fsDirectory = new File(srcFullUri);
            if (fsDirectory.exists()) {
                ArrayOutput output = new ShellExecutor().executeprocargs(new String[] { "rsync", "-a", srcFullUri.toString(), ensureEndingSlash(dstBaseUri).toString() });
                if (0 != output.exitstatus) {
                    throw new ProgrammErrorException("Wrong exit status. Exit status was: " + output.exitstatus + " Stderr was: \n" + output.errOutput.toString() + '\n' + "and stdout was: \n" + output.stdOutput.toString());
                }
                FileUtils.deleteDirectory(fsDirectory);
            }
        } else {
            // Not possible to use rsync; e.g. move from disk to S3
            URI srcFullUri = ensureEndingSlash(getFullyQualifyingUriForContext(contextId, srcBaseUri));
            URI dstFullUri = ensureEndingSlash(getFullyQualifyingUriForContext(contextId, dstBaseUri));

            try {
                // Grab associated file storages
                FileStorage srcStorage = FileStorages.getFileStorageService().getFileStorage(srcFullUri);
                FileStorage dstStorage = FileStorages.getFileStorageService().getFileStorage(dstFullUri);

                // Copy each file from source to destination
                Set<String> srcFiles = srcStorage.getFileList();
                Map<String, String> prevFileName2newFileName = copyFiles(srcFiles, srcStorage, dstStorage);

                // Propagate new file locations throughout registered FilestoreLocationUpdater instances
                propagateNewLocations(prevFileName2newFileName);

                srcStorage.deleteFiles(srcFiles.toArray(new String[srcFiles.size()]));
            } catch (OXException e) {
                throw new StorageException(e);
            } catch (SQLException e) {
                throw new StorageException(e);
            }
        }

        // Apply changes to context & clear caches
        try {
            ctx.setFilestoreId(dstFilestore.getId());

            OXUtilStorageInterface oxcox = OXUtilStorageInterface.getInstance();
            oxcox.changeFilestoreDataFor(ctx);

            CacheService cacheService = AdminServiceRegistry.getInstance().getService(CacheService.class);
            Cache cache = cacheService.getCache("Filestore");
            cache.clear();
            Cache qfsCache = cacheService.getCache("QuotaFileStorages");
            qfsCache.invalidateGroup(Integer.toString(contextId));
            Cache contextCache = cacheService.getCache("Context");
            contextCache.remove(ctx.getId());
        } catch (OXException e) {
            throw new StorageException(e);
        }
    }

}
