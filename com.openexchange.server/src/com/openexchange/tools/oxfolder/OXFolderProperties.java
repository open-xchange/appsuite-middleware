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

package com.openexchange.tools.oxfolder;

import java.util.concurrent.atomic.AtomicBoolean;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import com.openexchange.cache.impl.FolderCacheManager;
import com.openexchange.cache.impl.FolderQueryCacheManager;
import com.openexchange.cache.registry.CacheAvailabilityListener;
import com.openexchange.cache.registry.CacheAvailabilityRegistry;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.server.Initialization;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.oxfolder.memory.ConditionTreeMapManagement;

/**
 * <tt>OXFolderProperties</tt> contains both folder properties and folder cache properties
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class OXFolderProperties implements Initialization, CacheAvailabilityListener {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(OXFolderProperties.class);

    private static OXFolderProperties instance = new OXFolderProperties();

    /**
     * @return The singleton instance of {@link OXFolderProperties}
     */
    public static OXFolderProperties getInstance() {
        return instance;
    }

    /*
     * Fields
     */
    private final AtomicBoolean started = new AtomicBoolean();

    private boolean enableDBGrouping = true;

    private boolean enableFolderCache = true;

    /* private boolean ignoreSharedAddressbook = false; */ // finally dropped

    volatile boolean enableInternalUsersEdit = true;

    private boolean enableSharedFolderCaching = true;

    private OXFolderProperties() {
        super();
    }

    @Override
    public void start() throws OXException {
        if (!started.compareAndSet(false, true)) {
            LOG.error("Folder properties have already been started", new Throwable());
            return;
        }
        init();
        final CacheAvailabilityRegistry reg = CacheAvailabilityRegistry.getInstance();
        if (reg != null) {
            reg.registerListener(this);
        }
        if (enableFolderCache) {
            FolderCacheManager.initInstance();
        }
        FolderQueryCacheManager.initInstance();
        ConditionTreeMapManagement.startInstance();
    }

    @Override
    public void stop() throws OXException {
        if (!started.compareAndSet(true, false)) {
            LOG.error("Folder properties cannot be stopped since they have not been started before", new Throwable());
            return;
        }
        final CacheAvailabilityRegistry reg = CacheAvailabilityRegistry.getInstance();
        if (reg != null) {
            reg.unregisterListener(this);
        }
        ConditionTreeMapManagement.stopInstance();
        FolderCacheManager.releaseInstance();
        FolderQueryCacheManager.releaseInstance();
        reset();
    }

    @Override
    public void handleAvailability() throws OXException {
        final FolderCacheManager fcm = FolderCacheManager.getInstance();
        if (null != fcm) {
            fcm.initCache();
        }
        final FolderQueryCacheManager fqcm = FolderQueryCacheManager.getInstance();
        if (null != fqcm) {
            fqcm.initCache();
        }
    }

    @Override
    public void handleAbsence() throws OXException {
        final FolderCacheManager fcm = FolderCacheManager.getInstance();
        if (null != fcm) {
            fcm.releaseCache();
        }
        final FolderQueryCacheManager fqcm = FolderQueryCacheManager.getInstance();
        if (null != fqcm) {
            fqcm.releaseCache();
        }
    }

    private void reset() {
        enableSharedFolderCaching = true;
        enableDBGrouping = true;
        enableFolderCache = true;
        /* ignoreSharedAddressbook = false; */
        enableInternalUsersEdit = true;
    }

    private void init() {
        final ConfigurationService configurationService = ServerServiceRegistry.getInstance().getService(ConfigurationService.class);
        if (configurationService == null) {
            LOG.error("Cannot look-up configuration service");
            return;
        }
        /*
         * ENABLE_SHARED_FOLDER_CACHING
         */
        String value = configurationService.getProperty("ENABLE_SHARED_FOLDER_CACHING");
        if (null != value) {
            enableSharedFolderCaching = Boolean.parseBoolean(value.trim());
        }
        /*
         * ENABLE_DB_GROUPING
         */
        value = configurationService.getProperty("ENABLE_DB_GROUPING");
        if (null == value) {
            LOG.warn("Missing property ENABLE_DB_GROUPING.");
        } else {
            enableDBGrouping = Boolean.parseBoolean(value.trim());
        }
        /*
         * ENABLE_FOLDER_CACHE
         */
        value = configurationService.getProperty("ENABLE_FOLDER_CACHE");
        if (null == value) {
            LOG.warn("Missing property ENABLE_FOLDER_CACHE");
        } else {
            enableFolderCache = Boolean.parseBoolean(value.trim());
        }
        /*-
         * IGNORE_SHARED_ADDRESSBOOK
        value = configurationService.getProperty("IGNORE_SHARED_ADDRESSBOOK");
        if (null == value) {
            LOG.warn("Missing property IGNORE_SHARED_ADDRESSBOOK");
        } else {
            ignoreSharedAddressbook = Boolean.parseBoolean(value.trim());
        }
        */
        /*
         * ENABLE_INTERNAL_USER_EDIT
         */
        value = configurationService.getProperty("ENABLE_INTERNAL_USER_EDIT");
        if (null == value) {
            LOG.warn("Missing property ENABLE_INTERNAL_USER_EDIT");
        } else {
            final boolean enableInternalUsersEdit = Boolean.parseBoolean(value.trim());
            this.enableInternalUsersEdit = enableInternalUsersEdit;
        }
    }

    private static final String WARN_FOLDER_PROPERTIES_INIT = "Folder properties have not been started.";

    /**
     * @return <code>true</code> if database grouping is enabled ( <code>GROUB BY</code>); otherwise <code>false</code>
     */
    public static boolean isEnableDBGrouping() {
        if (!instance.started.get()) {
            LOG.error(WARN_FOLDER_PROPERTIES_INIT, new Throwable());
        }
        return instance.enableDBGrouping;
    }

    /**
     * @return <code>true</code> if folder cache is enabled; otherwise <code>false</code>
     */
    public static boolean isEnableFolderCache() {
        if (!instance.started.get()) {
            LOG.error(WARN_FOLDER_PROPERTIES_INIT, new Throwable());
        }
        return instance.enableFolderCache;
    }

    /*-
     *
     * @return <code>true</code> if shared address book should be omitted in folder tree display; otherwise <code>false</code>
    public static boolean isIgnoreSharedAddressbook() {
        if (!instance.started.get()) {
            LOG.error(WARN_FOLDER_PROPERTIES_INIT, new Throwable());
        }
        return instance.ignoreSharedAddressbook;
    }
    */

    /**
     * Context's system folder "<code>Global address book</code>" is created with write permission set to
     * {@link OCLPermission#WRITE_OWN_OBJECTS} if this property is set to <code>true</code>
     *
     * @return <code>true</code> if contacts located in global address book may be edited; otherwise <code>false</code>
     */
    public static boolean isEnableInternalUsersEdit() {
        if (!instance.started.get()) {
            LOG.error(WARN_FOLDER_PROPERTIES_INIT, new Throwable());
        }
        return instance.enableInternalUsersEdit;
    }

    /**
     * Checks whether caching for shared folders is enabled or not.
     *
     * @return <code>true</code> if caching for shared folder is enabled; otherwise <code>false</code>
     */
    public static boolean isEnableSharedFolderCaching() {
        if (!instance.started.get()) {
            LOG.error(WARN_FOLDER_PROPERTIES_INIT, new Throwable());
        }
        return instance.enableSharedFolderCaching;
    }

    /*-
     * ############################# MBEAN STUFF #############################
     */

    /**
     * Creates an appropriate instance of {@link ObjectName} from specified class name and domain name.
     *
     * @param className The class name to use as object name
     * @param domain The domain name
     * @return An appropriate instance of {@link ObjectName}
     * @throws MalformedObjectNameException If instantiation of {@link ObjectName} fails
     */
    public static ObjectName getObjectName(final String className, final String domain) throws MalformedObjectNameException {
        final int pos = className.lastIndexOf('.');
        return new ObjectName(domain, "name", pos == -1 ? className : className.substring(pos + 1));
    }
}
