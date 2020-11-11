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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import com.openexchange.exception.OXException;
import com.openexchange.java.Streams;
import com.openexchange.java.util.UUIDs;
import com.openexchange.mail.compose.impl.storage.db.RdbCompositionSpaceStorageService;
import com.openexchange.server.ServiceLookup;
import com.openexchange.timer.ScheduledTimerTask;
import com.openexchange.timer.TimerService;


/**
 * {@link FileCacheImpl} - The default file cache implementation.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.5
 */
public class FileCacheImpl implements FileCache {

    /** Simple class to delay initialization until needed */
    private static class LoggerHolder {
        static final Logger LOG = org.slf4j.LoggerFactory.getLogger(FileCacheImpl.class);
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private final ServiceLookup services;
    private final LocationSelector locationSelector;
    private volatile ScheduledTimerTask timerTask;

    /**
     * Initializes a new {@link FileCacheImpl}.
     *
     * @param storageService The storage service
     * @param services The service look-up
     * @throws OXException If needed service is absent
     */
    public FileCacheImpl(RdbCompositionSpaceStorageService storageService, ServiceLookup services) throws OXException {
        super();
        this.services = services;
        locationSelector = new LocationSelector(services);
        Runnable task = new Runnable() {

            @Override
            public void run() {
                try {
                    new FileCacheCleanUp(storageService, services).doCleanUp();
                } catch (Exception e) {
                    LoggerHolder.LOG.error("Failed file cache clean-up run", e);
                }
            }
        };
        timerTask = services.getServiceSafe(TimerService.class).scheduleWithFixedDelay(task, 300000, 3600000);
    }

    @Override
    public void signalStop() throws OXException {
        ScheduledTimerTask timerTask = this.timerTask;
        if (timerTask != null) {
            this.timerTask = null;
            timerTask.cancel();
            TimerService timerService = services.getOptionalService(TimerService.class);
            if (timerService != null) {
                timerService.purge();
            }
        }
    }

    @Override
    public Optional<String> getCachedContent(UUID compositionSpaceId, int userId, int contextId) throws OXException {
        Optional<File> optionalFile = locationSelector.getFileFor(compositionSpaceId, userId, contextId);
        if (!optionalFile.isPresent()) {
            return Optional.empty();
        }

        File file = optionalFile.get();
        if (!file.exists() || !file.isFile() || !file.canRead()) {
            return Optional.empty();
        }

        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            return Optional.of(Streams.stream2string(fis, "UTF-8"));
        } catch (IOException e) {
            // Failed to read cached content
            LoggerHolder.LOG.error("Failed to read cached content", e);
            deleteFileSafe(file);
        } finally {
            Streams.close(fis);
        }
        return Optional.empty();
    }

    @Override
    public boolean storeCachedContent(String content, UUID compositionSpaceId, int userId, int contextId) throws OXException {
        Optional<File> optionalDir = locationSelector.getLocation(userId, contextId);
        if (!optionalDir.isPresent()) {
            LoggerHolder.LOG.error("Missing location");
            return false;
        }

        File file = null;
        FileOutputStream fos = null;
        try {
            file = new File(optionalDir.get(), buildFileName(compositionSpaceId, userId, contextId));

            if (!file.exists()) {
                boolean created = file.createNewFile();
                if (false == created) {
                    LoggerHolder.LOG.error("Failed to create file {}", file);
                    return false;
                }
                file.deleteOnExit();
            }

            fos = new FileOutputStream(file, false);
            fos.write(content.getBytes(StandardCharsets.UTF_8));
            fos.flush();
            return true;
        } catch (Exception e) {
            // Failed to read cached content
            LoggerHolder.LOG.error("Failed to write cached content", e);
            deleteFileSafe(file);
        } finally {
            Streams.close(fos);
        }
        return false;
    }

    @Override
    public void deleteCachedContent(UUID compositionSpaceId, int userId, int contextId) throws OXException {
        locationSelector.deleteFileFor(compositionSpaceId, userId, contextId);
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    /**
     * Builds the file name for given arguments.
     *
     * @param compositionSpaceId The composition space identifier
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The file name
     */
    static String buildFileName(UUID compositionSpaceId, int userId, int contextId) {
        return new StringBuilder(FILE_NAME_PREFIX).append(UUIDs.getUnformattedString(compositionSpaceId)).append('-').append(userId).append('-').append(contextId).append(".tmp").toString();
    }

    /**
     * (Safely) Deletes given file.
     *
     * @param file The file to delete
     */
    static void deleteFileSafe(File file) {
        if (file == null) {
            return;
        }

        try {
            if (false == file.delete()) {
                LoggerHolder.LOG.error("Failed to delete cached content held by file {}", file);
            }
        } catch (Exception e) {
            LoggerHolder.LOG.error("Failed to delete cached content held by file {}", file, e);
        }
    }

}
