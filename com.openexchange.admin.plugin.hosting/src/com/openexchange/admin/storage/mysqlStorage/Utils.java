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

package com.openexchange.admin.storage.mysqlStorage;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import com.openexchange.admin.rmi.dataobjects.Context;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.storage.interfaces.OXUtilStorageInterface;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.FileStorage2EntitiesResolver;
import com.openexchange.filestore.FileStorages;
import com.openexchange.filestore.QuotaFileStorage;

/**
 * {@link Utils}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.3
 */
public class Utils {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Utils.class);

    /**
     * Resolves all file storages for a specific context.
     *
     * @param context The context to remove the file storages for
     * @param failOnError <code>true</code> to fail in case of errors, <code>false</code>, otherwise
     * @throws StorageException If deletion fails and <code>failOnError</code> is <code>true</code>
     */
    static void removeFileStorages(Context context, boolean failOnError) throws StorageException {
        LOG.debug("Starting filestore deletion for context {}...", context.getId());
        /*
         * try to resolve all file storages, the regular way
         */
        Map<URI, OXException> deleteFailures = new HashMap<URI, OXException>();
        List<QuotaFileStorage> storages = null;
        try {
            storages = resolveFileStorages(context.getId().intValue());
        } catch (OXException e) {
            LOG.error("Error resolving file storages for context {}.", context.getId(), e);
            List<URI> uris = OXUtilStorageInterface.getInstance().getUrisforFilestoresUsedBy(context.getId().intValue());
            for (URI uri : uris) {
                deleteFailures.put(uri, e);
            }
        }
        if (null != storages) {
            /*
             * perform regular deletion for resolved storages
             */
            for (QuotaFileStorage storage : storages) {
                try {
                    storage.remove();
                } catch (OXException e) {
                    LOG.error("Error removing file storage '{}' of context {}", storage.getUri(), context.getId(), e);
                    deleteFailures.put(storage.getUri(), e);
                }
            }
        }
        if (0 < deleteFailures.size()) {
            /*
             * fall-back to hard-deletion of "file" file storages as last resort
             */
            for (Map.Entry<URI, OXException> deleteFailure : deleteFailures.entrySet()) {
                if ("file".equalsIgnoreCase(deleteFailure.getKey().getScheme())) {
                    deleteFileStorageDirectory(deleteFailure.getKey(), failOnError);
                } else if (failOnError) {
                    throw new StorageException(deleteFailure.getValue());
                }
            }
        }
    }

    /**
     * Resolves all file storages for a specific context, utilizing the {@link FileStorage2EntitiesResolver}.
     *
     * @param contextId The identifier of the context to resolve the file storages for
     * @return The file storages
     * @throws OXException If storages cannot be resolved
     */
    private static List<QuotaFileStorage> resolveFileStorages(int contextId) throws OXException {
        FileStorage2EntitiesResolver resolver = FileStorages.getFileStorage2EntitiesResolver();
        List<com.openexchange.filestore.FileStorage> fileStorages = resolver.getFileStoragesUsedBy(contextId, true);
        List<QuotaFileStorage> storages = new ArrayList<com.openexchange.filestore.QuotaFileStorage>(fileStorages.size());
        for (com.openexchange.filestore.FileStorage fileStorage : fileStorages) {
            storages.add((QuotaFileStorage) fileStorage);
        }
        return storages;
    }

    /**
     * Tries to delete a file storage directory on the file system.
     *
     * @param uri The URI pointing to the file storage on the file system
     * @param failOnError <code>true</code> to fail in case of errors, <code>false</code>, otherwise
     * @throws StorageException If deletion fails and <code>failOnError</code> is <code>true</code>
     */
    private static void deleteFileStorageDirectory(URI uri, boolean failOnError) throws StorageException {
        LOG.info("Trying to hard-delete the directory for file storage '{}'...", uri);
        try {
            FileUtils.deleteDirectory(new File(uri));
            LOG.info("File storage directory deleted successfully.");
        } catch (IOException e) {
            LOG.error("Error during hard-deletion of '{}'", uri, e);
            if (failOnError) {
                throw new StorageException(e);
            }
        }
    }


    /**
     * Initializes a new {@link Utils}.
     */
    private Utils() {
        super();
    }

}
