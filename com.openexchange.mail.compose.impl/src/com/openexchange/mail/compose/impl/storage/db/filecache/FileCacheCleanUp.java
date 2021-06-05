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

package com.openexchange.mail.compose.impl.storage.db.filecache;

import static com.openexchange.mail.compose.impl.storage.db.filecache.FileCache.FILE_NAME_PREFIX;
import java.io.File;
import java.io.FileFilter;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import com.openexchange.exception.OXException;
import com.openexchange.java.util.UUIDs;
import com.openexchange.mail.compose.impl.storage.db.CompositionSpaceDbStorage;
import com.openexchange.mail.compose.impl.storage.db.RdbCompositionSpaceStorageService;
import com.openexchange.server.ServiceLookup;
import com.openexchange.uploaddir.UploadDirService;
import com.openexchange.groupware.contexts.impl.ContextExceptionCodes;

/**
 * {@link FileCacheCleanUp} - Clean-up for file cache.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.5
 */
public class FileCacheCleanUp {

    /** Simple class to delay initialization until needed */
    private static class LoggerHolder {
        static final Logger LOG = org.slf4j.LoggerFactory.getLogger(FileCacheCleanUp.class);
    }

    private final RdbCompositionSpaceStorageService storageService;
    private final ServiceLookup services;
    private final FileFilter filter;

    /**
     * Initializes a new {@link FileCacheCleanUp}.
     *
     * @param storageService The storage service
     * @param services The service look-up
     */
    public FileCacheCleanUp(RdbCompositionSpaceStorageService storageService, ServiceLookup services) {
        super();
        this.storageService = storageService;
        this.services = services;
        filter = file -> file.getName().startsWith(FILE_NAME_PREFIX);
    }

    /**
     * Cleans-up the directory containing files carrying cached content.
     *
     * @throws OXException If clean-up fails
     */
    public void doCleanUp() throws OXException {
        UploadDirService uploadDirService = services.getOptionalService(UploadDirService.class);
        if (uploadDirService == null) {
            return;
        }

        File[] files = uploadDirService.getUploadDir().listFiles(filter);
        if (files == null) {
            return;
        }

        for (File file : files) {
            Optional<CompositionSpaceInfo> optionalInfo = parseCompositionSpaceInfoFrom(file.getName());
            if (optionalInfo.isPresent()) {
                CompositionSpaceInfo info = optionalInfo.get();
                try {
                    CompositionSpaceDbStorage dbStorage = storageService.newDbStorageFor(info.userId, info.contextId);
                    if (false == dbStorage.exists(info.compositionSpaceId)) {
                        deleteFileSafe(file);
                    }
                } catch (OXException e) {
                    if (ContextExceptionCodes.NOT_FOUND.equals(e)) {
                        // Context doesn't exist anymore and the cached content can be deleted for this composition space
                        deleteFileSafe(file);
                        continue;
                    }
                    throw e;
                }
            }
        }
    }

    private static Optional<CompositionSpaceInfo> parseCompositionSpaceInfoFrom(String fileName) {
        try {
            int dashPos = fileName.indexOf('-', FILE_NAME_PREFIX.length());

            String unformattedUuid = fileName.substring(FILE_NAME_PREFIX.length(), dashPos);
            UUID compositionSpaceId = UUIDs.fromUnformattedString(unformattedUuid);

            int fromPos = dashPos + 1;
            dashPos = fileName.indexOf('-', fromPos);
            String sUserId = fileName.substring(fromPos, dashPos);
            int userId = Integer.parseInt(sUserId);

            fromPos = dashPos + 1;
            dashPos = fileName.indexOf('.', fromPos);
            String sContextId = fileName.substring(fromPos, dashPos);
            int contextId = Integer.parseInt(sContextId);

            return Optional.of(new CompositionSpaceInfo(compositionSpaceId, userId, contextId));
        } catch (Exception e) {
            LoggerHolder.LOG.error("Failed to parse composition space identifier from file name: {}", fileName, e);
            return Optional.empty();
        }
    }

    private static void deleteFileSafe(File file) {
        if (file == null) {
            return;
        }

        try {
            boolean deleted = file.delete();
            if (false == deleted) {
                LoggerHolder.LOG.error("Failed to delete cached content held by file {}", file);
            }
        } catch (Exception e) {
            LoggerHolder.LOG.error("Failed to delete cached content held by file {}", file, e);
        }
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private static class CompositionSpaceInfo {

        final UUID compositionSpaceId;
        final int userId;
        final int contextId;

        CompositionSpaceInfo(UUID compositionSpaceId, int userId, int contextId) {
            super();
            this.compositionSpaceId = compositionSpaceId;
            this.userId = userId;
            this.contextId = contextId;
        }
    }

}
