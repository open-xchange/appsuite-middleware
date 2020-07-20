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

package com.openexchange.mail.compose.mailstorage;

import java.io.File;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import com.openexchange.ajax.container.ThresholdFileHolder;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.ForcedReloadable;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.config.cascade.ConfigViews;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceLookup;
import com.openexchange.uploaddir.UploadDirService;

/**
 * {@link MailStorageCompositionSpaceConfig} - Configuration for mail storage backed composition spaces.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.5
 */
public class MailStorageCompositionSpaceConfig implements ForcedReloadable {

    /** Simple class to delay initialization until needed */
    private static class LoggerHolder {
        static final Logger LOG = org.slf4j.LoggerFactory.getLogger(MailStorageCompositionSpaceConfig.class);
    }

    private static final AtomicReference<MailStorageCompositionSpaceConfig> INSTANCE_REFERENCE = new AtomicReference<>(null);

    /**
     * Initializes the instance.
     *
     * @param services The service look-up to use
     * @return The newly created instance
     */
    public static MailStorageCompositionSpaceConfig initInstance(ServiceLookup services) {
        MailStorageCompositionSpaceConfig instance = new MailStorageCompositionSpaceConfig(services);
        INSTANCE_REFERENCE.set(instance);
        return instance;
    }

    /**
     * Drops the instance.
     */
    public static void dropInstance() {
        INSTANCE_REFERENCE.set(null);
    }

    /**
     * Gets the instance.
     *
     * @return The instance
     * @throws OXException If not yet initialized
     */
    public static MailStorageCompositionSpaceConfig getInstance() throws OXException {
        MailStorageCompositionSpaceConfig config = INSTANCE_REFERENCE.get();
        if (config == null) {
            throw OXException.general("Bundle \"com.openexchange.mail.compose.mailstorage\" not yet initialized");
        }
        return config;
    }

    /**
     * Gets the optional instance.
     *
     * @return The instance or empty if not yet initialized
     */
    public static Optional<MailStorageCompositionSpaceConfig> optInstance() {
        return Optional.ofNullable(INSTANCE_REFERENCE.get());
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    public static final String PROPERTY_ENABLED = "com.openexchange.mail.compose.mailstorage.enabled";

    public static final String PROPERTY_INMEMORY_THRESHOLD = "com.openexchange.mail.compose.mailstorage.inMemoryThreshold";

    public static final String PROPERTY_SPOOL_DIR = "com.openexchange.mail.compose.mailstorage.spoolDir";

    public static final String PROPERTY_FILE_CACHE_ENABLED = "com.openexchange.mail.compose.mailstorage.fileCacheEnabled";

    public static final String PROPERTY_FILE_CACHE_DIR = "com.openexchange.mail.compose.mailstorage.fileCachelDir";

    public static final String PROPERTY_FILE_CACHE_MAX_IDLE_SECONDS = "com.openexchange.mail.compose.mailstorage.fileCacheMaxIdleSeconds";

    public static final String PROPERTY_IN_MEMORY_CACHE_MAX_IDLE_SECONDS = "com.openexchange.mail.compose.mailstorage.inMemoryCacheMaxIdleSeconds";

    public static final String PROPERTY_EAGER_UPLOAD_CHECKS_ENABLED = "com.openexchange.mail.compose.mailstorage.eagerUploadChecksEnabled";

    // -------------------------------------------------------------------------------------------------------------------------------------

    private final ServiceLookup services;
    private volatile File spoolDir = null;
    private volatile File fileCacheDir = null;

    /**
     * Initializes a new {@link MailStorageCompositionSpaceConfig}.
     *
     * @param services The service look-up
     */
    private MailStorageCompositionSpaceConfig(ServiceLookup services) {
        super();
        this.services = services;
    }

    /**
     * Checks if mail storage backed composition spaces are enabled/available for given user.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return <code>true</code> if enabled/available; otherwise <code>false</code>
     * @throws OXException If availability check fails
     */
    public boolean isEnabled(int userId, int contextId) throws OXException {
        String propertyName = PROPERTY_ENABLED;
        ConfigViewFactory viewFactory = services.getServiceSafe(ConfigViewFactory.class);
        ConfigView view = viewFactory.getView(userId, contextId);
        return ConfigViews.getDefinedBoolPropertyFrom(propertyName, true, view);
    }

    /**
     * Gets the in-memory threshold for composition space data. Messages exceeding that threshold are spooled to disk.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The in-memory threshold in bytes
     * @throws OXException If the in-memory threshold cannot be returned
     */
    public int getInMemoryThreshold(int userId, int contextId) throws OXException {
        String propertyName = PROPERTY_INMEMORY_THRESHOLD;
        ConfigViewFactory viewFactory = services.getServiceSafe(ConfigViewFactory.class);
        ConfigView view = viewFactory.getView(userId, contextId);
        return ConfigViews.getDefinedIntPropertyFrom(propertyName, ThresholdFileHolder.DEFAULT_IN_MEMORY_THRESHOLD, view);
    }

    /**
     * Gets the dedicated spool directory used for temporary composition space data that is too big to be held in memory.
     *
     * @throws OXException If spool directory cannot be returned
     */
    public File getSpoolDirectory() throws OXException {
        return getSpoolDirectory(Optional.empty());
    }

    /**
     * Gets the dedicated spool directory used for temporary composition space data that is too big to be held in memory.
     *
     * @param optionalConfig The optional configuration service
     * @throws OXException If spool directory cannot be returned
     */
    public File getSpoolDirectory(Optional<ConfigurationService> optionalConfig) throws OXException {
        File spoolDir = this.spoolDir;
        if (spoolDir == null) {
            ConfigurationService config = optionalConfig.isPresent() ? optionalConfig.get() : services.getServiceSafe(ConfigurationService.class);
            String spoolDirPath = config.getProperty(PROPERTY_SPOOL_DIR);
            if (Strings.isEmpty(spoolDirPath)) {
                UploadDirService uploadDirService = services.getServiceSafe(UploadDirService.class);
                spoolDir = uploadDirService.getUploadDir();
            } else {
                spoolDir = new File(spoolDirPath);
                if (!spoolDir.exists()) {
                    if (!spoolDir.mkdirs()) {
                        throw OXException.general("Spool directory " + spoolDir + " does not exist and cannot be created.");
                    }
                    LoggerHolder.LOG.info("Spool directory {} configured through {} did not exist, but could be created.", spoolDir, PROPERTY_SPOOL_DIR);
                }
                if (!spoolDir.isDirectory()) {
                    throw OXException.general(spoolDir + " is not a directory.");
                }
            }

            this.spoolDir = spoolDir;
        }

        return spoolDir;
    }

    /**
     * Checks whether the local file cache is enabled.
     *
     * @param optionalConfig The optional configuration service
     * @return <code>true</code> if cache is enabled; otherwise <code>false</code>
     * @throws OXException If according configuration setting cannot be examined
     */
    public boolean isFileCacheEnabled() throws OXException {
        ConfigurationService config = services.getServiceSafe(ConfigurationService.class);
        return config.getBoolProperty(PROPERTY_FILE_CACHE_ENABLED, false);
    }

    /**
     * Gets the file cache directory.
     *
     * @throws OXException If file cache directory cannot be returned
     */
    public File getFileCacheDirectory() throws OXException {
        return getFileCacheDirectory(Optional.empty());
    }

    /**
     * Gets the file cache directory.
     *
     * @param optionalConfig The optional configuration service
     * @throws OXException If file cache directory cannot be returned
     */
    public File getFileCacheDirectory(Optional<ConfigurationService> optionalConfig) throws OXException {
        File fileCacheDir = this.fileCacheDir;
        if (fileCacheDir == null) {
            ConfigurationService config = optionalConfig.isPresent() ? optionalConfig.get() : services.getServiceSafe(ConfigurationService.class);
            String cacheDirPath = config.getProperty(PROPERTY_FILE_CACHE_DIR);
            if (Strings.isEmpty(cacheDirPath)) {
                // fall back to spool directory by default
                fileCacheDir = getSpoolDirectory(optionalConfig);
            } else {
                fileCacheDir = new File(cacheDirPath);
                if (!fileCacheDir.exists()) {
                    if (!fileCacheDir.mkdirs()) {
                        throw OXException.general("File cache directory " + fileCacheDir + " does not exist and cannot be created.");
                    }
                    LoggerHolder.LOG.info("File cache directory {} configured through {} did not exist, but could be created.", fileCacheDir, PROPERTY_FILE_CACHE_DIR);
                }
                if (!fileCacheDir.isDirectory()) {
                    throw OXException.general(fileCacheDir + " is not a directory.");
                }
            }

            this.fileCacheDir = fileCacheDir;
        }

        return fileCacheDir;
    }


    /**
     * Gets the max. time a MIME message file might stay in local cache without being read.
     *
     * @return The max. idle time in seconds
     * @throws OXException
     */
    public int getFileCacheMaxIdleSeconds() throws OXException {
        ConfigurationService config = services.getServiceSafe(ConfigurationService.class);
        return config.getIntProperty(PROPERTY_FILE_CACHE_MAX_IDLE_SECONDS, 300);
    }

    /**
     * Gets the max. time that metadata records about open composition spaces might stay in an in-memory cache without being accessed.
     *
     * @return The max. idle time in seconds
     * @throws OXException
     */
    public long getInMemoryCacheMaxIdleSeconds() throws OXException {
        ConfigurationService config = services.getServiceSafe(ConfigurationService.class);
        return config.getIntProperty(PROPERTY_IN_MEMORY_CACHE_MAX_IDLE_SECONDS, 3600);
    }

    /**
     * Gets whether eager upload checks for max. mail size and quota are enabled. With enabled checks, incoming requests that
     * violate these (for example by exceeding it with an attachment upload) are rejected with an according error
     * response before the request content was fully consumed and thus allowing to fail early and not forcing the client to upload
     * large contents that would lead to limit restriction errors anyway.
     * @throws OXException
     */
    public boolean isEagerUploadChecksEnabled() throws OXException {
        ConfigurationService config = services.getServiceSafe(ConfigurationService.class);
        return config.getBoolProperty(PROPERTY_EAGER_UPLOAD_CHECKS_ENABLED, false);
    }

    // --------------------------------------------------- Reloadable stuff ----------------------------------------------------------------

    @Override
    public void reloadConfiguration(ConfigurationService configService) {
        this.spoolDir = null;
        this.fileCacheDir = null;
    }

}
