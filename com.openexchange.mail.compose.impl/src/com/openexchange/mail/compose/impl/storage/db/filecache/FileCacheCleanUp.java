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
