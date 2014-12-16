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

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import com.openexchange.admin.osgi.FilestoreLocationUpdaterRegistry;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.dataobjects.Filestore;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.exceptions.ProgrammErrorException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.services.AdminServiceRegistry;
import com.openexchange.admin.storage.interfaces.OXUtilStorageInterface;
import com.openexchange.caching.Cache;
import com.openexchange.caching.CacheService;
import com.openexchange.databaseold.Database;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.FileStorage;
import com.openexchange.filestore.FileStorages;
import com.openexchange.groupware.filestore.FileLocationHandler;

/**
 * {@link Context2UserFilestoreDataMover} - The implementation to move files from a context to a dedicated user storage.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public class Context2UserFilestoreDataMover extends FilestoreDataMover {

    private final User user;

    /**
     * Initializes a new {@link Context2UserFilestoreDataMover}.
     */
    protected Context2UserFilestoreDataMover(Filestore srcFilestore, Filestore dstFilestore, User user, Context ctx) {
        super(srcFilestore, dstFilestore, ctx);
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
        try {
            // Grab associated quota-aware file storages
            FileStorage userStorage = FileStorages.getQuotaFileStorageService().getQuotaFileStorage(user.getId().intValue(), ctx.getId().intValue());
            FileStorage srcStorage = FileStorages.getQuotaFileStorageService().getQuotaFileStorage(ctx.getId().intValue());

            // Determine the files to move
            Set<String> srcFiles = new LinkedHashSet<String>();
            {
                Connection con = Database.getNoTimeout(ctx.getId().intValue(), true);
                try {
                    for (FileLocationHandler updater : FilestoreLocationUpdaterRegistry.getInstance().getServices()) {
                        srcFiles.addAll(updater.determineFileLocationsFor(user.getId().intValue(), ctx.getId().intValue(), con));
                    }
                } finally {
                    Database.backNoTimeout(ctx.getId().intValue(), true, con);
                }
            }

            Map<String, String> prevFileName2newFileName = new HashMap<String, String>(srcFiles.size());
            for (String file : srcFiles) {
                InputStream is = srcStorage.getFile(file);
                String newFile = userStorage.saveNewFile(is);
                if (null != newFile) {
                    prevFileName2newFileName.put(file, newFile);
                    LOGGER.info("Copied file " + file + " to " + newFile);
                }
            }

            // Propagate new file locations throughout registered FilestoreLocationUpdater instances
            propagateNewLocations(prevFileName2newFileName);

            srcStorage.deleteFiles(srcFiles.toArray(new String[srcFiles.size()]));
        } catch (OXException e) {
            throw new StorageException(e);
        } catch (SQLException e) {
            throw new StorageException(e);
        }

        // Apply changes to context & clear caches
        try {
            user.setFilestoreId(dstFilestore.getId());
            user.setFilestore_name(FileStorages.getNameForUser(user.getId().intValue(), ctx.getId().intValue()));
            user.setFilestoreOwner(user.getId());

            OXUtilStorageInterface oxcox = OXUtilStorageInterface.getInstance();
            oxcox.changeFilestoreDataFor(user, ctx);

            CacheService cacheService = AdminServiceRegistry.getInstance().getService(CacheService.class);
            Cache cache = cacheService.getCache("Filestore");
            cache.clear();
            Cache userCache = cacheService.getCache("User");
            userCache.remove(cacheService.newCacheKey(ctx.getId().intValue(), user.getId().intValue()));
        } catch (OXException e) {
            throw new StorageException(e);
        }
    }

}
