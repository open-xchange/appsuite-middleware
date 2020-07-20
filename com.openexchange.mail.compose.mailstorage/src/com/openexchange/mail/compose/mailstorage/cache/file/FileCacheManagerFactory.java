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
