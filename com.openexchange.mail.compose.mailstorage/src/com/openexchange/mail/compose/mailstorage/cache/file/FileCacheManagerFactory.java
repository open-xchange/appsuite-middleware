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

package com.openexchange.mail.compose.mailstorage.cache.file;

import java.io.File;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.Interests;
import com.openexchange.config.Reloadable;
import com.openexchange.config.Reloadables;
import com.openexchange.exception.OXException;
import com.openexchange.mail.compose.mailstorage.MailStorageCompositionSpaceConfig;
import com.openexchange.mail.compose.mailstorage.cache.CacheManager;
import com.openexchange.mail.compose.mailstorage.cache.CacheManagerFactory;
import com.openexchange.server.ServiceLookup;

/**
 * {@link FileCacheManagerFactory}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.5
 */
public class FileCacheManagerFactory implements Reloadable, CacheManagerFactory {

    /** Simple class to delay initialization until needed */
    private static class LoggerHolder {
        static final Logger LOG = org.slf4j.LoggerFactory.getLogger(FileCacheManagerFactory.class);
    }

    private final AtomicReference<CacheManager> currentlyActiveCacheManager;
    private final ConcurrentMap<File, FileCacheManager> cacheManagers;
    private final ServiceLookup services;

    /**
     * Initializes a new {@link FileCacheManagerFactory}.
     *
     * @param services The service look-up
     */
    public FileCacheManagerFactory(ServiceLookup services) {
        super();
        this.services = services;
        cacheManagers = new ConcurrentHashMap<>(4, 0.9F, 1);
        currentlyActiveCacheManager = new AtomicReference<>(null);
    }

    /**
     * Gets the currently applicable cache manager.
     *
     * @return The cache manager
     * @throws OXException If cache manager cannot be returned
     */
    @Override
    public CacheManager getCacheManager() throws OXException {
        CacheManager cacheManager = currentlyActiveCacheManager.get();
        if (cacheManager == null) {
            synchronized (this) {
                cacheManager = currentlyActiveCacheManager.get();
                if (cacheManager == null) {
                    cacheManager = doGetFileCacheManager();
                    currentlyActiveCacheManager.set(cacheManager);
                }
            }
        }
        return cacheManager;
    }

    private CacheManager doGetFileCacheManager() throws OXException {
        if (!MailStorageCompositionSpaceConfig.getInstance().isFileCacheEnabled()) {
            // File cache disabled as per configuration
            return DisabledCacheManager.getInstance();
        }

        File cacheDir = MailStorageCompositionSpaceConfig.getInstance().getFileCacheDirectory();
        FileCacheManager fileCacheManager = cacheManagers.get(cacheDir);
        if (fileCacheManager == null) {
            FileCacheManager newFileCacheManager = new FileCacheManager(cacheDir, services);
            fileCacheManager = cacheManagers.putIfAbsent(cacheDir, newFileCacheManager);
            if (fileCacheManager == null) {
                newFileCacheManager.startUp();
                fileCacheManager = newFileCacheManager;
            } else {
                fileCacheManager.awaitStartUp();
            }
        } else {
            fileCacheManager.awaitStartUp();
        }
        return fileCacheManager;
    }

    /**
     * Shuts-down this factory.
     */
    public void shutDown() {
        for (FileCacheManager fileCacheManager : cacheManagers.values()) {
            fileCacheManager.shutDown();
        }
        cacheManagers.clear();
    }

    @Override
    public synchronized void reloadConfiguration(ConfigurationService configService) {
        CacheManager current = currentlyActiveCacheManager.get();
        if (current == null) {
            return;
        }

        boolean enabled = configService.getBoolProperty(MailStorageCompositionSpaceConfig.PROPERTY_FILE_CACHE_ENABLED, false);
        if (enabled) {
            if (DisabledCacheManager.getInstance() == current) {
                // Switched from disabled to enabled
                currentlyActiveCacheManager.set(null);
                return;
            }
        } else {
            if (DisabledCacheManager.getInstance() != current) {
                // Switched from enabled to disabled
                currentlyActiveCacheManager.set(null);
                ((FileCacheManager) current).markDeprecated();
                return;
            }
        }

        if (DisabledCacheManager.getInstance() != current) {
            FileCacheManager currentFileCacheManager = (FileCacheManager) current;

            try {
                File currentCacheDir = MailStorageCompositionSpaceConfig.getInstance().getFileCacheDirectory(Optional.of(configService));
                if (!currentCacheDir.equals(currentFileCacheManager.getCacheDir())) {
                    // Cache directory changed
                    currentlyActiveCacheManager.set(null);
                    currentFileCacheManager.markDeprecated();
                    return;
                }
            } catch (Exception e) {
                LoggerHolder.LOG.error("Failed to apply file cache directory", e);
            }

            // Otherwise propagate possibly changed max. idle seconds
            long maxIdleSeconds = configService.getIntProperty(MailStorageCompositionSpaceConfig.PROPERTY_FILE_CACHE_MAX_IDLE_SECONDS, 300);
            try {
                currentFileCacheManager.reinitCleanUpTaskWith(maxIdleSeconds);
            } catch (Exception e) {
                LoggerHolder.LOG.error("Failed to re-initialize clean-up task for file cache using directory: {}", currentFileCacheManager.getCacheDir().getAbsolutePath(), e);
            }
        }
    }

    @Override
    public Interests getInterests() {
        return Reloadables.interestsForProperties(
            MailStorageCompositionSpaceConfig.PROPERTY_FILE_CACHE_ENABLED,
            MailStorageCompositionSpaceConfig.PROPERTY_FILE_CACHE_DIR,
            MailStorageCompositionSpaceConfig.PROPERTY_FILE_CACHE_MAX_IDLE_SECONDS);
    }

}
